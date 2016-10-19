package org.dbunit.dataset.builder;

import java.io.File;

public final class SqlParsingException extends CreateDataSetBuildersException {
    private final File schemaFile;

    public SqlParsingException(Throwable cause, File schemaFile, String msg, Object... args) {
        super(String.format(msg, args), cause);
        this.schemaFile = schemaFile;
    }

    public final String getSchemaFileName() {
        return schemaFile.getAbsolutePath();
    }
}
