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
/*
 * ClientBeanGenerator.java
 *
 * Created on May 12, 2004, 8:36 PM
 */

package org.netbeans.modules.visualweb.ejb.load;

import org.netbeans.modules.visualweb.ejb.datamodel.EjbInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodInfo;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodParam;
import org.netbeans.modules.visualweb.ejb.datamodel.MethodReturn;
import org.netbeans.modules.visualweb.ejb.util.Util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.*;
import org.openide.ErrorManager;

/**
 * Generates a wrapper bean for the given EJB session bean
 * @author  cao
 */
public class ClientBeanGenerator {
    /**
     * The EJB which this class is going to create wrapper class for
     */
    private EjbInfo ejbInfo;
    
    /**
     * Whether create() can be automatically called in the business wrapper method
     */
    private boolean autoInit = true;
    
    /**
     * To load the classes in the client jars
     */
    private URLClassLoader classLoader;
    
    /**
     * These are not real properties. The reason we want them
     * to look like JavaBean properties is because we can only
     * bind properties to JSF components currently. They should
     * be removed once we can support method binding
     */
    private ArrayList virtualProperties;
    
    /**
     * Creates a new instance of ClientBeanGenerator
     *
     * @param ejbInfo The EJB which the bean and BeanInfo wrapper class are for
     * @param port Where the Ejb can be looked up
     */
    public ClientBeanGenerator( EjbInfo ejbInfo, URLClassLoader classloader ) {
        this.ejbInfo = ejbInfo;
        this.classLoader = classloader;
        this.virtualProperties = new ArrayList();
    }
    
    /*
     * Generates the bean and BeanInfo class for the EJB and also compile the class
     *
     * @param srcDir Where the generated .java and .class will be saved
     * @return a Collection of ClassDescriptors describing the classes (.java and .class)
     *         generated by this method
     */
    public Collection generateClasses( String srcDir ) throws EjbLoadException {
        // First, generate the bean class source code
        ClassDescriptor beanClassDescriptor = generateBeanClasse( srcDir );
        
        // Take care the case where there is no package
        String beanClassName = beanClassDescriptor.getPackageName() + "." + beanClassDescriptor.getClassName();
        if( beanClassDescriptor.getPackageName() == null || beanClassDescriptor.getPackageName().length() == 0 )
            beanClassName = beanClassDescriptor.getClassName();
        
        ejbInfo.setBeanWrapperName( beanClassName );
        
        // Now, Generate the BeanInfo class source code
        ClientBeanInfoGenerator infoGenerator = new ClientBeanInfoGenerator( ejbInfo.getJNDIName(), beanClassName, virtualProperties );
        ClassDescriptor beanInfoClassDescriptor = infoGenerator.generateClass( srcDir );
        
        // Take care the case where there is no package
        String beanInfoClassName = beanInfoClassDescriptor.getPackageName() + "." + beanInfoClassDescriptor.getClassName();
        if( beanInfoClassDescriptor.getPackageName() == null || beanInfoClassDescriptor.getPackageName().length() == 0 )
            beanInfoClassName = beanInfoClassDescriptor.getClassName();
        
        ejbInfo.setBeanInfoWrapperName( beanInfoClassName );
        
        // Done! Return all the class descriptors
        
        ArrayList classDescriptors = new ArrayList();
        classDescriptors.add( beanClassDescriptor );
        classDescriptors.add( beanInfoClassDescriptor );
        
        return classDescriptors;
    }
    
    /**
     * Generates the Bean class for the EJB.
     *
     * @param srcDir where the java source code will be saved
     * @return The ClassDescriptor of the bean class just generated
     */
    private ClassDescriptor generateBeanClasse( String srcDir ) throws EjbLoadException {
        // Declare it outside the try-catch so that the file name can be logged in case of exception
        File javaFile = null;
        
        try {
            // Figure out the package name, class names, and directory, file name
            
            int i = ejbInfo.getCompInterfaceName().lastIndexOf( '.' );
            String className = ejbInfo.getCompInterfaceName().substring( i + 1 );
            
            // Get the package name. The wrapper bean will be in the same package
            //String packageName = EjbLoader.CLIENT_WRAPPER_PACKAGE_NAME + "." + className.toLowerCase();
            
            // The package for all the generated class for this ejb is <packageForThisEjb>.<ejbremoteinterfacename>
            if( i == -1 ) // no package name
                i = 0;
            String packageName = ejbInfo.getCompInterfaceName().substring( 0, i ) + "." + className.toLowerCase();
            
            // Home interface name
            String homeName = ejbInfo.getHomeInterfaceName().substring( ejbInfo.getHomeInterfaceName().lastIndexOf('.')+1 );
            
            // The Wrapper bean class name - <remote>Cleint
            String clientBeanClassName = className  + "Client"; // NOI18N
            
            String classDir = packageName.replace( '.', File.separatorChar );
            File dirF = new File( srcDir + File.separator + classDir );
            if( !dirF.exists() ) {
                if( !dirF.mkdirs() )
                    System.out.println( ".....failed to make dir" + srcDir + File.separator + classDir );
            }
            
            String clientBeanClassFile =  clientBeanClassName + ".java";  // NOI18N
            javaFile = new File( dirF, clientBeanClassFile );
            javaFile.createNewFile();
            
            ClassDescriptor beanClassDescriptor = new ClassDescriptor(
                    clientBeanClassName,  // class name
                    packageName,  // package name
                    javaFile.getAbsolutePath(),  // full path java file name
                    classDir + File.separator + clientBeanClassFile ); // file name with package in path
            
            // Generate java code
            
            PrintWriter out = new PrintWriter( new FileOutputStream(javaFile) );
            
            // package
            if( packageName != null && packageName.length() != 0 ) {
                out.println( "package " + packageName + ";" );
                out.println();
            }
            
            // comments
            out.println( "/**" );
            out.println( " * Source code created on " + new Date() );
            out.println( " */" );
            out.println();
            
            // Import
            out.println( "import javax.rmi.PortableRemoteObject;" );
            out.println( "import javax.naming.*;" );
            out.println( "import java.util.*;" );
            out.println( "import java.beans.Beans;" );
            out.println( "import java.lang.reflect.Method;" );
            
            // Do not import if the remote interface has no package
            if( ejbInfo.getCompInterfaceName().indexOf( '.' ) != -1 )
                out.println( "import " + ejbInfo.getCompInterfaceName() + ";" );
            
            // Do not import if the home interface has no package
            if( ejbInfo.getHomeInterfaceName().indexOf( '.' ) != -1 )
                out.println( "import " + ejbInfo.getHomeInterfaceName() + ";" );
            
            out.println();
            
            // start of class
            out.println( "public class " + clientBeanClassName + " implements java.io.Serializable {" );
            out.println();
            
            // private memeber variables
            String classVariable = className.toLowerCase();
            String homeVariable =  homeName.toLowerCase();
            out.println( "    private " + className + " " + classVariable + ";" );
            out.println( "    private " + homeName + " " + homeVariable + ";" );
            out.println( "    private boolean initialized = false;" );
            out.println();
            
            // Constructor
            out.println( "    public " + clientBeanClassName + "() {" );
            out.println( "    }" );
            out.println();
            
            // create() methods
            // For stateless session ejbs, there is always one create() without argument.
            // But it is not true for stateful session ejbs, which can have many create() methods with
            // all kinds of argumentst
            createInitMethod( out, homeVariable, homeName, classVariable );
            
            // Business methods
            for( Iterator iter = ejbInfo.getMethods().iterator(); iter.hasNext(); ) {
                MethodInfo methodInfo = (MethodInfo)iter.next();
                
                if( !methodInfo.isBusinessMethod() )
                    continue;
                
                // Check whether the method fits into the JavaBean pattern
                // If yes, we'll make a virtual property out of it so that
                // it can be property-binding to a JSF component
                virtualPropertyOrNot( methodInfo );
                
                out.println( "    /**" );
                out.println( "     * @see " + javaDocMethod( ejbInfo.getCompInterfaceName(), methodInfo ) );
                out.println( "     */");
                out.println( "    " + methodSignature( methodInfo ) + " {" );
                
                // If design time, return fake data if the method reutrn something (!void)
                out.println( "        if( Beans.isDesignTime() ) { " );
                if( methodInfo.getReturnType().getClassName().equals( "void" ) ) // NOI18N
                    out.println( "            return;" );
                else
                    out.println( "            return " + fakeReturnValue( methodInfo ) + ";" );
                out.println( "        }" );
                out.println();
                
                
                // create() will be automatically called if the autoInit is true.
                // Otherwise, check whether the bean is initialized or not
                if( autoInit )
                    out.println( "        create();" );
                else {
                    out.println( "        if( initialized == false ) { " );
                    out.println( "            throw new java.lang.RuntimeException( \"Bean not initialized\" );" );
                    out.println( "        }" );
                }
                
                out.println( "        " + invokeMethodStatement( classVariable, methodInfo ) + ";" );
                out.println( "    }" );
                out.println();
            }
            
            // End of client bean clas
            out.println( "}" );
            
            out.flush();
            out.close();
            
            return beanClassDescriptor;
        } catch( java.io.FileNotFoundException ex ) {
            // Log error
            String errMsg = "Error occurred when trying to generate the wrapper bean class for EJB " + ejbInfo.getJNDIName()
            + ". Could not find file " + javaFile.getAbsolutePath();
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.ClientBeanGenerator" ).log( errMsg );
            ex.printStackTrace();
            
            // Throw up as a SYSTEM_ERROR
            throw new EjbLoadException( ex.getMessage() );
        } catch( java.io.IOException ex ) {
            // Log error
            String errMsg = "Error occurred when trying to generate the wrapper bean class for EJB " + ejbInfo.getJNDIName()
            + ". Could not create file " + javaFile.getAbsolutePath();
            ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.ClientBeanGenerator" ).log( errMsg );
            ex.printStackTrace();
            
            // Throw up as a SYSTEM_ERROR
            throw new EjbLoadException( ex.getMessage() );
        }
    }
    
    private void virtualPropertyOrNot( MethodInfo method ) {
        String methodName = method.getName();
        
        if( methodName.startsWith( "get" ) &&
                !method.getReturnType().getClassName().equals( "void" ) &&
                method.getParameters().size() == 0 ) {
            virtualProperties.add( methodName.substring( 3 ) );
        }
    }
    
    private void createInitMethod( PrintWriter out, String homeVariable,  String homeName, String classVariable ) {
        
        for( Iterator iter = ejbInfo.getMethods().iterator(); iter.hasNext(); ) {
            MethodInfo methodInfo = (MethodInfo)iter.next();
            
            // One per create() method, not business method
            if( methodInfo.isBusinessMethod() )
                continue;
            
            ArrayList parameters = methodInfo.getParameters();
            
            // Auto create() can not be called if there is one or more create()
            // method takes arguments (stateful session ejb)
            if( parameters.size() != 0 )
                autoInit = false;
            
            StringBuffer paramStr = new StringBuffer();
            for( int i = 0; i < parameters.size(); i ++ ) {
                MethodParam p = (MethodParam)parameters.get(i);
                
                if( i != 0 )
                    paramStr.append( ", " );
                
                paramStr.append( p.getType() );
                paramStr.append( " " );
                paramStr.append(  p.getName() );
            }
            
            out.println( "    /**" );
            out.println( "     * @see " + javaDocMethod( ejbInfo.getHomeInterfaceName(), methodInfo ) );
            out.println( "     */");
            out.println( "    public void create(" + paramStr + ") throws javax.naming.NamingException, java.rmi.RemoteException, javax.ejb.CreateException {" );
            out.println( "        if( !Beans.isDesignTime() && !initialized ) { " );
            out.println( "            InitialContext ctx = new InitialContext();" );
            out.println( "            Object objRef = ctx.lookup( \"" + "java:comp/env/" + ejbInfo.getWebEjbRef() + "\" );" );
            out.println( "            " + homeVariable + " = (" + homeName + ")PortableRemoteObject.narrow( objRef, " + homeName + ".class );" );
            out.println( "            " + classVariable + " = " + invokeMethod( homeVariable, methodInfo ) + ";" );
            out.println( "            initialized = true;" );
            out.println( "        }" );
            out.println( "    }" );
            out.println();
        }
    }
    
    private String fakeReturnValue( MethodInfo method ) throws EjbLoadException {
        String fakeReturn = "null";
        
        String returnType = method.getReturnType().getClassName();
        
        // Can be one of the following:
        // int, long, double, float, short, byte
        // char
        // boolean
        // void
        if( returnType.equals( "int" ) ||
                returnType.equals( "long" ) ||
                returnType.equals( "double" ) ||
                returnType.equals( "float" ) ||
                returnType.equals( "short" ) ||
                returnType.equals( "byte" ) ) {
            return "0";
        } else if( returnType.equals( "boolean" ) ) {
            return "false";
        } else if( returnType.equals( "char" ) ) {
            return "\'A\'";
        } else if( returnType.indexOf( "[]" ) != -1 ) {
            // Return type is Array
            return null;
        } else {
            try {
                Class returnClass = Class.forName( returnType, true, classLoader );
                
                if( returnClass == String.class )
                    return "\"ABC\"";
                else if( returnClass == Collection.class )
                    return "new java.util.ArrayList()";
                else if( returnClass == Set.class )
                    return "new java.util.HashSet()";
                else // Must be a single object return type (including Array)
                    return "null";
            } catch( java.lang.ClassNotFoundException e ) {
                // Should never happen
                // Log error no matter what
                String errMsg = "Error occurred when trying to generate wrapper bean class for EJB " + ejbInfo.getJNDIName()
                + ". Could not find class " + method.getReturnType().getClassName();
                ErrorManager.getDefault().getInstance( "org.netbeans.modules.visualweb.ejb.load.ClientBeanGenerator").log( ErrorManager.ERROR, errMsg );
                e.printStackTrace();
                
                // Throw up as SYSTEM_ERROR
                throw new EjbLoadException( e.getMessage() );
            }
        }
    }
    
    private String javaDocMethod( String className, MethodInfo methodInfo ) {
        // Link to the EJB method - package.class#method(type, type, ...)
        StringBuffer linkMethod = new StringBuffer();
        linkMethod.append( className + "#" );
        linkMethod.append( methodInfo.getName() );
        linkMethod.append( "(" );
        ArrayList parameters = methodInfo.getParameters();
        for( int i = 0; i < parameters.size(); i ++ ) {
            MethodParam p = (MethodParam)parameters.get(i);
            
            if( i != 0 )
                linkMethod.append( ", " );
            
            linkMethod.append( p.getType() );
        }
        
        linkMethod.append( ")" );
        
        return linkMethod.toString();
    }
    
    /**
     * Forms the method signature for the given method information
     */
    private String methodSignature( MethodInfo methodInfo ) {
        StringBuffer buf = new StringBuffer();
        
        buf.append( "public " );
        buf.append( methodInfo.getReturnType().getClassName() );
        buf.append( " " );
        buf.append( methodInfo.getName() );
        buf.append( "(" );
        
        ArrayList parameters = methodInfo.getParameters();
        for( int i = 0; i < parameters.size(); i ++ ) {
            MethodParam p = (MethodParam)parameters.get(i);
            
            if( i != 0 )
                buf.append( ", " );
            
            buf.append( p.getType() );
            buf.append( " " );
            buf.append( p.getName() );
        }
        
        buf.append( ")" );
        
        ArrayList exceptions = methodInfo.getExceptions();
        if( exceptions != null && !exceptions.isEmpty() ) {
            buf.append( " throws " );
            
            for( int i = 0; i < exceptions.size(); i ++ ) {
                if( i != 0 )
                    buf.append( ", " );
                
                buf.append( (String)exceptions.get( i ) );
            }
        }
        
        if( autoInit ) {
            // Since the create() is called before calling the business method,
            // need to throw two more exceptions from the create():
            // javax.naming.NamingException, javax.ejb.CreateException
            buf.append( "," );
            buf.append( "javax.naming.NamingException, javax.ejb.CreateException" );
        }
        
        return buf.toString();
    }
    
    /*
     * Forms the method call string
     */
    private String invokeMethodStatement( String variableName, MethodInfo methodInfo ) {
        StringBuffer buf = new StringBuffer();
        
        if( !methodInfo.getReturnType().getClassName().equals( "void" ) )
            buf.append( "return " );
        
        buf.append( variableName );
        buf.append( "." );
        buf.append( methodInfo.getName() );
        buf.append( "(" );
        
        ArrayList parameters = methodInfo.getParameters();
        for( int i = 0; i < parameters.size(); i ++ ) {
            MethodParam p = (MethodParam)parameters.get(i);
            
            if( i != 0 )
                buf.append( ", " );
            
            buf.append( p.getName() );
        }
        
        buf.append( ")" );
        return buf.toString();
    }
    
    private String invokeMethod( String variableName, MethodInfo methodInfo ) {
        StringBuffer buf = new StringBuffer();
        
        buf.append( variableName );
        buf.append( "." );
        buf.append( methodInfo.getName() );
        buf.append( "(" );
        
        ArrayList parameters = methodInfo.getParameters();
        for( int i = 0; i < parameters.size(); i ++ ) {
            
            MethodParam p = (MethodParam)parameters.get(i);
            
            if( i != 0 )
                buf.append( ", " );
            
            buf.append( p.getName() );
        }
        
        buf.append( ")" );
        return buf.toString();
    }
    
    public static void main( String[] args ) {
        int i = 0;
        
        Integer ii = new Integer(0);
        
        Class returnClass = ii.getClass();
        
    }
}
