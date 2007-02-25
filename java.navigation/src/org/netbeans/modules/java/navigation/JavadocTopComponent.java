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
public final class JavadocTopComponent extends TopComponent {
    
    private static final Logger LOGGER = Logger.getLogger(JavadocTopComponent.class.getName());
    
    private static JavadocTopComponent instance;
    /** path to the icon used by the component and its open action */
    public static final String ICON_PATH = "org/netbeans/modules/java/navigation/resources/javadoc_action.png";
    
    private static final String PREFERRED_ID = "JavadocTopComponent";
    
    private JavadocTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(JavadocTopComponent.class, "CTL_JavadocTopComponent"));
        setToolTipText(NbBundle.getMessage(JavadocTopComponent.class, "HINT_JavadocTopComponent"));
        setIcon(Utilities.loadImage(ICON_PATH, true));
    }
    
    private static Rectangle zero = new Rectangle(0,0,1,1);

    void setJavadoc(String header, String javadoc){    
        if (javadoc == null) {
            javadocEditorPane.setText("");
        } else {
            javadoc = javadoc
                    .replaceAll("@author ",     "<b>Author:</b> ")
                    .replaceAll("@deprecated ", "<b>Deprecated:</b> ")
                    .replaceAll("@exception ",  "<b>Exception:</b> ")
                    .replaceAll("@param ",      "<b>Parameter:</b> ")
                    .replaceAll("@return ",     "<b>Return:</b> ")
                    .replaceAll("@see ",        "<b>See:</b> ")
                    .replaceAll("@since ",      "<b>Since:</b> ")
                    .replaceAll("@throws ",     "<b>Throws:</b> ")
                    .replaceAll("@version ",    "<b>Version:</b> ")
                    ;
            javadocEditorPane.setText(
                    "<html>" // NOI18N
                    + "<head>" // NOI18N
                    + "</head>" // NOI18N
                    + "<body>" // NOI18N
                    + (header == null ? "" : ("<b>" + header + "</b><br><hr>"))
                    + javadoc.replaceAll("\n", "<br>") // NOI18N
                    + "</body>" // NOI18N
                    + "</html>" // NOI18N
                    );
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                javadocEditorPane.scrollRectToVisible(zero);
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

        javadocScrollPane = new javax.swing.JScrollPane();
        javadocEditorPane = new javax.swing.JEditorPane();

        javadocEditorPane.setBackground(new java.awt.Color(255, 255, 222));
        javadocEditorPane.setContentType("text/html");
        javadocEditorPane.setEditable(false);
        javadocScrollPane.setViewportView(javadocEditorPane);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(javadocScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(javadocScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane javadocEditorPane;
    private javax.swing.JScrollPane javadocScrollPane;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link findInstance}.
     */
    public static synchronized JavadocTopComponent getDefault() {
        if (instance == null) {
            instance = new JavadocTopComponent();
        }
        return instance;
    }
    
    /**
     * Obtain the JavadocTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized JavadocTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            LOGGER.log(Level.WARNING, 
                       "Cannot find MyWindow component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof JavadocTopComponent) {
            return (JavadocTopComponent)win;
        }
        LOGGER.log(Level.WARNING, 
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    public void componentOpened() {
    }
    
    public void componentClosed() {
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
            return JavadocTopComponent.getDefault();
        }
    }
    
}
