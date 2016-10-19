package org.dbunit.dataset.builder;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.dbunit.dataset.builder.javageneration.EntityClass;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "generate-builders")
public final class CreateDataSetBuildersMojo extends AbstractMojo {
    @Parameter(property = "sayhi.file")
    private File schemaFile;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        try {
            final List<EntityClass> entityClasses = parseSchema(schemaFile);
            for (EntityClass entityClass : entityClasses) {
                entityClass.generateCode();
            }

        } catch (SqlParsingException e) {
            throw new MojoExecutionException("Error parsing the sql file: " + e.getSchemaFileName(), e);
        } catch (CreateDataSetBuildersException e) {
            throw new MojoExecutionException("Error creating builders: ", e);
        }
    }

    private List<EntityClass> parseSchema(File schemaFile) throws SqlParsingException {
        List<EntityClass> entityClasses = new ArrayList<>();
        try {
            String[] sqlStatements = readSql(schemaFile);
            for (String sqlStatement : sqlStatements) {
                CreateTable statement = (CreateTable) CCJSqlParserUtil.parse(new StringReader(sqlStatement));
                entityClasses.add(new EntityClass(statement));
            }
        } catch (JSQLParserException | IOException e) {
            throw new SqlParsingException(e, schemaFile, "Could not parse sql schema");
        }
        return entityClasses;
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
}
