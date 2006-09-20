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

package org.netbeans.core.windows.services;

import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.openide.DialogDescriptor;
import org.netbeans.junit.NbTestCase;
import org.openide.util.HelpCtx;

/** Tests issue 56534.
 *
 * @author Jiri Rechtacek
 */
public class NbPresenterTest extends NbTestCase {

    public NbPresenterTest (String testName) {
        super (testName);
    }

    protected boolean runInEQ () {
        return true;
    }

    public void testDialogsOptionsOnDefaultSystem () {
        System.setProperty ("xtest.looks_as_mac", "false");
        doTestDialogsOptions ();
    }
    
    public void testDialogsOptionsOnMac () {
        System.setProperty ("xtest.looks_as_mac", "true");
        doTestDialogsOptions ();
    }
    
    private void doTestDialogsOptions () {
        boolean modal = false;
        //boolean modal = true;
        JButton erase = new JButton ("Erase all my data");
        JButton rescue = new JButton ("Rescue");
        JButton cancel = new JButton ("Cancel");
        JButton [] options = new JButton [] {erase, rescue, cancel};
        DialogDescriptor dd = new DialogDescriptor (new JLabel ("Something interesting"), "My dialog", modal,
                // options
                options,
                rescue,
                // align
                DialogDescriptor.RIGHT_ALIGN,
                new HelpCtx (NbPresenterTest.class), null);
        

        dd.setClosingOptions (new Object[0]);
                
        NbPresenter presenter = new NbDialog (dd, (JFrame)null);
        presenter.setVisible (true);
        
        erase.doClick ();
        assertEquals ("Erase was invoked.", erase.getText (), ((JButton)dd.getValue ()).getText ());
        erase.doClick ();
        assertEquals ("Erase was invoked again on same dialog.", erase.getText (), ((JButton)dd.getValue ()).getText ());
        presenter.dispose ();

        presenter = new NbDialog (dd, (JFrame)null);
        presenter.setVisible (true);

        erase.doClick ();
        assertEquals ("Erase was invoked of reused dialog.", erase.getText (), ((JButton)dd.getValue ()).getText ());
        erase.doClick ();
        assertEquals ("Erase was invoked again on reused dialog.", erase.getText (), ((JButton)dd.getValue ()).getText ());
        presenter.dispose ();

        presenter = new NbDialog (dd, (JFrame)null);
        presenter.setVisible (true);

        rescue.doClick ();
        assertEquals ("Rescue was invoked of reused dialog.", rescue.getText (), ((JButton)dd.getValue ()).getText ());
        rescue.doClick ();
        assertEquals ("Rescue was invoked again on reused dialog.", rescue.getText (), ((JButton)dd.getValue ()).getText ());
        presenter.dispose ();
        
        presenter = new NbDialog (dd, (JFrame)null);
        presenter.setVisible (true);

        cancel.doClick ();
        assertEquals ("Cancel was invoked of reused dialog.", cancel.getText (), ((JButton)dd.getValue ()).getText ());
        cancel.doClick ();
        assertEquals ("Cancel was invoked again on reused dialog.", cancel.getText (), ((JButton)dd.getValue ()).getText ());
        presenter.dispose ();
    }
    
    public void testNbPresenterComparator () {
        JButton erase = new JButton ("Erase all my data");
        JButton rescue = new JButton ("Rescue");
        JButton cancel = new JButton ("Cancel");
        JButton [] options = new JButton [] {erase, rescue, cancel};
        DialogDescriptor dd = new DialogDescriptor (new JLabel ("Something interesting"), "My dialog", false,
                // options
                options,
                rescue,
                // align
                DialogDescriptor.RIGHT_ALIGN,
                null, null);
                
        dd.setClosingOptions (null);
                
        NbPresenter presenter = new NbDialog (dd, (JFrame)null);
        assertEquals ("Dialog has Rescue option as default value.", rescue, dd.getDefaultValue ());
        JButton [] backup = (JButton [])options.clone ();
        //showButtonArray (backup);
        Arrays.sort (options, presenter);
        //showButtonArray (options);
        JButton [] onceSorted = (JButton [])options.clone ();
        Arrays.sort (options, presenter);
        //showButtonArray (options);
        JButton [] twiceSorted = (JButton [])options.clone ();
        assertFalse ("Original options not same as sorted option.", Arrays.asList (backup).equals (Arrays.asList (onceSorted)));
        assertEquals ("Sorting of options is invariable.", Arrays.asList (onceSorted), Arrays.asList (twiceSorted));
        presenter.setVisible (true);
        erase.doClick ();
        assertEquals ("Dialog has been close by Erase option", erase, dd.getValue ());

        presenter = new NbDialog (dd, (JFrame)null);
        presenter.setVisible (true);

        options = (JButton [])backup.clone ();
        //showButtonArray (backup);
        Arrays.sort (options, presenter);
        JButton [] onceSorted2 = (JButton [])options.clone ();
        //showButtonArray (onceSorted2);
        Arrays.sort (options, presenter);
        JButton [] twiceSorted2 = (JButton [])options.clone ();
        //showButtonArray (twiceSorted2);
        assertFalse ("Original options not same as sorted option on reused dialog.", Arrays.asList (backup).equals (Arrays.asList (onceSorted2)));
        assertEquals ("Sorting of options is invariable also on reused dialog.", Arrays.asList (onceSorted2), Arrays.asList (twiceSorted2));
        assertEquals ("The options are sorted same on both dialogs.", Arrays.asList (onceSorted), Arrays.asList (twiceSorted2));
        
    }
    
    private void showButtonArray (Object [] array) {
        JButton [] arr = (JButton []) array;
        System.out.print("do: ");
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i].getText() + ", ");
        }
        System.out.println(".");
    }
    
}
