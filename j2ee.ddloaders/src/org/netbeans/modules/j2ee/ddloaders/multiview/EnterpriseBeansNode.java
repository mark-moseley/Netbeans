/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.BoxPanel;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pfiala
 */
public class EnterpriseBeansNode extends EjbSectionNode {

    protected EnterpriseBeans enterpriseBeans;

    public EnterpriseBeansNode(SectionNodeView sectionNodeView, EnterpriseBeans enterpriseBeans) {
        super(sectionNodeView, enterpriseBeans, Utils.getBundleMessage("LBL_EnterpriseBeans"),
                Utils.ICON_BASE_ENTERPRISE_JAVA_BEANS_NODE);
        this.enterpriseBeans = enterpriseBeans;
        setExpanded(true);
        //getSectionNodePanel().refreshView();
    }

    private SectionNode createNode(Ejb ejb) {
        SectionNodeView sectionNodeView = getSectionNodeView();
        if (ejb instanceof Session) {
            return new SessionNode(sectionNodeView, (Session) ejb);
        } else if (ejb instanceof Entity) {
            return new EntityNode(sectionNodeView, (Entity) ejb);
        } else if (ejb instanceof MessageDriven) {
            return new MessageDrivenNode(sectionNodeView, (MessageDriven) ejb);
        } else {
            return null;
        }
    }

    public SectionInnerPanel createInnerPanel() {
        SectionNodeView sectionNodeView = getSectionNodeView();
        BoxPanel boxPanel = new BoxPanel(sectionNodeView) {
            public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
                if (source == enterpriseBeans) {
                    if (oldValue != null && newValue == null||oldValue== null && newValue!= null) {
                        checkChildren();
                    }
                }
            }
        };
        populateBoxPanel(boxPanel);
        return boxPanel;
    }

    private void checkChildren() {
        Map nodeMap = new HashMap();
        Children children = getChildren();
        Node[] nodes = children.getNodes();
        for (int i = 0; i < nodes.length; i++) {
            Node node = nodes[i];
            nodeMap.put(((SectionNode) node).getKey(), node);
        }
        Ejb[] ejbs = enterpriseBeans.getEjbs();
        // sort beans according to their display name
        Arrays.sort(ejbs, new Comparator() {
            public int compare(Object o1, Object o2) {
                return Utils.getEjbDisplayName((Ejb) o1).compareTo(Utils.getEjbDisplayName((Ejb) o2));
            }
        });
        boolean dirty = nodes.length != ejbs.length;
        Node[] newNodes = new Node[ejbs.length];
        for (int i = 0; i < ejbs.length; i++) {
            Ejb ejb = ejbs[i];
            SectionNode node = (SectionNode) nodeMap.get(ejb);
            if (node == null) {
                node = createNode(ejb);
                dirty = true;
            }
            newNodes[i] = node;
        }
        if (dirty) {
            children.remove(nodes);
            children.add(newNodes);
            populateBoxPanel();
        }
    }

}
