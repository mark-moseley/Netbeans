/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.hints.pom.spi;

import java.util.prefs.Preferences;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbPreferences;

/**
 *
 * @author mkleint
 */
public final class Configuration {
    private String id;
    private String description;
    private boolean defaultEnabled;
    static final String ENABLED_KEY = "enabled";         // NOI18N
    static final String SEVERITY_KEY = "severity";       // NOI18N
    static final String IN_TASK_LIST_KEY = "inTaskList"; // NOI18N
    private HintSeverity defaultSeverity;
    private String displayName;

    public Configuration(String id, String displayName, String description, boolean defaultEnabled, HintSeverity defaultSeverity) {
        this.id = id;
        this.description = description;
        this.defaultEnabled = defaultEnabled;
        this.defaultSeverity = defaultSeverity;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Gets preferences node, which stores the options for given hint. It is not
     * necessary to override this method unless you want to create some special
     * behavior. The default implementation will create the the preferences node
     * by calling <code>NbPreferences.forModule(this.getClass()).node(profile).node(getId());</code>
     * @return Preferences node for given hint.
     */

    public String getId() {
        return id;
    }

    public Preferences getPreferences() {
//        Map<String, Preferences> override = HintsSettings.getPreferencesOverride();
//        if (override != null) {
//            Preferences p = override.get(getId());
//            if (p != null) {
//                return p;
//            }
//        }
        return NbPreferences.forModule(this.getClass()).node(getId()); //NOI18N
    }


    public String getDescription() {
        return description;
    }

    /** Finds out whether the rule is currently enabled.
     * @return true if enabled false otherwise.
     */
    public final boolean isEnabled(Preferences p) {
        return p.getBoolean(ENABLED_KEY, defaultEnabled);
    }

    public void setEnabled( Preferences p, boolean value ) {
        p.putBoolean(ENABLED_KEY, value);
    }


    /** Gets current severiry of the hint.
     * @return Hints severity in current profile.
     */
    public final HintSeverity getSeverity(Preferences p) {
        String s = p.get(SEVERITY_KEY, null );
        return s == null ? defaultSeverity : fromPreferenceString(s);
    }


    public void setSeverity( Preferences p, Configuration.HintSeverity severity ) {
        p.put(SEVERITY_KEY, severity.toPreferenceString());
    }



    /** Severity of hint
     *  <li><code>ERROR</code>  - will show up as error
     *  <li><code>WARNING</code>  - will show up as warrnig
     */
    public static enum HintSeverity {
        ERROR,
        WARNING;

        public Severity toEditorSeverity() {
            switch ( this ) {
                case ERROR:
                    return Severity.ERROR;
                case WARNING:
                    return Severity.VERIFIER;
                default:
                    return null;
            }
        }

        public String toPreferenceString() {
            switch ( this ) {
                case ERROR:
                    return "error";
                case WARNING:
                    return "warning";
                default:
                    return null;
            }
        }

    }

    public static HintSeverity fromPreferenceString(String sev) {
        if (sev.equals("error")) {
            return HintSeverity.ERROR;
        }
        if (sev.equals("warning")) {
            return HintSeverity.WARNING;
        }
        throw new IllegalStateException(sev);

    }

}
