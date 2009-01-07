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

package org.netbeans;

import java.io.UnsupportedEncodingException;
import org.openide.modules.PatchedPublic;

/**
 * Tool to patch bytecode, currently just to make access modifiers public.
 * Determines when and what to patch based on class-retention annotations.
 * @see PatchedPublic
 * @see #patch
 */
public final class PatchByteCode {

    private static final byte[] RUNTIME_INVISIBLE_ANNOTATIONS, PATCHED_PUBLIC;
    static {
        try {
            RUNTIME_INVISIBLE_ANNOTATIONS = "RuntimeInvisibleAnnotations".getBytes("UTF-8"); // NOI18N
            PATCHED_PUBLIC = ("L" + PatchedPublic.class.getName().replace('.', '/') + ";").getBytes("UTF-8"); // NOI18N
        } catch (UnsupportedEncodingException x) {
            throw new ExceptionInInitializerError(x);
        }
    }

    /**
     * Patches a class if it is needed.
     * @param arr the bytecode
     * @return the enhanced bytecode
     */
    public static byte[] patch(byte[] data) {
        int constant_pool_count = u2(data, /* magic + minor_version + major_version */ 8);
        int[] constantPoolOffsets = new int[constant_pool_count];
        int pos = 10; // 8 + constant_pool_count
        for (int i = 1; i < constant_pool_count; i++) {
            int tag = u1(data, pos++);
            //System.err.println("tag " + tag + " at #" + i + " at location " + pos);
            constantPoolOffsets[i] = pos;
            switch (tag) {
            case 1: // CONSTANT_Utf8
                int len = u2(data, pos);
                //try {System.err.println("UTF-8 constant: " + new String(data, pos + 2, len, "UTF-8"));} catch (UnsupportedEncodingException x) {}
                pos += len + 2;
                break;
            case 3: // CONSTANT_Integer
            case 4: // CONSTANT_Float
            case 9: // CONSTANT_Fieldref
            case 10: // CONSTANT_Methodref
            case 11: // CONSTANT_InterfaceMethodref
            case 12: // CONSTANT_NameAndType
                pos += 4;
                break;
            case 7: // CONSTANT_Class
            case 8: // CONSTANT_String
                pos += 2;
                break;
            case 5: // CONSTANT_Long
            case 6: // CONSTANT_Double
                pos += 8;
                i++; // next entry is ignored
                break;
            default:
                throw new IllegalArgumentException("illegal constant pool tag " + tag + " at index " + i + " out of " + constant_pool_count);
            }
        }
        pos += 6; // access_flags + this_class + super_class
        int interfaces_count = u2(data, pos);
        pos += 2; // interfaces_count
        pos += 2 * interfaces_count; // interfaces
        int fields_count = u2(data, pos);
        pos += 2; // fields_count
        for (int i = 0; i < fields_count; i++) {
            pos += 6; // access_flags + name_index + descriptor_index
            int attributes_count = u2(data, pos);
            pos += 2; // attributes_count
            for (int j = 0; j < attributes_count; j++) {
                pos += 2; // attribute_name_index
                int attribute_length = u4(data, pos);
                pos += 4; // attribute_length
                pos += attribute_length; // info
            }
        }
        int methods_count = u2(data, pos);
        pos += 2; // methods_count
        for (int i = 0; i < methods_count; i++) {
            int locationOfAccessFlags = pos;
            pos += 6; // access_flags + name_index + descriptor_index
            int attributes_count = u2(data, pos);
            pos += 2; // attributes_count
            for (int j = 0; j < attributes_count; j++) {
                int locationOfAttributeName = constantPoolOffsets[u2(data, pos)];
                pos += 2; // attribute_name_index
                int attribute_length = u4(data, pos);
                pos += 4; // attribute_length
                if (utf8Matches(data, locationOfAttributeName, RUNTIME_INVISIBLE_ANNOTATIONS)) {
                    int num_annotations = u2(data, pos);
                    int pos2 = pos + 2; // num_annotations
                    for (int k = 0; k < num_annotations; k++) {
                        if (utf8Matches(data, constantPoolOffsets[u2(data, pos2)], PATCHED_PUBLIC)) {
                            // Got it, we are setting the method to be public.
                            data[locationOfAccessFlags + 1] &= 0xF9; // - ACC_PRIVATE - ACC_PROTECTED
                            data[locationOfAccessFlags + 1] |= 0x01; // + ACC_PUBLIC
                        }
                        // XXX skip over annotation body so we can support >1 annotation on the member
                        // (i.e. @PatchedPublic occurs only after other annotations)
                        // but it is tedious to calculate the length of element_value structs
                        continue;
                    }
                }
                pos += attribute_length; // info
            }
        }
        return data;
    }

    private static int u1(byte[] data, int off) {
        byte b = data[off];
        return b >= 0 ? b : b + 256;
    }
    private static int u2(byte[] data, int off) {
        return (u1(data, off) << 8) + u1(data, off + 1);
    }
    private static int u4(byte[] data, int off) {
        return (u2(data, off) << 16) + u2(data, off + 2);
    }

    private static boolean utf8Matches(byte[] data, int off, byte[] expected) {
        if (u2(data, off) != expected.length) {
            return false;
        }
        for (int i = 0; i < expected.length; i++) {
            if (data[off + 2 + i] != expected[i]) {
                return false;
            }
        }
        return true;
    }
    
}
