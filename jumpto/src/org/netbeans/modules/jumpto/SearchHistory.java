/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jumpto;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import org.openide.util.NbPreferences;

/**
 * Class for handling history of searches for e.g. Goto Type, Goto File, Goto Symbol.
 * <p>
 * Use <code>CTRL + UP</code> and <code>CTRL + DOWN</code> to navigate within history strings,
 * limit is set to {@value #HISTORY_LIMIT}. Empty and duplicated strings are not saved.
 * <p>
 * Strings are saved in preferences (in userdir), the delimiter is ";" and the first item is the last searched.
 * <p>
 * This class is not thread safe.
 * @author Tomas Mysik
 */
public final class SearchHistory {
    private static enum MoveOffset {
        CURRENT, NEXT, PREVIOUS
    }
    private static final Logger LOGGER = Logger.getLogger(SearchHistory.class.getName());

    private static final String SEARCH_HISTORY = "SearchHistory"; // NOI18N

    private static final int HISTORY_LIMIT = 10;
    private static final String HISTORY_BACK = "historyBack"; // NOI18N
    private static final String HISTORY_FORWARD = "historyForward"; // NOI18N
    private static final String DELIMITER = ";"; // NOI18N

    private final String historyKey;
    private final JTextField textField;
    private final List<String> data;

    private int offset = -1;

    /**
     * Create search history for the given class and text field.
     * @param clazz a class, its name is used for history persistence.
     * @param textField a text field which is used for showing/storing history.
     */
    public SearchHistory(Class clazz, JTextField textField) {
        this(clazz.getName(), textField);
    }

    /**
     * Create search history for the given key and text field.
     * @param historyKey a key which is used for history persistence.
     * @param textField a text field which is used for showing/storing history.
     */
    public SearchHistory(String historyKey, JTextField textField) {
        assert historyKey != null && historyKey.trim().length() > 0;
        assert textField != null;

        this.historyKey = historyKey;
        this.textField = textField;
        data = readHistory();

        registerListeners();
    }

    /**
     * Add the actual text of the text field to the history list and save it.
     * @see #saveHistory(java.lang.String)
     */
    public void saveHistory() {
        saveHistory(textField.getText());
    }

    /**
     * Add the text to the history list and save it.
     * <p>
     * Text is ignored if it's empty or already in the history list.
     * @param text text to save.
     */
    public void saveHistory(String text) {
        addHistoryItem(text);
        while (data.size() > HISTORY_LIMIT) {
            String removed = data.remove(getLastIndex());
            LOGGER.fine("History item removed: " + removed);
        }
        storeHistory();
    }

    private void addHistoryItem(String text) {
        addHistoryItem(text, MoveOffset.CURRENT);
    }

    private void addHistoryItem(String text, MoveOffset moveOffset) {
        if (text == null || text.trim().length() == 0) {
            LOGGER.fine("String to store is empty => ignoring.");
            return;
        }
        if (text.contains(DELIMITER)){
            LOGGER.fine("String to store contain delimeter => ignoring.");
            return;
        }
        if (data.remove(text)) {
            LOGGER.fine(String.format("Text %s already in history, removing and readding.", text));
        }
        // save item & move the offset
        data.add(getFirstIndex(), text);
        switch (moveOffset) {
            case NEXT:
                offset = getFirstIndex() + 1;
                break;
            case PREVIOUS:
                offset = getLastIndex();
                break;
            default:
                // noop
                break;
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("History item added: " + text);
            LOGGER.fine("History: " + data);
            LOGGER.fine(String.format("Offset %d, moved %s", offset, moveOffset));
        }
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(SearchHistory.class).node(SEARCH_HISTORY);
    }

    private void registerListeners() {
        textField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_MASK, true), HISTORY_BACK);
        textField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_MASK, true), HISTORY_FORWARD);
        textField.getActionMap().put(HISTORY_BACK, new AbstractAction() {
            private static final long serialVersionUID = -29128233042020099L;

            @Override
            public void actionPerformed(ActionEvent e) {
                navigateBack();
            }
        });
        textField.getActionMap().put(HISTORY_FORWARD, new AbstractAction() {
            private static final long serialVersionUID = 2341042130613885L;

            @Override
            public void actionPerformed(ActionEvent e) {
                navigateForward();
            }
        });
    }

    void navigateBack() {
        LOGGER.fine("History back called, offset is " + offset);
        if (data.size() == 0) {
            LOGGER.fine("No data in history.");
            return;
        }
        String cachedText = getCachedText();
        offset++;
        if (offset > getLastIndex()) {
            offset = getFirstIndex();
        }
        navigate(cachedText, MoveOffset.NEXT);
        LOGGER.fine(String.format("History: %s, offset: %d", data, offset));
    }

    void navigateForward() {
        LOGGER.fine("History forward called, offset is " + offset);
        if (data.size() == 0) {
            LOGGER.fine("No data in history.");
            return;
        }
        String cachedText = getCachedText();
        offset--;
        if (offset < getFirstIndex()) {
            offset = getLastIndex();
        }
        navigate(cachedText, MoveOffset.PREVIOUS);
        LOGGER.fine(String.format("History: %s, offset: %d", data, offset));
    }

    private void navigate(String oldText, MoveOffset moveOffset) {
        assert data.size() > 0;
        // did user change the text?
        String newText = null;
        String userText = textField.getText();
        if (oldText != null && oldText.equals(userText)) {
            LOGGER.fine("Text not changed => not saving, just iterating.");
            newText = getCachedText();
            LOGGER.fine("New text is: " + newText);
        } else {
            LOGGER.fine("Text changed => remember the current one & set the last (or first) one to the text field.");
            int index = moveOffset.equals(MoveOffset.PREVIOUS) ? getLastIndex() : getFirstIndex();
            newText = getCachedText(index);
            LOGGER.fine("New text is: " + newText);
            addHistoryItem(userText, moveOffset);
        }
        assert newText != null && newText.trim().length() > 0;
        textField.setText(newText);
    }

    private List<String> readHistory() {
        String history = getPreferences().get(historyKey, null);
        if (history == null || history.trim().length() == 0) {
            LOGGER.fine("No history found");
            return new ArrayList<String>(2 * HISTORY_LIMIT);
        }
        List<String> deserialized = new ArrayList<String>(Arrays.asList(history.split(DELIMITER)));
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("History found: " + deserialized);
        }
        assert deserialized.size() <= HISTORY_LIMIT : String.format("Too many items found %d > %d", deserialized.size(), HISTORY_LIMIT);
        return deserialized;
    }

    private void storeHistory() {
        assert data.size() <= HISTORY_LIMIT : String.format("Too many items found %d > %d", data.size(), HISTORY_LIMIT);
        StringBuilder serialized = new StringBuilder(200);
        boolean prepend = false;
        for (String item : data) {
            if (prepend) {
                serialized.append(DELIMITER);
            }
            serialized.append(item);
            prepend = true;
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("History to save: " + serialized.toString());
        }
        getPreferences().put(historyKey, serialized.toString());
    }

    private int getFirstIndex() {
        return 0;
    }

    private int getLastIndex() {
        return data.size() - 1;
    }

    private String getCachedText() {
        return getCachedText(offset);
    }

    private String getCachedText(int offset) {
        if (offset >= getFirstIndex() && offset <= getLastIndex()) {
            return data.get(offset);
        }
        return null;
    }
}
