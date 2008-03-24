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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.repository.util;

import org.netbeans.modules.cnd.repository.disk.RepositoryCacheMap;
import org.netbeans.modules.cnd.test.BaseTestCase;

/**
 * test case for RepositoryCacheMap
 * @author Vladimir Kvashin
 */
public class RepositoryCacheMapTest extends BaseTestCase {

    public RepositoryCacheMapTest(String testName) {
	super(testName);
    }
    
    public void testNoSuchElementException() {
	int capacity = 2;
	RepositoryCacheMap map = new RepositoryCacheMap<String, String>(capacity);
	
	tryEqualKeys(map);
	tryEqualValues(map);
        
        Filter<String> filter = new Filter<String>() {
            public boolean accept(String value) {
                return true;
            }
        };
        
	map.remove(filter);

	tryEqualKeys(map);
	tryEqualValues(map);
        
	map.remove(filter);
    }

    private void tryEqualKeys(RepositoryCacheMap map) {
	String key = "key1";
	String value1 = "value1";
	String value2 = "value2";
	String value3 = "value3";
	String value4 = "value4";
	
	map.put(key, value1);
	map.put(key, value2);
	map.put(key, value3);
	map.put(key, value4);
	
	map.remove(key);
	
	map.put("a", "b");
    }
    
    private void tryEqualValues(RepositoryCacheMap map) {
	String key1 = "key1";
	String key2 = "key2";
	String key3 = "key3";
	String key4 = "key4";
	String val = "object";
	
	map.put(key1, val);
	map.put(key2, val);
	map.put(key3, val);
	map.put(key4, val);
	map.remove(key4);
	
	map.put("a", "b");
    }
}
