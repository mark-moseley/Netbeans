/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans;

import java.net.URL;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.zip.ZipEntry;
import java.io.*;
import java.net.MalformedURLException;
import java.security.*;
import java.security.cert.Certificate;
import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * A ProxyClassLoader capable of loading classes from a set of jar files
 * and local directories.
 *
 * @author  Petr Nejedly
 */
public class JarClassLoader extends ProxyClassLoader {
    private Source[] sources;
    /** temp copy JARs which ought to be deleted */
    private Set deadJars = null; // Set<JarFile>
    private static final boolean VERBOSE =
        Boolean.getBoolean("netbeans.classloader.verbose"); // NOI18N
    
    /** Creates new JarClassLoader.
     * Gives transitive flag as true.
     */
    public JarClassLoader (List files, ClassLoader[] parents) {
        this(files, parents, true);
    }
    
    /** Creates new JarClassLoader.
     * @since org.netbeans.core/1 > 1.6
     * @see ProxyClassLoader#ProxyClassLoader(ClassLoader[],boolean)
     */
    public JarClassLoader(List files, ClassLoader[] parents, boolean transitive) {
        super(parents, transitive);

        sources = new Source[files.size()];
        try {
            int i=0;
            for (Iterator it = files.iterator(); it.hasNext(); i++ ) {
                Object act = it.next();
                if (act instanceof File) {
                    sources[i] = new DirSource((File)act);
                } else {
                    sources[i] = new JarSource((JarFile)act);
                }
            }
        } catch (MalformedURLException exc) {
            throw new IllegalArgumentException(exc.getMessage());
        }
            
    }

    /** Allows to specify the right permissions, OneModuleClassLoader does it differently.
     */
    protected PermissionCollection getPermissions( CodeSource cs ) {           
        return Policy.getPolicy().getPermissions(cs);       
    }        
    
    
    protected Package definePackage(String name, Manifest man, URL url)
	throws IllegalArgumentException
    {
        if (man == null ) {
            return definePackage(name, null, null, null, null, null, null, null);
        }
        
	String path = name.replace('.', '/').concat("/"); // NOI18N
	Attributes spec = man.getAttributes(path);
        Attributes main = man.getMainAttributes();
	
        String specTitle = getAttr(spec, main, Name.SPECIFICATION_TITLE);
        String implTitle = getAttr(spec, main, Name.IMPLEMENTATION_TITLE);
        String specVersion = getAttr(spec, main, Name.SPECIFICATION_VERSION);
        String implVersion = getAttr(spec, main, Name.IMPLEMENTATION_VERSION);
        String specVendor = getAttr(spec, main, Name.SPECIFICATION_VENDOR);
        String implVendor = getAttr(spec, main, Name.IMPLEMENTATION_VENDOR);
        String sealed      = getAttr(spec, main, Name.SEALED);

        URL sealBase = "true".equalsIgnoreCase(sealed) ? url : null; // NOI18N
	return definePackage(name, specTitle, specVersion, specVendor,
			     implTitle, implVersion, implVendor, sealBase);
    }

    private static String getAttr(Attributes spec, Attributes main, Name name) {
        String val = null;
        if (spec != null) val = spec.getValue (name);
        if (val == null && main != null) val = main.getValue (name);
        return val;
    }

    protected Class simpleFindClass(String name, String path) {
        // look up the Sources and return a class based on their content
        for( int i=0; i<sources.length; i++ ) {
            Source src = sources[i];
            byte[] data = src.getClassData(name, path);
            if (data == null) continue;
            
            // do the enhancing
            byte[] d = PatchByteCode.patch (data);
            data = d;
            
            int j = name.lastIndexOf('.');
            String pkgName = name.substring(0, j);
            Package pkg = getPackage(pkgName);
            
            if (pkg != null) {
                // XXX full sealing check, URLClassLoader does something more
                if (pkg.isSealed() && !pkg.isSealed(src.getURL())) throw new SecurityException("sealing violation"); // NOI18N
            } else {
                Manifest man = src.getManifest();
                definePackage (pkgName, man, src.getURL());
            }

            return defineClass (name, data, 0, data.length, src.getProtectionDomain());
        } 
        return null;
    }
    // look up the jars and return a resource based on a content of jars
    protected URL findResource(String name) {
        for( int i=0; i<sources.length; i++ ) {
            URL item = sources[i].getResource(name);
            if (item != null) return item;
        }
	return null;
    }

    protected Enumeration simpleFindResources(String name) {
        Vector v = new Vector(3);
        // look up the jars and return a resource based on a content of jars

        for( int i=0; i<sources.length; i++ ) {
            URL item = sources[i].getResource(name);
            if (item != null) v.add(item);
        }
        return v.elements();
    }

    /** Try to release any JAR locks held by this classloader.
     * @see #21114
     */
    public final void releaseLocks() {
        if (deadJars != null) throw new IllegalStateException("Already had dead JARs: " + deadJars); // NOI18N
        deadJars = new HashSet(); // Set<JarFile>
        try {
            for (int i = 0; i < sources.length; i++) {
                if (sources[i] instanceof JarSource) {
                    JarFile origJar = ((JarSource)sources[i]).getJarFile();
                    File orig = new File(origJar.getName());
                    if (!orig.isFile()) {
                        // Can happen when a test module is deleted:
                        // the physical JAR has already been deleted
                        // when the module was disabled. In this case it
                        // is possible that a classloader request for something
                        // in the JAR could still come in. Does it matter?
                        // See comment in Module.cleanup.
                        continue;
                    }
                    String name = orig.getName();
                    String prefix, suffix;
                    int idx = name.lastIndexOf('.');
                    if (idx == -1) {
                        prefix = name;
                        suffix = null;
                    } else {
                        prefix = name.substring(0, idx);
                        suffix = name.substring(idx);
                    }
                    while (prefix.length() < 3) prefix += "x"; // NOI18N
                    File temp = File.createTempFile(prefix, suffix);
                    temp.deleteOnExit();
                    InputStream is = new FileInputStream(orig);
                    try {
                        OutputStream os = new FileOutputStream(temp);
                        try {
                            byte[] buf = new byte[4096];
                            int j;
                            while ((j = is.read(buf)) != -1) {
                                os.write(buf, 0, j);
                            }
                        } finally {
                            os.close();
                        }
                    } finally {
                        is.close();
                    }
                    // Don't use OPEN_DELETE even though it sounds like a good idea.
                    // Can cause real problems under 1.4; see Module.java.
                    JarFile tempJar = new JarFile(temp);
                    origJar.close();
                    forceRelease(orig);
                    deadJars.add(tempJar);
                    sources[i] = new JarSource(tempJar);
                    log("#21114: replacing " + orig + " with " + temp);
                }
            }
        } catch (IOException ioe) {
            JarClassLoader.notify(0, ioe);
        }
    }
    
    /** Release jar: locks when the classloader is shut down.
     * Should help reloading modules with changed resources.
     */
    public void destroy() {
        super.destroy();
        for (int i = 0; i < sources.length; i++) {
            if (sources[i] instanceof JarSource) {
                JarFile j = ((JarSource)sources[i]).getJarFile();
                File f = new File(j.getName());
                forceRelease(f);
            }
        }
    }
    
    /** Delete any temporary JARs we were holding on to.
     * Also close any other JARs in our list.
     */
    protected void finalize() throws Throwable {
        super.finalize();
        for (int i = 0; i < sources.length; i++) {
            if (sources[i] instanceof JarSource) {
                JarFile j = ((JarSource)sources[i]).getJarFile();
                File f = new File(j.getName());
                j.close();
                forceRelease(f);
                if (deadJars != null && deadJars.contains(j)) {
                    log("#21114: closing and deleting temporary JAR " + f);
                    if (f.isFile() && !f.delete()) {
                        log("(but failed to delete it)");
                    }
                }
            }
        }
    }
    
    /** Make sure the Java runtime's jar: URL cache is not holding
     * onto the specified file.
     * Workaround for JDK bug #4646668.
     */
    private static void forceRelease(File f) {
        if (fileCache == null || factory == null) return;
        try {
            synchronized (factory) {
                Iterator it = fileCache.values().iterator();
                while (it.hasNext()) {
                    JarFile j = (JarFile)it.next();
                    if (f.equals(new File(j.getName()))) {
                        j.close();
                        it.remove();
                        log("Removing jar: cache for " + f + " as workaround for JDK #4646668");
                    }
                }
            }
        } catch (Exception e) {
            JarClassLoader.annotate(e, 0, "Could not remove jar: cache for " + f, null, null, null);
            JarClassLoader.notify(0, e);
        }
    }
    private static Object factory = null;
    private static HashMap fileCache = null;
    static {
        try {
            Class juc = Class.forName("sun.net.www.protocol.jar.JarURLConnection"); // NOI18N
            Field factoryF = juc.getDeclaredField("factory"); // NOI18N
            factoryF.setAccessible(true);
            factory = factoryF.get(null);
            Class jff = Class.forName("sun.net.www.protocol.jar.JarFileFactory"); // NOI18N
            if (!jff.isInstance(factory)) throw new ClassCastException(factory.getClass().getName());
            Field fileCacheF = jff.getDeclaredField("fileCache"); // NOI18N
            fileCacheF.setAccessible(true);
            if (Modifier.isStatic(fileCacheF.getModifiers())) {
                // JDK 1.3.1 or 1.4 seems to have it static.
                fileCache = (HashMap)fileCacheF.get(null);
            } else {
                // But in 1.3.0 it appears to be an instance var.
                fileCache = (HashMap)fileCacheF.get(factory);
            }
            log("Workaround for JDK #4646668 active as part of IZ #21114");
        } catch (Exception e) {
            JarClassLoader.annotate(e, 0, "Workaround for JDK #4646668 as part of IZ #21114 failed", null, null, null);
            JarClassLoader.notify(0, e);
        }
    }

    abstract class Source {
        private URL url;
        private ProtectionDomain pd;
        
        public Source(URL url) {
            this.url = url;
            CodeSource cs = new CodeSource(url, new Certificate[0]);
            pd = new ProtectionDomain(cs, getPermissions(cs));
        }
        
        public final URL getURL() {
            return url;
        }
        
        public final ProtectionDomain getProtectionDomain() {
            return pd;
        }
  
        public final URL getResource(String name) {
            try {
                return doGetResource(name);
            } catch (MalformedURLException e) {
                log(e.toString());
            }
            return null;
        }
        
        protected abstract URL doGetResource(String name) throws MalformedURLException;
        
        public final byte[] getClassData(String name, String path) {
            try {
                return readClass(name, path);
            } catch (IOException e) {
                log(e.toString());
            }
            return null;
        }

        protected abstract byte[] readClass(String name, String path) throws IOException;

        public Manifest getManifest() {
            return null;
        }
    }

    class JarSource extends Source {
        JarFile src;
        
        public JarSource(JarFile file) throws MalformedURLException {
            super(new URL("file:" + file.getName()));
            src = file;
        }

        public Manifest getManifest() {
            try {
                return src.getManifest();
            } catch (IOException e) {
                return null;
            }
        }
        
        JarFile getJarFile() {
            return src;
        }
        
        protected URL doGetResource(String name) throws MalformedURLException {
            ZipEntry ze = src.getEntry(name);
            if (VERBOSE) {
                if (ze != null)
                    System.err.println("Loading " + name + " from " + src.getName()); // NOI18N
            }
            return ze == null ? null : new URL("jar:file:" + src.getName() + "!/" + ze.getName()); // NOI18N
        }
        
        protected byte[] readClass(String name, String path) throws IOException {
            ZipEntry ze = src.getEntry(path);
            if (ze == null) return null;
            if (VERBOSE) {
                System.err.println("Loading " + path + " from " + src.getName()); // NOI18N
            }
            
            int len = (int)ze.getSize();
            byte[] data = new byte[len];
            InputStream is = src.getInputStream(ze);
            int count = 0;
            while (count < len) {
                count += is.read(data, count, len-count);
            }
            return data;
        }
    }

    class DirSource extends Source {
        File dir;
        
        public DirSource(File file) throws MalformedURLException {
            super(file.toURL());
            dir = file;
        }

        protected URL doGetResource(String name) throws MalformedURLException {
            File resFile = new File(dir, name);
            return resFile.exists() ? resFile.toURL() : null;
        }
        
        protected byte[] readClass(String name, String path) throws IOException {
            File clsFile = new File(dir, path.replace('/', File.separatorChar));
            if (!clsFile.exists()) return null;
            
            int len = (int)clsFile.length();
            byte[] data = new byte[len];
            InputStream is = new FileInputStream(clsFile);
            int count = 0;
            while (count < len) {
                count += is.read(data, count, len-count);
            }
            return data;
        }
        
    }
    
    
    //
    // ErrorManager's methods
    // (do not want to depend on ErrorManager however)
    //
    
    static void log (String msg) {
        if ("0".equals(System.getProperty("org.netbeans.core.modules"))) { // NOI18N
            System.err.println(msg);
        }
    }
    
    static Throwable annotate (
        Throwable t, int x, String s, Object o1, Object o2, Object o3
    ) {
        System.err.println("annotated: " + t.getMessage () + " - " + s); // NOI18N
        return t;
    }
    
    static void notify (int x, Throwable t) {
        t.printStackTrace();
    }
}
