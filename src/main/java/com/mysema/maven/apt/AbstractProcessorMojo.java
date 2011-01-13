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
import java.util.LinkedHashMap;
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
import org.codehaus.plexus.util.StringUtils;

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
    
    /**
     * @parameter
     */
    protected Map<String,String> compilerOptions;

    /**
     * @parameter
     */
    protected boolean showWarnings = false;

    /**
     * @parameter
     */
    protected boolean logOnlyOnError = false;
    
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

            Map<String, String> compilerOpts = new LinkedHashMap<String, String>();

            // Default options
            compilerOpts.put("cp", compileClassPath);
            
            if (sourceEncoding != null){
                compilerOpts.put("encoding", sourceEncoding);    
            }
            
            compilerOpts.put("proc:only", null);
            compilerOpts.put("processor", processor);
            
            if (options != null){
                for (Map.Entry<String,String> entry : options.entrySet()){
                    compilerOpts.put("A"+entry.getKey()+"="+entry.getValue(), null);
                }
            }
            
            if (outputDirectory != null){
                compilerOpts.put("s", outputDirectory.getPath());    
            }
            
            if (!showWarnings) {
                compilerOpts.put("nowarn", null);
            }
            
            compilerOpts.put("sourcepath", getSourceDirectory().getCanonicalPath());
            
            // User options override default options
            if (compilerOptions != null) {
                compilerOpts.putAll(compilerOptions);
            }

            List<String> opts = new ArrayList<String>(compilerOpts.size() * 2);
            
            for (Map.Entry<String, String> compilerOption : compilerOpts.entrySet()) {
                opts.add("-" + compilerOption.getKey());
                String value = compilerOption.getValue();
                if (StringUtils.isNotBlank(value)) {
                    opts.add(value);
                }
            }
            
            Writer out = null;
            if (logOnlyOnError){
                out = new StringWriter();
            }            
            CompilationTask task = compiler.getTask(
                    out, fileManager, null, opts,
                    null, compilationUnits1);
            // Perform the compilation task.
            Boolean rv = task.call();
            if (rv.equals(Boolean.FALSE) && logOnlyOnError){
                getLog().error(out.toString());
            }

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
