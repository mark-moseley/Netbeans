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

import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.netbeans.spi.editor.highlighting.support.OffsetsBag;

/**
 *
 * @author Vita Stejskal, Miloslav Metelka
 */
public final class CompoundHighlightsContainer extends AbstractHighlightsContainer {

    private static final Logger LOG = Logger.getLogger(CompoundHighlightsContainer.class.getName());
    
    private static final Position MAX_POSITION = new Position() {
        public int getOffset() {
            return Integer.MAX_VALUE;
        }
    };

    private static final int MIN_CACHE_SIZE = 128;
    
    private Document doc;
    private HighlightsContainer[] layers;
    private boolean[] blacklisted;
    private long version = 0;

    private final Object LOCK = new String("CompoundHighlightsContainer.LOCK"); //NOI18N
    private final LayerListener listener = new LayerListener(this);

    private OffsetsBag cache;
    private boolean cacheObsolete;
    private Position cacheLowestPos;
    private Position cacheHighestPos;
    
    public CompoundHighlightsContainer() {
        this(null, null);
    }
    
    public CompoundHighlightsContainer(Document doc, HighlightsContainer[] layers) {
        setLayers(doc, layers);
    }
    
    /**
     * Gets the list of <code>Highlight</code>s from this layer in the specified
     * area. The highlights are obtained as a merge of the highlights from all the
     * delegate layers. The following rules must hold true for the parameters
     * passed in:
     * 
     * <ul>
     * <li>0 <= <code>startOffset</code> <= <code>endOffset</code></li>
     * <li>0 <= <code>endOffset</code> <= <code>document.getLength() - 1<code></li>
     * <li>Optionally, <code>endOffset</code> can be equal to Integer.MAX_VALUE
     * in which case all available highlights will be returned.</li>
     * </ul>
     *
     * @param startOffset    The beginning of the area.
     * @param endOffset      The end of the area.
     *
     * @return The <code>Highlight</code>s in the area between <code>startOffset</code>
     * and <code>endOffset</code>.
     */
    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        assert 0 <= startOffset : "offsets must be greater than or equal to zero"; //NOI18N
        assert startOffset <= endOffset : "startOffset must be less than or equal to endOffset; " + //NOI18N
            "startOffset = " + startOffset + " endOffset = " + endOffset; //NOI18N
        
        synchronized (LOCK) {
            if (doc == null || layers == null || layers.length == 0 || startOffset == endOffset) {
                return HighlightsSequence.EMPTY;
            }

            int [] update = null;
            
            int lowest = cacheLowestPos == null ? -1 : cacheLowestPos.getOffset();
            int highest = cacheHighestPos == null ? -1 : cacheHighestPos.getOffset();

            if (cacheObsolete) {
                cacheObsolete = false;
                discardCache();
            } else {
                if (lowest == -1 || highest == -1) {
                    // not sure what is cached -> reset the cache
                    discardCache();
                } else {
                    if (endOffset <= highest && startOffset < lowest) {
                        // below the cached area, but close enough
                        update = new int [] { expandBelow(startOffset, lowest), lowest };
                    } else if (startOffset >= lowest && endOffset > highest) {
                        // above the cached area, but close enough
                        update = new int [] { highest, expandAbove(highest, endOffset) };
                    } else if (startOffset < lowest && endOffset > highest) {
                        // extends the cached area on both sides
                        update = new int [] { expandBelow(startOffset, lowest), lowest, highest, expandAbove(highest, endOffset) };
                    } else if (startOffset >= lowest && endOffset <= highest) {
                        // inside the cached area
                    } else {
                        // completely off the area and too far
                        discardCache();
                    }
                }
            }

            OffsetsBag bag = cache;
            if (bag == null) {
                bag = new OffsetsBag(doc, true);
                cache = bag;
                lowest = highest = -1;
                update = new int [] { expandBelow(startOffset, endOffset), expandAbove(startOffset, endOffset) };
            }
            
            if (update != null) {
                for (int i = 0; i < update.length / 2; i++) {
                    if (update[2 * i + 1] >= doc.getLength()) {
                        update[2 * i + 1] = Integer.MAX_VALUE;
                    }
                    
                    updateCache(update[2 * i], update[2 * i + 1], bag);
                    
                    if (update[2 * i + 1] == Integer.MAX_VALUE) {
                        break;
                    }
                }
                
                if (lowest == -1 || highest == -1) {
                    cacheLowestPos = createPosition(update[0]);
                    cacheHighestPos = createPosition(update[update.length - 1]);
                } else {
                    cacheLowestPos = createPosition(Math.min(lowest, update[0]));
                    cacheHighestPos = createPosition(Math.max(highest, update[update.length - 1]));
                }
                
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Cache boundaries: " + //NOI18N
                        "<" + (cacheLowestPos == null ? "-" : cacheLowestPos.getOffset()) + //NOI18N
                        ", " + (cacheHighestPos == null ? "-" : cacheHighestPos.getOffset()) + "> " + //NOI18N
                        "when asked for <" + startOffset + ", " + endOffset + ">"); //NOI18N
                }
            }

            return new Seq(version, bag.getHighlights(startOffset, endOffset));
        }
    }

    /**
     * Gets the delegate layers.
     *
     * @return The layers, which this proxy layer delegates to.
     */
    public HighlightsContainer[] getLayers() {
        synchronized (LOCK) {
            return layers;
        }
    }
    
    /**
     * Sets the delegate layers. The layers are merged in the same order in which
     * they appear in the array passed into this method. That means that the first
     * layer in the array is the less important (i.e. the bottom of the z-order) and
     * the last layer in the array is the most visible one (i.e. the top of the z-order).
     *
     * <p>If you want the layers to be merged according to their real z-order sort
     * the array first by using <code>ZOrder.sort()</code>.
     *
     * @param layers    The new delegate layers. Can be <code>null</code>.
     * @see org.netbeans.api.editor.view.ZOrder#sort(HighlightLayer [])
     */
    public void setLayers(Document doc, HighlightsContainer[] layers) {
        Document docForEvents = null;
        
        synchronized (LOCK) {
            if (doc == null) {
                assert layers == null : "If doc is null the layers must be null too."; //NOI18N
            }
        
            docForEvents = doc != null ? doc : this.doc;
            
            // Remove the listener from the current layers
            if (this.layers != null) {
                for (int i = 0; i < this.layers.length; i++) {
                    this.layers[i].removeHighlightsChangeListener(listener);
                }
            }
    
            this.doc = doc;
            this.layers = layers;
            this.blacklisted = layers == null ? null : new boolean [layers.length];
            this.cacheObsolete = true;
            increaseVersion();

            // Add the listener to the new layers
            if (this.layers != null) {
                for (int i = 0; i < this.layers.length; i++) {
                    this.layers[i].addHighlightsChangeListener(listener);
                }
            }
        }

        if (docForEvents != null) {
            docForEvents.render(new Runnable() {
                public void run() {
                    fireHighlightsChange(0, Integer.MAX_VALUE);
                }
            });
        }
    }

    public void resetCache() {
        layerChanged(null, 0, Integer.MAX_VALUE);
    }
    
    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------
    
    private void layerChanged(HighlightsContainer layer, final int changeStartOffset, final int changeEndOffset) {
        Document docForEvents = null;

        synchronized (LOCK) {
            // XXX: Perhaps we could do something more efficient.
            LOG.fine("Cache obsoleted by changes in " + layer);
            cacheObsolete = true;
            increaseVersion();
            
            docForEvents = doc;
        }
        
        // Fire an event
        if (docForEvents != null) {
            docForEvents.render(new Runnable() {
                public void run() {
                    fireHighlightsChange(changeStartOffset, changeEndOffset);
                }
            });
        }
    }

    private void updateCache(final int startOffset, final int endOffset, OffsetsBag bag) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Updating cache: <" + startOffset + ", " + endOffset + ">"); //NOI18N
        }
        
        for (int i = 0; i < layers.length; i++) {
            if (blacklisted[i]) {
                continue;
            }

            try {
                final HighlightsSequence seq = layers[i].getHighlights(startOffset, endOffset);
                final int layerIndex = i; //saving this so we can debug corrupt layers (aka the ones that need clipping)
                final HighlightsContainer currentLayerObject = layers[i];
                bag.addAllHighlights(new HighlightsSequence() {

                    int start = -1, end = -1;
                    public boolean moveNext() {
                        boolean hasNext = seq.moveNext();
                        //XXX: the problem here is if the sequence we are wrapping is sorted by startOffset.
                        // In practice I think it is, but I cannot afford to make that assumption now.
                        // So I have to check both boundaries, not only start and end offset separately.
                        boolean retry = hasNext;
                        while(retry){
                            start = seq.getStartOffset();
                            end = seq.getEndOffset();
                            assert start <=end : "Start should come before the end offset in the sequence"; //NOI18N

                            if (start > endOffset || end < startOffset) {
                                //this highlight is totally outside our rage, there is nothing we can clip, we must retry
                                LOG.warning("Corrupt layer found (#" + layerIndex + ":" + currentLayerObject + "). Start offset " + start + " and end offset "+end+" are outside the range [" + startOffset + "," + endOffset+"], skipping."); //NOI18N
                                retry = hasNext = seq.moveNext();
                            }else{
                                retry = false;
                            }
                        }
                        if(hasNext){
                            if (start < startOffset) {
                                LOG.warning("Corrupt layer found (#" + layerIndex + ":" + currentLayerObject + "). Start offset " + start + " should be >=" + startOffset + ". Clipping..."); //NOI18N
                                start = startOffset;
                            }
                            if (end > endOffset) {
                                LOG.warning("Corrupt layer found (#" + layerIndex + ":" + currentLayerObject + "). End offset " + end + " should be <=" + endOffset + ". Clipping..."); //NOI18N
                                end = endOffset;
                            }
                        }
                        return hasNext;
                    }

                    public int getStartOffset() {
                        return start;
                    }

                    public int getEndOffset() {
                        return end;
                    }

                    public AttributeSet getAttributes() {
                        return seq.getAttributes();
                    }
                });
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine(dumpLayerHighlights(layers[i], startOffset, endOffset));
                }
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                blacklisted[i] = true;
                LOG.log(Level.WARNING, "The layer failed to supply highlights: " + layers[i], t); //NOI18N
            }
        }
    }
    
    private Position createPosition(int offset) {
        try {
            if (offset == Integer.MAX_VALUE) {
                return MAX_POSITION;
            } else {
                return doc.createPosition(offset);
            }
        } catch (BadLocationException e) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Invalid document position: offset = " + offset + //NOI18N
                    ", document.lenght = " + doc.getLength() + ", will not cache."); //NOI18N
            }
            return null;
        }
    }

    private void increaseVersion() {
        version++;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("CHC@" + Integer.toHexString(System.identityHashCode(this)) + //NOI18N
                ", OB@" + (cache == null ? "null" : Integer.toHexString(System.identityHashCode(cache))) + //NOI18N
                ", doc@" + Integer.toHexString(System.identityHashCode(doc)) + " version=" + version); //NOI18N
        }
    }
    
    private void discardCache() {
        if (cache != null) {
            cache.discard();
        }
        cache = null;
    }
    
    private static int expandBelow(int startOffset, int endOffset) {
        if (startOffset == 0 || endOffset == Integer.MAX_VALUE) {
            return startOffset;
        } else {
            int expandBy = Math.max((endOffset - startOffset) >> 2, MIN_CACHE_SIZE);
            return Math.max(startOffset - expandBy, 0);
        }
    }
    
    private static int expandAbove(int startOffset, int endOffset) {
        if (endOffset == Integer.MAX_VALUE) {
            return endOffset;
        } else {
            int expandBy = Math.max((endOffset - startOffset) >> 2, MIN_CACHE_SIZE);
            return endOffset + expandBy;
        }
    }

    private static String dumpLayerHighlights(HighlightsContainer layer, int startOffset, int endOffset) {
        StringBuilder sb = new StringBuilder();

        sb.append("Highlights in " + layer + ": {\n"); //NOI18N
        
        for(HighlightsSequence seq = layer.getHighlights(startOffset, endOffset); seq.moveNext(); ) {
            sb.append("  <"); //NOI18N
            sb.append(seq.getStartOffset());
            sb.append(", "); //NOI18N
            sb.append(seq.getEndOffset());
            sb.append(", "); //NOI18N
            sb.append(seq.getAttributes().getAttribute(StyleConstants.NameAttribute));
            sb.append(">\n"); //NOI18N
        }
        
        sb.append("} End of Highlights in " + layer); //NOI18N
        sb.append("\n"); //NOI18N
        
        return sb.toString();
    }

    private static final class LayerListener implements HighlightsChangeListener {
        
        private WeakReference<CompoundHighlightsContainer> ref;
        
        public LayerListener(CompoundHighlightsContainer container) {
            ref = new WeakReference<CompoundHighlightsContainer>(container);
        }
        
        public void highlightChanged(HighlightsChangeEvent event) {
            CompoundHighlightsContainer container = ref.get();
            if (container != null) {
                container.layerChanged(
                    (HighlightsContainer)event.getSource(), 
                    event.getStartOffset(), 
                    event.getEndOffset());
            }
        }
    } // End of Listener class

    private final class Seq implements HighlightsSequence {
        
        private HighlightsSequence seq;
        private long version;
        
        private int startOffset = -1;
        private int endOffset = -1;
        private AttributeSet attibutes = null;
        
        public Seq(long version, HighlightsSequence seq) {
            this.version = version;
            this.seq = seq;
        }
        
        public boolean moveNext() {
            synchronized (CompoundHighlightsContainer.this.LOCK) {
                if (checkVersion()) {
                    if (seq.moveNext()) {
                        startOffset = seq.getStartOffset();
                        endOffset = seq.getEndOffset();
                        attibutes = seq.getAttributes();
                        return true;
                    }
                }
                
                return false;
            }
        }

        public int getStartOffset() {
            synchronized (CompoundHighlightsContainer.this.LOCK) {
                assert startOffset != -1 : "Sequence not initialized, call moveNext() first."; //NOI18N
                return startOffset;
            }
        }

        public int getEndOffset() {
            synchronized (CompoundHighlightsContainer.this.LOCK) {
                assert endOffset != -1 : "Sequence not initialized, call moveNext() first."; //NOI18N
                return endOffset;
            }
        }

        public AttributeSet getAttributes() {
            synchronized (CompoundHighlightsContainer.this.LOCK) {
                assert attibutes != null : "Sequence not initialized, call moveNext() first."; //NOI18N
                return attibutes;
            }
        }

        // There can be concurrent modifications from different threads operating under
        // document's read lock. See IZ#106069.
        private boolean checkVersion() {
            return this.version == CompoundHighlightsContainer.this.version;
        }
    } // End of Seq class
}
