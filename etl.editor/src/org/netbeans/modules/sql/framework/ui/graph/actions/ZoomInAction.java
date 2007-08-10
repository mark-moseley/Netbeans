/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */     

package org.netbeans.modules.sql.framework.ui.graph.actions;

import java.awt.event.ActionEvent;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.netbeans.modules.etl.ui.DataObjectProvider;



import org.netbeans.modules.etl.ui.ETLDataObject;
import org.netbeans.modules.etl.ui.view.ETLCollaborationTopComponent;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.openide.util.NbBundle;


/**
 *
 * @author karthikeyan s
 */
public class ZoomInAction extends GraphAction {
    
    private static final URL zoomInImgUrl = ZoomInAction.class.getResource("/org/netbeans/modules/sql/framework/ui/resources/images/zoom_in_edm.png");
  
    
    public ZoomInAction() {
        //action name
        this.putValue(Action.NAME, NbBundle.getMessage(ZoomInAction.class, "ACTION_ZOOMIN"));

        //action icon
        this.putValue(Action.SMALL_ICON, new ImageIcon(zoomInImgUrl));

        //action tooltip
        this.putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(ZoomInAction.class, "ACTION_ZOOMIN_TOOLTIP"));
    }
  
    
    public void actionPerformed(ActionEvent e) {
         ETLCollaborationTopComponent etlEditor = null;
        try {
            etlEditor = DataObjectProvider.getProvider().getActiveDataObject().getETLEditorTC();
        } catch (Exception ex) {
            // ignore
        }
        if (etlEditor != null) {
             etlEditor.setZoomFactor(etlEditor.getZoomFactor() * 1.1);
        }
    }    
}