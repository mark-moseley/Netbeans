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
package org.netbeans.modules.uml.ui.support.diagramsupport;

import java.io.File;
import java.util.Iterator;
import java.util.ArrayList;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.DiagramDetails;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.ModelElementXMIIDPair;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.core.support.umlsupport.FileSysManip;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchive;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveDefinitions;
import org.netbeans.modules.uml.ui.support.archivesupport.IProductArchiveElement;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Trey Spiva
 */
public class ProxyDiagramImpl implements IProxyDiagram, 
    DiagramAreaEnumerations, IProductArchiveDefinitions
{
    private String m_Filename = "";
    private DispatchHelper m_DispatchHelper = new DispatchHelper();
    private DiagramDetails diagramDetails = null;
    
    public DiagramDetails getDiagramDetails()
    {
        IDiagram diagram = getDiagram();
        if (diagram != null)
        {
            diagramDetails = new DiagramDetails();
            diagramDetails.setDiagramName(diagram.getName());
            diagramDetails.setDiagramType(diagram.getDiagramKind());
            diagramDetails.setDiagramTypeName(diagram.getDiagramKindAsString());
            diagramDetails.setDiagramXMIID(diagram.getXMIID());
            diagramDetails.setNamespace(diagram.getNamespace());
            diagramDetails.setToplevelXMIID(diagram.getTopLevelId());
            diagramDetails.setDiagramProjectXMIID(diagram.getTopLevelId());
        }

        // This occurs when diagram has not yet been loaded in memory; 
        // thus, reading the diagram info from its file.
        else 
        {   
            FileObject fo = FileUtil.toFileObject(new File(m_Filename));
            if (fo != null)
            {
                if (diagramDetails == null ||
                    !fo.lastModified().equals(diagramDetails.getDateModified()))
                {
                    diagramDetails = DiagramParserFactory
                        .createDiagramParser(m_Filename).getDiagramInfo();
                } 
            }
        }
        
        return diagramDetails;
    }

    /**
     * Returns the name of the diagram
     *
     * @return tThen name of the diagram.
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#getName()
     */
    public String getName()
    {
        String retVal = "";

        IDiagram curDiagram = getDiagram();
        if (curDiagram != null)
            retVal = curDiagram.getName();

        else
        {
            DiagramDetails details = getDiagramDetails();

            if (details != null)
                retVal = details.getDiagramName();
        }

        return retVal;
    }

    /**
     * Sets the name of the diagram
     *
     * @param value The new name
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#setName(java.lang.String)
     */
    public void setName(String value)
    {
        IProxyDiagramManager manager = ProxyDiagramManager.instance();
        String validDiagramName = value; 
        
        IDrawingAreaEventDispatcher dispatcher = m_DispatchHelper.getDrawingAreaDispatcher(); 
        IEventPayload postPayload = dispatcher.createPayload("FireDrawingAreaPostPropertyChange");
         
        boolean proceed = false;
        IDiagram diagram = getDiagram();
        if (diagram != null)
        {
            // must setName before firing fireDrawingAreaPostPropertyChange
            diagram.setName(validDiagramName);   
            dispatcher.fireDrawingAreaPostPropertyChange(this, DAPK_NAME, postPayload);
        }
        else  // diagram is not open
        {      
            // TODO: rewrite for meteora - this is the case when a diagram has
            // not yet opened.
            
            // Since we are a proxy to the real diagram we have to send the event
            // ourself.  The process will be to first send a pre event to allow
            // listeners to reject the change.  If the name change is excepted
            // then we make the change and sent the post event.
            // If we can not obtain a product archive do not even bother sending the
            // pre event because we will not be able to store the value.
            IProductArchive productArchive = getArchive();
            if (productArchive != null)
            {
                try
                {
                    //proceed = dispatcher.fireDrawingAreaPrePropertyChange(this, DAPK_NAME, prePayload);

                    if (proceed == true)
                    {
                        // The diagram is closed, go into the xml file and set it
                        IProductArchiveElement element = productArchive.getElement(DIAGRAMINFO_STRING);
                        element.addAttributeString(DIAGRAMNAME_STRING, validDiagramName);
                        saveArchive(productArchive);
                        dispatcher.fireDrawingAreaPostPropertyChange(this, DAPK_NAME, postPayload);
                    }
                }
                catch (NullPointerException e)
                {
                    // Ignore I just want to stop processing.
                }
            }
        }
    }

    // TODO: meteora
    /**
     * Returns the alias of the diagram
     *
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#getAlias()
     */
    public String getAlias()
    {
        String retVal = "";

        IDiagram curDiagram = getDiagram();
        if (curDiagram != null)
            retVal = curDiagram.getAlias();

        else
        {
            DiagramDetails details = getDiagramDetails();
            if (details != null)
            {
                if (retVal == null || retVal.length() == 0)
                    // Empty alias, so return the name of the diagram
                    retVal = details.getDiagramName();
            }
        }

        return retVal;
    }

    /**
     * Sets the alias of the diagram
     *
     * @param sAlias [in] The new alias
     */
    public void setAlias(String value)
    {
        String filename = getFilename();
        if (filename != null && filename.length() > 0 && value != null)
        {
            IDiagram curDiagram = getDiagram();
            if (curDiagram != null)
            {
                curDiagram.setAlias(value);
            }
            else
            {
                try
                {
                    IDrawingAreaEventDispatcher dispatcher = m_DispatchHelper.getDrawingAreaDispatcher();
                    IEventPayload payload = dispatcher.createPayload("DrawingAreaPrePropertyChanged");

                    if (dispatcher.fireDrawingAreaPrePropertyChange(this, DAPK_ALIAS, payload) == true)
                    {
                        IProductArchive archive = getArchive();
                        if (archive != null)
                        {
                            IProductArchiveElement aElement = archive.getElement(DIAGRAMINFO_STRING);
                            if (aElement != null)
                            {
                                aElement.addAttributeString(DIAGRAMALIAS_STRING, value);
                                saveArchive(archive);
                                IEventPayload postPayload = dispatcher.createPayload("DrawingAreaPostPropertyChanged");
                                dispatcher.fireDrawingAreaPostPropertyChange(this, DAPK_ALIAS, postPayload);
                            }
                        }
                    }
                }
                catch (NullPointerException e)
                {
                    // Do nothing.  I just want to exit.
                }
            }
        }
    }

    /**
     * Gets the name or alias of this element.
     *
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#getNameWithAlias()
     */
    public String getNameWithAlias()
    {
        String retVal = "DrawingAreaPrePropertyChanged";

        if (ProductHelper.getShowAliasedNames() == true)
        {
            retVal = getAlias();
            if (retVal.length() <= 0)
            {
                retVal = getName();
            }
        }
        else
        {
            retVal = getName();
        }

        return retVal;
    }

    /**
     * Sets the name or alias of this element.
     */
    public void setNameWithAlias(String value)
    {
        if (ProductHelper.getShowAliasedNames() == true)
        {
            setAlias(value);
        }
        else
        {
            setName(value);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#getQualifiedName()
     */
    public String getQualifiedName()
    {
        String retVal = "";

        IDiagram curDiagram = getDiagram();
        if (curDiagram != null)
        {
            retVal = curDiagram.getQualifiedName();
        }
        else
        {
            // Get it from the closed file.
            ProxyDiagramManager manager = ProxyDiagramManager.instance();
            boolean bIncludeProjectName = ProductHelper.useProjectInQualifiedName();

            DiagramDetails details = manager.getDiagramDetails(m_Filename);

            try
            {
                INamespace space = manager.getDiagramNamespace(m_Filename);
                String diagramName = details.getDiagramName();
                if (diagramName.length() > 0)
                {
                    if (space instanceof IProject && !bIncludeProjectName)
                    {
                        retVal = diagramName;
                    }
                    else
                    {
                        if (space != null)
                        {
                            retVal = space.getQualifiedName();
                            retVal += "DrawingAreaPostPropertyChanged";
                        }
                        retVal += diagramName;
                    }
                }
            }
            catch (NullPointerException e)
            {
                // Just Bail and return an empty string.
                retVal = "";
            }
        }

        return retVal;
    }

    /**
     * Retrieve the documentation.
     *
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#getDocumentation()
     */
    public String getDocumentation()
    {
        String retVal = "";

        IDiagram curDiagram = getDiagram();
        if (curDiagram != null)
        {
            retVal = curDiagram.getDocumentation();
        }
        else
        {
            IProductArchive productArchive = getArchive();
            if (productArchive !=  null )
            {
                IProductArchiveElement element = productArchive.getElement(DIAGRAMINFO_STRING);
                if (element != null)
                {
                    retVal = element.getAttributeString(DIAGRAMNAME_DOCS);
                }
            }
        }

        return retVal;
    }

    /**
     * Put/Get the documentation
     */
    public void setDocumentation(String value)
    {
        String filename = getFilename();
        if (filename != null && filename.length() > 0 && value != null)
        {
            IDiagram curDiagram = getDiagram();
            if (curDiagram != null)
            {
                curDiagram.setDocumentation(value);
            }
            else
            {
                try
                {
                    IDrawingAreaEventDispatcher dispatcher = m_DispatchHelper.getDrawingAreaDispatcher();
                    IEventPayload payload = dispatcher.createPayload("DrawingAreaPrePropertyChanged");

                    if (dispatcher.fireDrawingAreaPrePropertyChange(this, DAPK_DOCUMENTATION, payload) == true)
                    {
                        IProductArchive archive = getArchive();
                        if (archive != null)
                        {
                            IProductArchiveElement aElement = archive.getElement(DIAGRAMINFO_STRING);
                            if (aElement != null)
                            {
                                aElement.addAttributeString(DIAGRAMNAME_DOCS, value);
                                saveArchive(archive);
                                IEventPayload postPayload = dispatcher.createPayload("DrawingAreaPostPropertyChanged");
                                dispatcher.fireDrawingAreaPostPropertyChange(this, DAPK_DOCUMENTATION, postPayload);
                            }
                        }
                    }
                }
                catch (NullPointerException e)
                {
                    // Do nothing.  I just want to exit.
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#getNamespace()
     */
    public INamespace getNamespace()
    {
        INamespace retVal = null;

        IDiagram curDiagram = getDiagram();
        if (curDiagram != null)
        {
            retVal = curDiagram.getNamespace();
        }
        else
        {
            ProxyDiagramManager manager = ProxyDiagramManager.instance();
            retVal = manager.getDiagramNamespace(m_Filename);
        }

        return retVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#setNamespace(org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace)
     */
    public void setNamespace(INamespace namespace)
    {
        String filename = getFilename();
        if ((namespace != null) && (filename.length() > 0))
        {
            IDiagram diagram = getDiagram();
            if (diagram != null)
            {
                diagram.setNamespace(namespace);
            }
            else
            {
                try
                {
                    IDrawingAreaEventDispatcher dispatcher = m_DispatchHelper.getDrawingAreaDispatcher();
                    IEventPayload payload = dispatcher.createPayload("DrawingAreaPrePropertyChanged");

                    if (dispatcher.fireDrawingAreaPrePropertyChange(this, DAPK_NAMESPACE, payload) == true)
                    {
                        IProductArchive archive = getArchive();
                        if ( archive != null )
                        {
                            IProductArchiveElement aElement = archive.getElement(DIAGRAMINFO_STRING);
                            aElement.addAttributeString(NAMESPACE_MEID, namespace.getXMIID());
                            aElement.addAttributeString(NAMESPACE_TOPLEVELID, namespace.getTopLevelId());
                            saveArchive(archive);

                            IEventPayload postPayload = dispatcher.createPayload("DrawingAreaPostPropertyChanged");
                            dispatcher.fireDrawingAreaPostPropertyChange(this, DAPK_NAMESPACE, postPayload);
                        }
                    }
                }
                catch (NullPointerException e)
                {
                    // Do nothing.  I just want to exit.
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#getNamespaceXMIID()
     */
    public String getNamespaceXMIID()
    {
        String retVal = "";

        DiagramDetails details = getDiagramDetails();
        if (details != null)
            retVal = details.getDiagramNamespaceXMIID();

        return retVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#getXMIID()
     */
    public String getXMIID()
    {
        String retVal = "";

        DiagramDetails details = getDiagramDetails();
        if (details != null)
            retVal = details.getDiagramXMIID();

        return retVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#getFilename()
     */
    public String getFilename()
    {
        return m_Filename;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#setFilename(java.lang.String)
     */
    public void setFilename(String value)
    {
        m_Filename = value;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#getDiagramKind()
     */
    public int getDiagramKind()
    {
        int retVal = IDiagramKind.DK_UNKNOWN;

        IDiagram curDiagram = getDiagram();
        if (curDiagram != null)
            retVal = curDiagram.getDiagramKind();

        else
        {
            DiagramDetails details = getDiagramDetails();
            if (details != null)
                retVal = details.getDiagramType();
        }

        return retVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#getDiagramKindName()
     */
    public String getDiagramKindName()
    {
//        IDiagramTypesManager manager = DiagramTypesManager.instance();
//        return manager.getDiagramTypeName(getDiagramKind());
        String retVal = null;

        IDiagram curDiagram = getDiagram();
        if (curDiagram != null)
            retVal = curDiagram.getDiagramKindAsString();

        else
        {
            DiagramDetails details = getDiagramDetails();
            if (details != null)
                retVal = details.getDiagramTypeName();
        }

        return retVal;
    }

    /**
     * Returns the project this diagram is a part of.  Only returns a project
     * if the project is open.
     *
     * @return The project.
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#getProject()
     */
    public IProject getProject()
    {
        IProject retVal = null;

        INamespace space = getNamespace();

        try
        {
            String topLevelID = space.getTopLevelId();
            if (topLevelID.length() > 0)
            {
                IProduct product = ProductHelper.getProduct();
                IApplication app = product.getApplication();
                retVal = app.getProjectByID(topLevelID);
            }
        }
        catch (NullPointerException e)
        {
            // Do nothing.
        }

        return retVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#getDiagram()
     */
    public IDiagram getDiagram()
    {
        IDiagram retVal = null;

        IProductDiagramManager manager = ProductHelper.getProductDiagramManager();
        if (manager != null)
        {
            retVal = manager.getOpenDiagram(m_Filename);
        }

        return retVal;
    }

    /**
     * Returns true if the diagram is open.
     *
     * @return true if this proxy diagram is open.
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#isOpen()
     */
    public boolean isOpen()
    {
        boolean retVal = false;

        if (getDiagram() != null)
        {
            retVal = true;
        }

        return retVal;
    }

    /**
     * Returns true if bDiagramFilename represents a valid filename.
     * It looks for both .etlp and .etld files.
     *
     * @return true if the diagram is valid.
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#isValidDiagram()
     */
    public boolean isValidDiagram()
    {
        boolean retVal = false;

        if ((m_Filename != null) && (m_Filename.length() > 0))
        {
            String fileWOExtension = StringUtilities.getFileName(m_Filename);

            File etlFile = new File(StringUtilities.ensureExtension(m_Filename, FileExtensions.DIAGRAM_LAYOUT_EXT));
            
            if (etlFile.exists() == true)
            {
                retVal = true;
            }
        }

        return retVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#isSame(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram)
     */
    public boolean isSame(IProxyDiagram pProxy)
    {
        boolean retVal = false;
        if (pProxy != null)
        {
            if ((m_Filename != null) && (m_Filename.length() > 0))
            {
                String rhsFilename = pProxy.getFilename();
                retVal = m_Filename.equalsIgnoreCase(rhsFilename);
            }
        }
        return retVal;
    }

    /**
     * Is the diagram readonly
     *
     * @return true if the diagram is readonly
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#getReadOnly()
     */
    public boolean getReadOnly()
    {
        boolean retVal = false;

        IDiagram diagram = getDiagram();
        if (diagram != null)
        {
            retVal = diagram.getReadOnly();
        }
        else if ((m_Filename != null) && (m_Filename.length() > 0))
        {
            String etlStr = FileSysManip.ensureExtension(m_Filename, FileExtensions.DIAGRAM_LAYOUT_EXT);
            File etlFile = new File(etlStr);
            if (etlFile.canWrite() == false)
            {
                retVal = true;
            }
        }

        return retVal;
    }

    /**
     * Adds an associated diagram to our list
     *
     * @param sDiagramXMIID The xmiid of the diagram to associate to.
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#addAssociatedDiagram(java.lang.String)
     */
    public void addAssociatedDiagram(String sDiagramXMIID)
    {
        if (sDiagramXMIID != null)
        {
            IDiagram diagram = getDiagram();
            if (diagram != null)
            {
                diagram.addAssociatedDiagram(sDiagramXMIID);
            }
            else
            {
                IProductArchive productArchive = getArchive();
                if (productArchive != null)
                {
                    productArchive.insertIntoTable(ASSOCIATED_DIAGRAMS_STRING, sDiagramXMIID);
                    saveArchive(productArchive);
                }
            }
        }
    }

    /**
     * Adds an associated diagram to our list
     *
     * @param pDiagram [in] The diagram we should associate to
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#addAssociatedDiagram2(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram)
     */
    public void addAssociatedDiagram(IProxyDiagram pDiagram)
    {
        if (pDiagram != null)
        {
            addAssociatedDiagram(pDiagram.getXMIID());
        }
    }

    /**
     * Adds an association between diagram 1 and 2 and 2 and 1
     *
     * @param pDiagram1 [in] The first diagram that's part of the association.
     * @param pDiagram2 [in] The second diagram that's part of the association.
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#addDualAssociatedDiagrams(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram)
     */
    public void addDualAssociatedDiagrams(IProxyDiagram pDiagram1, IProxyDiagram pDiagram2)
    {
        if ((pDiagram1.getReadOnly() == false) && (pDiagram2.getReadOnly() == false))
        {
            pDiagram1.addAssociatedDiagram(pDiagram2);
            pDiagram2.addAssociatedDiagram(pDiagram1);
        }
    }

    /**
     * Removes an associated diagram from our list
     *
     * @param sDiagramXMIID The xmiid of the diagram to remove.
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#removeAssociatedDiagram(java.lang.String)
     */
    public void removeAssociatedDiagram(String sDiagramXMIID)
    {
        if (sDiagramXMIID != null)
        {
            IDiagram diagram = getDiagram();
            if (diagram != null)
            {
                diagram.removeAssociatedDiagram(sDiagramXMIID);
            }
            else
            {
                IProductArchive productArchive = getArchive();
                if (productArchive != null)
                {
                    productArchive.removeFromTable(ASSOCIATED_DIAGRAMS_STRING, sDiagramXMIID);
                    saveArchive(productArchive);
                }
            }
        }
    }

    /**
     * Removes an associated diagram from our list
     *
     * @param pDiagram The diagram to remove.
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#removeAssociatedDiagram2(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram)
     */
    public void removeAssociatedDiagram(IProxyDiagram pDiagram)
    {
        if (pDiagram != null)
        {
            removeAssociatedDiagram(pDiagram.getXMIID());
        }
    }

    /**
     * Removes an association between diagram 1 and 2 and 2 and 1
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#removeDualAssociatedDiagrams(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram, org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram)
     */
    public void removeDualAssociatedDiagrams(IProxyDiagram pDiagram1, IProxyDiagram pDiagram2)
    {
        if ((pDiagram1.getReadOnly() == false) && (pDiagram2.getReadOnly() == false))
        {
            pDiagram1.removeAssociatedDiagram(pDiagram2);
            pDiagram2.removeAssociatedDiagram(pDiagram1);
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#getAssociatedDiagrams()
     */
    public ETList<IProxyDiagram> getAssociatedDiagrams()
    {
        //ETList<IProxyDiagram> retVal = new ETArrayList<IProxyDiagram>();
        ETList<IProxyDiagram> retVal = null;
        // TODO: meteora
        
//        IDiagram diagram = getDiagram();
//        ETList<IProxyDiagram> diagrams = null;
//        if (diagram != null)
//        {
//            diagrams = diagram.getAssociatedDiagrams();
//            int max = diagrams.size();
//
//            // HAVE TODO: Really have to stop using Binding IDiagram.
////         for(int index = 0; index < max; index++)
////         {
////            foundDiagrams.add(diagrams.item(index));
////         }
//        }
//        else
//        {
//            diagrams = new ETArrayList<IProxyDiagram>();
//            ProxyDiagramManager manager = ProxyDiagramManager.instance();
//            IProductArchive archive = getArchive();
//
//            if (archive != null)
//            {
//                int nKey = 1;
//                IProductArchiveElement foundElement = archive.getTableEntry(ASSOCIATED_DIAGRAMS_STRING, nKey);
//                while (foundElement != null)
//                {
//                    if (foundElement.isDeleted() == false)
//                    {
//                        String xmiID = foundElement.getID();
//                        if (xmiID.length() > 0)
//                        {
//                            IProxyDiagram foundDiagram = manager.getDiagram2(xmiID);
//                            if (foundDiagram != null)
//                            {
//                                retVal.add(foundDiagram);
//                            }
//                        }
//                    }
//
//                    nKey++;
//                    foundElement = null;
//                    foundElement = archive.getTableEntry(ASSOCIATED_DIAGRAMS_STRING, nKey);
//                }
//            }
//        }

        return retVal;
    }

    /**
     * Is this an associated diagram?
     *
     * @param sDiagramXMIID The diagram xmiid
     * @return TRUE if the diagram is associated with this diagram.
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#isAssociatedDiagram(java.lang.String)
     */
    public boolean isAssociatedDiagram(String sDiagramXMIID)
    {
        boolean retVal = false;
        // TODO: meteora
        
//        if ((sDiagramXMIID != null) && (sDiagramXMIID.length() > 0))
//        {
//            IDiagram diagram = getDiagram();
//            if (diagram != null)
//            {
//                retVal = diagram.isAssociatedDiagram(sDiagramXMIID);
//            }
//            else
//            {
//
//                DiagramDetails details = CachedDiagrams.instance().getInfo(m_Filename);
//
//                ArrayList<String> associatedDiagrams = details.getAssociatedDiagrams();
//                for (Iterator<String> iter = associatedDiagrams.iterator(); (iter.hasNext() == true) && (retVal == false);)
//                {
//                    if (sDiagramXMIID.equals(iter.next()) == true)
//                    {
//                        retVal = true;
//                    }
//                }
//            }
//        }

        return retVal;
    }

    /**
     * Is this an associated diagram?
     *
     * @param pDiagram The diagram
     * @return true if the diagram is associated with this diagram.
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#isAssociatedDiagram(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram)
     */
    public boolean isAssociatedDiagram(IProxyDiagram pDiagram)
    {
        boolean retVal = false;
        // TODO: meteora
        
//        if (pDiagram != null)
//        {
//            retVal = isAssociatedDiagram(pDiagram.getXMIID());
//        }

        return retVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#addAssociatedElement(java.lang.String, java.lang.String)
     */
    public void addAssociatedElement(String sTopLevelElementXMIID, String sModelElementXMIID)
    {
        // Get the IDiagram, and route the call to the open diagram
        IDiagram diagram = getDiagram();
        if (diagram != null)
        {
            diagram.addAssociatedElement(sTopLevelElementXMIID, sModelElementXMIID);
        }
        else
        {
            IProductArchive productArchive = getArchive();
            if (productArchive != null)
            {
                ETPairT<IProductArchiveElement, Integer> val = productArchive.insertIntoTable(ASSOCIATED_ELEMENTS_STRING, sModelElementXMIID);
                int nKey = ((Integer) val.getParamTwo()).intValue();
                IProductArchiveElement foundElement = val.getParamOne();
                assert (foundElement != null);
                if (foundElement != null)
                {
                    foundElement.addAttributeString(TOPLEVELID_STRING, sTopLevelElementXMIID);

                    saveArchive(productArchive);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#addAssociatedElement(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public void addAssociatedElement(IElement pElement)
    {
        if (pElement != null)
        {
            String sTopLevelXMIID = pElement.getTopLevelId();
            String sXMIID = pElement.getXMIID();
            if (sXMIID != null && sXMIID.length() > 0 && sTopLevelXMIID != null && sTopLevelXMIID.length() > 0)
            {
                addAssociatedElement(sTopLevelXMIID, sXMIID);
            }
        }
    }

    /**
     * Removes an associated element from our list
     *
     * @param sTopLevelElementXMIID [in] The elements toplevel id
     * @param sModelElementXMIID [in] The element we should remove
     */
    public void removeAssociatedElement(String sTopLevelElementXMIID, String sModelElementXMIID)
    {
        // Get the IDiagram, and route the call to the open diagram
        IDiagram pDiagram = getDiagram();
        if (pDiagram != null)
        {
            pDiagram.removeAssociatedElement(sTopLevelElementXMIID, sModelElementXMIID);
        }
        else
        {
            IProductArchive pProductArchive = getArchive();
            if (pProductArchive != null)
            {
                pProductArchive.removeFromTable(ASSOCIATED_ELEMENTS_STRING, sModelElementXMIID);
                saveArchive(pProductArchive);
            }
        }
    }

    /**
     * Removes an associated element from our list
     *
     * @param pElement [in] The element we should remove
     */
    public void removeAssociatedElement(IElement pElement)
    {
        if (pElement != null)
        {
            IElement pElementToRemove = null;
            // If a presentation element is passed in then get the element it represents.
            if (pElement instanceof IPresentationElement)
            {
                IPresentationElement pPE = (IPresentationElement) pElement;
                pElementToRemove = pPE.getFirstSubject();
            }
            else
            {
                pElementToRemove = pElement;
            }
            String sTopLevelXMIID = pElementToRemove.getTopLevelId();
            String sXMIID = pElementToRemove.getXMIID();
            if (sXMIID != null && sXMIID.length() > 0 && sTopLevelXMIID != null && sTopLevelXMIID.length() > 0)
            {
                removeAssociatedElement(sTopLevelXMIID, sXMIID);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#getAssociatedElements()
     */
    public ETList<IElement> getAssociatedElements()
    {
        ETList<IElement> elements = null;

        // TODO: meteora
//        // Get the IDiagram, and route the call to the open diagram
//        IDiagram diagram = getDiagram();
//        if (diagram != null)
//        {
//            elements = diagram.getAssociatedElements();
//        }
//        else
//        {
//            ETList<IElement> foundElements = new ETArrayList<IElement>();
//            if (foundElements != null)
//            {
//                // Get the details using the cached diagrams maintained in diagram support
//                DiagramDetails details = CachedDiagrams.instance().getInfo(m_Filename);
//
//                ElementReloader reloader = new ElementReloader();
//
//                if ((details != null) && (reloader != null))
//                {
//                    ArrayList<ModelElementXMIIDPair> associatedElements = details.getAssociatedElements();
//                    if ((associatedElements != null) && (associatedElements.size() > 0))
//                    {
//                        for (Iterator iter = associatedElements.iterator(); iter.hasNext();)
//                        {
//                            ModelElementXMIIDPair element = (ModelElementXMIIDPair) iter.next();
//                            IElement foundElement = reloader.getElement(element.getTopLevelID(), element.getModelElementID());
//                            if (foundElement != null)
//                            {
//                                foundElements.add(foundElement);
//                            }
//                        }
//                    }
//                }
//            }
//            elements = foundElements;
//        }

        return elements;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#isAssociatedElement(java.lang.String)
     */
    public boolean isAssociatedElement(String sModelElementXMIID)
    {
        boolean retVal = false;
        // TODO: meteora
        
//        if ((sModelElementXMIID != null) && (sModelElementXMIID.length() > 0))
//        {
//            IDiagram diagram = getDiagram();
//            if (diagram != null)
//            {
//                retVal = diagram.isAssociatedElement(sModelElementXMIID);
//            }
//            else
//            {
//                DiagramDetails details = CachedDiagrams.instance().getInfo(m_Filename);
//
//                ArrayList<ModelElementXMIIDPair> associatedElements = details.getAssociatedElements();
//
//                for (Iterator iter = associatedElements.iterator(); iter.hasNext();)
//                {
//                    ModelElementXMIIDPair pair = (ModelElementXMIIDPair) iter.next();
//                    if (sModelElementXMIID.equals(pair.getModelElementID()))
//                    {
//                        retVal = true;
//                        break;
//                    }
//                }
//            }
//        }

        return retVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram#isAssociatedElement2(org.netbeans.modules.uml.core.metamodel.core.foundation.IElement)
     */
    public boolean isAssociatedElement(IElement pElement)
    {
        // TODO Auto-generated method stub
        return false;
    }

    //**************************************************
    // Helper Methods
    //**************************************************
    /**
     * @param productArchive
     */
    protected void saveArchive(IProductArchive productArchive)
    {
        productArchive.save(null);
    }

    /**
     * @return
     */
    protected IProductArchive getArchive()
    {
        // TODO: meteora - N/A with new diagram file
        
//        IProductArchive retVal = new ProductArchiveImpl();
//
//        if (retVal.load(m_Filename) == false)
//        {
//            // Failed to load therefore do not return the product archive.
//            retVal = null;
//        }
//        return retVal;
        return null;
    }

    @Override
    public String toString()
    {
        return getName();
    }

//    // IDrawingPropertyProvider
//    public ETList<IDrawingProperty> getDrawingProperties()
//    {
//        ETList<IDrawingProperty> pProperties = null;
//
//        IDiagram pDiagram = getDiagram();
//        if (pDiagram instanceof UIDiagram)
//        {
//            IUIDiagram pAxDiagram = (IUIDiagram) pDiagram;
//            IDrawingAreaControl pControl = pAxDiagram.getDrawingArea();
//            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider) pControl;
//            pProperties = pProvider.getDrawingProperties();
//        }
//
//        return pProperties;
//    }
//
//    public void saveColor(String sDrawEngineType, String sResourceName, int nColor)
//    {
//        IDiagram pDiagram = getDiagram();
//        if (pDiagram instanceof IUIDiagram)
//        {
//            IUIDiagram pAxDiagram = (IUIDiagram) pDiagram;
//            IDrawingAreaControl pControl = pAxDiagram.getDrawingArea();
//            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider) pControl;
//            pProvider.saveColor(sDrawEngineType, sResourceName, nColor);
//        }
//    }
//
//    public void saveColor2(IColorProperty pProperty)
//    {
//        IDiagram pDiagram = getDiagram();
//        if (pDiagram instanceof IUIDiagram)
//        {
//            IUIDiagram pAxDiagram = (IUIDiagram) pDiagram;
//            IDrawingAreaControl pControl = pAxDiagram.getDrawingArea();
//            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider) pControl;
//            pProvider.saveColor2(pProperty);
//        }
//    }
//
//    public void saveFont(String sDrawEngineName, String sResourceName, String sFaceName, int nHeight, int nWeight, boolean bItalic, int nColor)
//    {
//        IDiagram pDiagram = getDiagram();
//        if (pDiagram instanceof IUIDiagram)
//        {
//            IUIDiagram pAxDiagram = (IUIDiagram) pDiagram;
//            IDrawingAreaControl pControl = pAxDiagram.getDrawingArea();
//            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider) pControl;
//            pProvider.saveFont(sDrawEngineName, sResourceName, sFaceName, nHeight, nWeight, bItalic, nColor);
//        }
//    }
//
//    public void saveFont2(IFontProperty pProperty)
//    {
//        IDiagram pDiagram = getDiagram();
//        if (pDiagram instanceof IUIDiagram)
//        {
//            IUIDiagram pAxDiagram = (IUIDiagram) pDiagram;
//            IDrawingAreaControl pControl = pAxDiagram.getDrawingArea();
//            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider) pControl;
//            pProvider.saveFont2(pProperty);
//        }
//    }
//
//    public void resetToDefaultResource(String sDrawEngineName, String sResourceName, String sResourceType)
//    {
//        IDiagram pDiagram = getDiagram();
//        if (pDiagram instanceof IUIDiagram)
//        {
//            IUIDiagram pAxDiagram = (IUIDiagram) pDiagram;
//            IDrawingAreaControl pControl = pAxDiagram.getDrawingArea();
//            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider) pControl;
//            pProvider.resetToDefaultResource(sDrawEngineName, sResourceName, sResourceType);
//        }
//    }
//
//    public void resetToDefaultResources()
//    {
//        IDiagram pDiagram = getDiagram();
//        if (pDiagram instanceof IUIDiagram)
//        {
//            IUIDiagram pAxDiagram = (IUIDiagram) pDiagram;
//            IDrawingAreaControl pControl = pAxDiagram.getDrawingArea();
//            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider) pControl;
//            pProvider.resetToDefaultResources();
//        }
//    }
//
//    public void resetToDefaultResources2(String sDrawEngineName)
//    {
//        IDiagram pDiagram = getDiagram();
//        if (pDiagram instanceof IUIDiagram)
//        {
//            IUIDiagram pAxDiagram = (IUIDiagram) pDiagram;
//            IDrawingAreaControl pControl = pAxDiagram.getDrawingArea();
//            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider) pControl;
//            pProvider.resetToDefaultResources2(sDrawEngineName);
//        }
//    }
//
//    public void dumpToFile(String sFile, boolean bDumpChildren, boolean bAppendToExistingFile)
//    {
//        IDiagram pDiagram = getDiagram();
//        if (pDiagram instanceof IUIDiagram)
//        {
//            IUIDiagram pAxDiagram = (IUIDiagram) pDiagram;
//            IDrawingAreaControl pControl = pAxDiagram.getDrawingArea();
//            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider) pControl;
//            pProvider.dumpToFile(sFile, bDumpChildren, bAppendToExistingFile);
//        }
//    }
//
//    public boolean displayFontDialog(IFontProperty pProperty)
//    {
//        IDiagram pDiagram = getDiagram();
//        if (pDiagram instanceof IUIDiagram)
//        {
//            IUIDiagram pAxDiagram = (IUIDiagram) pDiagram;
//            IDrawingAreaControl pControl = pAxDiagram.getDrawingArea();
//            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider) pControl;
//            return pProvider.displayFontDialog(pProperty);
//        }
//        else
//        {
//            return false;
//        }
//    }
//
//    public boolean displayColorDialog(IColorProperty pProperty)
//    {
//        IDiagram pDiagram = getDiagram();
//        if (pDiagram instanceof IUIDiagram)
//        {
//            IUIDiagram pAxDiagram = (IUIDiagram) pDiagram;
//            IDrawingAreaControl pControl = pAxDiagram.getDrawingArea();
//            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider) pControl;
//            return pProvider.displayColorDialog(pProperty);
//        }
//        else
//        {
//            return false;
//        }
//    }
////
//    public void invalidateProvider()
//    {
//        IDiagram pDiagram = getDiagram();
//        if (pDiagram instanceof IUIDiagram)
//        {
//            IUIDiagram pAxDiagram = (IUIDiagram) pDiagram;
//            IDrawingAreaControl pControl = pAxDiagram.getDrawingArea();
//            IDrawingPropertyProvider pProvider = (IDrawingPropertyProvider) pControl;
//            pProvider.invalidateProvider();
//        }
//    }
}
