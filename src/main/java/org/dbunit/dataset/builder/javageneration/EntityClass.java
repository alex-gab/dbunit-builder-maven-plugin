package org.dbunit.dataset.builder.javageneration;

import com.squareup.javapoet.*;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.builder.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static javax.lang.model.element.Modifier.*;

public final class EntityClass {
    public static final String BUILDER_CLASS_PREFIX = "SchemaDataRowBuilder";

    private final CreateTable statement;

    public EntityClass(CreateTable statement) {
        this.statement = statement;
    }

    public final void generateCode() throws CreateDataSetBuildersException {
        try {
            final String tableName = statement.getTable().getName();
            final String rowBuilderClassName = tableName + BUILDER_CLASS_PREFIX;
            final String packageName = "org.dbunit.dataset.builder";
            final ClassName rowBuilderClass = ClassName.get(packageName, rowBuilderClassName);

            final TypeSpec.Builder dataSetBuilder = TypeSpec.classBuilder("SchemaDataSetBuilder").
                    addModifiers(PUBLIC, FINAL).
                    superclass(AbstractSchemaDataSetBuilder.class).
                    addMethod(
                            MethodSpec.constructorBuilder().
                                    addModifiers(PUBLIC).
                                    addException(DataSetException.class).
                                    addStatement("super(new $T())", DataSetBuilder.class).
                                    build()
                    ).
                    addMethod(
                            MethodSpec.methodBuilder("new" + tableName + "Row").
                                    addModifiers(PUBLIC, FINAL).
                                    returns(rowBuilderClass).
                                    addStatement("return new $T($L, $S)", rowBuilderClass, "this", tableName).
                                    build()
                    );

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

            Path path = Paths.get("target/generated-test-sources/dbunit");
            Files.createDirectories(path);
            JavaFile.builder(packageName, dataSetBuilder.build()).build().writeTo(path);
            JavaFile.builder(packageName, dataRowBuilder.build()).build().writeTo(path);
        } catch (IOException e) {
            throw new CreateDataSetBuildersException("Code generation exception: ", e);
        }
    }
}
