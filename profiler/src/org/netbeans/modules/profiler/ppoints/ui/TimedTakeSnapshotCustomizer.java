/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.profiler.ppoints.ui;

import org.netbeans.modules.profiler.ppoints.TimedGlobalProfilingPoint;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.lib.profiler.ui.UIUtils;


/**
 *
 * @author Jiri Sedlacek
 */
public class TimedTakeSnapshotCustomizer extends ValidityAwarePanel implements DocumentListener, ValidityListener,
                                                                               HelpCtx.Provider {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String NAME_LABEL_TEXT = NbBundle.getMessage(TimedTakeSnapshotCustomizer.class,
                                                                      "TimedTakeSnapshotCustomizer_NameLabelText"); // NOI18N
    private static final String SETTINGS_LABEL_TEXT = NbBundle.getMessage(TimedTakeSnapshotCustomizer.class,
                                                                          "TimedTakeSnapshotCustomizer_SettingsLabelText"); // NOI18N
    private static final String TIME_LABEL_TEXT = NbBundle.getMessage(TimedTakeSnapshotCustomizer.class,
                                                                      "TimedTakeSnapshotCustomizer_TimeLabelText"); // NOI18N
                                                                                                                    // -----
    private static final String HELP_CTX_KEY = "TimedTakeSnapshotCustomizer.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);
    private static int defaultTextComponentHeight = -1;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private JLabel captionLabel;
    private JLabel nameLabel;
    private JLabel settingsHeaderLabel;
    private JLabel timeHeaderLabel;
    private JPanel captionPanel;
    private JSeparator settingsHeaderSeparator;
    private JSeparator timeHeaderSeparator;
    private JTextField nameTextField;
    private SnapshotCustomizer snapshotCustomizer;
    private TimeCustomizer timeCustomizer;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public TimedTakeSnapshotCustomizer(String caption, Icon icon) {
        initComponents(caption, icon);
        normalizeCaptionAreaWidth();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }

    public Component getInitialFocusTarget() {
        return nameTextField;
    }

    public void setPPFile(String fileName) {
        snapshotCustomizer.setPPFile(fileName);
    }

    public String getPPFile() {
        return snapshotCustomizer.getPPFile();
    }

    public void setPPName(String name) {
        nameTextField.setText(name);
    }

    public String getPPName() {
        return nameTextField.getText();
    }

    public void setPPResetResults(boolean resetResults) {
        snapshotCustomizer.setPPResetResults(resetResults);
    }

    public boolean getPPResetResults() {
        return snapshotCustomizer.getPPResetResults();
    }

    public void setPPTarget(boolean target) {
        snapshotCustomizer.setPPTarget(target);
    }

    public boolean getPPTarget() {
        return snapshotCustomizer.getPPTarget();
    }

    public void setPPType(boolean type) {
        snapshotCustomizer.setPPType(type);
    }

    public boolean getPPType() {
        return snapshotCustomizer.getPPType();
    }

    public int getPreferredCaptionAreaWidth() {
        // Not used for this customizer
        return -1;
    }

    public void setTimeCondition(TimedGlobalProfilingPoint.TimeCondition condition) {
        timeCustomizer.setTimeCondition(condition);
    }

    public TimedGlobalProfilingPoint.TimeCondition getTimeCondition() {
        return timeCustomizer.getTimeCondition();
    }

    public void changedUpdate(DocumentEvent e) {
        updateValidity();
    }

    //  private int getDefaultTextComponentHeight() {
    //    if (defaultTextComponentHeight == -1) defaultTextComponentHeight = new JComboBox().getPreferredSize().height;
    //    return defaultTextComponentHeight;
    //  }
    public void initComponents(String caption, Icon icon) {
        setLayout(new GridBagLayout());

        GridBagConstraints constraints;

        // captionPanel
        captionPanel = new JPanel(new BorderLayout(0, 0));
        captionPanel.setOpaque(true);
        captionPanel.setBackground(UIUtils.getProfilerResultsBackground());

        // captionLabel
        captionLabel = new JLabel(caption, icon, SwingConstants.LEADING);
        captionLabel.setFont(captionLabel.getFont().deriveFont(Font.BOLD));
        captionLabel.setOpaque(false);
        captionLabel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 6));
        captionPanel.add(captionLabel, BorderLayout.WEST);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 16, 0);
        add(captionPanel, constraints);

        // --- next row ----------------------------------------------------------

        // nameLabel
        nameLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, NAME_LABEL_TEXT);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 8, 10, 5);
        add(nameLabel, constraints);

        // nameTextField
        nameTextField = new JTextField("") { // NOI18N
                public Dimension getPreferredSize() {
                    return (TimedTakeSnapshotCustomizer.this.getParent() instanceof JViewport) ? getMinimumSize()
                                                                                               : new Dimension(400,
                                                                                                               super
                                                                                                                                                                                                                                       .getPreferredSize().height);
                }

                public Dimension getMinimumSize() {
                    return new Dimension(super.getMinimumSize().width, super.getPreferredSize().height);
                }
            };
        nameLabel.setLabelFor(nameTextField);
        nameTextField.getDocument().addDocumentListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 10, 13);
        add(nameTextField, constraints);

        // --- next row ----------------------------------------------------------
        JPanel settingsHeaderContainer = new JPanel(new GridBagLayout());

        // settingsHeaderLabel
        settingsHeaderLabel = new JLabel(SETTINGS_LABEL_TEXT);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 5);
        settingsHeaderContainer.add(settingsHeaderLabel, constraints);

        // settingsHeaderSeparator
        settingsHeaderSeparator = new JSeparator() {
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        settingsHeaderContainer.add(settingsHeaderSeparator, constraints);

        // locationHeaderContainer
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 8, 5, 8);
        add(settingsHeaderContainer, constraints);

        // --- next row ----------------------------------------------------------

        // snapshotCustomizer
        snapshotCustomizer = new SnapshotCustomizer();
        snapshotCustomizer.addValidityListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 20, 12, 13);
        add(snapshotCustomizer, constraints);

        // --- next row ----------------------------------------------------------
        JPanel triggerHeaderContainer = new JPanel(new GridBagLayout());

        // timeHeaderLabel
        timeHeaderLabel = new JLabel(TIME_LABEL_TEXT);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets = new Insets(0, 0, 0, 5);
        triggerHeaderContainer.add(timeHeaderLabel, constraints);

        // timeHeaderSeparator
        timeHeaderSeparator = new JSeparator() {
                public Dimension getMinimumSize() {
                    return getPreferredSize();
                }
            };
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.weightx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 0, 0, 0);
        triggerHeaderContainer.add(timeHeaderSeparator, constraints);

        // triggerHeaderContainer
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 8, 5, 8);
        add(triggerHeaderContainer, constraints);

        // --- next row ----------------------------------------------------------

        // timeCustomizer
        timeCustomizer = new TimeCustomizer();
        timeCustomizer.addValidityListener(this);
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 20, 5, 13);
        add(timeCustomizer, constraints);

        // --- next row ----------------------------------------------------------
        JPanel fillerPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 7;
        constraints.weighty = 1;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(0, 0, 0, 0);
        add(fillerPanel, constraints);
    }

    public void insertUpdate(DocumentEvent e) {
        updateValidity();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //    try {
        //      UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); //NOI18N
        ////      UIManager.setLookAndFeel("plaf.metal.MetalLookAndFeel"); //NOI18N
        ////      UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel"); //NOI18N
        ////      UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel"); //NOI18N
        //    } catch (Exception e){};
        //    
        //    TimedTakeSnapshotCustomizer main = new TimedTakeSnapshotCustomizer();
        //    main.addValidityListener(new ValidityListener() {
        //      public void validityChanged(boolean isValid) { System.err.println(">>> Validity changed to " + isValid); }
        //    });
        //    
        //    JFrame frame = new JFrame("Customize Profiling Point");
        //    frame.getContentPane().add(main);
        //    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //    frame.pack();
        //    frame.setVisible(true);
    }

    public void normalizeCaptionAreaWidth() {
        // Not used for this customizer
    }

    public void removeUpdate(DocumentEvent e) {
        updateValidity();
    }

    public void validityChanged(boolean isValid) {
        updateValidity();
    }

    private boolean isNameEmpty() {
        return nameTextField.getText().trim().length() == 0;
    }

    private void updateValidity() {
        boolean isValid = !isNameEmpty() && snapshotCustomizer.areSettingsValid() && timeCustomizer.areSettingsValid();

        if (isValid != areSettingsValid()) {
            fireValidityChanged(isValid);
        }
    }
}
