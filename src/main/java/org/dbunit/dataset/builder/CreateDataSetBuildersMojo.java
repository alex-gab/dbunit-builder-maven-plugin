package org.dbunit.dataset.builder;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.builder.javageneration.EntityClass;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;

@Mojo(name = "generate-builders")
public final class CreateDataSetBuildersMojo extends AbstractMojo {
    @Parameter(property = "schema.file")
    private File schemaFile;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        try {
            final List<CreateTable> statements = parseSchema(schemaFile);
            final String packageName = "org.dbunit.dataset.builder";
            final List<EntityClass> entityClasses = createEntities(statements, packageName);
            generateCode(entityClasses, packageName);
        } catch (SqlParsingException e) {
            throw new MojoExecutionException("Error parsing the sql file: " + e.getSchemaFileName(), e);
        } catch (CreateDataSetBuildersException e) {
            throw new MojoExecutionException("Error creating builders: ", e);
        }
    }

    private List<CreateTable> parseSchema(File schemaFile) throws SqlParsingException {
        List<CreateTable> createTables = new ArrayList<>();
        try {
            String[] sqlStatements = readSql(schemaFile);
            for (String sqlStatement : sqlStatements) {
                CreateTable statement = (CreateTable) CCJSqlParserUtil.parse(new StringReader(sqlStatement));
                createTables.add(statement);
            }
        } catch (JSQLParserException | IOException e) {
            throw new SqlParsingException(e, schemaFile, "Could not parse sql schema");
        }
        return createTables;
    }

    private String[] readSql(File schema) throws IOException {
        final InputStream schemaStream = new FileInputStream(schema);
        BufferedReader br = new BufferedReader(new InputStreamReader(schemaStream));
        String mysql = "";
        String line;
        while ((line = br.readLine()) != null) {
            mysql = mysql + line;
        }
        br.close();
        mysql = mysql.replaceAll("`", "");
        return mysql.split(";");
    }

    private List<EntityClass> createEntities(List<CreateTable> createTables, String packageName) {
        List<EntityClass> entityClasses = new ArrayList<>();
        for (CreateTable createTable : createTables) {
            entityClasses.add(new EntityClass(createTable, packageName));
        }
        return entityClasses;
    }

    private void generateCode(List<EntityClass> entityClasses, String packageName) throws CreateDataSetBuildersException {
        try {
            Path generationPath = createGenerationFolder();

            final TypeSpec.Builder dataSetBuilder = TypeSpec.classBuilder("SchemaDataSetBuilder").
                    addModifiers(PUBLIC, FINAL).
                    superclass(AbstractSchemaDataSetBuilder.class).
                    addMethod(
                            MethodSpec.constructorBuilder().
                                    addModifiers(PUBLIC).
                                    addException(DataSetException.class).
                                    addStatement("super(new $T())", DataSetBuilder.class).
                                    build()
                    );
            for (EntityClass entityClass : entityClasses) {
                entityClass.addNewRowMethod(dataSetBuilder);
                entityClass.generateRowBuilder(generationPath);
            }

            JavaFile.builder(packageName, dataSetBuilder.build()).build().writeTo(generationPath);
        } catch (IOException e) {
            throw new CreateDataSetBuildersException("Code generation exception: ", e);
        }
    }

    private Path createGenerationFolder() throws IOException {
        Path path = Paths.get("target/generated-test-sources/dbunit");
        Files.createDirectories(path);
        return path;
    }
}
