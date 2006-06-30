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

package org.netbeans.modules.editor;

import java.awt.event.InputEvent;
import java.util.Map;
import org.netbeans.editor.BaseSettingsInitializer;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.editor.ext.ExtSettingsInitializer;
import org.netbeans.modules.editor.options.OptionUtilities;
import org.netbeans.modules.editor.options.AllOptionsFolder;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.SettingsNames;
import org.netbeans.modules.editor.options.BaseOptions;
import org.openide.util.Utilities;

/**
* Customized settings for NetBeans editor
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorSettingsInitializer extends Settings.AbstractInitializer {

    public static final String NAME = "nb-editor-settings-initializer"; // NOI18N

    private static boolean inited;

    public static void init() {
        if (!inited) {
            inited = true;
            Settings.addInitializer(new BaseSettingsInitializer(), Settings.CORE_LEVEL);
            Settings.addInitializer(new ExtSettingsInitializer(), Settings.CORE_LEVEL);
            Settings.addInitializer(new NbEditorSettingsInitializer());

            Settings.reset();
        }
    }

    public NbEditorSettingsInitializer() {
        super(NAME);
    }

    /** Update map filled with the settings.
    * @param kitClass kit class for which the settings are being updated.
    *   It is always non-null value.
    * @param settingsMap map holding [setting-name, setting-value] pairs.
    *   The map can be empty if this is the first initializer
    *   that updates it or if no previous initializers updated it.
    */
    public void updateSettingsMap(Class kitClass, Map settingsMap) {

        if (kitClass == BaseKit.class) {
            settingsMap.put(BaseOptions.TOOLBAR_VISIBLE_PROP, Boolean.TRUE);
            settingsMap.put(BaseOptions.LINE_NUMBER_VISIBLE_PROP, SettingsDefaults.defaultLineNumberVisible);
            
	    //Fix for IZ bug #53744:
	    //On MAC OS X, Ctrl+left click has the same meaning as the right-click.
	    //The hyperlinking should be enabled for the Command key on MAC OS X, for Ctrl on others:
            int activationMask;
            
            if ((Utilities.getOperatingSystem() & Utilities.OS_MAC) != 0) {
                activationMask = InputEvent.META_MASK;
            } else {
                activationMask = InputEvent.CTRL_DOWN_MASK;
            }
            
            settingsMap.put(SettingsNames.HYPERLINK_ACTIVATION_MODIFIERS, new Integer(activationMask));
        }

        if (kitClass == NbEditorKit.class) {
            // init popup menu items from layer folder
            if (AllOptionsFolder.getDefault().baseInitialized()){
                // put to the settings map only if base options has been initialized. See #19470
                settingsMap.put(ExtSettingsNames.POPUP_MENU_ACTION_NAME_LIST,
                    OptionUtilities.getPopupStrings(OptionUtilities.getGlobalPopupMenuItems())
                );
            }
        }
        
    }

}
