/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 * 
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 * 
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy.operators;

import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import java.util.Hashtable;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SingleSelectionModel;

import javax.swing.plaf.MenuBarUI;

/**
 * <BR><BR>Timeouts used: <BR>
 * JMenuOperator.WaitBeforePopupTimeout - time to sleep before popup expanding <BR>
 * JMenuOperator.WaitPopupTimeout - time to wait popup displayed <BR>
 * ComponentOperator.WaitComponentTimeout - time to wait button displayed <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JMenuBarOperator extends JComponentOperator
    implements Outputable, Timeoutable {

    private TestOut output;
    private Timeouts timeouts;

    /**
     * Constructor.
     */
    public JMenuBarOperator(JMenuBar b) {
	super(b);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param cont Operator pointing a container to search component in.
     * @throws TimeoutExpiredException
     */
    public JMenuBarOperator(ContainerOperator cont) {
	this((JMenuBar)waitComponent(cont, 
				     new JMenuBarFinder(),
				     0));
	copyEnvironment(cont);
    }

    /**
     * Searches JMenuBar in frame.
     */    
    public static JMenuBar findJMenuBar(JFrame frame) {
	return(findJMenuBar((Container)frame));
    }

    /**
     * Searches JMenuBar in dialog.
     */    
    public static JMenuBar findJMenuBar(JDialog dialog) {
	return(findJMenuBar((Container)dialog));
    }

    /**
     * Searches JMenuBar in container.
     * @throws TimeoutExpiredException
     */    
    public static JMenuBar waitJMenuBar(Container cont) {
	return((JMenuBar)waitComponent(cont, new JMenuBarFinder()));
    }

    /**
     * Waits JMenuBar in frame.
     * @throws TimeoutExpiredException
     */    
    public static JMenuBar waitJMenuBar(JFrame frame) {
	return(waitJMenuBar((Container)frame));
    }

    /**
     * Waits JMenuBar in dialog.
     * @throws TimeoutExpiredException
     */    
    public static JMenuBar waitJMenuBar(JDialog dialog) {
	return(waitJMenuBar((Container)dialog));
    }

    /**
     * Waits JMenuBar in container.
     */    
    public static JMenuBar findJMenuBar(Container cont) {
	return((JMenuBar)findComponent(cont, new JMenuBarFinder()));
    }

    /**
     * Defines print output streams or writers.
     * @param out Identify the streams or writers used for print output.
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
     */
    public void setOutput(TestOut out) {
	super.setOutput(out);
	output = out;
    }

    /**
     * Returns print output streams or writers.
     * @return an object that contains references to objects for
     * printing to output and err streams.
     * @see org.netbeans.jemmy.Outputable
     * @see org.netbeans.jemmy.TestOut
     */
    public TestOut getOutput() {
	return(output);
    }

    /**
     * Defines current timeouts.
     * @param times A collection of timeout assignments.
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     */
    public void setTimeouts(Timeouts times) {
	super.setTimeouts(times);
	timeouts = times;
    }

    /**
     * Return current timeouts.
     * @return the collection of current timeout assignments.
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     */
    public Timeouts getTimeouts() {
	return(timeouts);
    }

    /**
     * Pushes menu.
     * @param choosers Array of choosers to find menuItems to push.
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
     */
    public JMenuItem pushMenu(ComponentChooser[] choosers) {
	return(pushMenu(choosers, true));
    }

    /**
     * Executes <code>pushMenu(choosers)</code> in a separate thread.
     * @see #pushMenu(ComponentChooser[])
     */
    public JMenuItem pushMenuNoBlock(ComponentChooser[] choosers) {
	return(pushMenu(choosers, false));
    }

    /**
     * Pushes menu.
     * @param names Menu items texts.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
     */
    public JMenuItem pushMenu(String[] names, boolean ce, boolean ccs) {
	return(pushMenu(names, new DefaultStringComparator(ce, ccs)));
    }

    /**
     * Executes <code>pushMenu(names, ce, ccs)</code> in a separate thread.
     * @see #pushMenu(String[], boolean,boolean)
     */
    public JMenuItem pushMenuNoBlock(String[] names, boolean ce, boolean ccs) {
	return(pushMenuNoBlock(names, new DefaultStringComparator(ce, ccs)));
    }

    /**
     * Pushes menu.
     * @param names Menu items texts.
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
     */
    public JMenuItem pushMenu(String[] names) {
	return(pushMenu(names, getComparator()));
    }

    /**
     * Executes <code>pushMenu(names)</code> in a separate thread.
     * @see #pushMenu(String[])
     */
    public JMenuItem pushMenuNoBlock(String[] names) {
	return(pushMenuNoBlock(names, getComparator()));
    }

    /**
     * Pushes menu.
     * @param path String menupath representation ("File/New", for example).
     * @param delim String menupath divider ("/").
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
     */
    public JMenuItem pushMenu(String path, String delim, boolean ce, boolean ccs) {
	return(pushMenu(parseString(path, delim), ce, ccs));
    }

    /**
     * Executes <code>pushMenu(path, delim, ce, ccs)</code> in a separate thread.
     * @see #pushMenu(String, String, boolean, boolean)
     */
    public JMenuItem pushMenuNoBlock(String path, String delim, boolean ce, boolean ccs) {
	return(pushMenuNoBlock(parseString(path, delim), ce, ccs));
    }

    /**
     * Pushes menu.
     * @param path String menupath representation ("File/New", for example).
     * @param delim String menupath divider ("/").
     * @return Last pushed JMenuItem.
     * @throws TimeoutExpiredException
     */
    public JMenuItem pushMenu(String path, String delim) {
	return(pushMenu(parseString(path, delim)));
    }

    /**
     * Executes <code>pushMenu(path, delim)</code> in a separate thread.
     * @see #pushMenu(String, String)
     */
    public JMenuItem pushMenuNoBlock(String path, String delim) {
	return(pushMenuNoBlock(parseString(path, delim)));
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	String[] items = new String[((JMenuBar)getSource()).getMenuCount()];
	for(int i = 0; i < ((JMenuBar)getSource()).getMenuCount(); i++) {
	    if(((JMenuBar)getSource()).getMenu(i) != null) {
		items[i] = ((JMenuBar)getSource()).getMenu(i).getText();
	    } else {
		items[i] = "null";
	    }
	}
	addToDump(result, "Submenu", items);
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JMenuBar.add(JMenu)</code> through queue*/
    public JMenu add(final JMenu jMenu) {
	return((JMenu)runMapping(new MapAction("add") {
		public Object map() {
		    return(((JMenuBar)getSource()).add(jMenu));
		}}));}

    /**Maps <code>JMenuBar.getComponentIndex(Component)</code> through queue*/
    public int getComponentIndex(final Component component) {
	return(runMapping(new MapIntegerAction("getComponentIndex") {
		public int map() {
		    return(((JMenuBar)getSource()).getComponentIndex(component));
		}}));}

    /**Maps <code>JMenuBar.getHelpMenu()</code> through queue*/
    public JMenu getHelpMenu() {
	return((JMenu)runMapping(new MapAction("getHelpMenu") {
		public Object map() {
		    return(((JMenuBar)getSource()).getHelpMenu());
		}}));}

    /**Maps <code>JMenuBar.getMargin()</code> through queue*/
    public Insets getMargin() {
	return((Insets)runMapping(new MapAction("getMargin") {
		public Object map() {
		    return(((JMenuBar)getSource()).getMargin());
		}}));}

    /**Maps <code>JMenuBar.getMenu(int)</code> through queue*/
    public JMenu getMenu(final int i) {
	return((JMenu)runMapping(new MapAction("getMenu") {
		public Object map() {
		    return(((JMenuBar)getSource()).getMenu(i));
		}}));}

    /**Maps <code>JMenuBar.getMenuCount()</code> through queue*/
    public int getMenuCount() {
	return(runMapping(new MapIntegerAction("getMenuCount") {
		public int map() {
		    return(((JMenuBar)getSource()).getMenuCount());
		}}));}

    /**Maps <code>JMenuBar.getSelectionModel()</code> through queue*/
    public SingleSelectionModel getSelectionModel() {
	return((SingleSelectionModel)runMapping(new MapAction("getSelectionModel") {
		public Object map() {
		    return(((JMenuBar)getSource()).getSelectionModel());
		}}));}

    /**Maps <code>JMenuBar.getSubElements()</code> through queue*/
    public MenuElement[] getSubElements() {
	return((MenuElement[])runMapping(new MapAction("getSubElements") {
		public Object map() {
		    return(((JMenuBar)getSource()).getSubElements());
		}}));}

    /**Maps <code>JMenuBar.getUI()</code> through queue*/
    public MenuBarUI getUI() {
	return((MenuBarUI)runMapping(new MapAction("getUI") {
		public Object map() {
		    return(((JMenuBar)getSource()).getUI());
		}}));}

    /**Maps <code>JMenuBar.isBorderPainted()</code> through queue*/
    public boolean isBorderPainted() {
	return(runMapping(new MapBooleanAction("isBorderPainted") {
		public boolean map() {
		    return(((JMenuBar)getSource()).isBorderPainted());
		}}));}

    /**Maps <code>JMenuBar.isSelected()</code> through queue*/
    public boolean isSelected() {
	return(runMapping(new MapBooleanAction("isSelected") {
		public boolean map() {
		    return(((JMenuBar)getSource()).isSelected());
		}}));}

    /**Maps <code>JMenuBar.menuSelectionChanged(boolean)</code> through queue*/
    public void menuSelectionChanged(final boolean b) {
	runMapping(new MapVoidAction("menuSelectionChanged") {
		public void map() {
		    ((JMenuBar)getSource()).menuSelectionChanged(b);
		}});}

    /**Maps <code>JMenuBar.processKeyEvent(KeyEvent, MenuElement[], MenuSelectionManager)</code> through queue*/
    public void processKeyEvent(final KeyEvent keyEvent, final MenuElement[] menuElement, final MenuSelectionManager menuSelectionManager) {
	runMapping(new MapVoidAction("processKeyEvent") {
		public void map() {
		    ((JMenuBar)getSource()).processKeyEvent(keyEvent, menuElement, menuSelectionManager);
		}});}

    /**Maps <code>JMenuBar.processMouseEvent(MouseEvent, MenuElement[], MenuSelectionManager)</code> through queue*/
    public void processMouseEvent(final MouseEvent mouseEvent, final MenuElement[] menuElement, final MenuSelectionManager menuSelectionManager) {
	runMapping(new MapVoidAction("processMouseEvent") {
		public void map() {
		    ((JMenuBar)getSource()).processMouseEvent(mouseEvent, menuElement, menuSelectionManager);
		}});}

    /**Maps <code>JMenuBar.setBorderPainted(boolean)</code> through queue*/
    public void setBorderPainted(final boolean b) {
	runMapping(new MapVoidAction("setBorderPainted") {
		public void map() {
		    ((JMenuBar)getSource()).setBorderPainted(b);
		}});}

    /**Maps <code>JMenuBar.setHelpMenu(JMenu)</code> through queue*/
    public void setHelpMenu(final JMenu jMenu) {
	runMapping(new MapVoidAction("setHelpMenu") {
		public void map() {
		    ((JMenuBar)getSource()).setHelpMenu(jMenu);
		}});}

    /**Maps <code>JMenuBar.setMargin(Insets)</code> through queue*/
    public void setMargin(final Insets insets) {
	runMapping(new MapVoidAction("setMargin") {
		public void map() {
		    ((JMenuBar)getSource()).setMargin(insets);
		}});}

    /**Maps <code>JMenuBar.setSelected(Component)</code> through queue*/
    public void setSelected(final Component component) {
	runMapping(new MapVoidAction("setSelected") {
		public void map() {
		    ((JMenuBar)getSource()).setSelected(component);
		}});}

    /**Maps <code>JMenuBar.setSelectionModel(SingleSelectionModel)</code> through queue*/
    public void setSelectionModel(final SingleSelectionModel singleSelectionModel) {
	runMapping(new MapVoidAction("setSelectionModel") {
		public void map() {
		    ((JMenuBar)getSource()).setSelectionModel(singleSelectionModel);
		}});}

    /**Maps <code>JMenuBar.setUI(MenuBarUI)</code> through queue*/
    public void setUI(final MenuBarUI menuBarUI) {
	runMapping(new MapVoidAction("setUI") {
		public void map() {
		    ((JMenuBar)getSource()).setUI(menuBarUI);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    private JMenuItem pushMenu(ComponentChooser[] choosers, boolean blocking) {
	makeComponentVisible();
	JMenuItem menuItem = (JMenuItem)(new ComponentSearcher((Container)getSource()).findComponent(choosers[0]));
	if(menuItem instanceof JMenu) {
	    ComponentChooser[] nextChoosers = new ComponentChooser[choosers.length - 1];
	    for(int i = 0; i < choosers.length - 1; i++) {
		nextChoosers[i] = choosers[i+1];
	    }
	    JMenuOperator mo = new JMenuOperator((JMenu)menuItem);
	    mo.copyEnvironment(this);
	    if(blocking) {
		return(mo.pushMenu(nextChoosers));
	    } else {
		return(mo.pushMenuNoBlock(nextChoosers));
	    }
	} else {
	    JMenuItemOperator mio = new JMenuItemOperator(menuItem);
	    output.printLine("Pushing menu " + mio.getText());
	    output.printGolden("Pushing menu " + mio.getText());
	    mio.copyEnvironment(this);
	    if(blocking) {
		mio.push();
	    } else {
		mio.pushNoBlock();
	    }
	    return(menuItem);
	}
    }

    private JMenuItem pushMenu(String[] names, StringComparator comparator) {
	return(pushMenu(JMenuItemOperator.createChoosers(names, comparator)));
    }

    private JMenuItem pushMenuNoBlock(String[] names, StringComparator comparator) {
	return(pushMenuNoBlock(JMenuItemOperator.createChoosers(names, comparator)));
    }

    private static class JMenuBarFinder extends Object implements ComponentChooser {
	public JMenuBarFinder() {
	    super();
	}
	public boolean checkComponent(Component comp) {
	    return(comp instanceof JMenuBar);
	}
	public String getDescription() {
	    return("Menubar");
	}
    }

    private static class JMenuFinder implements ComponentChooser {
	ComponentChooser subFinder;
	public JMenuFinder(ComponentChooser sf) {
	    subFinder = sf;
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JMenu) {
		return(subFinder.checkComponent(comp));
	    }
	    return(false);
	}
	public String getDescription() {
	    return(subFinder.getDescription());
	}
    }
}
