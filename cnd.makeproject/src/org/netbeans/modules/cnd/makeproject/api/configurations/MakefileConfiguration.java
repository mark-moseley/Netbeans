/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.File;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.cnd.api.utils.ElfExecutableFileFilter;
import org.netbeans.modules.cnd.api.utils.FileChooser;
import org.netbeans.modules.cnd.makeproject.configurations.ui.StringNodeProp;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.ui.utils.ElfDynamicLibraryFileFilter;
import org.netbeans.modules.cnd.makeproject.ui.utils.ElfStaticLibraryFileFilter;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.nodes.Sheet;

public class MakefileConfiguration {
    private MakeConfiguration makeConfiguration;

    private StringConfiguration buildCommandWorkingDir;
    private StringConfiguration buildCommand;
    private StringConfiguration cleanCommand;
    private StringConfiguration output;

    // Constructors
    public MakefileConfiguration(MakeConfiguration makeConfiguration) {
	this.makeConfiguration = makeConfiguration;
	buildCommandWorkingDir = new StringConfiguration(null, "."); // NOI18N
	buildCommand = new StringConfiguration(null, "${MAKE} -f Makefile"); // NOI18N
	cleanCommand = new StringConfiguration(null, "${MAKE} -f Makefile clean"); // NOI18N
	output = new StringConfiguration(null, ""); // NOI18N
    }

    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration MakeConfiguration) {
	this.makeConfiguration = makeConfiguration;
    }
    public MakeConfiguration getMakeConfiguration() {
	return makeConfiguration;
    }

    // Working Dir
    public StringConfiguration getBuildCommandWorkingDir() {
	return buildCommandWorkingDir;
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
	if (getBuildCommandWorkingDir().getValue().length() > 0 && IpeUtils.isPathAbsolute(getBuildCommandWorkingDir().getValue())) 
	    return getBuildCommandWorkingDir().getValue();
	else
	    return getMakeConfiguration().getBaseDir() + "/" + getBuildCommandWorkingDir().getValue();
    }

    public boolean canClean() {
	return getCleanCommand().getValue().length() > 0;
    }

    public String getAbsOutput() {
	if (getOutput().getValue().length() > 0 && IpeUtils.isPathAbsolute(getOutput().getValue()))
	    return getOutput().getValue();
	else
	    return getMakeConfiguration().getBaseDir() + "/"  + getOutput().getValue();
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
	set.setName("Makefile");
	set.setDisplayName("Makefile");
	set.setShortDescription("MakefileDefaults");
	set.put(new DirStringNodeProp(getBuildCommandWorkingDir(), "WorkingDirectory", "Working Directory", "Working Directory"));
	set.put(new StringNodeProp(getBuildCommand(), "BuildCommand Line", "Build Command Line", "Build Command Line"));
	set.put(new StringNodeProp(getCleanCommand(),  "CleanCommand Line", "Clean Command Line", "Clean Command Line"));
	set.put(new OutputStringNodeProp(getOutput(), "Output", "Output", "Output"));
	sheet.put(set);

	return sheet;
    }

    private class DirStringNodeProp extends StringNodeProp {
	public DirStringNodeProp(StringConfiguration stringConfiguration, String txt1, String txt2, String txt3) {
	    super(stringConfiguration, txt1, txt2, txt3);
	}

	public void setValue(Object v) {
            String path = IpeUtils.toRelativePath(getMakeConfiguration().getBaseDir(), (String)v); // FIXUP: not always relative path
            path = FilePathAdaptor.normalize(path);
	    super.setValue(path);
	}

	public PropertyEditor getPropertyEditor() {
	    return new DirEditor(getAbsBuildCommandWorkingDir());
	}
    }

    private class OutputStringNodeProp extends StringNodeProp {
	public OutputStringNodeProp(StringConfiguration stringConfiguration, String txt1, String txt2, String txt3) {
	    super(stringConfiguration, txt1, txt2, txt3);
	}

	public void setValue(Object v) {
            String path = IpeUtils.toRelativePath(getMakeConfiguration().getBaseDir(), (String)v); // FIXUP: not always relative path
            path = FilePathAdaptor.normalize(path);
	    super.setValue(path);
	}

	public PropertyEditor getPropertyEditor() {
	    return new ElfEditor(getAbsOutput());
	}
    }

    private class DirEditor extends PropertyEditorSupport implements ExPropertyEditor {
        private PropertyEnv propenv;
        private String seed;
        
        public DirEditor(String seed) {
            this.seed = seed;
        }
        
        public void setAsText(String text) {
            getBuildCommandWorkingDir().setValue(text);
        }
        
        public String getAsText() {
            return getBuildCommandWorkingDir().getValue();
        }
        
        public Object getValue() {
            return getBuildCommandWorkingDir().getValue();
        }
        
        public void setValue(Object v) {
            getBuildCommandWorkingDir().setValue((String)v);
        }
        
        public boolean supportsCustomEditor() {
            return true;
        }
        
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
        
        public void setAsText(String text) {
            getOutput().setValue(text);
        }
        
        public String getAsText() {
            return getOutput().getValue();
        }
        
        public Object getValue() {
            return getOutput().getValue();
        }
        
        public void setValue(Object v) {
            getOutput().setValue((String)v);
        }
        
        public boolean supportsCustomEditor() {
            return true;
        }
        
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
		java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("Elf_File"),
		java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/api/Bundle").getString("SelectLabel"),
		FileChooser.FILES_ONLY,
		new FileFilter[] {ElfExecutableFileFilter.getInstance(), ElfStaticLibraryFileFilter.getInstance(), ElfDynamicLibraryFileFilter.getInstance()},
		seed,
		true
		);
            setControlButtonsAreShown(false);
            setFileFilter(getAcceptAllFileFilter());
            
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
}
