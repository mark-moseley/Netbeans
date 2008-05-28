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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.api.options;

import java.util.Arrays;
import java.util.logging.Logger;
import org.netbeans.modules.options.CategoryModel;
import org.netbeans.modules.options.OptionsDisplayerImpl;
import org.openide.util.Mutex;
/**
 * Permits Options Dialog to open the options dialog with some category pre-selected.
 * @since 1.5
 * @author Radek Matous
 */
public final class OptionsDisplayer {
    private static final OptionsDisplayer INSTANCE = new OptionsDisplayer();
    private final OptionsDisplayerImpl impl = new OptionsDisplayerImpl(false);
    private static Logger log = Logger.getLogger(OptionsDisplayer.class.getName());
    /** Registration name of Advanced category (aka Miscellaneous). 
     * @since 1.8
     */
    public static final String ADVANCED = "Advanced"; // NOI18N
        
    private OptionsDisplayer() {}    
    /**
     * Get the default <code>OptionsDisplayer</code>
     * @return the default instance
     */
    public static OptionsDisplayer getDefault() {
        return INSTANCE;
    }
    
    /**
     * Open the options dialog with no guarantee which category is pre-selected.
     * @return true if optins dialog was sucesfully opened with some pre-selected
     * category. If no category is registered at all then false will be returned and
     * options dialog won't be opened.
     */
    public boolean open() {
        return open(CategoryModel.getInstance().getCurrentCategoryID());
    }
    
    /**
     * Open the options dialog with some category and subcategory pre-selected
     * according to given path.
     * @param path path of category and subcategories to be selected. Path is 
     * composed from registration names divided by slash. E.g. "MyCategory" or 
     * "MyCategory/Subcategory2" for the following registration:
     * <pre style="background-color: rgb(255, 255, 153);">
     * &lt;folder name="OptionsDialog"&gt;
     *     &lt;file name="MyCategory.instance"&gt;
     *         &lt;attr name="instanceClass" stringvalue="org.foo.MyCategory"/&gt;
     *         &lt;attr name="position" intvalue="900"/&gt;
     *     &lt;/file&gt;
     *     &lt;folder name="MyCategory"&gt;
     *         &lt;file name="SubCategory1.instance"&gt;
     *             &lt;attr name="instanceClass" stringvalue="org.foo.Subcategory1"/&gt;
     *         &lt;/file&gt;
     *         &lt;file name="SubCategory2.instance"&gt;
     *             &lt;attr name="instanceClass" stringvalue="org.foo.Subcategory2"/&gt;
     *         &lt;/file&gt;
     *     &lt;/file&gt;
     * &lt;/folder&gt;</pre>
     * @return true if optins dialog was sucesfully opened with required category.
     * If this method is called when options dialog is already opened then this method
     * will return immediately false without affecting currently selected category
     * in opened options dialog.
     * If category (i.e. the first item in the path) does not correspond to any
     * of registered categories then false is returned and options dialog is not opened
     * at all (e.g. in case that module providing such category is not installed or enabled).
     * If subcategory doesn't exist, it opens with category selected and
     * it returns true. It is up to particular <code>OptionsPanelController</code> 
     * to handle such situation.
     * @since 1.8
     */
    public boolean open(final String path) {
        log.fine("Open Options Dialog: " + path); //NOI18N
        return openImpl(path);
    }

    private boolean openImpl(final String path) {
        if(path == null) {
            log.warning("Category to open is null."); //NOI18N
            return false;
        }
        final String categoryId = path.indexOf('/') == -1 ? path : path.substring(0, path.indexOf('/'));
        final String subpath = path.indexOf('/') == -1 ? null : path.substring(path.indexOf('/')+1);
        Boolean retval = Mutex.EVENT.readAccess(new Mutex.Action<Boolean> () {
            public Boolean run() {
                Boolean r = impl.isOpen();
                boolean retvalForRun = !r;
                if (retvalForRun) {
                    retvalForRun = Arrays.asList(CategoryModel.getInstance().getCategoryIDs()).contains(categoryId);
                    if (!retvalForRun) {
                        log.warning("Unknown categoryId: " + categoryId); //NOI18N
                    }
                } else {
                    log.warning("Options Dialog is opened"); //NOI18N
                }
                if (retvalForRun) {
                    impl.showOptionsDialog(categoryId, subpath);
                }
                return Boolean.valueOf(retvalForRun);
            }
        });
        return retval;
    }
}