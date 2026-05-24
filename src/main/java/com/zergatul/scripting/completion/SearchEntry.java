package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.nodes.*;

public class SearchEntry {

    public final SearchEntry parent;
    public final BoundNode node;

    public SearchEntry(SearchEntry parent, BoundNode node) {
        this.parent = parent;
        this.node = node;
    }

    public boolean isSingleWordStatementStart(int line, int column) {
        return switch (node.getNodeType()) {

            case NAME_EXPRESSION -> {
                if (parent.node.getNodeType() == BoundNodeType.EXPRESSION_STATEMENT) {
                    if (node.getRange().containsOrEnds(line, column)) {
                        // check if we are inside simple statement after arrow
                        boolean isSimpleStatement = switch (parent.parent.node.getNodeType()) {
                            case CLASS_CONSTRUCTOR -> ((BoundClassConstructorNode) parent.parent.node).syntaxNode.arrow != null;
                            case CLASS_METHOD -> ((BoundClassMethodNode) parent.parent.node).syntaxNode.arrow != null;
                            case EXTENSION_METHOD -> ((BoundExtensionMethodNode) parent.parent.node).syntaxNode.arrow != null;
                            case LAMBDA_EXPRESSION -> true;
                            default -> false;
                        };
                        yield !isSimpleStatement;
                    }
                }
                yield false;
            }

            case CUSTOM_TYPE, DECLARED_CLASS_TYPE, ALIASED_TYPE, LET_TYPE, INVALID_TYPE -> {
                yield parent.node.getNodeType() == BoundNodeType.VARIABLE_DECLARATION;
            }

            case PREDEFINED_TYPE -> {
                if (parent.node.getNodeType() == BoundNodeType.VARIABLE_DECLARATION) {
                    BoundVariableDeclarationNode declaration = (BoundVariableDeclarationNode) parent.node;
                    yield declaration.name.value.isEmpty() && declaration.expression == null;
                }
                yield parent.node.getNodeType() == BoundNodeType.STATIC_REFERENCE;
            }

            case STATIC_REFERENCE -> {
                BoundStaticReferenceExpression staticReference = (BoundStaticReferenceExpression) node;
                yield new SearchEntry(this, staticReference.typeNode).isSingleWordStatementStart(line, column);
            }

            case IF_STATEMENT -> {
                BoundIfStatementNode statement = (BoundIfStatementNode) node;
                yield   statement.syntaxNode.openParen.getRange().isEmpty() &&
                        statement.condition.getRange().isEmpty() &&
                        statement.syntaxNode.closeParen.getRange().isEmpty() &&
                        statement.thenStatement.getNodeType() == BoundNodeType.INVALID_STATEMENT &&
                        statement.elseStatement == null;
            }

            case FOR_LOOP_STATEMENT -> {
                BoundForLoopStatementNode statement = (BoundForLoopStatementNode) node;
                yield   statement.syntaxNode.openParen.getRange().isEmpty() &&
                        statement.init != null && statement.init.getNodeType() == BoundNodeType.INVALID_STATEMENT &&
                        statement.condition != null && statement.condition.getRange().isEmpty() &&
                        statement.update != null && statement.update.getNodeType() == BoundNodeType.INVALID_STATEMENT &&
                        statement.syntaxNode.closeParen.getRange().isEmpty() &&
                        statement.body.getNodeType() == BoundNodeType.INVALID_STATEMENT;
            }

            case FOREACH_LOOP_STATEMENT -> {
                BoundForEachLoopStatementNode statement = (BoundForEachLoopStatementNode) node;
                yield   statement.syntaxNode.openParen.getRange().isEmpty() &&
                        statement.typeNode.getNodeType() == BoundNodeType.INVALID_TYPE &&
                        statement.name.value.isEmpty() &&
                        statement.iterable.getNodeType() == BoundNodeType.INVALID_EXPRESSION &&
                        statement.syntaxNode.closeParen.getRange().isEmpty() &&
                        statement.body.getNodeType() == BoundNodeType.INVALID_STATEMENT;
            }

            case WHILE_LOOP_STATEMENT -> {
                BoundWhileLoopStatementNode statement = (BoundWhileLoopStatementNode) node;
                yield   statement.condition.getRange().isEmpty() &&
                        statement.body.getNodeType() == BoundNodeType.INVALID_STATEMENT;
            }

            case RETURN_STATEMENT -> {
                BoundReturnStatementNode statement = (BoundReturnStatementNode) node;
                yield   statement.syntaxNode.keyword.getRange().containsOrEnds(line, column) &&
                        (statement.expression == null || statement.expression.getNodeType() == BoundNodeType.INVALID_EXPRESSION);
            }

            default -> false;
        };
    }
}