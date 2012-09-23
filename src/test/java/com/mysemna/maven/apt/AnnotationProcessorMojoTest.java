package com.mysemna.maven.apt;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mysema.maven.apt.AnnotationProcessorMojo;
import com.mysema.query.apt.QuerydslAnnotationProcessor;
import com.mysema.util.FileUtils;


public class AnnotationProcessorMojoTest {
    
    private File outputDir;
    
    private MavenProject project;
    
    private AnnotationProcessorMojo mojo;
    
    @Before
    public void setUp() throws DependencyResolutionRequiredException {
        outputDir = new File("target/generated-sources/java");
        Log log = EasyMock.createMock(Log.class);
        BuildContext buildContext = new DefaultBuildContext();
        project = EasyMock.createMock(MavenProject.class); 
        List sourceRoots = Lists.newArrayList("src/test/resources/project-to-test/src/main/java");
        URLClassLoader loader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        List classpath = ClassPathUtils.getClassPath(loader);
        EasyMock.expect(project.getCompileSourceRoots()).andReturn(sourceRoots);
        EasyMock.expect(project.getCompileSourceRoots()).andReturn(sourceRoots);
        EasyMock.expect(project.getCompileClasspathElements()).andReturn(classpath);
        project.addCompileSourceRoot(outputDir.getAbsolutePath());
        EasyMock.expectLastCall();
        EasyMock.replay(project);
        
        mojo = new AnnotationProcessorMojo();
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
    }
    
    @After
    public void tearDown() throws IOException {
        FileUtils.delete(outputDir);
    }
    
    @Test    
    public void Execute() throws MojoExecutionException {        
        mojo.execute();        
        EasyMock.verify(project);        
        assertTrue(new File(outputDir, "com/example/QEntity.java").exists());
    }
    
    @Test
    public void Processors() throws MojoExecutionException {
        mojo.setProcessor(null);
        mojo.setProcessors(new String[]{QuerydslAnnotationProcessor.class.getName()});
        mojo.execute();        
        EasyMock.verify(project);        
        assertTrue(new File(outputDir, "com/example/QEntity.java").exists());
    }
    
    @Test
    public void Includes() throws MojoExecutionException {
        mojo.setIncludes(Sets.newHashSet("com/example/**"));
        mojo.execute();        
        EasyMock.verify(project);        
        assertTrue(new File(outputDir, "com/example/QEntity.java").exists());
    }
    
    @Test
    public void Options() throws MojoExecutionException {
        mojo.setOptions(Collections.singletonMap("querydsl.packageSuffix", ".query"));
        mojo.execute();        
        EasyMock.verify(project);        
        assertTrue(new File(outputDir, "com/example/query/QEntity.java").exists());
    }
    
}
