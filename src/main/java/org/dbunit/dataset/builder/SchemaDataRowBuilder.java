package org.dbunit.dataset.builder;

import org.dbunit.dataset.DataSetException;

interface SchemaDataRowBuilder {
    ISchemaDataSetBuilder add() throws DataSetException;
}
