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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.api.utils.ElfExecutableFileFilter;
import org.netbeans.modules.cnd.api.utils.FileChooser;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.api.utils.ElfDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.ElfStaticLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.MacOSXDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.MacOSXExecutableFileFilter;
import org.netbeans.modules.cnd.api.utils.PeDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.api.utils.PeExecutableFileFilter;
import org.netbeans.modules.cnd.api.utils.PeStaticLibraryFileFilter;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Sheet;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class MakefileConfiguration {
    private MakeConfiguration makeConfiguration;
    
    private StringConfiguration buildCommandWorkingDir;
    private StringConfiguration buildCommand;
    private StringConfiguration cleanCommand;
    private StringConfiguration output;
    
    // Constructors
    public MakefileConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        buildCommandWorkingDir = new StringConfiguration(null, "ExistingProjectRoot"); // NOI18N
        buildCommand = new StringConfiguration(null, "${MAKE} -f ExistingMakefile"); // NOI18N
        cleanCommand = new StringConfiguration(null, "${MAKE} -f ExistingMakefile clean"); // NOI18N
        output = new StringConfiguration(null, ""); // NOI18N
    }
    
    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
    }
    public MakeConfiguration getMakeConfiguration() {
        return makeConfiguration;
    }
    
    // Working Dir
    public StringConfiguration getBuildCommandWorkingDir() {
        return buildCommandWorkingDir;
    }
    
    // Working Dir
    public String getBuildCommandWorkingDirValue() {
        if (buildCommandWorkingDir.getValue().length() == 0)
            return "."; // NOI18N
        else
            return buildCommandWorkingDir.getValue();
    }
    
    public void setBuildCommandWorkingDir(StringConfiguration buildCommandWorkingDir) {
        this.buildCommandWorkingDir = buildCommandWorkingDir;
    }
    
    // Build Command
    public StringConfiguration getBuildCommand() {
        return buildCommand;
    }
    
    public void setBuildCommand(StringConfiguration buildCommand) {
        this.buildCommand = buildCommand;
    }
    
    // Build Command
    public StringConfiguration getCleanCommand() {
        return cleanCommand;
    }
    
    public void setCleanCommand(StringConfiguration cleanCommand) {
        this.cleanCommand = cleanCommand;
    }
    
    // Output
    public StringConfiguration getOutput() {
        return output;
    }
    
    public void setOutput(StringConfiguration output) {
        this.output = output;
    }
    
    // Extra
    public boolean canBuild() {
        return getBuildCommand().getValue().length() > 0;
    }
    
    public String getAbsBuildCommandWorkingDir() {
        if (getBuildCommandWorkingDirValue().length() > 0 && IpeUtils.isPathAbsolute(getBuildCommandWorkingDirValue()))
            return getBuildCommandWorkingDirValue();
        else
            return getMakeConfiguration().getBaseDir() + "/" + getBuildCommandWorkingDirValue(); // NOI18N
    }
    
    public boolean canClean() {
        return getCleanCommand().getValue().length() > 0;
    }
    
    public String getAbsOutput() {
        if (getOutput().getValue().length() == 0)
            return ""; // NOI18N
        else if (IpeUtils.isPathAbsolute(getOutput().getValue()))
            return getOutput().getValue();
        else {
            // FIXME
//            List<String> paths = new ArrayList<String>();
//            paths.add(makeConfiguration.getBaseDir());
//            paths.addAll(mcd.getSourceRoots());
//
//            for (String dir : paths) {
//                dir = dir.replace("\\", "/");  // NOI18N
//                String path = dir.replace("\\", "/") + '/' + getOutput().getValue();
//                File file = new File(path);
//                if (file.exists()) {
//                    return path;
//                }
            return getMakeConfiguration().getBaseDir() + "/"  + getOutput().getValue(); // NOI18N
        }
    }
    
    // Clone and assign
    public void assign(MakefileConfiguration conf) {
        // MakefileConfiguration
        setMakeConfiguration(conf.getMakeConfiguration());
        getBuildCommandWorkingDir().assign(conf.getBuildCommandWorkingDir());
        getBuildCommand().assign(conf.getBuildCommand());
        getCleanCommand().assign(conf.getCleanCommand());
        getOutput().assign(conf.getOutput());
    }
    
    public Object clone() {
        MakefileConfiguration clone = new MakefileConfiguration(getMakeConfiguration());
        clone.setBuildCommandWorkingDir((StringConfiguration)getBuildCommandWorkingDir().clone());
        clone.setBuildCommand((StringConfiguration)getBuildCommand().clone());
        clone.setCleanCommand((StringConfiguration)getCleanCommand().clone());
        clone.setOutput((StringConfiguration)getOutput().clone());
        return clone;
    }
    
    public Sheet getSheet() {
        Sheet sheet = new Sheet();
        
        Sheet.Set set = new Sheet.Set();
        set.setName("Makefile"); // NOI18N
        set.setDisplayName(getString("MakefileTxt"));
        set.setShortDescription(getString("MakefileHint"));
        set.put(new DirStringNodeProp(getBuildCommandWorkingDir(), "WorkingDirectory", getString("WorkingDirectory_LBL"), getString("WorkingDirectory_TT"))); // NOI18N
        set.put(new StringNodeProp(getBuildCommand(), "BuildCommandLine", getString("BuildCommandLine_LBL"), getString("BuildCommandLine_TT"))); // NOI18N
        set.put(new StringNodeProp(getCleanCommand(),  "CleanCommandLine", getString("CleanCommandLine_LBL"), getString("CleanCommandLine_TT"))); // NOI18N
        set.put(new OutputStringNodeProp(getOutput(), "BuildResult", getString("BuildResult_LBL"), getString("BuildResult_TT"))); // NOI18N
        sheet.put(set);
        
        return sheet;
    }
    
    private class DirStringNodeProp extends StringNodeProp {
        public DirStringNodeProp(StringConfiguration stringConfiguration, String txt1, String txt2, String txt3) {
            super(stringConfiguration, txt1, txt2, txt3);
        }
        
        @Override
        public void setValue(String v) {
            String path = IpeUtils.toRelativePath(getMakeConfiguration().getBaseDir(), v); // FIXUP: not always relative path
            path = FilePathAdaptor.normalize(path);
            super.setValue(path);
        }
        
        @Override
        public PropertyEditor getPropertyEditor() {
            return new DirEditor(getAbsBuildCommandWorkingDir());
        }
    }
    
    private class OutputStringNodeProp extends StringNodeProp {
        public OutputStringNodeProp(StringConfiguration stringConfiguration, String txt1, String txt2, String txt3) {
            super(stringConfiguration, txt1, txt2, txt3);
        }
        
        @Override
        public void setValue(String v) {
            String path = IpeUtils.toRelativePath(getMakeConfiguration().getBaseDir(), v); // FIXUP: not always relative path
            path = FilePathAdaptor.normalize(path);
            super.setValue(path);
        }
        
        @Override
        public PropertyEditor getPropertyEditor() {
            String seed = getAbsOutput();
            if (seed.length() == 0)
                seed = getMakeConfiguration().getBaseDir();
            return new ElfEditor(seed);
        }
    }
    
    private class DirEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private PropertyEnv propenv;
        private String seed;
        
        public DirEditor(String seed) {
            this.seed = seed;
        }
        
        @Override
        public void setAsText(String text) {
            getBuildCommandWorkingDir().setValue(text);
        }
        
        @Override
        public String getAsText() {
            return getBuildCommandWorkingDir().getValue();
        }
        
        @Override
        public Object getValue() {
            return getBuildCommandWorkingDir().getValue();
        }
        
        @Override
        public void setValue(Object v) {
            getBuildCommandWorkingDir().setValue((String)v);
        }
        
        @Override
        public boolean supportsCustomEditor() {
            return true;
        }
        
        @Override
        public java.awt.Component getCustomEditor() {
            return new DirPanel(seed, this, propenv);
        }
        
        public void attachEnv(PropertyEnv propenv) {
            this.propenv = propenv;
        }
    }
    
    class DirPanel extends FileChooser implements PropertyChangeListener {
        PropertyEditorSupport editor;
        
        public DirPanel(String seed, PropertyEditorSupport editor, PropertyEnv propenv) {
            super(
                    java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("Run_Directory"),
                    java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("SelectLabel"),
                    FileChooser.DIRECTORIES_ONLY,
                    null,
                    seed,
                    true
                    );
            setControlButtonsAreShown(false);
            
            this.editor = editor;
            
            propenv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            propenv.addPropertyChangeListener(this);
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID) {
                String path = IpeUtils.toRelativePath(makeConfiguration.getBaseDir(), getSelectedFile().getPath()); // FIXUP: not always relative path
                path = FilePathAdaptor.normalize(path);
                editor.setValue(path);
            }
        }
    }
    
    private class ElfEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private PropertyEnv propenv;
        private String seed;
        
        public ElfEditor(String seed) {
            this.seed = seed;
        }
        
        @Override
        public void setAsText(String text) {
            getOutput().setValue(text);
        }
        
        @Override
        public String getAsText() {
            return getOutput().getValue();
        }
        
        @Override
        public Object getValue() {
            return getOutput().getValue();
        }
        
        @Override
        public void setValue(Object v) {
            getOutput().setValue((String)v);
        }
        
        @Override
        public boolean supportsCustomEditor() {
            return true;
        }
        
        @Override
        public java.awt.Component getCustomEditor() {
            return new ElfPanel(seed, this, propenv);
        }
        
        public void attachEnv(PropertyEnv propenv) {
            this.propenv = propenv;
        }
    }
    
    class ElfPanel extends FileChooser implements PropertyChangeListener {
        PropertyEditorSupport editor;
        
        public ElfPanel(String seed, PropertyEditorSupport editor, PropertyEnv propenv) {
            super(
                    "", // NOI18N
                    "", // NOI18N
                    FileChooser.FILES_ONLY,
                    null,
                    seed,
                    true
                    );
            
            setControlButtonsAreShown(false);
            
            if (Utilities.isWindows()) {
                addChoosableFileFilter(PeExecutableFileFilter.getInstance());
                addChoosableFileFilter(PeStaticLibraryFileFilter.getInstance());
                addChoosableFileFilter(PeDynamicLibraryFileFilter.getInstance());
            } else if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                addChoosableFileFilter(MacOSXExecutableFileFilter.getInstance());
                addChoosableFileFilter(ElfStaticLibraryFileFilter.getInstance());
                addChoosableFileFilter(MacOSXDynamicLibraryFileFilter.getInstance());
            } else {
                addChoosableFileFilter(ElfExecutableFileFilter.getInstance());
                addChoosableFileFilter(ElfStaticLibraryFileFilter.getInstance());
                addChoosableFileFilter(ElfDynamicLibraryFileFilter.getInstance());
            }
            setFileFilter(getAcceptAllFileFilter());
            
            this.editor = editor;
            
            propenv.setState(PropertyEnv.STATE_NEEDS_VALIDATION);
            propenv.addPropertyChangeListener(this);
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (PropertyEnv.PROP_STATE.equals(evt.getPropertyName()) && evt.getNewValue() == PropertyEnv.STATE_VALID && getSelectedFile() != null) {
                String path = IpeUtils.toRelativePath(makeConfiguration.getBaseDir(), getSelectedFile().getPath()); // FIXUP: not always relative path
                path = FilePathAdaptor.normalize(path);
                editor.setValue(path);
            }
        }
    }
    
    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(MakefileConfiguration.class);
        }
        return bundle.getString(s);
    }
}
