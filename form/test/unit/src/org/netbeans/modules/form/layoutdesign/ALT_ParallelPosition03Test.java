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

// Test for parallel same size with "next to" gesture.
public class ALT_ParallelPosition03Test extends LayoutTestCase {

    public ALT_ParallelPosition03Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());	    
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    // Create the layout - toggle button, text area, button (right-aligned with
    // togle button).
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lc = new LayoutComponent("jToggleButton1", false);
        // > START ADDING
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        {
            LayoutComponent[] comps = new LayoutComponent[] { lc };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 105, 23)
                };
            String defaultContId = null;
            Point hotspot = new Point(48,11);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
        // < START ADDING
        prefPaddingInParent.put("Form-jToggleButton1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(61,27);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 11, 105, 23)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jToggleButton1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(60,27);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 11, 105, 23)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        prefPaddingInParent.put("Form-jToggleButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jToggleButton1-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 11, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 11, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lc = new LayoutComponent("jScrollPane1", false);
        // > START ADDING
        baselinePosition.put("jScrollPane1-104-64", new Integer(0));
        {
            LayoutComponent[] comps = new LayoutComponent[] { lc };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 104, 64)
                };
            String defaultContId = null;
            Point hotspot = new Point(48,32);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
        // < START ADDING
        prefPaddingInParent.put("Form-jScrollPane1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jToggleButton1-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jToggleButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(172,47);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(121, 11, 104, 64)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jScrollPane1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jToggleButton1-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jToggleButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(172,46);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(121, 11, 104, 64)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jScrollPane1", new Dimension(104, 64));
        compPrefSize.put("jScrollPane1", new Dimension(104, 64));
        prefPaddingInParent.put("Form-jScrollPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-1-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 11, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compBounds.put("jScrollPane1", new Rectangle(121, 11, 104, 64));
        baselinePosition.put("jScrollPane1-104-64", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(104, 64));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 11, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(121, 11, 104, 64));
        baselinePosition.put("jScrollPane1-104-64", new Integer(0));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        lc = new LayoutComponent("jButton1", false);
        // > START ADDING
        baselinePosition.put("jButton1-75-23", new Integer(15));
        {
            LayoutComponent[] comps = new LayoutComponent[] { lc };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(0, 0, 75, 23)
                };
            String defaultContId = null;
            Point hotspot = new Point(33,11);
            ld.startAdding(comps, bounds, hotspot, defaultContId);
        }
        // < START ADDING
        prefPaddingInParent.put("Form-jButton1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jToggleButton1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jToggleButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(70,48);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(40, 40, 75, 23)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jButton1-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jToggleButton1-jButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jToggleButton1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jButton1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jToggleButton1-jButton1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(71,48);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(40, 40, 75, 23)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jButton1", new Dimension(75, 23));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        prefPaddingInParent.put("Form-jToggleButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 11, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compBounds.put("jScrollPane1", new Rectangle(121, 11, 104, 64));
        baselinePosition.put("jScrollPane1-104-64", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(104, 64));
        compBounds.put("jButton1", new Rectangle(40, 40, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 11, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(121, 11, 104, 64));
        baselinePosition.put("jScrollPane1-104-64", new Integer(0));
        compBounds.put("jButton1", new Rectangle(40, 40, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Resize the button to the left to have same size as the toggle button.
    // Fixed parallel group with both buttons resizing should be created.
    public void doChanges1() {
        // > START RESIZING
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        {
            String[] compIds = new String[] {
                "jButton1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(40, 40, 75, 23)
                };
            Point hotspot = new Point(36,49);
            int[] resizeEdges = new int[] {
                0,
                    -1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jToggleButton1-jButton1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(10,49);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 40, 105, 23)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jButton1-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jToggleButton1-jButton1-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(9,49);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 40, 105, 23)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        compPrefSize.put("jButton1", new Dimension(75, 23));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 11, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compPrefSize.put("jToggleButton1", new Dimension(105, 23));
        compBounds.put("jScrollPane1", new Rectangle(121, 11, 104, 64));
        baselinePosition.put("jScrollPane1-104-64", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(104, 64));
        compBounds.put("jButton1", new Rectangle(10, 40, 105, 23));
        baselinePosition.put("jButton1-105-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 11, 105, 23));
        baselinePosition.put("jToggleButton1-105-23", new Integer(15));
        compBounds.put("jScrollPane1", new Rectangle(121, 11, 104, 64));
        baselinePosition.put("jScrollPane1-104-64", new Integer(0));
        compBounds.put("jButton1", new Rectangle(10, 40, 105, 23));
        baselinePosition.put("jButton1-105-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }
    
}
