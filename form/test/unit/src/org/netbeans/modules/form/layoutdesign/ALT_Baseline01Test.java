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
 */

package org.netbeans.modules.form.layoutdesign;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.openide.filesystems.FileUtil;

// Tests vertical resizing of components aligned at baseline.
public class ALT_Baseline01Test extends LayoutTestCase {

    public ALT_Baseline01Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());	    
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    // Resize jTextField1 downwards.
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 14, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jTextField1", new Rectangle(48, 11, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jLabel2", new Rectangle(10, 40, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        compBounds.put("jTextField2", new Rectangle(48, 37, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 14, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(48, 11, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compBounds.put("jLabel2", new Rectangle(10, 40, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compBounds.put("jTextField2", new Rectangle(48, 37, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        {
            String[] compIds = new String[] {
                "jTextField1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(48, 11, 59, 20)
                };
            Point hotspot = new Point(77,29);
            int[] resizeEdges = new int[] {
                -1,
                    1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPadding.put("jTextField1-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jTextField2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(78,63);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(48, 11, 59, 54)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPadding.put("jTextField1-jLabel2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jTextField2-1-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(78,64);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(48, 11, 59, 55)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 31, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jLabel2", new Rectangle(10, 75, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        compBounds.put("jTextField2", new Rectangle(48, 72, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compBounds.put("jTextField1", new Rectangle(48, 11, 59, 55));
        baselinePosition.put("jTextField1-59-55", new Integer(31));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        prefPaddingInParent.put("Form-jLabel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jTextField2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 31, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jLabel2", new Rectangle(10, 75, 34, 14));
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compBounds.put("jTextField2", new Rectangle(48, 72, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compBounds.put("jTextField1", new Rectangle(48, 11, 59, 55));
        baselinePosition.put("jTextField1-59-55", new Integer(31));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }

    // Resize jLabel2 downwards.
    public void doChanges1() {
        // > START RESIZING
        baselinePosition.put("jLabel2-34-14", new Integer(11));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        {
            String[] compIds = new String[] {
                "jLabel2"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 75, 34, 14)
                };
            Point hotspot = new Point(32,91);
            int[] resizeEdges = new int[] {
                -1,
                    1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jLabel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(30,130);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 75, 34, 53)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jLabel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(30,131);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 75, 34, 54)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 31, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jTextField2", new Rectangle(48, 89, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compBounds.put("jTextField1", new Rectangle(48, 11, 59, 55));
        baselinePosition.put("jTextField1-59-55", new Integer(31));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jLabel2", new Rectangle(10, 72, 34, 54));
        baselinePosition.put("jLabel2-34-54", new Integer(31));
        compPrefSize.put("jLabel2", new Dimension(34, 14));
        prefPaddingInParent.put("Form-jTextField2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        prefPaddingInParent.put("Form-jLabel2-1-1", new Integer(11)); // parentId-compId-dimension-compAlignment
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 31, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jTextField2", new Rectangle(48, 89, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compBounds.put("jTextField1", new Rectangle(48, 11, 59, 55));
        baselinePosition.put("jTextField1-59-55", new Integer(31));
        compBounds.put("jLabel2", new Rectangle(10, 72, 34, 54));
        baselinePosition.put("jLabel2-34-54", new Integer(31));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }
    
}
