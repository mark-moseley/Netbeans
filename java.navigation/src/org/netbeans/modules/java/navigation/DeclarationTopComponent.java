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

package org.netbeans.modules.java.navigation;

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.Utilities;

/**
 * Top component which displays something.
 * 
 * @author Sandip V. Chitale (Sandip.Chitale@Sun.Com)
 */
public final class DeclarationTopComponent extends TopComponent {
    
    private static final Logger LOGGER = Logger.getLogger(DeclarationTopComponent.class.getName());
    
    private static DeclarationTopComponent instance;
    /** path to the icon used by the component and its open action */
    public static final String ICON_PATH = "org/netbeans/modules/java/navigation/resources/declaration_action.png";
    
    private static final String PREFERRED_ID = "DeclarationTopComponent";
    
    private static boolean shouldUpdate;
    
    private DeclarationTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(DeclarationTopComponent.class, "CTL_DeclarationTopComponent"));
        setToolTipText(NbBundle.getMessage(DeclarationTopComponent.class, "HINT_DeclarationTopComponent"));
        setIcon(Utilities.loadImage(ICON_PATH, true));
        
        // Don't highlight caret row 
        declarationEditorPane.putClientProperty(
            "HighlightsLayerExcludes", // NOI18N
            "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" // NOI18N
        );
    }
    
    private static final Rectangle ZERO = new Rectangle(0,0,1,1);
    
    void setDeclaration(String declaration) {
        if (declaration == null) {
            declarationEditorPane.setText("");
        } else {
            declarationEditorPane.setText(declaration);
        }
        declarationEditorPane.setCaretPosition(0);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                declarationEditorPane.scrollRectToVisible(ZERO);
            }
        });
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        declarationScrollPane = new javax.swing.JScrollPane();
        declarationEditorPane = new javax.swing.JEditorPane();

        declarationEditorPane.setBackground(new java.awt.Color(238, 238, 255));
        declarationEditorPane.setContentType("text/x-java");
        declarationEditorPane.setEditable(false);
        declarationScrollPane.setViewportView(declarationEditorPane);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(declarationScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(declarationScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane declarationEditorPane;
    private javax.swing.JScrollPane declarationScrollPane;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized DeclarationTopComponent getDefault() {
        if (instance == null) {
            instance = new DeclarationTopComponent();
        }
        return instance;
    }
    
    /**
     * Obtain the DeclarationTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized DeclarationTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            LOGGER.log(Level.WARNING, 
                    "Cannot find MyWindow component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof DeclarationTopComponent) {
            return (DeclarationTopComponent)win;
        }
        LOGGER.log(Level./* Shut up! Logged dozens of times in every session. */FINE,
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }
    
    public static boolean shouldUpdate() {
        if ( instance == null ) {
            return false;
        }
        else  {
            return instance.isShowing();
        }
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    public void componentOpened() {
    }
    
    public void componentClosed() {
    }
    
    @Override
    protected void componentShowing() {
        super.componentShowing();
        CaretListeningFactory.runAgain();
    }
    
         
    /** replaces this in object stream */
    public Object writeReplace() {
        return new ResolvableHelper();
    }
    
    protected String preferredID() {
        return PREFERRED_ID;
    }
    
    final static class ResolvableHelper implements Serializable {
        private static final long serialVersionUID = 1L;
        public Object readResolve() {
            return DeclarationTopComponent.getDefault();
        }
    }
    
}
