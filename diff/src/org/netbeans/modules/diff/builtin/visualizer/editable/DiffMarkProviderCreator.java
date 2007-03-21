/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.diff.builtin.visualizer.editable;

import org.netbeans.modules.editor.errorstripe.privatespi.MarkProviderCreator;
import org.netbeans.modules.editor.errorstripe.privatespi.MarkProvider;

import javax.swing.text.JTextComponent;

/**
 * Errorstripe mark provider for Diff pane.
 * 
 * @author Maros Sandor
 */
public class DiffMarkProviderCreator implements MarkProviderCreator {
    
    static final String MARK_PROVIDER_KEY = "org.netbeans.modules.diff.builtin.visualizer.editable.MarkProvider";

    public MarkProvider createMarkProvider(JTextComponent component) {
        return (MarkProvider) component.getClientProperty(MARK_PROVIDER_KEY);
    }
}
