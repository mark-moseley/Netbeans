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
package org.netbeans.modules.web.beans.navigation.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.beans.api.model.AmbiguousDependencyException;
import org.netbeans.modules.web.beans.api.model.ModelUnit;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.api.model.WebBeansModelException;
import org.netbeans.modules.web.beans.api.model.WebBeansModelFactory;
import org.netbeans.modules.web.beans.navigation.AmbiguousInjectablesModel;
import org.netbeans.modules.web.beans.navigation.AmbiguousInjectablesPanel;
import org.netbeans.modules.web.beans.navigation.ResizablePopup;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

import com.sun.source.util.TreePath;


/**
 * @author ads
 *
 */
public final class InspectInjectablesAtCaretAction extends BaseAction {

    private static final long serialVersionUID = 1857528107859448216L;
    
    private static final String INSPECT_INJACTABLES_AT_CARET =
        "LBL_InspectInjactablesAtCaret";                     // NOI18N
    
    private static final String INSPECT_INJACTABLES_AT_CARET_POPUP =
        "LBL_PopupInspectInjactablesAtCaret";                // NOI18N

    public InspectInjectablesAtCaretAction() {
        super(NbBundle.getMessage(InspectInjectablesAtCaretAction.class, 
                INSPECT_INJACTABLES_AT_CARET), 0);
        
        putValue(SHORT_DESCRIPTION, getValue(NAME));
        putValue(ExtKit.TRIMMED_TEXT,getValue(NAME));
        putValue(POPUP_MENU_TEXT, NbBundle.getMessage(
                InspectInjectablesAtCaretAction.class,
                INSPECT_INJACTABLES_AT_CARET_POPUP));

        putValue("noIconInMenu", Boolean.TRUE); // NOI18N*/
    }


    /* (non-Javadoc)
     * @see org.netbeans.editor.BaseAction#actionPerformed(java.awt.event.ActionEvent, javax.swing.text.JTextComponent)
     */
    @Override
    public void actionPerformed( ActionEvent event, final JTextComponent component ) {
        if ( component == null ){
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        final FileObject fileObject = NbEditorUtilities.getFileObject( 
                component.getDocument());
        if ( fileObject == null ){
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        Project project = FileOwnerQuery.getOwner( fileObject );
        if ( project == null ){
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        ClassPath boot = getClassPath( project , ClassPath.BOOT);
        ClassPath compile = getClassPath(project, ClassPath.COMPILE );
        ClassPath src = getClassPath(project , ClassPath.SOURCE);
        if ( boot == null || compile == null || src == null ){
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        ModelUnit modelUnit = ModelUnit.create( boot, compile , src);
        MetadataModel<WebBeansModel> metaModel = WebBeansModelFactory.
            getMetaModel( modelUnit );
        
        /*
         *  this list will contain variable element name and TypeElement 
         *  qualified name which contains variable element. 
         */
        final List<String> variableAtCaret = new ArrayList<String>(2);
        if ( !getVariableElementAtDot( component, variableAtCaret )){
            return;
        }
        
        try {
            metaModel.runReadAction( new MetadataModelAction<WebBeansModel, Void>() {

                public Void run( WebBeansModel model ) throws Exception {
                    inspectInjectables(component, fileObject, 
                            model, variableAtCaret );
                    return null;
                }
            });
        }
        catch (MetadataModelException e) {
            Logger.getLogger( InspectInjectablesAtCaretAction.class.getName()).
                log( Level.WARNING, e.getMessage(), e);
        }
        catch (IOException e) {
            Logger.getLogger( InspectInjectablesAtCaretAction.class.getName()).
                log( Level.WARNING, e.getMessage(), e);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.AbstractAction#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        if (EditorRegistry.lastFocusedComponent() == null
                || !EditorRegistry.lastFocusedComponent().isShowing())
        {
            return false;
        }
        return OpenProjects.getDefault().getOpenProjects().length > 0;
    }
    
    
    /* (non-Javadoc)
     * @see org.netbeans.editor.BaseAction#asynchonous()
     */
    @Override
    protected boolean asynchonous() {
        return true;
    }
    
    private ClassPath getClassPath( Project project, String type ) {
        ClassPathProvider provider = project.getLookup().lookup( 
                ClassPathProvider.class);
        if ( provider == null ){
            return null;
        }
        Sources sources = project.getLookup().lookup(Sources.class);
        if ( sources == null ){
            return null;
        }
        SourceGroup[] sourceGroups = sources.getSourceGroups( 
                JavaProjectConstants.SOURCES_TYPE_JAVA );
        ClassPath[] paths = new ClassPath[ sourceGroups.length];
        int i=0;
        for (SourceGroup sourceGroup : sourceGroups) {
            FileObject rootFolder = sourceGroup.getRootFolder();
            paths[ i ] = provider.findClassPath( rootFolder, type);
        }
        return ClassPathSupport.createProxyClassPath( paths );
    }

    /**
     * Variable element is resolved based on containing type element 
     * qualified name and simple name of variable itself.
     * Model methods are used further for injectable resolution.   
     */
    private void inspectInjectables( final JTextComponent component, 
            final FileObject fileObject, final WebBeansModel model, 
            final List<String> variablePath )
    {
        TypeElement typeElement = model.getCompilationController().getElements().
            getTypeElement( variablePath.get(0));
        if ( typeElement == null ){
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                    InspectInjectablesAtCaretAction.class, "LBL_EnclosedTypeNotFound",
                    variablePath.get(0)), StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
            return;
        }
        List<? extends Element> children = typeElement.getEnclosedElements();
        String name = variablePath.get( 1 );
        VariableElement var = null;
        for (Element element : children) {
            if ( element.getSimpleName().contentEquals(name ) && 
                    (element instanceof VariableElement))
            {
                var = (VariableElement)element;
            }
        }
        if ( var == null ){
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                    InspectInjectablesAtCaretAction.class, "LBL_VariableNotFound",
                    name), StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
            return;
        }
        try {
            if (model.isInjectionPoint(var)) {
                try {
                    Element injectable = model.getInjectable(var);
                    if ( injectable == null ){
                        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                                InspectInjectablesAtCaretAction.class, 
                                "LBL_InjectableNotFound"), 
                                StatusDisplayer.IMPORTANCE_ERROR_HIGHLIGHT);
                        return;
                    }
                    final ElementHandle<Element> handle = ElementHandle
                            .create(injectable);
                    final ClasspathInfo classpathInfo = model.getCompilationController().
                    getClasspathInfo();
                    SwingUtilities.invokeLater( new Runnable() {
                        
                        public void run() {
                            ElementOpen.open( classpathInfo, handle);
                        }
                    });
                }
                catch (final AmbiguousDependencyException adExcpeption) {
                    final List<AnnotationMirror> bindings = model.getBindings(var );
                    final VariableElement varElement = var;
                    final CompilationController controller = model.getCompilationController();
                    if ( SwingUtilities.isEventDispatchThread()){
                        showDialog( adExcpeption , varElement, 
                                controller, 
                                bindings);
                    }
                    else {
                        SwingUtilities.invokeLater( new Runnable() {
                            public void run() {
                                showDialog(adExcpeption, varElement, 
                                        controller,
                                        bindings);
                            }
                        });
                    }
                }
            }
        }
        catch (WebBeansModelException e) {
            /*
             * TODO : one need somehow notice user that injection point has
             * inconsistency
             */
        }
    }

    /**
     * Compilation controller from metamodel could not be used for getting 
     * TreePath via dot because it is not based on one FileObject ( Document ).
     * So this method is required for searching Element at dot.
     * If appropriate element is found it's name is placed into list 
     * along with name of containing type.
     * Resulted element could not be used in metamodel for injectable
     * access. I believe this is because element was gotten via other Compilation
     * controller so it is from other model.
     * As result this trick is used.  
     */
    private boolean getVariableElementAtDot( final JTextComponent component,
            final List<String> variableAtCaret ) 
    {
        JavaSource javaSource = JavaSource.forDocument( component.getDocument());
        if ( javaSource == null ){
            Toolkit.getDefaultToolkit().beep();
            return false;
        }
        try {
            javaSource.runUserActionTask(  new Task<CompilationController>(){
                public void run(CompilationController controller) throws Exception {
                    controller.toPhase( Phase.ELEMENTS_RESOLVED );
                    int dot = component.getCaret().getDot();
                    TreePath tp = controller.getTreeUtilities()
                        .pathFor(dot);
                    Element element = controller.getTrees().getElement(tp );
                    if ( !( element instanceof VariableElement) ){
                        return;
                    }
                    else {
                        TypeElement enclosingTypeElement = controller.
                            getElementUtilities().enclosingTypeElement(element);
                        variableAtCaret.add( enclosingTypeElement.
                                getQualifiedName().toString());
                        variableAtCaret.add( element.getSimpleName().toString());
                    }
                }
            }, true );
        }
        catch(IOException e ){
            Logger.getLogger( InspectInjectablesAtCaretAction.class.getName()).
                log( Level.WARNING, e.getMessage(), e);
        }
        return variableAtCaret.size() ==2 ;
    }
    
    private void showDialog( AmbiguousDependencyException adExcpeption , 
            VariableElement var , CompilationController controller,
            List<AnnotationMirror> bindings ) 
    {
        Collection<Element> elements = adExcpeption.getElements();
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(
                AmbiguousInjectablesModel.class, "LBL_WaitNode"));
        JDialog dialog = ResizablePopup.getDialog();
        String title = NbBundle.getMessage(InspectInjectablesAtCaretAction.class,
                "TITLE_Injectables" , var.getSimpleName().toString() );
        dialog.setTitle( title );
        dialog.setContentPane( new AmbiguousInjectablesPanel(elements, var, 
                bindings, controller));
        dialog.setVisible( true );
    }

}
