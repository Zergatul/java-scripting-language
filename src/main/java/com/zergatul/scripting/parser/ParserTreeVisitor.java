package com.zergatul.scripting.parser;

import com.zergatul.scripting.parser.nodes.*;

public abstract class ParserTreeVisitor {

    public void explicitVisit(ArgumentsListNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(ArrayCreationExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(ArrayInitializerExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(ArrayTypeNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(AssignmentOperatorNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(AssignmentStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(AwaitExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BinaryExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BinaryOperatorNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BlockStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BooleanLiteralExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(BreakStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(CharLiteralExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(CollectionExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(CompilationUnitNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(CompilationUnitMembersListNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(ConditionalExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(ContinueStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(CustomTypeNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(EmptyStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(ExpressionStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(FloatLiteralExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(ForEachLoopStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(ForLoopStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(FunctionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(JavaTypeNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(IfStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(IndexExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(IntegerLiteralExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(Integer64LiteralExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(InvalidExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(InvalidMetaExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(InvalidStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(InvalidTypeNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(InvocationExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(LambdaExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(LetTypeNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(MemberAccessExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(MetaTypeExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(MetaTypeOfExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(NameExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(ObjectCreationExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(ParameterListNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(ParameterNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(PostfixStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(PredefinedTypeNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(RefArgumentExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(RefTypeNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(ReturnStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(StatementsListNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(StaticFieldNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(StaticReferenceNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(StringLiteralExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(TypeCastExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(TypeTestExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(UnaryExpressionNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(UnaryOperatorNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(VariableDeclarationNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(VoidTypeNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void explicitVisit(WhileLoopStatementNode node) {
        visit(node);
        node.acceptChildren(this);
    }

    public void visit(ArgumentsListNode node) {}
    public void visit(ArrayCreationExpressionNode node) {}
    public void visit(ArrayInitializerExpressionNode node) {}
    public void visit(ArrayTypeNode node) {}
    public void visit(AssignmentOperatorNode node) {}
    public void visit(AssignmentStatementNode node) {}
    public void visit(AwaitExpressionNode node) {}
    public void visit(BinaryExpressionNode node) {}
    public void visit(BinaryOperatorNode node) {}
    public void visit(BlockStatementNode node) {}
    public void visit(BooleanLiteralExpressionNode node) {}
    public void visit(BreakStatementNode node) {}
    public void visit(CharLiteralExpressionNode node) {}
    public void visit(CollectionExpressionNode node) {}
    public void visit(CompilationUnitNode node) {}
    public void visit(CompilationUnitMembersListNode node) {}
    public void visit(ConditionalExpressionNode node) {}
    public void visit(ContinueStatementNode node) {}
    public void visit(CustomTypeNode node) {}
    public void visit(EmptyStatementNode node) {}
    public void visit(ExpressionStatementNode node) {}
    public void visit(FloatLiteralExpressionNode node) {}
    public void visit(ForEachLoopStatementNode node) {}
    public void visit(ForLoopStatementNode node) {}
    public void visit(FunctionNode node) {}
    public void visit(JavaTypeNode node) {}
    public void visit(IfStatementNode node) {}
    public void visit(IndexExpressionNode node) {}
    public void visit(IntegerLiteralExpressionNode node) {}
    public void visit(Integer64LiteralExpressionNode node) {}
    public void visit(InvalidMetaExpressionNode node) {}
    public void visit(InvalidExpressionNode node) {}
    public void visit(InvalidStatementNode node) {}
    public void visit(InvalidTypeNode node) {}
    public void visit(InvocationExpressionNode node) {}
    public void visit(LambdaExpressionNode node) {}
    public void visit(LetTypeNode node) {}
    public void visit(MemberAccessExpressionNode node) {}
    public void visit(MetaTypeExpressionNode node) {}
    public void visit(MetaTypeOfExpressionNode node) {}
    public void visit(ObjectCreationExpressionNode node) {}
    public void visit(NameExpressionNode node) {}
    public void visit(ParameterListNode node) {}
    public void visit(ParameterNode node) {}
    public void visit(PostfixStatementNode node) {}
    public void visit(PredefinedTypeNode node) {}
    public void visit(RefArgumentExpressionNode node) {}
    public void visit(RefTypeNode node) {}
    public void visit(ReturnStatementNode node) {}
    public void visit(StatementsListNode node) {}
    public void visit(StaticFieldNode node) {}
    public void visit(StaticReferenceNode node) {}
    public void visit(StringLiteralExpressionNode node) {}
    public void visit(TypeCastExpressionNode node) {}
    public void visit(TypeTestExpressionNode node) {}
    public void visit(UnaryExpressionNode node) {}
    public void visit(UnaryOperatorNode node) {}
    public void visit(VariableDeclarationNode node) {}
    public void visit(VoidTypeNode node) {}
    public void visit(WhileLoopStatementNode node) {}
}