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

package org.netbeans.modules.languages.css;

import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.languages.Context;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jan Jancura
 */
public class CSS {

    public static String navigatorTooltip (Context context) {
        if (!(context instanceof SyntaxContext)) return null;
        ASTNode n = (ASTNode) ((SyntaxContext) context).getASTPath ().getLeaf ();
        StringBuilder sb = new StringBuilder ();
        sb.append ("<html>");
        sb.append ("<p style=\"");
        ASTNode body = n.getNode ("body");
        if (body == null) return null;
        ASTNode declarations = body.getNode ("declarations");
        if (declarations == null) return null;
        String s = declarations.getAsText ();
        s = s.replaceAll ("\"", "");
        sb.append (s);
        sb.append ("\">Text Preview.</p>");
        sb.append ("</html>");
        return sb.toString ();
    }
    
    public static Runnable hyperlink (Context context) {
        Token t = context.getTokenSequence ().token ();
        String s = t.id ().name ();
        s = s.substring (1, s.length () - 1);
        s = s.replace ("%20", " "); // HACK
        Lookup l = TopComponent.getRegistry ().getActivated ().getLookup ();
        DataObject dob = (DataObject) l.lookup (DataObject.class);
        FileObject fo = dob.getPrimaryFile ().getParent ();
        final FileObject file = fo.getFileObject (s);
        if (file != null)
            return new Runnable () {
                public void run () {
                    try {
                        DataObject d = DataObject.find (file);
                        OpenCookie oc = (OpenCookie) d.getCookie (OpenCookie.class);
                        oc.open ();
                    } catch (DataObjectNotFoundException ex) {
                    }
                }
            };
        return null;
    }
}
