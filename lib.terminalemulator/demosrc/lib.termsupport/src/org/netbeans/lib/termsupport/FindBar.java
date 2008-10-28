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
 * 
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
package org.netbeans.lib.termsupport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A panel to facilitate text searches with the following elements:
 * <ul
 * <li>A search pattern text entry field.
 * <li>A Prev search button.
 * <li>A Next search button.
 * <li>An error area.
 * <li>A close button.
 * </ul>
 * <p>
 * A FindBar doesn't do any searching by itself but acts as a controller of a
 * {@link FindState} which it can multiplex via {@link FindBar#setState}.
 * @author ivan
 */
public final class FindBar extends JPanel {

    private static final Insets BUTTON_INSETS = new Insets(2, 1, 0, 1);

    private final Owner owner;
    private FindState state;

    private boolean updating = false;   // true while view is being updated

    private Action closeAction;
    private Action nextAction;
    private Action prevAction;

    private JTextField findText;
    private JLabel errorLabel;
    private Color originalColor;
    private final Color errorColor = Color.RED;

    /**
     * Callback interface used to communicate to the owner of a {@link FindBar}
     * that it's close button was pressed.
     */
    public interface Owner {
        public void close(FindBar who);
    }

    private final class CloseAction extends AbstractAction {

        public CloseAction() {
            super();
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_W,
                                                           InputEvent.CTRL_MASK);
            putValue(ACCELERATOR_KEY, accelerator);
            putValue(SMALL_ICON, new ImageIcon(FindBar.class.getResource("find_close.png")));
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }
            close();
        }
    }

    private final class NextAction extends AbstractAction {

        public NextAction() {
            super("Next");
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_N,
                                                           InputEvent.ALT_MASK);
            putValue(ACCELERATOR_KEY, accelerator);
            putValue(SMALL_ICON, new ImageIcon(FindBar.class.getResource("find_next.png")));
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }
            next();
        }
    }

    private final class PrevAction extends AbstractAction {

        public PrevAction() {
            super("Previous");
            KeyStroke accelerator = KeyStroke.getKeyStroke(KeyEvent.VK_P,
                                                           InputEvent.ALT_MASK);
            putValue(ACCELERATOR_KEY, accelerator);
            putValue(SMALL_ICON, new ImageIcon(FindBar.class.getResource("find_previous.png")));
        }

        public void actionPerformed(ActionEvent e) {
            if (!isEnabled()) {
                return;
            }
            prev();
        }
    }

    /**
     * Construct a FindBar.
     * @param owner Is used to call {@link Owner#close()} when the close
     *              button is pressed.
     */
    public FindBar(Owner owner) {
        super();
        this.owner = owner;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        JLabel findLabel = new JLabel();
        findLabel.setText("Find:");
        findText = new JTextField() {

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, super.getPreferredSize().height);
            }
        };
        originalColor = findText.getForeground();

        findText.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                if (!updating) {
                    state.setPattern(findText.getText());
                    error(state.getStatus(), false);
                }
            }

            public void removeUpdate(DocumentEvent e) {
                insertUpdate(e);
            }

            public void changedUpdate(DocumentEvent e) {
                insertUpdate(e);
            }
        });

        findLabel.setLabelFor(findText);
        prevAction = new PrevAction();
        JButton prevButton = new JButton(prevAction);
        adjustButton(prevButton);
        nextAction = new NextAction();
        JButton nextButton = new JButton(nextAction);
        adjustButton(nextButton);
        closeAction = new CloseAction();
        JButton closeButton = new JButton(closeAction);
        adjustButton(closeButton);

        errorLabel = new JLabel();

        add(Box.createRigidArea(new Dimension(5, 0)));
        add(findLabel);
        add(findText);
        add(prevButton);
        add(nextButton);
        add(Box.createRigidArea(new Dimension(5, 0)));
        add(errorLabel);
        add(Box.createHorizontalGlue());
        add(closeButton);
    }

    /**
     * Set the FindState for this panel.
     * @param state the FindState.
     */
    public void setState(FindState state) {
        this.state = state;

        // Adjust the view to reflect the model
        updating = true;
        try {
            findText.setText(state.getPattern());
            error(state.getStatus(), false);
        } finally {
            updating = false;
        }
    }

    /**
     * Get the FindState for this panel.
     * @return the FindState.
     */
    public FindState getState() {
        return state;
    }

    private void error(FindState.Status status, boolean prevNext) {
        switch (status) {
            case OK:
                errorLabel.setText("");
                findText.setForeground(originalColor);
                break;
            case NOTFOUND:
                errorLabel.setText("Not found");
                findText.setForeground(errorColor);
                break;
            case WILLWRAP:
                errorLabel.setText("One more to wrap");
                findText.setForeground(originalColor);
                break;
            case EMPTYPATTERN:
                if (prevNext)
                    errorLabel.setText("Empty pattern");
                else
                    errorLabel.setText("");
                findText.setForeground(originalColor);
                break;
        }
    }

    private void close() {
        owner.close(this);
    }

    private void next() {
        if (state != null) {
            state.next();
            error(state.getStatus(), true);
        }
    }

    private void prev() {
        if (state != null) {
            state.prev();
            error(state.getStatus(), true);
        }
    }

    /*
     * We're a panel so do our own toolbar-style fly-over hiliting of buttons.
     * Why not be a toolbar?
     * Because of it's graded background which we don't want.
     */
    private void adjustButton(final JButton button) {
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);

        button.setMargin(BUTTON_INSETS);
        button.setFocusable(false);
        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setContentAreaFilled(true);
                    button.setBorderPainted(true);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setContentAreaFilled(false);
                button.setBorderPainted(false);
            }
        });
    }
}
