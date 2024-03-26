package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.type.*;
import com.zergatul.scripting.type.operation.UnaryOperation;
import org.objectweb.asm.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

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

        MethodVisitor constructorVisitor = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        constructorVisitor.visitCode();
        constructorVisitor.visitVarInsn(ALOAD, 0);
        constructorVisitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);
        constructorVisitor.visitInsn(RETURN);
        constructorVisitor.visitMaxs(0, 0);
        constructorVisitor.visitEnd();

        MethodVisitor runVisitor = writer.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
        runVisitor.visitCode();
        compileStatements(runVisitor, parameters.getContext(), unit.statements);
        runVisitor.visitInsn(RETURN);
        runVisitor.visitMaxs(0, 0);
        runVisitor.visitEnd();

        writer.visitEnd();

        @SuppressWarnings("unchecked")
        Class<Runnable> dynamic = (Class<Runnable>) classLoader.defineClass(name.replace('/', '.'), writer.toByteArray());
        return createInstance(dynamic);
    }

    private void compileStatement(MethodVisitor visitor, CompilerContext context, BoundStatementNode statement) {
        switch (statement.getNodeType()) {
            case VARIABLE_DECLARATION -> compileVariableDeclaration(visitor, context, (BoundVariableDeclarationNode) statement);
            case ASSIGNMENT_STATEMENT -> compileAssignmentStatement(visitor, context, (BoundAssignmentStatementNode) statement);
            case EXPRESSION_STATEMENT -> compileExpressionStatement(visitor, context, (BoundExpressionStatementNode) statement);
            case IF_STATEMENT -> compileIfStatement(visitor, context, (BoundIfStatementNode) statement);
            case BLOCK_STATEMENT -> compileBlockStatement(visitor, context, (BoundBlockStatementNode) statement);
            case RETURN_STATEMENT -> compileReturnStatement(visitor, context, (BoundReturnStatementNode) statement);
            case FOR_LOOP_STATEMENT -> compileForLoopStatement(visitor, context, (BoundForLoopStatementNode) statement);
            case FOREACH_LOOP_STATEMENT -> compileForEachLoopStatement(visitor, context, (BoundForEachLoopStatementNode) statement);
            case BREAK_STATEMENT -> compileBreakStatement(visitor, context);
            case CONTINUE_STATEMENT -> compileContinueStatement(visitor, context);
            case EMPTY_STATEMENT -> compileEmptyStatement();
            case INCREMENT_STATEMENT, DECREMENT_STATEMENT -> compileIncDecStatement(visitor, context, (BoundIncDecStatementNode) statement);
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

        variable.compileStore(visitor);
    }

    private void compileAssignmentStatement(MethodVisitor visitor, CompilerContext context, BoundAssignmentStatementNode assignment) {
        switch (assignment.operator.operator) {
            case ASSIGNMENT -> {
                switch (assignment.left.getNodeType()) {
                    case NAME_EXPRESSION -> {
                        compileExpression(visitor, context, assignment.right);
                        BoundNameExpressionNode nameExpression = (BoundNameExpressionNode) assignment.left;
                        Variable variable = (Variable) context.getSymbol(nameExpression.value);
                        variable.compileStore(visitor);
                    }
                    case INDEX_EXPRESSION -> {
                        BoundIndexExpressionNode indexExpression = (BoundIndexExpressionNode) assignment.left;
                        compileExpression(visitor, context, indexExpression.callee);
                        compileExpression(visitor, context, indexExpression.index);
                        compileExpression(visitor, context, assignment.right);
                        indexExpression.operation.compileSet(visitor);
                    }
                }
            }
            default -> {
                throw new InternalException(); // TODO
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
        if (statement.expression == null) {
            visitor.visitInsn(SVoidType.instance.getReturnInst());
        } else {
            throw new InternalException(); // TODO
        }
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
        statement.index.compileStore(visitor);

        visitor.visitInsn(DUP);
        visitor.visitInsn(ARRAYLENGTH);
        statement.length.compileStore(visitor);

        visitor.visitLabel(begin);

        // index >= length -- GOTO end
        statement.index.compileLoad(visitor);
        statement.length.compileLoad(visitor);
        visitor.visitJumpInsn(IF_ICMPGE, end);

        // variable = array[index]
        visitor.visitInsn(DUP);
        statement.index.compileLoad(visitor);
        visitor.visitInsn(variable.getType().getArrayLoadInst());
        variable.compileStore(visitor);

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

    private void compileBreakStatement(MethodVisitor visitor, CompilerContext context) {
        context.compileBreak(visitor);
    }

    private void compileContinueStatement(MethodVisitor visitor, CompilerContext context) {
        context.compileContinue(visitor);
    }

    private void compileEmptyStatement() {

    }

    private void compileIncDecStatement(MethodVisitor visitor, CompilerContext context, BoundIncDecStatementNode statement) {
        switch (statement.expression.getNodeType()) {
            case NAME_EXPRESSION -> {
                BoundNameExpressionNode nameExpression = (BoundNameExpressionNode) statement.expression;
                compileExpression(visitor, context, statement.expression);
                statement.operation.apply(visitor);
                Variable variable = (Variable) context.getSymbol(nameExpression.value);
                variable.compileStore(visitor);
            }
            case INDEX_EXPRESSION -> {
                BoundIndexExpressionNode indexExpression = (BoundIndexExpressionNode) statement.expression;
                compileExpression(visitor, context, indexExpression.callee);
                compileExpression(visitor, context, indexExpression.index);
                visitor.visitInsn(DUP2);
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
            case UNARY_EXPRESSION -> compileUnaryExpression(visitor, context, (BoundUnaryExpressionNode) expression);
            case BINARY_EXPRESSION -> compileBinaryExpression(visitor, context, (BoundBinaryExpressionNode) expression);
            case IMPLICIT_CAST -> compileImplicitCastExpression(visitor, context, (BoundImplicitCastExpressionNode) expression);
            case NAME_EXPRESSION -> compileNameExpression(visitor, (BoundNameExpressionNode) expression);
            //case INVOCATION_EXPRESSION -> compileInvocationExpression(visitor, context, (BoundInvocationExpressionNode) expression);
            case METHOD_INVOCATION_EXPRESSION -> compileMethodInvocationExpression(visitor, context, (BoundMethodInvocationExpressionNode) expression);
            case PROPERTY_ACCESS_EXPRESSION -> compilePropertyAccessExpression(visitor, context, (BoundPropertyAccessExpressionNode) expression);
            case NEW_EXPRESSION -> compileNewExpression(visitor, context, (BoundNewExpressionNode) expression);
            case INDEX_EXPRESSION -> compileIndexExpression(visitor, context, (BoundIndexExpressionNode) expression);
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

    private void compileImplicitCastExpression(MethodVisitor visitor, CompilerContext context, BoundImplicitCastExpressionNode expression) {
        compileExpression(visitor, context, expression.operand);
        expression.operation.apply(visitor);
    }

    private void compileNameExpression(MethodVisitor visitor, BoundNameExpressionNode expression) {
        if (expression.symbol instanceof Variable variable) {
            variable.compileLoad(visitor);
        }
    }

    private void compileMethodInvocationExpression(MethodVisitor visitor, CompilerContext context, BoundMethodInvocationExpressionNode invocation) {
        compileExpression(visitor, context, invocation.objectReference);
        for (BoundExpressionNode expression : invocation.arguments.arguments) {
            compileExpression(visitor, context, expression);
        }
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(invocation.method.getMethod().getDeclaringClass()),
                invocation.method.getMethod().getName(),
                Type.getMethodDescriptor(invocation.method.getMethod()),
                false);
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
}