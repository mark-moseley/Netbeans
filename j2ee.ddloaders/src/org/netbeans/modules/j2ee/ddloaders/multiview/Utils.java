/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ddloaders.multiview.ui.BrowseFolders;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.EntityNode;
import org.netbeans.modules.j2ee.ejbjarproject.ui.logicalview.ejb.entity.methodcontroller.EntityMethodController;
import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.refactoring.ui.MoveClassUI;
import org.netbeans.modules.refactoring.ui.RefactoringPanel;
import org.netbeans.modules.refactoring.ui.RenameRefactoringUI;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.src.ClassElement;
import org.openide.src.Identifier;
import org.openide.src.MethodElement;
import org.openide.src.MethodParameter;
import org.openide.src.SourceException;
import org.openide.src.Type;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

import javax.jmi.reflect.RefObject;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.StringTokenizer;

/**
 * @author pfiala
 */
public class Utils {

    public static final String ICON_BASE_DD_VALID =
            "org/netbeans/modules/j2ee/ddloaders/resources/DDValidIcon"; // NOI18N
    public static final String ICON_BASE_DD_INVALID =
            "org/netbeans/modules/j2ee/ddloaders/resources/DDInvalidIcon"; // NOI18N
    public static final String ICON_BASE_EJB_MODULE_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/EjbModuleNodeIcon"; // NOI18N
    public static final String ICON_BASE_ENTERPRISE_JAVA_BEANS_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/EjbContainerNodeIcon"; // NOI18N
    public static final String ICON_BASE_SESSION_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/SessionNodeIcon"; // NOI18N
    public static final String ICON_BASE_ENTITY_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/EntityNodeIcon"; // NOI18N
    public static final String ICON_BASE_MESSAGE_DRIVEN_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/MessageNodeIcon"; // NOI18N
    public static final String ICON_BASE_MISC_NODE =
            "org/netbeans/modules/j2ee/ddloaders/resources/MiscNodeIcon"; // NOI18N

    private static BrowseFolders.FileObjectFilter imageFileFilter = new BrowseFolders.FileObjectFilter() {
        public boolean accept(FileObject fileObject) {
            return fileObject.getMIMEType().startsWith("image/"); // NOI18N
        }
    };

    public static String browseIcon(EjbJarMultiViewDataObject dataObject) {
        FileObject fileObject = org.netbeans.modules.j2ee.ddloaders.multiview.ui.BrowseFolders.showDialog(
                dataObject.getSourceGroups(), imageFileFilter);
        String relativePath;
        if (fileObject != null) {
            FileObject projectDirectory = dataObject.getProjectDirectory();
            relativePath = FileUtil.getRelativePath(projectDirectory, fileObject);
        } else {
            relativePath = null;
        }
        return relativePath;
    }

    public static Color getErrorColor() {
        // inspired by org.openide.WizardDescriptor
        Color c = UIManager.getColor("nb.errorForeground"); //NOI18N
        return c == null ? new Color(89, 79, 191) : c;
    }

    public static JTree findTreeComponent(Component component) {
        if (component instanceof JTree) {
            return (JTree) component;
        }
        if (component instanceof Container) {
            Component[] components = ((Container) component).getComponents();
            for (int i = 0; i < components.length; i++) {
                JTree tree = findTreeComponent(components[i]);
                if (tree != null) {
                    return tree;
                }
            }
        }
        return null;
    }

    public static void scrollToVisible(JComponent component) {
        org.netbeans.modules.xml.multiview.Utils.scrollToVisible(component);
    }

    public static String getBundleMessage(String messageId) {
        return NbBundle.getMessage(Utils.class, messageId);
    }

    public static String getBundleMessage(String messageId, Object param1) {
        return NbBundle.getMessage(Utils.class, messageId, param1);
    }

    public static String getBundleMessage(String messageId, Object param1, Object param2) {
        return NbBundle.getMessage(Utils.class, messageId, param1, param2);
    }

    public static String getBundleMessage(String messageId, Object param1, Object param2, Object param3) {
        return NbBundle.getMessage(Utils.class, messageId, param1, param2, param3);
    }

    public static boolean isJavaIdentifier(String id) {
        return Utilities.isJavaIdentifier(id);
    }

    /**
     * Returns true, if the passed string can be used as a qualified identifier.
     * it does not check for semantic, only for syntax.
     * The function returns true for any sequence of identifiers separated by
     * dots.
     */
    public static boolean isValidPackageName(String packageName) {
        if (packageName.length() > 0 && packageName.charAt(0) == '.') {
            return false;
        }
        StringTokenizer tokenizer = new StringTokenizer(packageName, "."); // NOI18N
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.length() == 0) {
                return false;
            }
            if (!isJavaIdentifier(token)) {
                return false;
            }
        }
        return true;
    }

    public static void removeInterface(ClassElement classElement, String interfaceName) {
        Identifier[] interfaces = classElement.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaceName.equals(interfaces[i].getFullName())) {
                Identifier identifier = Identifier.create(interfaceName);
                removeInterface(classElement, identifier);
            }
        }
    }

    public static void removeInterface(ClassElement classElement, Identifier identifier) {
        try {
            classElement.removeInterface(identifier);
        } catch (SourceException ex) {
            Utils.notifyError(ex);
        }
    }

    public static void removeClass(ClassPath classPath, String className) {
        FileObject sourceFile = getSourceFile(classPath, className);
        if (sourceFile != null) {
            try {
                JavaDataObject.find(sourceFile).delete();
            } catch (DataObjectNotFoundException e) {
                notifyError(e);
            } catch (IOException e) {
                notifyError(e);
            }
        }
    }

    public static FileObject getPackageFile(ClassPath classPath, String packageName) {
        return classPath.findResource(packageToPath(packageName));
    }

    public static String packageToPath(String packageName) {
        return packageName.replace('.', '/');
    }

    public static String getPackage(String ejbClass) {
        return ejbClass.substring(0, ejbClass.lastIndexOf('.'));
    }

    public static void notifyError(Exception ex) {
        NotifyDescriptor ndd = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(ndd);
    }

    public static FileObject getSourceFile(ClassPath classPath, String className) {
        return classPath.findResource(packageToPath(className) + ".java");
    }

    public static EntityNode createEntityNode(FileObject ejbJarFile, ClassPath classPath, Entity entity) {
        EjbJar ejbJar;
        try {
            ejbJar = DDProvider.getDefault().getDDRoot(ejbJarFile);
        } catch (IOException e) {
            notifyError(e);
            return null;
        }
        return new EntityNode(entity, ejbJar, classPath, ejbJarFile);
    }

    public static ClassPath getSourceClassPath(FileObject ejbJarFile) {
        EjbJarProject enterpriseProject = (EjbJarProject) FileOwnerQuery.getOwner(ejbJarFile);
        return enterpriseProject.getEjbModule().getJavaSources();
    }

    public static MethodElement getBusinessMethod(ClassElement interfaceElement, MethodElement method) {
        if (interfaceElement == null || method == null) {
            return null;
        } else {
            MethodParameter[] parameters = method.getParameters();
            Type[] paramTypes = new Type[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                paramTypes[i] = parameters[i].getType();
            }
            return interfaceElement.getMethod(method.getName(), paramTypes);
        }
    }

    public static void addBusinessMethod(ClassElement interfaceElement, MethodElement method, boolean remote) {
        if (interfaceElement == null || method == null) {
            return;
        }
        MethodElement businessMethod = getBusinessMethod(interfaceElement, method);
        if (businessMethod != null) {
            return;
        }
        MethodParameter[] parameters = method.getParameters();
        Type[] paramTypes = new Type[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            paramTypes[i] = parameters[i].getType();
        }
        try {
            method.setModifiers(0);
            if (remote) {
                addExceptionIfNecessary(method, RemoteException.class.getName());
            }
            interfaceElement.addMethod(method);
        } catch (SourceException e) {
            Utils.notifyError(e);
        }
    }

    private static void addExceptionIfNecessary(MethodElement me, String exceptionName) throws SourceException {
        Identifier[] exceptions = me.getExceptions();
        boolean containsException = false;
        for (int i = 0; i < exceptions.length; i++) {
            String curExName = exceptions[i].getFullName();
            containsException |= exceptionName.equals(curExName);
        }
        if (!containsException) {
            Identifier[] newEx = new Identifier[exceptions.length + 1];
            System.arraycopy(exceptions, 0, newEx, 0, exceptions.length);
            newEx[newEx.length - 1] = Identifier.create(exceptionName);
            me.setExceptions(newEx);
        }
    }

    public static void removeBusinessMethod(ClassElement interfaceElement, MethodElement method) {
        if (interfaceElement == null || method == null) {
            return;
        }
        MethodElement businessMethod = getBusinessMethod(interfaceElement, method);
        if (businessMethod == null) {
            return;
        }
        try {
            interfaceElement.removeMethod(businessMethod);
        } catch (SourceException e) {
            Utils.notifyError(e);
        }
    }

    public static ClassElement getClassElement(ClassPath classPath, String className) {
        return ClassElement.forName(className, getSourceFile(classPath, className));
    }

    public static JavaClass resolveJavaClass(String fullClassName) {
        return (JavaClass) JavaModel.getDefaultExtent().getType().resolve(fullClassName);
    }

    public static void activateRenameClassUI(String fullClassName) {
        JavaClass jmiObject = resolveJavaClass(fullClassName);
        activateRenameRefactoringUI(jmiObject);
    }

    public static void activateMoveClassUI(String fullClassName) {
        JavaClass sourceClass = resolveJavaClass(fullClassName);
        activateMoveClassUI(sourceClass);
    }

    public static void activateMoveClassUI(JavaClass sourceClass) {
        new RefactoringPanel(new MoveClassUI(sourceClass));
    }

    public static void activateRenameRefactoringUI(RefObject jmiObject) {
        new RefactoringPanel(new RenameRefactoringUI(jmiObject));
    }

    public static String getMethodName(String fieldName, boolean get) {
        return EntityMethodController.getMethodName(fieldName, get);
    }

    public static void renameMethod(MethodElement method, Identifier identifier) {
        if (method != null) {
            try {
                method.setName(identifier);
            } catch (SourceException e) {
                notifyError(e);
            }
        }
    }

    public static void removeMethod(ClassElement classElement, MethodElement method) {
        try {
            classElement.removeMethod(method);
        } catch (SourceException e) {
            Utils.notifyError(e);
        }
    }
}
