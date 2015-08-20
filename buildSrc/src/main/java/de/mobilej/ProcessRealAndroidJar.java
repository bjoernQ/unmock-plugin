/*
   Copyright (C) 2015 Björn Quentin

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package de.mobilej;

import org.gradle.api.logging.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;

/**
 * Here the heavy lifting happens.
 * <p/>
 * We take the all-android.jar and copy the wanted classes to the destination.
 * <p/>
 * The copied classes will be non-final/non-private and also all methods contained.
 * <p/>
 * Additionally all native methods will be changed to delegate to de.mobilej.ABridge.callXXX methods
 * for easier mocking.
 */
public class ProcessRealAndroidJar {


    public static boolean isUpToDate(String allAndroidSourceUrl, String downloadTo, String[] keepClasses, String[] renameClasses, String destFile,
                                     String intermediatesDir, File buildFile, Logger logger)
            throws Exception {

        final File intermediates = new File(intermediatesDir);
        File out = new File(intermediates, "unmock_work");
        if (out.exists() && buildFile.lastModified() < out.lastModified()) {
            return true;
        }

        return false;
    }

    public static void process(String allAndroidSourceUrl, String downloadTo, String[] keepClasses, String[] renameClasses, String destFile,
                               String intermediatesDir, File buildFile, Logger logger)
            throws Exception {

        if (allAndroidSourceUrl == null) {
            throw new IllegalArgumentException("No URL specified to download the full Android jar.");
        }

        List<ClassMapping> classesToMap = parseClassesToMap(renameClasses, logger);

        List<String> keepClassesList = new ArrayList<>(Arrays.asList(keepClasses));
        for (ClassMapping mapping : classesToMap) {
            keepClassesList.add(mapping.from);
        }
        keepClasses = keepClassesList.toArray(new String[keepClassesList.size()]);

        // DOWNLOAD the wished version
        // e.g. from https://oss.sonatype.org/content/groups/public/org/robolectric/android-all/
        // e.g.:
        // https://oss.sonatype.org/content/groups/public/org/robolectric/android-all/5.0.0_r2-robolectric-0/android-all-5.0.0_r2-robolectric-0.jar

        final File intermediates = new File(intermediatesDir);
        final File tmpDir = new File(downloadTo == null ? System.getProperty("java.io.tmpdir") : downloadTo);
        File allAndroidFile = new File(tmpDir,
                allAndroidSourceUrl.replace("/", "_").replace(":", "_"));

        if (!allAndroidFile.exists()) {
            URL website = new URL(allAndroidSourceUrl);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(allAndroidFile);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }

        File out = new File(intermediates, "unmock_work");
        if (out.exists() && buildFile.lastModified() < out.lastModified()) {
            return;
        }

        delete(out);
        out.mkdirs();

        ArrayList<String> clazzNames = findAllClazzesIn(
                allAndroidFile.getAbsolutePath());

        ClassPool pool = new ClassPool(null);
        pool.appendSystemPath();

        pool.insertClassPath(allAndroidFile.getAbsolutePath());

        createHelperClasses(out, pool);

        for (String clazzName : clazzNames) {
            CtClass clazz = pool.get(clazzName);

            boolean keep = false;
            for (String keepClass : keepClasses) {
                if (keepClass.startsWith("-")) {
                    String pureKeepClassName = keepClass.substring(1);
                    if (clazz.getName().equals(pureKeepClassName)) {
                        keep = true;
                        break;
                    } else if (clazz.getName().startsWith(pureKeepClassName + "$")) {
                        keep = true;
                        break;
                    }


                } else {
                    if (clazz.getName().startsWith(keepClass)) {
                        keep = true;
                        break;
                    }
                }
            }

            if (!keep) {
                continue;
            }

            try {
                process(clazz, classesToMap);
            } catch (Exception e) {
                logger.error("-> unable to process", e);
            }

            clazz.writeFile(out.getAbsolutePath());
        }

        createJarArchive(destFile,
                out.getAbsolutePath());

    }

    private static List<ClassMapping> parseClassesToMap(String[] renameClasses, Logger logger) {
        ArrayList<ClassMapping> result = new ArrayList<>();

        if (renameClasses == null) {
            return result;
        }

        for (String classRenaming : renameClasses) {
            int indexOfEquals = classRenaming.indexOf("=");
            if (indexOfEquals > 0 && indexOfEquals < classRenaming.length()) {
                String from = classRenaming.substring(0, indexOfEquals);
                String to = classRenaming.substring(indexOfEquals + 1);
                result.add(new ClassMapping(from, to));

            } else {
                logger.error("Unparseable mapping:" + classRenaming);
            }
        }

        return result;
    }

    public static boolean createJarArchive(String outfile, String srcFolder) throws Exception {

        FileOutputStream dest = new FileOutputStream(new File(outfile));

        JarOutputStream out = new JarOutputStream(new BufferedOutputStream(dest));

        addToJar(out, new File(srcFolder), new File(srcFolder));

        out.flush();
        out.close();

        return true;
    }

    private static void addToJar(JarOutputStream out, File folder, File root) throws Exception {
        File[] files = folder.listFiles();

        if (files == null) {
            return;
        }

        for (File f : files) {
            if (f.isDirectory()) {
                addToJar(out, f, root);
            } else {
                addJarEntry(out, f, root);
            }
        }
    }

    private static BufferedInputStream addJarEntry(ZipOutputStream out, File f, File root)
            throws IOException {
        int BUFFER = 2048;
        byte data[] = new byte[BUFFER];

        BufferedInputStream origin;
        FileInputStream fi = new FileInputStream(f);
        origin = new BufferedInputStream(fi, BUFFER);
        String name = f.getCanonicalPath().substring(root.getCanonicalPath().length() + 1).replace('\\', '/');
        JarEntry entry = new JarEntry(name);
        out.putNextEntry(entry);
        int count;
        while ((count = origin.read(data, 0, BUFFER)) != -1) {
            out.write(data, 0, count);
            out.flush();
        }

        origin.close();

        return origin;
    }

    private static void createHelperClasses(File out, ClassPool pool)
            throws CannotCompileException, IOException {
        CtClass bridge = pool.makeClass("de.mobilej.ABridge");

        bridge.addMethod(CtMethod.make(
                "public static Object callObject(String signature, Object thiz, Object[] args){" +
                        "return (Object)null;" +
                        "}", bridge));

        bridge.addMethod(CtMethod.make(
                "public static int callInt(String signature, Object thiz, Object[] args){" +
                        "return 0;" +
                        "}", bridge));

        bridge.addMethod(CtMethod.make(
                "public static long callLong(String signature, Object thiz, Object[] args){" +
                        "return 0L;" +
                        "}", bridge));

        bridge.addMethod(CtMethod.make(
                "public static boolean callBoolean(String signature, Object thiz, Object[] args){" +
                        "return false;" +
                        "}", bridge));

        bridge.addMethod(CtMethod.make(
                "public static void callVoid(String signature, Object thiz, Object[] args){" +
                        "return;" +
                        "}", bridge));

        bridge.writeFile(out.getAbsolutePath());
    }

    private static ArrayList<String> findAllClazzesIn(String file) throws IOException {
        ArrayList<String> res = new ArrayList<String>();

        ZipFile f = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = f.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".class")) {
                res.add(entry.getName().replace(".class", "").replace("/", "."));
            }

        }

        return res;
    }

    private static void process(CtClass clazz, List<ClassMapping> classMappings) throws Exception {

        if (clazz.isInterface()) {
            return;
        }

        clazz.defrost();

        clazz.setModifiers(clazz.getModifiers() | Modifier.PUBLIC);
        clazz.setModifiers(clazz.getModifiers() & ~Modifier.FINAL);
        clazz.setModifiers(clazz.getModifiers() & ~Modifier.PRIVATE);
        clazz.setModifiers(clazz.getModifiers() & ~Modifier.PROTECTED);

        CtMethod[] methods = clazz.getDeclaredMethods();

        for (CtMethod m : methods) {

            // we change native to normal method but need to
            // delegate
            if ((m.getModifiers() & Modifier.NATIVE) == Modifier.NATIVE) {
                String signature = m.getLongName();
                String thiz = "$0";
                if ((m.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
                    thiz = "null";
                }

                String retType = m.getReturnType().getName();

                switch (retType) {
                    case "void":
                        m.setBody("{ de.mobilej.ABridge.callVoid(\"" + signature + "\", " + thiz
                                + ", $args); } ");
                        break;
                    case "boolean":
                        m.setBody(
                                "{ return de.mobilej.ABridge.callBoolean(\"" + signature + "\", "
                                        + thiz
                                        + ", $args); } ");
                        break;
                    case "int":
                        m.setBody(
                                "{ return de.mobilej.ABridge.callInt(\"" + signature + "\", " + thiz
                                        + ", $args); } ");
                        break;
                    case "long":
                        m.setBody("{ return de.mobilej.ABridge.callLong(\"" + signature + "\", "
                                + thiz
                                + ", $args); } ");
                        break;
                    default:
                        m.setBody(
                                "{ return ($r)de.mobilej.ABridge.callObject(\"" + signature + "\","
                                        + thiz + ", $args); } ");
                        break;
                }
            }

            m.setModifiers(m.getModifiers() | Modifier.PUBLIC);
            m.setModifiers(m.getModifiers() & ~Modifier.FINAL);
            m.setModifiers(m.getModifiers() & ~Modifier.PRIVATE);
            m.setModifiers(m.getModifiers() & ~Modifier.PROTECTED);
            m.setModifiers(m.getModifiers() & ~Modifier.NATIVE);

        }


        for (ClassMapping mapping : classMappings) {
            clazz.replaceClassName(mapping.from, mapping.to);
        }
    }

    public static boolean delete(File file) {
        File[] flist = null;

        if (file == null) {
            return false;
        }

        if (file.isFile()) {
            return file.delete();
        }

        if (!file.isDirectory()) {
            return false;
        }

        flist = file.listFiles();
        if (flist != null && flist.length > 0) {
            for (File f : flist) {
                if (!delete(f)) {
                    return false;
                }
            }
        }

        return file.delete();
    }

    public static class ClassMapping {
        public final String from;
        public final String to;

        public ClassMapping(String from, String to) {
            this.from = from;
            this.to = to;
        }
    }
}
