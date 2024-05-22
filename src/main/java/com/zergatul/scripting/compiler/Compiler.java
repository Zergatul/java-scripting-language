package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.parser.AssignmentOperator;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.runtime.Action0;
import com.zergatul.scripting.runtime.Action1;
import com.zergatul.scripting.runtime.Action2;
import com.zergatul.scripting.type.*;
import org.objectweb.asm.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.*;

public class Compiler {

    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final DynamicCompilerClassLoader classLoader = new DynamicCompilerClassLoader();

    private final CompilationParameters parameters;

    public Compiler(CompilationParameters compilationUnitContext) {
        this.parameters = compilationUnitContext;
    }

    public CompilationResult compile(String code) {
        LexerInput lexerInput = new LexerInput(code);
        Lexer lexer = new Lexer(lexerInput);
        LexerOutput lexerOutput = lexer.lex();

        Parser parser = new Parser(lexerOutput);
        ParserOutput parserOutput = parser.parse();

        Binder binder = new Binder(parserOutput, parameters.getContext());
        BinderOutput binderOutput = binder.bind();

        if (binderOutput.diagnostics().isEmpty()) {
            return new CompilationResult(compileUnit(binderOutput.unit()));
        } else {
            return new CompilationResult(binderOutput.diagnostics());
        }
    }

    private Runnable createInstance(Class<Runnable> dynamic) {
        Constructor<Runnable> constructor;
        try {
            constructor = dynamic.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new InternalException();
        }

        Runnable instance;
        try {
            instance = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new InternalException();
        }

        return instance;
    }

    private Runnable compileUnit(BoundCompilationUnitNode unit) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String name = "com/zergatul/scripting/dynamic/DynamicClass_" + counter.incrementAndGet();
        writer.visit(V1_5, ACC_PUBLIC, name, null, Type.getInternalName(Object.class), new String[] { Type.getInternalName(Runnable.class) });

        CompilerContext context = parameters.getContext();
        context.setClassName(name);

        buildStaticVariables(unit, writer, context);
        buildFunctions(unit, writer, context);
        buildEmptyConstructor(writer);
        buildRunMethod(unit, writer, name);

        writer.visitEnd();

        @SuppressWarnings("unchecked")
        Class<Runnable> dynamic = (Class<Runnable>) classLoader.defineClass(name.replace('/', '.'), writer.toByteArray());
        return createInstance(dynamic);
    }

    private void buildStaticVariables(BoundCompilationUnitNode unit, ClassWriter writer, CompilerContext context) {
        if (unit.variables.isEmpty()) {
            return;
        }

        for (BoundVariableDeclarationNode variable : unit.variables) {
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
        for (BoundVariableDeclarationNode variable : unit.variables) {
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
        for (BoundFunctionNode function : unit.functions) {
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
        MethodVisitor constructorVisitor = writer.visitMethod(ACC_PUBLIC, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
        constructorVisitor.visitCode();
        constructorVisitor.visitVarInsn(ALOAD, 0);
        constructorVisitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);
        constructorVisitor.visitInsn(RETURN);
        constructorVisitor.visitMaxs(0, 0);
        constructorVisitor.visitEnd();
    }

    private void buildRunMethod(BoundCompilationUnitNode unit, ClassWriter writer, String className) {
        MethodVisitor visitor = writer.visitMethod(ACC_PUBLIC, "run", Type.getMethodDescriptor(Type.VOID_TYPE), null, null);
        visitor.visitCode();

        CompilerContext context = parameters.getContext();
        context.setClassName(className);
        for (BoundVariableDeclarationNode variable : unit.variables) {
            context.addStaticVariable((DeclaredStaticVariable) variable.name.symbol);
        }

        compileStatements(visitor, context, unit.statements);
        visitor.visitInsn(RETURN);
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
            default -> throw new InternalException();
        }
    }

    private void compileVariableDeclaration(MethodVisitor visitor, CompilerContext context, BoundVariableDeclarationNode declaration) {
        if (declaration.expression != null) {
            compileExpression(visitor, context, declaration.expression);
        } else {
            declaration.type.type.storeDefaultValue(visitor);
        }

        LocalVariable variable = (LocalVariable) declaration.name.symbol;
        context.addLocalVariable(variable);

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
        compileStatements(visitor, context.createChild(), statement.statements);
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
    }

    private void compileForEachLoopStatement(MethodVisitor visitor, CompilerContext context, BoundForEachLoopStatementNode statement) {
        context = context.createChild();

        Label begin = new Label();
        Label continueLabel = new Label();
        Label end = new Label();

        compileExpression(visitor, context, statement.iterable);

        LocalVariable variable = (LocalVariable) statement.name.symbol;
        context.addLocalVariable(variable);
        context.addLocalVariable(statement.index);
        context.addLocalVariable(statement.length);

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
        } else if (expression.symbol instanceof Function function) {
            compileLambdaFromFunction(visitor, context, function, expression.getRange());
        } else {
            throw new InternalException("Not implemented.");
        }
    }

    private void compileRefArgumentExpression(MethodVisitor visitor, CompilerContext context, BoundRefArgumentExpressionNode expression) {
        Variable variable = (Variable) expression.name.symbol;
        LocalVariable holder = expression.holder;

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

    private void compileLambdaFromFunction(MethodVisitor visitor, CompilerContext context, Function function, TextRange range) {
        SFunction type = function.getFunctionType();
        List<BoundParameterNode> parameters = new ArrayList<>(type.getParameters().size());
        List<LocalVariable> variables = new ArrayList<>(type.getParameters().size());
        CompilerContext lambdaContext = context.createFunction(type.getReturnType(), true);
        for (SType parameterType : type.getParameterTypes()) {
            LocalVariable variable = lambdaContext.addLocalVariable("p" + variables.size(), parameterType, null);
            variables.add(variable);
            parameters.add(new BoundParameterNode(
                    new BoundNameExpressionNode(variable, range),
                    parameterType,
                    range));
        }

        List<BoundExpressionNode> arguments = new ArrayList<>(type.getParameters().size());
        for (LocalVariable variable : variables) {
            arguments.add(new BoundNameExpressionNode(variable, range));
        }

        BoundFunctionInvocationExpression invocation = new BoundFunctionInvocationExpression(
                new BoundNameExpressionNode(function, range),
                type.getReturnType(),
                new BoundArgumentsListNode(arguments, range),
                List.of(), // TODO: ref parameters?
                range);
        BoundStatementNode statement = type.getReturnType() == SVoidType.instance ?
                new BoundExpressionStatementNode(invocation, range) :
                new BoundReturnStatementNode(invocation, range);
        BoundLambdaExpressionNode lambda = new BoundLambdaExpressionNode(
                new SLambdaFunction(type.getReturnType(), type.getParameterTypes().toArray(SType[]::new)),
                parameters,
                statement,
                range);
        compileLambdaExpression(visitor, context, lambda);
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
        if (!propertyAccess.property.canGet()) {
            throw new InternalException();
        }

        compileExpression(visitor, context, propertyAccess.callee);
        propertyAccess.property.compileGet(visitor);
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

    private void compileContextualLambdaExpression() {
        throw new InternalException("Contextual lambda expression should not be here.");
    }

    private void compileLambdaExpression(MethodVisitor visitor, CompilerContext context, BoundLambdaExpressionNode expression) {
        SLambdaFunction type = (SLambdaFunction) expression.type;
        Class<?> funcInterface = type.getJavaClass(); /*switch (expression.parameters.size()) {
            case 0 -> Action0.class;
            case 1 -> Action1.class;
            case 2 -> Action2.class;
            default -> throw new InternalException("Too much Action arguments.");
        };*/

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String name = "com/zergatul/scripting/dynamic/DynamicLambdaClass_" + counter.incrementAndGet();
        writer.visit(V1_5, ACC_PUBLIC, name, null, Type.getInternalName(Object.class), new String[] { Type.getInternalName(funcInterface) });

        buildEmptyConstructor(writer);

        Type[] argumentTypes = new Type[expression.parameters.size()];
        Arrays.fill(argumentTypes, Type.getType(Object.class));
        MethodVisitor invokeVisitor = writer.visitMethod(
                ACC_PUBLIC,
                "invoke",
                Type.getMethodDescriptor(
                        type.isFunction() ? Type.getType(Object.class) : Type.VOID_TYPE,
                        argumentTypes),
                null,
                null);
        invokeVisitor.visitCode();

        CompilerContext lambdaContext = context.createFunction(type.getReturnType(), true);
        LocalVariable[] arguments = new LocalVariable[expression.parameters.size()];
        for (int i = 0; i < expression.parameters.size(); i++) {
            arguments[i] = lambdaContext.addLocalVariable(null, SType.fromJavaType(Object.class), null);
        }
        for (int i = 0; i < expression.parameters.size(); i++) {
            BoundParameterNode parameter = expression.parameters.get(i);
            LocalVariable unboxed = (LocalVariable) parameter.getName().symbol;
            lambdaContext.addLocalVariable(unboxed);
            Class<?> boxedType = parameter.getType().getBoxedVersion();

            arguments[i].compileLoad(context, invokeVisitor); // load argument
            if (boxedType != null) {
                invokeVisitor.visitTypeInsn(CHECKCAST, Type.getInternalName(boxedType)); // cast to boxed, example: java.lang.Integer
                parameter.getType().compileUnboxing(invokeVisitor); // convert to unboxed
            } else {
                invokeVisitor.visitTypeInsn(CHECKCAST, Type.getInternalName(parameter.getType().getJavaClass()));
            }
            unboxed.compileStore(context, invokeVisitor);
        }
        compileStatement(invokeVisitor, lambdaContext, expression.body);
        if (!type.isFunction()) {
            invokeVisitor.visitInsn(RETURN);
        }
        invokeVisitor.visitMaxs(0, 0);
        invokeVisitor.visitEnd();

        writer.visitEnd();

        Class<?> dynamic = classLoader.defineClass(name.replace('/', '.'), writer.toByteArray());

        visitor.visitTypeInsn(NEW, Type.getInternalName(dynamic));
        visitor.visitInsn(DUP);
        visitor.visitMethodInsn(
                INVOKESPECIAL,
                Type.getInternalName(dynamic),
                "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE),
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
}