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

package org.netbeans.modules.javadoc.search;

import org.openide.windows.TopComponent;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import javax.swing.text.BadLocationException;


/**
 * Tries to find actual focused java word.
 *
 * @author Petr Hrebejk
 */
final class GetJavaWord extends Object {


    static String getCurrentJavaWord() {
        Node[] n = TopComponent.getRegistry ().getActivatedNodes ();

        if (n.length == 1) {
            EditorCookie ec = (EditorCookie) n[0].getCookie (EditorCookie.class);
            if (ec != null) {
                JEditorPane[] panes = ec.getOpenedPanes ();
                if ( panes == null )
                    return null;
                if (panes.length > 0) {
                    return forPane(panes[0]);
                }
            }
        }

        return null;
    }
    
    static String forPane(JEditorPane p) {
        if (p == null) return null;
 
        String selection = p.getSelectedText ();
 
        if ( selection != null && selection.length() > 0 ) {
            return selection;
        } else {
 
            // try to guess which word is underneath the caret's dot.
 
            Document doc = p.getDocument();
            Element lineRoot;
 
            if (doc instanceof StyledDocument) {
                lineRoot = NbDocument.findLineRootElement((StyledDocument)doc);
            } else {
                lineRoot = doc.getDefaultRootElement();
            }
            int dot = p.getCaret().getDot();
            Element line = lineRoot.getElement(lineRoot.getElementIndex(dot));
 
            if (line == null) return null;
 
            String text = null;
            try {
                text = doc.getText(line.getStartOffset(),
                    line.getEndOffset() - line.getStartOffset());
            } catch (BadLocationException e) {
                return null;
            }
            
            if ( text == null )
                return null;
            int pos = dot - line.getStartOffset();

            if ( pos < 0 || pos >= text.length() )
                return null;

            int bix, eix;

            for( bix = Character.isJavaIdentifierPart( text.charAt( pos ) ) ? pos : pos - 1;
                    bix >= 0 && Character.isJavaIdentifierPart( text.charAt( bix ) ); bix-- );
            for( eix = pos; eix < text.length() && Character.isJavaIdentifierPart( text.charAt( eix )); eix++ );

            return bix == eix ? null : text.substring( bix + 1, eix  );
        }
    }
}
