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

import org.netbeans.core.windows.Debug;

/**
 * Handle loading/saving of TopComponent reference in Mode configuration data.
 *
 * @author Marek Slama
 */

class TCRefParser {
    
    public static final String INSTANCE_DTD_ID_1_0
    = "-//NetBeans//DTD Top Component in Mode Properties 1.0//EN"; // NOI18N
    public static final String INSTANCE_DTD_ID_2_0
    = "-//NetBeans//DTD Top Component in Mode Properties 2.0//EN"; // NOI18N
    
    /** Unique id from file name */
    private String tc_id;
    
    /** Module parent folder */
    private FileObject moduleParentFolder;
    
    /** Local parent folder */
    private FileObject localParentFolder;
    
    private PropertyHandler propertyHandler;
    
    private InternalConfig internalConfig;
    
    /** true if wstcref file is present in module folder */
    private boolean inModuleFolder;
    /** true if wstcref file is present in local folder */
    private boolean inLocalFolder;
    
    public TCRefParser (String tc_id) {
        this.tc_id = tc_id;
    }
    
    /** Load tcref configuration. */
    TCRefConfig load () throws IOException {
        //log("");
        //log("++ TCRefParser.load ENTER" + " tcRef:" + tc_id);
        TCRefConfig tcRefCfg = new TCRefConfig();
        if (propertyHandler == null) {
            propertyHandler = new PropertyHandler();
        }
        InternalConfig internalCfg = getInternalConfig();
        internalCfg.clear();
        propertyHandler.readData(tcRefCfg, internalCfg);
        //log("++ TCRefParser.load LEAVE" + " tcRef:" + tc_id);
        //log("");
        return tcRefCfg;
    }
    
    /** Save tcref configuration. */
    void save (TCRefConfig tcRefCfg) throws IOException {
        //log("-- TCRefParser.save ENTER" + " tcRef:" + tc_id);
        if (propertyHandler == null) {
            propertyHandler = new PropertyHandler();
        }
        InternalConfig internalCfg = getInternalConfig();
        propertyHandler.writeData(tcRefCfg, internalCfg);
        //log("-- TCRefParser.save LEAVE" + " tcRef:" + tc_id);
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
    
    /** Setter for internal configuration data. Used only to pass module info
     * from import.
     * @param internalCfg instance of internal configuration data
     */
    void setInternalConfig (InternalConfig internalCfg) {
        internalConfig = internalCfg;
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
    
    void log (String s) {
        Debug.log(TCRefParser.class, s);
    }
    
    private final class PropertyHandler extends DefaultHandler {
        
        /** tcRef manager configuration data */
        private TCRefConfig tcRefConfig = null;
        
        /** internal configuration data */
        private InternalConfig internalConfig = null;
        
        /** xml parser */
        private XMLReader parser;
        
        /** Lock to prevent mixing readData and writeData */
        private final Object RW_LOCK = new Object();
        
        public PropertyHandler () {
        }
        
        private FileObject getConfigFOInput () {
            FileObject tcRefConfigFO;
            if (isInLocalFolder()) {
                //log("-- TCRefParser.getConfigFOInput" + " looking for LOCAL");
                tcRefConfigFO = localParentFolder.getFileObject
                (TCRefParser.this.getName(), PersistenceManager.TCREF_EXT);
            } else if (isInModuleFolder()) {
                //log("-- TCRefParser.getConfigFOInput" + " looking for MODULE");
                tcRefConfigFO = moduleParentFolder.getFileObject
                (TCRefParser.this.getName(), PersistenceManager.TCREF_EXT);
            } else {
                //XXX should not happen
                tcRefConfigFO = null;
            }
            //log("-- TCRefParser.getConfigFOInput" + " tcRefConfigFO:" + tcRefConfigFO);
            return tcRefConfigFO;
        }

        private FileObject getConfigFOOutput () throws IOException {
            FileObject tcRefConfigFO;
            tcRefConfigFO = localParentFolder.getFileObject
            (TCRefParser.this.getName(), PersistenceManager.TCREF_EXT);
            if (tcRefConfigFO != null) {
                //log("-- TCRefParser.getConfigFOOutput" + " tcRefConfigFO LOCAL:" + tcRefConfigFO);
                return tcRefConfigFO;
            } else {
                StringBuffer buffer = new StringBuffer();
                buffer.append(TCRefParser.this.getName());
                buffer.append('.');
                buffer.append(PersistenceManager.TCREF_EXT);
                //XXX should be improved localParentFolder can be null
                tcRefConfigFO = FileUtil.createData(localParentFolder, buffer.toString());
                //log("-- TCRefParser.getConfigFOOutput" + " LOCAL not found CREATE");

                return tcRefConfigFO;
            }
        }
        /** 
         Reads tcRef configuration data from XML file. 
         Data are returned in output params.
         */
        void readData (TCRefConfig tcRefCfg, InternalConfig internalCfg)
        throws IOException {
            tcRefConfig = tcRefCfg;
            internalConfig = internalCfg;
            
            FileObject cfgFOInput = getConfigFOInput();
            if (cfgFOInput == null) {
                throw new FileNotFoundException("[WinSys] Missing TCRef configuration file:" // NOI18N
                + TCRefParser.this.getName());
            }
            try {
                synchronized (RW_LOCK) {
                    //DUMP BEGIN
                    /*InputStream is = cfgFOInput.getInputStream();
                    byte [] arr = new byte [is.available()];
                    is.read(arr);
                    log("DUMP TCRef: " + TCRefParser.this.getName());
                    String s = new String(arr);
                    log(s);*/
                    //DUMP END
                    
                    getXMLParser().parse(new InputSource(cfgFOInput.getInputStream()));
                }
            } catch (SAXException exc) {
                //Turn into annotated IOException
                String msg = NbBundle.getMessage(TCRefParser.class,
                    "EXC_TCRefParse", cfgFOInput);
                IOException ioe = new IOException(msg);
                ErrorManager.getDefault().annotate(ioe, exc);
                throw ioe;
            }
                        
            tcRefCfg = tcRefConfig;
            internalCfg = internalConfig;
            
            tcRefConfig = null;
            internalConfig = null;
        }
        
        public void startElement (String nameSpace, String name, String qname, Attributes attrs) 
        throws SAXException {
            if ("tc-ref".equals(qname)) { // NOI18N
                handleTCRef(attrs);
            } else if (internalConfig.specVersion.compareTo(new SpecificationVersion("2.0")) == 0) { // NOI18N
                //Parse version 2.0
                if ("module".equals(qname)) { // NOI18N
                    handleModule(attrs);
                } else if ("tc-id".equals(qname)) { // NOI18N
                    handleTcId(attrs);
                } else if ("state".equals(qname)) { // NOI18N
                    handleState(attrs);
                }
            } else {
                log("-- TCRefParser.startElement PARSING OLD");
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
        
        /** Reads element "tc-ref" */
        private void handleTCRef (Attributes attrs) {
            String version = attrs.getValue("version"); // NOI18N
            if (version != null) {
                internalConfig.specVersion = new SpecificationVersion(version);
            } else {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                "[WinSys.TCRefParser.handleTCRef]" // NOI18N
                + " Warning: Missing attribute \"version\" of element \"tc-ref\"."); // NOI18N
                internalConfig.specVersion = new SpecificationVersion("2.0"); // NOI18N
            }
            //Before version 2.0 tc_id was attribute of tc-ref element
            //so we must read it directly here.
            if (internalConfig.specVersion.compareTo(new SpecificationVersion("2.0")) < 0) { // NOI18N
                String tc_id = attrs.getValue("id"); // NOI18N
                if (tc_id != null) {
                    //XXX handle old format
                } else {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "[WinSys.TCRefParser.handleTCRef]" // NOI18N
                    + " Warning: Missing attribute \"id\" of element \"tc-ref\"."); // NOI18N
                }
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
                tcRefConfig.tc_id = tc_id;
                if (!tc_id.equals(TCRefParser.this.getName())) {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "[WinSys.TCRefParser.handleTcId]" // NOI18N
                    + " Error: Value of attribute \"id\" of element \"tc-id\"" // NOI18N
                    + " and configuration file name must be the same."); // NOI18N
                    throw new SAXException("Invalid attribute value"); // NOI18N
                }
            } else {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                "[WinSys.TCRefParser.handleTcId]" // NOI18N
                + " Error: Missing required attribute \"id\" of element \"tc-id\"."); // NOI18N
                throw new SAXException("Missing required attribute"); // NOI18N
            }
        }
        
        private void handleState (Attributes attrs) throws SAXException {
            String opened = attrs.getValue("opened"); // NOI18N;
            if (opened != null) {
                if ("true".equals(opened)) { // NOI18N
                    tcRefConfig.opened = true;
                } else if ("false".equals(opened)) { // NOI18N
                    tcRefConfig.opened = false;
                } else {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "[WinSys.TCRefParser.handleState]" // NOI18N
                    + " Warning: Invalid value of attribute \"opened\"" // NOI18N
                    + " of element \"state\"."); // NOI18N
                    tcRefConfig.opened = false;
                }
            } else {
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                "[WinSys.TCRefParser.handleState]" // NOI18N
                + " Warning: Missing required attribute \"opened\"" // NOI18N
                + " of element \"state\"."); // NOI18N
                tcRefConfig.opened = false;
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
        
        public void startPrefixMapping(String str, String str1) throws SAXException {
        }
        
        public void characters(char[] values, int param, int param2) 
        throws SAXException {
        }
        
        public void setDocumentLocator(org.xml.sax.Locator locator) {
        }
        
        public void startDocument() throws SAXException {
        }
        
        /** Writes data from asociated tcRef to the xml representation */
        void writeData (TCRefConfig tcRefCfg, InternalConfig ic) throws IOException {
            final StringBuffer buff = fillBuffer(tcRefCfg, ic);
            synchronized (RW_LOCK) {
                FileObject cfgFOOutput = getConfigFOOutput();
                FileLock lock = cfgFOOutput.lock();
                OutputStreamWriter osw = null;
                try {
                    OutputStream os = cfgFOOutput.getOutputStream(lock);
                    osw = new OutputStreamWriter(os, "UTF-8"); // NOI18N
                    osw.write(buff.toString());
                    //log("DUMP TCRef: " + TCRefParser.this.getName());
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
        private StringBuffer fillBuffer (TCRefConfig tcRefCfg, InternalConfig ic) throws IOException {
            StringBuffer buff = new StringBuffer(800);
            String curValue = null;
            // header
            buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n"); // NOI18N
            /*buff.append("<!DOCTYPE tc-ref PUBLIC\n"); // NOI18N
            buff.append("          \"-//NetBeans//DTD Top Component in Mode Properties 2.0//EN\"\n"); // NOI18N
            buff.append("          \"http://www.netbeans.org/dtds/tc-ref2_0.dtd\">\n\n"); // NOI18N*/
            buff.append("<tc-ref version=\"2.0\">\n"); // NOI18N
            
            appendModule(ic, buff);
            appendTcId(tcRefCfg, buff);
            appendState(tcRefCfg, buff);
            
            buff.append("</tc-ref>\n"); // NOI18N
            return buff;
        }
        
        private void appendModule (InternalConfig ic, StringBuffer buff) {
            if (ic == null) {
                return;
            }
            if (ic.moduleCodeNameBase != null) {
                buff.append("    <module"); // NOI18N
                buff.append(" name=\""); // NOI18N
                buff.append(ic.moduleCodeNameBase);
                if (ic.moduleCodeNameRelease != null) {
                    buff.append("/" + ic.moduleCodeNameRelease); // NOI18N
                }
                if (ic.moduleSpecificationVersion != null) { 
                    buff.append("\" spec=\""); // NOI18N
                    buff.append(ic.moduleSpecificationVersion);
                }
                buff.append("\" />\n"); // NOI18N
            }
        }

        private void appendTcId (TCRefConfig tcRefCfg, StringBuffer buff) {
            buff.append("    <tc-id"); // NOI18N
            buff.append(" id=\""); // NOI18N
            buff.append(tcRefCfg.tc_id);
            buff.append("\""); // NOI18N
            buff.append(" />\n"); // NOI18N
        }
        
        private void appendState (TCRefConfig tcRefCfg, StringBuffer buff) {
            buff.append("    <state"); // NOI18N
            buff.append(" opened=\""); // NOI18N
            if (tcRefCfg.opened) {
                buff.append("true"); // NOI18N
            } else {
                buff.append("false"); // NOI18N
            }
            buff.append("\""); // NOI18N
            buff.append(" />\n"); // NOI18N
        }
        
        /** @return Newly created parser with set content handler, errror handler
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
            if (INSTANCE_DTD_ID_1_0.equals(publicId)
             || INSTANCE_DTD_ID_2_0.equals(publicId)) {
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
