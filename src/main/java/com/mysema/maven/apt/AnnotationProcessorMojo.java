/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.maven.apt;

import java.io.File;

/**
 * AnnotationProcessorMojo calls APT processors for code generation
 * 
 * @goal process
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class AnnotationProcessorMojo extends AbstractProcessorMojo {

    /**
     * @parameter expression="${project.build.sourceDirectory}" required=true
     */
    protected File sourceDirectory;
  

    @Override
    protected File getSourceDirectory() {
        return sourceDirectory;
    }
    
}
