/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows.view.ui;


import org.openide.windows.TopComponent;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;


/**
 * Interface describing component which is used inside <code>SimpleContainer</code>.
 * There will be at two implementations one for view and second one for editor type.
 *
 * @author  Peter Zavadsky
 */
public interface Tabbed {

    public void addTopComponent(String name, Icon icon, TopComponent tc, String toolTip);

    public void insertComponent(String name, Icon icon, Component comp, String toolTip, int position);
    
    public void setTopComponents(TopComponent[] tcs, TopComponent selected);
    
    public int getTabCount();
    
    public TopComponent[] getTopComponents();
    
    public TopComponent getTopComponentAt(int index);
    
    public int indexOf(Component tc);
    
    public void removeComponent(Component comp);
    
    public void setTitleAt(int index, String title);
    
    public void setIconAt(int index, Icon icon);
    
    public void setToolTipTextAt(int index, String toolTip);
    
    public void setSelectedComponent(Component comp);
    
    public TopComponent getSelectedTopComponent();

    public void addChangeListener(ChangeListener listener);
    
    public void removeChangeListener(ChangeListener listener);

    public void addActionListener (ActionListener al);

    public void removeActionListener (ActionListener al);

    public void setActive(boolean active);
    
    public int tabForCoordinate(Point p);
   
    public Shape getIndicationForLocation(Point location, TopComponent startingTransfer,
            Point startingPoint, boolean attachingPossible);
    
    public Object getConstraintForLocation(Point location, boolean attachingPossible);
    
    public Image createImageOfTab (int tabIndex);

    public String getCommandAtPoint (Point p);
}

