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

package org.netbeans.lib.lexer.inc;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.LanguageDescription;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.lib.lexer.LanguageManager;
import org.netbeans.spi.lexer.*;

/**
 * Control structure for managing of the lexer for a given document.
 * <br>
 * There is one structure for a document. It can be obtained by
 * {@link #get(Document)}.
 * <br>
 * Each document that wants to use the lexer framework
 * must be initialized by using {@link #init(Document, boolean, Object)}.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class DocumentInput<D extends Document>
extends MutableTextInput<D> implements DocumentListener {

    private static final String PROP_MIME_TYPE = "mimeType"; //NOI18N
    
    public static <D extends Document> DocumentInput<D> get(D doc) {
        @SuppressWarnings("unchecked")
        DocumentInput<D> di = (DocumentInput<D>)doc.getProperty(DocumentInput.class);
        if (di == null) {
            di = new DocumentInput<D>(doc);
            doc.putProperty(DocumentInput.class, di);
        }
        return di;
    }
    
    private D doc;
    
    private LanguageHierarchy languageHierarchy;
    
    private CharSequence text;
    
    public DocumentInput(D doc) {
        this.doc = doc;
        this.text = DocumentUtilities.getText(doc);
        
        doc.addDocumentListener(this);
        
    }
    
    public LanguageDescription<? extends TokenId> language() {
        LanguageDescription<? extends TokenId> lang = (LanguageDescription<? extends TokenId>)
                doc.getProperty(LanguageDescription.class);
        
        if (lang == null) {
            String mimeType = (String) doc.getProperty(PROP_MIME_TYPE);
            lang = LanguageManager.getInstance().findLanguage(mimeType);
        }
        
        return lang;
    }
    
    public CharSequence text() {
        return text;
    }
    
    public InputAttributes inputAttributes() {
        return (InputAttributes)doc.getProperty(InputAttributes.class);
    }

    public D inputSource() {
        return doc;
    }
    
    public void changedUpdate(DocumentEvent e) {
    }

    public void insertUpdate(DocumentEvent e) {
        modified(true, e);
    }

    public void removeUpdate(DocumentEvent e) {
        modified(false, e);
    }

    private void modified(boolean insert, DocumentEvent e) {
        int offset = e.getOffset();
        int length = e.getLength();
        if (insert) {
            tokenHierarchyControl().textModified(offset, 0, null, length);
        } else {
            tokenHierarchyControl().textModified(offset, length,
                    DocumentUtilities.getModificationText(e), 0);
        }
    }

}
