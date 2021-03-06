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

package org.netbeans.modules.j2ee.persistence.api.metadata.orm;

public interface OneToOne {

    public void setName(String value);

    public String getName();
    
    public void setTargetEntity(String value);
    
    public String getTargetEntity();
    
    public void setFetch(String value);
    
    public String getFetch();
    
    public void setOptional(boolean value);
    
    public boolean isOptional();
    
    public void setMappedBy(String value);
    
    public String getMappedBy();
    
    public void setPrimaryKeyJoinColumn(int index, PrimaryKeyJoinColumn value);
    
    public PrimaryKeyJoinColumn getPrimaryKeyJoinColumn(int index);
    
    public int sizePrimaryKeyJoinColumn();
    
    public void setPrimaryKeyJoinColumn(PrimaryKeyJoinColumn[] value);
    
    public PrimaryKeyJoinColumn[] getPrimaryKeyJoinColumn();
    
    public int addPrimaryKeyJoinColumn(PrimaryKeyJoinColumn value);
    
    public int removePrimaryKeyJoinColumn(PrimaryKeyJoinColumn value);
    
    public PrimaryKeyJoinColumn newPrimaryKeyJoinColumn();
    
    public void setJoinColumn(int index, JoinColumn value);
    
    public JoinColumn getJoinColumn(int index);
    
    public int sizeJoinColumn();
    
    public void setJoinColumn(JoinColumn[] value);
    
    public JoinColumn[] getJoinColumn();
    
    public int addJoinColumn(JoinColumn value);
    
    public int removeJoinColumn(JoinColumn value);
    
    public JoinColumn newJoinColumn();
    
    public void setJoinTable(JoinTable value);
    
    public JoinTable getJoinTable();
    
    public JoinTable newJoinTable();
    
    public void setCascade(CascadeType value);
    
    public CascadeType getCascade();
    
    public CascadeType newCascadeType();
    
}
