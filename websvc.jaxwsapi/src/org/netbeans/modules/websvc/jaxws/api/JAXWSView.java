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

package org.netbeans.modules.websvc.jaxws.api;

import java.util.Iterator;
import org.netbeans.modules.websvc.jaxws.JAXWSViewAccessor;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSViewProvider;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSViewImpl;
import org.openide.util.Lookup;
import org.openide.nodes.Node;
import org.netbeans.api.project.Project;

/** WebServicesView should be used to retrieve information and display objects
 *  for the webservices in a project.
 * <p>
 * A client may obtain a WebServicesView instance using 
 * <code>WebServicesView.getWebServicesView(fileObject)</code> static 
 * method, for any FileObject in the project directory structure.
 *
 * @author Peter Williams
 */
public final class JAXWSView {
    
    private JAXWSViewImpl impl;
    private static final Lookup.Result implementations =
        Lookup.getDefault().lookup(new Lookup.Template(JAXWSViewProvider.class));
    
    static  {
        JAXWSViewAccessor.DEFAULT = new JAXWSViewAccessor() {
            public JAXWSView createJAXWSView(JAXWSViewImpl spiWebServicesView) {
                return new JAXWSView(spiWebServicesView);
            }

            public JAXWSViewImpl getJAXWSViewImpl(JAXWSView wsv) {
                return wsv == null ? null : wsv.impl;
            }
        };
    }
    
    private JAXWSView(JAXWSViewImpl impl) {
        if (impl == null)
            throw new IllegalArgumentException ();
        this.impl = impl;
    }
    
    /** Find the JAXWSView for given file or null if the file does not belong
     * to any module support web services.
     */
    public static JAXWSView getJAXWSView() {
       Iterator it = implementations.allInstances().iterator();
       while (it.hasNext()) {
          JAXWSViewProvider impl = (JAXWSViewProvider)it.next();
          JAXWSView wsv = impl.findJAXWSView ();
          if (wsv != null) {
            return wsv;
          }
       }
        return null;
    }

    // Delegated methods from JAXWSViewImpl	
    public Node createJAXWSView(Project project) {
        return impl.createJAXWSView(project);
    }

}
