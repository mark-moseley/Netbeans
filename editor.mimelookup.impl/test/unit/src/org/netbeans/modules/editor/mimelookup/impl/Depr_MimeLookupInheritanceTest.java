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

package org.netbeans.modules.editor.mimelookup.impl;

import java.net.URL;
import java.util.List;
import javax.swing.JSeparator;
import junit.framework.Test;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.junit.NbTestCase;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.PasteAction;
import org.openide.actions.ReplaceAction;
import org.openide.actions.FindAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;
import org.openide.util.actions.CallbackSystemAction;


/** Testing merging and sorting merged objects
 * Testing a deprecated MimePath.childLookup behaviour
 * 
 *  @author Martin Roskanin
 */
public class Depr_MimeLookupInheritanceTest extends NbTestCase {
    
    private static final int WAIT_TIME = 2000;
    
    public Depr_MimeLookupInheritanceTest(java.lang.String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set up the default lookup, repository, etc.
        EditorTestLookup.setLookup(
            new URL[] {
                getClass().getResource("test-layer.xml")
            },
            getWorkDir(),
            new Object[] {},
            getClass().getClassLoader(), 
            null
        );
    }

    private void testPopupItems(MimeLookup lookup, Class[] layerObjects){
        PopupActions actions = (PopupActions) lookup.lookup(PopupActions.class);
        assertTrue("PopupActions should be found", actions != null);
        if (actions != null){
            List popupActions = actions.getPopupActions();
            int popupSize = popupActions.size();
            assertTrue("popupActions count is not the same as etalon action count" +
                    "Expecting:"+layerObjects.length+" Found:"+popupSize,
                    popupSize == layerObjects.length);
            
            for (int i = 0; i<layerObjects.length; i++){
                Object obj = popupActions.get(i);
                assertTrue("Incorrect sorting or item is missing in the popup menu." +
                        "Expecting:"+layerObjects[i]+" Found:"+obj.getClass(),
                        layerObjects[i].isAssignableFrom(obj.getClass()));
            }   
        }
    }
    
    /** Testing Base level popup items lookup and sorting */
    public void testBaseLevelPopups(){
        MimeLookup lookup = MimeLookup.getMimeLookup(""); //NOI18N
        Class layerObjects[] = {CutAction.class, CopyAction.class, PasteAction.class};
        testPopupItems(lookup, layerObjects);
    }

    /** Testing MIME level popup items lookup, inheritance and sorting */
    public void testMimeLevelPopups(){
        MimeLookup lookup = MimeLookup.getMimeLookup("text/x-java").childLookup("text/html"); //NOI18N
        Class layerObjects[] = {CutAction.class, DeleteAction.class, CopyAction.class,
                ReplaceAction.class, PasteAction.class};
        testPopupItems(lookup, layerObjects);
    }

    /** Testing MIME level popup items lookup, inheritance and sorting */
    public void testMimeLevelPopupsWithStringAndSeparator(){
        MimeLookup lookup = MimeLookup.getMimeLookup("text/x-java").childLookup("text/html").childLookup("text/xml"); //NOI18N
        Class layerObjects[] = {CutAction.class, DeleteAction.class, CopyAction.class,
                ReplaceAction.class, PasteAction.class, JSeparator.class, String.class};
        testPopupItems(lookup, layerObjects);
    }

    /**
     * Issue #61216: MimeLookup should support layer hidding
     */
    public void testHidding(){
        MimeLookup lookup = MimeLookup.getMimeLookup("text/xml");
        checkLookupObject(lookup, CopyAction.class, true);
        checkLookupObject(lookup, ReplaceAction.class, true);
        checkLookupObject(lookup, PasteAction.class, false);
        lookup = MimeLookup.getMimeLookup("text/x-ant+xml");
        checkLookupObject(lookup, CutAction.class, true);
        checkLookupObject(lookup, CopyAction.class, false);
        checkLookupObject(lookup, PasteAction.class, true);
        checkLookupObject(lookup, ReplaceAction.class, false);
    }
    
    /**
     * Issue #61245: Delegate application/*+xml -> text/xml
     */
    public void test61245(){
        MimeLookup lookup = MimeLookup.getMimeLookup("application/xml");
        checkLookupObject(lookup, FindAction.class, true);
        lookup = MimeLookup.getMimeLookup("application/xhtml+xml");
        checkLookupObject(lookup, CutAction.class, true);
        checkLookupObject(lookup, FindAction.class, false);
        checkLookupObject(lookup, ReplaceAction.class, true);
    }
    
    public void testAntXmlPopup(){
        MimeLookup lookup = MimeLookup.getMimeLookup("text/xml"); //NOI18N
        Class layerObjects[] = {CutAction.class, CopyAction.class, PasteAction.class, ReplaceAction.class};
        testPopupItems(lookup, layerObjects);
        lookup = MimeLookup.getMimeLookup("text/x-ant+xml"); //NOI18N
        Class layerObjects2[] = {CutAction.class, CopyAction.class, PasteAction.class, ReplaceAction.class, FindAction.class};
        testPopupItems(lookup, layerObjects2);
    }
    
    /** Method will wait max. <code> maxMiliSeconds </code> miliseconds for the <code> requiredValue </code>
     *  gathered by <code> resolver </code>.
     *
     *  @param maxMiliSeconds maximum time to wait for requiredValue
     *  @param resolver resolver, which is gathering an actual value
     *  @param requiredValue if resolver value equals requiredValue the wait cycle is finished
     *
     *  @return false if the given maxMiliSeconds time elapsed and the requiredValue wasn't obtained
     */
    protected boolean waitMaxMilisForValue(int maxMiliSeconds, ValueResolver resolver, Object requiredValue){
        int time = (int) maxMiliSeconds / 100;
        while (time > 0) {
            Object resolvedValue = resolver.getValue();
            if (requiredValue == null && resolvedValue == null){
                return true;
            }
            if (requiredValue != null && requiredValue.equals(resolvedValue)){
                return true;
            }
            try {
                Thread.currentThread().sleep(100);
            } catch (InterruptedException ex) {
                time=0;
            }
            time--;
        }
        return false;
    }
    
    /** Interface for value resolver needed for i.e. waitMaxMilisForValue method.  
     *  For more details, please look at {@link #waitMaxMilisForValue()}.
     */
    public static interface ValueResolver{
        /** Returns checked value */
        Object getValue();
    }

    
    private void checkLookupObject(final MimeLookup lookup, final Class clazz, final boolean shouldBePresent){
        waitMaxMilisForValue(WAIT_TIME, new ValueResolver(){
            public Object getValue(){
                Object obj = lookup.lookup(clazz);
                boolean bool = (shouldBePresent) ? obj != null : obj == null;
                return Boolean.valueOf(bool);
            }
        }, Boolean.TRUE);
        Object obj = lookup.lookup(clazz);
        if (shouldBePresent){
            assertTrue("Object should be present in the lookup",obj!=null);
        } else {
            assertTrue("Object should NOT be present in the lookup",obj==null);
        }
    }
    
    
}
