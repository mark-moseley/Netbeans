/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;

/**
 *
 * @author Jiri Rechtacek
 */
public class AutoupdateCatalogProvider implements UpdateProvider {
    private URL updateCenter;
    private String codeName;
    private String displayName;
    private AutoupdateCatalogCache cache = AutoupdateCatalogCache.getDefault ();
    private Logger log = Logger.getLogger ("org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalog");
    private String description = null;
    private CATEGORY category = null;

    public AutoupdateCatalogProvider (String name, String displayName, URL updateCenter) {
        this(name, displayName, updateCenter, CATEGORY.COMMUNITY);
    }
    
    /**
     * Creates a new instance of AutoupdateCatalog
     */
    public AutoupdateCatalogProvider (String name, String displayName, URL updateCenter, CATEGORY category) {
        this.codeName = name;
        this.displayName = displayName;
        this.updateCenter = updateCenter;
        this.category = (category != null) ? category : CATEGORY.COMMUNITY;
    }
    
    public String getName () {
        assert codeName != null : "UpdatesProvider must have a name.";
        return codeName;
    }
    
    public String getDisplayName () {
        return displayName == null ? codeName : displayName;
    }
    
    public String getDescription () {
        return description;
    }
    
    public void setNotification (String notification) {
        this.description = notification;
    }

    public Map<String, UpdateItem> getUpdateItems () throws IOException {
        URL toParse = cache.getCatalogURL (codeName);
        /*if (toParse == null && !firstRefreshDone) {
            firstRefreshDone = true;
            refresh(true);
            toParse = cache.getCatalogURL (codeName);
        }*/
        if (toParse == null) {
            log.log (Level.INFO, "No content in cache for " + codeName + " provider. Returns EMPTY_MAP");
            return Collections.emptyMap ();
        }
        
        return AutoupdateCatalogParser.getUpdateItems (toParse, this);
    }
    
    public boolean refresh (boolean force) throws IOException {
        boolean res = false;
        log.log (Level.FINER, "Try write(force? " + force + ") to cache Update Provider " + codeName + " from "  + getUpdateCenterURL ());
        if (force) {
            res = cache.writeCatalogToCache (codeName, getUpdateCenterURL ()) != null;
            description = null;
        } else {
            res = true;
        }
        return res;
    }
    
    public URL getUpdateCenterURL () {
        assert updateCenter != null : "XMLCatalogUpdatesProvider " + codeName + " must have a URL to Update Center";
        return updateCenter;
    }
    
    public void setUpdateCenterURL (URL newUpdateCenter) {
        assert newUpdateCenter != null;
        updateCenter = newUpdateCenter;
    }
    
    @Override
    public String toString () {
        return displayName + "[" + codeName + "] to " + updateCenter;
    }

    public CATEGORY getCategory() {
        return category;
    }    
}
