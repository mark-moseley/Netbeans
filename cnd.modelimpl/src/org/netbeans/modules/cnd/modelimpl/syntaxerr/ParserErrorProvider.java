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
package org.netbeans.modules.cnd.modelimpl.syntaxerr;

import org.netbeans.modules.cnd.modelimpl.syntaxerr.spi.ParserErrorFilter;
import antlr.RecognitionException;
import java.util.ArrayList;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import java.util.Collection;
import java.util.Iterator;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorInfo;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.modelimpl.syntaxerr.spi.ReadOnlyTokenBuffer;

/**
 * Error provider based on parser errors
 * @author Vladimir Kvashin
 */
public class ParserErrorProvider extends CsmErrorProvider {

    private static final boolean ENABLE = getBoolean("cnd.parser.error.provider", true);

    @Override
    protected boolean validate(Request request) {
        return ENABLE && super.validate(request) && !disableAsLibraryHeaderFile(request.getFile());
    }

    @Override
    protected  void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
        Collection<CsmErrorInfo> errorInfos = new ArrayList<CsmErrorInfo>();
        Collection<RecognitionException> recognitionExceptions = new ArrayList<RecognitionException>();
        ReadOnlyTokenBuffer buffer = ((FileImpl) request.getFile()).getErrors(recognitionExceptions);
        if (buffer != null) {
            ParserErrorFilter.getDefault().filter(recognitionExceptions, errorInfos, buffer, request.getFile());
            for (Iterator<CsmErrorInfo> iter = errorInfos.iterator(); iter.hasNext() && ! request.isCancelled(); ) {
                response.addError(iter.next());
            }
        }
    }

    private static boolean getBoolean(String name, boolean result) {
        String text = System.getProperty(name);
        if (text != null) {
            result = Boolean.parseBoolean(text);
        }
        return result;
    }

    public String getName() {
        return "syntax-error"; //NOI18N
    }


}
