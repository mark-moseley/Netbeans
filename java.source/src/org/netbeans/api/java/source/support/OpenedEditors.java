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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.api.java.source.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.java.source.JavaSourceSupportAccessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Parameters;

/**
 *
 * @author Jan Lahoda
 */
class OpenedEditors implements PropertyChangeListener {

    private List<JTextComponent> visibleEditors = new ArrayList<JTextComponent>();
    private Map<JTextComponent, FileObject> visibleEditors2Files = new HashMap<JTextComponent, FileObject>();
    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();

    private static OpenedEditors DEFAULT;

    private OpenedEditors() {
        EditorRegistry.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                stateChanged();
            }
        });
    }

    public static synchronized OpenedEditors getDefault() {
        if (DEFAULT == null) {
            DEFAULT = new OpenedEditors();
        }

        return DEFAULT;
    }

    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChangeEvent() {
        ChangeEvent e = new ChangeEvent(this);
        List<ChangeListener> listenersCopy = null;

        synchronized (this) {
            listenersCopy = new ArrayList(listeners);
        }

        for (ChangeListener l : listenersCopy) {
            l.stateChanged(e);
        }
    }

    public synchronized List<JTextComponent> getVisibleEditors() {
        List<JTextComponent> result = new LinkedList<JTextComponent>();
        
        for (JTextComponent c : visibleEditors) {
            if (visibleEditors2Files.get(c) != null) {
                result.add(c);
            }
        }
        
        return Collections.unmodifiableList(result);
    }

    public synchronized Collection<FileObject> getVisibleEditorsFiles() {
        List<FileObject> result = new LinkedList<FileObject>();
        
        for (FileObject file : visibleEditors2Files.values()) {
            if (file != null) {
                result.add(file);
            }
        }
        
        return Collections.unmodifiableCollection(result);
    }

    public synchronized void stateChanged() {
        for (JTextComponent c : visibleEditors) {
            c.removePropertyChangeListener(this);
            visibleEditors2Files.remove(c);
        }

        visibleEditors.clear();

        JTextComponent editor = EditorRegistry.lastFocusedComponent();

        FileObject fo = editor != null ? getFileObject(editor) : null;
        if (editor instanceof JEditorPane && fo != null && JavaSource.forFileObject(fo) != null) {
            visibleEditors.add(editor);
        }

        for (JTextComponent c : visibleEditors) {
            c.addPropertyChangeListener(this);
            visibleEditors2Files.put(c, getFileObject(c));
        }

        fireChangeEvent();
    }

    public synchronized void propertyChange(PropertyChangeEvent evt) {
        JTextComponent c = (JTextComponent) evt.getSource();
        FileObject originalFile = visibleEditors2Files.get(c);
        FileObject nueFile = getFileObject(c);

        if (originalFile != nueFile) {
            visibleEditors2Files.put(c, nueFile);
            fireChangeEvent();
        }
    }

    static FileObject getFileObject(JTextComponent pane) {
        Object source = pane.getDocument().getProperty(Document.StreamDescriptionProperty);
        
        if (!(source instanceof DataObject)) {
            return null;
        }
        
        DataObject file = (DataObject) source;
        
        if (file != null) {
            return file.getPrimaryFile();
        }

        return null;
    }

    /**Checks if the given file is supported. See {@link #filterSupportedMIMETypes}
     * for more details.
     *
     * @param file to check
     * @param type the type to check for the {@link SupportedMimeTypes} annotation
     * @return true if and only if the given file is supported (see {@link #filterSupportedMIMETypes})
     * @throws NullPointerException if <code>file == null</code> or <code>type == null</code>
     */
    public static boolean isSupported(FileObject file, String... mimeTypes) throws NullPointerException {
        Parameters.notNull("files", file);
        
        return !filterSupportedMIMETypes(Collections.singletonList(file), mimeTypes).isEmpty();
    }
    
    /**Filter unsupported files from the <code>files</code> parameter. A supported file
     * <code>f</code> is defined as follows:
     * <ul>
     *     <li><code>JavaSource.forFileObject(f) != null</code></li>
     *     <li>If the <code>type</code> is annotated with the {@link SupportedMimeTypes} annotation,
     *         the file is supported if <code>type.getAnnotation(SupportedMimeTypes.class).value()</code>
     *         contains <code>FileUtil.getMIMEType(f)</code>.
     *     </li>
     *     <li>If the <code>type</code> is not annotated with the {@link SupportedMimeTypes} annotation,
     *         the file is supported if <code>FileUtil.getMIMEType(f) == "text/x-java"</code>.
     * </ul>
     *
     * @param files the list of files to filter
     * @param type the type to check for the {@link SupportedMimeTypes} annotation
     * @return list of files that are supported (see above).
     * @throws NullPointerException if <code>files == null</code> or <code>type == null</code>
     */
    public static List<FileObject> filterSupportedMIMETypes(Collection<FileObject> files, String... mimeTypes) throws NullPointerException {
        Parameters.notNull("files", files);
        
        boolean            allowJavaExtension = false;
        
        if (mimeTypes == null) {
            mimeTypes = new String[] {"text/x-java"};
            allowJavaExtension = true;
        }
        
        List<String>       mimeTypesList = Arrays.asList(mimeTypes);
        boolean            allowAll  = mimeTypesList.contains("*");
        List<FileObject>   result    = new LinkedList<FileObject>();
        
        Logger.getLogger(OpenedEditors.class.getName()).log(Level.FINER, "mimeTypesList={0}", mimeTypesList);
        
        for (FileObject f : files) {
            Logger.getLogger(OpenedEditors.class.getName()).log(Level.FINER, "analyzing={0}", f);
            
            if (JavaSource.forFileObject(f) == null)
                continue;
            
            if (allowAll) {
                result.add(f);
                continue;
            }
            
            if (allowJavaExtension && "java".equals(f.getExt())) {
                result.add(f);
                continue;
            }
            
            String fileMimeType = FileUtil.getMIMEType(f);
            
            Logger.getLogger(OpenedEditors.class.getName()).log(Level.FINER, "fileMimeType={0}", fileMimeType);
            
            if (mimeTypesList.contains(fileMimeType)) {
                result.add(f);
                continue;
            }
            
            String shorterMimeType = fileMimeType;
            
            while (true) {
                int slash = shorterMimeType.indexOf('/');
                
                if (slash == (-1))
                    break;
                
                int plus  = shorterMimeType.indexOf('+', slash);
                
                if (plus == (-1))
                    break;
                
                shorterMimeType = shorterMimeType.substring(0, slash + 1) + shorterMimeType.substring(plus + 1);
                
                if (mimeTypesList.contains(shorterMimeType)) {
                    result.add(f);
                    break;
                }
            }
        }
        
        Logger.getLogger(OpenedEditors.class.getName()).log(Level.FINE, "filter({0}, {1})={2}", new Object[] {files, mimeTypesList, result});
        
        return result;
    }
    
    static {
        JavaSourceSupportAccessor.ACCESSOR = new JavaSourceSupportAccessor() {
            public Collection<FileObject> getVisibleEditorsFiles() {
                return OpenedEditors.getDefault().getVisibleEditorsFiles();
            }
        };
    }
}
