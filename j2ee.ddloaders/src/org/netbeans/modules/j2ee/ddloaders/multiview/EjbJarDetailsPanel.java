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

import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.EjbDetailForm;
import org.netbeans.modules.xml.multiview.ItemEditorHelper;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataObject;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author pfiala
 */
class EjbJarDetailsPanel extends EjbDetailForm {

    private final EjbJar ejbJar;
    private EjbJarMultiViewDataObject dataObject;

    private class LargeIconEditorModel extends TextItemEditorModel {

        public LargeIconEditorModel(XmlMultiViewDataObject dataObject) {
            super(dataObject);
        }

        protected String getValue() {
            return ejbJar.getLargeIcon();
        }

        protected void setValue(String value) {
            ejbJar.setLargeIcon(value);
        }
    }

    private class SmallIconEditorModel extends TextItemEditorModel {

        public SmallIconEditorModel(XmlMultiViewDataObject dataObject) {
            super(dataObject);
        }

        protected boolean validate(String value) {
            return value.length() > 0;
        }

        protected String getValue() {
            return ejbJar.getSmallIcon();
        }

        protected void setValue(String value) {
            ejbJar.setSmallIcon(value);
        }
    }

    private class DescriptionEditorModel extends TextItemEditorModel {

        public DescriptionEditorModel(XmlMultiViewDataObject dataObject) {
            super(dataObject);
        }

        protected boolean validate(String value) {
            return value.length() > 0;
        }

        protected String getValue() {
            return ejbJar.getDefaultDescription();
        }

        protected void setValue(String value) {
            ejbJar.setDescription(value);
        }
    }

    private class DisplayNameEditorModel extends TextItemEditorModel {

        public DisplayNameEditorModel(XmlMultiViewDataObject dataObject) {
            super(dataObject);
        }

        protected boolean validate(String value) {
            if (value.length() > 0) {
                return true;
            } else {
                reloadEditorText();
                return false;
            }
        }

        protected String getValue() {
            return ejbJar.getDefaultDisplayName();
        }

        protected void setValue(String value) {
            ejbJar.setDisplayName(value);
        }
    }

    public EjbJarDetailsPanel(SectionNodeView sectionNodeView, final EjbJar ejbJar) {
        super(sectionNodeView);
        this.dataObject = (EjbJarMultiViewDataObject) sectionNodeView.getDataObject();
        this.ejbJar = ejbJar;
        new ItemEditorHelper(getDisplayNameTextField(), new DisplayNameEditorModel(dataObject));
        new ItemEditorHelper(getDescriptionTextArea(), new DescriptionEditorModel(dataObject));
        new ItemEditorHelper(getSmallIconTextField(), new SmallIconEditorModel(dataObject));
        new ItemEditorHelper(getLargeIconTextField(), new LargeIconEditorModel(dataObject));
        getBrowseLargeIconButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String relativePath = Utils.browseIcon(dataObject);
                if (relativePath != null) {
                    getLargeIconTextField().setText(relativePath);
                }
            }
        });
        getBrowseSmallIconButton().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String relativePath = Utils.browseIcon(dataObject);
                if (relativePath != null) {
                    getSmallIconTextField().setText(relativePath);
                }
            }
        });

    }
}
