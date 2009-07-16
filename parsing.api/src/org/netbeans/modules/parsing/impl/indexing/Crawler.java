/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Zezula
 */
public abstract class Crawler {

    /**
     *
     * @param root
     * @param checkTimeStamps
     * @param mimeTypesToCheck The set of mime types that the <code>Crawler</code> should check.
     *   Can be <code>null</code> in which case all mime types will be checked.
     *
     * @throws java.io.IOException
     */
    protected Crawler(final URL root, boolean checkTimeStamps, CancelRequest cancelRequest) throws IOException {
        this.root = root;
        this.checkTimeStamps = checkTimeStamps;
        this.timeStamps = TimeStamps.forRoot(root, checkTimeStamps);
        this.cancelRequest = cancelRequest;
    }

    public final List<IndexableImpl> getResources() throws IOException {
        init ();
        return cache;
    }

    public final List<IndexableImpl> getDeletedResources () throws IOException {
        init ();
        return deleted;
    }

    public final void storeTimestamps() throws IOException {
        timeStamps.store();
    }

    public final boolean isFinished() {
        return finished;
    }

    protected final boolean isUpToDate(FileObject f) {
        // always call this in order to update the file's timestamp
        boolean upToDate = timeStamps.checkAndStoreTimestamp(f);
        return checkTimeStamps ? upToDate : false;
    }

    protected final boolean isCancelled() {
        return cancelRequest.isRaised();
    }

    protected abstract boolean collectResources(List<IndexableImpl> resources);

    // -----------------------------------------------------------------------
    // private implementation
    // -----------------------------------------------------------------------

    private final URL root;
    private final boolean checkTimeStamps;
    private final TimeStamps timeStamps;
    private final CancelRequest cancelRequest;

    private List<IndexableImpl> cache;
    private List<IndexableImpl> deleted;
    private boolean finished;

    private void init () throws IOException {
        if (this.cache == null) {
            List<IndexableImpl> resources = new LinkedList<IndexableImpl>();
            this.finished = collectResources(resources);
            this.cache = Collections.unmodifiableList(resources);
            final Set<String> unseen = timeStamps.getUnseenFiles();
            if (unseen != null) {
                deleted = new ArrayList<IndexableImpl>(unseen.size());
                for (String u : unseen) {
                    deleted.add(new DeletedIndexable(root, u));
                }
                deleted = Collections.unmodifiableList(deleted);
            } else {
                deleted = Collections.<IndexableImpl>emptyList();
            }
        }
    }
}
