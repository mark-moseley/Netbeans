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

package org.netbeans.modules.compapp.javaee.sunresources.tool.cmap;

import java.util.Properties;

import org.netbeans.modules.compapp.javaee.sunresources.tool.cmap.ResourceNode.ResourceType;

/**
 * @author echou
 *
 */
public class ResourceDepend {

    private CMapNode source;
    private String mappedName = ""; // NOI18N
    private String targetResType;
    private String targetResJndiName;
    private ResourceType type = ResourceType.OTHER;
    private ResourceNode target;
    private Properties props = new Properties();
    
    public ResourceDepend(CMapNode source) {
        this.source = source;
    }

    public String getMappedName() {
        return mappedName;
    }

    public void setMappedName(String mappedName) {
        this.mappedName = mappedName;
    }

    public String getTargetResType() {
        return targetResType;
    }

    public void setTargetResType(String targetResType) {
        this.targetResType = targetResType;
    }

    public String getTargetResJndiName() {
        return targetResJndiName;
    }

    public void setTargetResJndiName(String targetResJndiName) {
        this.targetResJndiName = targetResJndiName;
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("mappedName=" + mappedName + ", "); // NOI18N
        sb.append("targetResType=" + targetResType + ", "); // NOI18N
        sb.append("targetResJndiName=" + targetResJndiName + ", "); // NOI18N
        sb.append("type=" + type + ", "); // NOI18N
        sb.append("props=" + props); // NOI18N
        sb.append("targetNode=@" + ((target == null)?null:target.hashCode())); // NOI18N
        return sb.toString();
    }

    public CMapNode getTarget() {
        return target;
    }

    public void setTarget(ResourceNode target) {
        this.target = target;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }

}