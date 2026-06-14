package com.zergatul.scripting.type;

import java.util.Comparator;
import java.util.List;

public interface Invocable {

    Comparator<Invocable> SORT_ORDER = (inv1, inv2) -> {
        List<SType> params1 = inv1.getParameterTypes();
        List<SType> params2 = inv2.getParameterTypes();
        if (params1.size() != params2.size()) {
            return Integer.compare(params1.size(), params2.size());
        }

        int size = params1.size();
        for (int i = 0; i < size; i++) {
            String type1 = params1.get(i).toString();
            String type2 = params2.get(i).toString();
            int result = String.CASE_INSENSITIVE_ORDER.compare(type1, type2);
            if (result != 0) {
                return result;
            }
        }

        return 0;
    };

    List<MethodParameter> getParameters();

    default List<SType> getParameterTypes() {
        return getParameters().stream().map(MethodParameter::type).toList();
    }

    String toDiagnosticsString();
}