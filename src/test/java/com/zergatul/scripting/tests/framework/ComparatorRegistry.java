package com.zergatul.scripting.tests.framework;

import com.zergatul.scripting.*;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.highlighting.SemanticToken;
import com.zergatul.scripting.lexer.*;
import com.zergatul.scripting.parser.nodes.*;
import com.zergatul.scripting.type.NativeInstanceMethodReference;
import com.zergatul.scripting.type.NativeMethodReference;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ComparatorRegistry {

    private final Map<Class<?>, List<LabeledExtractor<?, ?>>> map = new HashMap<>();

    public static ComparatorRegistry create() {
        return new ComparatorRegistry()
                .register(SingleLineTextRange.class, builder -> builder
                        .extract("line", SingleLineTextRange::getLine1)
                        .extract("column", SingleLineTextRange::getColumn1)
                        .extract("position", SingleLineTextRange::getPosition)
                        .extract("length", SingleLineTextRange::getLength))
                .register(MultiLineTextRange.class, builder -> builder
                        .extract("line1", MultiLineTextRange::getLine1)
                        .extract("column1", MultiLineTextRange::getColumn1)
                        .extract("line2", MultiLineTextRange::getLine2)
                        .extract("column2", MultiLineTextRange::getColumn2)
                        .extract("position", MultiLineTextRange::getPosition)
                        .extract("length", MultiLineTextRange::getLength))
                .register(Token.class, builder -> builder
                        .extract("tokenType", Token::getTokenType)
                        .extract("leadingTrivia", Token::getLeadingTrivia)
                        .extract("trailingTrivia", Token::getTrailingTrivia)
                        .extract("range", Token::getRange))
                .register(Trivia.class, builder -> {})
                .register(ValueToken.class, builder -> builder
                        .extract("value", node -> node.value))
                .register(InvalidNumberToken.class, builder -> builder
                        .extract("value", node -> node.value))
                .register(EndOfFileToken.class, builder -> {})
                .register(DiagnosticMessage.class, builder -> builder
                        .extract("level", node -> node.level)
                        .extract("message", node -> node.message)
                        .extract("code", node -> node.code)
                        .extract("range", node -> node.range))
                .register(SemanticToken.class, builder -> builder
                        .extract("type", SemanticToken::type)
                        .extract("range", SemanticToken::range))
                /* Parser Nodes */
                .register(ArgumentsListNode.class, builder -> builder
                        .extract("openParen", node -> node.openParen)
                        .extract("arguments", node -> node.arguments)
                        .extract("closeParen", node -> node.closeParen))
                .register(ArrayCreationExpressionNode.class, builder -> builder
                        .extract("keyword", node -> node.keyword)
                        .extract("typeNode", node -> node.typeNode)
                        .extract("openBracket", node -> node.openBracket)
                        .extract("lengthExpression", node -> node.lengthExpression)
                        .extract("closeBracket", node -> node.closeBracket))
                .register(ArrayInitializerExpressionNode.class, builder -> builder
                        .extract("keyword", node -> node.keyword)
                        .extract("typeNode", node -> node.typeNode)
                        .extract("openBrace", node -> node.openBrace)
                        .extract("list", node -> node.list)
                        .extract("closeBrace", node -> node.closeBrace))
                .register(ArrayTypeNode.class, builder -> builder
                        .extract("underlying", node -> node.underlying)
                        .extract("openBracket", node -> node.openBracket)
                        .extract("closeBracket", node -> node.closeBracket))
                .register(AssignmentOperatorNode.class, builder -> builder
                        .extract("token", node -> node.token)
                        .extract("operator", node -> node.operator))
                .register(AssignmentStatementNode.class, builder -> builder
                        .extract("left", node -> node.left)
                        .extract("operator", node -> node.operator)
                        .extract("right", node -> node.right)
                        .extract("semicolon", node -> node.semicolon))
                .register(BinaryExpressionNode.class, builder -> builder
                        .extract("left", node -> node.left)
                        .extract("operator", node -> node.operator)
                        .extract("right", node -> node.right))
                .register(BinaryOperatorNode.class, builder -> builder
                        .extract("token", node -> node.token)
                        .extract("operator", node -> node.operator))
                .register(BlockStatementNode.class, builder -> builder
                        .extract("openBrace", node -> node.openBrace)
                        .extract("statements", node -> node.statements)
                        .extract("closeBrace", node -> node.closeBrace))
                .register(BooleanLiteralExpressionNode.class, builder -> builder
                        .extract("token", node -> node.token)
                        .extract("value", node -> node.value))
                .register(ClassMethodNode.class, builder -> builder
                        .extract("modifiers", node -> node.modifiers)
                        .extract("type", node -> node.type)
                        .extract("name", node -> node.name)
                        .extract("parameters", node -> node.parameters)
                        .extract("arrow", node -> node.arrow)
                        .extract("body", node -> node.body))
                .register(ClassNode.class, builder -> builder
                        .extract("keyword", node -> node.keyword)
                        .extract("name", node -> node.name)
                        .extract("openBrace", node -> node.openBrace)
                        .extract("members", node -> node.members)
                        .extract("closeBrace", node -> node.closeBrace))
                .register(CompilationUnitMembersListNode.class, builder -> builder
                        .extract("members", node -> node.members))
                .register(CompilationUnitNode.class, builder -> builder
                        .extract("members", node -> node.members)
                        .extract("statements", node -> node.statements)
                        .extract("end", node -> node.end))
                .register(CustomTypeNode.class, builder -> builder
                        .extract("token", node -> node.token)
                        .extract("value", node -> node.value))
                .register(ExpressionStatementNode.class, builder -> builder
                        .extract("expression", node -> node.expression)
                        .extract("semicolon", node -> node.semicolon))
                .register(ForEachLoopStatementNode.class, builder -> builder
                        .extract("keyword", node -> node.keyword)
                        .extract("openParen", node -> node.openParen)
                        .extract("typeNode", node -> node.typeNode)
                        .extract("name", node -> node.name)
                        .extract("iterable", node -> node.iterable)
                        .extract("closeParen", node -> node.closeParen)
                        .extract("body", node -> node.body))
                .register(ForLoopStatementNode.class, builder -> builder
                        .extract("keyword", node -> node.keyword)
                        .extract("openParen", node -> node.openParen)
                        .extract("init", node -> node.init)
                        .extract("semicolon1", node -> node.semicolon1)
                        .extract("condition", node -> node.condition)
                        .extract("semicolon2", node -> node.semicolon2)
                        .extract("update", node -> node.update)
                        .extract("closeParen", node -> node.closeParen)
                        .extract("body", node -> node.body))
                .register(FunctionNode.class, builder -> builder
                        .extract("modifiers", node -> node.modifiers)
                        .extract("returnType", node -> node.returnType)
                        .extract("name", node -> node.name)
                        .extract("parameters", node -> node.parameters)
                        .extract("arrow", node -> node.arrow)
                        .extract("body", node -> node.body))
                .register(IfStatementNode.class, builder -> builder
                        .extract("ifToken", node -> node.ifToken)
                        .extract("openParen", node -> node.openParen)
                        .extract("condition", node -> node.condition)
                        .extract("closeParen", node -> node.closeParen)
                        .extract("thenStatement", node -> node.thenStatement)
                        .extract("elseToken", node -> node.elseToken)
                        .extract("elseStatement", node -> node.elseStatement))
                .register(IntegerLiteralExpressionNode.class, builder -> builder
                        .extract("sign", node -> node.sign)
                        .extract("token", node -> node.token)
                        .extract("value", node -> node.value))
                .register(InvalidExpressionNode.class, builder -> builder
                        .extract("nodes", node -> node.nodes))
                .register(InvalidStatementNode.class, builder -> {})
                .register(InvalidTypeNode.class, builder -> builder
                        .extract("token", node -> node.token))
                .register(InvocationExpressionNode.class, builder -> builder
                        .extract("callee", node -> node.callee)
                        .extract("arguments", node -> node.arguments))
                .register(JavaQualifiedTypeNameNode.class, builder -> builder
                        .extract("tokens", node -> node.tokens)
                        .extract("value", node -> node.value))
                .register(JavaTypeNode.class, builder -> builder
                        .extract("java", node -> node.java)
                        .extract("openBracket", node -> node.openBracket)
                        .extract("name", node -> node.name)
                        .extract("closeBracket", node -> node.closeBracket))
                .register(LambdaExpressionNode.class, builder -> builder
                        .extract("openParen", node -> node.openParen)
                        .extract("parameters", node -> node.parameters)
                        .extract("closeParen", node -> node.closeParen)
                        .extract("arrow", node -> node.arrow)
                        .extract("body", node -> node.body))
                .register(LetTypeNode.class, builder -> builder
                        .extract("token", node -> node.token))
                .register(MemberAccessExpressionNode.class, builder -> builder
                        .extract("callee", node -> node.callee)
                        .extract("dot", node -> node.dot)
                        .extract("name", node -> node.name))
                .register(MetaTypeExpressionNode.class, builder -> builder
                        .extract("keyword", node -> node.keyword)
                        .extract("openParen", node -> node.openParen)
                        .extract("type", node -> node.type)
                        .extract("closeParen", node -> node.closeParen))
                .register(MetaTypeOfExpressionNode.class, builder -> builder
                        .extract("keyword", node -> node.keyword)
                        .extract("openParen", node -> node.openParen)
                        .extract("expression", node -> node.expression)
                        .extract("closeParen", node -> node.closeParen))
                .register(ModifiersNode.class, builder -> builder
                        .extract("tokens", node -> node.tokens))
                .register(NameExpressionNode.class, builder -> builder
                        .extract("token", node -> node.token)
                        .extract("value", node -> node.value))
                .register(ObjectCreationExpressionNode.class, builder -> builder
                        .extract("keyword", node -> node.keyword)
                        .extract("typeNode", node -> node.typeNode)
                        .extract("arguments", node -> node.arguments))
                .register(ParameterNode.class, builder -> builder
                        .extract("type", ParameterNode::getType)
                        .extract("name", ParameterNode::getName))
                .register(ParameterListNode.class, builder -> builder
                        .extract("openParen", node -> node.openParen)
                        .extract("parameters", node -> node.parameters)
                        .extract("closeParen", node -> node.closeParen))
                .register(ParserNode.class, builder -> builder
                        .extract("nodeType", ParserNode::getNodeType)
                        .extract("range", ParserNode::getRange))
                .register(PredefinedTypeNode.class, builder -> builder
                        .extract("token", node -> node.token)
                        .extract("type", node -> node.type))
                .register(ReturnStatementNode.class, builder -> builder
                        .extract("keyword", node -> node.keyword)
                        .extract("expression", node -> node.expression)
                        .extract("semicolon", node -> node.semicolon))
                .register(SeparatedList.class, builder -> builder
                        .extract("nodes", SeparatedList::getNodes)
                        .extract("commas", SeparatedList::getCommas))
                .register(StatementsListNode.class, builder -> builder
                        .extract("statements", node -> node.statements))
                .register(TypeTestExpressionNode.class, builder -> builder
                        .extract("expression", node -> node.expression)
                        .extract("keyword", node -> node.keyword)
                        .extract("type", node -> node.type))
                .register(UnaryExpressionNode.class, builder -> builder
                        .extract("operator", node -> node.operator)
                        .extract("operand", node -> node.operand))
                .register(UnaryOperatorNode.class, builder -> builder
                        .extract("token", node -> node.token)
                        .extract("operator", node -> node.operator))
                .register(VariableDeclarationNode.class, builder -> builder
                        .extract("type", node -> node.type)
                        .extract("name", node -> node.name)
                        .extract("expression", node -> node.expression)
                        .extract("semicolon", node -> node.semicolon))
                .register(VoidTypeNode.class, builder -> builder
                        .extract("token", node -> node.token))
                .register(WhileLoopStatementNode.class, builder -> builder
                        .extract("keyword", node -> node.keyword)
                        .extract("openParen", node -> node.openParen)
                        .extract("condition", node -> node.condition)
                        .extract("closeParen", node -> node.closeParen)
                        .extract("body", node -> node.body))
                /* Binder Nodes */
                .register(BoundArgumentsListNode.class, builder -> builder
                        .extract("syntaxNode", node -> node.syntaxNode)
                        .extract("arguments", node -> node.arguments))
                .register(BoundBlockStatementNode.class, builder -> builder
                        .extract("syntaxNode", node -> node.syntaxNode)
                        .extract("statements", node -> node.statements))
                .register(BoundCompilationUnitMembersListNode.class, builder -> builder
                        .extract("members", node -> node.members))
                .register(BoundCompilationUnitNode.class, builder -> builder
                        .extract("", node -> node.syntaxNode)
                        .extract("members", node -> node.members)
                        .extract("statements", node -> node.statements))
                .register(BoundExpressionNode.class, builder -> builder
                        .extract("type", node -> node.type))
                .register(BoundExpressionStatementNode.class, builder -> builder
                        .extract("expression", node -> node.expression))
                .register(BoundFunctionNode.class, builder -> builder
                        .extract("syntaxNode", node -> node.syntaxNode)
                        .extract("returnType", node -> node.returnType)
                        .extract("name", node -> node.name)
                        .extract("parameters", node -> node.parameters)
                        .extract("body", node -> node.body)
                        .extract("lifted", node -> node.lifted))
                .register(BoundInvalidExpressionNode.class, builder -> builder
                        .extract("children", node -> node.children))
                .register(BoundInvalidStatementNode.class, builder -> {})
                .register(BoundLambdaExpressionNode.class, builder -> builder
                        .extract("syntaxNode", node -> node.syntaxNode)
                        .extract("parameters", node -> node.parameters)
                        .extract("body", node -> node.body))
                .register(BoundMethodInvocationExpressionNode.class, builder -> builder
                        .extract("syntaxNode", node -> node.syntaxNode)
                        .extract("objectReference", node -> node.objectReference)
                        .extract("method", node -> node.method)
                        .extract("arguments", node -> node.arguments))
                .register(BoundMethodNode.class, builder -> builder
                        .extract("method", node -> node.method))
                .register(BoundNameExpressionNode.class, builder -> builder
                        .extract("value", node -> node.value)
                        .extract("symbolRef", node -> node.symbolRef))
                .register(BoundNode.class, builder -> builder
                        .extract("nodeType", BoundNode::getNodeType)
                        .extract("range", BoundNode::getRange))
                .register(BoundParameterListNode.class, builder -> builder
                        .extract("syntaxNode", node -> node.syntaxNode)
                        .extract("parameters", node -> node.parameters))
                .register(BoundParameterNode.class, builder -> builder
                        .extract("name", BoundParameterNode::getName)
                        .extract("typeNode", BoundParameterNode::getTypeNode)
                        .extract("type", BoundParameterNode::getType))
                .register(BoundPredefinedTypeNode.class, builder -> {})
                .register(BoundReturnStatementNode.class, builder -> builder
                        .extract("syntaxNode", node -> node.syntaxNode)
                        .extract("expression", node -> node.expression))
                .register(BoundStatementsListNode.class, builder -> builder
                        .extract("statements", node -> node.statements))
                .register(BoundTypeNode.class, builder -> builder
                        .extract("type", node -> node.type))
                /* */
                .register(NativeInstanceMethodReference.class, builder -> {})
                .register(NativeMethodReference.class, builder -> builder
                        .extract("method", NativeMethodReference::getUnderlying));
    }

    private <T> ComparatorRegistry register(Class<T> type, Consumer<Builder<T>> consumer) {
        Builder<T> builder = new Builder<T>(this, type);
        consumer.accept(builder);
        builder.register();
        return this;
    }

    /** Returns extractors for the class plus its supertypes (most-general first). */
    @SuppressWarnings("unchecked")
    <T> List<LabeledExtractor<T, ?>> chainFor(Class<? extends T> concrete) {
        if (!map.containsKey(concrete)) {
            throw new InternalException(String.format("Class %s not supported", concrete.getName()));
        }

        List<LabeledExtractor<T, ?>> result = new ArrayList<>();
        // Add in top-down order: Object -> ... -> concrete
        List<Class<?>> lineage = new ArrayList<>();
        Class<?> c = concrete;
        while (c != null) { lineage.addFirst(c); c = c.getSuperclass(); }
        for (Class<?> k : lineage) {
            List<LabeledExtractor<?, ?>> xs = map.get(k);
            if (xs != null) {
                for (LabeledExtractor<?, ?> x : xs) {
                    result.add((LabeledExtractor<T, ?>) x);
                }
            }
        }
        return result;
    }

    private static final class Builder<T> {

        private final ComparatorRegistry registry;
        private final Class<T> type;
        private final List<LabeledExtractor<T, ?>> local = new ArrayList<>();

        private Builder(ComparatorRegistry registry, Class<T> type) {
            this.registry = registry;
            this.type = type;
        }

        public <R> Builder<T> extract(String label, Function<T, @Nullable R> fn) {
            local.add(LabeledExtractor.of(label, fn));
            return this;
        }

        public void register() {
            registry.map.computeIfAbsent(type, k -> new ArrayList<>()).addAll(local);
        }
    }
}