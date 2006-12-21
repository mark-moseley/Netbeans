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
package org.netbeans.modules.vmd.flow;

import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DesignDocumentAwareness;
import org.netbeans.modules.vmd.api.io.IOUtils;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.api.flow.visual.FlowScene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.vmd.VMDMinimizeAbility;
import org.openide.util.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author David Kaspar
 */
public class FlowViewController implements DesignDocumentAwareness {

    public static final String FLOW_ID = "flow"; // NOI18N

//    private DataObjectContext context;
    private JComponent loadingPanel;
    private JPanel visual;
    private JToolBar toolbar;

    private JButton overviewButton;

    private DesignDocument designDocument;
    private JComponent view;

    public FlowViewController (DataObjectContext context) {
//        this.context = context;
        loadingPanel = IOUtils.createLoadingPanel ();
        visual = new JPanel (new BorderLayout ());
        toolbar = new JToolBar ();
        toolbar.setRollover (true);
        toolbar.setPreferredSize (new Dimension (14, 14));
        toolbar.setSize (new Dimension (14, 14));
        JPanel sep = new JPanel ();
        sep.setSize (10, 10);
        toolbar.add (sep);

        addToolbarButton ("layout", "Overview", new ActionListener() {
            public void actionPerformed (ActionEvent e) {
                layout ();
            }
        });

        overviewButton = addToolbarButton ("overview", "Overview", new ActionListener() {
            public void actionPerformed (ActionEvent e) {
                overview ();
            }
        });

        addToolbarButton ("collapse-all", "Collapse All", new ActionListener () {
            public void actionPerformed (ActionEvent e) {
                collapseAll ();
            }
        });

        addToolbarButton ("expand-all", "Expand All", new ActionListener() {
            public void actionPerformed (ActionEvent e) {
                expandAll ();
            }
        });

        context.addDesignDocumentAwareness (this);
    }

    private JButton addToolbarButton (String imageResourceName, String toolTipText, ActionListener listener) {
        final JButton button = new JButton (new ImageIcon (Utilities.loadImage ("org/netbeans/modules/vmd/flow/resources/" + imageResourceName + ".png")));
        button.setToolTipText (toolTipText);
        button.setBorderPainted (false);
        button.setRolloverEnabled (true);
        button.setSize (14, 14);
        button.addActionListener (listener);
        toolbar.add (button);
        return button;
    }

    private void layout () {
        IOUtils.runInAWTNoBlocking (new Runnable() {
            public void run () {
                FlowAccessController accessController = designDocument != null ? designDocument.getListenerManager ().getAccessController (FlowAccessController.class) : null;
                if (accessController == null)
                    return;
                FlowScene scene = accessController.getScene ();
                scene.layoutScene ();
                scene.validate ();
            }
        });
    }

    private void overview () {
        DesignDocument doc = designDocument;
        FlowAccessController accessController = doc != null ? doc.getListenerManager ().getAccessController (FlowAccessController.class) : null;
        if (accessController == null)
            return;

        JPopupMenu popup = new JPopupMenu ();
        popup.setLayout (new BorderLayout ());
        JComponent sateliteView = accessController.createSatelliteView ();
        popup.add (sateliteView, BorderLayout.CENTER);
        popup.show (overviewButton, (overviewButton.getSize ().width - sateliteView.getPreferredSize ().width) / 2, overviewButton.getSize ().height);
    }

    private void collapseAll () {
        IOUtils.runInAWTNoBlocking (new Runnable() {
            public void run () {
                FlowAccessController accessController = designDocument != null ? designDocument.getListenerManager ().getAccessController (FlowAccessController.class) : null;
                if (accessController == null)
                    return;
                FlowScene scene = accessController.getScene ();
                for (Object object : scene.getObjects ()) {
                    Widget widget = scene.findWidget (object);
                    if (widget instanceof VMDMinimizeAbility)
                        ((VMDMinimizeAbility) widget).collapseWidget ();
                }
            }
        });
    }

    private void expandAll () {
        IOUtils.runInAWTNoBlocking (new Runnable() {
            public void run () {
                FlowAccessController accessController = designDocument != null ? designDocument.getListenerManager ().getAccessController (FlowAccessController.class) : null;
                if (accessController == null)
                    return;
                FlowScene scene = accessController.getScene ();
                for (Object object : scene.getObjects ()) {
                    Widget widget = scene.findWidget (object);
                    if (widget instanceof VMDMinimizeAbility)
                        ((VMDMinimizeAbility) widget).expandWidget ();
                }
            }
        });
    }

    public JComponent getVisualRepresentation () {
        return visual;
    }

    public JComponent getToolbarRepresentation () {
        return toolbar;
    }

    public void setDesignDocument (final DesignDocument newDesignDocument) {
        IOUtils.runInAWTNoBlocking (new Runnable () {
            public void run () {
                designDocument = newDesignDocument;
                FlowAccessController accessController = designDocument != null ? designDocument.getListenerManager ().getAccessController (FlowAccessController.class) : null;
                view = accessController != null ? accessController.getCreateView () : null;

                visual.removeAll ();
                if (view != null) {
                    JScrollPane scroll = new JScrollPane (view);
                    scroll.getHorizontalScrollBar ().setUnitIncrement (64);
                    scroll.getHorizontalScrollBar ().setBlockIncrement (256);
                    scroll.getVerticalScrollBar ().setUnitIncrement (64);
                    scroll.getVerticalScrollBar ().setBlockIncrement (256);
                    visual.add (scroll, BorderLayout.CENTER);
                } else
                    visual.add (loadingPanel, BorderLayout.CENTER);
                visual.validate ();
            }
        });
    }

}
