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

import java.io.File;
import java.util.ArrayList;
import org.netbeans.modules.cnd.api.utils.IpeUtils;

/**
 *  Create a panel used for gathering the binary name and output directory for
 *  the simple application cases (all Makefile types other than complex).
 */

public class TargetNamePanel extends ItemChooser {

    /** Current name for the target label */
    private String label;

    /** Current mnemonic for the target label */
    private char mnemonic;

    /** Default target name for the current type */
    private String tname;

    /** Serial version number */
    static final long serialVersionUID = 6653452210904639697L;

    private boolean	initialized = false;


    /**
     *  Constructor for the Makefile binary panel.
     */
    TargetNamePanel(MakefileWizard wd) {
	super(wd, true);
	String subtitle = getString("TITLE_TargetName"); // NOI18N
	setSubTitle(subtitle);
	this.getAccessibleContext().setAccessibleDescription(subtitle);
    }


    /** Validate the binary name and output directory */
    public void validateData(ArrayList msgs, int key) {
	TargetData target = (TargetData) getMakefileData().getTarget(key);
	String cwd = getMakefileData().getBaseDirectory(MakefileData.EXPAND);
	File bindir = null;
	File outdir = null;
	File outpar = null;
	String bname = IpeUtils.expandPath(target.getName());
	String odir = IpeUtils.expandPath(target.getOutputDirectory());

	if (bname.length() > 0) {
	    File btmp;

	    if (bname.startsWith(File.separator)) {
		btmp = new File(bname);
	    } else {
		btmp = new File(cwd, bname);
	    }
	    bindir = btmp.getParentFile();
	}
	if (odir.length() > 0 && !odir.equals(cwd) && !odir.equals(bname)) {
	    if (odir.startsWith(File.separator)) {
		outdir = new File(odir);
	    } else {
		outdir = new File(cwd, odir);
	    }
	    outpar = outdir.getParentFile();
	}

	if (bindir != null && !bindir.getPath().equals(cwd) &&
			    bindir.exists() && !bindir.canWrite()) {
	    warn(msgs, WARN_BINDIR_NOT_WRITABLE, bindir.getPath(), bname);
	}
	if (outdir != null && !outdir.equals(bindir)) {
	    if (!outdir.exists()) {
		if (outpar == null || !outpar.canWrite()) {
		    warn(msgs, WARN_CANNOT_CREATE_OUTPUT_DIR, outdir.getPath());
		}
	    } else if (!outdir.canWrite()) {
		warn(msgs, WARN_CANNOT_WRITE_TO_OUTPUT_DIR, outdir.getPath());
	    }
	}
    }


    /**
     *  Convert a target from one type to another. If we cannot convert the
     *  target we move all existing targets 1 up in the array and create a new
     *  target in the 0th position.
     *
     *  @param type	The desired target type
     *  @param tlist	The ArrayList of existing targets
     *  @return		The ``type'' target
     */
    private TargetData convertOrCreate(int type, ArrayList tlist) {
	TargetData target = (TargetData) tlist.get(0);

	if (target.getTargetType() != type) {
	    for (int i = 0; i < tlist.size(); i++) {
		target = (TargetData) tlist.get(i);
		if (target.isConvertable(type)) {
		    // Convert target and move to 0th position
		    target.convert(type);
		    if (i > 0) {
			while (i > 0) {
			    tlist.set(i, tlist.get(i - 1));
			    i--;
			}
			tlist.set(0, target);
		    }
		    setup(type, true);
		    return target;
		}
	    }

	    /*
	     * We didn't find a target we could convert to the desired type. 
	     * Move all targets up an index and create a new target at index 0.
	     */
	    tlist.add(1, tlist.get(0));
	    tlist.add(0, (target = createTarget(type)));
	}

	return target;
    }

    private void setup(int type, boolean reinit) {

	if (type == MakefileData.EXECUTABLE_MAKEFILE_TYPE) {
	    label = getString("LBL_ExecutableName");			// NOI18N
	    mnemonic = getString("MNEM_ExecutableName").charAt(0);	// NOI18N
	    tname = getString("DFLT_ExecutableName");			// NOI18N

	} else if (type == MakefileData.ARCHIVE_MAKEFILE_TYPE) {
	    label = getString("LBL_ArchiveName");			// NOI18N
	    mnemonic = getString("MNEM_ArchiveName").charAt(0);		// NOI18N
	    tname = getString("DFLT_ArchiveName");	    		// NOI18N

	} else if (type == MakefileData.SHAREDLIB_MAKEFILE_TYPE) {
	    label = getString("LBL_ShobjName");				// NOI18N
	    mnemonic = getString("MNEM_ShobjName").charAt(0);		// NOI18N
	    tname = getString("DFLT_ShobjName");			// NOI18N

	} else {
	    throw(new IllegalStateException());
	}

	if (reinit) {
	    getNameLabel().setText(label);
	    getNameLabel().setDisplayedMnemonic(mnemonic);
	}
    }

    private TargetData createTarget(int type) {
	setup(type, true);
	String dir = getMakefileData().defaultOutputDirectory();
	return new TargetData(type, tname, dir, 0);
    }

    /**
     *  Create or convert the target to the correct type. If it is the correct
     *  type then reuse it unchanged. Set initial values for the panel too.
     */
    public void addNotify() {
	int type = getMakefileData().getMakefileType();
	TargetData target;

	if (!initialized) {
	    setup(type, false);
	    create(getString("LBL_TargetDirectory"),			// NOI18N
			getString("MNEM_TargetDirectory").charAt(0),	// NOI18N
			label, mnemonic);
	    target = createTarget(type);
	    target.setTargetName(tname);
	    getMakefileData().getTargetList().add(target);

	    initialized = true;
	} else {
	    ArrayList tlist = getMakefileData().getTargetList();

	    target = (TargetData) tlist.get(0);
	    if ((target.getTargetType() != type)) {
		target = convertOrCreate(type, tlist);
	    }
	}

	// init makefile directory and makefile name here. It used to be done in BaseDirectoryPanel, but
	// this panel is no longer part of the simple wizard
	MakefileWizard.getMakefileWizard().initDirPaths();
	MakefileWizard.getMakefileWizard().initMakefileName();

	String dir = target.getOutputDirectory();
	int pos;

	getText().setText(target.getTargetName());
	if ((pos = dir.lastIndexOf('/')) == -1) {
	    getDirText().setText(getMakefileData().getBaseDirectory() +
			    File.separator + target.getOutputDirectory());
	} else {
	    getDirText().setText(dir);
	}

	super.addNotify();
    }


    /** Get the data from the panel and update the target */
    public void removeNotify() {
	super.removeNotify();

	MakefileData md = getMakefileData();
	TargetData target = (TargetData) md.getTargetList().get(0);
	String name = getText().getText();
	String dir = getDirText().getText();
	String base = md.getBaseDirectory();

	try {
	    if (dir.startsWith(base) && (dir.length() == base.length() ||
				dir.charAt(base.length()) == '/')) {
		if (!dir.substring(base.length() + 1).equals(
				target.getOutputDirectory())) {
		    target.setOutputDirectory(dir.substring(base.length() + 1));
		}
	    } else {
		target.setOutputDirectory(dir);
	    }
	} catch (IndexOutOfBoundsException ex) {
	    target.setOutputDirectory(dir);
	}

	if (!name.equals(target.getTargetName())) {
	    target.setTargetName(name);
	    target.setName(name);
	}
    }
}

