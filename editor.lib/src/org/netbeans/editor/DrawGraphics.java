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

import java.awt.Graphics;
import java.awt.Font;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Shape;
import java.awt.Rectangle;
import javax.swing.text.View;

/** Draw graphics functions as abstraction over various kinds of drawing. It's used
* for drawing into classic graphics, printing and measuring.
* Generally there are only the setters for some properties because 
* the draw-engine doesn't retrieve the values that it previously
* set.
*
* @author Miloslav Metelka
* @version 1.00
*/
interface DrawGraphics {
    
    /** Set foreground color */
    public void setForeColor(Color foreColor);

    /** Set background color */
    public void setBackColor(Color backColor);

    /** Inform the draw-graphics about the current
    * background color of the component.
    */
    public void setDefaultBackColor(Color defaultBackColor);
    
    public void setStrikeThroughColor(Color strikeThroughColor);
    
    public void setUnderlineColor(Color underlineColor);
    
    public void setWaveUnderlineColor(Color waveUnderlineColor);

    /** Set current font */
    public void setFont(Font font);

    /** Set the current x-coordinate */
    public void setX(int x);

    /** Set the current y-coordinate */
    public void setY(int y);

    /** Set the height of the line. */
    public void setLineHeight(int lineHeight);

    /** Set the ascent of the line. */
    public void setLineAscent(int lineAscent);

    /** Get the AWT-graphics to determine whether this draws to a graphics.
    * This is useful for fast line numbering and others.
    */
    public Graphics getGraphics();

    /** Whether draw graphics supports displaying of line numbers.
    * If not line number displaying is not done.
    */
    public boolean supportsLineNumbers();

    /** Initialize this draw graphics before drawing */
    public void init(DrawContext ctx);

    /** Called when whole drawing ends. Can be used to deallocate
    * some resources etc.
    */
    public void finish();

    /** Fill rectangle at the current [x, y] with the current
    * background color.
    * @param width width of the rectangle to fill in points. The current x-coordinate
    *  must be increased by width automatically.
    */
    public void fillRect(int width);

    /** Draw characters from the specified offset in the buffer
    * @param offset offset in the buffer for drawn text; if the text contains
    *   tabs, then offset is set to -1 and length contains the count
    *   of the space characters that correspond to the expanded tabs
    * @param length length of the text being drawn
    * @param width width of the text being drawn in points. The current
    *   x-coordinate must be increased by width automatically.
    */
    public void drawChars(int offset, int length, int width);

    /** Draw the expanded tab characters.
    * @param offset offset in the buffer where the tab characters start.
    * @param length number of the tab characters
    * @param spaceCount number of spaces that replace the tabs
    * @param width width of the spaces in points. The current x-coordinate
    *   must be increased by width automatically.
    */
    public void drawTabs(int offset, int length, int spaceCount, int width);

    /** Set character buffer from which the characters are drawn. */
    public void setBuffer(char[] buffer);

    /** This method is called to notify this draw graphics in response
    * from targetPos parameter passed to draw().
    * @param offset position that was reached during the drawing.
    * @param ch character at offset
    * @param charWidth visual width of the character ch
    * @param ctx current draw context containing 
    * @return whether the drawing should continue or not. If it returns
    *   false it's guaranteed that this method will not be called again
    *   and the whole draw() method will be stopped. <BR>The only
    *   exception is when the -1 is used as the target offset
    *   when draw() is called which means that every offset
    *   is a potential target offset and must be checked.
    *   In this case the binary search is used when finding
    *   the target offset inside painted fragment. That greatly
    *   improves performance for long fragments because
    *   the font metrics measurements are relatively expensive.
    */
    public boolean targetOffsetReached(int offset, char ch, int x,
                                       int charWidth, DrawContext ctx);

    /** EOL encountered and should be handled. */
    public void eol();
    
    /** Setter for painted view */
    public void setView(javax.swing.text.View view);


    /** Abstract draw-graphics that maintains a fg and bg color, font,
    * current x and y coordinates.
    */
    static abstract class AbstractDG implements DrawGraphics {

        /** Current foreground color */
        Color foreColor;

        /** Current background color */
        Color backColor;

        /** Default background color */
        Color defaultBackColor;

        /** Current font */
        Font font;

        /** Character buffer from which the data are drawn */
        char[] buffer;

        /** Current x-coordinate */
        int x;

        /** Current y-coordinate */
        int y;

        /** Height of the line being drawn */
        int lineHeight;

        /** Ascent of the line being drawn */
        int lineAscent;

        public Color getForeColor() {
            return foreColor;
        }

        public void setForeColor(Color foreColor) {
            this.foreColor = foreColor;
        }

        public Color getBackColor() {
            return backColor;
        }

        public void setBackColor(Color backColor) {
            this.backColor = backColor;
        }

        public Color getDefaultBackColor() {
            return defaultBackColor;
        }

        public void setDefaultBackColor(Color defaultBackColor) {
            this.defaultBackColor = defaultBackColor;
        }

        public Font getFont() {
            return font;
        }

        public void setFont(Font font) {
            this.font = font;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getLineHeight() {
            return lineHeight;
        }

        public void setLineHeight(int lineHeight) {
            this.lineHeight = lineHeight;
        }

        public int getLineAscent() {
            return lineAscent;
        }

        public void setLineAscent(int lineAscent) {
            this.lineAscent = lineAscent;
        }

        public char[] getBuffer() {
            return buffer;
        }

        public void setBuffer(char[] buffer) {
            this.buffer = buffer;
        }

        public void drawChars(int offset, int length, int width) {
            x += width;
        }

        public void drawTabs(int offset, int length, int spaceCount, int width) {
            x += width;
        }

        public void setStrikeThroughColor(Color strikeThroughColor) {
        }
        
        public void setUnderlineColor(Color underlineColor) {
        }
        
        public void setWaveUnderlineColor(Color waveUnderlineColor) {
        }
        
        public void setView(javax.swing.text.View view) {
        }
        
    } // End of AbstractDG class

    static class SimpleDG extends AbstractDG {

        public Graphics getGraphics() {
            return null;
        }

        public boolean supportsLineNumbers() {
            return false;
        }

        public void init(DrawContext ctx) {
        }

        public void finish() {
        }

        public void fillRect(int width) {
        }

        public boolean targetOffsetReached(int offset, char ch, int x,
                                           int charWidth, DrawContext ctx) {
            return true; // shouldn't reach this place
        }

        public void eol() {
        }

    } // End of SimpleDG class

    /** Implementation of DrawGraphics to delegate to some Graphics.
    * It optimizes the drawing by joining together the pieces of
    * the text drawn with the same font and fg/bg color.
    */
    static final class GraphicsDG extends SimpleDG {

        /** Whether debug messages should be displayed */
        private static final boolean debug
            = Boolean.getBoolean("netbeans.debug.editor.draw.graphics"); // NOI18N

        private Graphics graphics;

        /** Start of the chars that were not drawn yet. It can be -1
        * to indicate the buffered characters were just flushed.
        */
        private int startOffset = -1;

        /** End of the chars that were not drawn yet */
        private int endOffset;

        /** X coordinate where the drawing of chars should occur */
        private int startX;

        /** Y coordinate where the drawing of chars should occur */
        private int startY;

        private int width;

        private Color strikeThroughColor;

        private Color underlineColor;
        
        private Color waveUnderlineColor;
        
        /** Whether annotations were drawn on the current line already */
        private int lastDrawnAnnosY;
        private int lastDrawnAnnosX;
        
        /** Annotation description cached for the lastDrawnAnnosY */
        private AnnotationDesc[] passiveAnnosAtY;

        /** Alpha used for drawing the glyphs on the background */
        private AlphaComposite alpha = null;

        /** Access to annotations for this document which will be
         * drawn on the background */
        private Annotations annos = null;
        
        private boolean drawTextLimitLine;
        private int textLimitWidth;
        private int defaultSpaceWidth;
        private Color textLimitLineColor;
        private int absoluteX;
        private int maxWidth;
        private View view;

        private int bufferStartOffset;
        private int frameStartOffset = Integer.MAX_VALUE;
        private int frameEndOffset = frameStartOffset;
        private JTextComponent component;
        private PropertyChangeListener componentListener;

        GraphicsDG(Graphics graphics) {
            this.graphics = graphics;
            // #33165 - set invalid y initially
            this.y = -1;
        }

        public @Override void setForeColor(Color foreColor) {
            if (!foreColor.equals(this.foreColor)) {
                flush();
                this.foreColor = foreColor;
            }
        }

        public @Override void setBackColor(Color backColor) {
            if (!backColor.equals(this.backColor)) {
                flush();
                this.backColor = backColor;
            }
        }

        public @Override void setStrikeThroughColor(Color strikeThroughColor) {
            if ((strikeThroughColor != this.strikeThroughColor)
                && (strikeThroughColor == null
                    || !strikeThroughColor.equals(this.strikeThroughColor))
            ) {
                flush();
                this.strikeThroughColor = strikeThroughColor;
            }
        }

        public @Override void setUnderlineColor(Color underlineColor) {
            if ((underlineColor != this.underlineColor)
                && (underlineColor == null
                    || !underlineColor.equals(this.underlineColor))
            ) {
                flush();
                this.underlineColor = underlineColor;
            }
        }

        public @Override void setWaveUnderlineColor(Color waveUnderlineColor) {
            if ((waveUnderlineColor != this.waveUnderlineColor)
                && (waveUnderlineColor == null
                    || !waveUnderlineColor.equals(this.waveUnderlineColor))
            ) {
                flush();
                this.waveUnderlineColor = waveUnderlineColor;
            }
        }

        public @Override void setFont(Font font) {
            if (!font.equals(this.font)) {
                flush();
                this.font = font;
            }
        }

        public @Override void setX(int x) {
            if (x != this.x) {
                flush();
                this.x = x;
            }
        }

        public @Override void setY(int y) {
            if (y != this.y) {
                flush();
                this.y = y;
            }
        }

        public @Override void init(DrawContext ctx) {
            component = ctx.getEditorUI().getComponent();
            // initialize reference to annotations
            annos = ctx.getEditorUI().getDocument().getAnnotations();
            drawTextLimitLine = ctx.getEditorUI().textLimitLineVisible;
            textLimitWidth = ctx.getEditorUI().textLimitWidth();
            defaultSpaceWidth = ctx.getEditorUI().defaultSpaceWidth;
            textLimitLineColor = ctx.getEditorUI().textLimitLineColor;
            absoluteX = ctx.getEditorUI().getTextMargin().left;
            maxWidth = ctx.getEditorUI().getExtentBounds().width;
            
            componentListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if (DrawLayer.TEXT_FRAME_START_POSITION_COMPONENT_PROPERTY.equals(evt.getPropertyName())) {
                        if (evt.getNewValue() instanceof Position) {
                            frameStartOffset = ((Position)evt.getNewValue()).getOffset();
                        } else {
                            frameStartOffset = Integer.MAX_VALUE;
                        }
                    }
                    if (DrawLayer.TEXT_FRAME_END_POSITION_COMPONENT_PROPERTY.equals(evt.getPropertyName())) {
                        if (evt.getNewValue() instanceof Position) {
                            frameEndOffset = ((Position)evt.getNewValue()).getOffset();
                        } else {
                            frameEndOffset = Integer.MAX_VALUE;
                        }
                    }
                }
            };
            component.addPropertyChangeListener(componentListener);
        }

        public @Override void finish() {
            // flush() already performed in setBuffer(null) and might cause problems here
            // as this code is typically called from finally clause.
            //flush();
            if (component != null) {
                component.removePropertyChangeListener(componentListener);
            }
        }
        
        public @Override void setView(View view){
            this.view = view;
        }

        private void flush() {
            flush(false);
        }


        private void flush(boolean atEOL) {
            if (y < 0) { // not yet initialized
                return ;
            }
            
            if (startOffset >= 0 && startOffset != endOffset) { // some text on the line
                // First possibly fill the rectangle
                fillRectImpl(startX, startY, x - startX);
            }
            
            // #33165 - for each fragment getPasiveAnnotations() was called
            // but it can done just once per line.
            if (lastDrawnAnnosY != y) {
                lastDrawnAnnosY = y;
                lastDrawnAnnosX = 0;
                if (AnnotationTypes.getTypes().isBackgroundDrawing().booleanValue()) {
                    if (alpha == null)
                        alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, AnnotationTypes.getTypes().getBackgroundGlyphAlpha().intValue() / 100f);
                    if (view!=null){
                        passiveAnnosAtY = annos.getPassiveAnnotations(view.getStartOffset());                        
                    }
                } else {
                    passiveAnnosAtY = null;
                }
            }
            
            int glyphX=2;
            if (passiveAnnosAtY != null) {
                Graphics2D g2d = (Graphics2D) graphics;

                Shape shape = graphics.getClip();

                // set alpha composite
                Composite origin = g2d.getComposite();
                g2d.setComposite(alpha);

                // clip the drawing area
                int clipX = atEOL ? Integer.MAX_VALUE : x;
                int clipY = Math.min(lastDrawnAnnosX, this.startX);
                Rectangle clip = new Rectangle(clipY, y, clipX - clipY, lineHeight);
                lastDrawnAnnosX = clipX;
                clip = clip.intersection(shape.getBounds());
                graphics.setClip(clip);

                for (int i=0; i < passiveAnnosAtY.length; i++) {
                    g2d.drawImage(passiveAnnosAtY[i].getGlyph(), glyphX, y, null);
                    glyphX += passiveAnnosAtY[i].getGlyph().getWidth(null)+1;
                }

                // restore original clip region
                graphics.setClip(shape);

                // restore original ocmposite
                g2d.setComposite(origin);
            }

            // If no text on the line then return + handle incorrect conditions
            if (startOffset < 0 || startOffset >= endOffset || endOffset > buffer.length) {
                startOffset = -1;
                return;
            }

            
            if (drawTextLimitLine) { // draw limit line
                Rectangle clip = graphics.getClipBounds();
                int lineX = absoluteX + textLimitWidth * defaultSpaceWidth;
                if (lineX >= startX && lineX <= x){
                    Color bakColor = graphics.getColor();
                    graphics.setColor(textLimitLineColor);
                    graphics.drawLine(lineX, startY, lineX, startY + lineHeight);
                    graphics.setColor(bakColor);
                }
            }

            // Text framing support
            if (frameStartOffset != Integer.MAX_VALUE && bufferStartOffset != -1) {
                if (bufferStartOffset + startOffset == frameStartOffset) { // draw vertical line
                    graphics.drawLine(startX, startY, startX, startY + lineHeight);
                }
                if (bufferStartOffset + startOffset >= frameStartOffset
                        && bufferStartOffset + endOffset <= frameEndOffset
                ) {
                    graphics.drawLine(startX, startY, x, startY);
                    graphics.drawLine(startX, startY, x, startY + lineHeight);
                }
                if (bufferStartOffset + endOffset == frameEndOffset) { // draw vertical line
                    graphics.drawLine(x, startY, x, startY + lineHeight);
                }
            }
            
            // Check whether the graphics uses right color
            graphics.setColor(foreColor);
            // Check whether the graphics uses right font
            graphics.setFont(font);

            if (debug) {
                String text = new String(buffer, startOffset, endOffset - startOffset);
                System.out.println("DrawGraphics: text='" + text // NOI18N
                    + "', text.length=" + text.length() // NOI18N
                    + ", x=" + startX + ", y=" + startY // NOI18N
                    + ", ascent=" + lineAscent // NOI18N
                    + ", clip=" + graphics.getClipBounds() // NOI18N
                    + ", color=" + graphics.getColor() // NOI18N
                );
            }

            graphics.drawChars(buffer, startOffset, endOffset - startOffset,
                               startX, startY + lineAscent);

            if (strikeThroughColor != null) { // draw strike-through
                FontMetricsCache.Info fmcInfo = FontMetricsCache.getInfo(font);
                graphics.setColor(strikeThroughColor);
                graphics.fillRect(startX,
                                  (int)(startY + fmcInfo.getStrikethroughOffset(graphics) + lineAscent + 1.5),
                                  x - startX,
                                  Math.max(1, Math.round(fmcInfo.getStrikethroughThickness(graphics)))
                                 );
            }

            if (waveUnderlineColor != null) { // draw wave underline
                FontMetricsCache.Info fmcInfo = FontMetricsCache.getInfo(font);
                graphics.setColor(waveUnderlineColor);

                int waveLength = x - startX;                
                if (waveLength > 0) {
                    int[] wf = {0, 0, -1, -1};
                    int[] xArray = new int[waveLength + 1];
                    int[] yArray = new int[waveLength + 1];
                    
                    int yBase = (int)(startY + fmcInfo.getUnderlineOffset(graphics) + lineAscent + 1.5);
                    for (int i=0;i<=waveLength;i++) {
                        xArray[i]=startX + i;
                        yArray[i]=yBase + wf[xArray[i] % 4];                    
                    }                    
                    graphics.drawPolyline(xArray, yArray, waveLength);
                }
            }

            if (underlineColor != null) { // draw underline
                FontMetricsCache.Info fmcInfo = FontMetricsCache.getInfo(font);
                graphics.setColor(underlineColor);
                graphics.fillRect(startX,
                                  (int)(startY + fmcInfo.getUnderlineOffset(graphics) + lineAscent + 1.5),
                                  x - startX,
                                  Math.max(1, Math.round(fmcInfo.getUnderlineThickness(graphics)))
                                 );
            }

            startOffset = -1; // signal no characters to draw
        }

        public @Override Graphics getGraphics() {
            return graphics;
        }

        public @Override boolean supportsLineNumbers() {
            return true;
        }

        public @Override void fillRect(int width) {
            fillRectImpl(x, y, width);
            x += width;
        }

        private void fillRectImpl(int rx, int ry, int width) {
            if (width > 0) { // only for non-zero width
                // only fill for different color than current background
                if (!backColor.equals(defaultBackColor)) {
                    graphics.setColor(backColor);
                    graphics.fillRect(rx, ry, width, lineHeight);
                }

            }
        }


        public @Override void drawChars(int offset, int length, int width) {
            if (length >= 0) {
                if (startOffset < 0) { // no token yet
                    startOffset = offset;
                    endOffset = offset + length;
                    this.startX = x;
                    this.startY = y;
                    this.width = width;

                } else { // already token before
                    endOffset += length;
                }
            }

            x += width;
        }

        public @Override void drawTabs(int offset, int length, int spaceCount, int width) {
            if (width > 0) {
                flush();
                fillRectImpl(x, y, width);
                x += width;
            }
        }

        public @Override void setBuffer(char[] buffer) {
            flush();
            this.buffer = buffer;
            startOffset = -1;
            bufferStartOffset = -1;
        }
        
        void setBufferStartOffset(int bufferStartOffset) {
            this.bufferStartOffset = bufferStartOffset;
        }

        public @Override void eol() {
            if (drawTextLimitLine) { // draw limit line
                int lineX = absoluteX + textLimitWidth * defaultSpaceWidth;
                if (lineX >= x-defaultSpaceWidth){
                    Color bakColor = graphics.getColor();
                    graphics.setColor(textLimitLineColor);
                    Rectangle clipB = graphics.getClipBounds();
                    if (clipB.width + clipB.x <= lineX && clipB.x < maxWidth) {
                        graphics.setClip(clipB.x, clipB.y, maxWidth - clipB.x, clipB.height);
                        graphics.drawLine(lineX, y, lineX, y + lineHeight);
                        graphics.setClip(clipB.x, clipB.y, clipB.width, clipB.height);
                    }else{
                        graphics.drawLine(lineX, y, lineX, y + lineHeight);
                    }
                    graphics.setColor(bakColor);
                }
            }
            flush(true);
        }

    } // End of GraphicsDG class

    static final class PrintDG extends SimpleDG {

        PrintContainer container;

        /** Whether there were some paints already on the line */
        boolean lineInited;

        /** Construct the new print graphics
        * @param container print container to which the tokens
        *   are added.
        */
        public PrintDG(PrintContainer container) {
            this.container = container;
        }

        public @Override boolean supportsLineNumbers() {
            return true;
        }

        public @Override void drawChars(int offset, int length, int width) {
            if (length > 0) {
                lineInited = true; // Fixed 42536
                char[] chars = new char[length];
                System.arraycopy(buffer, offset, chars, 0, length);
                container.add(chars, font, foreColor, backColor);
            }
        }

        private void printSpaces(int spaceCount) {
            char[] chars = new char[spaceCount];
            System.arraycopy(Analyzer.getSpacesBuffer(spaceCount), 0, chars, 0, spaceCount);
            container.add(chars, font, foreColor, backColor);
        }

        public @Override void drawTabs(int offset, int length, int spaceCount, int width) {
            lineInited = true; // Fixed 42536
            printSpaces(spaceCount);
        }

        public @Override void eol() {
            if (!lineInited && container.initEmptyLines()) {
                printSpaces(1);
            }
            container.eol();
            lineInited = false; // signal that the next line is not inited yet
        }

    } // End of PrintDG class

}
