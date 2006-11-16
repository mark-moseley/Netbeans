/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */                                                                                                                                                                                                                           
                                                                                                                                                                                                                               
package org.netbeans.api.java.source;                                                                                                                                                                                          
                                                                                                                                                                                                                               
import com.sun.source.tree.Tree;                                                                                                                                                                                               
import com.sun.source.util.TreePath;                                                                                                                                                                                           
import com.sun.tools.javac.tree.JCTree;                                                                                                                                                                                        
import java.util.ArrayList;
import java.util.logging.Logger;                                                                                                                                                                                                    
import javax.lang.model.element.Element;                                                                                                                                                                                       
import javax.swing.text.Position.Bias;                                                                                                                                                                                         
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;                                                                                                                                                                                     
import org.openide.loaders.DataObject;                                                                                                                                                                                         
import org.openide.loaders.DataObjectNotFoundException;                                                                                                                                                                        
import org.openide.text.CloneableEditorSupport;                                                                                                                                                                                
import org.openide.text.PositionRef;                                                                                                                                                                                           
                                                                                                                                                                                                                               
/**                                                                                                                                                                                                                            
 * Represents a handle for {@link TreePath} which can be kept and later resolved                                                                                                                                               
 * by another javac. The Javac {@link Element}s are valid only in the single                                                                                                                                                   
 * {@link javax.tools.CompilationTask} or single run of the                                                                                                                                                                    
 * {@link org.netbeans.api.java.source.CancellableTask}. If the client needs to                                                                                                                                                
 * keep a reference to the {@link TreePath} and use it in the other CancellableTask                                                                                                                                            
 * he has to serialize it into the {@link TreePathHandle}.                                                                                                                                                                     
 * <div class="nonnormative">                                                                                                                                                                                                  
 * <p>                                                                                                                                                                                                                         
 * Typical usage of TreePathHandle enclElIsCorrespondingEl:                                                                                                                                                                    
 * </p>                                                                                                                                                                                                                        
 * <pre>                                                                                                                                                                                                                       
 * final TreePathHandle[] tpHandle = new TreePathHandle[1];                                                                                                                                                                    
 * javaSource.runCompileControlTask(new CancellableTask<CompilationController>() {                                                                                                                                             
 *     public void run(CompilationController compilationController) {                                                                                                                                                          
 *         parameter.toPhase(Phase.RESOLVED);                                                                                                                                                                                  
 *         CompilationUnitTree cu = compilationController.getTree ();                                                                                                                                                          
 *         TreePath treePath = getInterestingTreePath (cu);                                                                                                                                                                    
 *         treePathHandle[0] = TreePathHandle.create (element, compilationController);                                                                                                                                         
 *    }                                                                                                                                                                                                                        
 * },priority);                                                                                                                                                                                                                
 *                                                                                                                                                                                                                             
 * otherJavaSource.runCompileControlTask(new CancellableTask<CompilationController>() {                                                                                                                                        
 *     public void run(CompilationController compilationController) {                                                                                                                                                          
 *         parameter.toPhase(Phase.RESOLVED);                                                                                                                                                                                  
 *         TreePath treePath = treePathHanlde[0].resolve (compilationController);                                                                                                                                              
 *         ....                                                                                                                                                                                                                
 *    }                                                                                                                                                                                                                        
 * },priority);                                                                                                                                                                                                                
 * </pre>                                                                                                                                                                                                                      
 * </div>                                                                                                                                                                                                                      
 *                                                                                                                                                                                                                             
 *                                                                                                                                                                                                                             
 * @author Jan Becicka                                                                                                                                                                                                         
 */                                                                                                                                                                                                                            
public final class TreePathHandle {                                                                                                                                                                                            
                                                                                                                                                                                                                               
    private PositionRef position;                                                                                                                                                                                              
    private KindPath kindPath;                                                                                                                                                                                                 
    private FileObject file;                                                                                                                                                                                                   
    private ElementHandle enclosingElement;                                                                                                                                                                                    
    private boolean enclElIsCorrespondingEl;                                                                                                                                                                                   
                                                                                                                                                                                                                               
    /**                                                                                                                                                                                                                        
     * getter for FileObject from give TreePathHandle                                                                                                                                                                          
     * @return FileObject for which was this handle created                                                                                                                                                                    
     */                                                                                                                                                                                                                        
    public FileObject getFileObject() {                                                                                                                                                                                        
        return file;                                                                                                                                                                                                           
    }                                                                                                                                                                                                                          
                                                                                                                                                                                                                               
    /**                                                                                                                                                                                                                        
     * Resolves an {@link TreePath} from the {@link TreePathHandle}.                                                                                                                                                           
     * @param compilationInfo representing the {@link javax.tools.CompilationTask}                                                                                                                                             
     * @return resolved subclass of {@link Element} or null if the elment does not exist on                                                                                                                                    
     * the classpath/sourcepath of {@link javax.tools.CompilationTask}.
     * @throws {@link IllegalArgumentException} when this {@link TreePathHandle} is not created for a source
     * represented by the compilationInfo.
     */                                                                                                                                                                                                                        
    public TreePath resolve (final CompilationInfo compilationInfo) throws IllegalArgumentException {
        assert compilationInfo != null;
        if (!compilationInfo.getFileObject().equals(getFileObject())) {
            throw new IllegalArgumentException ("TreePathHandle ["+FileUtil.getFileDisplayName(getFileObject())+"] was not created from " + FileUtil.getFileDisplayName(compilationInfo.getFileObject()));
        }                                                                                                                                                                                                                      
        Element element = enclosingElement.resolve(compilationInfo);                                                                                                                                                           
        TreePath tp = null;                                                                                                                                                                                                    
        if (element != null) {                                                                                                                                                                                                 
            TreePath startPath = compilationInfo.getTrees().getPath(element);                                                                                                                                                  
            if (startPath == null) {                                                                                                                                                                                           
                Logger.getLogger(TreePathHandle.class.getName()).fine("compilationInfo.getTrees().getPath(element) returned null for element %s " + element + "(" +file.getPath() +")");    //NOI18N
            } else {                                                                                                                                                                                                           
                tp = compilationInfo.getTreeUtilities().pathFor(startPath, position.getOffset()+1);
            }                                                                                                                                                                                                                  
        }                                                                                                                                                                                                                      
        if (tp == null) {                                                                                                                                                                                                      
            tp = compilationInfo.getTreeUtilities().pathFor(position.getOffset()+1);                                                                                                                                             
        }                                                                                                                                                                                                                      
        if (new KindPath(tp).equals(kindPath))                                                                                                                                                                                 
            return tp;                                                                                                                                                                                                         
        else                                                                                                                                                                                                                   
            return null;                                                                                                                                                                                                       
    }                                                                                                                                                                                                                          
                                                                                                                                                                                                                               
    /**                                                                                                                                                                                                                        
     * Resolves an {@link Element} from the {@link TreePathHandle}.                                                                                                                                                            
     * @param compilationInfo representing the {@link javax.tools.CompilationTask}                                                                                                                                             
     * @return resolved subclass of {@link Element} or null if the elment does not exist on                                                                                                                                    
     * the classpath/sourcepath of {@link javax.tools.CompilationTask}.                                                                                                                                                        
     */                                                                                                                                                                                                                        
    public Element resolveElement(final CompilationInfo info) {                                                                                                                                                                
        TreePath tp = null;                                                                                                                                                                                                    
        IllegalStateException ise = null;
        try {
            if (info.getFileObject().equals(this.file)) {
                tp = this.resolve(info);
            }
        } catch (IllegalStateException i) {
            ise=i;
        }
        if (tp==null) {                                                                                                                                                                                                        
            if (enclElIsCorrespondingEl) {                                                                                                                                                                                     
                return enclosingElement.resolve(info);                                                                                                                                                                         
            } else {
                if (ise==null)
                    return null;
                throw ise;                                                                                                                                                                                                   
            }                                                                                                                                                                                                                  
        }                                                                                                                                                                                                                      
        return info.getTrees().getElement(tp);                                                                                                                                                                                 
    }                                                                                                                                                                                                                          
                                                                                                                                                                                                                               
    /**                                                                                                                                                                                                                        
     * Returns the {@link Tree.Kind} of this element handle,                                                                                                                                                                   
     * it enclElIsCorrespondingEl the kind of the {@link Tree} from which the handle                                                                                                                                           
     * was created.                                                                                                                                                                                                            
     *                                                                                                                                                                                                                         
     * @return {@link Tree.Kind}                                                                                                                                                                                               
     */                                                                                                                                                                                                                        
    public Tree.Kind getKind() {                                                                                                                                                                                               
        return kindPath.kindPath.get(0);                                                                                                                                                             
    }                                                                                                                                                                                                                          
                                                                                                                                                                                                                               
    private TreePathHandle(PositionRef position, KindPath kindPath, FileObject file, ElementHandle element, boolean enclElIsCorrespondingEl) {                                                                                 
        this.kindPath = kindPath;                                                                                                                                                                                              
        this.position = position;                                                                                                                                                                                              
        this.file = file;                                                                                                                                                                                                      
        this.enclosingElement = element;                                                                                                                                                                                       
        this.enclElIsCorrespondingEl = enclElIsCorrespondingEl;                                                                                                                                                                
    }                                                                                                                                                                                                                          
                                                                                                                                                                                                                               
    /**                                                                                                                                                                                                                        
     * Factory method for creating {@link TreePathHandle}.                                                                                                                                                                     
     *                                                                                                                                                                                                                         
     * @param treePath for which the {@link TrePathHandle} should be created.                                                                                                                                                  
     * @return a new {@link TreePathHandle}                                                                                                                                                                                    
     * @throws {@link IllegalArgumentException} if the element enclElIsCorrespondingEl of not supported                                                                                                                        
     * {@link Tree.Kind}.                                                                                                                                                                                                      
     */                                                                                                                                                                                                                        
    public static TreePathHandle create (final TreePath treePath, CompilationInfo info) throws IllegalArgumentException {                                                                                                      
        int position = ((JCTree) treePath.getLeaf()).pos;                                                                                                                                                                      
        CloneableEditorSupport s = findCloneableEditorSupport(info.getFileObject());                                                                                                                                           
        PositionRef pos = s.createPositionRef(position, Bias.Forward);                                                                                                                                                         
        TreePath current = treePath;                                                                                                                                                                                           
        Element element;                                                                                                                                                                                                       
        boolean enclElIsCorrespondingEl = true;                                                                                                                                                                                
        do {                                                                                                                                                                                                                   
            element = info.getTrees().getElement(current);                                                                                                                                                                     
            current = current.getParentPath();                                                                                                                                                                                 
            if (element!=null && !isSupported(element)) {                                                                                                                                                                      
                enclElIsCorrespondingEl=false;                                                                                                                                                                                 
            }                                                                                                                                                                                                                  
        } while ((element == null || !isSupported(element)) && current !=null);                                                                                                                                                
        return new TreePathHandle(pos, new KindPath(treePath), info.getFileObject(),ElementHandle.create(element), enclElIsCorrespondingEl);                                                                                   
    }                                                                                                                                                                                                                          
                                                                                                                                                                                                                               
    private static boolean isSupported(Element el) {                                                                                                                                                                           
        switch (el.getKind()) {                                                                                                                                                                                                
            case PACKAGE:                                                                                                                                                                                                      
            case CLASS:                                                                                                                                                                                                        
            case INTERFACE:                                                                                                                                                                                                    
            case ENUM:                                                                                                                                                                                                         
            case METHOD:                                                                                                                                                                                                       
            case CONSTRUCTOR:                                                                                                                                                                                                  
            case INSTANCE_INIT:                                                                                                                                                                                                
            case STATIC_INIT:                                                                                                                                                                                                  
            case FIELD:
            case ANNOTATION_TYPE:    
            case ENUM_CONSTANT: return true;                                                                                                                                                                                   
            default: return false;                                                                                                                                                                                             
        }                                                                                                                                                                                                                      
    }                                                                                                                                                                                                                          
                                                                                                                                                                                                                               
    private static CloneableEditorSupport findCloneableEditorSupport(FileObject file) {                                                                                                                                        
        try {                                                                                                                                                                                                                  
            DataObject dob = DataObject.find(file);                                                                                                                                                                            
            Object obj = dob.getCookie(org.openide.cookies.OpenCookie.class);                                                                                                                                                  
            if (obj instanceof CloneableEditorSupport) {                                                                                                                                                                       
                return (CloneableEditorSupport)obj;                                                                                                                                                                            
            }                                                                                                                                                                                                                  
            obj = dob.getCookie(org.openide.cookies.EditorCookie.class);                                                                                                                                                       
            if (obj instanceof CloneableEditorSupport) {                                                                                                                                                                       
                return (CloneableEditorSupport)obj;                                                                                                                                                                            
            }                                                                                                                                                                                                                  
        } catch (DataObjectNotFoundException ex) {                                                                                                                                                                             
            ex.printStackTrace();                                                                                                                                                                                              
        }                                                                                                                                                                                                                      
        return null;                                                                                                                                                                                                           
    }                                                                                                                                                                                                                          
                                                                                                                                                                                                                               
    private static class KindPath {                                                                                                                                                                                            
        private ArrayList<Tree.Kind> kindPath = new ArrayList();                                                                                                                                                               
                                                                                                                                                                                                                               
        private KindPath(TreePath treePath) {                                                                                                                                                                                  
            while (treePath!=null) {                                                                                                                                                                                           
                kindPath.add(treePath.getLeaf().getKind());                                                                                                                                                                    
                treePath = treePath.getParentPath();                                                                                                                                                                           
            }                                                                                                                                                                                                                  
        }                                                                                                                                                                                                                      
                                                                                                                                                                                                                               
        public int hashCode() {                                                                                                                                                                                                
            return kindPath.hashCode();                                                                                                                                                                                        
        }                                                                                                                                                                                                                      
                                                                                                                                                                                                                               
        public boolean equals(Object object) {                                                                                                                                                                                 
            if (object instanceof KindPath)                                                                                                                                                                                    
                return kindPath.equals(((KindPath)object).kindPath);                                                                                                                                                           
            return false;                                                                                                                                                                                                      
        }                                                                                                                                                                                                                      
    }                                                                                                                                                                                                                          
}                                                                                                                                                                                                                              
