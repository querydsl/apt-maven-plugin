/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.maven.apt;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Base class for AnnotationProcessorMojo implementations
 * 
 * @author tiwe
 *
 */
public abstract class AbstractProcessorMojo extends AbstractMojo {

    /**
     * @parameter expression="${project}" readonly=true required=true
     */
    protected MavenProject project;

    /**
     * @parameter 
     */
    protected File outputDirectory;

    /**
     * @parameter
     */
    protected String[] processors;
    
    /**
     * @parameter
     */
    protected String processor;
    
    
    /**
     * @parameter expression="${project.build.sourceEncoding}" required=tue
     */
    protected String sourceEncoding;
    
    /**
     * @parameter
     */
    protected Map<String,String> options;
    

    @SuppressWarnings("unchecked")
    private String buildCompileClasspath() {
        List<String> pathElements = null;
        try {
            if (isForTest()){
                pathElements = project.getTestClasspathElements();
            }else{
                pathElements = project.getCompileClasspathElements();    
            }        
        } catch (DependencyResolutionRequiredException e) {
            super.getLog().warn("exception calling getCompileClasspathElements", e);
            return null;
        }
        if (pathElements.isEmpty()){
            return null;   
        }
        StringBuilder result = new StringBuilder();
        int i = 0;
        for (i = 0; i < pathElements.size() - 1; ++i) {
            result.append(pathElements.get(i)).append(File.pathSeparatorChar);
        }
        result.append(pathElements.get(i));
        return result.toString();
    }

    private String buildProcessor() {
        if (processors != null){
            StringBuilder result = new StringBuilder();
            for (String processor : processors){
                if (result.length() > 0){
                    result.append(",");
                }                    
                result.append(processor);
            }
            return result.toString();    
        }else if (processor != null){
            return processor;
        }else{
            String error = "Either processor or processors need to be given";
            getLog().error(error);
            throw new IllegalArgumentException(error);
        }
    }

    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException {                    
        if (outputDirectory != null && !outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }        
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
            List<File> files = FileUtils.getFiles(getSourceDirectory(), "**/*.java", null);
            Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjectsFromFiles(files);

            String compileClassPath = buildCompileClasspath();

            String processor = buildProcessor();

            List<String> opts = new ArrayList<String>(10);

            opts.add("-cp");
            opts.add(compileClassPath);
            
            if (sourceEncoding != null){
                opts.add("-encoding");
                opts.add(sourceEncoding);    
            }            
            
            opts.add("-proc:only");
            opts.add("-processor");
            opts.add(processor);
            
            if (options != null){
                for (Map.Entry<String,String> entry : options.entrySet()){
                    opts.add("-A"+entry.getKey()+"="+entry.getValue());
                }
            }
            
            if (outputDirectory != null){
                opts.add("-s");
                opts.add(outputDirectory.getPath());    
            }            
            
            Writer writer = new StringWriter();
            CompilationTask task = compiler.getTask(
                    writer, fileManager, null, opts,
                    null, compilationUnits1);
            // Perform the compilation task.
            task.call();
            FileUtils.fileWrite(
                    new File(project.getBasedir(), "target/apt-log").getAbsolutePath(), 
                    writer.toString());

            if (outputDirectory != null){
                if (isForTest()){
                    project.addTestCompileSourceRoot(outputDirectory.getAbsolutePath());                
                }else{
                    project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
                }    
            }
            
            
        } catch (Exception e1) {
            super.getLog().error("execute error", e1);
            throw new MojoExecutionException(e1.getMessage());
        }

    }

    protected abstract File getSourceDirectory();

    protected boolean isForTest(){
        return false;
    }
    
}
