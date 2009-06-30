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

/**
 * This interface has all of the bean info accessor methods.
 *
 * @Generated
 */
package org.netbeans.modules.j2ee.dd.api.client;

import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;

/**
 *
 * @author jungi
 */
public interface AppClient extends RootInterface {
    
    public static final String PROPERTY_VERSION="dd_version"; //NOI18N
    public static final String VERSION_1_4="1.4"; //NOI18N
    public static final String VERSION_5_0="5"; //NOI18N
    public static final int STATE_VALID=0;
    public static final int STATE_INVALID_PARSABLE=1;
    public static final int STATE_INVALID_UNPARSABLE=2;
    public static final String PROPERTY_STATUS="dd_status"; //NOI18N
    
    int addEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef valueInterface);
    
    int addEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry valueInterface);
    
    int addIcon(org.netbeans.modules.j2ee.dd.api.common.Icon valueInterface) throws VersionNotSupportedException;
    
    int addMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination valueInterface) throws VersionNotSupportedException;
    
    int addMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef valueInterface) throws VersionNotSupportedException;
    
    int addResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef valueInterface);
    
    int addResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef valueInterface);
    
    int addServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef valueInterface) throws VersionNotSupportedException;
    
    java.lang.String getCallbackHandler();
    
    org.netbeans.modules.j2ee.dd.api.common.EjbRef[] getEjbRef();
    
    org.netbeans.modules.j2ee.dd.api.common.EjbRef getEjbRef(int index);
    
    org.netbeans.modules.j2ee.dd.api.common.EnvEntry[] getEnvEntry();
    
    org.netbeans.modules.j2ee.dd.api.common.EnvEntry getEnvEntry(int index);
    
    org.xml.sax.SAXParseException getError();
    
    org.netbeans.modules.j2ee.dd.api.common.Icon getIcon(int index) throws VersionNotSupportedException;
    
    org.netbeans.modules.j2ee.dd.api.common.MessageDestination[] getMessageDestination() throws VersionNotSupportedException;
    
    org.netbeans.modules.j2ee.dd.api.common.MessageDestination getMessageDestination(int index) throws VersionNotSupportedException;
    
    org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef[] getMessageDestinationRef() throws VersionNotSupportedException;
    
    org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef getMessageDestinationRef(int index) throws VersionNotSupportedException;
    
    org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef[] getResourceEnvRef();
    
    org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef getResourceEnvRef(int index);
    
    org.netbeans.modules.j2ee.dd.api.common.ResourceRef[] getResourceRef();
    
    org.netbeans.modules.j2ee.dd.api.common.ResourceRef getResourceRef(int index);
    
    org.netbeans.modules.j2ee.dd.api.common.ServiceRef[] getServiceRef() throws VersionNotSupportedException;
    
    org.netbeans.modules.j2ee.dd.api.common.ServiceRef getServiceRef(int index) throws VersionNotSupportedException;
    
    int getStatus();
    
    int removeEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef valueInterface);
    
    int removeEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry valueInterface);
    
    int removeIcon(org.netbeans.modules.j2ee.dd.api.common.Icon valueInterface) throws VersionNotSupportedException;
    
    int removeMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination valueInterface) throws VersionNotSupportedException;
    
    int removeMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef valueInterface) throws VersionNotSupportedException;
    
    int removeResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef valueInterface);
    
    int removeResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef valueInterface);
    
    int removeServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef valueInterface) throws VersionNotSupportedException;
    
    void setCallbackHandler(java.lang.String value);
    
    void setEjbRef(int index, org.netbeans.modules.j2ee.dd.api.common.EjbRef valueInterface);
    
    void setEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef[] value);
    
    void setEnvEntry(int index, org.netbeans.modules.j2ee.dd.api.common.EnvEntry valueInterface);
    
    void setEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry[] value);
    
    void setIcon(int index, org.netbeans.modules.j2ee.dd.api.common.Icon valueInterface) throws VersionNotSupportedException;
    
    void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon[] value) throws VersionNotSupportedException;
    
    void setMessageDestination(int index, org.netbeans.modules.j2ee.dd.api.common.MessageDestination valueInterface) throws VersionNotSupportedException;
    
    void setMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination[] value) throws VersionNotSupportedException;
    
    void setMessageDestinationRef(int index, org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef valueInterface) throws VersionNotSupportedException;
    
    void setMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef[] value) throws VersionNotSupportedException;
    
    void setResourceEnvRef(int index, org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef valueInterface);
    
    void setResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef[] value);
    
    void setResourceRef(int index, org.netbeans.modules.j2ee.dd.api.common.ResourceRef valueInterface);
    
    void setResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef[] value);
    
    void setServiceRef(int index, org.netbeans.modules.j2ee.dd.api.common.ServiceRef valueInterface) throws VersionNotSupportedException;
    
    void setServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef[] value) throws VersionNotSupportedException;
    
    int sizeEjbRef();
    
    int sizeEnvEntry();
    
    int sizeIcon() throws VersionNotSupportedException;
    
    int sizeMessageDestination() throws VersionNotSupportedException;
    
    int sizeMessageDestinationRef() throws VersionNotSupportedException;
    
    int sizeResourceEnvRef();
    
    int sizeResourceRef();
    
    int sizeServiceRef() throws VersionNotSupportedException;
    
    org.netbeans.modules.j2ee.dd.api.common.EjbRef newEjbRef();

    org.netbeans.modules.j2ee.dd.api.common.EnvEntry newEnvEntry();

    org.netbeans.modules.j2ee.dd.api.common.Icon newIcon() throws VersionNotSupportedException;

    org.netbeans.modules.j2ee.dd.api.common.MessageDestination newMessageDestination() throws VersionNotSupportedException;

    org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef newMessageDestinationRef() throws VersionNotSupportedException;

    org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef newResourceEnvRef();

    org.netbeans.modules.j2ee.dd.api.common.ResourceRef newResourceRef();

    org.netbeans.modules.j2ee.dd.api.common.ServiceRef newServiceRef() throws VersionNotSupportedException;
    
    java.math.BigDecimal getVersion();
    void setVersion(java.math.BigDecimal version);
}
