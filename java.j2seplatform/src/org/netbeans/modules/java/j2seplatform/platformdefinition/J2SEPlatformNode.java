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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.text.MessageFormat;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

class J2SEPlatformNode extends AbstractNode {

    private J2SEPlatformImpl platform;
    private String toolTip;
    private boolean broken;

    public J2SEPlatformNode (J2SEPlatformImpl platform, DataObject definition) {
        super (Children.LEAF, Lookups.fixed(new Object[] {platform, definition}));
        this.platform = platform;
        super.setIconBaseWithExtension("org/netbeans/modules/java/j2seplatform/resources/platform.gif");
    }

    public String getDisplayName () {
        return this.platform.getDisplayName();
    }

    public String getHtmlDisplayName() {
        if (isBroken()) {
            return "<font color=\"#A40000\">"+this.platform.getDisplayName()+"</font>";
        }
        else {
            return null;
        }
    }

    public String getName () {
        return this.getDisplayName();
    }

    public void setName (String name) {
        this.platform.setDisplayName (name);
    }

    public void setDisplayName(String name) {
        this.setName (name);
    }
    
    public synchronized String getShortDescription() {
        if (this.toolTip == null) {
            this.toolTip = MessageFormat.format (
            NbBundle.getMessage(J2SEPlatformNode.class,"TXT_J2SEPlatformToolTip"),
            new Object[] {
                this.platform.getSpecification().getVersion()
            });
        }
        return this.toolTip;
    }

    public boolean hasCustomizer () {
        return true;
    }

    public java.awt.Component getCustomizer () {
        if (isBroken()) {
            return new BrokenPlatformCustomizer (this.platform);
        }
        else {
            return new J2SEPlatformCustomizer (this.platform);
        }
    }

    private boolean isBroken () {
        if (this.platform.getInstallFolders().size()==0) {
            return true;
        }
        for (String tool : PlatformConvertor.IMPORTANT_TOOLS) {
            if (platform.findTool(tool) == null) {
                return true;
            }
        }
        return false;
    }

}
