apt-maven-plugin
================

maven-apt-plugin provides Maven integration of the Java 6 APT functionality.

The supported goals are

    process - to process main sources
    test-process - to process test sources

Here is an example of a configuration

    <plugin>
        <groupId>com.mysema.maven</groupId>
        <artifactId>apt-maven-plugin</artifactId>
        <version>1.0.8</version>
        <executions>
            <execution>
                <goals>
                    <goal>process</goal>
                </goals>
                <configuration>
                    <outputDirectory>target/generated-sources/java</outputDirectory>
                    <processor>com.mysema.rdfbean.query.BeanAnnotationProcessor</processor>
                </configuration>
            </execution>
        </executions>
    </plugin>

Here is an example for usage with the m2e plugin https://github.com/mysema/apt-maven-plugin/wiki/m2e-usage


