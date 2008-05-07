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

package org.netbeans.modules.languages.features;

import javax.swing.text.Document;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.spi.editor.errorstripe.UpToDateStatus;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProvider;
import org.netbeans.spi.editor.errorstripe.UpToDateStatusProviderFactory;


/**
 *
 * @author Jan Jancura
 */
public class UpToDateStatusProviderFactoryImpl implements UpToDateStatusProviderFactory {
    
    /** Creates a new instance of UpToDateStatusProvider */
    public UpToDateStatusProviderFactoryImpl () {
    }

    public UpToDateStatusProvider createUpToDateStatusProvider (Document document) {
        return new UpToDateStatusProviderImpl ((NbEditorDocument) document);
    }
    
    private static class UpToDateStatusProviderImpl extends UpToDateStatusProvider {
        
//!        private ParserManager editorParser;
        
        
        private UpToDateStatusProviderImpl (NbEditorDocument doc) {
//!            editorParser = ParserManager.get (doc);
//            editorParser.addListener (new ParserManagerListener () {
//                public void parsed (State state, ASTNode ast) {
//                    firePropertyChange (PROP_UP_TO_DATE, null, null);
//                }
//            });
        }
        
        public UpToDateStatus getUpToDate () {
//!            switch (editorParser.getState ()) {
//                case ERROR:
//                    return UpToDateStatus.UP_TO_DATE_DIRTY;
//                case OK:
//                    return UpToDateStatus.UP_TO_DATE_OK;
//                case PARSING:
//                    return UpToDateStatus.UP_TO_DATE_PROCESSING;
//            }
            return UpToDateStatus.UP_TO_DATE_PROCESSING;
        }
    }
}


