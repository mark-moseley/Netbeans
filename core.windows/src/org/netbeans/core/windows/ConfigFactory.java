/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows;


import org.netbeans.core.windows.persistence.GroupConfig;
import org.netbeans.core.windows.persistence.ModeConfig;
import org.netbeans.core.windows.persistence.TCRefConfig;
import org.netbeans.core.windows.persistence.WindowManagerConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Factory which can produce winsys configuration (<code>WindowManagerConfig</code>).
 * Used as a fallback configuration in case of persistence problems.
 *
 * @author  Peter Zavadsky
 */
abstract class ConfigFactory {

    // For shorten names.
    private static final int VERTICAL   = Constants.VERTICAL;
    private static final int HORIZONTAL = Constants.HORIZONTAL;
    
    /** Creates a new instance of Defaults */
    private ConfigFactory() {
    }
    
    
    public static WindowManagerConfig createDefaultConfig() {
        WindowManagerConfig wmc = new WindowManagerConfig();
        wmc.xJoined = 100;
        wmc.yJoined = 100;
        wmc.widthJoined = 800;
        wmc.heightJoined = 600;
        wmc.relativeXJoined = -1;
        wmc.relativeYJoined = -1;
        wmc.relativeWidthJoined = -1;
        wmc.relativeHeightJoined = -1;
        wmc.centeredHorizontallyJoined = false;
        wmc.centeredVerticallyJoined = false;
        wmc.maximizeIfWidthBelowJoined = -1;
        wmc.maximizeIfHeightBelowJoined = -1;
        
        wmc.editorAreaState = Constants.EDITOR_AREA_JOINED;
        wmc.editorAreaConstraints = createDefaultEditorAreaConstraints();
        wmc.editorAreaBounds = null;
        wmc.editorAreaRelativeBounds = null;
        
        wmc.screenSize = new Dimension(1024, 750);
        wmc.activeModeName = "editor"; // NOI18N
        wmc.maximizedModeName = null;
        wmc.toolbarConfiguration = "Standard"; // NOI18N
        wmc.modes = createDefaultModeConfigs();
        wmc.groups = createDefaultGroupConfigs();
        return wmc;
    }
    
    /** @return list of <code>Item</code>S as constraints */
    private static SplitConstraint[] createDefaultEditorAreaConstraints() {
        return new SplitConstraint[] {
            new SplitConstraint(HORIZONTAL, 1, 0.75D),
            new SplitConstraint(VERTICAL,   0, 0.75D),
            new SplitConstraint(HORIZONTAL, 0, 0.75D)
        };
    }
    
    /** @return list of <code>ModeConfig</code>S. */
    private static ModeConfig[] createDefaultModeConfigs() {
        List l = new ArrayList();
        l.add(createDefaultExplorerModeConfig());
        l.add(createDefaultPropertiesModeConfig());
        l.add(createDefaultEditorModeConfig());
        l.add(createDefaultOutputModeConfig());
        l.add(createDefaultExecutionConfig());
        l.add(createDefaultFormModeConfig());
        return (ModeConfig[])l.toArray(new ModeConfig[0]);
    }
    
    private static ModeConfig createDefaultExplorerModeConfig() {
        ModeConfig mc = new ModeConfig();
        mc.name = "explorer"; // NOI18N
        mc.bounds = null;
        mc.relativeBounds = null;
        mc.frameState = -1;
        mc.state = Constants.MODE_STATE_JOINED;
        mc.kind = Constants.MODE_KIND_VIEW;
        mc.constraints = createDefaultExplorerConstraints();
        mc.selectedTopComponentID = "filesystems"; // NOI18N
        mc.permanent = true;
        mc.tcRefConfigs = createDefaultExplorerTCRefConfigs();
        return mc;
    }

    /** @return list of <code>Item</code>S */
    private static SplitConstraint[] createDefaultExplorerConstraints() {
        return new SplitConstraint[] {
            new SplitConstraint(HORIZONTAL, 0, 0.30D),
            new SplitConstraint(VERTICAL,   0, 0.70D)
        };
    }
    
    private static TCRefConfig[] createDefaultExplorerTCRefConfigs() {
        List tcRefConfigs = new ArrayList();
        tcRefConfigs.add(createDefaultFilesystemsTCRefConfig());
        tcRefConfigs.add(createDefaultRuntimeTCRefConfig());
        tcRefConfigs.add(createDefaultProjectsTCRefConfig());
        return (TCRefConfig[])tcRefConfigs.toArray(new TCRefConfig[0]);
    }

    private static TCRefConfig createDefaultFilesystemsTCRefConfig() {
        TCRefConfig tcrc = new TCRefConfig();
        tcrc.tc_id = "filesystems"; // NOI18N
        tcrc.opened = true;
        return tcrc;
    }
    
    private static TCRefConfig createDefaultRuntimeTCRefConfig() {
        TCRefConfig tcrc = new TCRefConfig();
        tcrc.tc_id = "runtime"; // NOI18N
        tcrc.opened = true;
        return tcrc;
    }
    
    private static TCRefConfig createDefaultProjectsTCRefConfig() {
        TCRefConfig tcrc = new TCRefConfig();
        tcrc.tc_id = "projects"; // NOI18N
        tcrc.opened = true;
        return tcrc;
    }
    
    private static ModeConfig createDefaultPropertiesModeConfig() {
        ModeConfig mc = new ModeConfig();
        mc.name = "properties"; // NOI18N
        mc.bounds = null;
        mc.relativeBounds = null;
        mc.frameState = -1;
        mc.state = Constants.MODE_STATE_JOINED;
        mc.kind = Constants.MODE_KIND_VIEW;
        mc.constraints = createDefaultPropertiesConstraints();
        mc.selectedTopComponentID = "properties"; // NOI18N
        mc.permanent = true;
        mc.tcRefConfigs = createDefaultPropertiesTCRefConfigs();
        return mc;
    }
    
    /** @return list of <code>Item</code>S */
    private static SplitConstraint[] createDefaultPropertiesConstraints() {
        return new SplitConstraint[] {
            new SplitConstraint(HORIZONTAL, 0, 0.30D),
            new SplitConstraint(VERTICAL,   1, 0.30D)
        };
    }
    
    private static TCRefConfig[] createDefaultPropertiesTCRefConfigs() {
        List tcRefConfigs = new ArrayList();
        tcRefConfigs.add(createDefaultPropertiesTCRefConfig());
        return (TCRefConfig[])tcRefConfigs.toArray(new TCRefConfig[0]);
    }

    private static TCRefConfig createDefaultPropertiesTCRefConfig() {
        TCRefConfig tcrc = new TCRefConfig();
        tcrc.tc_id = "properties"; // NOI18N
        tcrc.opened = true;
        return tcrc;
    }
    
    private static ModeConfig createDefaultEditorModeConfig() {
        ModeConfig mc = new ModeConfig();
        mc.name = "editor";
        mc.bounds = null;
        mc.relativeBounds = null;
        mc.frameState = -1;
        mc.state = Constants.MODE_STATE_JOINED;
        mc.kind = Constants.MODE_KIND_EDITOR;
        mc.constraints = createDefaultEditorConstraints();
        mc.selectedTopComponentID = null;
        mc.permanent = true;
        mc.tcRefConfigs = createDefaultEditorTCRefConfigs();
        return mc;
    }

    /** @return list of <code>Item</code>S */
    private static SplitConstraint[] createDefaultEditorConstraints() {
        return new SplitConstraint[] {};
    }
    
    private static TCRefConfig[] createDefaultEditorTCRefConfigs() {
        List tcRefConfigs = new ArrayList();
        tcRefConfigs.add(createDefaultWelcomeTCRefConfig());
        return (TCRefConfig[])tcRefConfigs.toArray(new TCRefConfig[0]);
    }

    private static TCRefConfig createDefaultWelcomeTCRefConfig() {
        TCRefConfig tcrc = new TCRefConfig();
        tcrc.tc_id = "Welcome"; // NOI18N
        tcrc.opened = true;
        return tcrc;
    }

    private static ModeConfig createDefaultOutputModeConfig() {
        ModeConfig mc = new ModeConfig();
        mc.name = "output";
        mc.bounds = null;
        mc.relativeBounds = null;
        mc.frameState = -1;
        mc.state = Constants.MODE_STATE_JOINED;
        mc.kind = Constants.MODE_KIND_VIEW;
        mc.constraints = createDefaultOutputConstraints();
        mc.selectedTopComponentID = null;
        mc.permanent = true;
        mc.tcRefConfigs = createDefaultOutputTCRefConfigs();
        return mc;
    }
    
    /** @return list of <code>Item</code>S */
    private static SplitConstraint[] createDefaultOutputConstraints() {
        return new SplitConstraint[] {
            new SplitConstraint(HORIZONTAL, 1, 0.70D),
            new SplitConstraint(VERTICAL,   1, 0.20D),
            new SplitConstraint(HORIZONTAL, 0, 0.80D)
        };
    }
    
    private static TCRefConfig[] createDefaultOutputTCRefConfigs() {
        List tcRefConfigs = new ArrayList();
        tcRefConfigs.add(createDefaultOutputTCRefConfig());
        return (TCRefConfig[])tcRefConfigs.toArray(new TCRefConfig[0]);
    }

    private static TCRefConfig createDefaultOutputTCRefConfig() {
        TCRefConfig tcrc = new TCRefConfig();
        tcrc.tc_id = "output"; // NOI18N
        tcrc.opened = true;
        return tcrc;
    }
   
    private static ModeConfig createDefaultExecutionConfig() {
        ModeConfig mc = new ModeConfig();
        mc.name = "execution";
        mc.bounds = null;
        mc.relativeBounds = null;
        mc.frameState = -1;
        mc.state = Constants.MODE_STATE_JOINED;
        mc.kind = Constants.MODE_KIND_VIEW;
        mc.constraints = createDefaultExecutionConstraints();
        mc.selectedTopComponentID = null;
        mc.permanent = true;
        mc.tcRefConfigs = createDefaultExecutionTCRefConfigs();
        return mc;
    }
    
    /** @return list of <code>Item</code>S */
    private static SplitConstraint[] createDefaultExecutionConstraints() {
        return new SplitConstraint[] {
            new SplitConstraint(HORIZONTAL, 1, 0.30D),
            new SplitConstraint(VERTICAL,   1, 0.20D),
            new SplitConstraint(HORIZONTAL, 1, 0.20D)
        };
    }
    
    private static TCRefConfig[] createDefaultExecutionTCRefConfigs() {
        List tcRefConfigs = new ArrayList();
        tcRefConfigs.add(createDefaultExecutionTCRefConfig());
        return (TCRefConfig[])tcRefConfigs.toArray(new TCRefConfig[0]);
    }

    private static TCRefConfig createDefaultExecutionTCRefConfig() {
        TCRefConfig tcrc = new TCRefConfig();
        tcrc.tc_id = "execution"; // NOI18N
        tcrc.opened = true;
        return tcrc;
    }

    
    private static ModeConfig createDefaultFormModeConfig() {
        ModeConfig mc = new ModeConfig();
        mc.name = "Form";
        mc.bounds = null;
        mc.relativeBounds = null;
        mc.frameState = -1;
        mc.state = Constants.MODE_STATE_JOINED;
        mc.kind = Constants.MODE_KIND_VIEW;
        mc.constraints = createDefaultFormConstraints();
        mc.selectedTopComponentID = "ComponentInspector"; // NOI18N
        mc.permanent = true;
        mc.tcRefConfigs = createDefaultFormTCRefConfigs();
        return mc;
    }

    /** @return list of <code>Item</code>S */
    private static SplitConstraint[] createDefaultFormConstraints() {
        return new SplitConstraint[] {
            new SplitConstraint(HORIZONTAL, 1, 0.70D),
            new SplitConstraint(VERTICAL,   0, 0.80D),
            new SplitConstraint(HORIZONTAL, 1, 0.50D)
        };
    }
    
    private static TCRefConfig[] createDefaultFormTCRefConfigs() {
        List tcRefConfigs = new ArrayList();
        tcRefConfigs.add(createDefaultComponentInspectorTCRefConfig());
        tcRefConfigs.add(createDefaultComponentPaletteTCRefConfig());
        return (TCRefConfig[])tcRefConfigs.toArray(new TCRefConfig[0]);
    }

    private static TCRefConfig createDefaultComponentInspectorTCRefConfig() {
        TCRefConfig tcrc = new TCRefConfig();
        tcrc.tc_id = "ComponentInspector"; // NOI18N
        tcrc.opened = true;
        return tcrc;
    }

    private static TCRefConfig createDefaultComponentPaletteTCRefConfig() {
        TCRefConfig tcrc = new TCRefConfig();
        tcrc.tc_id = "ComponentPalette"; // NOI18N
        tcrc.opened = true;
        return tcrc;
    }
    

    /** @return list of <code>GroupConfig</code>S. */
    private static GroupConfig[] createDefaultGroupConfigs() {
        List l = new ArrayList();
        // TODO
        return (GroupConfig[])l.toArray(new GroupConfig[0]);
    }


}

