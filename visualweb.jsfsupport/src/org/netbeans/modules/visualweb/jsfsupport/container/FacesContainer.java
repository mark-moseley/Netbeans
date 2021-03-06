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
package org.netbeans.modules.visualweb.jsfsupport.container;

import java.io.IOException;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.faces.FactoryFinder;
//import javax.portlet.PortletContext;
import javax.servlet.ServletContextEvent;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.ErrorManager;


import com.sun.rave.designtime.DesignContext;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.visualweb.jsfsupport.render.RaveRenderKit;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.xml.sax.SAXException;

/**
 * FacesContainer provides a "mock" web/servlet container environment for hosting the design of a
 * JSF application
 *
 * @author Robert Brewin, Carl Quinn, Tor Norbye
 * @author Winston Prakash - Modifications to support JSF 1.2
 * @version 1.0
 */
public class FacesContainer {

    protected static boolean DISABLE_RESET_APPLICATION_MAP = false;
    

    static {
        String string = System.getProperty("rave.disableResetApplicationMap");
        if (string != null) {
            Boolean bool = Boolean.valueOf(string);
            DISABLE_RESET_APPLICATION_MAP = bool.booleanValue();
        }
    }

    // State variables
    private boolean portletContainer;
    private RaveServletContext context;
    private RaveServletConfig config;
    private RaveConfigureListener configureListener;
    private RaveFacesContext facesContext;
    private UIViewRoot defViewRoot;
    private ClassLoader loader;

    // XXX #6460001.
    private static final String SYS_PROP_SAX_PARSER_FACTORY = "javax.xml.parsers.SAXParserFactory"; // NOI18N
    private static final String SYS_PROP_DOM_PARSER_FACTORY = "javax.xml.parsers.DocumentBuilderFactory"; // NO18N
    private static final String SAX_PARSER_FACTORY = "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl"; // NOI18N
    private static final String DOM_PARSER_FACTORY = "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl"; // NOI18N
    private static final String NB_STARTUP_SAX_FACTORY = "org.netbeans.core.startup.SAXFactoryImpl"; // NOI18N
    private static final String NB_STARTUP_DOM_FACTORY = "org.netbeans.core.startup.DOMFactoryImpl"; // NOI18N

    /**
     * Constructor for the <em>FacesContainer</em>. Only one such object will exist at any given
     * time for a project. Perform the initialization of the mock runtime container (server) as well
     * as any state variables required
     */
    public FacesContainer(ClassLoader cl, boolean isPortletContainer) {
        // XXX #6460001 Hack. There could be still the NB startup factories, which would cause problem with our context class loader.
        String origSaxProperty = System.getProperty(SYS_PROP_SAX_PARSER_FACTORY);
        String origDomProperty = System.getProperty(SYS_PROP_DOM_PARSER_FACTORY);
        System.setProperty(SYS_PROP_SAX_PARSER_FACTORY, SAX_PARSER_FACTORY);
        System.setProperty(SYS_PROP_DOM_PARSER_FACTORY, DOM_PARSER_FACTORY);
        try {
            //Trace.enableTraceCategory("jsfsupport.container");
            ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(cl);
                this.portletContainer = isPortletContainer;
                initialize(cl);
            } finally {
                Thread.currentThread().setContextClassLoader(oldContextClassLoader);
            }
        } finally {
            // XXX #6460001. Hack. By this time the startup shouldn't be getting back, otherwise it would cause issues later.
            if (!NB_STARTUP_SAX_FACTORY.equals(origSaxProperty)) {
                if (origSaxProperty != null) {
                    System.setProperty(SYS_PROP_SAX_PARSER_FACTORY, origSaxProperty);
                }
            }
            if (!NB_STARTUP_DOM_FACTORY.equals(origDomProperty)) {
                if (origDomProperty != null) {
                    System.setProperty(SYS_PROP_DOM_PARSER_FACTORY, origDomProperty);
                }
            }
        }
    }

    /**
     * Initilize or reinitialize this environment with a new class loader
     *
     * @param cl
     */
    public void initialize(ClassLoader cl) {
        this.loader = cl;

        // Initialize the mock ServletContext
        // This is not right when isPortlet is true, but too many things depend
        // on servlet context (see the configure listeners and such) so at designtime
        // we provide a servlet context too. Note however that the external context
        // is initialized with the portlet context only
        context = new RaveServletContext();

        // Initialize the mock ServletConfig object for this context
        config = new RaveServletConfig(context);
        try {
            Class klass = Class.forName("org.netbeans.modules.visualweb.jsfsupport.container.RaveConfigureListener", true, this.getClass().getClassLoader());
            configureListener = (RaveConfigureListener) klass.newInstance();

        } catch (Throwable e) {
            ErrorManager.getDefault().notify(e);
        }
        //configureListener = new RaveConfigureListener();

        // Initialize the Servlet itself
        try {
            new RaveServlet(config);
        } catch (javax.servlet.ServletException ex) {
            // Big, Bad Mojo ... TODO: recover gracefully
            System.err.println("Failed to create Servlet"); //NOI18N
        }

        ServletContextEvent e = new ServletContextEvent(context);

        configureListener.contextInitialized(e);

        // We no longer support portlet
//        if (portletContainer) {
//             
//            facesContext = new RaveFacesContext(new RaveExternalContext(new RavePortletContext()));
//        } else {
//            // Initialize the Rave FacesContext object
//            facesContext = new RaveFacesContext(new RaveExternalContext(context));
//        }
        facesContext = new RaveFacesContext(new RaveExternalContext(context));
        facesContext.unsetCurrentInstance();
        defViewRoot = newViewRoot();
        facesContext.setViewRoot(defViewRoot);  // stub viewRoot to satisfy some components
        facesContext.setCurrentInstance();
        facesContext.setServletContext(context);

        // Wrap the default run-time render kit with a design-time render kit, which
        // knows about design-time wrappers for renderers.
        RenderKitFactory factory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        String id = facesContext.getViewRoot().getRenderKitId();
        RenderKit renderKit = factory.getRenderKit(facesContext, id);
        if (!(renderKit instanceof RaveRenderKit)) {
            factory.addRenderKit(id, new RaveRenderKit(renderKit));
        }

        // Initialize the JsfTagSupport URI/TLD-FacesConfig map
        JsfTagSupport.initialize(loader);
    }

    /**
     * Destroy this environment, clearing out references to other resources
     */
    public void destroy() {
        facesContext.setDesignContext(null);
        // NPE from component base renderer.
        // Fix that first, before this memory leak problem
        //facesContext.release();
    }

    /**
     * @return the 'mock' <em>FacesContext</em> instance, making sure it is 'current' first
     */
    public RaveFacesContext getFacesContext() {
        facesContext.setCurrentInstance();
        return facesContext;
    }

    /**
     * Return whether this container is a portlet container. If false, it's a servlet container.
     * @return True iff this container is a portlet container
     */
    public boolean isPortletContainer() {
        return portletContainer;
    }

    /**
     * @return
     */
    public UIViewRoot newViewRoot() {
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            UIViewRoot viewRoot = new UIViewRoot();
            viewRoot.setViewId("/rave");     // TODO: Should this be something real ?
            String renderKitId = null;
            Application application = null;
            application = facesContext.getApplication();
            if (application != null) {
                renderKitId = application.getDefaultRenderKitId();
            }
            if (renderKitId == null) {
                renderKitId = RenderKitFactory.HTML_BASIC_RENDER_KIT;
            }
            viewRoot.setRenderKitId(renderKitId);
            return viewRoot;
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
        return null;
    }

    /**
     * Prepare our contexts to render UIComponent(s) from a given DesignContext, and return a usable
     * writer.
     * @param lc
     * @param frag
     * @param uiform
     * @return
     */
//    public DocFragmentJspWriter beginRender(DesignContext lc, UIViewRoot viewRoot, DocumentFragment frag) {
    public void beginRender(DesignContext lc, UIViewRoot viewRoot, ResponseWriter responseWriter) {
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);

            facesContext.setCurrentInstance();  // make sure the context is available to components via thread lookup
            facesContext.setDesignContext(lc);  //!CQ HACK? to have to point its state to each lc all the time
            facesContext.setViewRoot(viewRoot);  // the view root for the component tree to be rendered
            // TODO: We need to see if we may want to only change app scope on design context being from a different project
            if (!DISABLE_RESET_APPLICATION_MAP) {
                facesContext.getExternalContext().getApplicationMap().clear();
            }
            facesContext.getExternalContext().getSessionMap().clear();
            facesContext.getExternalContext().getRequestMap().clear();

//          DocFragmentJspWriter rw = new DocFragmentJspWriter(this, frag);
//          facesContext.setResponseWriter(rw);
            facesContext.setResponseWriter(responseWriter);

            Map requestMap = facesContext.getExternalContext().getRequestMap();
            requestMap.put("com.sun.faces.FormNumber", new Integer(0));
            requestMap.put("com.sun.faces.INVOCATION_PATH", "/rave");
//
//          return rw;
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
    }

    /**
     * Finish up a component rendering run.
     * @param rw
     */
//    public void endRender(DocFragmentJspWriter rw) {
    public void endRender(ResponseWriter responseWriter) {
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            try {
//                rw.flush();
                responseWriter.flush();
                facesContext.setViewRoot(defViewRoot);  // back to the empty default one
/* TODONOW
                 * We need to fix issue where we do not set design context properly, we assume it was
                 * set by something else, this is not good.  Reverting it back for now in order for me to
                 * be able to commit and have sanity pass.  Will work on issue with Deva, Tor, Craig.
                 */
//                facesContext.setDesignContext(null);
                facesContext.setResponseWriter(null);
                facesContext.setResponseStream(null);
            } catch (Exception e) {
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
    }

    /**
     * Set the ClassLoader associated with this container
     * @param loader The ClassLoader to be used for loading resources
     */
    public void setClassLoader(ClassLoader loader) {
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.loader);

            // Cleanup.
            if (facesContext != null) {
                facesContext.resetApplication();
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }

        // set the loader
        this.loader = loader;

        oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            // initilize the container
            initialize(loader);
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
    }

    public String findComponentClass(String tagName, String taglibUri) throws JsfTagSupportException {
        String errorMessage = org.openide.util.NbBundle.getMessage(FacesContainer.class, "JSF_COMPONENT_NOT_FOUND", new Object[]{tagName, taglibUri});
        try {
            return JsfTagSupport.getInstance(taglibUri).getComponentClass(loader, tagName);
        } catch (Exception ex) {
            throw new JsfTagSupportException(errorMessage, ex);
        }
    }
}
