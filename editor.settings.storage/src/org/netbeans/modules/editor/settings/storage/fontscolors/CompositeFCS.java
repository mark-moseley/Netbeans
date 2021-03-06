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

package org.netbeans.modules.editor.settings.storage.fontscolors;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;

/**
 *
 * @author Vita Stejskal
 */
public final class CompositeFCS extends FontColorSettings {

    private static final Logger LOG = Logger.getLogger(CompositeFCS.class.getName());
    
    // A few words about the default coloring. It's special, it always contains
    // foreground, background and font related attributes. If they hadn't been
    // supplied by a user the default coloring will use system defaults. Therefore
    // this coloring should not be merged with any other colorings when folowing
    // the chain of coloring delegates.
    
    /** The name of the default coloring. */
    private static final String DEFAULT = "default"; //NOI18N
    
    private static final int DEFAULT_FONT_SIZE = UIManager.get("customFontSize") != null ? //NOI18N
        ((Integer) UIManager.get("customFontSize")).intValue() : //NOI18N
        UIManager.getFont("TextField.font").getSize(); //NOI18N
    
    private static final AttributeSet HARDCODED_DEFAULT_COLORING = AttributesUtilities.createImmutable(
        StyleConstants.NameAttribute, DEFAULT,
        StyleConstants.Foreground, Color.black,
        StyleConstants.Background, Color.white,
        StyleConstants.FontFamily, "Monospaced", //NOI18N
        StyleConstants.FontSize, DEFAULT_FONT_SIZE < 12 ? 12 : DEFAULT_FONT_SIZE
    );
  
    // Special instance to mark 'no attributes' for a token. This should never
    // be passed outside of this class, use SimpleAttributeSet.EMPTY instead. There
    // might be other code doing 'attribs == SAS.EMPTY'.
    private static final AttributeSet NULL = new SimpleAttributeSet();
    
    private final FontColorSettingsImpl [] allFcsi;
    /* package */ final String profile;
    private final Map<String, AttributeSet> tokensCache = new HashMap<String, AttributeSet>();
    
    /** Creates a new instance of CompositeFCS */
    public CompositeFCS(MimePath mimePath, String profile) {
        super();
        
        assert mimePath != null : "The parameter allPaths should not be null"; //NOI18N
        assert profile != null : "The parameter profile should not be null"; //NOI18N
        
        // Skip all mime types from the end that do not define any colorings.
        // This is here to support dummy languages like text/x-java/text/x-java-string that
        // inherit all colorings from the outer language.
        while(mimePath.size() > 1) {
            String lastMimeType = mimePath.getMimeType(mimePath.size() - 1);
            boolean empty = FontColorSettingsImpl.get(MimePath.parse(lastMimeType)).getColorings(profile).isEmpty();
            if (!empty) {
                break;
            }
            mimePath = mimePath.getPrefix(mimePath.size() - 1);
        }
        
        MimePath [] allPaths = computeInheritedMimePaths(mimePath);
        assert allPaths.length > 0 : "allPaths should always contain at least MimePath.EMPTY"; //NOI18N
        
        this.allFcsi = new FontColorSettingsImpl [allPaths.length];
        for(int i = 0; i < allPaths.length; i++) {
            allFcsi[i] = FontColorSettingsImpl.get(allPaths[i]);
        }
        
        this.profile = profile;
    }

    /**
     * Gets the coloring for a highlight. Highlights are used for highlighting
     * important things in editor such as a caret row, text selection, marking
     * text found by the last search peration, etc. They are not bound to any
     * tokens and therefore are mime type independent.
     */
    public AttributeSet getFontColors(String highlightName) {
        assert highlightName != null : "The parameter highlightName must not be null."; //NOI18N
        
        if (highlightName.equals(DEFAULT)) {
            return getTokenFontColors(DEFAULT);
        }
        
        AttributeSet attribs = null;
        Map<String, AttributeSet> coloringsMap = EditorSettings.getDefault().getHighlightings(profile);
        if (coloringsMap != null) {
            attribs = coloringsMap.get(highlightName);
        }
        
//        dumpAttribs(attribs, highlightName, false);
        return attribs;
    }

    public AttributeSet getTokenFontColors(String tokenName) {
        assert tokenName != null : "The parameter tokenName must not be null."; //NOI18N
        
        synchronized (tokensCache) {
            AttributeSet attribs = tokensCache.get(tokenName);

            if (attribs == null) {
                attribs = findColoringForToken(tokenName);
//                dumpAttribs(attribs, tokenName, true);
                tokensCache.put(tokenName, attribs);
//            } else {
//                System.out.println("Using cached value for token '" + tokenName + "' CompoundFCS.this = " + this);
            }
            
            return attribs == NULL ? null : attribs;
        }
    }
    
    public boolean isDerivedFromMimePath(MimePath mimePath) {
        for(FontColorSettingsImpl fcsi : allFcsi) {
            if (fcsi.getMimePath() == mimePath) {
                return true;
            }
        }
        return false;
    }

    //-----------------------------------------------------------------------
    // private implementation
    //-----------------------------------------------------------------------
    
    private AttributeSet findColoringForToken(String tokenName) {
        ArrayList<AttributeSet> colorings = new ArrayList<AttributeSet>();
        String name = tokenName;
        
        for(FontColorSettingsImpl fcsi : allFcsi) {
            name = processLayer(fcsi, name, colorings);
        }

        if (tokenName.equals(DEFAULT)) {
            colorings.add(HARDCODED_DEFAULT_COLORING);
        }
        
        if (colorings.size() > 0) {
            return AttributesUtilities.createImmutable(colorings.toArray(new AttributeSet[colorings.size()]));
        } else {
            return NULL;
        }
    }
    
    private String processLayer(FontColorSettingsImpl fcsi, String name, ArrayList<AttributeSet> colorings) {
        // Try colorings first
        AttributeSet as = fcsi.getColorings(profile).get(name);
        if (as == null) {
            // If not found, try the layer's default colorings
            as = fcsi.getDefaultColorings(profile).get(name);
        }

        // If we found a coloring then process it
        if (as != null) {
            colorings.add(as);

            String nameOfColoring = (String) as.getAttribute(StyleConstants.NameAttribute);
            String nameOfDelegate = (String) as.getAttribute(EditorStyleConstants.Default);
            if (nameOfDelegate != null && !nameOfDelegate.equals(DEFAULT)) {
                if (!nameOfDelegate.equals(nameOfColoring)) {
                    // Find delegate on the same layer
                    nameOfDelegate = processLayer(fcsi, nameOfDelegate, colorings);
                }
            } else {
                // Use the coloring's name as the default name of a delegate
                nameOfDelegate = nameOfColoring;
            }

            name = nameOfDelegate;
        }

        // Return updated name - either the name of the coloring or the name of
        // the coloring's delegate
        return name;
    }
    
    private void dumpAttribs(AttributeSet attribs, String name, boolean tokenColoring) {
//        if (!allFcsi[0].getMimePath().getPath().equals("text/x-java")) { //NOI18N
//            return;
//        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Attribs for base mime path '"); //NOI18N
        sb.append(allFcsi[0].getMimePath().getPath());
        sb.append("' and "); //NOI18N
        if (tokenColoring) {
            sb.append("token '"); //NOI18N
        } else {
            sb.append("highlight '"); //NOI18N
        }
        sb.append(name);
        sb.append("' = {"); //NOI18N
        
        Enumeration<?> keys = attribs.getAttributeNames();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = attribs.getAttribute(key);

            sb.append("'" + key + "' = '" + value + "'"); //NOI18N
            if (keys.hasMoreElements()) {
                sb.append(", "); //NOI18N
            }
        }

        sb.append("} CompoundFCS.this = "); //NOI18N
        sb.append(this.toString());
        
        System.out.println(sb.toString());
    }

    private static MimePath [] computeInheritedMimePaths(MimePath mimePath) {
        List<String> paths = null;
        try {
            Method m = MimePath.class.getDeclaredMethod("getInheritedPaths", String.class, String.class); //NOI18N
            m.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> ret = (List<String>) m.invoke(mimePath, null, null);
            paths = ret;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Can't call org.netbeans.api.editor.mimelookup.MimePath.getInheritedPaths method.", e); //NOI18N
        }

        if (paths != null) {
            ArrayList<MimePath> mimePaths = new ArrayList<MimePath>(paths.size());

            for (String path : paths) {
                mimePaths.add(MimePath.parse(path));
            }

            return mimePaths.toArray(new MimePath[mimePaths.size()]);
        } else {
            return new MimePath [] { mimePath, MimePath.EMPTY };
        }
    }
}
