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

package org.netbeans.editor;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.Timer;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Caret;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.EventListenerList;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Position;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.netbeans.api.editor.fold.FoldStateChange;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.lib.editor.util.swing.DocumentListenerPriority;
import org.openide.util.WeakListeners;

/**
* Caret implementation
*
* @author Miloslav Metelka
* @version 1.00
*/

public class BaseCaret implements Caret,
MouseListener, MouseMotionListener, PropertyChangeListener,
DocumentListener, ActionListener, SettingsChangeListener,
AtomicLockListener, FoldHierarchyListener {

    /** Caret type representing block covering current character */
    public static final String BLOCK_CARET = "block-caret"; // NOI18N

    /** Default caret type */
    public static final String LINE_CARET = "line-caret"; // NOI18N

    /** One dot thin line compatible with Swing default caret */
    public static final String THIN_LINE_CARET = "thin-line-caret"; // NOI18N

    private static final boolean debugCaretFocus
    = Boolean.getBoolean("netbeans.debug.editor.caret.focus"); // NOI18N

    private static final boolean debugCaretFocusExtra
    = Boolean.getBoolean("netbeans.debug.editor.caret.focus.extra"); // NOI18N
    
    /**
     * Implementation of various listeners.
     */
    private ListenerImpl listenerImpl;
    
    /**
     * Present bounds of the caret. This rectangle needs to be repainted
     * prior the caret gets repainted elsewhere.
     */
    private Rectangle caretBounds;
    
    /** Component this caret is bound to */
    protected JTextComponent component;

    /** Position of the caret on the screen. This helps to compute
    * caret position on the next after jump.
    */
    Point magicCaretPosition;

    /** Draw mark designating the position of the caret.  */
    MarkFactory.ContextMark caretMark = new MarkFactory.ContextMark(Position.Bias.Forward, false);

    /** Draw mark that supports caret mark in creating selection */
    MarkFactory.ContextMark selectionMark = new MarkFactory.ContextMark(Position.Bias.Forward, false);

    /** Is the caret visible */
    boolean caretVisible;

    /** Whether blinking caret is currently visible.
     * <code>caretVisible</code> must be also true in order to paint the caret.
     */
    boolean blinkVisible;

    /** Is the selection currently visible? */
    boolean selectionVisible;

    /** Listeners */
    protected EventListenerList listenerList = new EventListenerList();

    /** Timer used for blinking the caret */
    protected Timer flasher;

    /** Type of the caret */
    String type;

    /** Is the caret italic for italic fonts */
    boolean italic;

    private int xPoints[] = new int[4];
    private int yPoints[] = new int[4];
    private Action selectWordAction;
    private Action selectLineAction;

    /** Change event. Only one instance needed because it has only source property */
    protected ChangeEvent changeEvent;

    /** Dot array of one character under caret */
    protected char dotChar[] = {' '};

    private boolean overwriteMode;

    /** Remembering document on which caret listens avoids
    * duplicate listener addition to SwingPropertyChangeSupport
    * due to the bug 4200280
    */
    private BaseDocument listenDoc;

    /** Font of the text underlying the caret. It can be used
    * in caret painting.
    */
    protected Font afterCaretFont;

    /** Font of the text right before the caret */
    protected Font beforeCaretFont;

    /** Foreground color of the text underlying the caret. It can be used
    * in caret painting.
    */
    protected Color textForeColor;

    /** Background color of the text underlying the caret. It can be used
    * in caret painting.
    */
    protected Color textBackColor;

    private transient FocusListener focusListener;

    /** Whether the text is being modified under atomic lock.
     * If so just one caret change is fired at the end of all modifications.
     */
    private transient boolean inAtomicLock = false;
    private transient boolean inAtomicUnlock = false;
    
    /** Helps to check whether there was modification performed
     * and so the caret change needs to be fired.
     */
    private transient boolean modified;
    
    /** Whether there was an undo done in the modification and the offset of the modification */
    private transient int undoOffset = -1;
    
    static final long serialVersionUID =-9113841520331402768L;

    private MouseEvent dndArmedEvent = null;
    
    /**
     * Set to true once the folds have changed. The caret should retain
     * its relative visual position on the screen.
     */
    private boolean updateAfterFoldHierarchyChange;
    
    public BaseCaret() {
        listenerImpl = new ListenerImpl();
        Settings.addSettingsChangeListener(this);
    }

    /** Called when settings were changed. The method is called
    * also in constructor, so the code must count with the evt being null.
    */
    public void settingsChange(SettingsChangeEvent evt) {
        if( evt != null && SettingsNames.CARET_BLINK_RATE.equals( evt.getSettingName() ) ) {
            
            JTextComponent c = component;
            if (c == null) return;
            if (evt.getKitClass() != Utilities.getKitClass(c)) return;
            
            Object value = evt.getNewValue();
            if( value instanceof Integer ) {
                setBlinkRate( ((Integer)value).intValue() );
            }
        }
        updateType();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateCaretBounds(); // the line height etc. may have change
            }
        });
    }

    void updateType() {
        JTextComponent c = component;
        if (c != null) {
            Class kitClass = Utilities.getKitClass(c);
            if (kitClass==null) return;
            String newType;
            boolean newItalic;
            Color caretColor;
            if (overwriteMode) {
                newType = SettingsUtil.getString(kitClass,
                                                 SettingsNames.CARET_TYPE_OVERWRITE_MODE, LINE_CARET);
                newItalic = SettingsUtil.getBoolean(kitClass,
                                                    SettingsNames.CARET_ITALIC_OVERWRITE_MODE, false);

                Color insertModeColor = getColor( kitClass, SettingsNames.CARET_COLOR_INSERT_MODE,
                    SettingsDefaults.defaultCaretColorInsertMode );
                
                caretColor = getColor( kitClass, SettingsNames.CARET_COLOR_OVERWRITE_MODE,
                    insertModeColor );
                
            } else { // insert mode
                newType = SettingsUtil.getString(kitClass,
                                                 SettingsNames.CARET_TYPE_INSERT_MODE, LINE_CARET);
                newItalic = SettingsUtil.getBoolean(kitClass,
                                                    SettingsNames.CARET_ITALIC_INSERT_MODE, false);
                caretColor = getColor( kitClass, SettingsNames.CARET_COLOR_INSERT_MODE,
                    SettingsDefaults.defaultCaretColorInsertMode );
            }

            this.type = newType;
            this.italic = newItalic;
            c.setCaretColor(caretColor);
            if (debugCaretFocusExtra){
                System.err.println("Updating caret color:"+caretColor); // NOI18N
            }

            resetBlink();
            dispatchUpdate(false);
        }
    }

    private static Color getColor( Class kitClass, String settingName, Color defaultValue) {
        Object value = Settings.getValue(kitClass, settingName);
        return (value instanceof Color) ? (Color)value : defaultValue;
    }

    /**
     * Assign new caret bounds into <code>caretBounds</code> variable.
     *
     * @return true if the new caret bounds were successfully computed
     *  and assigned or false otherwise.
     */
    private boolean updateCaretBounds() {
        JTextComponent c = component;
        if (c != null) {
            int offset = getDot();
            Rectangle newCaretBounds;
            try {
                newCaretBounds = c.getUI().modelToView(
                        c, offset, Position.Bias.Forward);
                BaseDocument doc = Utilities.getDocument(c);
                if (doc != null) {
                    doc.getChars(offset, this.dotChar, 0, 1);
                }
            } catch (BadLocationException e) {
                newCaretBounds = null;
                Utilities.annotateLoggable(e);
            }
        
            if (newCaretBounds != null) {
                caretBounds = newCaretBounds;
                return true;
            }
        }
        return false;
    }

    /** Called when UI is being installed into JTextComponent */
    public void install(JTextComponent c) {
        assert (SwingUtilities.isEventDispatchThread()); // must be done in AWT
        component = c;
        blinkVisible = true;
        
        // Assign dot and mark positions
        BaseDocument doc = Utilities.getDocument(c);
        if (doc != null) {
            modelChanged(null, doc);
        }

        // Attempt to assign initial bounds - usually here the component
        // is not yet added to the component hierarchy.
        updateCaretBounds();
        
        if (caretBounds == null) {
            // For null bounds wait for the component to get resized
            // and attempt to recompute bounds then
            component.addComponentListener(listenerImpl);
        }

        component.addPropertyChangeListener(this);
        component.addFocusListener(listenerImpl);
        component.addMouseListener(this);
        component.addMouseMotionListener(this);

        EditorUI editorUI = Utilities.getEditorUI(component);
        editorUI.addPropertyChangeListener( this );
        
        FoldHierarchy hierarchy = FoldHierarchy.get(c);
        if (hierarchy != null) {
            hierarchy.addFoldHierarchyListener(this);
        }
        
        if (component.hasFocus()) {
            if (debugCaretFocus || debugCaretFocusExtra) {
                System.err.println("Component has focus, calling BaseCaret.focusGained(); doc=" // NOI18N
                    + component.getDocument().getProperty(Document.TitleProperty));
            }
            listenerImpl.focusGained(null); // emulate focus gained
        }

        dispatchUpdate(false);
    }

    /** Called when UI is being removed from JTextComponent */
    public void deinstall(JTextComponent c) {
        component = null; // invalidate

        // No idea why the sync is done the way how it is, but the locks must
        // always be acquired in the same order otherwise the code will deadlock
        // sooner or later. See #100734
        synchronized (this) {
            synchronized (listenerImpl) {
                if (flasher != null) {
                    setBlinkRate(0);
                }
            }
        }
        
        c.removeMouseMotionListener(this);
        c.removeMouseListener(this);
        c.removeFocusListener(listenerImpl);
        c.removePropertyChangeListener(this);
        
        FoldHierarchy hierarchy = FoldHierarchy.get(c);
        if (hierarchy != null) {
            hierarchy.removeFoldHierarchyListener(this);
        }
        
        modelChanged(listenDoc, null);
    }

    protected void modelChanged(BaseDocument oldDoc, BaseDocument newDoc) {
        if (oldDoc != null) {
            // ideally the oldDoc param shouldn't exist and only listenDoc should be used
            assert (oldDoc == listenDoc);

            org.netbeans.lib.editor.util.swing.DocumentUtilities.removeDocumentListener(
                    oldDoc, this, DocumentListenerPriority.CARET_UPDATE);
            oldDoc.removeAtomicLockListener(this);

            try {
                caretMark.remove();
                selectionMark.remove();
            } catch (InvalidMarkException e) {
                Utilities.annotateLoggable(e);
            }

            listenDoc = null;
        }


        if (newDoc != null) {

            org.netbeans.lib.editor.util.swing.DocumentUtilities.addDocumentListener(
                    newDoc, this, DocumentListenerPriority.CARET_UPDATE);
            listenDoc = newDoc;
            newDoc.addAtomicLockListener(this);

            try {
                Utilities.insertMark(newDoc, caretMark, 0);
                Utilities.insertMark(newDoc, selectionMark, 0);
            } catch (InvalidMarkException e) {
                Utilities.annotateLoggable(e);
            } catch (BadLocationException e) {
                Utilities.annotateLoggable(e);
            }

            settingsChange(null); // update settings

            Utilities.runInEventDispatchThread(
                new Runnable() {
                    public void run() {
                        updateType();
                    }
                }
            );

        }
    }

    /** Renders the caret */
    public void paint(Graphics g) {
        JTextComponent c = component;
        if (c == null) return;
        EditorUI editorUI = Utilities.getEditorUI(c);

        // #70915 Check whether the caret was moved but the component was not
        // validated yet and therefore the caret bounds are still null
        // and if so compute the bounds and scroll the view if necessary.
        if (getDot() != 0 && caretBounds == null) {
            update(true);
        }
        if (caretBounds != null && isVisible() && blinkVisible) {
            paintCustomCaret(g);
        }
    }

    protected void paintCustomCaret(Graphics g) {
        JTextComponent c = component;
        if (c != null) {
            EditorUI editorUI = Utilities.getEditorUI(c);
            g.setColor(c.getCaretColor());
            if (THIN_LINE_CARET.equals(type)) { // thin line caret
                int upperX = caretBounds.x;
                if (beforeCaretFont != null && beforeCaretFont.isItalic() && italic) {
                    upperX += Math.tan(beforeCaretFont.getItalicAngle()) * caretBounds.height;
                }
                g.drawLine((int)upperX, caretBounds.y, caretBounds.x,
                        (caretBounds.y + caretBounds.height - 1));

            } else if (BLOCK_CARET.equals(type)) { // block caret
                if (afterCaretFont != null) g.setFont(afterCaretFont);
                if (afterCaretFont != null && afterCaretFont.isItalic() && italic) { // paint italic caret
                    int upperX = (int)(caretBounds.x
                            + Math.tan(afterCaretFont.getItalicAngle()) * caretBounds.height);
                    xPoints[0] = upperX;
                    yPoints[0] = caretBounds.y;
                    xPoints[1] = upperX + caretBounds.width;
                    yPoints[1] = caretBounds.y;
                    xPoints[2] = caretBounds.x + caretBounds.width;
                    yPoints[2] = caretBounds.y + caretBounds.height - 1;
                    xPoints[3] = caretBounds.x;
                    yPoints[3] = caretBounds.y + caretBounds.height - 1;
                    g.fillPolygon(xPoints, yPoints, 4);

                } else { // paint non-italic caret
                    g.fillRect(caretBounds.x, caretBounds.y, caretBounds.width, caretBounds.height);
                }
                
                if (!Character.isWhitespace(dotChar[0])) {
                    Color textBackgroundColor = c.getBackground();
                    if (textBackgroundColor != null)
                        g.setColor(textBackgroundColor);
                    // int ascent = FontMetricsCache.getFontMetrics(afterCaretFont, c).getAscent();
                    g.drawChars(dotChar, 0, 1, caretBounds.x,
                            caretBounds.y + editorUI.getLineAscent());
                }

            } else { // two dot line caret
                int blkWidth = 2;
                if (beforeCaretFont != null && beforeCaretFont.isItalic() && italic) {
                    int upperX = (int)(caretBounds.x 
                            + Math.tan(beforeCaretFont.getItalicAngle()) * caretBounds.height);
                    xPoints[0] = upperX;
                    yPoints[0] = caretBounds.y;
                    xPoints[1] = upperX + blkWidth;
                    yPoints[1] = caretBounds.y;
                    xPoints[2] = caretBounds.x + blkWidth;
                    yPoints[2] = caretBounds.y + caretBounds.height - 1;
                    xPoints[3] = caretBounds.x;
                    yPoints[3] = caretBounds.y + caretBounds.height - 1;
                    g.fillPolygon(xPoints, yPoints, 4);

                } else { // paint non-italic caret
                    g.fillRect(caretBounds.x, caretBounds.y, blkWidth, caretBounds.height - 1);
                }
            }
        }
    }

    /** Update the caret's visual position */
    void dispatchUpdate(final boolean scrollViewToCaret) {
        /* After using SwingUtilities.invokeLater() due to fix of #18860
         * there is another fix of #35034 which ensures that the caret's
         * document listener will be added AFTER the views hierarchy's
         * document listener so the code can run synchronously again
         * which should eliminate the problem with caret lag.
         * However the document can be modified from non-AWT thread
         * which is the case in #57316 and in that case the code
         * must run asynchronously in AWT thread.
         */
        Utilities.runInEventDispatchThread(
            new Runnable() {
                public void run() {
                    JTextComponent c = component;
                    if (c != null) {
                        BaseDocument doc = Utilities.getDocument(c);
                        if (doc != null) {
                            doc.readLock();
                            try {
                                update(scrollViewToCaret);
                            } finally {
                                doc.readUnlock();
                            }
                        }
                    }
                }
            }
        );
    }

    /**
     * Update the caret's visual position.
     * <br/>
     * The document is read-locked while calling this method.
     *
     * @param scrollViewToCaret whether the view of the text component should be
     *  scrolled to the position of the caret.
     */
    protected void update(boolean scrollViewToCaret) {
        JTextComponent c = component;
        if (c != null) {
            BaseTextUI ui = (BaseTextUI)c.getUI();
            EditorUI editorUI = ui.getEditorUI();
            BaseDocument doc = Utilities.getDocument(c);
            if (doc != null) {
                Rectangle oldCaretBounds = caretBounds; // no need to deep copy
                if (oldCaretBounds != null) {
                    if (italic) { // caret is italic - add char height to the width of the rect
                        oldCaretBounds.width += oldCaretBounds.height;
                    }
                    c.repaint(oldCaretBounds);
                }

                int dot = getDot();

                if (updateCaretBounds() && (scrollViewToCaret || updateAfterFoldHierarchyChange)) {
                    Rectangle scrollBounds = new Rectangle(caretBounds);
                    
                    // Optimization to avoid extra repaint:
                    // If the caret bounds were not yet assigned then attempt
                    // to scroll the window so that there is an extra vertical space 
                    // for the possible horizontal scrollbar that may appear
                    // if the line-view creation process finds line-view that
                    // is too wide and so the horizontal scrollbar will appear
                    // consuming an extra vertical space at the bottom.
                    if (oldCaretBounds == null) {
                        Component viewport = c.getParent();
                        if (viewport instanceof JViewport) {
                            Component scrollPane = viewport.getParent();
                            if (scrollPane instanceof JScrollPane) {
                                JScrollBar hScrollBar = ((JScrollPane)scrollPane).getHorizontalScrollBar();
                                if (hScrollBar != null) {
                                    int hScrollBarHeight = hScrollBar.getPreferredSize().height;
                                    Dimension extentSize = ((JViewport)viewport).getExtentSize();
                                    // If the extent size is high enough then extend
                                    // the scroll region by extra vertical space
                                    if (extentSize.height >= caretBounds.height + hScrollBarHeight) {
                                        scrollBounds.height += hScrollBarHeight;
                                    }
                                }
                            }
                        }
                    }
                    
                    Rectangle visibleBounds = c.getVisibleRect();
                    
                    // If folds have changed attempt to scrolll the view so that 
                    // relative caret's visual position gets retained
                    // (the absolute position will change because of collapsed/expanded folds).
                    if (oldCaretBounds != null && updateAfterFoldHierarchyChange) {
                        int oldRelY = oldCaretBounds.y - visibleBounds.y;
                        // Only fix if the caret is within visible bounds
                        if (oldRelY < visibleBounds.height) {
                            scrollBounds.y = Math.max(caretBounds.y - oldRelY, 0);
                            scrollBounds.height = visibleBounds.height;
                        }
                    }

                    // Historically the caret is expected to appear
                    // in the middle of the window if setDot() gets called
                    // e.g. by double-clicking in Navigator.
                    // If the caret bounds are more than a caret height below the present
                    // visible view bounds (or above the view bounds)
                    // then scroll the window so that the caret is in the middle
                    // of the visible window to see the context around the caret.
                    // This should work fine with PgUp/Down because these
                    // scroll the view explicitly.
                    if (!updateAfterFoldHierarchyChange &&
                        (caretBounds.y > visibleBounds.y + visibleBounds.height + caretBounds.height
                            || caretBounds.y + caretBounds.height < visibleBounds.y - caretBounds.height)
                    ) {
                        // Scroll into the middle
                        scrollBounds.y -= (visibleBounds.height - caretBounds.height) / 2;
                        scrollBounds.height = visibleBounds.height;
                    }

                    updateAfterFoldHierarchyChange = false;
                    
                    // Ensure that the viewport will be scrolled so that
                    // the caret is visible
                    if (scrollViewToCaret) {
                        c.scrollRectToVisible(scrollBounds);
                    }
                        
                    resetBlink();
                    c.repaint(caretBounds);
                }
            }
        }
    }
    
    private void updateSystemSelection() {
        if (getDot() != getMark() && component != null) {
            Clipboard clip = getSystemSelection();
            
            if (clip != null) {
                clip.setContents(new java.awt.datatransfer.StringSelection(component.getSelectedText()), null);
            }
        }
    }

    private Clipboard getSystemSelection() {
        return component.getToolkit().getSystemSelection();
    }
    
    /**
     * Redefine to Object.equals() to prevent defaulting to Rectangle.equals()
     * which would cause incorrect firing
     */
    public @Override boolean equals(Object o) {
        return (this == o);
    }

    public @Override int hashCode() {
        return System.identityHashCode(this);
    }
    
    /** Adds listener to track when caret position was changed */
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    /** Removes listeners to caret position changes */
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    /** Notifies listeners that caret position has changed */
    protected void fireStateChanged() {
        Runnable runnable = new Runnable() {
            public void run() {
                Object listeners[] = listenerList.getListenerList();
                for (int i = listeners.length - 2; i >= 0 ; i -= 2) {
                    if (listeners[i] == ChangeListener.class) {
                        if (changeEvent == null) {
                            changeEvent = new ChangeEvent(BaseCaret.this);
                        }
                        ((ChangeListener)listeners[i + 1]).stateChanged(changeEvent);
                    }
                }
            }
        };
        
        // Fix of #24336 - always do in AWT thread
        // Fix of #114649 - when under document's lock repost asynchronously
        if (inAtomicUnlock) {
            SwingUtilities.invokeLater(runnable);
        } else {
            Utilities.runInEventDispatchThread(runnable);
        }
        updateSystemSelection();
    }

    /**
     * Whether the caret currently visible.
     * <br>
     * Although the caret is visible it may be in a state when it's
     * not physically showing on screen in case when it's blinking.
     */
    public final boolean isVisible() {
        return caretVisible;
    }

    protected void setVisibleImpl(boolean v) {
        boolean visible = isVisible();
        synchronized (this) {
            synchronized (listenerImpl) {
                if (flasher != null) {
                    if (visible) {
                        flasher.stop();
                    }
                    if (v) {
                        if (debugCaretFocusExtra){
                            System.err.println("starting the caret blinking timer: visible=" // NOI18N
                                    + visible + ", blinkVisible=" + blinkVisible); // NOI18N
                        }
                        flasher.start();

                    } else {
                        if (debugCaretFocusExtra){
                            System.err.println("stopping the caret blinking timer"); // NOI18N
                        }
                        flasher.stop();
                    }
                }
            }

            caretVisible = v;
        }
        JTextComponent c = component;
        if (c != null && caretBounds != null) {
            Rectangle repaintRect = caretBounds;
            if (italic) {
                repaintRect = new Rectangle(repaintRect); // copy
                repaintRect.width += repaintRect.height; // ensure enough horizontally
            }
            c.repaint(repaintRect);
        }
    }

    synchronized void resetBlink() {
        boolean visible = isVisible();
        synchronized (listenerImpl) {
            if (flasher != null) {
                flasher.stop();
                blinkVisible = true;
                if (visible) {
                    if (debugCaretFocusExtra){
                        System.err.println("Reset blinking (caret already visible)" // NOI18N
                                + " - starting the caret blinking timer: visible=" // NOI18N
                                + visible + ", blinkVisible=" + blinkVisible // NOI18N
                        );
                    }
                    flasher.start();
                } else {
                    if (debugCaretFocusExtra){
                        System.err.println("Reset blinking (caret not visible)" // NOI18N
                                + " - caret blinking timer not started: visible=" // NOI18N
                                + visible + ", blinkVisible=" + blinkVisible // NOI18N
                        );
                    }
                }
            }
        }
    }

    /** Sets the caret visibility */
    public void setVisible(final boolean v) {
        Utilities.runInEventDispatchThread(
            new Runnable() {
                public void run() {
                    setVisibleImpl(v);
                }
            }
        );
    }

    /** Is the selection visible? */
    public final boolean isSelectionVisible() {
        return selectionVisible;
    }

    /** Sets the selection visibility */
    public void setSelectionVisible(boolean v) {
        if (selectionVisible == v) {
            return;
        }
        JTextComponent c = component;
        if (c != null) {
            selectionVisible = v;

            // repaint the block
            BaseTextUI ui = (BaseTextUI)c.getUI();
            try {
                ui.getEditorUI().repaintBlock(caretMark.getOffset(), selectionMark.getOffset());
            } catch (BadLocationException e) {
                Utilities.annotateLoggable(e);
            } catch (InvalidMarkException e) {
                Utilities.annotateLoggable(e);
            }

        }
    }

    /** Saves the current caret position.  This is used when
    * caret up or down actions occur, moving between lines
    * that have uneven end positions.
    *
    * @param p  the Point to use for the saved position
    */
    public void setMagicCaretPosition(Point p) {
        magicCaretPosition = p;
    }

    /** Get position used to mark begining of the selected block */
    public final Point getMagicCaretPosition() {
        return magicCaretPosition;
    }

    /** Sets the caret blink rate.
    * @param rate blink rate in milliseconds, 0 means no blink
    */
    public synchronized void setBlinkRate(int rate) {
        synchronized (listenerImpl) {
            if (flasher == null && rate > 0) {
                flasher = new Timer(rate, new WeakTimerListener(this));
            }
            if (flasher != null) {
                if (rate > 0) {
                    if (flasher.getDelay() != rate) {
                        if (debugCaretFocusExtra){
                            System.err.println("blink rate:"+rate); // NOI18N
                        }
                        flasher.setDelay(rate);
                    }
                } else { // zero rate - don't blink
                    if (debugCaretFocusExtra){
                        System.err.println("zero rate - don't blink"); // NOI18N
                        System.err.println("setting blinkVisible to true and disabling timer"); // NOI18N
                    }
                    flasher.stop();
                    flasher.removeActionListener(this);
                    flasher = null;
                    blinkVisible = true;
                }
            }
        }
    }

    /** Returns blink rate of the caret or 0 if caret doesn't blink */
    public synchronized int getBlinkRate() {
        synchronized (listenerImpl) {
            return (flasher != null) ? flasher.getDelay() : 0;
        }
    }

    /** Gets the current position of the caret */
    public int getDot() {
        if (component != null) {
            try {
                return caretMark.getOffset();
            } catch (InvalidMarkException e) {
            }
        }
        return 0;
    }

    /** Gets the current position of the selection mark.
    * If there's a selection this position will be different
    * from the caret position.
    */
    public int getMark() {
        if (component != null) {
            if (selectionVisible) {
                try {
                    return selectionMark.getOffset();
                } catch (InvalidMarkException e) {
                }
            } else { // selection not visible
                return getDot(); // must return same position as dot
            }
        }
        return 0;
    }

    /**
     * Assign the caret a new offset in the underlying document.
     * <br/>
     * This method implicitly sets the selection range to zero.
     */
    public void setDot(int offset) {
        // The first call to this method in NB is done when the component
        // is already connected to the component hierarchy but its size
        // is still (0,0,0,0) (although its preferred size is already non-empty).
        // This causes the TextUI.modelToView() to return null
        // because BasicTextUI.getVisibleEditorRect() returns null.
        // Thus caretBounds will be null in such case although
        // the offset in setDot() is already non-zero.
        // In such case the component listener listens for resizing
        // of the editor component and reassigns the caretBounds
        // once the component gets resized.
        setDot(offset, caretBounds, EditorUI.SCROLL_DEFAULT);
    }

    public void setDot(int offset, boolean expandFold) {
        setDot(offset, caretBounds, EditorUI.SCROLL_DEFAULT, expandFold);
    }
    
    
    /** Sets the caret position to some position. This
     * causes removal of the active selection. If expandFold set to true
     * fold containing offset position will be expanded.
     *
     * <p>
     * <b>Note:</b> This method is deprecated and the present implementation
     * ignores values of scrollRect and scrollPolicy parameters.
     *
     * @param offset offset in the document to which the caret should be positioned.
     * @param scrollRect rectangle to which the editor window should be scrolled.
     * @param scrollPolicy the way how scrolling should be done.
     *  One of <code>EditorUI.SCROLL_*</code> constants.
     * @param expandFold whether possible fold at the caret position should be expanded.
     *
     * @deprecated use #setDot(int, boolean) preceded by <code>JComponent.scrollRectToVisible()</code>.
     */
    
    public void setDot(int offset, Rectangle scrollRect, int scrollPolicy, boolean expandFold) {
        JTextComponent c = component;
        if (c != null) {
            setSelectionVisible(false);
            BaseDocument doc = (BaseDocument)c.getDocument();
            boolean dotChanged = false;
            doc.readLock();
            try {
                if (doc != null && offset >= 0 && offset <= doc.getLength()) {
                    dotChanged = true;
                    try {
                        Utilities.moveMark(doc, caretMark, offset);
                        // Unfold fold 
                        FoldHierarchy hierarchy = FoldHierarchy.get(c);
                        hierarchy.lock();
                        try {
                            Fold collapsed = null;
                            while (expandFold && (collapsed = FoldUtilities.findCollapsedFold(hierarchy, offset, offset)) != null && collapsed.getStartOffset() < offset &&
                                collapsed.getEndOffset() > offset) {
                                hierarchy.expand(collapsed);
                            }
                        } finally {
                            hierarchy.unlock();
                        }
                    } catch (BadLocationException e) {
                        throw new IllegalStateException(e.toString());
                        // setting the caret to wrong position leaves it at current position
                    } catch (InvalidMarkException e) {
                        throw new IllegalStateException(e.toString());
                        // Caret not installed or inside the initial-read
                    }
                }
            } finally {
                doc.readUnlock();
            }
            
            if (dotChanged) {
                fireStateChanged();
                dispatchUpdate(true);
            }
        }
    }
    
    /** Sets the caret position to some position. This
     * causes removal of the active selection.
     *
     * <p>
     * <b>Note:</b> This method is deprecated and the present implementation
     * ignores values of scrollRect and scrollPolicy parameters.
     *
     * @param offset offset in the document to which the caret should be positioned.
     * @param scrollRect rectangle to which the editor window should be scrolled.
     * @param scrollPolicy the way how scrolling should be done.
     *  One of <code>EditorUI.SCROLL_*</code> constants.
     *
     * @deprecated use #setDot(int) preceded by <code>JComponent.scrollRectToVisible()</code>.
     */
    public void setDot(int offset, Rectangle scrollRect, int scrollPolicy) {
        setDot(offset, scrollRect, scrollPolicy, true);
    }

    public void moveDot(int offset) {
        moveDot(offset, caretBounds, EditorUI.SCROLL_MOVE);
    }

    /** Makes selection by moving dot but leaving mark.
     * 
     * <p>
     * <b>Note:</b> This method is deprecated and the present implementation
     * ignores values of scrollRect and scrollPolicy parameters.
     *
     * @param offset offset in the document to which the caret should be positioned.
     * @param scrollRect rectangle to which the editor window should be scrolled.
     * @param scrollPolicy the way how scrolling should be done.
     *  One of <code>EditorUI.SCROLL_*</code> constants.
     *
     * @deprecated use #setDot(int) preceded by <code>JComponent.scrollRectToVisible()</code>.
     */
    public void moveDot(int offset, Rectangle scrollRect, int scrollPolicy) {
        JTextComponent c = component;
        if (c != null) {
            BaseDocument doc = (BaseDocument)c.getDocument();
            if (doc != null && offset >= 0 && offset <= doc.getLength()) {
                try {
                    int oldCaretPos = getDot();
                    if (offset == oldCaretPos) { // no change
                        return;
                    }
                    int selPos; // current position of selection mark

                    if (selectionVisible) {
                        selPos = selectionMark.getOffset();
                    } else {
                        Utilities.moveMark(doc, selectionMark, oldCaretPos);
                        selPos = oldCaretPos;
                    }

                    Utilities.moveMark(doc, caretMark, offset);
                    if (selectionVisible) { // selection already visible
                        Utilities.getEditorUI(c).repaintBlock(oldCaretPos, offset);
                        if (selPos == offset) { // same positions -> invisible selection
                            setSelectionVisible(false);
                        }

                    } else { // selection not yet visible
                        setSelectionVisible(true);
                    }
                } catch (BadLocationException e) {
                    throw new IllegalStateException(e.toString());
                    // position is incorrect
                } catch (InvalidMarkException e) {
                    throw new IllegalStateException(e.toString());
                }
            }
            fireStateChanged();
            dispatchUpdate(true);
        }
    }

    // DocumentListener methods
    public void insertUpdate(DocumentEvent evt) {
        JTextComponent c = component;
        if (c != null) {
            BaseDocument doc = (BaseDocument)component.getDocument();
            BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
            if ((bevt.isInUndo() || bevt.isInRedo())
                    && component == Utilities.getLastActiveComponent()
               ) {
                // in undo mode and current component
                undoOffset = evt.getOffset() + evt.getLength();
            } else {
                undoOffset = -1;
            }

            modified = true;

            modifiedUpdate();
        }
    }

    public void removeUpdate(DocumentEvent evt) {
        JTextComponent c = component;
        if (c != null) {
            BaseDocument doc = (BaseDocument)c.getDocument();
            // make selection invisible if removal shrinked block to zero size
            if (selectionVisible && (getDot() == getMark())) {
                setSelectionVisible(false);
            }
            
            BaseDocumentEvent bevt = (BaseDocumentEvent)evt;
            if ((bevt.isInUndo() || bevt.isInRedo())
                && c == Utilities.getLastActiveComponent()
            ) {
                // in undo mode and current component
                undoOffset = evt.getOffset();
            } else {
                undoOffset = -1;
            }

            modified = true;
            
            modifiedUpdate();
        }
    }
    
    private void modifiedUpdate() {
        if (!inAtomicLock) {
            JTextComponent c = component;
            if (modified && c != null) {
                if (undoOffset >= 0) { // last modification was undo => set the dot to undoOffset
                    setDot(undoOffset);
                } else { // last modification was not undo
                    fireStateChanged();
                    // Scroll to caret only for component with focus
                    dispatchUpdate(c.hasFocus());
                }
                modified = false;
            }
        }
    }
    
    public void atomicLock(AtomicLockEvent evt) {
        inAtomicLock = true;
    }
    
    public void atomicUnlock(AtomicLockEvent evt) {
        inAtomicLock = false;
        inAtomicUnlock = true;
        try {
            modifiedUpdate();
        } finally {
            inAtomicUnlock = false;
        }
    }
    
    public void changedUpdate(DocumentEvent evt) {
        // XXX: used as a backdoor from HighlightingDrawLayer
        if (evt == null) {
            dispatchUpdate(false);
        }
    }

    // MouseListener methods
    public void mouseClicked(MouseEvent evt) {
        JTextComponent c = component;
        if (c != null) {
            if (SwingUtilities.isLeftMouseButton(evt)) {
                if (evt.getClickCount() == 2) {
                    BaseTextUI ui = (BaseTextUI)c.getUI();
                    // Expand fold if offset is in collapsed fold
                    int offset = ui.viewToModel(c,
                                    evt.getX(), evt.getY());
                    FoldHierarchy hierarchy = FoldHierarchy.get(c);
                    Document doc = c.getDocument();
                    if (doc instanceof AbstractDocument) {
                        AbstractDocument adoc = (AbstractDocument)doc;
                        adoc.readLock();
                        try {
                            hierarchy.lock();
                            try {
                                Fold collapsed = FoldUtilities.findCollapsedFold(
                                    hierarchy, offset, offset);
                                if (collapsed != null && collapsed.getStartOffset() <= offset &&
                                    collapsed.getEndOffset() >= offset) {
                                    hierarchy.expand(collapsed);
                                } else {
                                    if (selectWordAction == null) {
                                        selectWordAction = ((BaseKit)ui.getEditorKit(
                                                                c)).getActionByName(BaseKit.selectWordAction);
                                    }
                                    selectWordAction.actionPerformed(null);
                                }
                            } finally {
                                hierarchy.unlock();
                            }
                        } finally {
                            adoc.readUnlock();
                        }
                    }
                } else if (evt.getClickCount() == 3) {
                    if (selectLineAction == null) {
                        BaseTextUI ui = (BaseTextUI)c.getUI();
                        selectLineAction = ((BaseKit)ui.getEditorKit(
                                                c)).getActionByName(BaseKit.selectLineAction);
                    }
                    selectLineAction.actionPerformed(null);
                }
            } else if (SwingUtilities.isMiddleMouseButton(evt)){
		if (evt.getClickCount() == 1) {
		    if (c == null) return;
                    Toolkit tk = c.getToolkit();
                    Clipboard buffer = getSystemSelection();
                    
                    if (buffer == null) return;

                    Transferable trans = buffer.getContents(null);
                    if (trans == null) return;

                    BaseDocument doc = (BaseDocument)c.getDocument();
                    if (doc == null) return;
                    
                    int offset = ((BaseTextUI)c.getUI()).viewToModel(c,
                                    evt.getX(), evt.getY());

                    try{
                        String pastingString = (String)trans.getTransferData(DataFlavor.stringFlavor);
                        if (pastingString == null) return;
                         try {
                             doc.atomicLock();
                             try {
                                 doc.insertString(offset, pastingString, null);
                                 setDot(offset+pastingString.length());
                             } finally {
                                 doc.atomicUnlock();
                             }
                         } catch( BadLocationException exc ) {
                         }
                    }catch(UnsupportedFlavorException ufe){
                    }catch(IOException ioe){
                    }
		}
            }
        }
    }

    private void mousePressedImpl(MouseEvent evt){
        JTextComponent c = component;
        if (c != null) {
            Utilities.getEditorUI(c).getWordMatch().clear(); // [PENDING] should be done cleanly

            // Position the cursor at the appropriate place in the document
            if ((SwingUtilities.isLeftMouseButton(evt) && 
                !(evt.isPopupTrigger()) &&
                 (evt.getModifiers() & (InputEvent.META_MASK|InputEvent.ALT_MASK)) == 0) ||
               !isSelectionVisible()) {
                int offset = ((BaseTextUI)c.getUI()).viewToModel(c,
                          evt.getX(), evt.getY());
                if (offset >= 0) {
                    if ((evt.getModifiers() & InputEvent.SHIFT_MASK) != 0) {
                        moveDot(offset);
                    } else {
                        setDot(offset);
                    }
                    setMagicCaretPosition(null);
                }
                if (c.isEnabled()) {
                    c.requestFocus();
                }
            }
        }
    }
    
    public void mousePressed(MouseEvent evt) {
        dndArmedEvent = null;

	if (isDragPossible(evt) && mapDragOperationFromModifiers(evt) != TransferHandler.NONE) {
            dndArmedEvent = evt;
	    evt.consume();
            return;
	}
        
        mousePressedImpl(evt);
    }

    public void mouseReleased(MouseEvent evt) {
        if (dndArmedEvent != null){
            mousePressedImpl(evt);        
        }
        dndArmedEvent = null;
    }

    public void mouseEntered(MouseEvent evt) {
    }

    public void mouseExited(MouseEvent evt) {
    }

    
    protected int mapDragOperationFromModifiers(MouseEvent e) {
        int mods = e.getModifiersEx();
        
        if ((mods & InputEvent.BUTTON1_DOWN_MASK) != InputEvent.BUTTON1_DOWN_MASK) {
            return TransferHandler.NONE;
        }
        
        return TransferHandler.COPY_OR_MOVE;
    }    
    /**
     * Determines if the following are true:
     * <ul>
     * <li>the press event is located over a selection
     * <li>the dragEnabled property is true
     * <li>A TranferHandler is installed
     * </ul>
     * <p>
     * This is implemented to check for a TransferHandler.
     * Subclasses should perform the remaining conditions.
     */
    protected boolean isDragPossible(MouseEvent e) {
        JComponent comp = getEventComponent(e);
        boolean possible =  (comp == null) ? true : (comp.getTransferHandler() != null);
        if (possible){
            JTextComponent c = (JTextComponent) getEventComponent(e);
            if (c.getDragEnabled()) {
                Caret caret = c.getCaret();
                int dot = caret.getDot();
                int mark = caret.getMark();
                if (dot != mark) {
                    Point p = new Point(e.getX(), e.getY());
                    int pos = c.viewToModel(p);

                    int p0 = Math.min(dot, mark);
                    int p1 = Math.max(dot, mark);
                    if ((pos >= p0) && (pos < p1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    protected JComponent getEventComponent(MouseEvent e) {
	Object src = e.getSource();
	if (src instanceof JComponent) {
	    JComponent c = (JComponent) src;
	    return c;
	}
	return null;
    }
    
    // MouseMotionListener methods
    public void mouseDragged(MouseEvent evt) {
        if (dndArmedEvent != null){
            evt.consume();
            return;
        }
        
        JTextComponent c = component;
        
        if (SwingUtilities.isLeftMouseButton(evt)) {
            if (c != null) {
                int offset = ((BaseTextUI)c.getUI()).viewToModel(c,
                          evt.getX(), evt.getY());
                // fix for #15204
                if (offset == -1)
                    offset = 0;
                // fix of #22846
                if (offset >= 0 && (evt.getModifiers() & InputEvent.SHIFT_MASK) == 0)
                    moveDot(offset);
            }
        }
    }

    public void mouseMoved(MouseEvent evt) {
    }

    // PropertyChangeListener methods
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if ("document".equals(propName)) { // NOI18N
            BaseDocument newDoc = (evt.getNewValue() instanceof BaseDocument)
                                  ? (BaseDocument)evt.getNewValue() : null;
            modelChanged(listenDoc, newDoc);

        } else if (EditorUI.OVERWRITE_MODE_PROPERTY.equals(propName)) {
            Boolean b = (Boolean)evt.getNewValue();
            overwriteMode = (b != null) ? b.booleanValue() : false;
            updateType();

        } else if ("ancestor".equals(propName) && evt.getSource() == component) { // NOI18N
            // The following code ensures that when the width of the line views
            // gets computed on background after the file gets opened
            // (so the horizontal scrollbar gets added after several seconds
            // for larger files) that the suddenly added horizontal scrollbar
            // will not hide the caret laying on the last line of the viewport.
            // A component listener gets installed into horizontal scrollbar
            // and if it's fired the caret's bounds will be checked whether
            // they intersect with the horizontal scrollbar
            // and if so the view will be scrolled.
            Container parent = component.getParent();
            if (parent instanceof JViewport) {
                parent = parent.getParent(); // parent of viewport
                if (parent instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane)parent;
                    JScrollBar hScrollBar = scrollPane.getHorizontalScrollBar();
                    if (hScrollBar != null) {
                        // Add weak listener so that editor pane could be removed
                        // from scrollpane without being held by scrollbar
                        hScrollBar.addComponentListener(
                                (ComponentListener)WeakListeners.create(
                                ComponentListener.class, listenerImpl, hScrollBar));
                    }
                }
            }
        }
    }

    // ActionListener methods
    /** Fired when blink timer fires */
    public void actionPerformed(ActionEvent evt) {
        JTextComponent c = component;
        if (c != null) {
            blinkVisible = !blinkVisible;
            if (caretBounds != null) {
                Rectangle repaintRect = caretBounds;
                if (italic) {
                    repaintRect = new Rectangle(repaintRect); // clone
                    repaintRect.width += repaintRect.height;
                }
                c.repaint(repaintRect);
            }
        }
    }

    public void foldHierarchyChanged(FoldHierarchyEvent evt) {
        int caretOffset = getDot();
        int addedFoldCnt = evt.getAddedFoldCount();
        final boolean scrollToView;
        if (addedFoldCnt > 0) {
            FoldHierarchy hierarchy = (FoldHierarchy)evt.getSource();
            Fold collapsed = null;
            while ((collapsed = FoldUtilities.findCollapsedFold(hierarchy, caretOffset, caretOffset)) != null && collapsed.getStartOffset() < caretOffset &&
                    collapsed.getEndOffset() > caretOffset) {
                hierarchy.expand(collapsed);                
            }
            scrollToView = true;
        } else {
            int startOffset = Integer.MAX_VALUE;
            // Set the caret's offset to the end of just collapsed fold if necessary
            if (evt.getAffectedStartOffset() <= caretOffset && evt.getAffectedEndOffset() >= caretOffset) {
                for (int i = 0; i < evt.getFoldStateChangeCount(); i++) {
                    FoldStateChange change = evt.getFoldStateChange(i);
                    if (change.isCollapsedChanged()) {
                        Fold fold = change.getFold();
                        if (fold.isCollapsed() && fold.getStartOffset() <= caretOffset && fold.getEndOffset() >= caretOffset) {
                            if (fold.getStartOffset() < startOffset) {
                                startOffset = fold.getStartOffset();
                            }
                        }
                    }
                }
                if (startOffset != Integer.MAX_VALUE) {
                    setDot(startOffset, false);
                }
            }
            scrollToView = false;
        }        
        // Update caret's visual position
        // Post the caret update asynchronously since the fold hierarchy is updated before
        // the view hierarchy and the views so the dispatchUpdate() could be picking obsolete
        // view information.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateAfterFoldHierarchyChange = true;
                dispatchUpdate(scrollToView); // do not scroll the window
            }
        });
    }
    
    private class ListenerImpl extends ComponentAdapter
    implements FocusListener {

        ListenerImpl() {
        }

        // FocusListener methods
        public void focusGained(FocusEvent evt) {
            if (debugCaretFocus || debugCaretFocusExtra) {
                System.err.println(
                        (debugCaretFocusExtra ? "\n" : "") // NOI18N
                        + "BaseCaret.focusGained(); doc=" // NOI18N
                        + component.getDocument().getProperty(Document.TitleProperty)
                );
            }
            
            JTextComponent c = component;
            if (c != null) {
                updateType();
                if (debugCaretFocusExtra) {
                    System.err.println("going to set caret visible to: "+c.isEnabled()); // NOI18N
                }
                setVisible(c.isEnabled()); // invisible caret if disabled
            } else {
                if (debugCaretFocusExtra) {
                    System.err.println("component is null, caret will not be dislayed"); // NOI18N
                }
            }
        }

        public void focusLost(FocusEvent evt) {
            if (debugCaretFocus || debugCaretFocusExtra) {
                System.err.println((debugCaretFocusExtra ? "\n" : "") // NOI18N
                        + "BaseCaret.focusLost(); doc=" // NOI18N
                        + component.getDocument().getProperty(Document.TitleProperty)
                        + "\nFOCUS GAINER: " + evt.getOppositeComponent() // NOI18N
                );
                if (debugCaretFocusExtra) {
                    System.err.println("FOCUS EVENT: " + evt); // NOI18N
                }
            }
	    setVisible(false);
        }

        // ComponentListener methods
        /**
         * May be called for either component or horizontal scrollbar.
         */
        public @Override void componentShown(ComponentEvent e) {
            // Called when horizontal scrollbar gets visible
            // (but the same listener added to component as well so must check first)
            // Check whether present caret position will not get hidden
            // under horizontal scrollbar and if so scroll the view
            Component hScrollBar = e.getComponent();
            if (hScrollBar != component) { // really called for horizontal scrollbar
                Component scrollPane = hScrollBar.getParent();
                if (caretBounds != null && scrollPane instanceof JScrollPane) {
                    Rectangle viewRect = ((JScrollPane)scrollPane).getViewport().getViewRect();
                    Rectangle hScrollBarRect = new Rectangle(
                            viewRect.x,
                            viewRect.y + viewRect.height,
                            hScrollBar.getWidth(),
                            hScrollBar.getHeight()
                            );
                    if (hScrollBarRect.intersects(caretBounds)) {
                        // Update caret's position
                        dispatchUpdate(true); // should be visible so scroll the view
                    }
                }
            }
        }

        
        /**
         * May be called for either component or horizontal scrollbar.
         */
        public @Override void componentResized(ComponentEvent e) {
            Component c = e.getComponent();
            if (c == component) { // called for component
                // In case the caretBounds are still null
                // (component not connected to hierarchy yet or it has zero size
                // so the modelToView() returned null) re-attempt to compute the bounds.
                if (caretBounds == null) {
                    dispatchUpdate(true);
                    if (caretBounds != null) { // detach the listener - no longer necessary
                        c.removeComponentListener(this);
                    }
                }
            }
        }

    } // End of ListenerImpl class

}
