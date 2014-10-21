package com.mysema.maven.apt;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

import com.mysema.codegen.CodegenException;

public final class ClassPathUtils {
    
    public static List<String> getClassPath(URLClassLoader cl) {
        try {
            List<String> paths = new ArrayList<String>();
            if (cl.getURLs().length == 1 && cl.getURLs()[0].getPath().contains("surefirebooter")) {
                // extract MANIFEST.MF Class-Path entry, since the Java Compiler doesn't handle
                // manifest only jars in the classpath correctly
                URL url = cl.findResource("META-INF/MANIFEST.MF");
                Manifest manifest = new Manifest(url.openStream());
                String classpath = (String) manifest.getMainAttributes().getValue("Class-Path");
                for (String entry : classpath.split(" ")) {
                    URL entryUrl = new URL(entry);
                    String decodedPath = URLDecoder.decode(entryUrl.getPath(), "UTF-8");
                    paths.add(new File(decodedPath).getAbsolutePath());
                }
            } else {
                for (URL url : cl.getURLs()) {
                    String decodedPath = URLDecoder.decode(url.getPath(), "UTF-8");
                    paths.add(new File(decodedPath).getAbsolutePath());
                }    
            }            
            return paths;
        } catch (UnsupportedEncodingException e) {
            throw new CodegenException(e);
        } catch (IOException e) {
            throw new CodegenException(e);
        }
    }

    private ClassPathUtils() {}
    
}
