package com.mysema.query.apt.plugin;

import java.io.File;

/**
 * TestAnnotationProcessorMojo calls APT processors for code generation
 * 
 * @goal test-process
 * @phase generate-sources
 * @requiresDependencyResolution test
 */
public class TestAnnotationProcessorMojo extends AbstractProcessorMojo {

  /**
   * @parameter expression="${project.build.testSourceDirectory}" required=true
   */
  protected File sourceDirectory;
    

    @Override
    protected File getSourceDirectory() {
        return sourceDirectory;
    }

    @Override
    protected boolean isForTest(){
        return true;
    }
    
}
