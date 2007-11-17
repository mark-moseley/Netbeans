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

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.text.JTextComponent;
import javax.swing.text.Caret;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.plaf.TextUI;
import org.netbeans.modules.editor.lib.ColoringMap;
import org.openide.util.WeakListeners;

/**
* Editor UI for the component. All the additional UI features
* like advanced scrolling, info about fonts, abbreviations,
* keyword matching are based on this class.
*
* @author Miloslav Metelka
* @version 1.00
*/
public class EditorUI implements ChangeListener, PropertyChangeListener, SettingsChangeListener {

    private static final Logger LOG = Logger.getLogger(EditorUI.class.getName());
    
    public static final String OVERWRITE_MODE_PROPERTY = "overwriteMode"; // NOI18N

    public static final String COMPONENT_PROPERTY = "component"; // NOI18N

    /** Default scrolling type is used for the standard
    * setDot() call. If the area is on the screen, it
    * jumps to it, otherwise it centers the requested area
    * vertically in the middle of the window and it uses
    * smallest covering on the right side.
    */
    public static final int SCROLL_DEFAULT = 0;

    /** Scrolling type used for regular caret moves.
    * The scrollJump is used when the caret requests area outside the screen.
    */
    public static final int SCROLL_MOVE = 1;

    /** Scrolling type where the smallest covering
    * for the requested rectangle is used. It's useful
    * for going to the end of the line for example.
    */
    public static final int SCROLL_SMALLEST = 2;

    /** Scrolling type for find operations, that can
    * request additional configurable area in each
    * direction, so the context around is visible too.
    */
    public static final int SCROLL_FIND = 3;


    private static final Insets NULL_INSETS = new Insets(0, 0, 0, 0);
    
    private static final Insets DEFAULT_INSETS = new Insets(0, SettingsDefaults.defaultTextLeftMarginWidth.intValue(), 0, 0);    

    private static final Dimension NULL_DIMENSION = new Dimension(0, 0);

    /** Default margin on the left and right side of the line number */
    public static final Insets defaultLineNumberMargin = new Insets(0, 3, 0, 3);

    /** Component this extended UI is related to. */
    private JTextComponent component;

    private JComponent extComponent;
    
    private JToolBar toolBarComponent;

    /** Property change support for firing property changes */
    PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /** Document for the case ext ui is constructed without the component */
    private BaseDocument printDoc;

    /** Draw layer chain */
    private DrawLayerList drawLayerList = new DrawLayerList();

    /** Map holding the [name, coloring] pairs */
    private ColoringMap coloringMap;

    /** Character (or better line) height. Particular view can use a different
    * character height however most views will probably use this one.
    */
    private int lineHeight = 1; // prevent possible division by zero

    private float lineHeightCorrection = 1.0f;

    /** Ascent of the line which is maximum ascent of all the fonts used. */
    private int lineAscent;

    /** Width of the space in the default coloring's font */
    int defaultSpaceWidth = 1;

    /** Should the search words be colored? */
    boolean highlightSearch;
    
    /** Enable displaying line numbers. Both this flag and <tt>lineNumberVisibleSetting</tt>
    * must be true to have the line numbers visible in the window. This flag is false
    * by default. It's turned on automatically if the getExtComponent is called.
    */
    boolean lineNumberEnabled;

    /** This flag corresponds to the LINE_NUMBER_VISIBLE setting. */
    boolean lineNumberVisibleSetting;

    /** Whether to show line numbers or not. This flag is obtained using bitwise AND
    * operation on lineNumberEnabled flag and lineNumberVisibleSetting flag.
    */
    boolean lineNumberVisible;

    /** Line number total width with indentation. It includes left and right
    * line-number margins and lineNumberDigitWidth * lineNumberMaxDigitCount.
    */
    int lineNumberWidth;

    /** Width of one digit used for line numbering. It's based
    * on the information from the line coloring.
    */
    int lineNumberDigitWidth;

    /** Current maximum count of digits in line number */
    int lineNumberMaxDigitCount;

    /** Margin between the line-number bar and the text. */
    int textLeftMarginWidth;

    /** This is the full margin around the text. The left margin
    * is an addition of component's margin and lineNumberWidth 
    * and textLeftMarginWidth.
    */
    Insets textMargin = DEFAULT_INSETS;

    /** How much columns/lines to add when the scroll is performed
    * so that the component is not scrolled so often.
    * Negative number means portion of the extent width/height
    */
    Insets scrollJumpInsets;

    /** How much columns/lines to add when the scroll is performed
    * so that the component is not scrolled so often.
    * Negative number means portion of the extent width/height
    */
    Insets scrollFindInsets;

    /** EditorUI properties */
    Hashtable props = new Hashtable(11);

    boolean textLimitLineVisible;

    Color textLimitLineColor;

    int textLimitWidth;

    private Rectangle lastExtentBounds = new Rectangle();

    private Dimension componentSizeIncrement = new Dimension();

    private Abbrev abbrev;

    private WordMatch wordMatch;

    private Object componentLock;

    /** Status bar */
    StatusBar statusBar;

    private FocusAdapter focusL;

    Map renderingHints;

    /** Glyph gutter used for drawing of annotation glyph icons. */
    private GlyphGutter glyphGutter = null;

    /** The line numbers can be shown in glyph gutter and therefore it is necessary 
     * to disable drawing of lines here. During the printing on the the other hand, line 
     * numbers must be visible. */
    private boolean disableLineNumbers = true;

    /** Left right corner of the JScrollPane */
    private JPanel glyphCorner;
    
    public static final String LINE_HEIGHT_CHANGED_PROP = "line-height-changed-prop"; //NOI18N

    /** init paste action #39678 */
    private static boolean isPasteActionInited = false;

    /** Construct extended UI for the use with a text component */
    public EditorUI() {
        Settings.addSettingsChangeListener(this);

        focusL = new FocusAdapter() {
                     public void focusGained(FocusEvent evt) {
                         Registry.activate(getComponent());
                         /* Fix of #25475 - copyAction's enabled flag
                          * must be updated on focus change
                          */
                         stateChanged(null);
                         if (component!=null){
                            BaseTextUI ui = (BaseTextUI)component.getUI();
                            if (ui!=null) ui.refresh();
                         }
                     }
                 };

        HighlightingDrawLayer.hookUp(this);
    }

    /** Construct extended UI for printing the given document */
    public EditorUI(BaseDocument printDoc) {
        this(printDoc, true, true);
    }
        
    /**
     * Construct extended UI for printing the given document
     * and specify which set of colors should be used.
     *
     * @param printDoc document that should be printed.
     * @param usePrintColoringMap Ignored.
     *  of the regular ones.
     * @param lineNumberEnabled if set to false the line numbers will not be printed.
     *  If set to true the visibility of line numbers depends on lineNumberVisibleSetting.
     */
    public EditorUI(BaseDocument printDoc, boolean usePrintColoringMap, boolean lineNumberEnabled) {
        this.printDoc = printDoc;

        settingsChange(null);

        setLineNumberEnabled(lineNumberEnabled);

        updateLineNumberWidth(0);

        drawLayerList.add(printDoc.getDrawLayerList());
        HighlightingDrawLayer.hookUp(this);
    }
    
    /**
     * Tries to gather all colorings defined for the kit's mime type. If the
     * mime type can't be determined from the editor kit's class passed in, this
     * method will try to load colorings defined for the empty mime path (ie. all
     * languages).
     * 
     * @param kitClass The kit class for which the colorings should be loaded.
     * 
     * @return The map with all colorings defined for the mime type of the editor
     *   kit class passed in. The returned map may be inaccurate, please use 
     *   the new Editor Settings API and its <code>FontColorSettings</code> class.
     * 
     * @deprecated Use Editor Settings API instead.
     */
    protected static Map<String, Coloring> getSharedColoringMap(Class kitClass) {
        String mimeType = BaseKit.kitsTracker_FindMimeType(kitClass);
        return ColoringMap.get(mimeType).getMap();
    }

    /**
     * A workaround method to initialize lineHeight variable
     * so that the DrawEngineLineView can use it.
     */
    void initLineHeight(JTextComponent c) {
        // Initialize lineHeight variable from the given component
        updateLineHeight(c);
    }

    /** Called when the <tt>BaseTextUI</tt> is being installed
    * into the component.
    */
    protected void installUI(JTextComponent c) {
        
        // Initialize the coloring map
        String mimeType = Utilities.getMimeType(c);
        this.coloringMap = ColoringMap.get(mimeType);
        this.coloringMap.addPropertyChangeListener(WeakListeners.propertyChange(this, coloringMap));
        
        synchronized (getComponentLock()) {
            this.component = c;
            
            putProperty(COMPONENT_PROPERTY, c);

            // listen on component
            component.addPropertyChangeListener(this);
            component.addFocusListener(focusL);

            // listen on caret
            Caret caret = component.getCaret();
            if (caret != null) {
                caret.addChangeListener(this);
            }

            BaseDocument doc = getDocument();
            if (doc != null) {
                modelChanged(null, doc);
            }
        }

        // Make sure all the things depending on non-null component will be updated
        settingsChange(null);
        
        // fix for issue #16352
        getDefaultColoring().apply(component);
        
        // enable drag and drop feature
        component.setDragEnabled(true);
    }

    /** Called when the <tt>BaseTextUI</tt> is being uninstalled
    * from the component.
    */
    protected void uninstallUI(JTextComponent c) {
        synchronized (getComponentLock()) {
            
            // fix for issue 12996
            if (component != null) {
                
                // stop listening on caret
                Caret caret = component.getCaret();
                if (caret != null) {
                    caret.removeChangeListener(this);
                }

                // stop listening on component
                component.removePropertyChangeListener(this);
                component.removeFocusListener(focusL);
            
            }

            BaseDocument doc = getDocument();
            if (doc != null) {
                modelChanged(doc, null);
            }

            component = null;
            putProperty(COMPONENT_PROPERTY, null);

            // Clear the font-metrics cache
            FontMetricsCache.clear();
        }

        // destroy the coloring map
        if (coloringMap != null) {
            coloringMap.removePropertyChangeListener(this);
            coloringMap = null;
        }
    }

    /** Get the lock assuring the component will not be changed
    * by <tt>installUI()</tt> or <tt>uninstallUI()</tt>.
    * It's useful for the classes that want to listen for the
    * component change in <tt>EditorUI</tt>.
    */
    public Object getComponentLock() {
        if (componentLock == null) {
            componentLock = new ComponentLock();
        }
        return componentLock;
    }
    static class ComponentLock {};

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, l);
    }

    protected final void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    protected void settingsChangeImpl(String settingName){
        Class kitClass = getKitClass();

        if (settingName == null || SettingsNames.LINE_NUMBER_VISIBLE.equals(settingName)) {
            lineNumberVisibleSetting = SettingsUtil.getBoolean(kitClass, 
                                           SettingsNames.LINE_NUMBER_VISIBLE,
                                           SettingsDefaults.defaultLineNumberVisible);
            lineNumberVisible = lineNumberEnabled && lineNumberVisibleSetting;
            
            // if this is printing, the drawing of original line numbers must be enabled
            if (component == null)
                disableLineNumbers = false;
            
            if (disableLineNumbers)
                lineNumberVisible = false;
        }

        BaseDocument doc = getDocument();
        if (doc != null) {

            if (settingName == null || SettingsNames.TEXT_LEFT_MARGIN_WIDTH.equals(settingName)) {
                textLeftMarginWidth = SettingsUtil.getInteger(kitClass,
                                      SettingsNames.TEXT_LEFT_MARGIN_WIDTH,
                                      SettingsDefaults.defaultTextLeftMarginWidth);
            }

            if (settingName == null || SettingsNames.LINE_HEIGHT_CORRECTION.equals(settingName)) {
                Object value = Settings.getValue(kitClass, SettingsNames.LINE_HEIGHT_CORRECTION);
                if (!(value instanceof Float) || ((Float)value).floatValue() < 0) {
                    value = SettingsDefaults.defaultLineHeightCorrection;
                }

                float newLineHeightCorrection = ((Float)value).floatValue();
                if (newLineHeightCorrection != lineHeightCorrection){
                    lineHeightCorrection = newLineHeightCorrection;
                    updateLineHeight(getComponent());
                }
            }

            if (settingName == null || SettingsNames.TEXT_LIMIT_LINE_VISIBLE.equals(settingName)) {
                textLimitLineVisible = SettingsUtil.getBoolean(kitClass,
                                       SettingsNames.TEXT_LIMIT_LINE_VISIBLE, SettingsDefaults.defaultTextLimitLineVisible);
            }

            if (settingName == null || SettingsNames.TEXT_LIMIT_LINE_COLOR.equals(settingName)) {
                Object value = Settings.getValue(kitClass, SettingsNames.TEXT_LIMIT_LINE_COLOR);
                textLimitLineColor = (value instanceof Color) ? (Color)value
                                     : SettingsDefaults.defaultTextLimitLineColor;
            }

            if (settingName == null || SettingsNames.TEXT_LIMIT_WIDTH.equals(settingName)) {
                textLimitWidth = SettingsUtil.getPositiveInteger(kitClass,
                                 SettingsNames.TEXT_LIMIT_WIDTH, SettingsDefaults.defaultTextLimitWidth);
            }

            // component only properties
            if (component != null) {
                if (settingName == null || SettingsNames.SCROLL_JUMP_INSETS.equals(settingName)) {
                    Object value = Settings.getValue(kitClass, SettingsNames.SCROLL_JUMP_INSETS);
                    scrollJumpInsets = (value instanceof Insets) ? (Insets)value : NULL_INSETS;
                }

                if (settingName == null || SettingsNames.SCROLL_FIND_INSETS.equals(settingName)) {
                    Object value = Settings.getValue(kitClass, SettingsNames.SCROLL_FIND_INSETS);
                    scrollFindInsets = (value instanceof Insets) ? (Insets)value : NULL_INSETS;
                }

                if (settingName == null || SettingsNames.COMPONENT_SIZE_INCREMENT.equals(settingName)) {
                    Object value = Settings.getValue(kitClass, SettingsNames.COMPONENT_SIZE_INCREMENT);
                    componentSizeIncrement = (value instanceof Dimension) ? (Dimension)value : NULL_DIMENSION;
                }

                if (settingName == null || SettingsNames.RENDERING_HINTS.equals(settingName)) {
                    
                    //is this ever really not a Map?
                    Object userSetHints = Settings.getValue(kitClass, SettingsNames.RENDERING_HINTS);
                    renderingHints = (userSetHints instanceof Map && ((Map)userSetHints).size() > 0) ? (Map)userSetHints : null;
                }
                
                if (settingName == null || SettingsNames.CARET_COLOR_INSERT_MODE.equals(settingName)
                        || SettingsNames.CARET_COLOR_OVERWRITE_MODE.equals(settingName)
                   ) {
                    Boolean b = (Boolean)getProperty(OVERWRITE_MODE_PROPERTY);
                    Color caretColor;
                    if (b == null || !b.booleanValue()) {
                        Object value = Settings.getValue(kitClass, SettingsNames.CARET_COLOR_INSERT_MODE);
                        caretColor = (value instanceof Color) ? (Color)value
                                     : SettingsDefaults.defaultCaretColorInsertMode;

                    } else {
                        Object value = Settings.getValue(kitClass, SettingsNames.CARET_COLOR_OVERWRITE_MODE);
                        caretColor = (value instanceof Color) ? (Color)value
                                     : SettingsDefaults.defaultCaretColorOvwerwriteMode;
                    }

                    JTextComponent c = component;
                    if (caretColor != null && c != null) {
                        c.setCaretColor(caretColor);
                    }
                }


                Utilities.runInEventDispatchThread(
                    new Runnable() {
                        public void run() {
                            JTextComponent c = component;
                            if (c != null) {
                                BaseKit kit = Utilities.getKit(c);
                                if (kit != null) {
                                    c.setKeymap(kit.getKeymap());
                                    updateComponentProperties();

                                    ((BaseTextUI)c.getUI()).preferenceChanged(true, true);
                                }
                            }
                        }
                    }
                );
            }
        }
    }
    
    public void settingsChange(SettingsChangeEvent evt) {
        if (component != null) {
            if (Utilities.getKit(component) == null) {
                return; // prevent problems if not garbage collected and settings changed
            }
        }
        String settingName = (evt != null) ? evt.getSettingName() : null;
        settingsChangeImpl(settingName);
    }

    public void stateChanged(ChangeEvent evt) {
        SwingUtilities.invokeLater(
            new Runnable() {
                
                /** @return true if the document supports guarded sections
                 * and when either the caret is in guarded block
                 * or when selection spans any guarded block(s).
                 */
                private boolean isCaretGuarded(){
                    JTextComponent c = component;
                    BaseDocument bdoc = getDocument();
                    boolean inGuardedBlock = false;
                    if (bdoc instanceof GuardedDocument){
                        GuardedDocument gdoc = (GuardedDocument)bdoc;

                        boolean selectionSpansGuardedSection = false;
                        for (int i=c.getSelectionStart(); i<c.getSelectionEnd(); i++){
                            if (gdoc.isPosGuarded(i)){
                                selectionSpansGuardedSection = true;
                                break;
                            }
                        }
                        
                        inGuardedBlock = (gdoc.isPosGuarded(c.getCaretPosition()) ||
                            selectionSpansGuardedSection);
                    }
                    return inGuardedBlock;
                }
                
                public void run() {
                    JTextComponent c = component;
                    if (c != null && c.hasFocus()) { // do nothing if the component does not have focus, see #110715
                        BaseKit kit = Utilities.getKit(c);
                        if (kit != null) {
                            boolean isEditable = c.isEditable();
                            boolean selectionVisible = c.getCaret().isSelectionVisible();
                            boolean caretGuarded = isCaretGuarded();

                            Action a = kit.getActionByName(BaseKit.copyAction);
                            if (a != null) {
                                a.setEnabled(selectionVisible);
                            }

                            a = kit.getActionByName(BaseKit.cutAction);
                            if (a != null) {
                                a.setEnabled(selectionVisible && !caretGuarded && isEditable);
                            }

                            a = kit.getActionByName(BaseKit.removeSelectionAction);
                            if (a != null) {
                                a.setEnabled(selectionVisible && !caretGuarded && isEditable);
                            }
                            
                            a = kit.getActionByName(BaseKit.pasteAction);
                            if (a != null) {
                                if (!isPasteActionInited) {
                                    // #39678
                                    a.setEnabled(!a.isEnabled());
                                    isPasteActionInited = true;
                                }
                                a.setEnabled(!caretGuarded && isEditable);
                            }
                        }
                    }
                }
            }
        );
    }

    protected void modelChanged(BaseDocument oldDoc, BaseDocument newDoc) {
        if (oldDoc != null) {
            // remove all document layers
            drawLayerList.remove(oldDoc.getDrawLayerList());
        }

        if (newDoc != null) {
            settingsChange(null);

            // add all document layers
            drawLayerList.add(newDoc.getDrawLayerList());
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if ("document".equals(propName)) { // NOI18N
            BaseDocument oldDoc = (evt.getOldValue() instanceof BaseDocument)
                                  ? (BaseDocument)evt.getOldValue() : null;
            BaseDocument newDoc = (evt.getNewValue() instanceof BaseDocument)
                                  ? (BaseDocument)evt.getNewValue() : null;
            modelChanged(oldDoc, newDoc);

        } else if ("margin".equals(propName)) { // NOI18N
            updateTextMargin();

        } else if ("caret".equals(propName)) { // NOI18N
            if (evt.getOldValue() instanceof Caret) {
                ((Caret)evt.getOldValue()).removeChangeListener(this);
            }
            if (evt.getNewValue() instanceof Caret) {
                ((Caret)evt.getNewValue()).addChangeListener(this);
            }

        } else if ("enabled".equals(propName)) { // NOI18N
            if (!component.isEnabled()) {
                component.getCaret().setVisible(false);
            }
        } 
        
        if (propName == null || ColoringMap.PROP_COLORING_MAP.equals(propName)) {
            settingsChangeImpl(null);
        }
    }

    /**
     * @deprecated Use Editor Settings or Editor Settings Storage API instead.
     *   This method is never called.
     */
    protected Map createColoringMap() {
        return Collections.emptyMap();
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public int getLineAscent() {
        return lineAscent;
    }

    /**
     * Tries to gather all colorings defined for the component's mime type. If
     * this instance is not installed to any component the method will try to
     * load colorings for the empty mime path (ie. all languages).
     * 
     * @return The map with all colorings defined the mime type of this instance.
     *   The returned map may be inaccurate, please use the new Editor Settings
     *   API and its <code>FontColorSettings</code> class.
     * 
     * @deprecated Use Editor Settings API instead.
     */
    public Map<String, Coloring> getColoringMap() {
        // Return mutable map
        return new HashMap<String, Coloring>(getCMInternal());
    }

    private Map<String, Coloring> getCMInternal() {
        ColoringMap cm = coloringMap;
        if (cm != null) {
            return cm.getMap();
        } else {
            return ColoringMap.get(null).getMap();
        }
    }
    
    /**
     * Gets the default coloring. The default coloring is the one called
     * <code>FontColorNames.DEFAULT_COLORING</code> and defined for the empty
     * mime path (ie. all languages).
     * 
     * @deprecated Use Editor Settings API instead.
     */
    public Coloring getDefaultColoring() {
        Coloring c = getCMInternal().get(SettingsNames.DEFAULT_COLORING);
        return c == null ? SettingsDefaults.defaultColoring : c;
    }

    /**
     * Gets a coloring from the coloring map. This metod uses the coloring map
     * returned by <code>getColoringMap</code> to find a coloring by its name.
     * 
     * @param coloringName The name of the coloring to find.
     * 
     * @retrun The coloring or <code>null</code> if there is no coloring with the
     *   requested name.
     * @deprecated Use Editor Settings API instead.
     */
    public Coloring getColoring(String coloringName) {
        return getCMInternal().get(coloringName);
    }

    private void updateLineHeight(JTextComponent component) {
        if (component == null) {
            return;
        }
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Computing lineHeight for '" + Utilities.getMimeType(component) + "'"); //NOI18N
        }
        
        Map<String, Coloring> cm = getCMInternal();
        int maxHeight = 1;
        int maxAscent = 0;
        for(String coloringName : cm.keySet()) {
            if (SettingsNames.STATUS_BAR_COLORING.equals(coloringName)
                || SettingsNames.STATUS_BAR_BOLD_COLORING.equals(coloringName)) {
                //#57112
                continue;
            }
            
            Coloring c = cm.get(coloringName);
            
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Probing coloring '" + coloringName + "' : " + c);
            }
            
            if (c != null) {
                Font font = c.getFont();
                if (font != null && (c.getFontMode() & Coloring.FONT_MODE_APPLY_SIZE) != 0) {
                    FontMetrics fm = FontMetricsCache.getFontMetrics(font, component);
                    if (fm != null) {
                        if (LOG.isLoggable(Level.FINE)) {
                            if (maxHeight < fm.getHeight()) {
                                LOG.fine("Updating maxHeight from " //NOI18N
                                    + maxHeight + " to " + fm.getHeight() // NOI18N
                                    + ", coloringName=" + coloringName // NOI18N
                                    + ", font=" + font // NOI18N
                                );
                            }

                            if (maxAscent < fm.getAscent()) {
                                LOG.fine("Updating maxAscent from " //NOI18N
                                    + maxAscent + " to " + fm.getAscent() // NOI18N
                                    + ", coloringName=" + coloringName // NOI18N
                                    + ", font=" + font // NOI18N
                                );
                            }
                        }

                        maxHeight = Math.max(maxHeight, fm.getHeight());
                        maxAscent = Math.max(maxAscent, fm.getAscent());
                    }
                }
            }
        }

        boolean changePreferences = false;
        if (lineHeight!=1 && lineHeight!=(int)(maxHeight * lineHeightCorrection)){
            changePreferences = true;
        }

        int oldProp = lineHeight;
        
        // Apply lineHeightCorrection
        lineHeight = (int)(maxHeight * lineHeightCorrection);
        lineAscent = (int)(maxAscent * lineHeightCorrection);
        if (changePreferences) {
            firePropertyChange(LINE_HEIGHT_CHANGED_PROP, new Integer(oldProp), new Integer(lineHeight));
        }

    }
    
    /**
     * Update various properties of the component in AWT thread.
     */
    void updateComponentProperties() {
        Class kitClass = Utilities.getKitClass(component);

        // Set the margin
        if (kitClass != null) {
            Object value = Settings.getValue(kitClass, SettingsNames.MARGIN);
            Insets margin = (value instanceof Insets) ? (Insets)value : null;
            component.setMargin(margin);
        }

        // Apply the default coloring to the component
        getDefaultColoring().apply(component);

        lineNumberDigitWidth = computeLineNumberDigitWidth();

        // Update line height
        updateLineHeight(getComponent());

        // Update space width of the default coloring's font
        FontMetricsCache.Info fmcInfo = FontMetricsCache.getInfo(getDefaultColoring().getFont());
        defaultSpaceWidth = fmcInfo.getSpaceWidth(component);

        updateLineNumberWidth(0);

        // update glyph gutter colors and fonts
        if (isGlyphGutterVisible()) {
            glyphGutter.update();
            updateScrollPaneCornerColor();
        }
    }
    
    protected void update(Graphics g) {
        // Possibly apply the rendering hints
        if (renderingHints != null) {
            ((Graphics2D)g).setRenderingHints(renderingHints);
        }
    }

    public final JTextComponent getComponent() {
        return component;
    }

    /** Get the document to work on. Either component's document or printed document
    * is returned. It can return null in case the component's document is not instance
    * of BaseDocument.
    */
    public final BaseDocument getDocument() {
        return (component != null) ? Utilities.getDocument(component) : printDoc;
    }

    private Class getKitClass() {
        return (component != null) ? Utilities.getKitClass(component)
               : ((printDoc != null) ? printDoc.getKitClass() : null);
    }

    public Object getProperty(Object key) {
        return props.get(key);
    }

    public void putProperty(Object key, Object value) {
        Object oldValue;
        if (value != null) {
            oldValue = props.put(key, value);
        } else {
            oldValue = props.remove(key);
        }
        firePropertyChange(key.toString(), oldValue, value);
    }

    /** Get extended editor component.
     * The extended component should normally be used
     * for editing files instead of just the JEditorPane
     * because it offers status bar and possibly
     * other useful components.
     * <br>
     * The component no longer includes toolbar - it's returned
     * by a separate method {@link #getToolbarComponent()}.
     * <br>
     * The getExtComponent() should not be used when
     * the JEditorPane is included in dialog.
     * @see #hasExtComponent()
     */
    public JComponent getExtComponent() {
        if (extComponent == null) {
            if (component != null) {
                extComponent = createExtComponent();
            }
        }
        return extComponent;
    }

    /**
     * Get the toolbar component appropriate for this editor.
     */
    public JToolBar getToolBarComponent() {
        if (toolBarComponent == null) {
            if (component != null) {
                toolBarComponent = createToolBarComponent();
            }
        }
        return toolBarComponent;
    }
    
    /**
     * Construct the toolbar component appropriate for this editor.
     * 
     * @return non-null toolbar component or null if there is no appropriate
     *  toolbar component for this editor.
     */
    protected JToolBar createToolBarComponent() {
       return null; 
    }

    protected void initGlyphCorner(JScrollPane scroller){
        glyphCorner = new JPanel();
        updateScrollPaneCornerColor();
        scroller.setCorner(JScrollPane.LOWER_LEFT_CORNER, glyphCorner);
    }
    
    protected void setGlyphGutter(GlyphGutter gutter){
        glyphGutter = gutter;
    }
    
    public final int getSideBarWidth(){
        JScrollPane scroll = (JScrollPane)SwingUtilities.getAncestorOfClass(JScrollPane.class, getParentViewport());
        if (scroll!=null && scroll.getRowHeader()!=null){
            Rectangle bounds = scroll.getRowHeader().getBounds();
            if (bounds!=null){
                return bounds.width;
            }
        }
        return 40;
    }
    
    protected JComponent createExtComponent() {
        setLineNumberEnabled(true); // enable line numbering

        // extComponent will be a panel
        JComponent ec = new JPanel(new BorderLayout());
        ec.putClientProperty(JTextComponent.class, component);

        // Add the scroll-pane with the component to the center
        JScrollPane scroller = new JScrollPane(component);
        scroller.getViewport().setMinimumSize(new Dimension(4,4));
        
        // remove default scroll-pane border, winsys will handle borders itself           
        scroller.setBorder(null);
        
        setGlyphGutter(new GlyphGutter(this));
        scroller.setRowHeaderView(glyphGutter);

        initGlyphCorner(scroller);
        
        ec.add(scroller);

        // Install the status-bar panel to the bottom
        ec.add(getStatusBar().getPanel(), BorderLayout.SOUTH);
        
        return ec;
    }
    
    /** Whether this ui uses extComponent or not.
     * @see #getExtComponent()
     */
    public boolean hasExtComponent() {
        return (extComponent != null);
    }

    /** @deprecated Use Editor Code Templates API instead. */
    public Abbrev getAbbrev() {
        if (abbrev == null) {
            abbrev = new Abbrev(this, true, true);
        }
        return abbrev;
    }

    public WordMatch getWordMatch() {
        if (wordMatch == null) {
            wordMatch = new WordMatch(this);
        }
        return wordMatch;
    }

    public StatusBar getStatusBar() {
        if (statusBar == null) {
            statusBar = new StatusBar(this);
        }
        return statusBar;
    }
    

    final DrawLayerList getDrawLayerList() {
        return drawLayerList;
    }

    /** 
     * Find the layer with some layer name in the layer hierarchy.
     * 
     * <p>Using of <code>DrawLayer</code>s has been deprecated.
     * 
     * @deprecated Please use Highlighting SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    public DrawLayer findLayer(String layerName) {
        return drawLayerList.findLayer(layerName);
    }

    /** 
     * Add new layer and use its priority to position it in the chain.
     * If there's the layer with same visibility then the inserted layer
     * will be placed after it.
     *
     * <p>Using of <code>DrawLayer</code>s has been deprecated.
     * 
     * @param layer layer to insert into the chain
     * 
     * @deprecated Please use Highlighting SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    public boolean addLayer(DrawLayer layer, int visibility) {
        return drawLayerList.add(layer, visibility);
    }

    /**
     * Using of <code>DrawLayer</code>s has been deprecated.
     * 
     * @deprecated Please use Highlighting SPI instead, for details see
     *   <a href="@org-netbeans-modules-editor-lib2@/overview-summary.html">Editor Library 2</a>.
     */
    public DrawLayer removeLayer(String layerName) {
        return drawLayerList.remove(layerName);
    }

    public void repaint(int startY) {
        repaint(startY, component.getHeight());
    }

    public void repaint(int startY, int height) {
        if (height <= 0) {
            return;
        }
        int width = Math.max(component.getWidth(), 0);
        startY = Math.max(startY, 0);
        component.repaint(0, startY, width, height);
    }

    public void repaintOffset(int pos) throws BadLocationException {
        repaintBlock(pos, pos);
    }

    /** Repaint the block between the given positions. */
    public void repaintBlock(int startPos, int endPos)
    throws BadLocationException {
        BaseTextUI ui = (BaseTextUI)component.getUI();
        if (startPos > endPos) { // swap
            int tmpPos = startPos;
            startPos = endPos;
            endPos = tmpPos;
        }
        
        int yFrom;
        int yTo;

        try {
            yFrom = ui.getYFromPos(startPos);
        } catch (BadLocationException e) {
            Utilities.annotateLoggable(e);
            yFrom = 0;
        }
        
        try {
            yTo = ui.getYFromPos(endPos) + lineHeight;
        } catch (BadLocationException e) {
            Utilities.annotateLoggable(e);
            yTo = (int) ui.getRootView(component).getPreferredSpan(View.Y_AXIS);
        }
        
        repaint(yFrom, yTo - yFrom);
    }

    /** Is the parent of some editor component a viewport */
    private JViewport getParentViewport() {
        Component pc = component.getParent();
        return (pc instanceof JViewport) ? (JViewport)pc : null;
    }

    /** Finds the frame - parent of editor component */
    public static Frame getParentFrame(Component c) {
        do {
            c = c.getParent();
            if (c instanceof Frame) {
                return (Frame)c;
            }
        } while (c != null);
        return null;
    }

    /** Possibly update virtual width. If the width
    * is really updated, the method returns true.
    * @deprecated virtual size is no longer used and effects of this method are ignored
    */
    public boolean updateVirtualWidth(int width) {
        return false;
    }

    /** Possibly update virtual height. If the height
    * is really updated, the method returns true. There is
    * a slight difference against virtual width in that
    * if the height is shrinked too much the virtual height
    * is shrinked too.
    * 0 can be used to update to the real height.
    * @deprecated virtual size is no longer used and effects of this method are ignored
    */
    public boolean updateVirtualHeight(int height) {
        return false;
    }

    public boolean isLineNumberEnabled() {
        return lineNumberEnabled;
    }

    public void setLineNumberEnabled(boolean lineNumberEnabled) {
        this.lineNumberEnabled = lineNumberEnabled;
        lineNumberVisible = lineNumberEnabled && lineNumberVisibleSetting;
        if (disableLineNumbers)
            lineNumberVisible = false;
    }

    void setLineNumberVisibleSetting(boolean lineNumberVisibleSetting) {
        this.lineNumberVisibleSetting = lineNumberVisibleSetting;
    }
    
    /** Update the width that will be occupied by the line number.
    * @param maxDigitCount maximum digit count that can the line number have.
    *  if it's lower or equal to zero it will be computed automatically.
    */
    public void updateLineNumberWidth(int maxDigitCount) {
        int oldWidth = lineNumberWidth;

        if (lineNumberVisible) {
            try {
                if (maxDigitCount <= 0) {
                    BaseDocument doc = getDocument();
                    int lineCnt = Utilities.getLineOffset(doc, doc.getLength()) + 1;
                    maxDigitCount = Integer.toString(lineCnt).length();
                }

                if (maxDigitCount > lineNumberMaxDigitCount) {
                    lineNumberMaxDigitCount = maxDigitCount;
                }

            } catch (BadLocationException e) {
                lineNumberMaxDigitCount = 1;
            }
            lineNumberWidth = lineNumberMaxDigitCount * lineNumberDigitWidth;
            Insets lineMargin = getLineNumberMargin();
            if (lineMargin != null) {
                lineNumberWidth += lineMargin.left + lineMargin.right;
            }

        } else {
            lineNumberWidth = 0;
        }

        updateTextMargin();
        if (oldWidth != lineNumberWidth) { // changed
            if (component != null) {
                component.repaint();
            }
        }
    }

    public void updateTextMargin() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        updateTextMargin();
                    }
                }
            );
        }

        Insets orig = textMargin;
        Insets cm = (component != null) ? component.getMargin() : null;
        int leftWidth = lineNumberWidth + textLeftMarginWidth;
        if (cm != null) {
            textMargin = new Insets(cm.top, cm.left + leftWidth,
                                    cm.bottom, cm.right);
        } else {
            textMargin = new Insets(0, leftWidth, 0, 0);
        }
        if (orig.top != textMargin.top || orig.bottom != textMargin.bottom) {
            ((BaseTextUI)component.getUI()).invalidateStartY();
        }
    }

    public Rectangle getExtentBounds() {
        return getExtentBounds(null);
    }

    /** Get position of the component extent. The (x, y) are set to (0, 0) if there's
    * no viewport or (-x, -y) if there's one.
    */
    public Rectangle getExtentBounds(Rectangle r) {
        if (r == null) {
            r = new Rectangle();
        }
        if (component != null) {
            JViewport port = getParentViewport();
            if (port != null) {
                Point p = port.getViewPosition();
                r.width = port.getWidth();
                r.height = port.getHeight();
                r.x = p.x;
                r.y = p.y;
            } else { // no viewport
                r.setBounds(component.getVisibleRect());
            }
        }
        return r;
    }

    /** Get the begining of the area covered by text */
    public Insets getTextMargin() {
        return textMargin;
    }

    /**
     * Scroll the editor window so that the given rectangle is visible.
     *
     * @param r rectangle to which the editor window should be scrolled.
     * @param scrollPolicy the way how scrolling should be done.
     *  One of <code>EditorUI.SCROLL_*</code> constants.
     *
     * @deprecated use <code>JComponent.scrollRectToVisible()</code> instead of this method.
     */
    public void scrollRectToVisible(final Rectangle r, final int scrollPolicy) {
        Utilities.runInEventDispatchThread(
            new Runnable() {
                public void run() {
                    scrollRectToVisibleFragile(r, scrollPolicy);
                }
            }
        );
    }

    /** Must be called with EventDispatchThread */
    boolean scrollRectToVisibleFragile(Rectangle r, int scrollPolicy) {
        Insets margin = getTextMargin();
        Rectangle bounds = getExtentBounds();
        r = new Rectangle(r); // make copy of orig rect
        r.x -= margin.left;
        r.y -= margin.top;
        bounds.width -= margin.left + margin.right;
        bounds.height -= margin.top + margin.bottom;
        return scrollRectToVisibleImpl(r, scrollPolicy, bounds);
    }

    /** Scroll the view so that requested rectangle is best visible.
     * There are different scroll policies available.
     * @return whether the extent has to be scrolled in any direction.
     */
    private boolean scrollRectToVisibleImpl(Rectangle r, int scrollPolicy,
                                            Rectangle bounds) {
        if (bounds.width <= 0 || bounds.height <= 0) {
            return false;
        }

        // handle find scrolling specifically
        if (scrollPolicy == SCROLL_FIND) {
            // converted inset
            int cnvFI = (scrollFindInsets.left < 0)
                ? (- bounds.width * scrollFindInsets.left / 100)
                : scrollFindInsets.left * defaultSpaceWidth;

            int nx = Math.max(r.x - cnvFI, 0);
            
            cnvFI = (scrollFindInsets.right < 0)
                ? (- bounds.width * scrollFindInsets.right / 100)
                : scrollFindInsets.right * defaultSpaceWidth;

            r.width += (r.x - nx) + cnvFI;
            r.x = nx;

            cnvFI = (scrollFindInsets.top < 0)
                ? (- bounds.height * scrollFindInsets.top / 100)
                : scrollFindInsets.top * lineHeight;

            int ny = Math.max(r.y - cnvFI, 0);

            cnvFI = (scrollFindInsets.bottom < 0)
                ? (- bounds.height * scrollFindInsets.bottom / 100)
                : scrollFindInsets.bottom * lineHeight;

            r.height += (r.y - ny) + cnvFI;
            r.y = ny;

            return scrollRectToVisibleImpl(r, SCROLL_SMALLEST, bounds); // recall
        }
        
        int viewWidth = (int)((TextUI)component.getUI()).getRootView(component).getPreferredSpan(View.X_AXIS);
        int viewHeight = (int)((TextUI)component.getUI()).getRootView(component).getPreferredSpan(View.Y_AXIS);
        
        // r must be within virtualSize's width
        if (r.x + r.width > viewWidth) {
            r.x = viewWidth - r.width;
            if (r.x < 0) {
                r.x = 0;
                r.width = viewWidth;
            }
            return scrollRectToVisibleImpl(r, scrollPolicy, bounds); // recall
        }
        // r must be within virtualSize's height
        if (r.y +r.height  > viewHeight) {
            r.y = viewHeight - r.height;
            if (r.y < 0) {
                r.y = 0;
                r.height = viewHeight;
            }
            return scrollRectToVisibleImpl(r, scrollPolicy, bounds);
        }

        // if r extends bounds dimension it must be corrected now
        if (r.width > bounds.width || r.height > bounds.height) {
            try {
                Rectangle caretRect = component.getUI().modelToView(
                                            component, component.getCaret().getDot(), Position.Bias.Forward);
                if (caretRect.x >= r.x
                        && caretRect.x + caretRect.width <= r.x + r.width
                        && caretRect.y >= r.y
                        && caretRect.y + caretRect.height <= r.y + r.height
                   ) { // caret inside requested rect
                    // move scroll rect for best caret visibility
                    int overX = r.width - bounds.width;
                    int overY = r.height - bounds.height;
                    if (overX > 0) {
                        r.x -= overX * (caretRect.x - r.x) / r.width;
                    }
                    if (overY > 0) {
                        r.y -= overY * (caretRect.y - r.y) / r.height;
                    }
                }
                r.height = bounds.height;
                r.width = bounds.width; // could be different algorithm
                return scrollRectToVisibleImpl(r, scrollPolicy, bounds);            
            } catch (BadLocationException ble){
                ble.printStackTrace();
            }
        }

        int newX = bounds.x;
        int newY = bounds.y;
        boolean move = false;
        // now the scroll rect is within bounds of the component
        // and can have size of the extent at maximum
        if (r.x < bounds.x) {
            move = true;
            switch (scrollPolicy) {
            case SCROLL_MOVE:
                newX = (scrollJumpInsets.left < 0)
                       ? (bounds.width * (-scrollJumpInsets.left) / 100)
                       : scrollJumpInsets.left * defaultSpaceWidth;
                newX = Math.min(newX, bounds.x + bounds.width - (r.x + r.width));
                newX = Math.max(r.x - newX, 0); // new bounds.x
                break;
            case SCROLL_DEFAULT:
            case SCROLL_SMALLEST:
            default:
                newX = r.x;
                break;
            }
            updateVirtualWidth(newX + bounds.width);
        } else if (r.x + r.width > bounds.x + bounds.width) {
            move = true;
            switch (scrollPolicy) {
            case SCROLL_SMALLEST:
                newX = r.x + r.width - bounds.width;
                break;
            default:
                newX = (scrollJumpInsets.right < 0)
                       ? (bounds.width * (-scrollJumpInsets.right) / 100 )
                       : scrollJumpInsets.right * defaultSpaceWidth;
                newX = Math.min(newX, bounds.width - r.width);
                newX = (r.x + r.width) + newX - bounds.width;
                break;
            }

            updateVirtualWidth(newX + bounds.width);
        }

        if (r.y < bounds.y) {
            move = true;
            switch (scrollPolicy) {
            case SCROLL_MOVE:
                newY = r.y;
                newY -= (scrollJumpInsets.top < 0)
                        ? (bounds.height * (-scrollJumpInsets.top) / 100 )
                        : scrollJumpInsets.top * lineHeight;
                break;
            case SCROLL_SMALLEST:
                newY = r.y;
                break;
            case SCROLL_DEFAULT:
            default:
                newY = r.y - (bounds.height - r.height) / 2; // center
                break;
            }
            newY = Math.max(newY, 0);
        } else if (r.y + r.height > bounds.y + bounds.height) {
            move = true;
            switch (scrollPolicy) {
            case SCROLL_MOVE:
                newY = (r.y + r.height) - bounds.height;
                newY += (scrollJumpInsets.bottom < 0)
                        ? (bounds.height * (-scrollJumpInsets.bottom) / 100 )
                        : scrollJumpInsets.bottom * lineHeight;
                break;
            case SCROLL_SMALLEST:
                newY = (r.y + r.height) - bounds.height;
                break;
            case SCROLL_DEFAULT:
            default:
                newY = r.y - (bounds.height - r.height) / 2; // center
                break;
            }
            newY = Math.max(newY, 0);
        }

        if (move) {
            setExtentPosition(newX, newY);
        }
        return move;
    }

    void setExtentPosition(int x, int y) {
        JViewport port = getParentViewport();
        if (port != null) {
            Point p = new Point(Math.max(x, 0), Math.max(y, 0));
            port.setViewPosition(p);
        }
    }

    public void adjustWindow(int caretPercentFromWindowTop) {
        final Rectangle bounds = getExtentBounds();
        if (component != null && (component.getCaret() instanceof Rectangle)) {
            Rectangle caretRect = (Rectangle)component.getCaret();
            bounds.y = caretRect.y - (caretPercentFromWindowTop * bounds.height) / 100
                       + (caretPercentFromWindowTop * lineHeight) / 100;
            Utilities.runInEventDispatchThread(
                new Runnable() {
                    public void run() {
                        scrollRectToVisible(bounds, SCROLL_SMALLEST);
                    }
                }
            );
        }
    }

    /** Set the dot according to the currently visible screen window.
    * #param percentFromWindowTop percentage giving the distance of the caret
    *  from the top of the currently visible window.
    */
    public void adjustCaret(int percentFromWindowTop) {
        JTextComponent c = component;
        if (c != null) {
            Rectangle bounds = getExtentBounds();
            bounds.y += (percentFromWindowTop * bounds.height) / 100
                        - (percentFromWindowTop * lineHeight) / 100;
            try {
                int offset = ((BaseTextUI)c.getUI()).getPosFromY(bounds.y);
                if (offset >= 0) {
                    caretSetDot(offset, null, SCROLL_SMALLEST);
                }
            } catch (BadLocationException e) {
            }
        }
    }

    /** Set the position of the caret and scroll the extent if necessary.
     * @param offset position where the caret should be placed
     * @param scrollRect rectangle that should become visible. It can be null
     *   when no scrolling should be done.
     * @param scrollPolicy policy to be used when scrolling.
     * @deprecated
     */
    public void caretSetDot(int offset, Rectangle scrollRect, int scrollPolicy) {
        if (component != null) {
            Caret caret = component.getCaret();
            if (caret instanceof BaseCaret) {
                ((BaseCaret)caret).setDot(offset, scrollRect, scrollPolicy);
            } else {
                caret.setDot(offset);
            }
        }
    }

    /** Set the position of the caret and scroll the extent if necessary.
     * @param offset position where the caret should be placed
     * @param scrollRect rectangle that should become visible. It can be null
     *   when no scrolling should be done.
     * @param scrollPolicy policy to be used when scrolling.
     * @deprecated
     */
    public void caretMoveDot(int offset, Rectangle scrollRect, int scrollPolicy) {
        if (component != null) {
            Caret caret = component.getCaret();
            if (caret instanceof BaseCaret) {
                ((BaseCaret)caret).moveDot(offset, scrollRect, scrollPolicy);
            } else {
                caret.moveDot(offset);
            }
        }
    }

    /** This method is called by textui to do the paint.
    * It is forwarded either to paint through the image
    * and then copy the image area to the screen or to
    * paint directly to this graphics. The real work occurs
    * in draw-engine.
    */
    protected void paint(Graphics g) {
        if (component != null) { // component must be installed
            update(g);
        }
    }

    /** Returns the line number margin */
    public Insets getLineNumberMargin() {
        return defaultLineNumberMargin;
    }

    private int computeLineNumberDigitWidth(){
        // Handle line number fonts and widths
        Coloring dc = getDefaultColoring();        
        Coloring lnc = getCMInternal().get(SettingsNames.LINE_NUMBER_COLORING);
        if (lnc != null) {
            Font lnFont = lnc.getFont();
            if (lnFont == null) {
                lnFont = dc.getFont();
            }
            if (component == null) return lineNumberDigitWidth;
            FontMetrics lnFM = FontMetricsCache.getFontMetrics(lnFont, component);
            if (lnFM == null) return lineNumberDigitWidth;
            int maxWidth = 1;
            char[] digit = new char[1]; // will be used for '0' - '9'
            for (int i = 0; i <= 9; i++) {
                digit[0] = (char)('0' + i);
                maxWidth = Math.max(maxWidth, lnFM.charsWidth(digit, 0, 1));
            }
            return maxWidth;
        }
        return lineNumberDigitWidth;
    }
    
    /** Returns width of the one digit */
    public int getLineNumberDigitWidth() {
        return lineNumberDigitWidth;
    }

    /** Is glyph gutter created and visible for the document or not */
    public boolean isGlyphGutterVisible() {
        return glyphGutter != null;
    }
    
    public final GlyphGutter getGlyphGutter() {
        return glyphGutter;
    }
    
    protected void updateScrollPaneCornerColor() {
        Coloring lineColoring = getCMInternal().get(SettingsNames.LINE_NUMBER_COLORING);
        Coloring defaultColoring = getDefaultColoring();
        
        Color backgroundColor;
        if (lineColoring != null && lineColoring.getBackColor() != null) {
            backgroundColor = lineColoring.getBackColor();
        } else {
            backgroundColor = defaultColoring.getBackColor();
        }
        
        if (glyphCorner != null){
            glyphCorner.setBackground(backgroundColor);
        }
    }

    int textLimitWidth() {
        int ret = textLimitWidth;
        Object textLimitLine = component == null ? null : component.getClientProperty("TextLimitLine"); //NOI18N
        if (textLimitLine instanceof Integer) {
            ret = ((Integer)textLimitLine).intValue();
        }
        return ret;
    }

}
