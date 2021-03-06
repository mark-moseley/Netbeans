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
package org.netbeans.modules.java.editor.overridden;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ImplementationProvider;
import org.netbeans.editor.JumpList;
import org.netbeans.editor.Utilities;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public final class IsOverriddenAnnotationAction extends AbstractAction {
    
    public IsOverriddenAnnotationAction() {
        putValue(NAME, NbBundle.getMessage(IsOverriddenAnnotationAction.class,
                                          "CTL_IsOverriddenAnnotationAction")); //NOI18N
        setEnabled(true);
    }
    
    public void actionPerformed(ActionEvent e) {
        if (!invokeDefaultAction((JTextComponent) e.getSource())) {
            Action actions[] = ImplementationProvider.getDefault().getGlyphGutterActions((JTextComponent) e.getSource());
            
            if (actions == null)
                return ;
            
            int nextAction = 0;
            
            while (nextAction < actions.length && actions[nextAction] != this)
                nextAction++;
            
            nextAction++;
            
            if (actions.length > nextAction) {
                Action a = actions[nextAction];
                if (a!=null && a.isEnabled()){
                    a.actionPerformed(e);
                }
            }
        }
    }
    
    private FileObject getFile(JTextComponent component) {
        Document doc = component.getDocument();
        DataObject od = (DataObject) doc.getProperty(Document.StreamDescriptionProperty);
        
        if (od == null) {
            return null;
        }
        
        return od.getPrimaryFile();
    }
    
    private IsOverriddenAnnotation findAnnotation(JTextComponent component, AnnotationDesc desc, int offset) {
        FileObject file = getFile(component);
        
        if (file == null) {
            if (ErrorManager.getDefault().isLoggable(ErrorManager.WARNING)) {
                ErrorManager.getDefault().log(ErrorManager.WARNING, "component=" + component + " does not have a file specified in the document."); //NOI18N
            }
            return null;
        }
        
        AnnotationsHolder ah = AnnotationsHolder.get(file);

        if (ah == null) {
            IsOverriddenAnnotationHandler.LOG.log(Level.INFO, "component=" + component + " does not have attached a IsOverriddenAnnotationHandler"); //NOI18N

            return null;
        }

        for(IsOverriddenAnnotation a : ah.getAnnotations()) {
            if (   a.getPosition().getOffset() == offset
                && desc.getShortDescription().equals(a.getShortDescription())) {
                return a;
            }
        }
        
        return null;
    }
    
    boolean invokeDefaultAction(final JTextComponent comp) {
        final Document doc = comp.getDocument();
        
        if (doc instanceof BaseDocument) {
            final int currentPosition = comp.getCaretPosition();
            final Annotations annotations = ((BaseDocument) doc).getAnnotations();
            final IsOverriddenAnnotation[] annotation = new IsOverriddenAnnotation[1];
            final Point[] p = new Point[1];
            
            doc.render(new Runnable() {
                public void run() {
                    try {
                        int line = Utilities.getLineOffset((BaseDocument) doc, currentPosition);
                        int startOffset = Utilities.getRowStartFromLineOffset((BaseDocument) doc, line);
                        AnnotationDesc desc = annotations.getActiveAnnotation(line);
                        p[0] = comp.modelToView(startOffset).getLocation();
                        annotation[0] = findAnnotation(comp, desc, startOffset);
                    }  catch (BadLocationException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            });
            
            if (annotation[0] == null)
                return false;
            
            JumpList.checkAddEntry(comp, currentPosition);
            
            annotation[0].mouseClicked(comp, p[0]);
            
            return true;
        }
        
        return false;
    }
    
}