package org.dbunit.dataset.builder;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;

public abstract class AbstractSchemaDataSetBuilder implements ISchemaDataSetBuilder {
    private final DataSetBuilder dataSetBuilder;

    AbstractSchemaDataSetBuilder(final DataSetBuilder dataSetBuilder) {
        this.dataSetBuilder = dataSetBuilder;
    }

    @Override
    public final IDataSet build() throws DataSetException {
        return dataSetBuilder.build();
    }

    DataSetBuilder getUndelyingBuilder() {
        return dataSetBuilder;
    }
}
