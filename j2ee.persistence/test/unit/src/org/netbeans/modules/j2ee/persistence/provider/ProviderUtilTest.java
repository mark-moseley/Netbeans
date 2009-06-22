/**
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


package org.netbeans.modules.j2ee.persistence.provider;

import java.io.File;
import java.net.URL;
import junit.framework.*;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.dd.common.Property;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests for ProviderUtil.
 * @author Erno Mononen
 */
public class ProviderUtilTest extends NbTestCase {
    
    private PersistenceUnit persistenceUnit1;
    private PersistenceUnit persistenceUnit2;
    
    public ProviderUtilTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        this.persistenceUnit1 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
        this.persistenceUnit2 = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit();
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ProviderUtilTest.class);
        return suite;
    }
    
    public void testGetProvider1() {
        persistenceUnit1.setProvider(ProviderUtil.HIBERNATE_PROVIDER.getProviderClass());
        assertEquals(ProviderUtil.HIBERNATE_PROVIDER, ProviderUtil.getProvider(persistenceUnit1));
    }
    

    public void testSetTableGeneration1(){
        Provider provider = ProviderUtil.TOPLINK_PROVIDER;
        persistenceUnit1.setProvider(provider.getProviderClass());
        
        ProviderUtil.setTableGeneration(persistenceUnit1, Provider.TABLE_GENERATION_CREATE, provider);
        assertPropertyExists(persistenceUnit1, provider.getTableGenerationPropertyName());
        assertValueExists(persistenceUnit1, provider.getTableGenerationCreateValue());
        assertNoSuchValue(persistenceUnit1, provider.getTableGenerationDropCreateValue());
        
        ProviderUtil.setTableGeneration(persistenceUnit1, Provider.TABLE_GENERATION_DROPCREATE, provider);
        assertPropertyExists(persistenceUnit1, provider.getTableGenerationPropertyName());
        assertValueExists(persistenceUnit1, provider.getTableGenerationDropCreateValue());
        assertNoSuchValue(persistenceUnit1, provider.getTableGenerationCreateValue());
        
    }
    
    public void testSetProvider1(){
        Provider provider = ProviderUtil.KODO_PROVIDER;
        ProviderUtil.setProvider(persistenceUnit1, provider, getConnection(), Provider.TABLE_GENERATTION_UNKOWN);
        assertEquals(provider.getProviderClass(), persistenceUnit1.getProvider());
        assertPropertyExists(persistenceUnit1, provider.getJdbcDriver());
        assertPropertyExists(persistenceUnit1, provider.getJdbcUrl());
        assertPropertyExists(persistenceUnit1, provider.getJdbcUsername());
    }
    
    public void testChangeProvider1(){
        Provider originalProvider = ProviderUtil.HIBERNATE_PROVIDER;
        ProviderUtil.setProvider(persistenceUnit1, originalProvider, getConnection(), Provider.TABLE_GENERATION_CREATE);
        assertEquals(originalProvider.getProviderClass(), persistenceUnit1.getProvider());
        
        Provider newProvider = ProviderUtil.TOPLINK_PROVIDER;
        ProviderUtil.setProvider(persistenceUnit1, newProvider, getConnection(), Provider.TABLE_GENERATION_DROPCREATE);
        // assert that old providers properties were removed
        assertNoSuchProperty(persistenceUnit1, originalProvider.getTableGenerationPropertyName());
        assertNoSuchProperty(persistenceUnit1, originalProvider.getJdbcDriver());
        assertNoSuchProperty(persistenceUnit1, originalProvider.getJdbcUrl());
        assertNoSuchProperty(persistenceUnit1, originalProvider.getJdbcUsername());
        // assert that new providers properties are set
        assertEquals(newProvider.getProviderClass(), persistenceUnit1.getProvider());
        assertPropertyExists(persistenceUnit1, newProvider.getJdbcDriver());
        assertPropertyExists(persistenceUnit1, newProvider.getJdbcUrl());
        assertPropertyExists(persistenceUnit1, newProvider.getJdbcUsername());
        assertPropertyExists(persistenceUnit1, newProvider.getTableGenerationPropertyName());
    }
    
    /**
     * Tests that changing of provider preserves existing
     * table generation value.
     */
    public void testTableGenerationPropertyIsPreserved1(){
        Provider originalProvider = ProviderUtil.KODO_PROVIDER;
        ProviderUtil.setProvider(persistenceUnit1, originalProvider, getConnection(), Provider.TABLE_GENERATION_CREATE);
        
        Provider newProvider = ProviderUtil.TOPLINK_PROVIDER;
        ProviderUtil.setProvider(persistenceUnit1, newProvider, getConnection(), Provider.TABLE_GENERATION_CREATE);
        assertEquals(newProvider.getTableGenerationPropertyName(),
                ProviderUtil.getProperty(persistenceUnit1, newProvider.getTableGenerationPropertyName()).getName());
        assertEquals(newProvider.getTableGenerationCreateValue(),
                ProviderUtil.getProperty(persistenceUnit1, newProvider.getTableGenerationPropertyName()).getValue());
        
        
        
    }
    
    public void testRemoveProviderProperties1(){
        Provider provider = ProviderUtil.KODO_PROVIDER;
        PersistenceUnit persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit();
        ProviderUtil.setProvider(persistenceUnit, provider, getConnection(), Provider.TABLE_GENERATION_CREATE);
        //        ProviderUtil.setTableGeneration(persistenceUnit, Provider.TABLE_GENERATION_CREATE, provider);
        
        ProviderUtil.removeProviderProperties(persistenceUnit);
        assertNoSuchProperty(persistenceUnit1, provider.getTableGenerationPropertyName());
        assertNoSuchProperty(persistenceUnit1, provider.getJdbcDriver());
        assertNoSuchProperty(persistenceUnit1, provider.getJdbcUrl());
        assertNoSuchProperty(persistenceUnit1, provider.getJdbcUsername());
        
    }
    
    
    public void testGetPUDataObject1() throws Exception{
        String invalidPersistenceXml = getDataDir().getAbsolutePath() + File.separator + "invalid_persistence.xml";
        FileObject invalidPersistenceFO = FileUtil.toFileObject(new File(invalidPersistenceXml));
        try{
            ProviderUtil.getPUDataObject(invalidPersistenceFO);
            fail("InvalidPersistenceXmlException should have been thrown");
        } catch (InvalidPersistenceXmlException ipx){
            assertEquals(invalidPersistenceXml, ipx.getPath());
        }
        
    }

    public void testGetProvider2() {
        persistenceUnit2.setProvider(ProviderUtil.HIBERNATE_PROVIDER.getProviderClass());
        assertEquals(ProviderUtil.HIBERNATE_PROVIDER, ProviderUtil.getProvider(persistenceUnit2));
    }

    public void testSetTableGeneration2(){
        Provider provider = ProviderUtil.TOPLINK_PROVIDER;
        persistenceUnit2.setProvider(provider.getProviderClass());

        ProviderUtil.setTableGeneration(persistenceUnit2, Provider.TABLE_GENERATION_CREATE, provider);
        assertPropertyExists(persistenceUnit2, provider.getTableGenerationPropertyName());
        assertValueExists(persistenceUnit2, provider.getTableGenerationCreateValue());
        assertNoSuchValue(persistenceUnit2, provider.getTableGenerationDropCreateValue());

        ProviderUtil.setTableGeneration(persistenceUnit2, Provider.TABLE_GENERATION_DROPCREATE, provider);
        assertPropertyExists(persistenceUnit2, provider.getTableGenerationPropertyName());
        assertValueExists(persistenceUnit2, provider.getTableGenerationDropCreateValue());
        assertNoSuchValue(persistenceUnit2, provider.getTableGenerationCreateValue());

    }

    public void testSetProvider2(){
        Provider provider = ProviderUtil.KODO_PROVIDER;
        ProviderUtil.setProvider(persistenceUnit2, provider, getConnection(), Provider.TABLE_GENERATTION_UNKOWN);
        assertEquals(provider.getProviderClass(), persistenceUnit2.getProvider());
        assertPropertyExists(persistenceUnit2, provider.getJdbcDriver());
        assertPropertyExists(persistenceUnit2, provider.getJdbcUrl());
        assertPropertyExists(persistenceUnit2, provider.getJdbcUsername());
    }

    public void testChangeProvider2(){
        Provider originalProvider = ProviderUtil.HIBERNATE_PROVIDER;
        ProviderUtil.setProvider(persistenceUnit2, originalProvider, getConnection(), Provider.TABLE_GENERATION_CREATE);
        assertEquals(originalProvider.getProviderClass(), persistenceUnit2.getProvider());

        Provider newProvider = ProviderUtil.TOPLINK_PROVIDER;
        ProviderUtil.setProvider(persistenceUnit2, newProvider, getConnection(), Provider.TABLE_GENERATION_DROPCREATE);
        // assert that old providers properties were removed
        assertNoSuchProperty(persistenceUnit2, originalProvider.getTableGenerationPropertyName());
        assertNoSuchProperty(persistenceUnit2, originalProvider.getJdbcDriver());
        assertNoSuchProperty(persistenceUnit2, originalProvider.getJdbcUrl());
        assertNoSuchProperty(persistenceUnit2, originalProvider.getJdbcUsername());
        // assert that new providers properties are set
        assertEquals(newProvider.getProviderClass(), persistenceUnit2.getProvider());
        assertPropertyExists(persistenceUnit2, newProvider.getJdbcDriver());
        assertPropertyExists(persistenceUnit2, newProvider.getJdbcUrl());
        assertPropertyExists(persistenceUnit2, newProvider.getJdbcUsername());
        assertPropertyExists(persistenceUnit2, newProvider.getTableGenerationPropertyName());
    }

    /**
     * Tests that changing of provider preserves existing
     * table generation value.
     */
    public void testTableGenerationPropertyIsPreserved2(){
        Provider originalProvider = ProviderUtil.KODO_PROVIDER;
        ProviderUtil.setProvider(persistenceUnit2, originalProvider, getConnection(), Provider.TABLE_GENERATION_CREATE);

        Provider newProvider = ProviderUtil.TOPLINK_PROVIDER;
        ProviderUtil.setProvider(persistenceUnit2, newProvider, getConnection(), Provider.TABLE_GENERATION_CREATE);
        assertEquals(newProvider.getTableGenerationPropertyName(),
                ProviderUtil.getProperty(persistenceUnit2, newProvider.getTableGenerationPropertyName()).getName());
        assertEquals(newProvider.getTableGenerationCreateValue(),
                ProviderUtil.getProperty(persistenceUnit2, newProvider.getTableGenerationPropertyName()).getValue());



    }

    public void testRemoveProviderProperties2(){
        Provider provider = ProviderUtil.KODO_PROVIDER;
        PersistenceUnit persistenceUnit = new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.PersistenceUnit();
        ProviderUtil.setProvider(persistenceUnit, provider, getConnection(), Provider.TABLE_GENERATION_CREATE);
        //        ProviderUtil.setTableGeneration(persistenceUnit, Provider.TABLE_GENERATION_CREATE, provider);

        ProviderUtil.removeProviderProperties(persistenceUnit);
        assertNoSuchProperty(persistenceUnit1, provider.getTableGenerationPropertyName());
        assertNoSuchProperty(persistenceUnit1, provider.getJdbcDriver());
        assertNoSuchProperty(persistenceUnit1, provider.getJdbcUrl());
        assertNoSuchProperty(persistenceUnit1, provider.getJdbcUsername());

    }
    

    /**
     * Asserts that property with given name exists in persistence unit.
     */
    protected void assertPropertyExists(PersistenceUnit pu, String propertyName){
        if (!propertyExists(pu, propertyName)){
            fail("Property " + propertyName + " was not found.");
        }
        assertTrue(true);
    }
    
    /**
     * Asserts that no property with given name exists in persistence unit.
     */
    protected void assertNoSuchProperty(PersistenceUnit pu, String propertyName){
        if (propertyExists(pu, propertyName)){
            fail("Property " + propertyName + " was found.");
        }
        assertTrue(true);
    }
    
    protected void assertNoSuchValue(PersistenceUnit pu, String value){
        if (valueExists(pu, value)){
            fail("Property with value " + value + " was found");
        }
        assertTrue(true);
    }
    
    protected void assertValueExists(PersistenceUnit pu, String value){
        if (!valueExists(pu, value)){
            fail("Property with value " + value + " was not found");
        }
        assertTrue(true);
    }
    
    
    /**
     * @return true if property with given name exists in persistence unit,
     * false otherwise.
     */
    protected boolean propertyExists(PersistenceUnit pu, String propertyName){
        Property[] properties = ProviderUtil.getProperties(pu);
        for (int i = 0; i < properties.length; i++) {
            if (properties[i].getName().equals(propertyName)){
                return true;
            }
        }
        return false;
    }
    
    /**
     * @return true if property with given value exists in persistence unit,
     * false otherwise.
     */
    protected boolean valueExists(PersistenceUnit pu, String propertyValue){
        Property[] properties = ProviderUtil.getProperties(pu);
        for (int i = 0; i < properties.length; i++) {
            if (properties[i].getValue().equals(propertyValue)){
                return true;
            }
        }
        return false;
    }
    
    private DatabaseConnection getConnection(){
        JDBCDriver driver = JDBCDriver.create("driver", "driver", "foo.bar", new URL[]{});
        return DatabaseConnection.create(driver, "foo", "bar", "schema", "password", false);
    }
    
}