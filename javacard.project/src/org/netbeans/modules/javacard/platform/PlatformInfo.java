/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.platform;

import org.netbeans.api.java.platform.Profile;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.javacard.constants.JavacardPlatformKeyNames;
import org.openide.modules.SpecificationVersion;
import java.util.Properties;

final class PlatformInfo {
    private final Properties props;
    public PlatformInfo(Properties props) {
        this.props = props;
    }

    public int getMajorVersion() {
        String mv = props.getProperty(JavacardPlatformKeyNames.PLATFORM_MAJORVERSION);
        if (mv != null) {
            try {
                return Integer.parseInt(mv);
            } catch (NumberFormatException nfe) {
                return 1;
            }
        } else {
            return 1;
        }
    }

    public int getMinorVersion() {
        String mv = props.getProperty(JavacardPlatformKeyNames.PLATFORM_MAJORVERSION);
        if (mv != null) {
            try {
                return Integer.parseInt(mv);
            } catch (NumberFormatException nfe) {
                return 1;
            }
        } else {
            return 1;
        }
    }

    public String getToolsClassPath() {
        return props.getProperty(JavacardPlatformKeyNames.PLATFORM_TOOLS_CLASSPATH);
    }

    public Specification getSpecificationVersion() {
        SpecificationVersion specVer = new SpecificationVersion(getMajorVersion() +
                "." + getMinorVersion()); //NOI18N
        Profile p = getProfile();
        return p == null ? new Specification(
                JavacardPlatformKeyNames.PLATFORM_SPECIFICATION_NAME, specVer) :
                new Specification(JavacardPlatformKeyNames.PLATFORM_SPECIFICATION_NAME,
                specVer, new Profile[]{p});
    }

    Profile getProfile() {
        String profile = props.getProperty (JavacardPlatformKeyNames.PLATFORM_PROFILE);
        Profile p = profile == null || profile.length() == 0 ? null :
            new Profile(profile, new SpecificationVersion("1.0")); //NOI18N
        return p;
    }

    public String getName() {
        return props.getProperty(JavacardPlatformKeyNames.PLATFORM_NAME);
    }

    public String getTitle() {
        return getName();
    }

    void writeTo(Properties props) {
        props.putAll(this.props);
        String kind = props.getProperty(JavacardPlatformKeyNames.PLATFORM_KIND);
        //Do this for "wrapper" platforms which will override some
        //properties
        if (JavacardPlatformKeyNames.PLATFORM_KIND_RI.equals(kind)) {
            String home = props.getProperty(JavacardPlatformKeyNames.PLATFORM_HOME);
            props.setProperty(JavacardPlatformKeyNames.RI_HOME, home);
        }
    }
}
