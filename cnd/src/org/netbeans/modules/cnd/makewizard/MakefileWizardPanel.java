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

package  org.netbeans.modules.cnd.makewizard;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

public abstract class MakefileWizardPanel extends JPanel {

    /** Serial version number */
    static final long serialVersionUID = -7158070292016837684L;

    /*
     * Default preferred width of the panel - should be the same for
     * all panels within one wizard.
     */ 
    protected static final int DEFAULT_WIDTH = 400;

    /* Default preferred height of the panel - should be the same for
     * all panels within one wizard.
     */ 
    protected static final int DEFAULT_HEIGHT = 325;

    private Vector listvec;

    /** The MakefileWizard controlling this panel */
    private MakefileWizard wd;

    /** Show up to this many items in validating lists */
    protected final static int MAX_ITEMS_TO_SHOW = 5;

    protected final static String WARN_CWD_NOT_DIR;
    protected final static String WARN_CWD_DOES_NOT_EXIST;
    protected final static String WARN_MAKEFILE_NOT_READABLE;
    protected final static String WARN_MAKEFILE_NOT_WRITABLE;
    protected final static String WARN_PARENT_DOES_NOT_EXIST;
    protected final static String WARN_PARENT_NOT_WRITABLE;
    protected final static String WARN_BINDIR_DOES_NOT_EXIST;
    protected final static String WARN_BINDIR_NOT_WRITABLE;
    protected final static String WARN_CANNOT_CREATE_OUTPUT_DIR;
    protected final static String WARN_CANNOT_WRITE_TO_OUTPUT_DIR;
    protected final static String WARN_GUIDIR_NOT_WRITABLE;
    protected final static String WARN_NO_SRC_FILES;
    protected final static String WARN_ABSPATH_SRC_COUNT;
    protected final static String WARN_HDR_SRC_COUNT;
    protected final static String WARN_DNE_FILES;
    protected final static String WARN_DNE_COUNT;
    protected final static String WARN_NO_INC_DIRS;
    protected final static String WARN_DNE_INCDIR;
    protected final static String WARN_DNE_INCDIR_COUNT;
    protected final static String WARN_INC_NOT_DIR;
    protected final static String WARN_INC_NOT_DIR_COUNT;
    protected final static String WARN_SUBDIR_DOES_NOT_EXIST;
    protected final static String WARN_SUBDIR_NOT_WRITABLE;
    protected final static String WARN_INFINITE_RECURSION;
    protected final static String WARN_INVALID_MAKEFLAGS;
    protected final static String WARN_EXTRA_LINES_IN_TARGET;
    protected final static String WARN_INVALID_LINES_IN_TARGET;
    protected final static String WARN_NONCOMPAT_NCT_OPTION;

    static {
	WARN_CWD_NOT_DIR = "WARN_CWD_NOT_DIR";				// NOI18N
	WARN_CWD_DOES_NOT_EXIST = "WARN_CWD_DOES_NOT_EXIST";		// NOI18N
	WARN_MAKEFILE_NOT_READABLE = "WARN_MAKEFILE_NOT_READABLE";	// NOI18N
	WARN_MAKEFILE_NOT_WRITABLE = "WARN_MAKEFILE_NOT_WRITABLE";	// NOI18N
	WARN_PARENT_DOES_NOT_EXIST = "WARN_PARENT_DOES_NOT_EXIST";	// NOI18N
	WARN_PARENT_NOT_WRITABLE = "WARN_PARENT_NOT_WRITABLE";		// NOI18N
	WARN_BINDIR_DOES_NOT_EXIST = "WARN_BINDIR_DOES_NOT_EXIST";	// NOI18N
	WARN_BINDIR_NOT_WRITABLE = "WARN_BINDIR_NOT_WRITABLE";		// NOI18N
	WARN_CANNOT_CREATE_OUTPUT_DIR = "WARN_CANNOT_CREATE_OUTPUT_DIR";// NOI18N
	WARN_CANNOT_WRITE_TO_OUTPUT_DIR =
			"WARN_CANNOT_WRITE_TO_OUTPUT_DIR";		// NOI18N
	WARN_GUIDIR_NOT_WRITABLE = "WARN_GUIDIR_NOT_WRITABLE";		// NOI18N
	WARN_NO_SRC_FILES = "WARN_NO_SRC_FILES";			// NOI18N
	WARN_ABSPATH_SRC_COUNT = "WARN_ABSPATH_SRC_COUNT";		// NOI18N
	WARN_HDR_SRC_COUNT = "WARN_HDR_SRC_COUNT";			// NOI18N
	WARN_DNE_FILES = "WARN_DNE_FILES";				// NOI18N
	WARN_DNE_COUNT = "WARN_DNE_COUNT";				// NOI18N
	WARN_NO_INC_DIRS = "WARN_NO_INC_DIRS";				// NOI18N
	WARN_DNE_INCDIR = "WARN_DNE_INCDIR";				// NOI18N
	WARN_DNE_INCDIR_COUNT = "WARN_DNE_INCDIR_COUNT";		// NOI18N
	WARN_INC_NOT_DIR = "WARN_INC_NOT_DIR";				// NOI18N
	WARN_INC_NOT_DIR_COUNT = "WARN_INC_NOT_DIR_COUNT";		// NOI18N
	WARN_SUBDIR_DOES_NOT_EXIST = "WARN_SUBDIR_DOES_NOT_EXIST";	// NOI18N
	WARN_SUBDIR_NOT_WRITABLE = "WARN_SUBDIR_NOT_WRITABLE";		// NOI18N
	WARN_INFINITE_RECURSION = "WARN_INFINITE_RECURSION";		// NOI18N
	WARN_INVALID_MAKEFLAGS = "WARN_INVALID_MAKEFLAGS";		// NOI18N
	WARN_EXTRA_LINES_IN_TARGET = "WARN_EXTRA_LINES_IN_TARGET";	// NOI18N
	WARN_INVALID_LINES_IN_TARGET = "WARN_INVALID_LINES_IN_TARGET";	// NOI18N
	WARN_NONCOMPAT_NCT_OPTION = "WARN_NONCOMPAT_NCT_OPTION";	// NOI18N
    }


    // for strings...
    private ResourceBundle	bundle;

    MakefileWizardPanel(MakefileWizard wd) {
	bundle = NbBundle.getBundle(MakefileWizardPanel.class);
	this.wd = wd;
	putClientProperty("WizardPanel_leftDimension", getLeftDimension());  // NOI18N
    }


    /** @return preferred size of the wizard panel - it should be the
     * same for all panels within one Wizard so that the wizard dialog
     * does not change its size when switching between panels */
    public Dimension getPreferredSize() {
	return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }


    /** Get the the subtitle of this panel */
    public String getSubTitle() {
	return getName();
    }


    /** Set the subtitle of this panel */
    public void setSubTitle(String subtitle) {
	setName(subtitle);
    }


    /** Get the MakefileData of this panel */
    protected MakefileData getMakefileData() {
	return wd.getMakefileData();
    }

    /** Get the TemplateWizard of this panel */
    protected TemplateWizard getTemplateWizard() {
	return wd.getTemplateWizard();
    }


    /** Used by panels which don't use removeNotify to update their data */
    public void removeNotify() {
	super.removeNotify();
    }


    /**
     *  Default validation method for derived classes which do not need to
     *  write their own validation method.
     */
    public void validateData(ArrayList msgs, int key) {
    }


    /**
     *  Put together the proper warning int he msgs ArrayList
     */
    protected void warn(ArrayList msgs, String prop) {
	msgs.add(NbBundle.getMessage(getClass(),
			    prop, new String("    ")));			// NOI18N
    }


    /**
     *  Put together the proper warning int he msgs ArrayList
     */
    protected void warn(ArrayList msgs, String prop, String arg1) {
	msgs.add(NbBundle.getMessage(getClass(),
			    prop, new String("    "), arg1));		// NOI18N
    }


    /**
     *  Put together the proper warning int he msgs ArrayList
     */
    protected void warn(ArrayList msgs, String prop, String arg1, String arg2) {
	msgs.add(NbBundle.getMessage(getClass(),
			    prop, new String("    "), arg1, arg2));	// NOI18N
    }


    /** Make sure the Steps panel is wide enough */
    private Dimension getLeftDimension() {
	return new Dimension(220, 233);
    }

  
    /**
     *  Fire a {@link PropertyChangeEvent} to each listener.
     *  @param propertyName the programmatic name of the property that
     *  was changed 
     *  @param oldValue the old value of the property
     *  @param newValue the new value of the property
     */
    protected void fireChange() {
	Vector vecclone = (Vector)listvec.clone();
	Enumeration en = vecclone.elements();
	ChangeEvent evt = new ChangeEvent(this);
	while(en.hasMoreElements()) {
	    ChangeListener elist = (ChangeListener) en.nextElement();
	    elist.stateChanged(evt);
	}
    }

    /** Helper method for getting a string from a bundle */
    protected String getString(String s) {
	return bundle.getString(s);
    }

    /**
     *  The default validation method. Most panels don't do validation so don't
     *  need to override this.
     */
    public boolean isPanelValid() { 
	return true;
    }
}
