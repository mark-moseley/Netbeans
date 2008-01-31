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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.hibernate.loaders.cfg.multiview;

import org.netbeans.modules.hibernate.loaders.cfg.*;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hibernate.cfg.model.Event;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.xml.multiview.ui.ConfirmDialog;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.netbeans.modules.xml.multiview.ui.SectionContainer;
import org.netbeans.modules.xml.multiview.ui.SectionContainerNode;
import org.netbeans.modules.xml.multiview.ui.SectionPanel;
import org.openide.util.NbBundle;

/**
 * ToolBarMultiView for Hibernate Configuration file
 * 
 * @author Dongmei Cao
 */
public class HibernateCfgToolBarMVElement extends ToolBarMultiViewElement {

    public static final String PROPERTIES = "Properties";
    public static final String JDBC_PROPS = "JDBC Properties";
    public static final String DATASOURCE_PROPS = "Datasource Properties";
    public static final String OPTIONAL_PROPS = "Optional Properties";
    public static final String CONFIGURATION_PROPS = "Configuration Properties";
    public static final String JDBC_CONNECTION_PROPS = "JDBC and Connection Properties";
    public static final String CACHE_PROPS = "Cache Properties";
    public static final String TRANSACTION_PROPS = "Transaction Properties";
    public static final String MISCELLANEOUS_PROPS = "Miscellaneous Properties";
    public static final String MAPPINGS = "Mappings";
    public static final String CLASS_CACHE = "Class Cache";
    public static final String COLLECTION_CACHE = "Collection Cache";
    public static final String CACHE = "Cache";
    public static final String EVENTS = "Events";
    public static final String EVENT = "Event";
    public static final String SECURITY = "Security";
    private ConfigurationView view;
    private ToolBarDesignEditor comp;
    private HibernateCfgDataObject configDataObject;
    private HibernateCfgPanelFactory factory;
    private Project project;
    private Action addEvent,  removeEventAction;

    public HibernateCfgToolBarMVElement(HibernateCfgDataObject dObj) {
        super(dObj);
        this.configDataObject = dObj;
        this.project = FileOwnerQuery.getOwner(dObj.getPrimaryFile());
        addEvent = new AddEventAction(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Add"));
        removeEventAction = new RemoveEventAction(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Remove"));

        comp = new ToolBarDesignEditor();
        factory = new HibernateCfgPanelFactory(comp, dObj);
        setVisualEditor(comp);

    /*repaintingTask = RequestProcessor.getDefault().create(new Runnable() {
    public void run() {
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
    public void run() {
    repaintView();
    }
    });
    }
    });*/
    }

    public SectionView getSectionView() {
        return view;
    }

    @Override
    public void componentOpened() {
        super.componentOpened();

    }

    @Override
    public void componentClosed() {
        super.componentClosed();

    }

    @Override
    public void componentShowing() {
        super.componentShowing();

        // TODO: can have more logic to handle when the view can not be displayed. See Persistence
        view = new ConfigurationView(configDataObject);
        view.initialize();
        comp.setContentView(view);

        Object lastActive = comp.getLastActive();
        if (lastActive != null) {
            view.openPanel(lastActive);
        } else {
            // Expand the first node in session factory if there is one
            Node childrenNodes[] = view.getSessionFactoryContainerNode().getChildren().getNodes();
            if (childrenNodes.length > 0) {
                view.selectNode(childrenNodes[0]);
            }
        }

        view.checkValidity();

    }

    private class ConfigurationView extends SectionView {

        private HibernateCfgDataObject configDataObject;
        private Node securityNode;
        private Node sessionFactoryContainerNode;
        private Node eventsContainerNode;
        private SectionContainer eventsCont;

        ConfigurationView(HibernateCfgDataObject dObj) {
            super(factory);
            configDataObject = dObj;
        }

        public Node getSessionFactoryContainerNode() {
            return this.sessionFactoryContainerNode;

        }

        public Node getEventsContainerNode() {
            return this.eventsContainerNode;
        }
        
        public SectionContainer getEventsContainer() {
            return this.eventsCont;
        }

        public Node getSecurityNode() {
            return this.securityNode;
        }

        /**
         * Initialize the view
         */
        void initialize() {

            HibernateConfiguration configuration = configDataObject.getHibernateConfiguration();

            // Node for JDBC properties
            Node jdbcPropsNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Jdbc_Properties"));
            // Node for data source properties
            Node datasourcePropsNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Datasource_Properties"));
            // Node for optional configuration properties
            Node configPropsNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Hibernate_Configuration_properties"));
            // Node for optional JDBC and Connection properties
            Node jdbcConnPropsNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Jdbc_Connection_Properties"));
            // Node for optional cache properties
            Node cachePropsNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Cache_Properties"));
            // Node for optional transaction properties
            Node transactionPropsNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Transaction_Properties"));
            // Node for optional miscellaneous properties
            Node miscPropsNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Miscellaneous_Properties"));

            // Container node for optional properties
            Children optionalPropsCh = new Children.Array();
            optionalPropsCh.add(new Node[]{configPropsNode, jdbcConnPropsNode, cachePropsNode, transactionPropsNode, miscPropsNode});
            SectionContainerNode optionalPropsContainerNode = new SectionContainerNode(optionalPropsCh);
            optionalPropsContainerNode.setDisplayName(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Optional_Properties"));

            SectionContainer optionalPropsContainer = new SectionContainer(this, optionalPropsContainerNode,
                    NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Optional_Properties"));
            optionalPropsContainer.addSection(new SectionPanel(this, configPropsNode, configPropsNode.getDisplayName(), HibernateCfgToolBarMVElement.CONFIGURATION_PROPS, false, false));
            optionalPropsContainer.addSection(new SectionPanel(this, jdbcConnPropsNode, jdbcConnPropsNode.getDisplayName(), HibernateCfgToolBarMVElement.JDBC_CONNECTION_PROPS, false, false));
            optionalPropsContainer.addSection(new SectionPanel(this, cachePropsNode, cachePropsNode.getDisplayName(), HibernateCfgToolBarMVElement.CACHE_PROPS, false, false));
            optionalPropsContainer.addSection(new SectionPanel(this, transactionPropsNode, transactionPropsNode.getDisplayName(), HibernateCfgToolBarMVElement.TRANSACTION_PROPS, false, false));
            optionalPropsContainer.addSection(new SectionPanel(this, miscPropsNode, miscPropsNode.getDisplayName(), HibernateCfgToolBarMVElement.MISCELLANEOUS_PROPS, false, false));

            // Node for the mappings inside the session-factory
            Node mappingsNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Mappings"));

            // Node for class-cache
            Node classCacheNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Class_Cache"));
            // Node for collection-cache
            Node collectionCacheNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Collection_Cache"));

            // Container Node for the cache inside the session-factory
            Children cacheCh = new Children.Array();
            cacheCh.add(new Node[]{classCacheNode, collectionCacheNode});
            SectionContainerNode cacheContainerNode = new SectionContainerNode(cacheCh);
            cacheContainerNode.setDisplayName(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Cache"));

            SectionContainer cacheCont = new SectionContainer(this, cacheContainerNode,
                    NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Cache"));
            cacheCont.addSection(new SectionPanel(this, classCacheNode, classCacheNode.getDisplayName(), HibernateCfgToolBarMVElement.CLASS_CACHE, false, false));
            cacheCont.addSection(new SectionPanel(this, collectionCacheNode, collectionCacheNode.getDisplayName(), HibernateCfgToolBarMVElement.COLLECTION_CACHE, false, false));

            // Nodes for events. One per event
            Event events[] = configuration.getSessionFactory().getEvent();
            Node eventNodes[] = new Node[events.length];
            for (int i = 0; i < events.length; i++) {
                // Use the event type as the node display name
                String type = events[i].getAttributeValue("Type"); // NOI18N
                eventNodes[i] = new ElementLeafNode(type);
            }
            Children eventsCh = new Children.Array();
            eventsCh.add(eventNodes);

            // Container Node for the events inside the session-factory
            eventsContainerNode = new SectionContainerNode(eventsCh);
            eventsContainerNode.setDisplayName(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Events"));

            eventsCont = new SectionContainer(this, eventsContainerNode,
                    NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Events"));
            eventsCont.setHeaderActions(new javax.swing.Action[]{addEvent});
            SectionPanel panels[] = new SectionPanel[events.length];
            for (int i = 0; i < events.length; i++) {
                panels[i] = new SectionPanel(this, eventNodes[i], eventNodes[i].getDisplayName(), events[i], false, false);
                panels[i].setHeaderActions(new javax.swing.Action[]{removeEventAction});
                eventsCont.addSection(panels[i]);
            }

            // Container Node to contain the session factory child nodes
            Children sessionFactoryCh = new Children.Array();
            sessionFactoryCh.add(new Node[]{jdbcPropsNode, datasourcePropsNode, mappingsNode, cacheContainerNode, eventsContainerNode, optionalPropsContainerNode});
            sessionFactoryContainerNode = new SectionContainerNode(sessionFactoryCh);
            sessionFactoryContainerNode.setDisplayName(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_SessionFactory"));

            SectionContainer sessionFactoryCont = new SectionContainer(this, sessionFactoryContainerNode,
                    NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_SessionFactory"));
            sessionFactoryCont.addSection(new SectionPanel(this, jdbcPropsNode, jdbcPropsNode.getDisplayName(), HibernateCfgToolBarMVElement.JDBC_PROPS, false, false));
            sessionFactoryCont.addSection(new SectionPanel(this, datasourcePropsNode, datasourcePropsNode.getDisplayName(), HibernateCfgToolBarMVElement.DATASOURCE_PROPS, false, false));
            sessionFactoryCont.addSection(new SectionPanel(this, mappingsNode, mappingsNode.getDisplayName(), HibernateCfgToolBarMVElement.MAPPINGS, false, false));
            sessionFactoryCont.addSection(cacheCont);
            sessionFactoryCont.addSection(eventsCont);
            sessionFactoryCont.addSection(optionalPropsContainer);

            // Node for security
            securityNode = new ElementLeafNode(NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Security"));

            // Add the session-factory and security to the root node
            Children rootChildren = new Children.Array();
            rootChildren.add(new Node[]{sessionFactoryContainerNode, securityNode});
            Node root = new AbstractNode(rootChildren);

            // Add sections for the nodes
            addSection(sessionFactoryCont);
            addSection(new SectionPanel(this, securityNode, securityNode.getDisplayName(), HibernateCfgToolBarMVElement.SECURITY, false, false));

            setRoot(root);
        }

        @Override
        public Error validateView() {
            // TODO: valiation code here
            return null;
        }
    }

    private class ElementLeafNode extends org.openide.nodes.AbstractNode {

        ElementLeafNode(String displayName) {
            super(org.openide.nodes.Children.LEAF);
            setDisplayName(displayName);
        }

        @Override
        public HelpCtx getHelpCtx() {
            //return new HelpCtx(HibernateCfgDataObject.HELP_ID_DESIGN_HIBERNATE_CONFIGURATION); //NOI18N
            return null;
        }
    }

    /**
     * For adding a new event in the configuration
     */
    private class AddEventAction extends javax.swing.AbstractAction {

        AddEventAction(String actionName) {
            super(actionName);
        }

        public void actionPerformed(java.awt.event.ActionEvent evt) {

            NewEventPanel dialogPanel = new NewEventPanel();
            EditDialog dialog = new EditDialog(dialogPanel, NbBundle.getMessage(HibernateCfgToolBarMVElement.class, "LBL_Event"), true) {

                protected String validate() {
                    // Nothing to validate
                    return null;
                }
            };

            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);

            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {

                String eventType = dialogPanel.getEventType();
                Event event = new Event();
                event.setAttributeValue("Type", eventType);
                configDataObject.getHibernateConfiguration().getSessionFactory().addEvent(event);
                configDataObject.modelUpdatedFromUI();

                ConfigurationView view = (ConfigurationView) comp.getContentView();
                Node eventNode = new ElementLeafNode(eventType);
                view.getEventsContainerNode().getChildren().add(new Node[]{eventNode});

                SectionPanel pan = new SectionPanel(view, eventNode, eventNode.getDisplayName(), event, false, false);
                pan.setHeaderActions(new javax.swing.Action[]{removeEventAction});
                view.getEventsContainer().addSection(pan, true);
            }
        }
    }

    /**
     * For removing an event from the configuration
     */
    private class RemoveEventAction extends javax.swing.AbstractAction {

        RemoveEventAction(String actionName) {
            super(actionName);
        }

        public void actionPerformed(java.awt.event.ActionEvent evt) {

            SectionPanel sectionPanel = ((SectionPanel.HeaderButton) evt.getSource()).getSectionPanel();
            Event event = (Event) sectionPanel.getKey();
            org.openide.DialogDescriptor desc = new ConfirmDialog(NbBundle.getMessage(HibernateCfgToolBarMVElement.class,
                    "TXT_Remove_Event",
                    event.getAttributeValue("Type"))); // NOI18N
            java.awt.Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(desc);
            dialog.setVisible(true);
            if (org.openide.DialogDescriptor.OK_OPTION.equals(desc.getValue())) {
                sectionPanel.getSectionView().removeSection(sectionPanel.getNode());
                configDataObject.getHibernateConfiguration().getSessionFactory().removeEvent(event);
                configDataObject.modelUpdatedFromUI();
            }
        }
        }
    }
