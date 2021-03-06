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

import java.util.Vector;
import org.netbeans.modules.cnd.api.utils.IpeUtils;

public class VectorConfiguration {
    private VectorConfiguration master;

    private Vector value;
    private boolean dirty = false;

    public VectorConfiguration(VectorConfiguration master) {
	this.master = master;
	value = new Vector();
	reset();
    }

    public VectorConfiguration getMaster() {
	return master;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean getDirty() {
        return dirty;
    }
    
    public void add(Object o) {
	getValue().add(o);
    }

    public void setValue(Vector b) {
	this.value = b;
    }

    public Vector getValue() {
	return value;
	/*
	if (master != null && !getModified())
	    return master.getValue();
	else
	    return value;
	*/
    }

    public String[] getValueAsArray() {
	return (String[])getValue().toArray(new String[getValue().size()]);
    }

    public boolean getModified() {
	return value.size() != 0;
    }

    public void reset() {
	value.removeAllElements();
    }

    public String getOption(String prependOption) {
	StringBuilder option = new StringBuilder();
	String[] values = getValueAsArray();
	for (int i = 0; i < values.length; i++)
	    option.append(prependOption + IpeUtils.escapeOddCharacters(values[i]) + " "); // NOI18N
	return option.toString();
    }
    
    // Clone and Assign
    public void assign(VectorConfiguration conf) {
        setDirty(!this.equals(conf));
	reset();
	getValue().addAll(conf.getValue());
    }
     
    public boolean equals(VectorConfiguration conf) {
        boolean eq = true;
        if (getValue().size() != conf.getValue().size())
            eq = false;
        else {
            for (int i = 0; i < getValue().size(); i++) {
                if (!getValue().get(i).equals(conf.getValue().get(i))) {
                    eq = false;
                    break;
                }
            }
        }
        return eq;
    }

    public Object clone() {
	VectorConfiguration clone = new VectorConfiguration(master);
	clone.setValue((Vector)getValue().clone());
	return clone;
    }
}
