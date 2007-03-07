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

package org.netbeans.core.projects;

import org.netbeans.junit.*;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.core.startup.ModuleHistory;

import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.nodes.Node;
import org.openide.loaders.DataObject;
import java.io.File;
import java.util.Collections;
import java.awt.Toolkit;
import java.awt.Image;
import java.net.URL;
import java.beans.BeanInfo;
import java.awt.image.PixelGrabber;
import java.awt.image.ImageObserver;
import org.netbeans.core.startup.MainLookup;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/** Test operation of the SystemFileSystem.
 * For now, just display attributes.
 * @author Jesse Glick
 */
public class SystemFileSystemTest extends NbTestCase {
    
    public SystemFileSystemTest(String name) {
        super(name);
    }
    
    private ModuleManager mgr;
    private File satJar;
    private Module satModule;
    protected void setUp() throws Exception {
        mgr = org.netbeans.core.startup.Main.getModuleSystem().getManager();
        org.netbeans.core.startup.Main.initializeURLFactory ();
        try {
            mgr.mutex().readAccess(new Mutex.ExceptionAction() {
                public Object run() throws Exception {
                    satJar = new File(SystemFileSystemTest.class.getResource("data/sfs-attr-test.jar").getFile());
                    satModule = mgr.create(satJar, new ModuleHistory(satJar.getAbsolutePath()), false, false, false);
                    assertEquals("no problems installing sfs-attr-test.jar", Collections.EMPTY_SET, satModule.getProblems());
                    mgr.enable(satModule);
                    return null;
                }
            });
        } catch (MutexException me) {
            throw me.getException();
        }
    }
    protected void tearDown() throws Exception {
        try {
            mgr.mutex().readAccess(new Mutex.ExceptionAction() {
                public Object run() throws Exception {
                    mgr.disable(satModule);
                    mgr.delete(satModule);
                    return null;
                }
            });
        } catch (MutexException me) {
            throw me.getException();
        }
        satModule = null;
        satJar = null;
        mgr = null;
    }
    
    public void testLocalizingBundle() throws Exception {
        FileObject bar = Repository.getDefault().getDefaultFileSystem().findResource("foo/bar.txt");
        Node n = DataObject.find(bar).getNodeDelegate();
        assertEquals("correct localized data object name", "Localized Name", n.getDisplayName());
    }
    
    public void testContentOfFileSystemIsInfluencedByLookup () throws Exception {
        FileSystem mem = FileUtil.createMemoryFileSystem();
        String dir = "/yarda/own/file";
        org.openide.filesystems.FileUtil.createFolder (mem.getRoot (), dir);
        
        assertNull ("File is not there yet", Repository.getDefault ().getDefaultFileSystem ().findResource (dir));
        MainLookup.register (mem);
        try {
            assertNotNull ("The file is there now", Repository.getDefault ().getDefaultFileSystem ().findResource (dir));
        } finally {
            MainLookup.unregister (mem);
        }
        assertNull ("File is no longer there", Repository.getDefault ().getDefaultFileSystem ().findResource (dir));
    }
    
    public void testIconFromURL() throws Exception {
        FileObject bar = Repository.getDefault().getDefaultFileSystem().findResource("foo/bar.txt");
        Node n = DataObject.find(bar).getNodeDelegate();
        Image reference = Toolkit.getDefaultToolkit().createImage(new URL("jar:" + satJar.toURL() + "!/sfs_attr_test/main.gif"));
        Image tested = n.getIcon(BeanInfo.ICON_COLOR_16x16);
        int h1 = imageHash("main.gif", reference, 16, 16);
        int h2 = imageHash("bar.txt icon", tested, 16, 16);
        assertEquals("correct icon", h1, h2);
    }
    
    /** @see "#18832" */
    public void testIconFromImageMethod() throws Exception {
        FileObject baz = Repository.getDefault().getDefaultFileSystem().findResource("foo/baz.txt");
        Node n = DataObject.find(baz).getNodeDelegate();
        Image reference = Toolkit.getDefaultToolkit().createImage(new URL("jar:" + satJar.toURL() + "!/sfs_attr_test/main-plus-badge.gif"));
        Image tested = n.getIcon(BeanInfo.ICON_COLOR_16x16);
        int h1 = imageHash("main-plus-badge.gif", reference, 16, 16);
        int h2 = imageHash("baz.txt icon", tested, 16, 16);
        assertEquals("correct icon", h1, h2);
    }
    
    private static int imageHash(String name, Image img, int w, int h) throws InterruptedException {
        int[] pixels = new int[w * h];
        PixelGrabber pix = new PixelGrabber(img, 0, 0, w, h, pixels, 0, w);
        pix.grabPixels();
        assertEquals(0, pix.getStatus() & ImageObserver.ABORT);
        if (false) {
            // Debugging.
            System.out.println("Pixels of " + name + ":");
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (x == 0) {
                        System.out.print('\t');
                    } else {
                        System.out.print(' ');
                    }
                    int p = pixels[y * w + x];
                    String hex = Integer.toHexString(p);
                    while (hex.length() < 8) {
                        hex = "0" + hex;
                    }
                    System.out.print(hex);
                    if (x == w - 1) {
                        System.out.print('\n');
                    }
                }
            }
        }
        int hash = 0;
        for (int i = 0; i < pixels.length; i++) {
            hash += 172881;
            int p = pixels[i];
            if ((p & 0xff000000) == 0) {
                // Transparent; normalize.
                p = 0;
            }
            hash ^= p;
        }
        return hash;
    }
    
}
