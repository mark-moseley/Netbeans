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

import java.awt.image.BufferedImage;

/**
 * Interface for all classes performing image comparision.
 *
 * @author Alexandre Iline (alexandre.iline@sun.com)
 */
public interface ImageComparator {

    /**
     * Should return true if images matches, false otherwise.
     */
    public boolean compare(BufferedImage image1, BufferedImage image2);
}
