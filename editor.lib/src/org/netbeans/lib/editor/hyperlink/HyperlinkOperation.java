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

package org.netbeans.lib.editor.hyperlink;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.JumpList;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProviderExt;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.spi.editor.highlighting.HighlightAttributeValue;
import org.netbeans.spi.editor.highlighting.HighlightsLayer;
import org.netbeans.spi.editor.highlighting.HighlightsLayerFactory;
import org.netbeans.spi.editor.highlighting.ZOrder;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;

/**
 *
 * @author Jan Lahoda
 */
public class HyperlinkOperation implements MouseListener, MouseMotionListener, PropertyChangeListener, KeyListener {

    private static Logger LOG = Logger.getLogger(HyperlinkOperation.class.getName());
    
    private JTextComponent component;
    private Document       currentDocument;
    private String         operationMimeType;
    private Cursor         oldComponentsMouseCursor;
    private boolean        hyperlinkUp;
    private boolean        listenersSetUp;

    private boolean        hyperlinkEnabled;
    private int            actionKeyMask;
    
    public static HyperlinkOperation create(JTextComponent component, String mimeType) {
        return new HyperlinkOperation(component, mimeType);
    }
    
    private static synchronized Cursor getMouseCursor(HyperlinkType type) {
        switch (type) {
            case GO_TO_DECLARATION:
                return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
            default:
                throw new UnsupportedOperationException();
        }
    }
    
    private static synchronized boolean isHyperlinkMouseCursor(Cursor c) {
        return    c == Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
               || c == Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)
               || c == Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
    }
    
    /** Creates a new instance of HoveringImpl */
    private HyperlinkOperation(JTextComponent component, String mimeType) {
        this.component = component;
        this.operationMimeType  = mimeType;
        this.oldComponentsMouseCursor = null;
        this.hyperlinkUp = false;
        this.listenersSetUp = false;
        
        readSettings();
        
        if (hyperlinkEnabled) {
            component.addPropertyChangeListener("document", this); // NOI18N
        }
    }
    
    private void documentUpdated() {
        if (!hyperlinkEnabled)
            return ;
        
        currentDocument = component.getDocument();
        
        if (currentDocument instanceof BaseDocument) {
            if (!listenersSetUp) {
                component.addMouseListener(this);
                component.addMouseMotionListener(this);
                component.addKeyListener(this);
                listenersSetUp = true;
            }
        }
    }
    
    private void readSettings() {
        String hyperlinkActivationKeyPropertyValue = System.getProperty("org.netbeans.lib.editor.hyperlink.HyperlinkOperation.activationKey");
        
        if (hyperlinkActivationKeyPropertyValue != null) {
            if ("off".equals(hyperlinkActivationKeyPropertyValue)) { // NOI18N
                this.hyperlinkEnabled = false;
                this.actionKeyMask = (-1);
            } else {
                this.hyperlinkEnabled = true;
                this.actionKeyMask = (-1);
                
                for (int cntr = 0; cntr < hyperlinkActivationKeyPropertyValue.length(); cntr++) {
                    int localMask = 0;
                    
                    switch (hyperlinkActivationKeyPropertyValue.charAt(cntr)) {
                        case 'S': localMask = InputEvent.SHIFT_DOWN_MASK; break;
                        case 'C': localMask = InputEvent.CTRL_DOWN_MASK;  break;
                        case 'A': localMask = InputEvent.ALT_DOWN_MASK;   break;
                        case 'M': localMask = InputEvent.META_DOWN_MASK;  break;
                        default:
                            LOG.warning("Incorrect value of org.netbeans.lib.editor.hyperlink.HyperlinkOperation.activationKey property (only letters CSAM are allowed): " + hyperlinkActivationKeyPropertyValue.charAt(cntr));
                    }
                    
                    if (localMask == 0) {
                        //some problem, ignore
                        this.actionKeyMask = (-1);
                        break;
                    }
                    
                    if (this.actionKeyMask == (-1))
                        this.actionKeyMask = localMask;
                    else
                        this.actionKeyMask |= localMask;
                }
                
                if (this.actionKeyMask == (-1)) {
                    LOG.warning("Some problem with property org.netbeans.lib.editor.hyperlink.HyperlinkOperation.activationKey, more information might be given above. Falling back to the default behaviour.");
                } else {
                    return;
                }
            }
        }
        
        this.hyperlinkEnabled = true;

        Preferences prefs = MimeLookup.getLookup(DocumentUtilities.getMimeType(component)).lookup(Preferences.class);
        this.actionKeyMask = prefs.getInt(SimpleValueNames.HYPERLINK_ACTIVATION_MODIFIERS, InputEvent.CTRL_DOWN_MASK);
    }
    
    public void mouseMoved(MouseEvent e) {
        HyperlinkType type = getHyperlinkType(e);
        
        if (type != null) {
            int position = component.viewToModel(e.getPoint());
            
            if (position < 0) {
                unHyperlink(true);
                
                return ;
            }
            
            performHyperlinking(position, type);
        } else {
            unHyperlink(true);
        }
    }
    
    public void mouseDragged(MouseEvent e) {
        //ignored
    }
    
    private HyperlinkType getHyperlinkType(InputEvent e) {
        return ((e.getModifiers() | e.getModifiersEx()) & actionKeyMask) == actionKeyMask ? HyperlinkType.GO_TO_DECLARATION : null;
    }
    
    private void performHyperlinking(int position, HyperlinkType type) {
        HyperlinkProviderExt provider = findProvider(position, type);
        
        if (provider != null) {
            int[] offsets = provider.getHyperlinkSpan(component.getDocument(), position, type);
            
            if (offsets != null) {
                makeHyperlink(type, provider, offsets[0], offsets[1]);
            }
        } else {
            unHyperlink(true);
        }
    }
    
    private void performAction(int position, HyperlinkType type) {
        HyperlinkProviderExt provider = findProvider(position, type);
        
        if (provider != null) {
            unHyperlink(true);
            
            //make sure the position is correct and the JumpList works:
            component.getCaret().setDot(position);
            JumpList.checkAddEntry(component, position);
            
            provider.performClickAction(component.getDocument(), position, type);
        }
    }
    
    private HyperlinkProviderExt findProvider(int position, HyperlinkType type) {
        Object mimeTypeObj = component.getDocument().getProperty(BaseDocument.MIME_TYPE_PROP);  //NOI18N
        String mimeType;
        
        if (mimeTypeObj instanceof String)
            mimeType = (String) mimeTypeObj;
        else {
            mimeType = this.operationMimeType;
        }
        
        Collection<? extends HyperlinkProviderExt> extProviders = HyperlinkProviderManagerExt.getHyperlinkProviderExts(mimeType);
        
        for (HyperlinkProviderExt provider : extProviders) {
            if (provider.getSupportedHyperlinkTypes().contains(type) && provider.isHyperlinkPoint(component.getDocument(), position, type)) {
                return provider;
            }
        }
        
        if (type != HyperlinkType.GO_TO_DECLARATION) {
            return null;
        }
        
        Collection<? extends HyperlinkProvider> providers = HyperlinkProviderManager.getHyperlinkProviders(mimeType);
        
        for (final HyperlinkProvider provider : providers) {
            if (provider.isHyperlinkPoint(component.getDocument(), position)) {
                return new HyperlinkProviderExt() {
                    public Set<HyperlinkType> getSupportedHyperlinkTypes() {
                        return EnumSet.of(HyperlinkType.GO_TO_DECLARATION);
                    }
                    public boolean isHyperlinkPoint(Document doc, int offset, HyperlinkType type) {
                        return provider.isHyperlinkPoint(doc, offset);
                    }
                    public int[] getHyperlinkSpan(Document doc, int offset, HyperlinkType type) {
                        return provider.getHyperlinkSpan(doc, offset);
                    }
                    public void performClickAction(Document doc, int offset, HyperlinkType type) {
                        provider.performClickAction(doc, offset);
                    }
                    public String getTooltipText(Document doc, int offset, HyperlinkType type) {
                        return null;
                    }
                };
            }
        }
        
        return null;
    }
    
    private synchronized void makeHyperlink(HyperlinkType type, HyperlinkProviderExt provider, final int start, final int end) {
        boolean makeCursorSnapshot = true;
        
        if (hyperlinkUp) {
            unHyperlink(false);
            makeCursorSnapshot = false;
        }
        
        OffsetsBag prepare = new OffsetsBag(component.getDocument());

        FontColorSettings fcs = MimeLookup.getLookup(MimePath.EMPTY).lookup(FontColorSettings.class);
        AttributeSet hyperlinksHighlight = fcs.getFontColors("hyperlinks"); //NOI18N
        prepare.addHighlight(start, end, AttributesUtilities.createComposite(
            hyperlinksHighlight != null ? hyperlinksHighlight : defaultHyperlinksHighlight,
            AttributesUtilities.createImmutable(EditorStyleConstants.Tooltip, new TooltipResolver(provider, start, type))));

        getBag(currentDocument).setHighlights(prepare);

        hyperlinkUp = true;

        if (makeCursorSnapshot) {
            if (component.isCursorSet()) {
                oldComponentsMouseCursor = component.getCursor();
            } else {
                oldComponentsMouseCursor = null;
            }
            component.setCursor(getMouseCursor(type));
        }
    }
    
    private synchronized void unHyperlink(boolean removeCursor) {
        if (!hyperlinkUp)
            return ;
        
        getBag(currentDocument).clear();
        
        if (removeCursor) {
            if (component.isCursorSet() && isHyperlinkMouseCursor(component.getCursor())) {
                component.setCursor(oldComponentsMouseCursor);
            }
            oldComponentsMouseCursor = null;
        }
        
        hyperlinkUp = false;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (currentDocument != component.getDocument())
            documentUpdated();
    }
    
    public void keyTyped(KeyEvent e) {
        //ignored
    }

    public void keyReleased(KeyEvent e) {
        if ((e.getModifiers() & actionKeyMask) == 0)
            unHyperlink(true);
    }

    public void keyPressed(KeyEvent e) {
       //ignored
    }

    public void mouseReleased(MouseEvent e) {
        //ignored
    }

    public void mousePressed(MouseEvent e) {
        //ignored
    }

    public void mouseExited(MouseEvent e) {
        //ignored
    }

    public void mouseEntered(MouseEvent e) {
        //ignored
    }

    public void mouseClicked(MouseEvent e) {
        HyperlinkType type = getHyperlinkType(e);
        
        if (type != null && !e.isPopupTrigger() && e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
            int position = component.viewToModel(e.getPoint());
            
            if (position < 0) {
                return ;
            }
            
            performAction(position, type);
        }
    }
    
    private static Object BAG_KEY = new Object();
    
    private static OffsetsBag getBag(Document doc) {
        OffsetsBag bag = (OffsetsBag) doc.getProperty(BAG_KEY);
        
        if (bag == null) {
            doc.putProperty(BAG_KEY, bag = new OffsetsBag(doc));
        }
        
        return bag;
    }
    
    private static AttributeSet defaultHyperlinksHighlight = AttributesUtilities.createImmutable(StyleConstants.Foreground, Color.BLUE, StyleConstants.Underline, Color.BLUE);
    
    public static final class HighlightFactoryImpl implements HighlightsLayerFactory {
        public HighlightsLayer[] createLayers(Context context) {
            return new HighlightsLayer[] {
                HighlightsLayer.create(HyperlinkOperation.class.getName(), ZOrder.CARET_RACK, true, getBag(context.getDocument()))
            };
        }
    }

    private static final class TooltipResolver implements HighlightAttributeValue<String> {

        private HyperlinkProviderExt provider;
        private int offset;
        private HyperlinkType type;

        public TooltipResolver(HyperlinkProviderExt provider, int offset, HyperlinkType type) {
            this.provider = provider;
            this.offset = offset;
            this.type = type;
        }

        public String getValue(JTextComponent component, Document document, Object attributeKey, int startOffset, int endOffset) {
            return provider.getTooltipText(document, offset, type);
        }
        
    }
}
