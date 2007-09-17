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

package org.netbeans.modules.web.api.webmodule;

import java.util.Iterator;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.webmodule.WebModuleAccessor;
import org.netbeans.modules.web.spi.webmodule.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * This class encapsulates a web module.
 * 
 * <p>A client may obtain a <code>WebModule</code> instance using
 * method {@link #getWebModule}, for any
 * {@link org.openide.filesystems.FileObject} in the web module directory structure.</p>
 * <div class="nonnormative">
 * <p>Use the classpath API to obtain the classpath for the document base (this classpath
 * is used for code completion of JSPs). An example:</p>
 * <pre>
 *     WebModule wm = ...;
 *     FileObject docRoot = wm.getDocumentBase ();
 *     ClassPath cp = ClassPath.getClassPath(docRoot, ClassPath.EXECUTE);
 * </pre>
 * <p>Note that no particular directory structure for web module is guaranteed 
 * by this API.</p>
 * </div>
 *
 * @author  Pavel Buzek
 */
public final class WebModule {
    
    public static final String J2EE_13_LEVEL = "1.3"; //NOI18N
    public static final String J2EE_14_LEVEL = "1.4"; //NOI18N
    public static final String JAVA_EE_5_LEVEL = "1.5"; //NOI18N
    
    private final WebModuleImplementation impl;
    private static final Lookup.Result implementations =
            Lookup.getDefault().lookupResult(WebModuleProvider.class);
    
    static  {
        WebModuleAccessor.DEFAULT = new WebModuleAccessor() {
            public WebModule createWebModule(WebModuleImplementation spiWebmodule) {
                return new WebModule(spiWebmodule);
            }
        };
    }
    
    private WebModule (WebModuleImplementation impl) {
        Parameters.notNull("impl", impl); // NOI18N
        this.impl = impl;
    }
    
    /**
     * Finds the web module for the given file.
     *
     * @param  file the file to find the web module for.
     * @return the web module this file belongs to or null if the file does not belong
     *         to any web module.
     * @throws NullPointerException if the <code>file</code> parameter is null.
     */
    public static WebModule getWebModule (FileObject file) {
        Parameters.notNull("file", file); // NOI18N
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            WebModuleProvider impl = (WebModuleProvider)it.next();
            WebModule wm = impl.findWebModule (file);
            if (wm != null) {
                return wm;
            }
        }
        return null;
    }

    /**
     * Returns the folder that contains sources of the static documents for
     * the web module (html, JSPs, etc.).
     *
     * @return the static documents folder; can be null.
     */
    public FileObject getDocumentBase () {
        return impl.getDocumentBase ();
    }
    
    /**
     * Returns the WEB-INF folder for the web module.
     * It may return null for web module that does not have any WEB-INF folder.
     * <div class="nonnormative">
     * <p>The WEB-INF folder would typically be a child of the folder returned
     * by {@link #getDocumentBase} but does not need to be.</p>
     * </div>
     *
     * @return the WEB-INF folder; can be null.
     */
    public FileObject getWebInf () {
        return impl.getWebInf ();
    }

    /**
     * Returns the deployment descriptor (<code>web.xml</code> file) of the web module.
     * <div class="nonnormative">
     * The web.xml file would typically be a child of the folder returned
     * by {@link #getWebInf} but does not need to be.
     * </div>
     *
     * @return the <code>web.xml</code> file; can be null.
     */
    public FileObject getDeploymentDescriptor () {
        return impl.getDeploymentDescriptor ();
    }
    
    /**
     * Returns the context path of the web module.
     *
     * @return the context path; can be null.
     */
    public String getContextPath () {
        return impl.getContextPath ();
    }
    
    /**
     * Returns the J2EE platform version of this module. The returned value is
     * one of the constants {@link #J2EE_13_LEVEL}, {@link #J2EE_14_LEVEL} or 
     * {@link #JAVA_EE_5_LEVEL}.
     *
     * @return J2EE platform version; never null.
     */
    public String getJ2eePlatformVersion () {
        return impl.getJ2eePlatformVersion ();
    }
    
    /**
     * Returns the Java source roots associated with the web module.
     * <div class="nonnormative">
     * <p>Note that not all the java source roots in the project (e.g. in a freeform project)
     * belong to the web module.</p>
     * </div>
     *
     * @return this web module's Java source roots; never null.
     *
     * @deprecated This method is deprecated, because its return values does
     * not contain enough information about the source roots. Source roots
     * are usually implemented by a <code>org.netbeans.api.project.SourceGroup</code>,
     * which is more than just a container for a {@link org.openide.filesystems.FileObject}.
     */
    @Deprecated
    public FileObject[] getJavaSources() {
        return impl.getJavaSources();
    }
    
    /**
     * Returns a model describing the metadata of this web module (servlets,
     * resources, etc.).
     *
     * @return this web module's metadata model; never null.
     */
    public MetadataModel<WebAppMetadata> getMetadataModel() {
        return impl.getMetadataModel();
    }
    
    @Override
    public boolean equals (Object obj) {
        if (obj == null) {
            return false;
        }
        if (!WebModule.class.isAssignableFrom(obj.getClass()))
            return false;
        WebModule wm = (WebModule) obj;
        return getDocumentBase().equals(wm.getDocumentBase())
            && getJ2eePlatformVersion().equals (wm.getJ2eePlatformVersion())
            && getContextPath().equals(wm.getContextPath());
    }
    
    @Override
    public int hashCode () {
        return getDocumentBase ().getPath ().length () + getContextPath ().length ();
    }
}
