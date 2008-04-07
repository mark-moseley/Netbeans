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
package org.netbeans.modules.php.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Radek Matous
 */
abstract class CopySupport {

    static CopySupport forProject() {
	return new CopyImpl();
    }

    abstract protected void projectOpened(PhpProject project);

    abstract protected void projectClosed(PhpProject project);

    private static final class CopyImpl extends CopySupport implements PropertyChangeListener, FileChangeListener {

	private static final SourceTargetPair<FileObject, FileObject> INVALID_CONFIG = SourceTargetPair.forInvalidConfig();
	private static final RequestProcessor RP = new RequestProcessor("PHP replication"); // NOI18N

	private volatile PhpProject project;
	private SourceTargetPair<FileObject, FileObject> config;
	private FileSystem fileSystem;
	private FileChangeListener weakFileChangeListener;	
	private boolean isProjectOpened;
	private static final Queue<SourceTargetPair<FileObject, File>> allPairs =
		new ConcurrentLinkedQueue<SourceTargetPair<FileObject, File>>();
	private static RequestProcessor.Task task = RP.create(new Runnable() {

	    public void run() {
		SourceTargetPair<FileObject, File> nextPair = allPairs.poll();
		Map<File, SourceTargetPair<FileObject, File>> m = new HashMap<File, SourceTargetPair<FileObject, File>>();
		while (nextPair != null) {
                    m.put(nextPair.getTarget(), nextPair);
                    nextPair = allPairs.poll();
		}
		for (SourceTargetPair<FileObject, File> pair : m.values()) {
		    if (pair.isCopyModifier()) {
			doCopy(pair);
		    } else if (pair.isDeleteModifier()) {
			doDelete(pair);
		    } else {
			assert false;
		    }
		}
	    }

	    private void doCopy(SourceTargetPair<FileObject, File> nextPair) {
		try {
		    File target = nextPair.getTarget();
		    File targetParent = target.getParentFile();
		    FileObject source = nextPair.getSource();
                    if (source.isData()) {
			doDelete(target);
                        FileObject parent = FileUtil.createFolder(targetParent);
                        FileUtil.copyFile(nextPair.getSource(), parent, source.getName(), source.getExt());
                    } else {
			String[] childs = target.list();
			if (childs == null || childs.length == 0) {
			    doDelete(target);
			}			
                        FileUtil.createFolder(target);
                    }
		} catch (IOException ex) {
		    Exceptions.printStackTrace(ex);
		}
	    }

	    private void doDelete(SourceTargetPair<FileObject, File> nextPair) {
		try {
		    doDelete(nextPair.getTarget());
		} catch (IOException ex) {
		    Exceptions.printStackTrace(ex);
		}
	    }

	    private void doDelete(File target) throws IOException {
		if (target.exists()) {
		    FileObject targetFo = FileUtil.toFileObject(target);
		    if (targetFo != null && targetFo.isValid()) {
			targetFo.delete();
		    } else {
			target.delete();
		    }
		}
	    }
	});

	public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
	    final String propertyName = propertyChangeEvent.getPropertyName();
	    if (propertyName.equals(PhpProjectProperties.COPY_SRC_TARGET) ||
		    propertyName.equals(PhpProjectProperties.SRC_DIR) ||
		    propertyName.equals(PhpProjectProperties.COPY_SRC_FILES)) {
		ProjectManager.mutex().readAccess(new Runnable() {
		    public void run() {
			setConfig(new ConfigurationFactory(project).getConfiguration());
		    }
		});		
	    }
	}

	public void fileFolderCreated(FileEvent fe) {
	    prepareForCopy(fe);
	}

	public void fileDataCreated(FileEvent fe) {
	    prepareForCopy(fe);
	}

	public void fileChanged(FileEvent fe) {
	    prepareForCopy(fe);
	}

	public void fileDeleted(FileEvent fe) {
	    prepareForDelete(fe);
	}

	public void fileRenamed(FileRenameEvent fe) {
            prepareForRename(fe);	    
	}

	public void fileAttributeChanged(FileAttributeEvent fe) {
	}

	@Override
	protected void projectOpened(PhpProject project) {
	    init(project);
	    isProjectOpened = true;	    	    
	    start();
	}

	@Override
	protected void projectClosed(PhpProject project) {	    
	    //init(project);
	    isProjectOpened = false;	    
	    stop();
	}

	PhpProject getProject() {
	    return project;
	}

	synchronized SourceTargetPair<FileObject, FileObject> getConfig() {
	    return (config != null) ? config : INVALID_CONFIG;
	}

	void setConfig(SourceTargetPair<FileObject, FileObject> config) {
	    assert config != null;
	    synchronized (this) {
		this.config = config;
	    }
	    start();
	}

	private FileObject getSourceRoot() {
	    return getConfig().getSource();
	}

	private FileObject getTargetRoot() {
	    return getConfig().getTarget();
	}

	private void init(PhpProject project) {
	    if (this.project == null) {
		this.project = project;
		PropertyEvaluator evaluator = project.getEvaluator();
		evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
		ConfigurationFactory factory = new ConfigurationFactory(project);
		setConfig(factory.getConfiguration());
	    } else {
		assert this.project.equals(project);
	    }
	}

	private boolean isProjectFileObject(FileObject fo) {
	    return !getConfig().isInvalidModifier() && FileUtil.isParentOf(getConfig().getSource(), fo);
	}

	private void prepareForCopy(FileObject source) {
	    if (isProjectFileObject(source)) {
		assert source.isValid();
		prepareOperation(source, false);
	    }
	}

	private void prepareForCopy(FileEvent fe) {
	    SourceTargetPair<FileObject, FileObject> config = getConfig();
	    if (config != null && !config.isInvalidModifier() && config.isCopyModifier()) {
		FileObject source = fe.getFile();
		prepareForCopy(source);
	    }
	}

	private void prepareForDelete(FileEvent fe) {
	    SourceTargetPair<FileObject, FileObject> config = getConfig();
	    if (config != null && !config.isInvalidModifier() && config.isCopyModifier()) {
		FileObject source = fe.getFile();
		prepareForDelete(source);
	    }
	}

	private void prepareForDelete(FileObject source) {
	    if (isProjectFileObject(source)) {
		assert !source.isValid();
		prepareOperation(source, true);
	    }
	}

	private void prepareForRename(FileRenameEvent fe) {
	    SourceTargetPair<FileObject, FileObject> config = getConfig();
	    if (config != null && !config.isInvalidModifier() && config.isCopyModifier()) {
                FileObject sourceFo = fe.getFile();                            
                if (isProjectFileObject(sourceFo)) {
                    if (sourceFo.isFolder()) {
                        FileObject[] children = sourceFo.getChildren();
                        for (FileObject fileObject : children) {
                            prepareForCopy(fileObject);
                        }
                    } else {
                        prepareForCopy(sourceFo);
                    }
                    
                    File target = targetForSource(sourceFo);
                    StringBuilder sb = new StringBuilder();
                    sb.append(fe.getName());
                    String ext = fe.getExt();                    
                    if ( ext != null && ext.trim().length() > 0) {
                        sb.append('.').append(ext);//NOI18N
                    }
                    File toDelete = new File(target.getParent(),sb.toString());                     
                    prepareOperation(SourceTargetPair.forDelete(sourceFo, toDelete));
                }
            }
	}
        
	private synchronized void prepareInitCopy() {	    
	    final FileObject targetRoot = getTargetRoot();
	    File target = FileUtil.toFile(targetRoot);
	    if (target != null) {
		String[] childNames = target.list();
		if (childNames != null && childNames.length == 0) {
		    Enumeration<? extends FileObject> e = getSourceRoot().getChildren(true);
		    while (e.hasMoreElements()) {
			FileObject source = e.nextElement();
			assert isProjectFileObject(source);
			prepareForCopy(source);
		    }
		}
	    }
	}

	private void prepareOperation(FileObject source, boolean delete) {
	    File target = targetForSource(source);
	    if (delete) {
		prepareOperation(SourceTargetPair.forDelete(source, target));
	    } else {
		prepareOperation(SourceTargetPair.forCopy(source, target));
	    }
	    task.schedule(300);
	}

	private void prepareOperation(SourceTargetPair<FileObject, File> srcTargetPair) {
            allPairs.offer(srcTargetPair);
            task.schedule(300);
        }   
        
	synchronized private void start() {
	    stop();
	    final SourceTargetPair<FileObject, FileObject> config = getConfig();
	    if (config != null && !config.isInvalidModifier() && config.isCopyModifier()) {
		prepareInitCopy();
		if (weakFileChangeListener == null && isProjectOpened) {
		    try {
			fileSystem = config.getSource().getFileSystem();
			weakFileChangeListener = FileUtil.weakFileChangeListener(this, fileSystem);
			fileSystem.addFileChangeListener(weakFileChangeListener);
		    } catch (FileStateInvalidException ex) {
			Exceptions.printStackTrace(ex);
		    }
		}
	    }
	}

	synchronized private void stop() {
	    if (weakFileChangeListener != null) {
		fileSystem.removeFileChangeListener(weakFileChangeListener);
		fileSystem = null;
		weakFileChangeListener = null;
	    }
	}

	private String relativePathForSource(FileObject fo) {
	    final FileObject sourceRoot = getSourceRoot();
	    assert FileUtil.isParentOf(sourceRoot, fo);
	    return FileUtil.getRelativePath(sourceRoot, fo);
	}

        private File targetForSource(FileObject source) {
            String relativePath = relativePathForSource(source);
            File targetRoot = FileUtil.toFile(getConfig().getTarget());
            assert targetRoot != null;
            File target = new File(targetRoot, relativePath);
            return target;
        }
    }

    private static class ConfigurationFactory {
	private final PhpProject project;
	private final SourceTargetPair<FileObject, FileObject> config;
	private final AntProjectHelper antProjectHelper;

	ConfigurationFactory(PhpProject project) {
	    assert project != null;
	    this.project = project;
	    final boolean copyEnabled = isCopyEnabled();
	    final FileObject sourceRoot = getSourceRoot();
	    final FileObject targetRoot = isCopyEnabled() ? getTargetRoot(true) : getTargetRoot(false);
	    if (sourceRoot != null && targetRoot != null && sourceRoot != targetRoot && targetRoot.canWrite()) {
		config = SourceTargetPair.forConfig(sourceRoot, targetRoot, copyEnabled);
	    } else {
		config = CopyImpl.INVALID_CONFIG;
	    }
	    antProjectHelper = project.getHelper();
	    assert antProjectHelper != null;
	}


	SourceTargetPair<FileObject, FileObject> getConfiguration() {
	    assert config != null;
	    return config;
	}

	boolean isCopyEnabled() {
	    boolean retval = false;
	    String copySrcFiles = project.getEvaluator().getProperty(PhpProjectProperties.COPY_SRC_FILES);
	    if (copySrcFiles != null && copySrcFiles.trim().length() > 0) {
		retval = Boolean.parseBoolean(copySrcFiles);
	    }
	    return retval;
	}

	private FileObject getSourceRoot() {
	    FileObject retval = null;
	    FileObject[] roots = Utils.getSourceObjects(project);
	    if (roots != null && roots.length > 0) {
		retval = roots[0];
		assert FileUtil.toFile(retval) != null : retval.toString();
		assert retval.isFolder() : retval.getPath();
	    }
	    return retval;
	}

	private FileObject getTargetRoot(boolean create) {
	    FileObject retval = null;
	    String targetString = project.getEvaluator().getProperty(PhpProjectProperties.COPY_SRC_TARGET);
	    if (targetString != null && targetString.trim().length() > 0) {
		File target = FileUtil.normalizeFile(new File(targetString));
		if (create) {
		    try {
			retval = FileUtil.createFolder(target);
		    } catch (IOException ex) {
			Exceptions.printStackTrace(ex);
		    }
		} else {
		    retval = FileUtil.toFileObject(target);
		}
	    }
	    return retval;
	}
    }

    private static class SourceTargetPair<S, T> {

	private static enum Modifier {

	    IDLE, COPY, DELETE, INVALID
	}
	private final S source;
	private final T target;
	private final Modifier modifier;

	static SourceTargetPair<FileObject, FileObject> forInvalidConfig() {
	    return new SourceTargetPair<FileObject, FileObject>();
	}

	static SourceTargetPair<FileObject, FileObject> forConfig(FileObject sourceRoot, FileObject targetRoot, boolean copyEnabled) {
	    assert sourceRoot.isFolder();
	    assert targetRoot.isFolder();
	    return new SourceTargetPair<FileObject, FileObject>(sourceRoot, targetRoot, copyEnabled ? Modifier.COPY : Modifier.IDLE);
	}

	static SourceTargetPair<FileObject, File> forDelete(FileObject source, File target) {
	    return new SourceTargetPair<FileObject, File>(source, target, Modifier.DELETE);
	}

	static SourceTargetPair<FileObject, File> forCopy(FileObject source, File target) {
	    return new SourceTargetPair<FileObject, File>(source, target, Modifier.COPY);
	}

	private SourceTargetPair() {
	    this.modifier = Modifier.INVALID;
	    this.source = null;
	    this.target = null;
	}

	private SourceTargetPair(final S sourceRoot, final T targetRoot,
		final Modifier modifier) {
	    this.modifier = modifier;
	    assert sourceRoot != null;
	    assert targetRoot != null;
	    this.source = sourceRoot;
	    this.target = targetRoot;
	}

	S getSource() {
	    assert isInvalidModifier() || source != null;
	    return source;
	}

	T getTarget() {
	    assert isInvalidModifier() || target != null;
	    return target;
	}

	boolean isIdleModifier() {
	    return modifier.equals(Modifier.IDLE);
	}

	boolean isCopyModifier() {
	    return modifier.equals(Modifier.COPY);
	}

	boolean isDeleteModifier() {
	    return modifier.equals(Modifier.DELETE);
	}

	boolean isInvalidModifier() {
	    return modifier.equals(Modifier.INVALID);
	}
    }
}
