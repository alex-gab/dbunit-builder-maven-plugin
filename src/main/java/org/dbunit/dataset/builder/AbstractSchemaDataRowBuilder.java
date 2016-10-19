package org.dbunit.dataset.builder;

import org.dbunit.dataset.DataSetException;

public abstract class AbstractSchemaDataRowBuilder<SDSB extends AbstractSchemaDataSetBuilder> implements SchemaDataRowBuilder {
    private final SDSB dataSetBuilder;
    private final DataSetBuilder undelyingDataSetBuilder;
    final DataRowBuilder dataRowBuilder;

    AbstractSchemaDataRowBuilder(final SDSB schemaDataSetBuilder, final String tableName) {
        this.dataSetBuilder = schemaDataSetBuilder;
        undelyingDataSetBuilder = dataSetBuilder.getUndelyingBuilder();
        dataRowBuilder = new DataRowBuilder(undelyingDataSetBuilder, tableName);
    }

    @Override
    public final SDSB add() throws DataSetException {
        undelyingDataSetBuilder.add(dataRowBuilder);
        return dataSetBuilder;
    }
}
