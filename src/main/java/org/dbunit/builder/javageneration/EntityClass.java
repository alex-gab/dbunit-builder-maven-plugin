package org.dbunit.builder.javageneration;

import com.squareup.javapoet.*;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.dbunit.builder.AbstractRow;
import org.dbunit.builder.Builder;
import org.dbunit.builder.Column;
import org.dbunit.builder.SqlTypes;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.builder.ColumnSpec;
import org.dbunit.dataset.builder.DataRowBuilder;

import java.io.IOException;
import java.nio.file.Path;

import static javax.lang.model.element.Modifier.*;

public final class EntityClass {
    private static final String BUILDER_CLASS_PREFIX = "Row";

    private final CreateTable statement;
    private final String packageName;
    private final String tableName;
    private final String rowClassName;
    private final ClassName rowBuilderClass;

    public EntityClass(CreateTable statement, String packageName) {
        this.statement = statement;
        this.packageName = packageName;
        tableName = statement.getTable().getName();
        rowClassName = tableName + BUILDER_CLASS_PREFIX;
        rowBuilderClass = ClassName.get(packageName, rowClassName);
    }

    public final void generateRowBuilder(Path generationPath) throws IOException {
        final ClassName builderClassType = ClassName.bestGuess(rowClassName + "Builder");

        final TypeSpec.Builder rowBuilder = getRowBuilder();
        final TypeSpec.Builder row = getRow();
        final MethodSpec.Builder rowConstructor = getRowConstructor(builderClassType);
        final MethodSpec.Builder addToDataSetMethod = getAddThisToDataSet();

        for (ColumnDefinition columnDefinition : statement.getColumnDefinitions()) {
            final String dataType = columnDefinition.getColDataType().getDataType();
            final String name = columnDefinition.getColumnName();
            final Class clazz = SqlTypes.valueOf(dataType).getJavaClass();

            addField(row, name, clazz);
            addField(rowBuilder, name, clazz);
            rowConstructor.addStatement("this.$L = $L.$L", name, "builder", name);
            addToDataSetMethod.addStatement("dataRowBuilder.with($L.getColumnSpec(), $L.getValue())", name, name);
            rowBuilder.addMethod(MethodSpec.methodBuilder(name).
                    addModifiers(PUBLIC, FINAL).
                    returns(builderClassType).
                    addParameter(clazz, name, FINAL).
                    addStatement("this.$L = new Column($T.newColumn($S), $L)", name, ColumnSpec.class, name, name).
                    addStatement("return this").
                    build());
        }

        addToDataSetMethod.addStatement("return dataRowBuilder.add()");
        row.addMethod(rowConstructor.build()).addMethod(addToDataSetMethod.build());
        addBuildMethod(rowBuilder);
        row.addType(rowBuilder.build());

        JavaFile.builder(packageName, row.build()).build().writeTo(generationPath);
    }

    private TypeSpec.Builder getRowBuilder() {
        final String builderClassName = rowClassName + "Builder";
        final ClassName builderClassType = ClassName.bestGuess(builderClassName);
        return TypeSpec.classBuilder(builderClassName).
                addSuperinterface(
                        ParameterizedTypeName.get(ClassName.get(Builder.class),
                                rowBuilderClass)).
                addModifiers(PUBLIC, STATIC, FINAL).
                addMethod(MethodSpec.constructorBuilder().addModifiers(PRIVATE).build()).
                addMethod(MethodSpec.methodBuilder(getFactoryMethodName(rowClassName)).
                        addModifiers(PUBLIC, STATIC).
                        returns(builderClassType).
                        addStatement("return new $L()", builderClassName).
                        build());
    }

    private TypeSpec.Builder getRow() {
        return TypeSpec.classBuilder(rowClassName).
                addModifiers(PUBLIC, FINAL).
                superclass(AbstractRow.class);
    }

    private MethodSpec.Builder getRowConstructor(ClassName builderClassType) {
        return MethodSpec.constructorBuilder().
                addModifiers(PRIVATE).
                addParameter(builderClassType, "builder", FINAL).
                addStatement("super($S)", tableName);
    }

    private String getFactoryMethodName(String className) {
        final String vowels = "aeiou";
        if (vowels.indexOf(Character.toLowerCase(className.charAt(0))) != -1) {
            return "an" + className;
        } else {
            return "a" + className;
        }
    }

    private MethodSpec.Builder getAddThisToDataSet() {
        return MethodSpec.methodBuilder("addThisToDataSet").
                addAnnotation(Override.class).
                addModifiers(PUBLIC, FINAL).
                addParameter(org.dbunit.dataset.builder.DataSetBuilder.class, "dataSetBuilder", FINAL).
                returns(org.dbunit.dataset.builder.DataSetBuilder.class).
                addException(DataSetException.class).
                addStatement("final $T dataRowBuilder = dataSetBuilder.newRow(tableName)", DataRowBuilder.class);
    }

    private void addField(TypeSpec.Builder destinationClass, String fieldName, Class fieldClass) {
        destinationClass.addField(
                ParameterizedTypeName.get(Column.class, fieldClass),
                fieldName,
                PRIVATE
        );
    }

    private void addBuildMethod(TypeSpec.Builder rowBuilder) {
        rowBuilder.addMethod(
                MethodSpec.methodBuilder("build").
                        addModifiers(PUBLIC, FINAL).
                        addAnnotation(Override.class).
                        returns(rowBuilderClass).
                        addStatement("return new $L(this)", rowClassName).
                        build());
    }
}
