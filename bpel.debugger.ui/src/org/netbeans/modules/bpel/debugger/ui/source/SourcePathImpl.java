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

package org.netbeans.modules.bpel.debugger.ui.source;

import java.awt.Dialog;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.bpel.debugger.api.BpelDebugger;
import org.netbeans.modules.bpel.debugger.api.BpelSourcesRegistry;
import org.netbeans.modules.bpel.debugger.api.SessionCookie;
import org.netbeans.modules.bpel.debugger.api.SourcePath;
import org.netbeans.modules.bpel.debugger.spi.SourcePathSelectionProvider;
import org.netbeans.modules.bpel.debugger.ui.util.EditorUtil;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.modules.bpel.model.api.Process;

/**
 *
 * @author Alexander Zgursky
 */
public class SourcePathImpl implements SourcePath {
    
    private final ContextProvider          myLookupProvider;
    private final BpelDebugger             myDebugger;
    private final Object myProcessingLock = new Object();
    private final RequestProcessor myRequestProcessor =
            new RequestProcessor("BpelSourcesLocator"); //NOI18N
    private final BpelSourcesRegistry mySourcesRegistry;
    private final PropertyChangeSupport myPcs =
            new PropertyChangeSupport(this);
    
    private RequestProcessor.Task myCurrentTask;
    
    private Set<String> myUpdatedSources =
            new HashSet<String>();
    
    private Set<String> myAvailableSources =
            new TreeSet<String>();
    
    private Set<String> mySelectedSources =
            new HashSet<String>();
    
    private Map<QName, Set<String>> myAvailableSourcesByQName =
            new HashMap<QName, Set<String>>();
    
    private Map<String, QName> myQNames =
            new HashMap<String, QName>();
    
    private Map<QName, String>       mySourceMap =
            new HashMap<QName, String>();

    private SessionCookie mySessionCookie;

    private SourcePathSelectionProvider mySelectionProvider;

    /** Creates new instance of SourcePath.
     *
     * @param lookupProvider debugger context
     */
    public SourcePathImpl(ContextProvider lookupProvider) {
        myLookupProvider = lookupProvider;
        myDebugger = (BpelDebugger) lookupProvider.lookupFirst
                (null, BpelDebugger.class);
        mySourcesRegistry = (BpelSourcesRegistry)Lookup.
                getDefault().lookup(BpelSourcesRegistry.class);
        updateAvailableSources();
    }
    
    public QName getProcessQName(String path) {
        while (true) {
            RequestProcessor.Task task;
            synchronized (myProcessingLock) {
                if (myCurrentTask == null) {
                    return myQNames.get(path);
                } else {
                    task = myCurrentTask;
                }
            }
            task.waitFinished();
        }
    }
    
    public String getSourcePath(QName processQName) {
        while (true) {
            RequestProcessor.Task task;
            synchronized (myProcessingLock) {
                if (myCurrentTask == null) {
                    Set<String> sources = new HashSet<String>();
                    sources.addAll(myAvailableSourcesByQName.get(processQName));
                    sources.retainAll(mySelectedSources);
                    if (sources.isEmpty()) {
                        return null;
                    } else if (sources.size() == 1) {
                        return sources.iterator().next();
                    } else {
                        return selectSource(processQName, sources);
                    }
                } else {
                    task = myCurrentTask;
                }
            }
            task.waitFinished();
        }
//        return mySourceMap.get(processQName);
    }

    public String[] getAvailableSources() {
        while (true) {
            RequestProcessor.Task task;
            synchronized (myProcessingLock) {
                if (myCurrentTask == null) {
                    return myAvailableSources.toArray(new String[myAvailableSources.size()]);
                } else {
                    task = myCurrentTask;
                }
            }
            task.waitFinished();
        }
    }

    public String[] getSelectedSources() {
        while (true) {
            RequestProcessor.Task task;
            synchronized (myProcessingLock) {
                if (myCurrentTask == null) {
                    return mySelectedSources.toArray(new String[mySelectedSources.size()]);
                } else {
                    task = myCurrentTask;
                }
            }
            task.waitFinished();
        }
    }

    public void setSelectedSources(String[] roots) {
        while (true) {
            RequestProcessor.Task task;
            synchronized (myProcessingLock) {
                if (myCurrentTask == null) {
                    mySelectedSources.clear();
                    for (String path : roots) {
                        mySelectedSources.add(path);
                    }
                    return;
                } else {
                    task = myCurrentTask;
                }
            }
            task.waitFinished();
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        myPcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        myPcs.removePropertyChangeListener(listener);
    }
    
    protected void addAvailableSource(String path) {
        synchronized (myProcessingLock) {
            myUpdatedSources.add(path);
            if (myCurrentTask == null) {
                myCurrentTask = myRequestProcessor.post(new MyProcessingRunnable());
            }
        }
    }
    
    private void updateAvailableSources() {
        String[] sourceRoots = mySourcesRegistry.getSourceRoots();
        for (String sourceRoot : sourceRoots) {
            addAvailableSource(sourceRoot);
        }
    }
    
    private String selectSource(QName processQName, Set<String> sources) {
        SeveralSourceFilesWarning panel = new SeveralSourceFilesWarning(
                processQName,
                sources.toArray(new String[sources.size()]));
        Object[] options = new Object[] {DialogDescriptor.OK_OPTION};
        DialogDescriptor desc = new DialogDescriptor(
                panel,
                NbBundle.getMessage(
                SeveralSourceFilesWarning.class, "CTL_MORE_THAN_ONE_SOURCE_WARNING_TITLE" // NOI18N
                ),
                true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null
                );
        Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
        dlg.setVisible(true);

        String path = panel.getSelectedInstance();
        sources.remove(path);
        mySelectedSources.removeAll(sources);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                myPcs.firePropertyChange(null, null, null);
            }
        });
        dlg.dispose();
        return path;
    }
    
    private synchronized SessionCookie getSessionCookie() {
        if (mySessionCookie == null) {
            mySessionCookie = (SessionCookie) myLookupProvider.lookupFirst(
                    null, SessionCookie.class);
        }
        return mySessionCookie;
    }
    
    private synchronized SourcePathSelectionProvider getSelectionProvider() {
        if (mySelectionProvider == null) {
            mySelectionProvider = (SourcePathSelectionProvider) myLookupProvider.lookupFirst(
                    null, SourcePathSelectionProvider.class);
        }
        return mySelectionProvider;
    }
    
    private class MyProcessingRunnable implements Runnable {
        public void run() {
            try {
                while (true) {
                    String nextSource;
                    synchronized(myProcessingLock) {
                        if (myUpdatedSources.isEmpty()) {
                            myCurrentTask = null;
                            break;
                        }
                        Iterator<String> iter = myUpdatedSources.iterator();
                        nextSource = iter.next();
                        iter.remove();
                    }
                    processSingleSource(nextSource);
                }
            } finally {
                //just for a back up
                myCurrentTask = null;
            }
        }
        
        private void processSingleSource(String path) {
            FileObject fo = FileUtil.toFileObject(new File(path));
            if (fo == null) {
                return;
            }
            registerFileObject(fo);
        }
        
        private void registerFileObject(FileObject fo) {
            if (fo.isFolder()) {
                //fo.addFileChangeListener(myFolderListener);
                for (FileObject child : fo.getChildren()) {
                    registerFileObject(child);
                }
            } else {
                DataObject dataObject;
                try {
                    dataObject = DataObject.find(fo);
                } catch (DataObjectNotFoundException ex) {
                    return;
                }

                BpelModel bpelModel = EditorUtil.getBpelModel(dataObject);
                if (bpelModel == null) {
                    return;
                }
                
                Process process = bpelModel.getProcess();
                if (process == null) {
                    return;
                }
                
                String name = process.getName();
                if (name == null || name.trim().equals("")) {
                    return;
                }
                
                File file = FileUtil.toFile(fo);
                if (fo == null) {
                    return;
                }
                QName processQName = new QName(process.getTargetNamespace(), name);
                register(file.getPath(), processQName);
            }
        }
        
        private void register(String path, QName processQName) {
            myQNames.put(path, processQName);
            myAvailableSources.add(path);
            if (getSelectionProvider() != null) {
                if (getSelectionProvider().isSelected(path)) {
                    mySelectedSources.add(path);
                }
            } else {
                mySelectedSources.add(path);
            }

            Set<String> availableForQName =
                    myAvailableSourcesByQName.get(processQName);
            if (availableForQName == null) {
                availableForQName = new HashSet<String>();
                myAvailableSourcesByQName.put(processQName, availableForQName);
            }
            availableForQName.add(path);
        }
    }
}
