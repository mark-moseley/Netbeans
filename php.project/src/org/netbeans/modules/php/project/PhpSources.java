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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;

/**
 * Php Sources class.
 * Is a wrapper for Sources created using 'new SourcesHelper(AntProjectHelper, PropertyEvaluator).createSources()'.
 * Is created to add possibility to reload Sources object stored into Project's lookup.<br>
 * Implements ChangeListener to react on wrapped Sourses.<br>
 * Implements AntProjectListener to react on modified properties file.<br>
 * @author avk
 */
public class PhpSources implements Sources, ChangeListener, PropertyChangeListener {

    public static final String SOURCES_TYPE_PHP = "PHPSOURCE"; // NOI18N

    private final Project project;
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;
    private final SourceRoots seleniumRoots;

    private boolean dirty;
    private Sources delegate;
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public PhpSources(Project project, AntProjectHelper helper, PropertyEvaluator evaluator, final SourceRoots sourceRoots, final SourceRoots testRoots, final SourceRoots seleniumRoots) {
        assert project != null;
        assert helper != null;
        assert evaluator != null;
        assert sourceRoots != null;
        assert testRoots != null;
        assert seleniumRoots != null;

        this.project = project;
        this.helper = helper;
        this.evaluator = evaluator;
        this.sourceRoots = sourceRoots;
        this.testRoots = testRoots;
        this.seleniumRoots = seleniumRoots;

        this.evaluator.addPropertyChangeListener(this);
        this.sourceRoots.addPropertyChangeListener(this);
        this.testRoots.addPropertyChangeListener(this);
        this.seleniumRoots.addPropertyChangeListener(this);
        delegate = initSources(); // have to register external build roots eagerly
    }

    public SourceGroup[] getSourceGroups(final String type) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<SourceGroup[]>() {
            public SourceGroup[] run() {
                Sources _delegate;
                synchronized (PhpSources.this) {
                    if (dirty) {
                        delegate.removeChangeListener(PhpSources.this);
                        delegate = initSources();
                        delegate.addChangeListener(PhpSources.this);
                        dirty = false;
                    }
                    _delegate = delegate;
                }
                return _delegate.getSourceGroups(type);
            }
        });
    }

    public void addChangeListener(ChangeListener changeListener) {
        changeSupport.addChangeListener(changeListener);
    }

    public void removeChangeListener(ChangeListener changeListener) {
        changeSupport.removeChangeListener(changeListener);
    }

    private Sources initSources() {
        SourcesHelper sourcesHelper = new SourcesHelper(project, helper, evaluator);   //Safe to pass APH
        register(sourcesHelper, sourceRoots);
        register(sourcesHelper, testRoots);
        register(sourcesHelper, seleniumRoots);
        sourcesHelper.registerExternalRoots(FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        return sourcesHelper.createSources();
    }

    private void register(SourcesHelper sourcesHelper, SourceRoots roots) {
        String[] propNames = roots.getRootProperties();
        String[] rootNames = roots.getRootNames();
        for (int i = 0; i < propNames.length; i++) {
            String prop = propNames[i];
            String displayName = roots.getRootDisplayName(rootNames[i], prop);
            String loc = "${" + prop + "}"; // NOI18N
            sourcesHelper.addPrincipalSourceRoot(loc, displayName, null, null); // NOI18N
            sourcesHelper.addTypedSourceRoot(loc, SOURCES_TYPE_PHP, displayName, null, null);
         }
     }

    private void fireChange() {
        synchronized (this) {
            dirty = true;
        }
        changeSupport.fireChange();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (SourceRoots.PROP_ROOTS.equals(propName)) {
            fireChange();
        }
    }

    public void stateChanged(ChangeEvent event) {
        fireChange();
    }
}
