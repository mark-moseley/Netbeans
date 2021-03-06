/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.editor.mimelookup.impl;

import java.net.URL;
import java.util.List;
import javax.swing.JSeparator;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.junit.NbTestCase;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.PasteAction;
import org.openide.actions.ReplaceAction;
import org.openide.actions.FindAction;
import org.openide.actions.NewAction;
import org.openide.util.Lookup;

/** Testing merging and sorting merged objects
 * 
 *  @author Martin Roskanin
 */
public class MimeLookupInheritanceTest extends NbTestCase {
    
    private static final int WAIT_TIME = 2000;
    
    public MimeLookupInheritanceTest(java.lang.String testName) {
        super(testName);
    }
    
    protected @Override void setUp() throws java.lang.Exception {
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

    private void testPopupItems(Lookup lookup, Class[] layerObjects){
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
        MimePath mp = MimePath.parse("text/x-java/text/html"); //NOI18N
        Lookup lookup = MimeLookup.getLookup(mp);
        Class layerObjects[] = {CutAction.class, CopyAction.class, NewAction.class, PasteAction.class};
        testPopupItems(lookup, layerObjects);
    }

    /** Testing MIME level popup items lookup, inheritance and sorting */
    public void testMimeLevelPopupsWithStringAndSeparator(){
        MimePath mp = MimePath.parse("text/x-java/text/html/text/xml"); //NOI18N
        Lookup lookup = MimeLookup.getLookup(mp);
        Class layerObjects[] = {CutAction.class, CopyAction.class, PasteAction.class, ReplaceAction.class, JSeparator.class, String.class};
        testPopupItems(lookup, layerObjects);
    }

    /**
     * Issue #61216: MimeLookup should support layer hidding
     */
    public void testHidding(){
        Lookup lookup = MimeLookup.getLookup(MimePath.get("text/xml"));
        checkLookupObject(lookup, CopyAction.class, true);
        checkLookupObject(lookup, ReplaceAction.class, true);
        checkLookupObject(lookup, PasteAction.class, false);
        lookup = MimeLookup.getLookup(MimePath.get("text/x-ant+xml"));
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
                Thread.sleep(100);
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

    
    private void checkLookupObject(final Lookup lookup, final Class clazz, final boolean shouldBePresent){
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
