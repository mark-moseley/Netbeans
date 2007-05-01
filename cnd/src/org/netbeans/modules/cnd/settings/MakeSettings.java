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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.settings;

import java.beans.PropertyEditorManager;
import java.io.File;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.builds.ErrorExpression;
import org.netbeans.modules.cnd.builds.ErrorExpressionEditor;
import org.netbeans.modules.cnd.execution41.org.openide.execution.Executor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Settings for the C/C++/Fortran Module. The compile/build options stored
 * in this class are <B>default</B> options which will be applied to new files.
 * Once a file has been created its options may diverge from the defaults. A
 * files options do not change if these default options are changed.
 *
 */

public class MakeSettings extends SystemOption {

    /** serial uid */
    static final long serialVersionUID = 1276277545941336641L;

    public static final String PROP_DEFAULT_BUILD_DIR	= "defaultBuildDirectory"; //NOI18N
    public static final String PROP_DEFAULT_MAKE_COMMAND = "defaultMakeCommand"; //NOI18N
    public static final String PROP_ERROR_EXPRESSION = "errorExpression"; //NOI18N
    public static final String PROP_EXECUTOR	    = "executor";	//NOI18N
    public static final String PROP_REUSE_OUTPUT    = "reuseOutput";	//NOI18N
    public static final String PROP_SAVE_ALL	    = "saveAll";	//NOI18N

    public static final ErrorExpression SUN_COMPILERS;
    public static final ErrorExpression GNU_COMPILERS;

    /** The resource bundle for the form editor */
    private static ResourceBundle bundle;

    static {
	SUN_COMPILERS = new ErrorExpression(
			    getString("LBL_SunErrorName"),             //NOI18N
			    getString("CTL_SunErrorRE"), 1, 2, -1, 3); //NOI18N
	GNU_COMPILERS = new ErrorExpression(
			    getString("LBL_GnuErrorName"),             //NOI18N
			    getString("CTL_GnuErrorRE"), 1, 2, -1, 3); //NOI18N
    }


    /**
     *  Initialize each property.
     */
    protected void initialize() {

	super.initialize();
	registerPropertyEditors();

	setDefaultMakeCommand(getString("DEFAULT_MAKE_COMMAND_VALUE")); // NOI18N
	setReuseOutput(false);
	setSaveAll(true);

	// Define and set the initial ErrorExpressions
	if (System.getProperty("os.name", "").toLowerCase().indexOf("sunos") >= 0)
	    setErrorExpression(SUN_COMPILERS);
	else
	    setErrorExpression(GNU_COMPILERS);
    }


    /** 
     *  Get the display name.
     *
     *  @return value of OPTION_MAKE_SETTINGS_NAME
     */
    public String displayName () {
	return getString("OPTION_MAKE_SETTINGS_NAME");	    	//NOI18N
    }


    /**
     *  Return the singleton instance. Instantiate it if necessary.
     */
    public static MakeSettings getDefault() {
	return (MakeSettings) findObject(MakeSettings.class, true);
    }


    /** @return localized string */
    static String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(MakeSettings.class);
	}
	return bundle.getString(s);
    }


    private void registerPropertyEditors() {
	String[] searchPath = PropertyEditorManager.getEditorSearchPath();
	String[] newSP = new String[searchPath.length + 1];

	for (int i = 0; i < searchPath.length; i++) {
	    newSP[i] = searchPath[i];
	}

	newSP[searchPath.length] = "org.netbeans.modules.cnd.builds"; // NOI18N
	PropertyEditorManager.setEditorSearchPath(newSP);
	PropertyEditorManager.registerEditor(
		ErrorExpression.class, ErrorExpressionEditor.class);
    }
	

    /**
     *  Getter for the default build directory. This should be a relative path
     *  from the filesystem of the current
     *  {@link org.openide.filesystems.FileObject#FileObject() FileObject}.
     *
     *  @return the default build directory
     */
    public String getDefaultBuildDirectory() {
        String dir = (String) getProperty(PROP_DEFAULT_BUILD_DIR);
        if (dir == null) {
            return "."; // NOI18N
        } else {
            return dir;
        }
    }


    /**
     *  Set the default build directory. This cannot be an absolute path.
     *
     *  @param path Relative path to the build directory
     */
    public void setDefaultBuildDirectory(String dir) {
	if (!dir.startsWith(File.separator)) {
	    String odir = getDefaultBuildDirectory();
	    if (!odir.equals(dir)) {
		putProperty(PROP_DEFAULT_BUILD_DIR, dir, true);
	    }
	} else {
	    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
				    getString("MSG_RelBuildPath")));    //NOI18N
	}
    }


    /**
     *  Getter for the default make(1) program.
     *
     *  @return the default name
     */
    public String getDefaultMakeCommand() {
	return (String) getProperty(PROP_DEFAULT_MAKE_COMMAND);
    }


    /**
     *  Set the default make command. This can be either a simple name or
     *  a path name to a specific make program.
     *
     *  @param make name or path of the desired make program 
     */
    public void setDefaultMakeCommand(String make) {
	putProperty(PROP_DEFAULT_MAKE_COMMAND, make);
    }


    /** @return Error Expression */
    public ErrorExpression getErrorExpression() {
	return (ErrorExpression) getProperty(PROP_ERROR_EXPRESSION);
    }


    /** Setter for PROP_ERROR_EXPRESSION */
    public void setErrorExpression(ErrorExpression err) {
	putProperty(PROP_ERROR_EXPRESSION, err);
    }

    /** If true, Ant Execution uses always the same Output tab. */
    public boolean getReuseOutput() {
        return ((Boolean) getProperty(PROP_REUSE_OUTPUT)).booleanValue();
    }

    /** Sets the reuseOutput property. */
    public void setReuseOutput(boolean b) {
        putProperty(PROP_REUSE_OUTPUT, b ? Boolean.TRUE : Boolean.FALSE, true);
    }

    /** Getter for the SaveAll property */
    public boolean getSaveAll () {
        return ((Boolean) getProperty(PROP_SAVE_ALL)).booleanValue();
    }
    
    /** Setter for the SaveAll property */
    public void setSaveAll (boolean sa) {
        putProperty(PROP_SAVE_ALL, sa ? Boolean.TRUE : Boolean.FALSE, true);
    }

    public HelpCtx getHelpCtx() {
	return new HelpCtx("Welcome_opt_building_make");  //NOI18N
    }
}
