/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.persistence;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;

import org.netbeans.core.projects.SessionManager;
import org.netbeans.core.projects.SystemFileSystem;
import org.netbeans.core.windows.Debug;

/**
 * Handle loading/saving of TopComponent reference in Group configuration data.
 *
 * @author Marek Slama
 */

class TCGroupParser {
    
    public static final String INSTANCE_DTD_ID_2_0
    = "-//NetBeans//DTD Top Component in Group Properties 2.0//EN"; // NOI18N
    
    /** Unique id from file name */
    private String tc_id;
    
    /** Module parent folder */
    private FileObject moduleParentFolder;
    
    /** Local parent folder */
    private FileObject localParentFolder;
    
    private PropertyHandler propertyHandler;
    
    private InternalConfig internalConfig;
    
    /** true if wstcgrp file is present in module folder */
    private boolean inModuleFolder;
    /** true if wstcgrp file is present in local folder */
    private boolean inLocalFolder;
    /** true if wstcgrp file is present in session layer */
    private boolean inSessionLayer = false;
    
    public TCGroupParser(String tc_id) {
        this.tc_id = tc_id;
    }
    
    /** Load tcgroup configuration. */
    TCGroupConfig load () throws IOException {
        //log("");
        //log("++ TCGroupParser.load ENTER" + " tcRef:" + tc_id);
        TCGroupConfig tcGroupCfg = new TCGroupConfig();
        if (propertyHandler == null) {
            propertyHandler = new PropertyHandler();
        }
        InternalConfig internalCfg = getInternalConfig();
        internalCfg.clear();
        propertyHandler.readData(tcGroupCfg, internalCfg);
        
        /*log("               specVersion: " + internalCfg.specVersion);
        log("        moduleCodeNameBase: " + internalCfg.moduleCodeNameBase);
        log("     moduleCodeNameRelease: " + internalCfg.moduleCodeNameRelease);
        log("moduleSpecificationVersion: " + internalCfg.moduleSpecificationVersion);*/
        //log("++ TCGroupParser.load LEAVE" + " tcGroup:" + tc_id);
        //log("");
        return tcGroupCfg;
    }
    
    /** Save tcGroup configuration. */
    void save (TCGroupConfig tcGroupCfg) throws IOException {
        //log("-- TCGroupParser.save ENTER" + " tcGroup:" + tc_id);
        if (propertyHandler == null) {
            propertyHandler = new PropertyHandler();
        }
        InternalConfig internalCfg = getInternalConfig();
        propertyHandler.writeData(tcGroupCfg, internalCfg);
        //log("-- TCGroupParser.save LEAVE" + " tcGroup:" + tc_id);
    }
    
    String getName () {
        return tc_id;
    }
    
    /** Getter for internal configuration data.
     * @return instance of internal configuration data
     */
    InternalConfig getInternalConfig () {
        if (internalConfig == null) {
            internalConfig = new InternalConfig();
        }
        return internalConfig;
    }
    
    boolean isInModuleFolder () {
        return inModuleFolder;
    }
    
    void setInModuleFolder (boolean inModuleFolder) {
        this.inModuleFolder = inModuleFolder;
    }
    
    boolean isInLocalFolder () {
        return inLocalFolder;
    }
    
    void setInLocalFolder (boolean inLocalFolder) {
        this.inLocalFolder = inLocalFolder;
    }
    
    void setModuleParentFolder (FileObject moduleParentFolder) {
        this.moduleParentFolder = moduleParentFolder;
    }
    
    void setLocalParentFolder (FileObject localParentFolder) {
        this.localParentFolder = localParentFolder;
    }
    
    boolean isInSessionLayer () {
        return inSessionLayer;
    }
    
    void setInSessionLayer (boolean inSessionLayer) {
        this.inSessionLayer = inSessionLayer;
    }
    
    void log (String s) {
        Debug.log(TCGroupParser.class, s);
    }
    
    private final class PropertyHandler extends DefaultHandler {
        
        /** tcRef manager configuration data */
        private TCGroupConfig tcGroupConfig = null;
        
        /** internal configuration data */
        private InternalConfig internalConfig = null;
        
        /** xml parser */
        private XMLReader parser;
        
        /** Lock to prevent mixing readData and writeData */
        private final Object RW_LOCK = new Object();
        
        public PropertyHandler () {
        }
        
        private FileObject getConfigFOInput () {
            FileObject tcGroupConfigFO;
            if (isInLocalFolder()) {
                //log("-- TCGroupParser.getConfigFOInput" + " looking for LOCAL");
                tcGroupConfigFO = localParentFolder.getFileObject
                (TCGroupParser.this.getName(), PersistenceManager.TCGROUP_EXT);
            } else if (isInModuleFolder()) {
                //log("-- TCGroupParser.getConfigFOInput" + " looking for MODULE");
                tcGroupConfigFO = moduleParentFolder.getFileObject
                (TCGroupParser.this.getName(), PersistenceManager.TCGROUP_EXT);
                //Check layer attribute and if it is session copy it to create
                //local file at session layer too.
                //Valid only till winsys is in project layer.
                Object attr = tcGroupConfigFO.getAttribute("SystemFileSystem.layer"); // NOI18N
                if ((attr instanceof String) && "session".equals(attr)) { // NOI18N
                    TCGroupParser.this.setInSessionLayer(true);
                }
            } else {
                //XXX should not happen
                tcGroupConfigFO = null;
            }
            //log("-- TCGroupParser.getConfigFOInput" + " tcGroupConfigFO:" + tcGroupConfigFO);
            return tcGroupConfigFO;
        }

        private FileObject getConfigFOOutput () throws IOException {
            FileObject tcGroupConfigFO;
            tcGroupConfigFO = localParentFolder.getFileObject
            (TCGroupParser.this.getName(), PersistenceManager.TCGROUP_EXT);
            if (tcGroupConfigFO != null) {
                //log("-- TCGroupParser.getConfigFOOutput" + " tcGroupConfigFO LOCAL:" + tcGroupConfigFO);
                return tcGroupConfigFO;
            } else {
                StringBuffer buffer = new StringBuffer();
                buffer.append(TCGroupParser.this.getName());
                buffer.append('.');
                buffer.append(PersistenceManager.TCGROUP_EXT);
                //XXX should be improved localParentFolder can be null
                if (TCGroupParser.this.isInSessionLayer()) {
                    SystemFileSystem.setLayerForNew(localParentFolder.getPath(),SessionManager.LAYER_SESSION);
                }
                tcGroupConfigFO = FileUtil.createData(localParentFolder, buffer.toString());
                if (TCGroupParser.this.isInSessionLayer()) {
                    SystemFileSystem.setLayerForNew(localParentFolder.getPath(),null);
                }
                //log("-- TCGroupParser.getConfigFOOutput" + " LOCAL not found CREATE");

                return tcGroupConfigFO;
            }
        }
        /** 
         Reads tcRef configuration data from XML file. 
         Data are returned in output params.
         */
        void readData (TCGroupConfig tcGroupCfg, InternalConfig internalCfg)
        throws IOException {
            tcGroupConfig = tcGroupCfg;
            internalConfig = internalCfg;
            
            FileObject cfgFOInput = getConfigFOInput();
            if (cfgFOInput == null) {
                throw new FileNotFoundException("[WinSys] Missing TCGroup configuration file:" // NOI18N
                + TCGroupParser.this.getName());
            }
            try {
                synchronized (RW_LOCK) {
                    //DUMP BEGIN
                    /*InputStream is = cfgFOInput.getInputStream();
                    byte [] arr = new byte [is.available()];
                    is.read(arr);
                    log("DUMP TCGroup: " + TCGroupParser.this.getName());
                    String s = new String(arr);
                    log(s);*/
                    //DUMP END
                    
                    getXMLParser().parse(new InputSource(cfgFOInput.getInputStream()));
                }
            } catch (SAXException exc) {
                //Turn into annotated IOException
                String msg = NbBundle.getMessage(TCGroupParser.class,
                    "EXC_TCGroupParse", cfgFOInput);
                IOException ioe = new IOException(msg);
                ErrorManager.getDefault().annotate(ioe, exc);
                throw ioe;
            }
                        
            tcGroupCfg = tcGroupConfig;
            internalCfg = internalConfig;
            
            tcGroupConfig = null;
            internalConfig = null;
        }
        
        public void startElement (String nameSpace, String name, String qname, Attributes attrs) 
        throws SAXException {
            if ("tc-group".equals(qname)) { // NOI18N
                handleTCGroup(attrs);
            } else if (internalConfig.specVersion.compareTo(new SpecificationVersion("2.0")) == 0) { // NOI18N
                //Parse version 2.0
                if ("module".equals(qname)) { // NOI18N
                    handleModule(attrs);
                } else if ("tc-id".equals(qname)) { // NOI18N
                    handleTcId(attrs);
                } else if ("open-close-behavior".equals(qname)) { // NOI18N
                    handleOpenCloseBehavior(attrs);
                }
            } else {
                log("-- TCGroupParser.startElement PARSING OLD");
                //Parse version < 2.0
            }
        }

        public void error(SAXParseException ex) throws SAXException  {
            throw ex;
        }

        public void fatalError(SAXParseException ex) throws SAXException {
            throw ex;
        }

        public void warning(SAXParseException ex) throws SAXException {
            // ignore
        }
        
        /** Reads element "tc-group" */
        private void handleTCGroup (Attributes attrs) {
            String version = attrs.getValue("version"); // NOI18N
            if (version != null) {
                internalConfig.specVersion = new SpecificationVersion(version);
            } else {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                "[WinSys.TCGroupParser.handleTCGroup]" // NOI18N
                + " Warning: Missing attribute \"version\" of element \"tc-group\"."); // NOI18N
                internalConfig.specVersion = new SpecificationVersion("2.0"); // NOI18N
            }
        }
        
        /** Reads element "module" and updates mode config content */
        private void handleModule (Attributes attrs) {
            String moduleCodeName = attrs.getValue("name"); // NOI18N
            //Parse code name
            internalConfig.moduleCodeNameBase = null;
            internalConfig.moduleCodeNameRelease = null;
            internalConfig.moduleSpecificationVersion = null;
            if (moduleCodeName != null) {
                int i = moduleCodeName.indexOf('/');
                if (i != -1) {
                    internalConfig.moduleCodeNameBase = moduleCodeName.substring(0, i);
                    internalConfig.moduleCodeNameRelease = moduleCodeName.substring(i + 1);
                    checkReleaseCode(internalConfig);
                } else {
                    internalConfig.moduleCodeNameBase = moduleCodeName;
                }
                internalConfig.moduleSpecificationVersion = attrs.getValue("spec"); // NOI18N
            }
        }

        /** Checks validity of <code>moduleCodeNameRelease</code> field. 
         * Helper method. */
        private void checkReleaseCode (InternalConfig internalConfig) {
            // #24844. Repair the wrongly saved "null" string
            // as release number.
            if("null".equals(internalConfig.moduleCodeNameRelease)) { // NOI18N
                ErrorManager.getDefault().notify(
                    ErrorManager.INFORMATIONAL,
                    new IllegalStateException(
                        "Module release code was saved as null string" // NOI18N
                        + " for module "  + internalConfig.moduleCodeNameBase // NOI18N
                        + "! Repairing.") // NOI18N
                );
                internalConfig.moduleCodeNameRelease = null;
            }
        }
        
        /** Reads element "tc-id" */
        private void handleTcId (Attributes attrs) throws SAXException {
            String tc_id = attrs.getValue("id"); // NOI18N
            if (tc_id != null) {
                tcGroupConfig.tc_id = tc_id;
                if (!tc_id.equals(TCGroupParser.this.getName())) {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "[WinSys.TCGroupParser.handleTcId]" // NOI18N
                    + " Error: Value of attribute \"id\" of element \"tc-id\"" // NOI18N
                    + " and configuration file name must be the same."); // NOI18N
                    throw new SAXException("Invalid attribute value"); // NOI18N
                }
            } else {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                "[WinSys.TCGroupParser.handleTcId]" // NOI18N
                + " Error: Missing required attribute \"id\" of element \"tc-id\"."); // NOI18N
                throw new SAXException("Missing required attribute"); // NOI18N
            }
        }
        
        /** Reads element "open-close-behavior" */
        private void handleOpenCloseBehavior (Attributes attrs) throws SAXException {
            String open = attrs.getValue("open"); // NOI18N;
            if (open != null) {
                if ("true".equals(open)) { // NOI18N
                    tcGroupConfig.open = true;
                } else if ("false".equals(open)) { // NOI18N
                    tcGroupConfig.open = false;
                } else {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "[WinSys.TCGroupParser.handleOpenCloseBehavior]" // NOI18N
                    + " Warning: Invalid value of attribute \"open\"" // NOI18N
                    + " of element \"open-close-behavior\"."); // NOI18N
                    tcGroupConfig.open = false;
                }
            } else {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                "[WinSys.TCGroupParser.handleOpenCloseBehavior]" // NOI18N
                + " Warning: Missing required attribute \"open\"" // NOI18N
                + " of element \"open-close-behavior\"."); // NOI18N
                tcGroupConfig.open = false;
            }
            
            String close = attrs.getValue("close"); // NOI18N;
            if (close != null) {
                if ("true".equals(close)) { // NOI18N
                    tcGroupConfig.close = true;
                } else if ("false".equals(close)) { // NOI18N
                    tcGroupConfig.close = false;
                } else {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "[WinSys.TCGroupParser.handleOpenCloseBehavior]" // NOI18N
                    + " Warning: Invalid value of attribute \"close\"" // NOI18N
                    + " of element \"open-close-behavior\"."); // NOI18N
                    tcGroupConfig.close = false;
                }
            } else {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                "[WinSys.TCGroupParser.handleOpenCloseBehavior]" // NOI18N
                + " Warning: Missing required attribute \"close\"" // NOI18N
                + " of element \"open-close-behavior\"."); // NOI18N
                tcGroupConfig.close = false;
            }
            
            String wasOpened = attrs.getValue("was-opened"); // NOI18N;
            if (wasOpened != null) {
                if ("true".equals(wasOpened)) { // NOI18N
                    tcGroupConfig.wasOpened = true;
                } else if ("false".equals(wasOpened)) { // NOI18N
                    tcGroupConfig.wasOpened = false;
                } else {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "[WinSys.TCGroupParser.handleOpenCloseBehavior]" // NOI18N
                    + " Warning: Invalid value of attribute \"was-opened\"" // NOI18N
                    + " of element \"open-close-behavior\"."); // NOI18N
                    tcGroupConfig.wasOpened = false;
                }
            } else {
                tcGroupConfig.wasOpened = false;
            }
        }
        
        public void endDocument() throws SAXException {
        }
        
        public void ignorableWhitespace(char[] values, int param, int param2) 
        throws SAXException {
        }
        
        public void endElement(String str, String str1, String str2) 
        throws SAXException {
        }
        
        public void skippedEntity(String str) throws SAXException {
        }
        
        public void processingInstruction(String str, String str1) 
        throws SAXException {
        }
                
        public void endPrefixMapping(String str) throws SAXException {
        }
        
        public void startPrefixMapping(String str, String str1) 
        throws SAXException {
        }
        
        public void characters(char[] values, int param, int param2) 
        throws SAXException {
        }
        
        public void setDocumentLocator(org.xml.sax.Locator locator) {
        }
        
        public void startDocument() throws SAXException {
        }
        
        /** Writes data from asociated tcRef to the xml representation */
        void writeData (TCGroupConfig tcGroupCfg, InternalConfig ic) throws IOException {
            final StringBuffer buff = fillBuffer(tcGroupCfg, ic);
            synchronized (RW_LOCK) {
                FileObject cfgFOOutput = getConfigFOOutput();
                FileLock lock = cfgFOOutput.lock();
                OutputStreamWriter osw = null;
                try {
                    OutputStream os = cfgFOOutput.getOutputStream(lock);
                    osw = new OutputStreamWriter(os, "UTF-8"); // NOI18N
                    osw.write(buff.toString());
                    //log("DUMP TCGroup: " + TCGroupParser.this.getName());
                    //log(buff.toString());
                } finally {
                    if (osw != null) {
                        osw.close();
                    }
                    lock.releaseLock();
                }
            }
        }
        
        /** Returns xml content in StringBuffer
         */
        private StringBuffer fillBuffer (TCGroupConfig tcGroupCfg, InternalConfig ic) throws IOException {
            StringBuffer buff = new StringBuffer(800);
            String curValue = null;
            // header
            buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n"). // NOI18N
            /*buff.append("<!DOCTYPE tc-group PUBLIC\n"); // NOI18N
            buff.append("          \"-//NetBeans//DTD Top Component in Group Properties 2.0//EN\"\n"); // NOI18N
            buff.append("          \"http://www.netbeans.org/dtds/tc-group2_0.dtd\">\n\n"); // NOI18N*/
                append("<tc-group version=\"2.0\">\n"); // NOI18N
            
            appendModule(ic, buff);
            appendTcId(tcGroupCfg, buff);
            appendOpenCloseBehavior(tcGroupCfg, buff);
            
            buff.append("</tc-group>\n"); // NOI18N
            return buff;
        }
        
        private void appendModule (InternalConfig ic, StringBuffer buff) {
            if (ic == null) {
                return;
            }
            if (ic.moduleCodeNameBase != null) {
                buff.append(" <module name=\""); // NOI18N
                buff.append(ic.moduleCodeNameBase);
                if (ic.moduleCodeNameRelease != null) {
                    buff.append("/").append(ic.moduleCodeNameRelease); // NOI18N
                }
                if (ic.moduleSpecificationVersion != null) { 
                    buff.append("\" spec=\""); // NOI18N
                    buff.append(ic.moduleSpecificationVersion);
                }
                buff.append("\" />\n"); // NOI18N
            }
        }

        private void appendTcId (TCGroupConfig tcGroupCfg, StringBuffer buff) {
            buff.append(" <tc-id id=\"").append(tcGroupCfg.tc_id).append("\"/>\n"); // NOI18N
        }
        
        private void appendOpenCloseBehavior (TCGroupConfig tcGroupCfg, StringBuffer buff) {
            buff.append(" <open-close-behavior open=\"").append(tcGroupCfg.open). // NOI18N
                append("\" close=\"").append(tcGroupCfg.close). // NOI18N
                append("\" was-opened=\"").append(tcGroupCfg.wasOpened).append("\"/>\n"); // NOI18N
        }
        
        /** @return Newly created parser with grou content handler, errror handler
         * and entity resolver
         */
        private XMLReader getXMLParser () throws SAXException {
            if (parser == null) {
                // get non validating, not namespace aware parser
                parser = XMLUtil.createXMLReader();
                parser.setContentHandler(this);
                parser.setErrorHandler(this);
                parser.setEntityResolver(this);
            }
            return parser;
        }

        /** Implementation of entity resolver. Points to the local DTD
         * for our public ID */
        public InputSource resolveEntity (String publicId, String systemId)
        throws SAXException {
            if (INSTANCE_DTD_ID_2_0.equals(publicId)) {
                InputStream is = new ByteArrayInputStream(new byte[0]);
                //getClass().getResourceAsStream(INSTANCE_DTD_LOCAL);
//                if (is == null) {
//                    throw new IllegalStateException ("Entity cannot be resolved."); // NOI18N
//                }
                return new InputSource(is);
            }
            return null; // i.e. follow advice of systemID
        }
    }
    
}
