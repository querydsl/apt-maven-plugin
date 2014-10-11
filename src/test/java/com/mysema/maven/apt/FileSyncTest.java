package com.mysema.maven.apt;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileSyncTest {

    @Test
    public void sync1() throws IOException {
        File source = Files.createTempDir();
        File sourceFile = new File(source, "inSource");
        sourceFile.createNewFile();
        File sourceFolder = new File(source, "inSourceFolder");
        sourceFolder.mkdir();
        File sourceFile2 = new File(sourceFolder, "inSource");
        sourceFile2.createNewFile();

        File target = Files.createTempDir();
        File targetFile = new File(target, "inTarget");
        targetFile.createNewFile();

        FileSync.syncFiles(source, target);
        assertTrue(new File(target, "inSource").exists());
        assertTrue(new File(target, "inSourceFolder" + File.separator + "inSource").exists());
        assertFalse(targetFile.exists());

        FileSync.syncFiles(source, target);
    }

    @Test
    public void sync2() throws IOException {
        File source = Files.createTempDir();
        File sourceFile = new File(source, "file");
        Files.write("abc", sourceFile, Charsets.UTF_8);
        File target = Files.createTempDir();
        File targetFile = new File(target, "file");
        Files.write("abc", targetFile, Charsets.UTF_8);
        long modified = targetFile.lastModified();

        FileSync.syncFiles(source, target);
        assertEquals(modified, targetFile.lastModified());
    }

}
