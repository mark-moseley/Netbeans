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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.Indexable;

/**
 *
 * @author Tomas Zezula
 */
public abstract class Crawler {

    protected Crawler (final URL root, boolean checkTimeStamps, Set<String> mimeTypesToCheck) throws IOException {
        this.root = root;
        this.timeStamps = checkTimeStamps ? TimeStamps.forRoot(root) : null;
        this.mimeTypesToCheck = mimeTypesToCheck != null ? mimeTypesToCheck : PathRecognizerRegistry.getDefault().getMimeTypes();
    }

//    public final synchronized String getDigest () throws IOException {
//        init ();
//        return this.digest;
//    }
//
//    protected final void addToDigest () {
//    }

    public final synchronized Map<String, Collection<Indexable>> getResources() throws IOException {
        init ();
        return cache;
    }

    public final Collection<Indexable> getDeletedResources () throws IOException {
        init ();
        return Collections.unmodifiableCollection(deleted);
    }

    protected final TimeStamps getTimeStamps() {
        return timeStamps;
    }

    protected abstract Map<String, Collection<Indexable>> collectResources(final Set<? extends String> supportedMimeTypes);

    // -----------------------------------------------------------------------
    // private implementation
    // -----------------------------------------------------------------------

    private final URL root;
    private final TimeStamps timeStamps;
    private final Set<String> mimeTypesToCheck;

//    private String digest;
    private Map<String, Collection<Indexable>> cache;
    private Collection<Indexable> deleted;

    private void init () throws IOException {
        if (this.cache == null) {
            this.cache = collectResources(mimeTypesToCheck);
            if (timeStamps != null) {
                final Set<String> unseen = timeStamps.store();
                deleted = new ArrayList<Indexable>(unseen.size());
                for (String u : unseen) {
                    deleted.add(SPIAccessor.getInstance().create(new DeletedIndexable(root, u)));
                }
            } else {
                deleted = Collections.<Indexable>emptyList();
            }
        }
    }
}
