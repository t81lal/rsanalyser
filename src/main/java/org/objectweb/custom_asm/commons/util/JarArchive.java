package org.objectweb.custom_asm.commons.util;

import org.objectweb.custom_asm.ClassReader;
import org.objectweb.custom_asm.ClassWriter;
import org.objectweb.custom_asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * @author Tyler Sedlar
 */
public class JarArchive {

    private final Map<String, ClassNode> nodes = new HashMap<>();

    private final File file;
    private Manifest manifest;

    public JarArchive(File file) {
        this.file = file;
    }

    public Map<String, ClassNode> build() {
        if (!nodes.isEmpty())
            return nodes;
        try {
            JarFile jar = new JarFile(file);
            manifest = jar.getManifest();
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    ClassNode cn = new ClassNode();
                    ClassReader reader = new ClassReader(jar.getInputStream(entry));
                    reader.accept(cn, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                    nodes.put(name.replace(".class", ""), cn);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error building classes (" + file.getName() + "): ", e.getCause());
        }
        return nodes;
    }

    public void write(File target) {
        try (JarOutputStream output = (manifest != null ? new JarOutputStream(new FileOutputStream(target), manifest) :
                new JarOutputStream(new FileOutputStream(target)))) {
            for (Map.Entry<String, ClassNode> entry : build().entrySet()) {
                output.putNextEntry(new JarEntry(entry.getKey().replaceAll("\\.", "/") + ".class"));
                ClassWriter writer = new ClassWriter(0);
                entry.getValue().accept(writer);
                output.write(writer.toByteArray());
                output.closeEntry();
            }
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write() {
        write(file);
    }
}
