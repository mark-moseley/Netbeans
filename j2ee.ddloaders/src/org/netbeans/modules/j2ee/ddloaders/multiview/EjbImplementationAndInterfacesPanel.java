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

package org.netbeans.modules.j2ee.ddloaders.multiview;

import java.awt.Component;
import org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.EjbImplementationAndInterfacesForm;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.ui.LinkButton;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.JTextField;

/**
 * @author pfiala
 */
public class EjbImplementationAndInterfacesPanel extends EjbImplementationAndInterfacesForm {

    private static final Logger LOGGER = Logger.getLogger(EjbImplementationAndInterfacesForm.class.getName());
    
    private EntityAndSessionHelper helper;
    private NonEditableDocument beanClassDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getEjbClass();
        }
    };
    private NonEditableDocument localComponentDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getLocal();
        }
    };
    private NonEditableDocument localHomeDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getLocalHome();
        }
    };
    private NonEditableDocument remoteComponentDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getRemote();
        }
    };
    private NonEditableDocument remoteHomeDocument = new NonEditableDocument() {
        protected String retrieveText() {
            return helper == null ? null : helper.getHome();
        }
    };

    private String className = null;
    private static final String LINK_BEAN = "linkBean";
    private static final String LINK_LOCAL = "linkLocal";
    private static final String LINK_LOCAL_HOME = "linkLocalHome";
    private static final String LINK_REMOTE = "linkRemote";
    private static final String LINK_REMOTE_HOME = "linkRemoteHome";

    /**
     * Creates new form BeanForm
     */
    public EjbImplementationAndInterfacesPanel(final SectionNodeView sectionNodeView,
            final EntityAndSessionHelper helper) {
        super(sectionNodeView);
        this.helper = helper;
        getBeanClassTextField().setDocument(beanClassDocument);
        getLocalComponentTextField().setDocument(localComponentDocument);
        getLocalHomeTextField().setDocument(localHomeDocument);
        getRemoteComponentTextField().setDocument(remoteComponentDocument);
        getRemoteHomeTextField().setDocument(remoteHomeDocument);

        scheduleRefreshView();

        FocusListener focusListener = new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                Component component = e.getComponent();
                if (component instanceof JTextField) {
                    className = ((JTextField) component).getText().trim();
                }
            }
        };
        addFocusListener(focusListener);

        XmlMultiViewDataSynchronizer synchronizer =
                ((EjbJarMultiViewDataObject) sectionNodeView.getDataObject()).getModelSynchronizer();

        initLinkButton(getBeanClassLinkButton(), LINK_BEAN);
        initLinkButton(getLocalComponentLinkButton(), LINK_LOCAL);
        initLinkButton(getLocalHomeLinkButton(), LINK_LOCAL_HOME);
        initLinkButton(getRemoteComponentLinkButton(), LINK_REMOTE);
        initLinkButton(getRemoteHomeLinkButton(), LINK_REMOTE_HOME);
    }

    private void initLinkButton(AbstractButton button, String key) {
        LinkButton.initLinkButton(button, this, null, key);
    }

    private void addInterfaces(boolean local) {
        String interfaceType = Utils.getBundleMessage(local ? "TXT_Local" : "TXT_Remote");
        String msg = Utils.getBundleMessage("MSG_AddInterfaces", interfaceType);
        String title = Utils.getBundleMessage("LBL_AddInterfaces");
        NotifyDescriptor descriptor = new NotifyDescriptor(msg, title, NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE, null, null);
        DialogDisplayer.getDefault().notify(descriptor);
        if (NotifyDescriptor.YES_OPTION == descriptor.getValue()) {
            try {
                helper.addInterfaces(local);
            } finally {
                scheduleRefreshView();
            }
        }
    }

    private void removeInterfaces(boolean local) {
        String componentInterface = local ? helper.getLocal() : helper.getRemote();
        String homeInterface = local ? helper.getLocalHome() : helper.getHome();
//        String businessInterfaceName = helper.getBusinessInterfaceName(local);
//        String msg;
//        if (businessInterfaceName == null) {
//            msg = Utils.getBundleMessage("MSG_RemoveInterfaces", homeInterface, componentInterface);
//        } else {
//            msg = Utils.getBundleMessage("MSG_RemoveInterfaces2", homeInterface, componentInterface,
//                    businessInterfaceName);
//        }
        String interfaceType = Utils.getBundleMessage(local ? "TXT_Local" : "TXT_Remote");
        String title = Utils.getBundleMessage("LBL_RemoveInterfaces", interfaceType);
//        NotifyDescriptor descriptor = new NotifyDescriptor(msg, title, NotifyDescriptor.YES_NO_OPTION,
//                NotifyDescriptor.WARNING_MESSAGE, null, null);
//        DialogDisplayer.getDefault().notify(descriptor);
//        if (NotifyDescriptor.YES_OPTION == descriptor.getValue()) {
//            try {
//                helper.removeInterfaces(local);
//            } finally {
//                scheduleRefreshView();
//            }
//        }
    }

    public void refreshView() {
        beanClassDocument.init();
        localComponentDocument.init();
        localHomeDocument.init();
        remoteComponentDocument.init();
        remoteHomeDocument.init();
    }

    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        if (source instanceof EntityAndSession) {
            scheduleRefreshView();
        }
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
        String javaClass = null;
        if(LINK_BEAN.equals(ddProperty)) {
            javaClass = helper.getEjbClass();
        } else if(LINK_LOCAL.equals(ddProperty)) {
            javaClass = helper.getLocal();
        } else if(LINK_LOCAL_HOME.equals(ddProperty)) {
            javaClass = helper.getLocalHome();
        } else if(LINK_REMOTE.equals(ddProperty)) {
            javaClass = helper.getRemote();
        } else if (LINK_REMOTE_HOME.equals(ddProperty)) {
            javaClass = helper.getHome();
        } 
        
        if (javaClass == null || "".equals(javaClass.trim())) {
            LOGGER.log(Level.INFO, "Could not resolve class for ddProperty:" + ddProperty ); //NO18N
            return;
        }
        Utils.openEditorFor(helper.ejbJarFile, javaClass);
    }
}
