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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.bpel.core.util;

import java.awt.Image;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.ImageIcon;

import org.openide.util.Utilities;

/**
 * copied from Jato and LiteJ2eePlugin module
 *
 * @author  Mike Frisino
 * @author  Matt Stevens (revisons on initial theft)
 * @version
 */
public abstract class BadgedIconCache {

    private static Map<String,Image> theCache =
		Collections.synchronizedMap(new WeakHashMap<String,Image>(101));
    

	public static final int NW_BADGE_X = 8;
    public static final int NW_BADGE_Y = 0;
    public static final int NE_BADGE_X = 16;
    public static final int NE_BADGE_Y = 0;
    public static final int SE_BADGE_X = 16;
    public static final int SE_BADGE_Y = 8;
    public static final int SW_BADGE_X = 8;
    public static final int SW_BADGE_Y = 8;
    
  
    public static final String DEFAULT_ICON =
       "org/openide/resources/defaultNode.gif";                           // NOI18N
	public static final String DEFAULT_ERROR_BADGE =   
		"org/netbeans/modules/bpel/core/resources/errorbadge.gif";        // NOI18N
	public static final String DEFAULT_WARNING_BADGE =
		"org/netbeans/modules/bpel/core/resources/warningbadge.gif";      // NOI18N
	
	private static final String ERROR_KEY_PREFIX = "ERR";                 // NOI18N
	private static final String WARNING_KEY_PREFIX = "WRN";               // NOI18N
	
	public static Image getErrorIcon(String base)
	{
		return getBadgedIcon(base,null,null,null,DEFAULT_ERROR_BADGE);
	}
	
	public static Image getWarningIcon(String base)
	{
		return getBadgedIcon(base,null,null,null,DEFAULT_WARNING_BADGE);
	}

	public static Image getErrorIcon(Image baseImage)
	{
		if(null == baseImage)
			return null;
		
		String key = ERROR_KEY_PREFIX + baseImage.toString();
		
        Image result = (Image)theCache.get(key);
        if (result == null)
		{
            result = createBadgedIcon(
				baseImage, null, null, null, DEFAULT_ERROR_BADGE);
            theCache.put(key, result);
		}
		
		return result;		
	}
	
	public static Image getWarningIcon(Image baseImage)
	{
		if(null == baseImage)
			return null;
		
		String key = WARNING_KEY_PREFIX + baseImage.toString();
		
        Image result = (Image)theCache.get(key);
        if (result == null)
		{
            result = createBadgedIcon(
				baseImage, null, null, null, DEFAULT_WARNING_BADGE);
            theCache.put(key, result);
		}
		
		return result;		
	}
	
    /**
     * Get a badged icon constructed from the icons given as method arguments.
     * Creates a key for the icon from the String arguments, and checks if we already
     * have a cached icon for that key; if so, return it; if not, create the icon
     * and add to cache.
     * <p>
     * Any argument, except "base", can be null; a null argument means "no badge
     * desired in that quadrant".  Note that there is a rule in the "Icon Badging"
     * specification which says that if the error badge is used, then
     * no other NW or SW badges should be used; that rule is *not* enforced by this
     * method, so it is up to the caller to obey it.
     * <p>
     * If you encounter problems with this method, then turn on logger messages for
     * Type: dbg, Group: 7, Level: 200, Module Name: com.sun.forte4j.j2ee.lib,
     * run it again and look for output trace messages.
     * <p>
     * All method parameters give the class loader path of a .gif (or equivalent)
     * resource; parameters are in order starting with NW quadrant and going clockwise.
     * Notes: these names are cAsE sensitive, even on non-case-sensitive OS's.;
     *        if no file extension is given, then ".gif" is assumed.
     * <p>
     * @param base classloader path for the base icon.
     * @param nwBadge classloader path of the icon file for the NW quadrant badge.
     * @param neBadge classloader path of the icon file for the NE quadrant badge.
     * @param seBadge classloader path of the icon file for the SE quadrant badge.
     * @param swBadge classloader path of the icon file for the SW quadrant badge.
     */
    public static Image getBadgedIcon(String base, String nwBadge, String neBadge,
                                        String seBadge, String swBadge) {
        if (base == null)
            return null;

        String key = buildIconKey(base, nwBadge, neBadge, seBadge, swBadge);

        Image result = (Image)theCache.get(key);
        if (result == null)
		{
            result = createBadgedIcon(base, nwBadge, neBadge, seBadge, swBadge);
            theCache.put(key, result);
        }
        
        return result;
    }

	/*
     * @param base the base icon.
     * @param nwBadge classloader path of the icon file for the NW quadrant badge.
     * @param neBadge classloader path of the icon file for the NE quadrant badge.
     * @param seBadge classloader path of the icon file for the SE quadrant badge.
     * @param swBadge classloader path of the icon file for the SW quadrant badge.
     */
    public static Image getBadgedIcon(String base, Image baseImage, 
			String nwBadge, String neBadge,
            String seBadge, String swBadge) {
        if (base == null)
            return null;

        String key = buildIconKey(base, nwBadge, neBadge, seBadge, swBadge);

        Image result = (Image)theCache.get(key);
        if (result == null)
		{
            result = createBadgedIcon(baseImage, nwBadge, neBadge, seBadge, 
                    swBadge);
            theCache.put(key, result);
        }
        
        return result;
    }


    /**
     * Construct hash table key = concatenate all arguments with ";" between
     * each one and null replaced by the string "null".
     * Order (clockwise from nw): base;nw;ne;se;sw
     *
     * Known minor problem: if any of the .gif files can't be opened, would be
     * better to use "null" in the key for that badge.  Currently the bad file name
     * is used to construct the key.
     */
    private static String buildIconKey(
		String base,
		String nwBadge,
		String neBadge,
		String seBadge,
		String swBadge)
	{
        String nullString = new String("null"); // NOI18N
        Object[] params = new Object[] {normalizeGifPath(base),
            ((nwBadge == null) ? nullString : normalizeGifPath(nwBadge)),
            ((neBadge == null) ? nullString : normalizeGifPath(neBadge)),
            ((seBadge == null) ? nullString : normalizeGifPath(seBadge)),
            ((swBadge == null) ? nullString : normalizeGifPath(swBadge)),
        };
 
        return MessageFormat.format("{0};{1};{2};{3};{4}", params); // NOI18N
    }

	/*
    private static String buildIconKey(
		Image baseIcon,
		String nwBadge,
		String neBadge,
		String seBadge,
		String swBadge)
	{
        String nullString = new String("null"); // NOI18N
		String iconAsString = baseIcon.
        Object[] params = new Object[] {normalizeGifPath(baseIcon),
            ((nwBadge == null) ? nullString : normalizeGifPath(nwBadge)),
            ((neBadge == null) ? nullString : normalizeGifPath(neBadge)),
            ((seBadge == null) ? nullString : normalizeGifPath(seBadge)),
            ((swBadge == null) ? nullString : normalizeGifPath(swBadge)),
        };
 
        return MessageFormat.format("{0};{1};{2};{3};{4}", params); // NOI18N
    }
	*/

    /**
     * Instantiate an Image for each not-null argument.
     * Then call org.openide.util.Utilities.mergeImages() to do the overlaying.
     * return the resulting icon.
     *
     * Note: the special rule that "if swBadge = seriousErrorBadge then other
     * 3 quadrants must be blank" is *not* enforced by this method; therefore,
     * it is up to the caller to respect this and pass appropriate arguments.
	 * 
	 * Does not do caching.
     */
    public static Image createBadgedIcon(
		String base,
		String nwBadge,
		String neBadge,
		String seBadge,
		String swBadge)
	{
		Image baseImage = getIcon(base);
        if (baseImage == null)
            baseImage = getIcon(DEFAULT_ICON);

		return createBadgedIcon(baseImage,nwBadge,neBadge,seBadge,swBadge);
    }
    
	/*
	 * 
	 * Does not do caching.
     */
    public static Image createBadgedIcon(
		Image baseImage,
		String nwBadge,
		String neBadge,
		String seBadge,
		String swBadge)
	{
		if(null == baseImage)
			return null;
		
		Image badgedImage = baseImage;
		
        // merge the icon for each quadrant with the base icon:
        if (nwBadge != null)
            badgedImage = mergeSingleImage(
				badgedImage, nwBadge, NW_BADGE_X, NW_BADGE_Y);

		if (neBadge != null)
            badgedImage = mergeSingleImage(
				badgedImage, neBadge, NE_BADGE_X, NE_BADGE_Y);

		if (seBadge != null)
            badgedImage = mergeSingleImage(
				badgedImage, seBadge, SE_BADGE_X, SE_BADGE_Y);

		if (swBadge != null)
            badgedImage = mergeSingleImage(
				badgedImage, swBadge, SW_BADGE_X, SW_BADGE_Y);

        return badgedImage;
    }

	public static Image mergeSingleImage(
		Image baseImage, String badge, int badge_x, int badge_y)
	{
        Image badgeImage = getIcon(badge);
        if (badgeImage == null)
            return baseImage;

        return Utilities.mergeImages(baseImage, badgeImage, badge_x, badge_y);
    }
    
    /**
     * Find icon resource file in cache if available; otherwise get from disk
     * and add to cache.
     */
    public static Image getIcon(String iconFile)
	{
		String filename = normalizeGifPath(iconFile);
        Image theImage = (Image)theCache.get(filename);
        if (theImage != null)
            return theImage;    // cache hit

        // got following line of code from openide.util.IconManager.java:
        // ("BadgedIconCache.class.getClassLoader().getResource(iconFile)" doesn't work)
        ClassLoader loader =  (ClassLoader)org.openide.util.Lookup.getDefault().
            lookup(ClassLoader.class);

		URL tmpURL = loader.getResource(filename);
        if (tmpURL == null)
            return null;

        theImage = new ImageIcon(tmpURL).getImage();
        theCache.put(filename, theImage);

		return theImage;
    }
    
    /**
     * Make the format of the .gif file path name a little more flexible:
     * fix so doesn't start with "/" and does end in ".gif".
     */
    public static String normalizeGifPath(String gifPath) {
        if (gifPath == null)
            return gifPath;
        
        String slash = "/";	// NOI18N
        String dot = ".";	// NOI18N
        
        if (gifPath.startsWith(slash)) {
            gifPath = gifPath.substring(slash.length());
        }
        
        if (gifPath.indexOf(dot) == -1) {
            gifPath = gifPath.concat(".gif");	// NOI18N
        }
        
        return gifPath;
    }
}
