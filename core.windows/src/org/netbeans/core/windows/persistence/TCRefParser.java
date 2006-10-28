/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.windows.persistence;

import java.util.logging.Level;
import org.netbeans.core.windows.Debug;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;
import java.util.logging.Logger;

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
    public static final String INSTANCE_DTD_ID_2_1
    = "-//NetBeans//DTD Top Component in Mode Properties 2.1//EN"; // NOI18N
    
    private static final boolean DEBUG = Debug.isLoggable(TCRefParser.class);
    
    /** Unique id from file name */
    private String tc_id;
    
    /** Module parent folder */
    private FileObject moduleParentFolder;
    
    /** Local parent folder */
    private FileObject localParentFolder;
    
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
        if (DEBUG) Debug.log(TCRefParser.class, "load ENTER" + " tcRef:" + tc_id);
        TCRefConfig tcRefCfg = new TCRefConfig();
        PropertyHandler propertyHandler = new PropertyHandler();
        InternalConfig internalCfg = getInternalConfig();
        internalCfg.clear();
        propertyHandler.readData(tcRefCfg, internalCfg);
        if (DEBUG) Debug.log(TCRefParser.class, "load LEAVE" + " tcRef:" + tc_id);
        return tcRefCfg;
    }
    
    /** Save tcref configuration. */
    void save (TCRefConfig tcRefCfg) throws IOException {
        if (DEBUG) Debug.log(TCRefParser.class, "save ENTER" + " tcRef:" + tc_id);
        PropertyHandler propertyHandler = new PropertyHandler();
        InternalConfig internalCfg = getInternalConfig();
        propertyHandler.writeData(tcRefCfg, internalCfg);
        if (DEBUG) Debug.log(TCRefParser.class, "save LEAVE" + " tcRef:" + tc_id);
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
        
        /** Lock to prevent mixing readData and writeData */
        private final Object RW_LOCK = new Object();
        
        public PropertyHandler () {
        }
        
        private FileObject getConfigFOInput () {
            FileObject tcRefConfigFO;
            if (isInLocalFolder()) {
                //log("getConfigFOInput" + " looking for LOCAL");
                tcRefConfigFO = localParentFolder.getFileObject
                (TCRefParser.this.getName(), PersistenceManager.TCREF_EXT);
            } else if (isInModuleFolder()) {
                //log("getConfigFOInput" + " looking for MODULE");
                tcRefConfigFO = moduleParentFolder.getFileObject
                (TCRefParser.this.getName(), PersistenceManager.TCREF_EXT);
            } else {
                //XXX should not happen
                tcRefConfigFO = null;
            }
            //log("getConfigFOInput" + " tcRefConfigFO:" + tcRefConfigFO);
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
            InputStream is = null;
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
                    is = cfgFOInput.getInputStream();
                    PersistenceManager.getDefault().getXMLParser(this).parse(new InputSource(is));
                }
            } catch (SAXException exc) {
                // Turn into annotated IOException
                String msg = NbBundle.getMessage(TCRefParser.class,
                                                 "EXC_TCRefParse", cfgFOInput);

                throw (IOException) new IOException(msg).initCause(exc);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException exc) {
                    Logger.getLogger(TCRefParser.class.getName()).log(Level.WARNING, null, exc);
                }
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
            } else if (internalConfig.specVersion.compareTo(new SpecificationVersion("2.0")) >= 0) { // NOI18N
                //Parse version 2.0
                if ("module".equals(qname)) { // NOI18N
                    handleModule(attrs);
                } else if ("tc-id".equals(qname)) { // NOI18N
                    handleTcId(attrs);
                } else if ("state".equals(qname)) { // NOI18N
                    handleState(attrs);
                } else if ("previousMode".equals(qname)) {
                    handlePreviousMode(attrs);
                }
            } else {
                log("-- TCRefParser.startElement PARSING OLD");
                //Parse version < 2.0
            }
        }

        public void error(SAXParseException ex) throws SAXException  {
            throw ex;
        }

        /** Reads element "tc-ref" */
        private void handleTCRef (Attributes attrs) {
            String version = attrs.getValue("version"); // NOI18N
            if (version != null) {
                internalConfig.specVersion = new SpecificationVersion(version);
            } else {
                PersistenceManager.LOG.log(Level.WARNING,
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
                    PersistenceManager.LOG.log(Level.WARNING,
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
                Logger.getLogger(TCRefParser.class.getName()).log(Level.WARNING, null,
                                  new IllegalStateException("Module release code was saved as null string" +
                                                            " for module " +
                                                            internalConfig.moduleCodeNameBase +
                                                            "! Repairing."));
                internalConfig.moduleCodeNameRelease = null;
            }
        }
        
        /** Reads element "tc-id" */
        private void handleTcId (Attributes attrs) throws SAXException {
            String tc_id = attrs.getValue("id"); // NOI18N
            if (tc_id != null) {
                tcRefConfig.tc_id = tc_id;
                if (!tc_id.equals(TCRefParser.this.getName())) {
                    PersistenceManager.LOG.log(Level.WARNING,
                    "[WinSys.TCRefParser.handleTcId]" // NOI18N
                    + " Error: Value of attribute \"id\" of element \"tc-id\"" // NOI18N
                    + " and configuration file name must be the same."); // NOI18N
                    throw new SAXException("Invalid attribute value"); // NOI18N
                }
            } else {
                PersistenceManager.LOG.log(Level.WARNING,
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
                    PersistenceManager.LOG.log(Level.WARNING,
                    "[WinSys.TCRefParser.handleState]" // NOI18N
                    + " Warning: Invalid value of attribute \"opened\"" // NOI18N
                    + " of element \"state\"."); // NOI18N
                    tcRefConfig.opened = false;
                }
            } else {
                PersistenceManager.LOG.log(Level.WARNING,
                "[WinSys.TCRefParser.handleState]" // NOI18N
                + " Warning: Missing required attribute \"opened\"" // NOI18N
                + " of element \"state\"."); // NOI18N
                tcRefConfig.opened = false;
            }
        }

        private void handlePreviousMode (Attributes attrs) throws SAXException {
            String name = attrs.getValue("name"); // NOI18N;
            if (name != null) {
                tcRefConfig.previousMode = name;
            } else {
                PersistenceManager.LOG.log(Level.WARNING,
                "[WinSys.TCRefParser.handlePreviousMode]" // NOI18N
                + " Warning: Missing required attribute \"name\"" // NOI18N
                + " of element \"previousMode\"."); // NOI18N
                tcRefConfig.previousMode = null;
            }
        }
        
        /** Writes data from asociated tcRef to the xml representation */
        void writeData (TCRefConfig tcRefCfg, InternalConfig ic) throws IOException {
            final StringBuffer buff = fillBuffer(tcRefCfg, ic);
            synchronized (RW_LOCK) {
                FileObject cfgFOOutput = getConfigFOOutput();
                FileLock lock = null;
                OutputStream os = null;
                OutputStreamWriter osw = null;
                try {
                    lock = cfgFOOutput.lock();
                    os = cfgFOOutput.getOutputStream(lock);
                    osw = new OutputStreamWriter(os, "UTF-8"); // NOI18N
                    osw.write(buff.toString());
                    //log("DUMP TCRef: " + TCRefParser.this.getName());
                    //log(buff.toString());
                } finally {
                    try {
                        if (osw != null) {
                            osw.close();
                        }
                    } catch (IOException exc) {
                        Logger.getLogger(TCRefParser.class.getName()).log(Level.WARNING, null, exc);
                    }
                    if (lock != null) {
                        lock.releaseLock();
                    }
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
            buff.append("          \"-//NetBeans//DTD Top Component in Mode Properties 2.1//EN\"\n"); // NOI18N
            buff.append("          \"http://www.netbeans.org/dtds/tc-ref2_1.dtd\">\n\n"); // NOI18N*/
            buff.append("<tc-ref version=\"2.1\">\n"); // NOI18N
            
            appendModule(ic, buff);
            appendTcId(tcRefCfg, buff);
            appendState(tcRefCfg, buff);
            if (tcRefCfg.previousMode != null) {
                appendPreviousMode(tcRefCfg, buff);
            }
            
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

            buff.append(PersistenceManager.escapeTcId4XmlContent(tcRefCfg.tc_id));
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
        
        private void appendPreviousMode(TCRefConfig tcRefCfg, StringBuffer buff) {
            buff.append("    <previousMode name=\""); // NOI18N
            buff.append(tcRefCfg.previousMode);
            buff.append("\" />\n"); // NOI18N
        }

    }
    
}
