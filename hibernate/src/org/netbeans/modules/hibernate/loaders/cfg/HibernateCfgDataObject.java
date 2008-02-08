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
package org.netbeans.modules.hibernate.loaders.cfg;

import org.netbeans.modules.hibernate.loaders.cfg.multiview.HibernateCfgToolBarMVElement;
import java.awt.Image;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.cfg.model.Security;
import org.netbeans.modules.hibernate.cfg.model.SessionFactory;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.netbeans.modules.xml.multiview.DesignMultiViewDesc;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * 
 * @author Dongmei Cao
 */
public class HibernateCfgDataObject extends XmlMultiViewDataObject {

    private static final int TYPE_TOOLBAR = 0;
    public static final int UPDATE_DELAY = 200;
    private static final String DESIGN_VIEW_ID = "hibernate_configuration_multiview_design"; // NOI18N
    private HibernateConfiguration configuration;
    private ModelSynchronizer modelSynchronizer;
    /**
     * The property name for the event fired when a security tag is added or removed
     */
    private static final String SECURITY_ADDED_OR_REMOVED = "security_added_or_removed";

    public HibernateCfgDataObject(FileObject pf, HibernateCfgDataLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);

        // Synchronize between the vew and XML file
        modelSynchronizer = new ModelSynchronizer(this);

        CookieSet cookies = getCookieSet();
        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
        org.xml.sax.InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        cookies.add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        cookies.add(validateCookie);
        parseDocument();
    }

    /**
     * Parses the document.
     * @return true if document could be parsed (it was valid), false otwherwise.
     */
    public boolean parseDocument() {
        if (configuration == null) {
            try {
                configuration = getHibernateConfiguration();
            } catch (RuntimeException ex) { // must catch RTE (thrown by schema2beans when document is not valid)
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                return false;
            }
        } else {
            try {
                java.io.InputStream is = getEditorSupport().getInputStream();
                HibernateConfiguration newConfiguration = null;
                try {
                    newConfiguration = HibernateConfiguration.createGraph(is);
                } catch (RuntimeException ex) { // must catch RTE (thrown by schema2beans when document is not valid)
                    ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                    return false;
                }
                if (newConfiguration != null) {
                    try {
                        configuration.merge(newConfiguration, BaseBean.MERGE_UPDATE);
                    } catch (IllegalArgumentException iae) {
                        ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, iae);
                        return false;
                    }
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the object graph representing the contents of the 
     * Hibernate configuration file with which this data object 
     * is associated.
     *
     * @return the persistence graph.
     */
    public HibernateConfiguration getHibernateConfiguration() {
        if (configuration == null) {
            try {
                configuration = HibernateCfgMetadata.getDefault().getRoot(getPrimaryFile());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        assert configuration != null;
        return configuration;
    }
    
    /**
     *  Adds the session Factory model object to the HibernateConfiguration. 
     *  @param sFactory
     */

    public void addSessionFactory(SessionFactory sFactory) {
        getHibernateConfiguration().setSessionFactory(sFactory);
        modelUpdatedFromUI();
    }

    /**
     * Saves the document.
     * @see EditorCookie#saveDocument
     */
    public void save() {
        EditorCookie edit = (EditorCookie) getCookie(EditorCookie.class);
        if (edit != null) {
            try {
                edit.saveDocument();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    protected Node createNodeDelegate() {
        return new HibernateCfgDataNode(this);
    }

    protected String getPrefixMark() {
        return null;
    }

    public void modelUpdatedFromUI() {
        setModified(true);
        modelSynchronizer.requestUpdateData();
    }

    @Override
    public boolean isDeleteAllowed() {
        return true;
    }

    @Override
    public boolean isCopyAllowed() {
        return true;
    }

    @Override
    public boolean isMoveAllowed() {
        return true;
    }

    @Override
    protected Image getXmlViewIcon() {
        return Utilities.loadImage("org/netbeans/modules/hibernate/resources/hibernateCfg.gif");
    }

    /** 
     * Enable to focus specific object in Multiview Editor
     * The default implementation opens the XML View
     */
    @Override
    public void showElement(Object element) {
        Object target = null;
        if (element instanceof SessionFactory ||
                element instanceof Security) {
            openView(0);
            target = element;
        }

        if (target != null) {
            final Object key = target;
            org.netbeans.modules.xml.multiview.Utils.runInAwtDispatchThread(new Runnable() {

                public void run() {
                    getActiveMultiViewElement0().getSectionView().openPanel(key);
                }
            });
        }
    }

    /** 
     * Enable to get active MultiViewElement object
     */
    public ToolBarMultiViewElement getActiveMultiViewElement0() {
        return (ToolBarMultiViewElement) super.getActiveMultiViewElement();
    }

    protected DesignMultiViewDesc[] getMultiViewDesc() {
        return new DesignMultiViewDesc[]{new DesignView(this, TYPE_TOOLBAR)};
    }

    private static class DesignView extends DesignMultiViewDesc {

        private static final long serialVersionUID = 1L;
        private int type;

        DesignView(HibernateCfgDataObject dObj, int type) {
            super(dObj, NbBundle.getMessage(HibernateCfgDataObject.class, "LBL_Design"));
            this.type = type;
        }

        public MultiViewElement createElement() {
            HibernateCfgDataObject dObj = (HibernateCfgDataObject) getDataObject();
            return new HibernateCfgToolBarMVElement(dObj);
        }

        public Image getIcon() {
            return Utilities.loadImage("org/netbeans/modules/hibernate/resources/hibernateCfg.gif");
        }

        public String preferredID() {
            return DESIGN_VIEW_ID + String.valueOf(type);
        }

        @Override
        public HelpCtx getHelpCtx() {
            //return new HelpCtx(HELP_ID_DESIGN_HIBERNATE_CONFIGURATION); //NOI18N
            return null;
        }
    }

    private class ModelSynchronizer extends XmlMultiViewDataSynchronizer {

        public ModelSynchronizer(XmlMultiViewDataObject dataObject) {
            super(dataObject, UPDATE_DELAY);
        }

        protected boolean mayUpdateData(boolean allowDialog) {
            return true;
        }

        protected void updateDataFromModel(Object model, FileLock lock, boolean modify) {
            if (model == null) {
                return;
            }
            try {
                Writer out = new StringWriter();
                ((HibernateConfiguration) model).write(out);
                out.close();
                getDataCache().setData(lock, out.toString(), modify);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
            } catch (Schema2BeansException e) {
                ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, e);
            } finally {
                if (lock != null) {
                    lock.releaseLock();
                }
            }
        }

        protected Object getModel() {
            return getHibernateConfiguration();
        }

        protected void reloadModelFromData() {
            parseDocument();
        }
    }
}
