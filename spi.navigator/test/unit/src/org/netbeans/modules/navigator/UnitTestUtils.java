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

package org.netbeans.modules.navigator;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.swing.text.Utilities;
import junit.framework.Assert;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.xml.sax.SAXException;

/**
 * Allows tests to install own layers for testing.
 * Copied from org.netbeans.api.project.TestUtil.
 *
 * @author Dafe Simonek
 */
public class UnitTestUtils extends ProxyLookup {

    public static UnitTestUtils DEFAULT_LOOKUP = null;

    /** Creates a new instance of UnitTestUtils */
    public UnitTestUtils() {
        Assert.assertNull(DEFAULT_LOOKUP);
        DEFAULT_LOOKUP = this;
    }
    
    /** Makes global layer from given string resource info */
    public static void prepareTest(String[] stringLayers) 
                throws IOException, SAXException, PropertyVetoException {
        prepareTest(stringLayers, null);
    }
    
    public static void prepareTest (String[] stringLayers, Lookup lkp) 
                throws IOException, SAXException, PropertyVetoException {
        URL[] layers = new URL[stringLayers.length];
        
        for (int cntr = 0; cntr < layers.length; cntr++) {
            layers[cntr] = Utilities.class.getResource(stringLayers[cntr]);
        }
        
        XMLFileSystem system = new XMLFileSystem();
        system.setXmlUrls(layers);
        
        Repository repository = new Repository(system);
        
        if (lkp == null) {
            DEFAULT_LOOKUP.setLookup(new Object[] { repository }, UnitTestUtils.class.getClassLoader());
        } else {
            DEFAULT_LOOKUP.setLookup(new Object[] { repository }, lkp, UnitTestUtils.class.getClassLoader());
        }
    }
    
    /**
     * Set the global default lookup with some fixed instances including META-INF/services/*.
     */
    private static void setLookup(Object[] instances, ClassLoader cl) {
        DEFAULT_LOOKUP.setLookups(new Lookup[] {
            Lookups.fixed(instances),
            Lookups.metaInfServices(cl),
            Lookups.singleton(cl),
        });
    }
    
    private static void setLookup(Object[] instances, Lookup lkp, ClassLoader cl) {
        DEFAULT_LOOKUP.setLookups(new Lookup[] {
            lkp,        
            Lookups.fixed(instances),
            Lookups.metaInfServices(cl),
            Lookups.singleton(cl),
        });
    }
    
    
    static {
        UnitTestUtils.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", UnitTestUtils.class.getName());
        Assert.assertEquals(UnitTestUtils.class, Lookup.getDefault().getClass());
    }
    
    public static void initLookup() {
        //currently nothing.
    }

}
