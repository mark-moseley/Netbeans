package test;

import javax.ejb.*;

/**
 * This is the bean class for the TestingSessionBean enterprise bean.
 * Created 29.4.2005 15:24:25
 * @author lm97939
 */
public class TestingSessionBean implements SessionBean, TestingSessionRemoteBusiness, TestingSessionLocalBusiness {
    private SessionContext context;
    
    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click the + sign on the left to edit the code.">
    // TODO Add code to acquire and use other enterprise resources (DataSource, JMS, enterprise bean, Web services)
    // TODO Add business methods or web service operations
    /**
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(SessionContext aContext) {
        context = aContext;
    }
    
    /**
     * @see javax.ejb.SessionBean#ejbActivate()
     */
    public void ejbActivate() {
        
    }
    
    /**
     * @see javax.ejb.SessionBean#ejbPassivate()
     */
    public void ejbPassivate() {
        
    }
    
    /**
     * @see javax.ejb.SessionBean#ejbRemove()
     */
    public void ejbRemove() {
        
    }
    // </editor-fold>
    
    /**
     * See section 7.10.3 of the EJB 2.0 specification
     * See section 7.11.3 of the EJB 2.1 specification
     */
    public void ejbCreate() {
        // TODO implement ejbCreate if necessary, acquire resources
        // This method has access to the JNDI context so resource aquisition
        // spanning all methods can be performed here such as home interfaces
        // and data sources.
    }
    
    
    
    // Add business logic below. (Right-click in editor and choose
    // "EJB Methods > Add Business Method" or "Web Service > Add Operation")

    public String testBusinessMethod1() {
        //TODO implement testBusinessMethod1
        return null;
    }

    public String testBusinessMethod2(String a, int b) throws Exception {
        //TODO implement testBusinessMethod2
        return null;
    }

    private test.TestingEntityLocalHome lookupTestingEntityBean() {
        try {
            javax.naming.Context c = new javax.naming.InitialContext();
            test.TestingEntityLocalHome rv = (test.TestingEntityLocalHome) c.lookup("java:comp/env/ejb/TestingEntityBean");
            return rv;
        }
        catch(javax.naming.NamingException ne) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,ne);
            throw new RuntimeException(ne);
        }
    }

    private test.TestingEntityRemoteHome lookupMyTestingEntityBean() {
        try {
            javax.naming.Context c = new javax.naming.InitialContext();
            Object remote = c.lookup("java:comp/env/ejb/MyTestingEntityBean");
            test.TestingEntityRemoteHome rv = (test.TestingEntityRemoteHome) javax.rmi.PortableRemoteObject.narrow(remote, test.TestingEntityRemoteHome.class);
            return rv;
        }
        catch(javax.naming.NamingException ne) {
            java.util.logging.Logger.getLogger(getClass().getName()).log(java.util.logging.Level.SEVERE,"exception caught" ,ne);
            throw new RuntimeException(ne);
        }
    }

    private javax.sql.DataSource getMyTestingDatabase() throws javax.naming.NamingException {
        javax.naming.Context c = new javax.naming.InitialContext();
        return (javax.sql.DataSource) c.lookup("java:comp/env/jdbc/myTestingDatabase");
    }
    
    
    
}
