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

/**
 * Vertical resizing of a component (jPanel2) in a sequence (jPanel1, jPanel2,
 * jButton1) to align with a component in another sequence (jButton2).
 * This test differs to ALT_SeqResizing03Test in that here the neighbor in the
 * original sequence (jButton1) is at arbitrary distance (not a preferred gap).
 */
public class ALT_SeqResizing04Test extends LayoutTestCase {

    public ALT_SeqResizing04Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    public void doChanges0() {
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(26, 30, 100, 100));
        compBounds.put("jPanel1", new Rectangle(26, 30, 100, 100));
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(100, 100));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 400, 349));
        compBounds.put("jPanel1", new Rectangle(26, 30, 100, 100));
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        compBounds.put("jButton2", new Rectangle(317, 54, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        compBounds.put("jPanel2", new Rectangle(180, 156, 100, 100));
        baselinePosition.put("jPanel2-100-100", new Integer(0));
        compBounds.put("jButton3", new Rectangle(317, 185, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compPrefSize.put("jButton3", new Dimension(73, 23));
        compBounds.put("jButton1", new Rectangle(43, 276, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        contInterior.put("jPanel2", new Rectangle(180, 156, 100, 100));
        compBounds.put("jPanel2", new Rectangle(180, 156, 100, 100));
        baselinePosition.put("jPanel2-100-100", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(0, 0));
        compPrefSize.put("jPanel2", new Dimension(100, 100));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        contInterior.put("jPanel1", new Rectangle(26, 30, 100, 100));
        contInterior.put("Form", new Rectangle(0, 0, 400, 349));
        compBounds.put("jPanel1", new Rectangle(26, 30, 100, 100));
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        compBounds.put("jButton2", new Rectangle(317, 54, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jPanel2", new Rectangle(180, 156, 100, 100));
        baselinePosition.put("jPanel2-100-100", new Integer(0));
        compBounds.put("jButton3", new Rectangle(317, 185, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(43, 276, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        contInterior.put("jPanel2", new Rectangle(180, 156, 100, 100));
        ld.updateCurrentState();
// < UPDATE CURRENT STATE
// > START RESIZING
        baselinePosition.put("jPanel2-100-100", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(0, 0));
        {
            String[] compIds = new String[]{"jPanel2"};
            Rectangle[] bounds = new Rectangle[]{new Rectangle(180, 156, 100, 100)};
            Point hotspot = new Point(230, 150);
            int[] resizeEdges = new int[]{-1, 0};
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
// < START RESIZING
        prefPaddingInParent.put("Form-jPanel2-1-0", new Integer(11));
        // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(235, 54);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(180, 54, 100, 202)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
        prefPaddingInParent.put("Form-jPanel2-1-0", new Integer(11));
        // parentId-compId-dimension-compAlignment
// > MOVE
        {
            Point p = new Point(235, 53);
            String containerId = "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[]{new Rectangle(180, 54, 100, 202)};
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
// < MOVE
// > END MOVING
        prefPadding.put("jPanel1-jPanel2-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel2-jButton2-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jPanel2-0-0", new Integer(10));
        // parentId-compId-dimension-compAlignment
        prefPadding.put("jPanel2-jButton3-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel1-jPanel2-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel2-jButton2-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jPanel2-jButton3-0-0-0", new Integer(6));
        // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        contInterior.put("jPanel2", new Rectangle(-32588, -32714, 100, 202));
        compBounds.put("jPanel2", new Rectangle(-32588, -32714, 100, 202));
        baselinePosition.put("jPanel2-100-202", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(0, 0));
        compPrefSize.put("jPanel2", new Dimension(100, 100));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        ld.endMoving(true);
// < END MOVING
        ld.externalSizeChangeHappened();
// > UPDATE CURRENT STATE
        contInterior.put("jPanel1", new Rectangle(26, 30, 100, 100));
        compBounds.put("jPanel1", new Rectangle(26, 30, 100, 100));
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        compMinSize.put("jPanel1", new Dimension(0, 0));
        compPrefSize.put("jPanel1", new Dimension(100, 100));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        hasExplicitPrefSize.put("jPanel1", new Boolean(false));
        contInterior.put("Form", new Rectangle(0, 0, 400, 349));
        compBounds.put("jPanel1", new Rectangle(26, 30, 100, 100));
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        compBounds.put("jButton2", new Rectangle(317, 54, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compPrefSize.put("jButton2", new Dimension(73, 23));
        compBounds.put("jButton3", new Rectangle(317, 185, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compPrefSize.put("jButton3", new Dimension(73, 23));
        compBounds.put("jButton1", new Rectangle(43, 276, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(73, 23));
        compBounds.put("jPanel2", new Rectangle(180, 54, 100, 202));
        baselinePosition.put("jPanel2-100-202", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(180, 54, 100, 202));
        compBounds.put("jPanel2", new Rectangle(180, 54, 100, 202));
        baselinePosition.put("jPanel2-100-202", new Integer(0));
        compMinSize.put("jPanel2", new Dimension(0, 0));
        compPrefSize.put("jPanel2", new Dimension(100, 202));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        hasExplicitPrefSize.put("jPanel2", new Boolean(false));
        contInterior.put("jPanel1", new Rectangle(26, 30, 100, 100));
        contInterior.put("Form", new Rectangle(0, 0, 400, 349));
        compBounds.put("jPanel1", new Rectangle(26, 30, 100, 100));
        baselinePosition.put("jPanel1-100-100", new Integer(0));
        compBounds.put("jButton2", new Rectangle(317, 54, 73, 23));
        baselinePosition.put("jButton2-73-23", new Integer(15));
        compBounds.put("jButton3", new Rectangle(317, 185, 73, 23));
        baselinePosition.put("jButton3-73-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(43, 276, 73, 23));
        baselinePosition.put("jButton1-73-23", new Integer(15));
        compBounds.put("jPanel2", new Rectangle(180, 54, 100, 202));
        baselinePosition.put("jPanel2-100-202", new Integer(0));
        contInterior.put("jPanel2", new Rectangle(180, 54, 100, 202));
        ld.updateCurrentState();
    }

}
