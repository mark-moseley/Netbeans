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

package org.netbeans.modules.web.api.webmodule;

import java.util.Iterator;
import org.netbeans.modules.web.webmodule.WebModuleAccessor;
import org.netbeans.modules.web.spi.webmodule.*;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/** WebModule should be used to access contens and properties of web module.
 * <p>
 * A client may obtain a WebModule instance using 
 * <code>WebModule.getWebModule(fileObject)</code> static method, for any 
 * FileObject in the web module directory structure.
 * </p>
 * <div class="nonnormative">
 * <p>
 * Use classpath API to obtain classpath for the document base.
 * <p>
 * Note that the particular directory structure for web module is not guaranteed 
 * by this API.
 * </div>
 *
 * @author  Pavel Buzek
 */
public final class WebModule {
    
    public static final String J2EE_13_LEVEL = "1.3"; //NOI18N
    public static final String J2EE_14_LEVEL = "1.4"; //NOI18N
    
    private WebModuleImplementation impl;
    private static final Lookup.Result implementations =
        Lookup.getDefault().lookup(new Lookup.Template(WebModuleProvider.class));
    
    static  {
        WebModuleAccessor.DEFAULT = new WebModuleAccessor() {
            public WebModule createWebModule(WebModuleImplementation spiWebmodule) {
                return new WebModule(spiWebmodule);
            }

            public WebModuleImplementation getWebModuleImplementation(WebModule wm) {
                return wm == null ? null : wm.impl;
            }
        };
    }
    
    private WebModule (WebModuleImplementation impl) {
        if (impl == null)
            throw new IllegalArgumentException ();
        this.impl = impl;
    }
    
    /** Find the WebModule for given file or null if the file does not belong
     * to any web module.
     */
    public static WebModule getWebModule (FileObject f) {
        if (f == null) {
            throw new IllegalArgumentException ();
        }
        Iterator it = implementations.allInstances().iterator();
        while (it.hasNext()) {
            WebModuleProvider impl = (WebModuleProvider)it.next();
            WebModule wm = impl.findWebModule (f);
            if (wm != null) {
                return wm;
            }
        }
        return null;
    }

    /** Folder that contains sources of the static documents for 
     * the web module (html, JSPs, etc.).
     */
    public FileObject getDocumentBase () {
        return impl.getDocumentBase ();
    }
    
    /** WEB-INF folder for the web module.
     * <div class="nonnormative">
     * The WEB-INF folder would typically be a child of the folder returned 
     * by {@link #getDocumentBase} but does not need to be.
     * </div>
     */
    public FileObject getWebInf () {
        return impl.getWebInf ();
    }

    /** Deployment descriptor (web.xml file) of the web module.
     * <div class="nonnormative">
     * The web.xml file would typically be a child of the folder returned 
     * by {@link #getWebInf} but does not need to be.
     * </div>
     */
    public FileObject getDeploymentDescriptor () {
        return impl.getDeploymentDescriptor ();
    }
    
    /** Context path of the web module.
     */
    public String getContextPath () {
        return impl.getContextPath ();
    }
    
    /** J2EE platform version - one of the constants {@link #J2EE_13_LEVEL}, 
     * {@link #J2EE_14_LEVEL}.
     * @return J2EE platform version
     */
    public String getJ2eePlatformVersion () {
        return impl.getJ2eePlatformVersion ();
    }
    
    /** Returns true if the object represents the same web module.
     */
    public boolean equals (Object obj) {
        if (!WebModule.class.isAssignableFrom(obj.getClass()))
            return false;
        WebModule wm = (WebModule) obj;
        return getDocumentBase().equals(wm.getDocumentBase())
            && getJ2eePlatformVersion().equals (wm.getJ2eePlatformVersion())
            && getContextPath().equals(wm.getContextPath());
    }
}