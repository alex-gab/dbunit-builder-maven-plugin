package org.dbunit.builder;

import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;

interface ISchemaDataSetBuilder {
    IDataSet build() throws DataSetException;
}
