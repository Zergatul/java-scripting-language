package com.zergatul.scripting.binding;

import com.zergatul.scripting.type.SDeclaredType;

import java.util.ArrayList;
import java.util.List;

public class ClassDeclaration {

    private final SDeclaredType classType;
    private final List<ClassFieldDeclaration> fields = new ArrayList<>();
    private final List<ClassConstructorDeclaration> constructors = new ArrayList<>();
    private final List<ClassMethodDeclaration> methods = new ArrayList<>();

    public ClassDeclaration(SDeclaredType classType) {
        this.classType = classType;
    }

    public void addField(ClassFieldDeclaration declaration) {
        fields.add(declaration);
    }

    public SDeclaredType classType() {
        return this.classType;
    }

    public boolean hasMember(String name) {
        return fields.stream().anyMatch(f -> f.name().equals(name)) || methods.stream().anyMatch(m -> m.name().equals(name));
    }
}
