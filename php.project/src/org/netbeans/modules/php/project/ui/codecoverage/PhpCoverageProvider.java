/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.codecoverage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.text.Document;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.codecoverage.api.CoverageManager;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProvider;
import org.netbeans.modules.gsf.codecoverage.api.CoverageProviderHelper;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageDetails;
import org.netbeans.modules.gsf.codecoverage.api.FileCoverageSummary;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.netbeans.modules.php.project.ui.codecoverage.CoverageVO.FileVO;
import org.netbeans.modules.php.project.ui.codecoverage.CoverageVO.LineVO;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

// XXX coverage data could be fetched lazily but... not sure what is more performance friendly
/**
 * @author Tomas Mysik
 */
public final class PhpCoverageProvider implements CoverageProvider {
    private static final Set<String> MIME_TYPES = Collections.singleton(PhpSourcePath.MIME_TYPE);

    private final Object lock = new Object();
    private final PhpProject project;
    // GuardedBy(this)
    private Boolean enabled = null;
    // GuardedBy(lock)
    private CoverageVO coverage = null;

    public PhpCoverageProvider(PhpProject project) {
        assert project != null;

        this.project = project;
    }

    public void setCoverage(CoverageVO coverage) {
        assert coverage != null;
        assert isEnabled() : "Coverage provider must be enabled";
        synchronized (lock) {
            this.coverage = coverage;
        }
    }

    public static void notifyProjectOpened(Project project) {
        CoverageManager.INSTANCE.setEnabled(project, true);
    }

    public boolean supportsHitCounts() {
        return true;
    }

    public boolean supportsAggregation() {
        return false;
    }

    public synchronized boolean isEnabled() {
        if (enabled == null) {
            enabled = CoverageProviderHelper.isEnabled(project);
        }
        return enabled;
    }

    public void setEnabled(boolean on) {
        if (enabled != null && on == enabled) {
            return;
        }
        enabled = on;
        CoverageProviderHelper.setEnabled(project, on);
    }

    public synchronized boolean isAggregating() {
        throw new IllegalStateException("Aggregating is not supported");
    }

    public synchronized void setAggregating(boolean on) {
        throw new IllegalStateException("Aggregating is not supported");
    }

    public Set<String> getMimeTypes() {
        return MIME_TYPES;
    }

    public void clear() {
        synchronized (lock) {
            coverage = null;
        }
    }

    public FileCoverageDetails getDetails(FileObject fo, Document doc) {
        assert fo != null;
        CoverageVO cov = getCoverage();
        if (cov == null) {
            return null;
        }
        String path = FileUtil.toFile(fo).getAbsolutePath();
        // XXX optimize - hold files in a linked hash map
        for (FileVO file : cov.getFiles()) {
            if (path.equals(file.getPath())) {
                return new PhpFileCoverageDetails(fo, file);
            }
        }
        return null;
    }

    public List<FileCoverageSummary> getResults() {
        CoverageVO cov = getCoverage();
        if (cov == null) {
            return null;
        }
        List<FileCoverageSummary> result = new ArrayList<FileCoverageSummary>(cov.getFiles().size());
        for (FileVO file : cov.getFiles()) {
            result.add(getFileCoverageSummary(file));
        }
        return result;
    }

    private CoverageVO getCoverage() {
        CoverageVO cov = null;
        synchronized (lock) {
            cov = coverage;
        }
        return cov;
    }

    static FileCoverageSummary getFileCoverageSummary(FileVO file) {
        assert file != null;
        FileObject fo = FileUtil.toFileObject(new File(file.getPath()));
        int executedLineCount = getExecutedLineCount(file);
        return new FileCoverageSummary(
                fo,
                fo.getNameExt(),
                file.getMetrics().loc,
                executedLineCount,
                file.getMetrics().loc - executedLineCount,
                -1);
    }

    private static int getExecutedLineCount(FileVO file) {
        int executed = file.getMetrics().loc;
        for (LineVO line : file.getLines()) {
            if (line.count == 0) {
                executed--;
            }
        }
        return executed;
    }
}
