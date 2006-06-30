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
package org.netbeans.modules.xml.text.syntax;

import java.util.*;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.xml.text.indent.XMLIndentEngine;
import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;


/**
 * Options for the xml editor kit
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public class XMLOptions extends AbstractBaseOptions {
    /** Serial Version UID */
    private static final long serialVersionUID = 2347735706857337892L;

    public static final String COMPLETION_AUTO_POPUP_PROP = "completionAutoPopup"; // NOI18N

    public static final String COMPLETION_AUTO_POPUP_DELAY_PROP = "completionAutoPopupDelay"; // NOI18N
    
    public static final String COMPLETION_INSTANT_SUBSTITUTION_PROP = "completionInstantSubstitution"; // NOI18N                
    
    static final String[] XML_PROP_NAMES = new String[] {
                                                COMPLETION_AUTO_POPUP_PROP,
                                                COMPLETION_AUTO_POPUP_DELAY_PROP,
                                                COMPLETION_INSTANT_SUBSTITUTION_PROP,
                                            };
    
    //
    // init
    //

    /** */
    public XMLOptions () {
        super (XMLKit.class, "xml"); // NOI18N
    }

    protected Class getDefaultIndentEngineClass () {
        return XMLIndentEngine.class;
    }
    
    public boolean getCompletionAutoPopup() {
        return getSettingBoolean(ExtSettingsNames.COMPLETION_AUTO_POPUP);
    }

    public void setCompletionAutoPopup(boolean v) {
        setSettingBoolean(ExtSettingsNames.COMPLETION_AUTO_POPUP, v, COMPLETION_AUTO_POPUP_PROP);
    }

    public int getCompletionAutoPopupDelay() {
        return getSettingInteger(ExtSettingsNames.COMPLETION_AUTO_POPUP_DELAY);
    }

    public void setCompletionAutoPopupDelay(int delay) {
        if (delay < 0) {
            NbEditorUtilities.invalidArgument("MSG_NegativeValue"); // NOI18N
            return;
        }
        setSettingInteger(ExtSettingsNames.COMPLETION_AUTO_POPUP_DELAY, delay,
            COMPLETION_AUTO_POPUP_DELAY_PROP);
    }

    public boolean getCompletionInstantSubstitution() {
        return getSettingBoolean(ExtSettingsNames.COMPLETION_INSTANT_SUBSTITUTION);
    }
    
    public void setCompletionInstantSubstitution(boolean v) {
        setSettingBoolean(ExtSettingsNames.COMPLETION_INSTANT_SUBSTITUTION, v,
            COMPLETION_INSTANT_SUBSTITUTION_PROP);
    }        
    
    // remap old XMLTokenContext to new XMLDefaultTokenContext
    // commented out match by name
    private static final String[][] TRANSLATE_COLORS = {
//        { "xml-comment", "xml-comment" },
//        { "xml-ref", "xml-ref" },
        { "xml-string", "xml-value" },
//        { "xml-attribute", "xml-attribute" },
        { "xml-symbol", "xml-operator" },
//        { "xml-tag", "xml-tag" },
        { "xml-keyword", "xml-doctype" },
        { "xml-plain", "xml-text"},
    };
    
    /**
     * Get coloring, possibly remap setting from previous versions
     * to new one.
     */
    public Map getColoringMap() {
        Map colors = super.getColoringMap();
        
        synchronized (this) {
            // get old customized colors and map them to new token IDs
            // the map will contain only such old colors that was customized AFAIK
            // because current initializer does not create them
            
            for (int i = 0; i<TRANSLATE_COLORS.length; i++) {
                String oldKey = TRANSLATE_COLORS[i][0];
                Object color = colors.get(oldKey);
                if (color != null) {
                    colors.remove(oldKey);
                    String newKey = TRANSLATE_COLORS[i][1];
                    colors.put(newKey, color);
                }
            }
            
            // do not save it explicitly if the user will do a customization
            // it get saved automatically (i.e.old keys removal will apply)
            
            return colors;
        }
    }
}
