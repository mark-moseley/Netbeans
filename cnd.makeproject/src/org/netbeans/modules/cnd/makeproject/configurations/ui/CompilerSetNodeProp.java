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

package org.netbeans.modules.cnd.makeproject.configurations.ui;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.openide.nodes.Node;

public class CompilerSetNodeProp extends Node.Property {
    private CompilerSet2Configuration configuration;
    private boolean canWrite;
    private String txt1;
    private String txt2;
    private String txt3;
    private String oldname;

    public CompilerSetNodeProp(CompilerSet2Configuration configuration, boolean canWrite, String txt1, String txt2, String txt3) {
        super(Integer.class);
        this.configuration = configuration;
	this.canWrite = canWrite;
	this.txt1 = txt1;
	this.txt2 = txt2;
	this.txt3 = txt3;
        oldname = configuration.getOption();
        configuration.setCompilerSetNodeProp(this);
    }
    
    public String getOldname() {
        return oldname;
    }

    @Override
    public String getName() {
	return txt2;
    }

    @Override
    public String getShortDescription() {
	return txt3;
    }
    
    @Override
    public String getHtmlDisplayName() {
        if (configuration.getCompilerSetName().getModified())
            return "<b>" + getDisplayName(); // NOI18N
        else
            return null;
    }
    
    public Object getValue() {
        return configuration.getCompilerSetName().getValue();
    }
    
    public void setValue(Object v) {
        configuration.setValue((String)v);
    }
    
    @Override
    public void restoreDefaultValue() {
        configuration.getCompilerSetName().reset();
    }
    
    @Override
    public boolean supportsDefaultValue() {
        return true;
    }
    
    @Override
    public boolean isDefaultValue() {
        return !configuration.getCompilerSetName().getModified();
    }

    public boolean canWrite() {
        return canWrite;
    }
    
    public boolean canRead() {
        return true;
    }

    public void repaint() {
        ((CompilerSetEditor) getPropertyEditor()).repaint();
    }

    @Override
    public PropertyEditor getPropertyEditor() {
	return new CompilerSetEditor();
    }

    private class CompilerSetEditor extends PropertyEditorSupport {
        @Override
        public String getJavaInitializationString() {
            return getAsText();
        }
        
        @Override
        public String getAsText() {
            String displayName = configuration.getDisplayName(true);
            return displayName;
        }
        
        @Override
        public void setAsText(String text) throws java.lang.IllegalArgumentException {
            setValue(text);
        }
        
        @Override
        public String[] getTags() {
            List<String> list = new ArrayList<String>();
            // TODO: this works unpredictable on switching development hosts
            // TODO: should be resolved later on
//            if (configuration.getCompilerSetManager().getCompilerSet(getOldname()) == null) {
//                list.add(getOldname());
//            }
            list.addAll(configuration.getCompilerSetManager().getCompilerSetNames());
            return (String[]) list.toArray(new String[list.size()]);
        }
        
        public void repaint() {
            firePropertyChange();
        }
    }
}
