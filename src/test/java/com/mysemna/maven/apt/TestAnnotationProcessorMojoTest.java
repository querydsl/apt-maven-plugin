package com.mysemna.maven.apt;

import java.io.File;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.easymock.EasyMock;
import org.junit.Test;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mysema.maven.apt.TestAnnotationProcessorMojo;
import com.mysema.query.apt.QuerydslAnnotationProcessor;


public class TestAnnotationProcessorMojoTest {
    
    @Test
    public void Execute() throws MojoExecutionException, DependencyResolutionRequiredException {
        Log log = EasyMock.createNiceMock(Log.class);
        BuildContext buildContext = new DefaultBuildContext();
        MavenProject project = new MavenProject();
        project.getCompileSourceRoots().add("src/test/resources/project-to-test/src/test/java");
        project.getBuild().setOutputDirectory("target/generated-test-sources/java");
        
        for (Object str : project.getCompileClasspathElements()) {
            System.err.println(str);
        }
        
        TestAnnotationProcessorMojo mojo = new TestAnnotationProcessorMojo();
        mojo.setBuildContext(buildContext);
        mojo.setCompilerOptions(Maps.<String,String>newHashMap());
        mojo.setIncludes(Sets.<String>newHashSet());
        mojo.setLog(log);
        mojo.setLogOnlyOnError(false);
        mojo.setOptions(Maps.<String,String>newHashMap());
        mojo.setProcessor(QuerydslAnnotationProcessor.class.getName());
        mojo.setProject(project);
        mojo.setSourceEncoding("UTF-8");
        mojo.setOutputDirectory(new File("target/generated-test-sources/java"));
        mojo.execute();
        
        // TODO fix classpath and issues and add assertions
    }
    
    
}
