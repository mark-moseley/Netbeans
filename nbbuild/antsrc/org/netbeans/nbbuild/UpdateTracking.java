/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.*;
import java.util.*;
import java.io.FileOutputStream;

import org.w3c.dom.*;
import org.xml.sax.InputSource;

import org.apache.tools.ant.BuildException;

/** This class represents module updates tracking
 *
 * @author  akemr
 */
class UpdateTracking {
    private static final String ELEMENT_MODULE = "module"; // NOI18N
    private static final String ATTR_CODENAME = "codename"; // NOI18N
    private static final String ELEMENT_VERSION = "module_version"; // NOI18N
    private static final String ATTR_VERSION = "specification_version"; // NOI18N
    private static final String ATTR_ORIGIN = "origin"; // NOI18N
    private static final String ATTR_LAST = "last"; // NOI18N
    private static final String ATTR_INSTALL = "install_time"; // NOI18N
    private static final String ELEMENT_FILE = "file"; // NOI18N
    private static final String ATTR_FILE_NAME = "name"; // NOI18N
    private static final String ATTR_CRC = "crc"; // NOI18N
    
    private static final String NBM_ORIGIN = "nbm"; // NOI18N
    private static final String INST_ORIGIN = "installer"; // NOI18N

    /** Platform dependent file name separator */
    private static final String FILE_SEPARATOR = System.getProperty ("file.separator");  // NOI18N           

    /** The name of the install_later file */
    public static final String TRACKING_DIRECTORY = "update_tracking"; // NOI18N
    
    private boolean pError = false;
    
    private File trackingFile = null;
    
    private String origin = NBM_ORIGIN;
    private String nbPath = null;
    private Module module = null;
    protected InputStream is = null;
    protected OutputStream os = null;
   
    // for generating xml in build process
    public UpdateTracking( String nbPath ) {
        this.nbPath = nbPath;
        origin = INST_ORIGIN;
    }
    
    /**
     * Use this constructor, only when you want to use I/O Streams
     */
    public UpdateTracking () {
        this.nbPath = null;
        origin = INST_ORIGIN;
    }
    
    public Version addNewModuleVersion( String codename, String spec_version ) {
        module = new Module();
        module.setCodename( codename );
        Version version = new Version();        
        version.setVersion( spec_version );
        version.setOrigin( origin );
        version.setLast( true );
        version.setInstall_time( System.currentTimeMillis() );
        module.setVersion( version );
        return version;
    }
    
    public String getVersionForCodeName( String codeName ) throws BuildException {
        module = new Module();
        module.setCodename( codeName );
//        if (this.is == null) {
            File directory = new File( nbPath + FILE_SEPARATOR + TRACKING_DIRECTORY );
            setTrackingFile(directory, getTrackingFileName());
            if (!trackingFile.exists() || !trackingFile.isFile())
                throw new BuildException ("Tracking file " + trackingFile.getAbsolutePath() + " cannot be found for module codenamebase " + codeName );
//        }
        read();
        if ( module.getVersions().size() != 1 ) 
            throw new BuildException ("Module with codenamebase " + codeName + " has got " + module.getVersions().size() + " specification versions. Correct number is 1.");
        return ((Version) module.getVersions().get(0)).getVersion();
    }
    
    public String[] getListOfNBM( String codeName ) throws BuildException {
        module = new Module();
        module.setCodename( codeName );
        if (this.is == null) {
            File directory = new File( nbPath + FILE_SEPARATOR + TRACKING_DIRECTORY );
            setTrackingFile(directory, getTrackingFileName());
            if (!trackingFile.exists() || !trackingFile.isFile())
                throw new BuildException ("Tracking file " + trackingFile.getAbsolutePath() + " cannot be found for module codenamebase " + codeName );
        }
        
        read();
        
        if ( module.getVersions().size() != 1 ) 
            throw new BuildException ("Module with codenamebase " + codeName + " has got " + module.getVersions().size() + " specification versions. Correct number is 1.");
        
        List files = ((Version) module.getVersions().get(0)).getFiles();
        String [] listFiles = new String[ files.size() ];
        for (int i=0; i < files.size(); i++) {
            listFiles[i] = (((ModuleFile) files.get(i)).getName());
        }
        
        return listFiles;
    }

    public void removeLocalized( String locale ) {
        File updateDirectory = new File( nbPath, TRACKING_DIRECTORY );
        File[] trackingFiles = updateDirectory.listFiles( new FileFilter() { // Get only *.xml files
            public boolean accept( File file ) {
                return file.isFile() &&file.getName().endsWith(".xml"); //NOI18N
            }
        } );
        if (trackingFiles != null)
            for (int i = trackingFiles.length-1; i >= 0; i--) {
                trackingFile = trackingFiles[i];
                read();
                module.removeLocalized( locale );
                write();
            }
    }
    
    void write( ) throws BuildException{
        Document document = XMLUtil.createDocument(ELEMENT_MODULE);  
        Element e_module = document.getDocumentElement();
        e_module.setAttribute(ATTR_CODENAME, module.getCodename());
        Iterator it2 = module.getVersions().iterator();
        while ( it2.hasNext() ) {
            Version ver = (Version)it2.next();
            Element e_version = document.createElement(ELEMENT_VERSION);
            e_version.setAttribute(ATTR_VERSION, ver.getVersion());
            e_version.setAttribute(ATTR_ORIGIN, ver.getOrigin());
            e_version.setAttribute(ATTR_LAST, "true");                          //NOI18N
            e_version.setAttribute(ATTR_INSTALL, Long.toString(ver.getInstall_time()));
            e_module.appendChild( e_version );
            Iterator it3 = ver.getFiles().iterator();
            while ( it3.hasNext() ) {
                ModuleFile file = (ModuleFile)it3.next();
                Element e_file = document.createElement(ELEMENT_FILE);
                e_file.setAttribute(ATTR_FILE_NAME, file.getName());
                e_file.setAttribute(ATTR_CRC, file.getCrc());
                e_version.appendChild( e_file );
            }
        }
        
        //document.getDocumentElement().normalize();
        if (this.os == null) {
            File directory = new File( nbPath + FILE_SEPARATOR + TRACKING_DIRECTORY );
            if (!directory.exists()) {
                directory.mkdirs();
            }
            setTrackingFile(directory, this.getTrackingFileName());
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(new File(directory,this.getTrackingFileName()));
            } catch (Exception e) {
                throw new BuildException("Could not get outputstream to write update tracking", e);
            }
            this.setTrackingOutputStream(fos);
        }
        try {
            try {
                XMLUtil.write(document, this.os);
            } finally {
                this.os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if ((trackingFile != null) && (trackingFile.exists()))
                trackingFile.delete();
            throw new BuildException("Could not write update tracking", e);
        }        
    }

    protected void setTrackingFile (File dir, String tFname) throws BuildException {
        this.trackingFile = new File(dir,tFname);
//        this.trackingFile.mkdirs();
        try {
            //setTrackingOutputStream(new FileOutputStream(this.trackingFile));
            if (this.trackingFile.exists())
                setTrackingInputStream(new FileInputStream(this.trackingFile));
        } catch (java.io.FileNotFoundException fnf) {
            throw new BuildException("Unable to find tracking file "+this.trackingFile.getAbsolutePath(), fnf);
        }
    }
    
    public void setTrackingOutputStream(OutputStream tos) {
        this.os = tos;
    }
    
    public OutputStream getTrackingOutputStream() {
        return this.os;
    }
    
    public void setTrackingInputStream(InputStream tis) {
        this.is = tis;
    }
    
    public String getTrackingFileName() throws BuildException {
        String trackingFileName = module.getCodenamebase();
        if ( ( trackingFileName == null ) || ( trackingFileName.length() == 0 ) )
            throw new BuildException ("Empty codenamebase, unable to locate tracking file");
        trackingFileName = trackingFileName.replace('.', '-') + ".xml"; //NOI18N
        return trackingFileName;
    }

    /** Scan through org.w3c.dom.Document document. */
    private void read() throws BuildException {
        /** org.w3c.dom.Document document */
        org.w3c.dom.Document document;
        if (this.is == null) {
            File directory = new File( nbPath + FILE_SEPARATOR + TRACKING_DIRECTORY );
            if (!directory.exists()) {
                directory.mkdirs();
            }
            setTrackingFile(directory,getTrackingFileName());
        }
        try {
            InputSource xmlInputSource = new InputSource( this.is );
            document = XMLUtil.parse( xmlInputSource, false, false, new ErrorCatcher(), null );
            if (is != null)
                is.close();
        } catch ( org.xml.sax.SAXException e ) {
            e.printStackTrace();
            if (trackingFile == null) {
                throw new BuildException ("Update tracking data in external InputStream is not well formatted XML document.", e);
            } else {
                throw new BuildException ("Update tracking file " + trackingFile.getAbsolutePath() + " is not well formatted XML document.", e);
            }
        } catch ( java.io.IOException e ) {
            e.printStackTrace();
            if (trackingFile == null) {
                throw new BuildException ("I/O error while accessing tracking data in InputStream", e);
            } else {
                throw new BuildException ("I/O error while accessing tracking file " + trackingFile.getAbsolutePath(), e);
            }
        }
            
        org.w3c.dom.Element element = document.getDocumentElement();
        if ((element != null) && element.getTagName().equals(ELEMENT_MODULE)) {
            scanElement_module(element);
        }
    }    
    
    /** Scan through org.w3c.dom.Element named module. */
    void scanElement_module(org.w3c.dom.Element element) { // <module>
        module = new Module();        
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
            if (attr.getName().equals(ATTR_CODENAME)) { // <module codename="???">
                module.setCodename( attr.getValue() );
            }
        }
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            if ( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
                org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                if (nodeElement.getTagName().equals(ELEMENT_VERSION)) {
                    scanElement_module_version(nodeElement, module);
                }
            }
        }
    }
    
    /** Scan through org.w3c.dom.Element named module_version. */
    void scanElement_module_version(org.w3c.dom.Element element, Module module) { // <module_version>
        Version version = new Version();        
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
            if (attr.getName().equals(ATTR_VERSION)) { // <module_version specification_version="???">
                version.setVersion( attr.getValue() );
            }
            if (attr.getName().equals(ATTR_ORIGIN)) { // <module_version origin="???">
                version.setOrigin( attr.getValue() );
            }
            if (attr.getName().equals(ATTR_LAST)) { // <module_version last="???">                
                version.setLast( Boolean.getBoolean(attr.getValue() ));
            }
            if (attr.getName().equals(ATTR_INSTALL)) { // <module_version install_time="???">
                long li = 0;
                try {
                    li = Long.parseLong( attr.getValue() );
                } catch ( NumberFormatException nfe ) {
                }
                version.setInstall_time( li );
            }
        }
        org.w3c.dom.NodeList nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            if ( node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE ) {
                org.w3c.dom.Element nodeElement = (org.w3c.dom.Element)node;
                if (nodeElement.getTagName().equals(ELEMENT_FILE)) {
                    scanElement_file(nodeElement, version);
                }
            }
        }
        module.addVersion( version );
    }
    
    /** Scan through org.w3c.dom.Element named file. */
    void scanElement_file(org.w3c.dom.Element element, Version version) { // <file>
        ModuleFile file = new ModuleFile();        
        org.w3c.dom.NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            org.w3c.dom.Attr attr = (org.w3c.dom.Attr)attrs.item(i);
            if (attr.getName().equals(ATTR_FILE_NAME)) { // <file name="???">
                file.setName( attr.getValue() );
            }
            if (attr.getName().equals(ATTR_CRC)) { // <file crc="???">
                file.setCrc( attr.getValue() );
            }
        }
        version.addFile (file );
    }
    
    class Module extends Object {        
        
        /** Holds value of property codename. */
        private String codename;
        
        /** Holds value of property versions. */
        private List versions = new ArrayList();
        
        /** Getter for property codenamebase.
         * @return Value of property codenamebase.
         */
        String getCodenamebase() {
	    String codenamebase = new String(codename);
            int idx = codenamebase.lastIndexOf ('/'); //NOI18N
            if (idx != -1) codenamebase = codenamebase.substring (0, idx);

            return codenamebase;
        }

         /** Getter for property codename.
         * @return Value of property codename.
         */
        String getCodename() {
            return codename;
        }
       
        /** Setter for property codename.
         * @param codename New value of property codename.
         */
        void setCodename(String codename) {
            this.codename = codename;
        }
        
        /** Getter for property versions.
         * @return Value of property versions.
         */
        List getVersions() {
            return versions;
        }
        
        /** Setter for property versions.
         * @param versions New value of property versions.
         */
        void setVersions(List versions) {
            this.versions = versions;
        }
        
        void addVersion( Version version ) {
            versions = new ArrayList();
            versions.add( version );
        }

        void setVersion( Version version ) {
            versions = new ArrayList();
            versions.add( version );
        }
        
        void removeLocalized( String locale ) {
            Iterator it = versions.iterator();
            while (it.hasNext()) {
                Version ver = (Version) it.next();
                ver.removeLocalized( locale );
            }
        }
    }
    
    public class Version extends Object {        
        
        /** Holds value of property version. */
        private String version;
        
        /** Holds value of property origin. */
        private String origin;
        
        /** Holds value of property last. */
        private boolean last;
        
        /** Holds value of property install_time. */
        private long install_time = 0;
        
        /** Holds value of property files. */
        private List files = new ArrayList();
        
        /** Getter for property version.
         * @return Value of property version.
         */
        String getVersion() {
            return version;
        }
        
        /** Setter for property version.
         * @param version New value of property version.
         */
        void setVersion(String version) {
            this.version = version;
        }
        
        /** Getter for property origin.
         * @return Value of property origin.
         */
        String getOrigin() {
            return origin;
        }
        
        /** Setter for property origin.
         * @param origin New value of property origin.
         */
        void setOrigin(String origin) {
            this.origin = origin;
        }
        
        /** Getter for property last.
         * @return Value of property last.
         */
        boolean isLast() {
            return last;
        }
        
        /** Setter for property last.
         * @param last New value of property last.
         */
        void setLast(boolean last) {
            this.last = last;
        }
        
        /** Getter for property install_time.
         * @return Value of property install_time.
         */
        long getInstall_time() {
            return install_time;
        }
        
        /** Setter for property install_time.
         * @param install_time New value of property install_time.
         */
        void setInstall_time(long install_time) {
            this.install_time = install_time;
        }
        
        /** Getter for property files.
         * @return Value of property files.
         */
        List getFiles() {
            return files;
        }
        
        /** Setter for property files.
         * @param files New value of property files.
         */
        void setFiles(List files) {
            this.files = files;
        }
        
        void addFile( ModuleFile file ) {
            files.add( file );
        }
        
        public void addFileWithCrc( String filename, String crc ) {
            ModuleFile file = new ModuleFile();
            file.setName( filename );
            file.setCrc( crc);
            files.add( file );
        }
        
        public void removeLocalized( String locale ) {
            ArrayList newFiles = new ArrayList();
            Iterator it = files.iterator();
            while (it.hasNext()) {
                ModuleFile file = (ModuleFile) it.next();
                if (file.getName().indexOf("_" + locale + ".") == -1 // NOI18N
                    && file.getName().indexOf("_" + locale + "/") == -1 // NOI18N
                    && !file.getName().endsWith("_" + locale) ) // NOI18N
                    newFiles.add ( file );
            }
            files = newFiles;
            
        }
        
    }
    
    class ModuleFile extends Object {        
        
        /** Holds value of property name. */
        private String name;
        
        /** Holds value of property crc. */
        private String crc;
        
        /** Getter for property name.
         * @return Value of property name.
         */
        String getName() {
            return name;
        }
        
        /** Setter for property name.
         * @param name New value of property name.
         */
        void setName(String name) {
            this.name = name;
        }
        
        /** Getter for property crc.
         * @return Value of property crc.
         */
        String getCrc() {
            return crc;
        }
        
        /** Setter for property crc.
         * @param crc New value of property crc.
         */
        void setCrc(String crc) {
            this.crc = crc;
        }
        
    }

    class ErrorCatcher implements org.xml.sax.ErrorHandler {
        private void message (String level, org.xml.sax.SAXParseException e) {
            pError = true;
        }

        public void error (org.xml.sax.SAXParseException e) {
            // normally a validity error
            pError = true;
        }

        public void warning (org.xml.sax.SAXParseException e) {
            //parseFailed = true;
        }

        public void fatalError (org.xml.sax.SAXParseException e) {
            pError = true;
        }
    }
    
}
