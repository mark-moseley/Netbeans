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

package org.netbeans.modules.cnd.makeproject.runprofiles;

import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.api.xml.AttrValuePair;
import org.netbeans.modules.cnd.api.xml.VersionException;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoderStream;
import org.netbeans.modules.cnd.makeproject.api.configurations.IntConfiguration;
import org.xml.sax.Attributes;

public class RunProfileXMLCodec extends XMLDecoder implements XMLEncoder {

    private RunProfile profile;

    // was: public RunProfile.PROFILE_ID
    private static final String PROFILE_ID = "runprofile"; // NOI18N

    private final static String VARIABLE_ELEMENT = "variable"; // NOI18N
    private final static String NAME_ATTR = "name"; // NOI18N
    private final static String VALUE_ATTR = "value"; // NOI18N
    private final static String ENVIRONMENT_ELEMENT = "environment"; // NOI18N
    private final static String ARGS_ELEMENT = "args"; // NOI18N
    private final static String RUNDIR_ELEMENT = "rundir"; // NOI18N
    private final static String BUILD_FIRST_ELEMENT = "buildfirst"; // NOI18N
    private final static String CONSOLE_TYPE_ELEMENT = "console-type"; // NOI18N
    private final static String TERMINAL_TYPE_ELEMENT = "terminal-type"; // NOI18N

    public final static String TRUE_VALUE = "true"; // NOI18N
    public final static String FALSE_VALUE = "false"; // NOI18N


    private final static int thisversion = 5;

    public RunProfileXMLCodec(RunProfile profile) {
	this.profile = profile;
    }

    public static int getVersion() {
	return thisversion;
    }

    // interface XMLDecoder
    public String tag() {
	return PROFILE_ID;
    }

    // interface XMLDecoder
    public void start(Attributes atts) throws VersionException {
        String what = "run profile"; // NOI18N
        int maxVersion = getVersion();
        checkVersion(atts, what, maxVersion);
    }

    // interface XMLDecoder
    public void end() {
        profile.clearChanged();
    }

    // interface XMLDecoder
    public void startElement(String element, Attributes atts) {
	if (element.equals(VARIABLE_ELEMENT)) {
	    profile.getEnvironment().
		putenv(atts.getValue(0), atts.getValue(1));
	}
    }

    // interface XMLDecoder
    public void endElement(String element, String currentText) {
	if (element.equals(ARGS_ELEMENT)) {
	    profile.setArgs(currentText);
	}
	else if (element.equals(RUNDIR_ELEMENT)) {
	    profile.setRunDir(currentText);
	}
	else if (element.equals(BUILD_FIRST_ELEMENT)) {
	    profile.setBuildFirst(currentText.equals(TRUE_VALUE));
	}
	else {
            int idx;            
            try {
                idx = Integer.parseInt(currentText);
            } catch (NumberFormatException ex) {
                idx = 0;
    }
            if (element.equals(CONSOLE_TYPE_ELEMENT)) {
                profile.getConsoleType().setValue(idx);
            } else if (element.equals(TERMINAL_TYPE_ELEMENT)) {
                profile.getTerminalType().setValue(idx);
            }
	}
    }


    /*
     * was: part of RunProfileHelper.java.writeEnvironmentBlock
     */

    private static void encode(XMLEncoderStream xes, String[] pair) {
	xes.element(VARIABLE_ELEMENT, 
		    new AttrValuePair[] {
			new AttrValuePair(NAME_ATTR, "" + pair[0]), // NOI18N
			new AttrValuePair(VALUE_ATTR, "" + pair[1]) // NOI18N
		    });
    }


    /*
     * was: RunProfileHelper.java.writeEnvironmentBlock
     */

    private static void encode(XMLEncoderStream xes, Env env) {
	String[][] environment = env.getenvAsPairs();
	xes.elementOpen(ENVIRONMENT_ELEMENT);
	for (int i = 0; i < environment.length; i++) {
	    encode(xes, environment[i]);
	}
	xes.elementClose(ENVIRONMENT_ELEMENT);
    }


    /*
     * was: RunProfileHelper.java.writeProfileBlock
     */

    private static void encode(XMLEncoderStream xes, RunProfile profile) {
	xes.elementOpen(PROFILE_ID, getVersion());
	xes.element(ARGS_ELEMENT, profile.getArgsFlat());
	xes.element(RUNDIR_ELEMENT, profile.getRunDir());
	xes.element(BUILD_FIRST_ELEMENT, "" + profile.getBuildFirst()); // NOI18N
        xes.element(CONSOLE_TYPE_ELEMENT, Integer.toString(profile.getConsoleType().getValue()));
        xes.element(TERMINAL_TYPE_ELEMENT, Integer.toString(profile.getTerminalType().getValue()));
	encode(xes, profile.getEnvironment());
	xes.elementClose(PROFILE_ID);
    }

    // interface XMLEncoder
    public void encode(XMLEncoderStream xes) {
	encode(xes, profile);
    } 
}
