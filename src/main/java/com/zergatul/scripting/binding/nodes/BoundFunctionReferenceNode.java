package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.NameExpressionNode;
import com.zergatul.scripting.symbols.Function;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.type.SStaticFunction;

import java.util.List;

public class BoundFunctionReferenceNode extends BoundExpressionNode {

    public final NameExpressionNode syntaxNode;
    public final String name;
    public final SymbolRef symbolRef;

    public BoundFunctionReferenceNode(NameExpressionNode node, SymbolRef symbolRef) {
        this(node, node.value, symbolRef, node.getRange());
    }

    public BoundFunctionReferenceNode(NameExpressionNode node, String name, SymbolRef symbolRef, TextRange range) {
        super(BoundNodeType.FUNCTION_REFERENCE, symbolRef.asFunction().getFunctionType(), range);
        this.syntaxNode = node;
        this.name = name;
        this.symbolRef = symbolRef;
    }

    public Function getFunction() {
        return symbolRef.asFunction();
    }

    public SStaticFunction getFunctionType() {
        return symbolRef.asFunction().getFunctionType();
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}