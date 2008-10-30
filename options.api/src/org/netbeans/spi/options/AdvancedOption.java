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

package org.netbeans.spi.options;

import org.netbeans.modules.options.AdvancedOptionImpl;
import java.util.Map;

/**
 * This class represents one category (like "Ant"
 * or "Form Editor") in Miscellaneous Panel of Options Dialog. Its instances should
 * be registered in layers and created by <code>createSubCategory</code> factory
 * method as follows:
 *
 * <pre style="background-color: rgb(255, 255, 153);">
 * &lt;folder name="OptionsDialog"&gt;
 *     &lt;folder name="Advanced"&gt;
 *         &lt;file name="FooAdvancedPanel.instance"&gt;
 *             &lt;attr name="instanceCreate" methodvalue="org.netbeans.spi.options.AdvancedOption.createSubCategory"/&gt;
 *             &lt;attr name="controller" newvalue="org.foo.ToDoOptionsController"/&gt;
 *             &lt;attr name="displayName" bundlevalue="org.foo.Bundle#LBL_Foo"/&gt;
 *             &lt;attr name="toolTip" bundlevalue="org.foo.Bundle#HINT_Foo"/&gt;
 *             &lt;attr name="keywords" bundlevalue="org.foo.Bundle#KW_Foo"/&gt;
 *             &lt;attr name="keywordsCategory" stringvalue="Advanced/FooSubTabInOptions"/&gt;
 *         &lt;/file&gt;
 *     &lt;/folder&gt;
 * &lt;/folder&gt;</pre>
 *
 * where:
 * <br/><b>controller</b> should be an instance of <code>OptionsPanelController</code>
 * <br/><b>displayName</b> should be a pointer to Bundle where your tab displayname is stored
 * <br/><b>toolTip</b> should be a pointer to Bundle where your tab toolTip is stored
 * <br/><b>keywords</b> should be localized keywords list, separated by comma in Bundle, for quickserach purposes
 * <br/><b>keywordsCategory</b> should be relative path to your panel inside Options dialog
 * <br/><br/>
 * No explicit sorting recognized (may be sorted e.g. by display name).
 *
 * <p><b>Related documentation</b>
 *
 * <ul>
 * <li><a href="http://platform.netbeans.org/tutorials/nbm-options.html">NetBeans Options Window Module Tutorial</a>
 * </ul>
 *
 * @see OptionsCategory
 * @see OptionsPanelController 
 * @author Jan Jancura
 * @author Max Sauer
 */
public abstract class AdvancedOption {

    //xml entry names
    private static final String DISPLAYNAME = "displayName";
    private static final String TOOLTIP = "toolTip";
    private static final String KEYWORDS = "keywords";
    private static final String CONTROLLER = "controller";
    private static final String KEYWORDS_CATEGORY = "keywordsCategory";

    /**
     * Returns name of category used in Advanced Panel of 
     * Options Dialog.
     *
     * @return name of category
     */
    public abstract String getDisplayName ();
    
    /**
     * Returns tooltip to be used on category name.
     *
     * @return tooltip for this category
     */
    public abstract String getTooltip ();

    /**
     * Returns {@link OptionsPanelController} for this category. PanelController 
     * creates visual component to be used inside of Advanced Panel.
     *
     * @return new instance of {@link OptionsPanelController} for this advanced options 
     *         category
     */
    public abstract OptionsPanelController create ();

    /**
     * Factory method for creating instaces of Advanced option in a declarative
     * way by loading necessary values from layer.xml
     *
     * @param attrs attributes defined in layer
     * @return instance of <code>AdvancedOption</code>
     */
    static AdvancedOption createSubCategory(Map attrs) {
        String displayName = (String) attrs.get(DISPLAYNAME);
        String tooltip = (String) attrs.get(TOOLTIP);
        String keywords = (String) attrs.get(KEYWORDS);
        OptionsPanelController controller = (OptionsPanelController) attrs.get(CONTROLLER);
        String keywordsCategory = (String) attrs.get(KEYWORDS_CATEGORY);

        return new AdvancedOptionImpl(controller, displayName, tooltip, keywords, keywordsCategory);
    }
}
