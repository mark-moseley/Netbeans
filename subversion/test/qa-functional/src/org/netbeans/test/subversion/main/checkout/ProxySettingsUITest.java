/*
 * ProxySettingsUITest.java
 *
 * Created on 10 May 2006, 11:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.subversion.main.checkout;

import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.ProxyConfigurationOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;

/**
 *
 * @author peter
 */
public class ProxySettingsUITest extends JellyTestCase {
    
    String os_name;
    
    /** Creates a new instance of ProxySettingsUITest */
    public ProxySettingsUITest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {        
        os_name = System.getProperty("os.name");
        //System.out.println(os_name);
        System.out.println("### "+getName()+" ###");
        
    }
    
    protected boolean isUnix() {
        boolean unix = false;
        if (os_name.indexOf("Windows") == -1) {
            unix = true;
        }
        return unix;
    }
    
    public static void main(String[] args) {
        // TODO code application logic here
        TestRunner.run(suite());
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ProxySettingsUITest("testProxySettings"));
        suite.addTest(new ProxySettingsUITest("testProxyBeforeUrl"));
        return suite;
    }    
    
    public void testProxySettings() {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        RepositoryStepOperator co1so = new RepositoryStepOperator();
        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_HTTPS + "localhost");
        ProxyConfigurationOperator pco = co1so.invokeProxy();
        pco.verify();
        pco.useSystemProxySettings();
        pco.noProxyDirectConnection();
        pco.hTTPProxy();
        pco.sOCKSProxy();
        pco.setProxyHost("host");// NOI18N
        pco.setPort("8080");
        pco.checkProxyServerRequiresLogin(true);
        pco.setName("name");// NOI18N
        pco.setPassword("password");// NOI18N
        pco.ok();
        co.btCancel().pushNoBlock();
    }
    
    public void testProxyBeforeUrl() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        RepositoryStepOperator co1so = new RepositoryStepOperator();
        co1so.setRepositoryURL(RepositoryStepOperator.ITEM_HTTPS);
        Exception exc = null;
        ProxyConfigurationOperator pco;
        try {
            co1so.invokeProxy();
        } catch (Exception e) {
            exc = e;
            System.out.println("Exception is thrown. Proxy dialog was not opened!!!");
            System.out.println("http://www.netbeans.org/issues/show_bug.cgi?id=76111");
            //e.printStackTrace();
        }
        assertNotNull(exc);        
        co.btCancel().pushNoBlock();
    }
}
