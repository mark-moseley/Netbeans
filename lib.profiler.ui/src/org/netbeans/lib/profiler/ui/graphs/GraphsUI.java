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

package org.netbeans.lib.profiler.ui.graphs;

import java.awt.Color;

/**
 *
 * @author Jiri Sedlacek
 */
public interface GraphsUI {

    // --- General colors definition -------------------------------------------
    
    public static final Color  PROFILER_BLUE = new Color(127, 63, 191);
    public static final Color  PROFILER_RED = new Color(255, 127, 127);
    public static final Color  PROFILER_GREEN = new Color(30, 157, 68);


    // --- Charts colors definition --------------------------------------------

    public static final Color  CHART_BACKGROUND_COLOR = Color.WHITE;
    public static final Color  SMALL_LEGEND_BACKGROUND_COLOR = Color.WHITE;
    public static final Color  SMALL_LEGEND_BORDER_COLOR = new Color(235, 235, 235);

    public static final float  TOOLTIP_OVERLAY_LINE_WIDTH = 2.1f;
    public static final Color  TOOLTIP_OVERLAY_LINE_COLOR = Color.DARK_GRAY;
    public static final Color  TOOLTIP_OVERLAY_FILL_COLOR = new Color(250, 250, 255);
    
    
    // --- Class History graphs ------------------------------------------------

    public static final String A_ALLOC_OBJECTS_NAME = "Allocated Objects";
    public static final float  A_ALLOC_OBJECTS_PAINTER_LINE_WIDTH = 3f;
    public static final Color  A_ALLOC_OBJECTS_PAINTER_LINE_COLOR = PROFILER_RED;
    public static final Color  A_ALLOC_OBJECTS_PAINTER_FILL_COLOR = null;
    public static final int    A_ALLOC_OBJECTS_MARKER_RADIUS = 5;
    public static final float  A_ALLOC_OBJECTS_MARKER_LINE1_WIDTH = 0.75f;
    public static final Color  A_ALLOC_OBJECTS_MARKER_LINE1_COLOR = Color.BLACK;
    public static final float  A_ALLOC_OBJECTS_MARKER_LINE2_WIDTH = 3.5f;
    public static final Color  A_ALLOC_OBJECTS_MARKER_LINE2_COLOR = Color.WHITE;
    public static final Color  A_ALLOC_OBJECTS_MARKER_FILL_COLOR = PROFILER_RED;

    public static final String A_ALLOC_BYTES_NAME = "Allocated Bytes";
    public static final float  A_ALLOC_BYTES_PAINTER_LINE_WIDTH = 3f;
    public static final Color  A_ALLOC_BYTES_PAINTER_LINE_COLOR = PROFILER_BLUE;
    public static final Color  A_ALLOC_BYTES_PAINTER_FILL_COLOR = null;
    public static final int    A_ALLOC_BYTES_MARKER_RADIUS = 5;
    public static final float  A_ALLOC_BYTES_MARKER_LINE1_WIDTH = 0.75f;
    public static final Color  A_ALLOC_BYTES_MARKER_LINE1_COLOR = Color.BLACK;
    public static final float  A_ALLOC_BYTES_MARKER_LINE2_WIDTH = 3.5f;
    public static final Color  A_ALLOC_BYTES_MARKER_LINE2_COLOR = Color.WHITE;
    public static final Color  A_ALLOC_BYTES_MARKER_FILL_COLOR = PROFILER_BLUE;

    public static final String L_LIVE_OBJECTS_NAME = "Live Objects";
    public static final float  L_LIVE_OBJECTS_PAINTER_LINE_WIDTH = 3f;
    public static final Color  L_LIVE_OBJECTS_PAINTER_LINE_COLOR = PROFILER_RED;
    public static final Color  L_LIVE_OBJECTS_PAINTER_FILL_COLOR = null;
    public static final int    L_LIVE_OBJECTS_MARKER_RADIUS = 5;
    public static final float  L_LIVE_OBJECTS_MARKER_LINE1_WIDTH = 0.75f;
    public static final Color  L_LIVE_OBJECTS_MARKER_LINE1_COLOR = Color.BLACK;
    public static final float  L_LIVE_OBJECTS_MARKER_LINE2_WIDTH = 3.5f;
    public static final Color  L_LIVE_OBJECTS_MARKER_LINE2_COLOR = Color.WHITE;
    public static final Color  L_LIVE_OBJECTS_MARKER_FILL_COLOR = PROFILER_RED;

    public static final String L_LIVE_BYTES_NAME = "Live Bytes";
    public static final float  L_LIVE_BYTES_PAINTER_LINE_WIDTH = 3f;
    public static final Color  L_LIVE_BYTES_PAINTER_LINE_COLOR = PROFILER_BLUE;
    public static final Color  L_LIVE_BYTES_PAINTER_FILL_COLOR = null;
    public static final int    L_LIVE_BYTES_MARKER_RADIUS = 5;
    public static final float  L_LIVE_BYTES_MARKER_LINE1_WIDTH = 0.75f;
    public static final Color  L_LIVE_BYTES_MARKER_LINE1_COLOR = Color.BLACK;
    public static final float  L_LIVE_BYTES_MARKER_LINE2_WIDTH = 3.5f;
    public static final Color  L_LIVE_BYTES_MARKER_LINE2_COLOR = Color.WHITE;
    public static final Color  L_LIVE_BYTES_MARKER_FILL_COLOR = PROFILER_BLUE;

    public static final String L_ALLOC_OBJECTS_NAME = "Allocated Objects";
    public static final float  L_ALLOC_OBJECTS_PAINTER_LINE_WIDTH = 3f;
    public static final Color  L_ALLOC_OBJECTS_PAINTER_LINE_COLOR = PROFILER_GREEN;
    public static final Color  L_ALLOC_OBJECTS_PAINTER_FILL_COLOR = null;
    public static final int    L_ALLOC_OBJECTS_MARKER_RADIUS = 5;
    public static final float  L_ALLOC_OBJECTS_MARKER_LINE1_WIDTH = 0.75f;
    public static final Color  L_ALLOC_OBJECTS_MARKER_LINE1_COLOR = Color.BLACK;
    public static final float  L_ALLOC_OBJECTS_MARKER_LINE2_WIDTH = 3.5f;
    public static final Color  L_ALLOC_OBJECTS_MARKER_LINE2_COLOR = Color.WHITE;
    public static final Color  L_ALLOC_OBJECTS_MARKER_FILL_COLOR = PROFILER_GREEN;


    // --- VM Telemetry graphs -------------------------------------------------

    public static final String HEAP_SIZE_NAME = "Heap Size";
    public static final float  HEAP_SIZE_PAINTER_LINE_WIDTH = 0f;
    public static final Color  HEAP_SIZE_PAINTER_LINE_COLOR = null;
    public static final Color  HEAP_SIZE_PAINTER_FILL_COLOR = PROFILER_RED;
    public static final int    HEAP_SIZE_MARKER_RADIUS = 5;
    public static final float  HEAP_SIZE_MARKER_LINE1_WIDTH = 0.75f;
    public static final Color  HEAP_SIZE_MARKER_LINE1_COLOR = Color.BLACK;
    public static final float  HEAP_SIZE_MARKER_LINE2_WIDTH = 3.5f;
    public static final Color  HEAP_SIZE_MARKER_LINE2_COLOR = Color.WHITE;
    public static final Color  HEAP_SIZE_MARKER_FILL_COLOR = PROFILER_RED;

    public static final String USED_HEAP_NAME = "Used Heap";
    public static final float  USED_HEAP_PAINTER_LINE_WIDTH = 0f;
    public static final Color  USED_HEAP_PAINTER_LINE_COLOR = null;
    public static final Color  USED_HEAP_PAINTER_FILL_COLOR = PROFILER_BLUE;
    public static final int    USED_HEAP_MARKER_RADIUS = 5;
    public static final float  USED_HEAP_MARKER_LINE1_WIDTH = 0.75f;
    public static final Color  USED_HEAP_MARKER_LINE1_COLOR = Color.BLACK;
    public static final float  USED_HEAP_MARKER_LINE2_WIDTH = 3.5f;
    public static final Color  USED_HEAP_MARKER_LINE2_COLOR = Color.WHITE;
    public static final Color  USED_HEAP_MARKER_FILL_COLOR = PROFILER_BLUE;

    public static final Color  HEAP_LIMIT_FILL_COLOR = new Color(220, 220, 220);
    
    public static final String SURVGEN_NAME = "Surviving Generations";
    public static final float  SURVGEN_PAINTER_LINE_WIDTH = 3f;
    public static final Color  SURVGEN_PAINTER_LINE_COLOR = PROFILER_RED;
    public static final Color  SURVGEN_PAINTER_FILL_COLOR = null;
    public static final int    SURVGEN_MARKER_RADIUS = 5;
    public static final float  SURVGEN_MARKER_LINE1_WIDTH = 0.75f;
    public static final Color  SURVGEN_MARKER_LINE1_COLOR = Color.BLACK;
    public static final float  SURVGEN_MARKER_LINE2_WIDTH = 3.5f;
    public static final Color  SURVGEN_MARKER_LINE2_COLOR = Color.WHITE;
    public static final Color  SURVGEN_MARKER_FILL_COLOR = PROFILER_RED;

    public static final String GC_TIME_NAME = "Relative Time Spent in GC";
    public static final float  GC_TIME_PAINTER_LINE_WIDTH = 3f;
    public static final Color  GC_TIME_PAINTER_LINE_COLOR = PROFILER_BLUE;
    public static final Color  GC_TIME_PAINTER_FILL_COLOR = null;
    public static final int    GC_TIME_MARKER_RADIUS = 5;
    public static final float  GC_TIME_MARKER_LINE1_WIDTH = 0.75f;
    public static final Color  GC_TIME_MARKER_LINE1_COLOR = Color.BLACK;
    public static final float  GC_TIME_MARKER_LINE2_WIDTH = 3.5f;
    public static final Color  GC_TIME_MARKER_LINE2_COLOR = Color.WHITE;
    public static final Color  GC_TIME_MARKER_FILL_COLOR = PROFILER_BLUE;

    public static final Color  GC_ACTIVITY_FILL_COLOR = new Color(250, 230, 230);

    public static final String THREADS_NAME = "Threads";
    public static final float  THREADS_PAINTER_LINE_WIDTH = 3f;
    public static final Color  THREADS_PAINTER_LINE_COLOR = PROFILER_RED;
    public static final Color  THREADS_PAINTER_FILL_COLOR = null;
    public static final int    THREADS_MARKER_RADIUS = 5;
    public static final float  THREADS_MARKER_LINE1_WIDTH = 0.75f;
    public static final Color  THREADS_MARKER_LINE1_COLOR = Color.BLACK;
    public static final float  THREADS_MARKER_LINE2_WIDTH = 3.5f;
    public static final Color  THREADS_MARKER_LINE2_COLOR = Color.WHITE;
    public static final Color  THREADS_MARKER_FILL_COLOR = PROFILER_RED;

    public static final String LOADED_CLASSES_NAME = "Loaded Classes";
    public static final float  LOADED_CLASSES_PAINTER_LINE_WIDTH = 3f;
    public static final Color  LOADED_CLASSES_PAINTER_LINE_COLOR = PROFILER_BLUE;
    public static final Color  LOADED_CLASSES_PAINTER_FILL_COLOR = null;
    public static final int    LOADED_CLASSES_MARKER_RADIUS = 5;
    public static final float  LOADED_CLASSES_MARKER_LINE1_WIDTH = 0.75f;
    public static final Color  LOADED_CLASSES_MARKER_LINE1_COLOR = Color.BLACK;
    public static final float  LOADED_CLASSES_MARKER_LINE2_WIDTH = 3.5f;
    public static final Color  LOADED_CLASSES_MARKER_LINE2_COLOR = Color.WHITE;
    public static final Color  LOADED_CLASSES_MARKER_FILL_COLOR = PROFILER_BLUE;

}
