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
 *
 */

package org.netbeans.modules.vmd.io.javame;

import org.netbeans.modules.mobility.snippets.SnippetsPaletteSupport;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataEditorViewLookupFactory;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.model.Debug;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import org.openide.util.Lookup;

/**
 * @author David Kaspar
 */
public final class MESourceLookupFactory implements DataEditorViewLookupFactory {
    
    public Collection<? extends Object> getLookupObjects(DataObjectContext context, DataEditorView view) {
        try {
            if (view instanceof MESourceEditorView  &&  view.getKind() == DataEditorView.Kind.CODE)
                return Collections.singleton(SnippetsPaletteSupport.getPaletteController());
        } catch (IOException e) {
            Debug.warning(e);
        }
        
        return null;
    }
    
    public Collection<? extends Lookup> getLookups(DataObjectContext context, DataEditorView view) {
        if (view.getKind().equals(DataEditorView.Kind.CODE))
            return Collections.singleton(view.getContext().getDataObject().getNodeDelegate().getLookup());
        return null;
    }
    
}
