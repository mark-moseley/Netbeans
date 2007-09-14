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

package org.netbeans.modules.db.explorer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.Environment;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.XMLDataObject;
import org.openide.util.RequestProcessor;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.xml.EntityCatalog;
import org.openide.xml.XMLUtil;
import org.openide.filesystems.Repository;
import org.netbeans.modules.db.explorer.nodes.RootNode;
import org.openide.util.Exceptions;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reads and writes the database connection registration format.
 *
 * @author Radko Najman, Andrei Badea
 */
public class DatabaseConnectionConvertor implements Environment.Provider, InstanceCookie.Of {
    
    /**
     * The path where the connections are registered in the SystemFileSystem.
     */
    public static final String CONNECTIONS_PATH = "Databases/Connections"; // NOI18N
    
    /**
     * The delay by which the write of the changes is postponed.
     */
    private static final int DELAY = 2000;
    
    private static FileObject newlyCreated;
    private static DatabaseConnection newlyCreatedInstance;
    
    private final Reference holder;

    /**
     * The lookup provided through Environment.Provider.
     */
    private Lookup lookup = null;

    private Reference refConnection = new WeakReference(null);
    
    private PCL listener;

    private static DatabaseConnectionConvertor createProvider() {
        return new DatabaseConnectionConvertor();
    }
    
    private DatabaseConnectionConvertor() {
        holder = new WeakReference(null);
    }

    private DatabaseConnectionConvertor(XMLDataObject object) {
        holder = new WeakReference(object);
        InstanceContent cookies = new InstanceContent();
        cookies.add(this);
        lookup = new AbstractLookup(cookies);
    }
    
    private DatabaseConnectionConvertor(XMLDataObject object, DatabaseConnection existingInstance) {
        this(object);
        refConnection = new WeakReference(existingInstance);
        attachListener();
    }
    
    // Environment.Provider methods
    
    public Lookup getEnvironment(DataObject obj) {
        if (obj.getPrimaryFile() == newlyCreated) {
            return new DatabaseConnectionConvertor((XMLDataObject)obj, newlyCreatedInstance).getLookup();
        } else {
            return new DatabaseConnectionConvertor((XMLDataObject)obj).getLookup();
        }
    }
    
    // InstanceCookie.Of methods

    public String instanceName() {
        XMLDataObject obj = getHolder();
        return obj == null ? "" : obj.getName();
    }
    
    public Class instanceClass() {
        return DatabaseConnection.class;
    }
    
    public boolean instanceOf(Class type) {
        return (type.isAssignableFrom(DatabaseConnection.class));
    }

    public Object instanceCreate() throws java.io.IOException, ClassNotFoundException {
        synchronized (this) {
            Object o = refConnection.get();
            if (o != null) {
                return o;
            }

            XMLDataObject obj = getHolder();
            if (obj == null) {
                return null;
            }
            Handler handler = new Handler();
            try {
                XMLReader reader = XMLUtil.createXMLReader();
                InputSource is = new InputSource(obj.getPrimaryFile().getInputStream());
                is.setSystemId(obj.getPrimaryFile().getURL().toExternalForm());
                reader.setContentHandler(handler);
                reader.setErrorHandler(handler);
                reader.setEntityResolver(EntityCatalog.getDefault());

                reader.parse(is);
            } catch (SAXException ex) {
                Exception x = ex.getException();
                if (x instanceof java.io.IOException)
                    throw (IOException)x;
                else
                    throw new java.io.IOException(ex.getMessage());
            }

            DatabaseConnection inst = createDatabaseConnection(handler);
            refConnection = new WeakReference(inst);
            attachListener();
            return inst;
        }
    }
    
    private XMLDataObject getHolder() {
        return (XMLDataObject)holder.get();
    }

    private void attachListener() {
        listener = new PCL();
        DatabaseConnection dbconn = ((DatabaseConnection)refConnection.get());
        dbconn.addPropertyChangeListener(WeakListeners.propertyChange(listener, dbconn));
    }

    private static DatabaseConnection createDatabaseConnection(Handler handler) {
        DatabaseConnection dbconn = new DatabaseConnection(
                handler.driverClass, 
                handler.driverName,
                handler.connectionUrl,
                handler.schema,
                handler.user,
                null);
        return dbconn;
    }

    /**
     * Creates the XML file describing the specified database connection.
     */
    public static DataObject create(DatabaseConnection dbconn) throws IOException {
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(CONNECTIONS_PATH);
        DataFolder df = DataFolder.findFolder(fo);

        AtomicWriter writer = new AtomicWriter(dbconn, df, convertToFileName(dbconn.getName()));
        df.getPrimaryFile().getFileSystem().runAtomicAction(writer);
        return writer.holder;
    }
    
    private static String convertToFileName(String databaseURL) {
        return databaseURL.substring(0, Math.min(32, databaseURL.length())).replaceAll("[^\\p{Alnum}]", "_"); // NOI18N
    }
    
    /**
     * Moves the existing database connections from the DatabaseOption 
     * used in 4.1 and previous to the SystemFileSystem.
     */
    public static void importOldConnections() {
        Vector dbconns = RootNode.getOption().getConnections();
        for (Iterator i = dbconns.iterator(); i.hasNext();) {
            try {
                create((DatabaseConnection) i.next());
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            i.remove();
        }
    }
    
    /**
     * Removes the file describing the specified database connection.
     */
    public static void remove(DatabaseConnection dbconn) throws IOException {
        String name = dbconn.getName();
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(CONNECTIONS_PATH); //NOI18N
        DataFolder folder = DataFolder.findFolder(fo);
        DataObject[] objects = folder.getChildren();
        
        for (int i = 0; i < objects.length; i++) {
            InstanceCookie ic = (InstanceCookie)objects[i].getCookie(InstanceCookie.class);
            if (ic != null) {
                Object obj = null;
                try {
                    obj = ic.instanceCreate();
                } catch (ClassNotFoundException e) {
                    continue;
                }
                if (obj instanceof DatabaseConnection) {
                    DatabaseConnection connection = (DatabaseConnection)obj;
                    if (connection.getName().equals(name)) {
                        objects[i].delete();
                        break;
                    }
                }
            }
        }
    }
    
    Lookup getLookup() {
        return lookup;
    }
    
    /**
     * Atomic writer for writing a changed/new database connection.
     */
    private static final class AtomicWriter implements FileSystem.AtomicAction {
        
        DatabaseConnection instance;
        MultiDataObject holder;
        String fileName;
        DataFolder parent;

        /**
         * Constructor for writing to an existing file.
         */
        AtomicWriter(DatabaseConnection instance, MultiDataObject holder) {
            this.instance = instance;
            this.holder = holder;
        }

        /**
         * Constructor for creating a new file.
         */
        AtomicWriter(DatabaseConnection instance, DataFolder parent, String fileName) {
            this.instance = instance;
            this.fileName = fileName;
            this.parent = parent;
        }

        public void run() throws java.io.IOException {
            FileLock lck;
            FileObject data;

            if (holder != null) {
                data = holder.getPrimaryEntry().getFile();
                lck = holder.getPrimaryEntry().takeLock();
            } else {
                FileObject folder = parent.getPrimaryFile();
                String fn = FileUtil.findFreeFileName(folder, fileName, "xml"); //NOI18N
                data = folder.createData(fn, "xml"); //NOI18N
                lck = data.lock();
            }

            try {
                OutputStream ostm = data.getOutputStream(lck);
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(ostm, "UTF8")); //NOI18N
                write(writer);
                writer.flush();
                writer.close();
                ostm.close();
            } finally {
                lck.releaseLock();
            }

            if (holder == null) {
                // a new DataObject must be created for instance
                // ensure that the object returned by this DO's InstanceCookie.instanceCreate()
                // method is the same as instance
                newlyCreated = data;
                newlyCreatedInstance = instance;
                holder = (MultiDataObject)DataObject.find(data);
                // ensure the Environment.Provider.getEnvironment() is called for the new DataObject
                holder.getCookie(InstanceCookie.class); 
                newlyCreated = null;
                newlyCreatedInstance = null;
            }
        }

        void write(PrintWriter pw) throws IOException {
            pw.println("<?xml version='1.0'?>"); //NOI18N
            pw.println("<!DOCTYPE connection PUBLIC '-//NetBeans//DTD Database Connection 1.0//EN' 'http://www.netbeans.org/dtds/connection-1_0.dtd'>"); //NOI18N
            pw.println("<connection>"); //NOI18N
            pw.println("  <driver-class value='" + XMLUtil.toAttributeValue(instance.getDriver()) + "'/>"); //NOI18N
            pw.println("  <driver-name value='" + XMLUtil.toAttributeValue(instance.getDriverName()) + "'/>"); // NOI18N
            pw.println("  <database-url value='" + XMLUtil.toAttributeValue(instance.getDatabase()) + "'/>"); //NOI18N
            if (instance.getSchema() != null) {
                pw.println("  <schema value='" + XMLUtil.toAttributeValue(instance.getSchema()) + "'/>"); //NOI18N
            }
            if (instance.getUser() != null) {
                pw.println("  <user value='" + XMLUtil.toAttributeValue(instance.getUser()) + "'/>"); //NOI18N
            }
            pw.println("</connection>"); //NOI18N
        }
    }

    /**
     * SAX handler for reading the XML file.
     */
    private static final class Handler extends DefaultHandler {
        
        private static final String ELEMENT_DRIVER_CLASS = "driver-class"; // NOI18N
        private static final String ELEMENT_DRIVER_NAME = "driver-name"; // NOI18N
        private static final String ELEMENT_DATABASE_URL = "database-url"; // NOI18N
        private static final String ELEMENT_SCHEMA = "schema"; // NOI18N
        private static final String ELEMENT_USER = "user"; // NOI18N
        private static final String ATTR_PROPERTY_VALUE = "value"; // NOI18N
        
        String driverClass;
        String driverName;
        String connectionUrl;
        String schema;
        String user;

        public void startDocument() throws SAXException {
        }

        public void endDocument() throws SAXException {
        }

        public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
            String value = attrs.getValue(ATTR_PROPERTY_VALUE);
            if (ELEMENT_DRIVER_CLASS.equals(qName)) {
                driverClass = value;
            } else if (ELEMENT_DRIVER_NAME.equals(qName)) {
                driverName = value;
            } else if (ELEMENT_DATABASE_URL.equals(qName)) {
                connectionUrl = value;
            } else if (ELEMENT_SCHEMA.equals(qName)) {
                schema = value;
            } else if (ELEMENT_USER.equals(qName)) {
                user = value;
            }
        }
    }
    
    private final class PCL implements PropertyChangeListener, Runnable {
        
        /**
         * The list of PropertyChangeEvent that cause the connections to be saved.
         * Should probably be a set of DatabaseConnection's instead.
         */
        LinkedList/*<PropertyChangeEvent>*/ keepAlive = new LinkedList();
        
        RequestProcessor.Task saveTask = null;
        
        public void propertyChange(PropertyChangeEvent evt) {
            synchronized (this) {
                if (saveTask == null)
                    saveTask = RequestProcessor.getDefault().create(this);
                keepAlive.add(evt);
            }
            saveTask.schedule(DELAY);
        }
        
        public void run() {
            PropertyChangeEvent e;

            synchronized (this) {
                e = (PropertyChangeEvent)keepAlive.removeFirst();
            }
            DatabaseConnection dbconn = (DatabaseConnection)e.getSource();
            XMLDataObject obj = getHolder();
            if (obj == null) {
                return;
            }
            try {
                obj.getPrimaryFile().getFileSystem().runAtomicAction(new AtomicWriter(dbconn, obj));
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.INFO, null, ex);
            }
        }
    }
}
