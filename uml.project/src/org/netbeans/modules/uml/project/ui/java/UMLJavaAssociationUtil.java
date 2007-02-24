/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
 * UMLJavaAssociationUtil.java
 *
 * Created on March 25, 2005, 11:16 AM
 */

package org.netbeans.modules.uml.project.ui.java;

import org.netbeans.modules.uml.project.AssociatedSourceProvider;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.jmi.javamodel.JavaClass;
import org.openide.loaders.DataObject;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import org.netbeans.jmi.javamodel.JavaEnum;
//import  org.netbeans.modules.uml.project.*;
//import org.netbeans.jmi.javamodel.*;
import org.netbeans.modules.javacore.api.JavaModel;
import org.openide.ErrorManager;
import org.openide.loaders.DataObjectNotFoundException;


/**
 *
 * @author Mike
 */
public class UMLJavaAssociationUtil {
	
	/** Creates a new instance of UMLJavaAssociationUtil */
	public UMLJavaAssociationUtil() {
	}
	
	
	// TODO - do we have to worry about or flag the edge case where a single
	// java project might be intentionally or unintentionally associated with
	// > 1 uml project? Perhaps we should detect that and throw an invalid state
	// exception?
	public static Project getAssociatedUMLProject(JavaClass c) {
//		DataObject sourceDO = JavaMetamodel.getManager().getDataObject(c.getResource());
            DataObject sourceDO = null;
            try {
                sourceDO = DataObject.find(JavaModel.getFileObject(c.getResource()));
            }catch (DataObjectNotFoundException e)
            {
                ErrorManager.getDefault().notify(e);
            }
      return getAssociatedUMLProject(sourceDO);
	}
   
   /**
    * Retrieve a UML project that is associatied with the given data object.
    */
   public static Project getAssociatedUMLProject(DataObject dObj)
   {  
      Project currentJavaProj =
               FileOwnerQuery.getOwner(dObj.getPrimaryFile());
      return getAssociatedUMLProject(currentJavaProj);   
   }
	
   /**
    * Retrieve a UML project that is associated with a specified 
    * project.
    */
   public static Project getAssociatedUMLProject(Project project)
   {
      // Filter out the inappropriate projects
      
      // TODO - complete the "association detecthion" It should probably
      // be even more fine tuned to check that the UMLProject is ALSO listing
      // the Java classes SourceGroup. There is possibility that > 1 UMLProject
      // might target same java project but different source groups. That would
      // be ok, but we would need to factor that into the check below.
      Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
      for (int i = 0; i < allProjects.length; i++)
      {
         AssociatedSourceProvider asp = (AssociatedSourceProvider)
         allProjects[i].getLookup().lookup(AssociatedSourceProvider.class);
         
         if ( asp != null )
         {
            Project umlJavaProj = asp.getAssociatedSourceProject();
            
            if(project == umlJavaProj)
            {
               return allProjects[i];
            }
            
         }
      }
      
      return null;
   }
	
 
	
	// For a given jmi.javamodel.JavaClass this will determine which 
	// embarcadero i type it would be associated with
	 public static Class getIType(JavaClass c) {
		 
		 // hmm, is this a instanceof or isAssignable from situation?
		 // TODO review for completeness
		 
		 // Generics ...not sure org.netbeans.jmi.javamodel.GenericElement
		 // is actually a super interface of JavaClass in jmi
		 
		 // work from most specific to most generic
		 if(c instanceof JavaEnum ) {
			 return IEnumeration.class;
		 }
		 else if(c.isInterface() ) {
			 return IInterface.class;
		 }
		 else {
			 // this will cover both simple class
			 // and Annotation
			 // and Generic (i think)
			 return IClass.class;
		 }
		 
	 }

//proj=The UML project to search
//qualifiedName=The fully qualified name of the class.
//type=The type of model element that you want to be returned IClass.class, IEnumeration.class, etc
public static INamedElement findElement(Project umlProject,
                                       String qualifiedName,
                                       Class type) {
	
	UMLProjectHelper mHelper = null;
	mHelper = (UMLProjectHelper) umlProject.getLookup().lookup(UMLProjectHelper.class);
	
	if(mHelper == null)
		return null; // should not happen
	
	
	IProject iProj = mHelper.getProject();
		

       if (iProj == null) {
           return null;
       }


       ITypeManager typeManager = iProj.getTypeManager();
       if (typeManager == null) {
           return null;
       }

       // Converts a Java qualified name to a UML qualified name
       // Example: java.swing.JFrame -> java::swing::JFrame
       //          java.swing.JFrame$InnerClass -> java::swing::JFrame::InnerClass
       String umlName = convertJavaToUML(qualifiedName);
	   	   
       ETList<INamedElement> elems = typeManager.getLocalCachedTypesByName(umlName);

       int count = elems != null? elems.getCount() : 0;
       for(int i = 0; i < count; i++) {
           INamedElement elem = elems.item(i);
           if (elem == null) continue;
           if (type != null && !type.isAssignableFrom(elem.getClass())) continue;
           String nameToCompare = getFullyQualifiedName(elem);
           if(nameToCompare.equals(qualifiedName)
                  || nameToCompare.replace('$', '.').equals(qualifiedName))
               return elem;
       }
       return null;
   }

    /**
     * Converts a fully qualified Java classname to Describe's internal
     * naming format. Note that inner class names should be delimited by '$'
     * instead of '.' for this to work correctly.
     *
     * Do *not* call this method for primitives!
     *
     * @param javaName A fully qualifid java class name.
     */
    public static String convertJavaToUML( String javaName ) {
        String retVal = javaName.replace('$', '.');

        // replace "." with "::"
        int pos = retVal.indexOf(".");
        while(pos > 0) {
            StringBuffer buf = new StringBuffer(retVal);
            retVal = buf.replace(pos, pos + 1, "::").toString();
            pos = retVal.indexOf(".");
        }

        return retVal;
    }

    /**
     * Builds a UML notated name from a Java package and class name. Note that
     * inner class names should be delimited by '$' instead of '.' for this
     * to work correctly.
     * @param pName The fully quallified package of the class.
     * @param name The name of the class.
     */
    public static String convertJavaToUML(String pName, String name) {
        String curPName = (pName.length() > 0 ? pName + "." : "");
        String fullScopeName  = curPName + name;
        return convertJavaToUML(fullScopeName);
    }

    /**
     * Converts a UML fully qualified name into a Java fully qualified name.
     * @param umlNam A UML formated string.
     */
    public static String convertUMLtoJava( String umlName ) {
        String retVal = umlName;

        // replace "::" with "."
        int pos = retVal.indexOf("::");
        while(pos > 0) {
            StringBuffer buf = new StringBuffer(retVal);
            retVal = buf.replace(pos, pos + 2, ".").toString();
            pos = retVal.indexOf("::");
        }

        return retVal;
    }

   /**
     *  Returns the fully-qualified name for the given named element.
     * @param element The element for which the qualified name needs to be
     *                determined.
     * @return Fully qualified name, with packages separated by periods and
     *         inner classes separated by '$'.
     */
    public static String getFullyQualifiedName(INamedElement element) {
        StringBuffer name = new StringBuffer(element.getName());

        for (IElement parent = element.getOwner(); parent != null;
                        parent = parent.getOwner()) {
            if (parent instanceof IClass) {
                IClass p = (IClass)  parent;
                name.insert(0, '$');
                name.insert(0, p.getName());
            } else if (parent instanceof IPackage
                            && !(parent instanceof IProject)) {
                IPackage p = (IPackage)  parent;
                name.insert(0, '.');
                name.insert(0, p.getName());
            } else
                break;
        }
        return name.toString();
    }
	
}
