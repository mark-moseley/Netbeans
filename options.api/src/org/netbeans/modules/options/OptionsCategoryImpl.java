/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.options;

import java.awt.Image;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.spi.options.OptionsCategory;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.Utilities;

/**
 * OptionsCategory implementation class. Used by factory method from
 * <code>OptionsCategory</code> as instance created from layer.xml values
 * 
 * @author Max Sauer
 */
public class OptionsCategoryImpl extends OptionsCategory {

    //category fields
    private String title;
    private String categoryName;
    private String iconBase;
    private ImageIcon icon;
    private OptionsPanelController controller;
    private String description;
    private String keywords;
    private String keywordsCategory;
    private String advancedOptionsFolder; //folder for lookup

    public OptionsCategoryImpl(String title, String categoryName, String iconBase, OptionsPanelController controller, String description, String keywords, String keywordsCategory, String advancedOptionsFolder) {
        //either controller or folder where instances od AdvancedOptionControllers are lookedup
        //have to be specified
        assert !(controller == null && advancedOptionsFolder == null);

        this.title = title;
        this.categoryName = categoryName;
        this.iconBase = iconBase;
        this.controller = controller;
        this.advancedOptionsFolder = advancedOptionsFolder;
        this.description = description;
        this.keywords = keywords;
        this.keywordsCategory = keywordsCategory;
    }

    @Override
    public Icon getIcon() {
        if (icon == null) {
            Image image = Utilities.loadImage(iconBase);
            if (image != null) {
                return new ImageIcon(image);
            }
            image = Utilities.loadImage(iconBase + ".png");
            if (image != null) {
                return new ImageIcon(image);
            }
            image = Utilities.loadImage(iconBase + ".gif");
            if (image == null) {
                return null;
            }
            icon = new ImageIcon(image);
        }
        return icon;
    }

    @Override
    public String getCategoryName () {
        return categoryName;
    }

    @Override
    public String getTitle () {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public OptionsPanelController create() {
        if(controller != null) {
            return controller;
        }
        else {
            return new TabbedController(advancedOptionsFolder);
        }
    }

    final Map<String, Set<String>> getKeywordsByCategory() {
        HashMap<String, Set<String>> result = new HashMap<String, Set<String>>();
        if(keywordsCategory != null && keywords != null)
            result.put(keywordsCategory, new HashSet(Collections.list(new StringTokenizer(keywords, ",")))); //NOI18N
        return result;
    }
}
