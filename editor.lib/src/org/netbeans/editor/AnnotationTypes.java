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

import java.net.URL;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.net.MalformedURLException;
import org.netbeans.editor.Settings;
import org.netbeans.editor.WeakPropertyChangeSupport;
import javax.swing.SwingUtilities;
import java.util.Set;
import java.util.HashSet;

/** Registry of all annotation types. This is singleton and it also keeps
 * the settings whether the annotation types which are not active are
 * drawn on the background, whther the combining of annotations is
 * turned on or off etc. These settings are shared by all views.
 *
 * @author David Konecny
 * @since 08/2001
 */

public class AnnotationTypes {

    /** Whether the pasive glyphs are drawn on the background under the text (boolean). */
    public static final String PROP_BACKGROUND_DRAWING = "backgroundDrawing"; // NOI18N

    /** The alpha of the pasive glyphs drawn on the background under the text (int 0..100%). */
    public static final String PROP_BACKGROUND_GLYPH_ALPHA = "backgroundGlyphAlpha"; // NOI18N
    
    /** Whether the glyphs should be combined according to combination annotation types (boolean). */
    public static final String PROP_COMBINE_GLYPHS = "combineGlyphs"; // NOI18N

    /** Whether the glyph icon should be drawn over the  line numbers (boolean). */
    public static final String PROP_GLYPHS_OVER_LINE_NUMBERS = "glyphsOverLineNumbers"; // NOI18N

    /** Whether the glyph gutter should be shown after opening editor or not (boolean). 
     * If this value is set to False, the gutter will automatically show after first annotation 
     * has been added.*/
    public static final String PROP_SHOW_GLYPH_GUTTER = "showGlyphGutter"; // NOI18N

    /** Property which is fired when list of annotation types is changing. */
    public static final String PROP_ANNOTATION_TYPES = "annotationTypes"; // NOI18N
    
    /** Storage of all properties. */
    private Map properties;

    /** Support for property change listeners*/
    private WeakPropertyChangeSupport support;
    
    /** Static map containing all annotation types: annotation_name <-> annotation_type */
    private Map allTypes = null;
    
    /** Flag whether the annotation types were loaded or not */
    private boolean loadedTypes = false;

    /** Flag whether the properties of this class were loaded or not */
    private boolean loadedSettings = false;
    
    /** Flag whether the loading of properties is in progress */
    private boolean loadingInProgress = false;
    
    /** Loader for loading annotation types from a storage*/
    private Loader loader = null;
    
    /** URL to the default glyph icon */
    private static URL defaultGlyphIcon = null;

    /** Single instance of this class */
    private static AnnotationTypes annoTypes = null;
    
    private AnnotationTypes() {
        properties = new HashMap(5*4/3);
        support = new WeakPropertyChangeSupport();
    }

    /**  Returns instance of AnnotationTypes singleton. */
    public static AnnotationTypes getTypes() {
        if (annoTypes == null) {
            annoTypes = new AnnotationTypes();
        }
        return annoTypes;
    }
    
    /** Gets Image which represents the default glyph icon. It is 
     * used in case the annotation type does not have its own icon. */
    public static URL getDefaultGlyphURL() {
        if (defaultGlyphIcon == null) {
            try {
                // TODO: in standalone this will not work
                defaultGlyphIcon = new URL("nbresloc:/org/netbeans/editor/resources/defaultglyph.gif"); // NOI18N
            } catch (MalformedURLException ex) {
                Utilities.annotateLoggable(ex);
            }
        }
        
        return defaultGlyphIcon;
    }

    /** Getter for BackgroundDrawing property
     * @return  whether the background drawing should be used or not */    
    public Boolean isBackgroundDrawing() {
        loadSettings();
        
        Boolean b = (Boolean)getProp(PROP_BACKGROUND_DRAWING);
        if (b == null)
            return Boolean.FALSE;
        return b;
    }

    /** Setter for the BackgroundDrawing property
     * @param drawing use background drawing or not */    
    public void setBackgroundDrawing(Boolean drawing) {
        if (!isBackgroundDrawing().equals(drawing)) {
            putProp(PROP_BACKGROUND_DRAWING, drawing);
            firePropertyChange(PROP_BACKGROUND_DRAWING, null, null);
            // force repaint of all documents
            Settings.touchValue(null, null);
            saveSetting(PROP_BACKGROUND_DRAWING, drawing);
        }
    }
    
    /** Getter for CombineGlyphs property
     * @return  whether the combination annotation types are used or not */    
    public Boolean isCombineGlyphs() {
        loadSettings();
        
        Boolean b = (Boolean)getProp(PROP_COMBINE_GLYPHS);
        if (b == null)
            return Boolean.TRUE;
        return b;
    }

    /** Setter for the CombineGlyphs property
     * @param combine combine annotation types */    
    public void setCombineGlyphs(Boolean combine) {
        if (!isCombineGlyphs().equals(combine)) {
            putProp(PROP_COMBINE_GLYPHS, combine);
            firePropertyChange(PROP_COMBINE_GLYPHS, null, null);
            // force repaint of all documents
            Settings.touchValue(null, null);
            saveSetting(PROP_COMBINE_GLYPHS, combine);
        }
    }
    
    /** Getter for BackgroundGlyphAlpha property
     * @return percentage of alpha (0..100) */    
    public Integer getBackgroundGlyphAlpha() {
        loadSettings();
        
        if (getProp(PROP_BACKGROUND_GLYPH_ALPHA) == null)
            return new Integer(40);
        return (Integer)getProp(PROP_BACKGROUND_GLYPH_ALPHA);
    }

    /** Setter for the BackgroundGlyphAlpha property
     * @param alpha alpha value in percentage (0..100) */    
    public void setBackgroundGlyphAlpha(int alpha) {
        if (alpha < 0 || alpha > 100) {
            return;
        }
        Integer i = new Integer(alpha);
        putProp(PROP_BACKGROUND_GLYPH_ALPHA, i);
        firePropertyChange(PROP_BACKGROUND_GLYPH_ALPHA, null, null);
        // force repaint of all documents
        Settings.touchValue(null, null);
        saveSetting(PROP_BACKGROUND_GLYPH_ALPHA, i);
    }

    /** Getter for GlyphsOverLineNumbers property
     * @return  whether the glyph should be drawn over the line numbers */    
    public Boolean isGlyphsOverLineNumbers() {
        loadSettings();
        
        Boolean b = (Boolean)getProp(PROP_GLYPHS_OVER_LINE_NUMBERS);
        if (b == null)
            return Boolean.TRUE;
        return b;
    }

    /** Setter for the GlyphsOverLineNumbers property
     * @param over draw the glyphd over the line numbers */    
    public void setGlyphsOverLineNumbers(Boolean over) {
        if (!isGlyphsOverLineNumbers().equals(over)) {
            putProp(PROP_GLYPHS_OVER_LINE_NUMBERS, over);
            firePropertyChange(PROP_GLYPHS_OVER_LINE_NUMBERS, null, null);
            saveSetting(PROP_GLYPHS_OVER_LINE_NUMBERS, over);
        }
    }
    
    /** Getter for ShowGlyphGutter property
     * @return  whether the glyph should be shown after opening editor or not */    
    public Boolean isShowGlyphGutter() {
        loadSettings();
        
        Boolean b = (Boolean)getProp(PROP_SHOW_GLYPH_GUTTER);
        if (b == null)
            return Boolean.TRUE;
        return b;
    }

    /** Setter for the ShowGlyphGutter property
     * @param gutter show gutter */    
    public void setShowGlyphGutter(Boolean gutter) {
        if (!isShowGlyphGutter().equals(gutter)) {
            putProp(PROP_SHOW_GLYPH_GUTTER, gutter);
            firePropertyChange(PROP_SHOW_GLYPH_GUTTER, null, null);
            saveSetting(PROP_SHOW_GLYPH_GUTTER, gutter);
        }
    }
    
    /** Gets property for appropriate string value */
    private Object getProp(String prop){
        return properties.get(prop);
    }
    
    /** Puts property to Map */
    private void putProp(Object key, Object value){
        if (value == null) {
            properties.remove(key);
            return;
        }
        properties.put(key,value);
    }
    
    
    /** Add listeners on changes of annotation type properties
     * @param l  change listener*/
    final public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        support.addPropertyChangeListener (l);
    }
    
    /** Remove listeners on changes of annotation type properties
     * @param l  change listener*/
    final public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        support.removePropertyChangeListener (l);
    }

    /** Fire property change to registered listeners. */
    final protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        support.firePropertyChange(this, propertyName, oldValue, newValue);
    }

    /** Initialize the map of all annotation types
     * @param map map containing all annotation types */    
    public final void setTypes(Map map) {
        if (allTypes != null) {
            allTypes = map;
            // loading of annotation types is done in non-AWT thread
            // while listeners on this change usually needs to update UI
            // that's reason why the change is fired in AWT thread
            SwingUtilities.invokeLater(new FirePropertyChange());
        } else {
            allTypes = map;
        }
    }

    public final void removeType(String name) {
        allTypes.remove(name);
        SwingUtilities.invokeLater(new FirePropertyChange());
    }
    
    /** Returns AnnotationType instance for the given name of the type
     * @param name annotation type name
     * @return instance describing annotation type */    
    public final AnnotationType getType(String name) {
        loadTypes();
        
        AnnotationType ret = null;
        if (allTypes != null){
            ret = (AnnotationType)allTypes.get(name);
        }
        
        if (ret == null){
            Utilities.annotateLoggable(new NullPointerException("null AnnotationType for:"+name)); //NOI18N
        }
        
        return ret;
    }

    /** Iterator of all annotation type names in the system */
    public Iterator getAnnotationTypeNames() {
        loadTypes();
        Set temp = new HashSet();
        if (allTypes != null)
            temp.addAll(allTypes.keySet());
        return temp.iterator();
    }

    /** Gets count of all annotation type names */
    public int getAnnotationTypeNamesCount() {
        loadTypes();
        
        return allTypes.keySet().size();
    }
    
    /** Gets count of all visible annotation type names */
    public int getVisibleAnnotationTypeNamesCount() {
        loadTypes();
        
        Iterator i = getAnnotationTypeNames();
        int count = 0;
        for (; i.hasNext(); ) {
            AnnotationType type = getType((String)i.next());
            if (type == null)
                continue;
            if (type.isVisible())
                count++;
        }
        return count;
    }
    
    /** Register loader for loading of annotation types */
    public void registerLoader(Loader l) {
        loader = l;
        loadedTypes = false;
        loadedSettings = false;
    }

    /** Check if the types were loaded and load them if not */
    private void loadTypes() {
        if (loadedTypes || loader == null)
            return;

        loader.loadTypes();
        
        loadedTypes = true;
    }
    
    /** Save changes in one annotation type */
    public void saveType(AnnotationType type) {
        if (!loadedTypes || loader == null)
            return;
        
        loader.saveType(type);
    }

    /** Check if the settings were loaded and load them if not */
    private void loadSettings() {
        if (loadedSettings || loader == null || loadingInProgress)
            return;

        loadingInProgress = true;
        loader.loadSettings();
        loadingInProgress = false;
        
        loadedSettings = true;
    }

    /** Save change of property */
    public void saveSetting(String settingName, Object value) {
        if (!loadedSettings || loader == null)
            return;
        
        loader.saveSetting(settingName, value);
    }
    
    /** Loader of annotation types. The loader must be registered and is called
     * when the annotation types data are queried. */
    public interface Loader {

        /** Load all annotation types data. */
        public void loadTypes();

        /** Load properties of this class. */
        public void loadSettings();

        /** Save one annotation type. */
        public void saveType(AnnotationType type);

        /** Save changed property of this class. */
        public void saveSetting(String settingName, Object value);
    }

    /** This class is defined instead of two anonymous Runnables which are needed. */
    private class FirePropertyChange implements Runnable {
        FirePropertyChange() {
        }
        public void run() {
            firePropertyChange(PROP_ANNOTATION_TYPES, null, null);
        }
    }
    
}
