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

package org.netbeans.modules.editor.impl.highlighting;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorNames;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.MarkBlock;
import org.netbeans.editor.MarkBlockChain;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.WeakListeners;

/**
 *
 * @author Vita Stejskal
 */
public final class GuardedBlocksHighlighting extends AbstractHighlightsContainer implements PropertyChangeListener {
    
    private static final Logger LOG = Logger.getLogger(GuardedBlocksHighlighting.class.getName());
    public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.oldlibbridge.GuardedBlocksHighlighting"; //NOI18N
    
    private final Document document;
    private final MarkBlockChain guardedBlocksChain;
    private final MimePath mimePath;

    private AttributeSet attribs = null;
    
    /** Creates a new instance of NonLexerSytaxHighlighting */
    public GuardedBlocksHighlighting(Document document, String mimeType) {
        this.document = document;
        if (document instanceof GuardedDocument) {
            this.guardedBlocksChain = ((GuardedDocument) document).getGuardedBlockChain();
            this.guardedBlocksChain.addPropertyChangeListener(WeakListeners.propertyChange(this, this.guardedBlocksChain));
        } else {
            this.guardedBlocksChain = null;
        }
        this.mimePath = MimePath.parse(mimeType);
    }

    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        synchronized (this) {
            if (guardedBlocksChain != null) {
                return new HSImpl(guardedBlocksChain.getChain(), startOffset, endOffset);
            } else {
                return HighlightsSequence.EMPTY;
            }
        }
    }
    
    // ----------------------------------------------------------------------
    //  PropertyChangeListener implementation
    // ----------------------------------------------------------------------

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null || evt.getPropertyName().equals(MarkBlockChain.PROP_BLOCKS_CHANGED)) {
            int start = evt.getOldValue() == null ? -1 : ((Integer) evt.getOldValue()).intValue();
            int end = evt.getNewValue() == null ? -1 : ((Integer) evt.getNewValue()).intValue();
            
            if (start < 0 || start >= document.getLength()) {
                start = 0;
            }
            
            if (end <= start || end > document.getLength()) {
                end = Integer.MAX_VALUE;
            }
            
            fireHighlightsChange(start, end);
        }
    }
    
    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------

    private final class HSImpl implements HighlightsSequence {
        
        private final int startOffset;
        private final int endOffset;

        private boolean init = false;
        private MarkBlock block;
        
        public HSImpl(MarkBlock block, int startOffset, int endOffset) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.block = block;
        }
        
        public boolean moveNext() {
            if (!init) {
                init = true;

                while(null != block) {
                    if (block.getEndOffset() > startOffset) {
                        break;
                    }

                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Skipping block: " + block + //NOI18N
                            ", blockStart = " + block.getStartOffset() + //NOI18N
                            ", blockEnd = " + block.getEndOffset() + //NOI18N
                            ", startOffset = " + startOffset + //NOI18N
                            ", endOffset = " + endOffset //NOI18N
                        );
                    }
                    
                    block = block.getNext();
                }
            } else if (block != null) {
                block = block.getNext();
            }
            
            if (block != null && block.getStartOffset() > endOffset) {
                block = null;
            }
            
            if (LOG.isLoggable(Level.FINE)) {
                if (block != null) {
                    LOG.fine("Next block: " + block + //NOI18N
                        ", blockStart = " + block.getStartOffset() + //NOI18N
                        ", blockEnd = " + block.getEndOffset() + //NOI18N
                        ", startOffset = " + startOffset + //NOI18N
                        ", endOffset = " + endOffset //NOI18N
                    );
                } else {
                    LOG.fine("Next block: null"); //NOI18N
                }
            }
            
            return block != null;
        }

        public int getStartOffset() {
            synchronized (GuardedBlocksHighlighting.this) {
                if (!init) {
                    throw new NoSuchElementException("Call moveNext() first."); //NOI18N
                } else if (block == null) {
                    throw new NoSuchElementException();
                }

                return Math.max(block.getStartOffset(), startOffset);
            }
        }

        public int getEndOffset() {
            synchronized (GuardedBlocksHighlighting.this) {
                if (!init) {
                    throw new NoSuchElementException("Call moveNext() first."); //NOI18N
                } else if (block == null) {
                    throw new NoSuchElementException();
                }

                return Math.min(block.getEndOffset(), endOffset);
            }
        }

        public AttributeSet getAttributes() {
            synchronized (GuardedBlocksHighlighting.this) {
                if (!init) {
                    throw new NoSuchElementException("Call moveNext() first."); //NOI18N
                } else if (block == null) {
                    throw new NoSuchElementException();
                }

                if (attribs == null) {
                    FontColorSettings fcs = MimeLookup.getLookup(mimePath).lookup(FontColorSettings.class);
                    if (fcs != null) {
                        attribs = fcs.getFontColors(FontColorNames.GUARDED_COLORING);
                    }

                    if (attribs == null) {
                        attribs = SimpleAttributeSet.EMPTY;
                    } else {
                        attribs = AttributesUtilities.createImmutable(
                            attribs, 
                            AttributesUtilities.createImmutable(ATTR_EXTENDS_EOL, Boolean.TRUE)
                        );
                    }
                }
                
                return attribs;
            }
        }
    } // End of HSImpl class

}
