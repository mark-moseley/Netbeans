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

package org.netbeans.api.editor;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.junit.NbTestCase;

/**
 * Tests of editor registry.
 *
 * @author Miloslav Metelka
 */
public class EditorRegistryTest extends NbTestCase {
    
    public EditorRegistryTest(String name) {
        super(name);
    }
    
    public void testRegistry() throws Exception {
        // Start listening
        EditorRegistry.addPropertyChangeListener(EditorRegistryListener.INSTANCE);

        // Test registration
        JTextComponent c1 = new JEditorPane();
        JTextComponent c2 = new JEditorPane();
        JTextComponent c3 = new JEditorPane();
        
        // Tested ignored ancestor and components with it
        IgnoredAncestorPanel tabContainer = new IgnoredAncestorPanel();
        JTextComponent iac1 = new JEditorPane();
        JPanel tab1 = new JPanel();
        tab1.add(iac1);
        JTextComponent iac2 = new JEditorPane();
        JPanel tab2 = new JPanel();
        tab2.add(iac2);
        tabContainer.add(tab1, BorderLayout.WEST);
        tabContainer.add(tab2, BorderLayout.CENTER);

        // Add to component hierarchy to ensure appearance in the registry
        JFrame frame = new JFrame();
        frame.getContentPane().add(c1, BorderLayout.NORTH);
        frame.getContentPane().add(c2, BorderLayout.CENTER);
        frame.getContentPane().add(c3, BorderLayout.SOUTH);
        frame.getContentPane().add(tabContainer, BorderLayout.WEST);
        frame.pack(); // Causes addition to the hierarchy

        // Register first two
        EditorRegistry.register(c1);
        EditorRegistry.register(c2);

        List<? extends JTextComponent> jtcList = EditorRegistry.componentList();
        assertSame(2, jtcList.size());
        assertSame(c1, jtcList.get(0));
        assertSame(c2, jtcList.get(1));
        
        // Ignore repetitive registration
        EditorRegistry.register(c2);
        EditorRegistry.register(c2);
        assertSame(2, EditorRegistry.componentList().size());
        
        // Extra component
        EditorRegistry.register(c3);
        assertSame(3, EditorRegistry.componentList().size());
        
        // Simulate focusGained
        EditorRegistry.focusGained(c3, null);
        assertSame(1, EditorRegistryListener.INSTANCE.firedCount);
        assertSame(c3, EditorRegistryListener.INSTANCE.newValue);
        assertSame(null, EditorRegistryListener.INSTANCE.oldValue);
        EditorRegistryListener.INSTANCE.reset(); // Reset to 0

        jtcList = EditorRegistry.componentList();
        assertSame(3, jtcList.size());
        assertSame(c3, jtcList.get(0));

        // Simulate document change of focused component
        Document oldDoc = c3.getDocument();
        Document newDoc = c3.getUI().getEditorKit(c3).createDefaultDocument();
        c3.setDocument(newDoc);
        assertSame(1, EditorRegistryListener.INSTANCE.firedCount);
        assertSame(newDoc, EditorRegistryListener.INSTANCE.newValue);
        assertSame(oldDoc, EditorRegistryListener.INSTANCE.oldValue);
        EditorRegistryListener.INSTANCE.reset(); // Reset to 0
        oldDoc = null;
        newDoc = null;

        // Simulate focusLost
        EditorRegistry.focusLost(c3, null);
        assertSame(1, EditorRegistryListener.INSTANCE.firedCount);
        assertSame(null, EditorRegistryListener.INSTANCE.newValue);
        assertSame(c3, EditorRegistryListener.INSTANCE.oldValue);
        EditorRegistryListener.INSTANCE.reset(); // Reset firedCount to 0

        EditorRegistry.focusGained(c1, null);
        assertSame(1, EditorRegistryListener.INSTANCE.firedCount);
        assertSame(c1, EditorRegistryListener.INSTANCE.newValue);
        assertSame(null, EditorRegistryListener.INSTANCE.oldValue);
        EditorRegistryListener.INSTANCE.reset(); // Reset firedCount to 0
        
        // Test ignored ancestor
        EditorRegistry.setIgnoredAncestorClass(IgnoredAncestorPanel.class);
        EditorRegistry.register(iac1);
        EditorRegistry.register(iac2);
        
        jtcList = EditorRegistry.componentList();
        assertSame(5, jtcList.size());
        EditorRegistry.focusGained(iac1, null);
        assertSame(iac1, EditorRegistry.lastFocusedComponent());
        EditorRegistryListener.INSTANCE.reset(); // Reset firedCount to 0
        // First close iac2 - should not fire EditorRegistry.LAST_FOCUSED_REMOVED_PROPERTY
        // since lastFocusedComponent is iac1
        tabContainer.remove(tab2);
        EditorRegistry.notifyClose(tab2);
        jtcList = EditorRegistry.componentList();
        assertSame(4, jtcList.size());
        assertSame(0, EditorRegistryListener.INSTANCE.firedCount); // Nothing was fired
        EditorRegistryListener.INSTANCE.reset(); // Reset firedCount to 0
        assertSame(iac1, EditorRegistry.lastFocusedComponent());

        // Note: Close notification may come even before corresponding focusLost()
        tabContainer.remove(tab1);
        EditorRegistry.notifyClose(tab1);
        jtcList = EditorRegistry.componentList();
        assertSame(3, jtcList.size());
        // Since iac1 was the first in the component list then focusLost() would be fired
        // followed by LAST_FOCUSED_REMOVED_PROPERTY
        assertSame(2, EditorRegistryListener.INSTANCE.firedCount);
        assertEquals(EditorRegistry.LAST_FOCUSED_REMOVED_PROPERTY, EditorRegistryListener.INSTANCE.propertyName);
        assertEquals(EditorRegistry.lastFocusedComponent(), EditorRegistryListener.INSTANCE.newValue);
        assertEquals(iac1, EditorRegistryListener.INSTANCE.oldValue);
        EditorRegistryListener.INSTANCE.reset(); // Reset firedCount to 0
        
        // Clean ignored ancestor stuff
        frame.getContentPane().remove(tabContainer);
        frame.pack();
        iac1 = iac2 = null;
        tab1 = tab2 = null;
        tabContainer = null;
        
        // Partial GC: c3
        frame.getContentPane().remove(c3);
        frame.pack();
        c3 = null;
        jtcList = null;
        EditorRegistryListener.INSTANCE.reset();
        System.gc();
        System.gc();
        assertSame(2, EditorRegistry.componentList().size());
        
        // Test full GC
        jtcList = null;
        frame.getContentPane().remove(c1);
        frame.getContentPane().remove(c2);
        frame.pack();
        c1 = null;
        c2 = null;
        jtcList = null;
        EditorRegistryListener.INSTANCE.reset();
        System.gc();
        System.gc();
        assertSame(0, EditorRegistry.componentList().size());

        
    }
    
    private static final class EditorRegistryListener implements PropertyChangeListener {
        
        static final EditorRegistryListener INSTANCE = new EditorRegistryListener();
        
        int firedCount;
        
        String propertyName;
        
        Object oldValue;
        
        Object newValue;
        
        public void propertyChange(PropertyChangeEvent evt) {
            firedCount++;
            propertyName = evt.getPropertyName();
            oldValue = evt.getOldValue();
            newValue = evt.getNewValue();
        }
        
        public void reset() {
            firedCount = 0;
            propertyName = null;
            oldValue = null;
            newValue = null;
        }

    }
    
    /**
     * Tested ignored ancestor (like NB's TabbedContainer).
     */
    static final class IgnoredAncestorPanel extends JPanel {
        
    }

}
