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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.editing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.ParserFile;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.netbeans.modules.gsf.spi.DefaultParserFile;
import org.netbeans.modules.javascript.editing.lexer.JsTokenId;
import org.openide.filesystems.FileObject;

/**
 *
 * @author tor
 */
class TestCompilationInfo extends GsfTestCompilationInfo {
    public TestCompilationInfo(JsTestBase test, FileObject fileObject, BaseDocument doc, String text) throws IOException {
        super(test, fileObject, doc, text);
    }
    
    @Override
    public ParserResult getEmbeddedResult(String embeddedMimeType, int offset) {
        assert embeddedMimeType.equals(JsTokenId.JAVASCRIPT_MIME_TYPE);
        
        if (embeddedResults.size() == 0) {
            GsfTestParseListener listener = new GsfTestParseListener();
            List<ParserFile> sourceFiles = new ArrayList<ParserFile>(1);
            ParserFile file = new DefaultParserFile(getFileObject(), null, false);
            sourceFiles.add(file);
            
TranslatedSource translatedSource = null; // TODO            
            JsParser.Context context = new JsParser.Context(file, listener, text, caretOffset, translatedSource);
            JsParser parser = new JsParser();
            JsParser.runtimeException = null;
            ParserResult parserResult = parser.parseBuffer(context, JsParser.Sanitize.NONE);
            JsTestBase.assertNull(JsParser.runtimeException != null ? JsParser.runtimeException.toString() : "", JsParser.runtimeException);
            for (Error error : listener.getErrors()) {
                parserResult.addError(error);
            }
            embeddedResults.put(JsTokenId.JAVASCRIPT_MIME_TYPE, parserResult);
            parserResult.setInfo(this);
        }
        
        return embeddedResults.get(embeddedMimeType);
    }

    public String getPreferredMimeType() {
        return JsTokenId.JAVASCRIPT_MIME_TYPE;
    }
}
