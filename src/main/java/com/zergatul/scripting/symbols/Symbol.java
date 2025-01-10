package com.zergatul.scripting.symbols;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.nodes.BoundNameExpressionNode;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.type.SType;
import org.objectweb.asm.MethodVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Symbol {

    private final String name;
    private final SType type;
    private final List<BoundNameExpressionNode> references = new ArrayList<>();
    private final TextRange definition;

    protected Symbol(String name, SType type) {
        this(name, type, null);
    }

    protected Symbol(String name, SType type, TextRange definition) {
        this.name = name;
        this.type = type;
        this.definition = definition;
    }

    public void addReference(BoundNameExpressionNode name) {
        references.add(name);
    }

    public List<BoundNameExpressionNode> getReferences() {
        return references;
    }

    public SType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public TextRange getDefinition() {
        return definition;
    }

    public boolean canSet() {
        return false;
    }

    public abstract void compileLoad(CompilerContext context, MethodVisitor visitor);

    public static boolean equals(Symbol s1, Symbol s2) {
        if (!Objects.equals(s1.name, s2.name)) {
            return false;
        }
        if (!s1.type.equals(s2.type)) {
            return false;
        }
        if (!Objects.equals(s1.definition, s2.definition)) {
            return false;
        }
        if (s1.references.size() != s2.references.size()) {
            return false;
        }
        for (int i = 0; i < s1.references.size(); i++) {
            BoundNameExpressionNode r1 = s1.references.get(i);
            BoundNameExpressionNode r2 = s2.references.get(i);
            if (!r1.value.equals(r2.value)) {
                return false;
            }
            if (!r1.getRange().equals(r2.getRange())) {
                return false;
            }
        }
        return true;
    }
}