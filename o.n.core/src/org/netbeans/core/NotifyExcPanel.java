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

package org.netbeans.core;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.core.startup.CLIOptions;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 * Notifies exceptions.
 *
 * This class is public only because the MainWindow needs get the flashing
 * icon to its status bar from this class (method getNotificationVisualizer()).
 *
 * @author  Jaroslav Tulach
 */
public final class NotifyExcPanel extends JPanel implements ActionListener {
    static final long serialVersionUID =3680397500573480127L;


    /** the instance */
    private static NotifyExcPanel INSTANCE = null;
    /** preferred width of this component */
    private static final int SIZE_PREFERRED_WIDTH=550;
    /** preferred height of this component */
    private static final int SIZE_PREFERRED_HEIGHT=250;

    /** enumeration of NbExceptionManager.Exc to notify */
    private static ArrayListPos exceptions;
    /** current exception */
    private NbErrorManager.Exc current;

    /** dialog descriptor */
    private DialogDescriptor descriptor;
    /** dialog that displayes the exceptions */
    java.awt.Dialog dialog;
    /** button to show next exceptions */
    private JButton next;
    /** button to show previous exceptions */
    private JButton previous;
    /** details button */
    private JButton details;
    /** details window */
    private JTextPane output;

    /** boolean to show/hide details */
    private static boolean showDetails;
    
    /** the last position of the exception dialog window */
    private static Rectangle lastBounds;

    /** Constructor.
    */
    private NotifyExcPanel () {
        setPreferredSize(new Dimension(SIZE_PREFERRED_WIDTH,SIZE_PREFERRED_HEIGHT));

        java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle(NotifyExcPanel.class);
        next = new JButton (bundle.getString("CTL_NextException"));
        // bugfix 25684, don't set Previous/Next as default capable
        next.setDefaultCapable (false);
        previous = new JButton (bundle.getString("CTL_PreviousException"));
        previous.setDefaultCapable (false);
        details = new JButton ();
        details.setDefaultCapable (false);

        output = new JTextPane() {
            public boolean getScrollableTracksViewportWidth() {
                return false;
            }
        };
        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, output.getFont().getSize() + 1)); // NOI18N
        output.setForeground(UIManager.getColor("Label.foreground")); // NOI18N
        output.setBackground(UIManager.getColor("Label.background")); // NOI18N

        setLayout( new BorderLayout() );
        add(new JScrollPane(output));
        setBorder( new javax.swing.border.BevelBorder(javax.swing.border.BevelBorder.LOWERED));
            
        next.setMnemonic(bundle.getString("CTL_NextException_Mnemonic").charAt(0));
        previous.setMnemonic(bundle.getString("CTL_PreviousException_Mnemonic").charAt(0));
        next.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_NextException"));
        previous.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_PreviousException"));
        output.getAccessibleContext().setAccessibleName(bundle.getString("ACSN_ExceptionStackTrace"));
        output.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_ExceptionStackTrace"));
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_NotifyExceptionPanel"));

        descriptor = new DialogDescriptor ("", ""); // NOI18N

        descriptor.setMessageType (DialogDescriptor.ERROR_MESSAGE);
        descriptor.setOptions (new Object[] {
                                   previous,
                                   next,
                                   DialogDescriptor.OK_OPTION
                               });
        descriptor.setAdditionalOptions (new Object[] {
                                             details
                                         });
        descriptor.setClosingOptions (new Object[0]);
        descriptor.setButtonListener (this);

        // bugfix #27176, create dialog in modal state if some other modal
        // dialog is opened at the time
        // #53328 do not let the error dialog to be created modal unless the main
        // window is visible. otherwise the error message may be hidden behind
        // the main window thus making the main window unusable
        descriptor.setModal( isModalDialogPresent() 
                && WindowManager.getDefault().getMainWindow().isVisible() );
        
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        if( null != lastBounds )
            dialog.setBounds( lastBounds );
        
        dialog.getAccessibleContext().setAccessibleName(bundle.getString("ACN_NotifyExcPanel_Dialog")); // NOI18N
        dialog.getAccessibleContext().setAccessibleDescription(bundle.getString("ACD_NotifyExcPanel_Dialog")); // NOI18N
    }

    private static boolean isModalDialogPresent() {
        return hasModalDialog(WindowManager.getDefault().getMainWindow())
            // XXX Trick to get the shared frame instance.
            || hasModalDialog(new JDialog().getOwner());
    }
    
    private static boolean hasModalDialog(Window w) {
        if (w == null) { // #63830
            return false;
        }
        Window[] ws = w.getOwnedWindows();
        for(int i = 0; i < ws.length; i++) {
            if(ws[i] instanceof Dialog && ((Dialog)ws[i]).isModal() && ws[i].isVisible()) {
                return true;
            } else if(hasModalDialog(ws[i])) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * For unit-testing only
     */
    static void cleanInstance() {
        INSTANCE = null;
    }


    /** Adds new exception into the queue.
     */
    public static void notify (
        final NbErrorManager.Exc t
    ) {
        if (!shallNotify(t.getSeverity(), false)) {
            return;
        }
        
        // #50018 Don't try to show any notify dialog when reporting headless exception
        if ("java.awt.HeadlessException".equals(t.getClassName()) && GraphicsEnvironment.isHeadless()) {
            t.printStackTrace(System.err);
            return;
        }

        SwingUtilities.invokeLater (new Runnable () {
            public void run() {
                String glm = t.getLocalizedMessage();
                Level gs = t.getSeverity();
                boolean loc = t.isLocalized();

                if (loc) {
                    if (gs == Level.WARNING) {
                        DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(glm, NotifyDescriptor.WARNING_MESSAGE)
                        );
                        return;
                    }

                    if (gs.intValue() == 1973) {
                        DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(glm, NotifyDescriptor.INFORMATION_MESSAGE)
                        );
                        return;
                    }

                    if (gs == Level.SEVERE) {
                        DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(glm, NotifyDescriptor.ERROR_MESSAGE)
                        );
                        return;
                    }
                }

                
                if( null == exceptions )
                    exceptions = new ArrayListPos();
                exceptions.add(t);
                exceptions.position = exceptions.size()-1;

                if(shallNotify(t.getSeverity(), true)) {
                    // Assertions are on, so show the exception window.
                    if( INSTANCE == null ) {
                        INSTANCE = new NotifyExcPanel();
                    }
                    INSTANCE.updateState(t);
                } else {
                    // No assertions, use the flashing icon.
                    if( null != flasher && null == INSTANCE ) {
                        //exception window is not visible, start flashing the icon
                        flasher.setToolTipText( getExceptionSummary( t ) );
                        flasher.startFlashing();
                    } else {
                        //exception window is already visible (or the flashing icon is not available)
                        //so we'll only update the exception window
                        if( INSTANCE == null ) {
                            INSTANCE = new NotifyExcPanel();
                        }
                        INSTANCE.updateState(t);
                    }
                }
            }
        });
    }
    
    /**
     * @return A brief exception summary for the flashing icon tooltip (either 
     * the exception message or exception class name).
     */
    private static String getExceptionSummary( final NbErrorManager.Exc t ) {
        String plainmsg;
        String glm = t.getLocalizedMessage();
        if (glm != null) {
            plainmsg = glm;
        } else if (t.getMessage() != null) {
            plainmsg = t.getMessage();
        } else {
            plainmsg = t.getClassName();
        }
        assert plainmsg != null;
        return plainmsg;
    }


    /**
     * updates the state of the dialog. called only in AWT thread.
     */
    private void updateState (NbErrorManager.Exc t) {
        if (!exceptions.existsNextElement()) {
            // it can be commented out while INSTANCE is not cached
            // (see the comment in actionPerformed)
            /*// be modal if some modal dialog is already opened, nonmodal otherwise
            boolean isModalDialogOpened = NbPresenter.currentModalDialog != null;
            if (descriptor.isModal() != isModalDialogOpened) {
                descriptor.setModal(isModalDialogOpened);
               // bugfix #27176, old dialog is disposed before recreating
               if (dialog != null) dialog.dispose ();
               // so we can safely send it to gc and recreate dialog
               // dialog = org.openide.DialogDisplayer.getDefault ().createDialog (descriptor);
            }*/
            // the dialog is not shown
            current = t;
            update ();
        } else {
            // add the exception to the queue
            next.setVisible (true);
        }
        try {
            //Dialog.show() will pump events for the AWT thread.  If the 
            //exception happened because of a paint, it will trigger opening
            //another dialog, which will trigger another exception, endlessly.
            //Catch any exceptions and append them to the list instead.
            ensurePreferredSize();
            dialog.setVisible(true);
            //throw new RuntimeException ("I am not so exceptional"); //uncomment to test
        } catch (Exception e) {
            exceptions.add(new NbErrorManager().createExc(
                e, Level.SEVERE, null));
            next.setVisible(true);
        }
    }

    private void ensurePreferredSize() {
        if( null != lastBounds )
            return; //we remember the last window position
        Dimension sz = dialog.getSize();
        Dimension pref = dialog.getPreferredSize();
        if (pref.height == 0) pref.height = SIZE_PREFERRED_HEIGHT;
        if (pref.width == 0) pref.width = SIZE_PREFERRED_WIDTH;
        if (!sz.equals(pref)) {
            dialog.setSize(pref.width, pref.height);
            dialog.validate();
            dialog.repaint();
        }
    }
    

    /** Updates the visual state of the dialog.
    */
    private void update () {
        // JST: this can be improved in future...
        boolean isLocalized = current.isLocalized();

        next.setVisible (exceptions.existsNextElement());
        previous.setVisible (exceptions.existsPreviousElement());

        if (showDetails) {
            details.setText (org.openide.util.NbBundle.getBundle(NotifyExcPanel.class).getString("CTL_Exception_Hide_Details"));
            details.setMnemonic(org.openide.util.NbBundle.getBundle(NotifyExcPanel.class).getString("CTL_Exception_Hide_Details_Mnemonic").charAt(0));
            details.getAccessibleContext().setAccessibleDescription(
                org.openide.util.NbBundle.getBundle(NotifyExcPanel.class).getString("ACSD_Exception_Hide_Details"));
        } else {
            details.setText(org.openide.util.NbBundle.getBundle(NotifyExcPanel.class).getString("CTL_Exception_Show_Details"));
            details.setMnemonic(org.openide.util.NbBundle.getBundle(NotifyExcPanel.class).getString("CTL_Exception_Show_Details_Mnemonic").charAt(0));
            details.getAccessibleContext().setAccessibleDescription(
                org.openide.util.NbBundle.getBundle(NotifyExcPanel.class).getString("ACSD_Exception_Show_Details"));
        }

        //    setText (current.getLocalizedMessage ());
        String title = org.openide.util.NbBundle.getBundle(NotifyExcPanel.class).getString("CTL_Title_Exception");

        if (showDetails) {
            descriptor.setMessage (this);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // XXX #28191: some other piece of code should underline these, etc.
                        StringWriter wr = new StringWriter();
                        current.printStackTrace(new PrintWriter(wr, true));
                        output.setText(wr.toString());
                        output.getCaret().setDot(0);
                        output.requestFocus ();
                }
            });
        } else {
            if (isLocalized) {
                String msg = current.getLocalizedMessage ();
                if (msg != null) {
                    descriptor.setMessage (msg);
                }
            } else {
                ResourceBundle curBundle = NbBundle.getBundle (NotifyExcPanel.class);
                if (current.getSeverity() == Level.WARNING) {
                    // less scary message for warning level
                    descriptor.setMessage (
                        java.text.MessageFormat.format(
                            curBundle.getString("NTF_ExceptionWarning"),
                            new Object[] {
                                current.getClassName ()
                            }
                        )
                    );
                    title = curBundle.getString("NTF_ExceptionWarningTitle"); // NOI18N
                } else {
                    // emphasize user-non-friendly exceptions
                    //      if (this.getMessage() == null || "".equals(this.getMessage())) { // NOI18N
                    descriptor.setMessage (
                        java.text.MessageFormat.format(
                            curBundle.getString("NTF_ExceptionalException"),
                            new Object[] {
                                current.getClassName (),
                                CLIOptions.getLogDir ()
                            }
                        )
                    );

                    title = curBundle.getString("NTF_ExceptionalExceptionTitle"); // NOI18N
                }
            }
        }

        descriptor.setTitle (title);
       
    }

    //
    // Handlers
    //

    public void actionPerformed(final java.awt.event.ActionEvent ev) {
        if (ev.getSource () == next && exceptions.setNextElement() || ev.getSource () == previous && exceptions.setPreviousElement()) {
            current = exceptions.get();
            update ();
            // bugfix #27266, don't change the dialog's size when jumping Next<->Previous
            //ensurePreferredSize();
            return;
        }

        if (ev.getSource () == details) {
            showDetails = !showDetails;
            lastBounds = null;
            try {
                update ();
                ensurePreferredSize();
                //throw new RuntimeException ("I am reallly exceptional!"); //uncomment to test
            } catch (Exception e) {
                //Do not allow an exception thrown here to trigger an endless
                //loop
                exceptions.add(new NbErrorManager().createExc(e, //ugly but works
                    Level.SEVERE, null));
                next.setVisible(true);
            }
            return;
        }

        // bugfix #40834, remove all exceptions to notify when close a dialog
        if (ev.getSource () == DialogDescriptor.OK_OPTION || ev.getSource () == DialogDescriptor.CLOSED_OPTION) {
            try {
                exceptions.removeAll();
            //Fixed bug #9435, call of setVisible(false) replaced by call of dispose()
            //It did not work on Linux when JDialog is reused.
            //dialog.setVisible (false);
            // XXX(-ttran) no, it still doesn't work, getPreferredSize() on the
            // reused dialog returns (0,0).  We stop caching the dialog
            // completely by setting INSTANCE to null here.
                lastBounds = dialog.getBounds();
                dialog.dispose();
                exceptions = null;
                INSTANCE = null;
                //throw new RuntimeException ("You must be exceptional"); //uncomment to test
            } catch (RuntimeException e) {
                //Do not allow window of opportunity when dialog in a possibly
                //inconsistent state may be reuse
                exceptions = null;
                INSTANCE = null;
                throw e;
            } finally {
                exceptions = null;
                INSTANCE = null;
            }
        }
    }


    /** Method that checks whether the level is high enough to be notified
     * at all.
     * @param dialog shall we check for dialog or just a blinking icon (false)
     */
    private static boolean shallNotify(Level level, boolean dialog) {
        int minAlert = Integer.getInteger("netbeans.exception.alert.min.level", 900); // NOI18N
        boolean assertionsOn = false;
        assert assertionsOn = true;
        int defReport = assertionsOn ? 900 : 1001;
        int minReport = Integer.getInteger("netbeans.exception.report.min.level", defReport); // NOI18N

        if (dialog) {
            return level.intValue() >= minReport;
        } else {
            return level.intValue() >= minAlert || level.intValue() >= minReport;
        }
    }
    

    /**
     * The icon shown in the main status bar that is flashing when an exception
     * is encountered.
     */
    static FlashingIcon flasher = null;
    
    /**
     * Return an icon that is flashing when a new internal exception occurs. 
     * Clicking the icon opens the regular exception dialog box. The icon
     * disappears (is hidden) after a short period of time and the exception
     * list is cleared.
     *
     * @return A flashing icon component or null if console logging is switched on.
     */
    public static Component getNotificationVisualizer() {
        //do not create flashing icon if not allowed in system properties
        if( null == flasher ) {
            ImageIcon img1 = new ImageIcon( Utilities.loadImage("org/netbeans/core/resources/exception.gif", true) );
            flasher = new ExceptionFlasher( img1 );
        }
        return flasher;
    }

    private static class ExceptionFlasher extends FlashingIcon {
        public ExceptionFlasher( Icon img1 ) {
            super( img1 );
        }

        /**
         * User clicked the flashing icon, display the exception window.
         */
        protected void onMouseClick() {
            if (null != exceptions && exceptions.size() > 0) {
                if (INSTANCE == null) {
                    INSTANCE = new NotifyExcPanel();
                }
                INSTANCE.updateState(exceptions.get(exceptions.size() - 1));
            }
        }
        
        /**
         * The flashing icon disappeared (timed-out), clear the current
         * exception list.
         */
        protected void timeout() {
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    if( null != INSTANCE )
                        return;
                    if( null != exceptions )
                        exceptions.clear();
                    exceptions = null;
                }
            });
        }
    }

    protected static class ArrayListPos extends ArrayList<NbErrorManager.Exc> {
        protected int position;

        protected ArrayListPos () {
            super();
            position=0;
        }

        protected boolean existsElement () {
            return size()>0;
        }

        protected boolean existsNextElement () {
            return position+1<size();
        }

        protected boolean existsPreviousElement () {
            return position>0&&size()>0;
        }

        protected boolean setNextElement () {
            if(!existsNextElement())
                return false;
            position++;
            return true;
        }

        protected boolean setPreviousElement () {
            if(!existsPreviousElement())
                return false;
            position--;
            return true;
        }

        protected NbErrorManager.Exc get () {
            return existsElement()?get(position):null;
        }

        protected void removeAll () {
            clear();
            position=0;
        }
    }
}
