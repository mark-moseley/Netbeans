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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.java.source.support;

import java.util.Arrays;
import java.util.HashSet;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.JavaSource.Priority;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jan Lahoda
 */
public class LookupBasedJavaSourceTaskFactoryTest extends NbTestCase {
    
    public LookupBasedJavaSourceTaskFactoryTest(String testName) {
        super(testName);
    }

    private FileObject testDir;
    private FileObject testFile1;
    private FileObject testFile2;
    private DataObject testFile1DO;
    private DataObject testFile2DO;
    private Node testFile1Node;
    private Node testFile2Node;
    
    @Override
    protected void setUp() throws Exception {
        testDir = SourceUtilsTestUtil.makeScratchDir(this);
        testFile1 = testDir.createData("test1.java");
        testFile2 = testDir.createData("test2.java");
        testFile1DO = DataObject.find(testFile1);
        testFile2DO = DataObject.find(testFile2);
        testFile1Node = testFile1DO.getNodeDelegate();
        testFile2Node = testFile2DO.getNodeDelegate();
    }
    
    public void testFactoryListensOnLookupChanges() throws Exception {
        int[] changeCount = new int[1];
        LookupBasedJavaSourceTaskFactory factory = new LookupBasedJavaSourceTaskFactoryImpl(changeCount);
        ChangeableLookup lookup = new ChangeableLookup();
        
        factory.setLookup(lookup);
        
        assertEquals(1, changeCount[0]);
        assertEquals(0, factory.getFileObjects().size());
        
        lookup.setLookupsImpl(new Lookup[] {Lookups.singleton(testFile1)});
        
        assertEquals(2, changeCount[0]);
        assertEquals(1, factory.getFileObjects().size());
        assertEquals(testFile1, factory.getFileObjects().get(0));
        
        lookup.setLookupsImpl(new Lookup[] {Lookups.singleton(testFile2)});
        
        assertEquals(3, changeCount[0]);
        assertEquals(1, factory.getFileObjects().size());
        assertEquals(testFile2, factory.getFileObjects().get(0));
        
        lookup.setLookupsImpl(new Lookup[] {});
        
        assertEquals(4, changeCount[0]);
        assertEquals(0, factory.getFileObjects().size());
        
        lookup.setLookupsImpl(new Lookup[] {Lookups.fixed(new Object[] {testFile1, testFile2})});
        
        assertEquals(5, changeCount[0]);
        assertEquals(2, factory.getFileObjects().size());
        assertEquals(new HashSet<FileObject>(Arrays.asList(testFile1, testFile2)), new HashSet<FileObject>(factory.getFileObjects()));
        
        lookup.setLookupsImpl(new Lookup[] {});
        
        assertEquals(6, changeCount[0]);
        assertEquals(0, factory.getFileObjects().size());
        
        lookup.setLookupsImpl(new Lookup[] {Lookups.singleton(testFile1DO)});
        
        assertEquals(7, changeCount[0]);
        assertEquals(1, factory.getFileObjects().size());
        assertEquals(testFile1, factory.getFileObjects().get(0));
        
        lookup.setLookupsImpl(new Lookup[] {Lookups.fixed(new Object[] {testFile1DO, testFile1Node})});
        
        assertEquals(8, changeCount[0]);
        assertEquals(1, factory.getFileObjects().size());
        assertEquals(testFile1, factory.getFileObjects().get(0));
        
        lookup.setLookupsImpl(new Lookup[] {Lookups.singleton(testFile2Node)});
        
        assertEquals(10, changeCount[0]);
        assertEquals(1, factory.getFileObjects().size());
        assertEquals(testFile2, factory.getFileObjects().get(0));
    }

    private static class LookupBasedJavaSourceTaskFactoryImpl extends LookupBasedJavaSourceTaskFactory {
        
        private int[] changeCount;
        
        public LookupBasedJavaSourceTaskFactoryImpl(int[] changeCount) {
            super(Phase.PARSED, Priority.MIN);
            this.changeCount = changeCount;
        }
        
        public CancellableTask<CompilationInfo> createTask(FileObject file) {
            return new CancellableTask<CompilationInfo>() {
                public void cancel() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
                public void run(CompilationInfo parameter) throws Exception {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
        }
        
        @Override
        protected void lookupContentChanged() {
            changeCount[0]++;
        }
        
    }
    
    private static class ChangeableLookup extends ProxyLookup {
        
        public void setLookupsImpl(Lookup[] lookups) {
            setLookups(lookups);
        }
    }
}
