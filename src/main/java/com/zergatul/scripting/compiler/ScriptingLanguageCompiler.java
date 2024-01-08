package com.zergatul.scripting.compiler;

import com.zergatul.scripting.compiler.operations.BinaryOperation;
import com.zergatul.scripting.compiler.operations.ImplicitCast;
import com.zergatul.scripting.compiler.operations.UnaryOperation;
import com.zergatul.scripting.compiler.types.*;
import com.zergatul.scripting.compiler.variables.FunctionEntry;
import com.zergatul.scripting.compiler.variables.StaticVariableEntry;
import com.zergatul.scripting.compiler.variables.VariableContextStack;
import com.zergatul.scripting.compiler.variables.VariableEntry;
import com.zergatul.scripting.generated.*;
import org.objectweb.asm.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.objectweb.asm.Opcodes.*;

public class ScriptingLanguageCompiler {

    private static AtomicInteger counter = new AtomicInteger(0);
    private static ScriptingClassLoader classLoader = new ScriptingClassLoader();
    private final Class<?> root;
    private final MethodVisibilityChecker visibilityChecker;

    public ScriptingLanguageCompiler(Class<?> root) {
        this(root, new MethodVisibilityChecker());
    }

    public ScriptingLanguageCompiler(Class<?> root, MethodVisibilityChecker visibilityChecker) {
        this.root = root;
        this.visibilityChecker = visibilityChecker;
    }

    public Runnable compile(String program) throws ParseException, ScriptCompileException {
        program += "\r\n"; // temp fix for error if last token is comment

        InputStream stream = new ByteArrayInputStream(program.getBytes(StandardCharsets.UTF_8));
        ScriptingLanguage parser = new ScriptingLanguage(stream);
        ASTInput input = parser.Input();
        Class<Runnable> dynamic = compileRunnable((cw, v1, v2) -> {
            compile(input, cw, v1, v2);
        });

        Constructor<Runnable> constructor;
        try {
            constructor = dynamic.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new ScriptCompileException("Cannot find constructor for dynamic class.");
        }

        Runnable instance;
        try {
            instance = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ScriptCompileException("Cannot instantiate dynamic class.");
        }

        return instance;
    }

    private Class<Runnable> compileRunnable(CompileConsumer consumer) throws ScriptCompileException {
        // since this is instance method, local vars start from 1
        // 0 = this
        return compileRunnable(consumer, new VariableContextStack(1));
    }

    @SuppressWarnings("unchecked")
    private Class<Runnable> compileRunnable(CompileConsumer consumer, VariableContextStack context) throws ScriptCompileException {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        String name = "com/zergatul/scripting/dynamic/DynamicClass_" + counter.incrementAndGet();
        writer.visit(V1_5, ACC_PUBLIC, name, null, Type.getInternalName(Object.class), new String[] { Type.getInternalName(Runnable.class) });

        MethodVisitor constructorVisitor = writer.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        constructorVisitor.visitCode();
        constructorVisitor.visitVarInsn(ALOAD, 0);
        constructorVisitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);

        MethodVisitor runVisitor = writer.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
        runVisitor.visitCode();

        MethodVisitorWrapper constructorVisitorWrapper = new MethodVisitorWrapper(constructorVisitor, name, context);

        MethodVisitorWrapper runVisitorWrapper = new MethodVisitorWrapper(runVisitor, name, context);
        runVisitorWrapper.getLoops().push(
                v -> {
                    throw new ScriptCompileException("Continue statement without loop.");
                },
                v -> {
                    throw new ScriptCompileException("Break statement without loop.");
                });
        consumer.apply(writer, constructorVisitorWrapper, runVisitorWrapper);

        constructorVisitor.visitInsn(RETURN);
        constructorVisitor.visitMaxs(0, 0);
        constructorVisitor.visitEnd();

        runVisitor.visitInsn(RETURN);
        runVisitor.visitMaxs(0, 0);
        runVisitor.visitEnd();

        for (StaticVariableEntry entry : runVisitorWrapper.getContextStack().getStaticVariables()) {
            if (entry.getClassName().equals(name)) {
                FieldVisitor fieldVisitor = writer.visitField(
                        ACC_PUBLIC | ACC_STATIC,
                        entry.getIdentifier(),
                        Type.getDescriptor(entry.getType().getJavaClass()),
                        null, null);
                fieldVisitor.visitEnd();
            }
        }

        writer.visitEnd();

        byte[] code = writer.toByteArray();
        return (Class<Runnable>) classLoader.defineClass(name.replace('/', '.'), code);
    }

    private void compile(
            ASTInput input,
            ClassWriter classWriter,
            CompilerMethodVisitor constructorVisitor,
            CompilerMethodVisitor runVisitor
    ) throws ScriptCompileException {
        if (input.jjtGetNumChildren() < 2) {
            throw new ScriptCompileException("ASTInput: num children < 2.");
        }

        if (!(input.jjtGetChild(0) instanceof ASTStaticVariablesList variablesList)) {
            throw new ScriptCompileException("ASTInput: static vars list expected.");
        }

        if (!(input.jjtGetChild(1) instanceof ASTFunctionsList functionsList)) {
            throw new ScriptCompileException("ASTInput: functions list expected.");
        }

        compile(variablesList, constructorVisitor);
        compile(functionsList, classWriter, constructorVisitor);

        for (int i = 2; i < input.jjtGetNumChildren(); i++) {
            if (!(input.jjtGetChild(i) instanceof ASTStatement statement)) {
                throw new ScriptCompileException("ASTInput statement expected.");
            }

            compile(statement, runVisitor);
        }
    }

    private void compile(ASTStaticVariablesList list, CompilerMethodVisitor visitor) throws ScriptCompileException {
        for (int i = 0; i < list.jjtGetNumChildren(); i++) {
            if (!(list.jjtGetChild(i) instanceof ASTStaticVariableDeclaration variableDeclaration)) {
                throw new ScriptCompileException("ASTStaticVariablesList declaration expected.");
            }

            compile(variableDeclaration, visitor);
        }
    }

    private void compile(
            ASTFunctionsList list,
            ClassWriter classWriter,
            CompilerMethodVisitor visitor
    ) throws ScriptCompileException {
        for (int i = 0; i < list.jjtGetNumChildren(); i++) {
            if (!(list.jjtGetChild(i) instanceof ASTFunctionDeclaration functionDeclaration)) {
                throw new ScriptCompileException("ASTFunctionsList: declaration expected.");
            }

            SType type = getFunctionReturnType(functionDeclaration);
            ASTIdentifier identifier = getFunctionIdentifier(functionDeclaration);
            List<FunctionParameter> parameters = getFunctionParameters(functionDeclaration);

            String name = (String) identifier.jjtGetValue();
            visitor.getContextStack().addFunction(
                    name,
                    type,
                    parameters.stream().map(p -> p.type).toArray(SType[]::new),
                    visitor.getClassName());
        }

        for (int i = 0; i < list.jjtGetNumChildren(); i++) {
            compile(
                    (ASTFunctionDeclaration) list.jjtGetChild(i),
                    classWriter,
                    visitor.getContextStack().newWithStaticVariables(0));
        }
    }

    private void compile(ASTStaticVariableDeclaration declaration, CompilerMethodVisitor visitor) throws ScriptCompileException {
        if (declaration.jjtGetNumChildren() != 1) {
            throw new ScriptCompileException("Invalid static var decl structure.");
        }

        Node node = declaration.jjtGetChild(0);
        if (!(node instanceof ASTLocalVariableDeclaration localVariableDeclaration)) {
            throw new ScriptCompileException("ASTLocalVariableDeclaration expected.");
        }

        ASTType astType = (ASTType) localVariableDeclaration.jjtGetChild(0);
        ASTVariableDeclarator variableDeclarator = (ASTVariableDeclarator) localVariableDeclaration.jjtGetChild(1);
        ASTVariableDeclaratorId variableDeclaratorId = (ASTVariableDeclaratorId) variableDeclarator.jjtGetChild(0);
        ASTIdentifier identifier = (ASTIdentifier) variableDeclaratorId.jjtGetChild(0);
        ASTVariableInitializer initializer = null;
        if (variableDeclarator.jjtGetNumChildren() > 1) {
            initializer = (ASTVariableInitializer) variableDeclarator.jjtGetChild(1);
        }

        SType type = parseType(astType);
        if (initializer != null) {
            SType returnType = compile((ASTExpression) initializer.jjtGetChild(0), visitor);
            if (!returnType.equals(type)) {
                UnaryOperation operation = ImplicitCast.get(returnType, type);
                if (operation != null) {
                    operation.apply(visitor);
                } else {
                    throw new ScriptCompileException(String.format("Static variable type %s assigned to expression of type %s.", type, returnType));
                }
            }
        } else {
            type.storeDefaultValue(visitor);
        }

        String variableName = (String) identifier.jjtGetValue();
        if (Arrays.stream(root.getDeclaredFields()).anyMatch(f -> f.getName().equals(variableName))) {
            throw new ScriptCompileException(String.format("Cannot declare static variable %s because API class with the same name exists.", variableName));
        }

        VariableEntry variable = visitor.getContextStack().addStatic(variableName, type, visitor.getClassName());
        variable.compileStore(visitor);
    }

    private void compile(
            ASTFunctionDeclaration declaration,
            ClassWriter classWriter,
            VariableContextStack context
    ) throws ScriptCompileException {
        ASTIdentifier identifier = getFunctionIdentifier(declaration);
        String name = (String) identifier.jjtGetValue();
        List<FunctionParameter> parameters = getFunctionParameters(declaration);
        ASTBlock block = getFunctionBody(declaration);
        FunctionEntry entry = context.getFunction(name);

        if (entry.getReturnType() != SVoidType.instance) {
            if (!isLastNode(block, ASTReturnStatement.class)) {
                throw new ScriptCompileException(String.format(
                        "Function %s: last statement should be return.",
                        name));
            }
        }

        for (FunctionParameter parameter : parameters) {
            context.addLocal(parameter.identifier, parameter.type);
        }

        MethodVisitor visitor = classWriter.visitMethod(
                ACC_PUBLIC | ACC_STATIC,
                name,
                entry.getDescriptor(),
                null,
                null);
        visitor.visitCode();

        MethodVisitorWrapper visitorWrapper = new MethodVisitorWrapper(visitor, name, context, entry.getReturnType());
        visitorWrapper.getLoops().push(
                v -> {
                    throw new ScriptCompileException("Continue statement without loop.");
                },
                v -> {
                    throw new ScriptCompileException("Break statement without loop.");
                });

        compile(block, visitorWrapper);

        visitor.visitInsn(RETURN);
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();
    }

    private SType getFunctionReturnType(ASTFunctionDeclaration declaration) throws ScriptCompileException {
        if (declaration.jjtGetChild(0) instanceof ASTType type) {
            return parseType(type);
        } else {
            return SVoidType.instance;
        }
    }

    private ASTIdentifier getFunctionIdentifier(ASTFunctionDeclaration declaration) throws ScriptCompileException {
        if (declaration.jjtGetNumChildren() == 3) {
            return (ASTIdentifier) declaration.jjtGetChild(0);
        }
        if (declaration.jjtGetNumChildren() == 4) {
            return (ASTIdentifier) declaration.jjtGetChild(1);
        }
        throw new ScriptCompileException("ASTFunctionDeclaration: unexpected children count.");
    }

    private List<FunctionParameter> getFunctionParameters(ASTFunctionDeclaration declaration) throws ScriptCompileException {
        List<FunctionParameter> parameters = new ArrayList<>();
        ASTFunctionParameterList list = (ASTFunctionParameterList) declaration.jjtGetChild(declaration.jjtGetNumChildren() - 2);
        for (int i = 0; i < list.jjtGetNumChildren(); i += 2) {
            SType type = parseType((ASTType) list.jjtGetChild(i));
            ASTIdentifier identifier = (ASTIdentifier) list.jjtGetChild(i + 1);
            parameters.add(new FunctionParameter(type, (String) identifier.jjtGetValue()));
        }
        return parameters;
    }

    private ASTBlock getFunctionBody(ASTFunctionDeclaration declaration) {
        return (ASTBlock) declaration.jjtGetChild(declaration.jjtGetNumChildren() - 1);
    }

    private void compile(ASTStatement statement, CompilerMethodVisitor visitor) throws ScriptCompileException {
        Node node = statement.jjtGetChild(0);
        if (node instanceof ASTEmptyStatement) {
            return;
        }
        if (node instanceof ASTStatementExpression statementExpression) {
            compile(statementExpression, visitor);
            return;
        }
        if (node instanceof ASTBlock block) {
            compile(block, visitor);
            return;
        }
        if (node instanceof ASTLocalVariableDeclaration localVariableDeclaration) {
            compile(localVariableDeclaration, visitor);
            return;
        }
        if (node instanceof ASTIfStatement ifStatement) {
            compile(ifStatement, visitor);
            return;
        }
        if (node instanceof ASTForStatement forStatement) {
            compile(forStatement, visitor);
            return;
        }
        if (node instanceof ASTForEachStatement forEachStatement) {
            compile(forEachStatement, visitor);
            return;
        }
        if (node instanceof ASTContinueStatement) {
            visitor.getLoops().compileContinue(visitor);
            return;
        }
        if (node instanceof ASTBreakStatement) {
            visitor.getLoops().compileBreak(visitor);
            return;
        }
        if (node instanceof ASTReturnStatement returnStatement) {
            compile(returnStatement, visitor);
            return;
        }

        throw new ScriptCompileException("ASTStatement case not implemented: " + node.getClass().getName() + ".");
    }

    private void compile(ASTStatementExpression statementExpression, CompilerMethodVisitor visitor) throws ScriptCompileException {
        Node node = statementExpression.jjtGetChild(0);
        if (node instanceof ASTPreIncrementExpression preIncrementExpression) {
            throw new ScriptCompileException("Not implemented");
        } else if (node instanceof ASTPreDecrementExpression preDecrementExpression) {
            throw new ScriptCompileException("Not implemented");
        } else if (node instanceof ASTPrimaryExpression primary) {
            if (statementExpression.jjtGetNumChildren() == 1) {
                SType type = compile(primary, visitor);
                if (type != SVoidType.instance) {
                    visitor.visitInsn(POP);
                }
                return;
            } else if (statementExpression.jjtGetNumChildren() == 2) {
                SType type = compile(primary, visitor);
                if (type != SIntType.instance) {
                    throw new ScriptCompileException("Operators ++/-- can only be applied to int.");
                }

                if (statementExpression.jjtGetChild(1) instanceof ASTIncrement) {
                    visitor.visitInsn(ICONST_1);
                    visitor.visitInsn(IADD);
                } else if (statementExpression.jjtGetChild(1) instanceof ASTDecrement) {
                    visitor.visitInsn(ICONST_1);
                    visitor.visitInsn(ISUB);
                } else {
                    throw new ScriptCompileException("ASTStatementExpression: unknown post operator.");
                }

                ASTPrimaryPrefix primaryPrefix = (ASTPrimaryPrefix) primary.jjtGetChild(0);
                if (primaryPrefix.jjtGetChild(0) instanceof ASTName name) {
                    if (name.jjtGetNumChildren() > 1) {
                        throw new ScriptCompileException("Assignment: cannot assign field.");
                    }

                    ASTIdentifier identifier = (ASTIdentifier) name.jjtGetChild(0);
                    VariableEntry variable = visitor.getContextStack().get((String) identifier.jjtGetValue());
                    if (variable == null) {
                        throw new ScriptCompileException(String.format("Variable %s is not declared.", identifier.jjtGetValue()));
                    }

                    if (primary.jjtGetNumChildren() == 1) {
                        variable.compileStore(visitor);
                    } else {
                        ASTPrimarySuffix primarySuffix = (ASTPrimarySuffix) primary.jjtGetChild(1);
                        if (primarySuffix.jjtGetChild(0) instanceof ASTExpression indexExpression) {
                            if (variable.getType() instanceof SArrayType arrayType) {
                                variable.compileLoad(visitor); // load array
                                visitor.visitInsn(SWAP);
                                SType indexType = compile(indexExpression, visitor); // load index
                                visitor.visitInsn(SWAP);
                                if (indexType != SIntType.instance) {
                                    throw new ScriptCompileException("Array index can be only integer.");
                                }
                                visitor.visitInsn(arrayType.getElementsType().getArrayStoreInst());
                            } else {
                                throw new ScriptCompileException(String.format("Variable %s is not array.", identifier.jjtGetValue()));
                            }
                        } else {
                            throw new ScriptCompileException("ASTAssignStatement -> ASTPrimarySuffix is not ASTExpression.");
                        }
                    }
                } else {
                    throw new ScriptCompileException("ASTAssignStatement -> ASTPrimaryPrefix is not ASTName.");
                }
                return;
            } else if (statementExpression.jjtGetNumChildren() == 3) {
                ASTAssignmentOperator assignmentOperator = (ASTAssignmentOperator) statementExpression.jjtGetChild(1);
                ASTExpression expression = (ASTExpression) statementExpression.jjtGetChild(2);
                compileAssignment(primary, assignmentOperator, expression, visitor);
                return;
            } else {
                throw new ScriptCompileException("ASTStatementExpression/ASTPrimaryExpression children > 3 not implemented");
            }
        }
        throw new ScriptCompileException("ASTStatementExpression case not implemented: " + node.getClass().getName() + ".");
    }

    private void compileAssignment(
            ASTPrimaryExpression primary,
            ASTAssignmentOperator operator,
            ASTExpression expression,
            CompilerMethodVisitor visitor
    ) throws ScriptCompileException {
        ASTPrimaryPrefix primaryPrefix = (ASTPrimaryPrefix) primary.jjtGetChild(0);

        if (primaryPrefix.jjtGetChild(0) instanceof ASTName name) {
            if (name.jjtGetNumChildren() > 1) {
                throw new ScriptCompileException("Assignment: cannot assign field.");
            }

            ASTIdentifier identifier = (ASTIdentifier) name.jjtGetChild(0);
            VariableEntry variable = visitor.getContextStack().get((String) identifier.jjtGetValue());
            if (variable == null) {
                throw new ScriptCompileException(String.format("Variable %s is not declared.", identifier.jjtGetValue()));
            }

            if (primary.jjtGetNumChildren() == 1) {
                SType rightType = compile(expression, visitor);
                if (!rightType.equals(variable.getType())) {
                    throw new ScriptCompileException(String.format("Attempt to assign %s to variable %s of type %s.", rightType, identifier.jjtGetValue(), variable.getType()));
                }

                variable.compileStore(visitor);
            } else {
                ASTPrimarySuffix primarySuffix = (ASTPrimarySuffix) primary.jjtGetChild(1);
                if (primarySuffix.jjtGetChild(0) instanceof ASTExpression indexExpression) {
                    if (variable.getType() instanceof SArrayType arrayType) {
                        variable.compileLoad(visitor); // load array
                        SType indexType = compile(indexExpression, visitor); // load index
                        if (indexType != SIntType.instance) {
                            throw new ScriptCompileException("Array index can be only integer.");
                        }
                        SType rightType = compile(expression, visitor); // load value
                        if (!arrayType.getElementsType().equals(rightType)) {
                            // TODO: implicit cast?
                            throw new ScriptCompileException("Array element type doesn't match.");
                        }
                        visitor.visitInsn(arrayType.getElementsType().getArrayStoreInst());
                    } else {
                        throw new ScriptCompileException(String.format("Variable %s is not array.", identifier.jjtGetValue()));
                    }
                } else {
                    throw new ScriptCompileException("ASTAssignStatement -> ASTPrimarySuffix is not ASTExpression.");
                }
            }
        } else {
            throw new ScriptCompileException("ASTAssignStatement -> ASTPrimaryPrefix is not ASTName.");
        }
    }

    private void compile(ASTBlock block, CompilerMethodVisitor visitor) throws ScriptCompileException {
        visitor.getContextStack().begin();
        int numChildren = block.jjtGetNumChildren();
        for (int i = 0; i < numChildren; i++) {
            compile((ASTBlockStatement) block.jjtGetChild(i), visitor);
        }
        visitor.getContextStack().end();
    }

    private SType compile(ASTPrimaryExpression primaryExpression, CompilerMethodVisitor visitor) throws ScriptCompileException {
        ASTPrimaryPrefix prefix = (ASTPrimaryPrefix) primaryExpression.jjtGetChild(0);
        Node prefixNode = prefix.jjtGetChild(0);
        if (primaryExpression.jjtGetNumChildren() == 1) {
            if (prefixNode instanceof ASTLiteral literal) {
                return compile(literal, visitor);
            }
            if (prefixNode instanceof ASTName name) {
                if (name.jjtGetNumChildren() == 1) {
                    // variable
                    ASTIdentifier identifier = (ASTIdentifier) name.jjtGetChild(0);
                    VariableEntry variable = visitor.getContextStack().get((String) identifier.jjtGetValue());
                    if (variable == null) {
                        throw new ScriptCompileException(String.format("Variable %s is not declared.", identifier.jjtGetValue()));
                    }

                    variable.compileLoad(visitor);
                    return variable.getType();
                } else if (name.jjtGetNumChildren() == 2) {
                    // identifier1.identifier2
                    String identifier1 = (String) ((ASTIdentifier) name.jjtGetChild(0)).jjtGetValue();
                    String identifier2 = (String) ((ASTIdentifier) name.jjtGetChild(1)).jjtGetValue();

                    VariableEntry variable = visitor.getContextStack().get(identifier1);
                    if (variable == null) {
                        throw new ScriptCompileException(String.format("Variable %s is not declared.", identifier1));
                    }

                    variable.compileLoad(visitor);
                    SType type = variable.getType().compileGetField(identifier2, visitor);
                    if (type == null) {
                        throw new ScriptCompileException(String.format("Cannot find field %s for variable %s.", identifier2, identifier1));
                    }
                    return type;
                } else {
                    throw new ScriptCompileException(String.format("ASTName: %s num children.", name.jjtGetNumChildren()));
                }
            }
            if (prefixNode instanceof ASTExpression expression) {
                return compile(expression, visitor);
            }
            if (prefixNode instanceof ASTEmptyArrayInitializationExpression emptyArrayInitializationExpression) {
                return compile(emptyArrayInitializationExpression, visitor);
            }
            if (prefixNode instanceof ASTInlineArrayInitializationExpression inlineArrayInitialization) {
                return compile(inlineArrayInitialization, visitor);
            }
            throw new ScriptCompileException("ASTPrimaryExpression(1) case not implemented: " + prefixNode.getClass().getName() + ".");
        } else {
            ASTPrimarySuffix suffix = (ASTPrimarySuffix) primaryExpression.jjtGetChild(1);
            if (prefixNode instanceof ASTLiteral) {
                throw new ScriptCompileException("ASTLiteral cannot have PrimarySuffix");
            }
            if (prefixNode instanceof ASTName name) {
                if (suffix.jjtGetChild(0) instanceof ASTArguments arguments) {
                    return compile(name, arguments, visitor);
                }
                if (suffix.jjtGetChild(0) instanceof ASTExpression expression) {
                    if (name.jjtGetNumChildren() > 1) {
                        throw new ScriptCompileException("ASTPrimaryExpression cannot reference fields.");
                    }

                    ASTIdentifier identifier = (ASTIdentifier) name.jjtGetChild(0);
                    VariableEntry variable = visitor.getContextStack().get((String) identifier.jjtGetValue());
                    if (variable == null) {
                        throw new ScriptCompileException(String.format("Variable %s is not declared.", identifier.jjtGetValue()));
                    }

                    if (variable.getType() instanceof SArrayType arrayType) {
                        variable.compileLoad(visitor); // load array

                        SType indexType = compile(expression, visitor); // load index
                        if (indexType != SIntType.instance) {
                            throw new ScriptCompileException("Array index can only be integer.");
                        }

                        visitor.visitInsn(arrayType.getElementsType().getArrayLoadInst());

                        return arrayType.getElementsType();
                    } else {
                        throw new ScriptCompileException(String.format("Variable %s is not array.", identifier.jjtGetValue()));
                    }
                }
                throw new ScriptCompileException("ASTPrimaryExpression(2) -> ASTName - unexpected case");
            }
            if (prefixNode instanceof ASTExpression) {
                throw new ScriptCompileException("ASTExpression cannot have PrimarySuffix");
            }
            throw new ScriptCompileException("ASTPrimaryExpression(2) case not implemented: " + prefixNode.getClass().getName() + ".");
        }
    }

    private SType compile(ASTName name, ASTArguments arguments, CompilerMethodVisitor visitor) throws ScriptCompileException {
        if (name.jjtGetNumChildren() == 1) {
            String identifier = (String) ((SimpleNode) name.jjtGetChild(0)).jjtGetValue();
            FunctionEntry entry = visitor.getContextStack().getFunction(identifier);

            if (entry == null) {
                throw new ScriptCompileException("Function " + identifier + " is not declared.");
            }

            if (arguments.jjtGetNumChildren() != entry.getParameters().length) {
                throw new ScriptCompileException("Call to function " + identifier + ": argument count mismatch.");
            }

            for (int i = 0; i < arguments.jjtGetNumChildren(); i++) {
                SType sourceType = compile((ASTExpression) arguments.jjtGetChild(i), visitor);
                SType destType = entry.getParameters()[i];
                if (!sourceType.equals(destType)) {
                    UnaryOperation operation = ImplicitCast.get(sourceType, destType);
                    if (operation == null) {
                        throw new ScriptCompileException("Call to function " + identifier + ": cannot cast argument of type " + sourceType + " to " + destType + ".");
                    }
                    operation.apply(visitor);
                }
            }

            visitor.visitMethodInsn(
                    INVOKESTATIC,
                    entry.getClassName(),
                    identifier,
                    entry.getDescriptor(),
                    false);

            return entry.getReturnType();
        }

        String fieldName = (String) ((SimpleNode) name.jjtGetChild(0)).jjtGetValue();

        Field field;
        try {
            field = root.getDeclaredField(fieldName);
        }
        catch (NoSuchFieldException e) {
            throw new ScriptCompileException("Cannot find field \"" + fieldName + "\".");
        }

        visitor.visitFieldInsn(
                GETSTATIC,
                Type.getInternalName(root),
                field.getName(),
                Type.getDescriptor(field.getType()));

        Class<?> currentInstance = field.getType();
        for (int i = 1; i < name.jjtGetNumChildren() - 1; i++) {
            fieldName = (String) ((SimpleNode) name.jjtGetChild(i)).jjtGetValue();
            try {
                field = currentInstance.getDeclaredField(fieldName);
            }
            catch (NoSuchFieldException e) {
                throw new ScriptCompileException("Cannot find field \"" + fieldName + "\".");
            }

            visitor.visitFieldInsn(
                    GETFIELD,
                    Type.getInternalName(currentInstance),
                    field.getName(),
                    Type.getDescriptor(field.getType()));

            currentInstance = field.getType();
        }

        int argsLength;
        SType[] methodArgumentTypes;
        BufferVisitor[] methodArgumentVisitors;
        if (arguments.jjtGetNumChildren() == 0) {
            argsLength = 0;
            methodArgumentTypes = new SType[0];
            methodArgumentVisitors = new BufferVisitor[0];
        } else {
            argsLength = arguments.jjtGetNumChildren();
            methodArgumentTypes = new SType[argsLength];
            methodArgumentVisitors = new BufferVisitor[argsLength];
            for (int i = 0; i < argsLength; i++) {
                methodArgumentVisitors[i] = new BufferVisitor(visitor.getContextStack(), visitor.getClassName());
                methodArgumentTypes[i] = compile((ASTExpression) arguments.jjtGetChild(i), methodArgumentVisitors[i]);
            }
        }

        String methodName = (String) ((SimpleNode) name.jjtGetChild(name.jjtGetNumChildren() - 1)).jjtGetValue();

        Method method = findMethod(field, methodName, methodArgumentTypes, methodArgumentVisitors, visitor.getClassName());
        if (method == null) {
            throw new ScriptCompileException("Cannot find method \"" + methodName + "\".");
        }

        for (int i = 0; i < argsLength; i++) {
            methodArgumentVisitors[i].releaseBuffer(visitor);
        }

        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(method.getDeclaringClass()),
                method.getName(),
                Type.getMethodDescriptor(method),
                false);

        return SType.fromJavaClass(method.getReturnType());
    }

    private void compile(ASTBlockStatement blockStatement, CompilerMethodVisitor visitor) throws ScriptCompileException {
        for (int i = 0; i < blockStatement.jjtGetNumChildren(); i++) {
            if (blockStatement.jjtGetChild(i) instanceof ASTStatement statement) {
                compile(statement, visitor);
            } else {
                throw new ScriptCompileException("Unexpected type in ASTBlockStatement.");
            }
        }
    }

    private VariableEntry compile(ASTLocalVariableDeclaration localVariableDeclaration, CompilerMethodVisitor visitor) throws ScriptCompileException {
        ASTType astType = (ASTType) localVariableDeclaration.jjtGetChild(0);
        ASTVariableDeclarator variableDeclarator = (ASTVariableDeclarator) localVariableDeclaration.jjtGetChild(1);
        ASTVariableDeclaratorId variableDeclaratorId = (ASTVariableDeclaratorId) variableDeclarator.jjtGetChild(0);
        ASTIdentifier identifier = (ASTIdentifier) variableDeclaratorId.jjtGetChild(0);
        ASTVariableInitializer initializer = null;
        if (variableDeclarator.jjtGetNumChildren() > 1) {
            initializer = (ASTVariableInitializer) variableDeclarator.jjtGetChild(1);
        }

        SType type = parseType(astType);
        if (initializer != null) {
            SType returnType = compile((ASTExpression) initializer.jjtGetChild(0), visitor);
            if (!returnType.equals(type)) {
                UnaryOperation operation = ImplicitCast.get(returnType, type);
                if (operation != null) {
                    operation.apply(visitor);
                } else {
                    throw new ScriptCompileException(String.format("Variable type %s assigned to expression of type %s.", type, returnType));
                }
            }
        } else {
            type.storeDefaultValue(visitor);
        }

        String variableName = (String) identifier.jjtGetValue();
        if (Arrays.stream(root.getDeclaredFields()).anyMatch(f -> f.getName().equals(variableName))) {
            throw new ScriptCompileException(String.format("Cannot declare variable %s because API class with the same name exists.", variableName));
        }

        VariableEntry variable = visitor.getContextStack().addLocal(variableName, type);
        variable.compileStore(visitor);

        return variable;
    }

    private SType compile(ASTEmptyArrayInitializationExpression arrayInitializationExpression, CompilerMethodVisitor visitor) throws ScriptCompileException {
        ASTType astType = (ASTType) arrayInitializationExpression.jjtGetChild(0);
        ASTExpression expression = (ASTExpression) arrayInitializationExpression.jjtGetChild(1);

        SType dimensionsType = compile(expression, visitor);
        if (dimensionsType != SIntType.instance) {
            throw new ScriptCompileException("Array dimensions must be integer");
        }

        // TODO: check if dimensions negative?

        SType type = parseType(astType);
        if (type.isReference()) {
            visitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(type.getJavaClass()));
        } else {
            visitor.visitIntInsn(NEWARRAY, ((SPrimitiveType) type).getArrayTypeInst());
        }

        return new SArrayType(type);
    }

    private SType compile(ASTInlineArrayInitializationExpression arrayInitialization, CompilerMethodVisitor visitor) throws ScriptCompileException {
        ASTType astType = (ASTType) arrayInitialization.jjtGetChild(0);
        ASTExpression[] expressions = new ASTExpression[arrayInitialization.jjtGetNumChildren() - 1];
        for (int i = 0; i < expressions.length; i++) {
            expressions[i] = (ASTExpression) arrayInitialization.jjtGetChild(i + 1);
        }

        visitor.visitLdcInsn(expressions.length);

        if (!(parseType(astType) instanceof SArrayType arrayType)) {
            throw new ScriptCompileException(String.format("ASTInlineArrayInitialization: array type expected. Got: %s", parseType(astType)));
        }

        SType elementsType = arrayType.getElementsType();
        if (arrayType.getElementsType().isReference()) {
            visitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(elementsType.getJavaClass()));
        } else {
            visitor.visitIntInsn(NEWARRAY, ((SPrimitiveType) elementsType).getArrayTypeInst());
        }

        for (int i = 0; i < expressions.length; i++) {
            visitor.visitInsn(DUP);  // load array
            visitor.visitLdcInsn(i); // load index
            SType expressionType = compile(expressions[i], visitor); // load value
            if (!expressionType.equals(elementsType)) {
                UnaryOperation operation = ImplicitCast.get(expressionType, elementsType);
                if (operation == null) {
                    throw new ScriptCompileException(String.format("Cannot cast %s to %s in inline init expression.", expressionType, elementsType));
                }
                operation.apply(visitor);
            }
            visitor.visitInsn(elementsType.getArrayStoreInst());
        }

        return arrayType;
    }

    private void compile(ASTIfStatement ifStatement, CompilerMethodVisitor visitor) throws ScriptCompileException {
        int numChildren = ifStatement.jjtGetNumChildren();
        if (numChildren < 2) {
            throw new ScriptCompileException("ASTIfStatement invalid children count.");
        }

        ASTExpression ifExpr = (ASTExpression) ifStatement.jjtGetChild(0);
        ASTStatement thenStmt = (ASTStatement) ifStatement.jjtGetChild(1);
        ASTStatement elseStmt = numChildren > 2 ? (ASTStatement) ifStatement.jjtGetChild(2) : null;
        SType type = compile(ifExpr, visitor);
        if (type != SBoolean.instance) {
            throw new ScriptCompileException("Expression inside \"if\" statement should return boolean.");
        }

        if (elseStmt == null) {
            Label endLabel = new Label();
            visitor.visitJumpInsn(IFEQ, endLabel);
            compile(thenStmt, visitor);
            visitor.visitLabel(endLabel);
        } else {
            Label elseLabel = new Label();
            visitor.visitJumpInsn(IFEQ, elseLabel);
            compile(thenStmt, visitor);
            Label endLabel = new Label();
            visitor.visitJumpInsn(GOTO, endLabel);
            visitor.visitLabel(elseLabel);
            compile(elseStmt, visitor);
            visitor.visitLabel(endLabel);
        }
    }

    private void compile(ASTForStatement forStatement, CompilerMethodVisitor visitor) throws ScriptCompileException {
        visitor.getContextStack().begin();

        ASTForInit init = null;
        ASTExpression exit = null;
        ASTForUpdate update = null;

        int index = 0;
        if (forStatement.jjtGetChild(index) instanceof ASTForInit node) {
            init = node;
            index++;
        }
        if (forStatement.jjtGetChild(index) instanceof ASTExpression node) {
            exit = node;
            index++;
        }
        if (forStatement.jjtGetChild(index) instanceof ASTForUpdate node) {
            update = node;
            index++;
        }

        ASTStatement body = (ASTStatement) forStatement.jjtGetChild(index);

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

        if (init != null) {
            if (init.jjtGetChild(0) instanceof ASTLocalVariableDeclaration localVariableDeclaration) {
                compile(localVariableDeclaration, visitor);
            } else if (init.jjtGetChild(0) instanceof ASTStatementExpressionList statementExpressionList) {
                compile(statementExpressionList, visitor);
            } else {
                throw new ScriptCompileException(
                        String.format("ForInit unexpected child: %s", init.jjtGetChild(0).getClass().getSimpleName()));
            }
        }

        visitor.visitLabel(begin);

        if (exit != null) {
            SType type = compile(exit, visitor);
            if (type != SBoolean.instance) {
                throw new ScriptCompileException("For loop exit expression must return boolean.");
            }

            visitor.visitJumpInsn(IFEQ, end);
        }

        visitor.getLoops().push(
                v -> v.visitJumpInsn(GOTO, continueLabel),
                v -> v.visitJumpInsn(GOTO, end));
        compile(body, visitor);
        visitor.getLoops().pop();

        visitor.visitLabel(continueLabel);
        if (update != null) {
            compile((ASTStatementExpressionList) update.jjtGetChild(0), visitor);
        }

        visitor.visitJumpInsn(GOTO, begin);
        visitor.visitLabel(end);

        visitor.getContextStack().end();
    }

    private void compile(ASTForEachStatement forEachStatement, CompilerMethodVisitor visitor) throws ScriptCompileException {
        visitor.getContextStack().begin();

        ASTLocalVariableDeclaration variable = (ASTLocalVariableDeclaration) forEachStatement.jjtGetChild(0);
        ASTExpression iterable = (ASTExpression) forEachStatement.jjtGetChild(1);
        ASTStatement body = (ASTStatement) forEachStatement.jjtGetChild(2);

        VariableEntry loopVar = compile(variable, visitor);

        VariableEntry indexVar = visitor.getContextStack().addLocal(null, SIntType.instance);

        Label begin = new Label();
        Label continueLabel = new Label();
        Label end = new Label();

        visitor.visitInsn(ICONST_0);
        indexVar.compileStore(visitor);

        SType object = compile(iterable, visitor);
        if (!(object instanceof SArrayType arrayType)) {
            throw new ScriptCompileException("foreach can only loop over array.");
        }
        VariableEntry arrayVar = visitor.getContextStack().addLocal(null, arrayType);
        if (!arrayType.getElementsType().equals(loopVar.getType())) {
            throw new ScriptCompileException("foreach loop variable type mismatch.");
        }
        arrayVar.compileStore(visitor);

        visitor.visitLabel(begin);

        // if index >= length GOTO end
        indexVar.compileLoad(visitor);
        arrayVar.compileLoad(visitor);
        visitor.visitInsn(ARRAYLENGTH);
        visitor.visitJumpInsn(IF_ICMPGE, end);

        // loopVar = arrayVar[index]
        arrayVar.compileLoad(visitor);
        indexVar.compileLoad(visitor);
        visitor.visitInsn(loopVar.getType().getArrayLoadInst());
        loopVar.compileStore(visitor);

        visitor.getLoops().push(
                v -> v.visitJumpInsn(GOTO, continueLabel),
                v -> v.visitJumpInsn(GOTO, end));
        compile(body, visitor);
        visitor.getLoops().pop();

        // index++
        visitor.visitLabel(continueLabel);
        indexVar.compileIncrement(visitor, 1);

        visitor.visitJumpInsn(GOTO, begin);
        visitor.visitLabel(end);

        visitor.getContextStack().end();
    }

    private void compile(ASTReturnStatement returnStatement, CompilerMethodVisitor visitor) throws ScriptCompileException {
        SType type = SVoidType.instance;
        if (returnStatement.jjtGetNumChildren() > 0) {
            if (!(returnStatement.jjtGetChild(0) instanceof ASTExpression expression)) {
                throw new ScriptCompileException("ASTReturnStatement: ASTExpression expected.");
            }

            type = compile(expression, visitor);
        }

        if (!visitor.getReturnType().equals(type)) {
            UnaryOperation operation = ImplicitCast.get(type, visitor.getReturnType());
            if (operation == null) {
                throw new ScriptCompileException(String.format(
                        "Cannot cast %s to %s at return statement.",
                        type.toString(),
                        visitor.getReturnType().toString()));
            }

            operation.apply(visitor);
        }

        visitor.visitInsn(visitor.getReturnType().getReturnInst());
    }

    private void compile(ASTStatementExpressionList list, CompilerMethodVisitor visitor) throws ScriptCompileException {
        for (int i = 0; i < list.jjtGetNumChildren(); i++) {
            compile((ASTStatementExpression) list.jjtGetChild(i), visitor);
        }
    }

    private SType compile(ASTExpression expression, CompilerMethodVisitor visitor) throws ScriptCompileException {
        if (expression.jjtGetNumChildren() != 1) {
            throw new ScriptCompileException("ASTExpression invalid children count.");
        }

        Node node = expression.jjtGetChild(0);
        if (node instanceof ASTConditionalExpression conditionalExpression) {
            return compile(conditionalExpression, visitor);
        }

        if (node instanceof ASTLambdaExpression lambdaExpression) {
            return compile(lambdaExpression, visitor);
        }

        throw new ScriptCompileException("ASTExpression case not implemented: " + node.getClass().getName() + ".");
    }

    private SType compile(ASTLambdaExpression lambdaExpression, CompilerMethodVisitor visitor) throws ScriptCompileException {
        if (lambdaExpression.jjtGetNumChildren() != 1) {
            throw new ScriptCompileException("ASTLambdaExpression invalid children count.");
        }

        Node node = lambdaExpression.jjtGetChild(0);
        if (node instanceof ASTBlock block) {
            // this is static function, so initial index for local vars can be 0
            Class<Runnable> dynamic = compileRunnable((cw, v1, v2) -> {
                compile(block, v2);
            }, visitor.getContextStack().newWithStaticVariables(0));

            Constructor<Runnable> constructor;
            try {
                constructor = dynamic.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new ScriptCompileException("ASTLambdaExpression cannot find dynamic class constructor.");
            }

            visitor.visitTypeInsn(NEW, Type.getInternalName(dynamic));
            visitor.visitInsn(DUP);
            visitor.visitMethodInsn(
                    INVOKESPECIAL,
                    Type.getInternalName(dynamic),
                    "<init>",
                    Type.getConstructorDescriptor(constructor),
                    false);

            return SAction.instance;
        }

        throw new ScriptCompileException("ASTLambdaExpression: ASTBlock expected.");
    }

    private SType compile(ASTConditionalExpression conditionalExpression, CompilerMethodVisitor visitor) throws ScriptCompileException {
        if (conditionalExpression.jjtGetNumChildren() == 1) {
            Node node = conditionalExpression.jjtGetChild(0);
            if (node instanceof ASTConditionalOrExpression conditionalOrExpression) {
                return compileOrExpression(conditionalOrExpression, visitor);
            }
            throw new ScriptCompileException("ASTConditionalExpression case not implemented: " + node.getClass().getName() + ".");
        }

        if (conditionalExpression.jjtGetNumChildren() == 3) {
            var condition = (ASTConditionalOrExpression) conditionalExpression.jjtGetChild(0);
            var expression1 = (ASTExpression) conditionalExpression.jjtGetChild(1);
            var expression2 = (ASTConditionalExpression) conditionalExpression.jjtGetChild(2);

            SType conditionReturnType = compileOrExpression(condition, visitor);
            if (conditionReturnType != SBoolean.instance) {
                throw new ScriptCompileException("ASTConditionalExpression should return boolean.");
            }

            Label elseLabel = new Label();
            visitor.visitJumpInsn(IFEQ, elseLabel);
            SType expression1ReturnType = compile(expression1, visitor);
            Label endLabel = new Label();
            visitor.visitJumpInsn(GOTO, endLabel);
            visitor.visitLabel(elseLabel);
            SType expression2ReturnType = compile(expression2, visitor);
            visitor.visitLabel(endLabel);

            if (expression1ReturnType != expression2ReturnType) {
                throw new ScriptCompileException("ASTConditionalExpression return types don't match.");
            }

            return expression1ReturnType;
        }

        throw new ScriptCompileException("ASTConditionalExpression invalid children count.");
    }

    private SType compileOrExpression(
            ASTConditionalOrExpression conditionalOrExpression,
            CompilerMethodVisitor visitor
    ) throws ScriptCompileException {
        return binaryOperatorCompile(conditionalOrExpression, visitor, this::compileAndExpression);
    }

    private SType compileAndExpression(
            ASTConditionalAndExpression conditionalAndExpression,
            CompilerMethodVisitor visitor
    ) throws ScriptCompileException {
        return binaryOperatorCompile(conditionalAndExpression, visitor, this::compileEqualityExpression);
    }

    private SType compileEqualityExpression(
            ASTEqualityExpression equalityExpression,
            CompilerMethodVisitor visitor
    ) throws ScriptCompileException {
        return binaryOperatorCompile(equalityExpression, visitor, this::compileRelationalExpression);
    }

    private SType compileRelationalExpression(
            ASTRelationalExpression relationalExpression,
            CompilerMethodVisitor visitor
    ) throws ScriptCompileException {
        return binaryOperatorCompile(relationalExpression, visitor, this::compileAdditiveExpression);
    }

    private SType compileAdditiveExpression(
            ASTAdditiveExpression additiveExpression,
            CompilerMethodVisitor visitor
    ) throws ScriptCompileException {
        return binaryOperatorCompile(additiveExpression, visitor, this::compileMultiplicativeExpression);
    }

    private SType compileMultiplicativeExpression(
            ASTMultiplicativeExpression multiplicativeExpression,
            CompilerMethodVisitor visitor
    ) throws ScriptCompileException {
        return binaryOperatorCompile(multiplicativeExpression, visitor, this::compileUnaryExpression);
    }

    private SType compileUnaryExpression(ASTUnaryExpression unaryExpression, CompilerMethodVisitor visitor) throws ScriptCompileException {
        int numChildren = unaryExpression.jjtGetNumChildren();
        if (numChildren == 1) {
            return compileUnaryExpressionNotPlusMinus((ASTUnaryExpressionNotPlusMinus) unaryExpression.jjtGetChild(0), visitor);
        }
        if (numChildren == 2) {
            Node operator = unaryExpression.jjtGetChild(0);
            SType type = compileUnaryExpression((ASTUnaryExpression) unaryExpression.jjtGetChild(1), visitor);
            UnaryOperation operation = type.unary(operator);

            if (operation != null) {
                operation.apply(visitor);
                return operation.getType();
            } else {
                throw new ScriptCompileException(
                        String.format("ASTUnaryExpression: cannot process type %s. Operator: %s.",
                                type.getClass().getSimpleName(),
                                operator.getClass().getSimpleName()));
            }
        }
        throw new ScriptCompileException("ASTUnaryExpression invalid children count.");
    }

    private SType compileUnaryExpressionNotPlusMinus(ASTUnaryExpressionNotPlusMinus unaryExpressionNotPlusMinus, CompilerMethodVisitor visitor) throws ScriptCompileException {
        int numChildren = unaryExpressionNotPlusMinus.jjtGetNumChildren();
        if (numChildren == 1) {
            return compile((ASTPrimaryExpression) unaryExpressionNotPlusMinus.jjtGetChild(0), visitor);
        }
        if (numChildren == 2) {
            Node operator = unaryExpressionNotPlusMinus.jjtGetChild(0);
            SType type = compileUnaryExpression((ASTUnaryExpression) unaryExpressionNotPlusMinus.jjtGetChild(1), visitor);
            UnaryOperation operation = type.unary(operator);

            if (operation != null) {
                operation.apply(visitor);
                return operation.getType();
            } else {
                throw new ScriptCompileException(
                        String.format("ASTUnaryExpressionNotPlusMinus: cannot process type %s. Operator: %s.",
                                type.getClass().getSimpleName(),
                                operator.getClass().getSimpleName()));
            }
        }
        throw new ScriptCompileException("ASTUnaryExpressionNotPlusMinus invalid children count.");
    }

    private SType compile(ASTLiteral literal, CompilerMethodVisitor visitor) throws ScriptCompileException {
        if (literal.jjtGetNumChildren() != 1) {
            throw new ScriptCompileException("ASTLiteral invalid children count.");
        }

        Node node = literal.jjtGetChild(0);
        if (node instanceof ASTStringLiteral stringLiteral) {
            String value = parseString((String) stringLiteral.jjtGetValue());
            visitor.visitLdcInsn(value);
            return SStringType.instance;
        }
        if (node instanceof ASTBooleanLiteral booleanLiteral) {
            boolean value = (boolean) booleanLiteral.jjtGetValue();
            visitor.visitIntInsn(BIPUSH, value ? 1 : 0);
            return SBoolean.instance;
        }
        if (node instanceof ASTIntegerLiteral integerLiteral) {
            int value = Integer.parseInt((String) integerLiteral.jjtGetValue());
            visitor.visitLdcInsn(value);
            return SIntType.instance;
        }
        if (node instanceof ASTFloatingPointLiteral floatingPointLiteral) {
            double value = Double.parseDouble((String) floatingPointLiteral.jjtGetValue());
            visitor.visitLdcInsn(value);
            return SFloatType.instance;
        }
        if (node instanceof ASTNullLiteral) {
            throw new ScriptCompileException("Cannot use null.");
            //visitor.visitInsn(ACONST_NULL);
            //return ScriptingLanguageType.NULL;
        }

        throw new ScriptCompileException("ASTLiteral case not implemented: " + node.getClass().getName() + ".");
    }

    @SuppressWarnings("unchecked")
    private <T1 extends SimpleNode, T2 extends SimpleNode> SType binaryOperatorCompile(
            T1 expression,
            CompilerMethodVisitor visitor,
            CompileNodeFunction<T2> childCompile
    ) throws ScriptCompileException {
        T2 child = (T2) expression.jjtGetChild(0);
        SType typeLeft = childCompile.apply(child, visitor);

        int numChildren = expression.jjtGetNumChildren();
        if (numChildren == 1) {
            return typeLeft;
        }

        for (int i = 1; i < numChildren; i += 2) {
            Node operator = expression.jjtGetChild(i);
            BufferVisitor bufferVisitor = new BufferVisitor(visitor.getContextStack(), visitor.getClassName());
            SType typeRight = childCompile.apply((T2) expression.jjtGetChild(i + 1), bufferVisitor);

            BinaryOperation operation = typeLeft.binary(operator, typeRight);
            if (operation != null) {
                operation.apply(visitor, bufferVisitor);
                typeLeft = operation.getType();
            } else {
                throw new ScriptCompileException(
                        String.format("%s: cannot process types %s and %s. Operator: %s.",
                                expression.getClass().getSimpleName(),
                                typeLeft.getClass().getSimpleName(),
                                typeRight.getClass().getSimpleName(),
                                operator.getClass().getSimpleName()));
            }
        }
        return typeLeft;
    }

    private Method findMethod(Field field, String name, SType[] argumentTypes, BufferVisitor[] argumentVisitors, String className) throws ScriptCompileException {
        FindMethodResult methodResult = null;
        for (Method m : field.getType().getMethods()) {
            if (m.getDeclaringClass() == Object.class) {
                continue; // skip Object methods
            }
            if (!m.getName().equals(name)) {
                continue;
            }
            if (!visibilityChecker.isVisible(m)) {
                continue;
            }
            Parameter[] parameters = m.getParameters();
            if (parameters.length != argumentTypes.length) {
                continue;
            }

            BufferVisitor[] casts = new BufferVisitor[parameters.length];
            boolean ok = true;
            for (int i = 0; i < parameters.length; i++) {
                Class<?> methodParameterClass = parameters[i].getType();
                Class<?> scriptParameterClass = argumentTypes[i].getJavaClass();
                if (methodParameterClass == scriptParameterClass) {
                    continue;
                }

                UnaryOperation operation = ImplicitCast.get(
                        SType.fromJavaClass(scriptParameterClass),
                        SType.fromJavaClass(methodParameterClass));
                if (operation != null) {
                    casts[i] = new BufferVisitor(null, className);
                    operation.apply(casts[i]);
                    continue;
                }
                ok = false;
                break;
            }

            if (!ok) {
                continue;
            }

            FindMethodResult result = new FindMethodResult(
                    m,
                    (int)Arrays.stream(casts).filter(Objects::nonNull).count(),
                    casts);
            if (methodResult == null || methodResult.count > result.count) {
                methodResult = result;
            }
        }

        if (methodResult == null) {
            return null;
        }

        for (int i = 0; i < argumentVisitors.length; i++) {
            if (methodResult.casts[i] != null) {
                methodResult.casts[i].releaseBuffer(argumentVisitors[i]);
            }
        }

        return methodResult.method;
    }

    private String parseString(String value) throws ScriptCompileException {
        value = value.substring(1, value.length() - 1); // truncate brackets
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == '\\') {
                char next = value.charAt(i + 1);
                switch (next) {
                    case 'n': value = replace(value, i, 2, "\n"); break;
                    case 'r': value = replace(value, i, 2, "\r"); break;
                    case 't': value = replace(value, i, 2, "\t"); break;
                    case 'b': value = replace(value, i, 2, "\b"); break;
                    case 'f': value = replace(value, i, 2, "\f"); break;
                    case '\\': value = replace(value, i, 2, "\\"); break;
                    case '"': value = replace(value, i, 2, "\""); break;
                    case '\'': value = replace(value, i, 2, "'"); break;
                    default:
                        throw new ScriptCompileException("Cannot parse string literal.");// not implemented
                }
            }
        }
        return value;
    }

    private String replace(String value, int from, int length, String replacement) {
        return value.substring(0, from) + replacement + value.substring(from + length);
    }

    private SType parseType(ASTType typeNode) throws ScriptCompileException {
        SType type = parsePrimitiveType((ASTPrimitiveType) typeNode.jjtGetChild(0));

        for (int i = 1; i < typeNode.jjtGetNumChildren(); i += 2) {
            type = new SArrayType(type);
        }

        return type;
    }

    private SPrimitiveType parsePrimitiveType(ASTPrimitiveType primitiveType) throws ScriptCompileException {
        if (primitiveType.jjtGetChild(0) instanceof ASTBooleanType) {
            return SBoolean.instance;
        } else if (primitiveType.jjtGetChild(0) instanceof ASTIntType) {
            return SIntType.instance;
        }  else if (primitiveType.jjtGetChild(0) instanceof ASTFloatType) {
            return SFloatType.instance;
        } else if (primitiveType.jjtGetChild(0) instanceof ASTStringType) {
            return SStringType.instance;
        } else {
            throw new ScriptCompileException(String.format("Unexpected primitive type %s.", primitiveType.getClass().getSimpleName()));
        }
    }

    private <T extends Node> boolean isLastNode(Node node, Class<T> clazz) {
        if (clazz.isInstance(node)) {
            return true;
        } else {
            if (node.jjtGetNumChildren() == 0) {
                return false;
            }
            return isLastNode(node.jjtGetChild(node.jjtGetNumChildren() - 1), clazz);
        }
    }

    private void printTopStackInt(CompilerMethodVisitor visitor) throws ScriptCompileException {
        Method method;
        try {
            method = PrintStream.class.getDeclaredMethod("println", int.class);
        } catch (NoSuchMethodException e) {
            throw new ScriptCompileException("printTopStackInt cannot find PrintStream.println(int) method.");
        }

        Field field;
        try {
            field = System.class.getDeclaredField("out");
        }
        catch (NoSuchFieldException e) {
            throw new ScriptCompileException("printTopStackInt cannot find System.out field");
        }

        visitor.visitInsn(DUP);
        visitor.visitFieldInsn(
                GETSTATIC,
                Type.getInternalName(System.class),
                field.getName(),
                Type.getDescriptor(field.getType()));
        visitor.visitInsn(SWAP);
        visitor.visitMethodInsn(
                INVOKEVIRTUAL,
                Type.getInternalName(PrintStream.class),
                method.getName(),
                Type.getMethodDescriptor(method),
                false);
    }

    private record FindMethodResult(Method method, int count, BufferVisitor[] casts) {}

    private record FunctionParameter(SType type, String identifier) {}

    @FunctionalInterface
    private interface CompileNodeFunction<T1> {
        SType apply(T1 t1, CompilerMethodVisitor visitor) throws ScriptCompileException;
    }
}