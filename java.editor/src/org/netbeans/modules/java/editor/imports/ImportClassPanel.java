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

package org.netbeans.modules.java.editor.imports;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.KeyStroke;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.ui.ElementIcons;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.java.editor.overridden.PopupUtil;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class ImportClassPanel extends javax.swing.JPanel {

    private JavaSource javaSource;
    private DefaultListModel model;
    private final int position;
    
    /** Creates new form ImportClassPanel */
    public ImportClassPanel(List<TypeElement> priviledged, List<TypeElement> denied, Font font, JavaSource javaSource, int position ) {
        // System.err.println("priviledged=" + priviledged);
        // System.err.println("denied=" + denied);
        this.javaSource = javaSource;
        this.position = position;
        createModel(priviledged, denied);
        initComponents();
        setBackground(jList1.getBackground());
        
        if ( model.size() > 0) {
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
        else {            
            remove( jScrollPane1 );
            JLabel nothingFoundJL = new JLabel("<No Classes Found>");
            if ( font != null ) {
                nothingFoundJL.setFont(font);
            }
            nothingFoundJL.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 4, 4, 4));
            nothingFoundJL.setEnabled(false);
            nothingFoundJL.setBackground(jList1.getBackground());
            //nothingFoundJL.setOpaque(true);
            add( nothingFoundJL );
        }
	
	setA11Y();
    }
    
    private void setA11Y() {
	this.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ImportClassPanel.class, "ImportClassPanel_ACN"));
	this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportClassPanel.class, "ImportClassPanel_ACSD"));
	jList1.getAccessibleContext().setAccessibleName(NbBundle.getMessage(ImportClassPanel.class, "ImportClassPanel_JList1_ACN"));
	jList1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ImportClassPanel.class, "ImportClassPanel_JList1_ACSD"));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        ctrlLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(64, 64, 64)));
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 4, 4, 4));

        jList1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                listKeyReleased(evt);
            }
        });
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                listMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jLabel1.setLabelFor(jList1);
        jLabel1.setText("Type to import:");
        jLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        jLabel1.setOpaque(true);
        add(jLabel1, java.awt.BorderLayout.PAGE_START);

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        jPanel1.setLayout(new java.awt.BorderLayout());

        ctrlLabel.setText(org.openide.util.NbBundle.getMessage(ImportClassPanel.class, "LBL_PackageImport")); // NOI18N
        jPanel1.add(ctrlLabel, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void listMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listMouseReleased
        importClass( 
                getSelected(), 
                (evt.getModifiers() & InputEvent.ALT_MASK) > 0,
                (evt.getModifiers() & InputEvent.SHIFT_MASK) > 0);
    }//GEN-LAST:event_listMouseReleased

    private void listKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_listKeyReleased
        KeyStroke ks = KeyStroke.getKeyStrokeForEvent(evt);
        if ( ks.getKeyCode() == KeyEvent.VK_ENTER || 
             ks.getKeyCode() == KeyEvent.VK_SPACE ) {
            importClass( 
                    getSelected(),
                    (evt.getModifiers() & InputEvent.ALT_MASK) > 0,
                    (evt.getModifiers() & InputEvent.SHIFT_MASK) > 0);
        }
    }//GEN-LAST:event_listKeyReleased
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel ctrlLabel;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JList jList1;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    
    public String getSelected() {
        TypeDescription typeDescription = ((TypeDescription)jList1.getSelectedValue());
        return typeDescription == null ? null : typeDescription.qualifiedName;
    }
    
    private void createModel( List<TypeElement> priviledged, List<TypeElement> denied ) {
                
        List<TypeDescription> l = new ArrayList<TypeDescription>( priviledged.size() );                
        for (TypeElement typeElement : priviledged) {
            l.add( new TypeDescription( typeElement, false ) );            
        }
        
        List<TypeDescription> ld = new ArrayList<TypeDescription>( priviledged.size() );                        
        for (TypeElement typeElement : denied ) {
            l.add( new TypeDescription( typeElement, true ) );
        }
        
        Collections.sort( l );
        
        model = new DefaultListModel();
        for( TypeDescription td : l ) {
            model.addElement( td );
        }
        
        
        
    }
    
    private void importClass( String name, final boolean packageImport, final boolean useFqn ) {
        PopupUtil.hidePopup();
        
        if ( packageImport && !useFqn ) {
            int index = name.lastIndexOf('.'); // NOI18N 
            if ( index != -1 ) {
                name = name.substring(0, index) + ".*"; // NOI18N        
            }
        }
        
        final String fqn = name;
        
        if (fqn != null) {
            Task<WorkingCopy> task = new Task<WorkingCopy>() {
                
                public void run(final WorkingCopy wc) throws IOException {
                    wc.toPhase(Phase.RESOLVED);
                    TreeMaker make = wc.getTreeMaker();
                    CompilationUnitTree cut = wc.getCompilationUnit();
                    // make a copy of list
                    List<ImportTree> imports = new ArrayList<ImportTree>(cut.getImports());
                    
                    if ( useFqn && replaceSimpleName(fqn, wc) ) {                        
                        return;
                    }
                    
                    // Test whether already imported                    
                    if ( isImported(fqn, imports)) {
                        Utilities.setStatusText(EditorRegistry.lastFocusedComponent(), 
                        NbBundle.getMessage(
                                ImportClassPanel.class,
                                packageImport ? "MSG_PackageAlreadyImported" : "MSG_ClassAlreadyImported", 
                                fqn));
                        return;
                    }
                    
                                     
                    // prepare the import tree to add
                    ImportTree njuImport = make.Import(make.Identifier(fqn), false);
                    for (ListIterator<ImportTree> it = imports.listIterator(); it.hasNext(); ) {
                        ImportTree item = it.next();
                        if (item.isStatic() || item.getQualifiedIdentifier().toString().compareTo(fqn) > 0) {
                            it.set(njuImport);
                            it.add(item);
                            break;
                        }
                    }
                    CompilationUnitTree cutCopy;
                    // import was inserted somewhere to inside the list, prepare
                    // copy of compilation unit.
                    if (imports.contains(njuImport)) {
                        cutCopy = make.CompilationUnit(
                            cut.getPackageName(),
                            imports,
                            cut.getTypeDecls(),
                            cut.getSourceFile()
                        );
                    } else {
                        // import section was not modified by for loop,
                        // either it means the section is empty or
                        // the import has to be added to the end of the section.
                        // prepare copy of compilation unit tree.
                        cutCopy = make.addCompUnitImport(cut, njuImport);
                    }
                    wc.rewrite(cut, cutCopy);
                }
                
                private boolean replaceSimpleName(String fqn, WorkingCopy wc) {
                    
                    TreeUtilities tu = wc.getTreeUtilities();
                    TreePath tp = tu.pathFor(position);
                    TreeMaker tm = wc.getTreeMaker();
                    
                    if ( tp.getLeaf().getKind() == Tree.Kind.IDENTIFIER) {
                        wc.rewrite(tp.getLeaf(), tm.Identifier(fqn));
                        return true;
                    }
                    return false;
                }
                
                private boolean isImported(String fqn, List<ImportTree> imports) {
                    for (ImportTree i : imports) {
                        if( fqn.equals( i.getQualifiedIdentifier().toString() )) {
                            return true;
                        }
                    }
                    return false;   
                    
                }
                
            };
            try {
                javaSource.runModificationTask(task).commit();
            } catch (IOException ex) {
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
                setIcon( ElementIcons.getElementIcon( td.kind, null ) );
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
