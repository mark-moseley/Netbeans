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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.manager.impl;

import java.awt.datatransfer.Transferable;
import org.netbeans.modules.websvc.manager.spi.WebServiceTransferManager;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author Ayub Khan
 */
public class JaxWsTransferManager implements WebServiceTransferManager {
    
    JaxWsEditorDrop editorDrop;
    
    public JaxWsTransferManager() {
        editorDrop = new JaxWsEditorDrop(this);
    }

    public Transferable addDataFlavors(Transferable transferable) {
        ExTransferable t = ExTransferable.create( transferable );
        editorDrop.setTransferable(transferable);
        ActiveEditorDropTransferable s = new ActiveEditorDropTransferable(editorDrop);
        t.put(s);

        return t;
    }

    private static class ActiveEditorDropTransferable extends ExTransferable.Single {
        
        private JaxWsEditorDrop drop;

        ActiveEditorDropTransferable(JaxWsEditorDrop drop) {
            super(JaxWsEditorDrop.FLAVOR);
            
            this.drop = drop;
        }
               
        public Object getData () {
            return drop;
        }
        
    }
}
