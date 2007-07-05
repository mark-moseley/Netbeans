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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping;
import org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNode;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDSectionNodeView;
import org.netbeans.modules.j2ee.sun.share.configbean.SunONEDeploymentConfiguration;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.ui.BoxPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodePanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Mutex;
import org.openide.util.RequestProcessor;


/**
 * @author Peter Williams
 */
public abstract class NamedBeanGroupNode extends BaseSectionNode implements BeanResolver, DescriptorReader {

    public static final String STANDARD_SERVLET_NAME = Servlet.SERVLET_NAME; // e.g. "ServletName"
    public static final String STANDARD_EJB_NAME = Ejb.EJB_NAME; // e.g. "EjbName"
    public static final String STANDARD_EJB_REF_NAME = EjbRef.EJB_REF_NAME; // e.g. "EjbRefName"
    public static final String STANDARD_RES_REF_NAME = ResourceRef.RES_REF_NAME; // e.g. "ResourceRefName"
    public static final String STANDARD_RESOURCE_ENV_REF_NAME = ResourceEnvRef.RESOURCE_ENV_REF_NAME; // e.g. "ResourceEnvRefName"
    public static final String STANDARD_SERVICE_REF_NAME = ServiceRef.SERVICE_REF_NAME; // e.g. "ServiceRefName"
    public static final String STANDARD_ROLE_NAME = SecurityRoleMapping.ROLE_NAME; // e.g. "RoleName"
    public static final String STANDARD_PORT_NAME = WebserviceDescription.WEBSERVICE_DESCRIPTION_NAME; // e.g. "WebserviceDescriptionName"
    public static final String STANDARD_WEBSERVICE_DESC_NAME = WebserviceDescription.WEBSERVICE_DESCRIPTION_NAME; // e.g. "WebserviceDescriptionName"
    public static final String STANDARD_MSGDEST_NAME = MessageDestination.MESSAGE_DESTINATION_NAME; // e.g. "MessageDestination"
    public static final String STANDARD_MSGDEST_REF_NAME = MessageDestinationRef.MESSAGE_DESTINATION_REF_NAME; // e.g. "MessageDestinationRef"
    
    private static RequestProcessor processor = new RequestProcessor("SunDDNodeBuilder", 1);
    
    protected CommonDDBean commonDD;
    private String beanNameProperty;
    private AddBeanAction addBeanAction;
    
    private volatile boolean doCheck = false;
    private volatile boolean checking = false;

    private static AtomicInteger newBeanId = new AtomicInteger(1);
    
    public NamedBeanGroupNode(SectionNodeView sectionNodeView, CommonDDBean commonDD,
            String beanNameProperty, String header, String iconBase,
            ASDDVersion version) {
        super(sectionNodeView, new NamedChildren(), commonDD, version, header, iconBase);
        
        this.commonDD = commonDD;
        this.beanNameProperty = beanNameProperty;
        
        setExpanded(true);
    }
    
    /** Expected to be called from derived class constructor, if needed.
     */
    protected void enableAddAction(String addActionTitle) {
        addBeanAction = new AddBeanAction(addActionTitle);
    }
    
    protected abstract SectionNode createNode(DDBinding binding);
    
    protected abstract CommonDDBean [] getBeansFromModel();
    
    protected abstract CommonDDBean addNewBean();

    protected abstract CommonDDBean addBean(CommonDDBean newBean);
    
    protected abstract void removeBean(CommonDDBean bean);
    
    
    @Override
    public SectionNodeInnerPanel createInnerPanel() {
        SectionNodeView sectionNodeView = getSectionNodeView();
        BoxPanel boxPanel = new BoxPanel(sectionNodeView) {
            @Override
            public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
                if(source == commonDD) {
                    if(oldValue != null && newValue == null || oldValue == null && newValue != null) {
                        checkChildren(null);
                    }
                }
            }
            
            @Override
            public void refreshView() {
                checkChildren(null);
            }
        };
        populateBoxPanel(boxPanel);
        return boxPanel;
    }
    
    @Override
    public SectionNodePanel getSectionNodePanel() {
        SectionNodePanel nodePanel = super.getSectionNodePanel();
        if(addBeanAction != null && nodePanel.getHeaderButtons() == null) {
            nodePanel.setHeaderActions(new Action [] { addBeanAction });
        }
        return nodePanel;
    }
    
    @Override
    protected SectionNodeInnerPanel createNodeInnerPanel() {
        SectionNodeInnerPanel innerPanel = super.createNodeInnerPanel();
        return innerPanel;
    }
    
    public void checkChildren(final CommonDDBean focusBean) {
        processor.post(new Runnable() {
            public void run() {
                // Compute dataset
                final SortedSet<DDBinding> bindingDataSet = computeBindingSet();
                
                // Notify AWT for UI update.
                Mutex.EVENT.readAccess(new Runnable() {
                    public void run() {
                        doCheck = true;
                        if (setChecking(true)) {
                            try {
                                while (doCheck) {
                                    doCheck = false;
                                    check(focusBean, bindingDataSet);
                                }
                            } finally {
                                setChecking(false);
                            }
                        }
                    }
                });
            }
        });
    }
    
    // !PW FIXME was private, change back soon
    protected synchronized boolean setChecking(boolean value) {
        if (value) {
            if (checking) {
                return false;
            } else {
                checking = true;
                return true;
            }
        } else {
            checking = false;
            return true;
        }
    }

    /** This method actually updates the display nodes in the UI.  It should not
     *  do any slow calculation and should only be called on AWT thread.
     */
    protected void check(final CommonDDBean focusBean, SortedSet<DDBinding> bindingDataSet) {
        assert SwingUtilities.isEventDispatchThread();
        
        Map<Object, Node> nodeMap = new HashMap<Object, Node>();
        Children children = getChildren();
        Node[] nodes = children.getNodes();
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            nodeMap.put(((SectionNode) node).getKey(), node);
            
            if(node instanceof NamedBeanNode) {
                NamedBeanNode nbn = (NamedBeanNode) node;
                DDBinding nodeBinding = nbn.getBinding();
                
                if(!nodeBinding.isVirtual()) {
                    // Nonvirtual nodes should alread be handled... 
                    // (barring obscure race conditions at least.)
                    continue;
                }
                
                CommonDDBean nodeSunBean = nodeBinding.getSunBean();
                
                // Attempt to normalize sunbeans for virtual nodes.
                // *** N^2 algorithm - find a better way...
                for(DDBinding candidate: bindingDataSet) {
                    CommonDDBean candidateSunBean = candidate.getSunBean();
                    if(nodeSunBean == candidateSunBean) {
                        break; 
                    }

                    if(!candidate.isVirtual()) {
                        continue;
                    }
                    
                    if(Utils.strEquivalent(nodeBinding.getBeanName(), candidate.getBeanName())) {
                        DDBinding replacement = candidate.rebind(nodeSunBean);
                        bindingDataSet.remove(candidate);
                        bindingDataSet.add(replacement);
                        break;
                    }
                }
            }
        }
       
        // !PW Optimization - How to match virtual servlets from prior pass with virtual servlets from this pass?
        // Currently their keys will always be created new.  Can we look them up?
        
        SectionNode focusNode = null;
        boolean dirty = nodes.length != bindingDataSet.size();
        List<Node> newNodeList = new ArrayList<Node>(bindingDataSet.size());
        
        int index = 0;
        Iterator<DDBinding> setIter = bindingDataSet.iterator();
        while(setIter.hasNext()) {
            DDBinding binding = setIter.next();
            SectionNode node = (SectionNode) nodeMap.get(binding.getSunBean());
            // if the node is null (didn't exist before) or the node has different binding, then (re)create it.
            if(node == null || (node instanceof NamedBeanNode && !binding.equals(((NamedBeanNode) node).getBinding()))) {
                node = createNode(binding);
                dirty = true;
            }
            newNodeList.add(node);
            if(!dirty) {
                dirty = ((SectionNode) nodes[index]).getKey() != node.getKey();
            }
            if(binding.getSunBean() == focusBean) {
                focusNode = node;
            }
            index++;
        }
        
        if (dirty) {
            Node [] newNodes = newNodeList.toArray(new Node[newNodeList.size()]);
            children.remove(nodes);
            children.add(newNodes);
            populateBoxPanel();
        }
        
        if(focusBean != null && focusNode != null) {
            SectionNodePanel nodePanel = focusNode.getSectionNodePanel();
            nodePanel.open();
            nodePanel.scroll();
            nodePanel.setActive(true);
        }
    }
    
    protected SortedSet<DDBinding> computeBindingSet() {
        CommonDDBean [] sunBeans = getBeansFromModel();
        Map<String, Object> stdBeanPropertyMap = readDescriptor();
        Map<String, Object> annotationPropertyMap = readAnnotations();
        
        return computeBindingSet(sunBeans, stdBeanPropertyMap, annotationPropertyMap);
    }
    
    @SuppressWarnings("unchecked")
    protected SortedSet<DDBinding> computeBindingSet(CommonDDBean [] sunBeans, 
            Map<String, Object> stdBeanPropertyMap, Map<String, Object> annotationPropertyMap) {
        SortedSet<DDBinding> bindingDataSet = new TreeSet<DDBinding>();

        // Match up like names
        if(sunBeans != null) {
            for(CommonDDBean sunBean: sunBeans) {
                String beanName = getBeanName(sunBean);
                beanName = (beanName != null) ? beanName.trim() : beanName;

                Map<String, Object> stdBeanProperties = null;
                if(stdBeanPropertyMap != null) {
                    Object value = stdBeanPropertyMap.get(beanName);
                    if(value != null) {
                        if(value instanceof Map<?, ?>) {
                            stdBeanProperties = (Map<String, Object>) value;
                            stdBeanPropertyMap.remove(beanName);
                        }
                    }
                }

                Map<String, Object> annotationProperties = null;
                if(annotationPropertyMap != null) {
                    Object value = annotationPropertyMap.get(beanName);
                    if(value != null) {
                        if(value instanceof Map<?, ?>) {
                            annotationProperties = (Map<String, Object>) value;
                            annotationPropertyMap.remove(beanName);
                        }
                    }
                }

                DDBinding binding = new DDBinding(this, sunBean, stdBeanProperties, annotationProperties, false);
                bindingDataSet.add(binding);
            }
        }
        
        // Add dummy entries for all unmatched standard servlets (unmatched sun servlets were added previous step)
        if(stdBeanPropertyMap != null) {
            Set<Map.Entry<String, Object>> entrySet = stdBeanPropertyMap.entrySet();
            for(Map.Entry<String, Object> entry: entrySet) {
                String beanName = entry.getKey();
                Object value = entry.getValue();
                if(value != null) {
                    if(value instanceof Map<?, ?>) {
                        Map<String, Object> stdBeanProperties = (Map<String, Object>) value;
                        CommonDDBean newSunBean = createBean();
                        setBeanName(newSunBean, beanName);

                        Map<String, Object> annotationProperties = null;
                        if(annotationPropertyMap != null) {
                            value = annotationPropertyMap.get(beanName);
                            if(value != null) {
                                if(value instanceof Map<?, ?>) {
                                    annotationProperties = (Map<String, Object>) value;
                                    annotationPropertyMap.remove(beanName);
                                }
                            }
                        }

                        DDBinding binding = new DDBinding(this, newSunBean, stdBeanProperties, annotationProperties, true);
                        bindingDataSet.add(binding);
                    }
                }
            }
        }

        // Add dummy entries for all unmatched standard servlets (unmatched sun servlets were added previous step)
        if(annotationPropertyMap != null) {
            Set<Map.Entry<String, Object>> entrySet = annotationPropertyMap.entrySet();
            for(Map.Entry<String, Object> entry: entrySet) {
                String beanName = entry.getKey();
                Object value = entry.getValue();
                if(value != null) {
                    if(value instanceof Map<?, ?>) {
                        Map<String, Object> annotationProperties = (Map<String, Object>) value;
                        CommonDDBean newSunBean = createBean();
                        setBeanName(newSunBean, beanName);

                        DDBinding binding = new DDBinding(this, newSunBean, null, annotationProperties, true);
                        bindingDataSet.add(binding);
                    }
                }
            }
        }
        
        // !PW FIXME Mix annotations into previous calculations if we had them (none for servlet, but what about @RunAs?)
        // Possibly only consider annotations on bound servlets?  Then we can look at specific servlet class for annotations
        // using <servlet-class> field in standard descriptor.
        
        return bindingDataSet;
    }
    
    protected <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        MetadataModel<T> metadataModel = null;
        SectionNodeView view = getSectionNodeView();
        XmlMultiViewDataObject dObj = view.getDataObject();
        SunONEDeploymentConfiguration dc = SunONEDeploymentConfiguration.getConfiguration(
                FileUtil.toFile(dObj.getPrimaryFile()));
        if(dc != null) {
            metadataModel = dc.getMetadataModel(type);
        }
        return metadataModel;
    }

    protected org.netbeans.modules.j2ee.dd.api.common.RootInterface getStandardRootDD() {
        org.netbeans.modules.j2ee.dd.api.common.RootInterface stdRootDD = null;
        SectionNodeView view = getSectionNodeView();
        XmlMultiViewDataObject dObj = view.getDataObject();
        SunONEDeploymentConfiguration dc = SunONEDeploymentConfiguration.getConfiguration(
                FileUtil.toFile(dObj.getPrimaryFile()));
        if(dc != null) {
            stdRootDD = dc.getStandardRootDD();
        }
        return stdRootDD;
    }

    protected org.netbeans.modules.j2ee.dd.api.common.RootInterface getWebServicesRootDD() {
        org.netbeans.modules.j2ee.dd.api.common.RootInterface wsRootDD = null;
        SectionNodeView view = getSectionNodeView();
        XmlMultiViewDataObject dObj = view.getDataObject();
        SunONEDeploymentConfiguration dc = SunONEDeploymentConfiguration.getConfiguration(
                FileUtil.toFile(dObj.getPrimaryFile()));
        if(dc != null) {
            wsRootDD = dc.getWebServicesRootDD();
        }
        return wsRootDD;
    }
    
    public int getNewBeanId() {
        return newBeanId.getAndIncrement();
    }
    
    // ------------------------------------------------------------------------
    // DescriptorReader implementation
    // ------------------------------------------------------------------------
    public Map<String, Object> readDescriptor() {
        CommonBeanReader reader = getAnnotationReader();
        return reader != null ? reader.readDescriptor(getStandardRootDD()) : null;
    }

    public Map<String, Object> readAnnotations() {
        Map<String, Object> result = null;
        CommonBeanReader reader = getAnnotationReader();
        if(reader != null) {
            SectionNodeView view = getSectionNodeView();
            XmlMultiViewDataObject dObj = view.getDataObject();
            result = reader.readAnnotations(dObj);
        }
        return result;
    }
    
    protected CommonBeanReader getAnnotationReader() {
        return null;
    }
    
    /** Determines the name of the parent node, if any.  For example, determines the
     *  name of an ejb that an resource-ref or other named reference is embedded in.
     */
    protected String getParentNodeName() {
        String parentName = null;
        Node parentNode = getParentNode();
        if(parentNode instanceof NamedBeanNode) {
            DDBinding binding = ((NamedBeanNode) parentNode).getBinding();
            if(binding != null) {
                parentName = binding.getBindingName();
                if(parentName.length() == 0) {
                    parentName = null;
                }
            }
        }
        return parentName;
    }
    
    /** -----------------------------------------------------------------------
     * AddBeanAction implementation
     */
    public final class AddBeanAction extends AbstractAction {
        
        public AddBeanAction(String actionText) {
            super(actionText);
//            char mnem = NbBundle.getMessage(PortInfoGroupNode.class, "MNE_Add" + resourceBase).charAt(0);
//            putValue(MNEMONIC_KEY, Integer.valueOf(mnem));
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            SectionNodeView view = getSectionNodeView();
            if(view instanceof DDSectionNodeView) {
                XmlMultiViewDataObject dObj = ((DDSectionNodeView) view).getDataObject();
                if(dObj instanceof SunDescriptorDataObject) {
                    SunDescriptorDataObject sunDO = (SunDescriptorDataObject) dObj;
                    sunDO.modelUpdatedFromUI();
                    // dataObject.setChangedFromUI(true);
                    
                    CommonDDBean newBean = addNewBean();
                    Node parent = getParentNode();
                    if(parent instanceof NamedBeanNode) { // ejb parent node
                        ((NamedBeanNode) getParentNode()).addVirtualBean();
                    }
                    checkChildren(newBean);
                }
            }
        }
    }
    
    public static class NamedChildren extends Children.SortedMap<DDBinding> implements Comparator<Node> {

        public NamedChildren() {
            setComparator(this);
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            Node n = getNode();
            if(n instanceof NamedBeanGroupNode) {
                NamedBeanGroupNode owner = (NamedBeanGroupNode) n;
                owner.checkChildren(null);
            }
        }
        
        @Override
        public boolean add(Node[] arr) {
            java.util.Map<DDBinding, Node> nodeMap = arrayToMap(arr);
            this.putAll(nodeMap);
            return true;
        }

        @Override
        public boolean remove(Node[] arr) {
            java.util.Map<DDBinding, Node> nodeMap = arrayToMap(arr);
            this.removeAll(nodeMap.keySet());
            return true;
        }

        private java.util.Map<DDBinding, Node> arrayToMap(Node [] arr) {
            java.util.Map<DDBinding, Node> nodeMap = new HashMap<DDBinding, Node>();
            for(Node n: arr) {
                if(n instanceof NamedBeanNode) {
                    NamedBeanNode node = (NamedBeanNode) n;
                    nodeMap.put(node.getBinding(), node);
                }
            }
            return nodeMap;
        }

        public int compare(Node n1, Node n2) {
            NamedBeanNode node1 = (NamedBeanNode) n1;
            NamedBeanNode node2 = (NamedBeanNode) n2;
            return node1.getBinding().compareTo(node2.getBinding());
        }
    }
}
