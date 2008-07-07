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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.Customizer;
import java.beans.Introspector;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JMenuItem;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.spi.debugger.ui.BreakpointAnnotation;

import org.netbeans.spi.debugger.ui.Controller;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Actions;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter.Popup;
import org.openide.util.actions.SystemAction;

/**
 * Customize action for line breakpoint, which is available from the gutter popup.
 *
 * @author Martin Entlicher
 */
public class BreakpointCustomizeAction extends SystemAction implements ContextAwareAction  {
    
    /** Creates a new instance of BreakpointCustomizeAction */
    public BreakpointCustomizeAction() {
        setEnabled(false);
    }

    public String getName() {
        return NbBundle.getMessage(BreakpointCustomizeAction.class, "CTL_customize");
    }

    public HelpCtx getHelpCtx() {
        return null;
    }
    
    public void actionPerformed(ActionEvent ev) {
    }
    
    public Action createContextAwareInstance(Lookup actionContext) {
        Collection<? extends BreakpointAnnotation> ann = actionContext.lookupAll(BreakpointAnnotation.class);
        if (ann.size() == 1) {
            return new BreakpointAwareAction(ann.iterator().next());
        } else {
            //Exceptions.printStackTrace(new IllegalStateException("expecting BreakpointAnnotation object in lookup "+actionContext));
            return this;
        }
    }
    
    private class BreakpointAwareAction implements Action, Popup {
        
        private BreakpointAnnotation ann;
        
        public BreakpointAwareAction(BreakpointAnnotation ann) {
            this.ann = ann;
        }

        public JMenuItem getPopupPresenter() {
            return new Actions.MenuItem (this, false);
        }
    
        public Object getValue(String key) {
            return BreakpointCustomizeAction.this.getValue(key);
        }

        public void putValue(String key, Object value) {
            //BreakpointCustomizeAction.this.putValue(key, value);
        }

        public void setEnabled(boolean b) {
            //BreakpointCustomizeAction.this.setEnabled(b);
        }

        public boolean isEnabled() {
            return getCustomizer(ann.getBreakpoint()) != null;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            BreakpointCustomizeAction.this.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            BreakpointCustomizeAction.this.removePropertyChangeListener(listener);
        }

        public void actionPerformed(ActionEvent e) {
            customize(ann.getBreakpoint());
        }
        
        private BeanInfo findBeanInfo(Class clazz) {
            Class biClass;
            try {
                biClass = Lookup.getDefault().lookup(ClassLoader.class).loadClass(clazz.getName()+"BeanInfo");
            } catch (ClassNotFoundException cnfex) {
                biClass = null;
            }
            if (biClass == null) {
                clazz = clazz.getSuperclass();
                if (clazz != null) {
                    return findBeanInfo(clazz);
                } else {
                    return null;
                }
            } else {
                try {
                    java.lang.reflect.Constructor c = biClass.getConstructor(new Class[0]);
                    c.setAccessible(true);
                    return (BeanInfo) c.newInstance(new Object[0]);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }
            }
        }
        
        private Customizer getCustomizer(Breakpoint b) {
            BeanInfo bi = findBeanInfo(b.getClass());
            if (bi == null) {
                try {
                    bi = Introspector.getBeanInfo(b.getClass());
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }
            }
            BeanDescriptor bd = bi.getBeanDescriptor();
            if (bd == null) return null;
            Class cc = bd.getCustomizerClass();
            if (cc == null) return null;
            try {
                Customizer c = (Customizer) cc.newInstance();
                c.setObject(b);
                return c;
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }

        private void customize(Breakpoint b) {
            Customizer c = getCustomizer(b);
            if (c == null) {
                return;
            }
            
            HelpCtx helpCtx = HelpCtx.findHelp (c);
            if (helpCtx == null) {
                helpCtx = new HelpCtx ("debug.add.breakpoint");  // NOI18N
            }
            final Controller[] cPtr = new Controller[] { null };
            if (c instanceof Controller) {
                cPtr[0] = (Controller) c;
            }
            final DialogDescriptor[] descriptorPtr = new DialogDescriptor[1];
            final Dialog[] dialogPtr = new Dialog[1];
            ActionListener buttonsActionListener = null;
            if (cPtr[0] != null) {
                buttonsActionListener = new ActionListener() {
                    public void actionPerformed(ActionEvent ev) {
                        if (descriptorPtr[0].getValue() == DialogDescriptor.OK_OPTION) {
                            boolean ok = cPtr[0].ok();
                            if (ok) {
                                dialogPtr[0].setVisible(false);
                            }
                        } else {
                            dialogPtr[0].setVisible(false);
                        }
                    }
                };
            }
            DialogDescriptor descriptor = new DialogDescriptor (
                c,
                NbBundle.getMessage (
                    BreakpointCustomizeAction.class,
                    "CTL_Breakpoint_Customizer_Title" // NOI18N
                ),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                helpCtx,
                buttonsActionListener
            );
            if (buttonsActionListener != null) {
                descriptor.setClosingOptions(new Object[] {});
            }
            Dialog d = DialogDisplayer.getDefault ().createDialog (descriptor);
            d.pack ();
            descriptorPtr[0] = descriptor;
            dialogPtr[0] = d;
            d.setVisible (true);
        }

    }

}
