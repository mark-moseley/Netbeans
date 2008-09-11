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
package org.netbeans.modules.xml.axi;

import java.io.IOException;
import org.netbeans.modules.xml.axi.impl.AXIModelImpl;
import org.netbeans.modules.xml.axi.visitor.AXIVisitor;
import org.netbeans.modules.xml.axi.Compositor.CompositorType;
import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Sequence;

/**
 * Represents a compositor in XML Schema. A compositor can be
 * one of Sequence, Choice or All.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class Compositor extends AXIComponent {
            
    /**
     * Represents the type of this compositor.
     * Can be one of sequence, choice and all.
     */
    public static enum CompositorType {
        SEQUENCE, CHOICE, ALL;
                
        public String getName() {
            return toString();
        }
        
        public String toString() {
            String retValue = super.toString();
            return retValue.substring(0,1) + retValue.substring(1).toLowerCase();
        }
    }
    
    /**
     * Creates a new instance of Compositor 
     */
    Compositor(AXIModel model, CompositorType type) {
        super(model);
        this.type = type;
    }
    
    /**
     * Creates a new instance of Compositor
     */
    Compositor(AXIModel model, SchemaComponent schemaComponent) {
        super(model, schemaComponent);
        if(schemaComponent instanceof Sequence)
            type = CompositorType.SEQUENCE;
        if(schemaComponent instanceof Choice)
            type = CompositorType.CHOICE;
        if(schemaComponent instanceof All)
            type = CompositorType.ALL;            
    }
    
    /**
     * Creates a proxy Compositor
     */
    public Compositor(AXIModel model, AXIComponent sharedComponent) {
        super(model, sharedComponent);
    }
    
    
    public void accept(AXIVisitor visitor) {
        visitor.visit(this);
    }
    
    /**
     * Returns the type of this compositor.
     */
    public CompositorType getType() {
        return type;
    }
        
    /**
     * Sets the type of this compositor.
     */
    public void setType(CompositorType value) {
        if(getModel() == null)
            return;
        getModel().startTransaction();
        try{
            firePropertyChangeEvent(Compositor.PROP_TYPE, getType(), value);
        }finally{
            getModel().endTransaction();
            try {
                ((AXIModelImpl)getModel()).setForceSync(true);
                getModel().sync();
            } catch(IOException iox) {
            } finally {
                if(getModel() != null)
                    ((AXIModelImpl)getModel()).setForceSync(false);
            }            
        }
    }
        
    void setCompositorType(Compositor.CompositorType newType) {
        this.type = newType;
    }
    
    /**
     * Returns the min occurs.
     */
    public String getMinOccurs() {
        return minOccurs;
    }
    
    public void setMinOccurs(String value) {        
        String oldValue = getMinOccurs();
        if( (oldValue == null && value == null) ||
            (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.minOccurs = value;
        firePropertyChangeEvent(PROP_MINOCCURS, oldValue, value);
    }
    
    /**
     * Returns the max occurs.
     */
    public String getMaxOccurs() {
        return maxOccurs;
    }
    
    public void setMaxOccurs(String value) {
        String oldValue = getMaxOccurs();
        if( (oldValue == null && value == null) ||
            (oldValue != null && oldValue.equals(value)) ) {
            return;
        }
        this.maxOccurs = value;
        firePropertyChangeEvent(PROP_MAXOCCURS, oldValue, value);
    }
        
    /**
     * Adds a Compositor as its child.
     */
    public void addCompositor(Compositor compositor) {
        appendChild(Compositor.PROP_COMPOSITOR, compositor);
    }
    
    /**
     * Removes an Compositor.
     */
    public void removeCompositor(Compositor compositor) {
        removeChild(Compositor.PROP_COMPOSITOR, compositor);
    }
    
    /**
     * Adds an Element as its child.
     */
    public void addElement(Element element) {
        appendChild(Element.PROP_ELEMENT, element);
    }
        
    /**
     * Removes an Element.
     */
    public void removeElement(Element element) {
        removeChild(Element.PROP_ELEMENT, element);
    }
	
    /**
     * true if #getMaxOccurs() and #getMinOccurs() allow multiciplity outside
     * [0,1], false otherwise. This method is only accurate after the element
     * has been inserted into the model.
     */
    public boolean allowsFullMultiplicity() {
		return !(getParent() instanceof Compositor && 
				((Compositor)getParent()).getType() == CompositorType.ALL);
    }	
    
    public String toString() {
        if(type == null)
            return null;
        
        return type.toString();
    }
    
    private CompositorType type;
    private String minOccurs;
    private String maxOccurs;
    
    public static final String PROP_COMPOSITOR  = "compositor"; // NOI18N
    public static final String PROP_MINOCCURS   = "minOccurs"; // NOI18N
    public static final String PROP_MAXOCCURS   = "maxOccurs"; // NOI18N
    public static final String PROP_TYPE        = "type";       // NOI18N
}
