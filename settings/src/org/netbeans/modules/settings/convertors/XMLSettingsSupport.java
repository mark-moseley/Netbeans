/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.settings.convertors;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

import org.openide.ErrorManager;
import org.openide.filesystems.*;
import org.openide.modules.ModuleInfo;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/** Class provides support for storing and reading session settings.
 * !!! KEEP CODE SYNCHRONOUS WITH org.netbeans.core.projects.XMLSettingsSupport !!!
 *
 * @see SerialDataConvertor
 * @author  Jan Pokorsky
 */
final class XMLSettingsSupport {
    
    public static final String INSTANCE_DTD_ID = "-//NetBeans//DTD Session settings 1.0//EN"; // NOI18N
    public static final String INSTANCE_DTD_WWW = "http://www.netbeans.org/dtds/sessionsettings-1_0.dtd"; // NOI18N
    public static final String INSTANCE_DTD_LOCAL = "/org/openide/resources/sessionsettings-1_0.dtd"; // NOI18N
    /** File extension for xml settings. */
    public static final String XML_EXT = "settings"; //NOI18N
    
    /** Store instanceof elements.
     * @param classes everything what class extends or implements
     * @param pw output
     */
    private static void storeInstanceOf (Set classes, PrintWriter pw) throws IOException {
        Iterator it = classes.iterator();
        Class clazz;
        while (it.hasNext()) {
            clazz = (Class) it.next();
            pw.print("    <instanceof class=\""); // NOI18N
            pw.print(clazz.getName());
            pw.println("\"/>"); // NOI18N
        }
    }
    
    /** Store settings version 1.0
     * @param inst settings instance
     * @param os output
     */
    public static void storeToXML10 (Object inst, Writer os, ModuleInfo mi)
    throws IOException {
        PrintWriter pw = new PrintWriter (os);
        
        pw.println ("<?xml version=\"1.0\"?>"); // NOI18N
        pw.print   ("<!DOCTYPE settings PUBLIC \""); pw.print(INSTANCE_DTD_ID); // NOI18N
            pw.print("\" \""); pw.print(INSTANCE_DTD_WWW); pw.println("\">"); // NOI18N
        pw.println ("<settings version=\"1.0\">"); // NOI18N
        storeModule(mi, pw);
        storeInstanceOf(getSuperClasses(inst.getClass(), null), pw);
        // default storage has been implemented by serialization
        storeSerialData(inst, pw);
        
        pw.println ("</settings>"); // NOI18N
        pw.flush ();
    }
    
    /** Store a default instance. Ensure copatibility for settings declared in
     * a manifest.
     * @param clazz class of instance
     * @param os output
     */
    private static void storeToXML10 (Class clazz, Writer os, ModuleInfo mi)
    throws IOException {
        
        PrintWriter pw = new PrintWriter (os);
        pw.println ("<?xml version=\"1.0\"?>"); // NOI18N
        pw.print   ("<!DOCTYPE settings PUBLIC \""); pw.print(INSTANCE_DTD_ID); // NOI18N
            pw.print("\" \""); pw.print(INSTANCE_DTD_WWW); pw.println("\">"); // NOI18N
        pw.println ("<settings version=\"1.0\">"); // NOI18N
        storeModule(mi, pw);
        storeInstanceOf(getSuperClasses(clazz, null), pw);
        pw.print   ("    <instance class=\""); pw.print(clazz.getName()); pw.println("\"/>"); // NOI18N
        pw.println ("</settings>"); // NOI18N
        pw.flush ();
    }
    
    private static void storeModule(ModuleInfo mi, PrintWriter pw)
    throws IOException {
        if (mi == null) return;
        
        String modulName = mi.getCodeName();
        SpecificationVersion spec = mi.getSpecificationVersion();
        pw.print("    <module"); // NOI18N
        if (modulName != null && modulName.length() != 0) {
            pw.print(" name=\""); pw.print(modulName); pw.print('"');// NOI18N
        }
        if (spec != null) {
            pw.print(" spec=\""); pw.print(spec.toString()); pw.print('"');// NOI18N
        }
        pw.println("/>"); // NOI18N
    }
    
    
    /** This object output stream subclass is used for storing InstanceDataObject.
     * More details in bug #15563
     */
    private static class SpecialObjectOutputStream extends org.openide.util.io.NbObjectOutputStream {
        /** Is the stream expecting the first object in stream? */
        private boolean first;
        
        public SpecialObjectOutputStream(OutputStream os) throws IOException {
            super (os);
            first = true;
        }

        /** Check if the first object in the stream is <CODE>null</CODE>.
         * If so, throw InvalidObjectException.
         */
        public Object replaceObject (Object obj) throws IOException {
            if (first) {
                if (obj == null)
                    // Object doesn't want to be serialized.
                    throw new NotSerializableException();
                first = false;
            }
            return super.replaceObject(obj);
        }
        

    }
    
    /** Stream allowing upgrade to a new class inside the origin .settings file
     */
    private static class SpecialObjectInputStream extends java.io.ObjectInputStream {
        
        public SpecialObjectInputStream(InputStream is) throws IOException {
            super(is);
            try {
                enableResolveObject (true);
            } catch (SecurityException ex) {
                throw new IOException (ex.toString ());
            }
        }

        /* Uses NetBeans module classloader to load the class.
         * @param v description of the class to load
         */
        protected Class resolveClass(ObjectStreamClass v) throws IOException, ClassNotFoundException {
            ClassLoader cl = getNBClassLoader();
            try {
                return Class.forName(v.getName(), false, cl);
            } catch (ClassNotFoundException cnfe) {
                String msg = "Offending classloader: " + cl; // NOI18N
                ErrorManager.getDefault ().annotate(cnfe, ErrorManager.INFORMATIONAL, msg, null, null, null);
                throw cnfe;
            }
        }
        
        protected Object resolveObject(Object obj) throws IOException {
            Object o = super.resolveObject(obj);
            // #30305 - prevent JDK 1.3.x bug in deserialization of URL
            if (System.getProperty("java.version").startsWith("1.3")) {
                if (o instanceof java.net.URL) {
                    java.net.URL u = (java.net.URL)o;
                    try {
                        // The URL.query and URL.path are empty after deserialization.
                        // Recreating URL instance is easy way how to workaround it.
                        o = new java.net.URL(u.getProtocol(), u.getHost(), u.getPort(), u.getFile());
                    } catch (java.net.MalformedURLException ex) {
                        // should not happen. can be ignored. original object is returned in this case
                    }
                }
            }
            return o;
        }

        /** use Utilities.translate to try to upgrade to new setting's class.
         * If the old class exists the origin descriptor is used and upgrade is
         * postponed to readResolve;
         * otherwise the same implementation like NbObjectInputStream.readClassDescriptor
         */
        protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
            ObjectStreamClass ose = super.readClassDescriptor();

            String name = ose.getName();
            String newN = org.openide.util.Utilities.translate(name);

            if (name == newN) {
                // no translation
                return ose;
            }

            ClassLoader cl = getNBClassLoader();
            try {
                Class origCl = Class.forName(name, false, cl);
                // translation postponed to readResolve
                return ObjectStreamClass.lookup(origCl);
            } catch (ClassNotFoundException ex) {
                // ok look up new descriptor
            }
            
            Class clazz = Class.forName(newN, false, cl);
            ObjectStreamClass newOse = ObjectStreamClass.lookup(clazz);

            return newOse;
        }
    
        /** Lazy create default NB classloader for use during deserialization. */
        private static ClassLoader getNBClassLoader() {
            ClassLoader c = (ClassLoader) org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);
            return c != null ? c : ClassLoader.getSystemClassLoader();
        }
        
    }
    
    
    // enlarged to not need do the test for negative byte values
    private final static char[] HEXDIGITS = {'0', '1', '2', '3', '4', '5', '6', '7',
                                             '8', '9', 'A', 'B', 'C', 'D', 'E', 'F',
                                             '0', '1', '2', '3', '4', '5', '6', '7'};
    
    private static final int INDENT = 8;
    private static final int BLOCK = 100;
    private static final int BUFFSIZE = INDENT + BLOCK;
    private static void storeSerialData (Object inst, PrintWriter pw) throws IOException {
        pw.print ("    <serialdata class=\""); pw.print(inst.getClass().getName()); pw.println("\">"); // NOI18N

        ByteArrayOutputStream baos = new ByteArrayOutputStream (1024);
        ObjectOutput oo = new SpecialObjectOutputStream (baos);
        oo.writeObject (inst);
        byte[] bdata = baos.toByteArray ();
        
        char[] cdata = new char[BUFFSIZE];
        for (int i=0; i < INDENT; i++ ) cdata[i] = ' ';
        
        int i = 0; // byte array pointer
        int j; // char array pointer
        int blen = bdata.length;
        
        while (i < blen) {
            int mark = INDENT + Math.min( 2*(blen-i), BLOCK );
            for (j=INDENT; j < mark; j += 2) {
                int b = ((int)bdata[i++]) + 256;
                cdata[j]   = HEXDIGITS[b >> 4];
                cdata[j+1] = HEXDIGITS[b & 15];
            }
            pw.write(cdata, 0, j);
            pw.println();
        }
        pw.println ("    </serialdata>"); // NOI18N
        pw.flush();
    }
    
    /** Get everything what class extends or implements. */
    private static Set getSuperClasses(Class clazz, Set classes) {
        if (classes == null) {
            classes = new HashSet();
        }
        
        if (clazz == null || !classes.add(clazz)) {
            return classes;
        }
        
        Class[] cs = clazz.getInterfaces();
        for (int i = 0; i < cs.length; i++) {
            getSuperClasses(cs[i], classes);
        }
        
        return getSuperClasses(clazz.getSuperclass(), classes);
    }
    
    /** Class must be subclass of org.openide.ServiceType. */
    private static Class getServiceTypeClass(Class type) {
        if (!org.openide.ServiceType.class.isAssignableFrom(type))
            throw new IllegalArgumentException();
        // finds direct subclass of service type
        while (type.getSuperclass () != org.openide.ServiceType.class) {
            type = type.getSuperclass();
        }
        return type;
    }
    
    /** Settings parser. */
    final static class SettingsRecognizer extends org.xml.sax.helpers.DefaultHandler {
        
        private static final String ELM_SETTING = "settings"; // NOI18N
        private static final String ATR_SETTING_VERSION = "version"; // NOI18N
        
        private static final String ELM_MODULE = "module"; // NOI18N
        private static final String ATR_MODULE_NAME = "name"; // NOI18N
        private static final String ATR_MODULE_SPEC = "spec"; // NOI18N
        private static final String ATR_MODULE_IMPL = "impl"; // NOI18N
        
        private static final String ELM_INSTANCE = "instance"; // NOI18N
        private static final String ATR_INSTANCE_CLASS = "class"; // NOI18N
        private static final String ATR_INSTANCE_METHOD = "method"; // NOI18N
        
        private static final String ELM_INSTANCEOF = "instanceof"; // NOI18N
        private static final String ATR_INSTANCEOF_CLASS = "class"; // NOI18N
        
        private static final String ELM_SERIALDATA = "serialdata"; // NOI18N
        private static final String ATR_SERIALDATA_CLASS = "class"; // NOI18N
        
        //private static final String VERSION = "1.0"; // NOI18N
        
        private boolean header;
        private Stack stack;
        
        private String version;
        private String instanceClass;
        private String instanceMethod;
        private Set instanceOf = new HashSet();
        
        private byte[] serialdata;
        private CharArrayWriter chaos = null;
        
        private String codeName;
        private String codeNameBase;
        private int codeNameRelease;
        private SpecificationVersion moduleSpec;
        private String moduleImpl;
        /** file with stored settings */
        private final FileObject source;
        
        /** XML handler recognizing settings.
         * @param header if true read just elements instanceof, module and attr classname.
         * @param source file with stored settings
         */
        public SettingsRecognizer (boolean header, FileObject source) {
            this.header = header;
            this.source = source;
        }
        
        public boolean isAllRead() {
            return !header;
        }
        
        public void setAllRead(boolean all) {
            if (!header) return;
            header = all;
        }
        
        public String getSettingsVerison() {
            return version;
        }
        
        public String getCodeName() {
            return codeName;
        }
        
        public String getCodeNameBase() {
            return codeNameBase;
        }
        
        public int getCodeNameRelease() {
            return codeNameRelease;
        }
        
        public SpecificationVersion getSpecificationVersion() {
            return moduleSpec;
        }
        
        public String getModuleImpl() {
            return moduleImpl;
        }
        
        /** Set of names. */
        public Set getInstanceOf() {
            return instanceOf;
        }
        
        /** Method attribute from the instance element. */
        public String getMethodName() {
            return instanceMethod;
        }
        
        /** Serialized instance, can be null. */
        private  InputStream getSerializedInstance() {
            if (serialdata == null) return null;
            return new ByteArrayInputStream(serialdata);
        }
        
        public org.xml.sax.InputSource resolveEntity(String publicId, String systemId)
        throws SAXException {
            if (INSTANCE_DTD_ID.equals (publicId)) {
                return new org.xml.sax.InputSource (new ByteArrayInputStream (new byte[0]));
            } else {
                return null; // i.e. follow advice of systemID
            }
        }
        
        public void characters(char[] values, int start, int length) throws SAXException {
            if (header) return;
            String element = (String) stack.peek();
            if (ELM_SERIALDATA.equals(element)) {
                // [PENDING] should be optimized to do not read all chars to memory
                if (chaos == null) chaos = new CharArrayWriter(length);
                chaos.write(values, start, length);
            }
        }
        
        public void startElement(String uri, String localName, String qName, Attributes attribs) throws SAXException {
            stack.push(qName);
            if (ELM_SETTING.equals(qName)) {
                version = attribs.getValue(ATR_SETTING_VERSION);
            } else if (ELM_MODULE.equals(qName)) {
                codeName = attribs.getValue(ATR_MODULE_NAME);
                resolveModuleElm(codeName);
                moduleImpl = attribs.getValue(ATR_MODULE_IMPL);
                try {
                    String spec = attribs.getValue(ATR_MODULE_SPEC);
                    moduleSpec = spec == null ? null : new SpecificationVersion(spec);
                } catch (NumberFormatException nfe) {
                    throw new SAXException(nfe);
                }
            } else if (ELM_INSTANCEOF.equals(qName)) {
                instanceOf.add(org.openide.util.Utilities.translate(
                    attribs.getValue(ATR_INSTANCEOF_CLASS)));
            } else if (ELM_INSTANCE.equals(qName)) {
                instanceClass = attribs.getValue(ATR_INSTANCE_CLASS);
                instanceClass = org.openide.util.Utilities.translate(instanceClass);
                instanceMethod = attribs.getValue(ATR_INSTANCE_METHOD);
            } else if (ELM_SERIALDATA.equals(qName)) {
                instanceClass = attribs.getValue(ATR_SERIALDATA_CLASS);
                instanceClass = org.openide.util.Utilities.translate(instanceClass);
                if (header) throw new StopSAXException();
            }
        }
        
        /** reade codenamebase + revision */
        private void resolveModuleElm (String codeName) {
            if (codeName != null) {
                int slash = codeName.indexOf ("/"); // NOI18N
                if (slash == -1) {
                    codeNameBase = codeName;
                    codeNameRelease = -1;
                } else {
                    codeNameBase = codeName.substring (0, slash);
                    try {
                        codeNameRelease = Integer.parseInt(codeName.substring(slash + 1));
                    } catch (NumberFormatException ex) {
                        ErrorManager emgr = ErrorManager.getDefault();
                        emgr.annotate(ex, "Content: \n" + getFileContent(source)); // NOI18N
                        emgr.annotate(ex, "Source: " + source); // NOI18N
                        emgr.notify(ErrorManager.INFORMATIONAL, ex);
                        codeNameRelease = -1;
                    }
                }
            } else {
                codeNameBase = null;
                codeNameRelease = -1;
            }
        }
        
        public void endElement(String uri, String localName, String qName) throws SAXException {
            //if (header) return;
            String element = (String) stack.pop();
            if (ELM_SERIALDATA.equals(element)) {
                if (chaos != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(chaos.size() >> 1);
                    try {
                        chars2Bytes(baos, chaos.toCharArray(), 0, chaos.size());
                        serialdata = baos.toByteArray();
                    } catch (IOException ex) {
                        ErrorManager.getDefault().notify(
                            ErrorManager.WARNING, ex
                        );
                    } finally {
                        try {
                            baos.close();
                        } catch (IOException ex) {
                            // doesn't matter
                        }
                    }
                }
            }
       }
       
        /** Tries to deserialize instance saved in is.
         * @param is    stream with stored object, can be null
         * @return deserialized object or null
         */
        private Object readSerial(InputStream is) throws IOException, ClassNotFoundException {
            if (is == null) return null;
            try {
                ObjectInput oi = new SpecialObjectInputStream (is);
                try {
                    Object o = oi.readObject ();
                    return o;
                } finally {
                    oi.close();
                }
            } catch (IOException ex) {
                ErrorManager emgr = ErrorManager.getDefault();
                emgr.annotate(ex, "Content: \n" + getFileContent(source)); // NOI18N
                emgr.annotate(ex, "Source: " + source); // NOI18N
                emgr.annotate(ex, "Cannot read class: " + instanceClass); // NOI18N
                throw ex;
            } catch (ClassNotFoundException ex) {
                ErrorManager emgr = ErrorManager.getDefault();
                emgr.annotate(ex, "Content: \n" + getFileContent(source)); // NOI18N
                emgr.annotate(ex, "Source: " + source); // NOI18N
                throw ex;
            }
        }
        
        /** Create an instance.
         * @return the instance of type {@link #instanceClass}
         * @exception IOException if an I/O error occured
         * @exception ClassNotFoundException if a class was not found
         */
        public Object instanceCreate() throws java.io.IOException, ClassNotFoundException {
            Object inst = null;
            
            // deserialize
            inst = readSerial(getSerializedInstance());

            // default instance
            if (inst == null) {
                if (instanceMethod != null) {
                    inst = createFromMethod(instanceClass, instanceMethod);
                } else {
                    // use default constructor
                    Class clazz = instanceClass();
                    if (SharedClassObject.class.isAssignableFrom(clazz)) {
                        inst = SharedClassObject.findObject(clazz, false);
                        if (null != inst) {
                            // instance already exists -> reset it to defaults
                            try {
                                Method method = SharedClassObject.class.getDeclaredMethod("reset", new Class[0]); // NOI18N
                                method.setAccessible(true);
                                method.invoke(inst, new Object[0]);
                            } catch (Exception e) {
                                ErrorManager.getDefault ().notify (e);
                            }
                        } else {
                            inst = SharedClassObject.findObject(clazz, true);
                        }
                    } else {
                        try {
                            inst = clazz.newInstance();
                        } catch (Exception ex) {
                            IOException ioe = new IOException();
                            ErrorManager emgr = ErrorManager.getDefault();
                            emgr.annotate(ioe, ex);
                            emgr.annotate(ioe, "Content: \n" + getFileContent(source)); // NOI18N
                            emgr.annotate(ioe, "Class: " + clazz); // NOI18N
                            emgr.annotate(ioe, "Source: " + source); // NOI18N
                            throw ioe;
                        }
                    }
                }
            }
            
            return inst;
        }
        
        /** Get file content as String. If some exception occures its stack trace 
          is return instead. */
        private static String getFileContent (FileObject fo) {
            try {
                InputStreamReader isr = new InputStreamReader(fo.getInputStream());
                char[] cbuf = new char[1024];
                int length;
                StringBuffer sbuf = new StringBuffer(1024);
                while (true) {
                    length = isr.read(cbuf);
                    if (length > 0) {
                        sbuf.append(cbuf, 0, length);
                    } else {
                        return sbuf.toString();
                    }
                }
            } catch (Exception ex) {
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                return sw.toString();
            }
        }
        
        /** create instance by invoking class method */
        private Object createFromMethod(String srcClazz, String srcMethod)
        throws ClassNotFoundException, IOException {
            int dotIndex = instanceMethod.lastIndexOf('.');
            String targetClass;
            String targetMethod;
            if (dotIndex > 0) {
                targetClass = srcMethod.substring(0, dotIndex);
                targetMethod = srcMethod.substring(dotIndex + 1);
            } else {
                targetClass = srcClazz;
                targetMethod = srcMethod;
            }

            Class clazz = loadClass(targetClass);

            try {
                Method method;
                try {
                    method = clazz.getMethod(targetMethod, new Class[]{FileObject.class});
                    method.setAccessible(true);
                    return method.invoke(null, new FileObject[] {source});
                } catch (NoSuchMethodException ex) {
                    method = clazz.getMethod(targetMethod, null);
                    method.setAccessible(true);
                    return method.invoke(null, new Object[0]);
                }
            } catch (Exception ex) {
                IOException ioe = new IOException("Wrong settings format."); // NOI18N
                ErrorManager emgr = ErrorManager.getDefault();
                emgr.annotate(ioe, ex);
                emgr.annotate(ioe, "Content: \n" + getFileContent(source)); // NOI18N
                emgr.annotate(ioe, "Source: " + source); // NOI18N
                throw ioe;
            }
        }
        
        /** The representation type that may be created as instances.
         * Can be used to test whether the instance is of an appropriate
         * class without actually creating it.
         *
         * @return the representation class of the instance
         * @exception IOException if an I/O error occurred
         * @exception ClassNotFoundException if a class was not found
         */
        public Class instanceClass() throws java.io.IOException, ClassNotFoundException {
            if (instanceClass == null) {
                throw new ClassNotFoundException(source +
                    ": missing 'class' attribute in 'instance' element"); //NOI18N
            }
            
            return loadClass(instanceClass);
        }
        
        /** try to load class from system and current classloader. */
        private Class loadClass(String clazz) throws ClassNotFoundException {
            return ((ClassLoader)Lookup.getDefault().lookup(ClassLoader.class)).loadClass(clazz);
        }
        
        /** get class name of instance */
        public String instanceName() {
            if (instanceClass == null) {
                return ""; // NOI18N
            } else {
                return instanceClass;
            }
        }
        
        private int tr(char c) {
            if (c >= '0' && c <= '9') return c - '0';
            if (c >= 'A' && c <= 'F') return c - 'A' + 10;
            if (c >= 'a' && c <= 'f') return c - 'a' + 10;
            return -1;
        }
        
        /** Converts array of chars to array of bytes. All whitespaces and
         * unknown chars are skipped.
         */
        private void chars2Bytes (OutputStream os, char[] chars, int off, int length)
        throws IOException {
            byte rbyte;
            int read;
            
            for (int i = off; i < length; ) {
                read = tr(chars[i++]);
                if (read >= 0) rbyte = (byte) (read << 4); // * 16;
                else continue;
                
                while (i < length) {
                    read = tr(chars[i++]);
                    if (read >= 0) {
                        rbyte += (byte) read;
                        os.write(rbyte);
                        break;
                    }
                }
            }
        }
                
        /** Parse settings file. */
        public void parse() throws IOException {
            stack = new Stack();
            InputStream in = null;
            
            try {
                XMLReader reader = org.openide.xml.XMLUtil.createXMLReader();
                reader.setContentHandler(this);
                reader.setErrorHandler(this);
                reader.setEntityResolver(this);
                in = source.getInputStream();
                reader.parse(new org.xml.sax.InputSource(in));
            } catch (XMLSettingsSupport.StopSAXException ex) {
                // Ok, header is read
            } catch (SAXException ex) {
                IOException ioe = new IOException(source.toString()); // NOI18N
                ErrorManager emgr = ErrorManager.getDefault();
                emgr.annotate(ioe, ex);
                if (ex.getException () != null) {
                    emgr.annotate (ioe, ex.getException());
                }
                emgr.annotate(ioe, "Content: \n" + getFileContent(source)); // NOI18N
                emgr.annotate(ioe, "Source: " + source); // NOI18N
                throw ioe;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    // ignore already closed
                }
            }
        }
        
        /** Parse setting from source. */
        public void parse(Reader source) throws IOException {
            stack = new Stack();
            
            try {
                XMLReader reader = org.openide.xml.XMLUtil.createXMLReader();
                reader.setContentHandler(this);
                reader.setErrorHandler(this);
                reader.setEntityResolver(this);
                reader.parse(new org.xml.sax.InputSource(source));
            } catch (XMLSettingsSupport.StopSAXException ex) {
                // Ok, header is read
            } catch (SAXException ex) {
                IOException ioe = new IOException(source.toString()); // NOI18N
                ErrorManager emgr = ErrorManager.getDefault();
                emgr.annotate(ioe, ex);
                if (ex.getException () != null) {
                    emgr.annotate (ioe, ex.getException());
                }
                throw ioe;
            }
        }
    }
    
    final static class StopSAXException extends SAXException {
        public StopSAXException() {
            super("Parser stopped"); // NOI18N
        }
    }
    
    public static final class Convertor extends org.netbeans.spi.settings.Convertor {
        
        public Object read(java.io.Reader r) throws java.io.IOException, ClassNotFoundException {
            // XXX should be passed also FileObject to ensure full functionality
            // depends on #26076
            XMLSettingsSupport.SettingsRecognizer rec = new XMLSettingsSupport.SettingsRecognizer(false, null);
            rec.parse(r);
            return rec.instanceCreate();
        }
        
        public void registerSaver(Object inst, org.netbeans.spi.settings.Saver s) {
        }
        
        public void unregisterSaver(Object inst, org.netbeans.spi.settings.Saver s) {
        }
        
        public void write(java.io.Writer w, Object inst) throws java.io.IOException {
            XMLSettingsSupport.storeToXML10(inst, w, ModuleInfoManager.getDefault().getModuleInfo(inst.getClass()));
        }
        
    }
}
