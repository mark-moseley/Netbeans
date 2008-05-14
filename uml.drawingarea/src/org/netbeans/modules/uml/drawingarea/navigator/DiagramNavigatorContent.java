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


/*
 * DiagramNavigatorPanel.java
 *
 * Created on June 4, 2007, 11:37 AM
 */

package org.netbeans.modules.uml.drawingarea.navigator;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.openide.windows.TopComponent;

/**
 *
 * @author  Jyothi
 */
public class DiagramNavigatorContent extends JPanel implements PropertyChangeListener {
    
    private JComponent currentView = null;
    
    /** Creates new form DiagramNavigatorPanel */
    public DiagramNavigatorContent() {
        initComponents();
//        System.err.println("@@@@ DiagNavContent");
    }
    
    public void navigate(final JComponent satelliteView) {
//        System.err.println(" navigate().. ");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JComponent view =  satelliteView;
                if(view != null) {
                    if(currentView != null) {
                        remove(currentView);
                    }
                    add(view, BorderLayout.CENTER);
                    currentView = view;
                    invalidate();
                    validate();
                }
            }
        });
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        if(property.equals(TopComponent.Registry.PROP_ACTIVATED)) {
            TopComponent view = (TopComponent) evt.getNewValue();
            Object ctrl = view.getLookup().lookup(DesignerScene.class);
            if(ctrl instanceof DesignerScene) {
                navigate(((DesignerScene)ctrl).getSatelliteView());
            }
        }
    }
        
        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the Form Editor.
         */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
    }
