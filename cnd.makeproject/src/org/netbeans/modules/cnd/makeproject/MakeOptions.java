/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.cnd.makeproject.api.platforms.Platform;
import org.openide.options.SystemOption;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;

public class MakeOptions extends SystemOption implements PropertyChangeListener {
    static final long serialVersionUID = 5619262632730516348L;
    static private MakeOptions instance = null;
    //
    // Default make options
    static final String MAKE_OPTIONS = "makeOptions"; // NOI18N
    static private String defaultMakeOptions = "";
    static private String makeOptions = null;
    //
    // Default Platform
    static final String PLATFORM = "platform"; // NOI18N
    static private int defaultPlatform;
    static private int platform = -1;
    //
    // Default Path mode
    public static final int PATH_REL_OR_ABS = 0;
    public static final int PATH_REL = 1;
    public static final int PATH_ABS = 2;
    public static String[] PathModeNames = new String[] {
        getString("TXT_Auto"),
        getString("TXT_AlwaysRelative"),
        getString("TXT_AlwaysAbsolute"),
    };
    static final String PATH_MODE = "pathMode"; // NOI18N
    static private int pathMode = PATH_REL;
    
    // Dependency checking
    static final String DEPENDENCY_CHECKING = "dependencyChecking"; // NOI18N
    static private boolean dependencyChecking = false;
    
    // Save
    static final String SAVE = "save";  // NOI18N
    static private boolean save = true;
    
    // Reuse
    static final String REUSE = "reuse";  // NOI18N
    static private boolean reuse = true;
    
    static public MakeOptions getInstance() {
        if (instance == null) {
            instance = (MakeOptions) SharedClassObject.findObject(MakeOptions.class, true);
        }
        return instance;
    }
    
    public static void setDefaultMakeOptions(String makeOptions) {
        defaultMakeOptions = makeOptions;
    }
    
    public static String getDefaultMakeOptions() {
        return defaultMakeOptions;
    }    
    
    public MakeOptions() {
        super();
        addPropertyChangeListener(this);
    }
    
    public String displayName() {
        return "Make Project Options"; // NOI18N (not visible)
    }
    
    public String getMakeOptions() {
        if (makeOptions == null) {
            makeOptions = defaultMakeOptions;
        }
        return makeOptions;
    }
    
    public void setMakeOptions(String value) {
        String oldValue = getMakeOptions();
        makeOptions = value;
        if (!oldValue.equals(value))
            firePropertyChange(MAKE_OPTIONS, oldValue, value);
    }
    
    public int getPlatform() {
        if (platform < 0) {
            platform = Platform.getDefaultPlatform();
        }
        return platform;
    }
    
    public void setPlatform(int value) {
        int oldValue = getPlatform();
        platform = value;
        if (oldValue != value)
            firePropertyChange(PLATFORM, "" + oldValue, "" + value); // NOI18N
    }
    
    public int getPathMode() {
        return pathMode;
    }
    
    public void setPathMode(int pathMode) {
        int oldValue = getPathMode();
        this.pathMode = pathMode;
        if (oldValue != pathMode)
            firePropertyChange(MAKE_OPTIONS, new Integer(oldValue), new Integer(pathMode));
    }
    
    public void setDepencyChecking(boolean dependencyChecking) {
        boolean oldValue = getDepencyChecking();
        this.dependencyChecking = dependencyChecking;
        if (oldValue != dependencyChecking)
            firePropertyChange(DEPENDENCY_CHECKING, new Boolean(oldValue), new Boolean(dependencyChecking));
    }
    
    public boolean getDepencyChecking() {
        return dependencyChecking;
    }
    
    public boolean getSave() {
        return save;
    }

    public void setSave(boolean save) {
        this.save = save;
    }
    
    public boolean getReuse() {
        return reuse;
    }

    public void setReuse(boolean reuse) {
        this.reuse = reuse;
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
    }
    
    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(MakeOptions.class, s);
    }

}

