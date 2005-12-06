/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide;


import org.netbeans.junit.NbTestSuite;

import java.awt.Component;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.event.ChangeListener;
import org.openide.InstantiatingIteratorTest.Listener;
import org.openide.util.HelpCtx;

/** Testing functional implementation calling the methods to interface <code>WizardDescriptor.AsynchronousInstantiatingIterator</code>
 * from WizardDescriptor. Check if the method <code>instantiate()</code> is called outside AWT in particular.
 * @see Issue 62161
 */
public class AsynchronousInstantiatingIteratorTest extends InstantiatingIteratorTest {

    
    public AsynchronousInstantiatingIteratorTest (String name) {
        super(name);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run (new NbTestSuite (AsynchronousInstantiatingIteratorTest.class));
        System.exit (0);
    }
    
    private Iterator iterator;

    protected void setUp () {
        iterator = new Iterator ();
        wd = new WizardDescriptor (iterator);
        wd.addPropertyChangeListener(new Listener ());
        java.awt.Dialog d = DialogDisplayer.getDefault ().createDialog (wd);
        checkOrder = false;
        shouldThrowException = false;
        //d.show();
    }
    
    /** Run all tests in AWT thread */
    protected boolean runInEQ() {
        return true;
    }

    public void testInstantiateInAWTQueueOrNot () {
        checkIfInAWT = true;

        wd.doNextClick ();
        finishWizard (wd);
        try {
            Set newObjects = wd.getInstantiatedObjects ();
        } catch (IllegalStateException ise) {
            fail ("IllegalStateException was caught because WD.instantiate() called in AWT queue.");
        }
        assertNotNull ("InstantiatingIterator was correctly instantiated.", getResult ());
    }
    
    public class Panel implements WizardDescriptor.FinishablePanel {
        private JLabel component;
        private String text;
        public Panel(String text) {
            this.text = text;
        }

        public Component getComponent() {
            if (component == null) {
                component = new JLabel (text);
            }
            return component;
        }
        
        public void addChangeListener(ChangeListener l) {
            changeListenersInPanel.add (l);
        }
        
        public HelpCtx getHelp() {
            return null;
        }
        
        public boolean isValid() {
            return true;
        }
        
        public void readSettings(Object settings) {
            log ("readSettings of panel: " + text + " [time: " + System.currentTimeMillis () +
                    "] with PROP_VALUE: " + handleValue (wd.getValue ()));
        }
        
        public void removeChangeListener(ChangeListener l) {
            changeListenersInPanel.remove (l);
        }
        
        public void storeSettings(Object settings) {
            if (checkOrder) {
                assertNull ("WD.P.storeSettings() called before WD.I.instantiate()", iterator.result);
                // bugfix #45093, remember storeSettings could be called multiple times
                // do check order only when the first time
                checkOrder = false;
            }
            log ("storeSettings of panel: " + text + " [time: " + System.currentTimeMillis () +
                    "] with PROP_VALUE: " + handleValue (wd.getValue ()));
            if (exceptedValue != null) {
                assertEquals ("WD.getValue() returns excepted value.", exceptedValue, handleValue (wd.getValue ()));
            }
        }
        
        public boolean isFinishPanel () {
            return true;
        }
        
    }
    
    protected Boolean getInitialized () {
        return iterator.initialized;
    }
    
    protected Set getResult () {
        return iterator.result;
    }
    
    public class Iterator implements WizardDescriptor.AsynchronousInstantiatingIterator {
        int index = 0;
        WizardDescriptor.Panel panels[] = new WizardDescriptor.Panel[2];
        java.util.Set helpSet;
        
        private Boolean initialized = null;
        private Set result = null;
        
        public WizardDescriptor.Panel current () {
            assertTrue ("WD.current() called on initialized iterator.", initialized != null && initialized.booleanValue ());
            return panels[index];
        }
        public String name () {
            return "Test iterator";
        }
        public boolean hasNext () {
            return index < 1;
        }
        public boolean hasPrevious () {
            return index > 0;
        }
        public void nextPanel () {
            if (!hasNext ()) throw new NoSuchElementException ();
            index ++;
        }
        public void previousPanel () {
            if (!hasPrevious ()) throw new NoSuchElementException ();
            index --;
        }
        public void addChangeListener (ChangeListener l) {
            changeListenersInIterator.add (l);
        }
        public void removeChangeListener (ChangeListener l) {
            changeListenersInIterator.remove (l);
        }
        public java.util.Set instantiate () throws IOException {
            if (checkIfInAWT) {
                if (SwingUtilities.isEventDispatchThread ()) {
                    throw new IOException ("Cannot run in AWT queue.");
                }
            }
            if (shouldThrowException) {
                throw new IOException ("Test throw IOException during instantiate().");
            }
            if (initialized.booleanValue ()) {
                helpSet.add ("member");
                result = helpSet;
            } else {
                result = null;
            }
            return result;
        }
        public void initialize (WizardDescriptor wizard) {
            helpSet = new HashSet ();
            panels[0] = new Panel("first panel");
            panels[1] = new Panel("second panel");
            initialized = Boolean.TRUE;
        }
        public void uninitialize (WizardDescriptor wizard) {
            helpSet.clear ();
            initialized = Boolean.FALSE;
            panels = null;
        }
    }
    
}
