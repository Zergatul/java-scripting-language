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
import com.zergatul.scripting.parser.NodeType;
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
import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.*;

public class Compiler {

    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final DynamicCompilerClassLoader classLoader = new DynamicCompilerClassLoader();

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
        String name =
                "com/zergatul/scripting/dynamic/" +
                (parameters.getClassNamePrefix() != null ? parameters.getClassNamePrefix() : "DynamicClass_") +
                counter.incrementAndGet();
        writer.visit(
                V1_8,
                ACC_PUBLIC,
                name,
                null,
                Type.getInternalName(Object.class),
                new String[] { Type.getInternalName(parameters.getFunctionalInterface()) });

        CompilerContext context = parameters.getContext();
        context.setClassName(name);

        context.copyGenericFunctionsFrom(output.context());

        compileCompilationUnitMembers(unit, writer, context);
        buildEmptyConstructor(writer);
        buildMainMethod(unit, writer, name);

        writer.visitEnd();

        byte[] bytecode = writer.toByteArray();
        saveClassFile(name, bytecode);

        @SuppressWarnings("unchecked")
        Class<T> dynamic = (Class<T>) classLoader.defineClass(name.replace('/', '.'), bytecode);
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
        List<BoundFunctionNode> functions = new ArrayList<>();
        List<BoundClassNode> classes = new ArrayList<>();
        for (BoundCompilationUnitMemberNode member : unit.members.members) {
            switch (member.getNodeType()) {
                case STATIC_VARIABLE -> fields.add((BoundStaticVariableNode) member);
                case FUNCTION -> functions.add((BoundFunctionNode) member);
                case CLASS -> classes.add((BoundClassNode) member);
                default -> throw new InternalException();
            }
        }

        compileClasses(classes, writer, context);
        compileGenericFunctions(context.getGenericFunctions());
        compileStaticVariables(fields, writer, context);
        compileFunctions(functions, writer, context);
    }

    private void compileClasses(List<BoundClassNode> classNodes, ClassWriter writer, CompilerContext context) {
        // setup class names in advance for forward references
        for (BoundClassNode classNode : classNodes) {
            String name = context.getClassName() + "$" + classNode.name.value;
            ((SDeclaredType) classNode.name.getSymbol().getType()).setInternalName(name);
        }

        // setup generic functions in advance for the same reason
        setupGenericFunctions(context.getGenericFunctions());

        for (BoundClassNode classNode : classNodes) {
            String name = classNode.name.getSymbol().getType().getInternalName();

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
                    Type.getInternalName(Object.class),
                    null);

            AnnotationVisitor annotationVisitor = innerWriter.visitAnnotation(Type.getDescriptor(CustomType.class), true);
            annotationVisitor.visit("name", classNode.name.value);
            annotationVisitor.visitEnd();

            innerWriter.visitInnerClass(
                    name,
                    context.getClassName(),
                    classNode.name.value,
                    ACC_PUBLIC | ACC_STATIC);

            CompilerContext classContext = context.createClass((SDeclaredType) classNode.name.type);
            for (BoundClassMemberNode member : classNode.members) {
                compileClassMember(innerWriter, member, classContext);
            }

            // add default constructor if we have zero constructors defined
            if (classNode.members.stream().noneMatch(m -> m.getNodeType() == NodeType.CLASS_CONSTRUCTOR)) {
                MethodVisitor constructorVisitor = innerWriter.visitMethod(ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
                constructorVisitor.visitCode();
                constructorVisitor.visitVarInsn(ALOAD, 0);
                constructorVisitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
                constructorVisitor.visitInsn(RETURN);
                constructorVisitor.visitMaxs(0, 0);
                constructorVisitor.visitEnd();
            }

            innerWriter.visitEnd();

            byte[] bytecode = innerWriter.toByteArray();
            saveClassFile(name, bytecode);

            Class<?> innerClass = classLoader.defineClass(name.replace('/', '.'), bytecode);
            ((SDeclaredType) classNode.name.getSymbol().getType()).setJavaClass(innerClass);
        }
    }

    private void compileClassMember(ClassWriter writer, BoundClassMemberNode member, CompilerContext context) {
        switch (member.getNodeType()) {
            case CLASS_FIELD -> compileClassField(writer, (BoundClassFieldNode) member);
            case CLASS_CONSTRUCTOR -> compileClassConstructor(writer, (BoundClassConstructorNode) member, context);
            case CLASS_METHOD -> compileClassMethod(writer, (BoundClassMethodNode) member, context);
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
        constructorVisitor.visitVarInsn(ALOAD, 0);
        constructorVisitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);

        context = context.createClassMethod(SVoidType.instance, false);
        for (BoundParameterNode parameter : constructor.parameters.parameters) {
            context.setStackIndex((LocalVariable) parameter.getName().getSymbol());
        }

        compileBlockStatement(constructorVisitor, context, constructor.body);

        constructorVisitor.visitInsn(RETURN);
        constructorVisitor.visitMaxs(0, 0);
        constructorVisitor.visitEnd();
    }

    private void compileClassMethod(ClassWriter writer, BoundClassMethodNode methodNode, CompilerContext context) {
        MethodVisitor methodVisitor = writer.visitMethod(ACC_PUBLIC, methodNode.name.value, methodNode.functionType.getMethodDescriptor(), null, null);
        methodVisitor.visitCode();

        context = context.createClassMethod(methodNode.functionType.getReturnType(), methodNode.isAsync);
        for (BoundParameterNode parameter : methodNode.parameters.parameters) {
            context.setStackIndex((LocalVariable) parameter.getName().getSymbol());
        }

        if (methodNode.isAsync) {
            compileAsyncBoundStatementList(methodVisitor, context, new BoundStatementsListNode(methodNode.body.statements));
        } else {
            compileBlockStatement(methodVisitor, context, methodNode.body);
            if (methodNode.functionType.getReturnType() == SVoidType.instance) {
                methodVisitor.visitInsn(RETURN);
            }
        }

        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void setupGenericFunctions(List<SGenericFunction> functions) {
        for (SGenericFunction function : functions) {
            String name = "com/zergatul/scripting/dynamic/GenericFunction_" + counter.incrementAndGet();
            function.setInternalName(name);
        }
    }

    private void compileGenericFunctions(List<SGenericFunction> functions) {
        for (SGenericFunction function : functions) {
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

            Class<?> interfaceClass = classLoader.defineClass(function.getInternalName().replace('/', '.'), bytecode);
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
            StaticVariable symbol = (StaticVariable) staticVariableNode.name.getSymbol();
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

    private void compileFunctions(List<BoundFunctionNode> functions, ClassWriter writer, CompilerContext context) {
        for (BoundFunctionNode function : functions) {
            Function symbol = (Function) function.name.getSymbol();
            SStaticFunction type = symbol.getFunctionType();

            MethodVisitor visitor = writer.visitMethod(
                    ACC_PUBLIC | ACC_STATIC,
                    function.name.value,
                    type.getMethodDescriptor(),
                    null,
                    null);
            visitor.visitCode();

            context = context.createStaticFunction(type.getReturnType(), function.isAsync);

            for (BoundParameterNode parameter : function.parameters.parameters) {
                context.setStackIndex((LocalVariable) parameter.getName().getSymbol());
            }

            if (function.isAsync) {
                compileAsyncBoundStatementList(visitor, context, new BoundStatementsListNode(List.of(function.body)));
            } else {
                if (!function.lifted.isEmpty()) {
                    compileClosureClass(visitor, context, function.lifted);
                }

                compileStatement(visitor, context, function.body);
                if (type.getReturnType() == SVoidType.instance) {
                    visitor.visitInsn(RETURN);
                }
            }

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

    private <T> void buildMainMethod(BoundCompilationUnitNode unit, ClassWriter writer, String className) {
        Class<?> functionalInterface = parameters.getFunctionalInterface();
        if (!functionalInterface.isInterface()) {
            throw new InternalException();
        }

        List<Method> methods = Arrays.stream(functionalInterface.getMethods()).filter(m -> !m.isDefault()).toList();
        if (methods.size() != 1) {
            throw new InternalException();
        }
        Method method = methods.get(0);

        MethodVisitor visitor = writer.visitMethod(ACC_PUBLIC, method.getName(), Type.getMethodDescriptor(method), null, null);
        visitor.visitCode();

        CompilerContext context = parameters.getContext();
        context.reserveStack(1 + method.getParameterCount()); // assuming all parameters size = 1

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
            BoundStatementsListNode statements = new BoundStatementsListNode(
                    prepend,
                    unit.statements.statements,
                    unit.statements.lifted,
                    unit.statements.getRange());
            unit = new BoundCompilationUnitNode(unit.members, statements, unit.getRange());
        }

        context.setClassName(className);
        for (BoundCompilationUnitMemberNode member : unit.members.members) {
            if (member.getNodeType() == NodeType.STATIC_VARIABLE) {
                BoundStaticVariableNode field = (BoundStaticVariableNode) member;
                context.addStaticSymbol(field.name.value, field.name.symbolRef);
            }
        }

        if (parameters.isAsync()) {
            compileAsyncBoundStatementList(visitor, context, unit.statements);
        } else {
            compileStatementList(visitor, context, unit.statements);
        }

        if (!parameters.isAsync() && parameters.getReturnType() == SVoidType.instance) {
            visitor.visitInsn(RETURN);
        }

        markEnd(visitor, context);
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
            if (parameters.isDebug()) {
                Label label = new Label();
                visitor.visitLabel(label);
                local.setDeclarationLabel(label);
            }
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
                    compileExpression(visitor, context, access.callee);
                    compileExpression(visitor, context, assignment.right);
                    access.property.property.compileSet(visitor);
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
                assignment.operator.operation.apply(visitor, buffer);

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
                assignment.operator.operation.apply(visitor, buffer);

                indexExpression.operation.compileSet(visitor);
            }
            case PROPERTY_ACCESS_EXPRESSION -> {
                BoundPropertyAccessExpressionNode propertyAccess = (BoundPropertyAccessExpressionNode) assignment.left;
                compileExpression(visitor, context, propertyAccess.callee);
                visitor.visitInsn(DUP);
                propertyAccess.property.property.compileGet(visitor);
                BufferedMethodVisitor buffer = new BufferedMethodVisitor();
                compileExpression(buffer, context, assignment.right);
                assignment.operator.operation.apply(visitor, buffer);
                propertyAccess.property.property.compileSet(visitor);
            }
            default -> throw new InternalException("Not implemented.");
        }
    }

    private void compileIfStatement(MethodVisitor visitor, CompilerContext context, BoundIfStatementNode statement) {
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
        context = context.createChild();
        compileStatements(visitor, context, statement.statements);
        markEnd(visitor, context);
    }

    private void compileReturnStatement(MethodVisitor visitor, CompilerContext context, BoundReturnStatementNode statement) {
        if (statement.expression != null) {
            compileExpression(visitor, context, statement.expression);

            if (context.isGenericFunction() && context.getReturnType().getBoxedVersion() != null) {
                context.getReturnType().compileBoxing(visitor);
                visitor.visitInsn(ARETURN);
                return;
            }
        }

        visitor.visitInsn(context.getReturnType().getReturnInst());
    }

    private void compileForLoopStatement(MethodVisitor visitor, CompilerContext context, BoundForLoopStatementNode statement) {
        context = context.createChild();

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

        markEnd(visitor, context);
    }

    private void compileForEachLoopStatement(MethodVisitor visitor, CompilerContext context, BoundForEachLoopStatementNode statement) {
        context = context.createChild();

        Label begin = new Label();
        Label continueLabel = new Label();
        Label end = new Label();

        compileExpression(visitor, context, statement.iterable);

        LocalVariable variable = statement.name.symbolRef.asLocalVariable();
        context.addLocalVariable(statement.name.symbolRef);
        if (parameters.isDebug()) {
            variable.setDeclarationLabel(begin);
        }
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

        markEnd(visitor, context);
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

        markEnd(visitor, context);
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
        String name = "com/zergatul/scripting/dynamic/DynamicClosure_" + counter.incrementAndGet();
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

        Class<?> closureClass = classLoader.defineClass(name.replace('/', '.'), bytecode);

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
        String name = "com/zergatul/scripting/dynamic/DynamicAsyncStateMachine_" + counter.incrementAndGet();
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

        CompilerContext nextMethodContext = context.createFunction(SType.fromJavaType(CompletableFuture.class), false);
        nextMethodContext.setAsyncStateMachineClassName(name);
        if (parameterVisitor.hasThisLocalVariable()) {
            nextMethodContext.setAsyncThisFieldName(parameterVisitor.getParameters().getFirst().getFieldName());
        }
        LocalVariable parameter = nextMethodContext.addLocalParameter("@result", SType.fromJavaType(Object.class), null);
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
            if (boundary.statements.isEmpty() || boundary.statements.get(boundary.statements.size() - 1).getNodeType() != NodeType.RETURN_STATEMENT) {
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

        classLoader.defineClass(name.replace('/', '.'), bytecode);

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
            if (node.expression.type.getBoxedVersion() != null) {
                node.expression.type.compileBoxing(visitor);
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
            case BOOLEAN_LITERAL -> compileBooleanLiteral(visitor, (BoundBooleanLiteralExpressionNode) expression);
            case INTEGER_LITERAL -> compileIntegerLiteral(visitor, (BoundIntegerLiteralExpressionNode) expression);
            case INTEGER64_LITERAL -> compileInteger64Literal(visitor, (BoundInteger64LiteralExpressionNode) expression);
            case FLOAT_LITERAL -> compileFloatLiteral(visitor, (BoundFloatLiteralExpressionNode) expression);
            case STRING_LITERAL -> compileStringLiteral(visitor, (BoundStringLiteralExpressionNode) expression);
            case CHAR_LITERAL -> compileCharLiteral(visitor, (BoundCharLiteralExpressionNode) expression);
            case UNARY_EXPRESSION -> compileUnaryExpression(visitor, context, (BoundUnaryExpressionNode) expression);
            case BINARY_EXPRESSION -> compileBinaryExpression(visitor, context, (BoundBinaryExpressionNode) expression);
            case TYPE_TEST_EXPRESSION -> compileTypeTestExpression(visitor, context, (BoundTypeTestExpressionNode) expression);
            case TYPE_CAST_EXPRESSION -> compileTypeCastExpression(visitor, context, (BoundTypeCastExpressionNode) expression);
            case CONDITIONAL_EXPRESSION -> compileConditionalExpression(visitor, context, (BoundConditionalExpressionNode) expression);
            case IMPLICIT_CAST -> compileImplicitCastExpression(visitor, context, (BoundImplicitCastExpressionNode) expression);
            case CONVERSION -> compileConversionExpression(visitor, context, (BoundConversionNode) expression);
            case NAME_EXPRESSION -> compileNameExpression(visitor, context, (BoundNameExpressionNode) expression);
            case THIS_EXPRESSION -> compileThisExpression(visitor, context, (BoundThisExpressionNode) expression);
            case STATIC_REFERENCE -> compileStaticReferenceExpression();
            case REF_ARGUMENT_EXPRESSION -> compileRefArgumentExpression(visitor, context, (BoundRefArgumentExpressionNode) expression);
            case METHOD_INVOCATION_EXPRESSION -> compileMethodInvocationExpression(visitor, context, (BoundMethodInvocationExpressionNode) expression);
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
            case META_TYPE_EXPRESSION -> compileMetaTypeExpression(visitor, (BoundMetaTypeExpressionNode) expression);
            case META_TYPE_OF_EXPRESSION -> compileMetaTypeOfExpression(visitor, context, (BoundMetaTypeOfExpressionNode) expression);
            default -> throw new InternalException();
        }
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
        expression.operator.operation.apply(visitor);
    }

    private void compileBinaryExpression(MethodVisitor visitor, CompilerContext context, BoundBinaryExpressionNode expression) {
        BufferedMethodVisitor buffer = new BufferedMethodVisitor();
        compileExpression(visitor, context, expression.left);
        compileExpression(buffer, context, expression.right);
        expression.operator.operation.apply(visitor, buffer);
    }

    private void compileTypeTestExpression(MethodVisitor visitor, CompilerContext context, BoundTypeTestExpressionNode test) {
        compileExpression(visitor, context, test.expression);
        if (!test.expression.type.isReference()) {
            test.expression.type.compileBoxing(visitor);
        }

        if (test.type.type.isReference()) {
            visitor.visitTypeInsn(INSTANCEOF, Type.getInternalName(test.type.type.getJavaClass()));
        } else {
            visitor.visitTypeInsn(INSTANCEOF, Type.getInternalName(test.type.type.getBoxedVersion()));
        }
    }

    private void compileTypeCastExpression(MethodVisitor visitor, CompilerContext context, BoundTypeCastExpressionNode test) {
        compileExpression(visitor, context, test.expression);
        if (!test.expression.type.isReference()) {
            test.expression.type.compileBoxing(visitor);
        }

        if (test.type.type.isReference()) {
            visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(test.type.type.getJavaClass()));
        } else {
            visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(test.type.type.getBoxedVersion()));
        }

        if (!test.type.type.isReference()) {
            test.type.type.compileUnboxing(visitor);
        }
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
                    (BoundFunctionReferenceNode) expression.expression,
                    (SFunctionalInterface) expression.type);

            case FUNCTION_TO_GENERIC -> compileFunctionToGeneric(
                    visitor,
                    context,
                    (BoundFunctionReferenceNode) expression.expression,
                    (SGenericFunction) expression.type);

            case METHOD_GROUP_TO_INTERFACE -> compileInstanceMethodToInterface(
                    visitor,
                    context,
                    (BoundMethodGroupExpressionNode) expression.expression,
                    expression.conversionInfo.method(),
                    (SFunctionalInterface) expression.type);

            case METHOD_GROUP_TO_GENERIC -> compileInstanceMethodToGeneric(
                    visitor,
                    context,
                    (BoundMethodGroupExpressionNode) expression.expression,
                    expression.conversionInfo.method(),
                    (SGenericFunction) expression.type);

            default -> throw new InternalException();
        }
    }

    private void compileImplicitCastConversion(MethodVisitor visitor, CompilerContext context, BoundConversionNode expression) {
        compileExpression(visitor, context, expression.expression);
        expression.conversionInfo.cast().apply(visitor);
    }

    private void compileFunctionToInterfaceConversion(MethodVisitor visitor, CompilerContext context, BoundFunctionReferenceNode functionReferenceNode, SFunctionalInterface functionalInterface) {
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
                functionReferenceNode.getFunction().getName(),
                functionalInterface.getActualMethodDescriptor(),
                false);

        visitor.visitInvokeDynamicInsn(
                functionalInterface.getMethodName(),
                Type.getMethodDescriptor(Type.getType(functionalInterface.getJavaClass())),
                bsm,
                Type.getType(functionalInterface.getRawMethodDescriptor()),
                impl,
                Type.getType(functionalInterface.getIntermediateMethodDescriptor()));
    }

    private void compileFunctionToGeneric(MethodVisitor visitor, CompilerContext context, BoundFunctionReferenceNode functionReferenceNode, SGenericFunction genericFunction) {
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
                functionReferenceNode.getFunction().getName(),
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
            visitor.visitVarInsn(ALOAD, 0);
        } else {
            visitor.visitVarInsn(ALOAD, 0);
            visitor.visitFieldInsn(GETFIELD, context.getAsyncStateMachineClassName(), fieldName, expression.type.getDescriptor());
        }
    }

    private void compileRefArgumentExpression(MethodVisitor visitor, CompilerContext context, BoundRefArgumentExpressionNode expression) {
        Variable variable = expression.name.symbolRef.asVariable();
        LocalVariable holder = expression.holder;
        context.setStackIndex(holder);

        String refClassDescriptor = Type.getInternalName(holder.getType().getJavaClass());
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
        invocation.method.method.compileInvoke(visitor);
        releaseRefVariables(visitor, context, invocation.refVariables);
    }

    private void compilePropertyAccessExpression(MethodVisitor visitor, CompilerContext context, BoundPropertyAccessExpressionNode propertyAccess) {
        if (!propertyAccess.property.property.canGet()) {
            throw new InternalException();
        }

        compileExpression(visitor, context, propertyAccess.callee);
        propertyAccess.property.property.compileGet(visitor);
    }

    private void compileArrayCreationExpression(MethodVisitor visitor, CompilerContext context, BoundArrayCreationExpressionNode expression) {
        compileExpression(visitor, context, expression.lengthExpression);

        SArrayType arrayType = (SArrayType) expression.type;
        SType elementsType = arrayType.getElementsType();
        if (elementsType.isReference()) {
            visitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(elementsType.getJavaClass()));
        } else {
            visitor.visitIntInsn(NEWARRAY, ((SPredefinedType) elementsType).getArrayTypeInst());
        }
    }

    private void compileArrayInitializerExpression(MethodVisitor visitor, CompilerContext context, BoundArrayInitializerExpressionNode expression) {
        visitor.visitLdcInsn(expression.items.size());

        SArrayType arrayType = (SArrayType) expression.type;
        SType elementsType = arrayType.getElementsType();
        if (elementsType.isReference()) {
            visitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(elementsType.getJavaClass()));
        } else {
            visitor.visitIntInsn(NEWARRAY, ((SPredefinedType) elementsType).getArrayTypeInst());
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
        visitor.visitLdcInsn(expression.items.size());

        SArrayType arrayType = (SArrayType) expression.type;
        SType elementsType = arrayType.getElementsType();
        if (elementsType.isReference()) {
            visitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(elementsType.getJavaClass()));
        } else {
            visitor.visitIntInsn(NEWARRAY, ((SPredefinedType) elementsType).getArrayTypeInst());
        }

        for (int i = 0; i < expression.items.size(); i++) {
            visitor.visitInsn(DUP);
            visitor.visitLdcInsn(i);
            compileExpression(visitor, context, expression.items.get(i));
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
        String name = "com/zergatul/scripting/dynamic/DynamicLambdaClass_" + counter.incrementAndGet();
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

        CompilerContext lambdaContext = context.createFunction(actualReturnType, false, !actualReturnType.equals(rawReturnType));
        lambdaContext.setClassName(name);

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
            SymbolRef symbolRef = lambdaContext.addLocalVariable(null, SType.fromJavaType(Object.class), null);
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
                Class<?> boxedType = parameter.getType().getBoxedVersion();

                arguments[i].compileLoad(context, invokeVisitor); // load argument
                if (boxedType != null) {
                    invokeVisitor.visitTypeInsn(CHECKCAST, Type.getInternalName(boxedType)); // cast to boxed, example: java.lang.Integer
                    parameter.getType().compileUnboxing(invokeVisitor); // convert to unboxed
                } else {
                    invokeVisitor.visitTypeInsn(CHECKCAST, Type.getInternalName(parameter.getType().getJavaClass()));
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
        invokeVisitor.visitMaxs(0, 0);
        invokeVisitor.visitEnd();

        writer.visitEnd();

        byte[] bytecode = writer.toByteArray();
        saveClassFile(name, bytecode);

        classLoader.defineClass(name.replace('/', '.'), bytecode);

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

        Function symbol = expression.functionReferenceNode.getFunction();
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

        if (node.type.getBoxedVersion() != null) {
            visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(node.type.getBoxedVersion()));
            node.type.compileUnboxing(visitor);
        } else {
            visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(node.type.getJavaClass()));
        }
    }

    private void compileStackLoad(MethodVisitor visitor, BoundStackLoadNode node) {
        visitor.visitVarInsn(node.type.getLoadInst(), node.index);
    }

    private void compileFunctionAsLambda(MethodVisitor visitor, CompilerContext context, BoundFunctionAsLambdaExpressionNode node) {
        Function function = (Function) node.name.getSymbol();
        SStaticFunction type = function.getFunctionType();
        List<BoundParameterNode> parameters = new ArrayList<>(type.getParameters().size());
        List<LocalVariable> variables = new ArrayList<>(type.getParameters().size());
        CompilerContext lambdaContext = context.createFunction(type.getReturnType(), false, true);
        for (SType parameterType : type.getParameterTypes()) {
            SymbolRef symbolRef = lambdaContext.addLocalVariable("p" + variables.size(), parameterType, null);;
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
                new BoundFunctionReferenceNode(node.name.value, node.name.symbolRef, node.name.getRange()),
                type.getReturnType(),
                new BoundArgumentsListNode(arguments));
        BoundStatementNode statement = type.getReturnType() == SVoidType.instance ?
                new BoundExpressionStatementNode(invocation) :
                new BoundReturnStatementNode(invocation);
        BoundLambdaExpressionNode lambda = new BoundLambdaExpressionNode(
                node.type,
                parameters,
                statement);
        compileLambdaExpression(visitor, context, lambda);
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
                    Type.getInternalName(holder.getType().getJavaClass()),
                    "get",
                    Type.getMethodDescriptor(Type.getType(variable.getType().getJavaClass())),
                    false);
            variable.compileStore(context, visitor);
        }
    }

    private void markEnd(MethodVisitor visitor, CompilerContext context) {
        if (parameters.isDebug()) {
            context.markEnd(visitor);
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
            if (range == null) {
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
}