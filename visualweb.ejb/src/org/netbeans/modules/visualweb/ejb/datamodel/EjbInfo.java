/*
 * EjbInfo.java
 *
 * Created on April 28, 2004, 4:58 PM
 */

package org.netbeans.modules.visualweb.ejb.datamodel;

import java.util.*;

/**
 * This class is to encapsulate the information Rave needs to know about an EJB
 *
 * @author cao
 */
public class EjbInfo implements java.lang.Cloneable, Comparable
{
    public final static int STATELESS_SESSION_BEAN = 1;
    public final static int STATEFUL_SESSION_BEAN = 2;
    public final static int ENTITY_BEAN = 3;
    public final static int MESSAGE_DRIVEN_BEAN = 4;
    
    private int beanType; // Must be one of the types listed above
    private String jndiName;
    private String ejbName;
    private String beanId;  // Can be null for SunAppServer and weblogic. But needed for websphere
    private String homeInterfaceName;
    private String compInterfaceName;
    private String webEjbRef;  // the ejb ref name used in web.xml/sun-web.xml
    
    private String beanWrapperName;
    private String beanInfoWrapperName;
    
    // A collection of business method - MethodInfo
    private ArrayList methodInfos = new ArrayList();
    
    /**
     * Creates a session bean info
     */
    public EjbInfo(String jndiName, String ejbName, String homeInterface, String compInterface)
    {
        this.beanType = STATELESS_SESSION_BEAN;
        this.jndiName = jndiName;
        this.ejbName = ejbName;
        this.homeInterfaceName = homeInterface;
        this.compInterfaceName = compInterface;
    }
    
    public EjbInfo() {};
    
    public void setBeanType( int type )
    {
        if( type != STATELESS_SESSION_BEAN &&
            type != STATEFUL_SESSION_BEAN &&
            type != ENTITY_BEAN &&
            type != MESSAGE_DRIVEN_BEAN )
            throw new java.lang.IllegalArgumentException( "Invalid EJB type: " + type );
        
        beanType = type;
    }
    
    public void setBeanId( String id )
    {
        this.beanId = id;
    }
    
    public void setEjbName( String name )
    {
        this.ejbName = name;
    }
    
    public void setJNDIName( String jndiName )
    {
        this.jndiName = jndiName;
    }
    
    public void setHomeInterfaceName( String interfaceName )
    {
        this.homeInterfaceName = interfaceName;
    }
    
    public void setCompInterfaceName( String interfaceName )
    {
        this.compInterfaceName = interfaceName;
    }
    
    public void setMethods( ArrayList methods )
    {
        this.methodInfos = methods;
    }
    
    public void addMethod( MethodInfo method )
    {
        if( methodInfos == null )
            methodInfos = new ArrayList();
        
        methodInfos.add( method );
    }
    
    public void setWebEjbRef( String webRef )
    {
        this.webEjbRef = webRef;
    }
    
    public void setBeanWrapperName( String name )
    {
        this.beanWrapperName = name;
    }
    
    public void setBeanInfoWrapperName( String name )
    {
        this.beanInfoWrapperName = name;
    }
    
    public boolean isStatelessSessionBean()
    {
        if( this.beanType == STATELESS_SESSION_BEAN )
            return true;
        else
            return false;
    }
    
    public int getBeanType() { return this.beanType; }
    public String getJNDIName() { return this.jndiName; }
    public String getEjbName() { return this.ejbName; }
    public String getBeanId() { return this.beanId; }
    public String getHomeInterfaceName() { return this.homeInterfaceName; }
    public String getCompInterfaceName() { return this.compInterfaceName; }
    public String getWebEjbRef() { return this.webEjbRef; }
    
    public ArrayList getMethods() 
    { 
        // Sort it first
        ArrayList methods = new ArrayList( this.methodInfos );
        Collections.sort( methods );
        return methods;
    }
    
    public boolean hasAnyMethodWithCollectionReturn()
    {
        for( Iterator iter = methodInfos.iterator(); iter.hasNext(); )
        {
            MethodInfo m = (MethodInfo)iter.next();
            if( m.getReturnType().isCollection() )
                return true;
        }
        
        // Didn't find any
        return false;
    }
    
    public boolean hasAnyConfigurableMethod()
    {
        for( Iterator iter = methodInfos.iterator(); iter.hasNext(); )
        {
            MethodInfo m = (MethodInfo)iter.next();
            if( m.isMethodConfigurable() )
                return true;
        }
        
        // Didn't find any
        return false;
    }
    
    public String getBeanTypeName()
    {
        // NOI18N
        switch( this.beanType )
        {
            case STATELESS_SESSION_BEAN:
            case STATEFUL_SESSION_BEAN:
                return "Session"; // NOI18N
            case ENTITY_BEAN:
                return "Entity"; // NOI18N
            case MESSAGE_DRIVEN_BEAN:
                return "Message Driven Bean"; // NOI18N
            default:
                return "Session"; // NOI18N
        }
    }
    
    public String getBeanWrapperName() 
    { 
         return this.beanWrapperName; 
    }
    
    public String getBeanInfoWrapperName() 
    { 
        return this.beanInfoWrapperName; 
    }
    
    /**
     * The EJB can be auto be auto init() if it only has one init() and
     * it takes no arguments
     */
    public boolean canBeAutoInit()
    {
        int numCreateMethods = 0;
        boolean foundNonArgCreateMethod = false;
        for( Iterator iter = methodInfos.iterator(); iter.hasNext(); )
        {
            MethodInfo method = (MethodInfo)iter.next();
            if( !method.isBusinessMethod() )
            {
                // Found a create method
                numCreateMethods ++;
                
                // And found a create method w/o arguemnt
                if( method.hasNoParameters() )
                    foundNonArgCreateMethod = true;
            }
        }
        
        if( numCreateMethods == 1 &&  foundNonArgCreateMethod )
            return true;
        else
            return false;
    }
    
    public String toString()
    {
        // NOI18N
        StringBuffer buf = new StringBuffer();
        buf.append( "Type: " + getBeanType() + "\n" ); 
        buf.append( "JNDI name: " + getJNDIName() + "\n" );
        buf.append( "EJB name: " + getEjbName() + "\n" );
        buf.append( "EJB name: " + getBeanId() + "\n" );
        buf.append( "Home Interface: " + getHomeInterfaceName()  + "\n" );
        buf.append( "Component Interface: " + getCompInterfaceName() + "\n" );
        buf.append( "Web EJB Ref: " + getWebEjbRef() + "\n" );
        buf.append( "Wrapper Bean Name: " + getBeanWrapperName() +"\n" );
        buf.append( "Wrapper Bean Info Name: " + getBeanInfoWrapperName() + "\n" );
        if( getMethods() != null ) 
        {
            buf.append( "Num of methods: " + getMethods().size() + "\n" );
            buf.append( getMethods().toString() );
        }
        
        return buf.toString();
    }
    
    public Collection getMethodNames()
    {
        ArrayList mNames = new ArrayList();
        
        for( Iterator iter = methodInfos.iterator(); iter.hasNext(); )
        {
            MethodInfo mInfo = (MethodInfo)iter.next();
            mNames.add( mInfo.getName() );
        }
        
        return mNames;
    }
    
    public MethodInfo getMethod( String name )
    {
        for( Iterator iter = methodInfos.iterator(); iter.hasNext(); )
        {
            MethodInfo mInfo = (MethodInfo)iter.next();
            if( mInfo.getName().equals( name ) )
                return mInfo;
        }
        
        return null;
    }
    
    public Object clone()
    {
        try
        {
            EjbInfo ejbCopy = (EjbInfo)super.clone();
            
            // Methods
            if( this.methodInfos != null )
            {
                ArrayList mdCopy = new ArrayList();
                
                for( Iterator iter = this.methodInfos.iterator(); iter.hasNext(); )
                {
                   mdCopy.add( ((MethodInfo)iter.next()).clone() );
                }
                
                ejbCopy.setMethods( mdCopy );
            }
            
            return ejbCopy;
        }
        catch( java.lang.CloneNotSupportedException e )
        {
            return null;
        }
    }
    
    // Implementing Comparable
   public int compareTo(Object o) {
       
        if( o == null || !(o instanceof EjbInfo) )
            return 0;
        
        String theOtherName = ((EjbInfo)o).getJNDIName();
        
        if( this.getJNDIName() == null || theOtherName == null )
            return 0;
        
        return this.getJNDIName().compareTo( theOtherName );
    }
    
}
