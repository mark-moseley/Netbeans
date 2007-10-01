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

package org.netbeans.modules.languages.features;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JMenu;
import javax.swing.text.JTextComponent;

import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.editor.NbEditorKit.GenerateFoldPopupAction;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;


/**
 *
 * @author Jan Jancura
 */
public class LanguagesGenerateFoldPopupAction extends GenerateFoldPopupAction {

    public static final String EXPAND_PREFIX = "Expand:";
    public static final String COLLAPSE_PREFIX = "Collapse:";
    
    protected void addAdditionalItems (JTextComponent target, JMenu menu) {
        try {
            String mimeType = (java.lang.String) target.getDocument ().getProperty ("mimeType");
            Language l = LanguagesManager.getDefault ().getLanguage (mimeType);
            Set expands = new HashSet ();
            addFoldTypes (target, menu, l, expands);
            Iterator<Language> it = l.getImportedLanguages ().iterator ();
            while (it.hasNext ())
                addFoldTypes (target, menu, it.next (), expands);
        } catch (ParseException ex) {
        }
    }

    private void addFoldTypes (JTextComponent target, JMenu menu, Language l, Set expands) {
        List<Feature> features = l.getFeatures (LanguagesFoldManager.FOLD);
        Iterator<Feature> it = features.iterator ();
        while (it.hasNext ()) {
            Feature fold = it.next ();
            String expand = l.localize((String) fold.getValue ("expand_type_action_name"));
            if (expand == null) continue;
            if (expands.contains (expand))
                continue;
            expands.add (expand);
            String collapse = l.localize((String) fold.getValue ("collapse_type_action_name"));
            if (collapse == null) continue;
            addAction (target, menu, EXPAND_PREFIX + expand);
            addAction (target, menu, COLLAPSE_PREFIX + collapse);
            setAddSeparatorBeforeNextAction (true);
        }
    }
}
    