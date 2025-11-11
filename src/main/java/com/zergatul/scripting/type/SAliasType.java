package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class SAliasType extends SSyntheticType {

    private final String name;
    @Nullable private SType underlying;

    public SAliasType(String name) {
        this.name = name;
    }

    public SAliasType(String name, SType underlying) {
        this.name = name;
        this.underlying = underlying;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SAliasType other) {
            return other.name.equals(name) && Objects.equals(other.underlying, underlying);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isFormingLoop(SAliasType other) {
        SAliasType current = other;
        while (true) {
            if (current == this) {
                return true;
            }
            if (current.underlying instanceof SAliasType nested) {
                current = nested;
            } else {
                return false;
            }
        }
    }

    public boolean canBeResolved() {
        SAliasType current = this;
        while (true) {
            if (current.underlying == null) {
                return false;
            }
            if (current.underlying instanceof SAliasType nested) {
                current = nested;
            } else {
                return true;
            }
        }
    }

    public void setUnderlying(SType type) {
        this.underlying = type;
    }

    public SType getFinalType() {
        SAliasType current = this;
        while (true) {
            if (current.underlying == null) {
                throw new InternalException();
            }
            if (current.underlying instanceof SAliasType nested) {
                current = nested;
            } else {
                return current.underlying;
            }
        }
    }
}