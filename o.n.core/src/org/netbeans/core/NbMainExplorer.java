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

import java.awt.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.beans.*;
import java.text.MessageFormat;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.openide.*;
import org.openide.actions.*;
import org.openide.awt.SplittedPanel;
import org.openide.awt.ToolbarToggleButton;
import org.openide.explorer.*;
import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;

import com.netbeans.developer.impl.output.OutputTab;

/** Default explorer which contains toolbar with cut/copy/paste,
* switchable property sheet and menu view actions in the toolbar.
*
* @author Ian Formanek, David Simonek
*/
public final class NbMainExplorer extends TopComponent implements ItemListener {
  /** The message formatter for Explorer title */
  private static MessageFormat formatExplorerTitle;

  /** The root nodes displayed as tabs - acquired from Places.roots() */
  private transient Node[] roots;
  /** ExplorerManagers for roots */
  private transient ExplorerManager[] managers;
  /** ExplorerManagers for property sheet */
  private transient ExplorerManager sheetManager;
  /** ExplorerManagers of the currently selected root (tab) */
  private transient ExplorerManager currentManager;
  /** Flag for tracking whether the manager/root listeners are added - used when opening/closing */
  private transient boolean listenersRegistered = false;

  /** Listener which tracks changes on the managers for each tab and provides synchronization
  * of rootContext, exploredContext and selectedNodes with property sheet and updating the title of the Explorer */
  private transient PropertyChangeListener managersListener;

  /** Listener which tracks changes on the root nodes (which are displayed as tabs) */
  private transient PropertyChangeListener rootsListener;

  /** action handler for cut/copy/paste/delete */
  private static ExplorerActions actions;

  /** Boolean flag - true, if this component is currently activated (and attached to ExplorerActions), false otherwise */
  private boolean activated = false;

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
  /** the default width of the property sheet pane */
  private int sheetWidth = 250;
  /** the default height of the property sheet pane */
  private int sheetHeight = 400;
  /** Minimal initial height of this top component */
  public static final int MIN_HEIGHT = 150;
  /** Default width of main explorer */
  public static final int DEFAULT_WIDTH = 350;

  /** Default constructor
  */
  public NbMainExplorer () {
    split = new SplittedPanel();

    final JTabbedPane tabs = new JTabbedPane ();
    tabs.setTabPlacement (SwingConstants.BOTTOM);

    managersListener = new PropertyChangeListener () {
      public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getSource () == currentManager) {
          if (sheetVisible && (sheetManager != null)) {
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
          setActivatedNodes (currentManager.getSelectedNodes ());
          updateTitle ();
        }
      }
    };

    rootsListener = new PropertyChangeListener () {
      public void propertyChange (PropertyChangeEvent evt) {
        for (int i = 0; i < roots.length; i++) {
          if (roots[i] == evt.getSource ()) {
            if (Node.PROP_DISPLAY_NAME.equals (evt.getPropertyName ())) {
              tabs.setTitleAt (i, roots[i].getDisplayName ());
            } else if (Node.PROP_ICON.equals (evt.getPropertyName ())) {
              tabs.setIconAt (i, new ImageIcon (roots [i].getIcon (BeanInfo.ICON_COLOR_16x16)));
            }
/* [IAN] - this is just waiting for Sun to fix bug #4158286 : no way to change ToolTip text on tabs in JTabbedPane
            else if (PROP_SHORT_DESCRIPTION.equals (evt.getPropertyName ())) {
              tabs.setTooltipAt (i, roots[i].getShortDescription ());
            } */
            break;
          }
        }
      }
    };


    roots = getRoots ();
    managers = new ExplorerManager[roots.length];
    for (int i = 0; i < roots.length; i++) {
      ExplorerPanel panel = new ExplorerPanel ();
      managers[i] = panel.getExplorerManager ();
      managers[i].setRootContext (roots[i]);
      BeanTreeView treeView = new BeanTreeView ();
      panel.setLayout (new BorderLayout ());
      panel.add (treeView);
      tabs.addTab (roots[i].getDisplayName (), new ImageIcon (roots [i].getIcon (BeanInfo.ICON_COLOR_16x16)), panel, roots[i].getShortDescription ());
    }
    currentManager = managers[0]; // [PENDING]

    tabs.addChangeListener (new javax.swing.event.ChangeListener () {
        public void stateChanged (javax.swing.event.ChangeEvent evt) {
          int index = tabs.getSelectedIndex ();
          currentManager = managers[index];
          if (activated) {
            actions.attach (currentManager);
          }
          if (sheetVisible && (sheetManager != null)) {
            sheetManager.setRootContext (currentManager.getRootContext ());
            sheetManager.setExploredContext (currentManager.getExploredContext ());
            try {
              sheetManager.setSelectedNodes (currentManager.getSelectedNodes ());
              } catch (PropertyVetoException e) {
                throw new InternalError ("Property Sheet must not not veto selection");
              }
          }
          updateTitle ();
          setActivatedNodes (currentManager.getSelectedNodes ());
        }
      }
    );

    split.add(tabs, SplittedPanel.ADD_LEFT);
    split.setSplitType(SplittedPanel.HORIZONTAL);
    split.setSplitAbsolute(true);

    setLayout(new BorderLayout ());
    add(split, BorderLayout.CENTER);
    add(toolbar = createToolbar(), BorderLayout.NORTH);

    updateTitle ();
  }

  /** Rarely used, only when fresh Expl created & no components selected. */
  public HelpCtx getHelpCtx () {
    return new HelpCtx (NbMainExplorer.class);
  }

  private Node[] getRoots () {
    Places.Nodes ns = TopManager.getDefault ().getPlaces ().nodes ();
    Node[] moduleRoots = ns.roots ();

//    Node[] roots = new Node[2 + moduleRoots.length];
//    roots[0] = ns.projectDesktop ();
//    roots[1] = ns.repository ();
//    System.arraycopy (moduleRoots, 0, roots, 2, moduleRoots.length);

    Node[] roots = new Node[1 + moduleRoots.length + 3];
    roots[0] = ns.repository ();
    System.arraycopy (moduleRoots, 0, roots, 1, moduleRoots.length);
    roots[roots.length - 3] = DesktopNode.createEnvironmentNode ();
    roots[roots.length - 2] = DesktopNode.createProjectSettingsNode ();
    roots[roots.length - 1] = DesktopNode.createSessionNode ();
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
    sheetSwitcher.setToolTipText (NbBundle.getBundle (NbMainExplorer.class).getString ("CTL_ToggleProperties"));
    sheetSwitcher.addItemListener (this);
    result.add (sheetSwitcher);
    result.setBorder(new EmptyBorder(2, 0, 2, 2));
    result.setFloatable (false);
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
      getSheetPanel();  
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

  private void setRequestedSize(Dimension dim) {
    Workspace ws = TopManager.getDefault().getWindowManager().
                   getCurrentWorkspace();
    if (ws != null) {
      Mode mode = ws.findMode(this);
      if (mode != null) {
        Rectangle bounds = mode.getBounds();
        bounds.width = dim.width;
        bounds.height = dim.height;
        mode.setBounds(bounds);
      }
    }
  }

  private void updateTitle () {
    String name = currentManager.getExploredContext().getDisplayName();
    if (name == null) {
      name = "";
    }
    if (formatExplorerTitle == null) {
      formatExplorerTitle = new MessageFormat (
        NbBundle.getBundle (NbMainExplorer.class).getString ("FMT_MainExplorerTitle")
      );
    }
    setName(formatExplorerTitle.format (
      new Object[] { name }
    ));
  }

  /** Adds listener to the explorer panel.
  */
  public void open () {
    super.open ();
    if (!listenersRegistered) {
      for (int i = 0; i < managers.length; i++) {
        managers[i].addPropertyChangeListener (managersListener); // synchronization of property sheet, activated nodes, title
      }
      // add listeners to changes on the roots
      for (int i = 0; i < roots.length; i++) {
        roots[i].addPropertyChangeListener (rootsListener);
      }
      listenersRegistered = true;
    }
    setActivatedNodes (currentManager.getSelectedNodes ());
    updateTitle ();
  }

  /** Removes listeners.
  */
  public boolean canClose () {
    boolean result = super.canClose();
    if (result) {
      for (int i = 0; i < managers.length; i++) {
        managers[i].removePropertyChangeListener (managersListener);
      }
      for (int i = 0; i < roots.length; i++) {
       roots[i].removePropertyChangeListener (rootsListener);
      }
      listenersRegistered = false;
    }
    return result;
  }

  /** Activates copy/cut/paste actions.
  */
  protected void componentActivated () {
    if (actions == null) {
      actions = new ExplorerActions ();
    }
    actions.attach (currentManager);
    activated = true;
  }

  /** Deactivates copy/cut/paste actions.
  */
  protected void componentDeactivated () {
    activated = false;
    actions.detach ();
  }

  /** Serialize this top component.
  * @param in the stream to serialize to
  */
  public void writeExternal (ObjectOutput out)
              throws IOException {
    super.writeExternal(out);
    out.writeObject(new Boolean(sheetVisible));
    out.writeObject(new Boolean(split.getPanesSwapped()));
    out.writeObject(new Integer(split.getSplitPosition()));
    out.writeObject(new Integer(split.getSplitType()));
  }
  
  
  /** Deserialize this top component, sets as default.
  * @param in the stream to deserialize from
  */
  public void readExternal (ObjectInput in)
              throws IOException, ClassNotFoundException {
    super.readExternal(in);
    sheetVisible = ((Boolean)in.readObject()).booleanValue();
    boolean swapped = ((Boolean)in.readObject()).booleanValue();
    split.setSplitPosition(((Integer)in.readObject()).intValue());
    split.setSplitType(((Integer)in.readObject()).intValue());
    if (sheetVisible) {
      //split.setKeepFirstSame(true);
      split.add(getSheetPanel(), SplittedPanel.ADD_RIGHT);
      if (swapped)
        split.swapPanes();
    }
    // toggle button (do without listening)
    sheetSwitcher.removeItemListener (this);
    sheetSwitcher.setSelected(sheetVisible); 
    sheetSwitcher.addItemListener (this);
    explorer = this;
  }

  /** Safe getter for sheet panel */
  private ExplorerPanel getSheetPanel () {
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
    return sheetPanel;
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
*  19   Gandalf   1.18        7/19/99  Jesse Glick     Context help.
*  18   Gandalf   1.17        7/16/99  Ian Formanek    Fixed bug #1800 - You can
*       drag off the explorer toolbar. 
*  17   Gandalf   1.16        7/15/99  Ian Formanek    Swapped Global and 
*       Project settings tabs
*  16   Gandalf   1.15        7/13/99  Ian Formanek    New MainExplorer tabs 
*       (usability&intuitiveness discussion results)
*  15   Gandalf   1.14        7/12/99  Jesse Glick     Context help.
*  14   Gandalf   1.13        7/11/99  David Simonek   window system change...
*  13   Gandalf   1.12        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  12   Gandalf   1.11        5/30/99  Ian Formanek    Fixed bug 1647 - Open, 
*       Compile, Rename, Execute and  etc. actions in popup menu in explorer are
*       sometimes disabled.  Fixed bug 1971 - If the tab is switched from 
*       Desktop to Repository with some nodes already selected, the actions in 
*       popupmenu might not be correctly enabled.  Fixed bug 1616 - Property 
*       sheet button in explorer has no tooltip.
*  11   Gandalf   1.10        5/15/99  David Simonek   switchable sheet 
*       serialized properly.....finally
*  10   Gandalf   1.9         5/14/99  David Simonek   serialization of 
*       switchable sheet state
*  9    Gandalf   1.8         5/11/99  David Simonek   changes to made window 
*       system correctly serializable
*  8    Gandalf   1.7         3/25/99  David Simonek   another small changes in 
*       window system
*  7    Gandalf   1.6         3/25/99  David Simonek   changes in window system,
*       initial positions, bugfixes
*  6    Gandalf   1.5         3/18/99  Ian Formanek    The title now updates 
*       when tab is switched
*  5    Gandalf   1.4         3/16/99  Ian Formanek    SINGLE mode removed, as 
*       it is there by default
*  4    Gandalf   1.3         3/16/99  Ian Formanek    Title improved
*  3    Gandalf   1.2         3/16/99  Ian Formanek    Added listening to icon 
*       and displayName changes on roots, support for ExplorerActions 
*       (Cut/Copy/...)
*  2    Gandalf   1.1         3/15/99  Ian Formanek    Added formatting of 
*       title, updating activatedNodes
*  1    Gandalf   1.0         3/14/99  Ian Formanek    
* $
*/
