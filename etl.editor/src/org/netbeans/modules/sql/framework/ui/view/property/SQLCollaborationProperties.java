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

package org.netbeans.modules.sql.framework.ui.view.property;

import com.sun.sql.framework.exception.BaseException;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.ui.view.BasicTopView;
import org.openide.util.Exceptions;


/**
 * @author Ahimanikya Satapathy
 */
public class SQLCollaborationProperties {

    SQLDefinition sqlDef;
    BasicTopView editor;

    public SQLCollaborationProperties(SQLDefinition def, BasicTopView editor) {
        this.sqlDef = def;
        this.editor = editor;
    }

    /**
     * Gets display name of this table.
     * 
     * @return table disply name.
     */
    public String getDisplayName() {
        return sqlDef.getDisplayName();
    }

    /**
     * Gets execution stratergy code set for this collaboration.
     * @return execution stratergy code
     */
    public Integer getExecutionStrategyCode() {
        return this.sqlDef.getExecutionStrategyCode();
    }
    
    /**
     * Sets display name to given value.
     *
     * @param newName new display name
     */
    public void setDisplayName(String newName) {
        this.sqlDef.setDisplayName(newName);
    }
    
    /**
     * Sets execution stratergy codefor this collaboration.
     * @param code execution stratergy code
     */
    public void setExecutionStrategyCode(Integer code) {
        this.sqlDef.setExecutionStrategyCode(code);
        setDirty(true);
    }
    
    public String getSourceModelName(){
        try {
            return this.sqlDef.getSourceDatabaseModels().get(0).getETLDBConnectionDefinition().getConnectionURL();
        } catch (BaseException ex) {
            Exceptions.printStackTrace(ex);
            return "";
        }
    }
    
    public String getTargetModelName(){
        try {
            return this.sqlDef.getTargetDatabaseModels().get(0).getETLDBConnectionDefinition().getConnectionURL();
        } catch (BaseException ex) {
            Exceptions.printStackTrace(ex);
            return "";
        }
    }
    
    protected void setDirty(boolean dirty) {
        editor.setDirty(dirty);
    }
}

