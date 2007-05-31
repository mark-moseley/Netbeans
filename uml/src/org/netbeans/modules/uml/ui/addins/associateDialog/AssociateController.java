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

package org.netbeans.modules.uml.ui.addins.associateDialog;


import java.awt.Frame;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
//import org.netbeans.modules.uml.associatewith.*;
import org.netbeans.modules.uml.ui.addins.associateDialog.*;

import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.CoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.ICoreProductManager;
import org.netbeans.modules.uml.core.coreapplication.IProductDescriptor;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IActor;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ITaggedValue;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IFeature;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.core.typemanagement.ITypeManager;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.ADProduct;
import org.netbeans.modules.uml.ui.products.ad.applicationcore.IADProduct;
import org.netbeans.modules.uml.ui.support.DiagramAndPresentationNavigator;
import org.netbeans.modules.uml.ui.support.IDiagramAndPresentationNavigator;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogKind;
import org.netbeans.modules.uml.ui.support.SimpleQuestionDialogResultKind;
import org.netbeans.modules.uml.ui.support.SwingPreferenceQuestionDialog;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.diagramsupport.IPresentationTarget;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.PresentationFinder;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;

/**
 * @author sumitabhk
 *
 *
 */
public class AssociateController implements IAssociateController
{
	private int m_Scope = 0;
	private int m_ResultType = 0;
	private String m_SearchString = "";
	private int m_Kind = 0;
	private String m_ReplaceString = "";
	private boolean m_DiagramNavigate = true;
	private boolean m_WholeWordSearch = false;
	private boolean m_SearchAlias = false;
	private boolean m_CaseSensitive = true;
	private boolean m_IsReplace = false;
	private ArrayList m_Projects = new ArrayList();
	private boolean m_Cancelled = false;
	private boolean m_ExternalLoad = false;
	private long m_ActiveWindow = 0;
	private IProject m_Project = null;
	private JDialog m_Dialog = null;

	/**
	 *
	 */
	public AssociateController()
	{
		super();
	}

//	public void showFindDialog()
//	{
//		setIsReplace(false);
//		Frame parent = ProductHelper.getProxyUserInterface().getWindowHandle();
//		FindDialogUI ui = new FindDialogUI(parent, true);
//		ui.doLayout();
//		//ui.setVisible(true);
//		ui.setModal(true);
//		ui.setController(this);
//		ui.show();
//	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#getScope()
	 */
	public int getScope()
	{
		return m_Scope;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#setScope(int)
	 */
	public void setScope(int value)
	{
		m_Scope = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#getSearchString()
	 */
	public String getSearchString()
	{
		return m_SearchString;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#setSearchString(java.lang.String)
	 */
	public void setSearchString(String value)
	{
		m_SearchString = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#getKind()
	 */
	public int getKind()
	{
		return m_Kind;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#setKind(int)
	 */
	public void setKind(int value)
	{
		m_Kind = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#getCaseSensitive()
	 */
	public boolean getCaseSensitive()
	{
		return m_CaseSensitive;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#setCaseSensitive(int)
	 */
	public void setCaseSensitive(boolean value)
	{
		m_CaseSensitive = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#getReplaceString()
	 */
	public String getReplaceString()
	{
		return m_ReplaceString;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#setReplaceString(java.lang.String)
	 */
	public void setReplaceString(String value)
	{
		m_ReplaceString = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#getIsReplace()
	 */
	public boolean getIsReplace()
	{
		return m_IsReplace;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#setIsReplace(int)
	 */
	public void setIsReplace(boolean value)
	{
		m_IsReplace = value;
	}

	public void setDialog(JDialog diag)
	{
		m_Dialog = diag;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#search(org.netbeans.modules.uml.ui.support.finddialog.IFindResults)
	 */
	public void search(IAssociateResults pResults)
	{
		// get the type of search the user selected
		if (m_Kind == 1) //FIND_KIND_ADVANCED
		{
			// If the user has chosen to do an advanced search
			// we take exactly what they typed in and search for it
			// this is for those users that know xpath queries
			searchUsingElementLocator(m_SearchString, pResults);
		}
		else
		{
			// search by what they typed in (name or description will be searched)
			if ((m_SearchString.indexOf('*') >= 0) && (!m_WholeWordSearch))
			{
				// found a wildcard
				searchWithWildcard(pResults);
			}
			else
			{
            // no wildcard found, so if they want to do a case sensitive search,
            // we can use the element locator
            if (m_CaseSensitive)
            {
            	String tempStr;
               	// case sensitive
				// now need to check if we are doing a find or a replace
				if (!m_IsReplace)
				{
					if (m_ResultType == 0)//FIND_TYPE_ELEMENT)
					{
						// just a find, so we can group the name and alias strings together
						tempStr = "//*[contains(@name, \"";
						tempStr += m_SearchString;
						tempStr += "\")";
						if (m_SearchAlias)
						{
							tempStr += " or contains(@alias, \"";
							tempStr += m_SearchString;
							tempStr += "\")";
						}
						tempStr += "]";
						// if this search has returned S_FALSE then the user was asked about something
						// and they said not to continue
					   	if (searchUsingElementLocator(tempStr, pResults))
						{
							searchForDiagrams(tempStr, pResults);
							process(m_SearchString, -1, /*WILDCARD_LOC_UNKNOWN,*/ pResults);
						}
					}
					else
					{
							tempStr = "//*";
							// if this search has returned S_FALSE then the user was asked about something
							// and they said not to continue
							if (searchUsingElementLocator(tempStr, pResults))
							{
								searchForDiagrams(tempStr, pResults);
								process(m_SearchString, -1, /*WILDCARD_LOC_UNKNOWN,*/ pResults);
							}
						}
					}
					else
					{
						// this is a replace, so we will be looking for name, description or alias
						if (m_SearchAlias)
						{
							tempStr = "//*[contains(@alias, \"";
							tempStr += m_SearchString;
							tempStr += "\")";
							// since there are cases where the name and the alias are the same, there
							// may not be an @alias attribute so we need to check the @name too
							tempStr += " or (not(@alias) and contains(@name, \"";
							tempStr += m_SearchString;
							tempStr += "\"))]";
							// if this search has returned S_FALSE then the user was asked about something
							// and they said not to continue
						   	if (searchUsingElementLocator(tempStr, pResults))
							{
								searchForDiagrams(tempStr, pResults);
								process(m_SearchString, -1, /*WILDCARD_LOC_UNKNOWN,*/ pResults);
							}
						}
						else if (m_ResultType == 0)//FIND_TYPE_ELEMENT)
						{
							tempStr = "//*[contains(@name, \"";
							tempStr += m_SearchString;
							tempStr += "\")]";
							// if this search has returned S_FALSE then the user was asked about something
							// and they said not to continue
						    if (searchUsingElementLocator(tempStr, pResults))
							{
								searchForDiagrams(tempStr, pResults);
								process(m_SearchString, -1, /*WILDCARD_LOC_UNKNOWN,*/ pResults);
							}
						}
						else
						{
							tempStr = "//*";
							// if this search has returned S_FALSE then the user was asked about something
							// and they said not to continue
							if (searchUsingElementLocator(tempStr, pResults))
							{
								searchForDiagrams(tempStr, pResults);
								process(m_SearchString, -1, /*WILDCARD_LOC_UNKNOWN,*/ pResults);
							}
						}
					}
            }
            else
            {
               // no wildcard found, but since they don't care about case, we can't use
               // the element locator directly
				   String tempStr = "//*";
					// if this search has returned S_FALSE then the user was asked about something
					// and they said not to continue
				    if (searchUsingElementLocator(tempStr, pResults))
					{
						searchForDiagrams(tempStr, pResults);
						process(m_SearchString, -1, /*WILDCARD_LOC_UNKNOWN,*/ pResults);
					}
            }
			}
		}
	}

	/**
	 * Show the replace dialog.
	 */
//	public void showReplaceDialog()
//	{
//		setIsReplace(true);
//        // the dialog needs to know about the find controller
//
//		Frame parent = ProductHelper.getProxyUserInterface().getWindowHandle();
//		ReplaceDialogUI ui = new ReplaceDialogUI(parent, true);
//		ui.doLayout();
//		//ui.setVisible(true);
//		ui.setModal(true);
//		ui.setController(this);
//		ui.show();
//	}


	public boolean navigateToElement(IElement pElement)
	{
		boolean result = true;
		if (pElement != null)
		{
			AssociateUtilities.startWaitCursor(m_Dialog);
	      	// get the core product
			IProduct pProduct = ProductHelper.getProduct();
		    if (pProduct != null)
		    {
				// find in tree
				IProjectTreeControl pTree = pProduct.getProjectTree();
				if (pTree != null)
				{
					pTree.findAndSelectInTree(pElement);
				}
				if (m_DiagramNavigate)
				{
					result = findInDiagrams(pElement);
				}
			}
			AssociateUtilities.endWaitCursor(m_Dialog);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#getResultType()
	 */
	public int getResultType()
	{
		return m_ResultType;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#setResultType(int)
	 */
	public void setResultType(int value)
	{
		m_ResultType = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#addToProjectList(java.lang.String)
	 */
	public void addToProjectList(String newVal)
	{
		m_Projects.add(newVal);
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#clearProjectList()
	 */
	public void clearProjectList()
	{
		m_Projects.clear();
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#replace(org.netbeans.modules.uml.ui.support.finddialog.IFindResults)
	 */
        public void replace(IAssociateResults pResults) {
            //go thru each elements in find results
            try {
                replaceInElements(pResults);
            } catch(Exception ex) {
                Frame parent = ProductHelper.getProxyUserInterface().getWindowHandle();
                JOptionPane.showMessageDialog(parent,
                DefaultAssociateDialogResource.getString("IDS_NOREPLACESTR"),
                DefaultAssociateDialogResource.getString("IDS_REPLACETITLE"),
                JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            //go thru each diagrams in find results
            try {
                replaceInDiagrams(pResults);
            } catch(Exception ex) {
                Frame parent = ProductHelper.getProxyUserInterface().getWindowHandle();
                JOptionPane.showMessageDialog(parent,
                DefaultAssociateDialogResource.getString("IDS_NOREPLACESTR"),
                DefaultAssociateDialogResource.getString("IDS_REPLACETITLE"),
                JOptionPane.INFORMATION_MESSAGE);
                return;
            }

        }

        private void replaceInElements(IAssociateResults pResults) throws Exception {
            // get the elements from the results object
            ETList<IElement> pElements = pResults.getElements();
            if (pElements != null) {
                // loop through the elements
                int eleCount = pElements.size();
                for (int y = 0; y < eleCount; y++) {
                    IElement pElement = pElements.get(y);
                    if (pElement != null) {
                        // "replace" its information
                        if (m_ResultType == 0)//FIND_TYPE_ELEMENT)
                        {
                            // if it is a named element
                            if (pElement instanceof INamedElement) {
                                INamedElement pNamedElement = (INamedElement)pElement;
                                if (pNamedElement != null) {
                                    String name = pNamedElement.getName();
                                    String str = replaceValue(name);
                                    pNamedElement.setName(str);
                                }
                            }
                        }
                        else if (m_SearchAlias) {
                            // if it is a named element
                            if (pElement instanceof INamedElement) {
                                INamedElement pNamedElement = (INamedElement)pElement;
                                if (pNamedElement != null) {
                                    String name = pNamedElement.getAlias();
                                    String str = replaceValue(name);
                                    pNamedElement.setAlias(str);
                                }
                            }
                        }
                        else {
                            String doc = pElement.getDocumentation();
                            String str = replaceValue(doc);
                            pElement.setDocumentation(str);
                        }
                    }
                }
            }
        }

        private void replaceInDiagrams(IAssociateResults pResults) throws Exception {
            // now loop through the diagrams in the result object
            ETList<IProxyDiagram> pDiagrams = pResults.getDiagrams();
            if (pDiagrams != null) {
                // loop through the elements
                int count = pDiagrams.size();
                for (int y = 0; y < count; y++) {
                    IProxyDiagram pDiagram = pDiagrams.get(y);
                    if (pDiagram != null) {
                        // "replace" its information
                        if (m_ResultType == 0)//FIND_TYPE_ELEMENT)
                        {
                            String name = pDiagram.getName();
                            String str = replaceValue(name);
                            pDiagram.setName(str);
                        }
                        else if (m_SearchAlias) {
                            String name = pDiagram.getAlias();
                            String str = replaceValue(name);
                            pDiagram.setAlias(str);
                        }
                        else {
                            String doc = pDiagram.getDocumentation();
                            String str = replaceValue(doc);
                            pDiagram.setDocumentation(str);
                        }
                    }
                }
            }

        }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#getSearchAlias()
	 */
	public boolean getSearchAlias()
	{
		return m_SearchAlias;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#setSearchAlias(int)
	 */
	public void setSearchAlias(boolean value)
	{
		m_SearchAlias = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#search2(com.embarcadero.describe.structure.IProject, org.netbeans.modules.uml.ui.support.finddialog.IFindResults)
	 */
        public void search2(IProject pProject, IAssociateResults pResults)
        {
            m_Project = pProject;
            search(pResults);
        }

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#navigateToDiagram(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram)
	 */
	public boolean navigateToDiagram(IProxyDiagram pDiagram)
	{
		boolean result = true;
	    // get the core product
		if (m_DiagramNavigate)
		{
			AssociateUtilities.startWaitCursor(m_Dialog);
			if (pDiagram != null)
			{
				IProduct pProduct = ProductHelper.getProduct();
				if (pProduct != null)
				{
					IProductDiagramManager pManager = pProduct.getDiagramManager();
					if (pManager != null)
					{
						pManager.openDiagram2(pDiagram, true, null);
					}
				}
			}
			AssociateUtilities.endWaitCursor(m_Dialog);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#getExternalLoad()
	 */
	public boolean getExternalLoad()
	{
		return m_ExternalLoad;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#setExternalLoad(int)
	 */
	public void setExternalLoad(boolean value)
	{
		m_ExternalLoad = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#getCancelled()
	 */
	public boolean getCancelled()
	{
		return m_Cancelled;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#setCancelled(int)
	 */
	public void setCancelled(boolean value)
	{
		m_Cancelled = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#getActiveWindow()
	 */
	public long getActiveWindow()
	{
		return m_ActiveWindow;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#setActiveWindow(int)
	 */
	public void setActiveWindow(long value)
	{
		m_ActiveWindow = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#getWholeWordSearch()
	 */
	public boolean getWholeWordSearch()
	{
		return m_WholeWordSearch;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#setWholeWordSearch(int)
	 */
	public void setWholeWordSearch(boolean value)
	{
		m_WholeWordSearch = value;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#getDiagramNavigate()
	 */
	public boolean getDiagramNavigate()
	{
		return m_DiagramNavigate;
	}

	/* (non-Javadoc)
	 * @see org.netbeans.modules.uml.ui.support.finddialog.IFindController#setDiagramNavigate(int)
	 */
	public void setDiagramNavigate(boolean value)
	{
		m_DiagramNavigate = value;
	}

	public boolean searchUsingElementLocator(String searchString, IAssociateResults pResults)
	{
		boolean flag = true;
		if (searchString != null && searchString.length() > 0)
		{
			// if the user has unchecked the case sensitive flag, the search may take awhile
			// due to using //* in the ElementLocator
			// warn the user
			boolean bContinue = warnUserAboutTime();
			if (bContinue)
			{
				if (!m_CaseSensitive)
				{
					// TODO
					//HANDLE event = CreateEvent(NULL, TRUE, FALSE, _T("FINDDLG"));
					//MyWaitDlgThread *thread = (MyWaitDlgThread *)AfxBeginThread(RUNTIME_CLASS(MyWaitDlgThread), THREAD_PRIORITY_NORMAL, 0, CREATE_SUSPENDED);
					//thread->m_Eventname = "FINDDLG";
					//thread->m_FindController = this;
					//if (::AfxGetMainWnd())
					//{
					//   thread->m_AlternateHWNDOwner = ::AfxGetMainWnd()->m_hWnd;
					//}
					//thread->ResumeThread();
					m_Cancelled = false;
					//while ((WaitForSingleObject(event, 0)==WAIT_TIMEOUT) && (m_Cancelled == false))
					while (!m_Cancelled)
					{
						if (!searchUsingElementLocator2(searchString, pResults))
						{
							m_Cancelled = true;
						}
						break;
					}
					//if (thread)
					//{
						//thread->m_bCloseDialog = TRUE;
					//}
					//if (m_Cancelled)
					//{
					  // pResults->put_Elements(0);
					//}
				}
				else
				{
					searchUsingElementLocator2(searchString, pResults);
				}
			}
			else
			{
				// return S_FALSE then the user was asked about something
				// and they said not to continue
				flag = false;
			}
		}
		return flag;
	}

	public boolean searchUsingElementLocator2(String searchString, IAssociateResults pResults)
	{
		boolean flag = true;
		if (searchString != null && searchString.length() > 0)
		{
			if (m_Project != null)
			{
				flag = searchUsingElementLocatorForProject(searchString, m_Project, pResults);
			}
			else
			{
				// get the right projects - this will either be all of the projects in the workspace if the user chose
				// workspace, or the projects that they told us to look through
				ETList<IProject> pProjects = getProjects(true);
				if (pProjects != null)
				{
					// loop through the projects that we determined were the correct ones
					int count = pProjects.size();
					for (int x = 0; x < count; x++)
					{
						if (m_Cancelled)
						{
							break;
						}
						IProject wsProject = pProjects.get(x);
						if (wsProject != null)
						{
							// need to get the IProject, not the WSProject in order to use the element locator
							String name = wsProject.getName();
							IApplication pApp = ProductHelper.getApplication();
							if (pApp != null)
							{
								IProject pProject = pApp.getProjectByName(name);
								if (pProject != null)
								{
									flag = searchUsingElementLocatorForProject(searchString, pProject, pResults);
								}
							}
						}
					}
				}
			}
		}
		return flag;
	}

	public boolean searchUsingElementLocatorForProject(String searchString, IProject pProject, IAssociateResults pResults)
	{
		boolean flag = true;
		if (searchString != null && searchString.length() > 0)
		{
			if (m_Cancelled)
			{
				flag = false;
				return flag;
			}
			if (m_ExternalLoad)
			{
	            ITypeManager typeMan = pProject.getTypeManager();
	            if( typeMan != null )
	            {
					typeMan.loadExternalElements();
				}
			}
			//
			// Get the elements from the results object.  This will be what we add to
			//
			ETList<IElement> pTempElements = pResults.getElements();
			if (pTempElements != null)
			{
				// find the elements matching the string
				IElementLocator pLocator = new ElementLocator();
				if (pLocator != null)
				{
					if (pProject instanceof INamespace)
					{
						INamespace pNamespace = (INamespace)pProject;
						ETList<IElement> pElements = pLocator.findElementsByDeepQuery( pNamespace, searchString );
						if (pElements != null)
						{
							// loop through the found elements
							int eleCount = pElements.size();
							for (int y = 0; y < eleCount; y++)
							{
								if (m_Cancelled)
								{
									flag = false;
									break;
								}
								IElement pElement = pElements.get(y);
								if (pElement != null)
								{
									boolean add = true;
									if (pElement instanceof ITaggedValue)
									{
										ITaggedValue pTV = (ITaggedValue)pElement;
										if (pTV != null)
										{
											// don't want to include a tagged value of type documentation
											String name = pTV.getName();
											if (name.equals("documentation"))
											{
												add = false;
											}
											// also don't want to include hidden tagged values
											boolean bHidden = pTV.isHidden();
											if (bHidden)
											{
												add = false;
											}
										}
									}
									if (add)
									{
										pTempElements.add(pElement);
									}
								}
							}
						}
					}
				}
			}
		}
		return flag;
	}

	public void searchWithWildcard(IAssociateResults pResults)
	{
      // where is the wildcard located in the string - the first, last, or somewhere in the middle
      //WildcardLocation loc = GetLocationOfWildcard(m_SearchString);
      int loc = getLocationOfWildcard(m_SearchString);
      if ( (loc == 1/*WILDCARD_LOC_LAST*/) || (loc == 0/*WILDCARD_LOC_FIRST*/) )
      {
         // if the wildcard is the first or last character, we can remove it, and then begin the search
         String toMatch = m_SearchString;
         toMatch.replace('*', ' ');
         String search = toMatch;
         if (m_CaseSensitive)
         {
        	// if we are case sensitive, we can use the element locator on a much narrower search
			String tempStr;
			if ( (m_ResultType == 0)/*FIND_TYPE_ELEMENT)*/ || (m_SearchAlias) )
			{
				tempStr = buildXPathString(m_SearchString);
			}
			else
			{
				tempStr = "//*";
			}
			// if this search has returned S_FALSE then the user was asked about something
			// and they said not to continue
		    if (searchUsingElementLocator(tempStr, pResults))
			{
				searchForDiagrams(tempStr, pResults);
				process(search, loc, pResults);
			}
         }
         else
         {
            // since we are not case sensitive, we have to get all of the elements and then do more
            // processing on their name
			   String tempStr = "//*";
				// if this search has returned S_FALSE then the user was asked about something
				// and they said not to continue
			    if (searchUsingElementLocator(tempStr, pResults))
				{
					searchForDiagrams(tempStr, pResults);
					process(search, loc, pResults);
				}
         }
      }
      else if (loc == 2/*WILDCARD_LOC_MIDDLE*/)
      {
		String tempStr = "//*";
		// if this search has returned S_FALSE then the user was asked about something
		// and they said not to continue
		if (searchUsingElementLocator(tempStr, pResults))
		{
			searchForDiagrams(tempStr, pResults);
			process(m_SearchString, loc, pResults);
		}
      }
	}

	public boolean findInDiagrams(IElement pElement)
	{
		boolean result = true;
		if (pElement != null)
		{
			AssociateUtilities.startWaitCursor(m_Dialog);
	      	// get the core product
			IProduct pProduct = ProductHelper.getProduct();
			if (pProduct != null)
			{
		        // get the presentation finder off of the IProduct
				PresentationFinder pPresentationFinder = new PresentationFinder();
				if (pPresentationFinder != null)
				{
					// find in diagrams
					ETList<IPresentationTarget> pPresentationTargets = null;
					if (pElement instanceof IActor)
					{
						pPresentationTargets = pPresentationFinder.getPresentationTargets(pElement);
					}
					else if (pElement instanceof IFeature)
					{
						IFeature pFeature = (IFeature)pElement;
						if (pFeature != null)
						{
							IClassifier pClassifier = pFeature.getFeaturingClassifier();
							if (pClassifier != null)
							{
								pPresentationTargets = pPresentationFinder.getPresentationTargets(pClassifier);
							}
						}
					}
					else
					{
						pPresentationTargets = pPresentationFinder.getPresentationTargets(pElement);
					}
					if (pPresentationTargets != null)
					{
		            	int count = pPresentationTargets.size();
		            	if (count > 0)
						{
							IDiagramAndPresentationNavigator pNavigator = new DiagramAndPresentationNavigator();
							if (pNavigator != null)
							{
								boolean bHandled = pNavigator.navigateToPresentationTarget(0, pElement, pPresentationTargets);
							}
						}
						else
						{
							result = false;
						}
					}
					else
					{
						result = false;
					}
				}
			}
			AssociateUtilities.endWaitCursor(m_Dialog);
		}
		return result;
	}

	public ETList<IProject> getProjects(boolean bAskAboutUnopened)
	{
		// TODO Right now getting all projects - see c++ for details
		// get the workspace from the core product
		IADProduct mADProduct = getProduct();
		IApplication app= mADProduct.initialize2(false);
		ETList<IProject> openProjects = app.getProjects();
		ETList<IProject> pProjects = new ETArrayList <IProject>();  
		try {
//			IWorkspace pWorkspace = ProductHelper.getWorkspace();
//			if (pWorkspace != null)
//			{
				// get all of the projects in the workspace
//				ETList<IWSProject> wsProjects = pWorkspace.getWSProjects();
//				if (wsProjects != null)
//				{
//					ETList<IWSProject> unopened = new ETArrayList<IWSProject>();
					// loop through the list of projects in the workspace
					int count = openProjects.size();
					for (int x = 0; x < count; x++)
					{
						IProject allProject = openProjects.get(x);
						if (allProject != null)
						{
						  // if they have chosen to search the workspace, add the project, no questions asked
						  if (m_Scope == 1/*FIND_SCOPE_WORKSPACE*/)
						  {
//							 boolean isOpen = wsProject.isOpen();
//							 if (isOpen)
//							 {
								pProjects.add(allProject);
//							 }
//							 else
//							 {
//								unopened.add(wsProject);
//							 }
						  }
						  else
						  {
							 // they have chosen to search by project, they may have more than one project selected
							 // so we need to check what they have selected
							 String name = allProject.getName();
							 if (name.length() > 0)
							 {
								// check the list of projects that they have selected
								for (int x2 = 0; x2 < m_Projects.size(); x2++)
								{
								   Object obj = m_Projects.get(x2);
								   if (obj instanceof String)
								   {
									   String projName = (String)obj;
									   if (projName.equals(name))
									   {
										  // have it selected, so add it to our list
//										  boolean isOpen = wsProject.isOpen();
//										  if (isOpen)
//										  {
											pProjects.add(allProject);
//										  }
//										  else
//										  {
//											 unopened.add(wsProject);
//										  }
										  break;
										}
								   }
								}
							}
						  }
						}
					}
		}
		catch (Exception e)
		{}
		return pProjects;
	}

	public String buildXPathString(String searchString)
	{
		String rv = "";
		if (searchString != null)
		{
			String str = searchString;
			if (searchString.indexOf('*') == 0)
			{
				// if the wildcard is the first character
				String str2 = searchString.equals("*")?"":str.substring(1, str.length() - 1);
				// if the wildcard is the last character
			  if (!m_IsReplace)
			  {
				  rv = "//*[contains(@name, \"";
				  rv += str2;
				  rv += "\")";
				  if (m_SearchAlias)
				  {
					  rv += " or contains(@alias, \"";
					  rv += str2;
					  rv += "\")";
				  }
				  rv += "]";
			  }
			  else
			  {
				  // doing a replace
				  if (m_SearchAlias)
				  {
					  rv = "//*[contains(@alias, \"";
					  rv += str2;
					  rv += "\")]";
				  }
				  else if (m_ResultType == 0/*FIND_TYPE_ELEMENT*/)
				  {
					  rv = "//*[contains(@name, \"";
					  rv += str2;
					  rv += "\")]";
				  }
				  else
				  {
				  }
				}
			}
			else
			{
				int last = str.length() - 1;
				if (str.indexOf('*') == last)
				{
				  String str2 = str.substring(0, last-1);
				  if (!m_IsReplace)
				  {
					  // if the wildcard is the last character
					  rv = "//*[starts-with(@name, \"";
					  rv += str2;
					  rv += "\")";
					  if (m_SearchAlias)
					  {
						  rv += " or starts-with(@alias, \"";
						  rv += str2;
						  rv += "\")";
					  }
					  rv += "]";
				  }
				  else
				  {
					  // doing a replace
					  if (m_SearchAlias)
					  {
						  rv = "//*[starts-with(@alias, \"";
						  rv += str2;
						  rv += "\")]";
					  }
					  else if (m_ResultType == 0/*FIND_TYPE_ELEMENT*/)
					  {
						  rv = "//*[starts-with(@name, \"";
						  rv += str2;
						  rv += "\")]";
					  }
					  else
					  {
					  }
				  }
				}
				else
				{
				}
			}
		}
		return rv;
	}

	public int getLocationOfWildcard(String searchString)
	{
		int loc = -1;
		if (searchString != null)
		{
			if (searchString.indexOf('*') == 0)
			{
				loc = 0; // WILDCARD_LOC_FIRST
			}
			else
			{
				int last = searchString.length() - 1;
				if (searchString.indexOf('*') == last)
				{
					loc = 1; // WILDCARD_LOC_LAST
				}
				else
				{
					loc = 2; // WILDCARD_LOC_MIDDLE
				}
			}
		}
		return loc;
	}

	public void process(String toMatch, int wildcardLoc, IAssociateResults pResults)
	{
		if (pResults != null)
		{
			processElements(toMatch, wildcardLoc, pResults);
			processDiagrams(toMatch, wildcardLoc, pResults);
		}
	}

	public void processElements(String toMatch, int wildcardLoc, IAssociateResults pResults)
	{
		if (toMatch != null && pResults != null)
		{
		  // get the elements from the results object
		  ETList<IElement> tempElements = pResults.getElements();
		  if (tempElements != null)
		  {
			// loop through the elements that we think might be a match
			int count = tempElements.size();
			for (int x = count - 1; x >= 0; x--)
			{
				if (m_Cancelled)
				{
					break;
				}
				IElement pElement = tempElements.get(x);
				if (pElement != null)
				{
					String value = getValueToUse(pElement);
					if (value != null && value.length() > 0)
					{
						boolean bSame = compareValues(toMatch, value, wildcardLoc);
						if (!(bSame))
						{
							// it didn't make it into the found elements yet because of its name or documentation
							// see if the user wanted to also search the alias field
							if (m_SearchAlias && !m_IsReplace)
							{
								if (pElement instanceof INamedElement)
								{
									INamedElement pNamedElement = (INamedElement)pElement;
									if (pNamedElement != null)
									{
										String alias = pNamedElement.getAlias();
										boolean bValid2 = compareValues(toMatch, alias, wildcardLoc);
										if (!(bValid2))
										{
											// did not match, so remove it
											tempElements.remove(x);
										}
									}
								}
							}
							else
							{
								tempElements.remove(x);
							}
						}
					}
					else
					{
						tempElements.remove(x);
					}
				}
			}
		  }
		}
	}

	public void processDiagrams(String toMatch, int wildcardLoc, IAssociateResults pResults)
	{
		if (toMatch != null && pResults != null)
		{
			String toMatch2 = toMatch;
			// get the diagrams from the results object
			ETList<IProxyDiagram> pDiagrams = pResults.getDiagrams();
			if (pDiagrams != null)
			{
				// loop through the diagrams
				int count = pDiagrams.size();
				for (int x = count - 1; x >= 0; x--)
				{
					IProxyDiagram pDiagram = pDiagrams.get(x);
					if (pDiagram != null)
					{
						// are we in the replace dialog
						String str = "";
						if (!m_IsReplace)
						{
							// no, in the find dialog, so get the name or the documentation of the diagram
							if (m_ResultType == 0/*FIND_TYPE_ELEMENT*/)
							{
								str = pDiagram.getName();
							}
							else
							{
								str = pDiagram.getDocumentation();
							}
							// had a problem where str was coming back null
							if (str == null){
								str = "";
							}
							// should it be case sensitive
							String str2 = str;
							if (!m_CaseSensitive)
							{
								str2 = str.toLowerCase();
								toMatch2 = toMatch.toLowerCase();
							}
							// determine if the info matches the passed in string
							boolean valid = process2(toMatch2, str2, wildcardLoc);
							if (!valid)
							{
								// it didn't make it into the found elements yet because of its name or documentation
								// see if the user wanted to also search the alias field
								if (m_SearchAlias)
								{
									// we aren't doing this check yet, because diagrams don't have an alias
									// property, but leaving it here, so when they do we can use it
									String alias = pDiagram.getAlias();
									if (alias == null){
										alias = "";
									}
									String alias2 = alias;
									if (!m_CaseSensitive)
									{
										alias2 = alias.toLowerCase();
									}
									// determine if the info matches the passed in string
									boolean valid2 = process2(toMatch2, alias2, wildcardLoc);
									if (!valid2)
									{
										// does not, so remove it
										pDiagrams.remove(x);
									}
								}
								else
								{
									// does not, so remove it
									pDiagrams.remove(x);
								}
							}
						}
						else
						{
							// in the replace dialog, so get the name or the documentation of the diagram
							if (m_ResultType == 0/*FIND_TYPE_ELEMENT*/)
							{
								str = pDiagram.getName();
							}
							else if (m_SearchAlias)
							{
								str = pDiagram.getAlias();
							}
							else
							{
								str = pDiagram.getDocumentation();
							}
							// should it be case sensitive
							if (str == null){
								str = "";
							}
							String str2 = str;
							if (!m_CaseSensitive)
							{
								str2 = str.toLowerCase();
								toMatch2 = toMatch.toLowerCase();
							}
							// determine if the info matches the passed in string
							boolean valid = process2(toMatch2, str2, wildcardLoc);
							if (!valid)
							{
								// does not, so remove it
								pDiagrams.remove(x);
							}
						}
					}
				}
			}
		}
	}

	public boolean process2(String toMatch, String value, int wildcardLoc)
	{
		boolean rv = false;
		if (toMatch != null && value != null)
		{
			if (wildcardLoc == -1/*WILDCARD_LOC_UNKNOWN*/)
			{
				// if not doing a whole word search then just look for what was typed in in what
				// was passed in
				if (!m_WholeWordSearch)
				{
					if (value.indexOf(toMatch) > -1)
					{
						rv = true;
					}
				}
				else
				{
					// we are doing a whole word search
					int pos = value.indexOf(toMatch);
					if ( (pos > -1 ) && ((toMatch.length() == value.length())) )
					{
						// if we found the substring in the string and the substring and string length is
						// the same
						rv = true;
					}
					else if (pos > -1)
					{
						boolean bFirst = false;
						if (pos == 0)
						{
							bFirst = true;
						}
						boolean bLast = false;
						if (pos + toMatch.length() == value.length())
						{
							bLast = true;
						}

						if ( bLast )
						{
							char charBefore = value.charAt(pos - 1);
							if (charBefore == ' ')
							{
								rv = true;
							}
						}
						else if (bFirst)
						{
							char charAfter = value.charAt(pos + toMatch.length());
							if (charAfter == ' ')
							{
								rv = true;
							}
						}
						else
						{
							if (pos + toMatch.length() < value.length())
							{
								char charBefore = value.charAt(pos - 1);
								char charAfter = value.charAt(pos + toMatch.length());
								if ( (charBefore == ' ') && (charAfter == ' ') )
								{
									rv = true;
								}
							}
						}
					}
				}
			}
			else if (wildcardLoc == 1/*WILDCARD_LOC_LAST*/)
			{
				if (value.indexOf(toMatch) == 0)
				{
					  rv = true;
				}
			}
			else if (wildcardLoc == 0/*WILDCARD_LOC_FIRST*/)
			{
				int len = value.length();
				int found = value.indexOf(toMatch);
				if (found > -1)
				{
				   if (len - found == toMatch.length())
				   {
					  rv = true;
				   }
				}
			}
			else if (wildcardLoc == 2/*WILDCARD_LOC_MIDDLE*/)
			{
				int len = value.length();
				int found = toMatch.indexOf('*');
				String first = toMatch.substring(0, found-1);
				if (value.indexOf(first) == 0)
				{
				   String second = toMatch.substring(found+1, toMatch.length() - 1);
				   int found2 = value.indexOf(second);
				   if (found2 > -1)
				   {
					  if (len - found2 == second.length())
					  {
						 rv = true;
					  }
				   }
				}
			}
		}
		return rv;
	}

	public void searchForDiagrams(String searchString, IAssociateResults pResults)
	{
		if (pResults != null)
		{
			if (searchString != null && searchString.length() > 0)
			{
				// if we have a project, then we do not need to figure out which ones to search
				if (m_Project != null)
				{
					searchForDiagrams2(searchString, m_Project, pResults);
				}
				else
				{
					// get the application
//					IApplication pApp = ProductHelper.getApplication();

					IADProduct mADProduct = getProduct();
				    if(mADProduct != null)
				    {         
					IApplication pApp = mADProduct.initialize2(false);
					if (pApp != null)
					{
						// get the right projects - this will either be all of the projects in the workspace if the user chose
						// workspace, or the projects that they told us to look through
//						ETList<IWSProject> pProjects = getProjects(false);
						ETList<IProject> pProjects = getProjects(false);
						if (pProjects != null)
						{
							// loop through the projects that we determined were the correct ones
							int count = pProjects.size();
							for (int x = 0; x < count; x++)
							{
								IProject currentProject = pProjects.get(x);
								if (currentProject != null)
								{
									// need to get the IProject, not the WSProject in order to use the element locator
									String name = currentProject.getName();
									IProject pProject = pApp.getProjectByName(name);
									if (pProject != null)
									{
										searchForDiagrams2(searchString, pProject, pResults);
									}
								}
							}
						}
					}
				  }
				}
			}
		}
	}

	public void searchForDiagrams2(String searchString, IProject pProject, IAssociateResults pResults)
	{
		if ((pProject != null) && (pResults != null))
		{
			if (searchString != null && searchString.length() > 0)
			{
				// get the diagrams from the results object - this will be the array that gets added to
				ETList<IProxyDiagram> pDiags = pResults.getDiagrams();
				if (pDiags != null)
				{
					IProxyDiagramManager pDiagManager = ProxyDiagramManager.instance();
					if (pDiagManager != null)
					{
						// add all diagrams in this project to our array, it will be filtered out
						// later
						ETList<IProxyDiagram> pTempDiagrams = pDiagManager.getDiagramsInDirectory(pProject);
						if (pTempDiagrams != null)
						{
							int dcount = pTempDiagrams.size();
							for (int x = 0; x < dcount; x++)
							{
								IProxyDiagram pDiagram = pTempDiagrams.get(x);
								if (pDiagram != null)
								{
									pDiags.add(pDiagram);
								}
							}
						}
					}
				}
			}
		}
	}

	public boolean warnUserAboutTime()
	{
		boolean bContinue = true;
		if (!m_CaseSensitive)
		{
			IPreferenceQuestionDialog pDialog = new SwingPreferenceQuestionDialog(m_Dialog);
			if (pDialog != null)
			{
				String title = AssociateUtilities.translateString("IDS_PROJNAME2");
				String msg = AssociateUtilities.translateString("IDS_LONGTIME");
				int result = pDialog.displayFromStrings("Default",
                                                    "FindDialog",
                                                    "UML_ShowMe_Allow_Lengthy_Searches",
                                                    "PSK_ALWAYS",
                                                    "PSK_NEVER",
                                                    "PSK_ASK",
                                                    msg,
                                                    SimpleQuestionDialogResultKind.SQDRK_RESULT_NO,
                                                    title,
                                                    SimpleQuestionDialogKind.SQDK_YESNO,
                                                    MessageIconKindEnum.EDIK_ICONQUESTION,
                                                    null);
				if (result != SimpleQuestionDialogResultKind.SQDRK_RESULT_YES)
				{
					bContinue = false;
				}
			}
		}
		return bContinue;
	}
	public String getValueToUse(IElement pElement)
	{
		String value = "";
		if (pElement != null)
		{
			boolean flag = true;
			if (m_IsReplace)
			{
				if (pElement instanceof IProject)
				{
					flag = false;
				}
			}
			if (flag)
			{
				if (m_ResultType == 0)//FIND_TYPE_ELEMENT)
				{
					if (pElement instanceof INamedElement)
					{
						INamedElement pNamedElement = (INamedElement)pElement;
						if (pNamedElement != null)
						{
							value = pNamedElement.getName();
						}
					}
				}
				else if (m_SearchAlias)
				{
					if (pElement instanceof INamedElement)
					{
						INamedElement pNamedElement = (INamedElement)pElement;
						if (pNamedElement != null)
						{
							value = pNamedElement.getAlias();
						}
					}
				}
				else
				{
					value = pElement.getDocumentation();
					if (value == null)
					{
						value = "";
					}
				}
			}
		}
		return value;
	}

	public boolean compareValues(String toMatch, String value, int wildcardLoc)
	{
		boolean bSame = false;
		if (toMatch != null && value != null)
		{
			// are we case sensitive or not
			String toMatch2 = toMatch;
			String value2 = value;
			if (!m_CaseSensitive)
			{
				value2 = value2.toLowerCase();
				toMatch2 = toMatch2.toLowerCase();
			}
			// determine if the current element matches the passed in string
			boolean bValid = process2(toMatch2, value2, wildcardLoc);
			if (bValid)
			{
				bSame = true;
			}
		}
		return bSame;
	}

	public String replaceValue(String value)
	{
		String newValue = "";
		if (value != null)
		{
			newValue = value;
			if (!m_CaseSensitive)
			{
				// Compile with case-insensitivity
				 Pattern pattern = Pattern.compile(m_SearchString, Pattern.CASE_INSENSITIVE);
				 Matcher matcher = pattern.matcher(value);
				 newValue = matcher.replaceAll(m_ReplaceString);
			}
			else
			{
				// Compile with case-sensitivity
				 Pattern pattern = Pattern.compile(m_SearchString);
				 Matcher matcher = pattern.matcher(value);
				 newValue = matcher.replaceAll(m_ReplaceString);
			}
		}
		return newValue;
	}
	public static IADProduct getProduct()
	   {
	      IADProduct retVal = null;
	      
	      ICoreProductManager productManager = CoreProductManager.instance();
	      ETList<IProductDescriptor> pDesc = productManager.getProducts();
	      //ETList pDesc = productManager.getProducts();
	      
	      // Make sure that another project has not already created a product
	      if((pDesc == null) || (pDesc.size() == 0))
	      {
	         // Create a new ADProduct
	         retVal = new ADProduct();         
	         productManager.setCoreProduct(retVal);         
	      }
	      else
	      {
	         IProductDescriptor descriptor = (IProductDescriptor)pDesc.get(0);
	         if(descriptor.getCoreProduct() instanceof IADProduct)
	         {
	            retVal = (IADProduct)descriptor.getCoreProduct();
	         }
	      }
	      
	      return retVal;
	   }

	public void showFindDialog() {
		// TODO Auto-generated method stub
		
	}

	public void showReplaceDialog() {
		// TODO Auto-generated method stub
		
	}
}




