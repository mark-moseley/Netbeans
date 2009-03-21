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

package org.netbeans.lib.profiler.charts;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jiri Sedlacek
 */
public interface ChartItem {
    
    public void addItemListener(ChartItemListener listener);
    
    public void removeItemListener(ChartItemListener listener);


    public static abstract class Abstract implements ChartItem {


        private List<ChartItemListener> listeners;


        public void addItemListener(ChartItemListener listener) {
            if (listeners == null) listeners = new ArrayList();
            if (!listeners.contains(listener)) listeners.add(listener);
        }

        public void removeItemListener(ChartItemListener listener) {
            if (listeners != null) listeners.remove(listener);
        }


        protected void fireItemChanged(ChartItemChange itemChange) {
            if (listeners != null)
                for (ChartItemListener listener : listeners)
                    listener.chartItemChanged(itemChange);
        }

    }

}
