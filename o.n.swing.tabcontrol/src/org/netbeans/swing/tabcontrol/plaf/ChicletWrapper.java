/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.swing.tabcontrol.plaf;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Date;

/**
 * GenericGlowingChiclet works nicely to draw Aqua-style decorations, but is a bit
 * slow.  In particular, Area.pruneEdges() is not cheap, but is heavily used to divide
 * the geometry.
 *
 * This wrapper class provides a cache of bitmaps painted by GenericGlowingChiclet,
 * timestamped against the last
 * time they were used, and occasionally prunes not recently used bitmaps.
 *
 * @author Tim Boudreau
 */
class ChicletWrapper implements Runnable {
    private boolean allowVertical = true;
    private boolean leftNotch = false;
    private boolean rightNotch = false;
    private int state = 0;
    private Rectangle bounds = new Rectangle();
    private float[] arcs = new float[4];
    GenericGlowingChiclet chiclet = GenericGlowingChiclet.INSTANCE; //XXX kill static instance

    public void setState (int state) {
        this.state = state;
    }

    public void setBounds (int x, int y, int w, int h) {
        bounds.setBounds (x, y, w, h);
    }

    static int drawCount = 0;
    public void draw (Graphics g) {
        if (bounds.width == 0 || bounds.height == 0) {
            return;
        }
        BufferedImage img = findBufferedImage();
        ((Graphics2D) g).drawRenderedImage(img, AffineTransform.getTranslateInstance(0, 0));
        drawCount ++;
        if (drawCount % 100 == 0) {
            //Occasionally prune old bitmaps
            SwingUtilities.invokeLater(this);
        }
    }

    public void setArcs (float a, float b, float c, float d) {
        arcs[0] = a;
        arcs[1] = b;
        arcs[2] = c;
        arcs[3] = d;
    }


    public void setAllowVertical (boolean b) {
        allowVertical = b;
    }

    public void setNotch (boolean right, boolean left) {
        leftNotch = left;
        rightNotch = right;
    }

    public Long hash() {
        long result =
            state * 701
            + Double.doubleToLongBits(arcs[0]) * 31
            + Double.doubleToLongBits(arcs[1]) * 37
            + Double.doubleToLongBits(arcs[2]) * 43
            + Double.doubleToLongBits(arcs[3]) * 47
            + bounds.width * 6703
            + bounds.height * 1783;

        if (leftNotch) {
            result *= 3121;
        }
        if (rightNotch) {
            result *= 4817;
        }
        if (allowVertical) {
            result *= 1951;
        }

        return new Long(result);
    }

    private static HashMap cache = new HashMap();

    private BufferedImage findBufferedImage() {
        Long hash = hash();
        CacheEntry entry = new CacheEntry (hash);

        BufferedImage result = (BufferedImage) cache.get(entry);
        if (result == null) {
            result = createImage();
        }
        //Store our new entry with new timestamp, even if we found an old one
        cache.put (entry, result);
        return result;
    }

    private BufferedImage createImage() {
        BufferedImage img = new BufferedImage (bounds.width, bounds.height,
            BufferedImage.TYPE_INT_ARGB_PRE);
        chiclet.setNotch(rightNotch, leftNotch);
        chiclet.setArcs (arcs[0], arcs[1], arcs[2], arcs[3]);
        chiclet.setBounds (bounds.x, bounds.y, bounds.width, bounds.height);
        chiclet.setAllowVertical(allowVertical);
        chiclet.setState (state);
        Graphics g = img.getGraphics();
        g.translate (-bounds.x, -bounds.y);
        ColorUtil.setupAntialiasing(g);
        chiclet.draw((Graphics2D)g);
        g.translate (bounds.x, bounds.y);
        return img;
    }

    public void run() {
        if (cache.size() < 5) {
            return;
        }
        HashMap newCache = (HashMap) cache.clone();
        long startTime = System.currentTimeMillis();
        CacheEntry[] entries = (CacheEntry[]) newCache.keySet().toArray(new CacheEntry[0]);
        Arrays.sort (entries);
        for (int i=entries.length-1; i >= entries.length / 3; i--) {
            if (startTime - entries[i].timestamp > 240000) {
                newCache.remove (entries[i]);
            }
        }
        cache = newCache;
    }

    private static final class CacheEntry implements Comparable {
        private final Long hash;
        long timestamp = System.currentTimeMillis();
        public CacheEntry (Long hash) {
            this.hash = hash;
        }

        public boolean equals (Object o) {
            if (o instanceof CacheEntry) {
                CacheEntry other = (CacheEntry) o;
                return other.hash() == hash();
            } else if (o instanceof Long) {
                return ((Long) o).longValue() == hash();
            } else {
                return false;
            }
        }

        long hash() {
            return hash.longValue();
        }

        public int hashCode() {
            return hash.intValue();
        }

        public int compareTo(Object o) {
            CacheEntry other = (CacheEntry) o;
            //Okay, every 4 days we might let an unused bitmap get old
            return (int) (timestamp - other.timestamp);
        }

        public String toString() {
            return "CacheEntry: " + new Date(timestamp) + " hash " + hash();
        }

    }

}
