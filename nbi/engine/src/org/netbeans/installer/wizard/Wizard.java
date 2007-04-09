/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */
package org.netbeans.installer.wizard;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.netbeans.installer.utils.helper.PropertyContainer;
import org.netbeans.installer.utils.helper.UiMode;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.helper.FinishHandler;
import org.netbeans.installer.utils.helper.Context;
import org.netbeans.installer.wizard.containers.WizardContainer;
import org.netbeans.installer.wizard.containers.SwingFrameContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 *
 * @author Kirill Sorokin
 */
public class Wizard {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static Wizard instance;
    
    private static String componentsInstanceUri;
    private static String componentsSchemaUri;
    
    public static synchronized Wizard getInstance() {
        if (instance == null) {
            // initialize uri for root wizard's components list
            if (System.getProperty(COMPONENTS_INSTANCE_URI_PROPERTY) != null) {
                componentsInstanceUri =
                        System.getProperty(COMPONENTS_INSTANCE_URI_PROPERTY);
            } else {
                componentsInstanceUri = DEFAULT_COMPONENTS_INSTANCE_URI;
            }
            
            // initialize uri for components list xml schema
            if (System.getProperty(COMPONENTS_SCHEMA_URI_PROPERTY) != null) {
                componentsInstanceUri =
                        System.getProperty(COMPONENTS_SCHEMA_URI_PROPERTY);
            } else {
                componentsSchemaUri = DEFAULT_COMPONENTS_SCHEMA_URI;
            }
            
            // create the root wizard and load its components
            instance = new Wizard();
            try {
                instance.components = loadWizardComponents(componentsInstanceUri);
            } catch (InitializationException e) {
                ErrorManager.notifyCritical(ResourceUtils.getString(
                        Wizard.class,
                        FAILED_TO_CREATE_INSTANCE_PROPERTY), e);
            }
        }
        
        return instance;
    }
    
    public static List<WizardComponent> loadWizardComponents(
            final String componentsUri) throws InitializationException {
        return loadWizardComponents(componentsUri, Wizard.class.getClassLoader());
    }
    
    public static List<WizardComponent> loadWizardComponents(
            final String componentsUri,
            final ClassLoader classLoader) throws InitializationException {
        try {
            final File schemaFile = FileProxy.getInstance().getFile(
                    componentsSchemaUri, 
                    classLoader);
            final File componentsFile = FileProxy.getInstance().getFile(
                    componentsUri, 
                    classLoader);
            
            final Schema schema = SchemaFactory.
                    newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).
                    newSchema(schemaFile);
            
            final DocumentBuilderFactory documentBuilderFactory =
                    DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setSchema(schema);
            documentBuilderFactory.setNamespaceAware(true);
            
            final Document document = documentBuilderFactory.newDocumentBuilder().
                    parse(componentsFile);
            
            return loadWizardComponents(document.getDocumentElement(), classLoader);
        } catch (DownloadException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Wizard.class,
                    FAILED_TO_LOAD_COMPONENTS_PROPERTY, 
                    componentsUri, 
                    classLoader), e);
        } catch (ParserConfigurationException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Wizard.class,
                    FAILED_TO_LOAD_COMPONENTS_PROPERTY, 
                    componentsUri, 
                    classLoader), e);
        } catch (SAXException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Wizard.class,
                    FAILED_TO_LOAD_COMPONENTS_PROPERTY, 
                    componentsUri, 
                    classLoader), e);
        } catch (IOException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Wizard.class,
                    FAILED_TO_LOAD_COMPONENTS_PROPERTY, 
                    componentsUri, 
                    classLoader), e);
        }
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private static List<WizardComponent> loadWizardComponents(
            final Element element,
            final ClassLoader classLoader) throws InitializationException {
        final List<WizardComponent> components = new ArrayList<WizardComponent>();
        
        for (Element child: XMLUtils.getChildren(element, COMPONENT_TAG_NAME)) {
            components.add(loadWizardComponent(child, classLoader));
        }
        
        return components;
    }
    
    private static WizardComponent loadWizardComponent(
            final Element element,
            final ClassLoader classLoader) throws InitializationException {
        final WizardComponent component;
        
        try {
            component = (WizardComponent) classLoader.loadClass(
                    element.getAttribute(CLASS_ATRIBUTE_NAME)).newInstance();
            
            Element child = XMLUtils.getChild(element, COMPONENTS_TAG_NAME);
            if (child != null) {
                component.addChildren(loadWizardComponents(child, classLoader));
            }
            
            child = XMLUtils.getChild(element, PROPERTIES_TAG_NAME);
            if (child != null) {
                component.getProperties().putAll(XMLUtils.parseProperties(child));
            }
        } catch (ParseException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Wizard.class,
                    FAILED_TO_LOAD_COMPONENT_PROPERTY,
                    element,
                    classLoader), e);
        } catch (ClassNotFoundException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Wizard.class,
                    FAILED_TO_LOAD_COMPONENT_PROPERTY,
                    element,
                    classLoader), e);
        } catch (IllegalAccessException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Wizard.class,
                    FAILED_TO_LOAD_COMPONENT_PROPERTY,
                    element,
                    classLoader), e);
        } catch (InstantiationException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Wizard.class,
                    FAILED_TO_LOAD_COMPONENT_PROPERTY,
                    element,
                    classLoader), e);
        }
        
        return component;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private List<WizardComponent> components;
    private WizardContainer container;
    
    private PropertyContainer propertyContainer;
    private Context context;
    private ClassLoader classLoader;
    
    private int index;
    private Wizard parent;
    
    private FinishHandler finishHandler;
    
    // constructors /////////////////////////////////////////////////////////////////
    private Wizard() {
        this.index = -1;
        this.context = new Context();
        this.classLoader = getClass().getClassLoader();
    }
    
    private Wizard(
            final Wizard parent) {
        this();
        
        this.parent = parent;
        this.container = parent.container;
        
        this.propertyContainer = parent.propertyContainer;
        this.context = new Context(parent.context);
        this.classLoader = parent.classLoader;
        
        this.finishHandler = parent.finishHandler;
    }
    
    private Wizard(
            final List<WizardComponent> components,
            final Wizard parent,
            int index) {
        this(parent);
        
        this.components = components;
        this.index = index;
    }
    
    private Wizard(
            final PropertyContainer product,
            final ClassLoader loader,
            final List<WizardComponent> components,
            final Wizard parent,
            final int index) {
        this(components, parent, index);
        
        this.propertyContainer = product;
        this.classLoader = loader;
    }
    
    // wizard lifecycle control methods /////////////////////////////////////////////
    public void open() {
        // if a parent exists, ask it - it knows better
        if (parent != null) {
            parent.open();
            return;
        }
        
        switch (UiMode.getCurrentUiMode()) {
        case SWING:
            container = new SwingFrameContainer();
            
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        Thread.currentThread().setUncaughtExceptionHandler(
                                ErrorManager.getExceptionHandler());
                    }
                });
            } catch (InvocationTargetException e) {
                ErrorManager.notifyDebug(ResourceUtils.getString(
                        Wizard.class,
                        FAILED_TO_ATTACH_ERROR_HANDLER_PROPERTY), e);
            } catch (InterruptedException e) {
                ErrorManager.notifyDebug(ResourceUtils.getString(
                        Wizard.class,
                        FAILED_TO_ATTACH_ERROR_HANDLER_PROPERTY), e);
            }
            
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    container.setVisible(true);
                }
            });
            break;
        case SILENT:
            // we don't have to initialize anything for silent mode
            break;
        default:
            ErrorManager.notifyCritical(ResourceUtils.getString(
                    Wizard.class,
                    UNKNOWN_UI_MODE_PROPERTY,
                    UiMode.getCurrentUiMode()));
        }
        
        next();
    }
    
    public void close() {
        // if a parent exists, ask it - it knows better
        if (parent != null) {
            parent.close();
            return;
        }
        
        switch (UiMode.getCurrentUiMode()) {
        case SWING:
            container.setVisible(false);
            break;
        case SILENT:
            // we don't have to initialize anything for silent mode
            break;
        default:
            ErrorManager.notifyCritical(ResourceUtils.getString(
                    Wizard.class,
                    UNKNOWN_UI_MODE_PROPERTY,
                    UiMode.getCurrentUiMode()));
        }
    }
    
    // component flow control methods ///////////////////////////////////////////////
    public void next() {
        final WizardComponent component = getNext();
        
        // if there is no next component in the current wizard, try to delegate
        // the call to the parent wizard, and if there is no parent wizard... finish
        // the sequence, and call the finish handler
        if (component != null) {
            index = components.indexOf(component);
            
            component.setWizard(this);
            component.initialize();
            
            switch (UiMode.getCurrentUiMode()) {
            case SWING:
                if (component.getWizardUi() != null) {
                    container.updateWizardUi(component.getWizardUi());
                }
                break;
            case SILENT:
                // nothing special should be done for silent mode
                break;
            default:
                ErrorManager.notifyCritical(ResourceUtils.getString(
                        Wizard.class,
                        UNKNOWN_UI_MODE_PROPERTY,
                        UiMode.getCurrentUiMode()));
            }
            
            component.executeForward();
        } else if (parent != null) {
            parent.next();
        } else {
            finishHandler.finish();
        }
    }
    
    public void previous() {
        final WizardComponent component = getPrevious();
        
        // if there is no previous component in the current wizard, try to delegate
        // the call to the parent wizard, and if there is no parent wizard... we
        // should be here in the first place
        if (component != null) {
            index = components.indexOf(component);
            
            component.setWizard(this);
            component.initialize();
            
            switch (UiMode.getCurrentUiMode()) {
            case SWING:
                if (component.getWizardUi() != null) {
                    container.updateWizardUi(component.getWizardUi());
                }
                break;
            case SILENT:
                ErrorManager.notifyCritical(ResourceUtils.getString(
                        Wizard.class,
                        CANNOT_MOVE_BACKWARD_SILENT_PROPERTY));
                break;
            default:
                ErrorManager.notifyCritical(ResourceUtils.getString(
                        Wizard.class,
                        UNKNOWN_UI_MODE_PROPERTY,
                        UiMode.getCurrentUiMode()));
            }
            
            component.executeBackward();
        } else if (parent != null) {
            parent.previous();
        } else {
            ErrorManager.notifyCritical(ResourceUtils.getString(
                    Wizard.class,
                    CANNOT_MOVE_BACKWARD_AT_FIRST_PROPERTY));
        }
    }
    
    // informational methods ////////////////////////////////////////////////////////
    public boolean hasPrevious() {
        // if current component is a point of no return - we cannot move backwards,
        // i.e. there is no previous component
        if ((getCurrent() != null) && getCurrent().isPointOfNoReturn()) {
            return false;
        }
        
        for (int i = index - 1; i > -1; i--) {
            final WizardComponent component = components.get(i);
            
            // if the component can be executed backward it is the previous one
            if (component.canExecuteBackward()) {
                return true;
            }
            
            // if the currently examined component is a point of no return and it
            // cannot be executed (since we passed the previous statement) - we have
            // no previous component
            if (component.isPointOfNoReturn()) {
                return false;
            }
        }
        
        // if we got this far, there is not previous component in the current wizard,
        // but no points of no return we encountered either. thus we should ask the
        // parent wizard if it has a previous component
        return (parent != null) && parent.hasPrevious();
    }
    
    public boolean hasNext() {
        // if there is no next component in the current wizard, we should check the
        // parent wizard if it has one
        return (getNext() != null) || ((parent != null) && parent.hasNext());
    }
    
    // getters/setters //////////////////////////////////////////////////////////////
    public int getIndex() {
        return index;
    }
    
    public WizardContainer getContainer() {
        return container;
    }
    
    public String getProperty(final String name) {
        return propertyContainer.getProperty(name);
    }
    
    public void setProperty(final String name, final String value) {
        propertyContainer.setProperty(name, value);
    }
    
    public Context getContext() {
        return context;
    }
    
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    
    public FinishHandler getFinishHandler() {
        return finishHandler;
    }
    
    public void setFinishHandler(final FinishHandler finishHandler) {
        this.finishHandler = finishHandler;
    }
    
    // factory methods for children /////////////////////////////////////////////////
    public Wizard createSubWizard(
            final PropertyContainer propertyContainer,
            final ClassLoader classLoader,
            final List<WizardComponent> components,
            final int index) {
        return new Wizard(propertyContainer, classLoader, components, this, index);
    }
    
    public Wizard createSubWizard(
            final List<WizardComponent> components,
            final int index) {
        return new Wizard(components, this, index);
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private WizardComponent getCurrent() {
        if ((index > -1) && (index < components.size())) {
            return components.get(index);
        } else {
            return null;
        }
    }
    
    private WizardComponent getPrevious() {
        // if current component is a point of no return - we cannot move backwards,
        // i.e. there is no previous component
        if ((getCurrent() != null) && getCurrent().isPointOfNoReturn()) {
            return null;
        }
        
        for (int i = index - 1; i > -1; i--) {
            final WizardComponent component = components.get(i);
            
            // if the component can be executed backward it is the previous one
            if (component.canExecuteBackward()) {
                return component;
            }
            
            // if the currently examined component is a point of no return and it
            // cannot be executed (since we passed the previous statement) - we have
            // no previous component
            if (component.isPointOfNoReturn()) {
                return null;
            }
        }
        
        // if we reached the before-first index and yet could not find a previous
        // component, then there is no previous component
        return null;
    }
    
    private WizardComponent getNext() {
        for (int i = index + 1; i < components.size(); i++) {
            final WizardComponent component = components.get(i);
            
            // if the component can be executed forward it is the next one
            if (component.canExecuteForward()) {
                return component;
            }
        }
        
        // if we reached the after-last index and yet could not find a next
        // component, then there is no next component
        return null;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String COMPONENTS_INSTANCE_URI_PROPERTY =
            "nbi.wizard.components.instance.uri"; // NOI18N
    
    public static final String DEFAULT_COMPONENTS_INSTANCE_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX +
            "org/netbeans/installer/wizard/wizard-components.xml"; // NOI18N
    
    public static final String COMPONENTS_SCHEMA_URI_PROPERTY =
            "nbi.wizard.components.schema.uri"; // NOI18N
    
    public static final String DEFAULT_COMPONENTS_SCHEMA_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX +
            "org/netbeans/installer/wizard/wizard-components.xsd"; // NOI18N
    
    public static final String COMPONENT_TAG_NAME =
            "component";
    
    public static final String CLASS_ATRIBUTE_NAME =
            "class";
    
    public static final String COMPONENTS_TAG_NAME =
            "components";
    
    public static final String PROPERTIES_TAG_NAME =
            "properties";
    
    public static final String FAILED_TO_CREATE_INSTANCE_PROPERTY = 
            "W.error.failed.to.create.instance";
    
    public static final String FAILED_TO_LOAD_COMPONENTS_PROPERTY = 
            "W.error.failed.to.load.components";
    
    public static final String FAILED_TO_LOAD_COMPONENT_PROPERTY = 
            "W.error.failed.to.load.component";
    
    public static final String FAILED_TO_ATTACH_ERROR_HANDLER_PROPERTY = 
            "W.error.failed.to.attach.error.handler";
    
    public static final String UNKNOWN_UI_MODE_PROPERTY = 
            "W.error.unknown.ui.mode";
    
    public static final String CANNOT_MOVE_BACKWARD_SILENT_PROPERTY = 
            "W.error.cannot.move.backward.silent";
    
    public static final String CANNOT_MOVE_BACKWARD_AT_FIRST_PROPERTY = 
            "W.error.cannot.move.backward.at.first";
}
