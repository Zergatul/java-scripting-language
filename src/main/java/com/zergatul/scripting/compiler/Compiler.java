package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.*;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.generator.BinderTreeGenerator;
import com.zergatul.scripting.generator.StateBoundary;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.parser.AssignmentOperator;
import com.zergatul.scripting.binding.nodes.BoundNodeType;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.runtime.AsyncStateMachine;
import com.zergatul.scripting.runtime.AsyncStateMachineException;
import com.zergatul.scripting.runtime.RuntimeType;
import com.zergatul.scripting.runtime.RuntimeTypes;
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.*;
import com.zergatul.scripting.visitors.ExternalParameterVisitor;
import com.zergatul.scripting.visitors.LiftedVariablesVisitor;
import com.zergatul.scripting.visitors.LocalParameterVisitor;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.objectweb.asm.Opcodes.*;

public class Compiler {

    private final CompilationParameters parameters;

    public Compiler(CompilationParameters parameters) {
        this.parameters = parameters;
    }

    public CompilationResult compile(String code) {
        LexerInput lexerInput = new LexerInput(code);
        Lexer lexer = new Lexer(lexerInput);
        LexerOutput lexerOutput = lexer.lex();

        Parser parser = new Parser(lexerOutput);
        ParserOutput parserOutput = parser.parse();

        Binder binder = new Binder(parserOutput, parameters);
        BinderOutput binderOutput = binder.bind();

        if (binderOutput.diagnostics().isEmpty()) {
            return CompilationResult.success(compileUnit(binderOutput));
        } else {
            return CompilationResult.failed(binderOutput.diagnostics());
        }
    }

    private <T> T compileUnit(BinderOutput output) {
        BoundCompilationUnitNode unit = output.unit();

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        emitSourceFile(writer);
        String name = "com/zergatul/scripting/dynamic/" + parameters.getMainClassName();
        writer.visit(
                V1_8,
                ACC_PUBLIC,
                name,
                null,
                Type.getInternalName(Object.class),
                new String[] { Type.getInternalName(parameters.getFunctionalInterface()) });

        CompilerContext context = parameters.getContext();
        context.setClassLoaderContext(new ClassLoaderContext());
        context.setClassName(name);

        context.copyGenericFunctionsFrom(output.context());

        compileCompilationUnitMembers(unit, writer, context);
        buildEmptyConstructor(writer);
        buildMainMethod(unit, writer, name, context);

        writer.visitEnd();

        compileMethodHandleCache(context);

        byte[] bytecode = writer.toByteArray();
        saveClassFile(name, bytecode);

        @SuppressWarnings("unchecked")
        Class<T> dynamic = (Class<T>) context.defineClass(name.replace('/', '.'), bytecode);
        return createInstance(dynamic);
    }

    private <T> T createInstance(Class<T> dynamic) {
        Constructor<T> constructor;
        try {
            constructor = dynamic.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new InternalException();
        }

        T instance;
        try {
            instance = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new InternalException();
        }

        return instance;
    }

    private void compileCompilationUnitMembers(BoundCompilationUnitNode unit, ClassWriter writer, CompilerContext context) {
        List<BoundStaticVariableNode> fields = new ArrayList<>();
        List<BoundFunctionDeclarationNode> functions = new ArrayList<>();
        List<BoundClassNode> classes = new ArrayList<>();
        List<BoundExtensionNode> extensions = new ArrayList<>();
        for (BoundCompilationUnitMemberNode member : unit.members.members) {
            switch (member.getNodeType()) {
                case STATIC_VARIABLE -> fields.add((BoundStaticVariableNode) member);
                case FUNCTION_DECLARATION -> functions.add((BoundFunctionDeclarationNode) member);
                case CLASS_DECLARATION -> classes.add((BoundClassNode) member);
                case EXTENSION_DECLARATION -> extensions.add((BoundExtensionNode) member);
                case TYPE_ALIAS -> {}
                default -> throw new InternalException();
            }
        }

        // have to be compiled first, because JVM requires parent classes/interfaces to be defined before defining child
        compileGenericFunctions(context.getGenericFunctions(), context);
        compileClasses(classes, writer, context);
        compileExtensions(extensions, writer, context);
        compileStaticVariables(fields, writer, context);
        compileFunctions(functions, writer, context);
    }

    private void compileClasses(List<BoundClassNode> classNodes, ClassWriter writer, CompilerContext context) {
        sortClassNodes(classNodes);

        // setup class names in advance for forward references
        for (BoundClassNode classNode : classNodes) {
            String name = context.getClassName() + "$" + classNode.name.value;
            classNode.getDeclaredType().setInternalName(name);
        }

        for (BoundClassNode classNode : classNodes) {
            SDeclaredType declaredType = classNode.getDeclaredType();
            String name = declaredType.getInternalName();

            writer.visitInnerClass(
                    name,
                    context.getClassName(),
                    classNode.name.value,
                    ACC_PUBLIC | ACC_STATIC);

            ClassWriter innerWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            innerWriter.visit(
                    V1_8,
                    ACC_PUBLIC,
                    name,
                    null,
                    declaredType.getBaseType().getInternalName(),
                    null);

            AnnotationVisitor annotationVisitor = innerWriter.visitAnnotation(Type.getDescriptor(CustomType.class), true);
            annotationVisitor.visit("name", classNode.name.value);
            annotationVisitor.visitEnd();

            innerWriter.visitInnerClass(
                    name,
                    context.getClassName(),
                    classNode.name.value,
                    ACC_PUBLIC | ACC_STATIC);

            CompilerContext classContext = context.createClass(classNode.getDeclaredType());
            for (BoundClassMemberNode member : classNode.members) {
                compileClassMember(innerWriter, member, classContext);
            }

            // add default constructor
            if (classNode.defaultBaseConstructor != null) {
                MethodVisitor constructorVisitor = innerWriter.visitMethod(ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
                constructorVisitor.visitCode();
                constructorVisitor.visitVarInsn(ALOAD, 0);
                classNode.defaultBaseConstructor.compileInvoke(constructorVisitor);
                constructorVisitor.visitInsn(RETURN);
                constructorVisitor.visitMaxs(0, 0);
                constructorVisitor.visitEnd();
            }

            innerWriter.visitEnd();

            byte[] bytecode = innerWriter.toByteArray();
            saveClassFile(name, bytecode);

            Class<?> innerClass = context.defineClass(name.replace('/', '.'), bytecode);
            classNode.getDeclaredType().setJavaClass(innerClass);
        }
    }

    private void sortClassNodes(List<BoundClassNode> classNodes) {
        // sort classes so base classes always come first
        classNodes.sort(Comparator.comparingInt(classNode -> classNode.getDeclaredType().getInheritanceDepth()));
    }

    private void compileClassMember(ClassWriter writer, BoundClassMemberNode member, CompilerContext context) {
        switch (member.getNodeType()) {
            case CLASS_FIELD -> compileClassField(writer, (BoundClassFieldNode) member);
            case CLASS_CONSTRUCTOR -> compileClassConstructor(writer, (BoundClassConstructorNode) member, context);
            case CLASS_METHOD -> compileClassMethod(writer, (BoundClassMethodNode) member, context);
            case CLASS_UNARY_OPERATION -> compileClassUnaryOperation(writer, (BoundClassUnaryOperationNode) member, context);
            case CLASS_BINARY_OPERATION -> compileClassBinaryOperation(writer, (BoundClassBinaryOperationNode) member, context);
            default -> throw new InternalException();
        }
    }

    private void compileClassField(ClassWriter writer, BoundClassFieldNode field) {
        writer.visitField(
                ACC_PUBLIC,
                field.name.value,
                field.typeNode.type.getDescriptor(),
                null, null);
    }

    private void compileClassConstructor(ClassWriter writer, BoundClassConstructorNode constructor, CompilerContext context) {
        MethodVisitor constructorVisitor = writer.visitMethod(ACC_PUBLIC, "<init>", constructor.functionType.getMethodDescriptor(), null, null);
        constructorVisitor.visitCode();

        context = context.createClassMethod(SVoidType.instance, false);
        for (BoundParameterNode parameter : constructor.parameters.parameters) {
            context.setStackIndex((LocalVariable) parameter.getName().getSymbolOrThrow());
        }

        // call other constructor
        constructorVisitor.visitVarInsn(ALOAD, 0);
        for (BoundExpressionNode expression : constructor.initializer.arguments.arguments) {
            compileExpression(constructorVisitor, context, expression);
        }
        constructor.initializer.constructor.compileInvoke(constructorVisitor);

        if (!constructor.lifted.isEmpty()) {
            compileClosureClass(constructorVisitor, context, constructor.lifted);
        }

        compileStatement(constructorVisitor, context, constructor.body);

        constructorVisitor.visitInsn(RETURN);
        constructorVisitor.visitMaxs(0, 0);
        constructorVisitor.visitEnd();
    }

    private void compileClassMethod(ClassWriter writer, BoundClassMethodNode methodNode, CompilerContext context) {
        int methodModifiers = ACC_PUBLIC;
        if (methodNode.syntaxNode.modifiers.isFinal()) {
            methodModifiers |= ACC_FINAL;
        }

        MethodVisitor methodVisitor = writer.visitMethod(methodModifiers, methodNode.name.value, methodNode.functionType.getMethodDescriptor(), null, null);
        methodVisitor.visitCode();

        context = context.createClassMethod(methodNode.functionType.getReturnType(), methodNode.isAsync());
        for (BoundParameterNode parameter : methodNode.parameters.parameters) {
            context.setStackIndex((LocalVariable) parameter.getName().getSymbolOrThrow());
        }

        if (methodNode.isAsync()) {
            compileAsyncBoundStatementList(methodVisitor, context, new BoundStatementsListNode(List.of(methodNode.body)));
        } else {
            if (!methodNode.lifted.isEmpty()) {
                compileClosureClass(methodVisitor, context, methodNode.lifted);
            }

            compileStatement(methodVisitor, context, methodNode.body);
            if (methodNode.functionType.getReturnType() == SVoidType.instance) {
                methodVisitor.visitInsn(RETURN);
            }
        }

        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void compileClassUnaryOperation(ClassWriter writer, BoundClassUnaryOperationNode unaryOperationNode, CompilerContext context) {
        int methodModifiers = ACC_PUBLIC | ACC_STATIC;

        MethodVisitor methodVisitor = writer.visitMethod(methodModifiers, unaryOperationNode.method.getName(), unaryOperationNode.functionType.getMethodDescriptor(), null, null);
        methodVisitor.visitCode();

        context = context.createClassStaticMethod(unaryOperationNode.functionType.getReturnType());
        for (BoundParameterNode parameter : unaryOperationNode.parameters.parameters) {
            context.setStackIndex((LocalVariable) parameter.getName().getSymbolOrThrow());
        }

        if (!unaryOperationNode.lifted.isEmpty()) {
            compileClosureClass(methodVisitor, context, unaryOperationNode.lifted);
        }

        compileStatement(methodVisitor, context, unaryOperationNode.body);
        if (unaryOperationNode.functionType.getReturnType() == SVoidType.instance) {
            methodVisitor.visitInsn(RETURN);
        }

        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void compileClassBinaryOperation(ClassWriter writer, BoundClassBinaryOperationNode binaryOperationNode, CompilerContext context) {
        int methodModifiers = ACC_PUBLIC | ACC_STATIC;

        MethodVisitor methodVisitor = writer.visitMethod(methodModifiers, binaryOperationNode.method.getName(), binaryOperationNode.functionType.getMethodDescriptor(), null, null);
        methodVisitor.visitCode();

        context = context.createClassStaticMethod(binaryOperationNode.functionType.getReturnType());
        for (BoundParameterNode parameter : binaryOperationNode.parameters.parameters) {
            context.setStackIndex((LocalVariable) parameter.getName().getSymbolOrThrow());
        }

        if (!binaryOperationNode.lifted.isEmpty()) {
            compileClosureClass(methodVisitor, context, binaryOperationNode.lifted);
        }

        compileStatement(methodVisitor, context, binaryOperationNode.body);
        if (binaryOperationNode.functionType.getReturnType() == SVoidType.instance) {
            methodVisitor.visitInsn(RETURN);
        }

        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void compileExtensions(List<BoundExtensionNode> extensions, ClassWriter writer, CompilerContext context) {
        for (BoundExtensionNode extension : extensions) {
            CompilerContext extensionContext = context.createExtension(extension.typeNode.type);
            for (BoundExtensionMemberNode memberNode : extension.members) {
                switch (memberNode.getNodeType()) {
                    case EXTENSION_METHOD -> compileExtensionMethod((BoundExtensionMethodNode) memberNode, writer, extensionContext);
                    case EXTENSION_UNARY_OPERATION -> compileExtensionUnaryOperation((BoundExtensionUnaryOperationNode) memberNode, writer, extensionContext);
                    case EXTENSION_BINARY_OPERATION -> compileExtensionBinaryOperation((BoundExtensionBinaryOperationNode) memberNode, writer, extensionContext);
                    default -> throw new InternalException();
                }
            }
        }
    }

    private void compileExtensionMethod(BoundExtensionMethodNode methodNode, ClassWriter writer, CompilerContext context) {
        MethodSymbol symbol = (MethodSymbol) methodNode.name.getSymbolOrThrow();
        SFunction type = (SFunction) symbol.getType();

        MethodVisitor visitor = writer.visitMethod(
                ACC_PUBLIC | ACC_STATIC,
                methodNode.method.getInternalName(),
                methodNode.method.getDescriptor(),
                null,
                null);
        visitor.visitCode();

        CompilerContext methodContext = context.createExtensionMethod(type.getReturnType(), false /*function.isAsync()*/);
        processContextStart(visitor, methodContext);

        LocalVariable thisParameter = methodContext.addLocalParameter("@this", methodNode.method.getOwner(), null);
        methodContext.setStackIndex(thisParameter);

        for (BoundParameterNode parameter : methodNode.parameters.parameters) {
            methodContext.setStackIndex((LocalVariable) parameter.getName().getSymbolOrThrow());
        }

        if (methodNode.isAsync()) {
            compileAsyncBoundStatementList(visitor, methodContext, new BoundStatementsListNode(List.of(methodNode.body)));
        } else {
            if (!methodNode.lifted.isEmpty()) {
                compileClosureClass(visitor, methodContext, methodNode.lifted);
            }

            compileStatement(visitor, methodContext, methodNode.body);
            if (type.getReturnType() == SVoidType.instance) {
                visitor.visitInsn(RETURN);
            }
        }

        processContextEnd(visitor, methodContext, methodNode.parameters.parameters.stream().map(p -> p.getName().symbolRef.asLocalVariable()).toList());
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();
    }

    private void compileExtensionUnaryOperation(BoundExtensionUnaryOperationNode operationNode, ClassWriter writer, CompilerContext context) {
        ExtensionUnaryOperation operation = (ExtensionUnaryOperation) operationNode.operation;

        MethodVisitor methodVisitor = writer.visitMethod(
                ACC_PUBLIC | ACC_STATIC,
                operation.getInternalName(),
                operation.getMethodDescriptor(),
                null, null);
        methodVisitor.visitCode();

        context = context.createClassStaticMethod(operation.getResultType());
        for (BoundParameterNode parameter : operationNode.parameters.parameters) {
            context.setStackIndex((LocalVariable) parameter.getName().getSymbolOrThrow());
        }

        if (!operationNode.lifted.isEmpty()) {
            compileClosureClass(methodVisitor, context, operationNode.lifted);
        }

        compileStatement(methodVisitor, context, operationNode.body);
        if (operation.getResultType() == SVoidType.instance) {
            methodVisitor.visitInsn(RETURN);
        }

        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void compileExtensionBinaryOperation(BoundExtensionBinaryOperationNode operationNode, ClassWriter writer, CompilerContext context) {
        ExtensionBinaryOperation operation = (ExtensionBinaryOperation) operationNode.operation;

        MethodVisitor methodVisitor = writer.visitMethod(
                ACC_PUBLIC | ACC_STATIC,
                operation.getInternalName(),
                operation.getMethodDescriptor(),
                null, null);
        methodVisitor.visitCode();

        context = context.createClassStaticMethod(operation.getResultType());
        for (BoundParameterNode parameter : operationNode.parameters.parameters) {
            context.setStackIndex((LocalVariable) parameter.getName().getSymbolOrThrow());
        }

        if (!operationNode.lifted.isEmpty()) {
            compileClosureClass(methodVisitor, context, operationNode.lifted);
        }

        compileStatement(methodVisitor, context, operationNode.body);
        if (operation.getResultType() == SVoidType.instance) {
            methodVisitor.visitInsn(RETURN);
        }

        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void compileGenericFunctions(List<SGenericFunction> functions, CompilerContext context) {
        for (SGenericFunction function : functions) {
            String name = "com/zergatul/scripting/dynamic/GenericFunction_" + context.getNextUniqueIndex();
            function.setInternalName(name);

            ClassWriter writer = new ClassWriter(0);
            writer.visit(
                    V1_8,
                    ACC_PUBLIC | ACC_ABSTRACT | ACC_INTERFACE,
                    function.getInternalName(),
                    null,
                    Type.getInternalName(Object.class),
                    null);

            MethodVisitor visitor = writer.visitMethod(
                    ACC_PUBLIC | ACC_ABSTRACT,
                    function.getMethodName(),
                    function.getMethodDescriptor(),
                    null,
                    null);
            visitor.visitEnd();

            writer.visitEnd();

            String shortName = function.getShortClassName();
            byte[] bytecode = writer.toByteArray();
            saveClassFile(shortName, bytecode);

            Class<?> interfaceClass = context.defineClass(function.getInternalName().replace('/', '.'), bytecode);
            function.setJavaClass(interfaceClass);
        }
    }

    private void compileStaticVariables(List<BoundStaticVariableNode> staticVariableNodes, ClassWriter writer, CompilerContext context) {
        if (staticVariableNodes.isEmpty()) {
            return;
        }

        for (BoundStaticVariableNode staticVariableNode : staticVariableNodes) {
            FieldVisitor fieldVisitor = writer.visitField(
                    ACC_PUBLIC | ACC_STATIC,
                    staticVariableNode.name.value,
                    Type.getDescriptor(staticVariableNode.type.type.getJavaClass()),
                    null, null);
            fieldVisitor.visitEnd();
        }

        // set static variables values in static constructor
        MethodVisitor visitor = writer.visitMethod(ACC_STATIC, "<clinit>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
        visitor.visitCode();
        for (BoundStaticVariableNode staticVariableNode : staticVariableNodes) {
            StaticVariable symbol = (StaticVariable) staticVariableNode.name.getSymbolOrThrow();
            context.addStaticSymbol(staticVariableNode.name.value, staticVariableNode.name.symbolRef);
            if (staticVariableNode.expression != null) {
                compileExpression(visitor, context, staticVariableNode.expression);
            } else {
                staticVariableNode.type.type.storeDefaultValue(visitor);
            }
            symbol.compileStore(context, visitor);
        }
        visitor.visitInsn(RETURN);
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();
    }

    private void compileFunctions(List<BoundFunctionDeclarationNode> functions, ClassWriter writer, CompilerContext context) {
        for (BoundFunctionDeclarationNode function : functions) {
            Function symbol = (Function) function.name.getSymbolOrThrow();
            SStaticFunction type = symbol.getFunctionType();

            MethodVisitor visitor = writer.visitMethod(
                    ACC_PUBLIC | ACC_STATIC,
                    function.name.value,
                    type.getMethodDescriptor(),
                    null,
                    null);
            visitor.visitCode();

            CompilerContext functionContext = context.createStaticFunction(type.getReturnType(), function.isAsync());
            processContextStart(visitor, functionContext);

            for (BoundParameterNode parameter : function.parameters.parameters) {
                functionContext.setStackIndex((LocalVariable) parameter.getName().getSymbolOrThrow());
            }

            if (function.isAsync()) {
                compileAsyncBoundStatementList(visitor, functionContext, new BoundStatementsListNode(List.of(function.body)));
            } else {
                if (!function.lifted.isEmpty()) {
                    compileClosureClass(visitor, functionContext, function.lifted);
                }

                compileStatement(visitor, functionContext, function.body);
                if (type.getReturnType() == SVoidType.instance) {
                    visitor.visitInsn(RETURN);
                }
            }

            processContextEnd(visitor, functionContext, function.parameters.parameters.stream().map(p -> p.getName().symbolRef.asLocalVariable()).toList());
            visitor.visitMaxs(0, 0);
            visitor.visitEnd();
        }
    }

    private static void buildEmptyConstructor(ClassWriter writer) {
        String descriptor = Type.getMethodDescriptor(Type.VOID_TYPE);
        MethodVisitor constructorVisitor = writer.visitMethod(ACC_PUBLIC, "<init>", descriptor, null, null);
        constructorVisitor.visitCode();
        constructorVisitor.visitVarInsn(ALOAD, 0);
        constructorVisitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
        constructorVisitor.visitInsn(RETURN);
        constructorVisitor.visitMaxs(0, 0);
        constructorVisitor.visitEnd();
    }

    private static String buildLambdaConstructor(String className, ClassWriter writer, List<Variable> closures) {
        for (int i = 0; i < closures.size(); i++) {
            String descriptor = closures.get(i).getType().getDescriptor();
            writer.visitField(ACC_PUBLIC | ACC_FINAL, "closure" + i, descriptor, null, null);
        }

        Type[] arguments = new Type[closures.size()];
        for (int i = 0; i < closures.size(); i++) {
            arguments[i] = Type.getType(closures.get(i).getType().getDescriptor());
        }

        String constructorDescriptor = Type.getMethodDescriptor(Type.VOID_TYPE, arguments);
        MethodVisitor visitor = writer.visitMethod(ACC_PUBLIC, "<init>", constructorDescriptor, null, null);
        visitor.visitCode();
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
        for (int i = 0; i < closures.size(); i++) {
            visitor.visitVarInsn(ALOAD, 0);
            visitor.visitVarInsn(ALOAD, i + 1);
            String descriptor = closures.get(i).getType().getDescriptor();
            visitor.visitFieldInsn(PUTFIELD, className, "closure" + i, descriptor);
        }
        visitor.visitInsn(RETURN);
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();

        return constructorDescriptor;
    }

    private void buildMainMethod(BoundCompilationUnitNode unit, ClassWriter writer, String className, CompilerContext context1) {
        Class<?> functionalInterface = parameters.getFunctionalInterface();
        if (!functionalInterface.isInterface()) {
            throw new InternalException();
        }

        List<Method> methods = Arrays.stream(functionalInterface.getMethods()).filter(m -> !m.isDefault()).toList();
        if (methods.size() != 1) {
            throw new InternalException();
        }
        Method method = methods.getFirst();

        MethodVisitor visitor = writer.visitMethod(ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(method), null, null);
        visitor.visitCode();

        CompilerContext context = context1.createInstanceMethod(parameters.getReturnType(), parameters.isAsync());
        context.reserveStack(1 + method.getParameterCount()); // assuming all parameters size = 1

        processContextStart(visitor, context);

        ExternalParameterVisitor treeVisitor = new ExternalParameterVisitor();
        unit.statements.accept(treeVisitor);
        List<BoundVariableDeclarationNode> prepend = new ArrayList<>();
        for (Variable variable : treeVisitor.getParameters()) {
            int parameterStackIndex;
            if (variable instanceof LiftedVariable lifted) {
                parameterStackIndex = 1 + ((ExternalParameter) lifted.getUnderlying()).getIndex();
            } else if (variable instanceof ExternalParameter external) {
                parameterStackIndex = 1 + external.getIndex();
                external.setStackIndex(parameterStackIndex);
            } else {
                throw new InternalException();
            }

            BoundVariableDeclarationNode declaration = new BoundVariableDeclarationNode(
                    new BoundNameExpressionNode(new MutableSymbolRef(variable)),
                    new BoundStackLoadNode(parameterStackIndex, variable.getType()));

            prepend.add(declaration);
        }

        if (!prepend.isEmpty()) {
            unit = unit.withStatements(unit.statements.withPrepend(prepend));
        }

        if (parameters.isAsync()) {
            compileAsyncBoundStatementList(visitor, context, unit.statements);
        } else {
            compileStatementList(visitor, context, unit.statements);
        }

        if (!parameters.isAsync() && parameters.getReturnType() == SVoidType.instance) {
            visitor.visitInsn(RETURN);
        }

        processContextEnd(visitor, context);
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();
    }

    private void compileStatement(MethodVisitor visitor, CompilerContext context, BoundStatementNode statement) {
        emitLineNumber(visitor, context, statement);
        switch (statement.getNodeType()) {
            case VARIABLE_DECLARATION -> compileVariableDeclaration(visitor, context, (BoundVariableDeclarationNode) statement);
            case ASSIGNMENT_STATEMENT -> compileAssignmentStatement(visitor, context, (BoundAssignmentStatementNode) statement);
            case AUGMENTED_ASSIGNMENT_STATEMENT -> compileAugmentedAssignmentStatement(visitor, context, (BoundAugmentedAssignmentStatementNode) statement);
            case EXPRESSION_STATEMENT -> compileExpressionStatement(visitor, context, (BoundExpressionStatementNode) statement);
            case IF_STATEMENT -> compileIfStatement(visitor, context, (BoundIfStatementNode) statement);
            case BLOCK_STATEMENT -> compileBlockStatement(visitor, context, (BoundBlockStatementNode) statement);
            case RETURN_STATEMENT -> compileReturnStatement(visitor, context, (BoundReturnStatementNode) statement);
            case FOR_LOOP_STATEMENT -> compileForLoopStatement(visitor, context, (BoundForLoopStatementNode) statement);
            case FOREACH_LOOP_STATEMENT -> compileForEachLoopStatement(visitor, context, (BoundForEachLoopStatementNode) statement);
            case WHILE_LOOP_STATEMENT -> compileWhileLoopStatement(visitor, context, (BoundWhileLoopStatementNode) statement);
            case BREAK_STATEMENT -> compileBreakStatement(visitor, context);
            case CONTINUE_STATEMENT -> compileContinueStatement(visitor, context);
            case EMPTY_STATEMENT -> compileEmptyStatement();
            case INCREMENT_STATEMENT, DECREMENT_STATEMENT -> compilePostfixStatement(visitor, context, (BoundPostfixStatementNode) statement);
            case SET_GENERATOR_STATE -> compileGoToGeneratorState(visitor, context, (BoundSetGeneratorStateNode) statement);
            case SET_GENERATOR_BOUNDARY -> compileSetGeneratorBoundary(visitor, context, (BoundSetGeneratorBoundaryNode) statement);
            case GENERATOR_RETURN -> compileGeneratorReturn(visitor, context, (BoundGeneratorReturnNode) statement);
            case GENERATOR_CONTINUE -> compileGeneratorContinue(visitor, context);
            default -> throw new InternalException();
        }
    }

    private void compileVariableDeclaration(MethodVisitor visitor, CompilerContext context, BoundVariableDeclarationNode declaration) {
        if (declaration.expression != null) {
            compileExpression(visitor, context, declaration.expression);
        } else {
            if (declaration.type != null) {
                declaration.type.type.storeDefaultValue(visitor);
            } else {
                // generator code doesn't generate type nodes
                declaration.name.type.storeDefaultValue(visitor);
            }
        }

        Variable variable = declaration.name.symbolRef.asVariable();
        context.addLocalVariable(declaration.name.symbolRef);

        if (variable instanceof LocalVariable local) {
            context.setStackIndex(local);
        }

        variable.compileStore(context, visitor);
    }

    private void compileAssignmentStatement(MethodVisitor visitor, CompilerContext context, BoundAssignmentStatementNode assignment) {
        if (assignment.operator.operator == AssignmentOperator.ASSIGNMENT) {
            switch (assignment.left.getNodeType()) {
                case NAME_EXPRESSION -> {
                    compileExpression(visitor, context, assignment.right);
                    BoundNameExpressionNode name = (BoundNameExpressionNode) assignment.left;
                    Variable variable = name.symbolRef.asVariable();
                    variable.compileStore(context, visitor);
                }
                case INDEX_EXPRESSION -> {
                    BoundIndexExpressionNode indexExpression = (BoundIndexExpressionNode) assignment.left;
                    compileExpression(visitor, context, indexExpression.callee);
                    compileExpression(visitor, context, indexExpression.index);
                    compileExpression(visitor, context, assignment.right);
                    indexExpression.operation.compileSet(visitor);
                }
                case PROPERTY_ACCESS_EXPRESSION -> {
                    BoundPropertyAccessExpressionNode access = (BoundPropertyAccessExpressionNode) assignment.left;
                    if (access.property.property.isPublic()) {
                        compileExpression(visitor, context, access.callee);
                        compileExpression(visitor, context, assignment.right);
                        access.property.property.compileSet(visitor);
                    } else {
                        String varHandleFieldName = context.createCachedPrivateMethodHandle(access.property.property);
                        visitor.visitFieldInsn(
                                GETSTATIC,
                                MethodHandleCache.CLASS_NAME,
                                varHandleFieldName,
                                Type.getDescriptor(VarHandle.class));
                        compileExpression(visitor, context, access.callee);
                        compileExpression(visitor, context, assignment.right);
                        visitor.visitMethodInsn(
                                INVOKEVIRTUAL,
                                Type.getInternalName(VarHandle.class),
                                "set",
                                Type.getMethodDescriptor(
                                        Type.VOID_TYPE,
                                        access.callee.type.getAsmType(),
                                        assignment.right.type.getAsmType()),
                                false);
                    }
                }
                default -> throw new InternalException("Not implemented.");
            }
        } else {
            throw new InternalException("Should not happen.");
        }
    }

    private void compileAugmentedAssignmentStatement(MethodVisitor visitor, CompilerContext context, BoundAugmentedAssignmentStatementNode assignment) {
        switch (assignment.left.getNodeType()) {
            case NAME_EXPRESSION -> {
                BufferedMethodVisitor buffer = new BufferedMethodVisitor();
                compileExpression(visitor, context, assignment.left);
                compileExpression(buffer, context, assignment.right);
                assignment.operation.apply(visitor, buffer, context, assignment.left.type, assignment.right.type);

                BoundNameExpressionNode name = (BoundNameExpressionNode) assignment.left;
                Variable variable = name.symbolRef.asVariable();
                variable.compileStore(context, visitor);
            }
            case INDEX_EXPRESSION -> {
                BoundIndexExpressionNode indexExpression = (BoundIndexExpressionNode) assignment.left;
                compileExpression(visitor, context, indexExpression.callee);
                compileExpression(visitor, context, indexExpression.index);
                StackHelper.duplicate2(visitor, indexExpression.callee.type, indexExpression.index.type);

                BufferedMethodVisitor buffer = new BufferedMethodVisitor();
                indexExpression.operation.compileGet(visitor);
                compileExpression(buffer, context, assignment.right);
                assignment.operation.apply(visitor, buffer, context, assignment.left.type, assignment.right.type);

                indexExpression.operation.compileSet(visitor);
            }
            case PROPERTY_ACCESS_EXPRESSION -> {
                BoundPropertyAccessExpressionNode propertyAccess = (BoundPropertyAccessExpressionNode) assignment.left;
                compileExpression(visitor, context, propertyAccess.callee);
                visitor.visitInsn(DUP);
                propertyAccess.property.property.compileGet(visitor);
                BufferedMethodVisitor buffer = new BufferedMethodVisitor();
                compileExpression(buffer, context, assignment.right);
                assignment.operation.apply(visitor, buffer, context, assignment.left.type, assignment.right.type);
                propertyAccess.property.property.compileSet(visitor);
            }
            default -> throw new InternalException("Not implemented.");
        }
    }

    private void compileIfStatement(MethodVisitor visitor, CompilerContext context, BoundIfStatementNode statement) {
        // add fallthrough variables to parent context
        for (SymbolRef ref : statement.flow.fallthroughLocals()) {
            LocalVariable variable = ref.asLocalVariable();
            context.setStackIndex(variable);
            context.addLocalVariable(ref);
            initVariableWithRawDefault(visitor, variable);
        }

        context = context.createChild();

        // add all possible flow variables
        for (SymbolRef ref : statement.flow.allLocals()) {
            if (statement.flow.fallthroughLocals().contains(ref)) {
                continue;
            }

            LocalVariable variable = ref.asLocalVariable();
            context.setStackIndex(variable);
            context.addLocalVariable(ref);
            initVariableWithRawDefault(visitor, variable);
        }

        compileExpression(visitor, context, statement.condition);
        if (statement.elseStatement == null) {
            Label endLabel = new Label();
            visitor.visitJumpInsn(IFEQ, endLabel);
            compileStatement(visitor, context, statement.thenStatement);
            visitor.visitLabel(endLabel);
        } else {
            Label elseLabel = new Label();
            visitor.visitJumpInsn(IFEQ, elseLabel);
            compileStatement(visitor, context, statement.thenStatement);
            Label endLabel = new Label();
            visitor.visitJumpInsn(GOTO, endLabel);
            visitor.visitLabel(elseLabel);
            compileStatement(visitor, context, statement.elseStatement);
            visitor.visitLabel(endLabel);
        }
    }

    private void compileBlockStatement(MethodVisitor visitor, CompilerContext context, BoundBlockStatementNode statement) {
        context = createChildContext(visitor, context);
        compileStatements(visitor, context, statement.statements);
        processContextEnd(visitor, context);
    }

    private void compileReturnStatement(MethodVisitor visitor, CompilerContext context, BoundReturnStatementNode statement) {
        if (statement.expression != null) {
            compileExpression(visitor, context, statement.expression);

            if (context.isGenericFunction() && context.getReturnType() instanceof SValueType valueType) {
                valueType.compileBoxing(visitor);
                visitor.visitInsn(ARETURN);
                return;
            }
        }

        visitor.visitInsn(context.getReturnType().getReturnInst());
    }

    private void compileForLoopStatement(MethodVisitor visitor, CompilerContext context, BoundForLoopStatementNode statement) {
        context = createChildContext(visitor, context);

        /*
            <init>
            begin:
            <exit>
            <body>
            continueLabel:
            <update>
            end:
        */

        Label begin = new Label();
        Label continueLabel = new Label();
        Label end = new Label();

        if (statement.init != null) {
            compileStatement(visitor, context, statement.init);
        }

        visitor.visitLabel(begin);

        if (statement.condition != null) {
            compileExpression(visitor, context, statement.condition);
            visitor.visitJumpInsn(IFEQ, end);
        }

        context.setBreak(v -> v.visitJumpInsn(GOTO, end));
        context.setContinue(v -> v.visitJumpInsn(GOTO, continueLabel));
        compileStatement(visitor, context, statement.body);

        visitor.visitLabel(continueLabel);
        if (statement.update != null) {
            compileStatement(visitor, context, statement.update);
        }

        visitor.visitJumpInsn(GOTO, begin);
        visitor.visitLabel(end);

        processContextEnd(visitor, context);
    }

    private void compileForEachLoopStatement(MethodVisitor visitor, CompilerContext context, BoundForEachLoopStatementNode statement) {
        context = createChildContext(visitor, context);

        Label begin = new Label();
        Label continueLabel = new Label();
        Label end = new Label();

        compileExpression(visitor, context, statement.iterable);

        LocalVariable variable = statement.name.symbolRef.asLocalVariable();
        context.addLocalVariable(statement.name.symbolRef);
        context.addLocalVariable(statement.index);
        context.addLocalVariable(statement.length);

        context.setStackIndex(variable);
        context.setStackIndex(statement.index.asLocalVariable());
        context.setStackIndex(statement.length.asLocalVariable());

        visitor.visitInsn(ICONST_0);
        statement.index.asLocalVariable().compileStore(context, visitor);

        visitor.visitInsn(DUP);
        visitor.visitInsn(ARRAYLENGTH);
        statement.length.asLocalVariable().compileStore(context, visitor);

        visitor.visitLabel(begin);

        // index >= length -- GOTO end
        statement.index.asLocalVariable().compileLoad(context, visitor);
        statement.length.asLocalVariable().compileLoad(context, visitor);
        visitor.visitJumpInsn(IF_ICMPGE, end);

        // variable = array[index]
        visitor.visitInsn(DUP);
        statement.index.asLocalVariable().compileLoad(context, visitor);
        visitor.visitInsn(variable.getType().getArrayLoadInst());
        variable.compileStore(context, visitor);

        // body
        context.setBreak(v -> v.visitJumpInsn(GOTO, end));
        context.setContinue(v -> v.visitJumpInsn(GOTO, continueLabel));
        compileStatement(visitor, context, statement.body);

        // index++
        visitor.visitLabel(continueLabel);
        visitor.visitIincInsn(statement.index.asLocalVariable().getStackIndex(), 1);

        visitor.visitJumpInsn(GOTO, begin);
        visitor.visitLabel(end);

        visitor.visitInsn(POP);

        processContextEnd(visitor, context);
    }

    private void compileWhileLoopStatement(MethodVisitor visitor, CompilerContext context, BoundWhileLoopStatementNode statement) {
        context = context.createChild();

        Label begin = new Label();
        Label continueLabel = new Label();
        Label end = new Label();

        visitor.visitLabel(begin);

        compileExpression(visitor, context, statement.condition);
        visitor.visitJumpInsn(IFEQ, end);

        context.setBreak(v -> v.visitJumpInsn(GOTO, end));
        context.setContinue(v -> v.visitJumpInsn(GOTO, continueLabel));
        compileStatement(visitor, context, statement.body);

        visitor.visitLabel(continueLabel);

        visitor.visitJumpInsn(GOTO, begin);
        visitor.visitLabel(end);

        processContextEnd(visitor, context);
    }

    private void compileBreakStatement(MethodVisitor visitor, CompilerContext context) {
        context.compileBreak(visitor);
    }

    private void compileContinueStatement(MethodVisitor visitor, CompilerContext context) {
        context.compileContinue(visitor);
    }

    private void compileEmptyStatement() {

    }

    private void compilePostfixStatement(MethodVisitor visitor, CompilerContext context, BoundPostfixStatementNode statement) {
        switch (statement.expression.getNodeType()) {
            case NAME_EXPRESSION -> {
                BoundNameExpressionNode name = (BoundNameExpressionNode) statement.expression;
                compileExpression(visitor, context, statement.expression);
                statement.operation.apply(visitor);
                Variable variable = name.symbolRef.asVariable();
                variable.compileStore(context, visitor);
            }
            case INDEX_EXPRESSION -> {
                BoundIndexExpressionNode indexExpression = (BoundIndexExpressionNode) statement.expression;
                compileExpression(visitor, context, indexExpression.callee);
                compileExpression(visitor, context, indexExpression.index);
                StackHelper.duplicate2(visitor, indexExpression.callee.type, indexExpression.index.type);
                indexExpression.operation.compileGet(visitor);
                statement.operation.apply(visitor);
                indexExpression.operation.compileSet(visitor);
            }
            case PROPERTY_ACCESS_EXPRESSION -> {
                BoundPropertyAccessExpressionNode propertyExpression = (BoundPropertyAccessExpressionNode) statement.expression;
                compileExpression(visitor, context, propertyExpression.callee);
                visitor.visitInsn(DUP);
                propertyExpression.property.property.compileGet(visitor);
                statement.operation.apply(visitor);
                propertyExpression.property.property.compileSet(visitor);
            }
            default -> throw new InternalException();
        }
    }

    private void compileExpressionStatement(MethodVisitor visitor, CompilerContext context, BoundExpressionStatementNode statement) {
        compileExpression(visitor, context, statement.expression);
        if (!statement.expression.type.equals(SVoidType.instance)) {
            visitor.visitInsn(POP);
        }
    }

    private void compileStatements(MethodVisitor visitor, CompilerContext context, List<BoundStatementNode> statements) {
        for (BoundStatementNode statement : statements) {
            compileStatement(visitor, context, statement);
        }
    }

    private void compileStatementList(MethodVisitor visitor, CompilerContext context, BoundStatementsListNode node) {
        if (!node.lifted.isEmpty()) {
            compileClosureClass(visitor, context, node.lifted);
        }
        for (BoundVariableDeclarationNode declaration : node.prepend) {
            compileVariableDeclaration(visitor, context, declaration);
        }
        compileStatements(visitor, context, node.statements);
    }

    private void compileClosureClass(MethodVisitor parentVisitor, CompilerContext parentContext, List<LiftedVariable> variables) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        emitSourceFile(writer);
        String name = "com/zergatul/scripting/dynamic/DynamicClosure_" + parentContext.getNextUniqueIndex();
        writer.visit(
                V1_8,
                ACC_PUBLIC,
                name,
                null,
                Type.getInternalName(Object.class),
                null);

        buildEmptyConstructor(writer);

        String[] fieldNames = new String[variables.size()];
        for (int i = 0; i < variables.size(); i++) {
            fieldNames[i] = variables.get(i).getName();
            if (fieldNames[i] == null) {
                fieldNames[i] = "lifted";
            }
        }
        uniquify(fieldNames);

        for (int i = 0; i < variables.size(); i++) {
            LiftedVariable variable = variables.get(i);
            variable.setField(name, fieldNames[i]);
            writer.visitField(ACC_PUBLIC, fieldNames[i], Type.getDescriptor(variable.getType().getJavaClass()), null, null);
        }

        writer.visitEnd();

        byte[] bytecode = writer.toByteArray();
        saveClassFile(name, bytecode);

        Class<?> closureClass = parentContext.defineClass(name.replace('/', '.'), bytecode);

        // create instance of closure class
        parentVisitor.visitTypeInsn(NEW, name);
        parentVisitor.visitInsn(DUP);
        parentVisitor.visitMethodInsn(
                INVOKESPECIAL,
                name,
                "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE),
                false);

        SymbolRef closureRef = parentContext.addLocalVariable(null, SType.fromJavaType(closureClass), null);
        parentContext.setStackIndex(closureRef.asLocalVariable());
        closureRef.asVariable().compileStore(parentContext, parentVisitor);

        for (LiftedVariable lifted : variables) {
            lifted.setClosure(closureRef.asLocalVariable());

            if (lifted.getUnderlying() instanceof LocalParameter parameter) {
                parameter.compileLoad(parentContext, parentVisitor);
                lifted.compileStore(parentContext, parentVisitor);
            }
        }
    }

    private void uniquify(String[] array) {
        if (array.length <= 1) {
            return;
        }

        List<List<Integer>> copies = new ArrayList<>();
        boolean[] used = new boolean[array.length];
        while (true) {
            copies.clear();
            Arrays.fill(used, false);
            for (int i1 = 0; i1 < array.length - 1; i1++) {
                if (used[i1]) {
                    continue;
                }

                List<Integer> current = new ArrayList<>();
                current.add(i1);
                for (int i2 = i1 + 1; i2 < array.length; i2++) {
                    if (!used[i2] && array[i1].equals(array[i2])) {
                        current.add(i2);
                    }
                }
                if (current.size() > 1) {
                    copies.add(current);
                    for (int index : current) {
                        used[index] = true;
                    }
                }
            }
            if (copies.isEmpty()) {
                return;
            }
            for (List<Integer> copy : copies) {
                for (int i = 0; i < copy.size(); i++) {
                    array[copy.get(i)] += "_" + i;
                }
            }
        }
    }

    private void compileAsyncBoundStatementList(MethodVisitor parentVisitor, CompilerContext context, BoundStatementsListNode node) {
        for (BoundVariableDeclarationNode declaration : node.prepend) {
            if (declaration.name.getSymbol() instanceof ExternalParameter parameter) {
                LiftedVariable lifted = new LiftedVariable(parameter);
                declaration.name.symbolRef.set(lifted);
            }
        }

        BinderTreeGenerator generator = new BinderTreeGenerator();
        generator.generate(node);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        emitSourceFile(writer);
        String name = "com/zergatul/scripting/dynamic/DynamicAsyncStateMachine_" + context.getNextUniqueIndex();
        writer.visit(
                V1_8,
                ACC_PUBLIC,
                name,
                null,
                Type.getInternalName(Object.class),
                new String[] { Type.getInternalName(AsyncStateMachine.class) });

        // state field
        writer.visitField(ACC_PRIVATE, "state", Type.getDescriptor(int.class), null, null);

        // lifted variables
        LiftedVariablesVisitor treeVisitor = new LiftedVariablesVisitor();
        LocalParameterVisitor parameterVisitor = new LocalParameterVisitor();
        for (BoundVariableDeclarationNode declaration : node.prepend) {
            declaration.accept(treeVisitor);
        }
        for (StateBoundary boundary : generator.boundaries) {
            for (BoundStatementNode statement : boundary.statements) {
                statement.accept(treeVisitor);
                statement.accept(parameterVisitor);
            }
        }

        List<LiftedVariable> variables = new ArrayList<>();
        variables.addAll(treeVisitor.getVariables());
        variables.addAll(parameterVisitor.getParameters());

        String[] fieldNames = new String[variables.size()];
        for (int i = 0; i < fieldNames.length; i++) {
            String varName = variables.get(i).getName();
            fieldNames[i] = varName != null ? varName : "lifted";
        }
        uniquify(fieldNames);
        for (int i = 0; i < variables.size(); i++) {
            LiftedVariable variable = variables.get(i);
            variable.setField(name, fieldNames[i]);
            writer.visitField(ACC_PUBLIC, fieldNames[i], variable.getType().getDescriptor(), null, null);
        }

        // build constructor
        SType[] ctorParameters1 = new SType[parameterVisitor.getParameters().size()];
        Type[] ctorParameters2 = new Type[parameterVisitor.getParameters().size()];
        for (int i = 0; i < ctorParameters1.length; i++) {
            ctorParameters1[i] = parameterVisitor.getParameters().get(i).getType();
            ctorParameters2[i] = Type.getType(ctorParameters1[i].getDescriptor());
        }
        int[] indexes = StackHelper.buildStackIndexes(ctorParameters1);
        MethodVisitor constructorVisitor = writer.visitMethod(ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, ctorParameters2), null, null);
        constructorVisitor.visitCode();
        constructorVisitor.visitVarInsn(ALOAD, 0);
        constructorVisitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
        //
        for (int i = 0; i < indexes.length; i++) {
            constructorVisitor.visitVarInsn(ALOAD, 0); // this
            constructorVisitor.visitVarInsn(ctorParameters1[i].getLoadInst(), indexes[i]);
            LiftedVariable lifted = parameterVisitor.getParameters().get(i);
            constructorVisitor.visitFieldInsn(PUTFIELD, lifted.getClassName(), lifted.getFieldName(), lifted.getType().getDescriptor());
        }
        //
        constructorVisitor.visitInsn(RETURN);
        constructorVisitor.visitMaxs(0, 0);
        constructorVisitor.visitEnd();

        // build next method
        MethodVisitor nextMethodVisitor = writer.visitMethod(
                ACC_PUBLIC,
                "next",
                Type.getMethodDescriptor(Type.getType(CompletableFuture.class), Type.getType(Object.class)),
                null,
                null);

        CompilerContext nextMethodContext = context.createInstanceMethod(SType.fromJavaType(CompletableFuture.class), false);
        nextMethodContext.setAsyncStateMachineClassName(name);
        if (parameterVisitor.hasThisLocalVariable()) {
            nextMethodContext.setAsyncThisFieldName(parameterVisitor.getParameters().getFirst().getFieldName());
        }
        LocalVariable parameter = nextMethodContext.addLocalParameter("@result", SJavaObject.instance, null);
        nextMethodContext.setStackIndex(parameter);

        // closure reference
        SymbolRef closureRef = nextMethodContext.addLocalVariable(null, new SLazyClassType(name), null);
        closureRef.asLocalVariable().setStackIndex(0);
        for (LiftedVariable lifted : variables) {
            lifted.setClosure(closureRef.asLocalVariable());
        }

        Label loop = new Label();
        nextMethodVisitor.visitLabel(loop);
        nextMethodContext.setGeneratorContinueLabel(loop);

        nextMethodVisitor.visitVarInsn(ALOAD, 0);
        nextMethodVisitor.visitFieldInsn(GETFIELD, name, "state", Type.getDescriptor(int.class));

        Label defaultLabel = new Label();
        int[] keys = new int[generator.boundaries.size()];
        Label[] labels = new Label[generator.boundaries.size()];
        for (int i = 0; i < generator.boundaries.size(); i++) {
            keys[i] = i;
            labels[i] = generator.boundaries.get(i).label;
        }
        nextMethodVisitor.visitLookupSwitchInsn(defaultLabel, keys, labels);

        for (StateBoundary boundary : generator.boundaries) {
            nextMethodVisitor.visitLabel(boundary.label);
            for (BoundStatementNode statement : boundary.statements) {
                compileStatement(nextMethodVisitor, nextMethodContext, statement);
            }
            if (boundary.statements.isEmpty() || boundary.statements.getLast().getNodeType() != BoundNodeType.RETURN_STATEMENT) {
                nextMethodVisitor.visitJumpInsn(GOTO, loop);
            }
        }

        nextMethodVisitor.visitLabel(defaultLabel);
        nextMethodVisitor.visitTypeInsn(NEW, Type.getInternalName(AsyncStateMachineException.class));
        nextMethodVisitor.visitInsn(DUP);
        nextMethodVisitor.visitMethodInsn(
                INVOKESPECIAL,
                Type.getInternalName(AsyncStateMachineException.class),
                "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE),
                false);
        nextMethodVisitor.visitInsn(ATHROW);

        nextMethodVisitor.visitJumpInsn(GOTO, loop);

        //markEnd(nextMethodVisitor, nextMethodContext);

        nextMethodVisitor.visitMaxs(0, 0);
        nextMethodVisitor.visitEnd();

        writer.visitEnd();

        byte[] bytecode = writer.toByteArray();
        saveClassFile(name, bytecode);

        context.defineClass(name.replace('/', '.'), bytecode);

        /**/
        parentVisitor.visitTypeInsn(NEW, name);
        parentVisitor.visitInsn(DUP);
        for (LiftedVariable lifted : parameterVisitor.getParameters()) {
            LocalVariable local = lifted.getUnderlying();
            local.compileLoad(context, parentVisitor);
        }
        parentVisitor.visitMethodInsn(
                INVOKESPECIAL,
                name,
                "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE, ctorParameters2),
                false);

        for (BoundVariableDeclarationNode declaration : node.prepend) {
            if (declaration.expression instanceof BoundStackLoadNode load) {
                if (declaration.name.getSymbol() instanceof LiftedVariable lifted) {
                    int index = variables.indexOf(lifted);
                    if (index < 0) {
                        throw new InternalException();
                    }
                    parentVisitor.visitInsn(DUP); // dup async state machine
                    compileStackLoad(parentVisitor, load);
                    parentVisitor.visitFieldInsn(PUTFIELD, lifted.getClassName(), lifted.getFieldName(), Type.getDescriptor(lifted.getType().getJavaClass()));
                } else {
                    throw new InternalException(); // all should be lifted!
                }
            } else {
                throw new InternalException();
            }
        }

        parentVisitor.visitInsn(ACONST_NULL);
        parentVisitor.visitMethodInsn(
                INVOKEVIRTUAL,
                name,
                "next",
                Type.getMethodDescriptor(Type.getType(CompletableFuture.class), Type.getType(Object.class)),
                false);
        parentVisitor.visitInsn(ARETURN);
    }

    private void compileSetGeneratorBoundary(MethodVisitor visitor, CompilerContext context, BoundSetGeneratorBoundaryNode node) {
        compileExpression(visitor, context, node.expression);

        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                context.getAsyncStateMachineClassName(),
                "getContinuation",
                Type.getMethodDescriptor(Type.getType(java.util.function.Function.class)),
                false);

        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(CompletableFuture.class),
                "thenCompose",
                Type.getMethodDescriptor(Type.getType(CompletableFuture.class), Type.getType(java.util.function.Function.class)),
                false);
        visitor.visitInsn(ARETURN);
    }

    private void compileGoToGeneratorState(MethodVisitor visitor, CompilerContext context, BoundSetGeneratorStateNode node) {
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitLdcInsn(node.boundary.index);
        visitor.visitFieldInsn(PUTFIELD, context.getAsyncStateMachineClassName(), "state", Type.getDescriptor(int.class));
    }

    private void compileGeneratorReturn(MethodVisitor visitor, CompilerContext context, BoundGeneratorReturnNode node) {
        // set state to invalid to prevent consequent calls to next()
        visitor.visitVarInsn(ALOAD, 0);
        visitor.visitLdcInsn(-1);
        visitor.visitFieldInsn(PUTFIELD, context.getAsyncStateMachineClassName(), "state", Type.getDescriptor(int.class));

        if (node.expression == null) {
            visitor.visitInsn(ACONST_NULL);
        } else {
            compileExpression(visitor, context, node.expression);
            if (node.expression.type instanceof SValueType valueType) {
                valueType.compileBoxing(visitor);
            }
        }

        visitor.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(CompletableFuture.class),
                "completedFuture",
                Type.getMethodDescriptor(Type.getType(CompletableFuture.class), Type.getType(Object.class)),
                false);
        visitor.visitInsn(ARETURN);
    }

    private void compileGeneratorContinue(MethodVisitor visitor, CompilerContext context) {
        visitor.visitJumpInsn(GOTO, context.getGeneratorContinueLabel());
    }

    private void compileExpression(MethodVisitor visitor, CompilerContext context, BoundExpressionNode expression) {
        emitLineNumber(visitor, context, expression);
        switch (expression.getNodeType()) {
            case NULL_EXPRESSION -> compileNull(visitor);
            case BOOLEAN_LITERAL -> compileBooleanLiteral(visitor, (BoundBooleanLiteralExpressionNode) expression);
            case INTEGER_LITERAL -> compileIntegerLiteral(visitor, (BoundIntegerLiteralExpressionNode) expression);
            case INTEGER64_LITERAL -> compileInteger64Literal(visitor, (BoundInteger64LiteralExpressionNode) expression);
            case FLOAT_LITERAL -> compileFloatLiteral(visitor, (BoundFloatLiteralExpressionNode) expression);
            case STRING_LITERAL -> compileStringLiteral(visitor, (BoundStringLiteralExpressionNode) expression);
            case CHAR_LITERAL -> compileCharLiteral(visitor, (BoundCharLiteralExpressionNode) expression);
            case PARENTHESIZED_EXPRESSION -> compileExpression(visitor, context, ((BoundParenthesizedExpressionNode) expression).inner);
            case UNARY_EXPRESSION -> compileUnaryExpression(visitor, context, (BoundUnaryExpressionNode) expression);
            case BINARY_EXPRESSION -> compileBinaryExpression(visitor, context, (BoundBinaryExpressionNode) expression);
            case IN_EXPRESSION -> compileInExpression(visitor, context, (BoundInExpressionNode) expression);
            case IS_EXPRESSION -> compileIsExpression(visitor, context, (BoundIsExpressionNode) expression);
            case TYPE_CAST_EXPRESSION -> compileTypeCastExpression(visitor, context, (BoundTypeCastExpressionNode) expression);
            case CONDITIONAL_EXPRESSION -> compileConditionalExpression(visitor, context, (BoundConditionalExpressionNode) expression);
            case IMPLICIT_CAST -> compileImplicitCastExpression(visitor, context, (BoundImplicitCastExpressionNode) expression);
            case CONVERSION -> compileConversionExpression(visitor, context, (BoundConversionNode) expression);
            case NAME_EXPRESSION -> compileNameExpression(visitor, context, (BoundNameExpressionNode) expression);
            case THIS_EXPRESSION -> compileThisExpression(visitor, context, (BoundThisExpressionNode) expression);
            case STATIC_REFERENCE -> compileStaticReferenceExpression();
            case REF_ARGUMENT_EXPRESSION -> compileRefArgumentExpression(visitor, context, (BoundRefArgumentExpressionNode) expression);
            case METHOD_INVOCATION_EXPRESSION -> compileMethodInvocationExpression(visitor, context, (BoundMethodInvocationExpressionNode) expression);
            case BASE_METHOD_INVOCATION_EXPRESSION -> compileBaseMethodInvocationExpression(visitor, context, (BoundBaseMethodInvocationExpressionNode) expression);
            case PROPERTY_ACCESS_EXPRESSION -> compilePropertyAccessExpression(visitor, context, (BoundPropertyAccessExpressionNode) expression);
            case ARRAY_CREATION_EXPRESSION -> compileArrayCreationExpression(visitor, context, (BoundArrayCreationExpressionNode) expression);
            case ARRAY_INITIALIZER_EXPRESSION -> compileArrayInitializerExpression(visitor, context, (BoundArrayInitializerExpressionNode) expression);
            case OBJECT_CREATION_EXPRESSION -> compileObjectCreationExpression(visitor, context, (BoundObjectCreationExpressionNode) expression);
            case COLLECTION_EXPRESSION -> compileCollectionExpression(visitor, context, (BoundCollectionExpressionNode) expression);
            case INDEX_EXPRESSION -> compileIndexExpression(visitor, context, (BoundIndexExpressionNode) expression);
            case LAMBDA_EXPRESSION -> compileLambdaExpression(visitor, context, (BoundLambdaExpressionNode) expression);
            case FUNCTION_INVOCATION -> compileFunctionInvocationExpression(visitor, context, (BoundFunctionInvocationExpression) expression);
            case OBJECT_INVOCATION -> compileVariableInvocation(visitor, context, (BoundObjectInvocationExpression) expression);
            case GENERATOR_GET_VALUE -> compileGeneratorGetValue(visitor, context, (BoundGeneratorGetValueNode) expression);
            case STACK_LOAD -> compileStackLoad(visitor, (BoundStackLoadNode) expression);
            case FUNCTION_AS_LAMBDA -> compileFunctionAsLambda(visitor, context, (BoundFunctionAsLambdaExpressionNode) expression);
            case META_CAST_EXPRESSION -> compileMetaCastExpression(visitor, context, (BoundMetaCastExpressionNode) expression);
            case META_TYPE_EXPRESSION -> compileMetaTypeExpression(visitor, (BoundMetaTypeExpressionNode) expression);
            case META_TYPE_OF_EXPRESSION -> compileMetaTypeOfExpression(visitor, context, (BoundMetaTypeOfExpressionNode) expression);
            default -> throw new InternalException();
        }
    }

    private void compileNull(MethodVisitor visitor) {
        visitor.visitInsn(ACONST_NULL);
    }

    private void compileBooleanLiteral(MethodVisitor visitor, BoundBooleanLiteralExpressionNode literal) {
        visitor.visitInsn(literal.value ? ICONST_1 : ICONST_0);
    }

    private void compileIntegerLiteral(MethodVisitor visitor, BoundIntegerLiteralExpressionNode literal) {
        visitor.visitLdcInsn(literal.value);
    }

    private void compileInteger64Literal(MethodVisitor visitor, BoundInteger64LiteralExpressionNode literal) {
        visitor.visitLdcInsn(literal.value);
    }

    private void compileFloatLiteral(MethodVisitor visitor, BoundFloatLiteralExpressionNode literal) {
        visitor.visitLdcInsn(literal.value);
    }

    private void compileStringLiteral(MethodVisitor visitor, BoundStringLiteralExpressionNode literal) {
        visitor.visitLdcInsn(literal.value);
    }

    private void compileCharLiteral(MethodVisitor visitor, BoundCharLiteralExpressionNode literal) {
        visitor.visitLdcInsn(literal.value);
    }

    private void compileUnaryExpression(MethodVisitor visitor, CompilerContext context, BoundUnaryExpressionNode expression) {
        compileExpression(visitor, context, expression.operand);
        expression.operator.operation.apply(visitor, context);
    }

    private void compileBinaryExpression(MethodVisitor visitor, CompilerContext context, BoundBinaryExpressionNode expression) {
        BufferedMethodVisitor buffer = new BufferedMethodVisitor();
        compileExpression(visitor, context, expression.left);
        compileExpression(buffer, context, expression.right);
        expression.operator.operation.apply(visitor, buffer, context, expression.left.type, expression.right.type);
    }

    private void compileInExpression(MethodVisitor visitor, CompilerContext context, BoundInExpressionNode expression) {
        compileExpression(visitor, context, expression.right);
        compileExpression(visitor, context, expression.left);
        expression.method.compileInvoke(visitor, context);
    }

    private void compileIsExpression(MethodVisitor visitor, CompilerContext context, BoundIsExpressionNode is) {
        compileExpression(visitor, context, is.expression);

        // lower "not" pattern
        boolean not = false;
        BoundPatternNode current = is.pattern;
        while (current instanceof BoundNotPattern notPattern) {
            not = !not;
            current = notPattern.inner;
        }

        // at this stage 'current' cannot be BoundNotPattern
        switch (current.getNodeType()) {
            case CONSTANT_PATTERN -> compileConstantPatternCheck(visitor, is, not, (BoundConstantPatternNode) current);
            case TYPE_PATTERN -> compileTypePatternCheck(visitor, is, not, (BoundTypePatternNode) current);
            case DECLARATION_PATTERN -> compileDeclarationPatternCheck(visitor, context, is, not, (BoundDeclarationPatternNode) current);
            default -> throw new InternalException();
        }
    }

    private void compileConstantPatternCheck(MethodVisitor visitor, BoundIsExpressionNode is, boolean not, BoundConstantPatternNode pattern) {
        switch (pattern.expression.getNodeType()) {
            case NULL_EXPRESSION -> {
                if (is.expression.type instanceof SValueType valueType) {
                    valueType.compileBoxing(visitor);
                }
                Label elseLabel = new Label();
                Label endLabel = new Label();
                visitor.visitJumpInsn(not ? IFNONNULL : IFNULL, elseLabel);
                visitor.visitInsn(ICONST_0);
                visitor.visitJumpInsn(GOTO, endLabel);
                visitor.visitLabel(elseLabel);
                visitor.visitInsn(ICONST_1);
                visitor.visitLabel(endLabel);
            }
            case BOOLEAN_LITERAL -> {
                BoundBooleanLiteralExpressionNode literal = (BoundBooleanLiteralExpressionNode) pattern.expression;
                if (is.expression.type == SBoolean.instance) {
                    loadBoolConstantAndCompare(visitor, not != literal.value);
                } else {
                    if (is.expression.type instanceof SValueType valueType) {
                        valueType.compileBoxing(visitor);
                    }
                    Label canCastLabel = new Label();
                    Label endLabel = new Label();
                    // ..., expr
                    visitor.visitInsn(DUP);
                    // ..., expr, expr
                    visitor.visitTypeInsn(INSTANCEOF, Type.getInternalName(Boolean.class));
                    // ..., expr, bool
                    visitor.visitJumpInsn(IFNE, canCastLabel);
                    // ..., expr
                    visitor.visitInsn(POP);
                    // ...
                    visitor.visitInsn(not ? ICONST_1 : ICONST_0);
                    // ..., false
                    visitor.visitJumpInsn(GOTO, endLabel);

                    visitor.visitLabel(canCastLabel);
                    // ..., expr
                    visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(Boolean.class));
                    // ..., (Boolean)(expr)
                    SBoolean.instance.compileUnboxing(visitor);
                    loadBoolConstantAndCompare(visitor, not != literal.value);

                    visitor.visitLabel(endLabel);
                }
            }
            case INTEGER_LITERAL -> {
                BoundIntegerLiteralExpressionNode literal = (BoundIntegerLiteralExpressionNode) pattern.expression;
                if (is.expression.type == SInt.instance) {
                    loadInt32ConstantAndCompare(visitor, not, literal.value);
                } else {
                    if (is.expression.type instanceof SValueType valueType) {
                        valueType.compileBoxing(visitor);
                    }
                    Label canCastLabel = new Label();
                    Label endLabel = new Label();
                    // ..., expr
                    visitor.visitInsn(DUP);
                    // ..., expr, expr
                    visitor.visitTypeInsn(INSTANCEOF, Type.getInternalName(Integer.class));
                    // ..., expr, bool
                    visitor.visitJumpInsn(IFNE, canCastLabel);
                    // ..., expr
                    visitor.visitInsn(POP);
                    // ...
                    visitor.visitInsn(not ? ICONST_1 : ICONST_0);
                    // ..., false
                    visitor.visitJumpInsn(GOTO, endLabel);

                    visitor.visitLabel(canCastLabel);
                    // ..., expr
                    visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(Integer.class));
                    // ..., (Boolean)(expr)
                    SInt.instance.compileUnboxing(visitor);
                    loadInt32ConstantAndCompare(visitor, not, literal.value);

                    visitor.visitLabel(endLabel);
                }
            }
            case INTEGER64_LITERAL -> {
                BoundInteger64LiteralExpressionNode literal = (BoundInteger64LiteralExpressionNode) pattern.expression;
                if (is.expression.type == SInt.instance) {
                    loadInt64ConstantAndCompare(visitor, not, literal.value);
                } else {
                    if (is.expression.type instanceof SValueType valueType) {
                        valueType.compileBoxing(visitor);
                    }
                    Label canCastLabel = new Label();
                    Label endLabel = new Label();
                    // ..., expr
                    visitor.visitInsn(DUP);
                    // ..., expr, expr
                    visitor.visitTypeInsn(INSTANCEOF, Type.getInternalName(Long.class));
                    // ..., expr, bool
                    visitor.visitJumpInsn(IFNE, canCastLabel);
                    // ..., expr
                    visitor.visitInsn(POP);
                    // ...
                    visitor.visitInsn(not ? ICONST_1 : ICONST_0);
                    // ..., false
                    visitor.visitJumpInsn(GOTO, endLabel);

                    visitor.visitLabel(canCastLabel);
                    // ..., expr
                    visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(Long.class));
                    // ..., (Boolean)(expr)
                    SInt64.instance.compileUnboxing(visitor);
                    loadInt64ConstantAndCompare(visitor, not, literal.value);

                    visitor.visitLabel(endLabel);
                }
            }
            case FLOAT_LITERAL -> {
                BoundFloatLiteralExpressionNode literal = (BoundFloatLiteralExpressionNode) pattern.expression;
                if (is.expression.type == SInt.instance) {
                    loadFloat64ConstantAndCompare(visitor, not, literal.value);
                } else {
                    if (is.expression.type instanceof SValueType valueType) {
                        valueType.compileBoxing(visitor);
                    }
                    Label canCastLabel = new Label();
                    Label endLabel = new Label();
                    // ..., expr
                    visitor.visitInsn(DUP);
                    // ..., expr, expr
                    visitor.visitTypeInsn(INSTANCEOF, Type.getInternalName(Double.class));
                    // ..., expr, bool
                    visitor.visitJumpInsn(IFNE, canCastLabel);
                    // ..., expr
                    visitor.visitInsn(POP);
                    // ...
                    visitor.visitInsn(not ? ICONST_1 : ICONST_0);
                    // ..., false
                    visitor.visitJumpInsn(GOTO, endLabel);

                    visitor.visitLabel(canCastLabel);
                    // ..., expr
                    visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(Double.class));
                    // ..., (Boolean)(expr)
                    SFloat.instance.compileUnboxing(visitor);
                    loadFloat64ConstantAndCompare(visitor, not, literal.value);

                    visitor.visitLabel(endLabel);
                }
            }
            default -> throw new InternalException();
        }
    }

    private void loadBoolConstantAndCompare(MethodVisitor visitor, boolean value) {
        visitor.visitInsn(value ? ICONST_1 : ICONST_0);
        Label elseLabel = new Label();
        Label endLabel = new Label();
        visitor.visitJumpInsn(IF_ICMPEQ, elseLabel);
        visitor.visitInsn(ICONST_0);
        visitor.visitJumpInsn(GOTO, endLabel);
        visitor.visitLabel(elseLabel);
        visitor.visitInsn(ICONST_1);
        visitor.visitLabel(endLabel);
    }

    private void loadInt32ConstantAndCompare(MethodVisitor visitor, boolean negate, int value) {
        visitor.visitLdcInsn(value);
        Label elseLabel = new Label();
        Label endLabel = new Label();
        visitor.visitJumpInsn(negate ? IF_ICMPNE : IF_ICMPEQ, elseLabel);
        visitor.visitInsn(ICONST_0);
        visitor.visitJumpInsn(GOTO, endLabel);
        visitor.visitLabel(elseLabel);
        visitor.visitInsn(ICONST_1);
        visitor.visitLabel(endLabel);
    }

    private void loadInt64ConstantAndCompare(MethodVisitor visitor, boolean negate, long value) {
        visitor.visitLdcInsn(value);
        Label elseLabel = new Label();
        Label endLabel = new Label();
        visitor.visitInsn(LCMP);
        visitor.visitJumpInsn(negate ? IFNE : IFEQ, elseLabel);
        visitor.visitInsn(ICONST_0);
        visitor.visitJumpInsn(GOTO, endLabel);
        visitor.visitLabel(elseLabel);
        visitor.visitInsn(ICONST_1);
        visitor.visitLabel(endLabel);
    }
    private void loadFloat64ConstantAndCompare(MethodVisitor visitor, boolean negate, double value) {
        visitor.visitLdcInsn(value);
        Label elseLabel = new Label();
        Label endLabel = new Label();
        visitor.visitInsn(DCMPL); // TODO: NaN?
        visitor.visitJumpInsn(negate ? IFNE : IFEQ, elseLabel);
        visitor.visitInsn(ICONST_0);
        visitor.visitJumpInsn(GOTO, endLabel);
        visitor.visitLabel(elseLabel);
        visitor.visitInsn(ICONST_1);
        visitor.visitLabel(endLabel);
    }

    private void compileTypePatternCheck(MethodVisitor visitor, BoundIsExpressionNode is, boolean not, BoundTypePatternNode pattern) {
        if (is.expression.type instanceof SValueType valueType) {
            valueType.compileBoxing(visitor);
        }

        if (pattern.typeNode.type instanceof SValueType valueType) {
            visitor.visitTypeInsn(INSTANCEOF, valueType.getBoxed().getInternalName());
        } else {
            visitor.visitTypeInsn(INSTANCEOF, pattern.typeNode.type.getInternalName());
        }

        if (not) {
            visitor.visitInsn(ICONST_1);
            visitor.visitInsn(IXOR);
        }
    }

    private void compileDeclarationPatternCheck(MethodVisitor visitor, CompilerContext context, BoundIsExpressionNode is, boolean not, BoundDeclarationPatternNode pattern) {
        if (is.expression.type instanceof SValueType valueType) {
            valueType.compileBoxing(visitor);
        }

        // ..., <expr>
        visitor.visitInsn(DUP);
        // ..., <expr>, <expr>

        String castToType;
        if (pattern.typeNode.type instanceof SValueType valueType) {
            castToType = valueType.getBoxed().getInternalName();
        } else {
            castToType = pattern.typeNode.type.getInternalName();
        }

        visitor.visitTypeInsn(INSTANCEOF, castToType);
        // ..., <expr>, <instanceof>
        visitor.visitInsn(DUP);
        // ..., <expr>, <instanceof>, <instanceof>

        Label elseLabel = new Label();
        Label endLabel = new Label();
        visitor.visitJumpInsn(IFEQ, elseLabel);
        // ..., <expr>, <instanceof>
        visitor.visitInsn(SWAP);
        // ..., <instanceof>, <expr>
        visitor.visitTypeInsn(CHECKCAST, castToType);
        // ..., <instanceof>, <casted>
        if (pattern.typeNode.type instanceof SValueType valueType) {
            valueType.compileUnboxing(visitor);
        }
        pattern.symbolNode.symbolRef.asVariable().compileStore(context, visitor);
        // ..., <instanceof>
        visitor.visitJumpInsn(GOTO, endLabel);

        visitor.visitLabel(elseLabel);
        // ..., <expr>, <instanceof>
        visitor.visitInsn(SWAP);
        // ..., <instanceof>, <expr>
        visitor.visitInsn(POP);
        // ..., <instanceof>
        visitor.visitLabel(endLabel);

        if (not) {
            visitor.visitInsn(ICONST_1);
            visitor.visitInsn(IXOR);
        }
    }

    private void compileTypeCastExpression(MethodVisitor visitor, CompilerContext context, BoundTypeCastExpressionNode test) {
        compileExpression(visitor, context, test.expression);
        if (test.expression.type instanceof SValueType valueType) {
            valueType.compileBoxing(visitor);
        }

        String referenceTypeInternalName;
        if (test.type.type instanceof SValueType valueType) {
            referenceTypeInternalName = valueType.getBoxed().getInternalName();
        } else {
            referenceTypeInternalName = test.type.type.getInternalName();
        }

        Label canCastLabel = new Label();
        Label endLabel = new Label();

        // ..., expr
        visitor.visitInsn(DUP); // expression on the stack is always 1 stack size, because if it's not we box possible long/double value
        // ..., expr, expr
        visitor.visitTypeInsn(INSTANCEOF, referenceTypeInternalName);
        // ..., expr, bool
        visitor.visitJumpInsn(IFNE, canCastLabel);
        // ..., expr
        visitor.visitInsn(POP);
        // ...
        if (test.type.type.isReference()) {
            visitor.visitInsn(ACONST_NULL);
        } else {
            test.type.type.storeDefaultValue(visitor);
        }
        // ..., NULL/default
        visitor.visitJumpInsn(GOTO, endLabel);

        visitor.visitLabel(canCastLabel);
        // ..., expr
        visitor.visitTypeInsn(CHECKCAST, referenceTypeInternalName);
        // ..., casted_expr
        if (test.type.type instanceof SValueType valueType) {
            valueType.compileUnboxing(visitor);
        }

        visitor.visitLabel(endLabel);
    }

    private void compileConditionalExpression(MethodVisitor visitor, CompilerContext context, BoundConditionalExpressionNode expression) {
        compileExpression(visitor, context, expression.condition);

        Label elseLabel = new Label();
        visitor.visitJumpInsn(IFEQ, elseLabel);
        compileExpression(visitor, context, expression.whenTrue);
        Label endLabel = new Label();
        visitor.visitJumpInsn(GOTO, endLabel);
        visitor.visitLabel(elseLabel);
        compileExpression(visitor, context, expression.whenFalse);
        visitor.visitLabel(endLabel);
    }

    private void compileImplicitCastExpression(MethodVisitor visitor, CompilerContext context, BoundImplicitCastExpressionNode expression) {
        compileExpression(visitor, context, expression.operand);
        expression.operation.apply(visitor);
    }

    private void compileConversionExpression(MethodVisitor visitor, CompilerContext context, BoundConversionNode expression) {
        switch (expression.conversionInfo.type()) {
            case IDENTITY -> compileExpression(visitor, context, expression.expression);

            case IMPLICIT_CAST -> compileImplicitCastConversion(visitor, context, expression);

            case FUNCTION_TO_INTERFACE -> compileFunctionToInterfaceConversion(
                    visitor,
                    context,
                    Objects.requireNonNull(expression.conversionInfo.function()),
                    (SFunctionalInterface) expression.type);

            case FUNCTION_TO_GENERIC -> compileFunctionToGeneric(
                    visitor,
                    context,
                    Objects.requireNonNull(expression.conversionInfo.function()),
                    (SGenericFunction) expression.type);

            case METHOD_GROUP_TO_INTERFACE -> compileInstanceMethodToInterface(
                    visitor,
                    context,
                    (BoundMethodGroupExpressionNode) expression.expression,
                    Objects.requireNonNull(expression.conversionInfo.method()),
                    (SFunctionalInterface) expression.type);

            case METHOD_GROUP_TO_GENERIC -> compileInstanceMethodToGeneric(
                    visitor,
                    context,
                    (BoundMethodGroupExpressionNode) expression.expression,
                    Objects.requireNonNull(expression.conversionInfo.method()),
                    (SGenericFunction) expression.type);

            default -> throw new InternalException();
        }
    }

    private void compileImplicitCastConversion(MethodVisitor visitor, CompilerContext context, BoundConversionNode expression) {
        compileExpression(visitor, context, expression.expression);
        expression.conversionInfo.cast().apply(visitor);
    }

    private void compileFunctionToInterfaceConversion(MethodVisitor visitor, CompilerContext context, Function function, SFunctionalInterface functionalInterface) {
        Handle bsm = new Handle(
                H_INVOKESTATIC,
                Type.getInternalName(LambdaMetafactory.class),
                "metafactory",
                "(Ljava/lang/invoke/MethodHandles$Lookup;"
                        + "Ljava/lang/String;"
                        + "Ljava/lang/invoke/MethodType;"
                        + "Ljava/lang/invoke/MethodType;"
                        + "Ljava/lang/invoke/MethodHandle;"
                        + "Ljava/lang/invoke/MethodType;)"
                        + "Ljava/lang/invoke/CallSite;",
                false);

        Handle impl = new Handle(
                H_INVOKESTATIC,
                context.getClassName(),
                function.getName(),
                function.getFunctionType().getMethodDescriptor(),
                false);

        visitor.visitInvokeDynamicInsn(
                functionalInterface.getMethodName(),
                Type.getMethodDescriptor(Type.getType(functionalInterface.getJavaClass())),
                bsm,
                Type.getType(functionalInterface.getRawMethodDescriptor()),
                impl,
                Type.getType(functionalInterface.getIntermediateMethodDescriptor()));
    }

    private void compileFunctionToGeneric(MethodVisitor visitor, CompilerContext context, Function function, SGenericFunction genericFunction) {
        Handle bsm = new Handle(
                H_INVOKESTATIC,
                Type.getInternalName(LambdaMetafactory.class),
                "metafactory",
                "(Ljava/lang/invoke/MethodHandles$Lookup;"
                        + "Ljava/lang/String;"
                        + "Ljava/lang/invoke/MethodType;"
                        + "Ljava/lang/invoke/MethodType;"
                        + "Ljava/lang/invoke/MethodHandle;"
                        + "Ljava/lang/invoke/MethodType;)"
                        + "Ljava/lang/invoke/CallSite;",
                false);

        Handle impl = new Handle(
                H_INVOKESTATIC,
                context.getClassName(),
                function.getName(),
                genericFunction.getMethodDescriptor(),
                false);

        visitor.visitInvokeDynamicInsn(
                genericFunction.getMethodName(),
                Type.getMethodDescriptor(Type.getObjectType(genericFunction.getInternalName())),
                bsm,
                Type.getType(genericFunction.getMethodDescriptor()),
                impl,
                Type.getType(genericFunction.getMethodDescriptor()));
    }

    private void compileInstanceMethodToInterface(MethodVisitor visitor, CompilerContext context, BoundMethodGroupExpressionNode methodGroupNode, MethodReference method, SFunctionalInterface functionalInterface) {
        Handle bsm = new Handle(
                H_INVOKESTATIC,
                Type.getInternalName(LambdaMetafactory.class),
                "metafactory",
                "(Ljava/lang/invoke/MethodHandles$Lookup;"
                        + "Ljava/lang/String;"
                        + "Ljava/lang/invoke/MethodType;"
                        + "Ljava/lang/invoke/MethodType;"
                        + "Ljava/lang/invoke/MethodHandle;"
                        + "Ljava/lang/invoke/MethodType;)"
                        + "Ljava/lang/invoke/CallSite;",
                false);

        Handle impl = new Handle(
                H_INVOKEVIRTUAL,
                method.getOwner().getInternalName(),
                method.getName(),
                method.getDescriptor(),
                false);

        compileExpression(visitor, context, methodGroupNode.callee);
        visitor.visitInvokeDynamicInsn(
                functionalInterface.getMethodName(),
                Type.getMethodDescriptor(
                        Type.getObjectType(functionalInterface.getInternalName()),
                        Type.getObjectType(method.getOwner().getInternalName())),
                bsm,
                Type.getType(functionalInterface.getRawMethodDescriptor()),
                impl,
                Type.getType(functionalInterface.getIntermediateMethodDescriptor()));
    }

    private void compileInstanceMethodToGeneric(MethodVisitor visitor, CompilerContext context, BoundMethodGroupExpressionNode methodGroupNode, MethodReference method, SGenericFunction genericFunction) {
        Handle bsm = new Handle(
                H_INVOKESTATIC,
                Type.getInternalName(LambdaMetafactory.class),
                "metafactory",
                "(Ljava/lang/invoke/MethodHandles$Lookup;"
                        + "Ljava/lang/String;"
                        + "Ljava/lang/invoke/MethodType;"
                        + "Ljava/lang/invoke/MethodType;"
                        + "Ljava/lang/invoke/MethodHandle;"
                        + "Ljava/lang/invoke/MethodType;)"
                        + "Ljava/lang/invoke/CallSite;",
                false);

        Handle impl = new Handle(
                H_INVOKEVIRTUAL,
                method.getOwner().getInternalName(),
                method.getName(),
                method.getDescriptor(),
                false);

        compileExpression(visitor, context, methodGroupNode.callee);
        visitor.visitInvokeDynamicInsn(
                genericFunction.getMethodName(),
                Type.getMethodDescriptor(
                        Type.getObjectType(genericFunction.getInternalName()),
                        Type.getObjectType(method.getOwner().getInternalName())),
                bsm,
                Type.getType(genericFunction.getMethodDescriptor()),
                impl,
                Type.getType(genericFunction.getMethodDescriptor()));
    }

    private void compileStaticReferenceExpression() {

    }

    private void compileNameExpression(MethodVisitor visitor, CompilerContext context, BoundNameExpressionNode expression) {
        if (expression.getSymbol() instanceof Variable variable) {
            variable.compileLoad(context, visitor);
        } else {
            throw new InternalException("Not implemented.");
        }
    }

    private void compileThisExpression(MethodVisitor visitor, CompilerContext context, BoundThisExpressionNode expression) {
        String fieldName = context.getAsyncThisFieldName();
        if (fieldName == null) {
            if (context.isExtension()) {
                visitor.visitVarInsn(expression.type.getLoadInst(), 0);
            } else {
                visitor.visitVarInsn(ALOAD, 0);
            }
        } else {
            visitor.visitVarInsn(ALOAD, 0);
            visitor.visitFieldInsn(GETFIELD, context.getAsyncStateMachineClassName(), fieldName, expression.type.getDescriptor());
        }
    }

    private void compileRefArgumentExpression(MethodVisitor visitor, CompilerContext context, BoundRefArgumentExpressionNode expression) {
        Variable variable = expression.name.symbolRef.asVariable();
        LocalVariable holder = expression.holder;
        context.setStackIndex(holder);

        String refClassDescriptor = holder.getType().getInternalName();
        visitor.visitTypeInsn(NEW, refClassDescriptor);
        // ..., Ref
        visitor.visitInsn(DUP);
        // ..., Ref, Ref
        visitor.visitInsn(DUP);
        // ..., Ref, Ref, Ref
        variable.compileLoad(context, visitor);
        // ..., Ref, Ref, Ref, value
        visitor.visitMethodInsn(
                INVOKESPECIAL,
                refClassDescriptor,
                "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(variable.getType().getJavaClass())),
                false);
        // ..., Ref, Ref
        holder.compileStore(context, visitor);
        // ..., Ref
    }

    private void compileMethodInvocationExpression(MethodVisitor visitor, CompilerContext context, BoundMethodInvocationExpressionNode invocation) {
        compileExpression(visitor, context, invocation.objectReference);
        for (BoundExpressionNode expression : invocation.arguments.arguments) {
            compileExpression(visitor, context, expression);
        }
        invocation.method.method.compileInvoke(visitor, context);
        releaseRefVariables(visitor, context, invocation.refVariables);
    }

    private void compileBaseMethodInvocationExpression(MethodVisitor visitor, CompilerContext context, BoundBaseMethodInvocationExpressionNode invocation) {
        visitor.visitVarInsn(ALOAD, 0);
        for (BoundExpressionNode expression : invocation.arguments.arguments) {
            compileExpression(visitor, context, expression);
        }

        // TODO: find better way?
        invocation.method.method.compileInvoke(new MethodVisitor(ASM9, visitor) {
            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                if (opcode == INVOKEVIRTUAL) {
                    super.visitMethodInsn(INVOKESPECIAL, owner, name, descriptor, isInterface);
                } else {
                    throw new InternalException();
                }
            }
        }, context);

        releaseRefVariables(visitor, context, invocation.refVariables);
    }

    private void compilePropertyAccessExpression(MethodVisitor visitor, CompilerContext context, BoundPropertyAccessExpressionNode propertyAccess) {
        if (!propertyAccess.property.property.canGet()) {
            throw new InternalException();
        }

        compileExpression(visitor, context, propertyAccess.callee);

        if (propertyAccess.property.property.isPublic()) {
            propertyAccess.property.property.compileGet(visitor);
        } else {
            String varHandleFieldName = context.createCachedPrivateMethodHandle(propertyAccess.property.property);
            visitor.visitFieldInsn(
                    GETSTATIC,
                    MethodHandleCache.CLASS_NAME,
                    varHandleFieldName,
                    Type.getDescriptor(VarHandle.class));
            visitor.visitInsn(SWAP);
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(VarHandle.class),
                    "get",
                    Type.getMethodDescriptor(
                            propertyAccess.property.property.getType().getAsmType(),
                            propertyAccess.callee.type.getAsmType()),
                    false);
        }
    }

    private void compileArrayCreationExpression(MethodVisitor visitor, CompilerContext context, BoundArrayCreationExpressionNode expression) {
        compileExpression(visitor, context, expression.lengthExpression);

        SArrayType arrayType = (SArrayType) expression.type;
        SType elementsType = arrayType.getElementsType();
        if (elementsType instanceof SValueType valueType) {
            visitor.visitIntInsn(NEWARRAY, valueType.getArrayTypeInst());
        } else {
            visitor.visitTypeInsn(ANEWARRAY, elementsType.getInternalName());
        }
    }

    private void compileArrayInitializerExpression(MethodVisitor visitor, CompilerContext context, BoundArrayInitializerExpressionNode expression) {
        visitor.visitLdcInsn(expression.items.size());

        SArrayType arrayType = (SArrayType) expression.type;
        SType elementsType = arrayType.getElementsType();
        if (elementsType instanceof SValueType valueType) {
            visitor.visitIntInsn(NEWARRAY, valueType.getArrayTypeInst());
        } else {
            visitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(elementsType.getJavaClass()));
        }

        for (int i = 0; i < expression.items.size(); i++) {
            visitor.visitInsn(DUP);
            visitor.visitLdcInsn(i);
            compileExpression(visitor, context, expression.items.get(i));
            visitor.visitInsn(elementsType.getArrayStoreInst());
        }
    }

    private void compileObjectCreationExpression(MethodVisitor visitor, CompilerContext context, BoundObjectCreationExpressionNode creation) {
        visitor.visitTypeInsn(NEW, creation.typeNode.type.getInternalName());
        visitor.visitInsn(DUP);
        for (BoundExpressionNode expression : creation.arguments.arguments) {
            compileExpression(visitor, context, expression);
        }
        creation.constructor.compileInvoke(visitor);
    }

    private void compileCollectionExpression(MethodVisitor visitor, CompilerContext context, BoundCollectionExpressionNode expression) {
        visitor.visitLdcInsn(expression.list.size());

        SArrayType arrayType = (SArrayType) expression.type;
        SType elementsType = arrayType.getElementsType();
        if (elementsType instanceof SValueType valueType) {
            visitor.visitIntInsn(NEWARRAY, valueType.getArrayTypeInst());
        } else {
            visitor.visitTypeInsn(ANEWARRAY, elementsType.getInternalName());
        }

        for (int i = 0; i < expression.list.size(); i++) {
            visitor.visitInsn(DUP);
            visitor.visitLdcInsn(i);
            compileExpression(visitor, context, expression.list.get(i));
            visitor.visitInsn(elementsType.getArrayStoreInst());
        }
    }

    private void compileIndexExpression(MethodVisitor visitor, CompilerContext context, BoundIndexExpressionNode expression) {
        compileExpression(visitor, context, expression.callee);
        compileExpression(visitor, context, expression.index);
        expression.operation.compileGet(visitor);
    }

    private void compileLambdaExpression(MethodVisitor visitor, CompilerContext context, BoundLambdaExpressionNode expression) {
        String methodName;
        SType rawReturnType;
        SType actualReturnType;
        SType[] rawParameters;
        SType[] actualParameters;
        String rawMethodDescriptor;

        SFunction functionType = (SFunction) expression.type;
        if (functionType instanceof SFunctionalInterface functionalInterface) {
            methodName = functionalInterface.getMethodName();
            rawReturnType = functionalInterface.getRawReturnType();
            actualReturnType = functionalInterface.getActualReturnType();
            rawParameters = functionalInterface.getRawParameters();
            actualParameters = functionalInterface.getActualParameters();
            rawMethodDescriptor = functionalInterface.getRawMethodDescriptor();

        } else if (functionType instanceof SGenericFunction genericFunction) {
            methodName = genericFunction.getMethodName();
            rawReturnType = actualReturnType = genericFunction.getReturnType();
            rawParameters = actualParameters = genericFunction.getParameterTypes().toArray(new SType[0]);
            rawMethodDescriptor = genericFunction.getMethodDescriptor();
        } else {
            throw new InternalException();
        }

        int[] paramStackIndexes = StackHelper.buildStackIndexes(rawParameters);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        emitSourceFile(writer);
        String name = "com/zergatul/scripting/dynamic/DynamicLambdaClass_" + context.getNextUniqueIndex();
        writer.visit(
                V1_8,
                ACC_PUBLIC,
                name,
                null,
                Type.getInternalName(Object.class),
                new String[] { functionType.getInternalName() });

        List<Variable> closures = new ArrayList<>();
        for (CapturedVariable captured : expression.captured) {
            if (captured.getUnderlying() instanceof CapturedVariable inner) {
                // captured from another context
                context.addLocalVariable(inner.getClosure());
            }
            Variable closure = context.getVariableOfType(captured.getClosureClassName());
            if (closure == null) {
                throw new InternalException();
            }
            if (!closures.contains(closure)) {
                closures.add(closure);
            }
        }

        String constructorDescriptor = buildLambdaConstructor(name, writer, closures);

        MethodVisitor invokeVisitor = writer.visitMethod(
                ACC_PUBLIC,
                methodName,
                rawMethodDescriptor,
                null,
                null);
        invokeVisitor.visitCode();

        CompilerContext lambdaContext = context.createInstanceMethod(actualReturnType, false, !actualReturnType.equals(rawReturnType));
        lambdaContext.setClassName(name);

        processContextStart(invokeVisitor, lambdaContext);

        if (!expression.captured.isEmpty()) {
            FieldVariable[] closureFieldVariables = new FieldVariable[closures.size()];
            for (int i = 0; i < closures.size(); i++) {
                Variable closure = closures.get(i);
                closureFieldVariables[i] = new FieldVariable(closure.getType(), name, "closure" + i);
            }

            for (CapturedVariable captured : expression.captured) {
                Variable closure = context.getVariableOfType(captured.getClosureClassName());
                int index = closures.indexOf(closure);
                if (index < 0) {
                    throw new InternalException();
                }

                captured.getClosure().set(closureFieldVariables[index]);
            }
        }

        LocalVariable[] arguments = new LocalVariable[expression.parameters.size()];
        for (int i = 0; i < expression.parameters.size(); i++) {
            SymbolRef symbolRef = lambdaContext.addLocalVariable(null, SJavaObject.instance, null);
            arguments[i] = symbolRef.asLocalVariable();
            lambdaContext.setStackIndex(arguments[i]);
        }
        for (int i = 0; i < expression.parameters.size(); i++) {
            SType raw = rawParameters[i];
            SType actual = actualParameters[i];
            if (!raw.equals(actual)) {
                BoundParameterNode parameter = expression.parameters.get(i);
                LocalVariable unboxed = parameter.getName().symbolRef.asLocalVariable();
                lambdaContext.addLocalVariable(parameter.getName().symbolRef);
                lambdaContext.setStackIndex(unboxed);

                arguments[i].compileLoad(context, invokeVisitor); // load argument
                if (parameter.getType() instanceof SValueType valueType) {
                    invokeVisitor.visitTypeInsn(CHECKCAST, valueType.getBoxed().getInternalName()); // cast to boxed, example: java.lang.Integer
                    valueType.compileUnboxing(invokeVisitor); // convert to unboxed
                } else {
                    invokeVisitor.visitTypeInsn(CHECKCAST, parameter.getType().getInternalName());
                }
                unboxed.compileStore(context, invokeVisitor);
            } else {
                Variable variable = expression.parameters.get(i).getName().symbolRef.asVariable();
                LocalVariable localVariable;
                if (variable instanceof LocalVariable) {
                    localVariable = (LocalVariable) variable;
                } else if (variable instanceof LiftedVariable lifted) {
                    localVariable = lifted.getUnderlying();
                } else {
                    throw new InternalException();
                }
                lambdaContext.addLocalVariable(expression.parameters.get(i).getName().symbolRef);
                localVariable.setStackIndex(paramStackIndexes[i]);
            }
        }
        if (!expression.lifted.isEmpty()) {
            compileClosureClass(invokeVisitor, lambdaContext, expression.lifted);
        }

        compileStatement(invokeVisitor, lambdaContext, expression.body);
        if (!functionType.isFunction()) {
            invokeVisitor.visitInsn(RETURN);
        }

        processContextEnd(invokeVisitor, lambdaContext);
        invokeVisitor.visitMaxs(0, 0);
        invokeVisitor.visitEnd();

        writer.visitEnd();

        byte[] bytecode = writer.toByteArray();
        saveClassFile(name, bytecode);

        context.defineClass(name.replace('/', '.'), bytecode);

        visitor.visitTypeInsn(NEW, name);
        visitor.visitInsn(DUP);
        for (Variable closure : closures) {
            closure.compileLoad(context, visitor);
        }
        visitor.visitMethodInsn(
                INVOKESPECIAL,
                name,
                "<init>",
                constructorDescriptor,
                false);
    }

    private void compileFunctionInvocationExpression(MethodVisitor visitor, CompilerContext context, BoundFunctionInvocationExpression expression) {
        for (BoundExpressionNode argument : expression.arguments.arguments) {
            compileExpression(visitor, context, argument);
        }

        Function symbol = expression.functionNode.function;
        SStaticFunction type = symbol.getFunctionType();
        visitor.visitMethodInsn(
                INVOKESTATIC,
                context.getClassName(),
                symbol.getName(),
                type.getMethodDescriptor(),
                false);

        releaseRefVariables(visitor, context, expression.refVariables);
    }

    private void compileVariableInvocation(MethodVisitor visitor, CompilerContext context, BoundObjectInvocationExpression expression) {
        compileExpression(visitor, context, expression.callee);

        for (BoundExpressionNode argument : expression.arguments.arguments) {
            compileExpression(visitor, context, argument);
        }

        SGenericFunction genericFunction = (SGenericFunction) expression.callee.type;
        visitor.visitMethodInsn(
                INVOKEINTERFACE,
                genericFunction.getInternalName(),
                genericFunction.getMethodName(),
                genericFunction.getMethodDescriptor(),
                true);
    }

    private void compileGeneratorGetValue(MethodVisitor visitor, CompilerContext context, BoundGeneratorGetValueNode node) {
        if (node.type == SVoidType.instance) {
            return;
        }

        SymbolRef parameter = context.getSymbol("@result");
        parameter.get().compileLoad(context, visitor);

        if (node.type instanceof SValueType valueType) {
            visitor.visitTypeInsn(CHECKCAST, valueType.getBoxed().getInternalName());
            valueType.compileUnboxing(visitor);
        } else {
            visitor.visitTypeInsn(CHECKCAST, node.type.getInternalName());
        }
    }

    private void compileStackLoad(MethodVisitor visitor, BoundStackLoadNode node) {
        visitor.visitVarInsn(node.type.getLoadInst(), node.index);
    }

    private void compileFunctionAsLambda(MethodVisitor visitor, CompilerContext context, BoundFunctionAsLambdaExpressionNode node) {
        Function function = (Function) node.name.getSymbolOrThrow();
        SStaticFunction type = function.getFunctionType();
        List<BoundParameterNode> parameters = new ArrayList<>(type.getParameters().size());
        List<LocalVariable> variables = new ArrayList<>(type.getParameters().size());
        CompilerContext lambdaContext = context.createInstanceMethod(type.getReturnType(), false, true);
        for (SType parameterType : type.getParameterTypes()) {
            SymbolRef symbolRef = lambdaContext.addLocalVariable("p" + variables.size(), parameterType, null);
            LocalVariable variable = symbolRef.asLocalVariable();
            lambdaContext.setStackIndex(variable);
            variables.add(variable);
            parameters.add(new BoundParameterNode(
                    new BoundNameExpressionNode(variable),
                    parameterType));
        }

        List<BoundExpressionNode> arguments = new ArrayList<>(type.getParameters().size());
        for (LocalVariable variable : variables) {
            arguments.add(new BoundNameExpressionNode(variable));
        }

        BoundFunctionInvocationExpression invocation = new BoundFunctionInvocationExpression(
                new BoundFunctionNode(node.name.syntaxNode, node.name.symbolRef.asFunction()),
                type.getReturnType(),
                new BoundArgumentsListNode(arguments));
        BoundStatementNode statement = type.getReturnType() == SVoidType.instance ?
                new BoundExpressionStatementNode(invocation) :
                new BoundReturnStatementNode(invocation);
        BoundLambdaExpressionNode lambda = new BoundLambdaExpressionNode(parameters, statement, node.type);
        compileLambdaExpression(visitor, context, lambda);
    }

    private void compileMetaCastExpression(MethodVisitor visitor, CompilerContext context, BoundMetaCastExpressionNode node) {
        compileExpression(visitor, context, node.expression);

        if (node.expression.type instanceof SValueType valueType) {
            valueType.compileBoxing(visitor);
        }

        if (node.type.type instanceof SValueType valueType) {
            visitor.visitTypeInsn(CHECKCAST, valueType.getBoxed().getInternalName());
        } else {
            visitor.visitTypeInsn(CHECKCAST, node.type.type.getInternalName());

        }

        if (node.type.type instanceof SValueType valueType) {
            valueType.compileUnboxing(visitor);
        }
    }

    private void compileMetaTypeExpression(MethodVisitor visitor, BoundMetaTypeExpressionNode node) {
        node.type.type.loadClassObject(visitor);
        visitor.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeTypes.class),
                "get",
                Type.getMethodDescriptor(Type.getType(RuntimeType.class), Type.getType(Class.class)),
                false);
    }

    private void compileMetaTypeOfExpression(MethodVisitor visitor, CompilerContext context, BoundMetaTypeOfExpressionNode node) {
        if (node.expression.type.isReference()) {
            compileExpression(visitor, context, node.expression);
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(Object.class),
                    "getClass",
                    Type.getMethodDescriptor(Type.getType(Class.class)),
                    false);
        } else {
            node.expression.type.loadClassObject(visitor);
        }

        visitor.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(RuntimeTypes.class),
                "get",
                Type.getMethodDescriptor(Type.getType(RuntimeType.class), Type.getType(Class.class)),
                false);
    }

    private void releaseRefVariables(MethodVisitor visitor, CompilerContext context, List<RefHolder> refs) {
        for (RefHolder ref : refs) {
            LocalVariable holder = ref.getHolder();
            Variable variable = ref.getVariable();
            holder.compileLoad(context, visitor);
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    holder.getType().getInternalName(),
                    "get",
                    Type.getMethodDescriptor(Type.getType(variable.getType().getJavaClass())),
                    false);
            variable.compileStore(context, visitor);
        }
    }

    private void emitSourceFile(ClassWriter writer) {
        if (parameters.getSourceFile() != null) {
            writer.visitSource(parameters.getSourceFile(), null);
        }
    }

    private void emitLineNumber(MethodVisitor visitor, CompilerContext context, Locatable locatable) {
        if (parameters.shouldEmitLineNumbers()) {
            TextRange range = locatable.getRange();
            if (range == TextRange.MISSING) {
                return;
            }
            int line = range.getLine1();
            if (context.getLastEmittedLine() != line) {
                Label label = new Label();
                visitor.visitLabel(label);
                visitor.visitLineNumber(line, label);
                context.setLastEmittedLine(line);
            }
        }
    }

    private void saveClassFile(String name, byte[] bytecode) {
        if (parameters.isDebug()) {
            String[] parts = name.split("/");
            try {
                Files.write(Path.of(parts[parts.length - 1] + ".class"), bytecode);
            } catch (IOException e) {
                throw new RuntimeException("Cannot write class file.", e);
            }
        }
    }

    private CompilerContext createChildContext(MethodVisitor visitor, CompilerContext context) {
        context = context.createChild();
        processContextStart(visitor, context);
        return context;
    }

    private void processContextStart(MethodVisitor visitor, CompilerContext context) {
        if (parameters.shouldEmitVariableNames()) {
            Label label = new Label();
            context.setStartLabel(label);
            visitor.visitLabel(label);
        }
    }

    private void processContextEnd(MethodVisitor visitor, CompilerContext context) {
        processContextEnd(visitor, context, List.of());
    }

    private void processContextEnd(MethodVisitor visitor, CompilerContext context, List<LocalVariable> variables) {
        if (parameters.shouldEmitVariableNames()) {
            context.emitLocalVariableTable(visitor, variables);
        }
    }

    private void initVariableWithRawDefault(MethodVisitor visitor, LocalVariable variable) {
        if (variable.getType().isReference()) {
            visitor.visitInsn(ACONST_NULL);
        } else {
            variable.getType().storeDefaultValue(visitor);
        }
        visitor.visitVarInsn(variable.getType().getStoreInst(), variable.getStackIndex());
    }

    private void compileMethodHandleCache(CompilerContext context) {
        if (context.getMethodHandleCache() == null) {
            return;
        }

        MethodHandleCache cache = context.getMethodHandleCache();

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        emitSourceFile(writer);
        writer.visit(
                V1_8,
                ACC_PUBLIC,
                MethodHandleCache.CLASS_NAME,
                null,
                Type.getInternalName(Object.class),
                null);

        // fields
        for (var entry : cache.getFieldsMap().entrySet()) {
            String name = entry.getValue();
            FieldVisitor fieldVisitor = writer.visitField(
                    ACC_PUBLIC | ACC_STATIC,
                    name,
                    Type.getDescriptor(VarHandle.class),
                    null, null);
            fieldVisitor.visitEnd();
        }

        // static constructor
        MethodVisitor visitor = writer.visitMethod(ACC_STATIC, "<clinit>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
        visitor.visitCode();

        visitor.visitMethodInsn(
                INVOKESTATIC,
                Type.getInternalName(MethodHandles.class),
                "lookup",
                Type.getMethodDescriptor(Type.getType(MethodHandles.Lookup.class)),
                false);
        visitor.visitVarInsn(ASTORE, 0);

        for (var entry : cache.getFieldsMap().entrySet()) {
            Field field = entry.getKey();
            String name = entry.getValue();

            visitor.visitLdcInsn(Type.getType(field.getDeclaringClass()));
            visitor.visitVarInsn(ALOAD, 0);
            visitor.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getInternalName(MethodHandles.class),
                    "privateLookupIn",
                    Type.getMethodDescriptor(Type.getType(MethodHandles.Lookup.class), Type.getType(Class.class), Type.getType(MethodHandles.Lookup.class)),
                    false);

            visitor.visitLdcInsn(Type.getType(field.getDeclaringClass()));
            visitor.visitLdcInsn(field.getName());
            SType.fromJavaType(field.getType()).loadClassObject(visitor);
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(MethodHandles.Lookup.class),
                    "findVarHandle",
                    Type.getMethodDescriptor(
                            Type.getType(VarHandle.class),
                            Type.getType(Class.class),
                            Type.getType(String.class),
                            Type.getType(Class.class)),
                    false);

            visitor.visitFieldInsn(
                    PUTSTATIC,
                    MethodHandleCache.CLASS_NAME,
                    name,
                    Type.getDescriptor(VarHandle.class));
        }

        visitor.visitInsn(RETURN);
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();

        byte[] bytecode = writer.toByteArray();
        saveClassFile("MethodHandleCache", bytecode);

        context.defineClass(MethodHandleCache.CLASS_NAME.replace('/', '.'), bytecode);
    }
}