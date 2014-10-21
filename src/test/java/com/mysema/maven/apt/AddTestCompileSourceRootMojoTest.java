package com.mysema.maven.apt;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

public class AddTestCompileSourceRootMojoTest {

    @Test
    public void Execute() throws MojoExecutionException, MojoFailureException {
        File output = new File("target/generated-sources/java");
        MavenProject project = new MavenProject();
        AddTestCompileSourceRootMojo mojo = new AddTestCompileSourceRootMojo();
        
        mojo.setProject(project);
        mojo.setOutputDirectory(output);
        mojo.execute();
        
        assertTrue(project.getTestCompileSourceRoots().contains(output.getAbsolutePath()));
    }

}
