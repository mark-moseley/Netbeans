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
package org.netbeans.modules.websvc.project.spi;

import java.net.URI;
import java.net.URL;

/**
 * SPI for {@link org.netbeans.modules.websvc.project.api.ServiceDescriptor} that is implemented by web service stack providersn. This contains
 * information about the location of the descriptor file that describes the web service The descriptor file may be deployed or a local project copy.
 * @see WebServiceFactory
 * @author mkuchtiak
 */
public interface ServiceDescriptorImplementation {
    
    /**
     * Returns an identifier for the service provider or consumer
     */
    String getIdentifier();

    /**
     * Returns the location of the deployed descriptor, if any.
     * @return URL of the deployed descriptor artifact or null.
     */
    URL getRuntimeLocation();

    /**
     * Returns the location of the descriptor in the project, if any. If this descriptor exists, the URI should be relative to the project
     * directory's location.
     * @return URI of the descriptor's location in the project or null.
     */
    URI getRelativeURI();
}
