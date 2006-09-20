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



import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.event.*;
import org.openide.*;
import org.openide.awt.Mnemonics;
import org.openide.util.*;


// XXX Before as org.netbeans.core.NbPresenter

/** Default implementation of Dialog created from NotifyDescriptor.
 *
 * @author Ian Formanek, Jaroslav Tulach
 */
class NbPresenter extends JDialog
implements PropertyChangeListener, WindowListener, Mutex.Action<Void>, Comparator<Object> {
    
    /** variable holding current modal dialog in the system */
    public static NbPresenter currentModalDialog;
    private static final Set<ChangeListener> listeners = new HashSet<ChangeListener>();
    
    protected NotifyDescriptor descriptor;
    
    private final JButton stdYesButton = new JButton(NbBundle.getBundle(NbPresenter.class).getString("YES_OPTION_CAPTION")); // NOI18N
    private final JButton stdNoButton = new JButton(NbBundle.getBundle(NbPresenter.class).getString("NO_OPTION_CAPTION")); // NOI18N
    private final JButton stdOKButton = new JButton(NbBundle.getBundle(NbPresenter.class).getString("OK_OPTION_CAPTION")); // NOI18N
    private final JButton stdCancelButton = new JButton(NbBundle.getBundle(NbPresenter.class).getString("CANCEL_OPTION_CAPTION")); // NOI18N
    private final JButton stdClosedButton = new JButton(NbBundle.getBundle(NbPresenter.class).getString("CLOSED_OPTION_CAPTION")); // NOI18N
    private final JButton stdHelpButton = new JButton();
    private final JButton stdDetailButton = new JButton(NbBundle.getBundle(NbPresenter.class).getString("HELP_OPTION_CAPTION")); // NOI18N
    {
        stdYesButton.setDefaultCapable(true);
        stdOKButton.setDefaultCapable(true);
        stdNoButton.setDefaultCapable(false);
        stdCancelButton.setDefaultCapable(false);
        stdCancelButton.setVerifyInputWhenFocusTarget(false);
        stdClosedButton.setDefaultCapable(false);
        stdHelpButton.setDefaultCapable(false);
        stdDetailButton.setDefaultCapable(false);
        Mnemonics.setLocalizedText (stdHelpButton, NbBundle.getBundle(NbPresenter.class).getString("HELP_OPTION_CAPTION")); // NOI18N
        
        /** Initilizes accessible contexts */
        initAccessibility();
    }
    private final static String ESCAPE_COMMAND = "Escape"; // NOI18N
    
    private Component currentMessage;
    private JScrollPane currentScrollPane;
    private boolean leaf = false;
    private JPanel currentButtonsPanel;
    private Component[] currentPrimaryButtons;
    private Component[] currentSecondaryButtons;
    
    /** useful only for DialogDescriptor */
    private int currentAlign;
    
    private ButtonListener buttonListener;
    /** Help context to actually associate with the dialog, as it is currently known. */
    private transient HelpCtx currentHelp = null;
    /** Used to prevent updateHelp from calling initializeButtons too many times. */
    private transient boolean haveCalledInitializeButtons = false;

    private static Logger LOG = Logger.getLogger(NbPresenter.class.getName());
    
    static final long serialVersionUID =-4508637164126678997L;
    
    /** Creates a new Dialog from specified NotifyDescriptor,
     * with given frame owner.
     * @param d The NotifyDescriptor to create the dialog from
     */
    public NbPresenter(NotifyDescriptor d, Frame owner, boolean modal) {
        super(owner, d.getTitle(), modal); // modal
        initialize(d);
    }
    
    /** Creates a new Dialog from specified NotifyDescriptor,
     * with given dialog owner.
     * @param d The NotifyDescriptor to create the dialog from
     */
    public NbPresenter(NotifyDescriptor d, Dialog owner, boolean modal) {
        super(owner, d.getTitle(), modal); // modal
        initialize(d);
    }
    
    boolean isLeaf () {
        return leaf;
    }
    
    private void initAccessibility(){
        
        ResourceBundle bundle;
        bundle = NbBundle.getBundle(NbPresenter.class);
        
        stdYesButton.getAccessibleContext().setAccessibleName(bundle.getString("ACS_YES_OPTION_NAME")); // NOI18N
        stdYesButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_YES_OPTION_DESC")); // NOI18N
        
        stdNoButton.getAccessibleContext().setAccessibleName(bundle.getString("ACS_NO_OPTION_NAME")); // NOI18N
        stdNoButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_NO_OPTION_DESC")); // NOI18N
        
        stdOKButton.getAccessibleContext().setAccessibleName(bundle.getString("ACS_OK_OPTION_NAME")); // NOI18N
        stdOKButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_OK_OPTION_DESC")); // NOI18N
        
        stdCancelButton.getAccessibleContext().setAccessibleName(bundle.getString("ACS_CANCEL_OPTION_NAME")); // NOI18N
        stdCancelButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CANCEL_OPTION_DESC")); // NOI18N
        
        stdClosedButton.getAccessibleContext().setAccessibleName(bundle.getString("ACS_CLOSED_OPTION_NAME")); // NOI18N
        stdClosedButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_CLOSED_OPTION_DESC")); // NOI18N
        
        stdHelpButton.getAccessibleContext().setAccessibleName(bundle.getString("ACS_HELP_OPTION_NAME")); // NOI18N
        stdHelpButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_HELP_OPTION_DESC")); // NOI18N
        
        stdDetailButton.getAccessibleContext().setAccessibleName(bundle.getString("ACS_HELP_OPTION_NAME")); // NOI18N
        stdDetailButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACS_HELP_OPTION_DESC")); // NOI18N
    }
    
    private void initialize(NotifyDescriptor d) {
        //Optimization related to jdk bug 4393857 - on pre 1.5 jdk's an
        //extra repaint is caused by the search for an opaque component up
        //to the component root. Post 1.5, root pane will automatically be
        //opaque.
        getRootPane().setOpaque(true);
        
        if (d instanceof WizardDescriptor) {
            // #81938: wizard close button shouln't work during finish progress
            setDefaultCloseOperation (WindowConstants.DO_NOTHING_ON_CLOSE);
        } else {
            // #55273: Dialogs created by DialogDisplayer are not disposed after close
            setDefaultCloseOperation (WindowConstants.DISPOSE_ON_CLOSE);
        }
        
        descriptor = d;

        buttonListener = new ButtonListener();
        // set leaf by DialogDescriptor, NotifyDescriptor is leaf as default
        leaf = d instanceof DialogDescriptor ? ((DialogDescriptor)d).isLeaf () : true;
        
        getRootPane().registerKeyboardAction(
            buttonListener,
            ESCAPE_COMMAND,
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT
        );

        initializePresenter();

        pack();
        setBounds(Utilities.findCenterBounds(getSize()));
    }

    /** Requests focus for <code>currentMessage</code> component.
     * If it is of <code>JComponent</code> type it tries default focus
     * request first. */
    private void requestFocusForMessage() {
        Component comp = currentMessage;
        
        if(comp == null) {
            return;
        }
        
        if(!(comp instanceof JComponent) 
            || !((JComponent)comp).requestDefaultFocus()) {
                
            comp.requestFocus();
        }
    }
    
    private void initializeMessage() {
        Object newMessage = descriptor.getMessage();
        // replace only if old and new messages are different
        if ((currentMessage == null) || !currentMessage.equals(newMessage)) {
            uninitializeMessage();

            if (descriptor.getMessageType() == NotifyDescriptor.PLAIN_MESSAGE &&
                (newMessage instanceof Component)) {
                // if plain message => use directly the component
                currentMessage = (Component)newMessage;
            } else {
                currentMessage = createOptionPane();
            }
            Dimension prefSize = currentMessage.getPreferredSize();
            final Rectangle screenBounds = Utilities.getUsableScreenBounds();
            
            if (prefSize.width > screenBounds.width - 100
                || prefSize.height > screenBounds.height- 100
                ) {
                currentScrollPane = new JScrollPane() {
                    public Dimension getPreferredSize() {
                        Dimension sz = new Dimension(super.getPreferredSize());
                        if (sz.width > screenBounds.width - 100) {
                            sz.width = screenBounds.width * 3 / 4;
                        }
                        if (sz.height > screenBounds.height - 100)
                            sz.height = screenBounds.height * 3 / 4;
                        return sz;
                    }
                };
                currentScrollPane.setViewportView(currentMessage);
                getContentPane().add(currentScrollPane, BorderLayout.CENTER);
            }
            else {
                getContentPane().add(currentMessage, BorderLayout.CENTER);
            }
        }
    }
    
    private void uninitializeMessage() {
        if (currentMessage != null) {
            if (currentScrollPane != null) {
                getContentPane().remove(currentScrollPane);
                currentScrollPane = null;
            }
            else {
                getContentPane().remove(currentMessage);
            }
            currentMessage = null;
        }
    }

    private void initializePresenter() {
        if (currentMessage != null) 
            return;
            
        initializeMessage();
        
        updateHelp();
        
        initializeButtons();
        haveCalledInitializeButtons = true;
        
        descriptor.addPropertyChangeListener(this);
        addWindowListener(this);
        
        initializeClosingOptions ();
    }
    
    /** Descriptor can be cached and reused. We need to remove listeners 
     *  from descriptor, buttons and disconnect componets from container hierarchy.
     */
    private void uninitializePresenter() {
        descriptor.removePropertyChangeListener(this);
        uninitializeMessage();
        uninitializeButtons();
        uninitializeClosingOptions ();
    }
    
    private final HackTypeAhead hack = new HackTypeAhead();
    public void addNotify() {
        super.addNotify();
        initializePresenter();
        
        hack.activate();
    }

    public void removeNotify() {
        super.removeNotify();
        uninitializePresenter();
        
    }

    /** Creates option pane message.
     */
    private JOptionPane createOptionPane() {
        Object msg = descriptor.getMessage();
        boolean override = true;
        String strMsg = null, strMsgLower;
        
        if (msg instanceof String) {
            msg = org.openide.util.Utilities.replaceString((String)msg, "\t", "    "); // NOI18N
            msg = org.openide.util.Utilities.replaceString((String)msg, "\r", ""); // NOI18N
            //If string is html text (contains "<html>" or "<HTML>")
            //we will not override JOptionPane.getMaxCharactersPerLineCount
            //so that html text will be displayed correctly in JOptionPane
            strMsg = (String)msg;
            strMsgLower = strMsg.toLowerCase();
            override = !strMsgLower.startsWith("<html>"); // NOI18N
        }
        if (msg instanceof javax.accessibility.Accessible) {
            strMsg = ((javax.accessibility.Accessible)msg).getAccessibleContext().getAccessibleDescription();
        }
        
        JOptionPane optionPane;
        if (override) {
            // initialize component (override max char count per line in a message)
            optionPane = new JOptionPane(
            msg,
            descriptor.getMessageType(),
            0, // options type
            null, // icon
            new Object[0], // options
            null // value
            ) {
                public int getMaxCharactersPerLineCount() {
                    return 100;
                }
            };
        } else {
            //Do not override JOptionPane.getMaxCharactersPerLineCount for html text
            optionPane = new JOptionPane(
            msg,
            descriptor.getMessageType(),
            0, // options type
            null, // icon
            new Object[0], // options
            null // value
            );
        }
        optionPane.setUI(new javax.swing.plaf.basic.BasicOptionPaneUI() {
            public Dimension getMinimumOptionPaneSize() {
                if (minimumSize == null) {
                    //minimumSize = UIManager.getDimension("OptionPane.minimumSize");
                    // this is called before defaults initialized?!!!
                    return new Dimension(MinimumWidth, 50);
                }
                return new Dimension(minimumSize.width, 50);
            }
        });
        optionPane.setWantsInput(false);
        optionPane.getAccessibleContext().setAccessibleDescription(strMsg);
        
        return optionPane;
    }
    
    private void uninitializeButtons() {
        if (currentButtonsPanel != null) {
            if (currentPrimaryButtons != null) {
                for (int i = 0; i < currentPrimaryButtons.length; i++) {
                    modifyListener(currentPrimaryButtons[i], buttonListener, false);
                }
            }
            if (currentSecondaryButtons != null) {
                for (int i = 0; i < currentSecondaryButtons.length; i++) {
                    modifyListener(currentSecondaryButtons[i], buttonListener, false);
                }
            }
            
            getContentPane().remove(currentButtonsPanel);
            currentButtonsPanel = null;
        }
    }
    
    private void initializeClosingOptions (boolean init) {
        Object[] options = getClosingOptions ();
        
        if (options == null) return ;
        for (int i = 0; i < options.length; i++) {
            modifyListener (options[i], buttonListener, init);
        }
    }
    
    private void initializeClosingOptions () {
        initializeClosingOptions (true);
    }
    
    private void uninitializeClosingOptions () {
        initializeClosingOptions (false);
    }
    
    /**
     * On Aqua look and feel, options should be sorted such that the default
     * button is always rightmost, and 'yes' options appear to thr right of
     * 'no' options.
     */
    public int compare (Object a, Object b) {
        boolean isDefaultButton = a.equals(descriptor.getDefaultValue ());
        int result;
        if (a.equals(NotifyDescriptor.OK_OPTION) || a.equals(NotifyDescriptor.YES_OPTION)) {
            result = 1;
        } else {
            result = 0;
        }
        
        if (isDefaultButton) {
            result++;
        }
        return result;
    }
    
    protected final void initializeButtons() {
        // -----------------------------------------------------------------------------
        // If there were any buttons previously, remove them and removeActionListener from them
        
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager ().getFocusOwner ();

        boolean dontShowHelp = ( descriptor instanceof WizardDescriptor && ( Boolean.FALSE.equals (((WizardDescriptor)descriptor).getProperty ("WizardPanel_helpDisplayed")) )); // NOI18N
        boolean helpButtonShown =
            stdHelpButton.isShowing() || ( descriptor instanceof WizardDescriptor && !dontShowHelp );
        
        
        uninitializeButtons();
        
        Object[] primaryOptions = descriptor.getOptions();
        Object[] secondaryOptions = descriptor.getAdditionalOptions();
        currentAlign = getOptionsAlign();
        
        // -----------------------------------------------------------------------------
        // Obtain main (primary) and additional (secondary) buttons
        
        currentPrimaryButtons = null;
        currentSecondaryButtons = null;

        boolean isAqua = "Aqua".equals (UIManager.getLookAndFeel().getID()) || //NOI18N
                        "true".equalsIgnoreCase (System.getProperty ("xtest.looks_as_mac"));
        if (isAqua) {
            //No mac dialogs with buttons on side
            currentAlign = DialogDescriptor.BOTTOM_ALIGN;
        }
        
        // explicitly provided options (AKA buttons)
        // JST: The following line causes only problems,
        //      I hope that my change will not cause additional ones ;-)
        //    if (descriptor.getOptionType () == NotifyDescriptor.DEFAULT_OPTION) {
        if (primaryOptions != null) {
            if (isAqua) {
                Arrays.sort(primaryOptions, this);
            }
            currentPrimaryButtons = new Component [primaryOptions.length];
            for (int i = 0; i < primaryOptions.length; i++) {
                if (primaryOptions[i] == NotifyDescriptor.YES_OPTION) {
                    currentPrimaryButtons[i] = stdYesButton;
                } else if (primaryOptions[i] == NotifyDescriptor.NO_OPTION) {
                    currentPrimaryButtons[i] = stdNoButton;
                } else if (primaryOptions[i] == NotifyDescriptor.OK_OPTION) {
                    currentPrimaryButtons[i] = stdOKButton;
                    stdOKButton.setEnabled(descriptor.isValid());
                } else if (primaryOptions[i] == NotifyDescriptor.CANCEL_OPTION) {
                    currentPrimaryButtons[i] = stdCancelButton;
                } else if (primaryOptions[i] == NotifyDescriptor.CLOSED_OPTION) {
                    currentPrimaryButtons[i] = stdClosedButton;
                } else if (primaryOptions[i] instanceof Component) {
                    currentPrimaryButtons[i] = (Component) primaryOptions [i];
                } else if (primaryOptions [i] instanceof Icon) {
                    JButton button = new JButton((Icon)primaryOptions [i]);
                    // ??? Why cannot be default capable ?
                    button.setDefaultCapable(false);
                    currentPrimaryButtons[i] = button;
                } else {
                    JButton button = new JButton();
                    Mnemonics.setLocalizedText (button, primaryOptions [i].toString ());
                    button.setDefaultCapable(primaryOptions[i].equals(descriptor.getDefaultValue ()));
                    currentPrimaryButtons[i] = button;
                }
            }
        } else { // predefined option types
            switch (descriptor.getOptionType()) {
                case NotifyDescriptor.YES_NO_OPTION:
                    if (isAqua) {
                        currentPrimaryButtons = new Component[2];
                        currentPrimaryButtons[0] = stdNoButton;
                        currentPrimaryButtons[1] = stdYesButton;
                    } else {
                        currentPrimaryButtons = new Component[2];
                        currentPrimaryButtons[0] = stdYesButton;
                        currentPrimaryButtons[1] = stdNoButton;
                    }
                    break;
                case NotifyDescriptor.YES_NO_CANCEL_OPTION:
                    currentPrimaryButtons = new Component[3];
                    if (isAqua) {
                        currentPrimaryButtons[0] = stdCancelButton;
                        currentPrimaryButtons[1] = stdNoButton;
                        currentPrimaryButtons[2] = stdYesButton;
                    } else {
                        currentPrimaryButtons[0] = stdYesButton;
                        currentPrimaryButtons[1] = stdNoButton;
                        currentPrimaryButtons[2] = stdCancelButton;
                    }
                    break;
                case NotifyDescriptor.OK_CANCEL_OPTION:
                default:
                    if (isAqua) {
                        currentPrimaryButtons = new Component[2];
                        currentPrimaryButtons[0] = stdCancelButton;
                        currentPrimaryButtons[1] = stdOKButton;
                    } else {
                        currentPrimaryButtons = new Component[2];
                        currentPrimaryButtons[0] = stdOKButton;
                        currentPrimaryButtons[1] = stdCancelButton;
                    }
                    stdOKButton.setEnabled(descriptor.isValid());
                    break;
            }
        }
        
        // Automatically add a help button if needed.
        
        if (!dontShowHelp && (currentHelp != null || helpButtonShown)) {
            if (currentPrimaryButtons == null) currentPrimaryButtons = new Component[] { };
            Component[] cPB2 = new Component[currentPrimaryButtons.length + 1];
            if (isAqua) { //NOI18N
                //Mac default dlg button should be rightmost, not the help button
                System.arraycopy(currentPrimaryButtons, 0, cPB2, 1, currentPrimaryButtons.length);
                cPB2[0] = stdHelpButton;
            } else {
                System.arraycopy(currentPrimaryButtons, 0, cPB2, 0, currentPrimaryButtons.length);
                cPB2[currentPrimaryButtons.length] = stdHelpButton;
            }
            currentPrimaryButtons = cPB2;

            stdHelpButton.setEnabled(currentHelp != null);
        }
        
        if ((secondaryOptions != null) && (secondaryOptions.length != 0)) {
            currentSecondaryButtons = new Component [secondaryOptions.length];
            Arrays.sort (secondaryOptions, this);
            for (int i = 0; i < secondaryOptions.length; i++) {
                if (secondaryOptions[i] == NotifyDescriptor.YES_OPTION) {
                    currentSecondaryButtons[i] = stdYesButton;
                } else if (secondaryOptions[i] == NotifyDescriptor.NO_OPTION) {
                    currentSecondaryButtons[i] = stdNoButton;
                } else if (secondaryOptions[i] == NotifyDescriptor.OK_OPTION) {
                    currentSecondaryButtons[i] = stdOKButton;
                    stdOKButton.setEnabled(descriptor.isValid());
                } else if (secondaryOptions[i] == NotifyDescriptor.CANCEL_OPTION) {
                    currentSecondaryButtons[i] = stdCancelButton;
                } else if (secondaryOptions[i] == NotifyDescriptor.CLOSED_OPTION) {
                    currentSecondaryButtons[i] = stdClosedButton;
                } else if (secondaryOptions[i] instanceof Component) {
                    currentSecondaryButtons[i] = (Component) secondaryOptions [i];
                } else if (secondaryOptions [i] instanceof Icon) {
                    JButton button = new JButton((Icon)secondaryOptions [i]);
                    currentSecondaryButtons[i] = button;
                } else {
                    JButton button = new JButton();
                    Mnemonics.setLocalizedText (button, secondaryOptions [i].toString ());
                    currentSecondaryButtons[i] = button;
                }
            }
        }
        
        // -----------------------------------------------------------------------------
        // Create panels for main (primary) and additional (secondary) buttons and add to content pane
        
        if (currentAlign == DialogDescriptor.BOTTOM_ALIGN || currentAlign == -1) {
            
            JPanel panelForPrimary = null;
            JPanel panelForSecondary = null;
            

            if (currentPrimaryButtons != null) {
                panelForPrimary = new JPanel();
                
                if (currentAlign == -1) {
                    panelForPrimary.setLayout(new org.openide.awt.EqualFlowLayout());
                } else {
                    panelForPrimary.setLayout(new org.openide.awt.EqualFlowLayout(FlowLayout.RIGHT));
                }
                for (int i = 0; i < currentPrimaryButtons.length; i++) {
                    modifyListener(currentPrimaryButtons[i], buttonListener, true); // add button listener
                    panelForPrimary.add(currentPrimaryButtons[i]);
                }
            }
            
            if (currentSecondaryButtons != null) {
                panelForSecondary = new JPanel();
                panelForSecondary.setLayout(new org.openide.awt.EqualFlowLayout(FlowLayout.LEFT));
                for (int i = 0; i < currentSecondaryButtons.length; i++) {
                    modifyListener(currentSecondaryButtons[i], buttonListener, true); // add button listener
                    panelForSecondary.add(currentSecondaryButtons[i]);
                }
            }
            
            // both primary and secondary buttons are used
            if ((panelForPrimary != null) && (panelForSecondary != null)) {
                currentButtonsPanel = new JPanel();
                currentButtonsPanel.setLayout(new BorderLayout());
                currentButtonsPanel.add(panelForPrimary, BorderLayout.EAST);
                currentButtonsPanel.add(panelForSecondary, BorderLayout.WEST);
            } else if (panelForPrimary != null) {
                currentButtonsPanel = panelForPrimary;
            } else {
                currentButtonsPanel = panelForSecondary;
            }
            
            // add final button panel to the dialog
            if ((currentButtonsPanel != null)&&(currentButtonsPanel.getComponentCount() != 0)) {
                currentButtonsPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(11, 6, 5, 5)));
                getContentPane().add(currentButtonsPanel, BorderLayout.SOUTH);
            }
            
        } else if (currentAlign == DialogDescriptor.RIGHT_ALIGN) {
            currentButtonsPanel = new JPanel();
            currentButtonsPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0f;
            gbc.insets = new Insets(5, 4, 2, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            
            if (currentPrimaryButtons != null) {
                for (int i = 0; i < currentPrimaryButtons.length; i++) {
                    modifyListener(currentPrimaryButtons[i], buttonListener, true); // add button listener
                    currentButtonsPanel.add(currentPrimaryButtons[i], gbc);
                }
            }
            
            GridBagConstraints padding = new GridBagConstraints();
            padding.gridwidth = GridBagConstraints.REMAINDER;
            padding.weightx = 1.0f;
            padding.weighty = 1.0f;
            padding.fill = GridBagConstraints.BOTH;
            currentButtonsPanel.add(new JPanel(), padding);
            
            gbc.insets = new Insets(2, 4, 5, 5);
            if (currentSecondaryButtons != null) {
                for (int i = 0; i < currentSecondaryButtons.length; i++) {
                    modifyListener(currentSecondaryButtons[i], buttonListener, true); // add button listener
                    currentButtonsPanel.add(currentSecondaryButtons[i], gbc);
                }
            }
            
            // add final button panel to the dialog
            if (currentButtonsPanel != null) {
                currentButtonsPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(6, 7, 5, 5)));
                getContentPane().add(currentButtonsPanel, BorderLayout.EAST);
            }
            
        }
        updateDefaultButton();
        
        
        Component fo = KeyboardFocusManager.getCurrentKeyboardFocusManager ().getFocusOwner ();
        
        if (fo != focusOwner && focusOwner != null) {
            focusOwner.requestFocus();
        }
    }
    
    /** Checks default button and updates it
     */
    private void updateDefaultButton() {
        // bugfix 37083, respects DialogDescriptor's initial value ?
        if (descriptor.getDefaultValue () != null && descriptor.getDefaultValue () instanceof JButton) {
            JButton b = (JButton)descriptor.getDefaultValue ();
            if (b.isVisible() && b.isEnabled () && b.isDefaultCapable ()) {
                getRootPane ().setDefaultButton (b);
                return ;
            }
        } else {
            // ??? unset default button if descriptor.getValue() is null
        }
        if (currentPrimaryButtons != null) {
            // finds default button
            for (int i = 0; i < currentPrimaryButtons.length; i++) {
                if (currentPrimaryButtons[i] instanceof JButton) {
                    JButton b = (JButton)currentPrimaryButtons[i];
                    if (b.isVisible() && b.isEnabled() && b.isDefaultCapable()) {
                        getRootPane().setDefaultButton(b);
                        return;
                    }
                }
            }
        }
        // no default capable button found
        getRootPane().setDefaultButton(null);
    }
    
    /** Enables/disables OK button if it is present
     */
    private void updateOKButton(boolean valid) {
        if (currentPrimaryButtons != null) {
            for (int i = 0; i < currentPrimaryButtons.length; i++) {
                if (currentPrimaryButtons[i] instanceof JButton) {
                    JButton b = (JButton)currentPrimaryButtons[i];
                    if ((b == stdOKButton) && b.isVisible()) {
                        b.setEnabled(valid);
                    }
                }
            }
        }
        if (currentSecondaryButtons != null) {
            for (int i = 0; i < currentSecondaryButtons.length; i++) {
                if (currentSecondaryButtons[i] instanceof JButton) {
                    JButton b = (JButton)currentSecondaryButtons[i];
                    if ((b == stdOKButton) && b.isVisible()) {
                        b.setEnabled(valid);
                    }
                }
            }
        }
    }
    
    private void modifyListener(Object comp, ButtonListener l, boolean add) {
        // on JButtons attach simply by method call
        if (comp instanceof JButton) {
            JButton b = (JButton)comp;
            if (add) { 
                List listeners;
                listeners = Arrays.asList (b.getActionListeners ());
                if (!listeners.contains (l)) {
                    b.addActionListener(l);
                }
                listeners = Arrays.asList (b.getComponentListeners ());
                if (!listeners.contains (l)) {
                    b.addComponentListener(l);
                }
                listeners = Arrays.asList (b.getPropertyChangeListeners ());
                if (!listeners.contains (l)) {
                    b.addPropertyChangeListener(l);
                }
            } else {
                b.removeActionListener(l);
                b.removeComponentListener(l);
                b.removePropertyChangeListener(l);
            }
            return;
        } else {
            // we will have to use dynamic method invocation to add the action listener
            // to generic component (and we succeed only if it has the addActionListener method)
            java.lang.reflect.Method m = null;
            try {
                m = comp.getClass().getMethod(add ? "addActionListener" : "removeActionListener", new Class[] { ActionListener.class });// NOI18N
                try {
                    m.setAccessible (true);
                } catch (SecurityException se) {
                    m = null; // no jo, we cannot make accessible
                }
            } catch (NoSuchMethodException e) {
                m = null; // no jo, we cannot attach ActionListener to this Component
            } catch (SecurityException e2) {
                m = null; // no jo, we cannot attach ActionListener to this Component
            }
            if (m != null) {
                try {
                    m.invoke(comp, new Object[] { l });
                } catch (Exception e) {
                    // not succeeded, so give up
                }
            }
        }
    }
    
    /** Shows the dialog, used in method show so no inner class is needed.
     */
    private void superShow() {
        assert SwingUtilities.isEventDispatchThread () : "Invoked super.show() in AWT event thread."; // NOI18N
        super.show();
    }
    
    @Override
    public void show() {
        //Bugfix #29993: Call show() asynchronously for non modal dialogs.
        if (isModal()) {
            Mutex.EVENT.readAccess(this);
        } else {
            if (SwingUtilities.isEventDispatchThread()) {
                doShow();
            } else {
                SwingUtilities.invokeLater(new Runnable () {
                    public void run () {
                        doShow();
                    }
                });
            }
        }
    }
    
    public Void run() {
        doShow();
        return null;
    }
    
    private void doShow () {
        NbPresenter prev = null;
        if (isModal()) {
            prev = currentModalDialog;
            currentModalDialog = this;
            fireChangeEvent();
        }
        
        superShow();
        
        if (currentModalDialog != prev) {
            currentModalDialog = prev;
            fireChangeEvent();
        }
    }
    
    public void propertyChange(final java.beans.PropertyChangeEvent evt) {
        boolean update = false;
        
        if (DialogDescriptor.PROP_OPTIONS.equals(evt.getPropertyName())) {
            initializeButtons();
            update = true;
        } else if (DialogDescriptor.PROP_OPTION_TYPE.equals(evt.getPropertyName())) {
            initializeButtons();
            update = true;
        } else if (DialogDescriptor.PROP_OPTIONS_ALIGN.equals(evt.getPropertyName())) {
            initializeButtons();
            update = true;
        } else if (DialogDescriptor.PROP_MESSAGE.equals(evt.getPropertyName())) {
            initializeMessage();
            requestFocusForMessage();
            // In case change of help ID on component message:
            updateHelp();
            update = true;
        } else if (DialogDescriptor.PROP_MESSAGE_TYPE.equals(evt.getPropertyName())) {
            initializeMessage();
            requestFocusForMessage();
            update = true;
        } else if (DialogDescriptor.PROP_TITLE.equals(evt.getPropertyName())) {
            setTitle(descriptor.getTitle());
        } else if (DialogDescriptor.PROP_HELP_CTX.equals(evt.getPropertyName())) {
            // bugfix #40057, restore focus owner after help update
            Component fo = KeyboardFocusManager.getCurrentKeyboardFocusManager ().getFocusOwner ();
            updateHelp();
            // In case buttons have changed: //just buttons!!
            currentButtonsPanel.revalidate();
            currentButtonsPanel.repaint();
            if (fo != null) fo.requestFocus();
        } else if (DialogDescriptor.PROP_VALID.equals(evt.getPropertyName())) {
            updateOKButton(((Boolean)(evt.getNewValue())).booleanValue());
        }
        
        if (update) {
            Dimension sz = getSize();
            Dimension prefSize = getPreferredSize();
            if (prefSize.width > sz.width || prefSize.height > sz.height) {
                setSize(Math.max(prefSize.width, sz.width),
                        Math.max(prefSize.height, sz.height));
            }

            validate();
            repaint();
        }
    }
    
    private void updateHelp() {
        //System.err.println ("Updating help for NbDialog...");
        HelpCtx help = getHelpCtx();
        // Handle help from the inner component automatically (see docs
        // in DialogDescriptor):
        if (HelpCtx.DEFAULT_HELP.equals(help)) {
            Object msg = descriptor.getMessage();
            if (msg instanceof Component) {
                help = HelpCtx.findHelp((Component) msg);
            }
            if (HelpCtx.DEFAULT_HELP.equals(help)) help = null;
        }
        if (! Utilities.compareObjects(currentHelp, help)) {
            currentHelp = help;
            if (help != null && help.getHelpID() != null) {
                //System.err.println ("New help ID for root pane: " + help.getHelpID ());
                HelpCtx.setHelpIDString(getRootPane(), help.getHelpID());
            }
            // Refresh button list if it had already been created.
            if (haveCalledInitializeButtons) initializeButtons();
        }
    }
    
    /** Options align.
     */
    protected int getOptionsAlign() {
        return -1;
    }
    
    /** Getter for button listener or null
     */
    protected ActionListener getButtonListener() {
        return null;
    }
    
    /** Closing options.
     */
    protected Object[] getClosingOptions() {
        return null;
    }
    
    /** Updates help.
     */
    protected HelpCtx getHelpCtx() {
        return null;
    }
    
    
    public void windowDeactivated(final java.awt.event.WindowEvent p1) {
    }
    public void windowClosed(final java.awt.event.WindowEvent p1) {
    }
    public void windowDeiconified(final java.awt.event.WindowEvent p1) {
    }
    public void windowOpened(final java.awt.event.WindowEvent p1) {
    }
    public void windowIconified(final java.awt.event.WindowEvent p1) {
    }
    public void windowClosing(final java.awt.event.WindowEvent p1) {
        // #81938: special handling WizardDescriptor to avoid close wizard during instantiate
        if (! (descriptor instanceof WizardDescriptor)) {
            descriptor.setValue(NotifyDescriptor.CLOSED_OPTION);
        }
    }
    public void windowActivated(final java.awt.event.WindowEvent p1) {
    }
    
    // Used by JavaHelp:
    public static void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public static void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    private static void fireChangeEvent() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Iterator<ChangeListener> it;
                synchronized (listeners) {
                    it = new HashSet<ChangeListener>(listeners).iterator();
                }
                ChangeEvent ev = new ChangeEvent(NbPresenter.class);
                while (it.hasNext()) {
                    it.next().stateChanged(ev);
                }
            }
        });
    }
    
    /** Button listener
     */
    private class ButtonListener implements ActionListener, ComponentListener, PropertyChangeListener {
        ButtonListener() {}
        public void actionPerformed(ActionEvent evt) {
            boolean isAqua = "Aqua".equals (UIManager.getLookAndFeel().getID()) || //NOI18N
                            "true".equalsIgnoreCase (System.getProperty ("xtest.looks_as_mac"));
            
            Object pressedOption = evt.getSource();
            // handle ESCAPE
            if (ESCAPE_COMMAND.equals (evt.getActionCommand ())) {
                pressedOption = NotifyDescriptor.CLOSED_OPTION;
            } else {
                // handle buttons
                if (evt.getSource() == stdHelpButton) {
                    org.netbeans.core.NbTopManager.get().showHelp(currentHelp);
                    return;
                }
                
                Object[] options = descriptor.getOptions();
                if (isAqua && options != null) {
                    Arrays.sort (options, NbPresenter.this);
                }
                
                if (
                options != null &&
                currentPrimaryButtons != null &&
                options.length == (currentPrimaryButtons.length - 
                    ((currentHelp != null) ? 1 : 0))
                ) {
                    int offset = currentHelp != null && isAqua ?
                        -1 : 0;
                    for (int i = 0; i < currentPrimaryButtons.length; i++) {
                        if (evt.getSource() == currentPrimaryButtons[i]) {
                            pressedOption = options[i + offset];
                        }
                    }
                }
                
                options = descriptor.getAdditionalOptions();
                if (isAqua && options != null) {
                    Arrays.sort (options, NbPresenter.this);
                }
                
                if (
                options != null &&
                currentSecondaryButtons != null &&
                options.length == currentSecondaryButtons.length
                ) {
                    for (int i = 0; i < currentSecondaryButtons.length; i++) {
                        if (evt.getSource() == currentSecondaryButtons[i]) {
                            pressedOption = options[i];
                        }
                    }
                }
                
                if (evt.getSource() == stdYesButton) {
                    pressedOption = NotifyDescriptor.YES_OPTION;
                } else if (evt.getSource() == stdNoButton) {
                    pressedOption = NotifyDescriptor.NO_OPTION;
                } else if (evt.getSource() == stdCancelButton) {
                    pressedOption = NotifyDescriptor.CANCEL_OPTION;
                } else if (evt.getSource() == stdClosedButton) {
                    pressedOption = NotifyDescriptor.CLOSED_OPTION;
                } else if (evt.getSource() == stdOKButton) {
                    pressedOption = NotifyDescriptor.OK_OPTION;
                }
            }

            descriptor.setValue(pressedOption);
            
            ActionListener al = getButtonListener();
            if (al != null) {
                
                if (pressedOption == evt.getSource()) {
                    al.actionPerformed(evt);
                } else {
                    al.actionPerformed(new ActionEvent(
                    pressedOption, evt.getID(), evt.getActionCommand(), evt.getModifiers()
                    ));
                }
            }
            
            Object[] arr = getClosingOptions();
            if (arr == null || pressedOption == NotifyDescriptor.CLOSED_OPTION) {
                // all options should close
                dispose();
            } else {
                java.util.List l = java.util.Arrays.asList(arr);
                
                if (l.contains(pressedOption)) {
                    dispose();
                }
            }
        }
        public void componentShown(final java.awt.event.ComponentEvent p1) {
            updateDefaultButton();
        }
        public void componentResized(final java.awt.event.ComponentEvent p1) {
        }
        
        public void componentHidden(final java.awt.event.ComponentEvent p1) {
            updateDefaultButton();
        }
        
        public void componentMoved(final java.awt.event.ComponentEvent p1) {
        }
        
        public void propertyChange(final java.beans.PropertyChangeEvent p1) {
            if ("enabled".equals(p1.getPropertyName())) {
                updateDefaultButton();
            }
        }
    }
    
    public javax.accessibility.AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleNbPresenter();
        }
        return accessibleContext;
    }
    
    private static String getMessageTypeDescription(int messageType) {
        switch(messageType) {
        case NotifyDescriptor.ERROR_MESSAGE:
            return NbBundle.getBundle(NbPresenter.class).getString("ACSD_ErrorMessage"); // NOI18N
        case NotifyDescriptor.WARNING_MESSAGE:
            return NbBundle.getBundle(NbPresenter.class).getString("ACSD_WarningMessage"); // NOI18N
        case NotifyDescriptor.QUESTION_MESSAGE:
            return NbBundle.getBundle(NbPresenter.class).getString("ACSD_QuestionMessage"); // NOI18N
        case NotifyDescriptor.INFORMATION_MESSAGE:
            return NbBundle.getBundle(NbPresenter.class).getString("ACSD_InformationMessage"); // NOI18N
        case NotifyDescriptor.PLAIN_MESSAGE:
            return NbBundle.getBundle(NbPresenter.class).getString("ACSD_PlainMessage"); // NOI18N
        }
        return ""; // NOI18N
    }

    private class AccessibleNbPresenter extends AccessibleJDialog {
        AccessibleNbPresenter() {}
        public String getAccessibleName() {
            if (accessibleName != null) {
                return accessibleName;
            } else {
                if (currentMessage instanceof javax.accessibility.Accessible
                    && currentMessage.getAccessibleContext().getAccessibleName() != null) {
                    return currentMessage.getAccessibleContext().getAccessibleName();
                } else {
                    return super.getAccessibleName();
                }
            }
        }
        public String getAccessibleDescription() {
            if (accessibleDescription != null) {
                return accessibleDescription;
            } else {
                if (currentMessage instanceof javax.accessibility.Accessible
                    && currentMessage.getAccessibleContext().getAccessibleDescription() != null) {
                    return java.text.MessageFormat.format(
                        getMessageTypeDescription(descriptor.getMessageType()),
                        new Object[] {
                            currentMessage.getAccessibleContext().getAccessibleDescription()
                        }
                    );
                } else {
                    return super.getAccessibleDescription();
                }
            }
        }
    }

    static Field markers;
    static Method dequeue;
    static {
        if (Boolean.getBoolean("netbeans.hack.50423")) { // NOI18N
            try {
                markers = DefaultKeyboardFocusManager.class.getDeclaredField("typeAheadMarkers"); // NOI18N
                markers.setAccessible(true);
                dequeue = DefaultKeyboardFocusManager.class.getDeclaredMethod("dequeueKeyEvents", new Class[] { Long.TYPE, java.awt.Component.class });
                dequeue.setAccessible(true);
            } catch (Throwable ex) {
                LOG.log(Level.WARNING, "Not activating workaround for #50423", ex); // NOI18N
            }
        }
    }
    
    private final class HackTypeAhead implements Runnable {
        private RequestProcessor.Task task = RequestProcessor.getDefault().create(this);
        
        
        public HackTypeAhead() {
        }
        
        public void activate() {
            if (markers != null) {
                task.schedule(1000);
            }
        }
        
        public void run() {
            if (!SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater(this);
                return;
            }

            KeyboardFocusManager fm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            Collection result = null;
            try {
                result = (Collection) markers.get(fm);
            } catch (Exception ex) {
                Logger.global.log(Level.WARNING, null, ex);
            }

            if (result == null || result.isEmpty()) {
                return;
            }

            LOG.warning("Symptoms of #50423: There is something in type ahead: " + result + " requesting focus change"); // NOI18N
            try {
                dequeue.invoke(fm, new Object[] { Long.valueOf(-1), NbPresenter.this });
            } catch (Exception ex) {
                Logger.global.log(Level.WARNING, null, ex);
            }
        }
    } // end of HackTypeAhead
}
