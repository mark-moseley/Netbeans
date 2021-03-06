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

package org.netbeans.modules.asm.core.ui.top;

import java.io.Serializable;
import java.util.Collection;

import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import org.netbeans.modules.asm.core.dataobjects.AsmDataObject;
import org.netbeans.modules.asm.core.dataobjects.AsmObjectUtilities;
import org.netbeans.modules.asm.model.AsmModel;

final class NavigatorTopComponent extends TopComponent implements LookupListener {
    
    private static NavigatorTopComponent instance;

    static final String ICON_PATH = "org/netbeans/modules/asm/core/resources/asm_icon.png";
    
    private static final String PREFERRED_ID = "NavigatorTopComponent";
    
    //private NavigatorTab []tabs;
    
    private final RegisterUsagesPanel regUsagePanel;
            
    private NavigatorTopComponent() {
        initComponents();
        setIcon(Utilities.loadImage(ICON_PATH, true));        
        setName(NbBundle.getMessage(NavigatorTopComponent.class, "CTL_NavigatorTopComponent"));
        //setToolTipText(NbBundle.getMessage(NavigatorTopComponent.class, "HINT_NavigatorTopComponent"));
        //mainTabbedPanel.setVisible(false);
        
        regUsagePanel = RegisterUsagesPanel.getInstance();
        add(regUsagePanel, java.awt.BorderLayout.CENTER);
        regUsagePanel.setVisible(false);
        
        /*tabs = new NavigatorTab[] {      
            RegisterUsagesPanel.getInstance()
        };
         
        for (NavigatorTab tab : tabs) {
            mainTabbedPanel.add(tab.getName(), tab.getPanel());
        } */               
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized NavigatorTopComponent getDefault() {
        if (instance == null) {
            instance = new NavigatorTopComponent();
        }
        return instance;
    }
    
    /**
     * Obtain the NavigatorTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized NavigatorTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot find Navigator component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof NavigatorTopComponent) {
            return (NavigatorTopComponent)win;
        }
        ErrorManager.getDefault().log(ErrorManager.WARNING, "There seem to be multiple components with the '" + PREFERRED_ID + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
          
    private Lookup.Result<AsmDataObject> lookupResult;
    
    @Override
    public void componentShowing() {        
        lookupResult = Utilities.actionsGlobalContext().lookup(new Lookup.Template<AsmDataObject>(AsmDataObject.class));
        lookupResult.addLookupListener (this);
        resultChanged (null);
    }
    
    @Override
    public void componentHidden() {      
        lookupResult.removeLookupListener(this);
        regUsagePanel.setVisible(false);
        //mainTabbedPanel.setVisible(false);
        lookupResult = null;
        setActivatedNodes(new Node[0]);
    }

    
    public void resultChanged(LookupEvent lookupEvent) {
        Collection <? extends DataObject> objs = lookupResult.allInstances();
        DataObject dob = objs.isEmpty() ? null : objs.iterator().next();  
        
        if (dob == null) {
            setActivatedNodes(new Node[0]);            
            //mainTabbedPanel.setVisible(false);
            regUsagePanel.setVisible(false);
            return;                         
        }
        
        setActivatedNodes(new Node[] {dob.getNodeDelegate()});
        
        AsmModel model = AsmObjectUtilities.getModel(dob);
        
        if (model != null) {
            addPanelsForModel(dob);
        }        
    }    
    
    private void addPanelsForModel(DataObject dob) {      
        //mainTabbedPanel.setVisible(true);
        regUsagePanel.setVisible(true);
        regUsagePanel.setDocument(dob);
        
        /*for (NavigatorTab tab : tabs) {
            tab.setDocument(dob);
        }*/
    }
    
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }
    
    @Override
    public String preferredID() {
        return PREFERRED_ID;
    }
                
    static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return NavigatorTopComponent.getDefault();
        }
    }    
}
