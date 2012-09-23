package com.mysemna.maven.apt;

import java.io.File;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mysema.maven.apt.AnnotationProcessorMojo;
import com.mysema.query.apt.QuerydslAnnotationProcessor;


public class AnnotationProcessorMojoTest {
    
    @Test
    public void Execute() throws MojoExecutionException, DependencyResolutionRequiredException {
        Log log = EasyMock.createNiceMock(Log.class);
        BuildContext buildContext = new DefaultBuildContext();
        MavenProject project = new MavenProject();
        project.getCompileSourceRoots().add("src/test/resources/project-to-test/src/main/java");
        project.getBuild().setOutputDirectory("target/generated-sources/java");
        
        for (Object str : project.getCompileClasspathElements()) {
            System.err.println(str);
        }
        
        AnnotationProcessorMojo mojo = new AnnotationProcessorMojo();
        mojo.setBuildContext(buildContext);
        mojo.setCompilerOptions(Maps.<String,String>newHashMap());
        mojo.setIncludes(Sets.<String>newHashSet());
        mojo.setLog(log);
        mojo.setLogOnlyOnError(false);
        mojo.setOptions(Maps.<String,String>newHashMap());
        mojo.setProcessor(QuerydslAnnotationProcessor.class.getName());
        mojo.setProject(project);
        mojo.setSourceEncoding("UTF-8");
        mojo.setOutputDirectory(new File("target/generated-sources/java"));
        mojo.execute();
    }
    
}
