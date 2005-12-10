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

// Testing situation when resizing of ceraetd parallel group should be preserved.
public class ALT_ParallelPosition19Test extends LayoutTestCase {
        
    public ALT_ParallelPosition19Test(String name) {
        super(name);
        try {
	    className = this.getClass().getName();
	    className = className.substring(className.lastIndexOf('.') + 1, className.length());	    
            startingFormFile = FileUtil.toFileObject(new File(url.getFile() + goldenFilesPath + className + "-StartingForm.form").getCanonicalFile());
        } catch (IOException ioe) {
            fail(ioe.toString());
        }
    }

    // One by one, resize the textfields to the right to snap next to the list.
    public void doChanges0() {
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(10, 11, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jTextField2", new Rectangle(10, 37, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compBounds.put("jTextField3", new Rectangle(10, 63, 59, 20));
        baselinePosition.put("jTextField3-59-20", new Integer(14));
        compPrefSize.put("jTextField3", new Dimension(59, 20));
        compBounds.put("jScrollPane1", new Rectangle(353, 11, 37, 132));
        baselinePosition.put("jScrollPane1-37-132", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(37, 132));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField1", new Rectangle(10, 11, 59, 20));
        baselinePosition.put("jTextField1-59-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(10, 37, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compBounds.put("jTextField3", new Rectangle(10, 63, 59, 20));
        baselinePosition.put("jTextField3-59-20", new Integer(14));
        compBounds.put("jScrollPane1", new Rectangle(353, 11, 37, 132));
        baselinePosition.put("jScrollPane1-37-132", new Integer(0));
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
                new Rectangle(10, 11, 59, 20)
                };
            Point hotspot = new Point(69,19);
            int[] resizeEdges = new int[] {
                1,
                    -1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPadding.put("jTextField1-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(340,36);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 11, 337, 20)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPadding.put("jTextField1-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(341,36);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 11, 337, 20)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        // > END MOVING
        prefPadding.put("jTextField2-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField3-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        prefPadding.put("jTextField1-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        ld.endMoving(true);
        // < END MOVING
        ld.externalSizeChangeHappened();
        // > UPDATE CURRENT STATE
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField2", new Rectangle(10, 37, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compBounds.put("jTextField3", new Rectangle(10, 63, 59, 20));
        baselinePosition.put("jTextField3-59-20", new Integer(14));
        compPrefSize.put("jTextField3", new Dimension(59, 20));
        compBounds.put("jScrollPane1", new Rectangle(353, 11, 37, 132));
        baselinePosition.put("jScrollPane1-37-132", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(37, 132));
        compBounds.put("jTextField1", new Rectangle(10, 11, 337, 20));
        baselinePosition.put("jTextField1-337-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField2", new Rectangle(10, 37, 59, 20));
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compBounds.put("jTextField3", new Rectangle(10, 63, 59, 20));
        baselinePosition.put("jTextField3-59-20", new Integer(14));
        compBounds.put("jScrollPane1", new Rectangle(353, 11, 37, 132));
        baselinePosition.put("jScrollPane1-37-132", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(10, 11, 337, 20));
        baselinePosition.put("jTextField1-337-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        baselinePosition.put("jTextField2-59-20", new Integer(14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        {
            String[] compIds = new String[] {
                "jTextField2"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 37, 59, 20)
                };
            Point hotspot = new Point(70,48);
            int[] resizeEdges = new int[] {
                1,
                    -1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPadding.put("jTextField2-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(343,53);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 37, 337, 20)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPadding.put("jTextField2-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(344,53);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 37, 337, 20)
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
        compBounds.put("jTextField3", new Rectangle(10, 63, 59, 20));
        baselinePosition.put("jTextField3-59-20", new Integer(14));
        compPrefSize.put("jTextField3", new Dimension(59, 20));
        compBounds.put("jScrollPane1", new Rectangle(353, 11, 37, 132));
        baselinePosition.put("jScrollPane1-37-132", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(37, 132));
        compBounds.put("jTextField1", new Rectangle(10, 11, 337, 20));
        baselinePosition.put("jTextField1-337-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jTextField2", new Rectangle(10, 37, 337, 20));
        baselinePosition.put("jTextField2-337-20", new Integer(14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jTextField3", new Rectangle(10, 63, 59, 20));
        baselinePosition.put("jTextField3-59-20", new Integer(14));
        compBounds.put("jScrollPane1", new Rectangle(353, 11, 37, 132));
        baselinePosition.put("jScrollPane1-37-132", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(10, 11, 337, 20));
        baselinePosition.put("jTextField1-337-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(10, 37, 337, 20));
        baselinePosition.put("jTextField2-337-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
        // > START RESIZING
        baselinePosition.put("jTextField3-59-20", new Integer(14));
        compPrefSize.put("jTextField3", new Dimension(59, 20));
        {
            String[] compIds = new String[] {
                "jTextField3"
                };
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 63, 59, 20)
                };
            Point hotspot = new Point(69,74);
            int[] resizeEdges = new int[] {
                1,
                    -1
                };
            boolean inLayout = true;
            ld.startResizing(compIds, bounds, hotspot, resizeEdges, inLayout);
        }
        // < START RESIZING
        prefPadding.put("jTextField3-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(341,82);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 63, 337, 20)
                };
            ld.move(p, containerId, autoPositioning, lockDimension, bounds);
        }
        // < MOVE
        prefPadding.put("jTextField3-jScrollPane1-0-0-0", new Integer(6)); // comp1Id-comp2Id-dimension-comp2Alignment-paddingType
        // > MOVE
        {
            Point p = new Point(342,82);
            String containerId= "Form";
            boolean autoPositioning = true;
            boolean lockDimension = false;
            Rectangle[] bounds = new Rectangle[] {
                new Rectangle(10, 63, 337, 20)
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
        compBounds.put("jScrollPane1", new Rectangle(353, 11, 37, 132));
        baselinePosition.put("jScrollPane1-37-132", new Integer(0));
        compPrefSize.put("jScrollPane1", new Dimension(37, 132));
        compBounds.put("jTextField1", new Rectangle(10, 11, 337, 20));
        baselinePosition.put("jTextField1-337-20", new Integer(14));
        compPrefSize.put("jTextField1", new Dimension(59, 20));
        compBounds.put("jTextField2", new Rectangle(10, 37, 337, 20));
        baselinePosition.put("jTextField2-337-20", new Integer(14));
        compPrefSize.put("jTextField2", new Dimension(59, 20));
        compBounds.put("jTextField3", new Rectangle(10, 63, 337, 20));
        baselinePosition.put("jTextField3-337-20", new Integer(14));
        compPrefSize.put("jTextField3", new Dimension(59, 20));
        contInterior.put("Form", new Rectangle(0, 0, 400, 300));
        compBounds.put("jScrollPane1", new Rectangle(353, 11, 37, 132));
        baselinePosition.put("jScrollPane1-37-132", new Integer(0));
        compBounds.put("jTextField1", new Rectangle(10, 11, 337, 20));
        baselinePosition.put("jTextField1-337-20", new Integer(14));
        compBounds.put("jTextField2", new Rectangle(10, 37, 337, 20));
        baselinePosition.put("jTextField2-337-20", new Integer(14));
        compBounds.put("jTextField3", new Rectangle(10, 63, 337, 20));
        baselinePosition.put("jTextField3-337-20", new Integer(14));
        ld.updateCurrentState();
        // < UPDATE CURRENT STATE
    }
    
}
