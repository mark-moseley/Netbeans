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

package org.apache.tools.ant.module.api.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

// XXX tests needed: runTarget (perhaps)

/**
 * Test functionality of ActionUtils.
 * @author Jesse Glick
 */
public class ActionUtilsTest extends NbTestCase {
    
    public ActionUtilsTest(String name) {
        super(name);
    }
    
    private FileObject dir, f1, f2, subdir, f3, fx, subdir2, f3a, f4, subsubdir, f5, f5a;
    private DataObject d1, d2, d3, dx;
    private Node n1, n2, n3, nx;
    
    protected void setUp() throws Exception {
        super.setUp();
        LocalFileSystem fs = new LocalFileSystem();
        fs.setRootDirectory(getWorkDir());
        dir = fs.getRoot();
        f1 = dir.createData("f1.data");
        f2 = dir.createData("f2");
        subdir = dir.createFolder("sub");
        f3 = subdir.createData("f3.data");
        f3a = subdir.createData("f3a.data");
        subdir2 = dir.createFolder("subdir2");
        f4 = subdir2.createData("f3.nondata");
        subsubdir = subdir2.createFolder("sub");
        f5 = subdir2.createData("f1.data");
        f5a = subsubdir.createData("f3.data");
        fx = new XMLFileSystem().getRoot();
        d1 = DataObject.find(f1);
        d2 = DataObject.find(f2);
        d3 = DataObject.find(f3);
        dx = DataObject.find(fx);
        n1 = d1.getNodeDelegate();
        n2 = d2.getNodeDelegate();
        n3 = d3.getNodeDelegate();
        nx = dx.getNodeDelegate();
    }
    
    public void testFindSelectedFiles() throws Exception {
        assertEquals("one selected file", Collections.singletonList(f1), filesFrom(new Node[] {n1}, null, null, true));
        assertEquals("two selected files", Arrays.asList(new FileObject[] {f1, f2}), filesFrom(new Node[] {n1, n2}, null, null, true));
        assertEquals("zero selection", null, filesFrom(new Node[0], null, null, true));
        assertEquals("not a file selection", null, filesFrom(new Node[] {Node.EMPTY}, null, null, true));
        assertEquals("not a disk file", null, filesFrom(new Node[] {nx}, null, null, true));
        assertEquals("order significant", Arrays.asList(new FileObject[] {f2, f1}), filesFrom(new Node[] {n2, n1}, null, null, true));
        assertEquals("one disk file", Collections.singletonList(f1), filesFrom(new Node[] {n1, nx}, null, null, false));
        assertEquals("one non-disk file", null, filesFrom(new Node[] {n1, nx}, null, null, true));
        assertEquals("one *.data", Collections.singletonList(f1), filesFrom(new Node[] {n1, n2}, null, ".data", false));
        assertEquals("one not *.data", null, filesFrom(new Node[] {n1, n2}, null, ".data", true));
        assertEquals("one file in sub/", Collections.singletonList(f3), filesFrom(new Node[] {n1, n3}, subdir, null, false));
        assertEquals("one not in sub/", null, filesFrom(new Node[] {n1, n3}, subdir, null, true));
        assertEquals("one sub/*.data", Collections.singletonList(f3), filesFrom(new Node[] {n3}, subdir, ".data", true));
    }
    
    private static Lookup context(Node[] sel) {
        Lookup[] delegates = new Lookup[sel.length + 1];
        for (int i = 0; i < sel.length; i++) {
            delegates[i] = sel[i].getLookup();
        }
        delegates[sel.length] = Lookups.fixed(sel);
        return new ProxyLookup(delegates);
    }
    
    private static List/*<FileObject>*/ filesFrom(Node[] sel, FileObject dir, String suffix, boolean strict) {
        return files2List(ActionUtils.findSelectedFiles(context(sel), dir, suffix, strict));
    }
    
    public void testAntIncludesList() throws Exception {
        assertEquals("2 includes", "f1.data,sub/f3.data", ActionUtils.antIncludesList(new FileObject[] {f1, f3}, dir));
        assertEquals("1 include", "f1.data", ActionUtils.antIncludesList(new FileObject[] {f1}, dir));
        assertEquals("no includes", "", ActionUtils.antIncludesList(new FileObject[0], dir));
    }
    
    public void testRegexpMapFiles() throws Exception {
        Pattern fromRx = Pattern.compile("\\.data$");
        String toSubst = ".nondata";
        assertEquals("mapped one file", Collections.singletonList(f4), files2List(
            ActionUtils.regexpMapFiles(new FileObject[] {f3, f3a}, subdir, fromRx, subdir2, toSubst, false)));
        assertEquals("did not map one file", null, files2List(
            ActionUtils.regexpMapFiles(new FileObject[] {f3, f3a}, subdir, fromRx, subdir2, toSubst, true)));
        assertEquals("mapped two file", Arrays.asList(new FileObject[] {f5, f5a}), files2List(
            ActionUtils.regexpMapFiles(new FileObject[] {f1, f3}, dir, null, subdir2, null, true)));
        // XXX test that files which match a regexp, but are substituted to be the same thing, still are OK
    }
    
    private static List/*<FileObject>*/ files2List(FileObject[] files) {
        return files != null ? Arrays.asList(files) : null;
    }
    
}
