package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import com.zergatul.scripting.type.MemberModifiers;
import com.zergatul.scripting.type.Visibility;

import java.util.List;

public class ModifiersNode extends ParserNode {

    public final List<Token> tokens;

    public ModifiersNode(List<Token> tokens, TextRange range) {
        super(ParserNodeType.MODIFIERS, range);
        this.tokens = tokens;
    }

    public boolean isAsync() {
        return tokens.stream().anyMatch(t -> t.is(TokenType.ASYNC));
    }

    public boolean isAbstract() {
        return tokens.stream().anyMatch(t -> t.is(TokenType.ABSTRACT));
    }

    public boolean isOverride() {
        return tokens.stream().anyMatch(t -> t.is(TokenType.OVERRIDE));
    }

    public boolean isVirtual() {
        return tokens.stream().anyMatch(t -> t.is(TokenType.VIRTUAL));
    }

    public boolean isPublic() {
        return tokens.stream().anyMatch(t -> t.is(TokenType.PUBLIC));
    }

    public boolean isProtected() {
        return tokens.stream().anyMatch(t -> t.is(TokenType.PROTECTED));
    }

    public boolean isPrivate() {
        return tokens.stream().anyMatch(t -> t.is(TokenType.PRIVATE));
    }

    public boolean hasVisibilityModifier() {
        return isPublic() || isProtected() || isPrivate();
    }

    public Visibility getVisibility() {
        if (isProtected()) {
            return Visibility.PROTECTED;
        }
        if (isPrivate()) {
            return Visibility.PRIVATE;
        }
        return Visibility.PUBLIC;
    }

    public boolean isFinal() {
        return !isAbstract() && !isOverride() && !isVirtual();
    }

    public boolean hasMethodModifiers() {
        for (Token token : tokens) {
            switch (token.getTokenType()) {
                case ASYNC, ABSTRACT, OVERRIDE, VIRTUAL: return true;
            }
        }
        return false;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    @Override
    public List<Locatable> getChildNodes() {
        return List.copyOf(tokens);
    }

    public MemberModifiers toMemberModifiers() {
        return new MemberModifiers(isAsync(), isAbstract(), isVirtual(), isOverride(), false, getVisibility());
    }
}