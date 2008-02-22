/* Copyright (c) 2007 Olivier Chafik, All Rights Reserved
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.  
 */
package org.netbeans.core.nativeaccess.transparency;

import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.MultiPixelPackedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;

/**
 * Methods that are useful to decompose a raster in ranges of contiguous unoccupied pixels.
 * An occupied pixel has two possible meanings, depending on the raster :
 * <ul>
 * <li>if the raster has an alpha layer, occupied means with alpha not null</li>
 * <li>if the raster doesn't have any alpha layer, occupied means not completely black</li>
 * </ul>
 * @author Olivier Chafik
 */
public class RasterRangesUtils {
    /// Masks used to isolate the current column in a set of 8 binary columns packed in a byte 
    private static final int[] subColMasks = new int[] {
        0x0080, 0x0040, 0x0020, 0x0010,
        0x0008, 0x0004, 0x0002, 0x0001
    };
        
    /**
     * Abstraction of a sink for ranges.
     */
    public static interface RangesOutput {
        /**
         * Output a rectangular range.
         * @param x x coordinate of the top-left corner of the range
         * @param y y coordinate of the top-left corner of the range
         * @param w witdh of the range
         * @param h height of the range
         * @return true if the output succeeded, false otherwise
         */
        public boolean outputRange(int x, int y, int w, int h);
    }
        
    /**
     * Outputs ranges of occupied pixels.
     * In a raster that has an alpha layer, a pixel is occupied if its alpha value is not null.
     * In a raster without alpha layer, a pixel is occupied if it is not completely black.
     * @param raster image to be segmented in non black or non-transparent ranges
     * @param out destination of the non null ranges
     * @return true if the output succeeded, false otherwise
     */
    public static boolean outputOccupiedRanges(Raster raster, RangesOutput out) {
        Rectangle bounds = raster.getBounds();
        SampleModel sampleModel = raster.getSampleModel();
        boolean hasAlpha = sampleModel.getNumBands() == 4;
                
        // Try to use the underlying data array directly for a few common raster formats
        if (raster.getParent() == null && bounds.x == 0 && bounds.y == 0) {
            // No support for subraster (as obtained with Image.getSubimage(...))
                        
            DataBuffer data = raster.getDataBuffer();
            if (data.getNumBanks() == 1) {
                // There is always a single bank for all BufferedImage types, except maybe TYPE_CUSTOM
                                
                if (sampleModel instanceof MultiPixelPackedSampleModel) {
                    MultiPixelPackedSampleModel packedSampleModel = (MultiPixelPackedSampleModel)sampleModel;
                    if (packedSampleModel.getPixelBitStride() == 1) {
                        // TYPE_BYTE_BINARY
                        return outputOccupiedRangesOfBinaryPixels(((DataBufferByte)data).getData(), bounds.width, bounds.height, out);
                    }
                } else if (sampleModel instanceof SinglePixelPackedSampleModel) {
                    if (sampleModel.getDataType() == DataBuffer.TYPE_INT) {
                        // TYPE_INT_ARGB, TYPE_INT_ARGB_PRE, TYPE_INT_BGR or TYPE_INT_RGB
                        return outputOccupiedRanges(((DataBufferInt)data).getData(), bounds.width, bounds.height, hasAlpha ? 0xff000000 : 0xffffff, out);
                    }
                    // TODO could easily handle cases of TYPE_USHORT_GRAY and TYPE_BYTE_GRAY.
                }
            }
        }

        // Fallback behaviour : copy pixels of raster
        int[] pixels = raster.getPixels(0, 0, bounds.width, bounds.height, (int[])null);
        return outputOccupiedRanges(pixels, bounds.width, bounds.height, hasAlpha ? 0xff000000 : 0xffffff, out);
    }
        
    /**
     * Output the non-null values of a binary image as ranges of contiguous values.
     * @param binaryBits byte-packed binary bits of an image
     * @param w width of the image (in pixels)
     * @param h height of the image
     * @param output
     * @return true if the output succeeded, false otherwise
     */
    public static boolean outputOccupiedRangesOfBinaryPixels(byte[] binaryBits, int w, int h, RangesOutput output) {
        int scanlineBytes = binaryBits.length / h;
        for (int row = 0; row < h; row++) {
            int rowOffsetBytes = row * scanlineBytes;
            int startCol = -1;
            // Look at each batch of 8 columns in this row
            for (int byteCol = 0; byteCol < scanlineBytes; byteCol++) {
                int firstByteCol = byteCol << 3;
                byte byteColBits = binaryBits[rowOffsetBytes + byteCol];
                if (byteColBits == 0) {
                    // all 8 bits are zeroes
                    if (startCol >= 0) {
                        // end of current region
                        if (!output.outputRange(startCol, row, firstByteCol - startCol, 1)) {
                            return false;
                        }
                        startCol = -1;
                    }
                } else if (byteColBits == 0xff) {
                    // all 8 bits are ones
                    if (startCol < 0) {
                        // start of new region
                        startCol = firstByteCol;
                    }
                } else {
                    // mixed case : some bits are ones, others are zeroes
                    for (int subCol = 0; subCol < 8; subCol++) {
                        int col = firstByteCol | subCol;
                        if ((byteColBits & subColMasks[subCol]) != 0) {
                            if (startCol < 0) {
                                // start of new region
                                startCol = col;
                            }
                        } else {
                            if (startCol >= 0) {
                                // end of current region
                                if (!output.outputRange(startCol, row, col - startCol, 1)) {
                                    return false;
                                }
                                startCol = -1;
                            }
                        }
                    }
                }
            }
            if (startCol >= 0) {
                // end of last region
                if (!output.outputRange(startCol, row, w - startCol, 1)) {
                    return false;
                }
                startCol = -1;
            }
        }
        return true;
    }
        
    /**
     * Output the occupied values of an integer-pixels image as ranges of contiguous values.
     * A pixel is considered occupied if the bitwise AND of its integer value with the provided occupationMask is not null.
     * @param pixels integer values of the pixels of an image
     * @param w width of the image (in pixels)
     * @param h height of the image
     * @param occupationMask mask used to select which bits are used in a pixel to check its occupied status. 0xff000000 would only take the alpha layer into account, for instance.
     * @param out where to output all the contiguous ranges of non occupied pixels
     * @return true if the output succeeded, false otherwise
     */
    public static boolean outputOccupiedRanges(int[] pixels, int w, int h, int occupationMask, RangesOutput out) {
                
        for (int row = 0; row < h; row++) {
            int idxOffset = row * w;
            int startCol = -1;
                        
            for (int col = 0; col < w; col++) {
                if ((pixels[idxOffset + col] & occupationMask) != 0) {
                    if (startCol < 0) {
                        startCol = col;
                    }
                } else {
                    if (startCol >= 0) {
                        // end of current region
                        if (!out.outputRange(startCol, row, col - startCol, 1)) {
                            return false;
                        }
                        startCol = -1;
                    }
                }
            }
            if (startCol >= 0) {
                // end of last region of current row
                if (!out.outputRange(startCol, row, w - startCol, 1)) {
                    return false;
                }
                startCol = -1;
            }
        }
        return true;
    }
}
