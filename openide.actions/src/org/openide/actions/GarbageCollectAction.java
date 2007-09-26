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

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CallableSystemAction;

import java.awt.*;
import java.awt.event.*;


import javax.swing.*;


// Toolbar presenter like MemoryMeterAction, except:
// 1. Does not have a mark etc.
// 2. But pressing it runs GC.
// 3. Slim profile fits nicely in the menu bar (at top level).
// 4. Displays textual memory usage directly, not via tooltip.
// Intended to be unobtrusive enough to leave on for daily use.

/**
 * Perform a system garbage collection.
 * @author Jesse Glick, Tim Boudreau
 */
public class GarbageCollectAction extends CallableSystemAction {
    public String getName() {
        return NbBundle.getBundle(GarbageCollectAction.class).getString("CTL_GarbageCollect"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(GarbageCollectAction.class);
    }

    public void performAction() {
        gc();
    }

    private static void gc() {
        // Can be slow, would prefer not to block on it.
        RequestProcessor.getDefault().post(
            new Runnable() {
                public void run() {
                    System.gc();
                    System.runFinalization();
                    System.gc();
                }
            }
        );
    }

    protected boolean asynchronous() {
        return false;
    }

    public Component getToolbarPresenter() {
        return new HeapViewWrapper();
//        return new MemButton();
    }

    private static final class HeapViewWrapper extends JComponent {
        public HeapViewWrapper() {
            add(new HeapView());
            setLayout(null);
        }
        
        public boolean isOpaque() {
            return false;
        }
        
        public Dimension getMinimumSize() {
            return calcPreferredSize();
        }

        public Dimension getPreferredSize() {
            return calcPreferredSize();
        }

        public Dimension getMaximumSize() {
            Dimension pref = calcPreferredSize();
            Container parent = getParent();
            if (parent != null && parent.getHeight() > 0) {
                pref.height = parent.getHeight();
            }
            return pref;
        }
        
        public Dimension calcPreferredSize() {
            Dimension pref = getHeapView().heapViewPreferredSize();
            pref.height += 1;
            pref.width += 6;
            return pref;
        }

        @SuppressWarnings("deprecation")
        @Override public void layout() {
            int w = getWidth();
            int h = getHeight();
            HeapView heapView = getHeapView();
            heapView.setBounds(4, 2, w - 6, h - 4);
        }

        private HeapView getHeapView() {
            return (HeapView)getComponent(0);
        }
    }
    
}
