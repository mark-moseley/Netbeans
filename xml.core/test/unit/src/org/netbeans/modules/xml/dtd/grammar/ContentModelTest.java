/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.dtd.grammar;

import junit.framework.*;
import org.netbeans.junit.*;
import java.util.*;
import java.util.StringTokenizer;
import org.openide.util.enum.*;

/**
 *
 * @author Petr Kuzel
 */
public class ContentModelTest extends NbTestCase {
    
    public ContentModelTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(ContentModelTest.class);
        
        return suite;
    }
    
    /** Test of parseContentModel method, of class org.netbeans.modules.xml.text.completion.dtd.ContentModel. */
    public void testParseContentModel() {
        System.out.println("testParseContentModel");

        // test for exceptions only
        
        try {
            ContentModel.parseContentModel("(simple)");
            ContentModel.parseContentModel("(se,qu,en,ce)");
            ContentModel.parseContentModel("(ch|oi|ce)");
            ContentModel.parseContentModel("(opt?,mand+,end)");
            ContentModel.parseContentModel("(#PCDATA|opt|mand+|end)");
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }
    
    /** Test of whatCanFollow method, of class org.netbeans.modules.xml.text.completion.dtd.ContentModel. */
    public void testWhatCanFollow() {
        System.out.println("testWhatCanFollow");

        Enumeration in, gold;
        
        // test Element and multiplicity group models ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        in = new InputEnumeration("");
        gold = new InputEnumeration("element");
        probe("(element)", in, gold);        
        
        in = new InputEnumeration("element");
        gold = new InputEnumeration("element");
        probe("(element)*", in, gold);        
        
        in = new InputEnumeration("element");
        gold = new InputEnumeration("element");
        probe("(element)+", in, gold);        
        
        in = new InputEnumeration("element");
        gold = new InputEnumeration("");
        probe("(element)?", in, gold);        
        

        in = new InputEnumeration("element");
        gold = new InputEnumeration("");
        probe("(element)", in, gold);        

        in = new InputEnumeration("invalid-element");
        gold = null;
        probe("(element)", in, gold);        
        
        // test sequence ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        
        in = new InputEnumeration("se qu");
        gold = new InputEnumeration("en");
        probe("(se,qu,en,ce)", in, gold);        

        in = new InputEnumeration("se invalid-qu");
        gold = null;
        probe("(se,qu,en,ce)", in, gold);        

        // test choice ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        
        in = new InputEnumeration("");
        gold = new InputEnumeration("element element2");
        probe("(element|element2)", in, gold);

        in = new InputEnumeration("invalid-element");        
        gold = null;
        probe("(element|element2)", in, gold);

        in = new InputEnumeration("element");
        gold = new InputEnumeration("");
        probe("(element|element2)", in, gold);

        in = new InputEnumeration("element2");
        gold = new InputEnumeration("");
        probe("(element|element2)", in, gold);

        in = new InputEnumeration("");
        gold = new InputEnumeration("element");
        probe("(element|element)", in, gold);
        
        
        // test options in sequence ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        
        in = new InputEnumeration("se qu");
        gold = new InputEnumeration("en ce");        
        probe("(se,qu,en?,ce)", in, gold);
        
        
        // test a choice of conflicting sequences

        in = new InputEnumeration("conflict");
        gold = new InputEnumeration("qu ce se");        
        probe("((conflict,qu) | (conflict,ce?,se))", in, gold);
                
    }

    /**
     * Perform whatCanFollow() and compare it to expected result.
     */
    private void probe(final String modelDesc, final Enumeration in, final Enumeration gold) {
        System.out.println("Probing: " + modelDesc + " for: " + in);
                
        ContentModel model = ContentModel.parseContentModel(modelDesc);
        
        Enumeration out = model.whatCanFollow(in);
        
        if (gold != null) {
            assertNotNull("\tNon-null enumeration expected!", out);
            ProbeEnum outp = new ProbeEnum(out);
            ProbeEnum goldp = new ProbeEnum(gold);
            assertEquals("Enums must be same!", goldp, outp);            
        } else {
            assertNull("Null result expected.", out);
        }

    }

    /**
     * Subclass StringTokenizer for better toString() reports.
     */
    private static class InputEnumeration extends StringTokenizer {
        
        private final String in;
        
        InputEnumeration(String in) {
            super(in);
            this.in = in;
        }
        
        public String toString() {
            return in;
        }
    }
    
    /**
     * Two enumerations are same if contains the sama value <b>set</b>.
     */
    private class ProbeEnum {
        
        private List list = new ArrayList(9);        
        private Set set = new HashSet(9);
        
        public ProbeEnum(Enumeration en) {
            while (en.hasMoreElements()) {
                Object next = en.nextElement();
                list.add(next);
                set.add(next);
            }
        }
        
        public boolean equals(Object obj) {
            if (obj instanceof ProbeEnum) return equals((ProbeEnum) obj);
            return super.equals(obj);
        }
        
        public boolean equals(ProbeEnum peer) {
            return set.containsAll(peer.set) && peer.set.containsAll(set);
        }
        
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            for (Iterator it = list.iterator(); it.hasNext(); ) {
                Object next = it.next();
                buffer.append(next.toString() + ",");
            }
            return buffer.toString();
        }
    }
}
