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
package org.netbeans.modules.visualweb.designer;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ImageIcon;
import org.openide.ErrorManager;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;


/**
 * Image cache, to avoid repeated loading of the same image urls.
 *
 * @author Tor Norbye
 */
public class ImageCache {
//    HashMap images;
    private Map<URL, ImageIcon> images;

    /** Construct a new image cache */
    public ImageCache() {
    }

    /** Get an image by a particular URL */
    public ImageIcon get(URL url) {
        if (images == null) {
            return null;
        }

        return images.get(url);
    }

    /** Put an image into the cache */
    public void put(URL url, ImageIcon image) {
        if (images == null) {
            images = new HashMap<URL, ImageIcon>(); // TODO - initial size?
        }

        images.put(url, image);
    }

    /** Clear out the cache */
    public void flush() {
        if (images != null) {
            // Ensure that the files actually get reloaded from disk;
            // NetBeans may be hanging on to old bits in its filesystem
            // layer
            Iterator<URL> it = images.keySet().iterator();

            while (it.hasNext()) {
                URL url = it.next();

                // XXX Lame validity check, missing nice NB API.
                try {
                    new URI(url.toExternalForm());
                } catch(URISyntaxException ex) {
                    // XXX #6368790 It means the url is not valid URI and URLMapper.findFileObject
                    // would show an exception to the user. We just log it.
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    continue;
                }

                FileObject fo = URLMapper.findFileObject(url);

                if (fo != null) {
                    fo.refresh(false);
                }
            }

            images = null;
        }
    }

    @Override
    public String toString() {
        return super.toString() + "[images=" + images + "]"; // NOI18N
    }
}
