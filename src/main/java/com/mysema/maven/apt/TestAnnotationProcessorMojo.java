/*
 * Copyright (c) 2009 Mysema Ltd.
 * All rights reserved.
 * 
 */
package com.mysema.maven.apt;

import java.io.File;

/**
 * TestAnnotationProcessorMojo calls APT processors for code generation
 * 
 * @goal test-process
 * @phase generate-test-sources
 * @requiresDependencyResolution test
 */
public class TestAnnotationProcessorMojo extends AbstractProcessorMojo {

    /**
     * @parameter
     */
    private File outputDirectory;

    /**
     * @parameter
     */
    private File testOutputDirectory;
  
    @Override
    public File getOutputDirectory() {
        return testOutputDirectory != null ? testOutputDirectory : outputDirectory;
    }

    @Override
    protected boolean isForTest(){
        return true;
    }
    
}
