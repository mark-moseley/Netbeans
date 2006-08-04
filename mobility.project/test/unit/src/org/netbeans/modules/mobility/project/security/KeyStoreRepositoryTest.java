/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * KeyStoreRepositoryTest.java
 * JUnit based test
 *
 * Created on 16 February 2006, 11:30
 */
package org.netbeans.modules.mobility.project.security;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import org.netbeans.core.startup.NbRepository;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.mobility.project.TestUtil;
import org.netbeans.modules.mobility.project.security.KeyStoreRepository.KeyStoreBean.KeyAliasBean;
import org.openide.filesystems.Repository;
import org.openide.util.Lookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import sun.security.tools.KeyTool;

/**
 *
 * @author lukas
 */
public class KeyStoreRepositoryTest extends NbTestCase {
    static
    {
        TestUtil.setLookup(new Lookup[] {Lookups.fixed(new Object[] {KeyStoreRepository.class},new InstanceContent.Convertor() {
            public Object convert(Object obj) {
                if (obj == KeyStoreRepository.class)
                    return KeyStoreRepository.createRepository();
                if (obj == Repository.class)
                    return Repository.getDefault();
                return null;
            }
            
            public Class type(Object obj) {
                return (Class)obj;
            }
            
            public String id(Object obj) {
                return obj.toString();
            }
            
            public String displayName(Object obj) {
                return ((Class)obj).getName();
            }
        } ),Lookups.metaInfServices(NbRepository.class.getClassLoader())});
    }
    
    public KeyStoreRepositoryTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        System.setProperty("system.dir",getWorkDir().getAbsolutePath());
    }
    
    protected void tearDown() throws Exception {
    }
    
    
    //This method must be the first of test methods executed to make sure that getDefault is using correct path
    public void testBean() throws IOException {
        KeyStoreRepository defRep=KeyStoreRepository.getDefault();
        assertNotNull(defRep);
        List l=defRep.getKeyStores();
        assertNotNull(l);
        assertTrue(l.size()==1);
        KeyStoreRepository.KeyStoreBean bean=(KeyStoreRepository.KeyStoreBean)l.get(0);
        assertNotNull(bean);
        assertTrue(bean.isValid());
        assertTrue(bean.isOpened());
        File f=bean.getKeyStoreFile();
        assertNotNull(f);
        assertTrue(f.getPath().equals(getWorkDir().getPath()+ File.separator +"j2me" + File.separator + "builtin.ks"));
        String s=bean.getKeyStorePath();
        assertTrue(s.equals(getWorkDir().getPath()+ File.separator +"j2me" + File.separator + "builtin.ks"));
        
        String pass=bean.getPassword();
        assertEquals(pass,"password");
        bean.setPassword("newPassword");
        pass=bean.getPassword();
        assertEquals(pass,"newPassword");
        
        Set set=bean.aliasses();
        assertNotNull(set);
        assertTrue(set.size()==3);
        assertTrue(bean.getType().equals("JKS"));
        assertNull(bean.getAlias("fake"));
        KeyStoreRepository.KeyStoreBean.KeyAliasBean alias=bean.createInvalidKeyAliasBean("fake");
        assertNotNull(alias);
        
        //just to get code coverage right
        bean.hashCode();
        bean.getStore();
        bean.setKeyStoreFile(bean.getKeyStoreFile());
    }
    
    public void testWarmUp() {
        Thread check=new Thread(new KeyStoreRepositoryWarmUp());
        check.start();
    }
    
    
    public void testAlias() throws IOException {
        Date date;
        
        assertFalse(KeyStoreRepository.isDefaultKeystore(null));
        KeyStoreRepository defRep=KeyStoreRepository.getDefault();
        assertNotNull(defRep);
        List l=defRep.getKeyStores();
        assertNotNull(l);
        assertTrue(l.size()==1);
        KeyStoreRepository.KeyStoreBean bean=(KeyStoreRepository.KeyStoreBean)l.get(0);
        assertNotNull(bean);
        Set set=bean.aliasses();
        assertNotNull(set);
        assertTrue(set.size()==3);
        Object als[]=set.toArray();
        KeyAliasBean alias=(KeyAliasBean) als[1];
        assertEquals(alias.getIssuerName(),"CN=minimal");
        
        //MD5 can't be tested so i called it just to get coverage
        alias.getMd5();
        
        //Serial Number can't be tested so i called it just to get coverage
        alias.getSerialNumber();
        
        //SHa can't be tested so i called it just to get coverage
        alias.getSha();
        
        assertEquals(alias.getPassword(),"password");
        String result=alias.getSubjectName();
        assertEquals(result,"CN=minimal");
        long now=System.currentTimeMillis();
        Calendar cal=Calendar.getInstance();
        cal.setTimeInMillis(now);
        now/=(1000*60*60*24);
        cal.add(Calendar.DAY_OF_YEAR,180);
        date=alias.getNotAfter();
        long after=date.getTime()/(1000*60*60*24);
        date=alias.getNotBefore();
        long before=date.getTime()/(1000*60*60*24);
        assertTrue(now==before);
        now=cal.getTimeInMillis()/(1000*60*60*24);
        assertTrue(now==after);
        assertTrue(alias.isValid());
        
        //just to get code coverage right
        alias.hashCode();
    }
    
    public void testKeystore() throws Exception {
        KeyStoreRepository defRep=KeyStoreRepository.getDefault();
        assertNotNull(defRep);
        KeyStoreRepository.KeyStoreBean bean=defRep.getKeyStore("testKeyStore",false);
        assertNull(bean);
        bean=defRep.getKeyStore("testKeyStore",true);
        assertNotNull(bean);
        assertFalse(bean.isValid());
        assertFalse(bean.isOpened());
        assertFalse(defRep.isDefaultKeystore(bean));
        Object o=defRep.getPassword("testFile");
        assertNull(o);
        Object o1=new String[] {"test1","test2"};
        o=defRep.putPassword("testFile",o1);
        assertNull(o);
        o=defRep.getPassword("testFile");
        assertEquals(o1,o);
        o=defRep.removePassword("testFile");
        assertEquals(o1,o);
        o=defRep.getPassword("testFile");
        assertNull(o);
        bean=KeyStoreRepository.KeyStoreBean.create(getWorkDir().getAbsolutePath()+"/testStore.p12","pass123456");
        defRep.addKeyStore(bean);
        KeyStoreRepository.KeyStoreBean bean1=defRep.getKeyStore(getWorkDir().getAbsolutePath()+"/testStore.p12",false);
        assertEquals(bean,bean1);
        
        PropertyChangeListener listener=new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {}
        };
        
        defRep.addPropertyChangeListener(listener);
        
        //Check defaulting of type
        bean.setType("fake");
        
        //And now correct type
        bean.setType("pkcs12");
        assertTrue(bean.openKeyStore(true));
        bean.addKeyToStore("trusted", "CN=trusted", "password", -1); // NOI18N
        bean.addKeyToStore("untrusted", "CN=untrusted", "password", -1); // NOI18N
        bean.addKeyToStore("minimal", "CN=minimal", "password", -1); // NOI18N
        Set set=bean.aliasses();
        Object als[]=set.toArray();
        KeyStoreRepository.KeyStoreBean.KeyAliasBean alias=(KeyStoreRepository.KeyStoreBean.KeyAliasBean)als[0];
        assertNotNull(bean.getAlias(alias.getAlias()));
        assertTrue(bean.removeAliasFromStore(alias));
        Set set1=bean.aliasses();
        assertTrue(set.size()-1==set1.size());
        assertNull(bean.getAlias(alias.getAlias()));
        
        ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(getWorkDir()+"/testStore"));
        defRep.writeExternal(out);
        out.close();
        
        defRep.removeKeyStore(bean);
        bean1=defRep.getKeyStore(bean.getKeyStorePath(),false);
        assertNull(bean1);
        
        assertTrue(bean.closeKeyStore());
        assertTrue(!bean.isOpened());
        bean.openKeyStore();
        
        ObjectInputStream in=new ObjectInputStream(new FileInputStream(getWorkDir()+"/testStore"));
        defRep.readExternal(in);
        in.close();
        bean1=defRep.getKeyStore(bean.getKeyStorePath(),false);
        assertEquals(bean,bean1);
        
        defRep.removePropertyChangeListener(listener);
    }
}
