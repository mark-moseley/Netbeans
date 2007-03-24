/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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
