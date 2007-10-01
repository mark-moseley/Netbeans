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

package org.netbeans.modules.editor.completion;

import java.awt.Color;
import java.awt.Dimension;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtSettingsDefaults;
import org.netbeans.editor.ext.ExtSettingsNames;

/**
 * Maintenance of the editor settings related to the code completion.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class CompletionSettings implements SettingsChangeListener {
    
    public static final CompletionSettings INSTANCE = new CompletionSettings();
    
    private static final Object NULL_VALUE = new Object();
    
    private Reference<JTextComponent> editorComponentRef;
    
    private Map<String, Object> settingName2value = new HashMap<String, Object>();
    
    private CompletionSettings() {
        Settings.addSettingsChangeListener(this);
    }
    
    public boolean completionAutoPopup() {
        return ((Boolean)getValue(
                ExtSettingsNames.COMPLETION_AUTO_POPUP,
                ExtSettingsDefaults.defaultCompletionAutoPopup)
        ).booleanValue();
    }
    
    public int completionAutoPopupDelay() {
        return ((Integer)getValue(
                ExtSettingsNames.COMPLETION_AUTO_POPUP_DELAY,
                ExtSettingsDefaults.defaultCompletionAutoPopupDelay)
        ).intValue();
    }
    
    public boolean documentationAutoPopup() {
        return ((Boolean)getValue(
                ExtSettingsNames.JAVADOC_AUTO_POPUP,
                ExtSettingsDefaults.defaultJavaDocAutoPopup)
        ).booleanValue();
    }
    
    public int documentationAutoPopupDelay() {
        return ((Integer)getValue(
                ExtSettingsNames.JAVADOC_AUTO_POPUP_DELAY,
                ExtSettingsDefaults.defaultJavaDocAutoPopupDelay)
        ).intValue();
    }
    
    public Dimension completionPopupMaximumSize() {
        return (Dimension)getValue(
                ExtSettingsNames.COMPLETION_PANE_MAX_SIZE,
                ExtSettingsDefaults.defaultCompletionPaneMaxSize);
    }
    
    public Dimension documentationPopupPreferredSize() {
        return (Dimension)getValue(
                ExtSettingsNames.JAVADOC_PREFERRED_SIZE,
                ExtSettingsDefaults.defaultJavaDocPreferredSize);
    }
    
    public Color documentationBackgroundColor() {
        return (Color)CompletionSettings.INSTANCE.getValue(
                ExtSettingsNames.JAVADOC_BG_COLOR,
                ExtSettingsDefaults.defaultJavaDocBGColor);
    }

    public boolean completionInstantSubstitution() {
        return ((Boolean)getValue(
                ExtSettingsNames.COMPLETION_INSTANT_SUBSTITUTION,
                ExtSettingsDefaults.defaultCompletionInstantSubstitution)
        ).booleanValue();
    }
    
    public void notifyEditorComponentChange(JTextComponent newEditorComponent) {
        this.editorComponentRef = new WeakReference<JTextComponent>(newEditorComponent);
        clearSettingValues();
    }
    
    public Object getValue(String settingName) {
        Object value;
        synchronized (this) {
            value = settingName2value.get(settingName);
        }
        
        if (value == null) {
            JTextComponent c = editorComponentRef.get();
            if (c != null) {
                Class kitClass = Utilities.getKitClass(c);
                if (kitClass != null) {
                    value = Settings.getValue(kitClass, settingName);
                    if (value == null) {
                        value = NULL_VALUE;
                    }
                }
            }
            
            if (value != null) {
                synchronized (this) {
                    settingName2value.put(settingName, value);
                }
            }
        }
        
        if (value == NULL_VALUE) {
            value = null;
        }
        return value;
    }
    
    public Object getValue(String settingName, Object defaultValue) {
        Object value = getValue(settingName);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }
    
    public void settingsChange(SettingsChangeEvent evt) {
        clearSettingValues();
    }
    
    private synchronized void clearSettingValues() {
        settingName2value.clear();
    }
}
