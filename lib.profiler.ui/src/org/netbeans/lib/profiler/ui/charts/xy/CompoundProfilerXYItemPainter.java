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

import java.awt.Color;
import org.netbeans.lib.profiler.charts.xy.*;
import org.netbeans.lib.profiler.charts.CompoundItemPainter;
import org.netbeans.lib.profiler.charts.ChartContext;

/**
 *
 * @author Jiri Sedlacek
 */
public class CompoundProfilerXYItemPainter extends CompoundItemPainter implements XYItemPainter {

    public CompoundProfilerXYItemPainter(XYItemPainter painter1, XYItemPainter painter2) {
        super(painter1, painter2);
    }


    public Color getItemColor(XYItem item) {
        Color itemColor = getPainter1().getItemColor(item);
        if (itemColor == null) itemColor = getPainter2().getItemColor(item);
        return itemColor;
    }

    public long[] getDataValues(long[] viewValues, XYItem item, ChartContext context) {
        return getPainter1().getDataValues(viewValues, item, context);
    }


    protected XYItemPainter getPainter1() {
        return (XYItemPainter)super.getPainter1();
    }

    protected XYItemPainter getPainter2() {
        return (XYItemPainter)super.getPainter2();
    }

}
