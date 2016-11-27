package org.dbunit.builder;

import java.io.File;

final class SqlParsingException extends CreateDataSetBuildersException {
    private final File schemaFile;

    SqlParsingException(Throwable cause, File schemaFile, String msg, Object... args) {
        super(String.format(msg, args), cause);
        this.schemaFile = schemaFile;
    }

    final String getSchemaFileName() {
        return schemaFile.getAbsolutePath();
    }
}
