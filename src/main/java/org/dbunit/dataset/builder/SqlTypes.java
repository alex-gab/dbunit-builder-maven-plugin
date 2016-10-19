package org.dbunit.dataset.builder;

import java.util.Date;

public enum SqlTypes {
    VARCHAR(String.class),
    DATE(Date.class),
    NUMBER(Integer.class);

    private final Class javaClass;

    SqlTypes(Class javaClass) {
        this.javaClass = javaClass;
    }

    public final Class getJavaClass() {
        return javaClass;
    }
}
