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
 * The Original Software is NetBeans.
 * The Initial Developer of the Original Software is Sun Microsystems, Inc.
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2003
 * All Rights Reserved.
 *
 * Contributor(s): Sun Microsystems, Inc.
 */

package gui.debuggercore;

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.actions.RunToCursorAction;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.PNGEncoder;
import org.netbeans.junit.NbTestSuite;
import org.openide.nodes.Node;

/**
 *
 * @author ehucka
 */
public class Watches extends JellyTestCase {
    
    /**
     *
     * @param name
     */
    public Watches(String name) {
        super(name);
    }
    
    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }
    
    /**
     *
     * @return
     */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new Watches("testWatchesPublicVariables"));
        suite.addTest(new Watches("testWatchesProtectedVariables"));
        suite.addTest(new Watches("testWatchesPrivateVariables"));
        suite.addTest(new Watches("testWatchesPackagePrivateVariables"));
        suite.addTest(new Watches("testWatchesFiltersBasic"));
        suite.addTest(new Watches("testWatchesFiltersLinkedList"));
        suite.addTest(new Watches("testWatchesFiltersArrayList"));
        suite.addTest(new Watches("testWatchesFiltersVector"));
        suite.addTest(new Watches("testWatchesFiltersHashMap"));
        suite.addTest(new Watches("testWatchesFiltersHashtable"));
        suite.addTest(new Watches("testWatchesFiltersTreeMap"));
        suite.addTest(new Watches("testWatchesFiltersTreeSet"));
        suite.addTest(new Watches("testWatchesFilters1DArray"));
        suite.addTest(new Watches("testWatchesFilters2DArray"));
        suite.addTest(new Watches("testWatchesValues"));
        return suite;
    }
    
    /**
     *
     */
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
        if ("testWatchesPublicVariables".equals(getName())) {
            //open source
            org.netbeans.jellytools.nodes.Node beanNode = new org.netbeans.jellytools.nodes.Node(new SourcePackagesNode(Utilities.testProjectName), "examples.advanced|MemoryView.java"); //NOI18N
            new OpenAction().performAPI(beanNode); // NOI18N
            EditorOperator op = new EditorOperator("MemoryView.java");
            Utilities.setCaret(op, 76);
            new RunToCursorAction().perform();
            Utilities.getDebugToolbar().waitComponentVisible(true);
            Utilities.waitDebuggerConsole("Thread main stopped at MemoryView.java:76", 0);
        }
    }
    
    /**
     *
     */
    public void tearDown() {
        JemmyProperties.getCurrentOutput().printTrace("\nteardown\n");
        if ("testWatchesValues".equals(getName())) {
            Utilities.endAllSessions();
        }
        Utilities.deleteAllWatches();
    }
    
    /**
     *
     */
    public void testWatchesPublicVariables() throws Throwable {
        try {
            createWatch("Vpublic");
            createWatch("Spublic");
            createWatch("inheritedVpublic");
            createWatch("inheritedSpublic");
            Utilities.showDebuggerView(Utilities.watchesViewTitle);
            TreeTableOperator jTableOperator = new TreeTableOperator(new TopComponentOperator(Utilities.watchesViewTitle));
            checkTTVLine(jTableOperator, 0, "Vpublic", "String", "\"Public Variable\"");
            checkTTVLine(jTableOperator, 1, "Spublic", "String", "\"Public Variable\"");
            checkTTVLine(jTableOperator, 2, "inheritedVpublic", "String", "\"Inherited Public Variable\"");
            checkTTVLine(jTableOperator, 3, "inheritedSpublic", "String", "\"Inherited Public Variable\"");
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    /**
     *
     */
    public void testWatchesProtectedVariables() throws Throwable {
        try {
            createWatch("Vprotected");
            createWatch("Sprotected");
            createWatch("inheritedVprotected");
            createWatch("inheritedSprotected");
            Utilities.showDebuggerView(Utilities.watchesViewTitle);
            TreeTableOperator jTableOperator = new TreeTableOperator(new TopComponentOperator(Utilities.watchesViewTitle));
            checkTTVLine(jTableOperator, 0, "Vprotected", "String", "\"Protected Variable\"");
            checkTTVLine(jTableOperator, 1, "Sprotected", "String", "\"Protected Variable\"");
            checkTTVLine(jTableOperator, 2, "inheritedVprotected", "String", "\"Inherited Protected Variable\"");
            checkTTVLine(jTableOperator, 3, "inheritedSprotected", "String", "\"Inherited Protected Variable\"");
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    /**
     *
     */
    public void testWatchesPrivateVariables() throws Throwable {
        try {
            createWatch("Vprivate");
            createWatch("Sprivate");
            createWatch("inheritedVprivate");
            createWatch("inheritedSprivate");
            Utilities.showDebuggerView(Utilities.watchesViewTitle);
            TreeTableOperator jTableOperator = new TreeTableOperator(new TopComponentOperator(Utilities.watchesViewTitle));
            checkTTVLine(jTableOperator, 0, "Vprivate", "String", "\"Private Variable\"");
            checkTTVLine(jTableOperator, 1, "Sprivate", "String", "\"Private Variable\"");
            checkTTVLine(jTableOperator, 2, "inheritedVprivate", "String", "\"Inherited Private Variable\"");
            checkTTVLine(jTableOperator, 3, "inheritedSprivate", "String", "\"Inherited Private Variable\"");
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    /**
     *
     */
    public void testWatchesPackagePrivateVariables() throws Throwable {
        try {
            createWatch("VpackagePrivate");
            createWatch("SpackagePrivate");
            createWatch("inheritedVpackagePrivate");
            createWatch("inheritedSpackagePrivate");
            Utilities.showDebuggerView(Utilities.watchesViewTitle);
            TreeTableOperator jTableOperator = new TreeTableOperator(new TopComponentOperator(Utilities.watchesViewTitle));
            checkTTVLine(jTableOperator, 0, "VpackagePrivate", "String", "\"Package-private Variable\"");
            checkTTVLine(jTableOperator, 1, "SpackagePrivate", "String", "\"Package-private Variable\"");
            checkTTVLine(jTableOperator, 2, "inheritedVpackagePrivate", "String", "\"Inherited Package-private Variable\"");
            checkTTVLine(jTableOperator, 3, "inheritedSpackagePrivate", "String", "\"Inherited Package-private Variable\"");
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    /**
     *
     */
    public void testWatchesFiltersBasic() throws Throwable {
        try {
            createWatch("1==1");
            createWatch("1==0");
            createWatch("Integer.toString(10)");
            createWatch("clazz");
            createWatch("n");
            Utilities.showDebuggerView(Utilities.watchesViewTitle);
            TreeTableOperator jTableOperator = new TreeTableOperator(new TopComponentOperator(Utilities.watchesViewTitle));
            checkTTVLine(jTableOperator, 0, "1==1", "boolean", "true");
            checkTTVLine(jTableOperator, 1, "1==0", "boolean", "false");
            checkTTVLine(jTableOperator, 2, "Integer.toString(10)", "String", "\"10\"");
            checkTTVLine(jTableOperator, 3, "clazz", "Class", "class java.lang.Runtime");
            assertTrue("Node \'clazz\' has no child nodes", hasChildNodes("clazz", jTableOperator));
            checkTTVLine(jTableOperator, 4, "n", "int", "50");
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    /**
     *
     */
    public void testWatchesFiltersLinkedList() throws Throwable {
        try {
            createWatch("llist");
            createWatch("llist.toString()");
            createWatch("llist.getFirst()");
            createWatch("llist.getLast()");
            createWatch("llist.get(1)");
            Utilities.showDebuggerView(Utilities.watchesViewTitle);
            TreeTableOperator jTableOperator = new TreeTableOperator(new TopComponentOperator(Utilities.watchesViewTitle));
            checkTTVLine(jTableOperator, 0, "llist", "LinkedList", null);
            assertTrue("Node \'llist\' has no child nodes", hasChildNodes("llist", jTableOperator));
            checkTTVLine(jTableOperator, 1, "llist.toString()", "String", "\"[0. item, 1. item, 2. item, 3. item, 4. item, 5. item, 6. item, 7. item, 8. item, 9. item, 10. item, 11. item, 12. item, 13. item, 14. item, 15. item, 16. item, 17. item, 18. item, 19. item, 20. item, 21. item, 22. item, 23. item, 24. item, 25. item, 26. item, 27. item, 28. item, 29. item, 30. item, 31. item, 32. item, 33. item, 34. item, 35. item, 36. item, 37. item, 38. item, 39. item, 40. item, 41. item, 42. item, 43. item, 44. item, 45. item, 46. item, 47. item, 48. item, 49. item]\"");
            checkTTVLine(jTableOperator, 2, "llist.getFirst()", "String", "\"0. item\"");
            checkTTVLine(jTableOperator, 3, "llist.getLast()", "String", "\"49. item\"");
            checkTTVLine(jTableOperator, 4, "llist.get(1)", "String", "\"1. item\"");
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    /**
     *
     */
    public void testWatchesFiltersArrayList() throws Throwable {
        try {
            createWatch("alist");
            createWatch("alist.toString()");
            createWatch("alist.get(2)");
            Utilities.showDebuggerView(Utilities.watchesViewTitle);
            TreeTableOperator jTableOperator = new TreeTableOperator(new TopComponentOperator(Utilities.watchesViewTitle));
            checkTTVLine(jTableOperator, 0, "alist", "ArrayList", null);
            assertTrue("Node \'alist\' has no child nodes", hasChildNodes("alist", jTableOperator));
            checkTTVLine(jTableOperator, 1, "alist.toString()", "String", "\"[0. item, 1. item, 2. item, 3. item, 4. item, 5. item, 6. item, 7. item, 8. item, 9. item, 10. item, 11. item, 12. item, 13. item, 14. item, 15. item, 16. item, 17. item, 18. item, 19. item, 20. item, 21. item, 22. item, 23. item, 24. item, 25. item, 26. item, 27. item, 28. item, 29. item, 30. item, 31. item, 32. item, 33. item, 34. item, 35. item, 36. item, 37. item, 38. item, 39. item, 40. item, 41. item, 42. item, 43. item, 44. item, 45. item, 46. item, 47. item, 48. item, 49. item]\"");
            checkTTVLine(jTableOperator, 2, "alist.get(2)", "String", "\"2. item\"");
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    /**
     *
     */
    public void testWatchesFiltersVector() throws Throwable {
        try {
            createWatch("vec");
            createWatch("vec.toString()");
            createWatch("vec.get(3)");
            Utilities.showDebuggerView(Utilities.watchesViewTitle);
            TreeTableOperator jTableOperator = new TreeTableOperator(new TopComponentOperator(Utilities.watchesViewTitle));
            checkTTVLine(jTableOperator, 0, "vec", "Vector", null);
            assertTrue("Node \'vec\' has no child nodes", hasChildNodes("vec", jTableOperator));
            checkTTVLine(jTableOperator, 1, "vec.toString()", "String", "\"[0. item, 1. item, 2. item, 3. item, 4. item, 5. item, 6. item, 7. item, 8. item, 9. item, 10. item, 11. item, 12. item, 13. item, 14. item, 15. item, 16. item, 17. item, 18. item, 19. item, 20. item, 21. item, 22. item, 23. item, 24. item, 25. item, 26. item, 27. item, 28. item, 29. item, 30. item, 31. item, 32. item, 33. item, 34. item, 35. item, 36. item, 37. item, 38. item, 39. item, 40. item, 41. item, 42. item, 43. item, 44. item, 45. item, 46. item, 47. item, 48. item, 49. item]\"");
            checkTTVLine(jTableOperator, 2, "vec.get(3)", "String", "\"3. item\"");
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    /**
     *
     */
    public void testWatchesFiltersHashMap() throws Throwable {
        try {
            createWatch("hmap");
            createWatch("hmap.containsKey(\"4\")");
            createWatch("hmap.get(\"5\")");
            createWatch("hmap.put(\"6\",\"test\")");
            Utilities.showDebuggerView(Utilities.watchesViewTitle);
            TreeTableOperator jTableOperator = new TreeTableOperator(new TopComponentOperator(Utilities.watchesViewTitle));
            checkTTVLine(jTableOperator, 0, "hmap", "HashMap", null);
            assertTrue("Node \'hmap\' has no child nodes", hasChildNodes("hmap", jTableOperator));
            checkTTVLine(jTableOperator, 1, "hmap.containsKey(\"4\")", "boolean", "true");
            checkTTVLine(jTableOperator, 2, "hmap.get(\"5\")", "String", "\"5. item\"");
            checkTTVLine(jTableOperator, 3, "hmap.put(\"6\",\"test\")", "String", "\"6. item\"");
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    /**
     *
     */
    public void testWatchesFiltersHashtable() throws Throwable {
        try {
            createWatch("htab");
            createWatch("htab.containsKey(\"7\")");
            createWatch("htab.get(\"9\")");
            createWatch("htab.put(\"10\", \"test\")");
            Utilities.showDebuggerView(Utilities.watchesViewTitle);
            TreeTableOperator jTableOperator = new TreeTableOperator(new TopComponentOperator(Utilities.watchesViewTitle));
            checkTTVLine(jTableOperator, 0, "htab", "Hashtable", null);
            assertTrue("Node \'htab\' has no child nodes", hasChildNodes("htab", jTableOperator));
            checkTTVLine(jTableOperator, 1, "htab.containsKey(\"7\")", "boolean", "true");
            checkTTVLine(jTableOperator, 2, "htab.get(\"9\")", "String", "\"9. item\"");
            checkTTVLine(jTableOperator, 3, "htab.put(\"10\", \"test\")", "String", "\"10. item\"");
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    /**
     *
     */
    public void testWatchesFiltersTreeMap() throws Throwable {
        try {
            createWatch("tmap");
            createWatch("tmap.containsKey(\"11\")");
            createWatch("tmap.get(\"12\")");
            createWatch("tmap.put(\"13\",\"test\")");
            Utilities.showDebuggerView(Utilities.watchesViewTitle);
            TreeTableOperator jTableOperator = new TreeTableOperator(new TopComponentOperator(Utilities.watchesViewTitle));
            checkTTVLine(jTableOperator, 0, "tmap", "TreeMap", null);
            assertTrue("Node \'tmap\' has no child nodes", hasChildNodes("tmap", jTableOperator));
            checkTTVLine(jTableOperator, 1, "tmap.containsKey(\"11\")", "boolean", "true");
            checkTTVLine(jTableOperator, 2, "tmap.get(\"12\")", "String", "\"12. item\"");
            checkTTVLine(jTableOperator, 3, "tmap.put(\"13\",\"test\")", "String", "\"13. item\"");
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    /**
     *
     */
    public void testWatchesFiltersTreeSet() throws Throwable {
        try {
            createWatch("tset");
            createWatch("tset.contains(\"14. item\")");
            createWatch("tset.iterator()");
            Utilities.showDebuggerView(Utilities.watchesViewTitle);
            TreeTableOperator jTableOperator = new TreeTableOperator(new TopComponentOperator(Utilities.watchesViewTitle));
            checkTTVLine(jTableOperator, 0, "tset", "TreeSet", null);
            assertTrue("Node \'tset\' has no child nodes", hasChildNodes("tset", jTableOperator));
            checkTTVLine(jTableOperator, 1, "tset.contains(\"14. item\")", "boolean", "true");
            checkTTVLine(jTableOperator, 2, "tset.iterator()", "TreeMap$KeyIterator", null);
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    /**
     *
     */
    public void testWatchesFilters1DArray() throws Throwable {
        try {
            createWatch("policko");
            createWatch("policko.length");
            createWatch("policko[1]");
            createWatch("policko[10]");
            createWatch("pole");
            createWatch("pole.length");
            createWatch("pole[1]");
            Utilities.showDebuggerView(Utilities.watchesViewTitle);
            TreeTableOperator jTableOperator = new TreeTableOperator(new TopComponentOperator(Utilities.watchesViewTitle));
            checkTTVLine(jTableOperator, 0, "policko", "int[]", null);
            assertTrue("Node \'policko\' has no child nodes", hasChildNodes("policko", jTableOperator));
            checkTTVLine(jTableOperator, 1, "policko.length", "int", "5");
            checkTTVLine(jTableOperator, 2, "policko[1]", "int", "2");
            checkTTVLine(jTableOperator, 3, "policko[10]", null, ">Array index \"10\" is out of range <0,4><");
            checkTTVLine(jTableOperator, 4, "pole", "int[]", null);
            assertTrue("Node \'pole\' has no child nodes", hasChildNodes("pole", jTableOperator));
            checkTTVLine(jTableOperator, 5, "pole.length", "int", "50");
            checkTTVLine(jTableOperator, 6, "pole[1]", "int", "0");
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    /**
     *
     */
    public void testWatchesFilters2DArray() throws Throwable {
        try {
            createWatch("d2");
            createWatch("d2.length");
            createWatch("d2[1]");
            createWatch("d2[1].length");
            createWatch("d2[1][1]");
            createWatch("d2[15].length");
            Utilities.showDebuggerView(Utilities.watchesViewTitle);
            TreeTableOperator jTableOperator = new TreeTableOperator(new TopComponentOperator(Utilities.watchesViewTitle));
            checkTTVLine(jTableOperator, 0, "d2", "int[][]", null);
            assertTrue("Node \'d2\' has no child nodes", hasChildNodes("d2", jTableOperator));
            checkTTVLine(jTableOperator, 1, "d2.length", "int", "10");
            checkTTVLine(jTableOperator, 2, "d2[1]", "int[]", null);
            assertTrue("Node \'d2[1]\' has no child nodes", hasChildNodes("d2[1]", jTableOperator));
            checkTTVLine(jTableOperator, 3, "d2[1].length", "int", "20");
            checkTTVLine(jTableOperator, 4, "d2[1][1]", "int", "0");
            checkTTVLine(jTableOperator, 5, "d2[15].length", null, ">Array index \"15\" is out of range <0,9><");
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    /**
     *
     */
    public void testWatchesValues() throws Throwable {
        try {
            EditorOperator op = new EditorOperator("MemoryView.java");
            Utilities.setCaret(op, 104);
            new RunToCursorAction().perform();
            MainWindowOperator.getDefault().waitStatusText("Thread main stopped at MemoryView.java:104.");
            
            createWatch("free");
            createWatch("taken");
            createWatch("total");
            createWatch("this");
            
            Utilities.showDebuggerView(Utilities.watchesViewTitle);
            TreeTableOperator jTableOperator = new TreeTableOperator(new TopComponentOperator(Utilities.watchesViewTitle));
            Node.Property property;
            int count = 0;
            
            try {
                if (!("free".equals(jTableOperator.getValueAt(count,0).toString())))
                    assertTrue("Watch for expression \'free\' was not created", false);
                property = (Node.Property)jTableOperator.getValueAt(count,1);
                if (!("long".equals(property.getValue())))
                    assertTrue("Watch type for expression \'free\' is " + property.getValue() + ", should be long", false);
                property = (Node.Property)jTableOperator.getValueAt(count++,2);
                long free = Long.parseLong(property.getValue().toString());
                
                if (!("taken".equals(jTableOperator.getValueAt(count,0).toString())))
                    assertTrue("Watch for expression \'taken\' was not created", false);
                property = (Node.Property)jTableOperator.getValueAt(count,1);
                if (!("int".equals(property.getValue())))
                    assertTrue("Watch type for expression \'taken\' is " + property.getValue() + ", should be long", false);
                property = (Node.Property)jTableOperator.getValueAt(count++,2);
                long taken = Long.parseLong(property.getValue().toString());
                
                if (!("total".equals(jTableOperator.getValueAt(count,0).toString())))
                    assertTrue("Watch for expression \'total\' was not created", false);
                property = (Node.Property)jTableOperator.getValueAt(count,1);
                if (!("long".equals(property.getValue())))
                    assertTrue("Watch type for expression \'total\' is " + property.getValue() + ", should be long", false);
                property = (Node.Property)jTableOperator.getValueAt(count++,2);
                long total = Long.parseLong(property.getValue().toString());
                
                assertTrue("Watches values does not seem to be correct (total != free + taken)", total == free + taken);
                
                if (!("this".equals(jTableOperator.getValueAt(count,0).toString())))
                    assertTrue("Watch for expression \'this\' was not created", false);
                property = (Node.Property)jTableOperator.getValueAt(count,1);
                if (!("MemoryView".equals(property.getValue())))
                    assertTrue("Watch type for expression \'this\' is " + property.getValue() + ", should be MemoryView", false);
                assertTrue("Watch this has no child nodes", hasChildNodes("this", jTableOperator));
            } catch (java.lang.IllegalAccessException e1) {
                assertTrue(e1.getMessage(), false);
            } catch (java.lang.reflect.InvocationTargetException e2) {
                assertTrue(e2.getMessage(), false);
            }
        } catch (Throwable th) {
            try {
                // capture screen before cleanup in finally clause is completed
                PNGEncoder.captureScreen(getWorkDir().getAbsolutePath()+File.separator+"screenBeforeCleanup.png");
            } catch (Exception e1) {
                // ignore it
            }
            throw th;
        }
    }
    
    /**
     *
     * @param exp
     */
    protected void createWatch(String exp) {
        new ActionNoBlock(Utilities.runMenu + "|" + Utilities.newWatchItem, null).perform();
        //new ActionNoBlock(null, null, Utilities.newWatchShortcut).performShortcut();
        NbDialogOperator dialog = new NbDialogOperator(Utilities.newWatchTitle);
        new JTextFieldOperator(dialog, 0).typeText(exp);
        dialog.ok();
        try {
            new Waiter(new Waitable() {
                public Object actionProduced(Object dialog) {
                    NbDialogOperator op = (NbDialogOperator)dialog;
                    if (!op.isVisible()) {
                        return Boolean.TRUE;
                    }
                    return null;
                }
                
                public String getDescription() {
                    return "Wait new watch dialog is closed";
                }
            }).waitAction(dialog);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     *
     * @param table
     * @param lineNumber
     * @param name
     * @param type
     * @param value
     */
    protected void checkTTVLine(TreeTableOperator table, int lineNumber, String name, String type, String value) {
        try {
            table.scrollToCell(lineNumber, 0);
            org.openide.nodes.Node.Property property;
            String string = null;
            assertTrue("Node " + name + " not displayed in Watches view", name.equals(table.getValueAt(lineNumber, 0).toString()));
            property = (org.openide.nodes.Node.Property)table.getValueAt(lineNumber, 1);
            string = property.getValue().toString();
            int maxWait = 100;
            while (string.equals(Utilities.evaluatingPropertyText) && maxWait > 0) {
                new EventTool().waitNoEvent(300);
                maxWait--;
            }
            assertTrue("Node " + name + " has wrong type in Watches view (displayed: " + string + ", expected: " + type + ")",
                    (type == null) || type.length() == 0 || type.equals(string));
            property = (org.openide.nodes.Node.Property)table.getValueAt(lineNumber, 2);
            string = property.getValue().toString();
            maxWait = 100;
            while (string.equals(Utilities.evaluatingPropertyText) && maxWait > 0) {
                new EventTool().waitNoEvent(300);
                maxWait--;
            }
            assertTrue("Node " + name + " has wrong value in Watches view (displayed: " + string + ", expected: " + value + ")",
                    (type == null) || !type.equals(string));
        } catch (java.lang.IllegalAccessException e1) {
            assertTrue(e1.getMessage(), false);
        } catch (java.lang.reflect.InvocationTargetException e2) {
            assertTrue(e2.getMessage(), false);
        }
    }
    
    protected boolean hasChildNodes(String nodePath, TreeTableOperator jTableOperator) {
        org.netbeans.jellytools.nodes.Node node = new org.netbeans.jellytools.nodes.Node(jTableOperator.tree(), nodePath);
        node.select();
        return !node.isLeaf();
    }
}
