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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.accessibility.Accessible;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

/**
 * Implements a basic "wizard" GUI system.
 * A list of <em>wizard panels</em> may be specified and these
 * may be traversed at the proper times using the "Previous"
 * and "Next" buttons (or "Finish" on the last one).
 * @see DialogDisplayer#createDialog
 * @see <a href="doc-files/wizard-guidebook.html">Wizard Guidebook
 * (describes the set of properties controlling the display of wizard panels)</a>
 */
public class WizardDescriptor extends DialogDescriptor {
    /** "Next" button option.
    * @see #setOptions */
    public static final Object NEXT_OPTION = new String("NEXT_OPTION"); // NOI18N

    /** "Finish" button option.
    * @see #setOptions */
    public static final Object FINISH_OPTION = OK_OPTION;

    /** "Previous" button option.
    * @see #setOptions */
    public static final Object PREVIOUS_OPTION = new String("PREVIOUS_OPTION"); // NOI18N
    private static final ActionListener CLOSE_PREVENTER = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            }

            public String toString() {
                return "CLOSE_PREVENTER"; // NOI18N
            }
        };

    /** <CODE>Boolean</CODE> property. The value is taken from <CODE>WizardDescriptor.getProperty()</CODE> or
     * <CODE>((JComponent)Panel.getComponent()).getClientProperty()</CODE> in this order.
     * Set to <CODE>true</CODE> for enabling other properties. It is relevant only on
     * initialization (client property in first panel).
     * When false or not present in JComponent.getClientProperty(), then supplied panel is
     * used directly without content, help or panel name auto layout.
     */
    private static final String PROP_AUTO_WIZARD_STYLE = "WizardPanel_autoWizardStyle"; // NOI18N

    /** <CODE>Boolean</CODE> property. The value is taken from <CODE>WizardDescriptor.getProperty()</CODE> or
     * <CODE>((JComponent)Panel.getComponent()).getClientProperty()</CODE> in this order.
     * Set to <CODE>true</CODE> for showing help pane in the left pane. It is relevant only on
     * initialization (client property in first panel).
     */
    private static final String PROP_HELP_DISPLAYED = "WizardPanel_helpDisplayed"; // NOI18N

    /** <CODE>Boolean</CODE> property. The value is taken from <CODE>WizardDescriptor.getProperty()</CODE> or
     * <CODE>((JComponent)Panel.getComponent()).getClientProperty()</CODE> in this order.
     * Set to <CODE>true</CODE> for showing content pane in the left pane. It is relevant only on
     * initialization (client property in first panel).
     */
    private static final String PROP_CONTENT_DISPLAYED = "WizardPanel_contentDisplayed"; // NOI18N

    /** <CODE>Boolean</CODE> property. The value is taken from <CODE>WizardDescriptor.getProperty()</CODE> or
     * <CODE>((JComponent)Panel.getComponent()).getClientProperty()</CODE> in this order.
     * Set to <CODE>true</CODE> for displaying numbers in the content. It is relevant only on
     * initialization (client property in first panel).
     */
    private static final String PROP_CONTENT_NUMBERED = "WizardPanel_contentNumbered"; // NOI18N

    /** <CODE>Integer</CODE> property. The value is taken from <CODE>WizardDescriptor.getProperty()</CODE> or
     * <CODE>((JComponent)Panel.getComponent()).getClientProperty()</CODE> in this order.
     * Represents index of content item which will be highlited.
     */
    private static final String PROP_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex"; // NOI18N

    /** <CODE>String[]</CODE> property. The value is taken from <CODE>WizardDescriptor.getProperty()</CODE> or
     * <CODE>((JComponent)Panel.getComponent()).getClientProperty()</CODE> in this order.
     * Represents array of content items.
     */
    private static final String PROP_CONTENT_DATA = "WizardPanel_contentData"; // NOI18N

    /** <CODE>Color</CODE> property. The value is taken from <CODE>WizardDescriptor.getProperty()</CODE> or
     * <CODE>((JComponent)Panel.getComponent()).getClientProperty()</CODE> in this order.
     * Set to background color of content pane.
     */
    private static final String PROP_CONTENT_BACK_COLOR = "WizardPanel_contentBackColor"; // NOI18N

    /** <CODE>Color</CODE> property. The value is taken from <CODE>WizardDescriptor.getProperty()</CODE> or
     * <CODE>((JComponent)Panel.getComponent()).getClientProperty()</CODE> in this order.
     * Set to foreground color of content pane.
     */
    private static final String PROP_CONTENT_FOREGROUND_COLOR = "WizardPanel_contentForegroundColor"; // NOI18N

    /** <CODE>Image</CODE> property. The value is taken from <CODE>WizardDescriptor.getProperty()</CODE> or
     * <CODE>((JComponent)Panel.getComponent()).getClientProperty()</CODE> in this order.
     * Set to image which should be displayed in the left pane (behind the content).
     */
    private static final String PROP_IMAGE = "WizardPanel_image"; // NOI18N

    /** <CODE>String</CODE> property. The value is taken from <CODE>WizardDescriptor.getProperty()</CODE> or
     * <CODE>((JComponent)Panel.getComponent()).getClientProperty()</CODE> in this order.
     * Set to side where the image should be drawn.
     */
    private static final String PROP_IMAGE_ALIGNMENT = "WizardPanel_imageAlignment"; // NOI18N

    /** <CODE>Dimension</CODE> property. The value is taken from <CODE>WizardDescriptor.getProperty()</CODE> or
     * <CODE>((JComponent)Panel.getComponent()).getClientProperty()</CODE> in this order.
     * Dimension of left pane, should be same as dimension of <CODE>PROP_IMAGE</CODE>.
     * It is relevant only on initialization (client property in first panel).
     */
    private static final String PROP_LEFT_DIMENSION = "WizardPanel_leftDimension"; // NOI18N

    /** <CODE>URL</CODE> property. The value is taken from <CODE>WizardDescriptor.getProperty()</CODE> or
     * <CODE>((JComponent)Panel.getComponent()).getClientProperty()</CODE> in this order.
     * Represents URL of help displayed in left pane.
     */
    private static final String PROP_HELP_URL = "WizardPanel_helpURL"; // NOI18N

    /** <CODE>String</CODE> property. The value is taken from <CODE>WizardDescriptor.getProperty()</CODE>.
     * If it contains non-null value the String is displayed the bottom of the wizard
     * and should inform user why the panel is invalid and why the Next/Finish
     * buttons were disabled.
     * @since 3.39
     */
    private static final String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"; // NOI18N

    private static Logger err = Logger.getLogger(WizardDescriptor.class.getName ());

    /** real buttons to be placed instead of the options */
    private final JButton nextButton = new JButton();
    private final JButton finishButton = new JButton();
    private final JButton cancelButton = new JButton();
    private final JButton previousButton = new JButton();
    private FinishAction finishOption;
    private Set /*<Object>*/ newObjects = Collections.EMPTY_SET;

    /** a component with wait cursor */
    private Component waitingComponent;
    private boolean changeStateInProgress = false;

    /** Whether wizard panel will be constructed from <CODE>WizardDescriptor.getProperty()</CODE>/
     * <CODE>(JComponent)Panel.getComponent()</CODE> client properties or returned
     * <CODE>Component</CODE> will be inserted to wizard dialog directly.
     */
    private boolean autoWizardStyle = false;

    /** Whether properties from first <CODE>(JComponent)Panel.getComponent()</CODE>
     * have been initialized.
     */
    private boolean init = false;

    /** Panel which is used when in <CODE>AUTO_WIZARD_STYLE</CODE> mode.*/
    private WizardPanel wizardPanel;

    /** Image */
    private Image image;

    /** Content data */
    private String[] contentData = new String[] {  };

    /** Selected content index */
    private int contentSelectedIndex = -1;

    /** Background color*/
    private Color contentBackColor;

    /** Foreground color*/
    private Color contentForegroundColor;

    /** Help URL displayed in the left pane */
    private URL helpURL;

    /** Listener on a user component client property changes*/
    private PropL propListener;

    /** 'North' or 'South' */
    private String imageAlignment = "North"; // NOI18N

    /** Iterator between panels in the wizard and its settings */
    private SettingsAndIterator<?> data;

    /** Change listener that invokes method update state */
    private ChangeListener weakChangeListener;
    private PropertyChangeListener weakPropertyChangeListener;
    private ActionListener weakNextButtonListener;
    private ActionListener weakPreviousButtonListener;
    private ActionListener weakFinishButtonListener;
    private ActionListener weakCancelButtonListener;

    // base listener which won't be directly attached, will only wrapped by WeakListener
    private Listener baseListener;

    /** message format to create title of the document */
    private MessageFormat titleFormat;

    /** hashtable with additional settings that is usually used
    * by Panels to store their data
    */
    private Map<String,Object> properties;
    ResourceBundle bundle = NbBundle.getBundle(WizardDescriptor.class);

    /** Request processor that is used for asynchronous jobs (background validation, 
     * asynchronous instantiation i.e.) and supports Thread.interrupted().
     * It's package-private to accessible for unit tests.
     */
    static final RequestProcessor ASYNCHRONOUS_JOBS_RP = 
        new RequestProcessor("wizard-descriptor-asynchronous-jobs", 1, true); // NOI18N
    
    private RequestProcessor.Task backgroundValidationTask;

    private boolean validationRuns;

    private ProgressHandle handle;

    private static final String PROGRESS_BAR_DISPLAY_NAME = NbBundle.getMessage (WizardDescriptor.class, "CTL_InstantiateProgress_Title"); // NOI18N

    private ActionListener escapeActionListener;

    {
        // button init
        ResourceBundle b = NbBundle.getBundle("org.openide.Bundle"); // NOI18N
        nextButton.setText(b.getString("CTL_NEXT"));
        Mnemonics.setLocalizedText(previousButton, b.getString("CTL_PREVIOUS"));
        Mnemonics.setLocalizedText(finishButton, b.getString("CTL_FINISH"));
        finishButton.getAccessibleContext().setAccessibleDescription(b.getString("ACSD_FINISH"));
        cancelButton.setText(b.getString("CTL_CANCEL"));
        cancelButton.getAccessibleContext().setAccessibleDescription(b.getString("ACSD_CANCEL"));

        finishButton.setDefaultCapable(true);
        nextButton.setDefaultCapable(true);
        previousButton.setDefaultCapable(false);
        cancelButton.setDefaultCapable(false);
    }

    /** Create a new wizard from a fixed list of panels, passing some settings to the panels.
    * @param wizardPanels the panels to use
    * @param settings the settings to pass to panels, or <code>null</code>
    * @see #WizardDescriptor(WizardDescriptor.Iterator, Object)
    */
    public <Data> WizardDescriptor(Panel<Data>[] wizardPanels, Data settings) {
        this(new SettingsAndIterator<Data>(new ArrayIterator<Data>(wizardPanels), settings));
    }
    
    /** Create a new wizard from a fixed list of panels with settings
    * defaulted to <CODE>this</CODE>.
    *
    * @param wizardPanels the panels to use
    * @see #WizardDescriptor(WizardDescriptor.Iterator, Object)
    */
    public WizardDescriptor(Panel<WizardDescriptor>[] wizardPanels) {
        this(SettingsAndIterator.create(new ArrayIterator<WizardDescriptor>(wizardPanels)));
    }

    /** Create wizard for a sequence of panels, passing some settings to the panels.
    * @param panels iterator over all {@link WizardDescriptor.Panel}s that can appear in the wizard
    * @param settings the settings to provide to the panels (may be any data understood by them)
    * @see WizardDescriptor.Panel#readSettings
    * @see WizardDescriptor.Panel#storeSettings
    */
    public <Data>WizardDescriptor(Iterator<Data> panels, Data settings) {
        this(new SettingsAndIterator<Data>(panels, settings));
    }
    
    /** Constructor for subclasses. The expected use is to call this
     * constructor and then call {@link #setPanelsAndSettings} to provide
     * the right iterator, panels and data the wizard should use. This 
     * allows to eliminate unchecked warnings as described in 
     * <a href="http://www.netbeans.org/issues/show_bug.cgi?id=102261">issue 102261</a>.
     * @since 7.4
     */
    protected WizardDescriptor() {
        this(SettingsAndIterator.empty());
    }
    
    private <Data> WizardDescriptor(SettingsAndIterator<Data> data) {
        super("", "", true, DEFAULT_OPTION, null, CLOSE_PREVENTER); // NOI18N
        
        this.data = data;

        baseListener = new Listener();

        weakNextButtonListener = WeakListeners.create(
                ActionListener.class, baseListener, nextButton
            ); // NOI18N
        weakPreviousButtonListener = WeakListeners.create(
                ActionListener.class, baseListener, previousButton
            ); // NOI18N
        weakFinishButtonListener = WeakListeners.create(
                ActionListener.class, baseListener, finishButton
            ); // NOI18N
        weakCancelButtonListener = WeakListeners.create(
                ActionListener.class, baseListener, cancelButton
            ); // NOI18N

        nextButton.addActionListener(weakNextButtonListener);
        previousButton.addActionListener(weakPreviousButtonListener);
        finishButton.addActionListener(weakFinishButtonListener);
        cancelButton.addActionListener(weakCancelButtonListener);

        finishOption = new WizardDescriptor.FinishAction();

        super.setOptions(new Object[] { previousButton, nextButton, finishButton, cancelButton });
        super.setClosingOptions(new Object[] { finishOption, cancelButton });

        // attach the change listener to iterator
        weakChangeListener = WeakListeners.change(baseListener, data.getIterator(this));
        data.getIterator(this).addChangeListener(weakChangeListener);

        callInitialize();
    }

    /** Create wizard for a sequence of panels, with settings
    * defaulted to <CODE>this</CODE>.
    *
    * @param panels iterator over all {@link WizardDescriptor.Panel}s that can appear in the wizard
    */
    public WizardDescriptor(Iterator<WizardDescriptor> panels) {
        this(SettingsAndIterator.create(panels));
    }

    /** Initializes settings.
     */
    protected void initialize() {
        super.initialize();

        updateState();
        
        // #81938: special handling WizardDescriptor to avoid close wizard during instantiate
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                final Window w = SwingUtilities.getWindowAncestor((Component) getMessage());
                if (w != null) {
                    w.addWindowListener (new WindowListener () {
                        public void windowActivated (WindowEvent e) {
                        }
                        public void windowClosed (WindowEvent e) {
                        }
                        public void windowClosing (WindowEvent e) {
                            if (!changeStateInProgress) {
                                w.setVisible (false);
                                w.dispose ();
                            }
                        }
                        public void windowDeactivated (WindowEvent e) {
                        }
                        public void windowDeiconified (WindowEvent e) {
                        }
                        public void windowIconified (WindowEvent e) {
                        }
                        public void windowOpened (WindowEvent e) {
                        }
                    });
                }
            }
        });
    }

    /** Set a different list of panels.
    * Correctly updates the buttons.
    * @param panels the new list of {@link WizardDescriptor.Panel}s
    * @deprecated use setPanelsAndSettings if needed.
    */
    @Deprecated
    @SuppressWarnings("unchecked")
    public final synchronized void setPanels(Iterator panels) {
        if (data.getIterator(this) != null) {
            data.getIterator(this).removeChangeListener(weakChangeListener);
        }

        data = data.clone(panels);
        weakChangeListener = WeakListeners.change(baseListener, data.getIterator(this));
        data.getIterator(this).addChangeListener(weakChangeListener);
        init = false;

        updateState();
    }

    /** Set a different list of panels.
    * Correctly updates the buttons.
    * @param panels the new list of {@link WizardDescriptor.Panel}s
    * @param settings the new settings that will be passed to the panels
    * @since 7.2
    */
    public final synchronized <Data> void setPanelsAndSettings(Iterator<Data> panels, Data settings) {
        if (data.getIterator(this) != null) {
            data.getIterator(this).removeChangeListener(weakChangeListener);
        }

        data = new SettingsAndIterator<Data>(panels, settings);
        weakChangeListener = WeakListeners.change(baseListener, data.getIterator(this));
        data.getIterator(this).addChangeListener(weakChangeListener);
        init = false;

        updateState();
    }

    /** Set options permitted by the wizard considered as a <code>DialogDescriptor</code>.
    * Substitutes tokens such as {@link #NEXT_OPTION} with the actual button.
    *
    * @param options the options to set
    */
    public void setOptions(Object[] options) {
        super.setOptions(convertOptions(options));
    }

    /**
    * @param options the options to set
    */
    public void setAdditionalOptions(Object[] options) {
        super.setAdditionalOptions(convertOptions(options));
    }

    /**
    * @param options the options to set
    */
    public void setClosingOptions(Object[] options) {
        super.setClosingOptions(convertOptions(options));
    }

    /** Converts some options.
    */
    private Object[] convertOptions(Object[] options) {
        Object[] clonedOptions = options.clone();

        for (int i = clonedOptions.length - 1; i >= 0; i--) {
            if (clonedOptions[i] == NEXT_OPTION) {
                clonedOptions[i] = nextButton;
            }

            if (clonedOptions[i] == PREVIOUS_OPTION) {
                clonedOptions[i] = previousButton;
            }

            if (clonedOptions[i] == FINISH_OPTION) {
                clonedOptions[i] = finishButton;
            }

            if (clonedOptions[i] == CANCEL_OPTION) {
                clonedOptions[i] = cancelButton;
            }
        }

        return clonedOptions;
    }

    /** Overriden to ensure that returned value is one of
     * the XXX_OPTION constants.
     */
    public Object getValue() {
        return backConvertOption(super.getValue());
    }

    /** Converts the option back to one of the constants.
     * It is called from getValue().
     */
    private Object backConvertOption(Object op) {
        if (op == nextButton) {
            return NEXT_OPTION;
        }

        if (op == previousButton) {
            return PREVIOUS_OPTION;
        }

        if (op == finishButton) {
            return FINISH_OPTION;
        }

        if (op == cancelButton) {
            return CANCEL_OPTION;
        }

        // if we don't know just return the original value
        return op;
    }

    /** Sets the message format to create title of the wizard.
    * The format can take two parameters. The name of the
    * current component and the name returned by the iterator that
    * defines the order of panels. The default value is something
    * like
    * <PRE>
    *   {0} wizard {1}
    * </PRE>
    * That can be expanded to something like this
    * <PRE>
    *   EJB wizard (1 of 8)
    * </PRE>
    * This method allows anybody to provide own title format.
    *
    * @param format message format to the title
    */
    public void setTitleFormat(MessageFormat format) {
        titleFormat = format;

        if (init) {
            updateState();
        }
    }

    /** Getter for current format to be used to format title.
    * @return the format
    * @see #setTitleFormat
    */
    public synchronized MessageFormat getTitleFormat() {
        if (titleFormat == null) {
            // ok, initialize the default one
            titleFormat = new MessageFormat(NbBundle.getMessage(WizardDescriptor.class, "CTL_WizardName"));
        }

        return titleFormat;
    }

    /** Allows Panels that use WizardDescriptor as settings object to
    * store additional settings into it.
    *
    * @param name name of the property
    * @param value value of property
    */
    public void putProperty(final String name, final Object value) {
        Object oldValue;

        synchronized (this) {
            if (properties == null) {
                properties = new HashMap<String,Object>(7);
            }

            oldValue = properties.get(name);
            properties.put(name, value);
        }

        // bugfix #27738, firing changes in a value of the property
        firePropertyChange(name, oldValue, value);

        if (propListener != null) {
            Mutex.EVENT.readAccess(
                new Runnable() {
                    public void run() {
                        propListener.propertyChange(new PropertyChangeEvent(this, name, null, null));
                    }
                }
            );
        }

        if (PROP_ERROR_MESSAGE.equals(name)) {
            // #76318: New Entity wizard shows unreadable red error
            if (init && OK_OPTION.equals (getValue ())) return ; // call getValue() only on initialized WD
            if (wizardPanel != null) {
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        wizardPanel.setErrorMessage((String) ((value == null) ? " " : value), (nextButton.isEnabled () || finishButton.isEnabled ()) ? Boolean.TRUE : Boolean.FALSE); //NOI18N
                    }
                });
            }
        }
    }

    /** Getter for stored property.
    * @param name name of the property
    * @return the value
    */
    public synchronized Object getProperty(String name) {
        return (properties == null) ? null : properties.get(name);
    }
    
    /** Read only map with stored properties.
     * @return read only map of properties stored using {@link #putProperty} method
     * @since 7.2
     */
    public synchronized Map<String,Object> getProperties() {
        return properties == null ? Collections.<String,Object>emptyMap() : new HashMap<String,Object>(properties);
    }

    public void setHelpCtx(final HelpCtx helpCtx) {
        if ((wizardPanel != null) && (helpCtx != null)) {
            HelpCtx.setHelpIDString(wizardPanel, helpCtx.getHelpID());
        }

        // we call the inherited method after setting the ID
        // on the panel becuase super.setHelpCtx fires the change
        super.setHelpCtx(helpCtx);
    }

    /** Returns set of newly instantiated objects if the wizard has been correctly finished.
     * Returns the empty set as default. If the wizard uses the InstantiatingIterator
     * then WizardDescriptor returns a set of Object as same as InstantiatingIterator.instantiate().
     *
     * @exception IllegalStateException if this method is called on the unfinished wizard
     * @return a set of Objects created
     * @since 4.41
     */
    public Set /*<Object>*/ getInstantiatedObjects() {
        // 
        if (!(FINISH_OPTION.equals(getValue()))) {
            throw new IllegalStateException();
        }

        return newObjects;
    }

    /** Updates buttons to reflect the current state of the panels.
    * Can be overridden by subclasses
    * to change the options to special values. In such a case use:
    * <p><code><PRE>
    *   super.updateState ();
    *   setOptions (...);
    * </PRE></code>
    */
    protected synchronized void updateState() {
        updateStateOpen(data);
    }
    private <A> void updateStateOpen(SettingsAndIterator<A> data) {
        Panel<A> p = data.getIterator(this).current();

        // listeners on the panel
        if (data.current != p) {
            if (data.current != null) {
                // remove
                data.current.removeChangeListener(weakChangeListener);
                data.current.storeSettings(data.getSettings(this));
            }

            // Hack - obtain current panel again
            // It's here to allow dynamic change of panels in wizard
            // (which can be done in storeSettings method)
            p = data.getIterator(this).current();

            // add to new, detach old change listener and attach new one
            data.getIterator(this).removeChangeListener(weakChangeListener);
            weakChangeListener = WeakListeners.change(baseListener, p);
            data.getIterator(this).addChangeListener(weakChangeListener);
            p.addChangeListener(weakChangeListener);

            data.current = p;
            p.readSettings(data.getSettings(this));
        }

        boolean next = data.getIterator(this).hasNext();
        boolean prev = data.getIterator(this).hasPrevious();
        boolean valid = p.isValid();

        // AWT sensitive code
        if (SwingUtilities.isEventDispatchThread ()) {
            updateStateInAWT ();
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                   updateStateInAWT ();
                } 
            });
        }
        // end of AWT sensitive code
        
        //    nextButton.setVisible (next);
        //    finishButton.setVisible (!next || (current instanceof FinishPanel));
        // XXX Why setValue on Next ?
        //        if (next) {
        //            setValue (nextButton);
        //        } else {
        ////            setValue (finishButton); 
        //        }
        setHelpCtx(p.getHelp());

        java.awt.Component c = p.getComponent();

        if ((c == null) || c instanceof java.awt.Window) {
            throw new IllegalStateException("Wizard panel " + p + " gave a strange component " + c); // NOI18N
        }

        if (!init) {
            if (c instanceof JComponent) {
                autoWizardStyle = getBooleanProperty((JComponent) c, PROP_AUTO_WIZARD_STYLE);

                if (autoWizardStyle) {
                    wizardPanel = new WizardPanel(
                            getBooleanProperty((JComponent) c, PROP_CONTENT_DISPLAYED),
                            getBooleanProperty((JComponent) c, PROP_HELP_DISPLAYED),
                            getBooleanProperty((JComponent) c, PROP_CONTENT_NUMBERED), getLeftDimension((JComponent) c)
                        );
                    initBundleProperties();
                }
            }

            if (propListener == null) {
                propListener = new PropL();
            }

            init = true;
        }

        //update wizardPanel
        if (wizardPanel != null) {
            Component oldComp = wizardPanel.getRightComponent();

            if (oldComp != null) {
                oldComp.removePropertyChangeListener(weakPropertyChangeListener);
            }

            if (c instanceof JComponent) {
                setPanelProperties((JComponent) c);
                wizardPanel.setContent(contentData);
                wizardPanel.setSelectedIndex(contentSelectedIndex);
                wizardPanel.setContentBackColor(contentBackColor);
                wizardPanel.setContentForegroundColor(contentForegroundColor);
                wizardPanel.setImage(image);
                wizardPanel.setImageAlignment(imageAlignment);
                wizardPanel.setHelpURL(helpURL);
                updateButtonAccessibleDescription();
                weakPropertyChangeListener = WeakListeners.propertyChange(propListener, c);
                c.addPropertyChangeListener(weakPropertyChangeListener);
            }

            if (wizardPanel.getRightComponent() != c) {
                wizardPanel.setRightComponent(c);

                if (wizardPanel != getMessage()) {
                    setMessage(wizardPanel);
                } else {
                    // force revalidate and repaint because the contents of
                    // wizardPanel has changed.  See NbPresenter code
                    firePropertyChange(DialogDescriptor.PROP_MESSAGE, null, wizardPanel);
                }
            }
        } else if (c != getMessage()) {
            setMessage(c);
        }

        String panelName = c.getName();

        if (panelName == null) {
            panelName = ""; // NOI18N
        }

        Object[] args = { panelName, data.getIterator(this).name() };
        MessageFormat mf = getTitleFormat();

        if (autoWizardStyle) {
            wizardPanel.setPanelName(mf.format(args));
        } else {
            setTitle(mf.format(args));
        }

        Component fo = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

        if ((fo != null) && (!fo.isEnabled()) && (wizardPanel != null)) {
            wizardPanel.requestFocus();
        }
    }
    
    // for xtesting usage only
    boolean isForwardEnabled () {
        return data.getIterator(this).current ().isValid () && !validationRuns;
    }

    private void updateStateInAWT () {
        Panel<?> p = data.getIterator(this).current ();        
        boolean next = data.getIterator(this).hasNext ();
        boolean prev = data.getIterator(this).hasPrevious ();
        boolean valid = p.isValid () && !validationRuns;

        nextButton.setEnabled (next && valid);
        previousButton.setEnabled (prev);
        cancelButton.setEnabled (true);
        
        if (data.current instanceof FinishablePanel) {
            // check if isFinishPanel
            if (((FinishablePanel)data.current).isFinishPanel ()) {
                finishButton.setEnabled (valid);
            } else {
                // XXX What if the last panel is not FinishPanel ??? enable ?
                finishButton.setEnabled (valid && !next);
            }
        } else {
            // original way
            finishButton.setEnabled (
                valid &&
                (!next || (data.current instanceof FinishPanel))
            );
        }
    }

    
    /** Shows blocking wait cursor during updateState run */
    private void updateStateWithFeedback() {
        try {
            showWaitCursor();
            updateState();
        } finally {
            showNormalCursor();
        }
    }

    /** Shows next step in UI of wizards, displays wait cursot during the change.
     */
    private void goToNextStep(Dimension previousSize) {
        try {
            showWaitCursor();

            boolean alreadyUpdated = false;
            Font controlFont = (Font) UIManager.getDefaults().get("controlFont"); //NOI18N
            Integer defaultSize = (Integer) UIManager.get("nbDefaultFontSize");

            if (defaultSize == null) {
                //Plastic look and feel...
                defaultSize = new Integer(11);
            }

            // enable auto-resizing policy only for fonts bigger thne default
            if ((controlFont != null) && (controlFont.getSize() > defaultSize.intValue())) { //NOI18N

                Window parentWindow = SwingUtilities.getWindowAncestor((Component) getMessage());

                if (parentWindow != null) {
                    updateState();
                    alreadyUpdated = true;
                    resizeWizard(parentWindow, previousSize);
                }
            }

            if (!alreadyUpdated) {
                updateState();
            }

            if (wizardPanel != null) {
                wizardPanel.requestFocus();
            }
        } finally {
            showNormalCursor();
        }
    }

    /** Tries to resize wizard wisely if needed. Keeps "display inertia" so that
     * wizard is only enlarged, not shrinked, and location is changed only when
     * wizard window exceeds screen bounds after resize.
     */
    private void resizeWizard(Window parentWindow, Dimension prevSize) {
        Dimension curSize = data.getIterator(this).current().getComponent().getPreferredSize();

        // only enlarge if needed, don't shrink
        if ((curSize.width > prevSize.width) || (curSize.height > prevSize.height)) {
            Rectangle origBounds = parentWindow.getBounds();
            int newWidth = Math.max(origBounds.width + (curSize.width - prevSize.width), origBounds.width);
            int newHeight = Math.max(origBounds.height + (curSize.height - prevSize.height), origBounds.height);
            Rectangle screenBounds = Utilities.getUsableScreenBounds();
            Rectangle newBounds;

            // don't allow to exceed screen size, center if needed
            if (((origBounds.x + newWidth) > screenBounds.width) || ((origBounds.y + newHeight) > screenBounds.height)) {
                newWidth = Math.min(screenBounds.width, newWidth);
                newHeight = Math.min(screenBounds.height, newHeight);
                newBounds = Utilities.findCenterBounds(new Dimension(newWidth, newHeight));
            } else {
                newBounds = new Rectangle(origBounds.x, origBounds.y, newWidth, newHeight);
            }

            parentWindow.setBounds(newBounds);
            parentWindow.invalidate();
            parentWindow.validate();
            parentWindow.repaint();
        }
    }

    private void showWaitCursor() {
        if ((wizardPanel == null) || (wizardPanel.getRootPane() == null)) {
            // if none root pane --> don't set wait cursor
            return;
        }
        
        // bugfix #92539: JR: I don't see the reason this code, I have tried comment out
//        Window parentWindow = SwingUtilities.getWindowAncestor((Component) getMessage());
//        if (parentWindow != null) {
//            parentWindow.setEnabled (false);
//        }
//        
        if (wizardPanel != null) {
            // save escapeActionListener for normal state
            KeyStroke ks = KeyStroke.getKeyStroke (KeyEvent.VK_ESCAPE, 0);
            escapeActionListener = wizardPanel.getRootPane ().getActionForKeyStroke (ks);
            wizardPanel.getRootPane ().unregisterKeyboardAction (ks);
        }

        waitingComponent = wizardPanel.getRootPane().getContentPane();
        waitingComponent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        changeStateInProgress = true;
    }

    private void showNormalCursor() {
        if (waitingComponent == null) {
            // none waitingComponent --> don't change cursor to normal
            return;
        }
        
        Window parentWindow = SwingUtilities.getWindowAncestor((Component) getMessage());
        if (parentWindow != null) {
            parentWindow.setEnabled (true);
        }
        
        if (wizardPanel != null) {
            // set back escapeActionListener as same as NbPresenter does
            if (escapeActionListener != null) {
                wizardPanel.getRootPane ().registerKeyboardAction (escapeActionListener, "Escape",
                    KeyStroke.getKeyStroke (KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            }
        }
        
        waitingComponent.setCursor(null);
        waitingComponent = null;
        changeStateInProgress = false;
    }

    /* commented out - issue #32927. Replaced by javadoc info in WizardDescriptor.Panel
    private static final Set warnedPanelIsComponent = new WeakSet(); // Set<Class>
    private static synchronized void warnPanelIsComponent(Class c) {
        if (warnedPanelIsComponent.add(c)) {
            StringBuffer buffer = new StringBuffer(150);
            buffer.append("WARNING - the WizardDescriptor.Panel implementation "); // NOI18N
            buffer.append(c.getName());
            buffer.append(" provides itself as the result of getComponent().\n"); // NOI18N
            buffer.append("This hurts performance and can cause a clash when Component.isValid() is overridden.\n"); // NOI18N
            buffer.append("Please use a separate component class, see details at http://performance.netbeans.org/howto/dialogs/wizard-panels.html."); // NOI18N
            err.log(ErrorManager.WARNING, buffer.toString());
        }
    }
    */

    /** Tryes to get property from getProperty() if doesn't succeed then tryes at
     * supplied <CODE>JComponent</CODE>s client property.
     * @param c origin of property
     * @param s name of property
     * @return boolean property
     */
    private boolean getBooleanProperty(JComponent c, String s) {
        Object property = getProperty(s);

        if (property instanceof Boolean) {
            return ((Boolean) property).booleanValue();
        }

        property = c.getClientProperty(s);

        if (property instanceof Boolean) {
            return ((Boolean) property).booleanValue();
        }

        return false;
    }

    /** Tryes to get dimension of wizard panel's left pane from getProperty()
     * if doesn't succeed then tryes at
     * supplied <CODE>JComponent</CODE>s client property.
     * @return <CODE>Dimension</CODE> dimension of wizard panel's left pane
     */
    private Dimension getLeftDimension(JComponent c) {
        Dimension leftDimension;
        Object property = c.getClientProperty(PROP_LEFT_DIMENSION);

        if (property instanceof Dimension) {
            leftDimension = (Dimension) property;
        } else {
            leftDimension = new Dimension(198, 233);
        }

        return leftDimension;
    }

    /** Tryes to get properties from getProperty() if doesn't succeed then tryes at
     * supplied <CODE>JComponent</CODE>s client properties and store them
     * to appropriate fields.
     * @param c origin of property
     * @param s name of property
     * @return boolean property
     */
    private void setPanelProperties(JComponent c) {
        // TODO: Method should be devided into individual setter/getter methods !?
        Object property = getProperty(PROP_CONTENT_SELECTED_INDEX);

        if (property instanceof Integer) {
            contentSelectedIndex = ((Integer) property).intValue();
        } else {
            property = c.getClientProperty(PROP_CONTENT_SELECTED_INDEX);

            if (property instanceof Integer) {
                contentSelectedIndex = ((Integer) property).intValue();
            }
        }

        property = getProperty(PROP_CONTENT_DATA);

        if (property instanceof String[]) {
            contentData = (String[]) property;
        } else {
            property = c.getClientProperty(PROP_CONTENT_DATA);

            if (property instanceof String[]) {
                contentData = (String[]) property;
            }
        }

        property = getProperty(PROP_IMAGE);

        if (property instanceof Image) {
            image = (Image) property;
        } else if ((properties == null) || (!properties.containsKey(PROP_IMAGE))) {
            property = c.getClientProperty(PROP_IMAGE);

            if (property instanceof Image) {
                image = (Image) property;
            }
        }

        property = getProperty(PROP_IMAGE_ALIGNMENT);

        if (property instanceof String) {
            imageAlignment = (String) property;
        } else {
            property = c.getClientProperty(PROP_IMAGE_ALIGNMENT);

            if (property instanceof String) {
                imageAlignment = (String) property;
            }
        }

        property = getProperty(PROP_CONTENT_BACK_COLOR);

        if (property instanceof Color) {
            contentBackColor = (Color) property;
        } else {
            property = c.getClientProperty(PROP_CONTENT_BACK_COLOR);

            if (property instanceof Color) {
                contentBackColor = (Color) property;
            }
        }

        property = getProperty(PROP_CONTENT_FOREGROUND_COLOR);

        if (property instanceof Color) {
            contentForegroundColor = (Color) property;
        } else {
            property = c.getClientProperty(PROP_CONTENT_FOREGROUND_COLOR);

            if (property instanceof Color) {
                contentForegroundColor = (Color) property;
            }
        }

        property = c.getClientProperty(PROP_HELP_URL);

        if (property instanceof URL) {
            helpURL = (URL) property;
        } else if (property == null) {
            helpURL = null;
        }
    }

    private void initBundleProperties() {
        contentBackColor = new Color(
                getIntFromBundle("INT_WizardBackRed"), // NOI18N
                getIntFromBundle("INT_WizardBackGreen"), // NOI18N
                getIntFromBundle("INT_WizardBackBlue") // NOI18N
            ); // NOI18N

        contentForegroundColor = new Color(
                getIntFromBundle("INT_WizardForegroundRed"), // NOI18N
                getIntFromBundle("INT_WizardForegroundGreen"), // NOI18N
                getIntFromBundle("INT_WizardForegroundBlue") // NOI18N
            ); // NOI18N
        imageAlignment = bundle.getString("STRING_WizardImageAlignment"); //NOI18N
    }

    /** Overrides superclass method. Adds reseting of wizard
     * for <code>CLOSED_OPTION</code>. */
    public void setValue(Object value) {
        setValueOpen(value, data);
    }
    
    private <A> void setValueOpen(Object value, SettingsAndIterator<A> data) {
        Object convertedValue = backConvertOption(value);
        // set new value w/o fire PROP_VALUE change
        Object oldValue = getValue();
        setValueWithoutPCH(convertedValue);

        // #17360: Reset wizard on CLOSED_OPTION too.
        if (CLOSED_OPTION.equals(convertedValue)) {
            try {
                resetWizard();
            } catch (RuntimeException x) {
                // notify to log
                err.log(Level.FINE, null, x);
            }
        } else if (FINISH_OPTION.equals(convertedValue) || NEXT_OPTION.equals(convertedValue)) {
            //Bugfix #25820: make sure that storeSettings
            //is called before propertyChange.
            if (data.current != null) {
                data.current.storeSettings(data.getSettings(this));
            }
        }

        // notify listeners about PROP_VALUE change
        firePropertyChange(PROP_VALUE, oldValue, convertedValue);
    }

    /** Resets wizard when after closed/cancelled/finished the wizard dialog. */
    private void resetWizard() {
        resetWizardOpen(data);
    }
    
    private <A> void resetWizardOpen(SettingsAndIterator<A> data) {
        if (data.current != null) {
            data.current.storeSettings(data.getSettings(this));
            data.current.removeChangeListener(weakChangeListener);
            data.current = null;

            if (wizardPanel != null) {
                wizardPanel.resetPreferredSize();
            }
        }

        callUninitialize();

        // detach the change listener at the end of wizard
        data.getIterator(this).removeChangeListener(weakChangeListener);
    }

    private int getIntFromBundle(String key) {
        return Integer.parseInt(bundle.getString(key));
    }

    private static Image getDefaultImage() {
        return Utilities.loadImage("org/netbeans/modules/dialogs/defaultWizard.gif", true);
    }

    private void updateButtonAccessibleDescription() {
        String stepName = ((contentData != null) && (contentSelectedIndex > 0) &&
            ((contentSelectedIndex - 1) < contentData.length)) ? contentData[contentSelectedIndex - 1] : ""; // NOI18N
        previousButton.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(WizardDescriptor.class, "ACSD_PREVIOUS", new Integer(contentSelectedIndex), stepName)
        );

        stepName = ((contentData != null) && (contentSelectedIndex < (contentData.length - 1)) &&
            ((contentSelectedIndex + 1) >= 0)) ? contentData[contentSelectedIndex + 1] : ""; // NOI18N
        nextButton.getAccessibleContext().setAccessibleDescription(
            NbBundle.getMessage(WizardDescriptor.class, "ACSD_NEXT", new Integer(contentSelectedIndex + 2), stepName)
        );
    }

    private void lazyValidate(final WizardDescriptor.Panel panel, final Runnable onValidPerformer) {

        Runnable validationPeformer = new Runnable() {
            public void run() {
                
                err.log (Level.FINE, "validationPeformer entry."); // NOI18N
                ValidatingPanel v = (ValidatingPanel) panel;

                try {
                    // try validation current panel
                    v.validate();
                    validationRuns = false;

                    // validation succesfull
                    if (SwingUtilities.isEventDispatchThread ()) {
                        err.log (Level.FINE, "Runs onValidPerformer directly in EDT."); // NOI18N
                        onValidPerformer.run();
                    } else {
                        err.log (Level.FINE, "invokeLater onValidPerformer."); // NOI18N
                        SwingUtilities.invokeLater (new Runnable () {
                            public void run () {
                                err.log (Level.FINE, "Runs onValidPerformer from invokeLater."); // NOI18N
                                onValidPerformer.run();
                            } 
                        });
                    }
                } catch (WizardValidationException wve) {
                    validationRuns = false;
                    updateState ();
                    
                    // cannot continue, notify user
                    if (wizardPanel != null) {
                        wizardPanel.setErrorMessage(wve.getLocalizedMessage(), Boolean.FALSE);
                    }

                    // focus source of this problem
                    final JComponent comp = wve.getSource();
                    if (comp != null && comp.isFocusable()) {
                        comp.requestFocus();
                    }
                }

            }
        };

        if (panel instanceof AsynchronousValidatingPanel) {
            AsynchronousValidatingPanel p = (AsynchronousValidatingPanel) panel;
            validationRuns = true;  // disable Next> Finish buttons
            p.prepareValidation();
            err.log (Level.FINE, "Do ASYNCHRONOUS_JOBS_RP.post(validationPeformer)."); // NOI18N
            updateStateWithFeedback ();
            backgroundValidationTask = ASYNCHRONOUS_JOBS_RP.post(validationPeformer);
        } else if (panel instanceof ValidatingPanel) {
            validationRuns = true;
            err.log (Level.FINE, "Runs validationPeformer."); // NOI18N
            validationPeformer.run();
        } else {
            err.log (Level.FINE, "Runs onValidPerformer."); // NOI18N
            onValidPerformer.run();
        }

    }

    // helper methods which call to InstantiatingIterator
    private void callInitialize() {
        assert data.getIterator(this) != null;

        if (data.getIterator(this) instanceof InstantiatingIterator) {
            ((InstantiatingIterator) data.getIterator(this)).initialize(this);
        }

        newObjects = Collections.EMPTY_SET;
    }

    private void callUninitialize() {
        assert data.getIterator(this) != null;

        if (data.getIterator(this) instanceof InstantiatingIterator) {
            ((InstantiatingIterator) data.getIterator(this)).uninitialize(this);
        }
    }

    private void callInstantiate() throws IOException {
        callInstantiateOpen(data);
    }
    
    private <A> void callInstantiateOpen(SettingsAndIterator<A> data) throws IOException {
        Iterator<A> panels = data.getIterator(this);
        
        assert panels != null;
        
        err.log (Level.FINE, "Is AsynchronousInstantiatingIterator? " + (panels instanceof AsynchronousInstantiatingIterator));
        err.log (Level.FINE, "Is ProgressInstantiatingIterator? " + (panels instanceof ProgressInstantiatingIterator));
        if (panels instanceof ProgressInstantiatingIterator) {
            handle = ProgressHandleFactory.createHandle (PROGRESS_BAR_DISPLAY_NAME);
            
            JComponent progressComp = ProgressHandleFactory.createProgressComponent (handle);
            if (wizardPanel != null) {
                wizardPanel.setProgressComponent (progressComp, ProgressHandleFactory.createDetailLabelComponent (handle));
            }
            
            err.log (Level.FINE, "Show progressPanel controlled by iterator later.");
        } else if (panels instanceof AsynchronousInstantiatingIterator) {
            handle = ProgressHandleFactory.createHandle (PROGRESS_BAR_DISPLAY_NAME);
            
            JComponent progressComp = ProgressHandleFactory.createProgressComponent (handle);
            if (wizardPanel != null) {
                wizardPanel.setProgressComponent (progressComp, ProgressHandleFactory.createMainLabelComponent (handle));
            }
            
            handle.start ();
            err.log (Level.FINE, "Show progressPanel later.");
        }
         
        // bugfix #44444, force store settings before do instantiate new objects
        panels.current().storeSettings(data.getSettings(this));

        if (panels instanceof InstantiatingIterator) {
            showWaitCursor();

            try {
                assert ! (panels instanceof AsynchronousInstantiatingIterator) || ! SwingUtilities.isEventDispatchThread () : "Cannot invoked within EDT if AsynchronousInstantiatingIterator!";
                
                if (panels instanceof ProgressInstantiatingIterator) {
                    assert handle != null : "ProgressHandle must be not null.";
                    err.log (Level.FINE, "Calls instantiate(ProgressHandle) on iterator: " + panels.getClass ().getName ());
                    newObjects = ((ProgressInstantiatingIterator) panels).instantiate (handle);
                } else {
                    err.log (Level.FINE, "Calls instantiate() on iterator: " + panels.getClass ().getName ());
                    newObjects = ((InstantiatingIterator) panels).instantiate ();
                }
            } finally {

                // set cursor back to normal
                showNormalCursor();
                
            }
        }
    }

    private static Font doDeriveFont(Font original, int style) {
        if (Utilities.isMac()) {
            // don't use deriveFont() - see #49973 for details
            return new Font(original.getName(), style, original.getSize());
        }

        return original.deriveFont(style);
    }

    // support methods for xtesting
    final void doNextClick() {
        if (nextButton.isEnabled()) {
            nextButton.doClick();
        }
    }

    final void doPreviousClick() {
        if (previousButton.isEnabled()) {
            previousButton.doClick();
        }
    }

    final void doFinishClick() {
        if (finishButton.isEnabled()) {
            finishButton.doClick();
        }
    }

    final void doCancelClick() {
        if (cancelButton.isEnabled()) {
            cancelButton.doClick();
        }
    }

    // helper method, might be removed from code
    // returns false if Next button is disabled
    final boolean isNextEnabled() {
        return nextButton.isEnabled();
    }

    // helper method, might be removed from code
    // returns false if Finish button is disabled
    final boolean isFinishEnabled() {
        return finishButton.isEnabled();
    }

    /** Iterator on the sequence of panels.
    * @see WizardDescriptor.Panel
    */
    public interface Iterator<Data> {
        /** Get the current panel.
        * @return the panel
        */
        public Panel<Data> current();

        /** Get the name of the current panel.
        * @return the name
        */
        public String name();

        /** Test whether there is a next panel.
        * @return <code>true</code> if so
        */
        public boolean hasNext();

        /** Test whether there is a previous panel.
        * @return <code>true</code> if so
        */
        public boolean hasPrevious();

        /** Move to the next panel.
        * I.e. increment its index, need not actually change any GUI itself.
        * @exception NoSuchElementException if the panel does not exist
        */
        public void nextPanel();

        /** Move to the previous panel.
        * I.e. decrement its index, need not actually change any GUI itself.
        * @exception NoSuchElementException if the panel does not exist
        */
        public void previousPanel();

        /** Add a listener to changes of the current panel.
        * The listener is notified when the possibility to move forward/backward changes.
        * @param l the listener to add
        */
        public void addChangeListener(ChangeListener l);

        /** Remove a listener to changes of the current panel.
        * @param l the listener to remove
        */
        public void removeChangeListener(ChangeListener l);
    }

    /** One wizard panel with a component on it.
     *
     * For good performance, implementation of this interface should be as
     * lightweight as possible. Defer creation and initialization of
     * UI component of wizard panel into {@link #getComponent} method.
     *
     * Please see complete guide at http://performance.netbeans.org/howto/dialogs/wizard-panels.html
     */
    public interface Panel<Data> {
        /** Get the component displayed in this panel.
         *
         * Note; method can be called from any thread, but not concurrently
         * with other methods of this interface. Please see complete guide at
         * http://performance.netbeans.org/howto/dialogs/wizard-panels.html for
         * correct implementation.
         *
         * @return the UI component of this wizard panel
         */
        public java.awt.Component getComponent();

        /** Help for this panel.
        * When the panel is active, this is used as the help for the wizard dialog.
        * @return the help or <code>null</code> if no help is supplied
        */
        public HelpCtx getHelp();

        /** Provides the wizard panel with the current data--either
        * the default data or already-modified settings, if the user used the previous and/or next buttons.
        * This method can be called multiple times on one instance of <code>WizardDescriptor.Panel</code>.
        * <p>The settings object is originally supplied to {@link WizardDescriptor#WizardDescriptor(WizardDescriptor.Iterator,Object)}.
        * In the case of a <code>TemplateWizard.Iterator</code> panel, the object is
        * in fact the <code>TemplateWizard</code>.
        * @param settings the object representing wizard panel state
        * @exception IllegalStateException if the the data provided
        * by the wizard are not valid.
        */
        public void readSettings(Data settings);

        /** Provides the wizard panel with the opportunity to update the
        * settings with its current customized state.
        * Rather than updating its settings with every change in the GUI, it should collect them,
        * and then only save them when requested to by this method.
        * Also, the original settings passed to {@link #readSettings} should not be modified (mutated);
        * rather, the object passed in here should be mutated according to the collected changes,
        * in case it is a copy.
        * This method can be called multiple times on one instance of <code>WizardDescriptor.Panel</code>.
        * <p>The settings object is originally supplied to {@link WizardDescriptor#WizardDescriptor(WizardDescriptor.Iterator,Object)}.
        * In the case of a <code>TemplateWizard.Iterator</code> panel, the object is
        * in fact the <code>TemplateWizard</code>.
        * @param settings the object representing wizard panel state
        */
        public void storeSettings(Data settings);

        /** Test whether the panel is finished and it is safe to proceed to the next one.
        * If the panel is valid, the "Next" (or "Finish") button will be enabled.
        * <p><strong>Tip:</strong> if your panel is actually the component itself
        * (so {@link #getComponent} returns <code>this</code>), be sure to specifically
        * override this method, as the unrelated implementation in {@link java.awt.Component#isValid}
        * if not overridden could cause your wizard to behave erratically.
        * @return <code>true</code> if the user has entered satisfactory information
        */
        public boolean isValid();

        /** Add a listener to changes of the panel's validity.
        * @param l the listener to add
        * @see #isValid
        */
        public void addChangeListener(ChangeListener l);

        /** Remove a listener to changes of the panel's validity.
        * @param l the listener to remove
        */
        public void removeChangeListener(ChangeListener l);
    }

    /** A special interface for panels in middle of the
    * iterators path that would like to have the finish button
    * enabled. So both Next and Finish are enabled on panel
    * implementing this interface.
    * @deprecated 4.28 Use FinishablePanel instead.
    */
    @Deprecated
    public interface FinishPanel<Data> extends Panel<Data> {
    }

    /** A special interface for panels that need to do additional
     * validation when Next or Finish button is clicked.
     * @since 4.28
     */
    public interface ValidatingPanel<Data> extends Panel<Data> {
        /**
         * Is called when Next of Finish buttons are clicked and
         * allows deeper check to find out that panel is in valid
         * state and it is ok to leave it.
         *
         * @throws WizardValidationException when validation fails
         * @since 4.28
         */
        public void validate() throws WizardValidationException;
    }


    /**
     * A special interface for panels that need to do additional
     * asynchronous validation when Next or Finish button is clicked.
     *
     * <p>During backround validation is Next or Finish button
     * disabled. On validation success wizard automatically
     * progress to next panel or finishes.
     *
     * <p>During backround validation Cancel button is hooked
     * to signal the validation thread using interrupt().
     *
     * @since 6.2 (16 May 2005)
     */
    public interface AsynchronousValidatingPanel<Data> extends ValidatingPanel<Data> {

        /**
         * Called synchronously from UI thread when Next
         * of Finish buttons clicked. It allows to lock user
         * input to assure official data for background validation.
         */
        public void prepareValidation();

        /**
         * Is called in separate thread when Next of Finish buttons
         * are clicked and allows deeper check to find out that panel
         * is in valid state and it is ok to leave it.
         *
         * @throws WizardValidationException when validation fails
         */
        public void validate() throws WizardValidationException;
    }


    /** A special interface for panel that needs to dynamically enabled
     * Finish button.
     * @since 4.28
     */
    public interface FinishablePanel<Data> extends Panel<Data> {
        /** Specify if this panel would enable Finish button. Finish button is
         * enabled if and only if isValid() returns true and isFinishPanel()
         * returns true.
         *
         * @return Finish button could be enabled
         * @since 4.28
         */
        boolean isFinishPanel();
    }

    /**
     * Iterator for a wizard that needs to somehow instantiate new objects.
     * (This interface can replace
     * <a href="@OPENIDE/LOADERS@/org/openide/loaders/TemplateWizard.Iterator.html"><code>TemplateWizard.Iterator</code></a>
     * in a template's declaration.)
     * @since org.openide/1 4.33
     */
    public interface InstantiatingIterator<Data> extends Iterator<Data> {
        /** Returns set of instantiated objects. If instantiation fails then wizard remains open to enable correct values.
         *
         * @throws IOException
         * @return a set of objects created (the exact type is at the discretion of the caller)
         */
        public Set/*<?>*/ instantiate() throws IOException;

        /** Initializes this iterator, called from WizardDescriptor's constructor.
         *
         * @param wizard wizard's descriptor
         */
        public void initialize(WizardDescriptor wizard);

        /** Uninitializes this iterator, called when the wizard is being
         * closed, no matter what closing option invoked.
         *
         * @param wizard wizard's descriptor
         */
        public void uninitialize(WizardDescriptor wizard);
    }
    
    /**
     * Iterator for a wizard that needs to somehow instantiate new objects outside ATW queue.
     * (This interface can replace
     * <a href="@OPENIDE/LOADERS@/org/openide/loaders/TemplateWizard.Iterator.html"><code>TemplateWizard.Iterator</code></a>
     * in a template's declaration.)
     * @since org.openide/1 6.5
     */
    public interface AsynchronousInstantiatingIterator<Data> extends InstantiatingIterator<Data> {

        /**
         * Is called in separate thread when the Finish button
         * are clicked and allows implement asynchronous
         * instantating of newly created objects.
         *
         * @throws IOException when instantiate fails
         * @return a set of objects created (the exact type is at the discretion of the caller)
         */
        public Set/*<?>*/ instantiate () throws IOException;

    }

    /**
     * Iterator for a wizard that wants to notify users while instantiate is running by a progress bar.
     * The method <code>instantiate</code> is called outside ATW queue.
     * (This interface can replace
     * <a href="@OPENIDE/LOADERS@/org/openide/loaders/TemplateWizard.Iterator.html"><code>TemplateWizard.Iterator</code></a>
     * in a template's declaration.)
     * @since org.openide.dialogs 7.1
     */
    public interface ProgressInstantiatingIterator<Data> extends AsynchronousInstantiatingIterator<Data> {

        /**
         * Is called in separate thread when the Finish button
         * are clicked and allows implement asynchronous instantating of newly created objects.
         * While instantiating users are notified by progress bar in wizard's panel. Notfication will
         * be visualized by a progress bar.
         * Note: The <code>ProgressHandle</code> is not started, need to start it and report progress by
         * messages in the <code>progress()</code> method.
         * 
         * @param handle progress bar handle
         * @throws IOException when instantiate fails
         * @return a set of objects created (the exact type is at the discretion of the caller)
         */
        public Set/*<?>*/ instantiate (ProgressHandle handle) throws IOException;
        
    }

    /** Special iterator that works on an array of <code>Panel</code>s.
    */
    public static class ArrayIterator<Data> extends Object implements Iterator<Data> {
        /** Array of items.
        */
        private Panel<Data>[] panels;

        /** Index into the array
        */
        private int index;

        /* Default constructor. It's here to allow subclasses to
        * be serializable easily. Panel initialization is done
        * through initializePanels() protected method. */
        public ArrayIterator() {
            panels = initializePanels();
            index = 0;
        }

        /** Construct an iterator.
        * @param array the list of panels to use
        */
        public ArrayIterator(Panel<Data>[] array) {
            panels = array;
            index = 0;
        }

        /**
         * Construct an iterator.
         * @param panels the list of panels to use
         * @since org.openide.dialogs 7.5
         */
        @SuppressWarnings("unchecked") // exists so that other code does not have to do it
        public ArrayIterator(List<Panel<Data>> panels) {
            this.panels = panels.toArray(new Panel[panels.size()]);
            index = 0;
        }

        /** Allows subclasses to initialize their arrays of panels when
        * constructed using default constructor.
        * (for example during deserialization.
        * Default implementation returns empty array. */
        @SuppressWarnings("unchecked")
        protected Panel<Data>[] initializePanels() {
            return new Panel[0];
        }

        /* The current panel.
        */
        public Panel<Data> current() {
            return panels[index];
        }

        /* Current name of the panel */
        public String name() {
            return NbBundle.getMessage(
                WizardDescriptor.class, "CTL_ArrayIteratorName", new Integer(index + 1), new Integer(panels.length)
            );
        }

        /* Is there a next panel?
        * @return true if so
        */
        public boolean hasNext() {
            return index < (panels.length - 1);
        }

        /* Is there a previous panel?
        * @return true if so
        */
        public boolean hasPrevious() {
            return index > 0;
        }

        /* Moves to the next panel.
        * @exception NoSuchElementException if the panel does not exist
        */
        public synchronized void nextPanel() {
            if ((index + 1) == panels.length) {
                throw new java.util.NoSuchElementException();
            }

            index++;
        }

        /* Moves to previous panel.
        * @exception NoSuchElementException if the panel does not exist
        */
        public synchronized void previousPanel() {
            if (index == 0) {
                throw new java.util.NoSuchElementException();
            }

            index--;
        }

        /* Ignores the listener, there are no changes in order of panels.
        */
        public void addChangeListener(ChangeListener l) {
        }

        /* Ignored.
        */
        public void removeChangeListener(ChangeListener l) {
        }

        /** Resets this iterator to initial state.
        * Called by subclasses when they need re-initialization of the iterator.
        */
        protected void reset() {
            index = 0;
        }
    }

    /** Listener to changes in the iterator and panels.
    */
    private final class Listener implements ChangeListener, ActionListener {
        Listener() {
        }

        /** Change in the observed objects */
        public void stateChanged(ChangeEvent ev) {
            updateState();
        }

        /** Action listener */
        public void actionPerformed(ActionEvent ev) {
            final Iterator<?> panels = data.getIterator(WizardDescriptor.this);
            if (wizardPanel != null) {
                wizardPanel.setErrorMessage(" ", null); //NOI18N
            }

            Object src = ev.getSource();
            err.log (Level.FINE, "actionPerformed entry. Source: " + src); // NOI18N
            if (src == nextButton) {
                final Dimension previousSize = panels.current().getComponent().getSize();
                Runnable onValidPerformer = new Runnable() {

                    public void run() {
                        err.log(Level.FINE,
                                "onValidPerformer on next button entry.");
                        panels.nextPanel();
                        try {
                            // change UI to show next step, show wait cursor during
                            // the change
                            goToNextStep(previousSize);
                        }
                        catch (IllegalStateException ise) {
                            panels.previousPanel();
                            String msg = ise.getMessage();
                            if (msg != null) {
                                // this is only for backward compatitility
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
                            } else {
                                // this should be used (it checks for exception
                                // annotations and severity)
                                Exceptions.printStackTrace(ise);
                            }
                            updateState();
                        }
                        err.log(Level.FINE,
                                "onValidPerformer on next button exit.");
                    }
                };
                lazyValidate(panels.current(), onValidPerformer);
            }

            if (ev.getSource() == previousButton) {
                panels.previousPanel();

                // show wait cursor when updating previous button
                updateStateWithFeedback();
            }

            if (ev.getSource() == finishButton) {
                Runnable onValidPerformer = new Runnable() {
                    public void run() {
                        err.log (Level.FINE, "onValidPerformer on finish button entry."); // NOI18N
                        
                        // disable all buttons to indicate that instantiate runs
                        previousButton.setEnabled (false);
                        nextButton.setEnabled (false);
                        finishButton.setEnabled (false);
                        cancelButton.setEnabled (false);
                        
                        Runnable performFinish = new Runnable () {
                            public void run () {
                                err.log (Level.FINE, "performFinish entry."); // NOI18N
                                Object oldValue = getValue();
                                    
                                // do instantiate
                                try {
                                    callInstantiate();
                                    setValueWithoutPCH(OK_OPTION);
                                    resetWizard();
                                } catch (IOException ioe) {
                                    // notify to log
                                    err.log(Level.FINE, null, ioe);

                                    setValueWithoutPCH(NEXT_OPTION);
                                    updateStateWithFeedback();

                                    // notify user by the wizard's status line
                                    putProperty(PROP_ERROR_MESSAGE, ioe.getLocalizedMessage());

                                    // if validation failed => cannot move to next panel
                                    return;
                                } catch (RuntimeException x) {
                                    // notify to log
                                    err.log(Level.FINE, null, x);

                                    setValueWithoutPCH(NEXT_OPTION);
                                    updateStateWithFeedback();

                                    // notify user by the wizard's status line
                                    putProperty(PROP_ERROR_MESSAGE, x.getLocalizedMessage());

                                    // if validation failed => cannot move to next panel
                                    return;
                                }
                                firePropertyChange(PROP_VALUE, oldValue, OK_OPTION);
                                
                                SwingUtilities.invokeLater (new Runnable () {
                                    public void run () {
                                        // all is OK
                                        // close wizrad
                                        err.log (Level.FINE, "WD.finishOption.fireActionPerformed()");
                                        finishOption.fireActionPerformed();
                                        err.log (Level.FINE, "Set value to OK_OPTION.");
                                        setValue (OK_OPTION);
                                    }
                                });
                                err.log (Level.FINE, "performFinish exit."); // NOI18N
                            }
                        };
                        
                        if (panels instanceof AsynchronousInstantiatingIterator) {
                            err.log (Level.FINE, "Do ASYNCHRONOUS_JOBS_RP.post(performFinish)."); // NOI18N
                            ASYNCHRONOUS_JOBS_RP.post (performFinish);
                        } else {
                            err.log (Level.FINE, "Run performFinish."); // NOI18N
                            performFinish.run ();
                        }
                        
                        err.log (Level.FINE, "onValidPerformer on finish button exit."); // NOI18N
                        
                    }
                };
                lazyValidate(panels.current(), onValidPerformer);
            }

            if (ev.getSource() == cancelButton) {
                if (backgroundValidationTask != null) {
                    backgroundValidationTask.cancel();
                }
                Object oldValue = getValue();
                setValueWithoutPCH(CANCEL_OPTION);

                if (Arrays.asList(getClosingOptions()).contains(cancelButton)) {
                    try {
                        resetWizard();
                    } catch (RuntimeException x) {
                        // notify to log
                        err.log(Level.FINE, null, x);
                    }

                }

                firePropertyChange(PROP_VALUE, oldValue, CANCEL_OPTION);
            }
        }
    }

    /** Listenes on a users client property changes
     */
    private class PropL implements PropertyChangeListener {
        PropL() {
        }

        /** Accepts client property changes of user component */
        public void propertyChange(PropertyChangeEvent e) {
            if (wizardPanel == null) {
                return;
            }

            String propName = e.getPropertyName();
            setPanelProperties((JComponent) wizardPanel.getRightComponent());

            if (PROP_CONTENT_DATA.equals(propName)) {
                wizardPanel.setContent(contentData);
                updateButtonAccessibleDescription();
            } else if (PROP_CONTENT_SELECTED_INDEX.equals(propName)) {
                wizardPanel.setSelectedIndex(contentSelectedIndex);
                updateButtonAccessibleDescription();
            } else if (PROP_CONTENT_BACK_COLOR.equals(propName)) {
                wizardPanel.setContentBackColor(contentBackColor);
            } else if (PROP_CONTENT_FOREGROUND_COLOR.equals(propName)) {
                wizardPanel.setContentForegroundColor(contentForegroundColor);
            } else if (PROP_IMAGE.equals(propName)) {
                wizardPanel.setImage(image);
            } else if (PROP_IMAGE_ALIGNMENT.equals(propName)) {
                wizardPanel.setImageAlignment(imageAlignment);
            } else if (PROP_HELP_URL.equals(propName)) {
                wizardPanel.setHelpURL(helpURL);
            }
        }
    }

    // end of calling to InstantiatingIterator

    /** Panel which paints image as its background.
     */
    private static class ImagedPanel extends JComponent implements Accessible, Runnable {
        /** background image */
        Image image;

        /** helper variables for passing image between threads and painting
         * methods */
        Image tempImage;

        /** helper variables for passing image between threads and painting
         * methods */
        Image image2Load;

        /** true if default image is used */
        boolean isDefault = false;

        /** true if loading of image is in progress, false otherwise */
        boolean loadPending = false;
        boolean north = true;

        /** sync lock for image variables access */
        private final Object IMAGE_LOCK = new Object();

        /** Constrcuts panel with given image on background.
         * @param im background image, null means default image
         */
        public ImagedPanel(Image im) {
            setImage(im);
            setLayout(new BorderLayout());
            setOpaque(true);
        }

        /** Overriden to paint backround image */
        protected void paintComponent(Graphics graphics) {
            graphics.setColor(getBackground());
            graphics.fillRect(0, 0, getWidth(), getHeight());

            if (image != null) {
                graphics.drawImage(image, 0, north ? 0 : (getHeight() - image.getHeight(null)), this);
            } else if (image2Load != null) {
                loadImageInBackground(image2Load);
                image2Load = null;
            }
        }

        public void setImageAlignment(String align) {
            north = "North".equals(align); // NOI18N
        }

        /** Sets background image for this component. Image will be loaded
         * asynchronously if not loaded yet. Null means default image.
         */
        public void setImage(Image im) {
            if (im != null) {
                loadImage(im);
                isDefault = false;

                return;
            }

            if (!isDefault) {
                loadImage(getDefaultImage());
                isDefault = true;
            }
        }

        private void loadImage(Image im) {
            // check image and just set variable if fully loaded already
            MediaTracker mt = new MediaTracker(this);
            mt.addImage(im, 0);

            if (mt.checkID(0)) {
                image = im;

                if (isShowing()) {
                    repaint();
                }

                return;
            }

            // start loading in background or just mark that loading should
            // start when paint is invoked
            if (isShowing()) {
                loadImageInBackground(im);
            } else {
                synchronized (IMAGE_LOCK) {
                    image = null;
                }

                image2Load = im;
            }
        }

        private void loadImageInBackground(Image image) {
            synchronized (IMAGE_LOCK) {
                tempImage = image;

                // coalesce with previous task if hasn't really started yet
                if (loadPending) {
                    return;
                }

                loadPending = true;
            }

            // 30ms is safety time to ensure code will run asynchronously
            RequestProcessor.getDefault().post(this, 30);
        }

        /** Loads image stored in image2Load variable.
         * Then invokes repaint when image is fully loaded.
         */
        public void run() {
            Image localImage;

            // grab value 
            synchronized (IMAGE_LOCK) {
                localImage = tempImage;
                tempImage = null;
                loadPending = false;
            }

            // actually loads image
            ImageIcon localImageIcon = new ImageIcon(localImage);
            boolean shouldRepaint = false;

            synchronized (IMAGE_LOCK) {
                // don't commit results if another loading was started after us 
                if (!loadPending) {
                    image = localImageIcon.getImage();

                    // keep repaint call out of sync section
                    shouldRepaint = true;
                }
            }

            if (shouldRepaint) {
                repaint();
            }
        }
    }

    /** Text list cell renderer. Wraps text of items at specified width. Allows numbering
     * of items.
     */
    private static class WrappedCellRenderer extends JPanel implements ListCellRenderer {
        JTextArea ta = new JTextArea();
        JLabel numberLabel;
        int selected = -1;
        boolean contentNumbered;
        int taWidth;

        /**
         * @param contentNumbered Whether content will be numbered
         * @param wrappingWidth Width of list item.
         */
        private WrappedCellRenderer(boolean contentNumbered, int wrappingWidth) {
            super(new BorderLayout());
            this.contentNumbered = contentNumbered;

            ta.setOpaque(false);
            ta.setEditable(false);
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            ta.setFont(UIManager.getFont("Label.font")); // NOI18N
            ta.getAccessibleContext().setAccessibleDescription(""); // NOI18N

            taWidth = wrappingWidth - 12 - 12;

            numberLabel = new JLabel() {
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);

                            // #9804. Draw bullet if the content is not numbered.
                            if (!WrappedCellRenderer.this.contentNumbered) {
                                java.awt.Rectangle rect = g.getClipBounds();
                                g.fillOval(rect.x, rect.y, 7, 7);
                            }
                        }
                    };
            numberLabel.setLabelFor(ta); // a11y
            numberLabel.setHorizontalAlignment(SwingConstants.LEFT);
            numberLabel.setFont(ta.getFont());
            numberLabel.setOpaque(false);
            numberLabel.setPreferredSize(new Dimension(25, 0));
            add(numberLabel, BorderLayout.WEST);
            taWidth -= 25;

            Insets taInsets = ta.getInsets();
            ta.setSize(taWidth, taInsets.top + taInsets.bottom + 1);

            add(ta, BorderLayout.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setOpaque(false);
        }

        public Component getListCellRendererComponent(
            JList list, Object value, int index, boolean isSelected, boolean cellHasFocus
        ) {
            if (index == selected) {
                numberLabel.setFont(doDeriveFont(numberLabel.getFont(), Font.BOLD));
                ta.setFont(doDeriveFont(ta.getFont(), Font.BOLD));
            } else {
                numberLabel.setFont(doDeriveFont(numberLabel.getFont(), Font.PLAIN));
                ta.setFont(doDeriveFont(ta.getFont(), Font.PLAIN));
            }

            if (contentNumbered) {
                numberLabel.setText(Integer.toString(index + 1) + "."); // NOI18N
            }

            // #21322: on JDK1.4 wrapping width is cleared between two rendering runs
            Insets taInsets = ta.getInsets();
            ta.setSize(taWidth, taInsets.top + taInsets.bottom + 1);
            ta.setText((String) value);

            return this;
        }

        private void setSelectedIndex(int i) {
            selected = i;
        }

        private void setForegroundColor(Color color) {
            if (numberLabel != null) {
                numberLabel.setForeground(color);
                numberLabel.setBackground(color);
            }

            ta.setForeground(color);
        }
    }

    /** Wizard panel. Allows auto layout of content, wizard panel name and input panel.
     */
    private static class WizardPanel extends JPanel {
        /** Users panel is inserted into this panel. */
        private JPanel rightPanel = new JPanel(new BorderLayout());

        /** Name of the users panel. */
        private JLabel panelName = new JLabel("Step"); //NOI18N

        /** List of content. */
        private JList contentList;

        /** Users component. Should be held for removing from rightPanel */
        private Component rightComponent;

        /** Panel which paints image */
        private ImagedPanel contentPanel;

        /** Name of content. Can be switched off.  */
        private JPanel contentLabelPanel;

        /** Wrapped list cell renderer */
        private WrappedCellRenderer cellRenderer;

        /** Tabbed pane is used only when both content and help are displayed */
        private JTabbedPane tabbedPane;

        /** HTML Browser is used only when help is displayed in the left pane */
        private HtmlBrowser htmlBrowser;

        /** Each wizard panel have to be larger or same as this */
        private Dimension cachedDimension;

        /** Label of steps pane */
        private JLabel label;
        
        private JPanel progressBarPanel;

        /** Selected index of content */
        private int selectedIndex;
        private javax.swing.JLabel m_lblMessage;
        private Color nbErrorForeground;
        private Color nbWarningForeground;

        /** Creates new <CODE>WizardPanel<CODE>.
         * @param contentDisplayed whether content will be displayed in the left pane
         * @param helpDisplayed whether help will be displayed in the left pane
         * @param contentNumbered whether content will be numbered
         * @param leftDimension dimension of content or help pane
         */
        public WizardPanel(
            boolean contentDisplayed, boolean helpDispalyed, boolean contentNumbered, Dimension leftDimension
        ) {
            super(new BorderLayout());
            initComponents(contentDisplayed, helpDispalyed, contentNumbered, leftDimension);
            setOpaque(false);
            resetPreferredSize();
        }

        private void initComponents(
            boolean contentDisplayed, boolean helpDisplayed, boolean contentNumbered, Dimension leftDimension
        ) {
            if (contentDisplayed) {
                createContentPanel(contentNumbered, leftDimension);

                if (!helpDisplayed) {
                    add(contentPanel, BorderLayout.WEST);
                }
            }

            if (helpDisplayed) {
                htmlBrowser = new BoundedHtmlBrowser(leftDimension);
                htmlBrowser.setPreferredSize(leftDimension);

                if (!contentDisplayed) {
                    add(htmlBrowser, BorderLayout.WEST);
                }
            }

            if (helpDisplayed && contentDisplayed) {
                tabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
                tabbedPane.addTab(NbBundle.getMessage(WizardDescriptor.class, "CTL_ContentName"), contentPanel);
                tabbedPane.addTab(NbBundle.getMessage(WizardDescriptor.class, "CTL_HelpName"), htmlBrowser);
                tabbedPane.setEnabledAt(1, false);
                tabbedPane.setOpaque(false);

                //                tabbedPane.setPreferredSize(leftDimension);
                add(tabbedPane, BorderLayout.WEST);
            }

            panelName.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, panelName.getForeground()));
            panelName.setFont(doDeriveFont(panelName.getFont(), Font.BOLD));

            JPanel labelPanel = new JPanel(new BorderLayout());
            labelPanel.add(panelName, BorderLayout.NORTH);
            labelPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 11));
            rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 11, 11));
            panelName.setLabelFor(labelPanel);

            nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
            if (nbErrorForeground == null) {
                //nbErrorForeground = new Color(89, 79, 191); // RGB suggested by Bruce in #28466
                nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
            }
            
            nbWarningForeground = UIManager.getColor("nb.warningForeground"); //NOI18N
            if (nbWarningForeground == null) {
                nbWarningForeground = new Color(51, 51, 51); // Label.foreground
            }
            
            JPanel errorPanel = new JPanel(new BorderLayout());
            errorPanel.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 11));
            m_lblMessage = new FixedHeightLabel ();
            m_lblMessage.setForeground (nbErrorForeground);
            errorPanel.add(m_lblMessage, BorderLayout.CENTER);
            
            progressBarPanel = new JPanel (new BorderLayout ());
            progressBarPanel.setVisible (false);
            
            if (contentDisplayed) {
                // place for visualize progress bar in content panel (if contentDisplayed)
                progressBarPanel.setOpaque (false);
                progressBarPanel.setBorder (BorderFactory.createEmptyBorder (0, 4, 7, 4));
                contentPanel.add (progressBarPanel, BorderLayout.SOUTH);
            } else {
                // placeholder for progress bar components in WizardPanel (if no contentDisplayed set)
                progressBarPanel.add (new JLabel (), BorderLayout.NORTH);
                JProgressBar pb = new JProgressBar ();
                pb.setOrientation (JProgressBar.HORIZONTAL);
                pb.setAlignmentX(0.5f);
                pb.setAlignmentY(0.5f);
                pb.setString ("0"); // NOI18N
                progressBarPanel.add (pb, BorderLayout.CENTER);

                progressBarPanel.setBorder (BorderFactory.createEmptyBorder (4, 0, 0, 0));
                errorPanel.add (progressBarPanel, BorderLayout.SOUTH);
            }

            JPanel fullRightPanel = new JPanel(new BorderLayout());
            fullRightPanel.add(labelPanel, BorderLayout.NORTH);
            fullRightPanel.add(rightPanel, BorderLayout.CENTER);
            fullRightPanel.add(errorPanel, BorderLayout.SOUTH);

            // #65506: the wizard panel should fit into window w/o scrollbar
            add(fullRightPanel, BorderLayout.CENTER);

            if (getBorder() == null) {
                // Look & Feel has not set the border already
                JSeparator sep = new JSeparator();
                sep.setForeground(Color.darkGray);
                add(sep, BorderLayout.SOUTH);
            }
        }

        public void setErrorMessage(String msg, Boolean canContinue) {
            m_lblMessage.setForeground(nbErrorForeground);
            if (msg != null && msg.trim ().length () > 0 && canContinue != null) {
                if (canContinue.booleanValue ()) {
                    m_lblMessage.setIcon (new ImageIcon (Utilities.loadImage ("org/netbeans/modules/dialogs/warning.gif"))); // NOI18N
                    m_lblMessage.setForeground (nbWarningForeground);
                } else {
                    m_lblMessage.setIcon (new ImageIcon (Utilities.loadImage ("org/netbeans/modules/dialogs/error.gif"))); // NOI18N
                }
                m_lblMessage.setToolTipText (msg);
            } else {
                m_lblMessage.setIcon (null);
                m_lblMessage.setToolTipText (null);
            }
            
            m_lblMessage.setText(msg);
        }
        
        private void setProgressComponent (JComponent progressComp, JLabel progressLabel) {
            if (progressLabel != null) {
                progressLabel.setText (PROGRESS_BAR_DISPLAY_NAME);
                progressBarPanel.add (progressLabel, BorderLayout.NORTH);
            }
            progressBarPanel.add (progressComp, BorderLayout.CENTER);
            progressBarPanel.setVisible (true);
        }

        /** Creates content panel.
         * @param contentNumbered <CODE>boolean</CODE> whether content will be numbered
         * @param leftDimension <CODE>Dimension</CODE> dimension of content pane
         */
        private void createContentPanel(boolean contentNumbered, Dimension leftDimension) {
            contentList = new JList();
            cellRenderer = new WrappedCellRenderer(contentNumbered, leftDimension.width);
            cellRenderer.setOpaque(false);
            contentList.setCellRenderer(cellRenderer);
            contentList.setOpaque(false);
            contentList.setEnabled(false);
            contentList.getAccessibleContext().setAccessibleDescription(""); // NOI18N

            JScrollPane scroll = new JScrollPane(contentList);
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.getViewport().setOpaque(false);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            // #89392: remove GTK's viewport border
            scroll.setViewportBorder(null);

            label = new JLabel(NbBundle.getMessage(WizardDescriptor.class, "CTL_ContentName"));
            label.setForeground(Color.white);
            label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, label.getForeground()));
            label.setFont(doDeriveFont(label.getFont(), Font.BOLD));
            contentLabelPanel = new JPanel(new BorderLayout());
            contentLabelPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
            contentLabelPanel.setOpaque(false);
            contentLabelPanel.add(label, BorderLayout.NORTH);

            contentPanel = new ImagedPanel(null);
            contentPanel.add(contentLabelPanel, BorderLayout.NORTH);
            contentPanel.add(scroll, BorderLayout.CENTER);

            contentPanel.setPreferredSize(leftDimension);
            label.setLabelFor(contentList);
        }

        /** Setter for lists items.
         * @param content Array of list items.
         */
        public void setContent(final String[] content) {
            final JList list = contentList;

            if (list == null) {
                return;
            }

            // #18055: Ensure it runs in AWT thread.
            // Remove this when component handling will be assured
            // by other means that runs always in AWT.
            Mutex.EVENT.writeAccess(
                new Runnable() {
                    public void run() {
                        list.setListData(content);
                        list.revalidate();
                        list.repaint();
                        contentLabelPanel.setVisible(content.length > 0);
                    }
                }
            );
        }

        /** Setter for selected list item.
         * @param index Index of selected item in the list.
         */
        public void setSelectedIndex(final int index) {
            selectedIndex = index;

            if (cellRenderer != null) {
                cellRenderer.setSelectedIndex(index);

                final JList list = contentList;

                if (list == null) {
                    return;
                }

                // #18055. See previous #18055 comment.
                Mutex.EVENT.readAccess(
                    new Runnable() {
                        public void run() {
                            list.ensureIndexIsVisible(index);

                            // Fix of #10787.
                            // This is workaround for swing bug - BasicListUI doesn't ask for preferred
                            // size of rendered list cell as a result of property selectedIndex change. 
                            // It does only on certain JList property changes (e.g. fixedCellWidth).
                            // Maybe subclassing BasicListUI could be better fix.
                            list.setFixedCellWidth(0);
                            list.setFixedCellWidth(-1);
                        }
                    }
                );
            }
        }

        /** Setter for content background color.
         * @param color content background color.
         */
        public void setContentBackColor(Color color) {
            if (contentPanel != null) {
                contentPanel.setBackground(color);
            }
        }

        /** Setter for content foreground color.
         * @param color content foreground color.
         */
        public void setContentForegroundColor(Color color) {
            if (cellRenderer == null) {
                return;
            }

            cellRenderer.setForegroundColor(color);
            label.setForeground(color);
            label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, label.getForeground()));
        }

        /** Setter for content background image.
         * @param image content background image
         */
        public void setImage(Image image) {
            if (contentPanel != null) {
                contentPanel.setImage(image);
            }
        }

        /** Setter for image alignment.
         * @param align image alignment - 'North', 'South'
         */
        public void setImageAlignment(String align) {
            if (contentPanel != null) {
                contentPanel.setImageAlignment(align);
            }
        }

        /** Setter for user's component.
         * @param c user's component
         */
        public void setRightComponent(Component c) {
            if (rightComponent != null) {
                rightPanel.remove(rightComponent);
            }

            rightComponent = c;
            rightPanel.add(rightComponent, BorderLayout.CENTER);

            //            validate();
        }

        /** Getter for user's component.
         * @return <CODE>Component</CODE> user's component
         */
        public Component getRightComponent() {
            return rightComponent;
        }

        /** Setter for wizard panel name.
         * @param name panel name
         */
        public void setPanelName(String name) {
            panelName.setText(name);
        }

        /** Setter for help URL.
         * @param helpURL help URL
         */
        public void setHelpURL(URL helpURL) {
            if (htmlBrowser == null) {
                return;
            }

            if (helpURL != null) {
                if (!helpURL.equals(htmlBrowser.getDocumentURL())) {
                    htmlBrowser.setURL(helpURL);
                }

                if (tabbedPane != null) {
                    tabbedPane.setEnabledAt(tabbedPane.indexOfComponent(htmlBrowser), true);
                }
            } else if (tabbedPane != null) {
                tabbedPane.setSelectedComponent(contentPanel);
                tabbedPane.setEnabledAt(tabbedPane.indexOfComponent(htmlBrowser), false);
            }
        }

        public void resetPreferredSize() {
            cachedDimension = new Dimension(600, 365);
        }

        public Dimension getPreferredSize() {
            Dimension dim = super.getPreferredSize();

            if (dim.height > cachedDimension.height) {
                cachedDimension.height = dim.height;
            }

            if (dim.width > cachedDimension.width) {
                cachedDimension.width = dim.width;
            }

            return cachedDimension;
        }

        /** Overriden to delegate call to user component.
         */
        public void requestFocus() {
            if ((rightComponent != null) && rightComponent.isDisplayable()) {
                JComponent comp = (JComponent) rightComponent;
                Container rootAnc = comp.getFocusCycleRootAncestor();
                FocusTraversalPolicy policy = rootAnc.getFocusTraversalPolicy();
                Component focus = policy.getComponentAfter(rootAnc, comp);

                if (focus != null) {
                    focus.requestFocus();
                } else {
                    comp.requestFocus();
                }
            } else {
                super.requestFocus();
            }
        }

        /** Overriden to delegate call to user component.
         */
        @Deprecated
        public boolean requestDefaultFocus() {
            if (rightComponent instanceof JComponent) {
                return ((JComponent) rightComponent).requestDefaultFocus();
            }

            return super.requestDefaultFocus();
        }

        public javax.accessibility.AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) {
                accessibleContext = new AccessibleWizardPanel();
            }

            return accessibleContext;
        }

        private class AccessibleWizardPanel extends AccessibleJPanel {
            AccessibleWizardPanel() {
            }

            public String getAccessibleDescription() {
                if (accessibleDescription != null) {
                    return accessibleDescription;
                }

                if (rightComponent instanceof Accessible) {
                    if (rightComponent.getAccessibleContext().getAccessibleDescription() == null) {
                        return null;
                    }

                    return NbBundle.getMessage(
                        WizardDescriptor.class, "ACSD_WizardPanel", new Integer(selectedIndex + 1), panelName.getText(),
                        rightComponent.getAccessibleContext().getAccessibleDescription()
                    );
                }

                return super.getAccessibleDescription();
            }
        }
    }

    /** Overriden to return wished preferred size */
    private static class BoundedHtmlBrowser extends HtmlBrowser {
        Dimension dim;

        public BoundedHtmlBrowser(Dimension d) {
            super(false, false);
            dim = d;
        }

        public Dimension getPreferredSize() {
            return dim;
        }
    }

    // helper, make possible close wizard as finish
    static class FinishAction extends Object {
        ActionListener listner;

        public void addActionListener(ActionListener ac) {
            listner = ac;
        }

        public void removeActionListener(ActionListener ac) {
            listner = null;
        }

        public void fireActionPerformed() {
            if (listner != null) {
                listner.actionPerformed(new ActionEvent(this, 0, ""));
            }
        }
    }
    
    private static final class FixedHeightLabel extends JLabel {
        
        private static final int ESTIMATED_HEIGHT = 16;

        public FixedHeightLabel () {
            super ();
        }
        
        public Dimension getPreferredSize() {
            Dimension preferredSize = super.getPreferredSize();
            assert ESTIMATED_HEIGHT == Utilities.loadImage ("org/netbeans/modules/dialogs/warning.gif").getHeight (null) : "Use only 16px icon.";
            preferredSize.height = Math.max (ESTIMATED_HEIGHT, preferredSize.height);
            return preferredSize;
        }
    }
    
    private static final class SettingsAndIterator<Data> {
        private final Iterator<Data> panels;
        private final Data settings;
        private final boolean useThis;
        /** current panel */
        private Panel<Data> current;

        
        public SettingsAndIterator(Iterator<Data> iterator, Data settings) {
            this(iterator, settings, false);
        }
        public SettingsAndIterator(Iterator<Data> iterator, Data settings, boolean useThis) {
            this.panels = iterator;
            this.settings = settings;
            this.useThis = useThis;
        }
        public static SettingsAndIterator<WizardDescriptor> create(Iterator<WizardDescriptor> iterator) {
            return new SettingsAndIterator<WizardDescriptor>(iterator, null, true);
        }
        public static SettingsAndIterator<Void> empty() {
            return new SettingsAndIterator<Void>(new EmptyPanel(), (Void)null);
        }

        public Iterator<Data> getIterator(WizardDescriptor caller) {
            return panels;
        }
        
        @SuppressWarnings("unchecked")
        public Data getSettings(WizardDescriptor caller) {
            return useThis ? (Data)caller : settings;
        }
        
        public SettingsAndIterator<Data> clone(Iterator<Data> it) {
            SettingsAndIterator<Data> s = new SettingsAndIterator<Data>(it, settings, useThis);
            return s;
        }
    }
    
    private static final class EmptyPanel implements Panel<Void>, Iterator<Void> {
        public Component getComponent() {
            return new JPanel();
        }

        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        public void readSettings(Void settings) {
        }

        public void storeSettings(Void settings) {
        }

        public boolean isValid() {
            return true;
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }

        public Panel<Void> current() {
            return this;
        }

        public String name() {
            return ""; // NORTH
        }

        public boolean hasNext() {
            return false;
        }

        public boolean hasPrevious() {
            return false;
        }

        public void nextPanel() {
        }

        public void previousPanel() {
        }
    } // end of EmptyPanel
}
