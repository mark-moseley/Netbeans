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

package org.openide.nodes;

import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;



/**
 * @author Some Czech
 */
public class PropertiesTest extends NbTestCase {

    public PropertiesTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(PropertiesTest.class));
    }

    public void testReflection() throws Exception {

        Node.Property np = null;

        // Test normal property
        TestBean tb = new TestBean();
        np = new PropertySupport.Reflection( tb, int.class, "number" );        
        assertEquals( "Value", np.getValue(), new Integer( 1 ) ); 
        
        // Test setter only of type String
        NoSuchMethodException thrownException = null;
        try {
            np = new PropertySupport.Reflection( tb, String.class, "setterOnlyString" );                
        }
        catch ( NoSuchMethodException e  ){
            thrownException = e;
        }        
        assertNotNull( "Exception should be thrown", thrownException );
        
        // Test setter only of type boolean
        thrownException = null;
        try {
            np = new PropertySupport.Reflection( tb, boolean.class, "setterOnlyBoolean" );                
        }
        catch ( NoSuchMethodException e  ){
            thrownException = e;
        }        
        assertNotNull( "Exception should be thrown", thrownException );
        
        // Test no boolean with is
        thrownException = null;
        try {
            np = new PropertySupport.Reflection( tb, long.class, "isSetLong" );                
        }
        catch ( NoSuchMethodException e  ){
            thrownException = e;
        }        
        assertNotNull( "Exception should be thrown", thrownException );
        
        
        // Test get/set boolean
        np = new PropertySupport.Reflection( tb, boolean.class, "getSetBoolean" );        
        assertEquals( "Value", np.getValue(), Boolean.TRUE ); 
                
        // Test is/set boolean
        np = new PropertySupport.Reflection( tb, boolean.class, "isSetBoolean" );        
        assertEquals( "Value", np.getValue(), Boolean.TRUE ); 
        
    }
    
    public static class TestBean {
        
        public int getNumber() {
            return 1;
        }
        
        public void setNumber( int number ) {
        }
        
        public void setSetterOnlyString( String text ) {
        }
        
        public void setSetterOnlyBoolean( boolean value ) {
        }
        
        public long isIsSetLong() {
            return 10L;
        }
        
        public void setIsSetLong( long value ) {
        }
        
        public boolean getGetSetBoolean() {
            return true;
        }
        
        public void setGetSetBoolean( boolean value ) {
        }
        
        
        public boolean isIsSetBoolean() {
            return true;
        }
        
        public void setIsSetBoolean( boolean value ) {
        }
    }
    

}
