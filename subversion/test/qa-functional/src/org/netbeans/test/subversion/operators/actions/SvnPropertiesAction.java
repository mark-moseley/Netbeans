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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.test.subversion.operators.actions;

import org.netbeans.jellytools.actions.ActionNoBlock;

/**
 *
 * @author novakm
 */
public class SvnPropertiesAction extends ActionNoBlock {
    /** "Subversion" menu item. */
    public static final String SVN_ITEM = "Subversion";
            
    /** "Copy..." menu item. */
    public static final String SVN_PROPERTIES_ITEM = "Svn Properties";
    public SvnPropertiesAction() {
        super(SVN_ITEM + "|" + SVN_PROPERTIES_ITEM, SVN_ITEM + "|" + SVN_PROPERTIES_ITEM);
    }
}