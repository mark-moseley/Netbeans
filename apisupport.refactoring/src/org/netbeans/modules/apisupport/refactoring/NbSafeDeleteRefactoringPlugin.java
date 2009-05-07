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

package org.netbeans.modules.apisupport.refactoring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.EditableManifest;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Milos Kleint - inspired by j2eerefactoring
 */
public class NbSafeDeleteRefactoringPlugin extends AbstractRefactoringPlugin {
    
    /** This one is important creature - makes sure that cycles between plugins won't appear */
    private static ThreadLocal semafor = new ThreadLocal();
    
    /**
     * Creates a new instance of NbRenameRefactoringPlugin
     */
    public NbSafeDeleteRefactoringPlugin(AbstractRefactoring refactoring) {
        super(refactoring);
    }
    
    public void cancelRequest() {
        
    }
    
    public Problem fastCheckParameters() {
        return null;
    }
    
    /** Collects refactoring elements for a given refactoring.
     * @param refactoringElements Collection of refactoring elements - the implementation of this method
     * should add refactoring elements to this collections. It should make no assumptions about the collection
     * content.
     * @return Problems found or null (if no problems were identified)
     */
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        if (semafor.get() != null) {
            return null;
        }
        semafor.set(new Object());
        try {
            SafeDeleteRefactoring delete = (SafeDeleteRefactoring)refactoring;
            Problem problem = null;
            Lookup lkp = delete.getRefactoringSource();
            InfoHolder infoholder = examineLookup(lkp);
            final TreePathHandle handle = lkp.lookup(TreePathHandle.class);
            
            Project project = FileOwnerQuery.getOwner(handle.getFileObject());
            if (project == null || project.getLookup().lookup(NbModuleProvider.class) == null) {
                // take just netbeans module development into account..
                return null;
            }
            
            if (infoholder.isClass) {
                checkManifest(project, infoholder.fullName, refactoringElements);
                checkLayer(project, infoholder.fullName, refactoringElements);
            }
            if (infoholder.isMethod) {
                checkMethodLayer(infoholder, handle.getFileObject(), refactoringElements);
            }
            if (infoholder.isConstructor) {
                checkConstructorLayer(infoholder, handle.getFileObject(), refactoringElements);
            }
            
            err.log("Gonna return problem: " + problem);
            return problem;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } finally {
            semafor.set(null);
        }
    }
    
    protected RefactoringElementImplementation createManifestRefactoring(
            String fqname,
            FileObject manifestFile,
            String attributeKey,
            String attributeValue,
            String section) {
        return new ManifestSafeDeleteRefactoringElement(manifestFile, attributeValue,
                attributeKey, section);
    }
    
    protected RefactoringElementImplementation createConstructorLayerRefactoring(String constructor, String fqname,
            LayerUtils.LayerHandle handle,
            FileObject layerFileObject,
            String layerAttribute) {
        return new LayerSafeDeleteRefactoringElement(constructor, handle, layerFileObject);
            
    }

    protected RefactoringElementImplementation createLayerRefactoring(String fqname,
            LayerUtils.LayerHandle handle,
            FileObject layerFileObject,
            String layerAttribute) {
        return new LayerSafeDeleteRefactoringElement(fqname, handle, layerFileObject, layerAttribute);
    
    }

    protected RefactoringElementImplementation createMethodLayerRefactoring(String method, String fqname,
            LayerUtils.LayerHandle handle,
            FileObject layerFileObject,
            String layerAttribute) {
        return new LayerSafeDeleteRefactoringElement(method, handle, layerFileObject);
    }
    
    
    public final class ManifestSafeDeleteRefactoringElement extends AbstractRefactoringElement {
        
        private String attrName;
        private String sectionName = null;
        private String oldContent;
        
        public ManifestSafeDeleteRefactoringElement(FileObject parentFile, String attributeValue, String attributeName) {
            this.name = attributeValue;
            this.parentFile = parentFile;
            attrName = attributeName;
            // read old content here. in the unprobable case when 2 classes are to be removed
            // and both are placed in same services file, we need the true original content
            oldContent = Utility.readFileIntoString(parentFile);
        }
        public ManifestSafeDeleteRefactoringElement(FileObject parentFile, String attributeValue, String attributeName, String secName) {
            this(parentFile, attributeValue, attributeName);
            sectionName = secName;
        }
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            if (sectionName != null) {
                return NbBundle.getMessage(NbSafeDeleteRefactoringPlugin.class, "TXT_ManifestSectionDelete", this.name, sectionName);
            }
            return NbBundle.getMessage(NbSafeDeleteRefactoringPlugin.class, "TXT_ManifestDelete", this.name, attrName);
        }
        
        public void performChange() {
            FileLock lock = null;
            OutputStream stream = null;
            InputStream instream = null;
            
            try {
                instream = parentFile.getInputStream();
                EditableManifest manifest = new EditableManifest(instream);
                instream.close();
                instream = null;
                if (sectionName != null) {
                    manifest.removeSection(name);
                } else {
                    manifest.removeAttribute(attrName, null);
                }
                lock = parentFile.lock();
                stream = parentFile.getOutputStream(lock);
                manifest.write(stream);
            } catch (FileNotFoundException ex) {
                //TODO
                err.notify(ex);
            } catch (IOException exc) {
                //TODO
                err.notify(exc);
            } catch (IllegalArgumentException exc) {
                // #161903: thrown from removeSection/Attribute means entry is probably already deleted,
                // can be ignored here
                err.notify(exc);
            } finally {
                if (instream != null) {
                    try {
                        instream.close();
                    } catch (IOException ex) {
                        err.notify(ex);
                    }
                }
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ex) {
                        err.notify(ex);
                    }
                }
                if (lock != null) {
                    lock.releaseLock();
                }
            }
        }
        
        public void undoChange() {
            if (oldContent != null) {
                Utility.writeFileFromString(parentFile, oldContent);
            }
        }
        
    }
    
    public final class LayerSafeDeleteRefactoringElement extends AbstractRefactoringElement  {
        
        private FileObject layerFO;
        private LayerUtils.LayerHandle handle;

        private String attribute;
        /**
         * Creates a new instance of LayerRenameRefactoringElement
         */
        public LayerSafeDeleteRefactoringElement(String name, LayerUtils.LayerHandle handle, FileObject layerFO, String attr) {
            this(name, handle, layerFO);
            attribute = attr;
        }
        
        public LayerSafeDeleteRefactoringElement(String name, LayerUtils.LayerHandle handle, FileObject layerFO) {
            this.name = name;
            this.handle = handle;
            parentFile = handle.getLayerFile();
            this.layerFO = layerFO;
        }
        
        /** Returns text describing the refactoring formatted for display (using HTML tags).
         * @return Formatted text.
         */
        public String getDisplayText() {
            return NbBundle.getMessage(NbSafeDeleteRefactoringPlugin.class, "TXT_LayerDelete", layerFO.getNameExt());
        }
        
        public void performChange() {
            boolean on = handle.isAutosave();
            if (!on) {
                //TODO is this a hack or not?
                handle.setAutosave(true);
            }
            try {
                if (attribute != null) {
                    layerFO.setAttribute(attribute, null);
                    if ("originalFile".equals(attribute)) {
                        layerFO.delete();
                    }
                } else {
                    layerFO.delete();
                }
                deleteEmptyParent(layerFO.getParent());
            } catch (IOException exc) {
                ErrorManager.getDefault().notify(exc);
            } 
            if (!on) {
                handle.setAutosave(false);
            }
            
        }

        private void deleteEmptyParent(FileObject parent) throws IOException {
            if (parent != null) {
                if (!parent.getChildren(true).hasMoreElements() && 
                        !parent.getAttributes().hasMoreElements()) {
                    FileObject parentToDel = parent.getParent();
                    parent.delete();
                    deleteEmptyParent(parentToDel);
                } 
            }
        }
        
        public void undoChange() {
            //TODO
        }

    }

}
