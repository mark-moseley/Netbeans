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

package org.netbeans.api.editor.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The definition of a code template. A code template is basically a piece of
 * code with an abbreviation associated to it. When a user types the abbreviation
 * to the editor and presses the expansion key the code associated with the
 * abbreviation gets expanded. The code can contain various parameter that the user
 * can enter during the expansion.
 * 
 * <p>The <code>CodeTemplateDescription</code>s can be obtained from
 * <code>CodeTemplateSettings</code> class that can be loaded from <code>MimeLookup</code>
 * for a particular mime type. See the example below.
 * 
 * <pre>
 * Lookup l = MimeLookup.getLookup(MimePath.parse(mimePath));
 * CodeTemplateSettings cds = l.lookup(CodeTemplateSettings.class);
 * List<CodeTemplateDescription> codeTemplates = cds.getCodeTemplateDescriptions();
 * </pre>
 * 
 * @see CodeTemplateSettings
 * @author Miloslav Metelka
 */
public final class CodeTemplateDescription {

    private final String abbreviation;
    private final String description;
    private final String parametrizedText;
    private final List<String> contexts;    
    private final String uniqueId;
    
    /**
     * Creates a new code template description. It call the other constructor
     * passing <code>null</code> for the <code>contexts</code> parameter.
     * 
     * @param abbreviation The abbreviation text that expands this code template.
     * @param description The code template's display text.
     * @param parametrizedText The actual code template that will get expanded when
     *   a user writes the abbreviation in the editor.
     */
    public CodeTemplateDescription(String abbreviation, String description, String parametrizedText) {
        this(abbreviation, description, parametrizedText, null, null);
    }
    
    /**
     * Creates a new code template description.
     * 
     * <p>Usually clients do not need to create <code>CodeTemplateDescription</code>s
     * by themselvs. Instead they use <code>MimeLookup</code> and <code>CodeTemplateSettings</code>
     * to access code templates registered in the system.
     *
     * @param abbreviation The abbreviation text that expands this code template.
     * @param description The code template's display text.
     *   Can be <code>null</code>
     * @param parametrizedText The actual code template that will get expanded when
     *   a user writes the abbreviation in the editor.
     * @param contexts The list of context ids that apply for this code template.
     *   Can be <code>null</code>
     * @param uniqueId The id uniquely identifying this template. If you pass
     *   non-<code>null</code> value, please make sure that it is really a unique
     *   id for this template. Can be <code>null</code>.
     */
    public CodeTemplateDescription(
        String abbreviation, 
        String description, 
        String parametrizedText, 
        List<String> contexts,
        String uniqueId
    ) {
        assert abbreviation != null : "The abbreviation parameter can't be null"; //NOI18N
        assert parametrizedText != null : "The parametrizedText parameter can't be null"; //NOI18N
        
        this.abbreviation = abbreviation;
        this.description = description;
        this.parametrizedText = parametrizedText;
        this.contexts = contexts == null ? 
            Collections.<String>emptyList() : 
            Collections.unmodifiableList(new ArrayList<String>(contexts));
        this.uniqueId = uniqueId;
    }
    
    /**
     * Gets the abbreviation text that triggers expansion of this code template.
     * 
     * <p>The abbreviation text should be unique among all code templates defined
     * for a one mime type so that each code template can be expanded individually.
     *
     * @return The abbreviation text that expands this code template.
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Gets textual description of this code template. It's a display text
     * that can be shown in UI such as the code completion window or Tools-Options
     * dialog.
     *
     * @return The display text for this code template or <code>null</code> if this
     *   code template has no descriptions.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the code text of this code template.
     * 
     * This is the text that will be expanded when a user types the abbreviation
     * in the editor and presses the expansion key. The text can contain parameters
     * in the form of "${...}".
     *
     * @return The code text with parameters.
     */
    public String getParametrizedText() {
        return parametrizedText;
    }

    /**
     * Gets the list of contexts that apply for this code template. The contexts
     * are simply unique identifiers used by the infrastructure to filter out
     * code templates that are not suitable for the editor context, where a user
     * types.
     * 
     * <p>The actual identifiers are defined by each particular language (mime type)
     * and can be different for different languages. The language defines contexts
     * for its constructs such as loops, methods, classes, if-else blocks, etc. and
     * than tags each code template available for that language with a context,
     * where it is meaningful to apply the template.
     * 
     * @return The contexts for this code template.
     */
    public List<String> getContexts() {
        return contexts;
    }

    /**
     * Gets an id that can be used for identifying this template. A code template
     * does not generally have to have a unique id, but if it has one it is
     * guaranteed to uniquely identify the template.
     * 
     * <p class="nonnormative">Unique ids can be useful for tools importing and
     * exporting code templates from other applications such as TextMate, etc.
     * 
     * @return The unique id or <code>null</code>.
     * @since 1.11
     */
    public String getUniqueId() {
        return uniqueId;
    }
    
    public String toString() {
        return "abbrev='" + getAbbreviation() + "', parametrizedText='" + getParametrizedText() + "'"; // NOI18N
    }
    
}
