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
 * E2EDataNode.java
 *
 * Created on June 27, 2005, 2:54 PM
 *
 */
package org.netbeans.modules.mobility.end2end;

import java.awt.Image;
import javax.swing.Action;
import org.netbeans.modules.mobility.end2end.ui.editor.GenerateAction;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.FileSystemAction;
import org.openide.actions.OpenAction;
import org.openide.actions.PasteAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.SaveAction;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Michal Skvor
 */
public class E2EDataNode extends DataNode {
    
    /** Creates a new instance of E2EDataNode */
    public E2EDataNode( E2EDataObject obj ) {
        super( obj, Children.LEAF );
    }
    
    public Image getIcon(@SuppressWarnings("unused")
	final int type) {
        return Utilities.loadImage(
                "org/netbeans/modules/mobility/end2end/resources/e2eclienticon.png" ); // NOI18N
    }
    
    public Action[] getActions(@SuppressWarnings("unused")
	final boolean context) {
        final Action[] result = new Action[] {
            SystemAction.get( OpenAction.class ),
            SystemAction.get( SaveAction.class ),
            null,
            SystemAction.get( GenerateAction.class ),
            null,
            SystemAction.get( FileSystemAction.class ),
            null,
            SystemAction.get( CutAction.class ),
            SystemAction.get( CopyAction.class ),
            SystemAction.get( PasteAction.class ),
            null,
            SystemAction.get( DeleteAction.class ),
            null,
            SystemAction.get( PropertiesAction.class )
        };
        return result;
    }
    
    public Action getPreferredAction() {
        return SystemAction.get( OpenAction.class );
    }
    
    public boolean canCopy(){
        return false;
    }
    
    public boolean canCut(){
        return false;
    }
}
