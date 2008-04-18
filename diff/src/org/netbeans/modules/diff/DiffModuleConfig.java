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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.diff;

import org.openide.util.NbPreferences;
import org.openide.util.Lookup;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.InstanceDataObject;
import org.netbeans.spi.diff.DiffProvider;
import org.netbeans.modules.diff.builtin.provider.BuiltInDiffProvider;
import org.netbeans.modules.diff.cmdline.CmdlineDiffProvider;

import java.util.prefs.Preferences;
import java.util.*;
import java.awt.Color;

/**
 * Module settings for Diff module.
 * 
 * @author Maros Sandor
 */
public class DiffModuleConfig {
                                                                                             
    public static final String PREF_EXTERNAL_DIFF_COMMAND = "externalDiffCommand"; // NOI18N

    private static final String PREF_IGNORE_LEADINGTRAILING_WHITESPACE = "ignoreWhitespace"; // NOI18N
    private static final String PREF_IGNORE_INNER_WHITESPACE = "ignoreInnerWhitespace"; // NOI18N
    private static final String PREF_IGNORE_CASE = "ignoreCase"; // NOI18N
    private static final String PREF_USE_INTERNAL_DIFF = "useInternalDiff"; // NOI18N
    private static final String PREF_ADDED_COLOR = "addedColor"; // NOI18N
    private static final String PREF_CHANGED_COLOR = "changedColor"; // NOI18N
    private static final String PREF_DELETED_COLOR = "deletedColor"; // NOI18N
    
    private static final DiffModuleConfig INSTANCE = new DiffModuleConfig();
    
    private final Color defaultAddedColor = new Color(180, 255, 180);
    private final Color defaultChangedColor = new Color(160, 200, 255);
    private final Color defaultDeletedColor = new Color(255, 160, 180);

    public static DiffModuleConfig getDefault() {
        return INSTANCE;
    }

    private DiffModuleConfig() {
    }
    
    public Color getAddedColor() {
        return getColor(PREF_ADDED_COLOR, defaultAddedColor);
    }

    public Color getChangedColor() {
        return getColor(PREF_CHANGED_COLOR, defaultChangedColor);
    }

    public Color getDeletedColor() {
        return getColor(PREF_DELETED_COLOR, defaultDeletedColor);
    }
    
    public void setChangedColor(Color changedColor) {
        putColor(PREF_CHANGED_COLOR, changedColor);
    }

    public void setAddedColor(Color addedColor) {
        putColor(PREF_ADDED_COLOR, addedColor);
    }
   
    public void setDeletedColor(Color deletedColor) {
        putColor(PREF_DELETED_COLOR, deletedColor);
    }

    private void putColor(String key, Color color) {
        getPreferences().putInt(key, color.getRGB());
    }

    private Color getColor(String key, Color defaultColor) {
        int rgb = getPreferences().getInt(key, defaultColor.getRGB());
        return new Color(rgb);
    }
  
    public DiffProvider getDefaultDiffProvider() {
        DiffProvider provider = Lookup.getDefault().lookup(DiffProvider.class);
        if (provider instanceof BuiltInDiffProvider) {
            ((BuiltInDiffProvider) provider).setOptions(getOptions());
        } else if (provider instanceof CmdlineDiffProvider) {
            ((CmdlineDiffProvider) provider).setDiffCommand(getDiffCommand());
        }
        return provider;
    }

    private String getDiffCommand() {
        return getPreferences().get(PREF_EXTERNAL_DIFF_COMMAND, "diff {0} {1}");
    }

    public void setOptions(BuiltInDiffProvider.Options options) {
        getPreferences().putBoolean(PREF_IGNORE_LEADINGTRAILING_WHITESPACE, options.ignoreLeadingAndtrailingWhitespace);
        getPreferences().putBoolean(PREF_IGNORE_INNER_WHITESPACE, options.ignoreInnerWhitespace);
        getPreferences().putBoolean(PREF_IGNORE_CASE, options.ignoreCase);
        getBuiltinProvider().setOptions(options);
    }

    public BuiltInDiffProvider.Options getOptions() {
        BuiltInDiffProvider.Options options = new BuiltInDiffProvider.Options();
        options.ignoreLeadingAndtrailingWhitespace = getPreferences().getBoolean(PREF_IGNORE_LEADINGTRAILING_WHITESPACE, true);
        options.ignoreInnerWhitespace = getPreferences().getBoolean(PREF_IGNORE_INNER_WHITESPACE, false);
        options.ignoreCase = getPreferences().getBoolean(PREF_IGNORE_CASE, false);
        return options;
    }
        
    private BuiltInDiffProvider getBuiltinProvider() {
        Collection<? extends DiffProvider> diffs = Lookup.getDefault().lookupAll(DiffProvider.class);
        for (DiffProvider diff : diffs) {
            if (diff instanceof BuiltInDiffProvider) {
                return (BuiltInDiffProvider) diff;
            }
        }
        throw new IllegalStateException("No builtin diff provider");
    }
    
    public void setUseInteralDiff(boolean useInternal) {
        getPreferences().putBoolean(PREF_USE_INTERNAL_DIFF, useInternal);
        Collection<? extends DiffProvider> diffs = Lookup.getDefault().lookupAll(DiffProvider.class);
        if (useInternal) {
            setDefaultProvider(getBuiltinProvider());
        } else {
            for (DiffProvider diff : diffs) {
                if (diff instanceof CmdlineDiffProvider) {
                    setDefaultProvider(diff);
                    break;
                }
            }
        }
    }

    public boolean isUseInteralDiff() {
        return getPreferences().getBoolean(PREF_USE_INTERNAL_DIFF, true);
    }

    private void setDefaultProvider(DiffProvider ds) {
        // TODO: for compatibility with legacy diff component, think of better way
        FileSystem dfs = org.openide.filesystems.Repository.getDefault().getDefaultFileSystem();
        FileObject services = dfs.findResource("Services/DiffProviders");
        DataFolder df = DataFolder.findFolder(services);
        DataObject[] children = df.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof InstanceDataObject) {
                InstanceDataObject ido = (InstanceDataObject) children[i];
                if (ido.instanceOf(ds.getClass())) {
                    try {
                        if (ds.equals(ido.instanceCreate())) {
                            df.setOrder(new DataObject[] { ido });
                            break;
                        }
                    } catch (java.io.IOException ioex) {
                    } catch (ClassNotFoundException cnfex) {}
                }
            }
        }
    }
    
    // properties ~~~~~~~~~~~~~~~~~~~~~~~~~

    public Preferences getPreferences() {
        return NbPreferences.forModule(DiffModuleConfig.class);
    }
    
    
}
