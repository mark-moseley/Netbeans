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
package org.netbeans.modules.cnd.gizmo.options;

import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.api.xml.AttrValuePair;
import org.netbeans.modules.cnd.api.xml.VersionException;
import org.netbeans.modules.cnd.api.xml.XMLDecoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoder;
import org.netbeans.modules.cnd.api.xml.XMLEncoderStream;
import org.xml.sax.Attributes;

public class GizmoOptionsXMLCodec extends XMLDecoder implements XMLEncoder {
    private GizmoOptions gizmoOptions;

    private final static String PROFILE_ON_RUN_ELEMENT = "profileOnRun"; // NOI18N
    private final static String CPU_ELEMENT = "cpu"; // NOI18N
    private final static String MEMORY_ELEMENT = "memory"; // NOI18N
    private final static String SYNCHRONIZATION_ELEMENT = "synchronization"; // NOI18N
    private final static String DATA_PROVIDER_ELEMENT = "dataprovider"; // NOI18N

    public final static String TRUE_VALUE = "true"; // NOI18N
    public final static String FALSE_VALUE = "false"; // NOI18N

    private final static int thisversion = 1;

    public GizmoOptionsXMLCodec(GizmoOptions gizmoOptions) {
        this.gizmoOptions = gizmoOptions;
    }

    public static int getVersion() {
        return thisversion;
    }

    // interface XMLDecoder
    public String tag() {
        return GizmoOptions.PROFILE_ID;
    }

    // interface XMLDecoder
    public void start(Attributes atts) throws VersionException {
        String what = "gizmo options"; // NOI18N
        int maxVersion = getVersion();
        checkVersion(atts, what, maxVersion);
    }

    // interface XMLDecoder
    public void end() {
        gizmoOptions.clearChanged();
    }

    // interface XMLDecoder
    public void startElement(String element, Attributes atts) {
    }

    // interface XMLDecoder
    public void endElement(String element, String currentText) {
        if (element.equals(PROFILE_ON_RUN_ELEMENT)) {
            boolean b = currentText.equals(TRUE_VALUE);
            gizmoOptions.getProfileOnRun().setValue(b);
        }
        if (element.equals(CPU_ELEMENT)) {
            boolean b = currentText.equals(TRUE_VALUE);
            gizmoOptions.getCpu().setValue(b);
        }
        if (element.equals(MEMORY_ELEMENT)) {
            boolean b = currentText.equals(TRUE_VALUE);
            gizmoOptions.getMemory().setValue(b);
        }
        if (element.equals(SYNCHRONIZATION_ELEMENT)) {
            boolean b = currentText.equals(TRUE_VALUE);
            gizmoOptions.getSynchronization().setValue(b);
        }
        else if (element.equals(DATA_PROVIDER_ELEMENT)) {
            int i = new Integer(currentText).intValue();
            gizmoOptions.getDataProvider().setValue(i);
        }
    }

    private static void encode(XMLEncoderStream xes, GizmoOptions gizmoOptions) {
        xes.elementOpen(GizmoOptions.PROFILE_ID, getVersion());
        if (gizmoOptions.getProfileOnRun().getModified()) {
            xes.element(PROFILE_ON_RUN_ELEMENT, "" + gizmoOptions.getProfileOnRun().getValue()); // NOI18N
        }
        if (gizmoOptions.getProfileOnRun().getModified()) {
            xes.element(CPU_ELEMENT, "" + gizmoOptions.getCpu().getValue()); // NOI18N
        }
        if (gizmoOptions.getProfileOnRun().getModified()) {
            xes.element(MEMORY_ELEMENT, "" + gizmoOptions.getMemory().getValue()); // NOI18N
        }
        if (gizmoOptions.getProfileOnRun().getModified()) {
            xes.element(SYNCHRONIZATION_ELEMENT, "" + gizmoOptions.getSynchronization().getValue()); // NOI18N
        }
        if (gizmoOptions.getDataProvider().getModified()) {
            xes.element(DATA_PROVIDER_ELEMENT, "" + gizmoOptions.getDataProvider().getValue()); // NOI18N
        }
        xes.elementClose(GizmoOptions.PROFILE_ID);
    }

    // interface XMLEncoder
    public void encode(XMLEncoderStream xes) {
        encode(xes, gizmoOptions);
    }
}
