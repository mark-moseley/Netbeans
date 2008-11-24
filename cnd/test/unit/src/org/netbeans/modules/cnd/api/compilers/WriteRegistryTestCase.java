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

package org.netbeans.modules.cnd.api.compilers;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.api.compilers.ToolchainManager.ToolchainDescriptor;


/**
 *
 * @author Alexander Simon
 */
public class WriteRegistryTestCase extends NbTestCase {
    
    public WriteRegistryTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testWrtiteDescriptor() throws Exception {
        List<ToolchainDescriptor> original = ToolchainManager.getInstance().getAllToolchains();
        ((ToolchainManagerImpl)ToolchainManager.getInstance()).writeToolchains();
        ((ToolchainManagerImpl)ToolchainManager.getInstance()).reinitToolchainManager();
        List<ToolchainDescriptor> restored = ToolchainManager.getInstance().getAllToolchains();
        for(int i = 0; i < original.size(); i++) {
           assertTrue("Tool chain "+original.get(i)+" not equals "+restored.get(i), deepObjectComparing(original.get(i),restored.get(i)));
        }
    }

    private boolean deepObjectComparing(Object original, Object restored){
        if (!original.getClass().equals(restored.getClass())){
            System.out.println("Class "+original.getClass()+" not equals "+restored.getClass());
            return false;
        }
        Field[] fields = original.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++){
            try {
                Object o1 = fields[i].get(original);
                Object o2 = fields[i].get(restored);
                if (o1 instanceof ToolchainManagerImpl.Compiler){
                     if (!((ToolchainManagerImpl.Compiler)o1).isValid() && !((ToolchainManagerImpl.Compiler)o1).isValid()){
                         continue;
                     }
                }
                if (o1 == null && o2 == null){
                    // both objects have null references
                    continue;
                } else if (o1 != null && o2 == null) {
                    System.out.println("Fields "+fields[i].getName()+" in class "+original.getClass()+" not equal: o1 != null && o2 == null");
                    return false;
                } else if (o1 == null && o2 != null) {
                    System.out.println("Fields "+fields[i].getName()+" in class "+original.getClass()+" not equal: o1 == null && o2 != null");
                    return false;
                }
                if (o1 instanceof String) {
                    if (!o1.equals(o2)){
                        System.out.println("String fields "+fields[i].getName()+" in class "+original.getClass()+" not equal: "+o1+" != "+o2);
                        return false;
                    }
                } else if (o1 instanceof Boolean) {
                    if (!o1.equals(o2)){
                        System.out.println("Boolean fields "+fields[i].getName()+" in class "+original.getClass()+" not equal: "+o1+" != "+o2);
                        return false;
                    }
                } else if (o1 instanceof Integer) {
                    if (!o1.equals(o2)){
                        System.out.println("Integer fields "+fields[i].getName()+" in class "+original.getClass()+" not equal: "+o1+" != "+o2);
                        return false;
                    }
                } else if (o1 instanceof String[]) {
                    if (!Arrays.equals((String[])o1, (String[])o2)){
                        System.out.println("String[] fields "+fields[i].getName()+" in class "+original.getClass()+" not equal:\n\t"+
                                Arrays.toString((String[])o1)+"\n\t"+Arrays.toString((String[])o2));
                        return false;
                    }
                } else if (o1 instanceof Map) {
                    Map m1 = (Map)o1;
                    Map m2 = (Map)o2;
                    if (!m1.equals(m2)){
                        System.out.println("Map fields "+fields[i].getName()+" in class "+original.getClass()+" not equal:\n\t"+
                                m1+"\n\t"+m2);
                        return false;
                    }
                } else if (o1 instanceof Collection) {
                    Iterator i1 = ((Collection)o1).iterator();
                    Iterator i2 = ((Collection)o2).iterator();
                    while(i1.hasNext() && i2.hasNext()) {
                        if (!deepObjectComparing(i1.next(), i2.next())){
                            return false;
                        }
                    }
                    if (i1.hasNext() || i2.hasNext()){
                        System.out.println("Collection fields "+fields[i].getName()+" in class "+original.getClass()+" not equal:\n\t"+
                                o1+"\n\t"+o2);
                        return false;
                    }
                } else {
                    if (!deepObjectComparing(o1, o2)){
                        return false;
                    }
                }
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
                return false;
            } catch (IllegalAccessException ex) {
            }
        }
        return true;
    }

}
