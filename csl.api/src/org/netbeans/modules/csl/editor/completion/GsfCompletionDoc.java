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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.csl.editor.completion;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.core.LanguageRegistry;
import org.netbeans.napi.gsfret.source.CompilationController;
import org.netbeans.napi.gsfret.source.UiUtils;
import org.netbeans.modules.csl.core.Language;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;


/**
 * Produce completion-popup help for elements
 * 
 * @author Tor Norbye
 */
public class GsfCompletionDoc implements CompletionDocumentation {
    private String content = null;
    private URL docURL = null;
    private AbstractAction goToSource = null;
    private ElementHandle elementHandle;
    private Language language;
    private CompilationController controller;

    private GsfCompletionDoc(final CompilationController controller, final ElementHandle elementHandle,
        URL url) {
        this.controller = controller;
        this.language = controller.getLanguage();
        if (elementHandle != null && elementHandle.getMimeType() != null) {
            Language embeddedLanguage = LanguageRegistry.getInstance().getLanguageByMimeType(elementHandle.getMimeType());
            if (embeddedLanguage != null && embeddedLanguage.getParser() != null) {
                language = embeddedLanguage;
            }
        }

        CodeCompletionHandler completer = language.getCompletionProvider();

        this.elementHandle = elementHandle;

        if (elementHandle != null) {
            goToSource =
                new AbstractAction() {
                        public void actionPerformed(ActionEvent evt) {
                            Completion.get().hideAll();
                            UiUtils.open(controller.getSource(), elementHandle);
                        }
                    };
            if (url != null) {
                docURL = url;
            } else {
                docURL = null;
            }
        }

        if (completer != null) {
            this.content = completer.document(controller, elementHandle);
        }

        if (this.content == null) {
            Completion.get().hideDocumentation();
        }
    }

    public static final GsfCompletionDoc create(CompilationController controller,
        ElementHandle elementHandle) {
        return new GsfCompletionDoc(controller, elementHandle, null);
    }

    public String getText() {
        return content;
    }

    public URL getURL() {
        return docURL;
    }

    public CompletionDocumentation resolveLink(String link) {
        if (link.startsWith("www.")) {
            link = "http://" + link;
        }
        if (link.matches("[a-z]+://.*")) { // NOI18N
            try {
                URL url = new URL(link);
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
                return null;
            } catch (MalformedURLException mue) {
                Exceptions.printStackTrace(mue);
            }
        }
        
        ElementHandle handle = language.getCompletionProvider().resolveLink(link, elementHandle);
        if (handle != null) {
            URL url = null;
            if(handle instanceof ElementHandle.UrlHandle) {
                String url_text = ((ElementHandle.UrlHandle)handle).getUrl();
                try {
                    url = new URL(url_text);
                } catch (MalformedURLException mue) {
                    Logger.getLogger("global").log(Level.INFO, null, mue);
                }
            }
            
            return new GsfCompletionDoc(controller, handle, url);
        }
        return null;
    }

    public Action getGotoSourceAction() {
        return goToSource;
    }
}
