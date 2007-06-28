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

package org.netbeans.modules.uml.codegen.action;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.netbeans.api.project.Project;
import org.netbeans.modules.uml.codegen.action.ui.GenerateCodePanel;

import org.netbeans.modules.uml.common.ui.SaveNotifier;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IClass;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackage;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IVersionableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IInterface;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.project.ProjectUtil;
import org.netbeans.modules.uml.project.UMLProject;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import org.netbeans.modules.uml.project.ui.customizer.UMLProjectProperties;
import org.netbeans.modules.uml.util.AbstractNBTask;
import org.netbeans.modules.uml.util.DummyCorePreference;

import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public class GenerateCodeAction extends CookieAction
{
    public enum CodeGenType {Class, Interface, Enumeration};
    
    private IProject parentProject = null;
    
    /**
     * Creates a new instance of GenerateCodeAction
     */
    public GenerateCodeAction()
    {
    }
    
    private final static String PROJECT_SOURCES_SUFFIX = 
        NbBundle.getMessage(GenerateCodeAction.class, 
        "TXT_Project_sources_suffix"); // NOI18N
    
    private final static String PROJECT_SRC_FOLDER = 
        NbBundle.getMessage(GenerateCodeAction.class, 
        "TXT_Project_src_folder"); // NOI18N

    private final static int GC_NODE_PROJECT = 1;
    private final static int GC_NODE_NAMESPACES = 2;
    private final static int GC_NODE_CODEGENS = 4;
    
    protected void performAction(Node[] nodes)
    {
        int genCodeNodeType = 0;
        
        final ETArrayList<IElement> elements = new ETArrayList<IElement>();
        ArrayList<IPackage> pkgList = new ArrayList();
        
        // get the parent UML IProject
        IElement element = (IElement)nodes[0].getCookie(IElement.class);
 
        if (element == null)
        {
            parentProject = lookupProject(nodes[0]);
            element = parentProject;
        } 
        
        else
            parentProject = getParentProject(nodes[0]);
        
        if (nodes.length == 1)
        {
            if (element instanceof IProject)
            {
                genCodeNodeType = GC_NODE_PROJECT;
                element.addElement(element);
            }
            
            else if (element instanceof IPackage)
            {
                genCodeNodeType = GC_NODE_NAMESPACES;
                pkgList.add((IPackage)element);
            }
            
            else // code gen capable element
            {
                genCodeNodeType = GC_NODE_CODEGENS;
                elements.add(element);
            }                
        }

        else
        {
            boolean isPackageNodeSelected = false;
            boolean isCodeGenCapableNodeSelected = false;
            
            for (Node curNode : nodes)
            {
                IElement curElement = 
                    (IElement)curNode.getCookie(IElement.class);

                if (curElement != null)
                {
                    if (curElement instanceof IPackage)
                    {
                        isPackageNodeSelected = true;
                        genCodeNodeType |= GC_NODE_NAMESPACES;

                        // need to save these to reteive children and possibly
                        // "recursive" children based on dialog selections
                        pkgList.add((IPackage)curElement);
                    }
                    
                    // we only want to add the non-Package elements
                    else
                    {
                        genCodeNodeType |= GC_NODE_CODEGENS;
                        elements.add(curElement);
                    }
                } // if curElement !null
            } // for
        } // else - more than one node selected

        // display gen code options dialog
        GenerateCodePanel gcPanel = new GenerateCodePanel(
            retrieveExportFolderDefault(nodes[0]), 
            retrieveUMLProject().getUMLProjectProperties());
        
//        DialogDescriptor dd = new DialogDescriptor(
//            gcPanel,NbBundle.getMessage(GenerateCodeAction.class, 
//                "LBL_ExportCodeDialogTitle"), // NOI18N
//            true, // modal flag
//            NotifyDescriptor.OK_CANCEL_OPTION, // button option type
//            NotifyDescriptor.OK_OPTION, // default button
//            DialogDescriptor.DEFAULT_ALIGN, // button alignment
//            new HelpCtx("uml_diagrams_generating_code"), // NOI18N
//            gcPanel); // button action listener
        
        gcPanel.requestFocus();

//        if (DialogDisplayer.getDefault().notify(dd) 
//                != NotifyDescriptor.OK_OPTION)
//        {
//            return; // cancel this action
//        }

        if (!displayDialogDescriptor(gcPanel))
            return;
        
        final String destFolderName = gcPanel.getSelectedFolderName();
        final boolean backupSources = gcPanel.isBackupSources();
	final boolean generateMarkers = gcPanel.isGenerateMarkers();
        
        AntProjectHelper antHlp = 
            ProjectUtil.getAntProjectHelper(retrieveUMLProject());
        
        // save target source folder location in private.properties
        EditableProperties edProps = antHlp.getProperties(
            AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            
        edProps.setProperty(
            UMLProjectProperties.GEN_CODE_SOURCE_FOLDER, destFolderName);
        
        antHlp.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, edProps);
        
        // if UML project is dirty, save it first
        if (parentProject.isDirty())
        {
            UMLProject umlProject = retrieveUMLProject();

            boolean prefVal = NbPreferences.forModule(DummyCorePreference.class)
                .getBoolean("UML_Prompt_to_Save_Project", false); // NOI18N
            
            if (prefVal)
            {
                Object result = SaveNotifier.getDefault().displayNotifier(
                    NbBundle.getMessage(GenerateCodeAction.class,
                        "MSG_DialogTitle_AuthorizeUMLProjectSave"), // NOI18N
                        NbBundle.getMessage(GenerateCodeAction.class, "MSG_UMLProject"), // NOI18N
                    parentProject.getName());

                if (result == NotifyDescriptor.CANCEL_OPTION || 
                    result == NotifyDescriptor.CLOSED_OPTION)
                {
                    // don't save project means abort code gen action
                    return;
                }

                if (result == SaveNotifier.SAVE_ALWAYS_OPTION)
                {
                    NbPreferences.forModule(DummyCorePreference.class)
                        .putBoolean("UML_Prompt_to_Save_Project", false); // NOI18N
                }
            }
            
            // umlProject.saveProject();
            parentProject.save(parentProject.getFileName(), true);
        }
        
        ETList<IElement> selElements = new ETArrayList();
        
        // action invoked from Project node
        if ((genCodeNodeType & GC_NODE_PROJECT) == GC_NODE_PROJECT)
            selElements = retrieveNamespaceElements(parentProject, true);
        
        // action not invoked from Project node
        else
        {
            // action invocation included at least one selected non-Package node
            if ((genCodeNodeType & GC_NODE_CODEGENS) == GC_NODE_CODEGENS)
                selElements.addThese(elements);

            // action invocation included at least one selected Package node
            if ((genCodeNodeType & GC_NODE_NAMESPACES) == GC_NODE_NAMESPACES)
            {
                // iterate through all selected package nodes and get 
                // the code gen capable elements - recursively if requested
                for (IPackage pkg: pkgList)
                {
                    ETList<IElement> selected = 
                        retrieveNamespaceElements((INamespace)pkg, true);

                    if (selected != null && selected.size() > 0)
                        selElements.addThese(selected);
                }
            }
        }        
        
        if (selElements == null || selElements.size() == 0)
        {
            DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Message(
                    NbBundle.getMessage(
                        GenerateCodeAction.class, "MSG_NoElementsFound"), // NOI18N
                    NotifyDescriptor.ERROR_MESSAGE));

            return;
        }
        
        final ETList<IElement> ecElements = selElements;
        
        RequestProcessor processor = 
            new RequestProcessor("uml/ExportCode"); // NOI18N
        
        HashMap settings = new HashMap();
        
        settings.put(AbstractNBTask.SETTING_KEY_TASK_NAME,NbBundle.getMessage(GenerateCodeAction.class, 
            "CTL_ExportCodeActionName")); // NOI18N

        settings.put(AbstractNBTask.SETTING_KEY_TOTAL_ITEMS, selElements.size());
        
        GenerateCodeTask task = new GenerateCodeTask(
            settings, selElements, parentProject.getName(), 
            destFolderName, backupSources, generateMarkers);
        
        processor.post(task);

        // even though the user is prompted to save dirty UML project before
        // code gen, somehow, the UML project gets dirty again during Code Gen, 
        // so we have to save it again
        if (parentProject.isDirty())
            parentProject.save(parentProject.getFileName(), true);

        // createJavaProject(ProjectUtil.getOpenJavaProjects()[0]);
    }

    
    private boolean displayDialogDescriptor(GenerateCodePanel gcPanel)
    {
        GenerateCodeDescriptor red = new GenerateCodeDescriptor(
                gcPanel, // inner pane
                NbBundle.getMessage(GenerateCodeAction.class,
                "LBL_GenerateCodeDialog_Title"), // NOI18N
                true, // modal flag
                NotifyDescriptor.OK_CANCEL_OPTION, // button option type
                NotifyDescriptor.OK_OPTION, // default button
                DialogDescriptor.DEFAULT_ALIGN, // button alignment
                new HelpCtx("uml_diagrams_generating_code"), // NOI18N
                gcPanel); // button action listener
        
        gcPanel.getAccessibleContext().setAccessibleName(NbBundle
                .getMessage(GenerateCodeAction.class, "ACSN_CodeGenDialog")); // NOI18N
        gcPanel.getAccessibleContext().setAccessibleDescription(NbBundle
                .getMessage(GenerateCodeAction.class, "ACSD_CodeGenDialog")); // NOI18N
        
        gcPanel.requestFocus();
        
        return (DialogDisplayer.getDefault().notify(red) ==
                NotifyDescriptor.OK_OPTION);
    }
    
    
    protected Class[] cookieClasses()
    {
        return new Class[] {UMLProject.class, IElement.class};
    }

    
    protected int mode()
    {
        return MODE_ALL;
    }
    
    
    protected boolean enable(Node[] nodes)
    {
        if (nodes == null || nodes.length == 0)
            return false;
        
        IElement element = (IElement)nodes[0].getCookie(IElement.class);
        
        IProject parentProject = null;
        if (element == null)
        {
            parentProject = lookupProject(nodes[0]);
            element = parentProject;
        }
        
        // we may have a UML project node and it doesn't hold IElement 
        // as a cookie so it won't pass the automated IElement mode test
        // so we need to verify that it is indeed a UML project (IProject)
        // and if it is, then it is 'enabled'
        boolean checkSuperEnable = nodes.length > 1 || parentProject == null;
        
        if (checkSuperEnable && !super.enable(nodes))
            return false;
        
        // get the parent UML IProject
        if (parentProject == null)
            parentProject = getParentProject(nodes[0]);
        
        Project assocProject = ProjectUtil.findNetBeansProjectForModel(parentProject);
        if(assocProject instanceof UMLProject)
        {
            UMLProject umlProject = (UMLProject) assocProject;
        
            if (umlProject.getUMLProjectProperties().getProjectMode()
                .equals(UMLProject.PROJECT_MODE_ANALYSIS_STR))
            {
                return false;
            }
        }
        else
        {
            return false;
        }
        
        // single node selection criteria is a little different/easier/faster
        // than multi-node selection criteria
        if (nodes.length == 1)
        {
            // node has to be Project, Package, 
            // or Code Gen type (Class, Interface or Enum
            if (element instanceof IProject ||
                element instanceof IPackage)
            {
                return true;
            }
            
            else if (isCodeGenElement(element))
                return true;
			else 
				return false;
        }
        
        // multiple nodes were selected - all must meet certain conditions
        // for this action to be available
        
        for (Node curNode: nodes)
        {
            IElement curEle = (IElement)curNode.getCookie(IElement.class);
            
            // UML project node can't be part of a multi-node selection
            if (curEle instanceof IProject)
                return false;
            
            // all selected elements must be from same UML project
            if (curEle == null || getParentProject(curEle) != parentProject)
                return false;
				
            // all selected nodes must be of type package or code gen capable
            if ((curEle instanceof IPackage) || isCodeGenElement(curEle))
				continue;
			else 
				return false;
        } // for-each curNode/nodes
        
        return true;
    }
        

    private boolean isCodeGenElement(IElement element)
    {
        try
        {
            CodeGenType.valueOf(element.getElementType());
			
            if (element.toString().equalsIgnoreCase(
                getUnnamedElementPreference()))
            {
                return false;
            }
            
            // if the element is an inner element, then we
            // do not enable this action
            IElement owner = element.getOwner();
            if (owner instanceof IClass ||
                owner instanceof IEnumeration || owner instanceof IInterface)
            {
                return false;
            };
			
            return true;
        }
        
        catch (IllegalArgumentException ex)
        {
            return false;
        }
    }
    
    protected boolean asynchronous()
    {
        return false;
    }
    
    public HelpCtx getHelpCtx()
    {
        return null;
    }
    
    public String getName()
    {
        return NbBundle.getMessage(
            GenerateCodeAction.class, "CTL_ExportCodeActionName"); // NOI18N
    }
    

    // helper methods

    private IProject getParentProject(Node node)
    {
        IElement element = (IElement)node.getCookie(IElement.class);
        if (element == null)
            return null;
        
        return (getParentProject(element));
    }
    
    private IProject getParentProject(IElement element)
    {
        return element.getProject();
    }
    
    private IProject lookupProject(Node node)
    {
        UMLProject umlProject = 
            (UMLProject)node.getLookup().lookup(UMLProject.class);
        
        if (umlProject != null)
        {
            UMLProjectHelper helper = (UMLProjectHelper)umlProject.getLookup()
                .lookup(UMLProjectHelper.class);
            
            return helper.getProject();
        }
        return null;
    }
    
    private UMLProject retrieveUMLProject()
    {
        return (UMLProject)ProjectUtil
            .findNetBeansProjectForModel(parentProject);
    }
    

    public ETList<IElement> retrieveNamespaceElements(
        INamespace nsElement, boolean recursiveSearch)
    {
        String query = null;
        IElementLocator elementLocator = new ElementLocator();

        if (nsElement instanceof IProject)
        {
            query =
                "//*[name() = \'UML:Class\'" + // NOI18N
                " or name() = \'UML:Interface\'" + // NOI18N
                " or name() = \'UML:Enumeration\']"; // NOI18N
                // " and name() != \'UML:Package.elementImport\']"; // NOI18N
            
            return elementLocator.findElementsByDeepQuery(nsElement, query);
        }
        
        else // package scoped
        {
            org.dom4j.Node node = ((IVersionableElement)nsElement).getNode();
            query = node.getUniquePath();
            
            if (recursiveSearch)
            {
                query += 
                    "//*[name() = \'UML:Class\'" + // NOI18N
                    " or name() = \'UML:Interface\'" + // NOI18N
                    " or name() = \'UML:Enumeration\']"; // NOI18N
                    // " and name() != \'UML:Package.elementImport\']"; // NOI18N

                return elementLocator.findElementsByDeepQuery(nsElement, query);
            }
            
            else 
            {
                query +=
                    "/.[name() = \'UML:Class\'" + // NOI18N
                    " or name() = \'UML:Interface\'" + // NOI18N
                    " or name() = \'UML:Enumeration\']"; // NOI18
                    // " and name() != \'UML:Package.elementImport\']"; // NOI18N

                return elementLocator.findElementsByQuery(nsElement, query);
            }            
        }

    }


    private String retrieveExportFolderDefault(Node node)
    {
        // get target source folder location in private.properties,
        // if it has been set

        AntProjectHelper antHlp = 
            ProjectUtil.getAntProjectHelper(retrieveUMLProject());
        
        // save target source folder location in private.properties
        EditableProperties edProps = antHlp.getProperties(
            AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            
        String lastFolderUsed = 
            edProps.getProperty(UMLProjectProperties.GEN_CODE_SOURCE_FOLDER);
        
        if (lastFolderUsed != null && lastFolderUsed.length() > 0)
            return lastFolderUsed;

        // if the export source folder hasn't been saved to file yet,
        // provide a suggestion for a place to put the sources

        String folderName = null;
        IElement element = (IElement)node.getCookie(IElement.class);
        
        // elment node was selected
        if (element != null)
        {
            File path = FileUtil.toFile(ProjectUtil.findElementOwner(element)
                .getProjectDirectory().getParent());
            
            if (path != null)
                folderName = path.getPath();
        }
        
        // project node was selected
        else
        {
            File path = new File(lookupProject(node).getBaseDirectory());
            folderName = path.getParent();
        }

        // defensive code: use user's home dir as the base dir
        if (folderName == null || folderName.length() == 0)
            folderName = System.getProperty("user.home"); // NOI18N

        // append suggested Java project name + "src" dir
        return folderName + File.separatorChar + parentProject.getName() + 
            PROJECT_SOURCES_SUFFIX + File.separatorChar + PROJECT_SRC_FOLDER;
    }

    
//    private void createJavaProject(org.netbeans.api.project.Project oldProject)
//    {
//        DataObject dobj =
//            (DataObject)oldProject.getLookup().lookup(DataObject.class);
//
//        // TemplateWizard.Iterator it = TemplateWizard.getIteratorr(dobj);
//        TemplateWizard templateWiz = new TemplateWizard();
//        
//        try
//        {
//            templateWiz.instantiate();
//            // Set set = it.instantiate(templateWiz);
//        }
//        
//        catch (IOException ex)
//        {
//            ex.printStackTrace();
//        }
//    }
    
    private String getUnnamedElementPreference()
    {
        //Kris Richards - returning the default value.
        return "Unnamed" ;
    }
}
