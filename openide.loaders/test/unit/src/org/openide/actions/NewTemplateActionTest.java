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

package org.openide.actions;


import javax.swing.Action;
import javax.swing.JPopupMenu;
import junit.textui.TestRunner;

import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;

import org.openide.loaders.TemplateWizard;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.*;
/** Test creating NewTemplateAction by context.
 *  See issue 28785.
 */
public class NewTemplateActionTest extends NbTestCase {
    public NewTemplateActionTest(String name) {
        super(name);
    }
    
    public void testContextAware () {
        NewTemplateAction global = (NewTemplateAction)NewTemplateAction.get (NewTemplateAction.class);
        
        CookieNode node = new CookieNode ();
        JPopupMenu popup = Utilities.actionsToPopup (new Action[] {
            global
        }, node.getLookup ());
        
        assertTrue ("NewTemplateAction's cookie must be called.", node.counter > 0);
        
        global.getPopupPresenter ();
        
        assertTrue ("When calling wizard on global action, the CookieNode's cookie is not " +
            "as it is not selected", node.counter > 0
        );
    }

    public void testContextAwareWithChanges () {
        doContextAwareWithChanges (false);
    }
    public void testContextAwareWithChangesWithDeepGC () {
        doContextAwareWithChanges (true);
    }
    
    private void doContextAwareWithChanges (boolean withGC) {
        class P implements Lookup.Provider {
            private Lookup lookup = Lookup.EMPTY;
            
            public Lookup getLookup () {
                return lookup;
            }
        }
        P provider = new P ();
        Lookup lookup = Lookups.proxy (provider);
        
        NewTemplateAction global = (NewTemplateAction)NewTemplateAction.get (NewTemplateAction.class);
        Action clone = global.createContextAwareInstance (lookup);
        CookieNode node = new CookieNode ();
        
        //assertTrue ("Global is enabled", global.isEnabled ());
        assertFalse ("Local is not enabled if no nodes provided", clone.isEnabled ());
        
        JPopupMenu popup = Utilities.actionsToPopup (new Action[] {
            global
        }, lookup);
        
        if (withGC) {
            try {
                assertGC ("Will fail", new java.lang.ref.WeakReference (this));
            } catch (Throwable t) {
            }
        }
        
        assertFalse ("No node selected, no query", node.counter > 0);
        
        provider.lookup = node.getLookup ();
        lookup.lookup (Object.class); // does refresh
        
        assertTrue ("After change of Lookup the CookieNode is queried for cookie", node.counter > 0);
        assertTrue ("Local is enabled if a node is provided", clone.isEnabled ());
    }
    
    private static class CookieNode extends AbstractNode implements NewTemplateAction.Cookie {
        public CookieNode () {
            super (Children.LEAF);
            getCookieSet ().add (this);
        }
        
        int counter = 0;
        public TemplateWizard getTemplateWizard () {
            counter ++;
            return new TemplateWizard ();
        }
    }
}
