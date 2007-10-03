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

// Testing indent separated from aligning (component indented, whole group
// aligned).
public class ALT_Indent03Test extends LayoutTestCase {

    public ALT_Indent03Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());	    
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    // Move jLabel2 slightly to the right to indent under jLabel1. Other labels
    // (which are in group with jLabel2) should not move.
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 11, 68, 14));
        baselinePosition.put("jLabel1-68-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(68, 14));
        compBounds.put("jLabel2", new Rectangle(10, 31, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        compBounds.put("jLabel3", new Rectangle(10, 51, 34, 14));
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(34, 14));
        compBounds.put("jLabel4", new Rectangle(10, 71, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        compBounds.put("jScrollPane1", new Rectangle(48, 31, 37, 132));
        baselinePosition.put("jScrollPane1-37-132", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(37, 132));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 11, 68, 14));
        baselinePosition.put("jLabel1-68-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(10, 31, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compBounds.put("jLabel3", new Rectangle(10, 51, 34, 14));
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(10, 71, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compBounds.put("jScrollPane1", new Rectangle(48, 31, 37, 132));
        baselinePosition.put("jScrollPane1-37-132", new Integer(0));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START MOVING
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        {
            String[] compIds = new String[] {
                "jLabel2"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 31, 34, 14)
                };
            Point hotspot = new Point(28,37);
            ld.startMoving(compIds, bounds, hotspot);
        }
        // < START MOVING
        prefPaddingInParent.put("Form-jLabel2-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel1-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jScrollPane1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jLabel2-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jScrollPane1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(35,37);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(20, 31, 34, 14)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jLabel2-1-0", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jLabel1-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jScrollPane1-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jScrollPane1-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel3-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel4-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jLabel2-0-0", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel2-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPadding.put("jScrollPane1-jLabel2-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jScrollPane1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel1-jLabel2-0-0-2", new Integer(10)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(37,37);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(20, 31, 34, 14)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPadding.put("jLabel3-jScrollPane1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel4-jScrollPane1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jScrollPane1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jLabel2-jLabel3-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 11, 68, 14));
        baselinePosition.put("jLabel1-68-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(68, 14));
        compBounds.put("jLabel3", new Rectangle(10, 51, 34, 14));
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        compPrefSize.put("jLabel3", new Dimension(34, 14));
        compBounds.put("jLabel4", new Rectangle(10, 71, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compPrefSize.put("jLabel4", new Dimension(34, 14));
        compBounds.put("jScrollPane1", new Rectangle(58, 31, 37, 132));
        baselinePosition.put("jScrollPane1-37-132", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(37, 132));
        compBounds.put("jLabel2", new Rectangle(20, 31, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        prefPaddingInParent.put("Form-jLabel1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jScrollPane1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 11, 68, 14));
        baselinePosition.put("jLabel1-68-14", new Integer(11));
        compBounds.put("jLabel3", new Rectangle(10, 51, 34, 14));
        baselinePosition.put("jLabel3-34-14", new Integer(11));
        compBounds.put("jLabel4", new Rectangle(10, 71, 34, 14));
        baselinePosition.put("jLabel4-34-14", new Integer(11));
        compBounds.put("jScrollPane1", new Rectangle(58, 31, 37, 132));
        baselinePosition.put("jScrollPane1-37-132", new Integer(0));
        compBounds.put("jLabel2", new Rectangle(20, 31, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }
    
}
