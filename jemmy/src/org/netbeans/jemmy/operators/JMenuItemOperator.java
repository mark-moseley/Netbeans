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

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Outputable;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.Timeoutable;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import java.awt.Component;
import java.awt.Container;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import java.util.Hashtable;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;

import javax.swing.event.MenuDragMouseEvent;
import javax.swing.event.MenuDragMouseListener;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;

import javax.swing.plaf.MenuItemUI;

/**
 *
 * <BR><BR>Timeouts used: <BR>
 * JMenuItemOperator.PushMenuTimeout - time between button pressing and releasing<BR>
 * ComponentOperator.WaitComponentTimeout - time to wait button displayed <BR>
 * ComponentOperator.WaitComponentEnabledTimeout - time to wait button enabled <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class JMenuItemOperator extends AbstractButtonOperator 
implements Timeoutable, Outputable{

    private final static long PUSH_MENU_TIMEOUT = 0;

    private Timeouts timeouts;
    private TestOut output;

    /**
     * Constructor.
     */
    public JMenuItemOperator(JMenuItem item) {
	super(item);
	setTimeouts(JemmyProperties.getProperties().getTimeouts());
	setOutput(JemmyProperties.getProperties().getOutput());
    }

    public JMenuItemOperator(ContainerOperator cont, ComponentChooser chooser, int index) {
	this((JMenuItem)cont.
             waitSubComponent(new JMenuItemFinder(chooser),
                              index));
	copyEnvironment(cont);
    }

    public JMenuItemOperator(ContainerOperator cont, ComponentChooser chooser) {
	this(cont, chooser, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param text Button text. 
     * @param index Ordinal component index.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public JMenuItemOperator(ContainerOperator cont, String text, int index) {
	this((JMenuItem)waitComponent(cont, 
				      new JMenuItemByLabelFinder(text, 
								 cont.getComparator()),
				      index));
	setTimeouts(cont.getTimeouts());
	setOutput(cont.getOutput());
    }
    
    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param text Button text. 
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public JMenuItemOperator(ContainerOperator cont, String text) {
	this(cont, text, 0);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public JMenuItemOperator(ContainerOperator cont, int index) {
	this((JMenuItem)
	     waitComponent(cont, 
			   new JMenuItemFinder(),
			   index));
	copyEnvironment(cont);
    }

    /**
     * Constructor.
     * Waits component in container first.
     * Uses cont's timeout and output for waiting and to init operator.
     * @throws TimeoutExpiredException
     */
    public JMenuItemOperator(ContainerOperator cont) {
	this(cont, 0);
    }

    /**
     * Searches JMenuItem in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JMenuItem instance or null if component was not found.
     */
    public static JMenuItem findJMenuItem(Container menu, ComponentChooser chooser, int index) {
	return((JMenuItem)findComponent(menu, new JMenuItemFinder(chooser), index));
    }

    /**
     * Searches 0'th JMenuItem in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JMenuItem instance or null if component was not found.
     */
    public static JMenuItem findJMenuItem(Container menu, ComponentChooser chooser) {
	return(findJMenuItem(menu, chooser, 0));
    }

    /**
     * Searches JMenuItem by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JMenuItem instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JMenuItem findJMenuItem(Container menu, String text, boolean ce, boolean ccs, int index) {
	return(findJMenuItem(menu, 
			     new JMenuItemByLabelFinder(text, 
							new DefaultStringComparator(ce, 
										      ccs)), 
			     index));
    }

    /**
     * Searches JMenuItem by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JMenuItem instance or null if component was not found.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     */
    public static JMenuItem findJMenuItem(Container menu, String text, boolean ce, boolean ccs) {
	return(findJMenuItem(menu, text, ce, ccs, 0));
    }

    /**
     * Waits JMenuItem in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @param index Ordinal component index.
     * @return JMenuItem instance.
     * @throws TimeoutExpiredException
     */
    public static JMenuItem waitJMenuItem(Container menu, ComponentChooser chooser, int index) {
	return((JMenuItem)waitComponent(menu, new JMenuItemFinder(chooser), index));
    }

    /**
     * Waits 0'th JMenuItem in container.
     * @param cont Container to search component in.
     * @param chooser org.netbeans.jemmy.ComponentChooser implementation.
     * @return JMenuItem instance.
     * @throws TimeoutExpiredException
     */
    public static JMenuItem waitJMenuItem(Container menu, ComponentChooser chooser) {
	return(waitJMenuItem(menu, chooser, 0));
    }

    /**
     * Waits JMenuItem by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @param index Ordinal component index.
     * @return JMenuItem instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JMenuItem waitJMenuItem(Container menu, String text, boolean ce, boolean ccs, int index) {
	return(waitJMenuItem(menu, 
			     new JMenuItemByLabelFinder(text, 
							new DefaultStringComparator(ce, ccs)), 
			     index));
    }

    /**
     * Waits JMenuItem by text.
     * @param cont Container to search component in.
     * @param text Button text. If null, contents is not checked.
     * @param ce Compare text exactly.
     * @param ccs Compare text case sensitively.
     * @return JMenuItem instance.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public static JMenuItem waitJMenuItem(Container menu, String text, boolean ce, boolean ccs) {
	return(waitJMenuItem(menu, text, ce, ccs, 0));
    }

    static {
	Timeouts.initDefault("JMenuItemOperator.PushMenuTimeout", PUSH_MENU_TIMEOUT);
    }

    /**
     * Defines current timeouts.
     * @param timeouts A collection of timeout assignments.
     * @see org.netbeans.jemmy.Timeoutable
     * @see org.netbeans.jemmy.Timeouts
     */
    public void setTimeouts(Timeouts timeouts) {
	super.setTimeouts(timeouts);
	this.timeouts = timeouts;
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
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.remove("Selected");
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>JMenuItem.addMenuDragMouseListener(MenuDragMouseListener)</code> through queue*/
    public void addMenuDragMouseListener(final MenuDragMouseListener menuDragMouseListener) {
	runMapping(new MapVoidAction("addMenuDragMouseListener") {
		public void map() {
		    ((JMenuItem)getSource()).addMenuDragMouseListener(menuDragMouseListener);
		}});}

    /**Maps <code>JMenuItem.addMenuKeyListener(MenuKeyListener)</code> through queue*/
    public void addMenuKeyListener(final MenuKeyListener menuKeyListener) {
	runMapping(new MapVoidAction("addMenuKeyListener") {
		public void map() {
		    ((JMenuItem)getSource()).addMenuKeyListener(menuKeyListener);
		}});}

    /**Maps <code>JMenuItem.getAccelerator()</code> through queue*/
    public KeyStroke getAccelerator() {
	return((KeyStroke)runMapping(new MapAction("getAccelerator") {
		public Object map() {
		    return(((JMenuItem)getSource()).getAccelerator());
		}}));}

    /**Maps <code>JMenuItem.getComponent()</code> through queue*/
    public Component getComponent() {
	return((Component)runMapping(new MapAction("getComponent") {
		public Object map() {
		    return(((JMenuItem)getSource()).getComponent());
		}}));}

    /**Maps <code>JMenuItem.getSubElements()</code> through queue*/
    public MenuElement[] getSubElements() {
	return((MenuElement[])runMapping(new MapAction("getSubElements") {
		public Object map() {
		    return(((JMenuItem)getSource()).getSubElements());
		}}));}

    /**Maps <code>JMenuItem.isArmed()</code> through queue*/
    public boolean isArmed() {
	return(runMapping(new MapBooleanAction("isArmed") {
		public boolean map() {
		    return(((JMenuItem)getSource()).isArmed());
		}}));}

    /**Maps <code>JMenuItem.menuSelectionChanged(boolean)</code> through queue*/
    public void menuSelectionChanged(final boolean b) {
	runMapping(new MapVoidAction("menuSelectionChanged") {
		public void map() {
		    ((JMenuItem)getSource()).menuSelectionChanged(b);
		}});}

    /**Maps <code>JMenuItem.processKeyEvent(KeyEvent, MenuElement[], MenuSelectionManager)</code> through queue*/
    public void processKeyEvent(final KeyEvent keyEvent, final MenuElement[] menuElement, final MenuSelectionManager menuSelectionManager) {
	runMapping(new MapVoidAction("processKeyEvent") {
		public void map() {
		    ((JMenuItem)getSource()).processKeyEvent(keyEvent, menuElement, menuSelectionManager);
		}});}

    /**Maps <code>JMenuItem.processMenuDragMouseEvent(MenuDragMouseEvent)</code> through queue*/
    public void processMenuDragMouseEvent(final MenuDragMouseEvent menuDragMouseEvent) {
	runMapping(new MapVoidAction("processMenuDragMouseEvent") {
		public void map() {
		    ((JMenuItem)getSource()).processMenuDragMouseEvent(menuDragMouseEvent);
		}});}

    /**Maps <code>JMenuItem.processMenuKeyEvent(MenuKeyEvent)</code> through queue*/
    public void processMenuKeyEvent(final MenuKeyEvent menuKeyEvent) {
	runMapping(new MapVoidAction("processMenuKeyEvent") {
		public void map() {
		    ((JMenuItem)getSource()).processMenuKeyEvent(menuKeyEvent);
		}});}

    /**Maps <code>JMenuItem.processMouseEvent(MouseEvent, MenuElement[], MenuSelectionManager)</code> through queue*/
    public void processMouseEvent(final MouseEvent mouseEvent, final MenuElement[] menuElement, final MenuSelectionManager menuSelectionManager) {
	runMapping(new MapVoidAction("processMouseEvent") {
		public void map() {
		    ((JMenuItem)getSource()).processMouseEvent(mouseEvent, menuElement, menuSelectionManager);
		}});}

    /**Maps <code>JMenuItem.removeMenuDragMouseListener(MenuDragMouseListener)</code> through queue*/
    public void removeMenuDragMouseListener(final MenuDragMouseListener menuDragMouseListener) {
	runMapping(new MapVoidAction("removeMenuDragMouseListener") {
		public void map() {
		    ((JMenuItem)getSource()).removeMenuDragMouseListener(menuDragMouseListener);
		}});}

    /**Maps <code>JMenuItem.removeMenuKeyListener(MenuKeyListener)</code> through queue*/
    public void removeMenuKeyListener(final MenuKeyListener menuKeyListener) {
	runMapping(new MapVoidAction("removeMenuKeyListener") {
		public void map() {
		    ((JMenuItem)getSource()).removeMenuKeyListener(menuKeyListener);
		}});}

    /**Maps <code>JMenuItem.setAccelerator(KeyStroke)</code> through queue*/
    public void setAccelerator(final KeyStroke keyStroke) {
	runMapping(new MapVoidAction("setAccelerator") {
		public void map() {
		    ((JMenuItem)getSource()).setAccelerator(keyStroke);
		}});}

    /**Maps <code>JMenuItem.setArmed(boolean)</code> through queue*/
    public void setArmed(final boolean b) {
	runMapping(new MapVoidAction("setArmed") {
		public void map() {
		    ((JMenuItem)getSource()).setArmed(b);
		}});}

    /**Maps <code>JMenuItem.setUI(MenuItemUI)</code> through queue*/
    public void setUI(final MenuItemUI menuItemUI) {
	runMapping(new MapVoidAction("setUI") {
		public void map() {
		    ((JMenuItem)getSource()).setUI(menuItemUI);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    protected void prepareToClick() {
	output.printLine("Push menu item\n    :" + getSource().toString());
	output.printGolden("Push menu item");
	Timeouts times = timeouts.cloneThis();
	times.setTimeout("AbstractButtonOperator.PushButtonTimeout", 
			 timeouts.getTimeout("JMenuItemOperator.PushMenuTimeout"));
	super.setTimeouts(times);
	super.setOutput(output.createErrorOutput());
    }

    static ComponentChooser[] createChoosers(String[] names, StringComparator comparator) {
	ComponentChooser[] choosers = new ComponentChooser[names.length];
	for(int i = 0; i < choosers.length; i++) {
	    choosers[i] = new JMenuItemOperator.JMenuItemByLabelFinder(names[i], comparator);
	}
	return(choosers);
    }

    public static class JMenuItemByLabelFinder implements ComponentChooser {
	String label;
	StringComparator comparator;
	public JMenuItemByLabelFinder(String lb, StringComparator comparator) {
	    label = lb;
	    this.comparator = comparator;
	}
	public JMenuItemByLabelFinder(String lb) {
            this(lb, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof JMenuItem) {
		if(((JMenuItem)comp).getText() != null) {
		    return(comparator.equals(((JMenuItem)comp).getText(),
					     label));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("JMenuItem with text \"" + label + "\"");
	}
    }

    public static class JMenuItemFinder extends Finder {
	public JMenuItemFinder(ComponentChooser sf) {
            super(JMenuItem.class, sf);
	}
	public JMenuItemFinder() {
            super(JMenuItem.class);
	}
    }
}
