/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.j2ee.ddloaders.web;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.event.ChangeListener;

import org.openide.DialogDescriptor;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;

import org.xml.sax.*;
import org.openide.util.NbBundle;

import org.netbeans.modules.j2ee.ddloaders.web.event.*;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.spi.xml.cookies.*;
import org.openide.DialogDisplayer;
import org.netbeans.modules.j2ee.dd.impl.web.WebAppProxy;
import org.netbeans.modules.j2ee.dd.impl.web.WebParseUtils;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;

import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.ddloaders.catalog.EnterpriseCatalog;

import org.netbeans.modules.j2ee.ddloaders.web.multiview.*;
import org.netbeans.modules.j2ee.ddloaders.multiview.DDMultiViewDataObject;
import org.netbeans.modules.xml.multiview.DesignMultiViewDesc;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.openide.util.Exceptions;

/** Represents a DD object in the Repository.
 *
 * @author  mkuchtiak
 */
public class DDDataObject extends  DDMultiViewDataObject
    implements DDChangeListener, ChangeListener, PropertyChangeListener {
    private transient WebApp webApp;
    private transient FileObject srcRoots[];
    protected transient final static RequestProcessor RP = new RequestProcessor("XML Parsing");   // NOI18N
    protected boolean changedFromUI;

    private static final long serialVersionUID = 8857563089355069362L;

    /** Property name for documentDTD property */
    public static final String PROP_DOCUMENT_DTD = "documentDTD";   // NOI18N
    public static final String HELP_ID_PREFIX_OVERVIEW="dd_multiview_overview_"; //NOI18N
    public static final String HELP_ID_PREFIX_SERVLETS="dd_multiview_servlets_"; //NOI18N
    public static final String HELP_ID_PREFIX_FILTERS="dd_multiview_filters_"; //NOI18N
    public static final String HELP_ID_PREFIX_PAGES="dd_multiview_pages_"; //NOI18N
    public static final String HELP_ID_PREFIX_REFERENCES="dd_multiview_references_"; //NOI18N
    public static final String HELP_ID_PREFIX_SECURITY="dd_multiview_security_"; //NOI18N

    private static final Logger LOG = Logger.getLogger(DDMultiViewDataObject.class.getName());

    /** Holder of documentDTD property value */
    private String documentDTD;

    /** List of updates to servlets that should be processed */
    private Vector updates;

    private transient RequestProcessor.Task updateTask;
    private transient FileObjectObserver fileListener;

    public DDDataObject (FileObject pf, DDDataLoader loader) throws DataObjectExistsException {
        super (pf, loader);
        init (pf,loader);
    }

    private void init (FileObject fo,DDDataLoader loader) {
        // added ValidateXMLCookie
        InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        getCookieSet().add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);

        fileListener = new FileObjectObserver(fo);

        Project project = FileOwnerQuery.getOwner (getPrimaryFile ());
        if (project != null) {
            Sources sources = ProjectUtils.getSources(project);
            sources.addChangeListener (this);
        }
        refreshSourceFolders ();
        addPropertyChangeListener(this);
    }

    private void refreshSourceFolders () {
        ArrayList srcRootList = new ArrayList ();

        Project project = FileOwnerQuery.getOwner (getPrimaryFile ());
        if (project != null) {
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            for (int i = 0; i < groups.length; i++) {
                if (WebModule.getWebModule (groups [i].getRootFolder ()) != null) {
                    srcRootList.add (groups [i].getRootFolder ());
                    DataLoaderPool.getDefault().removeOperationListener(operationListener); //avoid being added multiple times
                    DataLoaderPool.getDefault().addOperationListener(operationListener);
                }
            }
        }
        srcRoots = (FileObject []) srcRootList.toArray (new FileObject [srcRootList.size ()]);
    }

    private String getPackageName (FileObject clazz) {
        for (int i = 0; i < srcRoots.length; i++) {
            String rp = FileUtil.getRelativePath (srcRoots [i], clazz);
            if (rp != null) {
                if (clazz.getExt ().length () > 0) {
                    rp = rp.substring (0, rp.length () - clazz.getExt ().length () - 1);
                }
                return rp.replace ('/', '.');
            }
        }
        return null;
    }

    public WebApp getWebApp() {
        if (webApp == null) {
            try {
                webApp = createWebApp();
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.INFO, null, ex);
            }
        }
        return webApp;
    }

    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (DDDataObject.PROP_DOCUMENT_VALID.equals (evt.getPropertyName ())) {
            if (this.isValid()){
                ((DDDataNode)getNodeDelegate()).iconChanged();
            }
        }
    }

    private WebApp createWebApp() throws java.io.IOException {
        WebApp webApp = DDProvider.getDefault().getDDRoot(getPrimaryFile());
        if (webApp != null) {
            setSaxError(webApp.getError());
        }
        return webApp;
    }

    protected org.openide.nodes.Node createNodeDelegate () {
        return new DDDataNode(this);
    }

    /**
     * Sets only reasonable mappings (mappings with existing servlet element
     * @param mappings - all mappings
     */
    public void setReasonableMappings(ServletMapping[] mappings) {
        List newMappings = new ArrayList();
        Servlet[] servlets = webApp.getServlet();
        for (int i=0;i<mappings.length;i++) {
            for (int j=0;j<servlets.length;j++) {
                if (servlets[j].getServletName().equals(mappings[i].getServletName())) {
                    newMappings.add(mappings[i]);
                    break;
                }
            }
        }
        ServletMapping[] maps = new ServletMapping[newMappings.size()];
        newMappings.toArray(maps);
        webApp.setServletMapping(maps);
        //setNodeDirty(true);
        //modelUpdatedFromUI();
    }

    protected void parseDocument() throws IOException {
        if (webApp == null || ((WebAppProxy) webApp).getOriginal() == null) {
            try {
                webApp = DDProvider.getDefault().getDDRoot(getPrimaryFile());
            } catch (IOException e) {
                if (webApp == null) {
                    webApp = new WebAppProxy(null, null);
                }
            }
        }
        // update model with the document
        parseDocument(true);
    }

    protected void validateDocument() throws IOException {
        // parse document without updating model
        parseDocument(false);
    }

    private void parseDocument(boolean updateWebApp) throws IOException {
        WebAppProxy webAppProxy = (WebAppProxy) webApp;
        try {
            // preparsing
            SAXParseException error = WebParseUtils.parse(new InputSource(createReader()), new EnterpriseCatalog());
            setSaxError(error);

            String version = WebParseUtils.getVersion(new InputSource(createReader()));
            // creating model
            WebAppProxy app = new WebAppProxy(org.netbeans.modules.j2ee.dd.impl.common.DDUtils.createWebApp(
                    createInputStream(), version), version);
            if (updateWebApp) {
                if (version.equals(webAppProxy.getVersion()) && webAppProxy.getOriginal() != null) {
                    webApp.merge(app, WebApp.MERGE_UPDATE);
                } else if (app.getOriginal() != null) {
                    webApp = webAppProxy = app;
                }
            }
            webAppProxy.setStatus(error != null ? WebApp.STATE_INVALID_PARSABLE : WebApp.STATE_VALID);
            webAppProxy.setError(error);
        } catch (SAXException ex) {
            webAppProxy.setStatus(WebApp.STATE_INVALID_UNPARSABLE);
            if (ex instanceof SAXParseException) {
                webAppProxy.setError((SAXParseException) ex);
            } else if (ex.getException() instanceof SAXParseException) {
                webAppProxy.setError((SAXParseException) ex.getException());
            }
            setSaxError(ex);
        } catch (IllegalArgumentException iae) {
            // see #104180
            webAppProxy.setStatus(WebApp.STATE_INVALID_UNPARSABLE);
            LOG.log(Level.FINE, "IAE thrown during merge, see #104180.", iae); //NO18N
        }
    }

    protected RootInterface getDDModel() {
        return getWebApp();
    }

    public boolean isDocumentParseable() {
        return WebApp.STATE_INVALID_UNPARSABLE != getWebApp().getStatus();
    }

    protected String getPrefixMark() {
        return "<web-app";
    }

    /**
     * Adds servlet and servlet-mapping elements to map servlet.
     *
     * One servlet element and one matching servlet-mapping element. The servlet-name is
     * set to Servlet_&lt clazz&gt by default.
     *
     * @param clazz class name of servlet
     * @param urlPattern path to servlet class (pkg/foo/Bar)
     */
    private void createDefaultServletConfiguration (String clazz, String urlPattern) {
        // PENDING: should be synchronized
        WebApp wappTo = getWebApp ();
        try {
            Servlet newSrvlt = (Servlet)webApp.createBean("Servlet");
            newSrvlt.setServletClass (clazz);
            String name = DDUtils.findFreeName (wappTo.getServlet (), "ServletName" , "Servlet_"+clazz); // NOI18N
            newSrvlt.setServletName (name);
            newSrvlt.setDescription (NbBundle.getMessage (DDDataObject.class, "TXT_newServletElementDescription"));
            newSrvlt.setDisplayName ("Servlet "+clazz); // NOI18N
            wappTo.addServlet (newSrvlt);

            ServletMapping newSM = (ServletMapping)webApp.createBean("ServletMapping");
            newSM.setServletName (name);
            newSM.setUrlPattern (urlPattern);
            wappTo.addServletMapping (newSM);

            //setNodeDirty (true);
            //modelUpdatedFromUI();
        } catch (ClassNotFoundException ex) {}
    }

    protected DataObject handleCopy(DataFolder f) throws IOException {
        DataObject dObj = super.handleCopy(f);
        try { dObj.setValid(false); }catch(java.beans.PropertyVetoException e){}
        return dObj;
    }

    protected void dispose () {
        // no more changes in DD
        synchronized (this) {
            updates = null;
            if (updateTask != null) {
                updateTask.cancel();
            }
        }
        super.dispose ();
    }

    /** Getter for property documentDTD.
     * @return Value of property documentDTD or <CODE>null</CODE> if documentDTD cannot be obtained.
     */
    public String getDocumentDTD () {
        if (documentDTD == null) {
            WebApp wa = getWebApp ();
        }
        return documentDTD;
    }

    /** This methods gets called when servlet is changed
     * @param evt - object that describes the change.
     */
    public void deploymentChange (DDChangeEvent evt) {
        // fix of #28542, don't add servlet, if it's already defined in DD
        if (evt.getType() == DDChangeEvent.SERVLET_ADDED && servletDefined(evt.getNewValue())) {
            return;
        }

        // these are now handled in j2ee/refactoring - see #70389. 
        else if (evt.getType() == DDChangeEvent.LISTENER_DELETED 
                || evt.getType() == DDChangeEvent.FILTER_DELETED
                || evt.getType() == DDChangeEvent.SERVLET_DELETED){
            
            return;
        }
        
        synchronized (this) {
            if (updates == null) {
                updates = new Vector ();
            }
            updates.addElement (evt);
        }

        // schedule processDDChangeEvent
        if (updateTask == null) {
            updateTask = RequestProcessor.getDefault().post (new Runnable () {
                public void run () {
                    java.util.List changes = null;
                    synchronized (DDDataObject.this) {
                        if (!DDDataObject.this.isValid()) {
                            return;
                        }
                        if (updates != null) {
                            changes = updates;
                            updates = null;
                        }
                    }
                    if (changes != null) {
                        showDDChangesDialog(changes);
                    }
                }
            }, 2000, Thread.MIN_PRIORITY);
        }
        else {
            updateTask.schedule (2000);
        }
    }

    private boolean servletDefined(String classname) {
        WebApp webApp = getWebApp();
        if (webApp == null) {
            return true;
        }
        Servlet[] servlets = webApp.getServlet();
        for ( int i = 0; i < servlets.length; i++ ) {
            if (servlets[i].getServletClass() != null && servlets[i].getServletClass().equals(classname)) {
                return true;
            }
        }
        return false;
    }

    private void showDDChangesDialog (List changes) {
        final JButton processButton;
        final JButton processAllButton;
        final JButton closeButton;
        final DDChangesPanel connectionPanel;
        final DialogDescriptor confirmChangesDescriptor;
        final Dialog confirmChangesDialog[] = { null };

        processButton = new JButton (NbBundle.getMessage (DDDataObject.class, "LAB_processButton"));
        processButton.setMnemonic (NbBundle.getMessage (DDDataObject.class, "LAB_processButton_Mnemonic").charAt (0));
        processButton.setToolTipText (NbBundle.getMessage (DDDataObject.class, "ACS_processButtonA11yDesc"));
        processAllButton = new JButton (NbBundle.getMessage (DDDataObject.class, "LAB_processAllButton"));
        processAllButton.setMnemonic (NbBundle.getMessage (DDDataObject.class, "LAB_processAllButton_Mnemonic").charAt (0));
        processAllButton.setToolTipText (NbBundle.getMessage (DDDataObject.class, "ACS_processAllButtonA11yDesc"));
        closeButton = new JButton (NbBundle.getMessage (DDDataObject.class, "LAB_closeButton"));
        closeButton.setMnemonic (NbBundle.getMessage (DDDataObject.class, "LAB_closeButton_Mnemonic").charAt (0));
        closeButton.setToolTipText (NbBundle.getMessage (DDDataObject.class, "ACS_closeButtonA11yDesc"));
        final Object [] options = new Object [] {
            processButton,
            processAllButton
        };
        final Object [] additionalOptions = new Object [] {
            closeButton
        };
        WebModule wm = WebModule.getWebModule(getPrimaryFile ());
        String fsname=""; // NOI18N
        if (wm!=null) {
            fsname=wm.getContextPath();
        }
        String caption = NbBundle.getMessage (DDDataObject.class, "MSG_SynchronizeCaption", fsname);
        connectionPanel = new DDChangesPanel (caption, processButton);
        confirmChangesDescriptor = new DialogDescriptor (
            connectionPanel,
            NbBundle.getMessage (DDDataObject.class, "LAB_ConfirmDialog"),
            true,
            options,
            processButton,
            DialogDescriptor.RIGHT_ALIGN,
            HelpCtx.DEFAULT_HELP,
            new ActionListener () {
                public void actionPerformed (ActionEvent e) {
                    if (e.getSource () instanceof Component) {
                        Component root;

                        // hack to avoid multiple calls for disposed dialogs:
                        root = javax.swing.SwingUtilities.getRoot ((Component)e.getSource ());
                        if (!root.isDisplayable ()) {
                            return;
                        }
                    }
                    if (options[0].equals (e.getSource ())) {
                        int min = connectionPanel.changesList.getMinSelectionIndex ();
                        int max = connectionPanel.changesList.getMaxSelectionIndex ();
                        for (int i = max; i >= min; i--) {
                            if (connectionPanel.changesList.isSelectedIndex (i)) {
                                final DDChangeEvent ev = (DDChangeEvent)connectionPanel.listModel.getElementAt (i);
                                processDDChangeEvent (ev);
                                connectionPanel.listModel.removeElementAt (i);
                            }
                        }
                        if (connectionPanel.listModel.isEmpty ()) {
                            confirmChangesDialog[0].setVisible (false);
                        }
                        else {
                            processButton.setEnabled (false);
                        }
                    }
                    else if (options[1].equals (e.getSource ())) {
                        Enumeration en = connectionPanel.listModel.elements ();
                        while (en.hasMoreElements ()) {
                            processDDChangeEvent ((DDChangeEvent)en.nextElement ());
                        }
                        confirmChangesDialog[0].setVisible (false);
                        connectionPanel.setChanges (null);
                    }
                    else if (additionalOptions[0].equals (e.getSource ())) {
                        confirmChangesDialog[0].setVisible (false);
                        connectionPanel.setChanges (null);
                    }
                }
            }
        );
        confirmChangesDescriptor.setAdditionalOptions (additionalOptions);

        processButton.setEnabled (false);
        processAllButton.requestFocus ();
        connectionPanel.setChanges (changes);

        try {
            confirmChangesDialog[0] = DialogDisplayer.getDefault ().createDialog (confirmChangesDescriptor);
            confirmChangesDialog[0].setVisible(true);
        } finally {
            confirmChangesDialog[0].dispose ();
        }
    }

    private void processDDChangeEvent (DDChangeEvent evt) {
        if (!isValid()) {
            return;
        }

        if (evt.getType () == DDChangeEvent.SERVLET_ADDED) {
            String clz = evt.getNewValue ();

            // new from template or copy of another servlet
            String urimapping = "/servlet/"+clz;    // NOI18N
            createDefaultServletConfiguration (clz, urimapping);
        }
        else if (evt.getType () == DDChangeEvent.SERVLET_CHANGED) {
            // update servlet-class in servlet element
            String old = evt.getOldValue ();
            if (old == null) {
                return;
            }

            Servlet [] servlets = getWebApp ().getServlet ();
            for (int i=0; i<servlets.length; i++) {
                if (old.equals (servlets[i].getServletClass ())) {
                    servlets[i].setServletClass ((String)evt.getNewValue ());
                }
            }
        }
        else if (evt.getType () == DDChangeEvent.SERVLET_DELETED) {
            // delete servlet and matching servlet-mappings
            String clz = evt.getNewValue ();
            if (clz == null) {
                return;
            }

            WebApp wa = getWebApp ();
            Servlet [] servlets = wa.getServlet ();
            java.util.Vector servletNames = new java.util.Vector ();
            for (int i=0; i<servlets.length; i++) {
                if (clz.equals (servlets[i].getServletClass ())) {
                    servletNames.addElement (servlets[i].getServletName ());
                    wa.removeServlet (servlets[i]);
                }
            }
            ServletMapping [] mappings = wa.getServletMapping ();
            for (int i=0; i<mappings.length; i++) {
                if (servletNames.contains (mappings[i].getServletName ())) {
                    wa.removeServletMapping (mappings[i]);
                }
            }
        }
        else if (evt.getType () == DDChangeEvent.FILTER_CHANGED) {
            String old = evt.getOldValue ();
            if (old == null) {
                return;
            }

            Filter [] filters = getWebApp ().getFilter ();
            for (int i=0; i<filters.length; i++) {
                if (old.equals (filters[i].getFilterClass ())) {
                    filters[i].setFilterClass ((String)evt.getNewValue ());
                }
            }
        }
        else if (evt.getType () == DDChangeEvent.FILTER_DELETED) {
            String clz = evt.getNewValue ();
            if (clz == null) {
                return;
            }

            WebApp wa = getWebApp ();
            Filter [] filters = wa.getFilter ();
            java.util.Vector filterNames = new java.util.Vector ();
            for (int i=0; i<filters.length; i++) {
                if (clz.equals (filters[i].getFilterClass ())) {
                    filterNames.addElement (filters[i].getFilterName ());
                    wa.removeFilter (filters[i]);
                }
            }
            FilterMapping [] mappings = wa.getFilterMapping ();
            for (int i=0; i<mappings.length; i++) {
                if (filterNames.contains (mappings[i].getFilterName ())) {
                    wa.removeFilterMapping (mappings[i]);
                }
            }
        }
        else if (evt.getType () == DDChangeEvent.LISTENER_CHANGED) {
            String old = evt.getOldValue ();
            if (old == null) {
                return;
            }

            Listener [] listeners = getWebApp ().getListener ();
            for (int i=0; i<listeners.length; i++) {
                if (old.equals (listeners[i].getListenerClass ())) {
                    listeners[i].setListenerClass ((String)evt.getNewValue ());
                }
            }
        }
        else if (evt.getType () == DDChangeEvent.LISTENER_DELETED) {
            String clz = evt.getNewValue ();
            if (clz == null) {
                return;
            }

            WebApp wa = getWebApp ();
            Listener [] listeners = wa.getListener ();
            for (int i=0; i<listeners.length; i++) {
                if (clz.equals (listeners[i].getListenerClass ())) {
                    wa.removeListener (listeners[i]);
                    break;
                }
            }
        }
        try {
            writeModel(getWebApp());
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    private OperationListener operationListener = new OperationAdapter() {
        public void operationDelete(OperationEvent ev) {
            FileObject fo = ev.getObject().getPrimaryFile();
            String resourceName = getPackageName (fo);
            if (resourceName != null && "java".equals(fo.getExt()) && getWebApp() != null) { //NOI18N
                boolean foundElement=false;
                Servlet[] servlets = getWebApp().getServlet();
                for (int i=0;i<servlets.length;i++) {
                    if (resourceName.equals(servlets[i].getServletClass())) {
                        DDChangeEvent ddEvent = new DDChangeEvent(DDDataObject.this,DDDataObject.this,null,resourceName,DDChangeEvent.SERVLET_DELETED);
                        deploymentChange (ddEvent);
                        foundElement=true;
                        break;
                    }
                }
                if (foundElement) {
                    return;
                }
                Filter[] filters = getWebApp().getFilter();
                for (int i=0;i<filters.length;i++) {
                    if (resourceName.equals(filters[i].getFilterClass())) {
                        DDChangeEvent ddEvent = new DDChangeEvent(DDDataObject.this,DDDataObject.this,null,resourceName,DDChangeEvent.FILTER_DELETED);
                        deploymentChange (ddEvent);
                        foundElement=true;
                        break;
                    }
                }
                if (foundElement) {
                    return;
                }
                Listener[] listeners = getWebApp().getListener();
                for (int i=0;i<listeners.length;i++) {
                    if (resourceName.equals(listeners[i].getListenerClass())) {
                        DDChangeEvent ddEvent = new DDChangeEvent(DDDataObject.this,DDDataObject.this,null,resourceName,DDChangeEvent.LISTENER_DELETED);
                        deploymentChange (ddEvent);
                        break; // listener with that class should be only one
                    }
                }
            }
        }
    };

    public void stateChanged (javax.swing.event.ChangeEvent e) {
        refreshSourceFolders ();
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(HELP_ID_PREFIX_OVERVIEW+"overviewNode"); //NOI18N
    }

    /** Used to detect if data model has already been created or not.
     * Method is called before switching to the design view from XML view when the document isn't parseable.
     */
    protected boolean isModelCreated() {
        return (webApp!=null && ((org.netbeans.modules.j2ee.dd.impl.web.WebAppProxy)webApp).getOriginal()!=null);
    }

    /** WeakListener for accepting external changes to web.xml
    */
    private class FileObjectObserver implements FileChangeListener {
        FileObjectObserver (FileObject fo) {
            fo.addFileChangeListener((FileChangeListener)org.openide.util.WeakListeners.create(
                                        FileChangeListener.class, this, fo));
        }

        public void fileAttributeChanged(FileAttributeEvent fileAttributeEvent) {
        }

        public void fileChanged(FileEvent fileEvent) {
            /*
           WebAppProxy webApp = (WebAppProxy) DDDataObject.this.getWebApp();
           boolean needRewriting = true;
           if (webApp!= null && webApp.isWriting()) { // change from outside
               webApp.setWriting(false);
               needRewriting=false;
           }
           if (isSavingDocument()) {// document is being saved
               setSavingDocument(false);
               needRewriting=false;
           }
           if (needRewriting) getEditorSupport().restartTimer();
            */
        }

        public void fileDataCreated(FileEvent fileEvent) {
        }

        public void fileDeleted(FileEvent fileEvent) {
        }

        public void fileFolderCreated(FileEvent fileEvent) {
        }

        public void fileRenamed(FileRenameEvent fileRenameEvent) {
        }
    }


    public static final String DD_MULTIVIEW_PREFIX = "dd_multiview"; // NOI18N
    public static final String MULTIVIEW_OVERVIEW = "Overview"; // NOI18N
    public static final String MULTIVIEW_SERVLETS = "Servlets"; // NOI18N
    public static final String MULTIVIEW_FILTERS = "Filters"; // NOI18N
    public static final String MULTIVIEW_PAGES = "Pages"; // NOI18N
    public static final String MULTIVIEW_REFERENCES = "References"; // NOI18N
    public static final String MULTIVIEW_SECURITY = "Security"; //NOI18N

    private ServletsMultiViewElement servletMVElement;

    protected DesignMultiViewDesc[] getMultiViewDesc() {
        return new DesignMultiViewDesc[] {
            new DDView(this,MULTIVIEW_OVERVIEW),
            new DDView(this,MULTIVIEW_SERVLETS),
            new DDView(this,MULTIVIEW_FILTERS),
            new DDView(this,MULTIVIEW_PAGES),
            new DDView(this,MULTIVIEW_REFERENCES),
            new DDView(this, MULTIVIEW_SECURITY)
            //new DDView(this,"Security")
        };
    }

    private static class DDView extends DesignMultiViewDesc implements Serializable {
        private static final long serialVersionUID = -4814134594154669985L;
        private String name;

        DDView() {}

        DDView(DDDataObject dObj,String name) {
            super(dObj, name);
            this.name=name;
        }

        public org.netbeans.core.spi.multiview.MultiViewElement createElement() {
            DDDataObject dObj = (DDDataObject)getDataObject();
            if (name.equals(MULTIVIEW_OVERVIEW)) {
                return new OverviewMultiViewElement(dObj,0);
            } else if (name.equals(MULTIVIEW_SERVLETS)) {
                return new ServletsMultiViewElement(dObj,1);
            } else if (name.equals(MULTIVIEW_FILTERS)) {
                return new FiltersMultiViewElement(dObj,2);
            } else if(name.equals(MULTIVIEW_PAGES)) {
                return new PagesMultiViewElement(dObj,3);
            } else if(name.equals(MULTIVIEW_REFERENCES)) {
                return new ReferencesMultiViewElement(dObj,4);
            } else if (name.equals(MULTIVIEW_SECURITY)) {
                return new SecurityMultiViewElement(dObj, 5);
            }
            return null; 
        }

        public HelpCtx getHelpCtx() {
            if (name.equals(MULTIVIEW_OVERVIEW)) {
                return new HelpCtx(HELP_ID_PREFIX_OVERVIEW+"overviewNode"); //NOI18N
            } else if (name.equals(MULTIVIEW_SERVLETS)) {
                return new HelpCtx(HELP_ID_PREFIX_SERVLETS+"servletsNode"); //NOI18N
            } else if (name.equals(MULTIVIEW_FILTERS)) {
                return new HelpCtx(HELP_ID_PREFIX_FILTERS+"filtersNode"); //NOI18N
            } else if(name.equals(MULTIVIEW_PAGES)) {
                return new HelpCtx(HELP_ID_PREFIX_OVERVIEW+"overviewNode"); //NOI18N
            } else if(name.equals(MULTIVIEW_REFERENCES)) {
                return new HelpCtx(HELP_ID_PREFIX_REFERENCES+"references"); //NOI18N
            }
            return null;
        }

        public java.awt.Image getIcon() {
            return org.openide.util.Utilities.loadImage("org/netbeans/modules/j2ee/ddloaders/web/resources/DDDataIcon.gif"); //NOI18N
        }

        public String preferredID() {
            return DD_MULTIVIEW_PREFIX+name;
        }

        public String getDisplayName() {
            return NbBundle.getMessage(DDDataObject.class,"TTL_"+name);
        }
    }

    /** Enable to focus specific object in Multiview Editor
     *  The default implementation opens the XML View
     */
    public void showElement(Object element) {
        Object target=null;
        if (element instanceof Servlet) {
            openView(1);
            target=element;
        } else if (element instanceof Filter) {
            openView(2);
            target=element;
        } else if (element instanceof Listener) {
            openView(0);
            target="listeners"; //NOI18N
        } else if (element instanceof InitParam) {
            InitParam param = (InitParam)element;
            InitParam[] params = getWebApp().getContextParam();
            for (int i=0;i<params.length;i++) {
                if (params[i]==param) {
                    openView(0);
                    target="context_params"; //NOI18N
                    break;
                }
            }
        } else if (element instanceof ErrorPage) {
            openView(3);
            target="error_pages"; //NOI18N
        }
        if (target!=null) {
            final Object key=target;
            org.netbeans.modules.xml.multiview.Utils.runInAwtDispatchThread(new Runnable() {
                public void run() {
                    ToolBarMultiViewElement mvEl = getActiveMVElement();
                    if (mvEl!=null) mvEl.getSectionView().openPanel(key);
                }
            });
        }
    }
    /** 
     * Do not allow to remove web.xml except for version 2.5.
     */
    public boolean isDeleteAllowed() {
        return WebApp.VERSION_2_5.equals(getWebApp().getVersion());
    }
    /** Enable to access Active element 
     */
    public ToolBarMultiViewElement getActiveMVElement() {
        return (ToolBarMultiViewElement)super.getActiveMultiViewElement();
    }

    public boolean isChangedFromUI() {
        return changedFromUI;
    }
    
    public void setChangedFromUI(boolean changedFromUI) {
        this.changedFromUI=changedFromUI;
    }
}
