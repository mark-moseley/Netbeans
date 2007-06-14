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


package org.netbeans.modules.uml.project.ui.nodes;

import javax.swing.Action;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeItemImpl;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.project.ui.nodes.actions.NewDiagramType;
import org.netbeans.modules.uml.project.ui.nodes.actions.NewPackageType;
import org.netbeans.modules.uml.project.ui.nodes.actions.NewElementType;
import org.netbeans.modules.uml.project.ui.nodes.actions.NewAttributeType;
import org.netbeans.modules.uml.project.ui.nodes.actions.NewOperationType;
import org.openide.actions.NewAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;

/**
 * 
 * @author Trey Spiva
 */
public class UMLFolderNode extends UMLElementNode implements ITreeFolder
{

   private String m_GetMethodName  = "";
   private String m_Name           = "";
   private String m_ID             = "";

   public final static String METHOD_NAME_GET_ATTRIBUTES = "getAttributes";
   public final static String METHOD_NAME_GET_OPERATIONS = "getOperations";
   
   
   public UMLFolderNode()
   {
      this(new ProjectTreeItemImpl());
   }
   
   public UMLFolderNode(Lookup lookup)
   {
      this(lookup, new ProjectTreeItemImpl());
   }
   
   public UMLFolderNode(Children ch, Lookup lookup)
   {
	  this(ch, lookup, new ProjectTreeItemImpl());
   }
   
   /**
    * @param item
    */
   public UMLFolderNode(IProjectTreeItem item)
   {
      super(item);
   }
   
   /**
    * @param item
    */
   public UMLFolderNode(Lookup lookup, IProjectTreeItem item)
   {
      super(lookup, item);
   }

   /**
	* @param item
	*/
   public UMLFolderNode(Children ch, Lookup lookup, IProjectTreeItem item)
   {
	  super(ch, lookup, item);
   }
   
   /* (non-Javadoc)
	* @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem#addChild(org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem)
	*/
	public void addChild(ITreeItem item)
	{
//        String yourID = item.getData().getModelElementXMIID();
//        String myID = getData().getModelElementXMIID();
//        if(yourID.equals(myID) == false)
//        {
            super.addChild(item);
//        }
	}
    
   public void setID(String id)
   {
      m_ID = id;
   }
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder#getID()
    */
   public String getID()
   {
      return m_ID;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder#setGetMethod(java.lang.String)
    */
   public void setGetMethod(String name)
   {
      m_GetMethodName = name;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder#getGetMethod()
    */
   public String getGetMethod()
   {
      return m_GetMethodName;
   }

   /**
    * Sets the element that this tree folder represents.
    *
    * @param newVal The element that this folder represents
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder#setElement(com.embarcadero.describe.foundation.IElement)
    */
   public void setElement(IElement newVal)
   {
      IProjectTreeItem data = getData();
      if(data != null)
      {
         data.setModelElement(newVal);
      }
   }

   
	/**
	 * Get the new types that can be created in this node. For example, a node
	 * representing a class will permit attributes, operations, classes,
	 * interfaces, and enumerations to be added.
	 *
	 * @return An array of new type operations that are allowed.
	 */
	public NewType[] getNewTypes()
	{
		String elType = getElementType();
		
		NewType[] retVal = null;
		
		if (getModelElement() instanceof INamespace)
		{
			// Class types: Java Class
			if (elType.equals(ELEMENT_TYPE_CLASS))
			{
				if (getGetMethod().equals(METHOD_NAME_GET_OPERATIONS))
				{
					return new NewType[]
					{
						new NewOperationType(this)
					};
				}
				
				else if(getGetMethod().equals(METHOD_NAME_GET_ATTRIBUTES))
				{
					return new NewType[]
					{
						new NewAttributeType(this)
					};
				}
			}
			
			else if (elType.equals(ELEMENT_TYPE_OPERATION))
                        {
                           return new NewType[]
                           {
                              new NewDiagramType(this),
                              new NewPackageType(this),
                              new NewElementType(this)
                           };
                        }
		} // if getModelElement() instanceof INamespace

		if (retVal == null)
		{
			retVal = new NewType[0];
		}
		
		return retVal;
	}
   
   
	public Action[] getActions(boolean context)
        {
           String elemType = getElementType();
           if (elemType.equals(ELEMENT_TYPE_OPERATION) ||
                 elemType.equals(ELEMENT_TYPE_ATTRIBUTE))
           {
              return super.getNewMenuAction();
              //			return new Action[]
              //			{
              //				SystemAction.get(NewAction.class)
              //			};
           }
           
           else return new Action[]{null};
        }
	
    public boolean canDestroy()
    {
        return false;
    }
	
   /**
    * Returns the element that this tree folder represents.
    *
    * @return The element that this folder represents, or NULL
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder#getElement()
    */
   public IElement getElement()
   {
      IElement retVal = null;
      
      IProjectTreeItem data = getData();
      if(data != null)
      {
         retVal = data.getModelElement();
      }
      
      return retVal;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder#setPath(java.lang.String)
    */
   public void setPath(ITreeItem[] defPath)
   {
      IProjectTreeItem data = getData();
      if(data != null)
      {
         data.setPath(defPath);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder#setIsGetMethodAnImport(boolean)
    */
   public void setIsGetMethodAnImport(boolean value)
   {
      

   }

   /**
    * Is the get method the one used to get imported packages or elements?
    *
    * retVal <b>true</b> if the get method is one used to get package or element
    *        imports.
    * @see org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder#isGetMethodAnImport()
    */
   public boolean isGetMethodAnImport()
   {
      boolean retVal = false;
      if((getGetMethod().equals("ImportedElements") == true) || 
         (getGetMethod().equals("ImportedPackages") == true))
      {
         retVal = true;
      }
      
      return retVal;
   }
   
   /**
    * Is the get method the one used to get imported elements?
    * 
    * @return <b>true</b> if the get method is an import, <b>false</b>
    *         if the get method is not an import.
    */
   public boolean isGetMethodAnImportElement()
   {
      boolean retVal = false;
      if(getGetMethod().equals("ImportedElements") == true) 
      {
         retVal = true;
      }

      return retVal;
   }

   /**
    * Is the get method the one used to get imported packages?
    * 
    * @return <b>true</b> if the get method is an import, <b>false</b>
    *         if the get method is not an import.
    */
   public boolean isGetMethodAnImportPackage()
   {
      boolean retVal = false;
     if(getGetMethod().equals("ImportedPackages") == true)
     {
        retVal = true;
     }

     return retVal;
   }
   
   public String getType()
   {
         return getName();
   }
   /* (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj)
   {
      boolean retVal = false;
      
      if (obj instanceof ITreeFolder)
      {
         ITreeFolder folder = (ITreeFolder)obj;
         
         if((getDisplayName() != null) && (folder.getDisplayName() != null))
         {
            if(getDisplayName().equals(folder.getDisplayName()) == true)
            {
               if((getID() == null) && (folder.getID() == null))
               {
                  retVal = true;
               }
               else if((getID() != null) && (folder.getID() != null))
               {
                  retVal = getID().equals(folder.getID());
               }
            }
         }
      }
      
      return retVal;
   }
   
   public void setName(String val)
   {
	   m_Name = val;
	   super.setName(val);
   }

   public String getName()
   {
	   return m_Name;
   }
   
//   public String getDisplayName()
//   {
//	   return getName();
//   }
//   
//   public String getDisplayedName()
//   {
//	   return getName();
//   }
   
   public boolean canRename()
   {
	   return false;
   }
}
