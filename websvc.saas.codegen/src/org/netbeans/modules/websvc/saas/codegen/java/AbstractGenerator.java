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

package org.netbeans.modules.websvc.saas.codegen.java;

import java.io.IOException;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Peter Liu
 */
public abstract class AbstractGenerator {
    public static final String REST_CONNECTION = "RestConnection"; //NOI18N
    public static final String REST_CONNECTION_TEMPLATE = "Templates/SaaSServices/RestConnection.java"; //NOI18N
    public static final String COMMENT_END_OF_HTTP_MEHTOD_GET = "TODO return proper representation object";      //NOI18N
    public static final String GENERIC_REF_CONVERTER_TEMPLATE = "Templates/SaaSServices/RefConverter.java"; //NOI18N
    public static final String GENERIC_REF_CONVERTER = "GenericRefConverter"; //NOI18N
    public static final String CONVERTER_SUFFIX = "Converter";      //NOI18N
    public static final String CONVERTER_FOLDER = "converter";      //NOI18N
    public static final String RESOURCE_SUFFIX = "Resource";      //NOI18N
    
    private ProgressHandle pHandle;
    private int totalWorkUnits;
    private int workUnits;
    
    public AbstractGenerator() {
    }
    
    public abstract Set<FileObject> generate(ProgressHandle pHandle) throws IOException;
    
    protected void initProgressReporting(ProgressHandle pHandle) {
        initProgressReporting(pHandle, true);
    }
    
    protected void initProgressReporting(ProgressHandle pHandle, boolean start) {
        this.pHandle = pHandle;
        this.totalWorkUnits = getTotalWorkUnits();
        this.workUnits = 0;
        
        if (start) {
            if (totalWorkUnits > 0) {
                pHandle.start(totalWorkUnits);
            } else {
                pHandle.start();
            }
        }
    }
    
    protected void reportProgress(String message) {     
        if (pHandle != null) {
            if (totalWorkUnits > 0) {
                pHandle.progress(message, ++workUnits);
            } else {
                pHandle.progress(message);
            }
        }
    }
    
    protected void finishProgressReporting() {
        if (pHandle != null) {
            pHandle.finish();
        }
    }
    
    protected int getTotalWorkUnits() {
        return 0;
    }
    
    protected ProgressHandle getProgressHandle() {
        return pHandle;
    }
}
