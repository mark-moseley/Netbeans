/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.impl;

import java.awt.Component;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.beans.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.netbeans.ide.*;
import com.netbeans.ide.actions.*;
import com.netbeans.ide.explorer.ExplorerPanel;
import com.netbeans.ide.explorer.ExplorerManager;
import com.netbeans.ide.explorer.view.BeanTreeView;
import com.netbeans.ide.explorer.propertysheet.PropertySheetView;
import com.netbeans.ide.awt.SplittedPanel;
import com.netbeans.ide.awt.ToolbarToggleButton;
import com.netbeans.ide.windows.TopComponent;
import com.netbeans.ide.nodes.Node;
import com.netbeans.ide.util.actions.SystemAction;

/** Default explorer which contains toolbar with cut/copy/paste,
* switchable property sheet and menu view actions in the toolbar.
*
* @author Ian Formanek, David Simonek
*/
public final class NbMainExplorer extends TopComponent implements ItemListener {
  /** ExplorerManagers for roots */
  private transient ExplorerManager[] managers;
  private transient ExplorerManager sheetManager;
  private transient ExplorerManager currentManager;
  
  /** Switchable property view panel */
  private transient ExplorerPanel sheetPanel;
  /** Splitted panel containing tree view and property view */
  private transient SplittedPanel split;
  /** Explorer's toolbar */
  private transient JToolBar toolbar;
  /** Explorer's toolbar */
  private transient ToolbarToggleButton sheetSwitcher;
  /** Flag specifying if property sheet is visible */
  private boolean sheetVisible = false;
  /** the width of the property sheet pane */
  private int sheetWidth = 250;
  private int sheetHeight = 400;

  /** Default constructor
  */
  public NbMainExplorer () {
    setName ("Explorer"); // [PENDING]
    split = new SplittedPanel();
    
    final JTabbedPane tabs = new JTabbedPane ();
    tabs.setTabPlacement (SwingConstants.BOTTOM);

    Node[] roots = getRoots ();
    managers = new ExplorerManager[roots.length];
    for (int i = 0; i < roots.length; i++) {
      ExplorerPanel panel = new ExplorerPanel ();
      managers[i] = panel.getExplorerManager ();
      managers[i].setRootContext (roots[i]);
      managers[i].addPropertyChangeListener (new PropertyChangeListener () {
          public void propertyChange (PropertyChangeEvent evt) {
            if ((evt.getSource () == currentManager) && sheetVisible && (sheetManager != null)) {
              if (ExplorerManager.PROP_ROOT_CONTEXT.equals (evt.getPropertyName ())) {
                sheetManager.setRootContext (currentManager.getRootContext ());
              } else if (ExplorerManager.PROP_EXPLORED_CONTEXT.equals (evt.getPropertyName ())) {
                sheetManager.setExploredContext (currentManager.getExploredContext ());
              } else if (ExplorerManager.PROP_SELECTED_NODES.equals (evt.getPropertyName ())) {
                try {
                  sheetManager.setSelectedNodes (currentManager.getSelectedNodes ());
                } catch (PropertyVetoException e) {
                  throw new InternalError ("Property Sheet must not not veto selection");
                }
              }
            }
          }
        }
      );
      BeanTreeView treeView = new BeanTreeView ();
      panel.setLayout (new BorderLayout ());
      panel.add (treeView);
      tabs.addTab (roots[i].getDisplayName (), new ImageIcon (roots [i].getIcon (BeanInfo.ICON_COLOR_16x16)), panel); // [PENDING - hint]
    }
    currentManager = managers[0]; // [PENDING]

    // [PENDING - addIconChangeListener]

    tabs.addChangeListener (new javax.swing.event.ChangeListener () { 
        public void stateChanged (javax.swing.event.ChangeEvent evt) {
          int index = tabs.getSelectedIndex ();
          currentManager = managers[index];
          if (sheetVisible && (sheetManager != null)) {
            sheetManager.setRootContext (currentManager.getRootContext ());
            sheetManager.setExploredContext (currentManager.getExploredContext ());
            try {
              sheetManager.setSelectedNodes (currentManager.getSelectedNodes ());
              } catch (PropertyVetoException e) {
                throw new InternalError ("Property Sheet must not not veto selection");
              }
          }
        }
      }
    );
      
    split.add(tabs, SplittedPanel.ADD_LEFT);
    split.setSplitType(SplittedPanel.HORIZONTAL);
    split.setSplitAbsolute(true);
    
    setLayout(new BorderLayout ());
    add(split, BorderLayout.CENTER);
    add(toolbar = createToolbar(), BorderLayout.NORTH);
    // in single by default
    setMode(TopComponent.Mode.SINGLE);
  }

  private Node[] getRoots () {
    Places.Nodes pn = TopManager.getDefault ().getPlaces ().nodes ();
    Node[] moduleRoots = pn.roots ();
    Node[] roots = new Node[2 + moduleRoots.length];
    roots[0] = pn.projectDesktop ();
    roots[1] = pn.repository ();
    System.arraycopy (moduleRoots, 0, roots, 2, moduleRoots.length);
    return roots;
  }
  
  /** Utility method, creates the explorer's toolbar */
  JToolBar createToolbar () {
    JToolBar result = SystemAction.createToolbarPresenter(
      new SystemAction[] {
        SystemAction.get(CutAction.class),
        SystemAction.get(CopyAction.class),
        SystemAction.get(PasteAction.class),
        null,
        SystemAction.get(DeleteAction.class),
        null
      }
    );
    // property sheet switch action
    ImageIcon icon = new ImageIcon (getClass().getResource(
      "/com/netbeans/developer/impl/resources/actions/properties.gif"));
    sheetSwitcher = new ToolbarToggleButton (icon, sheetVisible);
    sheetSwitcher.setMargin (new java.awt.Insets (2, 0, 1, 0));
    //sheetSwitcher.setToolTipText (Explorer.explorerBundle.getString("ACT_PropertySheet"));
    sheetSwitcher.addItemListener (this);
    result.add (sheetSwitcher);
    result.setBorder(new EmptyBorder(2, 0, 2, 2));
    return result;
  }

  /** Implementation of the ItemListener interface */
  public void itemStateChanged (ItemEvent evt) {
    sheetVisible = sheetSwitcher.isSelected();

    //Component parent = getParent();
    //while (!(parent instanceof Frame)) parent = parent.getParent();
    java.awt.Dimension size = split.getSize ();
    java.awt.Dimension compSize = getSize ();
    int splitType = split.getSplitType ();
    boolean swapped = split.getPanesSwapped();
    if (sheetVisible) { // showing property sheet pane
      if (sheetPanel == null) {
        PropertySheetView propertySheet = new PropertySheetView ();
        sheetPanel = new ExplorerPanel ();
        sheetPanel.add (propertySheet, BorderLayout.CENTER);
        sheetManager = sheetPanel.getExplorerManager ();
        sheetManager.setRootContext (currentManager.getRootContext ());
        sheetManager.setExploredContext (currentManager.getExploredContext ());
        try {
          sheetManager.setSelectedNodes (currentManager.getSelectedNodes ());
        } catch (PropertyVetoException e) {
          throw new InternalError ("Property Sheet must not not veto selection");
        }
      }
      
      int splitPos;
      if (splitType == SplittedPanel.HORIZONTAL) {
        splitPos = swapped ? sheetWidth : size.width;
        compSize.width += sheetWidth;
      } else {
        splitPos = swapped ? sheetHeight : size.height;
        compSize.height += sheetHeight;
      }
      setRequestedSize (compSize);
      split.setSplitPosition (splitPos);
      if (swapped) {
        split.setKeepFirstSame(true);
        split.add(sheetPanel, SplittedPanel.ADD_LEFT);
      } else {
        split.setKeepSecondSame(true);
        split.add(sheetPanel, SplittedPanel.ADD_RIGHT);
      }
    }
    else {              // hiding property sheet pane
      split.remove(sheetPanel);
      int splitPos = split.getSplitPosition ();
      if (splitType == SplittedPanel.HORIZONTAL) {
        sheetWidth = sheetPanel.getSize().width;
        compSize.width -= sheetWidth;
      } else {
        sheetHeight = sheetPanel.getSize().height;
        compSize.height -= sheetHeight;
      }
      setRequestedSize (compSize);
      //split.setSplitPosition (splitPos);
    }
  }

  /** Adds listener to the explorer panel.
  */
  public void open () {
    super.open ();
//    managerListener = new PropL ();
//    explorer.addPropertyChangeListener (managerListener);
//    setActivatedNodes (explorer.getSelectedNodes ());
//    updateTitle ();
  }

  /** Removes listeners.
  */
  public boolean close () {
    super.close ();
//    explorer.removePropertyChangeListener (managerListener);
    return true;
  }

  /** Activates copy/cut/paste actions.
  */
  protected void componentActivated () {
//    if (actions == null) {
//      actions = new ExplorerActions ();
//    }
//    actions.attach (explorer);
  }

  /** Deactivates copy/cut/paste actions.
  */
  protected void componentDeactivated () {
//    actions.detach ();
  }
  

// -----------------------------------------------------------------------------
// Static methods

  /** Static method to obtains the shared instance of NbMainExplorer
  * @return the shared instance of NbMainExplorer
  */
  public static NbMainExplorer getExplorer () {
    if (explorer == null) {
      explorer = new NbMainExplorer ();
    }
    return explorer;
  }
  
  /** Shared instance of NbMainExplorer */
  private static NbMainExplorer explorer;
}

/*
* Log
*  1    Gandalf   1.0         3/14/99  Ian Formanek    
* $
*/
