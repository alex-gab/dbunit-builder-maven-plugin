package org.dbunit.builder;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Time;
import java.util.Date;
import java.util.UUID;

public enum SqlTypes {
    INT(Integer.class),
    BOOOLEAN(Boolean.class),
    TINYINT(Byte.class),
    SMALLINT(Short.class),
    BIGINT(Long.class),
    IDENTITY(Long.class),
    DECIMAL(BigDecimal.class),
    DOUBLE(Double.class),
    REAL(Float.class),
    TIME(Time.class),
    DATE(java.sql.Date.class),
    TIMESTAMP(Date.class),
    VARCHAR(String.class),
    CHAR(String.class),
    BLOB(Blob.class),
    CLOB(Clob.class),
    UUID(UUID.class);

    private final Class javaClass;

    SqlTypes(Class javaClass) {
        this.javaClass = javaClass;
    }

    public final Class getJavaClass() {
        return javaClass;
    }
}
