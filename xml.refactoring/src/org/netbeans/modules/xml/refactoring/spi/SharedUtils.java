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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.refactoring.spi;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.api.SafeDeleteRefactoring;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.xml.nbprefuse.AnalysisConstants;
import org.netbeans.modules.xml.refactoring.CannotRefactorException;
import org.netbeans.modules.xml.refactoring.ErrorItem;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
import org.netbeans.modules.xml.refactoring.spi.RefactoringUtil;
import org.netbeans.modules.xml.refactoring.ui.DeleteRefactoringUI;
import org.netbeans.modules.xml.refactoring.ui.FileRenameRefactoringUI;
import org.netbeans.modules.xml.refactoring.ui.ModelProvider;
import org.netbeans.modules.xml.refactoring.ui.ReferenceableProvider;
import org.netbeans.modules.xml.refactoring.ui.RenameRefactoringUI;
import org.netbeans.modules.xml.retriever.catalog.ProjectCatalogSupport;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.EmbeddableRoot;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.windows.TopComponent;

/**
 * Shared utilities for service implementation code.
 *
 * @author Nam Nguyen
 */
public class SharedUtils {
    
    public static final String WSDL_MIME_TYPE = "text/xml-wsdl";  // NOI18N
    
      
    
    public static void renameTarget(Nameable target, String newName) throws IOException {
        if ( target.getModel() == null) return;
        Model model =target.getModel();
        
        boolean startTransaction = ! model.isIntransaction();
        try {
            if (startTransaction) {
                model.startTransaction();
            }
            ((Nameable)target).setName(newName);
        } finally {
            if (startTransaction && model.isIntransaction()) {
                model.endTransaction();
            }
        }
        
       // request.setRenamedTarget(request.getNameableTarget());
    }
    
        
     public static void deleteTarget(NamedReferenceable target) throws IOException {
        if (target == null || getModel(target) == null) return;
        //System.out.println("DeleteTarget called");
        Model model = getModel(target);
        boolean startTransaction = ! model.isIntransaction();
        try {
            if (startTransaction) {
                model.startTransaction();
            }
            model.removeChildComponent(target);
        } finally {
            if (startTransaction && model.isIntransaction()) {
                model.endTransaction();
            }
        }
        //request.setDone(true);
    }

    
    
    public static List<ErrorItem> addCascadeDeleteErrors(List<Model> models, Class<? extends Model> referencingModelType) {
        List<ErrorItem> errors = new ArrayList<ErrorItem>();
        for (Model model:models) {
            if (! (referencingModelType.isAssignableFrom(model.getClass()))) {
                continue;
            }
            String msg = NbBundle.getMessage(RefactoringUtil.class, "MSG_CascadeDeleteNotSupported");
            errors.add(new ErrorItem(model, msg));
        }
        return errors;
    }
    
    
    public static boolean isWritable(FileObject fo) {
        boolean canLock = false;
        FileLock lock = null;
        try {
            lock = fo.lock();
            canLock = true;
        } catch(IOException ioe) {
            if (lock != null) lock.releaseLock();
        }
        return fo != null && fo.canWrite() && canLock;
    }

    public static String getURI(FileObject fo) {
        return FileUtil.toFile(fo).toURI().toString();
    }
    
   
    public static ProjectCatalogSupport getCatalogSupport(FileObject file) {
        Project p = FileOwnerQuery.getOwner(file);
        if (p != null) {
            return (ProjectCatalogSupport) p.getLookup().lookup(ProjectCatalogSupport.class);
        }
        return null;
    }
    
    

    public static FileObject renameFile(FileObject fo, String newName) throws IOException {
        String extension = fo.getExt();
        FileLock lock = null;
        try {
            lock = fo.lock();
            fo.rename(lock, newName, extension);
            return fo;
        } finally {
            if (lock != null) lock.releaseLock();
        }
    }
    
    /*public static void refreshCatalogModel(FileRenameRequest request, FileObject referencedFO) {
        for (UsageGroup ug : request.getUsages().getUsages()) {
            FileObject referencingFO = ug.getFileObject();
            ProjectCatalogSupport pcs = getCatalogSupport(referencingFO);
            if (pcs == null) continue;
            for (Component uc : ug.getRefactorComponents()) {
                String reference = ug.getEngine().getModelReference(uc);
                if (reference == null) continue;
                try {
                    if (pcs != null && pcs.removeCatalogEntry(new URI(reference))) {
                        pcs.createCatalogEntry(referencingFO, referencedFO);
                    }
                } catch(Exception ex) {
                    Logger.getLogger(SharedUtils.class.getName()).log(Level.FINE, ex.getMessage());
                }
            }
        }
    }*/
       
    protected static List<SourceGroup> getSourceGroups(Referenceable ref) {
            List<SourceGroup> sourceGroups = new ArrayList<SourceGroup>();
            Project project = RefactoringUtil.findCurrentProject(ref);
            if (project != null) {
                sourceGroups.addAll(RefactoringUtil.findSourceRoots(project));
                Set<Project> referencings = RefactoringUtil.getReferencingProjects(project);
                for (Project p : referencings) {
                    sourceGroups.addAll(RefactoringUtil.findSourceRoots(p));
                }
            }
       
        return sourceGroups;
    }
    
    public static Set<FileObject> getSearchFiles(Referenceable ref) {
        HashSet<FileObject> files = new HashSet<FileObject>();
        for (SourceGroup sourceGroup : getSourceGroups(ref)) {
            files.addAll(RefactoringUtil.findSourceFiles(sourceGroup.getRootFolder()));
        }
        // make sure target source is also included in search in case outside projects
        Model model = null;
        if (ref instanceof Model) {
           model=  (Model) ref;
        } else if (ref instanceof Component) {
            model = ((Component)ref).getModel();
        } 
        
        DataObject dobj = RefactoringUtil.getDataObject(model);
        if (dobj != null) {
            files.add(dobj.getPrimaryFile());
        }
        return files;
    }
    
        
    public static Model getModel(Referenceable ref) {
        if (ref instanceof Model) {
            return (Model) ref;
        } else if (ref instanceof Component) {
            return ((Component)ref).getModel();
        } else {
            return null;
        }
    }
    
     public static String calculateNewLocationString(String currentLocationString, RenameRefactoring request) {
        StringBuilder sb = new StringBuilder();
        int i = currentLocationString.lastIndexOf('/');
        if (i > -1) {
            sb.append(currentLocationString.substring(0, i+1));
        }
        sb.append(request.getNewName());
        sb.append("."); //NOI18N
        Referenceable ref = request.getRefactoringSource().lookup(Referenceable.class);
        Model model = SharedUtils.getModel(ref);
        FileObject fo = model.getModelSource().getLookup().lookup(FileObject.class);
        sb.append(fo.getExt());
        return sb.toString();
    }
     
     public static Map<Model, Set<RefactoringElementImplementation>> getModelMap(List<RefactoringElementImplementation> elements){
        Map<Model, Set<RefactoringElementImplementation>> results = new HashMap<Model, Set<RefactoringElementImplementation>>();
        for(RefactoringElementImplementation element:elements){
           Model model = ((Component)element.getComposite()).getModel();
           Set<RefactoringElementImplementation> elementsInModel = results.get(model);
           if(elementsInModel == null){
               elementsInModel = new HashSet<RefactoringElementImplementation>();
               elementsInModel.add(element);
               results.put(model, elementsInModel);
           } else
               elementsInModel.add(element);
        }
        return results;
    }
     
     public static void silentDeleteRefactor(NamedReferenceable target, boolean failsOnUsage) throws CannotRefactorException, IOException {
         SafeDeleteRefactoring refactoring = new SafeDeleteRefactoring(Lookups.singleton(target));
         XMLRefactoringTransaction transaction = new XMLRefactoringTransaction((Referenceable)target, refactoring);
         transaction.setLocalScope();
         refactoring.getContext().add(transaction);
         refactor(refactoring, failsOnUsage);
     }
     
     public static void silentRename(Nameable target, String newName, boolean failsOnUsage) throws CannotRefactorException, IOException {
         RenameRefactoring refactoring = new RenameRefactoring(Lookups.singleton(target));
         refactoring.setNewName(newName);
         XMLRefactoringTransaction transaction = new XMLRefactoringTransaction((Referenceable)target, refactoring);
         transaction.setLocalScope();
         refactoring.getContext().add(transaction);
         refactoring.getContext().add(target.getName());
         refactor(refactoring, failsOnUsage);
     }
     
      public static void silentFileRefactor(Model model, String name, boolean b) throws CannotRefactorException, IOException{
         RenameRefactoring refactoring = new RenameRefactoring(Lookups.singleton(model));
         refactoring.setNewName(name);
         XMLRefactoringTransaction transaction = new XMLRefactoringTransaction((Referenceable)model, refactoring);
         refactoring.getContext().add(transaction);
         refactor(refactoring, b);
    }
     
     
     public static void refactor(AbstractRefactoring refactoring, boolean failsOnUsages) throws CannotRefactorException, IOException {
        RefactoringSession session = RefactoringSession.create("Silent Refactor");
        Problem problem = refactoring.checkParameters();
        while(problem != null) {
            if(problem.isFatal())
                throw new CannotRefactorException(problem.getMessage());
            problem = problem.getNext();
        }
        
        problem = refactoring.prepare(session);
        if(failsOnUsages && session.getRefactoringElements().size() > 0 ) {
            throw new CannotRefactorException(NbBundle.getMessage(RefactoringUtil.class, "MSG_HasUsages"));
        }
        while(problem != null) {
            if( problem.isFatal() )
                throw new CannotRefactorException(problem.getMessage());
            problem = problem.getNext();
        }
        session.doRefactoring(true);
    }
     
      public static void showRefactoringUI(AbstractRefactoring request) {
        Referenceable target = request.getRefactoringSource().lookup(Referenceable.class);
        org.netbeans.modules.refactoring.spi.ui.RefactoringUI ui  = null;
        if(target instanceof Model && request instanceof RenameRefactoring)
            ui = new FileRenameRefactoringUI((Model)target);
        else if(target instanceof Nameable && request instanceof RenameRefactoring)
            ui = new RenameRefactoringUI((Nameable)target);
        
        TopComponent activetc = TopComponent.getRegistry().getActivated();
        if (activetc instanceof CloneableEditorSupport.Pane) {
            //new RefactoringPanel(ui, activetc);
            UI.openRefactoringUI(ui, activetc);
        } else {
           // new RefactoringPanel(ui);
            UI.openRefactoringUI(ui);
        }
    }
      
      public static void showDeleteRefactoringUI(NamedReferenceable target) {
          org.netbeans.modules.refactoring.spi.ui.RefactoringUI ui  = new DeleteRefactoringUI(target);
          TopComponent activetc = TopComponent.getRegistry().getActivated();
          if (activetc instanceof CloneableEditorSupport.Pane) {
              //new RefactoringPanel(ui, activetc);
              UI.openRefactoringUI(ui, activetc);
          } else {
              // new RefactoringPanel(ui);
              UI.openRefactoringUI(ui);
          }
    }
      
      public static void showRenameRefactoringUI(Nameable target) {
          org.netbeans.modules.refactoring.spi.ui.RefactoringUI ui  = new RenameRefactoringUI(target);
          TopComponent activetc = TopComponent.getRegistry().getActivated();
          if (activetc instanceof CloneableEditorSupport.Pane) {
              //new RefactoringPanel(ui, activetc);
              UI.openRefactoringUI(ui, activetc);
          } else {
              // new RefactoringPanel(ui);
              UI.openRefactoringUI(ui);
          }
    }
      
      public static void showFileRenameRefactoringUI(Model target) {
          org.netbeans.modules.refactoring.spi.ui.RefactoringUI ui  = new FileRenameRefactoringUI(target);
          TopComponent activetc = TopComponent.getRegistry().getActivated();
          if (activetc instanceof CloneableEditorSupport.Pane) {
              //new RefactoringPanel(ui, activetc);
              UI.openRefactoringUI(ui, activetc);
          } else {
              // new RefactoringPanel(ui);
              UI.openRefactoringUI(ui);
          }
    }
      
       /**
     * @param fobj a FileObject
     * @returns Image  java.awt.Image for the FileObject
     *
     */
    public static Image getImage(FileObject fobj){
        try {
            return DataObject.find(fobj).getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return null;
    }
    
    
     public static FileObject getFileObject(final Component xamComp){
        return (FileObject)xamComp.getModel().getModelSource().getLookup().lookup(FileObject.class);
    }
     
        public static String getXmlFileType(FileObject fobj){
        if (fobj.getExt().equals(AnalysisConstants.SCHEMA_FILE_EXTENSION)){
            return AnalysisConstants.SCHEMA_FILE_TYPE;
        }
        if ( WSDL_MIME_TYPE.equals(FileUtil.getMIMEType(fobj))) {
            return AnalysisConstants.WSDL_FILE_TYPE;
        }
        if (fobj.getExt().equals(AnalysisConstants.BPEL_FILE_EXTENSION)){
            return AnalysisConstants.BPEL_FILE_TYPE;
        }
        return "";  //NOI18N
    }
    
    public static String getXmlFileTypeDisplayName(String fileType){
        if (fileType.equals(AnalysisConstants.SCHEMA_FILE_TYPE)){
            return NbBundle.getMessage(SharedUtils.class, "LBL_Schema");
        }
        
        if (fileType.equals(AnalysisConstants.WSDL_FILE_TYPE)){
            return NbBundle.getMessage(SharedUtils.class, "LBL_WSDL");
        }
        
        if (fileType.equals(AnalysisConstants.BPEL_FILE_TYPE)){
            return NbBundle.getMessage(SharedUtils.class, "LBL_BPEL");
        }
        return "";  //NOI18N
        
    }
    
     
    /**
     * Check for the ReferenceableProvider Node.Cookie in node 0
     * 
     * @returns NamedReferenceable instance of null if the node does
     *          not have the provider cookie
     */
    public static Referenceable getReferenceable(final org.openide.nodes.Node[] nodes) {
        Referenceable referenceable = null;
        ReferenceableProvider provider =
                (ReferenceableProvider)nodes[0].getCookie(ReferenceableProvider.class);
        if (provider != null){
            referenceable = provider.getReferenceable();
        } else {
            ModelProvider modelProvider = (ModelProvider)nodes[0].getCookie(ModelProvider.class);
            if (modelProvider != null) {
                referenceable = modelProvider.getModel();
            }
        }
        return referenceable;
    }
    
     /**
     * Quiet and local rename refactoring.
     */
    public static void locallyRenameRefactor(Nameable target, String newName) {
        RenameRefactoring refactoring = new RenameRefactoring(Lookups.singleton(target));
        refactoring.setNewName(newName);
        XMLRefactoringTransaction transaction = new XMLRefactoringTransaction((Referenceable)target, refactoring);
        transaction.setLocalScope();
        refactoring.getContext().add(transaction);
        refactoring.getContext().add(target.getName());
        doQuietRefactor(refactoring);
    }
    
    /**
     * Quietly execute the given refactoring request.
     * 
     */
    public static void doQuietRefactor(AbstractRefactoring request) {
        try {
            //RefactoringManager.getInstance().execute(request, false);
            refactor(request, false);
        } catch(IOException ex) {
            String msg = ex.getMessage();
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        } catch (CannotRefactorException cre) {
            Referenceable target = request.getRefactoringSource().lookup(Referenceable.class);
            showRenameRefactoringUI((Nameable)target);
        }
    }
    
    public static String getName(Referenceable target) {
        if (target instanceof Model) {
            FileObject fo = (FileObject) ((Model)target).getModelSource().getLookup().lookup(FileObject.class);
            assert fo != null : "Target model does not contain FileObject in lookkup";
            return fo.getName();
        } else {
            return ((Named)target).getName();
        }
    }
    
    public static Set<Component> getLocalSearchRoots(Referenceable target) {
        Set<Component> scope =null;
        if (target instanceof DocumentModel) {
            scope = new HashSet<Component>();
            scope.add(((DocumentModel)target).getRootComponent());
        } else if (target instanceof Component) {
            scope = Collections.singleton(getRootOf((Component)target));
        }
        return scope;
    }
    
    public static Component getRootOf(Component component) {
        Component root = (Component) component;
        while (root != null) {
            Component parent = getEffectiveParent(root);
            if (parent == null) {
                break;
            }
            root = parent;
        }
        return root;
    }
    
    
    public static Component getEffectiveParent(Component component) {
        if (component instanceof  EmbeddableRoot) {
            return ((EmbeddableRoot) component).getForeignParent();
        } else {
            return component.getParent();
        }
    }
    
    public static Collection<RefactoringElement> findUsages(Referenceable target, Component searchRoot) {
        WhereUsedQuery query = new WhereUsedQuery(Lookups.singleton(target));
        query.getContext().add(searchRoot);
        RefactoringSession session = RefactoringSession.create("Inner Query");
        query.prepare(session);
        return session.getRefactoringElements();
    }
    
    public Node getDisplayNode(Component component) {
        AbstractNode n = new AbstractNode(Children.LEAF);
        String name = component instanceof Named ?
            ((Named) component).getName() : component.getClass().getName();
        n.setName(name);
        return n;
    }

    public Node getDisplayNode(Model model) {
        AbstractNode n = new AbstractNode(Children.LEAF);
        FileObject fo = (FileObject) model.getModelSource().getLookup().lookup(FileObject.class);
        assert fo != null : "Model source does not provide FileObject lookup";
        n.setName(fo.getName());
        return n;
    }

   }
