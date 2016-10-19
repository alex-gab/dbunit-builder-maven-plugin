package org.dbunit.dataset.builder;

public class CreateDataSetBuildersException extends Exception {
    public CreateDataSetBuildersException(Throwable cause, String msg, Object... args) {
        super(String.format(msg, args), cause);
    }

    public CreateDataSetBuildersException(String msg, Object... args) {
        super(String.format(msg, args));
    }
}
