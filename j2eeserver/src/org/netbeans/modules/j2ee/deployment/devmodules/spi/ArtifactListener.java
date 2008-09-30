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

package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import java.io.File;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 * @since 1.48
 */
public interface ArtifactListener {

    public void artifactsUpdated(Iterable<Artifact> artifacts);

    public static final class Artifact {

        private final File file;

        private final File distributionPath;

        private final boolean library;

        private final boolean relocatable;

        public Artifact(File file, File distributionPath, boolean library, boolean relocatable) {
            this.file = file;
            this.distributionPath = distributionPath;
            this.library = library;
            this.relocatable = relocatable;
        }

        public static Artifact forFile(File file) {
            return new Artifact(FileUtil.normalizeFile(file), null, false, false);
        }

        public File getFile() {
            return file;
        }

        public Artifact referencedLibrary() {
            return new Artifact(this.file, this.distributionPath, true, this.relocatable);
        }

        public boolean isReferencedLibrary() {
            return library;
        }

        public Artifact distributionPath(File distributionPath) {
            return new Artifact(this.file, distributionPath, this.library, this.relocatable);
        }

        public File getDistributionPath() {
            return distributionPath;
        }

        public Artifact relocatable() {
            return new Artifact(this.file, this.distributionPath, this.library, true);
        }

        public boolean isRelocatable() {
            return relocatable;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Artifact other = (Artifact) obj;
            if (this.file != other.file && (this.file == null || !this.file.equals(other.file))) {
                return false;
            }
            if (this.distributionPath != other.distributionPath && (this.distributionPath == null || !this.distributionPath.equals(other.distributionPath))) {
                return false;
            }
            if (this.library != other.library) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 59 * hash + (this.file != null ? this.file.hashCode() : 0);
            hash = 59 * hash + (this.distributionPath != null ? this.distributionPath.hashCode() : 0);
            hash = 59 * hash + (this.library ? 1 : 0);
            return hash;
        }

    }
}
