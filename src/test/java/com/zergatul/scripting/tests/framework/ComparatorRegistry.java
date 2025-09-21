package com.zergatul.scripting.tests.framework;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.Node;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.Trivia;
import com.zergatul.scripting.parser.nodes.*;

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
                .register(Token.class, builder -> builder
                        .extract("tokenType", Token::getTokenType)
                        .extract("leadingTrivia", Token::getLeadingTrivia)
                        .extract("trailingTrivia", Token::getTrailingTrivia)
                        .extract("range", Token::getRange))
                .register(Trivia.class, builder -> {})
                .register(Node.class, builder -> builder
                        .extract("nodeType", Node::getNodeType)
                        .extract("range", Node::getRange))
                /* Parser Nodes */
                .register(ArgumentsListNode.class, builder -> builder
                        .extract("arguments", node -> node.arguments))
                .register(ArrayTypeNode.class, builder -> builder
                        .extract("underlying", node -> node.underlying))
                .register(AssignmentOperatorNode.class, builder -> builder
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
                        .extract("operator", node -> node.operator))
                .register(BlockStatementNode.class, builder -> builder
                        .extract("statements", node -> node.statements))
                .register(BooleanLiteralExpressionNode.class, builder -> builder
                        .extract("value", node -> node.value))
                .register(CompilationUnitMembersListNode.class, builder -> builder
                        .extract("members", node -> node.members))
                .register(CompilationUnitNode.class, builder -> builder
                        .extract("members", node -> node.members)
                        .extract("statements", node -> node.statements))
                .register(ExpressionStatementNode.class, builder -> builder
                        .extract("expression", node -> node.expression))
                .register(FunctionNode.class, builder -> builder
                        .extract("modifiers", node -> node.modifiers)
                        .extract("returnType", node -> node.returnType)
                        .extract("name", node -> node.name)
                        .extract("parameters", node -> node.parameters)
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
                        .extract("value", node -> node.value))
                .register(InvocationExpressionNode.class, builder -> builder
                        .extract("callee", node -> node.callee)
                        .extract("arguments", node -> node.arguments))
                .register(LambdaExpressionNode.class, builder -> builder
                        .extract("parameters", node -> node.parameters)
                        .extract("arrow", node -> node.arrow)
                        .extract("body", node -> node.body))
                .register(MemberAccessExpressionNode.class, builder -> builder
                        .extract("callee", node -> node.callee)
                        .extract("dot", node -> node.dot)
                        .extract("name", node -> node.name))
                .register(ModifiersNode.class, builder -> builder
                        .extract("tokens", node -> node.tokens))
                .register(NameExpressionNode.class, builder -> builder
                        .extract("value", node -> node.value))
                .register(ParameterNode.class, builder -> builder
                        .extract("type", ParameterNode::getType)
                        .extract("name", ParameterNode::getName))
                .register(ParameterListNode.class, builder -> builder
                        .extract("openParen", node -> node.openParen)
                        .extract("parameters", node -> node.parameters)
                        .extract("closeParen", node -> node.closeParen))
                .register(PredefinedTypeNode.class, builder -> builder
                        .extract("type", node -> node.type))
                .register(ReturnStatementNode.class, builder -> builder
                        .extract("keyword", node -> node.keyword)
                        .extract("expression", node -> node.expression))
                .register(StatementsListNode.class, builder -> builder
                        .extract("statements", node -> node.statements))
                .register(UnaryExpressionNode.class, builder -> builder
                        .extract("operator", node -> node.operator)
                        .extract("operand", node -> node.operand))
                .register(UnaryOperatorNode.class, builder -> builder
                        .extract("operator", node -> node.operator))
                .register(VariableDeclarationNode.class, builder -> builder
                        .extract("type", node -> node.type)
                        .extract("name", node -> node.name)
                        .extract("expression", node -> node.expression)
                        .extract("semicolon", node -> node.semicolon))
                .register(VoidTypeNode.class, builder -> {});
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
        while (c != null) { lineage.add(0, c); c = c.getSuperclass(); }
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

        public <R> Builder<T> extract(String label, Function<T, R> fn) {
            local.add(LabeledExtractor.of(label, fn));
            return this;
        }

        /** You can also pass pre-labeled extractors if you want to reuse them. */
        public Builder<T> extract(LabeledExtractor<T, ?> extractor) {
            local.add(extractor);
            return this;
        }

        public ComparatorRegistry register() {
            registry.map.computeIfAbsent(type, k -> new ArrayList<>()).addAll(local);
            return registry;
        }
    }
}