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

package org.netbeans.modules.java.editor.imports;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.text.BadLocationException;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.modules.java.editor.overridden.PopupUtil;
import org.openide.ErrorManager;

/**
 *
 * @author Jan Lahoda
 */
public class ImportClassPanel extends javax.swing.JPanel {
    
    private JavaSource javaSource;
    private DefaultListModel model;
    
    /** Creates new form ImportClassPanel */
    public ImportClassPanel(List<TypeElement> priviledged, List<TypeElement> denied, Font font, JavaSource javaSource ) {
        // System.err.println("priviledged=" + priviledged);
        // System.err.println("denied=" + denied);
        this.javaSource = javaSource;
        createModel(priviledged, denied);
        initComponents();
        jList1.setModel( model );
        setFocusable(false);
        setNextFocusableComponent(jList1);
        jScrollPane1.setBackground( jList1.getBackground() );
        setBackground( jList1.getBackground() );
        if ( font != null ) {
            jList1.setFont(font);
        }
        int modelSize = jList1.getModel().getSize();
        if ( modelSize > 0 ) {
            jList1.setSelectedIndex(0);            
        }
        jList1.setVisibleRowCount( modelSize > 8 ? 8 : modelSize );
        jList1.setCellRenderer( new Renderer( jList1 ) );
        jList1.grabFocus();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();

        setLayout(new java.awt.BorderLayout());

        setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(74, 74, 74)), javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        jScrollPane1.setBorder(null);
        jList1.setBorder(null);
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                listMouseReleased(evt);
            }
        });
        jList1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                listKeyReleased(evt);
            }
        });

        jScrollPane1.setViewportView(jList1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

    }// </editor-fold>//GEN-END:initComponents

    private void listMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listMouseReleased
        importClass( getSelected() );
    }//GEN-LAST:event_listMouseReleased

    private void listKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_listKeyReleased
        KeyStroke ks = KeyStroke.getKeyStrokeForEvent(evt);
        if ( ks.getKeyCode() == KeyEvent.VK_ENTER || 
             ks.getKeyCode() == KeyEvent.VK_SPACE ) {
            importClass( getSelected() );
        }
    }//GEN-LAST:event_listKeyReleased
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JList jList1;
    public javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    
    public String getSelected() {
        TypeDescription typeDescription = ((TypeDescription)jList1.getSelectedValue());
        return typeDescription == null ? null : typeDescription.qualifiedName;
    }
    
    private void createModel( List<TypeElement> priviledged, List<TypeElement> denied ) {
                
        List<TypeDescription> l = new ArrayList( priviledged.size() );                
        for (TypeElement typeElement : priviledged) {
            l.add( new TypeDescription( typeElement, false ) );            
        }
        
        List<TypeDescription> ld = new ArrayList( priviledged.size() );                        
        for (TypeElement typeElement : denied ) {
            l.add( new TypeDescription( typeElement, true ) );
        }
        
        Collections.sort( l );
        
        model = new DefaultListModel();
        for( TypeDescription td : l ) {
            model.addElement( td );
        }
        
        
        
    }
    
    private void importClass( final String fqn ) {
        PopupUtil.hidePopup();
        
        if ( fqn != null ) {

            
            CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
                public void cancel() {}
                public void run(final CompilationController ci) throws IOException {
                    try {
                        SourceUtils.addImports(ci, Collections.singletonList(fqn));
                    }            
                    catch (IOException ex) {
                        ErrorManager.getDefault().notify(ex);
                    } 
                    catch (BadLocationException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }            
                }
            };
            
            try {
                javaSource.runUserActionTask(task, true); // XXX Do it using TreeMaker
            }
            catch (IOException ex ) {
                ErrorManager.getDefault().notify(ex);
            }
                                    
        }
    }
            
    private static class Renderer extends DefaultListCellRenderer {
        
        private static int DARKER_COLOR_COMPONENT = 5;
        private static int LIGHTER_COLOR_COMPONENT = DARKER_COLOR_COMPONENT;
                
        
        private Color denidedColor = new Color( 0x80, 0x80, 0x80 ); 
        private Color fgColor;
        private Color bgColor;
        private Color bgColorDarker;
        private Color bgSelectionColor;
        private Color fgSelectionColor;
        
        public Renderer( JList list ) {
            setFont( list.getFont() );            
            fgColor = list.getForeground();
            bgColor = list.getBackground();
            bgColorDarker = new Color(
                                    Math.abs(bgColor.getRed() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getGreen() - DARKER_COLOR_COMPONENT),
                                    Math.abs(bgColor.getBlue() - DARKER_COLOR_COMPONENT)
                            );
            bgSelectionColor = list.getSelectionBackground();
            fgSelectionColor = list.getSelectionForeground();        
        }
        
        public Component getListCellRendererComponent( JList list,
                                                       Object value,
                                                       int index,
                                                       boolean isSelected,
                                                       boolean hasFocus) {
                        
            if ( isSelected ) {
                setForeground(fgSelectionColor);
                setBackground(bgSelectionColor);
            }
            else {
                setForeground(fgColor);
                setBackground( index % 2 == 0 ? bgColor : bgColorDarker );
            }
            
            if ( value instanceof TypeDescription ) {
                TypeDescription td = (TypeDescription)value;                
                 // setIcon(td.getIcon());
                setText(td.qualifiedName);
                if ( td.isDenied ) {
                    setForeground( denidedColor );
                }
                setIcon( UiUtils.getElementIcon( td.kind, null ) );
            }
            else {
                setText( value.toString() );
            }
                                    
            return this;
        }
        
     }
     
     private static class TypeDescription implements Comparable<TypeDescription> {
         private boolean isDenied;
         private final ElementKind kind;
         private final String qualifiedName;
                          
         public TypeDescription(TypeElement typeElement, boolean isDenied ) {
            this.isDenied = isDenied;
            this.kind = typeElement.getKind();
            this.qualifiedName = typeElement.getQualifiedName().toString();
         } 

        public int compareTo( TypeDescription o ) {
            
            if ( isDenied && !o.isDenied ) {
                return 1;
            }
            else if ( !isDenied && o.isDenied ) {
                return -1;
            }
            else {
                return qualifiedName.compareTo( o.qualifiedName );
            }        
        }
         
         
         
     }
            

}
