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

package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.makeproject.configurations.ui.CompilerSetNodeProp;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

public class CompilerSet2Configuration implements PropertyChangeListener {

    private DevelopmentHostConfiguration developmentHostConfiguration;
    private StringConfiguration compilerSetName;
    private CompilerSetNodeProp compilerSetNodeProp;
    private String flavor;
    private boolean dirty = false;

    private CompilerSet2Configuration(CompilerSet2Configuration other) {
        this.developmentHostConfiguration = (DevelopmentHostConfiguration) other.developmentHostConfiguration.clone();
        this.compilerSetName = (StringConfiguration) other.compilerSetName.clone();
        this.flavor = other.flavor;
        this.compilerSetNodeProp = null;        
    }
    
    // Constructors
    public CompilerSet2Configuration(DevelopmentHostConfiguration developmentHostConfiguration) {
        this.developmentHostConfiguration = developmentHostConfiguration;
        String csName = getCompilerSetManager().getDefaultCompilerSet().getName();
        if (csName == null || csName.length() == 0) {
            if (getCompilerSetManager().getCompilerSetNames().size() > 0) {
                csName = getCompilerSetManager().getCompilerSet(0).getName();
            } else {
                if (Utilities.getOperatingSystem() == Utilities.OS_SOLARIS)
                    csName = "Sun"; // NOI18N
                else
                    csName = "GNU"; // NOI18N
            }
        }
        compilerSetName = new StringConfiguration(null, csName);
        flavor = null;
        compilerSetNodeProp = null;
    }

    // we can't store CSM because it's dependent on devHostConfig name which is not persistent
    public CompilerSetManager getCompilerSetManager() {
        return CompilerSetManager.getDefault(developmentHostConfiguration.getName());
    }

//
//    // MakeConfiguration
//    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
//        this.makeConfiguration = makeConfiguration;
//    }
//    public MakeConfiguration getMakeConfiguration() {
//        return makeConfiguration;
//    }

    // compilerSetName
    public StringConfiguration getCompilerSetName() {
        return compilerSetName;
    }

    public void setCompilerSetName(StringConfiguration compilerSetName) {
        this.compilerSetName = compilerSetName;
    }

    public void setCompilerSetNodeProp(CompilerSetNodeProp compilerSetNodeProp) {
        this.compilerSetNodeProp = compilerSetNodeProp;
    }

    // ----------------------------------------------------------------------------------------------------

    public void setValue(String name) {
        if (!getOption().equals(name)) {
            setValue(name, null);
        }
    }

    public void setNameAndFlavor(String name, int version) {
        String nm;
        String fl;
        int index = name.indexOf("|"); // NOI18N
        if (index > 0) {
            nm = name.substring(0, index);
            fl = name.substring(index+1);
        }
        else {
            nm = name;
            fl = name;
        }
        setValue(CompilerFlavor.mapOldToNew(nm, version), CompilerFlavor.mapOldToNew(fl, version));
    }

    public void setValue(String name, String flavor) {
        getCompilerSetName().setValue(name);
        setFlavor(flavor);
    }

    /*
     * Keep backward compatibility with CompilerSetConfiguration (for now)
     */
    public int getValue() {
        // TODO: only usage of getValue is next:
        // CompilerSetManager.getDefault(developmentHostConfiguration.getName()).getCompilerSet(conf.getCompilerSet().getValue());

        String s = getCompilerSetName().getValue();
	if (s != null) {
            int i = 0;
            for (String csname : CompilerSetManager.getDefault(developmentHostConfiguration.getName()).getCompilerSetNames()) {
                if (s.equals(csname)) {
                    return i;
                }
                i++;
            }
        }
        return 0; // Default
    }

    /*
     * TODO: spread it out (Sergey)
     * Should this return csm.getCurrentCompilerSet()? (GRP)
     */
    public CompilerSet getCompilerSet() {
        return getCompilerSetManager().getCompilerSet(getCompilerSetName().getValue());
    }

    public int getPlatform() {
        return getCompilerSetManager().getPlatform();
    }

    public String getName() {
        return getDisplayName();
    }

    public String getDisplayName() {
        return getDisplayName(false);
    }

    public String getDisplayName(boolean displayIfNotFound) {
        CompilerSet compilerSet = getCompilerSetManager().getCompilerSet(getCompilerSetName().getValue());
        String displayName = null;

        if (compilerSet != null) {
            displayName = compilerSet.getName();
        }
        if (displayName != null) {
            return displayName;
        } else {
            if (displayIfNotFound)
                return createNotFoundName(getCompilerSetName().getValue());
            else
                return ""; // NOI18N
        }
    }

    public String createNotFoundName(String name) {
        return name.equals(CompilerSet.None) ? name : NbBundle.getMessage(CompilerSet2Configuration.class,  "NOT_FOUND", name); // NOI18N
    }

    // Clone and assign
    public void assign(CompilerSet2Configuration conf) {
        String oldName = getCompilerSetName().getValue();
        String newName = conf.getCompilerSetName().getValue();
        setDirty(newName != null && !newName.equals(oldName));
//        setMakeConfiguration(conf.getMakeConfiguration());
        setValue(conf.getCompilerSetName().getValue());
    }

    @Override
    public Object clone() {
        CompilerSet2Configuration clone = new CompilerSet2Configuration(this);
        return clone;
    }

    public void setDevelopmentHostConfiguration(DevelopmentHostConfiguration developmentHostConfiguration) {
        this.developmentHostConfiguration = developmentHostConfiguration;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean getDirty() {
        return dirty;
    }

    /*
     * Backward compatibility with old CompilerSetConfiguration (for now)
     */
    public boolean isValid() {
        return getCompilerSet() != null;
    }

    public void setValid() {
        // Nothing
    }

    public String getOldName() {
        return getCompilerSetName().getValue();
    }

    public String getOption() {
        return getCompilerSetName().getValue();
    }

    public String getNameAndFlavor() {
        StringBuilder ret = new StringBuilder();
        ret.append(getOption());
        if (getFlavor() != null) {
            ret.append("|"); // NOI18N
            ret.append(getFlavor());
        }
        return ret.toString();
    }

    public String getFlavor() {
        if (flavor == null) {
            CompilerSet cs = getCompilerSet();
            if (cs != null)
                this.flavor = cs.getCompilerFlavor().toString();

        }
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public void propertyChange(final PropertyChangeEvent evt) {
        final String key = evt.getNewValue().toString();
        if (key.equals(CompilerSetManager.LOCALHOST)) {
            setValue(getCompilerSetManager().getCompilerSet(0).getName());
        } else {
            setValue(CompilerSetManager.getDefault(key).getCompilerSet(0).getName());
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    ServerList server = (ServerList) Lookup.getDefault().lookup(ServerList.class);
                    if (server != null) {
                        ServerRecord record = server.get(key);
                        if (record != null) {
                            CompilerSetManager csm = CompilerSetManager.getDefault(key);
                            setValue(csm.getCompilerSet(0).getName());
                            if (compilerSetNodeProp != null) {
                                compilerSetNodeProp.repaint();
                            }
                        }
                    }
                }
            });
        }
    }
}
