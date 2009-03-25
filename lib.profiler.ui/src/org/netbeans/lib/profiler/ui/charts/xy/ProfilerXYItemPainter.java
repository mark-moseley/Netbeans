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

package org.netbeans.lib.profiler.ui.charts.xy;

import org.netbeans.lib.profiler.charts.Utils;
import org.netbeans.lib.profiler.charts.ChartContext;
import org.netbeans.lib.profiler.charts.ChartItem;
import org.netbeans.lib.profiler.charts.ChartItemChange;
import org.netbeans.lib.profiler.charts.ItemSelection;
import org.netbeans.lib.profiler.charts.LongRect;
import org.netbeans.lib.profiler.charts.xy.XYItemChange;
import org.netbeans.lib.profiler.charts.xy.XYItemPainter;
import org.netbeans.lib.profiler.charts.xy.XYItemSelection;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.List;
import org.netbeans.lib.profiler.charts.xy.XYItem;

/**
 *
 * @author Jiri Sedlacek
 */
public class ProfilerXYItemPainter extends XYItemPainter.Abstract {

    private static final int TYPE_ABSOLUTE = 0;
    private static final int TYPE_RELATIVE = 1;

    private final int lineWidth;
    private final Color lineColor;
    private final Color fillColor;

    private final Stroke lineStroke;

    private final int type;
    private final int maxOffset;


    // --- Constructor ---------------------------------------------------------

    public static ProfilerXYItemPainter absolutePainter(float lineWidth,
                                                       Color lineColor,
                                                       Color fillColor) {
        
        return new ProfilerXYItemPainter(lineWidth, lineColor, fillColor,
                                         TYPE_ABSOLUTE, 0);
    }

    public static ProfilerXYItemPainter relativePainter(float lineWidth,
                                                       Color lineColor,
                                                       Color fillColor,
                                                       int maxOffset) {

        return new ProfilerXYItemPainter(lineWidth, lineColor, fillColor,
                                         TYPE_RELATIVE, maxOffset);
    }


    private ProfilerXYItemPainter(float lineWidth, Color lineColor, Color fillColor,
                                  int type, int maxOffset) {

        if (lineColor == null && fillColor == null)
            throw new IllegalArgumentException("No parameters defined"); // NOI18N

        this.lineWidth = (int)Math.ceil(lineWidth);
        this.lineColor = Utils.checkedColor(lineColor);
        this.fillColor = Utils.checkedColor(fillColor);

        this.lineStroke = new BasicStroke(lineWidth, BasicStroke.CAP_ROUND,
                                          BasicStroke.JOIN_ROUND);

        this.type = type;
        this.maxOffset = maxOffset;
    }


    // --- ItemPainter implementation ------------------------------------------
    
    public LongRect getItemBounds(ChartItem item) {
//        if (!(item instanceof XYItem))
//            throw new UnsupportedOperationException("Unsupported item: " + item); // NOI18N

        XYItem xyItem = (XYItem)item;
        if (type == TYPE_ABSOLUTE) {
            return getDataBounds(xyItem.getBounds());
        } else {
            LongRect itemBounds1 = new LongRect(xyItem.getBounds());
            itemBounds1.y = 0;
            itemBounds1.height = 0;
            return itemBounds1;
        }
    }

    public LongRect getItemBounds(ChartItem item, ChartContext context) {
//        if (!(item instanceof XYItem))
//            throw new UnsupportedOperationException("Unsupported item: " + item); // NOI18N

        XYItem xyItem = (XYItem)item;
        if (type == TYPE_ABSOLUTE) {
            return getViewBounds(xyItem.getBounds(), context);
        } else {
            return getViewBoundsRelative(xyItem.getBounds(), xyItem, context);
        }
    }


    public boolean isBoundsChange(ChartItemChange itemChange) {
//        if (!(itemChange instanceof XYItemChange))
//            throw new UnsupportedOperationException("Unsupported itemChange: " + itemChange);

        // Items can only be added => always bounds change
        XYItemChange change = (XYItemChange)itemChange;
        return !LongRect.equals(change.getOldValuesBounds(),
                                change.getNewValuesBounds());
    }

    public boolean isAppearanceChange(ChartItemChange itemChange, ChartContext context) {
//        if (!(itemChange instanceof XYItemChange))
//            throw new UnsupportedOperationException("Unsupported itemChange: " + itemChange);
        
        // Items can only be added => always appearance change
        XYItemChange change = (XYItemChange)itemChange;
        LongRect dirtyBounds = change.getDirtyValuesBounds();
        return dirtyBounds.width != 0 || dirtyBounds.height != 0;
    }

    public LongRect getDirtyBounds(ChartItemChange itemChange, ChartContext context) {
//        if (!(itemChange instanceof XYItemChange))
//            throw new UnsupportedOperationException("Unsupported itemChange: " + itemChange);
        
        // Items can only be added => always dirty bounds for last value
        XYItemChange change = (XYItemChange)itemChange;
        if (type == TYPE_ABSOLUTE) {

            return getViewBounds(change.getDirtyValuesBounds(), context);
        } else {
            LongRect oldValuesBounds = change.getOldValuesBounds();
            LongRect newValuesBounds = change.getNewValuesBounds();
            if (oldValuesBounds.y != newValuesBounds.y ||
                oldValuesBounds.height != newValuesBounds.height) {

                return getItemBounds(change.getItem(), context);
            } else {
                return getViewBoundsRelative(change.getDirtyValuesBounds(),
                                             change.getItem(), context);
            }
//            return new LongRect(0, 0, context.getViewportWidth(), context.getViewportHeight());
        }
//        return new LongRect(0, 0, context.getViewportWidth(), context.getViewportHeight());
    }


    public double getItemValue(double viewY, XYItem item, ChartContext context) {
        if (type == TYPE_ABSOLUTE) {
            return super.getItemValue(viewY, item, context);
        } else {
            double itemValueFactor = (double)context.getDataHeight() /
                                     (double)item.getBounds().height;
            return context.getDataY(viewY) / itemValueFactor;
        }
    }

    public double getItemValueScale(XYItem item, ChartContext context) {
        if (type == TYPE_ABSOLUTE) {
            return super.getItemValueScale(item, context);
        } else {
            double itemValueFactor = (double)context.getDataHeight() /
                                     (double)item.getBounds().height;
//            System.err.println(">>> itemValueFactor: " + itemValueFactor);
            return context.getDataHeight(itemValueFactor);
        }
    }


    public boolean supportsHovering(ChartItem item) {
        return false;
    }

    public boolean supportsSelecting(ChartItem item) {
        return false;
    }

    public LongRect getSelectionBounds(ItemSelection selection, ChartContext context) {
        throw new UnsupportedOperationException("getSelectionBounds() not supported"); // NOI18N
    }

    public XYItemSelection getClosestSelection(ChartItem item, int viewX,
                                                       int viewY, ChartContext context) {
        return null;
    }

    public void paintItem(ChartItem item, List<ItemSelection> highlighted,
                          List<ItemSelection> selected, Graphics2D g,
                          Rectangle dirtyArea, ChartContext context) {
//        if (!(item instanceof XYItem))
//            throw new UnsupportedOperationException("Unsupported item: " + item); // NOI18N
//        if (!(context instanceof ProfilerXYChartComponent.Context))
//            throw new UnsupportedOperationException("Unsupported context: " + context);
        
        paint((XYItem)item, highlighted, selected, g, dirtyArea,
              (ProfilerXYChart.Context)context);
    }


    // --- Private implementation ----------------------------------------------

    private LongRect getDataBounds(LongRect itemBounds) {
        LongRect bounds = new LongRect(itemBounds);

        if (fillColor != null) {
            bounds.height += bounds.y;
            bounds.y = 0;
        }

        return bounds;
    }

    private LongRect getViewBounds(LongRect itemBounds, ChartContext context) {
        LongRect dataBounds = getDataBounds(itemBounds);

        LongRect viewBounds = context.getViewRect(dataBounds);
        LongRect.addBorder(viewBounds, lineWidth);

        return viewBounds;
    }

    private LongRect getViewBoundsRelative(LongRect dataBounds, XYItem item,
                                           ChartContext context) {
        LongRect itemBounds = item.getBounds();

        double itemValueFactor = ((double)context.getDataHeight() /*-
                                 context.getDataHeight(maxOffset)*/) /
                                 ((double)itemBounds.height);

        // TODO: fix the math!!!
        double value1 = context.getDataOffsetY() + itemValueFactor *
                      (double)(dataBounds.y - itemBounds.y);
        double value2 = context.getDataOffsetY() + itemValueFactor *
                      (double)(dataBounds.y + dataBounds.height - itemBounds.y);

        long viewX = (long)Math.ceil(context.getViewX(dataBounds.x));
        long viewWidth = (long)Math.ceil(context.getViewWidth(dataBounds.width));
        if (context.isRightBased()) viewX -= viewWidth;

        long viewY1 = (long)Math.ceil(context.getViewY(value1));
        long viewY2 = (long)Math.ceil(context.getViewY(value2));
        long viewHeight = context.isBottomBased() ? viewY1 - viewY2 :
                                                    viewY2 - viewY1;
        if (!context.isBottomBased()) viewY2 -= viewHeight;

        LongRect viewBounds =  new LongRect(viewX, viewY2, viewWidth, viewHeight);
        LongRect.addBorder(viewBounds, lineWidth);

        return viewBounds;
    }

    
    private void paint(XYItem item, List<ItemSelection> highlighted,
                       List<ItemSelection> selected, Graphics2D g,
                       Rectangle dirtyArea, ProfilerXYChart.Context context) {

        if (item.getValuesCount() < 2) return;

        int[][] points = createPoints(item, dirtyArea, context, type, maxOffset);
        int[] xPoints  = points[0];
        int[] yPoints  = points[1];
        int npoints = xPoints.length;

//long start = System.currentTimeMillis();
        if (fillColor != null) {
            int zeroY = (int)context.getViewY(0);
            Polygon polygon = new Polygon();
            polygon.xpoints = xPoints;
            polygon.ypoints = yPoints;
            polygon.npoints = npoints;
            polygon.xpoints[npoints - 2] = xPoints[npoints - 3];
            polygon.ypoints[npoints - 2] = zeroY;
            polygon.xpoints[npoints - 1] = xPoints[0];
            polygon.ypoints[npoints - 1] = zeroY;
            g.setPaint(fillColor);
            g.fill(polygon);
        }

        if (lineColor != null) {
            g.setPaint(lineColor);
            g.setStroke(lineStroke);
            g.drawPolyline(xPoints, yPoints, npoints - 2);
        }
//System.err.println(">>> Paint: " + (System.currentTimeMillis() - start) + " [ms], dirtyArea: " + dirtyArea);
//        if (type == TYPE_RELATIVE) {
//        g.setColor(Color.RED);
//        Rectangle bbox = new Rectangle(dirtyArea);
////        bbox.width -= 1;
////        bbox.height -= 1;
//            g.draw(bbox);
////            System.err.println(">>> Here");
//        }

//        if (type == TYPE_RELATIVE_BOUNDED) {
//            System.err.println(">>> paintItem, dirtyArea: " + dirtyArea);
//        }
        
    }

    private static int[][] createPoints(XYItem item, Rectangle dirtyArea,
                                 ProfilerXYChart.Context context,
                                 int type, int maxOffset) {
        
        int[] visibleBounds   = context.getVisibleBounds(dirtyArea);
        int firstVisibleIndex = visibleBounds[0];
        int lastVisibleIndex  = visibleBounds[1];
        int visibleIndexes    = lastVisibleIndex - firstVisibleIndex + 1;

        int extraFirstIndex = firstVisibleIndex == 0 ? 0 : 1;
        int extraLastIndex  = lastVisibleIndex == item.getValuesCount() - 1 ? 0 : 1;

        int[] xPoints = new int[visibleIndexes + extraFirstIndex + extraLastIndex + 2];
        int[] yPoints = new int[visibleIndexes + extraFirstIndex + extraLastIndex + 2];


        double itemValueFactor = type == TYPE_RELATIVE ?
                                         ((double)context.getDataHeight() /*-
                                          context.getDataHeight(maxOffset)*/) /
                                         ((double)item.getBounds().height) : 0;

        for (int i = 0; i < visibleIndexes; i++) {
            xPoints[i + extraFirstIndex] = ChartContext.getCheckedIntValue(Math.ceil(
                                        context.getViewX(item.getXValue(firstVisibleIndex + i))));
            yPoints[i + extraFirstIndex] = ChartContext.getCheckedIntValue(Math.ceil(
                                        getYValue(item, firstVisibleIndex + i,
                                        type, context, itemValueFactor)));
        }

        if (extraFirstIndex == 1) {
            xPoints[0] = ChartContext.getCheckedIntValue(Math.ceil(context.getViewX(
                                      item.getXValue(firstVisibleIndex - 1))));
            yPoints[0] = ChartContext.getCheckedIntValue(Math.ceil(
                                      getYValue(item, firstVisibleIndex - 1,
                                      type, context, itemValueFactor)));
        }

        if (extraLastIndex == 1) {
            xPoints[xPoints.length - 3] = ChartContext.getCheckedIntValue(Math.ceil(context.getViewX(
                                      item.getXValue(lastVisibleIndex + 1))));
            yPoints[xPoints.length - 3] = ChartContext.getCheckedIntValue(Math.ceil(
                                      getYValue(item, lastVisibleIndex + 1,
                                      type, context, itemValueFactor)));
        }
        
        return new int[][] { xPoints, yPoints };
    }

    private static double getYValue(XYItem item, int valueIndex,
                                  int type, ChartContext context, double itemValueFactor) {
        if (type == TYPE_ABSOLUTE) {
            return context.getViewY(item.getYValue(valueIndex));
        } else {
            return context.getViewY(context.getDataOffsetY() + (itemValueFactor *
                        (item.getYValue(valueIndex) - item.getBounds().y)));
        }
    }

}
