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

package org.netbeans.modules.form.layoutdesign;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.openide.filesystems.FileUtil;

public class ALT_Bug134368_1Test extends LayoutTestCase {

    public ALT_Bug134368_1Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    /**
     * Resize passwordField2 - drag right edge to the left, don't snap.
     */
    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 398, 134));
        compBounds.put("messageLabel", new Rectangle(10, 11, 388, 0));
        baselinePosition.put("messageLabel-388-0", new Integer(4));
        compPrefSize.put("messageLabel", new Dimension(0, 0));
        compBounds.put("jLabel3", new Rectangle(19, 40, 41, 14));
        baselinePosition.put("jLabel3-41-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(41, 14));
        compBounds.put("jLabel2", new Rectangle(10, 17, 50, 14));
        baselinePosition.put("jLabel2-50-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(50, 14));
        compBounds.put("passwordField2", new Rectangle(64, 40, 334, 17));
        baselinePosition.put("passwordField2-334-17", new Integer(11));
        compPrefSize.put("passwordField2", new Dimension(11, 17));
        compBounds.put("passwordField1", new Rectangle(64, 17, 334, 17));
        baselinePosition.put("passwordField1-334-17", new Integer(11));
        compPrefSize.put("passwordField1", new Dimension(11, 17));
        compBounds.put("okButton", new Rectangle(256, 100, 47, 23));
        baselinePosition.put("okButton-47-23", new Integer(15));
        compPrefSize.put("okButton", new Dimension(47, 23));
        compBounds.put("cancelButton", new Rectangle(321, 100, 65, 23));
        baselinePosition.put("cancelButton-65-23", new Integer(15));
        compPrefSize.put("cancelButton", new Dimension(65, 23));
        contInterior.put("Form", new Rectangle(0, 0, 398, 134));
        compBounds.put("messageLabel", new Rectangle(10, 11, 388, 0));
        baselinePosition.put("messageLabel-388-0", new Integer(4));
        compBounds.put("jLabel3", new Rectangle(19, 40, 41, 14));
        baselinePosition.put("jLabel3-41-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(10, 17, 50, 14));
        baselinePosition.put("jLabel2-50-14", new Integer(11));
        compBounds.put("passwordField2", new Rectangle(64, 40, 334, 17));
        baselinePosition.put("passwordField2-334-17", new Integer(11));
        compBounds.put("passwordField1", new Rectangle(64, 17, 334, 17));
        baselinePosition.put("passwordField1-334-17", new Integer(11));
        compBounds.put("okButton", new Rectangle(256, 100, 47, 23));
        baselinePosition.put("okButton-47-23", new Integer(15));
        compBounds.put("cancelButton", new Rectangle(321, 100, 65, 23));
        baselinePosition.put("cancelButton-65-23", new Integer(15));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
// > START RESIZING
        baselinePosition.put("passwordField2-334-17", new Integer(11));
        compPrefSize.put("passwordField2", new Dimension(11, 17));
        {
            String[] compIds = new String[]{"passwordField2"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(64, 40, 334, 17)};
            Point hotspot = new Point(402, 49);
            int[] resizeEdges = new int[]{1, -1};
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPaddingInParent.put("Form-passwordField2-0-1", new Integer(10));
        // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(291, 62);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(64, 40, 223, 17)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-passwordField2-0-1", new Integer(10));
        // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(291, 61);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(64, 40, 223, 17)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 398, 134));
        compBounds.put("messageLabel", new Rectangle(10, 11, 388, 0));
        baselinePosition.put("messageLabel-388-0", new Integer(4));
        compPrefSize.put("messageLabel", new Dimension(0, 0));
        compBounds.put("jLabel3", new Rectangle(19, 40, 41, 14));
        baselinePosition.put("jLabel3-41-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(41, 14));
        compBounds.put("jLabel2", new Rectangle(10, 17, 50, 14));
        baselinePosition.put("jLabel2-50-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(50, 14));
        compBounds.put("passwordField2", new Rectangle(64, 40, 223, 17));
        baselinePosition.put("passwordField2-223-17", new Integer(11));
        compPrefSize.put("passwordField2", new Dimension(11, 17));
        compBounds.put("passwordField1", new Rectangle(64, 17, 334, 17));
        baselinePosition.put("passwordField1-334-17", new Integer(11));
        compPrefSize.put("passwordField1", new Dimension(11, 17));
        compBounds.put("okButton", new Rectangle(256, 100, 47, 23));
        baselinePosition.put("okButton-47-23", new Integer(15));
        compPrefSize.put("okButton", new Dimension(47, 23));
        compBounds.put("cancelButton", new Rectangle(321, 100, 65, 23));
        baselinePosition.put("cancelButton-65-23", new Integer(15));
        compPrefSize.put("cancelButton", new Dimension(65, 23));
        contInterior.put("Form", new Rectangle(0, 0, 398, 134));
        compBounds.put("messageLabel", new Rectangle(10, 11, 388, 0));
        baselinePosition.put("messageLabel-388-0", new Integer(4));
        compBounds.put("jLabel3", new Rectangle(19, 40, 41, 14));
        baselinePosition.put("jLabel3-41-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(10, 17, 50, 14));
        baselinePosition.put("jLabel2-50-14", new Integer(11));
        compBounds.put("passwordField2", new Rectangle(64, 40, 223, 17));
        baselinePosition.put("passwordField2-223-17", new Integer(11));
        compBounds.put("passwordField1", new Rectangle(64, 17, 334, 17));
        baselinePosition.put("passwordField1-334-17", new Integer(11));
        compBounds.put("okButton", new Rectangle(256, 100, 47, 23));
        baselinePosition.put("okButton-47-23", new Integer(15));
        compBounds.put("cancelButton", new Rectangle(321, 100, 65, 23));
        baselinePosition.put("cancelButton-65-23", new Integer(15));
        ld.updateCurrentState();
    }

}
