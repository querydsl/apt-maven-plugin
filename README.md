apt-maven-plugin
================

apt-maven-plugin provides Maven integration of the Java 6 APT functionality.

The supported goals are

    process - to process main sources
    test-process - to process test sources

Here is an example of a configuration

    <plugin>
        <groupId>com.mysema.maven</groupId>
        <artifactId>apt-maven-plugin</artifactId>
        <version>1.0.9</version>
        <executions>
            <execution>
                <goals>
                    <goal>process</goal>
                </goals>
                <configuration>
                    <outputDirectory>target/generated-sources/java</outputDirectory>
                    <processor>com.mysema.query.apt.jpa.JPAAnnotationProcessor</processor>
                </configuration>
            </execution>
        </executions>
    </plugin>

Here is an example for usage with the m2e plugin https://github.com/mysema/apt-maven-plugin/wiki/m2e-usage

If you're using AspectJ to introduce artifacts into classes (fields, methods or annotations), then 
`apt-maven-plugin` will not work properly and classpath inspecting tools need to be used, such as GenericExporter, 
if you use Querydsl.


