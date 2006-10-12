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

package org.netbeans.modules.j2ee.ddloaders.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Enumeration;
import javax.swing.Action;
import junit.framework.Assert;
import org.netbeans.junit.NbTestCase;
import org.openide.actions.OpenAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Test for {@link EarDataNode}.
 *
 * @author Martin Krauskopf
 */
public class EarDataNodeTest extends NbTestCase {
    
    // Copied from org.netbeans.api.project.TestUtil:
    static {
        // XXX replace with MockServices
        System.setProperty("org.openide.util.Lookup", Lkp.class.getName());
        Assert.assertEquals(Lkp.class, Lookup.getDefault().getClass());
    }
    
    public static final class Lkp extends ProxyLookup {
        private static Lkp DEFAULT;
        public Lkp() {
            Assert.assertNull(DEFAULT);
            DEFAULT = this;
            setLookup(new Object[0]);
        }
        public static void setLookup(Object[] instances) {
            ClassLoader l = Lkp.class.getClassLoader();
            DEFAULT.setLookups(new Lookup[] {
                Lookups.fixed(instances),
                Lookups.metaInfServices(l),
                Lookups.singleton(l),
            });
        }
    }
    
    public EarDataNodeTest(String testName) throws Exception {
        super(testName);
        Lkp.setLookup(new Object[] {
            new Pool(),
            new MR(),
            new Repo()
        });
    }
    
    public void testGetActions() throws Exception {
        File ddFile = new File(getWorkDir(), "application.xml");
        String ddContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<application version=\"1.4\" xmlns=\"http://java.sun.com/xml/ns/j2ee\" " +
                "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee " +
                "http://java.sun.com/xml/ns/j2ee/application_1_4.xsd\">" +
                "</application>";
        EarDataNodeTest.dump(ddFile, ddContent);
        FileObject fo = FileUtil.toFileObject(ddFile);
        EarDataObject edo = (EarDataObject) DataObject.find(fo);
        Action[] action = edo.getNodeDelegate().getActions(false);
        for (int i = 0; i < action.length; i++) {
            assertFalse("OpenAction is not present yet", action[i] instanceof OpenAction);
        }
    }
    
    public static void dump(File f, String contents) throws IOException {
        f.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(f);
        try {
            Writer w = new OutputStreamWriter(os, "UTF-8");
            w.write(contents);
            w.flush();
        } finally {
            os.close();
        }
    }
    
    private static final class Pool extends DataLoaderPool {
        
        protected Enumeration loaders() {
            return Enumerations.singleton(DataLoader.getLoader(EarDataLoader.class));
        }
        
    }
    
    private static final class MR extends MIMEResolver {
        
        public String findMIMEType(FileObject fo) {
            return fo.getNameExt().equals("application.xml")
                    ? EarDataLoader.REQUIRED_MIME_PREFIX_1 : null;
        }
        
    }
    
    private static final class Repo extends Repository {
        
        public Repo() throws Exception {
            super(mksystem());
        }
        
        private static FileSystem mksystem() throws Exception {
            URL layerFile = Repo.class.getClassLoader().getResource(
                    "org/netbeans/modules/j2ee/ddloaders/resources/layer.xml");
            assert layerFile != null;
            MultiFileSystem mfs = new MultiFileSystem(new FileSystem[] {
                new XMLFileSystem(layerFile)
            });
            return mfs;
        }
        
    }
    
}
