/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.diff;

import java.awt.Component;
import java.io.IOException;
import java.io.Reader;
import javax.swing.JPanel;
import org.netbeans.api.diff.Diff;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.modules.diff.builtin.DefaultDiff;

/**
 *
 * @author Martin Entlicher
 */
public class DefaultDiffViewTest extends DiffViewAbstract {
    
    /** Creates a new instance of DefaultDiffViewTest */
    public DefaultDiffViewTest(String name) {
        super(name);
    }
    
    protected DiffView createDiffView(StreamSource ss1, StreamSource ss2) throws IOException {
        DefaultDiff dd = new DefaultDiff();
        return dd.createDiff(ss1, ss2);
    }
    
}
