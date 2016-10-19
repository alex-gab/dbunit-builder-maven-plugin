package org.dbunit.dataset.builder.javageneration;

import com.squareup.javapoet.*;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.dbunit.dataset.builder.AbstractSchemaDataRowBuilder;
import org.dbunit.dataset.builder.ColumnSpec;
import org.dbunit.dataset.builder.SqlTypes;

import java.io.IOException;
import java.nio.file.Path;

import static javax.lang.model.element.Modifier.*;

public final class EntityClass {
    private static final String BUILDER_CLASS_PREFIX = "SchemaDataRowBuilder";

    private final CreateTable statement;
    private final String packageName;
    private final String tableName;
    private final String rowBuilderClassName;
    private final ClassName rowBuilderClass;

    public EntityClass(CreateTable statement, String packageName) {
        this.statement = statement;
        this.packageName = packageName;
        tableName = statement.getTable().getName();
        rowBuilderClassName = tableName + BUILDER_CLASS_PREFIX;
        rowBuilderClass = ClassName.get(packageName, rowBuilderClassName);
    }

    public final void addNewRowMethod(TypeSpec.Builder dataSetBuilder) {
        dataSetBuilder.
                addMethod(
                        MethodSpec.methodBuilder("new" + tableName + "Row").
                                addModifiers(PUBLIC, FINAL).
                                returns(rowBuilderClass).
                                addStatement("return new $T($L, $S)", rowBuilderClass, "this", tableName).
                                build()
                );
    }

    public final void generateRowBuilder(Path generationPath) throws IOException {
        final ClassName schemaDataSetBuilderClassName = ClassName.get(packageName, "SchemaDataSetBuilder");
        ParameterizedTypeName superclass =
                ParameterizedTypeName.get(
                        ClassName.get(AbstractSchemaDataRowBuilder.class),
                        schemaDataSetBuilderClassName);
        final TypeSpec.Builder dataRowBuilder = TypeSpec.classBuilder(rowBuilderClassName).
                addModifiers(PUBLIC, FINAL).
                superclass(superclass).
                addMethod(
                        MethodSpec.constructorBuilder().
                                addParameter(schemaDataSetBuilderClassName, "schemaDataSetBuilder", FINAL).
                                addParameter(String.class, "tableName", FINAL).
                                addStatement("super($L, $L)", "schemaDataSetBuilder", "tableName").
                                build()
                );

        for (ColumnDefinition columnDefinition : statement.getColumnDefinitions()) {
            final String dataType = columnDefinition.getColDataType().getDataType();
            final String name = columnDefinition.getColumnName();
            final Class clazz = SqlTypes.valueOf(dataType).getJavaClass();

            TypeName columnSpec = ParameterizedTypeName.get(ColumnSpec.class, clazz);

            final FieldSpec field = FieldSpec.builder(columnSpec, name, PRIVATE, STATIC, FINAL).
                    initializer("ColumnSpec.newColumn($S)", name).
                    build();
            dataRowBuilder.
                    addField(field).
                    addMethod(
                            MethodSpec.methodBuilder(name).
                                    addModifiers(PUBLIC, FINAL).
                                    returns(rowBuilderClass).
                                    addParameter(clazz, name, FINAL).
                                    addStatement("$L.with($L.$N, $L)", "dataRowBuilder", rowBuilderClassName, field, name).
                                    addStatement("return this").
                                    build()
                    );
        }


        JavaFile.builder(packageName, dataRowBuilder.build()).build().writeTo(generationPath);
    }
}
