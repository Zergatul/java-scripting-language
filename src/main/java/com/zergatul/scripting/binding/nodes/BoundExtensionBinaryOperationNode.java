package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ClassOperatorOverloadNode;
import com.zergatul.scripting.symbols.LiftedVariable;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.UnaryOperation;

import java.util.List;

public class BoundExtensionBinaryOperationNode extends BoundExtensionOperatorOverloadNode {

    public final BinaryOperation operation;

    public BoundExtensionBinaryOperationNode(
            ClassOperatorOverloadNode node,
            BinaryOperation operation,
            BoundTypeNode returnTypeNode,
            BoundParameterListNode parameters,
            BoundStatementNode body,
            List<LiftedVariable> lifted
    ) {
        super(BoundNodeType.EXTENSION_BINARY_OPERATION, node, returnTypeNode, parameters, body, lifted);
        this.operation = operation;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }
}