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
import org.netbeans.jemmy.DialogWaiter;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Timeouts;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;

import java.util.Hashtable;

/**
 * <BR><BR>Timeouts used: <BR>
 * DialogWaiter.WaitDialogTimeout - time to wait dialog displayed <BR>
 * DialogWaiter.AfterDialogTimeout - time to sleep after dialog has been dispayed <BR>
 * ComponentOperator.WaitStateTimeout - time to wait for title <BR>
 *
 * @see org.netbeans.jemmy.Timeouts
 * 
 * @author Alexandre Iline (alexandre.iline@sun.com)
 *	
 */

public class DialogOperator extends WindowOperator {

    public static final String TITLE_DPROP = "Title";
    public static final String IS_MODAL_DPROP = "Modal";
    public static final String IS_RESIZABLE_DPROP = "Resizable";

    public DialogOperator(Dialog w) {
	super(w);
    }

    public DialogOperator(ComponentChooser chooser, int index, Operator env) {
	this(waitDialog(new DialogFinder(chooser),
                        index, 
                        env.getTimeouts(),
			env.getOutput()));
	copyEnvironment(env);
    }

    public DialogOperator(ComponentChooser chooser, int index) {
	this(chooser, index, Operator.getEnvironmentOperator());
    }

    public DialogOperator(ComponentChooser chooser) {
	this(chooser, 0);
    }

    public DialogOperator(WindowOperator owner, ComponentChooser chooser, int index) {
	this((Dialog)owner.
             waitSubWindow(new DialogFinder(chooser),
                           index));
	copyEnvironment(owner);
    }

    public DialogOperator(WindowOperator owner, ComponentChooser chooser) {
	this(owner, chooser, 0);
    }

    /**
     * Constructor.
     * Waits for a dialog to show. The dialog is identified as the
     * <code>index+1</code>'th <code>java.awt.Dialog</code> that shows, is
     * owned by the window managed by the <code>WindowOperator</code>
     * <code>owner</code>, and that has the desired title.  Uses owner's
     * timeout and output for waiting and to init this operator.
     * @param owner Operator pointing to a window owner.
     * @param title The desired title.
     * @param index Ordinal index.  The first dialog has <code>index</code> 0.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public DialogOperator(WindowOperator owner, String title, int index) {
	this(waitDialog(owner,
			 new DialogByTitleFinder(title,
                                                 owner.getComparator()), 
			 index));
	copyEnvironment(owner);
    }

    /**
     * Uses owner's timeout and output for waiting and to init operator.
     * Waits for a dialog to show. The dialog is identified as the
     * first <code>java.awt.Dialog</code> that shows, is
     * owned by the window managed by the <code>WindowOperator</code>
     * <code>owner</code>, and that has the desired title.  Uses owner's
     * timeout and output for waiting and to init this operator.
     * @param owner Operator pointing to a window owner.
     * @param title The desired title.
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public DialogOperator(WindowOperator owner, String title) {
	this(owner, title, 0);
    }

    /**
     * Constructor.
     * Waits for the index'th dialog between owner's children.
     * Uses owner'th timeout and output for waiting and to init operator.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public DialogOperator(WindowOperator owner, int index) {
	this((Dialog)
	     waitDialog(owner, 
			new DialogFinder(),
			index));
	copyEnvironment(owner);
    }

    /**
     * Constructor.
     * Waits for the first dialog between owner's children.
     * Uses owner'th timeout and output for waiting and to init operator.
     * @throws TimeoutExpiredException
     */
    public DialogOperator(WindowOperator owner) {
	this(owner, 0);
    }

    /**
     * Constructor.
     * Waits for the dialog with "title" subtitle.
     * Constructor can be used in complicated cases when
     * output or timeouts should differ from default.
     * @param title
     * @param index
     * @param timeouts 
     * @param output
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @throws TimeoutExpiredException
     */
    public DialogOperator(String title, int index, Operator env) {
	this(new DialogByTitleFinder(title, 
                                     env.getComparator()),
             index,
             env);
    }

    /**
     * Constructor.
     * Waits for the dialog with "title" subtitle.
     * Uses current timeouts and output values.
     * @param title
     * @param index
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @see JemmyProperties#getCurrentTimeouts()
     * @see JemmyProperties#getCurrentOutput()
     * @throws TimeoutExpiredException
     */
    public DialogOperator(String title, int index){
	this(title, index,
	     ComponentOperator.getEnvironmentOperator());
    }

    /**
     * Constructor.
     * Waits for the dialog with "title" subtitle.
     * Uses current timeouts and output values.
     * @param title
     * @see ComponentOperator#isCaptionEqual(String, String, boolean, boolean)
     * @see JemmyProperties#getCurrentTimeouts()
     * @see JemmyProperties#getCurrentOutput()
     * @throws TimeoutExpiredException
     */
    public DialogOperator(String title) {
	this(title, 0);
    }

    /**
     * Constructor.
     * Waits for the index'th dialog.
     * Uses current timeout and output for waiting and to init operator.
     * @param index Ordinal component index.
     * @throws TimeoutExpiredException
     */
    public DialogOperator(int index) {
	this((Dialog)
	     waitDialog(new DialogFinder(),
			index,
			ComponentOperator.getEnvironmentOperator().getTimeouts(),
			ComponentOperator.getEnvironmentOperator().getOutput()));
	copyEnvironment(ComponentOperator.getEnvironmentOperator());
    }

    /**
     * Constructor.
     * Waits for the first dialog.
     * Uses current timeout and output for waiting and to init operator.
     * @throws TimeoutExpiredException
     */
    public DialogOperator() {
	this(0);
    }

    /**
     * Waits for title. Uses getComparator() comparator.
     * @param title Title to wait for.
     */
    public void waitTitle(final String title) {
	getOutput().printLine("Wait \"" + title + "\" title of dialog \n    : "+
			      getSource().toString());
	getOutput().printGolden("Wait \"" + title + "\" title");
	waitState(new DialogByTitleFinder(title, getComparator()));
    }

    /**
     * Returns information about component.
     */
    public Hashtable getDump() {
	Hashtable result = super.getDump();
	result.put(TITLE_DPROP, ((Dialog)getSource()).getTitle());
	result.put(IS_MODAL_DPROP, ((Dialog)getSource()).isModal() ? "true" : "false");
	result.put(IS_RESIZABLE_DPROP, ((Dialog)getSource()).isResizable() ? "true" : "false");
	return(result);
    }

    ////////////////////////////////////////////////////////
    //Mapping                                             //

    /**Maps <code>Dialog.getTitle()</code> through queue*/
    public String getTitle() {
	return((String)runMapping(new MapAction("getTitle") {
		public Object map() {
		    return(((Dialog)getSource()).getTitle());
		}}));}

    /**Maps <code>Dialog.isModal()</code> through queue*/
    public boolean isModal() {
	return(runMapping(new MapBooleanAction("isModal") {
		public boolean map() {
		    return(((Dialog)getSource()).isModal());
		}}));}

    /**Maps <code>Dialog.isResizable()</code> through queue*/
    public boolean isResizable() {
	return(runMapping(new MapBooleanAction("isResizable") {
		public boolean map() {
		    return(((Dialog)getSource()).isResizable());
		}}));}

    /**Maps <code>Dialog.setModal(boolean)</code> through queue*/
    public void setModal(final boolean b) {
	runMapping(new MapVoidAction("setModal") {
		public void map() {
		    ((Dialog)getSource()).setModal(b);
		}});}

    /**Maps <code>Dialog.setResizable(boolean)</code> through queue*/
    public void setResizable(final boolean b) {
	runMapping(new MapVoidAction("setResizable") {
		public void map() {
		    ((Dialog)getSource()).setResizable(b);
		}});}

    /**Maps <code>Dialog.setTitle(String)</code> through queue*/
    public void setTitle(final String string) {
	runMapping(new MapVoidAction("setTitle") {
		public void map() {
		    ((Dialog)getSource()).setTitle(string);
		}});}

    //End of mapping                                      //
    ////////////////////////////////////////////////////////

    protected static Dialog waitDialog(ComponentChooser chooser, int index,
					 Timeouts timeouts, TestOut output) {
	try {
	    DialogWaiter waiter = new DialogWaiter();
	    waiter.setTimeouts(timeouts);
	    waiter.setOutput(output);
	    return((Dialog)waiter.
		   waitDialog(new DialogFinder(chooser), index));
	} catch(InterruptedException e) {
	    output.printStackTrace(e);
	    return(null);
	}
    }

    protected static Dialog waitDialog(WindowOperator owner, ComponentChooser chooser, int index) {
	return(waitDialog((Window)owner.getSource(), 
			  chooser, index, 
			  owner.getTimeouts(), owner.getOutput()));
    }

    protected static Dialog waitDialog(Window owner, ComponentChooser chooser, int index,
					 Timeouts timeouts, TestOut output) {
	try {
	    DialogWaiter waiter = new DialogWaiter();
	    waiter.setTimeouts(timeouts);
	    waiter.setOutput(output);
	    return((Dialog)waiter.
		   waitDialog(owner, new DialogFinder(chooser), index));
	} catch(InterruptedException e) {
	    JemmyProperties.getCurrentOutput().printStackTrace(e);
	    return(null);
	}
    }


    public static class DialogFinder extends Finder {
	public DialogFinder(ComponentChooser sf) {
            super(Dialog.class, sf);
	}
	public DialogFinder() {
            super(Dialog.class);
	}
    }

    public static class DialogByTitleFinder implements ComponentChooser {
	String title;
	StringComparator comparator;
	public DialogByTitleFinder(String t, StringComparator comparator) {
	    title = t;
	    this.comparator = comparator;
	}
	public DialogByTitleFinder(String t) {
            this(t, Operator.getDefaultStringComparator());
	}
	public boolean checkComponent(Component comp) {
	    if(comp instanceof Dialog) {
		if(((Dialog)comp).isShowing() && ((Dialog)comp).getTitle() != null) {
		    return(comparator.equals(((Dialog)comp).getTitle(), title));
		}
	    }
	    return(false);
	}
	public String getDescription() {
	    return("Dialog with title \"" + title + "\"");
	}
    }
}

