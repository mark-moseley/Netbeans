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

package org.netbeans.modules.project.libraries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.Lookup;
import org.openide.xml.EntityCatalog;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



/**
 *
 * @author Tomas Zezula
 */
public class LibrariesStorageTest extends NbTestCase {
    
    private FileObject storageFolder;
    LibrariesStorage storage;
    
    public LibrariesStorageTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setLookup(new Object[] {
            new TestEntityCatalog()});
        this.registerLibraryTypeProvider();
        this.storageFolder = TestUtil.makeScratchDir(this);
        this.createLibraryDefinition(this.storageFolder,"Library1");
        this.storage = new LibrariesStorage (this.storageFolder);
    }

    protected void tearDown() throws Exception {
    }
        

    public void testGetLibraries() throws Exception {
        this.storage.getLibraries();
        LibraryImplementation[] libs = this.storage.getLibraries();
        assertEquals("Libraries count",1,libs.length);
        assertLibEquals(libs, new String[] {"Library1"});
        createLibraryDefinition(this.storageFolder,"Library2");
        libs = this.storage.getLibraries();
        assertEquals("Libraries count",2,libs.length);
        assertLibEquals(libs, new String[] {"Library1", "Library2"});
        TestListener l = new TestListener ();
        this.storage.addPropertyChangeListener(l);                
        TestLibraryTypeProvider tlp = (TestLibraryTypeProvider) LibraryTypeRegistry.getDefault().getLibraryTypeProvider (TestLibraryTypeProvider.TYPE);
        tlp.reset();
        createLibraryDefinition(this.storageFolder,"Library3");
        libs = this.storage.getLibraries();
        assertEquals("Libraries count",3,libs.length);
        assertLibEquals(libs, new String[] {"Library1", "Library2", "Library3"});
        assertEquals("Event count",1,l.getEventNames().size());        
        assertEquals("Event names",LibraryProvider.PROP_LIBRARIES,l.getEventNames().get(0));                
        assertTrue("Library created called",tlp.wasCreatedCalled());        
    }

    public void testAddLibrary() throws Exception {
        this.storage.getLibraries();
        LibraryImplementation[] libs = this.storage.getLibraries();
        assertEquals("Libraries count",1,libs.length);
        assertLibEquals(libs, new String[] {"Library1"});
        TestLibraryTypeProvider tlp = (TestLibraryTypeProvider) LibraryTypeRegistry.getDefault().getLibraryTypeProvider (TestLibraryTypeProvider.TYPE);
        tlp.reset();
        LibraryImplementation impl = new TestLibrary("Library2");
        this.storage.addLibrary(impl);
        libs = this.storage.getLibraries();
        assertEquals("Libraries count",2,libs.length);
        assertLibEquals(libs, new String[] {"Library1","Library2"});
        assertTrue (tlp.wasCreatedCalled());
    }

    public void testRemoveLibrary() throws Exception {
        this.storage.getLibraries();
        LibraryImplementation[] libs = this.storage.getLibraries();
        assertEquals("Libraries count",1,libs.length);
        assertLibEquals(libs, new String[] {"Library1"});
        TestLibraryTypeProvider tlp = (TestLibraryTypeProvider) LibraryTypeRegistry.getDefault().getLibraryTypeProvider (TestLibraryTypeProvider.TYPE);
        tlp.reset();
        this.storage.removeLibrary(libs[0]);
        libs = this.storage.getLibraries();
        assertEquals("Libraries count",0,libs.length);
        assertTrue ("Library deleted called",  tlp.wasDeletedCalled());
    }

    public void testUpdateLibrary() throws Exception {
        this.storage.getLibraries();
        LibraryImplementation[] libs = this.storage.getLibraries();
        assertEquals("Libraries count",1,libs.length);
        assertLibEquals(libs, new String[] {"Library1"});
        TestLibraryTypeProvider tlp = (TestLibraryTypeProvider) LibraryTypeRegistry.getDefault().getLibraryTypeProvider (TestLibraryTypeProvider.TYPE);
        tlp.reset();
        LibraryImplementation newLib = new TestLibrary ((TestLibrary)libs[0]);
        newLib.setName ("NewLibrary");
        this.storage.updateLibrary(libs[0],newLib);
        libs = this.storage.getLibraries();
        assertEquals("Libraries count",1,libs.length);
        assertLibEquals(libs, new String[] {"NewLibrary"});
        assertTrue ("Library created called",  tlp.wasCreatedCalled());
    }
    
    private static void assertLibEquals (LibraryImplementation[] libs, String[] names) {
        assertEquals("Libraries Equals (size)",names.length,libs.length);
        Set s = new HashSet (Arrays.asList(names)); //Ordering is not important
        for (int i=0; i<libs.length; i++) {
            String name = libs[i].getName();
            assertTrue("Libraries Equals (unknown library "+name+")", s.remove(name));
        }
    }
    
    private static void registerLibraryTypeProvider () throws Exception {
        StringTokenizer tk = new StringTokenizer("org-netbeans-api-project-libraries/LibraryTypeProviders","/");
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        while (tk.hasMoreElements()) {
            String pathElement = tk.nextToken();
            FileObject tmp = root.getFileObject(pathElement);
            if (tmp == null) {
                tmp = root.createFolder(pathElement);
            }
            root = tmp;
        }
        if (root.getChildren().length == 0) {
//            FileObject inst = root.createData("TestLibraryTypeProvider","instance");
//            inst.setAttribute("newvalue","")
            InstanceDataObject.create (DataFolder.findFolder(root),"TestLibraryTypeProvider",TestLibraryTypeProvider.class);
        }
    }
    
    private static void createLibraryDefinition (final FileObject storageFolder, final String libName) throws IOException {
        storageFolder.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run () throws IOException {
                FileObject defFile = storageFolder.createData(libName,"xml");
                FileLock lock = null;
                PrintWriter out = null;
                try {
                    lock = defFile.lock();
                    out = new PrintWriter(new OutputStreamWriter(defFile.getOutputStream (lock),"UTF-8"));
                    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");      //NOI18N
                    out.println("<!DOCTYPE library PUBLIC \"-//NetBeans//DTD Library Declaration 1.0//EN\" \"http://www.netbeans.org/dtds/library-declaration-1_0.dtd\">"); 
                    out.println("<library version=\"1.0\">");
                    out.println("\t<name>"+libName+"</name>");
                    out.println("\t<type>"+TestLibraryTypeProvider.TYPE+"</type>");
                    for (int i = 0; i < TestLibraryTypeProvider.supportedTypes.length; i++) {
                        out.println("\t<volume>");
                        out.println ("\t\t<type>"+TestLibraryTypeProvider.supportedTypes[i]+"</type>");
                        out.println("\t</volume>");
                    }
                    out.println("</library>");
                } finally {
                    if (out !=  null)
                        out.close();
                    if (lock != null)
                        lock.releaseLock();
                }
            }
        });
    }
    
    private static class TestListener implements PropertyChangeListener {
        
        private List eventNames = new ArrayList ();
        
        public List getEventNames () {
            return this.eventNames;
        }
        
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            this.eventNames.add (propertyChangeEvent.getPropertyName());
        }
        
        public void reset () {
            this.eventNames.clear();
        }
        
    }
    
    
    private static class TestEntityCatalog extends EntityCatalog {        
        
        private static final String DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!ELEMENT library (name, type, description?, localizing-bundle?, volume*) >\n" +
            "<!ATTLIST library version CDATA #FIXED \"1.0\" >\n" +
            "<!ELEMENT name (#PCDATA) >\n" +
            "<!ELEMENT description (#PCDATA) >\n" +
            "<!ELEMENT localizing-bundle (#PCDATA)>\n" +
            "<!ELEMENT volume (type, resource*) >\n" +
            "<!ELEMENT type (#PCDATA) >\n" +
            "<!ELEMENT resource (#PCDATA) >\n";
        
        public InputSource resolveEntity(String str, String str1) throws SAXException, IOException {
            if ("-//NetBeans//DTD Library Declaration 1.0//EN".equals(str)) {
                InputSource in = new InputSource (new StringReader(DTD));            
                return in;
            }
            else {
                return null;
            }
        }                
    }
    
    public static class TestLibraryTypeProvider implements LibraryTypeProvider, java.io.Serializable {
        
        static final String[] supportedTypes = new String[] {"bin","src"};
        
        static final String TYPE = "Test";
        
        private boolean createdCalled;
        
        private boolean deletedCalled;
        
        public java.beans.Customizer getCustomizer(String volumeType) {
            return null;
        }

        public void libraryDeleted(LibraryImplementation libraryImpl) {
            this.deletedCalled = true;
        }

        public void libraryCreated(LibraryImplementation libraryImpl) {
            this.createdCalled = true;
        }
        
        public void reset () {
            this.createdCalled = false;
            this.deletedCalled = false;
        }
        
        public boolean wasCreatedCalled () {
            return this.createdCalled;
        }
        
        public boolean wasDeletedCalled () {
            return this.deletedCalled;
        }

        public String[] getSupportedVolumeTypes() {
            return supportedTypes;
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        public String getLibraryType() {
            return TYPE;
        }

        public String getDisplayName() {
            return "Test Library Type";
        }

        public LibraryImplementation createLibrary() {
            return new TestLibrary ();
        }
        
    }
    
    private static class TestLibrary implements LibraryImplementation {
        
        private String name;
        private String locBundle;
        private String description;
        private Map contents;
        private PropertyChangeSupport support;
        
        public TestLibrary () {
            this.support = new PropertyChangeSupport (this);
            this.contents = new HashMap (2);
        }
        
        public TestLibrary (String name) {
            this ();            
            this.name = name;
        }
        
        public TestLibrary (TestLibrary lib) {
            this ();
            this.name = lib.name;
            this.locBundle = lib.locBundle;
            this.description = lib.description;
            this.contents = lib.contents;
        }
        
        public String getType() {
            return TestLibraryTypeProvider.TYPE;
        }
        
        public String getName () {
            return this.name;
        }
        
        public void setName(String name) {
            this.name = name;
            this.support.firePropertyChange(PROP_NAME,null,null);
        }
        
        public String getLocalizingBundle() {
            return this.locBundle;
        }

        public void setLocalizingBundle(String resourceName) {
            this.locBundle = resourceName;
            this.support.firePropertyChange("localizingBundle",null,null);
        }
        
        public String getDescription() {
            return this.description;
        }

        public void setDescription(String text) {
            this.description = text;
            this.support.firePropertyChange(PROP_DESCRIPTION,null,null);
        }

        public List getContent(String volumeType) throws IllegalArgumentException {
            for (int i=0; i< TestLibraryTypeProvider.supportedTypes.length; i++) {
                if (TestLibraryTypeProvider.supportedTypes[i].equals (volumeType)) {
                    List l = (List) this.contents.get (volumeType);
                    if (l == null) {
                        l = Collections.EMPTY_LIST;
                    }
                    return l;
                }
            }
            throw new IllegalArgumentException ();
        }

        public void setContent(String volumeType, List path) throws IllegalArgumentException {
            for (int i=0; i< TestLibraryTypeProvider.supportedTypes.length; i++) {
                if (TestLibraryTypeProvider.supportedTypes[i].equals (volumeType)) {
                    List l = (List) this.contents.put (volumeType, path);
                    this.support.firePropertyChange(PROP_CONTENT,null,null);
                    return;
                }
            }
            throw new IllegalArgumentException ();
        }                       
        
        public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
            this.support.addPropertyChangeListener(l);
        }
        
        public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
            this.support.removePropertyChangeListener(l);
        }                
    }
    
}
