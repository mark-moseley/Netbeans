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

package org.netbeans.modules.debugger.jpda.heapwalk;

import com.sun.tools.profiler.heap.FieldValue;
import com.sun.tools.profiler.heap.Instance;
import com.sun.tools.profiler.heap.JavaClass;
import com.sun.tools.profiler.heap.Value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.api.debugger.jpda.JPDAArrayType;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;

/**
 *
 * @author Martin Entlicher
 */
public class InstanceImpl implements Instance {
    
    private ObjectVariable var;
    private int instanceNo;
    protected HeapImpl heap;
    
    /** Creates a new instance of InstanceImpl */
    protected InstanceImpl(HeapImpl heap, ObjectVariable var, int instanceNo) {
        this.var = var;
        this.instanceNo = instanceNo;
        this.heap = heap;
    }
    
    protected InstanceImpl(HeapImpl heap, ObjectVariable var) {
        this.var = var;
        this.instanceNo = -1;
        this.heap = heap;
    }
    
    public static Instance createInstance(HeapImpl heap, ObjectVariable var) {
        JPDAClassType classType = var.getClassType();
        if (classType == null) {
            return createInstance(heap, var, 0);
        } else {
            return createInstance(heap, var, -1);
        }
    }
    
    public static Instance createInstance(HeapImpl heap, ObjectVariable var, int instanceNo) {
        Instance instance;
        JPDAClassType type = var.getClassType();
        if (type instanceof JPDAArrayType) {
            boolean isPrimitiveArray = false;
            isPrimitiveArray = !(((JPDAArrayType) type).getComponentType() instanceof JPDAClassType);
            if (isPrimitiveArray) {
                instance = new PrimitiveArrayInstanceImpl(heap, var, instanceNo);
            } else {
                instance = new ObjectArrayInstanceImpl(heap, var, instanceNo);
            }
        } else {
            instance = new InstanceImpl(heap, var, instanceNo);
        }
        return instance;
    }

    public JavaClass getJavaClass() {
        JPDAClassType type = var.getClassType();
        if (type != null) {
            return new JavaClassImpl(heap, type);
        } else {
            return new JavaClassImpl(var.getType());
        }
    }

    public long getInstanceId() {
        return var.getUniqueID();
    }

    public synchronized int getInstanceNumber() {
        if (instanceNo < 0) {
            instanceNo = heap.getInstanceNumberCollector().getInstanceNumber(var);
        }
        return instanceNo;
    }

    /*private int computeInstanceNumber() {
        JPDAClassType classType = var.getClassType();
        if (classType == null) {
            return 0;
        }
        List<ObjectVariable> vars = classType.getInstances(0);
        int i = 1;
        for (ObjectVariable obj: vars) {
            if (var.getUniqueID() == obj.getUniqueID()) {
                break;
            }
            i++;
        }
        return i;
    }*/
    
    public int getSize() {
        return 0;
    }

    public List<FieldValue> getFieldValues() {
        int fieldsCount = var.getFieldsCount();
        org.netbeans.api.debugger.jpda.Field[] varFields = var.getFields(0, fieldsCount);
        List<FieldValue> fields = new ArrayList<FieldValue>(varFields.length);
        for (org.netbeans.api.debugger.jpda.Field field : varFields) {
            if (!field.isStatic()) {
                if (field instanceof ObjectVariable) {
                    Instance instance;
                    if (((ObjectVariable) field).getUniqueID() == 0L) {
                        instance = null;
                    } else {
                        instance = InstanceImpl.createInstance(heap, (ObjectVariable) field);
                    }
                    fields.add(new ObjectFieldValueImpl(heap, this, field, instance));
                } else {
                    fields.add(new FieldValueImpl(heap, this, field));
                }
            }
        }
        return fields;
    }

    public Object getValueOfField(String name) {
        // TODO
        return null;
   }

    public List<FieldValue> getStaticFieldValues() {
        return getJavaClass().getStaticFieldValues();
    }

    public List<Value> getReferences() {
        List<ObjectVariable> references = var.getReferringObjects(0);
        List<Value> values = new ArrayList<Value>(references.size());
        Set referencedFields = new HashSet();
        for (ObjectVariable obj : references) {
            JPDAClassType type = obj.getClassType();
            if (type instanceof JPDAArrayType) {
                int length = obj.getFieldsCount();
                int CHUNK = 1000;
                for (int i = 0; i < length; i += CHUNK) {
                    int to = Math.min(i + CHUNK, length);
                    Variable[] items = obj.getFields(i, to - i);
                    int j = i;
                    for (Variable item: items) {
                        if (var.equals(item)) {
                            Instance instance = createInstance(heap, obj);
                            values.add(new ArrayItemValueImpl(instance, this, j));
                            break;
                        }
                        j++;
                    }
                    if (j < to) {
                        break;
                    }
                }
            } else {
                int count = obj.getFieldsCount();
                org.netbeans.api.debugger.jpda.Field[] allFields = obj.getFields(0, count);
                for (org.netbeans.api.debugger.jpda.Field field : allFields) {
                    if (field instanceof ObjectVariable &&
                        !referencedFields.contains(field) &&
                        var.getUniqueID() == ((ObjectVariable) field).getUniqueID()) {
                        
                        referencedFields.add(field);
                        Instance instance = createInstance(heap, obj);
                        values.add(new ObjectFieldValueImpl(heap, instance, field, this));
                        break;
                    }
                }
            }
        }
        return values;
    }

    public boolean isGCRoot() {
        return false;
    }

    public int getRetainedSize() {
        return 0;
    }

    public int getReachableSize() {
        return 0;
    }
    
    public Instance getNearestGCRootPointer() {
        return null;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof InstanceImpl)) {
            return false;
        }
        return var.getUniqueID() == ((InstanceImpl) obj).var.getUniqueID();
    }

    public int hashCode() {
        return (int) var.getUniqueID();
    }
    
}
