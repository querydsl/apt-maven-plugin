package com.mysema.maven.apt;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.easymock.EasyMock;
import org.junit.Test;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mysema.query.apt.QuerydslAnnotationProcessor;


public class TestAnnotationProcessorMojoTest {
    
    @Test    
    public void Execute() throws MojoExecutionException, DependencyResolutionRequiredException {
        File outputDir = new File("target/generated-test-sources/java");
        Log log = EasyMock.createMock(Log.class);
        BuildContext buildContext = new DefaultBuildContext();
        MavenProject project = EasyMock.createMock(MavenProject.class); 
        List sourceRoots = Lists.newArrayList("src/test/resources/project-to-test/src/test/java");
        URLClassLoader loader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        List classpath = ClassPathUtils.getClassPath(loader);
        EasyMock.expect(project.getTestCompileSourceRoots()).andReturn(sourceRoots);
        EasyMock.expect(project.getTestCompileSourceRoots()).andReturn(sourceRoots);
        EasyMock.expect(project.getTestClasspathElements()).andReturn(classpath);
        project.addTestCompileSourceRoot(outputDir.getAbsolutePath());
        EasyMock.expectLastCall();
        EasyMock.replay(project);
        
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
        mojo.setOutputDirectory(outputDir);
        mojo.execute();
        
        EasyMock.verify(project);
        
        Assert.assertTrue(new File(outputDir, "com/example/QEntity2.java").exists());
    }
    
}
