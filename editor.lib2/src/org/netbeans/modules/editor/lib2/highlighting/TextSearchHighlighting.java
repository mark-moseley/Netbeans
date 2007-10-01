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

package org.netbeans.modules.editor.lib2.highlighting;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.modules.editor.lib2.search.EditorFindSupport;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal
 */
public class TextSearchHighlighting extends AbstractHighlightsContainer implements PropertyChangeListener, HighlightsChangeListener, DocumentListener {

    private static final Logger LOG = Logger.getLogger(TextSearchHighlighting.class.getName());
    
    public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.lib2.highlighting.TextSearchHighlighting"; //NOI18N
    
    private final MimePath mimePath;
    private final JTextComponent component;
    private final Document document;
    private final OffsetsBag bag;
    
    /** Creates a new instance of TextSearchHighlighting */
    public TextSearchHighlighting(JTextComponent component) {
        // Determine the mime type
        EditorKit kit = component.getUI().getEditorKit(component);
        String mimeType = kit == null ? null : kit.getContentType();
        this.mimePath = mimeType == null ? MimePath.EMPTY : MimePath.parse(mimeType);
        
        this.component = component;
        this.document = component.getDocument();
        
        // Let the bag update first...
        this.bag = new OffsetsBag(document);
        this.bag.addHighlightsChangeListener(this);
        
        // ...and the internal listener second
        this.document.addDocumentListener(WeakListeners.document(this, this.document));
        
        EditorFindSupport.getInstance().addPropertyChangeListener(
            WeakListeners.propertyChange(this, EditorFindSupport.getInstance())
        );
        
        fillInTheBag();
    }

    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        return bag.getHighlights(startOffset, endOffset);
    }
    
    public void highlightChanged(HighlightsChangeEvent event) {
        fireHighlightsChange(event.getStartOffset(), event.getEndOffset());
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null ||
            EditorFindSupport.FIND_WHAT.equals(evt.getPropertyName()) ||
            EditorFindSupport.FIND_HIGHLIGHT_SEARCH.equals(evt.getPropertyName()))
        {
            fillInTheBag();
        }
    }

    public void insertUpdate(DocumentEvent e) {
        this.bag.removeHighlights(e.getOffset(), e.getOffset() + e.getLength(), false);
    }

    public void removeUpdate(DocumentEvent e) {
        this.bag.removeHighlights(e.getOffset() - 1, e.getOffset() + e.getLength() - 1, false);
    }

    public void changedUpdate(DocumentEvent e) {
        // not interested
    }
    
    private void fillInTheBag() {
        document.render(new Runnable() {
            public void run() {
                OffsetsBag newBag = new OffsetsBag(document);

                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("TSH: filling the bag; enabled = " + isEnabled());
                }

                if (isEnabled()) {
                    try {
                        int [] blocks = EditorFindSupport.getInstance().getBlocks(
                            new int [] {-1, -1}, document, 0, document.getLength());

                        assert blocks.length % 2 == 0 : "Wrong number of block offsets";

                        AttributeSet attribs = getAttribs();
                        for (int i = 0; i < blocks.length / 2; i++) {
                            newBag.addHighlight(blocks[2 * i], blocks[2 * i + 1], attribs);
                        }
                    } catch (BadLocationException e) {
                        LOG.log(Level.WARNING, e.getMessage(), e);
                    }
                }

                bag.setHighlights(newBag);
            }
        });
    }
    
    private boolean isEnabled() {
        Object prop = EditorFindSupport.getInstance().getFindProperty(
            EditorFindSupport.FIND_HIGHLIGHT_SEARCH);
        return (prop instanceof Boolean) && ((Boolean) prop).booleanValue();
    }
    
    private AttributeSet getAttribs() {
        FontColorSettings fcs = MimeLookup.getLookup(mimePath).lookup(FontColorSettings.class);
        AttributeSet attribs = fcs.getFontColors(FontColorNames.HIGHLIGHT_SEARCH_COLORING);
        return attribs == null ? SimpleAttributeSet.EMPTY : attribs;
    }
}
