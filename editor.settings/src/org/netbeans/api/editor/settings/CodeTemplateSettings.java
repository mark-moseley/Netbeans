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

package org.netbeans.api.editor.settings;

import java.util.List;
import javax.swing.KeyStroke;

/**
 * Code templates settings are represented by map
 * of key=&lt;String&gt;code template name
 * and value=&lt;String&gt;code template string.
 * <br>
 * Instances of this class should be retrieved from the {@link org.netbeans.api.editor.mimelookup.MimeLookup}
 * for a given mime-type.
 * <br>
 * <font color="red">This class must NOT be extended by any API clients</font>
 *
 * @author Martin Roskanin
 */
public abstract class CodeTemplateSettings {

    /**
     * Construction prohibited for API clients.
     */
    public CodeTemplateSettings() {
        // Control instantiation of the allowed subclass only
        if (!"org.netbeans.modules.editor.settings.xxx".equals(getClass().getName())) { // NOI18N
            throw new IllegalStateException("Instantiation prohibited."); // NOI18N
        }
    }
    
    /**
     * Gets list of code template descriptions.
     *
     * @return non-modifiable list of the code template descriptions.
     */
    public abstract List/*<CodeTemplateDescription>*/ getCodeTemplateDescriptions();
    
    /**
     * Get the keystroke that expands the code templates abbreviations.
     *
     * @return non-null keystroke that expands the code template abbreviations
     *  into code templates.
     */
    public abstract KeyStroke getExpandKey();
    
}
