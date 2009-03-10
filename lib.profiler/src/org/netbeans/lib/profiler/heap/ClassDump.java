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

package org.netbeans.lib.profiler.heap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Tomas Hurka
 */
class ClassDump extends HprofObject implements JavaClass {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------
    
    private static final boolean DEBUG = false;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    final ClassDumpSegment classDumpSegment;
    private int instances;
    private long loadClassOffset;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    ClassDump(ClassDumpSegment segment, long offset) {
        super(offset);
        classDumpSegment = segment;
        assert getHprofBuffer().get(offset) == HprofHeap.CLASS_DUMP;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public long getAllInstancesSize() {
        if (isArray()) {
            return ((Long) classDumpSegment.arrayMap.get(this)).longValue();
        }

        return ((long)getInstancesCount()) * getInstanceSize();
    }

    public boolean isArray() {
        return classDumpSegment.arrayMap.get(this) != null;
    }

    public Instance getClassLoader() {
        long loaderId = getHprofBuffer().getID(fileOffset + classDumpSegment.classLoaderIDOffset);

        return getHprof().getInstanceByID(loaderId);
    }

    public Field getField(String name) {
        Iterator fIt = getFields().iterator();

        while (fIt.hasNext()) {
            Field field = (Field) fIt.next();

            if (field.getName().equals(name)) {
                return field;
            }
        }

        return null;
    }

    public List /*<Field>*/ getFields() {
        List filedsList = (List) classDumpSegment.fieldsCache.get(this);
        if (filedsList == null) {
            filedsList = computeFields();
            classDumpSegment.fieldsCache.put(this,filedsList);
        }
        return filedsList;
    }

    public int getInstanceSize() {
        if (isArray()) {
            return -1;
        }

        return classDumpSegment.getMinimumInstanceSize()
               + getHprofBuffer().getInt(fileOffset + classDumpSegment.instanceSizeOffset);
    }

    public List /*<Instance>*/ getInstances() {
        int instancesCount = getInstancesCount();

        if (instancesCount == 0) {
            return Collections.EMPTY_LIST;
        }

        long classId = getJavaClassId();
        HprofHeap heap = getHprof();
        HprofByteBuffer dumpBuffer = getHprofBuffer();
        int idSize = dumpBuffer.getIDSize();
        List instances = new ArrayList(instancesCount);
        TagBounds allInstanceDumpBounds = heap.getAllInstanceDumpBounds();
        long[] offset = new long[] { allInstanceDumpBounds.startOffset };

        while (offset[0] < allInstanceDumpBounds.endOffset) {
            long start = offset[0];
            int classIdOffset = 0;
            long instanceClassId = 0L;
            int tag = heap.readDumpTag(offset);
            Instance instance;

            if (tag == HprofHeap.INSTANCE_DUMP) {
                classIdOffset = idSize + 4;
            } else if (tag == HprofHeap.OBJECT_ARRAY_DUMP) {
                classIdOffset = idSize + 4 + 4;
            } else if (tag == HprofHeap.PRIMITIVE_ARRAY_DUMP) {
                byte type = dumpBuffer.get(start + 1 + idSize + 4 + 4);
                instanceClassId = classDumpSegment.getPrimitiveArrayClass(type).getJavaClassId();
            }

            if (classIdOffset != 0) {
                instanceClassId = dumpBuffer.getID(start + 1 + classIdOffset);
            }

            if (instanceClassId == classId) {
                if (tag == HprofHeap.INSTANCE_DUMP) {
                    instance = new InstanceDump(this, start);
                } else if (tag == HprofHeap.OBJECT_ARRAY_DUMP) {
                    instance = new ObjectArrayDump(this, start);
                } else if (tag == HprofHeap.PRIMITIVE_ARRAY_DUMP) {
                    instance = new PrimitiveArrayDump(this, start);
                } else {
                    throw new IllegalArgumentException("Illegal tag " + tag); // NOI18N
                }

                instances.add(instance);

                if (--instancesCount == 0) {
                    return instances;
                }
            }
        }

        if (DEBUG) {
            System.out.println("Class " + getName() + " Col " + instances.size() + " instances " + getInstancesCount()); // NOI18N
        }

        return instances;
    }

    public int getInstancesCount() {
        if (instances == 0) {
            getHprof().computeInstances();
        }

        return instances;
    }

    public long getJavaClassId() {
        return getHprofBuffer().getID(fileOffset + classDumpSegment.classIDOffset);
    }

    public String getName() {
        return getLoadClass().getName();
    }

    public List /*<FieldValue>*/ getStaticFieldValues() {
        HprofByteBuffer buffer = getHprofBuffer();
        long offset = fileOffset + getStaticFieldOffset();
        int i;
        int fields;
        List filedsList;
        HprofHeap heap = getHprof();

        fields = buffer.getShort(offset);
        offset += 2;
        filedsList = new ArrayList(fields);

        for (i = 0; i < fields; i++) {
            byte type = buffer.get(offset + classDumpSegment.fieldTypeOffset);
            int fieldSize = classDumpSegment.fieldSize + heap.getValueSize(type);
            HprofFieldValue value;

            if (type == HprofHeap.OBJECT) {
                value = new HprofFieldObjectValue(this, offset);
            } else {
                value = new HprofFieldValue(this, offset);
            }

            filedsList.add(value);
            offset += fieldSize;
        }

        return filedsList;
    }

    public Collection /*<JavaClass>*/ getSubClasses() {
        List classes = classDumpSegment.hprofHeap.getAllClasses();
        List subclasses = new ArrayList(classes.size() / 10);
        Map subclassesMap = new HashMap((classes.size() * 4) / 3);

        subclassesMap.put(this, Boolean.TRUE);

        for (int i = 0; i < classes.size(); i++) {
            JavaClass jcls = (JavaClass) classes.get(i);
            Boolean b = (Boolean) subclassesMap.get(jcls);

            if (b == null) {
                b = isSubClass(jcls, subclassesMap);
            }

            if ((b == Boolean.TRUE) && (jcls != this)) {
                subclasses.add(jcls);
            }
        }

        return subclasses;
    }

    public JavaClass getSuperClass() {
        long superClassId = getHprofBuffer().getID(fileOffset + classDumpSegment.superClassIDOffset);

        return classDumpSegment.getClassDumpByID(superClassId);
    }

    public Object getValueOfStaticField(String name) {
        Iterator fIt = getStaticFieldValues().iterator();

        while (fIt.hasNext()) {
            FieldValue fieldValue = (FieldValue) fIt.next();

            if (fieldValue.getField().getName().equals(name)) {
                if (fieldValue instanceof HprofFieldObjectValue) {
                    return ((HprofFieldObjectValue) fieldValue).getInstance();
                } else {
                    return ((HprofFieldValue) fieldValue).getTypeValue();
                }
            }
        }

        return null;
    }

    private List /*<Field>*/ computeFields() {
        HprofByteBuffer buffer = getHprofBuffer();
        long offset = fileOffset + getInstanceFieldOffset();
        int i;
        int fields = buffer.getShort(offset);
        List filedsList = new ArrayList(fields);

        for (i = 0; i < fields; i++) {
            filedsList.add(new HprofField(this, offset + 2 + (i * classDumpSegment.fieldSize)));
        }

        return filedsList;
    }
    
    List getAllInstanceFields() {
        List fields = new ArrayList(50);

        for (JavaClass jcls = this; jcls != null; jcls = jcls.getSuperClass()) {
            fields.addAll(jcls.getFields());
        }

        return fields;
    }

    void setClassLoadOffset(long offset) {
        loadClassOffset = offset;
    }

    int getConstantPoolSize() {
        long cpOffset = fileOffset + classDumpSegment.constantPoolSizeOffset;
        HprofByteBuffer buffer = getHprofBuffer();
        int cpRecords = buffer.getShort(cpOffset);
        HprofHeap heap = getHprof();

        cpOffset += 2;

        for (int i = 0; i < cpRecords; i++) {
            byte type = buffer.get(cpOffset + 2);
            int size = heap.getValueSize(type);
            cpOffset += (2 + 1 + size);
        }

        return (int) (cpOffset - (fileOffset + classDumpSegment.constantPoolSizeOffset));
    }

    HprofHeap getHprof() {
        return classDumpSegment.hprofHeap;
    }

    HprofByteBuffer getHprofBuffer() {
        return classDumpSegment.hprofHeap.dumpBuffer;
    }

    int getInstanceFieldOffset() {
        int staticFieldOffset = getStaticFieldOffset();

        return staticFieldOffset + getStaticFiledSize(staticFieldOffset);
    }

    LoadClass getLoadClass() {
        return new LoadClass(getHprof().getLoadClassSegment(), loadClassOffset);
    }

    List getReferences() {
        return getHprof().findReferencesFor(getJavaClassId());
    }

    int getStaticFieldOffset() {
        return classDumpSegment.constantPoolSizeOffset + getConstantPoolSize();
    }

    int getStaticFiledSize(int staticFieldOffset) {
        int i;
        HprofByteBuffer buffer = getHprofBuffer();
        int idSize = buffer.getIDSize();
        long fieldOffset = fileOffset + staticFieldOffset;
        int fields = getHprofBuffer().getShort(fieldOffset);
        HprofHeap heap = getHprof();

        fieldOffset += 2;

        for (i = 0; i < fields; i++) {
            byte type = buffer.get(fieldOffset + idSize);
            int size = heap.getValueSize(type);
            fieldOffset += (idSize + 1 + size);
        }

        return (int) (fieldOffset - staticFieldOffset - fileOffset);
    }

    void findStaticReferencesFor(long instanceId, List refs) {
        int i;
        HprofByteBuffer buffer = getHprofBuffer();
        int idSize = buffer.getIDSize();
        long fieldOffset = fileOffset + getStaticFieldOffset();
        int fields = getHprofBuffer().getShort(fieldOffset);
        List staticFileds = null;
        HprofHeap heap = getHprof();

        fieldOffset += 2;

        for (i = 0; i < fields; i++) {
            byte type = buffer.get(fieldOffset + idSize);
            int size = heap.getValueSize(type);

            if ((type == HprofHeap.OBJECT) && (instanceId == buffer.getID(fieldOffset + idSize + 1))) {
                if (staticFileds == null) {
                    staticFileds = getStaticFieldValues();
                }

                refs.add(staticFileds.get(i));
            }

            fieldOffset += (idSize + 1 + size);
        }
    }

    void incrementInstance() {
        instances++;
    }

    private static Boolean isSubClass(JavaClass jcls, Map subclassesMap) {
        JavaClass superClass = jcls.getSuperClass();
        Boolean b;

        if (superClass == null) {
            b = Boolean.FALSE;
        } else {
            b = (Boolean) subclassesMap.get(superClass);

            if (b == null) {
                b = isSubClass(superClass, subclassesMap);
            }
        }

        subclassesMap.put(jcls, b);

        return b;
    }
}
