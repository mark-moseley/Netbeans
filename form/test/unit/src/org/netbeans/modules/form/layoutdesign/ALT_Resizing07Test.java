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

package org.netbeans.modules.form.layoutdesign;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.openide.filesystems.FileUtil;

// Resizing without snapping.
public class ALT_Resizing07Test extends LayoutTestCase {
        
    public ALT_Resizing07Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());	    
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    // Resize textfield slightly to the left, then to the right - almost to the
    // right border but still without snapping. Effective alignment should not change.
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 14, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jTextField1", new Rectangle(103, 11, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 14, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(103, 11, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
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
                new Rectangle(103, 11, 59, 20)
                };
            Point hotspot = new Point(102,19);
            int[] resizeEdges = new int[] {
                0,
                    -1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPadding.put("jLabel1-jTextField1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(89,22);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(90, 11, 72, 20)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPadding.put("jLabel1-jTextField1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(88,22);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(89, 11, 73, 20)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPadding.put("jLabel1-jTextField1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jTextField1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 14, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jTextField1", new Rectangle(89, 11, 73, 20));
        baselinePosition.put("jTextField1-73-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 14, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(89, 11, 73, 20));
        baselinePosition.put("jTextField1-73-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        baselinePosition.put("jTextField1-73-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        {
            String[] compIds = new String[] {
                "jTextField1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(89, 11, 73, 20)
                };
            Point hotspot = new Point(161,18);
            int[] resizeEdges = new int[] {
                1,
                    -1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPaddingInParent.put("Form-jTextField1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(380,24);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(89, 11, 292, 20)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPaddingInParent.put("Form-jTextField1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        // > MOVE
        {
            Point p = new Point(379,24);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(89, 11, 291, 20)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPadding.put("jLabel1-jTextField1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPaddingInParent.put("Form-jTextField1-0-1", new Integer(10)); // parentId-compId-dimension-compAlignment
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 14, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jTextField1", new Rectangle(89, 11, 291, 20));
        baselinePosition.put("jTextField1-291-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jLabel1", new Rectangle(10, 14, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jTextField1", new Rectangle(89, 11, 291, 20));
        baselinePosition.put("jTextField1-291-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }
    
}
