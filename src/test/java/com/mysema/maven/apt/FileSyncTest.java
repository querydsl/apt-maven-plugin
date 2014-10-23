package com.mysema.maven.apt;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import org.junit.Test;
import static org.junit.Assert.*;

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

    @Test
    public void sync3() throws IOException {
        Joiner joiner = Joiner.on(File.separator);
        File source = Files.createTempDir();
        File target = Files.createTempDir();
        File sourceFile1 = new File(source, joiner.join("com","mysema","querydsl","Query.java"));
        File sourceFile2 = new File(source, joiner.join("com","mysema","Entity.java"));
        File targetFile1 = new File(target, joiner.join("com","mysema","querydsl","OldQuery.java"));
        sourceFile1.getParentFile().mkdirs();
        sourceFile2.getParentFile().mkdirs();
        targetFile1.getParentFile().mkdirs();
        Files.write("abc", sourceFile1, Charsets.UTF_8);
        Files.write("def", sourceFile2, Charsets.UTF_8);
        Files.write("ghi", targetFile1, Charsets.UTF_8);

        FileSync.syncFiles(source, target);
        assertFalse(targetFile1.exists());
        assertTrue(new File(target, joiner.join("com","mysema","querydsl","Query.java")).exists());
        assertTrue(new File(target, joiner.join("com","mysema","Entity.java")).exists());
    }

}
