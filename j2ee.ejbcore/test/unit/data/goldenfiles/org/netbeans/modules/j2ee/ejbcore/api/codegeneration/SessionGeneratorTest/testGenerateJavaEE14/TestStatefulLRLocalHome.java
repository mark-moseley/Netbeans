/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package testGenerateJavaEE14;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

/**
 *
 * @author {user}
 */
public interface TestStatefulLRLocalHome extends EJBLocalHome {
    
    testGenerateJavaEE14.TestStatefulLRLocal create()  throws CreateException;

}
