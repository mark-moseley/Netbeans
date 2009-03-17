/*
 * Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package org.netbeans.lib.profiler.charts.canvas;

import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JScrollBar;

/**
 *
 * @author Jiri Sedlacek
 */
public abstract class InteractiveCanvasComponent extends TransformableCanvasComponent {
    
    private ScrollBarManager hScrollBarManager;
    private ScrollBarManager vScrollBarManager;

    private MousePanHandler mousePanHandler;
    private int mousePanningButton;
    private Cursor mousePanningCursor;

    private double mouseZoomingFactor;
    private MouseZoomHandler mouseZoomHandler;


    public InteractiveCanvasComponent() {
        mousePanningButton = MouseEvent.BUTTON1;
        mousePanningCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
        enableMousePanning();

        mouseZoomingFactor = 0.05d;
        enableMouseZooming();
    }
    

    public final void attachHorizontalScrollBar(JScrollBar scrollBar) {
        if (hScrollBarManager == null) hScrollBarManager = new ScrollBarManager();
        hScrollBarManager.attachScrollBar(scrollBar, true);
    }

    public final void detachHorizontalScrollBar() {
        if (hScrollBarManager != null) hScrollBarManager.detachScrollBar();
        hScrollBarManager = null;
    }

    public final void attachVerticalScrollBar(JScrollBar scrollBar) {
        if (vScrollBarManager == null) vScrollBarManager = new ScrollBarManager();
        vScrollBarManager.attachScrollBar(scrollBar, false);
    }

    public final void detachVerticalScrollBar() {
        if (vScrollBarManager != null) vScrollBarManager.detachScrollBar();
        vScrollBarManager = null;
    }
    
    
    // --- Private implementation ----------------------------------------------

    private void updateScrollBars(boolean valueOnly) {
        if (hScrollBarManager != null) hScrollBarManager.syncScrollBar(valueOnly);
        if (vScrollBarManager != null) vScrollBarManager.syncScrollBar(valueOnly);
    }

    protected void offsetChanged(long oldOffsetX, long oldOffsetY,
                                 long newOffsetX, long newOffsetY) {
        super.offsetChanged(oldOffsetX, oldOffsetY, newOffsetX, newOffsetY);
        updateScrollBars(true);
    }

    protected void scaleChanged(double oldScaleX, double oldScaleY,
                                double newScaleX, double newScaleY) {
        super.scaleChanged(oldScaleX, oldScaleY, newScaleX, newScaleY);
        updateScrollBars(false);
    }

    protected void dataBoundsChanged(long dataOffsetX, long dataOffsetY,
                                     long dataWidth, long dataHeight,
                                     long oldDataOffsetX, long oldDataOffsetY,
                                     long oldDataWidth, long oldDataHeight) {
        super.dataBoundsChanged(dataOffsetX, dataOffsetY, dataWidth, dataHeight,
                                oldDataOffsetX, oldDataOffsetY, oldDataWidth, oldDataHeight);
        updateScrollBars(false);
    }

    protected void reshaped(Rectangle oldBounds, Rectangle newBounds) {
        super.reshaped(oldBounds, newBounds);
        updateScrollBars(false);
    }


    // --- ScrollBarManager ----------------------------------------------------

    private class ScrollBarManager implements AdjustmentListener, MouseWheelListener {

        private static final int SCROLLBAR_UNIT_INCREMENT = 20;

        private JScrollBar scrollBar;
        private double scrollBarFactor;
        boolean horizontal;

        boolean internalChange;


        public void attachScrollBar(JScrollBar scrollBar, boolean horizontal) {
            if (this.scrollBar == scrollBar) return;
            if (this.scrollBar != null) detachScrollBar();
            this.scrollBar = scrollBar;
            this.horizontal = horizontal;
            scrollBar.addAdjustmentListener(this);
            scrollBar.addMouseWheelListener(this);
            if (!horizontal)
                InteractiveCanvasComponent.this.addMouseWheelListener(this);
        }

        public void detachScrollBar() {
            if (scrollBar == null) return;
            if (!horizontal)
                InteractiveCanvasComponent.this.removeMouseWheelListener(this);
            scrollBar.removeMouseWheelListener(this);
            scrollBar.removeAdjustmentListener(this);
            scrollBar = null;
        }

        public void syncScrollBar(boolean valueOnly) {
            internalChange = true;

            if (valueOnly) {

                long offsetX = getOffsetX();
                long offsetY = getOffsetY();

                int value = horizontal ? getInt(offsetX) : getInt(offsetY);
                if (reversedValue()) value = scrollBar.getMaximum() -
                                           scrollBar.getVisibleAmount() - value;

                scrollBar.setValue(value);

            } else {

                updateFactor();

                long offsetX = getOffsetX();
                long offsetY = getOffsetY();
                long maxOffsetX = getMaxOffsetX();
                long maxOffsetY = getMaxOffsetY();

                int value   = horizontal ? getInt(offsetX) : getInt(offsetY);
                int extent  = horizontal ? getInt(getWidth()) : getInt(getHeight());
                int maximum = horizontal ? getInt(maxOffsetX) : getInt(maxOffsetY);

                int unitIncr =  horizontal ? getInt(SCROLLBAR_UNIT_INCREMENT) :
                                       getInt(SCROLLBAR_UNIT_INCREMENT);
                int blockIncr = horizontal ? getInt(getWidth() - 20) :
                                       getInt(getHeight() - 20);

                if (reversedValue()) value = maximum - value;

                scrollBar.setEnabled(maximum > 0);
                scrollBar.setValues(value, extent, 0, maximum + extent);
                scrollBar.setUnitIncrement(unitIncr);
                scrollBar.setBlockIncrement(blockIncr);

            }

            internalChange = false;
        }

        public void adjustmentValueChanged(AdjustmentEvent e) {
            if (internalChange) return;

            if (e.getValueIsAdjusting() && !isOffsetAdjusting())
                offsetAdjustingStarted();
            else if (!e.getValueIsAdjusting() && isOffsetAdjusting())
                offsetAdjustingFinished();
            
            if (horizontal) setOffset(getValue(), getOffsetY());
            else setOffset(getOffsetX(), getValue());

            repaintDirtyAccel();
//            repaintDirty();
        }

        public long getValue() {
            long value = scrollBar.getValue();
            if (reversedValue()) value = scrollBar.getMaximum() -
                                         scrollBar.getVisibleAmount() - value;
            return (long)((double)value / scrollBarFactor);
        }


        private void updateFactor() {
            long maxOffsetX = getMaxOffsetX();
            long maxOffsetY = getMaxOffsetY();
            
            if (horizontal) {
                int width = getWidth();
                scrollBarFactor = ((maxOffsetX + width) > Integer.MAX_VALUE) ?
                ((double)Integer.MAX_VALUE / (double)(maxOffsetX + width)) : 1;
            } else {
                int height = getHeight();
                scrollBarFactor = ((maxOffsetY + height) > Integer.MAX_VALUE) ?
                ((double)Integer.MAX_VALUE / (double)(maxOffsetY + height)) : 1;
            }
        }

        private boolean reversedValue() {
            return horizontal ? isRightBased() : isBottomBased();
        }

        private int getInt(long value) {
            return (int)((double)value * scrollBarFactor);
        }


        public void mouseWheelMoved(MouseWheelEvent e) {
            // Mouse wheel zooming takes precedence over scrolling
            if (isMouseZoomingEnabled() &&
                e.getSource() == InteractiveCanvasComponent.this) return;

            // Change the ScrollBar value
            if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                int unitsToScroll = e.getUnitsToScroll();
                int direction = unitsToScroll < 0 ? -1 : 1;
                if (unitsToScroll != 0) {
                    int increment = scrollBar.getUnitIncrement(direction);
                    int oldValue = scrollBar.getValue();
                    int newValue = oldValue + increment * unitsToScroll;
                    newValue = Math.max(Math.min(newValue, scrollBar.getMaximum() -
                            scrollBar.getVisibleAmount()), scrollBar.getMinimum());
                    if (oldValue != newValue) scrollBar.setValue(newValue);
                }
            }
        }

    }


    // --- Mouse panning support -----------------------------------------------

    public final void setMousePanningEnabled(boolean enabled) {
        if (enabled) enableMousePanning();
        else disableMousePanning();
    }

    public final void enableMousePanning() {
        if (mousePanHandler != null) return;

        mousePanHandler = new MousePanHandler();
        addMouseListener(mousePanHandler);
        addMouseMotionListener(mousePanHandler);
    }

    public final void disableMousePanning() {
        if (mousePanHandler == null) return;

        removeMouseListener(mousePanHandler);
        removeMouseMotionListener(mousePanHandler);
        mousePanHandler = null;

        setCursor(Cursor.getDefaultCursor());
    }

    public final boolean isMousePanningEnabled() {
        return mousePanHandler != null;
    }

    public final boolean panningPossible() {
        return getContentsWidth() > getWidth() ||
               getContentsHeight() > getHeight();
    }

    public final void setMousePanningButton(int mousePanningButton) {
        this.mousePanningButton = mousePanningButton;
    }

    public final int getMousePanningButton() {
        return mousePanningButton;
    }

    public final void setMousePanningCursor(Cursor mousePanningCursor) {
        this.mousePanningCursor = mousePanningCursor;
    }

    public final Cursor getMousePanningCursor() {
        return mousePanningCursor;
    }

    private class MousePanHandler extends MouseAdapter implements MouseMotionListener {

        private boolean dragging;
        private int lastMouseDragX;
        private int lastMouseDragY;

        public void mousePressed(MouseEvent e) {
            dragging = panningPossible() && e.getButton() == mousePanningButton;
            if (!dragging) return;

            lastMouseDragX = e.getX();
            lastMouseDragY = e.getY();

            if (mousePanningCursor != null && isMousePanningEnabled())
                setCursor(mousePanningCursor);

//            if (!isOffsetAdjusting()) offsetAdjustingStarted();
        }

        public void mouseReleased(MouseEvent e) {
            dragging = false;
            if (mousePanningCursor != null) setCursor(Cursor.getDefaultCursor());

//            if (isOffsetAdjusting()) offsetAdjustingFinished();
        }

        public void mouseDragged(MouseEvent e) {
            if (!dragging) return;

            int mouseDragX = e.getX();
            int mouseDragY = e.getY();

            long oldOffsetX = getOffsetX();
            long oldOffsetY = getOffsetY();

            if (lastMouseDragX != 0 && lastMouseDragY != 0) {
                int mouseDragDx = isRightBased()  ? mouseDragX - lastMouseDragX :
                                                    lastMouseDragX - mouseDragX;
                int mouseDragDy = isBottomBased() ? mouseDragY - lastMouseDragY :
                                                    lastMouseDragY - mouseDragY;
                
                setOffset(oldOffsetX + mouseDragDx, oldOffsetY + mouseDragDy);
                
                repaintDirtyAccel();
//                repaintDirty();
            }

            if (getOffsetX() != oldOffsetX) lastMouseDragX = mouseDragX;
            if (getOffsetY() != oldOffsetY) lastMouseDragY = mouseDragY;
        }

        public void mouseMoved(MouseEvent e) {}
    }


    // --- Mouse zooming support -----------------------------------------------

    public final void setMouseZoomingEnabled(boolean enabled) {
        if (enabled) enableMouseZooming();
        else disableMouseZooming();
    }

    public final void enableMouseZooming() {
        if (mouseZoomHandler != null) return;

        mouseZoomHandler = new MouseZoomHandler();
        addMouseWheelListener(mouseZoomHandler);
        addMouseMotionListener(mouseZoomHandler);
    }

    public final void disableMouseZooming() {
        if (mouseZoomHandler == null) return;

        removeMouseWheelListener(mouseZoomHandler);
        removeMouseMotionListener(mouseZoomHandler);
        mouseZoomHandler = null;
    }

    public final boolean isMouseZoomingEnabled() {
        return mouseZoomHandler != null;
    }

    public final double getMouseZoomingFactor() {
        return mouseZoomingFactor;
    }

    public final void setMouseZoomingFactor(double mouseZoomingFactor) {
        this.mouseZoomingFactor = mouseZoomingFactor;
    }

    private class MouseZoomHandler implements MouseWheelListener, MouseMotionListener {
//        private double cachedScaleX;
//        private double cachedScaleY;
        private double cachedScaleRatio;
        private long scrolledAmount;
        private boolean cachedValuesValid;

        private long cachedDataX;
        private long cachedDataY;


        public void mouseMoved(MouseEvent e) {
            cachedValuesValid = false;
        }

        public void mouseDragged(MouseEvent e) {}

        public void mouseWheelMoved(MouseWheelEvent e) {
            if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {

                if (fitsWidth() && fitsHeight()) return;

                int x = e.getX();
                int y = e.getY();
                double scaleX = getScaleX();
                double scaleY = getScaleY();

                if (!cachedValuesValid) {
                    cachedDataX = getDataX(x);
                    cachedDataY = getDataY(y);
//                    cachedScaleX = scaleX;
//                    cachedScaleY = scaleY;
                    cachedScaleRatio = fitsWidth() || fitsHeight() ?
                                       1 : scaleY / scaleX;
//                    scrolledAmount = 0;
                    cachedValuesValid = true;
                }

                int unitsToScroll = e.getUnitsToScroll();
                if (unitsToScroll > 0 && scaleX * scaleY != 0 || unitsToScroll < 0) {
                    scrolledAmount -= unitsToScroll;
//                    double zoomChange = -(double)scrolledAmount * mouseZoomingFactor;
//
//                    double newScaleX = Math.max(cachedScaleX + zoomChange, 0);
//                    double newScaleY = Math.max(cachedScaleY + zoomChange * cachedScaleRatio, 0);

                    double newScaleX = fitsWidth() ? getScaleX() :
                        Math.pow(1d + mouseZoomingFactor, scrolledAmount);
                    double newScaleY = fitsHeight() ? getScaleY() :
                        Math.pow((1d + mouseZoomingFactor) * cachedScaleRatio, scrolledAmount);

                    setScale(newScaleX, newScaleY);

                    long newX = getViewX(cachedDataX);
                    long newY = getViewY(cachedDataY);
                    long dx = isRightBased() ? x - newX : newX - x;
                    long dy = isBottomBased() ? y - newY : newY - y;
                    setOffset(getOffsetX() + dx, getOffsetY() + dy);

                    repaint();
                }
            }
        }
    }

}
