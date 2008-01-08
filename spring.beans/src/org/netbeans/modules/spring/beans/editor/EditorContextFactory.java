/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.netbeans.modules.spring.beans.editor;

import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.Document;

/**
 *
 * @author Rohan Ranade
 */
public final class EditorContextFactory {
    private static Map<Document, DocumentContext> contextCache = 
            new WeakHashMap<Document, DocumentContext>();

    public static DocumentContext getDocumentContext(Document document, int caretOffset) {
        DocumentContext context = contextCache.get(document);
        if(context == null) {
            context = new DocumentContext(document);
            contextCache.put(document, context);
        }
        
        context.reset(caretOffset);
        
        return context;
    }
}