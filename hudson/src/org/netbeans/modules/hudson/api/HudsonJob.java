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

package org.netbeans.modules.hudson.api;

import java.util.Collection;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;

/**
 * Instance of the Hudson Job in specified instance
 *
 * @author Michal Mocnak
 */
public interface HudsonJob extends Lookup.Provider, Comparable<HudsonJob> {
    
    /**
     * Describes state of the Hudson Job
     */
    public enum Color {
        aborted, aborted_anime,
        blue, blue_anime,
        disabled,
        red, red_anime, yellow, yellow_anime, grey, grey_anime
    }
    
    /**
     * Display name of the Hudson Job
     *
     * @return job display name
     */
    public String getDisplayName();
    
    /**
     * Name of the Hudson Job
     *
     * @return job's name
     */
    public String getName();
    
    /**
     * Description of the Hudson Job
     *
     * @return job's description
     */
    public String getDescription();
    
    /**
     * URL of the Hudson Job
     *
     * @return job url
     */
    public String getUrl();
    
    /**
     * Views where the job is situated
     * 
     * @return views
     */
    public Collection<HudsonView> getViews();
    
    /**
     * Color of the Hudson Job's state
     *
     * @return job color (state)
     */
    public Color getColor();
    
    /**
     * Returns job's queue state
     *
     * @return true if the job is in queue
     */
    public boolean isInQueue();
    
    /**
     * Returns job's buildable state
     *
     * @return true if the job is buildable
     */
    public boolean isBuildable();
    
    /**
     * Returns number of the last build
     * 
     * @return last build number, or -1 for none
     */
    public int getLastBuild();
    
    /**
     * Returns number of the last stable build
     * 
     * @return last stable build number, or -1 for none
     */
    public int getLastStableBuild();
    
    /**
     * Returns number of the last successful build
     * 
     * @return last successful build number, or -1 for none
     */
    public int getLastSuccessfulBuild();
    
    /**
     * Returns number of the last failed build
     *
     * @return last failed build number, or -1 for none
     */
    public int getLastFailedBuild();

    /**
     * Returns number of the last completed build
     *
     * @return last completed build number, or -1 for none
     */
    public int getLastCompletedBuild();

    /**
     * Obtains a list of recorded builds for the job.
     * @return a possibly empty set of builds
     */
    Collection<? extends HudsonJobBuild> getBuilds();

    /**
     * Starts Hudson job
     */
    public void start();
    
    /**
     * Returns default job lookup
     * 
     * @return default job lookup
     */
    // XXX replace with a getter for HudsonInstance
    public Lookup getLookup();

    /**
     * Obtains a filesystem representing the remote workspace as accessed by Hudson web services.
     */
    FileSystem getRemoteWorkspace();

}
