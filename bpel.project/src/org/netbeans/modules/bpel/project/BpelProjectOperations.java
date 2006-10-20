package org.netbeans.modules.bpel.project;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.bpel.project.ui.customizer.IcanproProjectProperties;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.MoveOperationImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
public class BpelProjectOperations implements DeleteOperationImplementation, CopyOperationImplementation, MoveOperationImplementation {
    private BpelproProject project;
    
    /** Creates a new instance of JbiProjectOperations */
    public BpelProjectOperations(BpelproProject project) {
        this.project = project;
    }
    
    public List/*<FileObject>*/ getDataFiles() {
        List/*<FileObject>*/ files = new ArrayList();
        

        files.add(project.getSourceDirectory());
        
        PropertyEvaluator evaluator = project.evaluator();
        String prop = evaluator.getProperty(IcanproProjectProperties.SOURCE_ROOT);
        if (prop != null) {
            FileObject projectDirectory = project.getProjectDirectory();
            FileObject srcDir = project.getAntProjectHelper().resolveFileObject(prop);
            if (projectDirectory != srcDir && !files.contains(srcDir))
                files.add(srcDir);
        }
        
     
        return files;
    }    
    
    private static void addFile(FileObject projectDirectory, String fileName, List/*<FileObject>*/ result) {
        FileObject file = projectDirectory.getFileObject(fileName);
        
        if (file != null) {
            result.add(file);
        }
    }
    
    public List/*<FileObject>*/ getMetadataFiles() {
        FileObject projectDirectory = project.getProjectDirectory();
        List/*<FileObject>*/ files = new ArrayList();
        
        addFile(projectDirectory, "nbproject", files); // NOI18N
        addFile(projectDirectory, "build.xml", files); // NOI18N
        addFile(projectDirectory, "catalog.xml", files); //NOI18N
        addFile(projectDirectory, projectDirectory.getName(), files); //NOI18N
        addFile(projectDirectory, BpelProjectXmlCatalogProvider.TYPE_RETRIEVED , files); //NOI18N
        
        return files;
    }    
    
    
    public void notifyDeleting() throws IOException {
       BpelproActionProvider ap = (BpelproActionProvider) project.getLookup().lookup(BpelproActionProvider.class);
        assert ap != null;
        
        Lookup context = Lookups.fixed(new Object[0]);
        Properties p = new Properties();
        String[] targetNames = ap.getTargetNames(ActionProvider.COMMAND_CLEAN, context, p);
        FileObject buildXML = project.getProjectDirectory().getFileObject(GeneratedFilesHelper.BUILD_XML_PATH);
        
        assert targetNames != null;
        assert targetNames.length > 0;
        
        ActionUtils.runTarget(buildXML, targetNames, p).waitFinished();
    }
    
    
    public void notifyDeleted() throws IOException  {

        project.getAntProjectHelper().notifyDeleted();
    }
       
    public void notifyCopied(Project original, File originalPath, final String newName) {
        if (original == null) {
            // do nothing for the original project.
            return ;
        }
        
        project.getReferenceHelper().fixReferences(originalPath);
        
        String oldName = project.getName();
        project.setName(newName);
    }
    
    public void notifyCopying() {
        // do nothing.
        // This does copy the old distribution file over though, which is
        // probably OK because "ant clean" will clean it up.
    }
    
    public void notifyMoved(Project original, File originalPath, final String newName) {
        if (original == null) {
            project.getAntProjectHelper().notifyDeleted();
            return ;
        }
        String oldName = project.getName();
        project.setName(newName);
        project.getReferenceHelper().fixReferences(originalPath);
    }
    
     public void notifyMoving() throws IOException {
        notifyDeleting();
    }
}
