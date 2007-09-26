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
 * Software is Sun Microsystems, Inc.
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.openide.loaders;

import java.io.IOException;
import java.util.Map;
import org.openide.filesystems.FileObject;

/** This is an interface for <q>smart templating</q> that allows
 * any module to intercept calls to {@link DataObject#createFromTemplate} 
 * and handle them themselves. The NetBeans IDE provides default
 * implementation that allows use of Freemarker templating engine.
 * Read more in the <a href="@TOP@/architecture-summary.html#script">howto document</a>.
 *
 * @author Jaroslav Tulach
 * @since 6.1
 */
public abstract class CreateFromTemplateHandler {
    /** Method that allows a handler to reject a file. If all handlers
     * reject a file, regular processing defined in {@link DataObject#handleCreateFromTemplate}
     * is going to take place.
     * 
     * @param orig the file of the template
     * @return true if this handler wants to handle the createFromTemplate operation
     */
    protected abstract boolean accept(FileObject orig);
    
    /** Handles the creation of new file. 
     * @param orig the source file 
     * @param f the folder to create a file in
     * @param name the name of new file to create in the folder
     * @param parameters map of additional arguments as specified by registered {@link CreateFromTemplateAttributesProvider}s
     * @return the newly create file
     * @throws IOException if something goes wrong with I/O
     */
    protected abstract FileObject createFromTemplate(
        FileObject orig,
        FileObject f, 
        String name,
        Map<String,Object> parameters
    ) throws IOException;
    
}
