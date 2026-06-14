package com.zergatul.scripting.tests.framework;

public class ComparatorCompilationParameters {

    private final Class<?> api;
    private final Class<?>[] customTypes;

    private ComparatorCompilationParameters(Class<?> api, Class<?>[] customTypes) {
        this.api = api;
        this.customTypes = customTypes;
    }

    public Class<?> getApi() {
        return api;
    }

    public Class<?>[] getCustomTypes() {
        return customTypes;
    }

    public static class Builder {

        private Class<?> api;
        private Class<?>[] customTypes;

        public Builder api(Class<?> api) {
            this.api = api;
            return this;
        }

        public Builder customType(Class<?> type) {
            if (customTypes == null) {
                customTypes = new Class[] { type };
            } else {
                throw new UnsupportedOperationException();
            }
            return this;
        }

        public ComparatorCompilationParameters build() {
            return new ComparatorCompilationParameters(api, customTypes);
        }
    }
}