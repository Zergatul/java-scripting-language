package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.parser.nodes.ClassOperatorOverloadNode;
import com.zergatul.scripting.symbols.LiftedVariable;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.SMethodFunction;

import java.util.List;

public class BoundClassUnaryOperationNode extends BoundClassOperatorOverloadNode {

    public final UnaryOperator operator;

    public BoundClassUnaryOperationNode(
            ClassOperatorOverloadNode node,
            SMethodFunction functionType,
            MethodReference method,
            UnaryOperator operator,
            BoundTypeNode returnTypeNode,
            BoundParameterListNode parameters,
            BoundStatementNode body,
            List<LiftedVariable> lifted
    ) {
        super(BoundNodeType.CLASS_UNARY_OPERATION, node, functionType, method, returnTypeNode, parameters, body, lifted);
        this.operator = operator;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }
}