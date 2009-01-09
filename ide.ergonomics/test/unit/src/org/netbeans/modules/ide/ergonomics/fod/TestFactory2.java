package org.netbeans.modules.ide.ergonomics.fod;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=ProjectFactory.class, position=29999)
public final class TestFactory2 extends ProjectOpenedHook
implements ProjectFactory, Project, ProjectInformation {

    static Set<FileObject> recognize = new HashSet<FileObject>();
    int closed;
    int opened;
    int listenerCount;
    final FileObject dir;

    public TestFactory2() {
        dir = null;
    }

    private TestFactory2(FileObject dir) {
        this.dir = dir;
    }

    public boolean isProject(FileObject projectDirectory) {
        return recognize.contains(projectDirectory);
    }

    public Project loadProject(FileObject pd, ProjectState state) throws IOException {
        return isProject(pd) ? new TestFactory2(pd) : null;
    }

    public void saveProject(Project project) throws IOException, ClassCastException {
    }

    public FileObject getProjectDirectory() {
        return dir;
    }

    public Lookup getLookup() {
        return Lookups.singleton(this);
    }

    public String getName() {
        return "x";
    }

    public String getDisplayName() {
        return "y";
    }

    public Icon getIcon() {
        return null;
    }

    public Project getProject() {
        return this;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listenerCount++;
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listenerCount--;
    }

    @Override
    protected void projectOpened() {
        opened++;
    }

    @Override
    protected void projectClosed() {
        closed++;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TestFactory2) {
            return super.equals(obj);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }


}
