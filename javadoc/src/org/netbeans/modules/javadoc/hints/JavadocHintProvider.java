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

package org.netbeans.modules.javadoc.hints;

import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.text.Document;
import org.netbeans.api.java.queries.AccessibilityQuery;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * A hint provider checking missing and broken javadocs.
 * 
 * @author Jan Pokorsky
 */
public final class JavadocHintProvider extends AbstractHint {
    
    public static final String SCOPE_KEY = "scope";             // NOI18N
    public static final String SCOPE_DEFAULT = "protected"; // NOI18N

    private boolean createJavadocKind;
    
    private JavadocHintProvider(boolean createJavadocKind) {
        super( false, true, createJavadocKind ? AbstractHint.HintSeverity.CURRENT_LINE_WARNING : AbstractHint.HintSeverity.WARNING );
        this.createJavadocKind = createJavadocKind;
    }
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.METHOD, Kind.CLASS, Kind.VARIABLE);
    }
    
    public List<ErrorDescription> run(CompilationInfo javac, TreePath path) {
        if (Boolean.FALSE.equals(AccessibilityQuery.isPubliclyAccessible(javac.getFileObject().getParent()))) {
            return null;
        }
        
        if (javac.getElements().getTypeElement("java.lang.Object") == null) { // NOI18N
            // broken java platform
            return Collections.<ErrorDescription>emptyList();
        }
        
        HintSeverity hintSeverity = getSeverity();
        Severity severity = hintSeverity.toEditorSeverity();
                
        Document doc = null;
        
        try {
            doc = javac.getDocument();
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        
        if (doc == null) {
            return null;
        }
        
        Access access = Access.resolve(getPreferences(null).get(SCOPE_KEY, SCOPE_DEFAULT));
        Analyzer a = new Analyzer(javac, doc, path, severity, hintSeverity, createJavadocKind, access);
        return a.analyze();
    }

    public void cancel() {
        //XXX implement me;
    }
    
    public String getId() {
        return createJavadocKind ? "create-javadoc" : "error-in-javadoc"; //NOI18N //NOI18N
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(JavadocHintProvider.class, createJavadocKind ? "DN_CREATE_JAVADOC_HINT" : "DN_ERROR_IN_JAVADOC_HINT"); // NOI18N
    }
    
    public String getDescription() {
        return NbBundle.getMessage(JavadocHintProvider.class, createJavadocKind ? "DESC_CREATE_JAVADOC_HINT" : "DESC_ERROR_IN_JAVADOC_HINT"); // NOI18N
    }
        
    
    @Override
    public JComponent getCustomizer(final Preferences node) {
        JPanel outerPanel = new JPanel( new GridBagLayout() );
        outerPanel.setOpaque( false );
        
        JPanel res = new JPanel( new GridBagLayout() );
        res.setOpaque( false );
        res.setBorder( BorderFactory.createTitledBorder(NbBundle.getMessage(JavadocHintProvider.class, "LBL_SCOPE") ) ); //NOI18N
        ActionListener l = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JRadioButton rb = (JRadioButton)e.getSource();
                node.put( SCOPE_KEY, rb.getText() );
            }
        };
        ButtonGroup group = new ButtonGroup();
        
        int row = 0;
        JRadioButton radio = new JRadioButton();
        Mnemonics.setLocalizedText(radio, NbBundle.getMessage(JavadocHintProvider.class, "CTL_PUBLIC_OPTION")); // NOI18N
        radio.setToolTipText(NbBundle.getMessage(JavadocHintProvider.class, "HINT_PUBLIC_OPTION")); // NOI18N
        radio.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JavadocHintProvider.class, "ACD_PUBLIC_OPTION")); // NOI18N
        radio.getAccessibleContext().setAccessibleName(NbBundle.getMessage(JavadocHintProvider.class, "ACN_PUBLIC_OPTION")); // NOI18N
        radio.addActionListener( l );
        group.add( radio );
        radio.setSelected( radio.getText().equals( node.get(SCOPE_KEY, SCOPE_DEFAULT) ) );
        radio.setOpaque(false);
        res.add( radio, new GridBagConstraints(0,row++,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, new Insets(8,8,0,8),0,0 ) );
        
        radio = new JRadioButton();
        Mnemonics.setLocalizedText(radio, NbBundle.getMessage(JavadocHintProvider.class, "CTL_PROTECTED_OPTION")); // NOI18N
        radio.setToolTipText(NbBundle.getMessage(JavadocHintProvider.class, "HINT_PROTECTED_OPTION")); // NOI18N
        radio.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JavadocHintProvider.class, "ACD_PROTECTED_OPTION")); // NOI18N
        radio.getAccessibleContext().setAccessibleName(NbBundle.getMessage(JavadocHintProvider.class, "ACN_PROTECTED_OPTION")); // NOI18N
        radio.addActionListener( l );
        group.add( radio );
        radio.setSelected( radio.getText().equals( node.get(SCOPE_KEY, SCOPE_DEFAULT) ) );
        radio.setOpaque(false);
        res.add( radio, new GridBagConstraints(0,row++,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, new Insets(8,8,0,8),0,0 ) );
        
        radio = new JRadioButton();
        Mnemonics.setLocalizedText(radio, NbBundle.getMessage(JavadocHintProvider.class, "CTL_PACKAGE_OPTION")); // NOI18N
        radio.setToolTipText(NbBundle.getMessage(JavadocHintProvider.class, "HINT_PACKAGE_OPTION")); // NOI18N
        radio.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JavadocHintProvider.class, "ACD_PACKAGE_OPTION")); // NOI18N
        radio.getAccessibleContext().setAccessibleName(NbBundle.getMessage(JavadocHintProvider.class, "ACN_PACKAGE_OPTION")); // NOI18N
        radio.addActionListener( l );
        group.add( radio );
        radio.setSelected( radio.getText().equals( node.get(SCOPE_KEY, SCOPE_DEFAULT) ) );
        radio.setOpaque(false);
        res.add( radio, new GridBagConstraints(0,row++,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, new Insets(8,8,0,8),0,0 ) );
        
        radio = new JRadioButton();
        Mnemonics.setLocalizedText(radio, NbBundle.getMessage(JavadocHintProvider.class, "CTL_PRIVATE_OPTION")); // NOI18N
        radio.setToolTipText(NbBundle.getMessage(JavadocHintProvider.class, "HINT_PRIVATE_OPTION")); // NOI18N
        radio.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(JavadocHintProvider.class, "ACD_PRIVATE_OPTION")); // NOI18N
        radio.getAccessibleContext().setAccessibleName(NbBundle.getMessage(JavadocHintProvider.class, "ACN_PRIVATE_OPTION")); // NOI18N
        radio.addActionListener( l );
        group.add( radio );
        radio.setSelected( radio.getText().equals( node.get(SCOPE_KEY, SCOPE_DEFAULT) ) );
        radio.setOpaque(false);
        res.add( radio, new GridBagConstraints(0,row++,1,1,0.0,0.0,GridBagConstraints.WEST,GridBagConstraints.HORIZONTAL, new Insets(8,8,0,8),0,0 ) );
        
        outerPanel.add( res, new GridBagConstraints(0,0,1,1,0.0,0.0,GridBagConstraints.NORTHWEST,GridBagConstraints.BOTH, new Insets(0,0,0,0),0,0 ) );
        outerPanel.add( new JLabel(), new GridBagConstraints(1,1,1,1,1.0,1.0,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE, new Insets(0,0,0,0),0,0 ) );
        return outerPanel;
    }
    
    public static JavadocHintProvider createCreateJavadoc() {
        return new JavadocHintProvider(true);
    }
    
    public static JavadocHintProvider createErrorInJavadoc() {
        return new JavadocHintProvider(false);
    }
    
}
