package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
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
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.*;
import com.zergatul.scripting.visitors.AwaitVisitor;
import com.zergatul.scripting.visitors.ExternalParameterVisitor;
import com.zergatul.scripting.visitors.LiftedVariablesVisitor;
import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
            return CompilationResult.success(compileUnit(binderOutput.unit()));
        } else {
            return CompilationResult.failed(binderOutput.diagnostics());
        }
    }

    private <T> T compileUnit(BoundCompilationUnitNode unit) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String name = "com/zergatul/scripting/dynamic/DynamicClass_" + counter.incrementAndGet();
        writer.visit(
                V1_5,
                ACC_PUBLIC,
                name,
                null,
                Type.getInternalName(Object.class),
                new String[] { Type.getInternalName(parameters.getFunctionalInterface()) });

        CompilerContext context = parameters.getContext();
        context.setClassName(name);

        buildStaticVariables(unit, writer, context);
        buildFunctions(unit, writer, context);
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

    private void buildStaticVariables(BoundCompilationUnitNode unit, ClassWriter writer, CompilerContext context) {
        if (unit.variables.variables.isEmpty()) {
            return;
        }

        for (BoundVariableDeclarationNode variable : unit.variables.variables) {
            FieldVisitor fieldVisitor = writer.visitField(
                    ACC_PUBLIC | ACC_STATIC,
                    variable.name.value,
                    Type.getDescriptor(variable.type.type.getJavaClass()),
                    null, null);
            fieldVisitor.visitEnd();
        }

        // set static variables values in static constructor
        MethodVisitor visitor = writer.visitMethod(ACC_STATIC, "<clinit>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
        visitor.visitCode();
        for (BoundVariableDeclarationNode variable : unit.variables.variables) {
            StaticVariable symbol = (StaticVariable) variable.name.symbol;
            context.addStaticVariable(symbol);
            if (variable.expression != null) {
                compileExpression(visitor, context, variable.expression);
            } else {
                variable.type.type.storeDefaultValue(visitor);
            }
            symbol.compileStore(context, visitor);
        }
        visitor.visitInsn(RETURN);
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();
    }

    private void buildFunctions(BoundCompilationUnitNode unit, ClassWriter writer, CompilerContext context) {
        for (BoundFunctionNode function : unit.functions.functions) {
            Function symbol = (Function) function.name.symbol;
            SFunction type = symbol.getFunctionType();

            MethodVisitor visitor = writer.visitMethod(
                    ACC_PUBLIC | ACC_STATIC,
                    function.name.value,
                    type.getDescriptor(),
                    null,
                    null);
            visitor.visitCode();

            context = context.createStaticFunction(type.getReturnType());

            for (BoundParameterNode parameter : function.parameters.parameters) {
                context.setStackIndex((LocalVariable) parameter.getName().symbol);
            }
            compileBlockStatement(visitor, context, function.block);

            if (type.getReturnType() != SVoidType.instance) {
                type.getReturnType().storeDefaultValue(visitor);
            }

            visitor.visitInsn(type.getReturnType().getReturnInst());
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
        for (Variable parameter : treeVisitor.getParameters()) {
            int parameterStackIndex;
            if (parameter instanceof LiftedVariable lifted) {
                parameterStackIndex = 1 + ((ExternalParameter) lifted.getUnderlying()).getIndex();
            } else if (parameter instanceof ExternalParameter external) {
                parameterStackIndex = 1 + external.getIndex();
                external.setStackIndex(parameterStackIndex);
            } else {
                throw new InternalException();
            }

            BoundVariableDeclarationNode declaration = new BoundVariableDeclarationNode(
                    new BoundNameExpressionNode(parameter),
                    new BoundStackLoadNode(parameterStackIndex, parameter.getType()));

            prepend.add(declaration);
        }

        if (!prepend.isEmpty()) {
            BoundStatementsListNode statements = new BoundStatementsListNode(
                    prepend,
                    unit.statements.statements,
                    unit.statements.lifted,
                    unit.statements.getRange());
            unit = new BoundCompilationUnitNode(unit.variables, unit.functions, statements, unit.getRange());
        }

        context.setClassName(className);
        for (BoundVariableDeclarationNode variable : unit.variables.variables) {
            context.addStaticVariable((DeclaredStaticVariable) variable.name.symbol);
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

        Variable variable = (Variable) declaration.name.symbol;
        context.addLocalVariable(variable);

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
                    Variable variable = (Variable) name.symbol;
                    variable.compileStore(context, visitor);
                }
                case INDEX_EXPRESSION -> {
                    BoundIndexExpressionNode indexExpression = (BoundIndexExpressionNode) assignment.left;
                    compileExpression(visitor, context, indexExpression.callee);
                    compileExpression(visitor, context, indexExpression.index);
                    compileExpression(visitor, context, assignment.right);
                    indexExpression.operation.compileSet(visitor);
                }
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
                Variable variable = (Variable) name.symbol;
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

        LocalVariable variable = (LocalVariable) statement.name.symbol;
        context.addLocalVariable(variable);
        if (parameters.isDebug()) {
            variable.setDeclarationLabel(begin);
        }
        context.addLocalVariable(statement.index);
        context.addLocalVariable(statement.length);

        context.setStackIndex(variable);
        context.setStackIndex(statement.index);
        context.setStackIndex(statement.length);

        visitor.visitInsn(ICONST_0);
        statement.index.compileStore(context, visitor);

        visitor.visitInsn(DUP);
        visitor.visitInsn(ARRAYLENGTH);
        statement.length.compileStore(context, visitor);

        visitor.visitLabel(begin);

        // index >= length -- GOTO end
        statement.index.compileLoad(context, visitor);
        statement.length.compileLoad(context, visitor);
        visitor.visitJumpInsn(IF_ICMPGE, end);

        // variable = array[index]
        visitor.visitInsn(DUP);
        statement.index.compileLoad(context, visitor);
        visitor.visitInsn(variable.getType().getArrayLoadInst());
        variable.compileStore(context, visitor);

        // body
        context.setBreak(v -> v.visitJumpInsn(GOTO, end));
        context.setContinue(v -> v.visitJumpInsn(GOTO, continueLabel));
        compileStatement(visitor, context, statement.body);

        // index++
        visitor.visitLabel(continueLabel);
        visitor.visitIincInsn(statement.index.getStackIndex(), 1);

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
                Variable variable = (Variable) name.symbol;
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
        String name = "com/zergatul/scripting/dynamic/DynamicClosure_" + counter.incrementAndGet();
        writer.visit(
                V1_5,
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

        LocalVariable closure = parentContext.addLocalVariable(null, SType.fromJavaType(closureClass), null);
        parentContext.setStackIndex(closure);
        closure.compileStore(parentContext, parentVisitor);

        for (LiftedVariable lifted : variables) {
            lifted.setClosure(closure);
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
            if (declaration.name.symbol instanceof ExternalParameter parameter) {
                LiftedVariable lifted = new LiftedVariable(parameter);
                for (BoundNameExpressionNode name : parameter.getReferences()) {
                    name.overrideSymbol(lifted);
                }
            }
        }

        BinderTreeGenerator generator = new BinderTreeGenerator();
        generator.generate(node.statements);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String name = "com/zergatul/scripting/dynamic/DynamicAsyncStateMachine_" + counter.incrementAndGet();
        writer.visit(
                V1_5,
                ACC_PUBLIC,
                name,
                null,
                Type.getInternalName(Object.class),
                new String[] { Type.getInternalName(AsyncStateMachine.class) });

        // state field
        writer.visitField(ACC_PRIVATE, "state", Type.getDescriptor(int.class), null, null);

        // lifted variables
        LiftedVariablesVisitor treeVisitor = new LiftedVariablesVisitor();
        for (BoundVariableDeclarationNode declaration : node.prepend) {
            declaration.accept(treeVisitor);
        }
        for (StateBoundary boundary : generator.boundaries) {
            for (BoundStatementNode statement : boundary.statements) {
                statement.accept(treeVisitor);
            }
        }

        List<LiftedVariable> variables = treeVisitor.getVariables();
        String[] fieldNames = new String[variables.size()];
        for (int i = 0; i < fieldNames.length; i++) {
            String varName = variables.get(i).getName();
            fieldNames[i] = varName != null ? varName : "lifted";
        }
        uniquify(fieldNames);
        for (int i = 0; i < variables.size(); i++) {
            LiftedVariable variable = variables.get(i);
            variable.setField(name, fieldNames[i]);
            writer.visitField(ACC_PUBLIC, fieldNames[i], Type.getDescriptor(variable.getType().getJavaClass()), null, null);
        }

        // build constructor
        MethodVisitor constructorVisitor = writer.visitMethod(ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
        constructorVisitor.visitCode();
        constructorVisitor.visitVarInsn(ALOAD, 0);
        constructorVisitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
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

        CompilerContext nextMethodContext = context.createFunction(SType.fromJavaType(CompletableFuture.class));
        nextMethodContext.setAsyncStateMachineClassName(name);
        LocalVariable parameter = nextMethodContext.addLocalParameter("@result", SType.fromJavaType(Object.class), null);
        nextMethodContext.setStackIndex(parameter);

        // closure reference
        LocalVariable closure = nextMethodContext.addLocalVariable(null, new SLazyClassType(name), null);
        closure.setStackIndex(0);
        for (LiftedVariable lifted : variables) {
            lifted.setClosure(closure);
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
        parentVisitor.visitMethodInsn(
                INVOKESPECIAL,
                name,
                "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE),
                false);

        for (BoundVariableDeclarationNode declaration : node.prepend) {
            if (declaration.expression instanceof BoundStackLoadNode load) {
                if (declaration.name.symbol instanceof LiftedVariable lifted) {
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

        visitor.visitInsn(ACONST_NULL);
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
        switch (expression.getNodeType()) {
            case BOOLEAN_LITERAL -> compileBooleanLiteral(visitor, (BoundBooleanLiteralExpressionNode) expression);
            case INTEGER_LITERAL -> compileIntegerLiteral(visitor, (BoundIntegerLiteralExpressionNode) expression);
            case FLOAT_LITERAL -> compileFloatLiteral(visitor, (BoundFloatLiteralExpressionNode) expression);
            case STRING_LITERAL -> compileStringLiteral(visitor, (BoundStringLiteralExpressionNode) expression);
            case CHAR_LITERAL -> compileCharLiteral(visitor, (BoundCharLiteralExpressionNode) expression);
            case UNARY_EXPRESSION -> compileUnaryExpression(visitor, context, (BoundUnaryExpressionNode) expression);
            case BINARY_EXPRESSION -> compileBinaryExpression(visitor, context, (BoundBinaryExpressionNode) expression);
            case CONDITIONAL_EXPRESSION -> compileConditionalExpression(visitor, context, (BoundConditionalExpressionNode) expression);
            case IMPLICIT_CAST -> compileImplicitCastExpression(visitor, context, (BoundImplicitCastExpressionNode) expression);
            case NAME_EXPRESSION -> compileNameExpression(visitor, context, (BoundNameExpressionNode) expression);
            case STATIC_REFERENCE -> compileStaticReferenceExpression();
            case REF_ARGUMENT_EXPRESSION -> compileRefArgumentExpression(visitor, context, (BoundRefArgumentExpressionNode) expression);
            case METHOD_INVOCATION_EXPRESSION -> compileMethodInvocationExpression(visitor, context, (BoundMethodInvocationExpressionNode) expression);
            case PROPERTY_ACCESS_EXPRESSION -> compilePropertyAccessExpression(visitor, context, (BoundPropertyAccessExpressionNode) expression);
            case NEW_EXPRESSION -> compileNewExpression(visitor, context, (BoundNewExpressionNode) expression);
            case INDEX_EXPRESSION -> compileIndexExpression(visitor, context, (BoundIndexExpressionNode) expression);
            case LAMBDA_EXPRESSION -> compileLambdaExpression(visitor, context, (BoundLambdaExpressionNode) expression);
            case FUNCTION_INVOCATION -> compileFunctionInvocationExpression(visitor, context, (BoundFunctionInvocationExpression) expression);
            case GENERATOR_GET_VALUE -> compileGeneratorGetValue(visitor, context, (BoundGeneratorGetValueNode) expression);
            case STACK_LOAD -> compileStackLoad(visitor, (BoundStackLoadNode) expression);
            case FUNCTION_AS_LAMBDA -> compileFunctionAsLambda(visitor, context, (BoundFunctionAsLambdaExpressionNode) expression);
            default -> throw new InternalException();
        }
    }

    private void compileBooleanLiteral(MethodVisitor visitor, BoundBooleanLiteralExpressionNode literal) {
        visitor.visitInsn(literal.value ? ICONST_1 : ICONST_0);
    }

    private void compileIntegerLiteral(MethodVisitor visitor, BoundIntegerLiteralExpressionNode literal) {
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

    private void compileStaticReferenceExpression() {

    }

    private void compileNameExpression(MethodVisitor visitor, CompilerContext context, BoundNameExpressionNode expression) {
        if (expression.symbol instanceof Variable variable) {
            variable.compileLoad(context, visitor);
        } else {
            throw new InternalException("Not implemented.");
        }
    }

    private void compileRefArgumentExpression(MethodVisitor visitor, CompilerContext context, BoundRefArgumentExpressionNode expression) {
        Variable variable = (Variable) expression.name.symbol;
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

    private void compileNewExpression(MethodVisitor visitor, CompilerContext context, BoundNewExpressionNode expression) {
        if (expression.lengthExpression != null) {
            compileExpression(visitor, context, expression.lengthExpression);
        } else {
            visitor.visitLdcInsn(expression.items.size());
        }

        SArrayType arrayType = (SArrayType) expression.type;
        SType elementsType = arrayType.getElementsType();
        if (elementsType.isReference()) {
            visitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(elementsType.getJavaClass()));
        } else {
            visitor.visitIntInsn(NEWARRAY, ((SPredefinedType) elementsType).getArrayTypeInst());
        }

        if (expression.items != null) {
            for (int i = 0; i < expression.items.size(); i++) {
                visitor.visitInsn(DUP);
                visitor.visitLdcInsn(i);
                compileExpression(visitor, context, expression.items.get(i));
                visitor.visitInsn(elementsType.getArrayStoreInst());
            }
        }
    }

    private void compileIndexExpression(MethodVisitor visitor, CompilerContext context, BoundIndexExpressionNode expression) {
        compileExpression(visitor, context, expression.callee);
        compileExpression(visitor, context, expression.index);
        expression.operation.compileGet(visitor);
    }

    private void compileLambdaExpression(MethodVisitor visitor, CompilerContext context, BoundLambdaExpressionNode expression) {
        SFunctionalInterface type = (SFunctionalInterface) expression.type;
        Class<?> funcInterface = type.getJavaClass();

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String name = "com/zergatul/scripting/dynamic/DynamicLambdaClass_" + counter.incrementAndGet();
        writer.visit(V1_5, ACC_PUBLIC, name, null, Type.getInternalName(Object.class), new String[] { Type.getInternalName(funcInterface) });

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
                type.getMethodName(),
                type.getMethodDescriptor(),
                null,
                null);
        invokeVisitor.visitCode();

        CompilerContext lambdaContext = context.createFunction(type.getActualReturnType(), true);
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

                captured.setClosure(closureFieldVariables[index]);
            }
        }

        LocalVariable[] arguments = new LocalVariable[expression.parameters.size()];
        for (int i = 0; i < expression.parameters.size(); i++) {
            arguments[i] = lambdaContext.addLocalVariable(null, SType.fromJavaType(Object.class), null);
            lambdaContext.setStackIndex(arguments[i]);
        }
        for (int i = 0; i < expression.parameters.size(); i++) {
            SType raw = type.getRawParameters()[i];
            SType actual = type.getActualParameters()[i];
            if (!raw.equals(actual)) {
                BoundParameterNode parameter = expression.parameters.get(i);
                LocalVariable unboxed = (LocalVariable) parameter.getName().symbol;
                lambdaContext.addLocalVariable(unboxed);
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
                LocalVariable variable = (LocalVariable) expression.parameters.get(i).getName().symbol;
                lambdaContext.addLocalVariable(variable);
                variable.setStackIndex(type.getParameterStackIndex(i));
            }
        }
        if (!expression.lifted.isEmpty()) {
            compileClosureClass(invokeVisitor, lambdaContext, expression.lifted);
        }
        compileStatement(invokeVisitor, lambdaContext, expression.body);
        if (!type.isFunction()) {
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
        Function symbol = (Function) expression.name.symbol;
        SFunction type = symbol.getFunctionType();

        for (BoundExpressionNode argument : expression.arguments.arguments) {
            compileExpression(visitor, context, argument);
        }

        visitor.visitMethodInsn(
                INVOKESTATIC,
                context.getClassName(),
                symbol.getName(),
                type.getDescriptor(),
                false);

        releaseRefVariables(visitor, context, expression.refVariables);
    }

    private void compileGeneratorGetValue(MethodVisitor visitor, CompilerContext context, BoundGeneratorGetValueNode node) {
        if (node.type == SVoidType.instance) {
            return;
        }

        Symbol parameter = context.getSymbol("@result");
        parameter.compileLoad(context, visitor);
        visitor.visitTypeInsn(CHECKCAST, Type.getInternalName(node.type.getBoxedVersion()));
        node.type.compileUnboxing(visitor);
    }

    private void compileStackLoad(MethodVisitor visitor, BoundStackLoadNode node) {
        visitor.visitVarInsn(node.type.getLoadInst(), node.index);
    }

    private void compileFunctionAsLambda(MethodVisitor visitor, CompilerContext context, BoundFunctionAsLambdaExpressionNode node) {
        Function function = (Function) node.name.symbol;
        SFunction type = function.getFunctionType();
        List<BoundParameterNode> parameters = new ArrayList<>(type.getParameters().size());
        List<LocalVariable> variables = new ArrayList<>(type.getParameters().size());
        CompilerContext lambdaContext = context.createFunction(type.getReturnType(), true);
        for (SType parameterType : type.getParameterTypes()) {
            LocalVariable variable = lambdaContext.addLocalVariable("p" + variables.size(), parameterType, null);
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
                new BoundNameExpressionNode(function),
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