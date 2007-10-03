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

// GENERATED CODE FOR Sun ONE Studio class.
//
package org.netbeans.modules.dbschema.migration.archiver.deserializer;

import java.lang.*;
import java.lang.reflect.*;

import org.xml.sax.*;

public  class BaseSpecificXMLDeserializer extends BaseXMLDeserializer
    implements SpecificXMLDeserializer
{
    // Fields

    public static final String WRONG_TAG = "Saw tag {1} when {2} was expected.";
    protected   java.lang.Integer State;
    protected   java.lang.Class ParameterClass;
    protected   java.lang.Class ParameterSetMethod;

//@olsen+MBO: used unsynchronized HashMap and ArrayListStack
    protected   ArrayListStack StateStack;
    protected   ArrayListStack ObjectStack;
    protected   java.util.HashMap ActiveAliasHash;
/*
    protected   java.util.Stack StateStack;
    protected   java.util.Stack ObjectStack;
    protected   java.util.Hashtable ActiveAliasHash;
    protected   java.util.Hashtable ClassHash; // @olsen+MBO: NOT USED!
*/

    protected   java.util.Vector ParameterArray;
    public      java.util.Vector ParameterTypeArray;
    protected   XMLDeserializer MasterDeserializer;
    private     ClassLoader classLoader;  //@lars


    // Constructors

    //@lars: added classloader-constructor
    public BaseSpecificXMLDeserializer (ClassLoader cl)
    {
        super();
//@olsen+MBO: used unsynchronized HashMap and ArrayListStack
        this.ObjectStack    = new ArrayListStack();
        this.StateStack     = new ArrayListStack();
        this.classLoader    = (cl != null  ?  cl  :  getClass ().getClassLoader ());
/*
        this.ObjectStack    = new java.util.Stack();
        this.StateStack     = new java.util.Stack();
*/
        this.setMasterDeserializer(this);
    } /*Constructor-End*/

    public  BaseSpecificXMLDeserializer()
    {
        this (null);
    } /*Constructor-End*/

    // Methods

    public   void setMasterDeserializer(XMLDeserializer master)
    {
        this.MasterDeserializer = master;
    } /*Method-End*/

    public   void unexpectedTag(String actual,String expected, boolean endTagExpected) throws org.xml.sax.SAXException
    {

        if (endTagExpected)
        {
            //String endTag = new String("/" + expected);
            //expected = endTag;
            expected = "/" + expected;

        }

        String message = new String("Saw tag " + actual +  " when " + expected + " was expected.");

        SAXException tagError = new SAXException(message);

        throw tagError;
    } /*Method-End*/

    public   void validateTag(String actual, String expected, boolean endTagExpected) throws org.xml.sax.SAXException
    {
        if ( !actual.equals(expected) )
        {
            this.unexpectedTag(actual, expected, endTagExpected);
        }
    } /*Method-End*/

    public   void popState()
    {
        this.State = (Integer)(this.StateStack.pop());
    } /*Method-End*/

    public   void pushState(int newState)
    {
        // put the old state on the top of the stack

        this.StateStack.push( this.State );

        // and now set the state to the new state
        this.State = new Integer(newState);
    } /*Method-End*/

    public   void addActiveAlias(String name, String alias)
    {
        if (this.ActiveAliasHash == null)
        {
            //this.ActiveAliasHash = new java.util.Hashtable(20, 0.75F);
            this.ActiveAliasHash = new java.util.HashMap(20);
        }

//@olsen+MBO: removed redundant code
/*
        if (this.ActiveAliasHash.containsKey(alias))
        {
            this.ActiveAliasHash.remove(alias);
        }
*/
        this.ActiveAliasHash.put(alias, name);
    } /*Method-End*/

    public  String  lookupAlias(String name)
    {
        //
        //   this method searches the alias hashtable
        //   if it exists and returns the name of the alias
        //   otherwise it simply returns the name that was
        //   passed in
        //
        String retName = null;

        if (this.ActiveAliasHash != null)
        {
            retName = (String)(this.ActiveAliasHash.get(name));

        }
        if (retName == null)
        {
            retName = name;
        }

        return retName;
    } /*Method-End*/

    public  Class  findClass(String name) throws java.lang.ClassNotFoundException
    {
        Class lReturnClass;

//@lars: added classloader
//        lReturnClass = Class.forName (name);
        
        
        name=  org.netbeans.modules.dbschema.migration.archiver.MapClassName.getRealClassName(name);
        lReturnClass = java.lang.Class.forName(name, true /*initialize the class*/, this.classLoader);

        return lReturnClass;
    } /*Method-End*/

    public  Object  popObject()
    {
        return this.ObjectStack.pop();
    } /*Method-End*/

    public   void pushObject(Object obj)
    {
        this.ObjectStack.push(obj);
    } /*Method-End*/

    public  String  unescapeName(String name)
    {
        // this method is going to strip the _ and - from
        // the beginning of the name

//@olsen+MBO: minimized number of objects and operations
        if (name.startsWith("_-")) {
            return name.substring(2);
        }

        int idx = name.indexOf('-');
        if (idx >= 0) {
            StringBuffer buf = new StringBuffer(name);
            buf.setCharAt(idx, '_');
            return buf.toString();
        }

        return name;
/*
        StringBuffer lStr = new StringBuffer(name);

        if ( (lStr.charAt(0) == '_') &&
             (lStr.charAt(1) == '-') )
        {
            lStr.delete(0,2);
        }
        else
        {
            boolean lFound = false;
            int lLocation;
            // search for dash
            loop:
            for (lLocation = 0; lLocation < lStr.length(); lLocation++)
            {
                if (lStr.charAt(lLocation) == '-')
                {
                    lFound = true;
                     break loop;
                }// end if
            }// end for

            // if we find an dash replace it with a underscore
            if (lFound)
            {
                lStr.replace(lLocation, lLocation + 1, "_");

            }// end if


        }// end if
        return lStr.toString();
*/
    } /*Method-End*/

    public  boolean  useExistingAttribute(org.xml.sax.AttributeList atts, String attrname, Object existing) throws org.xml.sax.SAXException
    {
        boolean retBool = false;

        String useDirective = atts.getValue("USE");

        if (useDirective != null &&
            useDirective.equals("EXISTING"))
        {
//@olsen+MBO: ever stepped in?

            java.lang.Object lCurrentObj = this.topObject();
            Field lField = null;
            try
            {
                lField = lCurrentObj.getClass().getDeclaredField(attrname);
                existing = lField.get(lCurrentObj);
            }
            catch (IllegalArgumentException e1)
            {
                // add the illegal arg exception to the exception stack
                // and then mask it under a SAXexception and raise the
                // SAXException

                //String message = new String("Illegal Argument used " + lCurrentObj.getClass().getName());
                String message = ("Illegal Argument used " + lCurrentObj.getClass().getName());
                SAXException useError = new SAXException(message);
                throw useError;
            }
            catch (IllegalAccessException e2)
             {
                 // add the illegal access exception to the exception stack
                 // and then mask it under a SAXexception and raise the
                 // SAXException
                 //String message = new String("Illegal Access of field " + lField);
                 String message = ("Illegal Access of field " + lField);
                 SAXException useError = new SAXException(message);
                 throw useError;
             }
            catch (NoSuchFieldException e3)
             {
                 // add the no such field exception to the exception stack
                 // and then mask it under a SAXexception and raise the
                 // SAXException

                 //String message = new String("No such field " + attrname);
                 String message = ("No such field " + attrname);
                 SAXException useError = new SAXException(message);
                 throw useError;
             }

            retBool = true;
        }
        else if (useDirective != null)
        {
            //String message = new String("Invalid value USE for attribute " + useDirective);
            String message = ("Invalid value USE for attribute " + useDirective);

            SAXException useError = new SAXException(message);
            throw useError;

        }// end if

        return retBool;
    } /*Method-End*/

    public  java.lang.Object  topObject() throws org.xml.sax.SAXException
    {
        if (this.ObjectStack.size() == 0)
        {
            //String message = new String("Object Stack Empty");
            String message = ("Object Stack Empty");

            SAXException stackError = new SAXException(message);

            throw stackError;

        }

        return this.ObjectStack.peek();
    } /*Method-End*/

    public   void freeResources()
    {
        super.freeResources();
        this.ObjectStack.clear();
        //ParameterArray.clear();
        //ParameterTypeArray.clear();
        StateStack.clear();
        if (ActiveAliasHash != null)
            ActiveAliasHash.clear();
    } /*Method-End*/

    public void DumpStatus()
    {
        // This method is a debug method to dump status information about this object
        super.DumpStatus();

        System.out.println("Dump Status from class BaseSpecificXMLSerializer");
        System.out.println("Current state " + this.State);
        System.out.println("State stack " + this.StateStack);
        System.out.println("Object Stack " + this.ObjectStack);
        System.out.println("Dump Status from class BasespecificXMLSerializer - END");

    }

}  // end of class
