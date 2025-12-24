package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.parser.nodes.ClassOperatorOverloadNode;
import com.zergatul.scripting.symbols.LiftedVariable;
import com.zergatul.scripting.type.ExtensionUnaryOperation;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.SMethodFunction;
import com.zergatul.scripting.type.operation.UnaryOperation;

import java.util.List;

public class BoundExtensionUnaryOperationNode extends BoundExtensionOperatorOverloadNode {

    public final UnaryOperation operation;

    public BoundExtensionUnaryOperationNode(
            ClassOperatorOverloadNode node,
            UnaryOperation operation,
            BoundTypeNode returnTypeNode,
            BoundParameterListNode parameters,
            BoundStatementNode body,
            List<LiftedVariable> lifted
    ) {
        super(BoundNodeType.EXTENSION_UNARY_OPERATION, node, returnTypeNode, parameters, body, lifted);
        this.operation = operation;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }
}