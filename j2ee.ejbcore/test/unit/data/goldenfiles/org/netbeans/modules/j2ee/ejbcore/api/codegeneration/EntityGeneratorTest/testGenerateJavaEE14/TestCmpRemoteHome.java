/*
 * TestCmpRemoteHome.java
 *
 * Created on {date}, {time}
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package testGenerateJavaEE14;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

/**
 *
 * @author {user}
 */
public interface TestCmpRemoteHome extends EJBHome {
    
    testGenerateJavaEE14.TestCmpRemote findByPrimaryKey(java.lang.Long key)  throws FinderException, RemoteException;
    
    testGenerateJavaEE14.TestCmpRemote create(java.lang.Long key)  throws CreateException, RemoteException;

}
