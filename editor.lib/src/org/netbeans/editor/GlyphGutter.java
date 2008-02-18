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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.text.BadLocationException;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.InputEvent;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.event.*;
import java.util.Collections;
import java.util.Map;
import javax.swing.Action;
import javax.accessibility.*;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.View;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldHierarchyEvent;
import org.netbeans.api.editor.fold.FoldHierarchyListener;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/** GlyphGutter is component for displaying line numbers and annotation
 * glyph icons. Component also allow to "cycle" through the annotations. It
 * means that if there is more than one annotation on the line, only one of them
 * might be visible. And clicking the special cycling button in the gutter the user
 * can cycle through the annotations.
 *
 * @author  David Konecny
 * @since 07/2001
 */

public class GlyphGutter extends JComponent implements Annotations.AnnotationsListener, Accessible, SettingsChangeListener, SideBarFactory {

    /** EditorUI which part this gutter is */
    private EditorUI editorUI;
    
    /** Document to which this gutter is attached*/
    private BaseDocument doc;
    
    /** Annotations manager responsible for annotations for this line */
    private Annotations annos;
    
    /** Cycling button image */
    private Image gutterButton;
    
    /** Backroung color of the gutter */
    private Color backgroundColor;
    
    /** Foreground color of the gutter. Used for drawing line numbers. */
    private Color foreColor;
    
    /** Font used for drawing line numbers */
    private Font font;
    
    /** Height of the line as it was calculated in EditorUI. */
    private int lineHeight;

    /** Flag whther the gutter was initialized or not. The painting is disabled till the
     * gutter is not initialized */
    private boolean init;
    
    /** Width of the glyph gutter*/ 
    private int glyphGutterWidth;

    /** Predefined width of the glyph icons */
    private final static int glyphWidth = 16;

    /** Preddefined width of the cycling button */
    private final static int glyphButtonWidth = 9;
    
    /** Predefined left area width - area between left border of the number
     *  and the left border of the glyphgutter
     */
    private final static int leftGap= 10;

    /** Predefined right area width - area between right border of the number
     *  and the right border of the glyphgutter
     */
    private final static int rightGap= 4;
    
    /** Whether the line numbers are shown or not */
    private boolean showLineNumbers = true;
    
    /** The gutter height is enlarged by number of lines which specifies this constant */
    private static final int ENLARGE_GUTTER_HEIGHT = 300;
    
    /** The hightest line number. This value is used for calculating width of the gutter */
    private int highestLineNumber = 0;
    
    /** Whether the annotation glyph can be drawn over the line numbers */
    private boolean drawOverLineNumbers = false;

    /* These two variables are used for caching of count of line annos 
     * on the line over which is the mouse caret. Just for sake of optimalization. */
    private int cachedCountOfAnnos = -1;
    private int cachedCountOfAnnosForLine = -1;

    /** Property change listener on AnnotationTypes changes */
    private PropertyChangeListener annoTypesListener;
    private PropertyChangeListener editorUIListener;
    private GlyphGutter.GlyphGutterFoldHierarchyListener glyphGutterFoldHierarchyListener;
    private GutterMouseListener gutterMouseListener;
    private FoldHierarchy foldHierarchy;
    private volatile Map renderingHints = null;
    
    public GlyphGutter(){}
    
    public GlyphGutter(EditorUI editorUI) {
        super();
        this.editorUI = editorUI;
        init = false;
        doc = editorUI.getDocument();
        annos = doc.getAnnotations();
        
        // Annotations class is model for this view, so the listener on changes in
        // Annotations must be added here
        annos.addAnnotationsListener(this);

        // do initialization
        init();
        update();
        Settings.addSettingsChangeListener(this);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        foldHierarchy = FoldHierarchy.get(editorUI.getComponent());
        glyphGutterFoldHierarchyListener = new GlyphGutterFoldHierarchyListener();
        foldHierarchy.addFoldHierarchyListener(glyphGutterFoldHierarchyListener);
        editorUIListener = new EditorUIListener();
        editorUI.addPropertyChangeListener(editorUIListener);
        setOpaque (true);
    }

    private Map getRenderingHints() {
        if (renderingHints == null) {
            Object value = null;
            JTextComponent comp = editorUI.getComponent();
            if (comp != null) {
                value = (Map)(Toolkit.getDefaultToolkit().getDesktopProperty(
                        "awt.font.desktophints")); //NOI18N
                //Don't bother seeing if the hints are explicitly turned off (if they
                //even can be) as in EditorUI - it's a tooltip, desktop default is
                //fine
                if (value == null) {
                    value = Settings.getValue(Utilities.getKitClass(comp), 
                            SettingsNames.RENDERING_HINTS);
                }
            }
            renderingHints = (value instanceof Map) ? (java.util.Map)value : Collections.EMPTY_MAP;
        }
        return renderingHints;
    }
    
    public void settingsChange(SettingsChangeEvent evt) {
        if (editorUI == null) // no long er active
            return;

        final JTextComponent component = editorUI.getComponent();
        if (evt == null || component == null) return;

        String settingName = evt.getSettingName();
        if (settingName == null || SettingsNames.RENDERING_HINTS.equals(settingName)) {
            renderingHints = null;
        }
        
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    Rectangle rect = component.getVisibleRect();
                    if (rect != null) {
                        resize();
                    }
                }
            }
        );
    }
    
    
    /* Read accessible context
     * @return - accessible context
     */
    public @Override AccessibleContext getAccessibleContext () {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                public @Override AccessibleRole getAccessibleRole() {
                    return AccessibleRole.PANEL;
                }
            };
        }
        return accessibleContext;
    }

    /** Do initialization of the glyph gutter*/
    protected void init() {
        if (editorUI == null)
            return ;

        gutterButton = org.openide.util.Utilities.loadImage("org/netbeans/editor/resources/glyphbutton.gif");

        setToolTipText ("");
        getAccessibleContext().setAccessibleName(NbBundle.getBundle(BaseKit.class).getString("ACSN_Glyph_Gutter")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(BaseKit.class).getString("ACSD_Glyph_Gutter")); // NOI18N

        // add mouse listener for cycling button
        // TODO: clicking the line number should select whole line
        // TODO: clicking the line number abd dragging the mouse should select block of lines
        gutterMouseListener = new GutterMouseListener ();
        addMouseListener (gutterMouseListener);
        addMouseMotionListener (gutterMouseListener);

        AnnotationTypes.getTypes().addPropertyChangeListener( annoTypesListener = new PropertyChangeListener() {
            public void propertyChange (PropertyChangeEvent evt) {
                if (evt.getPropertyName() == null ||
                    evt.getPropertyName().equals(AnnotationTypes.PROP_GLYPHS_OVER_LINE_NUMBERS) ||
                    evt.getPropertyName().equals(AnnotationTypes.PROP_SHOW_GLYPH_GUTTER))
                {
                    update();
                }
            }
        });
        
    }
    
    /** Update colors, fonts, sizes and invalidate itself. This method is
     * called from EditorUI.update() */
    public void update() {
        if (editorUI == null)
            return ;
        Coloring lineColoring = (Coloring)editorUI.getColoringMap().get(SettingsNames.LINE_NUMBER_COLORING);
        Coloring defaultColoring = (Coloring)editorUI.getDefaultColoring();
        
        // fix for issue #16940
        // the real cause of this problem is that closed document is not garbage collected, 
        // because of *some* references (see #16072) and so any change in AnnotationTypes.PROP_*
        // properties is fired which must update this component although it is not visible anymore
        if (lineColoring == null)
            return;
        
        if (lineColoring.getBackColor() != null)
            backgroundColor = lineColoring.getBackColor();
        else
            backgroundColor = defaultColoring.getBackColor();

        if (lineColoring.getForeColor() != null)
            foreColor = lineColoring.getForeColor();
        else
            foreColor = defaultColoring.getForeColor();
        
        if (lineColoring.getFont() != null) {
            Font lineFont = lineColoring.getFont();
            font = (lineFont != null) ? lineFont.deriveFont((float)lineFont.getSize()-1) : null;
        } else {
            font = defaultColoring.getFont();
            font = new Font("Monospaced", Font.PLAIN, font.getSize()-1); //NOI18N
        }

        lineHeight = editorUI.getLineHeight();

        showLineNumbers = editorUI.lineNumberVisibleSetting;

        drawOverLineNumbers = AnnotationTypes.getTypes().isGlyphsOverLineNumbers().booleanValue();
        
        
        
        init = true;

        // initialize the value with current number of lines
        highestLineNumber = getLineCount();
        
        repaint();
        resize();
    }
    
   
    protected void resize() {
        Dimension dim = new Dimension();
        glyphGutterWidth = getWidthDimension();
        dim.width = glyphGutterWidth;
        dim.height = getHeightDimension();
        
        
        // enlarge the gutter so that inserting new lines into 
        // document does not cause resizing too often
        dim.height += ENLARGE_GUTTER_HEIGHT * lineHeight;
        
        setPreferredSize(dim);

        revalidate();
    }

    /** Return number of lines in the document */
    protected int getLineCount() {
        int lineCnt;
        try {
            if (doc != null) {
                lineCnt = Utilities.getLineOffset(doc, doc.getLength()) + 1;
            } else { // deactivated
                lineCnt = 1;
            }
        } catch (BadLocationException e) {
            lineCnt = 1;
        }
        return lineCnt;
    }

    /** Gets number of digits in the number */
    protected int getDigitCount(int number) {
        return Integer.toString(number).length();
    }

    protected int getLineNumberWidth() {
        int newWidth = 0;
        
        if (editorUI != null) {
            /*
            Insets insets = editorUI.getLineNumberMargin();
            if (insets != null) {
                newWidth += insets.left + insets.right;
            }
             */
            newWidth += getDigitCount(highestLineNumber) * editorUI.getLineNumberDigitWidth();
        }
        
        return newWidth;
    }
    
    protected int getWidthDimension() {
        int newWidth = 0;
        
        if (showLineNumbers) {
            int lineNumberWidth = getLineNumberWidth();
            newWidth = leftGap + lineNumberWidth + rightGap;
        } else {
            if (editorUI != null) {
                if (annos.isGlyphColumn() || 
                        AnnotationTypes.getTypes().isShowGlyphGutter().booleanValue()){
                    newWidth += glyphWidth;
                }

                if (annos.isGlyphButtonColumn()){
                             newWidth += glyphButtonWidth;
                }
            }
        }

        return newWidth;
    }
    
    protected int getHeightDimension() {
        if (editorUI == null)
            return 0;
        JComponent comp = editorUI.getComponent();
        if (comp == null)
            return 0;
        return highestLineNumber * lineHeight + (int)comp.getSize().getHeight();
    }
    

    void paintGutterForView(Graphics g, View view, int y){
        if (editorUI == null)
            return ;
        JTextComponent component = editorUI.getComponent();
        if (component == null) return;
        BaseTextUI textUI = (BaseTextUI)component.getUI();

        g.setFont(font); 
        g.setColor(foreColor);

        FontMetrics fm = FontMetricsCache.getFontMetrics(font, this);
        Element rootElem = textUI.getRootView(component).getElement();
        int line = rootElem.getElementIndex(view.getStartOffset());
        // find the nearest visible line with an annotation
        int lineWithAnno = annos.getNextLineWithAnnotation(line);

        int lineNumberWidth = fm.stringWidth(String.valueOf(line + 1));

        int count = annos.getNumberOfAnnotations(line);
        AnnotationDesc anno = annos.getActiveAnnotation(line);
        
        if (showLineNumbers){
            boolean glyphHasIcon = false;
            if (line == lineWithAnno){
                if (anno != null && !(anno.isDefaultGlyph()&&count == 1) && anno.getGlyph()!=null){
                    glyphHasIcon = true;
                }
            }
            if ((!glyphHasIcon) ||
                    (!drawOverLineNumbers) || 
                    (drawOverLineNumbers && line != lineWithAnno) ) {
                g.drawString(String.valueOf(line + 1), glyphGutterWidth-lineNumberWidth-rightGap, y + editorUI.getLineAscent());
            }
        }

        // draw anotation if we get to the line with some annotation
        if (line == lineWithAnno) {
            int xPos = (showLineNumbers) ? getLineNumberWidth() : 0;
            if (drawOverLineNumbers) {
                xPos = getWidth() - glyphWidth;
                if (count > 1)
                    xPos -= glyphButtonWidth;
            }

            if (anno != null) {
                // draw the glyph only when the annotation type has its own icon (no the default one)
                // or in case there is more than one annotations on the line
                if ( ! (count == 1 && anno.isDefaultGlyph()) ) {
                    if (anno.getGlyph() != null)
                        g.drawImage(anno.getGlyph(), xPos, y + (lineHeight-anno.getGlyph().getHeight(null)) / 2 + 1, null);
                }
            }

            // draw cycling button if there is more than one annotations on the line
            if (count > 1)
                if (anno.getGlyph() != null)
                    g.drawImage(gutterButton, xPos+glyphWidth-1, y + (lineHeight-anno.getGlyph().getHeight(null)) / 2, null);

            // update the value with next line with some anntoation
            lineWithAnno = annos.getNextLineWithAnnotation(line+1);
        }
    }
    
    /** Paint the gutter itself */
    public @Override void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (editorUI == null)
            return ;

        // Possibly apply the rendering hints
        Map hints = getRenderingHints();
        if (!hints.isEmpty()) {
            ((java.awt.Graphics2D)g).setRenderingHints(hints);
        }
        
        // if the gutter was not initialized yet, skip the painting        
        if (!init) return;
        
        Rectangle clip = g.getClipBounds();   

        JTextComponent component = editorUI.getComponent();
        if (component == null) return;
        
        BaseTextUI textUI = (BaseTextUI)component.getUI();
        View rootView = Utilities.getDocumentView(component);
        if (rootView == null) return;
      
        g.setColor(backgroundColor);
        g.fillRect(clip.x, clip.y, clip.width, clip.height);
        
        //painting gutter line
        g.setColor(SettingsDefaults.defaultGutterLine);
        g.drawLine(glyphGutterWidth-1, clip.y, glyphGutterWidth-1, clip.height + clip.y);

        AbstractDocument doc = (AbstractDocument)component.getDocument();
        doc.readLock();
        try{
            foldHierarchy.lock();
            try{
                int startPos = textUI.getPosFromY(clip.y);
                int startViewIndex = rootView.getViewIndex(startPos,Position.Bias.Forward);
                int rootViewCount = rootView.getViewCount();

                if (startViewIndex >= 0 && startViewIndex < rootViewCount) {
                    // find the nearest visible line with an annotation
                    Rectangle rec = textUI.modelToView(component, rootView.getView(startViewIndex).getStartOffset());
                    int y = (rec == null) ? 0 : rec.y;

                    int clipEndY = clip.y + clip.height;
                    for (int i = startViewIndex; i < rootViewCount; i++){
                        View view = rootView.getView(i);                
                        paintGutterForView(g, view, y);
                        y += editorUI.getLineHeight();
                        if (y >= clipEndY) {
                            break;
                        }
                    }
                }
                
            }finally{
                foldHierarchy.unlock();
            }
        }catch(BadLocationException ble){
            ErrorManager.getDefault().notify(ble);
        }finally{
            doc.readUnlock();
        }
    }

    /** Data for the line has changed and the line must be redraw. */
    public void changedLine(int line) {
        
        if (!init || editorUI == null)
            return;

        // reset cache if there was some change
        cachedCountOfAnnos = -1;
        
        // redraw also lines around - three lines will be redrawn
        if (line > 0)
            line--;
        JTextComponent component = editorUI.getComponent();
        if (component!=null){
            BaseTextUI textUI = (BaseTextUI)component.getUI();
            try{
                Element rootElem = component.getDocument().getDefaultRootElement();
                if (line >= rootElem.getElementCount()) { // #42504
                    return;
                }
                Element lineElem = rootElem.getElement(line);
                if (lineElem == null) return;
                int lineOffset = lineElem.getStartOffset();
                Rectangle mtvRect = textUI.modelToView(component, lineOffset);
                if (mtvRect == null) return;
                repaint(0, mtvRect.y, (int)getSize().getWidth(), 3*lineHeight);
                checkSize();
            }catch(BadLocationException ble){
                ErrorManager.getDefault().notify(ble);
            }
        }
    }

    /** Repaint whole gutter.*/
    public void changedAll() {

        if (!init || editorUI == null)
            return;

        // reset cache if there was some change
        cachedCountOfAnnos = -1;
        
        int lineCnt;
        try {
            lineCnt = Utilities.getLineOffset(doc, doc.getLength()) + 1;
        } catch (BadLocationException e) {
            lineCnt = 1;
        }

        repaint();
        checkSize();
    }

    /** Check whether it is not necessary to resize the gutter */
    protected void checkSize() {
        int count = getLineCount();
        if (count > highestLineNumber) {
            highestLineNumber = count;
        }
        Dimension dim = getPreferredSize();
        if (getWidthDimension() > dim.width ||
            getHeightDimension() > dim.height) {
            resize();
        }
        
    }

    /** Get tooltip text for the mouse position */
    // TODO: does not work for asynchronous tooltip texts
    public @Override String getToolTipText (MouseEvent e) {
        if (editorUI == null)
            return null;
        int line = getLineFromMouseEvent(e);
        if (annos.getNumberOfAnnotations(line) == 0)
            return null;
        if (isMouseOverCycleButton(e) && annos.getNumberOfAnnotations(line) > 1) {
            return java.text.MessageFormat.format (
                NbBundle.getBundle(BaseKit.class).getString ("cycling-glyph_tooltip"), //NOI18N
                new Object[] { new Integer (annos.getNumberOfAnnotations(line)) });
        }
        else if (isMouseOverGlyph(e)) {
            return annos.getActiveAnnotation(line).getShortDescription();
        }
        else
            return null;
    }

    /** Count the X position of the glyph on the line. */
    private int getXPosOfGlyph(int line) {
        if (editorUI == null)
            return 0;
        int xPos = (showLineNumbers) ? getLineNumberWidth() : 0;
        if (drawOverLineNumbers) {
            xPos = getWidth() - glyphWidth;
            if (cachedCountOfAnnos == -1 || cachedCountOfAnnosForLine != line) {
                cachedCountOfAnnos = annos.getNumberOfAnnotations(line);
                cachedCountOfAnnosForLine = line;
            }
            if (cachedCountOfAnnos > 1)
                xPos -= glyphButtonWidth;
        }
        return xPos;
    }

    /** Check whether the mouse is over some glyph icon or not */
    private boolean isMouseOverGlyph(MouseEvent e) {
        int line = getLineFromMouseEvent(e);
        if (e.getX() >= getXPosOfGlyph(line) && e.getX() <= getXPosOfGlyph(line)+glyphWidth)
            return true;
        else
            return false;
    }
    
    /** Check whether the mouse is over the cycling button or not */
    private boolean isMouseOverCycleButton(MouseEvent e) {
        int line = getLineFromMouseEvent(e);
        if (e.getX() >= getXPosOfGlyph(line)+glyphWidth && e.getX() <= getXPosOfGlyph(line)+glyphWidth+glyphButtonWidth)
            return true;
        else
            return false;
    }

    public JComponent createSideBar(JTextComponent target) {
        EditorUI eui = Utilities.getEditorUI(target);
        if (eui == null){
            return null;
        }
        GlyphGutter glyph = new GlyphGutter(eui);
        eui.setGlyphGutter(glyph);
        return glyph;
    }
    
    private int getLineFromMouseEvent(MouseEvent e){
        int line = -1;
        if (editorUI != null) {
            try{
                JTextComponent component = editorUI.getComponent();
                BaseTextUI textUI = (BaseTextUI)component.getUI();
                int clickOffset = textUI.viewToModel(component, new Point(0, e.getY()));
                line = Utilities.getLineOffset(doc, clickOffset);
            }catch (BadLocationException ble){
                ble.printStackTrace();
            }
        }
        return line;
    }
    
    class GutterMouseListener extends MouseAdapter implements MouseMotionListener {
        
        /** start line of the dragging. */
        private int dragStartLine;
        /** end line of the dragging. */
        private int dragEndLine;
        /** end line of last selection. */
        private int currentEndLine;
        /** If true, the selection goes forwards. */
        private boolean selectForward;

        public @Override void mouseClicked(MouseEvent e) {
            if (editorUI==null)
                return;
            // cycling button was clicked by left mouse button
            if (e.getModifiers() == InputEvent.BUTTON1_MASK) {
                if (isMouseOverCycleButton(e)) {
                    int line = getLineFromMouseEvent(e);
                    e.consume();
                    annos.activateNextAnnotation(line);
                } else {
                    Action actions[] = ImplementationProvider.getDefault().getGlyphGutterActions(editorUI.getComponent());
                    if (actions != null && actions.length >0) {
                        Action a = actions[0]; //TODO - create GUI chooser
                        if (a!=null && a.isEnabled()){
                            int currentLine = -1;
                            int line = getLineFromMouseEvent(e);
                            if (line == -1) return;
                            try {
                                currentLine = Utilities.getLineOffset(doc, editorUI.getComponent().getCaret().getDot());
                            } catch (BadLocationException ex) {
                                return;
                            }
                            if (line != currentLine) {
                                int offset = Utilities.getRowStartFromLineOffset(doc, line);
                                JumpList.checkAddEntry();
                                editorUI.getComponent().getCaret().setDot(offset);
                            }
                            e.consume();
                            a.actionPerformed(new ActionEvent(editorUI.getComponent(), 0, ""));
                            repaint();
                        }
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
        }

        private void showPopup(MouseEvent e) {
            if (editorUI == null)
                return;
            // annotation glyph was clicked by right mouse button
            if (e.isPopupTrigger()) {
                int line = getLineFromMouseEvent(e);
                int offset;
                if (annos.getActiveAnnotation(line) != null)
                    offset = annos.getActiveAnnotation(line).getOffset();
                else
                    offset = Utilities.getRowStartFromLineOffset(doc, line);
                if (editorUI.getComponent().getCaret().getDot() != offset)
                    JumpList.checkAddEntry();
                editorUI.getComponent().getCaret().setDot(offset);
                JPopupMenu pm = annos.createPopupMenu(Utilities.getKit(editorUI.getComponent()), line);
                if (pm != null) {
                    e.consume();
                    pm.show(GlyphGutter.this, e.getX(), e.getY());
                    pm.addPopupMenuListener( new PopupMenuListener() {
                            public void popupMenuCanceled(PopupMenuEvent e2) {
                                editorUI.getComponent().requestFocus();
                            }
                            public void popupMenuWillBecomeInvisible(PopupMenuEvent e2) {
                                editorUI.getComponent().requestFocus();
                            }
                            public void popupMenuWillBecomeVisible(PopupMenuEvent e2) {
                            }
                        });
                }
            }
        }
        
        public @Override void mouseReleased(MouseEvent e) {
            showPopup(e);
            if (!e.isConsumed() && (isMouseOverGlyph(e) || isMouseOverCycleButton(e))) {
                e.consume();
            }
        }
        
        public @Override void mousePressed (MouseEvent e) {
            showPopup(e);
            if (!e.isConsumed() && (isMouseOverGlyph(e) || isMouseOverCycleButton(e))) {
                e.consume();
            }
            // "click gutter selects line" functionality was disabled
//            // only react when it is not a cycling button
//            if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
//                if (! isMouseOverCycleButton(e)) {
//                    dragStartLine = (int)( (float)e.getY() / (float)lineHeight );
//                    updateSelection (true);
//                }
//            }
        }
        
        public void mouseDragged(MouseEvent e) {
            // "click gutter selects line" functionality was disabled
//            dragEndLine = (int)( (float)e.getY() / (float)lineHeight );
//            updateSelection (false);
        }
        
        public void mouseMoved(MouseEvent e) {}
        
//        /** Updates the selection */
//        private void updateSelection (boolean newSelection) {
//            if (editorUI == null)
//                return ;
//            javax.swing.text.JTextComponent comp = Utilities.getLastActiveComponent ();
//            try {
//                if (newSelection) {
//                    selectForward = true;
//                    // try to get the startOffset. In case of -1 it is most
//                    // likely the end of the document
//                    int rowStart = Utilities.getRowStartFromLineOffset (doc, dragStartLine);
//                    if (rowStart < 0) {
//                        rowStart = Utilities.getRowStart (doc, doc.getLength ());
//                        dragStartLine = Utilities.getLineOffset (doc, rowStart);
//                    }
//                    comp.setCaretPosition (rowStart);
//                    int offSet = Utilities.getRowEnd (doc, rowStart);
//                    if (offSet < doc.getLength()) {
//                        offSet = offSet + 1;
//                    }
//                    comp.moveCaretPosition (offSet);
//                    currentEndLine = dragEndLine = dragStartLine;
//                } else {
//                    if (currentEndLine == dragEndLine) return;
//                    // select backwards
//                    if (dragEndLine < dragStartLine) {
//                        if (selectForward) {
//                            // selection start should be at start of (dragLine + 1)
//                            int offSet = Utilities.getRowStartFromLineOffset (doc, dragStartLine + 1);
//                            if (offSet < 0) {
//                                offSet = Utilities.getRowEnd (doc, Utilities.getRowStartFromLineOffset (doc, dragStartLine));
//                            }
//                            comp.setCaretPosition (offSet);
//                            selectForward = false;
//                        }
//                        int rowStart = Utilities.getRowStartFromLineOffset (doc, dragEndLine);
//                        if (rowStart < 0) rowStart = 0;
//                        comp.moveCaretPosition (rowStart);
//                    }
//                    // select forwards
//                    else {
//                        if (! selectForward) {
//                            // select start should be at dragStartLine
//                            comp.setCaretPosition (Utilities.getRowStartFromLineOffset (doc, dragStartLine));
//                            selectForward = true;
//                        }
//                        // try to get the begin of (endLine + 1)
//                        int offSet = Utilities.getRowStartFromLineOffset (doc, dragEndLine + 1);;
//                        // for last line or more -1 is returned, so set to docLength...
//                        if (offSet < 0) {
//                            offSet = doc.getLength ();
//                        }
//                        comp.moveCaretPosition (offSet);
//                    }
//                }
//                currentEndLine = dragEndLine;
//            } catch (BadLocationException ble) {
//                ErrorManager.getDefault().notify(ble);
//            }
//        }
    }

    class GlyphGutterFoldHierarchyListener implements FoldHierarchyListener{
    
        public GlyphGutterFoldHierarchyListener(){
        }
        
        public void foldHierarchyChanged(FoldHierarchyEvent evt) {
            repaint();
        }
    }
    
    /** Listening to EditorUI to properly deinstall attached listeners */
    class EditorUIListener implements PropertyChangeListener{
        public void propertyChange (PropertyChangeEvent evt) {
            if (evt!=null && EditorUI.COMPONENT_PROPERTY.equals(evt.getPropertyName())) {
                if (evt.getNewValue() == null){
                    // component deinstalled, lets uninstall all isteners
                    editorUI.removePropertyChangeListener(editorUIListener);
                    annos.removeAnnotationsListener(GlyphGutter.this);
                    foldHierarchy.removeFoldHierarchyListener(glyphGutterFoldHierarchyListener);
                    if (gutterMouseListener!=null){
                        removeMouseListener(gutterMouseListener);
                        removeMouseMotionListener(gutterMouseListener);
                    }
                    if (annoTypesListener !=null){
                        AnnotationTypes.getTypes().removePropertyChangeListener(annoTypesListener);
                    }
                    foldHierarchy.removeFoldHierarchyListener(glyphGutterFoldHierarchyListener);
                    foldHierarchy = null;
                    // Release document reference
                    doc = null;
                    editorUI.removePropertyChangeListener(this);
                    editorUI = null;
                    annos = null;
                }
            }
        }
    }

}
