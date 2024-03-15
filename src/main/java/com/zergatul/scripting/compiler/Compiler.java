package com.zergatul.scripting.compiler;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.binding.Binder;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.lexer.Lexer;
import com.zergatul.scripting.lexer.LexerInput;
import com.zergatul.scripting.lexer.LexerOutput;
import com.zergatul.scripting.parser.Parser;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.type.SVoidType;
import org.objectweb.asm.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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

    private void compileStatements(MethodVisitor visitor, CompilerContext context, List<BoundStatementNode> statements) {
        for (BoundStatementNode statement : statements) {
            compileStatement(visitor, context, statement);
        }
    }

    private void compileStatement(MethodVisitor visitor, CompilerContext context, BoundStatementNode statement) {
        switch (statement.getNodeType()) {
            case VARIABLE_DECLARATION -> compileVariableDeclaration(visitor, context, (BoundVariableDeclarationNode) statement);
            case ASSIGNMENT_STATEMENT -> compileAssignmentStatement(visitor, context, (BoundAssignmentStatementNode) statement);
            case EXPRESSION_STATEMENT -> compileExpressionStatement(visitor, context, (BoundExpressionStatementNode) statement);
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
        throw new InternalException(); // TODO
    }

    private void compileExpressionStatement(MethodVisitor visitor, CompilerContext context, BoundExpressionStatementNode statement) {
        compileExpression(visitor, context, statement.expression);
        if (!statement.expression.type.equals(SVoidType.instance)) {
            visitor.visitInsn(POP);
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
            case INVOCATION_EXPRESSION -> compileInvocationExpression(visitor, context, (BoundInvocationExpressionNode) expression);
            case PROPERTY_ACCESS_EXPRESSION -> compilePropertyAccessExpression(visitor, context, (BoundPropertyAccessExpressionNode) expression);
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

    private void compileInvocationExpression(MethodVisitor visitor, CompilerContext context, BoundInvocationExpressionNode invocation) {
        if (invocation.method == null) {
            throw new InternalException();
        }

        if (invocation.callee instanceof BoundMethodAccessExpressionNode methodAccess) {
            // this is method call
            compileExpression(visitor, context, methodAccess.callee);

            for (BoundExpressionNode expression : invocation.arguments.arguments) {
                compileExpression(visitor, context, expression);
            }

            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(invocation.method.getMethod().getDeclaringClass()),
                    invocation.method.getMethod().getName(),
                    Type.getMethodDescriptor(invocation.method.getMethod()),
                    false);

            return;
        }

        throw new InternalException();
    }

    private void compilePropertyAccessExpression(MethodVisitor visitor, CompilerContext context, BoundPropertyAccessExpressionNode propertyAccess) {
        if (!propertyAccess.property.canGet()) {
            throw new InternalException();
        }

        compileExpression(visitor, context, propertyAccess.callee);
        propertyAccess.property.compileGet(visitor);
    }
}