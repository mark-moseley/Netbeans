/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.projectimport.eclipse;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.projectimport.ProjectImporterException;

/**
 * @author Martin Krauskopf
 */
public class PreferredVMParserTest extends ProjectImporterTestCase {
    
    public PreferredVMParserTest(String testName) {
        super(testName);
    }
    
    /** Also test 57661. */
    public void testParse() throws ProjectImporterException {
        String org_eclipse_jdt_launching_PREF_VM_XML =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<vmSettings defaultVM=\"57,org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType13,1135246830946\" defaultVMConnector=\"\">\n" +
                "<vmType id=\"org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType\">\n" +
                "<vm id=\"0\" name=\"jdk-6-beta-bin-b59c\" path=\"/space/java/jdk-6-beta-bin-b59c\"/>\n" +
                "<vm id=\"1135246830946\" name=\"jdk-6-rc-bin-b64\" path=\"/space/java/jdk-6-rc-bin-b64\">\n" +
                "<libraryLocations>\n" +
                "<libraryLocation jreJar=\"/space/java/0_lib/commons-collections-2.1.jar\" jreSrc=\"\" pkgRoot=\"\"/>\n" +
                "<libraryLocation jreJar=\"/space/java/jdk-6-rc-bin-b64/jre/lib/resources.jar\" jreSrc=\"/space/java/jdk-6-rc-bin-b64/src.zip\" pkgRoot=\"\"/>\n" +
                "<libraryLocation jreJar=\"/space/java/jdk-6-rc-bin-b64/jre/lib/rt.jar\" jreSrc=\"/space/java/jdk-6-rc-bin-b64/src.zip\" pkgRoot=\"\"/>\n" +
                "<libraryLocation jreJar=\"/space/java/jdk-6-rc-bin-b64/jre/lib/jsse.jar\" jreSrc=\"/space/java/jdk-6-rc-bin-b64/src.zip\" pkgRoot=\"\"/>\n" +
                "<libraryLocation jreJar=\"/space/java/jdk-6-rc-bin-b64/jre/lib/jce.jar\" jreSrc=\"/space/java/jdk-6-rc-bin-b64/src.zip\" pkgRoot=\"\"/>\n" +
                "<libraryLocation jreJar=\"/space/java/jdk-6-rc-bin-b64/jre/lib/charsets.jar\" jreSrc=\"/space/java/jdk-6-rc-bin-b64/src.zip\" pkgRoot=\"\"/>\n" +
                "<libraryLocation jreJar=\"/space/java/jdk-6-rc-bin-b64/jre/lib/ext/sunjce_provider.jar\" jreSrc=\"\" pkgRoot=\"\"/>\n" +
                "<libraryLocation jreJar=\"/space/java/jdk-6-rc-bin-b64/jre/lib/ext/sunpkcs11.jar\" jreSrc=\"\" pkgRoot=\"\"/>\n" +
                "<libraryLocation jreJar=\"/space/java/jdk-6-rc-bin-b64/jre/lib/ext/dnsns.jar\" jreSrc=\"\" pkgRoot=\"\"/>\n" +
                "<libraryLocation jreJar=\"/space/java/jdk-6-rc-bin-b64/jre/lib/ext/localedata.jar\" jreSrc=\"\" pkgRoot=\"\"/>\n" +
                "</libraryLocations>\n" +
                "</vm>\n" +
                "</vmType>\n" +
                "</vmSettings>\n";
        
        Map jdks = PreferredVMParser.parse(org_eclipse_jdt_launching_PREF_VM_XML);
        
        Map expectedJDKs = new HashMap();
        expectedJDKs.put("jdk-6-rc-bin-b64", "/space/java/jdk-6-rc-bin-b64");
        expectedJDKs.put("org.eclipse.jdt.launching.JRE_CONTAINER", "/space/java/jdk-6-rc-bin-b64");
        expectedJDKs.put("jdk-6-beta-bin-b59c", "/space/java/jdk-6-beta-bin-b59c");
        
        assertEquals("JDKs were successfully parsed", expectedJDKs, jdks);
    }
    
}
