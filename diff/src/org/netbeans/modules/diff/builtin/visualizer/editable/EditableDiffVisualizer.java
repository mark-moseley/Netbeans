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
package org.netbeans.modules.diff.builtin.visualizer.editable;

import org.netbeans.spi.diff.DiffVisualizer;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.diff.DiffView;
import org.netbeans.api.diff.StreamSource;
import org.openide.util.NbBundle;

import java.awt.Component;
import java.io.Reader;
import java.io.IOException;

/**
 * Registration of the editable visualizer. 
 * 
 * @author Maros Sandor
 */
public class EditableDiffVisualizer extends DiffVisualizer {

    /**
     * Get the display name of this diff visualizer, CALLED VIA REFLECTION.
     */
    public String getDisplayName() {
        return NbBundle.getMessage(EditableDiffVisualizer.class, "CTL_EditableDiffVisualizer_Name"); // NOI18N
    }
    
    /**
     * Get a short description of this diff visualizer, CALLED VIA REFLECTION.
     */
    public String getShortDescription() {
        return NbBundle.getMessage(EditableDiffVisualizer.class, "CTL_EditableDiffVisualizer_Desc"); // NOI18N
    }
    
    public Component createView(Difference[] diffs, String name1, String title1, Reader r1, String name2, String title2, Reader r2, String MIMEType) throws IOException {
        DiffView view = createDiff(diffs, StreamSource.createSource(name1, title1, MIMEType, r1), StreamSource.createSource(name2, title2, MIMEType, r2));
        return view.getComponent();
    }

    public DiffView createDiff(Difference[] diffs, StreamSource s1, StreamSource s2) throws IOException {
        return new EditableDiffView(s1, s2);
    }
} 
