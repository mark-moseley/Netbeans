/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.core.output2.ui;

import java.awt.Rectangle;
import javax.swing.plaf.TextUI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import org.netbeans.core.output2.OutputDocument;
import org.openide.util.Exceptions;

/**
 * A scroll pane containing an editor pane, with special handling of the caret
 * and scrollbar - until a keyboard or mouse event, after a call to setDocument(),
 * the caret and scrollbar are locked to the last line of the document.  This avoids
 * "jumping" scrollbars as the position of the caret (and thus the scrollbar) get updated
 * to reposition them at the bottom of the document on every document change.
 *
 * @author  Tim Boudreau
 */
public abstract class AbstractOutputPane extends JScrollPane implements DocumentListener, MouseListener, MouseMotionListener, KeyListener, ChangeListener, MouseWheelListener, Runnable {
    private boolean locked = true;
    
    private int fontHeight = -1;
    private int fontWidth = -1;
    protected JEditorPane textView;
    int lastCaretLine = 0;
    int caretBlinkRate = 500;
    boolean hadSelection = false;
    boolean recentlyReset = false;

    public AbstractOutputPane() {
        textView = createTextView();
        init();
    }

    //#114290
    public void doUpdateCaret() {
        Caret car = textView.getCaret();
        if (car instanceof DefaultCaret) {
            ((DefaultCaret)car).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        }
    }

    public void dontUpdateCaret() {
        Caret car = textView.getCaret();
        if (car instanceof DefaultCaret) {
            ((DefaultCaret)car).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }
    }

    @Override
    public void requestFocus() {
        textView.requestFocus();
    }
    
    @Override
    public boolean requestFocusInWindow() {
        return textView.requestFocusInWindow();
    }
    
    protected abstract JEditorPane createTextView();

    protected void documentChanged() {
        lastLength = -1;
        if (lineToScroll != -1) {
            if (scrollToLine(lineToScroll)) {
                lineToScroll = -1;
            }
        } else {
            ensureCaretPosition();
        }

        if (recentlyReset && isShowing()) {
            recentlyReset = false;
        }
        if (locked) {
            setMouseLine(-1);
        }
        if (isWrapped()) {
            //Saves having OutputEditorKit have to do its own listening
            getViewport().revalidate();
            getViewport().repaint();
        }
    }
    
    public abstract boolean isWrapped();
    public abstract void setWrapped (boolean val);

    public boolean hasSelection() {
        return textView.getSelectionStart() != textView.getSelectionEnd();
    }

    public boolean isScrollLocked() {
        return locked;
    }

    /**
     * Ensure that the document is scrolled all the way to the bottom (unless
     * some user event like scrolling or placing the caret has unlocked it).
     * <p>
     * Note that this method is always called on the event queue, since 
     * OutputDocument only fires changes on the event queue.
     */
    public final void ensureCaretPosition() {
        if (locked && !enqueued) {
            //Make sure the scrollbar is updated *after* the document change
            //has been processed and the scrollbar model's maximum updated
            enqueued = true;
            SwingUtilities.invokeLater(this);
        }
    }
    
    /** True when invokeLater has already been called on this instance */
    private boolean enqueued = false;
    /**
     * Scrolls the pane to the bottom, invokeLatered to ensure all state has
     * been updated, so the bottom really *is* the bottom.
     */
    public void run() {
        enqueued = false;
        if (locked) {
            getVerticalScrollBar().setValue(getVerticalScrollBar().getModel().getMaximum());
            getHorizontalScrollBar().setValue(getHorizontalScrollBar().getModel().getMinimum());
        }
    }

    public int getSelectionStart() {
        return textView.getSelectionStart();
    }
    
    public int getSelectionEnd() {
        return textView.getSelectionEnd();
    }

    public void setSelection (int start, int end) {
        int rstart = Math.min (start, end);
        int rend = Math.max (start, end);
        if (rstart == rend) {
            getCaret().setDot(rstart);
        } else {
            textView.setSelectionStart(rstart);
            textView.setSelectionEnd(rend);
        }
    }

    public void selectAll() {
        unlockScroll();
        getCaret().setVisible(true);
        textView.setSelectionStart(0);
        textView.setSelectionEnd(getLength());
    }

    public boolean isAllSelected() {
        return textView.getSelectionStart() == 0 && textView.getSelectionEnd() == getLength();
    }

    protected void init() {
        setViewportView(textView);
        textView.setEditable(false);

        textView.addMouseListener(this);
        textView.addMouseWheelListener(this);
        textView.addMouseMotionListener(this);
        textView.addKeyListener(this);
        //#107354
        OCaret oc = new OCaret();
        oc.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        textView.setCaret (oc);
        
        getCaret().setSelectionVisible(true);
        
        getVerticalScrollBar().getModel().addChangeListener(this);
        getVerticalScrollBar().addMouseMotionListener(this);
        
        getViewport().addMouseListener(this);
        getVerticalScrollBar().addMouseListener(this);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
        addMouseListener(this);

        getCaret().addChangeListener(this);
        Integer i = (Integer) UIManager.get("customFontSize"); //NOI18N
        int size;
        if (i != null) {
            size = i.intValue();
        } else {
            Font f = (Font) UIManager.get("controlFont");
            size = f != null ? f.getSize() : 11;
        }
        textView.setFont (new Font ("Monospaced", Font.PLAIN, size)); //NOI18N
        setBorder (BorderFactory.createEmptyBorder());
        setViewportBorder (BorderFactory.createEmptyBorder());
        
        Color c = UIManager.getColor("nb.output.selectionBackground");
        if (c != null) {
            textView.setSelectionColor(c);
        }
    }

    public final Document getDocument() {
        return textView.getDocument();
    }
    
    /**
     * This method is here for use *only* by unit tests.
     */
    public final JTextComponent getTextView() {
        return textView;
    }

    public final void copy() {
        if (getCaret().getDot() != getCaret().getMark()) {
            textView.copy();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    protected void setDocument (Document doc) {
        if (hasSelection()) {
            hasSelectionChanged(false);
        }
        hadSelection = false;
        lastCaretLine = 0;
        lastLength = -1;
        lineToScroll = -1;
        Document old = textView.getDocument();
        old.removeDocumentListener(this);
        if (doc != null) {
            textView.setDocument(doc);
            doc.addDocumentListener(this);
            lockScroll();
            recentlyReset = true;
        } else {
            textView.setDocument (new PlainDocument());
            textView.setEditorKit(new DefaultEditorKit());
        }
    }
    
    protected void setEditorKit(EditorKit kit) {
        Document doc = textView.getDocument();
        
        textView.setEditorKit(kit);
        textView.setDocument(doc);
        updateKeyBindings();
    }
    
    /**
     * Setting the editor kit will clear the action map/key map connection
     * to the TopComponent, so we reset it here.
     */
    protected final void updateKeyBindings() {
        Keymap keymap = textView.getKeymap();
        keymap.removeKeyStrokeBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
    }
    
    protected EditorKit getEditorKit() {
        return textView.getEditorKit();
    }
    
    public final int getLineCount() {
        return textView.getDocument().getDefaultRootElement().getElementCount();
    }

    private int lastLength = -1;
    public final int getLength() {
        if (lastLength == -1) {
            lastLength = textView.getDocument().getLength();
        }
        return lastLength;
    }
    
    private boolean inSendCaretToLine = false;
    private int lineToScroll = -1;
    public final boolean sendCaretToLine(int idx, boolean select) {
        int lastLine = getLineCount() - 1;
        if (idx > lastLine) {
            idx = lastLine;
        }
        inSendCaretToLine = true;
        getCaret().setVisible(true);
        getCaret().setSelectionVisible(true);
        Element el = textView.getDocument().getDefaultRootElement().getElement(idx);
        int position = el.getStartOffset();
        if (select) {
            getCaret().setDot(el.getEndOffset() - 1);
            getCaret().moveDot(position);
            getCaret().setSelectionVisible(true);
            textView.repaint();
        } else {
            getCaret().setDot(position);
        }

        if (!scrollToLine(idx + 3) && isScrollLocked()) {
            lineToScroll = idx + 3;
        }
        locked = false;
        inSendCaretToLine = false;
        return true;
    }
    
    boolean scrollToLine(int line) {
        int lineIdx = Math.min(getLineCount() - 1, line);
        Rectangle rect = null;
        try {
            rect = textView.modelToView(textView.getDocument().getDefaultRootElement().getElement(lineIdx).getStartOffset());
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        if (rect == null) {
            return false;
        }

        boolean oldLocked = locked;
        textView.scrollRectToVisible(rect);
        locked = oldLocked;

        Rectangle visRect = textView.getVisibleRect();
        return line == lineIdx && visRect.y + visRect.height == rect.y + rect.height;
    }

    public final void lockScroll() {
        if (!locked) {
            locked = true;
        }
    }
    
    public final void unlockScroll() {
        if (locked) {
            locked = false;
        }
        lineToScroll = -1;
    }

    protected abstract void caretEnteredLine (int line);
    
    protected abstract void lineClicked (int line, Point p);
    
    protected abstract void postPopupMenu (Point p, Component src);
    
    public final int getCaretLine() {
        int result = 0;
        int charPos = getCaret().getDot();
        if (charPos > 0) {
            result = textView.getDocument().getDefaultRootElement().getElementIndex(charPos);
        }
        return result;
    }

    public final boolean isLineSelected(int idx) {
        Element line = textView.getDocument().getDefaultRootElement().getElement(idx);
        return line.getStartOffset() == getSelectionStart() && line.getEndOffset()-1 == getSelectionEnd();
    }

    public final int getCaretPos() {
        return getCaret().getDot();
    }

    @Override
    public final void paint (Graphics g) {
        if (fontHeight == -1) {
            fontHeight = g.getFontMetrics(textView.getFont()).getHeight();
            fontWidth = g.getFontMetrics(textView.getFont()).charWidth('m'); //NOI18N
        }
        super.paint(g);
    }

//***********************Listener implementations*****************************

    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == getVerticalScrollBar().getModel()) {
            if (!locked) { //XXX check if doc is still being written?
                BoundedRangeModel mdl = getVerticalScrollBar().getModel();
                if (mdl.getValue() + mdl.getExtent() == mdl.getMaximum()) {
                    lockScroll();
                }
            }
        } else {
            if (!locked) {
                maybeSendCaretEnteredLine();
            }
            boolean hasSelection = textView.getSelectionStart() != textView.getSelectionEnd();
            if (hasSelection != hadSelection) {
                hadSelection = hasSelection;
                hasSelectionChanged (hasSelection);
            }
        }
    }

    private boolean caretLineChanged() {
        int line = getCaretLine();
        boolean result = line != lastCaretLine;
        lastCaretLine = line;
        return result;
    }

    private void maybeSendCaretEnteredLine() {
        if (EventQueue.getCurrentEvent() instanceof MouseEvent) {
            //User may have clicked a hyperlink, in which case, we'll test
            //it and see if it's really in the text of the hyperlink - so
            //don't do anything here
            return;
        }
        //Don't message the controller if we're programmatically setting
        //the selection, or if the caret moved because output was written - 
        //it can cause the controller to send events to OutputListeners which
        //should only happen for user events
        if ((!locked && caretLineChanged()) && !inSendCaretToLine) {
            boolean sel = textView.getSelectionStart() != textView.getSelectionEnd();
            if (!sel) {
                caretEnteredLine(getCaretLine());
            }
            if (sel != hadSelection) {
                hadSelection = sel;
                hasSelectionChanged (sel);
            }
        }
    }


    private void hasSelectionChanged(boolean sel) {
        ((AbstractOutputTab) getParent()).hasSelectionChanged(sel);
    }

    public final void changedUpdate(DocumentEvent e) {
        //Ensure it is consumed
        e.getLength();
        documentChanged();
        if (e.getOffset() >= getCaretPos() && (locked || !(e instanceof OutputDocument.DO))) {
            //#119985 only move caret when not in editable section
            OutputDocument doc = (OutputDocument)e.getDocument();
            if (! (e instanceof OutputDocument.DO) && getCaretPos() >= doc.getOutputLength()) {
                return ;
            }
            
            getCaret().setDot(e.getOffset() + e.getLength());
        }
    }

    public final void insertUpdate(DocumentEvent e) {
        //Ensure it is consumed
        e.getLength();
        documentChanged();
        if (e.getOffset() >= getCaretPos() && (locked || !(e instanceof OutputDocument.DO))) {
            //#119985 only move caret when not in editable section
            OutputDocument doc = (OutputDocument)e.getDocument();
            if (! (e instanceof OutputDocument.DO) && getCaretPos() >= doc.getOutputLength()) {
                return ;
            }
            
            getCaret().setDot(e.getOffset() + e.getLength());
        }
    }

    public final void removeUpdate(DocumentEvent e) {
        //Ensure it is consumed
        e.getLength();
        documentChanged();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
        setMouseLine (-1);
    }

    private int mouseLine = -1;
    public void setMouseLine (int line, Point p) {
        if (mouseLine != line) {
            mouseLine = line;
        }
    }
    
    public final void setMouseLine (int line) {
        setMouseLine (line, null);
    }


    public void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        int pos = textView.viewToModel(p);
        if (pos < getLength()) {
            int line = getDocument().getDefaultRootElement().getElementIndex(pos);
            int lineStart = getDocument().getDefaultRootElement().getElement(line).getStartOffset();
            int lineLength = getDocument().getDefaultRootElement().getElement(line).getEndOffset() -
                    lineStart;

            try {
                Rectangle r = textView.modelToView(lineStart + lineLength -1);
                int maxX = r.x + r.width;
                boolean inLine = p.x <= maxX;
                if (isWrapped()) {
                    Rectangle ra = textView.modelToView(lineStart);
                    if (ra.y <= r.y) {
                        if (p.y < r.y) {
                            inLine = true;
                        }
                    }
                }
                
                if (inLine) {
                    setMouseLine (line, p);
                } else {
                    setMouseLine(-1);
                }
            } catch (BadLocationException ble) {
                setMouseLine(-1);
            }
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (e.getSource() == getVerticalScrollBar()) {
            int y = e.getY();
            if (y > getVerticalScrollBar().getHeight()) {
                lockScroll();
            }
        }
    }

    /** last pressed position for hyperlink test */
    private int lastPressedPos = -1;

    public void mousePressed(MouseEvent e) {
        if (e.getSource() == textView && SwingUtilities.isLeftMouseButton(e)) {
            lastPressedPos = textView.viewToModel(e.getPoint());
        }
        if (locked && !e.isPopupTrigger()) {
            Element el = getDocument().getDefaultRootElement().getElement(getLineCount()-1);
            getCaret().setDot(el.getStartOffset());
            unlockScroll();
            //We should now set the caret position so the caret doesn't
            //seem to ignore the first click
            if (e.getSource() == textView) {
                getCaret().setDot (textView.viewToModel(e.getPoint()));
            }
        }
        if (e.isPopupTrigger()) {
            //Convert immediately to our component space - if the 
            //text view scrolls before the component is opened, popup can
            //appear above the top of the screen
            Point p = SwingUtilities.convertPoint((Component) e.getSource(), 
                e.getPoint(), this);
            
            postPopupMenu (p, this);
        }
    }

    public final void mouseReleased(MouseEvent e) {
        if (e.getSource() == textView && SwingUtilities.isLeftMouseButton(e)) {
            int pos = textView.viewToModel(e.getPoint());
            if (pos != -1 && pos == lastPressedPos) {
                int line = textView.getDocument().getDefaultRootElement().getElementIndex(pos);
                if (line >= 0) {
                    lineClicked(line, e.getPoint());
                    e.consume(); //do NOT allow this window's caret to steal the focus from editor window
                }
            }
            lastPressedPos = -1;
        }
        if (e.isPopupTrigger()) {
            Point p = SwingUtilities.convertPoint((Component) e.getSource(), 
            //Convert immediately to our component space - if the 
            //text view scrolls before the component is opened, popup can
            //appear above the top of the screen
                e.getPoint(), this);
            
            postPopupMenu (p, this);
        }
    }

    public void keyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_END:
                if (keyEvent.isControlDown()) {
                    lockScroll();
                }
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_HOME:
            case KeyEvent.VK_PAGE_UP:
            case KeyEvent.VK_PAGE_DOWN:
                unlockScroll();
                break;
        }
    }

    public void keyReleased(KeyEvent keyEvent) {
    }

    public void keyTyped(KeyEvent keyEvent) {
    }

    public final void mouseWheelMoved(MouseWheelEvent e) {
        BoundedRangeModel sbmodel = getVerticalScrollBar().getModel();
        int max = sbmodel.getMaximum();
        int range = sbmodel.getExtent();

        int currPosition = sbmodel.getValue();
        if (e.getSource() == textView) {
            int newPosition = Math.max (0, Math.min (sbmodel.getMaximum(),
                currPosition + (e.getUnitsToScroll() * textView.getFontMetrics(textView.getFont()).getHeight())));
            // height is a magic constant because of #57532
            sbmodel.setValue (newPosition);
            if (newPosition + range >= max) {
                lockScroll();
                return;
            }
        }
        unlockScroll();
    }

    Caret getCaret() {
        return textView.getCaret();
    }
    
    private class OCaret extends DefaultCaret {
        @Override
        public void paint(Graphics g) {
            JTextComponent component = textView;
            if(isVisible() && y >= 0) {
                try {
                    TextUI mapper = component.getUI();
                    Rectangle r = mapper.modelToView(component, getDot(), Position.Bias.Forward);

                    if ((r == null) || ((r.width == 0) && (r.height == 0))) {
                        return;
                    }
                    if (width > 0 && height > 0 &&
                                    !this._contains(r.x, r.y, r.width, r.height)) {
                        // We seem to have gotten out of sync and no longer
                        // contain the right location, adjust accordingly.
                        Rectangle clip = g.getClipBounds();

                        if (clip != null && !clip.contains(this)) {
                            // Clip doesn't contain the old location, force it
                            // to be repainted lest we leave a caret around.
                            repaint();
                        }
 //                       System.err.println("WRONG! Caret dot m2v = " + r + " but my bounds are " + x + "," + y + "," + width + "," + height);
                        
                        // This will potentially cause a repaint of something
                        // we're already repainting, but without changing the
                        // semantics of damage we can't really get around this.
                        damage(r);
                    }
                    g.setColor(component.getCaretColor());
                    g.drawLine(r.x, r.y, r.x, r.y + r.height - 1);
                    g.drawLine(r.x+1, r.y, r.x+1, r.y + r.height - 1);

                } catch (BadLocationException e) {
                    // can't render I guess
//                    System.err.println("Can't render cursor");
                }
            }
        }
        
        private boolean _contains(int X, int Y, int W, int H) {
            int w = this.width;
            int h = this.height;
            if ((w | h | W | H) < 0) {
                // At least one of the dimensions is negative...
                return false;
            }
            // Note: if any dimension is zero, tests below must return false...
            int x = this.x;
            int y = this.y;
            if (X < x || Y < y) {
                return false;
            }
            if (W > 0) {
                w += x;
                W += X;
                if (W <= X) {
                    // X+W overflowed or W was zero, return false if...
                    // either original w or W was zero or
                    // x+w did not overflow or
                    // the overflowed x+w is smaller than the overflowed X+W
                    if (w >= x || W > w) {
                        return false;
                    }
                } else {
                    // X+W did not overflow and W was not zero, return false if...
                    // original w was zero or
                    // x+w did not overflow and x+w is smaller than X+W
                    if (w >= x && W > w) {
                        //This is the bug in DefaultCaret - returns false here
                        return true;
                    }
                }
            }
            else if ((x + w) < X) {
                return false;
            }
            if (H > 0) {
                h += y;
                H += Y;
                if (H <= Y) {
                    if (h >= y || H > h) return false;
                } else {
                    if (h >= y && H > h) return false;
                }
            }
            else if ((y + h) < Y) {
                return false;
            }
            return true;
        }        

        @Override
        public void mouseReleased(MouseEvent e) {
            if( !e.isConsumed() ) {
                super.mouseReleased(e);
            }
        }

        @Override
        public void focusGained(FocusEvent e) {
            getCaret().setBlinkRate(caretBlinkRate);
            getCaret().setVisible(true);
        }

        @Override
        public void focusLost(FocusEvent e) {
            getCaret().setVisible(false);
        }

        @Override
        public void setSelectionVisible(boolean vis) {
            if (vis) {
                super.setSelectionVisible(vis);
            }
        }
    }
}
