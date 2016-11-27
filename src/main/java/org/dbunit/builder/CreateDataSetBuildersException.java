package org.dbunit.builder;

class CreateDataSetBuildersException extends Exception {
    public CreateDataSetBuildersException(Throwable cause, String msg, Object... args) {
        super(String.format(msg, args), cause);
    }

    CreateDataSetBuildersException(String msg, Object... args) {
        super(String.format(msg, args));
    }
}
