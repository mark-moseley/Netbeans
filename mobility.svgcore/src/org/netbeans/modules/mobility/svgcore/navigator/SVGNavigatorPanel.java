/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt. 
  * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */   
package org.netbeans.modules.mobility.svgcore.navigator;

import java.util.Collection;
import javax.swing.JComponent;
import org.netbeans.modules.mobility.svgcore.SVGDataObject;
import org.netbeans.modules.mobility.svgcore.model.SVGFileModel;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;

/** An implementation of NavigatorPanel for XML navigator.
 *
 * @author Pavel Benes (based on the class NavigatorPanel by Marek Fukala)
 * @version 1.0
 */
public class SVGNavigatorPanel implements NavigatorPanel {
    
    private SVGNavigatorContent navigator = SVGNavigatorContent.getDefault();
    
    private Lookup.Result dataObjectSelection;
    //private Lookup.Result elementSelection;
    
    private final LookupListener dataObjectListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            navigate(dataObjectSelection.allInstances());
        }
    };
    
    /*
    private final LookupListener elementSelectionListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            select(elementSelection.allInstances());
        }
    };    
    */
    
    /** public no arg constructor needed for system to instantiate the provider. */
    public SVGNavigatorPanel() {
    }
    
    public String getDisplayHint() {
        return NbBundle.getMessage(SVGNavigatorPanel.class, "SVG_files_navigator");
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(SVGNavigatorPanel.class, "SVG_View");
    }
    
    public JComponent getComponent() {
        return navigator;
    }
    
    public Lookup getLookup() {
        return null;
    }
    
    public void panelActivated(Lookup context) {
        dataObjectSelection = context.lookup(new Lookup.Template<SVGDataObject>(SVGDataObject.class));
        dataObjectSelection.addLookupListener(dataObjectListener);
        dataObjectSelection.allItems();
        dataObjectListener.resultChanged(null);
/*
        elementSelection = context.lookup(new Lookup.Template(SelectionPathBean.class));
        elementSelection.addLookupListener(elementSelectionListener);
        elementSelection.allItems();
        elementSelectionListener.resultChanged(null);
 */
    }
    
    public void panelDeactivated() {
        /*
        elementSelection.removeLookupListener(elementSelectionListener);
        elementSelection = null;
        */
        dataObjectSelection.removeLookupListener(dataObjectListener);
        dataObjectSelection = null;
        navigator.release(); //hide the UI
    }
        
    public void navigate(Collection/*<DataObject>*/ selectedFiles) {
        if (selectedFiles.size() == 1) {
            final SVGDataObject d = (SVGDataObject) selectedFiles.iterator().next();
            //TODO Potential memory leak, use weak reference or something
            d.getModel().addSelectionListener( new SVGFileModel.SelectionListener() {
                public void selectionChanged(String selectedId) {
                    navigator.select(selectedId);
                }
            });
            navigator.navigate(d);        
        }
    }    
    
    /*
    public void select(Collection selectedPath){
        if (selectedPath.size() == 1){
            SelectionPathBean path = (SelectionPathBean) selectedPath.iterator().next();
            navigator.select(path.getActualSelection());
        }
    }
    */
}
