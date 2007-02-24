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
 * Created on May 27, 2003
 *
 */
package org.netbeans.modules.uml.ui.support;

import java.awt.Frame;
import java.util.Vector;

import org.netbeans.modules.uml.core.IApplication;
//import org.netbeans.modules.uml.core.addinframework.IAddIn;
//import org.netbeans.modules.uml.core.addinframework.IAddInManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IConfigManager;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlmessagingcore.IMessageService;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.IDataFormatter;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;

import org.netbeans.modules.uml.ui.support.applicationmanager.IAcceleratorManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationResourceMgr;
import org.netbeans.modules.uml.ui.support.applicationmanager.IPresentationTypesMgr;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductProjectManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProgressCtrl;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.support.messaging.IProgressDialog;
import org.netbeans.modules.uml.ui.support.messaging.IMessenger;
import org.netbeans.modules.uml.core.scm.ISCMIntegrator;
import org.netbeans.modules.uml.ui.swing.propertyeditor.IPropertyEditor;
import org.netbeans.modules.uml.ui.addins.diagramcreator.IDiagCreatorAddIn;
import org.netbeans.modules.uml.ui.addins.diagramcreator.DiagCreatorAddIn;

/**
 * @author treys
 *
 */
public class ProductHelper
{
   private static IProductDiagramManager m_DiagramManager = null;
   
	private ProductHelper()
	{
	}
	
	static public IWorkspace getWorkspace(ICoreProduct product)
	{
		IWorkspace retVal = null;
		
		if (product != null)
      {
         retVal = product.getCurrentWorkspace();
      }
				
      return retVal; 
	}
	

	/// Gets the current product
	public static ICoreProduct getCoreProduct( )
	{
		return ProductRetriever.retrieveProduct();
	}

	/// Gets the current product
	public static IProduct getProduct( )
	{
		IProduct retVal = null; 
		
		ICoreProduct product = getCoreProduct();
		if(product instanceof IProduct)
		{
			retVal = (IProduct)product;
		}
      
		return retVal;
	}

	/// Gets the current application
	public static IApplication getApplication()
	{
		return getCoreProduct().getApplication();
	}

	/// Gets the current workspace
	public static IWorkspace getWorkspace()
	{
		return getCoreProduct().getCurrentWorkspace();
	}

	/** Gets the IProductDiagramManager */
	public static IProductDiagramManager getProductDiagramManager()
	{  
      // HAVE TODO: 
      if (m_DiagramManager == null)
      {
		IProduct product = getProduct();
		if(product != null)
		{
		   m_DiagramManager = product.getDiagramManager();
		}
      }
      
      return m_DiagramManager;
	}

    /** Sets the IProductDiagramManager */
    public static void setProductDiagramManager(IProductDiagramManager manager)
    {  
//       if(getProduct() != null)
//       {
//          retVal = product.getDiagramManager();
//          product.setDiagramManager(manager);
//       }
      
       m_DiagramManager = manager;
    }
    
	/// Gets the IProxyUserInterface
	public static IProxyUserInterface getProxyUserInterface()
	{
		IProxyUserInterface retObj = null;
		IProduct prod = getProduct();
		if (prod != null)
		{
			retObj = prod.getProxyUserInterface();
		}
		return retObj;
	}
   
   public static Frame getWindowHandle()
   {
      IProxyUserInterface ui = getProxyUserInterface();
      return ui != null ? ui.getWindowHandle() : null;      
   }

	/** Gets the preference value using IPreferenceManager2 */
	public static String getPreferenceValue(String path, String name )
	{
      String retVal = "";
      
      IPreferenceManager2 manager = getPreferenceManager();
      if(manager != null)
      {
         retVal = manager.getPreferenceValue(path, name);
      }
      
		return retVal;
	}

	/** Sets the preference value using IPreferenceManager2 */
	public static void setPreferenceValue(String path, String name, String value )
	{
      IPreferenceManager2 manager = getPreferenceManager();
      if(manager != null)
      {
         manager.setPreferenceValue(path, name, value);
      }
	}

	/// Gets the IPreferenceManager2
	public static IPreferenceManager2 getPreferenceManager()
	{
      IProduct product = getProduct();
      if (product != null)
      	return product.getPreferenceManager();
//      return ProductRetriever.retrievePreferenceManager();
	  return null;
	}

	/// Gets the IProductProjectManager
	public static IProductProjectManager getProductProjectManager()
	{
		IProduct prod = getProduct();
		if (prod != null)
		{
			return prod.getProjectManager();
		}
		return null;
	}

	// Gets the IMessenger
	public static IMessenger getMessenger()
	{
		ICoreProduct prod = getCoreProduct();
		if (prod instanceof IProduct)
		{
			return ((IProduct)prod).getMessenger();
		}
		return null;
	}

	/// Gets the IMessageService
	public static IMessageService getMessageService()
	{
		ICoreProduct prod = getCoreProduct();
		if (prod != null)
		{
			return prod.getMessageService();
		}
		return null;
	}

	/// Gets the IAxProjectTreeControl
	public static IProjectTreeControl getProjectTree()
	{
		IProduct prod = getProduct();
		if (prod != null)
		{
			return prod.getProjectTree();
		}
		return null;
	}

	/// Gets the IAxProjectTreeControl
	public static IProjectTreeControl getDesignCenterTree()
	{
		IProduct prod = getProduct();
		if (prod != null)
		{
			return prod.getDesignCenterTree();
		}
		return null;
	}

	/// Gets the IConfigManager
	public static IConfigManager getConfigManager()
	{
		ICoreProduct prod = getCoreProduct();
		if (prod != null)
		{
			return prod.getConfigManager();
		}
		return null;//getCoreProduct().getConfigManager();
	}

	/// Gets the IAcceleratorManager
	public static IAcceleratorManager getAcceleratorManager()
	{
		IProduct prod = getProduct();
		if (prod != null)
		{
			return prod.getAcceleratorManager();
		}
		return null;
	}

	/// Gets the IProgressCtrl
	public static IProgressCtrl getProgressCtrl()
	{
		IProduct prod = getProduct();
		if (prod != null)
		{
			return prod.getProgressCtrl();
		}
		return null;
	}

   /// Gets the IProgressCtrl
   public static IProgressDialog getProgressDialog()
   {
      IProduct prod = getProduct();
      if (prod != null)
      {
         return prod.getProgressDialog();
      }
      return null;
   }

	/// Gets the IPropertyEditor
	public static IPropertyEditor getPropertyEditor()
	{
		IProduct prod = getProduct();
		if (prod != null)
		{
			return prod.getPropertyEditor();
		}
		return null;
	}

	/// Gets the ISCMIntegrator

	public static ISCMIntegrator getSCMIntegrator()
	{
		IProduct prod = getProduct();
		if (prod != null)
		{
			return prod.getSCMIntegrator();
		}
		return null;
	}

	/// Returns the help directory
	public String getHelpDirectory()
	{
		// Find out where the "home" directory is for the Describe installation. That
		// is where the "Application" VBA Project will be located.
		IProduct prod = getProduct();
		if (prod != null)
		{
			IConfigManager conMan = prod.getConfigManager();
			if (conMan != null)
			{
				return conMan.getDocsLocation();
			}
		}
		return null;
	}

	/**
	 * Returns the number of open projects
	 *
	 * @param bAllExtensions[in]		Whether or not to include all project types (etd, etpat) that are open
	 *
	 * return long					Number of projects that are open
	 *
	 */
	public static int getNumOpenProjects(boolean bAllExtensions)
	{
		int count = 0;
		Vector<IProject> projs = getOpenProjects(bAllExtensions);
		if (projs != null)
		{
			count = projs.size();
		}
		return count;
	}

	/**
	 * Returns the open projects
	 *
	 * @param pProjects [out] The list of the open projects
	 * @param bAllExtensions [in]	Whether or not to include all project types (etd, etpat) that are open
	 */
	public static Vector<IProject> getOpenProjects(boolean bAllExtensions)
	{
		Vector<IProject> retProjs = null;
		IProject proj;
		IApplication pApp = getApplication();
		if (pApp != null)
		{
			retProjs = new Vector<IProject>();
			ETList<IProject> normalProjs = pApp.getProjects();
			ETList<IProject> patProjs = null;
			if (bAllExtensions)
			{
				patProjs = pApp.getProjects("etpat");
			}
			
			if (normalProjs != null)
			{
				int count = normalProjs.size();
				for (int i=0; i<count; i++)
				{
					retProjs.add(normalProjs.get(i));
				}
			}
			
			if (patProjs != null)
			{
				int count = patProjs.size();
				for (int i=0; i<count; i++)
				{
					retProjs.add(patProjs.get(i));
				}
			}
		}
		return retProjs;
	}

	/// Gets the current application
	//public static IVBAIntegrator getVBAIntegrator()
	{
	}
   
	/// Gets the data formatter on the product
	public static IDataFormatter getDataFormatter()
	{
		IDataFormatter retVal = null;
      
      if(getCoreProduct() != null)
      {
         retVal = getCoreProduct().getDataFormatter();
      }
      
      return retVal;
	}

	/// Gets the addin manager associated with the current product
//	public static IAddInManager getAddInManager()
//	{
//		IProduct prod = getProduct();
//		if (prod != null)
//		{
//			return prod.getAddInManager();
//		}
//		return null;
//	}

	/// Retrieves the addin associated with the input progid, i.e. "DiagramCreatorAddIn.DiagCreatorAddIn"
//	public static IAddIn retrieveAddIn( String progID)
//	{
//		IAddInManager man = getAddInManager();
//		if (man != null)
//		{
//			return man.retrieveAddIn(progID);
//		}
//		return null;
//	}
        
        public static IDiagCreatorAddIn getDiagCreatorAddIn() {
            return new DiagCreatorAddIn();
        }

	/// Returns the unnamed element (also called default element name)
	public String getDefaultElementName()
	{
		return null;
	}

	/// Returns the name of the executable IDE that is running this code
	public String getApplicationIDEName()
	{
		return null;
	}
   
	/**
	 * Helper for showing the qualified name
	 */
	public static boolean useProjectInQualifiedName()
   {
		boolean useProject = false;
		IPreferenceManager2 cpPreferenceManager2 = getPreferenceManager();
		if( cpPreferenceManager2 != null)
		{
			String value = cpPreferenceManager2.getPreferenceValue("", "ProjectNamespace");
			if( value.equals("PSK_YES"))
			{
				useProject = true;
			}
		}
		return useProject;
   }

	/// Returns the type of the executable IDE that is running this code
	//ApplicationIDEType GetApplicationIDEType() throw();

	/// Helpers for the aliased names
	public static boolean getShowAliasedNames()
	{
		boolean bShow = false;
   
		IPreferenceManager2 cpPreferenceManager2 = getPreferenceManager();
		if( cpPreferenceManager2 != null)
		{
			bShow = cpPreferenceManager2.getShowAliasedNames();
		}
		return bShow;
	}
	
	public static void setShowAliasedNames(boolean bShow)
	{
		IPreferenceManager2 cpPreferenceManager2 = getPreferenceManager();
		if( cpPreferenceManager2 != null)
		{
			cpPreferenceManager2.setShowAliasedNames(bShow);
		}
	}
	
	public static void toggleShowAliasedNames()
	{
		IPreferenceManager2 cpPreferenceManager2 = getPreferenceManager();
		if( cpPreferenceManager2 != null)
		{
		   cpPreferenceManager2.toggleShowAliasedNames();
		}
	}
	
	private static ProductHelper m_Instance = null;  

	/// The gui thread id.  Used to cache up the core product
	//private static DWORD m_ThreadIDForCoreProduct;

	/// The core product for the thread m_ThreadIDForCoreProduct
	private static ICoreProduct m_CoreProductForThreadID = null;/**
    * @param string
    * @param string2
    * @param b
    */
   public static void setPreferenceValue(String string, String string2, boolean b)
   {
      // TODO Auto-generated method stub
      
   }

   public static IPresentationTypesMgr getPresentationTypesMgr()
   {
		IPresentationTypesMgr pPresentationTypesMgr = null;
		
		IProduct pProduct = getProduct();
		if (pProduct != null)
		{
			pPresentationTypesMgr = pProduct.getPresentationTypesMgr();
		}
		
		return pPresentationTypesMgr;
   }
   
   public static IPresentationResourceMgr getPresentationResourceMgr()
   {
   		IPresentationResourceMgr pMgr = null;

		IProduct pProduct = getProduct();
		if (pProduct != null)
		{
			pMgr = pProduct.getPresentationResourceMgr();
		}
		return pMgr;
   }
   
	/// The name of the application IDE that this DLL is running in
	//CComBSTR m_bsApplicationIDEName; // access only via GetApplicationIDEName()

	/// The type of the application IDE that this DLL is running in
	//ApplicationIDEType m_ApplicationIDEType; // access only via GetApplicationIDEType()
	
}



