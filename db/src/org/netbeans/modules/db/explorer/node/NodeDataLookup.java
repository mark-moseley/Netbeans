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

package org.netbeans.modules.db.explorer.node;

import java.util.HashSet;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * This is a Lookup that allowsdata instances to be easily
 * added and removed.
 * 
 * @author Rob Englander
 */
public class NodeDataLookup extends AbstractLookup {

    /** the data instances held in the lookup */
    private HashSet dataInstances = new HashSet();

    /** the content of the underlying AbstractLookup */
    private InstanceContent content;
    
    /**
     * Constructor
     */
    public NodeDataLookup() {
        this(createContent());
    }
    
    /**
     * This private constructor is used by the public constructor
     * so that the InstanceContent can be captured.
     * 
     * @param content the InstanceContent to construct the object with
     */
    private NodeDataLookup(InstanceContent content) {
        super(content);
        this.content = content;
    }
    
    /**
     * This is a static method used by the constructor so that a new
     * instance of InstanceContent can be created and based to the base class
     * constructor.
     * 
     * @return an InstanceContent
     */
    private static InstanceContent createContent() {
        return new InstanceContent();
    }
               
    /**
     * Add an object instance to the lookup
     * 
     * @param data the data instance to be added
     */
    public void add(Object data) {
        dataInstances.add(data);
        content.set(dataInstances, null);
    }
    
    /**
     * Remove the object instance from the lookup.
     * 
     * @param data
     */
    public void remove(Object data) {
        dataInstances.remove(data);
        content.set(dataInstances, null);
    }
    
}
