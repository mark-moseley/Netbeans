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

package  org.netbeans.modules.cnd.makewizard;

import java.util.ResourceBundle;
import org.openide.util.NbBundle;

/** The Standard Libraries flags are stored here */
public final class StdLibFlags {

    public StdLib motif= new StdLib(getString("TB_Motif"), getString("MNEM_Motif").charAt(0), "-lXm -lXt -lXext -lX11");
    public StdLib math = new StdLib(getString("TB_Math"), getString("MNEM_Math").charAt(0), "-lm");
    public StdLib socketnsl = new StdLib(getString("TB_Nsl"), getString("MNEM_Nsl").charAt(0), "-lsocket -lnsl");
    public StdLib solthread = new StdLib(getString("TB_SolThreads"), getString("MNEM_SolThreads").charAt(0), "-lthread");
    public StdLib posixthread = new StdLib(getString("TB_PosixThreads"), getString("MNEM_PosixThreads").charAt(0), "-lpthread");
    public StdLib posix4 = new StdLib(getString("TB_Posix4"), getString("MNEM_Posix4").charAt(0), "-lposix4");
    public StdLib i18n = new StdLib(getString("TB_I18n"), getString("MNEM_I18n").charAt(0), "-lintl");
    public StdLib genlib = new StdLib(getString("TB_GenLib"), getString("MNEM_GenLib").charAt(0), "-lm");
    public StdLib dynamiclib = new StdLib(getString("TB_DynamicLib"), getString("MNEM_DynamicLib").charAt(0), "-ldl");
    public StdLib curses = new StdLib(getString("TB_Curses"), getString("MNEM_Curses").charAt(0), "-lcurses");
    public StdLib rwtools = new StdLib(getString("TB_RWTools"), getString("MNEM_RWTools").charAt(0), "-library=rwtools7,iostream");
    public StdLib perflib = new StdLib(getString("TB_PerfLib"), getString("MNEM_PerfLib").charAt(0), "-xlic_lib=sunperf");

    private StdLib[] solarisStdLibs = {
	motif,
	perflib,
	math,
	socketnsl,
	solthread,
	posixthread,
	posix4,
	i18n,
	genlib,
	//dynamiclib,
	curses,
	rwtools,
    };

    private StdLib[] linuxStdLibs = {
	motif,
	math,
	posixthread,
	dynamiclib,
	curses,
    };

    /** Determine the link type */
    private int linkType;
    public final static int STATIC_LINK_TYPE = 0;
    public final static int DYNAMIC_LINK_TYPE = 1;


    /** Create a new StdLibFlags and initialize */
    public StdLibFlags() {
	linkType = DYNAMIC_LINK_TYPE;
    }

    /** Construct a StdLibFlags from an existing one */
    public StdLibFlags(StdLibFlags old) {
	this.motif = new StdLib(old.motif);
	this.math = new StdLib(old.math);
	this.socketnsl = new StdLib(old.socketnsl);
	this.solthread = new StdLib(old.solthread);
	this.posixthread = new StdLib(old.posixthread);
	this.posix4 = new StdLib(old.posix4);
	this.i18n = new StdLib(old.i18n);
	this.genlib = new StdLib(old.genlib);
	this.dynamiclib = new StdLib(old.dynamiclib);
	this.curses = new StdLib(old.curses);
	this.rwtools = new StdLib(old.rwtools);
	this.perflib = new StdLib(old.perflib);
    }


    public StdLib[] getSolarisStdLibs() {
	return solarisStdLibs;
    }

    public StdLib[] getLinuxStdLibs() {
	return linuxStdLibs;
    }

    /** Getter for the boolean Motif library flag */
    public boolean isMotifLibs() {
	return motif.isUsed();
    }

    /** Setter for the boolean Motif library flag */
    public void setMotifLibs(boolean motifLibs) {
	motif.setUsed(motifLibs);
    }

    /** Getter for the link type library flag */
    public int getLinkType() {
	return linkType;
    }

    /** Setter for the boolean link type library flag */
    public void setLinkType(int linkType) {
	if (linkType == STATIC_LINK_TYPE || linkType == DYNAMIC_LINK_TYPE) {
	    this.linkType = linkType;
	}
    }


    public String getSysLibFlags(int toolset, int os, boolean is64Bit, TargetData t) {
	StringBuffer buf = new StringBuffer(1024);

	if (getLinkType() == StdLibFlags.DYNAMIC_LINK_TYPE) {
	    if (toolset == MakefileData.SUN_TOOLSET_TYPE) {
		//buf.append("-Bdynamic ");					//NOI18N
		; // nothing
	    }
	    else {
		; // nothing
	    }
	} else if (getLinkType() == StdLibFlags.STATIC_LINK_TYPE) {
	    if (toolset == MakefileData.SUN_TOOLSET_TYPE) {
		buf.append("-Bstatic ");					//NOI18N
	    }
	    else {
		buf.append("-static ");					//NOI18N
	    }
	}

	// X-Designer will supply these libraries another way
	if (isMotifLibs() && !t.containsXdFiles()) {
	    if (os == MakefileData.SOLARIS_OS_TYPE) {
		if (is64Bit) {
		    buf.append("-L/usr/openwin/lib/sparcv9 ");		//NOI18N
		    buf.append("-L/usr/dt/lib/sparcv9 ");		 	//NOI18N
		    buf.append("-R/usr/openwin/lib/sparcv9 ");		//NOI18N
		    buf.append("-R/usr/dt/lib/sparcv9 ");		 	//NOI18N
		} else {
		    buf.append("-L/usr/openwin/lib -L/usr/dt/lib ");	//NOI18N
		    buf.append("-R/usr/openwin/lib -R/usr/dt/lib ");	//NOI18N
		}
	    }
	    else if (os == MakefileData.LINUX_OS_TYPE) {
		if (is64Bit) {
		    // ???
		    buf.append("-L/usr/X11R6/lib ");	//NOI18N
		}
		else {
		    buf.append("-L/usr/X11R6/lib ");	//NOI18N
		}
	    }
	    else {
		; // FIXUP - error
	    }
	}

	StdLib[] stdLibs;
	if (os == MakefileData.SOLARIS_OS_TYPE) {
	    stdLibs = getSolarisStdLibs();
	}
	else {
	    stdLibs = getLinuxStdLibs();
	}

	// Unset certain libs if x-designer
	if (t.containsXdFiles()) {
	    motif.setUsed(false);
	    socketnsl.setUsed(false);
	    genlib.setUsed(false);
	}

	for (int i = 0; i < stdLibs.length; i++) {
	    if (stdLibs[i].isUsed()) {
		buf.append(stdLibs[i].getCmd());
		buf.append(" "); // NOI18N
	    }
	}

	return buf.toString();
    }


    private String indent = new String("    ");				//NOI18N

    /** Default dump has no indent */
    protected void dump() {
	println("    stdLibFlags = {");					//NOI18N
	println("    }\n");						//NOI18N
    }


    /**
     *  Allow caller to indent all data. This is usefull for indenting target
     *  dumps within MakefileData dumps.
     */
    public void dump(String in) {
	setIndent(in);
	dump();
    }

    private void println(String s) {
	System.out.println(indent + s);
    }

    private void setIndent(String indent) {
	this.indent = indent;
    }

    private ResourceBundle bundle = null;
    private String getString(String s) {
	if (bundle == null) {
            bundle = NbBundle.getBundle(MakefileWizardPanel.class);
	}
        return bundle.getString(s);
    }

}
