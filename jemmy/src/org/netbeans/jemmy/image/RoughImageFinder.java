/*
 * Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License Version
 * 1.0 (the "License"). You may not use this file except in compliance with
 * the License. A copy of the License is available at http://www.sun.com/
 *
 * The Original Code is the Jemmy library.
 * The Initial Developer of the Original Code is Alexandre Iline.
 * All Rights Reserved.
 * 
 * Contributor(s): Alexandre Iline.
 *
 * $Id$ $Revision$ $Date$
 * 
 */

package org.netbeans.jemmy.image;

import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * Performs "rough" image search.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public class RoughImageFinder implements ImageFinder {
    double roughness = .0;
    int bigWidth, bigHeight;
    int[][] bigPixels;

    /**
     * Creates an instance allowing to find an image inside the one
     * passed as parameter with some "roughness".
     * @param area - Image to search in.
     * @param roughness - Allowed 
     */
    public RoughImageFinder(BufferedImage area, double roughness) {
        this.roughness = roughness;
        bigWidth  = area.getWidth();
        bigHeight = area.getHeight();
        bigPixels = new int[bigWidth][bigHeight];
        for(int x = 0; x < bigWidth; x++) {
            for(int y = 0; y < bigHeight; y++) {
                bigPixels[x][y] = area.getRGB(x, y);
            }
        }
    }

    /**
     * Performs "rough" search.
     * @return Poing where number of unmatching pixels less or equal to
     * <code>image1.getWidth() * image1.getHeight() * roughness<code>
     */
    public Point findImage(BufferedImage image, int index) {
        int smallWidth  = image.getWidth();
        int smallHeight = image.getHeight();
        int[][] smallPixels = new int[smallWidth][smallHeight];
        for(int x = 0; x < smallWidth; x++) {
            for(int y = 0; y < smallHeight; y++) {
                smallPixels[x][y] = image.getRGB(x, y);
            }
        }
        double maxRoughPixels = (double)(smallWidth * smallHeight) * roughness;
        int count = 0;
        for(int X = 0; X <= bigWidth - smallWidth; X++) {
            for(int Y = 0; Y <= bigHeight - smallHeight; Y++) {
                int roughPixels = 0;
                for(int x = 0; x < smallWidth; x++) {
                    for(int y = 0; y < smallHeight; y++) {
                        if(smallPixels[x][y] != bigPixels[X + x][Y + y]) {
                            roughPixels++;
                            if(roughPixels > maxRoughPixels) {
                                break;
                            }
                        }
                    }
                    if(roughPixels > maxRoughPixels) {
                        break;
                    }
                }
                if(roughPixels <= maxRoughPixels) {
                    if(count == index) {
                        return(new Point(X, Y));
                    }
                    count++;
                }
            }
        }
        return(null);
    }
}
