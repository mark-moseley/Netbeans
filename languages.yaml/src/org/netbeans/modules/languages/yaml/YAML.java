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

package org.netbeans.modules.languages.yaml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.SyntaxContext;


/**
 *
 * @author Jan Jancura
 */
public class YAML {
    
    public static ASTNode parse (SyntaxContext context) {
        ASTNode root = (ASTNode) context.getASTPath ().getRoot ();
        if (root.getChildren ().isEmpty ())
            return root;
        return parse (root.getChildren (), 0, new int[] {0});
    }
    
    private static ASTNode parse (List<ASTItem> items, int indent, int[] index) {
        List<ASTItem> ch = new ArrayList<ASTItem> ();
        while (index[0] < items.size ()) {
            ASTItem item = items.get (index[0]);
            int ci = 0;
            if (!item.getChildren ().isEmpty ()) {
                ASTItem indentNode = item.getChildren ().get (0);
                if (!indentNode.getChildren ().isEmpty ()) {
                    ASTItem indentToken = indentNode.getChildren ().get (0);
                    if (indentToken instanceof ASTToken)
                        ci = ((ASTToken) indentToken).getLength ();
                }
            }
            if (ci > indent)
                ch.add (parse (items, ci, index));
            else
            if (ci < indent)
                break;
            else {
                ch.add (item);
                index[0]++;
            }
        }
        int offset = 0;
        if (!ch.isEmpty ())
            offset = ch.get (0).getOffset( );
        return ASTNode.create (
            "text/x-yaml",
            "Collection",
            ch,
            offset
        );
    }
}





