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
 * specific language governing permissions and limitations under the1
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
package org.netbeans.modules.hibernate.hqleditor;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import org.dom4j.DocumentException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.hibernate.hqleditor.ui.HQLEditorTopComponent;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.netbeans.modules.hibernate.util.HibernateUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * HQL Editor controller. Controls overall HQL query execution.
 *
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class HQLEditorController {

    private Logger logger = Logger.getLogger(HQLEditorController.class.getName());
    private HQLEditorTopComponent editorTopComponent = null;

    private enum AnnotationAccessType {

        FIELD_TYPE,
        METHOD_TYPE;
    };

    public void executeHQLQuery(final String hql,
            final FileObject configFileObject,
            final int maxRowCount,
            final ProgressHandle ph) {
        final List<URL> localResourcesURLList = new ArrayList<URL>();

        try {
            ph.progress(10);
            ph.setDisplayName(NbBundle.getMessage(HQLEditorTopComponent.class, "queryExecutionPrepare"));
            final Project project = FileOwnerQuery.getOwner(configFileObject);
            // Construct custom classpath here.
            HibernateEnvironment env = project.getLookup().lookup(HibernateEnvironment.class);
            localResourcesURLList.addAll(env.getProjectClassPath(configFileObject));
            for (FileObject mappingFO : env.getAllHibernateMappingFileObjects()) {
                localResourcesURLList.add(mappingFO.getURL());
            }
            final ClassLoader customClassLoader = env.getProjectClassLoader(
                    localResourcesURLList.toArray(new URL[]{}));

            Thread t = new Thread() {

                @Override
                public void run() {
                    Thread.currentThread().setContextClassLoader(customClassLoader);
                    HQLExecutor queryExecutor = new HQLExecutor();
                    HQLResult hqlResult = new HQLResult();
                    try {
                        // Parse POJOs from HQL
                        // Check and if required compile POJO files mentioned in HQL
                        SessionFactory sessionFactory = processAndConstructSessionFactory(hql, configFileObject, customClassLoader, project);

                        ph.progress(50);
                        ph.setDisplayName(NbBundle.getMessage(HQLEditorTopComponent.class, "queryExecutionPassControlToHibernate"));
                        hqlResult = queryExecutor.execute(hql, sessionFactory, maxRowCount, ph);
                        ph.progress(80);
                        ph.setDisplayName(NbBundle.getMessage(HQLEditorTopComponent.class, "queryExecutionProcessResults"));

                    } catch (Exception e) {
                        logger.log(Level.INFO, "Problem in executing HQL", e);
                        hqlResult.getExceptions().add(e);
                    }
                    editorTopComponent.setResult(hqlResult, customClassLoader);
                }
            };
            t.setContextClassLoader(customClassLoader);
            t.start();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void init(Node[] activatedNodes) {
        editorTopComponent = new HQLEditorTopComponent(this);
        editorTopComponent.open();
        editorTopComponent.requestActive();
        editorTopComponent.setFocusToEditor();

        editorTopComponent.fillHibernateConfigurations(activatedNodes);
    }

    public SessionFactory getHibernateSessionFactoryForThisContext(FileObject configFileObject,
            Set<FileObject> mappingFOList,
            List<Class> annotatedClassList,
            ClassLoader customClassLoader) throws Exception {

        AnnotationConfiguration customConfiguration = null;
        try {
            Class configClass = customClassLoader.loadClass("org.hibernate.cfg.AnnotationConfiguration");
            customConfiguration = (AnnotationConfiguration) configClass.newInstance();

        } catch (ClassNotFoundException classNotFoundException) {
            Exceptions.printStackTrace(classNotFoundException);
        } catch (InstantiationException instantiationException) {
            Exceptions.printStackTrace(instantiationException);
        } catch (IllegalAccessException illegalAccessException) {
            Exceptions.printStackTrace(illegalAccessException);
        }

        try {
            org.dom4j.io.SAXReader saxReader = new org.dom4j.io.SAXReader();
            org.dom4j.Document document = saxReader.read(configFileObject.getInputStream());
            org.dom4j.Element sessionFactoryElement = document.getRootElement().element("session-factory"); //NOI18N
            Iterator mappingIterator = sessionFactoryElement.elementIterator("mapping"); //NOI18N
            while (mappingIterator.hasNext()) {
                org.dom4j.Element node = (org.dom4j.Element) mappingIterator.next();
                logger.fine("Removing mapping element ..  " + node);
                node.getParent().remove(node);
            }

            // Fix for 142899.  The actual exception generated while creating SessionFactory is not 
            // catchable so this pre-check ensures that there's no exception window comes up during 
            // query execution. 
            String sessionName = sessionFactoryElement.attributeValue("name");
            if (sessionName != null && (!sessionName.trim().equals(""))) {
                java.util.Properties prop = new java.util.Properties();
                Iterator propertyIterator = sessionFactoryElement.elementIterator("property");
                while (propertyIterator.hasNext()) {
                    org.dom4j.Element propNode = (org.dom4j.Element) propertyIterator.next();
                    if (org.hibernate.cfg.Environment.JNDI_CLASS.equals(propNode.attributeValue("name"))) {
                        prop.setProperty(
                                javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                                propNode.getTextTrim());
                    }
                    if (org.hibernate.cfg.Environment.JNDI_URL.equals(propNode.attributeValue("name"))) {
                        prop.setProperty(
                                javax.naming.Context.PROVIDER_URL,
                                propNode.getTextTrim());
                    }
                }

                try {
                    javax.naming.InitialContext context = new javax.naming.InitialContext(prop);
                    context.bind("dummy", new Object());
                    context.unbind("dummy");
                } catch (javax.naming.NamingException namingException) {
                    logger.log(Level.INFO, "Incorrect JNDI properties", namingException);
                    throw namingException;
                }

            }

            // End fix for 142899.

            //   add mappings
            for (FileObject mappingFO : mappingFOList) {
                logger.info("Adding mapping to custom configuration " + mappingFO.getName());
                org.dom4j.Element mappingElement = sessionFactoryElement.addElement("mapping"); //NOI18N
                File mappingFile = FileUtil.toFile(mappingFO);
                mappingElement.addAttribute("file", mappingFile.getAbsolutePath()); //NOI18N
            }
            // add annotated pojos.
            for (Class annotatedPOJO : annotatedClassList) {
                logger.info("Adding annotated class to custom configuration " + annotatedPOJO.getName());

                customConfiguration.addAnnotatedClass(annotatedPOJO);
            }
//        // configure 
            logger.info("configuring custom configuration..");
            customConfiguration.configure(getW3CDocument(document));
            return customConfiguration.buildSessionFactory();
        } catch (Exception e) {
            logger.log(Level.INFO, "Problem in constructing custom configuration", e);
            throw e;
        }

    }

    private org.w3c.dom.Document getW3CDocument(org.dom4j.Document document) {
        try {
            return new org.dom4j.io.DOMWriter().write(document);
        } catch (DocumentException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public SessionFactory processAndConstructSessionFactory(String hql, FileObject configFileObject,
            ClassLoader customClassLoader, Project project) throws Exception {
        HibernateEnvironment env = project.getLookup().lookup(HibernateEnvironment.class);

        StringTokenizer hqlTokenizer = new StringTokenizer(hql, " \n\r\f\t(),"); //NOI18N
        List<String> tokenList = new ArrayList<String>();
        while (hqlTokenizer.hasMoreTokens()) {
            tokenList.add(hqlTokenizer.nextToken());
        }

        // Process Mappings
        Set<FileObject> matchedMappingFOList = new HashSet<FileObject>();
        List<FileObject> mappingFOList = new ArrayList<FileObject>();
        Map<FileObject, List<String>> mappingPOJOMap = env.getAllPOJONamesFromConfiguration(configFileObject);

        for (FileObject mappingFO : mappingPOJOMap.keySet()) {
            List<String> pojoNameList = mappingPOJOMap.get(mappingFO);
            logger.info("pojoNameList from configution : ");
            for (String name : pojoNameList) {
                logger.info("pojo-name " + name);
            }

            for (String className : pojoNameList) {
                for (String hqlClassName : tokenList) {
                    if (foundClassNameMatch(hqlClassName, className)) {
                        Class clazz = processMatchingClass(className, customClassLoader, project);
                        logger.info("matching classname = " + className);
                        logger.info("Got clazz " + clazz);
                        if (clazz != null) {
                            matchedMappingFOList.add(mappingFO);
                            mappingFOList.add(mappingFO);
                            getRelatedMappings(mappingFO, matchedMappingFOList, mappingPOJOMap);
                            for (FileObject relatedMappingFO : matchedMappingFOList) {
                                List<String> relatedPojoNames = mappingPOJOMap.get(relatedMappingFO);
                                if (relatedPojoNames != null) {
                                    logger.info("Processing relationships from " + relatedMappingFO + " mapping file.");
                                    logger.info("Related POJOs " + relatedPojoNames);
                                    for (String relatedClassName : relatedPojoNames) {
                                        Class relatedClazz = processMatchingClass(relatedClassName, customClassLoader, project);
                                        logger.info("Got related POJO clazz " + relatedClazz);
                                        if (relatedClazz != null) {
                                            mappingFOList.add(relatedMappingFO);
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }

        // Process Annotated POJOs.
        List<String> annotatedPOJOClassNameList = env.getAnnotatedPOJOClassNames(configFileObject);
        List<Class> matchedAnnotatedPOJOClassNameList = new ArrayList<Class>();
        if (annotatedPOJOClassNameList.size() != 0) {
            for (String annotatedClassName : annotatedPOJOClassNameList) {
                for (String hqlClassName : tokenList) {
                    if (foundClassNameMatch(hqlClassName, annotatedClassName)) {
                        Class clazz = processMatchingClass(annotatedClassName, customClassLoader, project);
                        logger.info("matching classname = " + annotatedClassName);
                        logger.info("Got clazz " + clazz);
                        if (clazz != null) {
                            matchedAnnotatedPOJOClassNameList.add(clazz);
                            List<Class> relatedPOJOClasses = getRelatedPOJOClasses(clazz, annotatedPOJOClassNameList, customClassLoader, project);
                            logger.info("Related POJO Class list " + relatedPOJOClasses);
                            matchedAnnotatedPOJOClassNameList.addAll(relatedPOJOClasses);
                        }
                    }
                }
            }
        }

        return getHibernateSessionFactoryForThisContext(
                configFileObject,
                matchedMappingFOList,
                matchedAnnotatedPOJOClassNameList,
                customClassLoader);
    }

    private List<Class> getRelatedPOJOClasses(Class clazz, List<String> annotatedPOJOClassNameList,
            ClassLoader ccl, Project project) {
        List<Class> relatedPOJOClasses = new ArrayList<Class>();
        getRelatedPOJOClassesByType(clazz, annotatedPOJOClassNameList, relatedPOJOClasses, ccl, project);
        return relatedPOJOClasses;
    }

    private void getRelatedPOJOClassesByType(Class clazz, List<String> annotatedPOJOClassNameList, List<Class> relatedPOJOClasses,
            ClassLoader ccl, Project project) {

        AnnotationAccessType annotationAccessType = findAnnotationAccessType(clazz);
        if (annotationAccessType == AnnotationAccessType.METHOD_TYPE) {
            logger.info("Annotation Access type for " + clazz.getName() + " is : METHOD_TYPE");
            getRelatedPOJOClassesByMethodType(clazz, annotatedPOJOClassNameList, relatedPOJOClasses, ccl, project);
        } else {
            logger.info("Annotation Access type for " + clazz.getName() + " is : FIELD_TYPE");
            getRelatedPOJOClassesByFieldType(clazz, annotatedPOJOClassNameList, relatedPOJOClasses, ccl, project);
        }
    }

    // Annotation access type can be field or method type.
    // Determines by the position of @Id or @EmbeddedId. 
    private AnnotationAccessType findAnnotationAccessType(Class clazz) {

        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(javax.persistence.Id.class) ||
                    field.isAnnotationPresent(javax.persistence.EmbeddedId.class)) {
                return AnnotationAccessType.FIELD_TYPE;
            }
        }
        return AnnotationAccessType.METHOD_TYPE;
    }

    private void getRelatedPOJOClassesByMethodType(Class clazz, List<String> annotatedPOJOClassNameList, List<Class> relatedPOJOClasses,
            ClassLoader ccl, Project project) {

        for (java.lang.reflect.Method m : clazz.getMethods()) {
            if (m.isAnnotationPresent(javax.persistence.ManyToOne.class) || m.isAnnotationPresent(javax.persistence.OneToOne.class) ||
                    m.isAnnotationPresent(javax.persistence.OneToMany.class)) {
                logger.info("Found relationship in " + m.getName() + " method of " + clazz.getName() + " related POJO.");
                try {
                    Class relatedPOJOClass = m.getReturnType();
                    // Check for Java collection and Map types.
                    if (java.util.Collection.class.isAssignableFrom(relatedPOJOClass)) {
                        try {
                            Class returnClassType = (Class) ((java.lang.reflect.ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0];
                            logger.info("Method return type is java.util.Collection");
                            if (!returnClassType.equals(java.lang.Object.class)) {
                                logger.info("Re-assigning related class to " + returnClassType);
                                relatedPOJOClass = returnClassType;
                            }
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    } else if (java.util.Map.class.isAssignableFrom(relatedPOJOClass)) {
                        logger.info("Accessor method return type is java.util.Map");
                    }

                    if (annotatedPOJOClassNameList.contains(relatedPOJOClass.getName())) {
                        logger.info("Related POJO Class " + relatedPOJOClass.getName());
                        if (relatedPOJOClasses.contains(relatedPOJOClass)) {
                            logger.info("Already processed " + relatedPOJOClass + ". Skipping.");
                            continue;
                        }
                        logger.info("adding to related POJO class list " + relatedPOJOClass);
                        relatedPOJOClasses.add(relatedPOJOClass);
                        getRelatedPOJOClassesByType(relatedPOJOClass, annotatedPOJOClassNameList, relatedPOJOClasses, ccl, project);

                    }
                    // Add other side classes of relation, if targetEntity defined.
                    javax.persistence.OneToMany oneToManyAnnotation = m.getAnnotation(javax.persistence.OneToMany.class);
                    if (oneToManyAnnotation != null) {
                        Class targetEntityClass = oneToManyAnnotation.targetEntity();
                        if (targetEntityClass != null && (!targetEntityClass.getName().equals("void"))) {
                            if (relatedPOJOClasses.contains(targetEntityClass)) {
                                // Already processed class
                                logger.info("Already processed " + targetEntityClass + ". Skipping.");
                                continue;
                            }
                            targetEntityClass = processMatchingClass(targetEntityClass.getName(), ccl, project);
                            if (targetEntityClass != null) {
                                logger.info("adding to related POJO class list from targetEntity : " + targetEntityClass);
                                relatedPOJOClasses.add(targetEntityClass);
                                getRelatedPOJOClassesByType(targetEntityClass, annotatedPOJOClassNameList, relatedPOJOClasses, ccl, project);
                            }
                        }
                    }

                    javax.persistence.ManyToOne manyToOneAnnotation = m.getAnnotation(javax.persistence.ManyToOne.class);
                    if (manyToOneAnnotation != null) {
                        Class targetEntityClass = manyToOneAnnotation.targetEntity();
                        if (targetEntityClass != null && (!targetEntityClass.getName().equals("void"))) {
                            if (relatedPOJOClasses.contains(targetEntityClass)) {
                                // Already processed class
                                logger.info("Already processed " + targetEntityClass + ". Skipping.");
                                continue;
                            }
                            targetEntityClass = processMatchingClass(targetEntityClass.getName(), ccl, project);
                            if (targetEntityClass != null) {
                                logger.info("adding to related POJO class list from targetEntity : " + targetEntityClass);
                                relatedPOJOClasses.add(targetEntityClass);
                                getRelatedPOJOClassesByType(targetEntityClass, annotatedPOJOClassNameList, relatedPOJOClasses, ccl, project);
                            }
                        }
                    }
                    javax.persistence.OneToOne oneToOneAnnotation = m.getAnnotation(javax.persistence.OneToOne.class);
                    if (oneToOneAnnotation != null) {
                        Class targetEntityClass = oneToOneAnnotation.targetEntity();
                        if (targetEntityClass != null && (!targetEntityClass.getName().equals("void"))) {
                            if (relatedPOJOClasses.contains(targetEntityClass)) {
                                // Already processed class
                                logger.info("Already processed " + targetEntityClass + ". Skipping.");
                                continue;
                            }
                            targetEntityClass = processMatchingClass(targetEntityClass.getName(), ccl, project);
                            if (targetEntityClass != null) {
                                logger.info("adding to related POJO class list from targetEntity : " + targetEntityClass);
                                relatedPOJOClasses.add(targetEntityClass);
                                getRelatedPOJOClassesByType(targetEntityClass, annotatedPOJOClassNameList, relatedPOJOClasses, ccl, project);
                            }
                        }
                    }
                } catch (IllegalArgumentException illegalArgumentException) {
                    logger.log(Level.INFO, "Accessor method is not annotated", illegalArgumentException);
                }

            }
        }
    }

    private void getRelatedPOJOClassesByFieldType(Class clazz, List<String> annotatedPOJOClassNameList,
            List<Class> relatedPOJOClasses, ClassLoader ccl, Project project) {
        // Process declared variables.
        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(javax.persistence.ManyToOne.class) || field.isAnnotationPresent(javax.persistence.OneToOne.class) ||
                    field.isAnnotationPresent(javax.persistence.OneToMany.class)) {
                logger.info("Found relationship in " + field.getName() + " field of " + clazz.getName() + " related POJO.");
                try {
                    Class relatedPOJOClass = field.getType();
                    // Check for Java collection and Map types.
                    if (java.util.Collection.class.isAssignableFrom(relatedPOJOClass)) {
                        try {
                            Class returnClassType = (Class) ((java.lang.reflect.ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                            logger.info("Field type is java.util.Collection");
                            if (!returnClassType.equals(java.lang.Object.class)) {
                                logger.info("Re-assigning related class to " + returnClassType);
                                relatedPOJOClass = returnClassType;
                            }
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    } else if (java.util.Map.class.isAssignableFrom(relatedPOJOClass)) {
                        logger.info("Field type is java.util.Map");
                    }

                    if (annotatedPOJOClassNameList.contains(relatedPOJOClass.getName())) {
                        logger.info("Related POJO Class " + relatedPOJOClass.getName());
                        if (relatedPOJOClasses.contains(relatedPOJOClass)) {
                            logger.info("Already processed " + relatedPOJOClass + ". Skipping.");
                            continue;
                        }
                        logger.info("adding to related POJO class list " + relatedPOJOClass);
                        relatedPOJOClasses.add(relatedPOJOClass);
                        getRelatedPOJOClassesByType(relatedPOJOClass, annotatedPOJOClassNameList, relatedPOJOClasses, ccl, project);

                    }
                    // Add other side classes of relation, if targetEntity defined.
                    javax.persistence.OneToMany oneToManyAnnotation = field.getAnnotation(javax.persistence.OneToMany.class);
                    if (oneToManyAnnotation != null) {
                        Class targetEntityClass = oneToManyAnnotation.targetEntity();
                        if (targetEntityClass != null && (!targetEntityClass.getName().equals("void"))) {
                            if (relatedPOJOClasses.contains(targetEntityClass)) {
                                // Already processed class
                                logger.info("Already processed " + targetEntityClass + ". Skipping.");
                                continue;
                            }
                            targetEntityClass = processMatchingClass(targetEntityClass.getName(), ccl, project);
                            if (targetEntityClass != null) {
                                logger.info("adding to related POJO class list from targetEntity : " + targetEntityClass);
                                relatedPOJOClasses.add(targetEntityClass);
                                getRelatedPOJOClassesByType(targetEntityClass, annotatedPOJOClassNameList, relatedPOJOClasses, ccl, project);
                            }
                        }
                    }

                    javax.persistence.ManyToOne manyToOneAnnotation = field.getAnnotation(javax.persistence.ManyToOne.class);
                    if (manyToOneAnnotation != null) {
                        Class targetEntityClass = manyToOneAnnotation.targetEntity();
                        if (targetEntityClass != null && (!targetEntityClass.getName().equals("void"))) {
                            if (!relatedPOJOClasses.contains(targetEntityClass)) {
                                targetEntityClass = processMatchingClass(targetEntityClass.getName(), ccl, project);
                                if (targetEntityClass != null) {
                                    logger.info("adding to related POJO class list from targetEntity : " + targetEntityClass);
                                    relatedPOJOClasses.add(targetEntityClass);
                                    getRelatedPOJOClassesByType(targetEntityClass, annotatedPOJOClassNameList, relatedPOJOClasses, ccl, project);
                                }
                            } else {
                                logger.info("Already processed " + targetEntityClass + ". Skipping.");
                                continue;
                            }
                        } else {
                            // No targetEntity defined.
                        }
                    }
                    javax.persistence.OneToOne oneToOneAnnotation = field.getAnnotation(javax.persistence.OneToOne.class);
                    if (oneToOneAnnotation != null) {
                        Class targetEntityClass = oneToOneAnnotation.targetEntity();
                        if (targetEntityClass != null && (!targetEntityClass.getName().equals("void"))) {
                            if (relatedPOJOClasses.contains(targetEntityClass)) {
                                // Already processed class
                                logger.info("Already processed " + targetEntityClass + ". Skipping.");
                                continue;
                            }
                            targetEntityClass = processMatchingClass(targetEntityClass.getName(), ccl, project);
                            if (targetEntityClass != null) {
                                logger.info("adding to related POJO class list from targetEntity : " + targetEntityClass);
                                relatedPOJOClasses.add(targetEntityClass);
                                getRelatedPOJOClassesByType(targetEntityClass, annotatedPOJOClassNameList, relatedPOJOClasses, ccl, project);
                            }
                        }
                    }
                } catch (IllegalArgumentException illegalArgumentException) {
                    logger.log(Level.INFO, "Field is not annotated", illegalArgumentException);
                }

            }
        }
    }

    private Set<FileObject> getRelatedMappings(FileObject mappingFO, Set<FileObject> relatedMappings, Map<FileObject, List<String>> mappingPOJOMap) {
        try {
            org.dom4j.io.SAXReader xmlReader = new org.dom4j.io.SAXReader();
            org.dom4j.Document document = xmlReader.read(FileUtil.toFile(mappingFO));
            Iterator classElementIterator = document.getRootElement().elementIterator("class");
            while (classElementIterator.hasNext()) {
                org.dom4j.Element classElement = (org.dom4j.Element) classElementIterator.next();
                logger.info("Processing many-to-one");
                processMappingRelationships(classElement.elementIterator("many-to-one"), relatedMappings, mappingPOJOMap);
                logger.info("Processing one-to-one");
                processMappingRelationships(classElement.elementIterator("one-to-one"), relatedMappings, mappingPOJOMap);
                logger.info("Processing set");
                processMappingRelationships(classElement.elementIterator("set"), relatedMappings, mappingPOJOMap);
                logger.info("Processing idbag");
                processMappingRelationships(classElement.elementIterator("idbag"), relatedMappings, mappingPOJOMap);
                logger.info("Processing map");
                processMappingRelationships(classElement.elementIterator("map"), relatedMappings, mappingPOJOMap);
                logger.info("Processing bag");
                processMappingRelationships(classElement.elementIterator("bag"), relatedMappings, mappingPOJOMap);
                logger.info("Processing list");
                processMappingRelationships(classElement.elementIterator("list"), relatedMappings, mappingPOJOMap);
                logger.info("Processing array");
                processMappingRelationships(classElement.elementIterator("array"), relatedMappings, mappingPOJOMap);

            }
        } catch (Exception e) {
            logger.log(Level.INFO, "Problem in parsing mapping file for relation", e);
        }

        return relatedMappings;
    }

    private Set<FileObject> processMappingRelationships(Iterator relationshipIterator, Set<FileObject> relatedMappings, Map<FileObject, List<String>> mappingPOJOMap) {
        while (relationshipIterator.hasNext()) {
            org.dom4j.Element relationshipElement = (org.dom4j.Element) relationshipIterator.next();
            String pojoName = relationshipElement.attributeValue("class");
            if (pojoName == null) {
                // Check for Collection based relationship types.
                org.dom4j.Element connectionTypeElement = relationshipElement.element("one-to-many");
                if (connectionTypeElement == null) {
                    connectionTypeElement = relationshipElement.element("composite-element");
                }
                if (connectionTypeElement == null) {
                    connectionTypeElement = relationshipElement.element("one-to-many");
                }
                if (connectionTypeElement == null) {
                    connectionTypeElement = relationshipElement.element("many-to-many");
                }
                if (connectionTypeElement != null) {
                    pojoName = connectionTypeElement.attributeValue("class");
                }
            }
            FileObject relatedMappingFO = findRelatedMappingFO(pojoName, mappingPOJOMap);
            if (relatedMappingFO != null && (!relatedMappings.contains(relatedMappingFO))) {
                relatedMappings.add(relatedMappingFO);
                getRelatedMappings(relatedMappingFO, relatedMappings, mappingPOJOMap);
            }
        }
        logger.info("Related mapping files : " + relatedMappings);
        return relatedMappings;
    }

    private FileObject findRelatedMappingFO(String pojoName, Map<FileObject, List<String>> mappingPOJOMap) {
        for (FileObject mappingFile : mappingPOJOMap.keySet()) {
            List<String> pojoNameList = mappingPOJOMap.get(mappingFile);
            if (pojoNameList.contains(pojoName)) {
                logger.info("Related POJO : " + pojoName);
                return mappingFile;
            }
        }
        return null; // mapping file not found.
    }

    private boolean foundClassNameMatch(String hqlClassName, String className) {
        boolean foundMatch = false;
        if (hqlClassName.indexOf(".") != -1) {
            if (className.endsWith(hqlClassName)) {
                foundMatch = true;
            }
        } else {
            if (className.indexOf(".") == -1) {
                if (className.equals(hqlClassName)) {
                    foundMatch = true;
                }
            } else {
                String actualClassName = className.substring(className.lastIndexOf(".") + 1);
                if (actualClassName.equals(hqlClassName)) {
                    foundMatch = true;
                }
            }
        }
        return foundMatch;
    }

    private Class processMatchingClass(String className, ClassLoader customClassLoader, Project project) {
        FileObject clazzFO = HibernateUtil.findJavaFileObjectInProject(className, project);
        FileObject buildFolderFO = HibernateUtil.getBuildFO(project);
        if (buildFolderFO == null) {
            return null; // Unable to find the build folder.
        }
        return checkAndCompile(className, clazzFO, buildFolderFO, customClassLoader, project);
    }

    private Class checkAndCompile(String className, FileObject sourceFO, FileObject buildFolderFO, ClassLoader customClassLoader, Project project) {
        Class clazz = null;

        try {
            clazz = customClassLoader.loadClass(className);
            if (clazz != null) {
                logger.info("Found pre-existing class. Returning.." + clazz.getName());
                return clazz;
            }
        } catch (ClassNotFoundException e) {
            // Compile the class here.
            logger.info("CNF. Processing .. " + className);

            try {
                JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
                StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(null, null, null);
                className = className.replace(".", File.separator);

                File sourceFile = FileUtil.toFile(sourceFO);

                Iterable<? extends JavaFileObject> compilationUnits =
                        fileManager.getJavaFileObjectsFromFiles(Arrays.asList(new File[]{sourceFile}));
                List<File> outputPath = new ArrayList<File>();
                outputPath.add(FileUtil.toFile(buildFolderFO));
                List<File> sourcePath = new ArrayList<File>();
                for (SourceGroup sourceGroup : HibernateUtil.getSourceGroups(project)) {
                    sourcePath.add(
                            FileUtil.toFile(sourceGroup.getRootFolder()));
                }
                fileManager.setLocation(StandardLocation.CLASS_OUTPUT, outputPath);
                fileManager.setLocation(StandardLocation.CLASS_PATH, getProjectClasspath(project, sourceFO));
                fileManager.setLocation(StandardLocation.SOURCE_PATH, sourcePath);
                List<String> options = new ArrayList<String>();
                options.add("-target"); //NOI18N
                String jdkVersion = System.getProperty("java.specification.version"); //NOI18N
                if (jdkVersion == null || jdkVersion.equals("")) { //NOI18N
                    // Set to 1.5
                    jdkVersion = "1.5"; //NOI18N
                }
                options.add(jdkVersion);



                // for some reason the following is not working.. Bug in JavaC API?
//                options.add("-classpath");
//                options = addClasspath(project, sourceFO, options);
                //TODO diagnostic listener - plugin log
                Boolean b = javaCompiler.getTask(null, fileManager, null, options, null, compilationUnits).call();
                logger.info("b = " + b);
                if (b == false) { // Compilation errors.
                    FileObject classfileFO = buildFolderFO.getFileObject(className + ".class");
                    if (classfileFO != null && classfileFO.isValid()) {
                        classfileFO.delete();
                    }
                    return clazz;
                }
                try {
                    className = className.replace(File.separator, ".");
                    clazz = customClassLoader.loadClass(className);
                    if (clazz != null) {
                        logger.info("Found class after processing. Returning.." + clazz.getName());
                        return clazz;
                    }
                } catch (ClassNotFoundException ee) {
                    logger.info("CNF after processing.. " + className);
                    Exceptions.printStackTrace(ee);
                }
            // Ant approach -- commented out for future use.
            //  FileObject buildXMLFileObject = project.getProjectDirectory().getFileObject("build", "xml");
            //   java.util.Properties p = new java.util.Properties();
            // p.setProperty("javac.includes", ActionUtils.antIncludesList(
            //        files, 
            //        configFileObject, 
            //        recursive));
            //  ExecutorTask task = ActionUtils.runTarget(buildXMLFileObject, new String[]{"compile-single"}, p);
            //  InputOutput io = task.getInputOutput();
            //io.
            //  int r = task.result();
            //  System.out.println("result = " + r);
            } catch (Exception ee) {
                Exceptions.printStackTrace(ee);
            }

        }
        return clazz;
    }

    private List<File> getProjectClasspath(Project project, FileObject sourceFO) {
        List<File> cpEntries = new ArrayList<File>();
        HibernateEnvironment env = (HibernateEnvironment) project.getLookup().lookup(HibernateEnvironment.class);

        for (URL url : env.getProjectClassPath(sourceFO)) {
            String cpEntry = url.getPath();
            cpEntry = cpEntry.replace("file:", "");
            cpEntry = cpEntry.replace("!/", "");
            File f = new File(cpEntry);
            cpEntries.add(f);
        }
        logger.info("Adding classpath " + cpEntries);
        return cpEntries;
    }
}
