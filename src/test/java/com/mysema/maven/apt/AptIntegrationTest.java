package com.mysema.maven.apt;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;

public class AptIntegrationTest {

    @Test
    public void exportSources() throws VerificationException {
        String projectPath = getProjectPath("project-to-test");
        runProject(projectPath, Arrays.asList("clean", "generate-sources"));
        assertFileExists(projectPath + "/target/generated-sources/java/com/example/QEntity.java");
    }

    @Test
    public void exportTestSources() throws VerificationException {
        String projectPath = getProjectPath("project-to-test");
        runProject(projectPath, Arrays.asList("clean", "generate-test-sources"));
        assertFileExists(projectPath + "/target/generated-test-sources/java/com/example/QEntity2.java");
    }

    private void runProject(String path, List<String> goals) throws VerificationException {
        Verifier verifier = new Verifier(path);
        verifier.executeGoals(goals);
        verifier.verifyErrorFreeLog();
        verifier.resetStreams();
    }

    private String getProjectPath(String project) {
        return new File("target/test-classes/" + project).getAbsolutePath();
    }

    private void assertFileExists(String path) {
        assertTrue(new File(path).exists());
    }

}