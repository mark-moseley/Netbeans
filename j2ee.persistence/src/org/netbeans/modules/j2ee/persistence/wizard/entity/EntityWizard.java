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

package org.netbeans.modules.j2ee.persistence.wizard.entity;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.persistence.util.GenerationUtils;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
import org.netbeans.modules.j2ee.persistence.util.EntityMethodGenerator;
import org.netbeans.modules.j2ee.persistence.util.JPAClassPathHelper;
import org.netbeans.modules.j2ee.persistence.wizard.DelegatingWizardDescriptorPanel;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * A wizard for creating entity classes.
 *
 * @author Martin Adamek
 * @author Erno Mononen
 */

public final class EntityWizard implements WizardDescriptor.InstantiatingIterator {
    private WizardDescriptor.Panel[] panels;
    private int index = 0;
    private EntityWizardDescriptor ejbPanel;
    private WizardDescriptor wiz;
    private SourceGroup[] sourceGroups;
    
    public static EntityWizard create() {
        return new EntityWizard();
    }
    
    public String name() {
        return NbBundle.getMessage(EntityWizard.class, "LBL_EntityEJBWizardTitle");
    }
    
    public void uninitialize(WizardDescriptor wiz) {
    }
    
    public void initialize(WizardDescriptor wizardDescriptor) {
        wiz = wizardDescriptor;
        Project project = Templates.getProject(wiz);
        sourceGroups = Util.getJavaSourceGroups(project);
        ejbPanel = new EntityWizardDescriptor();
        WizardDescriptor.Panel p = new ValidatingPanel(JavaTemplates.createPackageChooser(project,sourceGroups, ejbPanel, true));
        panels = new WizardDescriptor.Panel[] {p};
        Util.mergeSteps(wiz, panels, null);
        
    }
    
    public Set instantiate() throws IOException {
        
        FileObject result = generateEntity(
                Templates.getTargetFolder(wiz),
                Templates.getTargetName(wiz),
                ejbPanel.getPrimaryKeyClassName(),
                false // setting PROPERTY access type by default
                );
        
        try{
            PersistenceUnit punit = ejbPanel.getPersistenceUnit();
            if (punit != null){
                ProviderUtil.addPersistenceUnit(punit, Templates.getProject(wiz));
            }
            addEntityToPersistenceUnit(result);
        } catch (InvalidPersistenceXmlException ipx){
            // just log for debugging purposes, at this point the user has
            // already been warned about an invalid persistence.xml
            Logger.getLogger(EntityWizard.class.getName()).log(Level.FINE, "Invalid persistence.xml: " + ipx.getPath(), ipx); //NO18N
        }
        
        return Collections.singleton(result);
    }
    
    /**
     * Adds the given entity to the persistence unit defined in the project in which this wizard
     * was invoked.
     * @param entity the entity to be added.
     */
    private void addEntityToPersistenceUnit(FileObject entity) throws InvalidPersistenceXmlException{
        
        Project project = Templates.getProject(wiz);
        String entityFQN = "";
        ClassPathProvider classPathProvider = project.getLookup().lookup(ClassPathProvider.class);
        if (classPathProvider != null) {
            entityFQN = classPathProvider.findClassPath(entity, ClassPath.SOURCE).getResourceName(entity, '.', false);
        }
        
        if (project != null && !Util.isSupportedJavaEEVersion(project) && ProviderUtil.getDDFile(project) != null) {
            PUDataObject pudo = ProviderUtil.getPUDataObject(project);
            PersistenceUnit pu[] = pudo.getPersistence().getPersistenceUnit();
            //only add if a PU exists, if there are more we do not know where to add - UI needed to ask
            if (pu.length == 1) {
                pudo.addClass(pu[0], entityFQN);
            }
        }
    }
    
    
    public void addChangeListener(javax.swing.event.ChangeListener l) {
    }
    
    public void removeChangeListener(javax.swing.event.ChangeListener l) {
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    
    public void nextPanel() {
        if (! hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    public void previousPanel() {
        if (! hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    /**
     * Generates an entity class.
     *
     * @param targetFolder the target folder for the entity.
     * @param targetName the target name of the entity.
     * @param primaryKeyClassName the name of the primary key class, needs to be
     *  resolvable in the generated entity's scope.
     * @param isAccessProperty defines the access strategy for the id field.
     * @return a FileObject representing the generated entity.
     */
    public static FileObject generateEntity(final FileObject targetFolder, final String targetName,
            final String primaryKeyClassName, final boolean isAccessProperty) throws IOException {
        
        FileObject entityFo = GenerationUtils.createClass(targetFolder, targetName, null);
        ClassPath compile = ClassPath.getClassPath(targetFolder, ClassPath.COMPILE);
        Set<ClassPath> compileClassPaths = new HashSet<ClassPath>();
        compileClassPaths.add(compile);
        
        JPAClassPathHelper cpHelper = new JPAClassPathHelper(
                Collections.<ClassPath>singleton(ClassPath.getClassPath(targetFolder, ClassPath.BOOT)), 
                Collections.<ClassPath>singleton(ClassPath.getClassPath(targetFolder, ClassPath.COMPILE)), 
                Collections.<ClassPath>singleton(ClassPath.getClassPath(targetFolder, ClassPath.SOURCE))
                );
        

        JavaSource targetSource = JavaSource.create(cpHelper.createClasspathInfo(), entityFo);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            
            public void run(WorkingCopy workingCopy) throws Exception {
                GenerationUtils genUtils = GenerationUtils.newInstance(workingCopy);
                ClassTree clazz = genUtils.getClassTree();
                ClassTree modifiedClazz = genUtils.ensureNoArgConstructor(clazz);
                TreeMaker make = workingCopy.getTreeMaker();
                
                String idFieldName = "id"; // NO18N
                TypeMirror type = workingCopy.getTreeUtilities().parseType(primaryKeyClassName, genUtils.getTypeElement());
                Tree typeTree = make.Type(type);
                
                Set<Modifier> serialVersionUIDModifiers = new HashSet<Modifier>();
                serialVersionUIDModifiers.add(Modifier.PRIVATE);
                serialVersionUIDModifiers.add(Modifier.STATIC);
                serialVersionUIDModifiers.add(Modifier.FINAL);
                
                VariableTree serialVersionUID = make.Variable(make.Modifiers(serialVersionUIDModifiers), "serialVersionUID", genUtils.createType("long"), make.Literal(Long.valueOf("1"))); //NO18N
                VariableTree idField = make.Variable(genUtils.createModifiers(Modifier.PRIVATE), idFieldName, typeTree, null);
                ModifiersTree idMethodModifiers = genUtils.createModifiers(Modifier.PUBLIC);
                MethodTree idGetter = genUtils.createPropertyGetterMethod(idMethodModifiers, idFieldName, typeTree);
                MethodTree idSetter = genUtils.createPropertySetterMethod(idMethodModifiers, idFieldName, typeTree);
                AnnotationTree idAnnotation = genUtils.createAnnotation("javax.persistence.Id"); //NO18N
                ExpressionTree generationStrategy = genUtils.createAnnotationArgument("strategy", "javax.persistence.GenerationType", "AUTO"); //NO18N
                AnnotationTree generatedValueAnnotation = genUtils.createAnnotation("javax.persistence.GeneratedValue", Collections.singletonList(generationStrategy)); //NO18N
                
                if (isAccessProperty){
                    idField = genUtils.addAnnotation(idField, idAnnotation);
                    idField = genUtils.addAnnotation(idField, generatedValueAnnotation);
                } else {
                    idGetter = genUtils.addAnnotation(idGetter, idAnnotation);
                    idGetter = genUtils.addAnnotation(idGetter, generatedValueAnnotation);
                }
                
                List<VariableTree> classFields = new ArrayList<VariableTree>();
                classFields.add(serialVersionUID);
                classFields.add(idField);
                modifiedClazz = genUtils.addClassFields(clazz, classFields);
                modifiedClazz = make.addClassMember(modifiedClazz, idSetter);
                modifiedClazz = make.addClassMember(modifiedClazz, idGetter);
                modifiedClazz = genUtils.addImplementsClause(modifiedClazz, "java.io.Serializable");
                modifiedClazz = genUtils.addAnnotation(modifiedClazz, genUtils.createAnnotation("javax.persistence.Entity"));
                
                String entityClassFqn = genUtils.getTypeElement().getQualifiedName().toString();
                EntityMethodGenerator methodGenerator = new EntityMethodGenerator(workingCopy, genUtils);
                List<VariableTree> fieldsForEquals = Collections.<VariableTree>singletonList(idField); 
                modifiedClazz = make.addClassMember(modifiedClazz, methodGenerator.createHashCodeMethod(fieldsForEquals));
                modifiedClazz = make.addClassMember(modifiedClazz, methodGenerator.createEqualsMethod(targetName, fieldsForEquals));
                modifiedClazz = make.addClassMember(modifiedClazz, methodGenerator.createToStringMethod(entityClassFqn, fieldsForEquals));
                workingCopy.rewrite(clazz, modifiedClazz);
            }
        };
        
        targetSource.runModificationTask(task).commit();
        
        return entityFo;
    }
    
    /**
     * A panel which checks whether the target project has a valid server set,
     * otherwise it delegates to the real panel.
     */
    private static final class ValidatingPanel extends DelegatingWizardDescriptorPanel {
        
        public ValidatingPanel(WizardDescriptor.Panel delegate) {
            super(delegate);
        }
        
        public boolean isValid() {
            if (!ProviderUtil.isValidServerInstanceOrNone(getProject())) {
                getWizardDescriptor().putProperty("WizardPanel_errorMessage",
                        NbBundle.getMessage(EntityWizardDescriptor.class, "ERR_MissingServer")); // NOI18N
                return false;
            }
            return super.isValid();
        }
    }
}
