/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.maven.apt;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.Scanner;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Base class for AnnotationProcessorMojo implementations
 * 
 * @author tiwe
 * 
 */
public abstract class AbstractProcessorMojo extends AbstractMojo {

    private static final String JAVA_FILE_FILTER = "/*.java";
    private static final String[] ALL_JAVA_FILES_FILTER = new String[] { "**" + JAVA_FILE_FILTER };

    /**
     * @component
     */
    private BuildContext buildContext;

    /**
     * @parameter expression="${project}" readonly=true required=true
     */
    private MavenProject project;

    /**
     * @parameter
     */
    private String[] processors;

    /**
     * @parameter
     */
    private String processor;

    /**
     * @parameter expression="${project.build.sourceEncoding}" required=true
     */
    private String sourceEncoding;

    /**
     * @parameter
     */
    private Map<String, String> options;

    /**
     * @parameter
     */
    private Map<String, String> compilerOptions;

    /**
     * A list of inclusion package filters for the apt processor.
     * 
     * If not specified all sources will be used for apt processor
     * 
     * <pre>
     * e.g.:
     * &lt;includes&gt;
     * 	&lt;include&gt;com.mypackge.**.bo.**&lt;/include&gt;
     * &lt;/includes&gt;
     * </pre>
     * 
     * will include all files which match com/mypackge/ ** /bo/ ** / *.java
     * 
     * @parameter
     */
    private Set<String> includes = new HashSet<String>();

    /**
     * @parameter
     */
    private boolean showWarnings = false;

    /**
     * @parameter
     */
    private boolean logOnlyOnError = false;

    /**
     * @parameter expression="${plugin.artifacts}" readonly=true required=true
     */
    private List<Artifact> pluginArtifacts;

    @SuppressWarnings("unchecked")
    private String buildCompileClasspath() {
        List<String> pathElements = null;
        try {
            if (isForTest()) {
                pathElements = project.getTestClasspathElements();
            } else {
                pathElements = project.getCompileClasspathElements();
            }
        } catch (DependencyResolutionRequiredException e) {
            super.getLog().warn("exception calling getCompileClasspathElements", e);
            return null;
        }

        if (pluginArtifacts != null) {
            for (Artifact a : pluginArtifacts) {
                if (a.getFile() != null) {
                    pathElements.add(a.getFile().getAbsolutePath());
                }
            }
        }

        if (pathElements.isEmpty()) {
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
        if (processors != null) {
            StringBuilder result = new StringBuilder();
            for (String processor : processors) {
                if (result.length() > 0) {
                    result.append(",");
                }
                result.append(processor);
            }
            return result.toString();
        } else if (processor != null) {
            return processor;
        } else {
            String error = "Either processor or processors need to be given";
            getLog().error(error);
            throw new IllegalArgumentException(error);
        }
    }

    private List<String> buildCompilerOptions(String processor, String compileClassPath) throws IOException {
        Map<String, String> compilerOpts = new LinkedHashMap<String, String>();

        // Default options
        compilerOpts.put("cp", compileClassPath);

        if (sourceEncoding != null) {
            compilerOpts.put("encoding", sourceEncoding);
        }

        compilerOpts.put("proc:only", null);
        compilerOpts.put("processor", processor);

        if (options != null) {
            for (Map.Entry<String, String> entry : options.entrySet()) {
                if (entry.getValue() != null) {
                    compilerOpts.put("A" + entry.getKey() + "=" + entry.getValue(), null);    
                } else {
                    compilerOpts.put("A" + entry.getKey() + "=", null);
                }
                
            }
        }

        if (getOutputDirectory() != null) {
            compilerOpts.put("s", getOutputDirectory().getPath());
        }

        if (!showWarnings) {
            compilerOpts.put("nowarn", null);
        }
        
        StringBuilder builder = new StringBuilder();
        for (File file : getSourceDirectories()) {
            if (builder.length() > 0) {
                builder.append(";");
            }
            builder.append(file.getCanonicalPath());
        }
        compilerOpts.put("sourcepath", builder.toString());

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
        return opts;
    }

    /**
     * Filter files for apt processing based on the {@link #includes} filter and
     * also taking into account m2e {@link BuildContext} to filter-out unchanged
     * files when invoked as incremental build
     * 
     * @param directories
     *            source directories in which files are located for apt processing
     * 
     * @return files for apt processing. Returns empty set when there is no
     *         files to process
     */
    private Set<File> filterFiles(Set<File> directories) {
        String[] filters = ALL_JAVA_FILES_FILTER;
        if (includes != null && !includes.isEmpty()) {
            filters = includes.toArray(new String[includes.size()]);
            for (int i = 0; i < filters.length; i++) {
                filters[i] = filters[i].replace('.', '/') + JAVA_FILE_FILTER;
            }
        }
        
        Set<File> files = new HashSet<File>();        
        for (File directory : directories) {
            // support for incremental build in m2e context
            Scanner scanner = buildContext.newScanner(directory);
            scanner.setIncludes(filters);
            scanner.scan();
            
            String[] includedFiles = scanner.getIncludedFiles();
            if (includedFiles != null) {
                for (String includedFile : includedFiles) {
                    files.add(new File(scanner.getBasedir(), includedFile));
                }        
            }
        }
        return files;
    }

    public void execute() throws MojoExecutionException {
        if (getOutputDirectory() == null) {
        	return;
        }        
        if (System.getProperty("maven.apt.skip") != null) {
        	return;
        }
        
        if (!getOutputDirectory().exists()) {
            getOutputDirectory().mkdirs();
        }

        // make sure to add compileSourceRoots also during configuration build in m2e context
        if (isForTest()) {
            project.addTestCompileSourceRoot(getOutputDirectory().getAbsolutePath());
        } else {
            project.addCompileSourceRoot(getOutputDirectory().getAbsolutePath());
        }

        Set<File> sourceDirectories = getSourceDirectories();

        getLog().debug("Using build context: " + buildContext);

        StandardJavaFileManager fileManager = null;
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new MojoExecutionException("You need to run build with JDK or have tools.jar on the classpath."
                        + "If this occures during eclipse build make sure you run eclipse under JDK as well");
            }

            Set<File> files = filterFiles(sourceDirectories);
            if (files.isEmpty()) {
                getLog().debug("No Java sources found (skipping)");
                return;
            }

            fileManager = compiler.getStandardFileManager(null, null, null);
            Iterable<? extends JavaFileObject> compilationUnits1 = fileManager.getJavaFileObjectsFromFiles(files);

            String compileClassPath = buildCompileClasspath();

            String processor = buildProcessor();

            List<String> compilerOptions = buildCompilerOptions(processor, compileClassPath);

            Writer out = null;
            if (logOnlyOnError) {
                out = new StringWriter();
            }
            CompilationTask task = compiler.getTask(out, fileManager, null, compilerOptions, null, compilationUnits1);
            // Perform the compilation task.
            Boolean rv = task.call();
            if (Boolean.FALSE.equals(rv) && logOnlyOnError) {
                getLog().error(out.toString());
            }

            buildContext.refresh(getOutputDirectory());
        } catch (Exception e1) {
            getLog().error("execute error", e1);
            throw new MojoExecutionException(e1.getMessage(), e1);
            
        } finally {
            if (fileManager != null) {
                try {
                    fileManager.close();
                } catch (Exception e) {
                    getLog().warn("Unable to close fileManager", e);
                }
            }
        }
    }

    protected abstract File getOutputDirectory();
    
    @SuppressWarnings("unchecked")
    protected Set<File> getSourceDirectories() {
        File outputDirectory = getOutputDirectory();
        Set<File> directories = new HashSet<File>();        
        List<String> directoryNames = isForTest() ? project.getTestCompileSourceRoots() 
                                                  : project.getCompileSourceRoots();
        for (String name : directoryNames) {
            File file = new File(name);
            if (!file.equals(outputDirectory) && file.exists()) {
                directories.add(file);    
            }            
        }
        return directories;
    }

    protected boolean isForTest() {
        return false;
    }

    public void setBuildContext(BuildContext buildContext) {
        this.buildContext = buildContext;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public void setProcessors(String[] processors) {
        this.processors = processors;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public void setSourceEncoding(String sourceEncoding) {
        this.sourceEncoding = sourceEncoding;
    }

    public void setOptions(Map<String, String> options) {
        this.options = options;
    }

    public void setCompilerOptions(Map<String, String> compilerOptions) {
        this.compilerOptions = compilerOptions;
    }

    public void setIncludes(Set<String> includes) {
        this.includes = includes;
    }

    public void setShowWarnings(boolean showWarnings) {
        this.showWarnings = showWarnings;
    }

    public void setLogOnlyOnError(boolean logOnlyOnError) {
        this.logOnlyOnError = logOnlyOnError;
    }

    public void setPluginArtifacts(List<Artifact> pluginArtifacts) {
        this.pluginArtifacts = pluginArtifacts;
    }

}
