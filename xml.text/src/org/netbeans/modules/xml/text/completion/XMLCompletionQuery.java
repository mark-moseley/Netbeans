/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.text.completion;

import java.util.*;
import java.awt.Color;
import java.net.URL;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import org.w3c.dom.*;
import org.xml.sax.*;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.*;

import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.text.syntax.dom.*;
import org.netbeans.modules.xml.spi.model.*;

import org.netbeans.modules.xml.core.lib.Convertors;
import javax.swing.Icon;
import org.openide.util.Task;
import org.openide.TopManager;
import org.openide.util.RequestProcessor;

/**
 * Consults grammar and presents list of possible choices
 * in particular document context.
 * <p>
 * <b>Warning:</b> It is public for unit test purposes only!
 *
 * @author Petr Nejedly
 * @author Sandeep Randhawa
 * @author Petr Kuzel
 * @author Asgeir Orn Asgeirsson
 * @version 1.01
 */

public class XMLCompletionQuery implements CompletionQuery, XMLTokenIDs {

    // the name of a property indentifing cached query
    static final String DOCUMENT_GRAMMAR_BINDING_PROP = "doc-bind-query";
    
    // shared context instance, - we are always called from AWT thread
    private DefaultContext ctx = new DefaultContext();
    
    // document that is completed
    private BaseDocument doc;

    private XMLSyntaxSupport sup;

    // are we at token boundary or inside
    private boolean boundary;
    
    /** 
     * Perform the query on the given component. The query usually
     * gets the component's document, the caret position and searches back
     * to examine surrounding context. Then it returns the result.
     * <p>
     * It is also called after every keystroke while opened completion
     * popup. So some result cache could be used. It is not easy at
     * this level because of BACKSPACE that can extend result.
     *
     * @param component the component to use in this query.
     * @param offset position in the component's document to which the query will
     *   be performed. Usually it's a caret position.
     * @param support syntax-support that will be used during resolving of the query.
     * @return result of the query or null if there's no result.
     */
    public CompletionQuery.Result query(JTextComponent component, int offset, SyntaxSupport support) {        

        // assert precondition
        
        if (SwingUtilities.isEventDispatchThread() == false) {
            throw new IllegalStateException("Called from non-AWT thread: " + Thread.currentThread().getName());  //NOI18N
        }
        
        doc = (BaseDocument)component.getDocument();
        sup = (XMLSyntaxSupport)support.get(XMLSyntaxSupport.class);
        if( sup == null ) return null;// No SyntaxSupport for us, no hint for user
        
        try {
            TokenItem token = null;            
            
            token = sup.getPreviousToken( offset);
            if( token != null ) { // inside document
                boundary = token.getOffset() + token.getImage().length() == offset;
            } else {
                return null; //??? start of document no choice now, but should be prolog if not followed by it
            }

            // find out last typed chars that can hint
            
            int itemOffset = token.getOffset();
            String preText = "";
            int erase = 0;
            int eraseRight = 0;
            int id = token.getTokenID().getNumericID();
            
            // determine last typed text, prefix text
            
           if ( boundary == false ) {
                
                preText = token.getImage().substring( 0, offset - token.getOffset() );
                if ("".equals(preText)) throw new IllegalStateException("Cannot get token prefix at " + offset);

                // manipulate offset to delete rest of an old name
                // for cases where it iseasy to locate original name end
                
                switch (id) {

                    case XMLDefaultTokenContext.TAG_ID:
                    case XMLDefaultTokenContext.CHARACTER_ID:
                    case XMLDefaultTokenContext.ARGUMENT_ID:
                        
                        int i = token.getImage().length();
                        int tail = i - (offset - itemOffset);
                        offset += tail;
                        eraseRight = tail;
                        break;
                }
            } else {
               switch (id) {
                    case XMLDefaultTokenContext.TEXT_ID:
                    case XMLDefaultTokenContext.TAG_ID:
                    case XMLDefaultTokenContext.ARGUMENT_ID:
                    case XMLDefaultTokenContext.CHARACTER_ID:
                    case PI_CONTENT_ID:
                        preText = token.getImage();
                        break;                        
                }
            }

            // adjust how much do you want to erase from the pretext
            
            switch (id) {
                case XMLDefaultTokenContext.TAG_ID:
                case XMLDefaultTokenContext.CHARACTER_ID:
                    // do not erase start delimiters
                    erase = preText.length() - 1 + eraseRight;
                    break;

                case XMLDefaultTokenContext.ARGUMENT_ID:
                    erase = preText.length() + eraseRight;
                    break;
            }

            SyntaxElement element = sup.getElementChain( offset);
            if (element == null) throw new IllegalStateException("There exists a token therefore a syntax element must exist at " + offset + ", too.");

            // completion request originates from area covered by DOM, 
            if (element instanceof SyntaxNode) {
                List list = query((SyntaxNode) element, token, preText);
                if (list != null && list.isEmpty() == false) {
                    String debugMsg = Boolean.getBoolean("netbeans.debug.xml") ? " " + offset + "-" + erase : "";
                    String title = Util.THIS.getString("MSG_result", new Object[] {preText}) + debugMsg;
                    return new CompletionQuery.DefaultResult( component, title, list, offset - erase, erase );
                } else {
                    
                    // auto complete end tag without showing popup
                    
                    if (preText.endsWith("</") && token.getTokenID() == TEXT) { // NOI18N
                        list = findStartTag((SyntaxNode)element);
                        if (list != null && list.size() == 1) {
                            ElementResultItem item = (ElementResultItem)list.get(0);
                            item.substituteText(component, offset, 0, 0);
                        }
                    }
                    
                    return null;
                }
            } else {
                // prolog, internal DTD no completition yet
                if (token.getTokenID() == PI_CONTENT) {
                    if (preText.endsWith("encoding=")) {                        // NOI18N
                        List encodings = new ArrayList(2);
                        encodings.add(new XMLResultItem("\"UTF-8\""));          // NOI18N
                        encodings.add(new XMLResultItem("\"UTF-16\""));         // NOI18N
                        return new CompletionQuery.DefaultResult(
                            component,
                            Util.THIS.getString("MSG_encoding_comp"),
                            encodings,
                            offset,
                            0
                        );
                    }
                }
                return null; 
            }

        } catch (BadLocationException e) {
            Util.THIS.debug(e);
        }

        return null;
    }

    /**
     * Find out what to complete: attribute, value, element, entity or notation?
     * <p>
     * <pre>
     * Triggering criteria:
     *
     * ELEMENT      TOKEN (,=seq)   PRETEXT         QUERY
     * -------------------------------------------------------------------
     * Text         text            &lt;            element name
     * Text         text            &lt;/           pairing end element
     * StartTag     tag             &lt;prefix      element name
     * StartTag     ws                              attribute name
     * StartTag     attr, operator  =               quoted attribute value
     * StartTag     value           'prefix         attribute value
     * StartTag     tag             >               element value
     * Text         text            &amp;           entity ref name     
     * StartTag     value           &amp;           entity ref name
     * </pre>
     *
     * @return List of XMLResultItems
     */
    private List query(SyntaxNode element, TokenItem token, String text) {
        int id = token.getTokenID().getNumericID();
        
        switch ( id) {
            case XMLDefaultTokenContext.TEXT_ID:
                if ( text.endsWith("<" )) {
                    ctx.init(element, "");
                    return queryElements();
                } else if ( text.startsWith("&")) {
                    ctx.init(element, text.substring(1));
                    return queryEntities();
                } else {
                    //??? join all previous texts? 
                    // No they are DOM nodes.
                    ctx.init(element, text);
                    return queryValues();
                }
//                break;
                
            case XMLDefaultTokenContext.TAG_ID:
                if ( StartTag.class.equals(element.getClass()) 
                || EmptyTag.class.equals(element.getClass())) {
                    if (text.equals("")) {  
                        //??? should not occure
                        if (token.getImage().endsWith(">")) {
                            ctx.init(element, text);
                            return queryValues();
                        } else {
                            ctx.init(element, text);
                            return queryElements();
                        }
                    } else if (text.endsWith("/>")) {
                        ctx.init(element, "");
                        return queryValues();                        
                    } else if (text.endsWith(">")) {
                        ctx.init(element, "");
                        return queryValues();
                    } else if (text.startsWith("</")) {
                        //??? replace immediatelly?
                        ctx.init(element, text.substring(2));
                        return queryElements();
                    } else if (text.startsWith("<")) {
                        ctx.init(element, text.substring(1));
                        return queryElements();
                    }
                } else {
                    // end tag, pairing tag completion if not at boundary
                    if ("".equals(text) && token.getImage().endsWith(">")) {
                        ctx.init(element, text);
                        return queryValues();
                    }
                }
                break;
                
            case XMLDefaultTokenContext.VALUE_ID:
                if (text.endsWith("&")) {
                    ctx.init(element, "");
                    return queryEntities();
                } else if ("".equals(text)) {   //??? improve check to addres inner '"'
                    String image = token.getImage();
                    char ch = image.charAt(image.length()-1);
                    
                    // findout if it is closing '
                    
                    if (ch == '\'' || ch == '"') {
                        
                        if (image.charAt(0) == ch && image.length() > 1) {
                            // we got whole quoted value as single token ("xxx"|)
                            return null;                            
                        }

                        boolean closing = false;
                        TokenItem prev = token.getPrevious();

                        while (prev != null) {
                            int tid = prev.getTokenID().getNumericID();
                            if (tid == XMLDefaultTokenContext.VALUE_ID) {
                                closing = true;
                                break;
                            } else if (tid == XMLDefaultTokenContext.CHARACTER_ID) {
                                prev = prev.getPrevious();
                            } else {
                                break;
                            }
                        }
                        if (closing == false) {
                            ctx.init(element, text);
                            return queryValues();
                        }
                    } else {
                        ctx.init(element, text);
                        return queryValues();                        
                    }
                } else {
                    ctx.init(element, text);
                    return queryValues();
                }
                break;
                
            case XMLDefaultTokenContext.OPERATOR_ID:
                if ("".equals(text)) {
                    if ("=".equals(token.getImage())) {
                        ctx.init(element, "");
                        return queryValues();
                    }
                }
                break;

            case XMLDefaultTokenContext.WS_ID:
                if (StartTag.class.equals(element.getClass()) 
                || EmptyTag.class.equals(element.getClass())) {
                    ctx.initVirtualAttr((Element)element, "");
                    return queryAttributes();
                } else {
                    // end tag no attributes to complete
                    return null;
                }
//                break;
                
            case XMLDefaultTokenContext.ARGUMENT_ID:
                if (StartTag.class.equals(element.getClass()) 
                || EmptyTag.class.equals(element.getClass())) {
                    ctx.initVirtualAttr((Element)element, text);
                    return queryAttributes();
                }
                break;
                
            case XMLDefaultTokenContext.CHARACTER_ID:  // entity reference
                if (text.startsWith("&#")) {
                    // character ref, ignore
                    return null;
                } else if (text.endsWith(";")) {
                        ctx.init(element, "");
                        return queryValues();                    
                } else if (text.startsWith("&")) {
                    ctx.init(element, text.substring(1));
                    return queryEntities();                    
                } else if ("".equals(text)) {
                    if (token.getImage().endsWith(";")) {
                        ctx.init(element, text);
                        return queryValues();
                    }
                }
                break;
                
            default:
                
        }
        
//        System.err.println("Cannot complete: " + element + "\n\t" + token + "\n\t" + text);
        return null;
    }

    
    // Delegate queriing to performer ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    private List queryEntities() {
        Enumeration res = getPerformer().queryEntities(ctx.getCurrentPrefix());
        return translateEntityRefs(res);
    }
    
    private List queryElements() {
        Enumeration res = getPerformer().queryElements(ctx);
        return translateElements(res);
    }

    private List queryAttributes() {
        Enumeration res = getPerformer().queryAttributes(ctx);
        return translateAttributes(res);
    }

    private List queryValues() {
        Enumeration res = getPerformer().queryValues(ctx);
        return translateValues(res);
    }
    
    private List queryNotations() {  //!!! to be implemented
        Enumeration res = getPerformer().queryNotations(ctx.getCurrentPrefix());
        return null;
    }
    
    // Translate general results to editor ones ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        
    private List translateEntityRefs(Enumeration refs ) {
        List result = new ArrayList(133);
        while ( refs.hasMoreElements() ) {
            GrammarResult next = (GrammarResult) refs.nextElement();
            EntityRefResultItem ref = new EntityRefResultItem(next);
            result.add( ref );
        }
        return result;
    }
    
    private List translateElements(Enumeration els ) {
        List result = new ArrayList(13);
        while (els.hasMoreElements()) {
            GrammarResult next = (GrammarResult) els.nextElement();
            ElementResultItem ei = new ElementResultItem(next);
            result.add( ei );
        }
        return result;
    }
    
    
    private List translateAttributes(Enumeration attrs ) {
        List result = new ArrayList(13);
        while (attrs.hasMoreElements()) {
            GrammarResult next = (GrammarResult) attrs.nextElement();            
            AttributeResultItem attr = new AttributeResultItem(next, boundary == false);
            result.add( attr );
        }
        return result;
    }
    
    private List translateValues(Enumeration values ) {
        List result = new ArrayList(3);
        while (values.hasMoreElements()) {
            GrammarResult next = (GrammarResult) values.nextElement();
            ValueResultItem val = new ValueResultItem(next);
            result.add( val );
        }
        return result;
    }

    
    /**
     * User just typed <sample>&lt;/</sample> so we must locate
     * paing start tag.
     */
    private static List findStartTag(SyntaxNode text) {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("XMLCompletionQuery.findStartTag: text=" + text);

        Node parent = text.getParentNode();
        if (parent == null) {
            return Collections.EMPTY_LIST;
        }

        String name = parent.getNodeName();
        if ( Util.THIS.isLoggable() ) /* then */ {
            Util.THIS.debug ("    name=" + name);
        }
        if ( name == null ) {
            return Collections.EMPTY_LIST;
        }

        XMLResultItem res = new ElementResultItem(name);
        if ( Util.THIS.isLoggable() ) /* then */ {
            Util.THIS.debug ("    result=" + res);
        }

        List list = new ArrayList(1);
        list.add (res);

        return list;
    }
    
    
    // Grammar binding ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    /**
     * Obtain reqistered query, cache results in document property 
     * <code>PROP_DOCUMENT_QUERY</code>. It is always called from single
     * thread.
     */
    protected GrammarQuery getPerformer() {

        GrammarCache desc = (GrammarCache) doc.getProperty(DOCUMENT_GRAMMAR_BINDING_PROP);

        if (desc == null) {  
            desc = new GrammarCache();
            desc.attach(doc, sup);
            doc.putProperty(DOCUMENT_GRAMMAR_BINDING_PROP, desc);
        }
        
        return desc.getGrammar(300);
    }
    

    static class GrammarCache {

        // last invalidation time
        private long timestamp = System.currentTimeMillis();
        private int  delay = 0;

        // current cache state
        private int state = INVALID;
        
        static final int VALID = 1;
        static final int LOADING = 2;
        static final int INVALID = 3;
        
        // cache entry
        private GrammarQuery grammar;  

        // noop loader
        private static final RequestProcessor.Task EMPTY_LOADER =
            RequestProcessor.createRequest(Task.EMPTY);
        
        // current loader
        private RequestProcessor.Task loader = EMPTY_LOADER;

        //!!! REMOVE document that is needed just by DTD parser
        private Document doc;
        
        /**
         * Return any suitable grammar that you can get 
         * till expires given timeout.
         */
        public synchronized GrammarQuery getGrammar(int timeout) {
            
            switch (state) {
                case VALID:
                    return grammar;
                                                        
                case INVALID:
                    state = LOADING;
                    loadGrammar();  // async
                    
                case LOADING:
                    waitLoaded(timeout); // possible thread switch !!!
                    
                    //??? return last loaded grammar (use option?)
                    if (grammar != null) return grammar;
                   
                default:                    
                    return EmptyQuery.INSTANCE;
            }
        }
        

        /**
         * Start listening at internal DTD invalidating grammar on its change
         */
        public void attach(final javax.swing.text.Document doc, final XMLSyntaxSupport sup) {
            this.doc = doc;
            doc.addDocumentListener( new DocumentListener() {
                
                public void insertUpdate(DocumentEvent e) {
                    try {
                        SyntaxElement el = sup.getElementChain(e.getOffset() + 1);  // it returns in or previous so +1
                        if (isDoctype(el)) {
                            invalidateGrammar();
                        }
                    } catch (BadLocationException ex) {
                        ex.printStackTrace();
                    }
                }
              
                public void removeUpdate(DocumentEvent e) {
                    try {
                        // ignore removal at document end
                        if (e.getOffset() >= e.getDocument().getLength()) return;
                        SyntaxElement el = sup.getElementChain(e.getOffset() + 1);  // it returns in or previous so +1
                        if (isDoctype(el)) {
                            invalidateGrammar();
                        }                  
                    } catch (BadLocationException ex) {                        
                        ex.printStackTrace();
                    }
                }
              
                public void changedUpdate(DocumentEvent e) {
                    // not interested
                }
                
                private boolean isDoctype(SyntaxElement el) {
                    return el instanceof SyntaxElement.Declaration;
                }
            });
        }

        /**
         * Notification from invalidator thread, the grammar need to be reloaded.
         */
        public synchronized void invalidateGrammar() {
            
            // make current loader a zombie
            loader.cancel();
            loader = EMPTY_LOADER;
            if (state == LOADING || state == VALID) {
                notifyProgress(loader, Util.THIS.getString("MSG_loading_cancel"));
            }
            
            // optimalize reload policy
            delay = (System.currentTimeMillis() - timestamp) < 1000 ? 500 : 0;
            timestamp = System.currentTimeMillis();
            
            state = INVALID;
        }


        /**
         * Nofification from grammar loader thread, new valid grammar.
         * @param grammar grammar or <code>null</code> if cannot load.
         */
        private synchronized void grammarLoaded(Task loader, GrammarQuery grammar) {

            try {
                // eliminate zombie loader
                if (this.loader != loader) return;

                String status = (grammar != null) ? Util.THIS.getString("MSG_loading_done") 
                    : Util.THIS.getString("MSG_loading_failed");

                this.grammar = grammar == null ? EmptyQuery.INSTANCE : grammar;
                state = VALID;

                notifyProgress(loader, status);            
            } finally {
                notifyAll();
            }
        }

        /**
         * Notify loader progress filtering out messages from zombies
         */
        private void notifyProgress(Task loader, String msg) {
            if (this.loader != loader) return;
            TopManager.getDefault().setStatusText(msg);
        }
        
        /**
         * Async grammar fetching
         */
        private void loadGrammar() {

            class LoaderTask extends Task {
                
                // my represenetation in RQ as others see it
                private RequestProcessor.Task self;
                
                public void run() {
                    
                    GrammarQuery loaded = null;                    
                    try {
                    
                        String status = Util.THIS.getString("MSG_loading");
                        notifyProgress(self, status);

                        //!!! hardcoded DTD grammar, replace with lookup

                        InputSource in = Convertors.documentToInputSource(doc);
                        loaded = new org.netbeans.modules.xml.text.completion.dtd.DTDParser().parse(in);

                    } finally {
                        grammarLoaded(self, loaded);
                        notifyFinished();
                    }

                }
            }
                        
            // we need a fresh thread per loader (it some request blocks)
            RequestProcessor rp = new RequestProcessor("tmp/XML grammar fetching"); //NOI18N
            LoaderTask task = new LoaderTask();
            loader = rp.create(task);
            task.self = loader;
            
            // do not allow too many loaders if just editing invalidation
            // area
            loader.schedule(delay);
        }

        /**
         * Wait till grammar is loaded or given timeout expires
         */
        private void waitLoaded(int timeout) {
            try {
                if (state == LOADING) wait(timeout);
            } catch (InterruptedException ex) {
            }
        }

    }
                
}
