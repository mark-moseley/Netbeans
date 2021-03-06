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

package org.netbeans.modules.uml.ui.addins.associateDialog;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTable;
//import org.netbeans.modules.uml.associatewith.*;
import org.netbeans.modules.uml.ui.addins.associateDialog.*;

import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.UserSettings;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductProjectManager;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;

import javax.swing.text.Position;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.preferenceframework.IPreferenceAccessor;
import org.netbeans.modules.uml.core.preferenceframework.PreferenceAccessor;

import org.netbeans.modules.uml.ui.addins.associateDialog.AssociateController;
import org.netbeans.modules.uml.ui.addins.associateDialog.AssociateTableModel;

//import org.netbeans.modules.uml.project.UMLProjectHelper;
//import org.netbeans.api.project.ui.OpenProjects;

public class AssociateUtilities {
    public static boolean m_bWaiting = false;
    
    public AssociateUtilities() {
        super();
    }
    
    /**
     * Basic setup of the grid
     * @param grid[in]		The grid to hide/show the columns for
     */
    public static void initializeGrid(JTable grid) {
        // Don't think we need to do this anymore because it will
        // be handled by the FindTableModel
    }
    /**
     * Determines from preferences what columns to hide/show for find results
     * @param tableData[in]		The grid to hide/show the columns for
     */
    public static ETList<String> buildColumns() {
        
        ETList<String> strs = new ETArrayList<String>();
        
        strs.add("IDS_ICON");
        strs.add("IDS_NAME");
        strs.add("IDS_ALIAS");
        strs.add("IDS_TYPE");
        strs.add("IDS_FULLNAME");
        strs.add("IDS_PROJECT");
        
        
        return strs;
    }
    
    
    /**
     * Load the combo boxes with the last few selections
     *
     * @param[in] str       The string to search for in the ini file which determines which string to
     *                      load in the combo box
     * @param[in] comboBox  The combo box to load
     *
     * @return
     */
    public static void populateComboBoxes(String sStr, JComboBox box) {
        UserSettings userSettings = new UserSettings();
        if (userSettings != null) {
            box.removeAllItems();
            String value = userSettings.getSettingValue("FindDialog", sStr);
            if (value != null && value.length() > 0) {
                ETList<String> tokens = StringUtilities.splitOnDelimiter(value, "|");
                if (tokens != null) {
                    DefaultComboBoxModel listModel = new DefaultComboBoxModel();
                    
                    int cnt = tokens.size();
                    for (int x = cnt-1; x >= 0; x--) {
                        String str = tokens.get(x);
                        if (str != null && str.length() > 0) {
                            listModel.addElement(str);
                        }
                    }
                    box.setModel(listModel);
                }
            }
            box.setSelectedIndex(-1);
        }
    }
    
    /**
     * Save the strings entered in the combo boxes for future use
     *
     * @param[in] str       The string to save to in the ini file
     * @param[in] comboBox  The combo box to query for its string value
     *
     * @return
     *
     */
    public static void saveSearchString(String sStr, JComboBox box) {
        // get what is selected in the combo box
        String str = (String)(box.getSelectedItem());
        if (str != null && str.length() > 0) {
            // get the string that is stored in the ini file
            // this will be a string delimited by "|" which represents the last 10 choices
            // made in the find dialog
            UserSettings userSettings = new UserSettings();
            if (userSettings != null) {
                String remvalue = userSettings.getSettingValue("FindDialog", sStr);
                if (remvalue != null && remvalue.length() > 0) {
                    // break the string up based on "|"
                    ETList<String> tokens = StringUtilities.splitOnDelimiter(remvalue, "|");
                    if (tokens != null) {
                        int cnt = tokens.size();
                        // if there are more then 10 items in the list, append the current item onto the list, only if it
                        // isn't already there
                        if (cnt > 10) {
                            if (!inList(str, tokens)) {
                                String newStr = "";
                                for (int x = 1; x < cnt; x++) {
                                    String str2 = tokens.get(x);
                                    if (str2 != null && str2.length() > 0) {
                                        newStr += str2;
                                        newStr += "|";
                                    }
                                }
                                newStr += str;
                                // write it back out to the ini file
                                userSettings.setSettingValue("FindDialog", sStr, newStr);
                            }
                        } else {
                            // there haven't been 10 items entered, so just create the string list
                            // and write it to the ini file, but only if it isn't already there
                            if (!inList(str, tokens)) {
                                remvalue += "|";
                                remvalue += str;
                                userSettings.setSettingValue("FindDialog", sStr, remvalue);
                            }
                        }
                    }
                } else {
                    // write it out to the ini file
                    userSettings.setSettingValue("FindDialog", sStr, str);
                }
            }
        }
    }
    
    /**
     *	Is the passed in string part of the passed in list
     *
     *
     * @param xstr[in]		String
     * @param tokens[in]		List
     *
     * @return boolean
     *
     */
    public static boolean inList(String sStr, ETList<String> tokens) {
        boolean inList = false;
        for (int x = 0; x < tokens.size(); x++) {
            String temp = tokens.get(x);
            if (sStr.equals(temp)) {
                inList = true;
                break;
            }
        }
        return inList;
    }
    
    /**
     * Get all of the projects found in the current workspace and populate the list box with their names
     *
     * @return
     *
     */
        /*public static void populateProjectList(JList box)
        {
                if (box != null)
                {
                        try {
                                // get the workspace from the core product
                                IWorkspace pWorkspace = ProductHelper.getWorkspace();
                                if (pWorkspace != null)
                                {
                                        // get all of the projects in the workspace
                                        ETList<IWSProject> pProjects = pWorkspace.getWSProjects();
                                        if (pProjects != null)
                                        {
                                                DefaultListModel listModel;
                                                listModel = new DefaultListModel();
         
                                                // loop through
                                                int numOfProjects = pProjects.size();
                                                for (int x = 0; x < numOfProjects; x++)
                                                {
                                                        IWSProject pProject = pProjects.get(x);
                                                        if (pProject != null)
                                                        {
                                                                // get their names
                                                                String name = pProject.getName();
                                                                if (name.length() > 0)
                                                                {
                                                                        listModel.addElement(name);
                                                                }
                                                        }
                                                }
                                                box.setModel(listModel);
                                        }
                                }
                        }
                        catch (Exception e)
                        {}
                }
        }*/
    
     /* During consolidation of associate-with, this method required uml/project to be added as a dependency in project.xml, and this was
      resulting in the circular dependency. */
    //   public static void populateProjectList(JList box)
    //    {
    //	   Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
    //	   DefaultListModel listModel = new DefaultListModel();
    //	   for (int i = 0; i < allProjects.length; i++)
    //	   {
    //		UMLProjectHelper helper = (UMLProjectHelper)allProjects[i].getLookup().lookup(UMLProjectHelper.class);
    //		if(helper!=null)
    //		{
    //			IProject projectElement = helper.getProject();
    //			if (projectElement != null)
    //	        {
    //				String pName= projectElement.getName();
    //				if (pName.length() > 0)
    //	            {
    //					listModel.addElement(pName);
    //				}
    //			}
    //			box.setModel(listModel);
    //		}
    //	   }
    //    }
    
    /**
     * Called when the grid is clicked on.  This will take the user to the selected element in its diagram,
     * or display a dialog with all of its occurrences.
     *
     * @return
     *
     */
    public static boolean onDblClickFindResults(int row, AssociateTableModel model, AssociateController controller) {
        boolean bSuccess = false;
        IElement pElement = model.getElementAtRow(row);
        if (pElement != null) {
            bSuccess = controller.navigateToElement(pElement);
        } else {
            IProxyDiagram pDiagram = model.getDiagramAtRow(row);
            if (pDiagram != null) {
                bSuccess = controller.navigateToDiagram(pDiagram);
            }
        }
        return bSuccess;
    }
    
    public static void loadController() {
        // Don't think this is needed in java
    }
    
    public static ETList< Object > loadResultsIntoArray(IAssociateResults pResults) {
        ETList< Object > results = new ETArrayList< Object >();
        if ( pResults != null ) {
            ETList<IElement> pElements = pResults.getElements();
            if (pElements != null) {
                int count = pElements.size();
                for (int x = 0; x < count; x++) {
                    IElement pElement = pElements.get(x);
                    if (pElement != null) {
                        results.add(pElement);
                    }
                }
            }
            // get the diagrams from the results object
            ETList<IProxyDiagram> pDiagrams = pResults.getDiagrams();
            if (pDiagrams != null) {
                // loop through the diagrams
                int count = pDiagrams.size();
                for (int x = 0; x < count; x++) {
                    IProxyDiagram pDiagram = pDiagrams.get(x);
                    if (pDiagram != null) {
                        results.add(pDiagram);
                    }
                }
            }
        }
        return results;
    }
    /**
     * Loads the grid with the elements found from the search
     *
     * @param[in] results   The results object that houses the information that meets the find criteria
     *
     * @return HRESULT
     *
     */
    public static void loadResults(JTable grid, IAssociateResults pResults) {
        // not needed, handled in FindTableModel
    }
    
    /**
     * Figure out what project to select in list.  We will select the current project.  If there
     * is not a current project, we will select the first one in the list
     *
     * @return
     *
     */
    public static void selectProjectInList(JList box) {
        if (box != null) {
            IProductProjectManager pManager = ProductHelper.getProductProjectManager();
            
            if (pManager != null) {
                IProject pProject = pManager.getCurrentProject();
                if (pProject != null) {
                    String projName = pProject.getName();
                    int found = box.getNextMatch(projName, 0, Position.Bias.Forward);
                    if (found != -1) {
                        box.setSelectedIndex( found );
                    }
                } else {
                    if (box.getModel().getSize() == 0){
                        box.setSelectedIndex(-1);
                    } else{
                        box.setSelectedIndex( 0 );
                    }
                }
            }
            
            //			Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
            //	        for (int i = 0; i < allProjects.length; i++)
            //	        {
            //	            Lookup lookup = allProjects[i].getLookup();
            //	            IProject projectElement =((UMLProjectHelper)lookup
            //	                    .lookup(UMLProjectHelper.class)).getProject();
            //
            //				if (projectElement != null)
            //				{
            //					String projName = projectElement.getName();
            //					int found = box.getNextMatch(projName, 0, Position.Bias.Forward);
            //					if (found != -1)
            //					{
            //						box.setSelectedIndex( found );
            //					}
            //				}
            //				else
            //				{
            //					if (box.getModel().getSize() == 0){
            //						box.setSelectedIndex(-1);
            //					}
            //					else{
            //						box.setSelectedIndex( 0 );
            //					}
            //				}
            //			}
        }
    }
    
    /**
     * Set the icon in the grid
     *
     *
     * @param pMgr[in]				The resource manager who knows about the icons
     * @param grid[in]				The grid that has the results data
     * @param pNamed[in]				The element to get the icon for
     * @param[in] row					The row in which to set the picture based on its data
     * @param col[in]					The col in which to set the picture based on its data
     *
     * @return HRESULT
     *
     */
    public static void setPicture() {
        // Not needed, handled by FindTableModel
    }
    public static void setPictureD() {
        // Not needed, handled by FindTableModel
    }
    public static void loadProjectListOfController(JList pList, AssociateController pController) {
        if ( (pList != null) && (pController != null) ) {
            Object[] selObjs = pList.getSelectedValues();
            if (selObjs.length > 0) {
                int cnt = selObjs.length;
                for (int x = 0; x < cnt; x++) {
                    Object obj = selObjs[x];
                    if (obj != null) {
                        if (obj instanceof String) {
                            String str = (String)obj;
                            pController.addToProjectList(str);
                        }
                    }
                }
            }
        }
    }
    public static String translateString(String inStr) {
        return DefaultAssociateDialogResource.getString(inStr);
    }
    
    public static Font getGridFontFromPreferences() {
        Font pFont = null;
        IPreferenceAccessor pAccessor = PreferenceAccessor.instance();
        if (pAccessor != null) {
            String name = pAccessor.getFontName("DefaultGridFont");
            String size = pAccessor.getFontSize("DefaultGridFont");
            Integer height = new Integer(size);
            int style = Font.PLAIN;
            boolean bBold = pAccessor.getFontBold("DefaultGridFont");
            boolean bItalic = pAccessor.getFontItalic("DefaultGridFont");
            if (bBold){
                style |= Font.BOLD;
            }
            if (bItalic){
                style |= Font.ITALIC;
            }
            pFont = new Font(name, style, height.intValue());
        }
        return pFont;
    }
    public static void startWaitCursor(Component c) {
        if (!m_bWaiting && c != null) {
            c.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            m_bWaiting = true;
        }
    }
    public static void endWaitCursor(Component c) {
        if (m_bWaiting && c != null) {
            m_bWaiting = false;
            c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
}