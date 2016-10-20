# DBUnit - Generating Data Set Builders Based on Database Schema

## Introduction

Maven plugin that generates data set builders in order to create data set right into the code.
Code generation is based on the database schema file.

The idea was based on
[marcphilipp's dbunit-datasetbuilder project](https://github.com/marcphilipp/dbunit-datasetbuilder).

## Configuration

The `dbunit-builder-maven-plugin` must be added to the list of plugins in the `pom.xml` file:
```xml
<plugin>
    <groupId>org.dbunit.dataset.builder</groupId>
    <artifactId>dbunit-builder-maven-plugin</artifactId>
    <version>1.0</version>
    <configuration>
        <schemaFile>src/test/resources/schema.sql</schemaFile>
    </configuration>
</plugin>
```
A relative path to the `schema.sql` file must be provided in the plugin configuration section.

You can execute the `dbunit-builder:generate-builders` goal, or you can directly link the goal to the `generate-test-sources` phase:
```xml
<plugin>
    <groupId>org.dbunit.dataset.builder</groupId>
    <artifactId>dbunit-builder-maven-plugin</artifactId>
    <version>1.0</version>
    <configuration>
        <schemaFile>src/test/resources/schema.sql</schemaFile>
    </configuration>
    <executions>
        <execution>
            <phase>generate-test-sources</phase>
            <goals>
                <goal>generate-builders</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```


The sources are generated by default in `${project.build.directory}/generated-test-sources/dbunit`.

You might also want to configure `build-helper-maven-plugin` to include the above directory as a compilation unit to your project. Otherwise, you will have to copy-paste the generated sources
directly in your test source files in order to compile them.

Also the plugin must be added as a dependency to your project, because at this stage of development the generated classes directly depend on the plugin:
```xml
 <dependency>
    <groupId>org.dbunit.dataset.builder</groupId>
    <artifactId>dbunit-builder-maven-plugin</artifactId>
    <version>1.0</version>
</dependency>
```


A database `schema.sql` file must be provided in order for the plugin to generate the builders:
```sql
CREATE TABLE Employee (
  name    VARCHAR,
  hiredate TIMESTAMP,
  salary      INT
);
```
At this stage of development, only the [H2 Data Types](http://www.h2database.com/html/datatypes.html) are supported.

## Usage
You can use the generated builders in your test classes as follows:
```
IDataSet dataSet = new SchemaDataSetBuilder().newEmployeeRow().name("JAMES").hiredate(new GregorianCalendar(1981, DECEMBER, 3).getTime()).salary(950).add().
                newEmployeeRow().name("SMITH").hiredate(new GregorianCalendar(1980, DECEMBER, 17).getTime()).salary(800).add().
                newEmployeeRow().name("ADAMS").hiredate(new GregorianCalendar(1983, JANUARY, 12).getTime()).salary(1100).add().
                newEmployeeRow().name("MILLER").hiredate(new GregorianCalendar(1982, JANUARY, 23).getTime()).salary(1900).add().
                build();
```
