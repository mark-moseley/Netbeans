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


package org.netbeans.modules.asm.core.hyperlink;

import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;

import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;

import org.netbeans.modules.asm.core.assistance.GoToLabelAction;
import org.netbeans.modules.asm.core.dataobjects.AsmObjectUtilities;
import org.netbeans.modules.asm.model.AsmState;

public class AsmHyperlinkProvider implements HyperlinkProvider {

    private Document lastDocument;
    private int []lastResult;
         
    private GoToLabelAction labelResolver;
       
    public AsmHyperlinkProvider() {
        labelResolver = new GoToLabelAction();
        lastResult = new int[] { -1, -1, -1 };
    }
    
    public boolean isHyperlinkPoint(Document doc, int offset) {                      
        AsmState state = AsmObjectUtilities.getAccessor(doc).getState();
        lastDocument = doc;
        if (state != null) {
            int res[] = labelResolver.computeLabel(state, offset);
            if (res[0] != -1)  {
                lastResult = res;
                return true;
            }                
        }
                                                   
        return false;
    }

    public int[] getHyperlinkSpan(Document doc, int offset) {
        if(doc != lastDocument && !checkResult()) {
            return null;
        }
        
        return new int[] { lastResult[0], 
                           lastResult[1] 
                         };                 
    }

    public void performClickAction(Document doc, int offset) {
        
        if(doc != lastDocument || !checkResult()) {
            return;
        }
        
        DataObject ob = NbEditorUtilities.getDataObject(doc);
        int position = lastResult[2];
        
        if (!openFileInEditor(ob)) {
            return;
        }
        
        EditorCookie ed = ob.getCookie(org.openide.cookies.EditorCookie.class);
        
        if (ed != null) {
            try {
                ed.openDocument();
            } catch (IOException ex) {
                return;
            }
            
            JEditorPane pane = ed.getOpenedPanes()[0];
            pane.setCaretPosition(position);
            
            TopComponent tc = (TopComponent)
                    SwingUtilities.getAncestorOfClass(TopComponent.class,
                                                      pane);
            if (tc != null) { 
                tc.requestActive();
            }
        }
    }

   private boolean checkResult() {
       return !(lastResult[0] == -1 || lastResult[1] == -1 ||
                lastResult[2] == -1);       
   }
    
    private boolean openFileInEditor(DataObject ob) {
        EditCookie ck = (EditCookie) ob.getCookie(EditCookie.class);
        if (ck != null) {
            ck.edit();
            return true;
        }
        OpenCookie oc = (OpenCookie) ob.getCookie(OpenCookie.class);
        if (oc != null) {
            oc.open();
            return true;
        }
        return false;
    }
    
}
