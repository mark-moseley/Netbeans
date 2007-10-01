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
/*
 * OpenAction.java
 *
 * Created on September 24, 2004, 8:41 PM
 */

package org.netbeans.modules.java.navigation.actions;

import java.awt.Toolkit;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.openide.awt.*;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.*;
import org.openide.filesystems.FileObject;
import org.openide.nodes.*;
import org.openide.util.*;

import javax.swing.*;
import java.awt.event.*;

/**
 * An action that opens editor and jumps to the element given in constructor.
 * Similar to editor's go to declaration action.
 *
 * @author tim, Dafe Simonek
 */
public final class OpenAction extends AbstractAction {
    
    private ElementHandle<? extends Element> elementHandle;   
    private FileObject fileObject;
    private String displayName;
      
    public OpenAction( ElementHandle<? extends Element> elementHandle, FileObject fileObject ) {
        this( elementHandle, fileObject, null );
    }
    
    public OpenAction( ElementHandle<? extends Element> elementHandle, FileObject fileObject, String displayName ) {
        this.elementHandle = elementHandle;
        this.fileObject = fileObject;
        this.displayName = displayName;
        putValue ( Action.NAME, NbBundle.getMessage ( OpenAction.class, "LBL_Goto" ) ); //NOI18N
    }
    
    public void actionPerformed (ActionEvent ev) {
        if( null == fileObject ) {
            Toolkit.getDefaultToolkit().beep();
            if( null != displayName ) {
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(OpenAction.class, "MSG_NoSource", displayName) );  //NOI18N
            }
        } else {
            ElementOpen.open(fileObject, elementHandle);
        }
        // find source
//        boolean isSourceAvailable = false;
//        repo.beginTrans(false);
//        try {
//            if (!elem.isValid()) {
//                return;
//            }
//            ClassDefinition declaringClass = elem instanceof ClassDefinition ?
//                    (ClassDefinition)elem : ((Feature)elem).getDeclaringClass();
//            isSourceAvailable = JUtils.getSourceForBinary(declaringClass) != null;
//        } finally {
//            repo.endTrans();
//        }
//        
//        if (isSourceAvailable) {
//            openElement(elem);
//        } else {
//            // no source attached
//            StatusDisplayer.getDefault().setStatusText(
//                    NbBundle.getMessage(OpenAction.class, "MSG_NoSource", elem)  //NOI18N
//            ); 
//        }
    }

    public boolean isEnabled () {
          return true;
    }

    
    // following hard-to-read code is copied from editor/JavaKit class. fuj.
//
//    /** Opens source code of given element in editor and goes to element
//     * Works only if element really has source
//     */
//    private static boolean openElement(final Element element) {
//        repo.beginTrans(false);
//        try {
//            Resource resource = element.isValid() ? element.getResource() : null;
//            if (resource != null) {
//                JavaModel.setClassPath(resource);
//                DataObject dob = jmm.getDataObject(resource);
//                if (dob != null) {
//                    final EditorCookie.Observable ec = (EditorCookie.Observable)dob.getCookie(EditorCookie.Observable.class);
//                    if (ec != null) {
//                        StatusDisplayer.getDefault().setStatusText(
//                                NbBundle.getMessage(OpenAction.class, "MSG_OpeningElement", // NOI18N
//                                element instanceof NamedElement ? ((NamedElement)element).getName() : "")
//                        ); 
//                        SwingUtilities.invokeLater(new Runnable() {
//                            public void run() {
//                                JEditorPane[] panes = ec.getOpenedPanes();
//                                if (panes != null && panes.length > 0) {
//                                    // editor already opened, so just select
//                                    selectElementInPane(panes[0], element, false);
//                                } else {
//                                    // editor not yet
//                                    ec.addPropertyChangeListener(new PropertyChangeListener() {
//                                        public void propertyChange(PropertyChangeEvent evt) {
//                                            if (EditorCookie.Observable.PROP_OPENED_PANES.equals(evt.getPropertyName())) {
//                                                final JEditorPane[] panes = ec.getOpenedPanes();
//                                                if (panes != null && panes.length > 0) {
//                                                    selectElementInPane(panes[0], element, true);
//                                                }
//                                                ec.removePropertyChangeListener(this);
//                                            }
//                                        }
//                                    });
//                                    ec.open();
//                                }
//                            }
//
//                        });
//                        return true;
//                    }
//                }
//            }
//        } finally {
//            repo.endTrans(false);
//        }
//        return false;
//    }
// 
//    /** Jumps to element in given editor pane. When delayProcessing is 
//     * specified, waits for real visible open before jump 
//     */
//    private static void selectElementInPane(final JEditorPane pane, final Element element, boolean delayProcessing) {
//        //final Cursor editCursor = pane.getCursor();
//        //pane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//        if (delayProcessing) {
//            // [dafe] I don't know why, but editor guys are waiting for focus
//            // in delay processing, so I will do the same
//            pane.addFocusListener(new FocusAdapter() {
//                public void focusGained(FocusEvent e) {
//                    RequestProcessor.getDefault().post(new Runnable() {
//                        public void run() {
//                            jumpToElement(pane, element);
//                        }
//                    });
//                    pane.removeFocusListener(this);
//                }
//            });
//        } else {
//            // immediate processing
//            RequestProcessor.getDefault().post(new Runnable() {
//                public void run() {
//                    jumpToElement(pane, element);
//                }
//            });
//            // try to activate outer TopComponent
//            Container temp = pane;
//            // #81238 - don't throw exception when no outer TC exists
//            while (temp != null && !(temp instanceof TopComponent)) {
//                temp = temp.getParent();
//            }
//            if (temp != null) {
//                ((TopComponent) temp).requestActive();
//            }
//        }
//    }
//
//    /** Jumps to element on given editor pane. Call only outside AWT thread!
//     */
//    private static void jumpToElement (JEditorPane pane, Element element) {
//        int caretPos = pane.getCaretPosition();
//        Container parent = pane.getParent();
//        Point viewPos = parent instanceof JViewport ? ((JViewport)parent).getViewPosition() : null;
//        PositionBounds bounds = null;
//        // get elem position first
//        repo.beginTrans(false);
//        try {
//            if (element.isValid()) {
//                JavaModel.setClassPath(element.getResource());
//                bounds = jmm.getElementPosition(element);
//            }
//        } finally {
//            repo.endTrans(false);
//        }
//        // and actually jump if user not moving already
//        if (bounds != null && pane.getCaretPosition() == caretPos &&
//                (viewPos == null || viewPos.equals(((JViewport)parent).getViewPosition()))) {
//            pane.setCaretPosition(bounds.getBegin().getOffset());
//        }
//        StatusDisplayer.getDefault().setStatusText(""); // NOI18N
//    }

    
}
