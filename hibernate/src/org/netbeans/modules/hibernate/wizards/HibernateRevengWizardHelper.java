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
package org.netbeans.modules.hibernate.wizards;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.hibernate.wizards.support.SelectedTables;
import org.netbeans.modules.hibernate.wizards.support.TableClosure;
import org.openide.filesystems.FileObject;

/**
 * Helper class to access all the information from the panels
 * used by the reverse engineering wizard.
 * @author gowri
 */
public class HibernateRevengWizardHelper {

    private Project project;
    private TableClosure tableClosure;
    private SelectedTables selectedTables;
    private SourceGroup location;
    private String packageName;    
    private boolean domainGen;
    private boolean hbmGen;
    private FileObject confFile;

    public HibernateRevengWizardHelper(Project project) {
        this.project = project;
    }
    
    public Project getProject() {
        return project;
    }
    
    public SelectedTables getSelectedTables() {
        return selectedTables;
    }    

    public void setSelectedTables(SelectedTables selectedTables) {
        assert selectedTables != null;
        this.selectedTables = selectedTables;
    }
    
    public void setTableClosure(TableClosure tableClosure) {
        assert tableClosure != null;
        this.tableClosure = tableClosure;
    }
    
    public TableClosure getTableClosure() {
        return tableClosure;
    }
    
    public SourceGroup getLocation() {
        return location;
    }

    public void setLocation(SourceGroup location) {
        this.location = location;
    }
    
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    public boolean getDomainGen() {
        return domainGen;
    }
    
    public void setDomainGen(boolean value) {
        this.domainGen = value;
    }
    
    public boolean getHbmGen() {
        return hbmGen;
    }
    
    public void setHbmGen(boolean value) {
        this.hbmGen = value;
    }   
    
    public void setConfigurationFile(FileObject confFile) {
        this.confFile = confFile;
    }
    
    public FileObject getConfigurationFile() {
        return confFile;
    }
    
}
