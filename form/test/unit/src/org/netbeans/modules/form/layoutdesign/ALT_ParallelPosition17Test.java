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

// Test for subordinate resizing interval (creating fixed parallel group).
// A fixed component surrounded with fixed gaps (and components) is resized to
// right align with a parallel component which forces it to become resizing,
// preserving fixed gaps around it.
public class ALT_ParallelPosition17Test extends LayoutTestCase {
        
    public ALT_ParallelPosition17Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());	    
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    // Resize jButton1 to right align with the toggle button.
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 11, 179, 23));
        baselinePosition.put("jToggleButton1-179-23", new Integer(15));
        compPrefSize.put("jToggleButton1", new Dimension(179, 23));
        compBounds.put("jLabel1", new Rectangle(10, 44, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jButton1", new Rectangle(87, 40, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        compBounds.put("jButton2", new Rectangle(215, 40, 75, 23));
        baselinePosition.put("jButton2-75-23", new Integer(15));
        compPrefSize.put("jButton2", new Dimension(75, 23));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 11, 179, 23));
        baselinePosition.put("jToggleButton1-179-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(10, 44, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jButton1", new Rectangle(87, 40, 75, 23));
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compBounds.put("jButton2", new Rectangle(215, 40, 75, 23));
        baselinePosition.put("jButton2-75-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        baselinePosition.put("jButton1-75-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        {
            String[] compIds = new String[] {
                "jButton1"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(87, 40, 75, 23)
                };
            Point hotspot = new Point(164,52);
            int[] resizeEdges = new int[] {
                1,
                    -1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPadding.put("jButton1-jButton2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(183,51);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(87, 40, 94, 23)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPadding.put("jButton1-jButton2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(184,51);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(87, 40, 102, 23)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPadding.put("jLabel1-jButton1-0-0-0", new Integer(4)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jButton1-jButton2-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        compPrefSize.put("jButton1", new Dimension(75, 23));
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 11, 179, 23));
        baselinePosition.put("jToggleButton1-179-23", new Integer(15));
        compPrefSize.put("jToggleButton1", new Dimension(179, 23));
        compBounds.put("jLabel1", new Rectangle(10, 44, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compPrefSize.put("jLabel1", new Dimension(34, 14));
        compBounds.put("jButton2", new Rectangle(215, 40, 75, 23));
        baselinePosition.put("jButton2-75-23", new Integer(15));
        compPrefSize.put("jButton2", new Dimension(75, 23));
        compBounds.put("jButton1", new Rectangle(87, 40, 102, 23));
        baselinePosition.put("jButton1-102-23", new Integer(15));
        compPrefSize.put("jButton1", new Dimension(75, 23));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jToggleButton1", new Rectangle(10, 11, 179, 23));
        baselinePosition.put("jToggleButton1-179-23", new Integer(15));
        compBounds.put("jLabel1", new Rectangle(10, 44, 34, 14));
        baselinePosition.put("jLabel1-34-14", new Integer(11));
        compBounds.put("jButton2", new Rectangle(215, 40, 75, 23));
        baselinePosition.put("jButton2-75-23", new Integer(15));
        compBounds.put("jButton1", new Rectangle(87, 40, 102, 23));
        baselinePosition.put("jButton1-102-23", new Integer(15));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }
    
}
