package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import com.zergatul.scripting.parser.PredefinedType;

import java.util.List;

public class PredefinedTypeNode extends TypeNode {

    public final Token token;
    public final PredefinedType type;

    public PredefinedTypeNode(Token token, PredefinedType type) {
        super(ParserNodeType.PREDEFINED_TYPE, token.getRange());
        this.token = token;
        this.type = type;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(token);
    }
}