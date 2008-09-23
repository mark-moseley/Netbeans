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

package org.netbeans.modules.web.core.syntax.completion;

import java.util.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import org.netbeans.editor.*;
import org.netbeans.modules.web.core.syntax.deprecated.JspTagTokenContext;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.modules.web.core.syntax.*;
import org.netbeans.modules.web.jsps.parserapi.PageInfo.BeanData;
import org.netbeans.spi.editor.completion.CompletionItem;


/**
 * JSP completion support finder
 *
 * @author Marek Fukala
 * @author Petr Nejedly
 * @author Tomasz.Slota@Sun.COM
 */

public class JspCompletionQuery {
   
    private static final List<String> JSP_DELIMITERS = Arrays.asList(new String[]{"<%", "<%=", "<%!"});
    
    private static final JspCompletionQuery JSP_COMPLETION_QUERY = new JspCompletionQuery();
    
    static JspCompletionQuery instance() {
        return JSP_COMPLETION_QUERY;
    }
    
    void query(CompletionResultSet result, JTextComponent component, int offset) {
        BaseDocument doc = (BaseDocument)component.getDocument();
        JspSyntaxSupport sup = JspSyntaxSupport.get(doc);
        
        try {
            SyntaxElement elem = sup.getElementChain( offset );
            if (elem == null)
                // this is a legal option, when I don't have anything to say just return null
                return;

            switch (elem.getCompletionContext()) {
                // TAG COMPLETION
            case JspSyntaxSupport.TAG_COMPLETION_CONTEXT :
                queryJspTag(result, component, offset, sup,
                        (SyntaxElement.Tag)elem);
                break;
                
                // ENDTAG COMPLETION
            case JspSyntaxSupport.ENDTAG_COMPLETION_CONTEXT :
                queryJspEndTag(result, component, offset, sup);
                break;
                
                //DIRECTIVE COMPLETION IN JSP SCRIPTLET (<%| should offer <%@taglib etc.)
            case JspSyntaxSupport.SCRIPTINGL_COMPLETION_CONTEXT:
                queryJspDirectiveInScriptlet(result, offset, sup);
                //query for jsp delimiters
                queryJspDelimitersInScriptlet(result, offset, sup);
                break;
                
                // DIRECTIVE COMPLETION
            case JspSyntaxSupport.DIRECTIVE_COMPLETION_CONTEXT :
                queryJspDirective(result, component, offset, sup, (SyntaxElement.Directive)elem, doc);
                break;
                
                // EXPRESSION LANGUAGE
            case JspSyntaxSupport.EL_COMPLETION_CONTEXT:
                queryEL(result, component, offset, sup);
                break;
                
                // CONTENT LANGUAGE
            case JspSyntaxSupport.CONTENTL_COMPLETION_CONTEXT :
                // JSP tags results
                queryJspTagInContent(result, offset, sup, doc);
                
                // JSP directive results
                queryJspDirectiveInContent(result, component, offset, sup, doc);
                
                //query for jsp delimiters
                queryJspDelimitersInContent(result, offset, sup);
                break;
                
            }
            
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    private void queryJspTag(CompletionResultSet result, JTextComponent component, int offset,
            JspSyntaxSupport sup, SyntaxElement.Tag elem) throws BadLocationException {

        // find the current item
        TokenItem item = sup.getItemAtOrBefore(offset);
        
        if (item == null) {
            return ;
        }
        
        TokenID id = item.getTokenID();
        String tokenPart = item.getImage().substring(0, offset - item.getOffset());
        String token = item.getImage().trim();
        int anchor = -1;
        
        // SYMBOL
        if (id == JspTagTokenContext.SYMBOL) {
            if (tokenPart.equals("<")) { // NOI18N
                // just after the beginning of the tag
                anchor = offset;
                addTagPrefixItems(result, anchor, sup, sup.getTagPrefixes("")); // NOI18N
            }
            if (tokenPart.endsWith("\"")) { // NOI18N
                // try an attribute value
                String attrName = findAttributeForValue(sup, item);
                if (attrName != null) {
                    AttributeValueSupport attSup =
                            AttributeValueSupport.getSupport(true, elem.getName(), attrName);
                    if (attSup != null) {
                        attSup.result(result, component, offset, sup, elem, ""); // NOI18N
                    }
                }
            }
            if(tokenPart.endsWith(">") && !tokenPart.endsWith("/>")) {
                result.addItem(sup.getAutocompletedEndTag(offset));
            }
            
            
        }
        
        // TAG
        if (id == JspTagTokenContext.TAG
                || id == JspTagTokenContext.WHITESPACE
                || id == JspTagTokenContext.EOL) {
            // inside a JSP tag name
            if (isBlank(tokenPart.charAt(tokenPart.length() - 1))
                    || tokenPart.equals("\n")) {
                // blank character - do attribute completion
                anchor = offset;
                addAttributeItems(result, anchor, sup,  elem, sup.getTagAttributes(elem.getName(), ""), null); // NOI18N
            } else {
                int colonIndex = tokenPart.indexOf(":"); // NOI18N
                anchor = offset - tokenPart.length();
                if (colonIndex == -1) {
                    addTagPrefixItems(result, anchor, sup, sup.getTagPrefixes(tokenPart));
                } else {
                    String prefix = tokenPart.substring(0, colonIndex);
                    addTagPrefixItems(result, anchor, sup, prefix, sup.getTags(tokenPart), elem);
                }
            }
        }
        
        // ATTRIBUTE
        if (id == JspTagTokenContext.ATTRIBUTE) {
            // inside or after an attribute
            if (isBlank(tokenPart.charAt(tokenPart.length() - 1))) {
                // blank character - do attribute completion
                anchor = offset;
                addAttributeItems(result, anchor, sup, elem, sup.getTagAttributes(elem.getName(), ""), null); // NOI18N
            } else {
                anchor = offset - tokenPart.length();
                addAttributeItems(result, anchor , sup, elem, sup.getTagAttributes(elem.getName(), tokenPart), token);
            }
        }
        
        // ATTRIBUTE VALUE
        if (id == JspTagTokenContext.ATTR_VALUE) {
            // inside or after an attribute
            String valuePart = tokenPart.trim();
            //return empty completion if the CC is not invoked inside a quotations
            if(valuePart.length() == 0) {
                return;
            }
            
            item = item.getPrevious();
            while ((item != null) && (item.getTokenID() == JspTagTokenContext.ATTR_VALUE)) {
                valuePart = item.getImage() + valuePart;
                item = item.getPrevious();
            }
            // get rid of the first quote
            valuePart = valuePart.substring(1);
            String attrName = findAttributeForValue(sup, item);
            if (attrName != null) {
                AttributeValueSupport attSup =
                        AttributeValueSupport.getSupport(true, elem.getName(), attrName);
                if (attSup != null) {
                    attSup.result(result, component, offset, sup, elem, valuePart);
                }
            }
            
        }
        
        if(anchor != -1) {
            result.setAnchorOffset(anchor);
        }
       
    }
    
    private void queryJspEndTag(CompletionResultSet result, JTextComponent component, int offset, JspSyntaxSupport sup) throws BadLocationException {
        // find the current item
        TokenItem item = sup.getItemAtOrBefore(offset);
        if (item == null) {
            return;
        }
        String tokenPart = item.getImage().substring(0, offset - item.getOffset());
        int anchor = offset - tokenPart.length();
        result.setAnchorOffset(anchor);   
        result.addAllItems(sup.getPossibleEndTags(offset, anchor, tokenPart));
    }
    
    /** Gets a list of completion items for EL */
    private void queryEL(CompletionResultSet result, JTextComponent component, int offset, JspSyntaxSupport sup) throws BadLocationException {
        
        ELExpression elExpr = new ELExpression(sup);
        
        switch (elExpr.parse(offset)){
        case ELExpression.EL_START:
            // implicit objects
            for (ELImplicitObjects.ELImplicitObject implOb : ELImplicitObjects.getELImplicitObjects(elExpr.getReplace())) {
                result.addItem(JspCompletionItem.createELImplicitObject(implOb.getName(), offset - elExpr.getReplace().length(), implOb.getType()));
            }
            
            // defined beans on the page
            BeanData[] beans = sup.getBeanData();
            if (beans != null){
                for (int i = 0; i < beans.length; i++) {
                    if (beans[i].getId().startsWith(elExpr.getReplace()))
                        result.addItem(JspCompletionItem.createELBean(beans[i].getId(), offset - elExpr.getReplace().length(), beans[i].getClassName()));
                }
            }
            //Functions
            List functions = ELFunctions.getFunctions(sup, elExpr.getReplace());
            Iterator iter = functions.iterator();
            while (iter.hasNext()) {
                ELFunctions.Function fun = (ELFunctions.Function) iter.next();
                result.addItem(JspCompletionItem.createELFunction(
                        fun.getName(),
                        offset - elExpr.getReplace().length(),
                        fun.getReturnType(),
                        fun.getPrefix(),
                        fun.getParameters()));
            }
            break;
        case ELExpression.EL_BEAN:
        case ELExpression.EL_IMPLICIT:
            
            List<CompletionItem> items = elExpr.getPropertyCompletionItems(elExpr.getObjectClass(), offset - elExpr.getReplace().length());
            result.addAllItems(items);
            
            break;
        }
        
    }
    
    /** Gets a list of JSP directives which can be completed just after <% in java scriptlet context */
    private void queryJspDirectiveInScriptlet(CompletionResultSet result, int offset, JspSyntaxSupport sup) throws BadLocationException {
        
        TokenItem item = sup.getItemAtOrBefore(offset);
        if (item == null) {
            return;
        }
        
        TokenID id = item.getTokenID();
        String tokenPart = item.getImage().substring(0, offset - item.getOffset());
        
        if(id == JspTagTokenContext.SYMBOL2 && tokenPart.equals("<%")) {
            addDirectiveItems(result, offset - tokenPart.length(), (List<TagInfo>)sup.getDirectives("")); // NOI18N
        }
        
    }
    
    private void queryJspDelimitersInScriptlet(CompletionResultSet result, int offset, JspSyntaxSupport sup) throws BadLocationException {
        TokenItem item = sup.getItemAtOrBefore(offset);
        if (item == null) {
            return;
        }
        
        TokenID id = item.getTokenID();
        String tokenPart = item.getImage().substring(0, offset - item.getOffset());
        
        if(id == JspTagTokenContext.SYMBOL2 && tokenPart.startsWith("<%")) {
            addDelimiterItems(result, offset - tokenPart.length(), tokenPart); // NOI18N
        }
        
    }
    
     private void queryJspDelimitersInContent(CompletionResultSet result, int offset, JspSyntaxSupport sup) throws BadLocationException {
        TokenItem item = sup.getItemAtOrBefore(offset);
        if (item == null) {
            return;
        }
        
        TokenID id = item.getTokenID();
        String tokenPart = item.getImage().substring(0, offset - item.getOffset());
        
        if(tokenPart.startsWith("<")) {
            addDelimiterItems(result, offset - tokenPart.length(), tokenPart); // NOI18N
        }
        
    }
    
    private void queryJspDirective(CompletionResultSet result, JTextComponent component, int offset, JspSyntaxSupport sup,
            SyntaxElement.Directive elem, BaseDocument doc) throws BadLocationException {
        // find the current item
        TokenItem item = sup.getItemAtOrBefore(offset);
        if (item == null) {
            return;
        }
        
        TokenID id = item.getTokenID();
        String tokenPart = item.getImage().substring(0, offset - item.getOffset());
        String token = item.getImage().trim();
        
        // SYMBOL
        if (id.getNumericID() == JspTagTokenContext.SYMBOL_ID) {
            if (tokenPart.startsWith("<")) { // NOI18N
                //calculate a position of the potential replacement
                int removeLength = tokenPart.length(); 
                addDirectiveItems(result, offset - removeLength, sup.getDirectives("")); // NOI18N
            }
            if (tokenPart.endsWith("\"")) { // NOI18N
                // try an attribute value
                String attrName = findAttributeForValue(sup, item);
                if (attrName != null) {
                    AttributeValueSupport attSup =
                            AttributeValueSupport.getSupport(false, elem.getName(), attrName);
                    if (attSup != null) {
                        attSup.result(result, component, offset, sup, elem, ""); // NOI18N
                    }
                }
            }
        }
        
        // DIRECTIVE
        if (id.getNumericID() == JspTagTokenContext.TAG_ID
                || id.getNumericID() == JspTagTokenContext.WHITESPACE_ID
                || id.getNumericID() == JspTagTokenContext.EOL_ID) {
            // inside a JSP directive name or after a whitespace
            if (isBlank(tokenPart.charAt(tokenPart.length() - 1))
                    || tokenPart.equals("\n")) {
                TokenItem prevItem = item.getPrevious();
                TokenID prevId = prevItem.getTokenID();
                String prevToken = prevItem.getImage().trim();
                if (prevId.getNumericID() == JspTagTokenContext.TAG_ID
                        ||  prevId.getNumericID() == JspTagTokenContext.ATTR_VALUE_ID
                        ||  prevId.getNumericID() == JspTagTokenContext.WHITESPACE_ID
                        ||  prevId.getNumericID() == JspTagTokenContext.EOL_ID) {
                    // blank character - do attribute completion
                    addAttributeItems(result, offset, sup, elem, sup.getDirectiveAttributes(elem.getName(), ""), null); // NOI18N
                } else if (prevId.getNumericID() == JspTagTokenContext.SYMBOL_ID && prevToken.equals("<%@")) { // NOI18N
                    // just after the beginning of the directive
                    int removeLength = tokenPart.length() + 2;
                    addDirectiveItems(result, offset - removeLength, sup.getDirectives("")); // NOI18N
                }
            } else {
                boolean add = true;
                //I need to get the whitespace token length before the tag name
                int whitespaceLength = 0;
                TokenItem prevItem = item.getPrevious();
                TokenID prevId = prevItem.getTokenID();
                //test whether there is a space before the currently completed tagname
                if(prevId.getNumericID() == JspTagTokenContext.TAG_ID && "".equals(prevItem.getImage().trim())) //try to trim the token image - just for sure since I am not absolutely sure if the TAG_ID is only for whitespaces in this case.
                    whitespaceLength = prevItem.getImage().length();
                
                
                List<TagInfo> list = (List<TagInfo>)sup.getDirectives(tokenPart);
                if (list.size() == 1){
                    TagInfo directive = list.get(0);
                    //is the cc invoce just after the directive?
                    if (directive.getTagName().equalsIgnoreCase(tokenPart)) {
                        add = false;
                    }
                }
                if (add){
                    int removeLength = whitespaceLength + tokenPart.length() + "<%@".length(); 
                    addDirectiveItems(result, offset - removeLength, list);
                }
            }
        }
        
        // ATTRIBUTE
        if (id.getNumericID() == JspTagTokenContext.ATTRIBUTE_ID) {
            // inside or after an attribute
            if (isBlank(tokenPart.charAt(tokenPart.length() - 1))) {
                // blank character - do attribute completion
                addAttributeItems(result, offset, sup, elem, sup.getDirectiveAttributes(elem.getName(), ""), null); // NOI18N
            } else {
                int removeLength = tokenPart.length();
                addAttributeItems(result, offset - removeLength, sup, elem, sup.getDirectiveAttributes(elem.getName(), tokenPart), token);
            }
        }
        
        // ATTRIBUTE VALUE
        if (id.getNumericID() == JspTagTokenContext.ATTR_VALUE_ID) {
            // inside or after an attribute
            String valuePart = tokenPart;
            item = item.getPrevious();
            while ((item != null) && (item.getTokenID().getNumericID() == JspTagTokenContext.ATTR_VALUE_ID
                    || item.getTokenID().getNumericID() == JspTagTokenContext.EOL_ID)) {
                valuePart = item.getImage() + valuePart;
                item = item.getPrevious();
            }
            // get rid of the first quote
            valuePart = valuePart.substring(1);
            String attrName = findAttributeForValue(sup, item);
            if (attrName != null) {
                AttributeValueSupport attSup =
                        AttributeValueSupport.getSupport(false, elem.getName(), attrName);
                //we cannot set substitute offset for file cc items
                if (attSup != null) {
                    attSup.result(result, component, offset, sup, elem, valuePart); // NOI18N
                }
            }
            
        }
        
    }
    
    
    private void queryJspTagInContent(CompletionResultSet result, int offset, JspSyntaxSupport sup, BaseDocument doc) throws BadLocationException {
        // find the current item
        
        TokenItem item = sup.getItemAtOrBefore(offset);
        if (item == null) {
            return;
        }
        
        String tokenPart = item.getImage().substring(0,
                (offset - item.getOffset()) >= item.getImage().length() ? item.getImage().length() : offset - item.getOffset());
        int ltIndex = tokenPart.lastIndexOf('<');
        if (ltIndex != -1) {
            tokenPart = tokenPart.substring(ltIndex + 1);
        }
        while (ltIndex == -1) {
            item = item.getPrevious();
            if (item == null) {
                return;
            }
            String newImage = item.getImage();
            ltIndex = newImage.lastIndexOf('<');
            if (ltIndex != -1)
                tokenPart = newImage.substring(ltIndex + 1) + tokenPart;
            else {
                tokenPart = newImage + tokenPart;
            }
            if (tokenPart.length() > 20) {
                //huh, what the hell is that? I belive it should be > 42 ;-)
                return;
            }
        }
        // we found ltIndex, tokenPart is either the part of the token we are looking for
        // or '/' + what we are looking for
        int removeLength = tokenPart.length();
        if (tokenPart.startsWith("/")) { // NOI18N
            tokenPart = tokenPart.substring(1);
            int anchor = offset - removeLength + 1;
            result.setAnchorOffset(anchor);
            result.addAllItems(sup.getPossibleEndTags(offset, anchor, tokenPart, true)); //get only first end tag
        } else {
            int anchor = offset - removeLength;
            result.setAnchorOffset(anchor);
            addTagPrefixItems(result, anchor, sup, sup.getTagPrefixes(tokenPart));
        }

    }
    
    private void queryJspDirectiveInContent(CompletionResultSet result, JTextComponent component, int offset, JspSyntaxSupport sup, BaseDocument doc) throws BadLocationException {
        // find the current item
        TokenItem item = sup.getItemAtOrBefore(offset);
        if (item == null) {
            //return empty completion result
            return;
        }
        
        String tokenPart = item.getImage().substring(0,
                (offset - item.getOffset()) >= item.getImage().length() ? item.getImage().length() : offset - item.getOffset());
        
        if(!tokenPart.equals("<") && !tokenPart.equals("<%")) { // NOI18N
            //return empty completion result
            return ;
        }
        
        addDirectiveItems(result, offset - tokenPart.length(), sup.getDirectives("")); // NOI18N
        
    }
    
    private boolean isBlank(char c) {
        return c == ' ';
    }
    
    /** Finds an attribute name, assuming that the item is either
     * SYMBOL after the attribute name or ATTR_VALUE after this attribute name.
     * May return null if nothing found.
     */
    protected String findAttributeForValue(JspSyntaxSupport sup, TokenItem item) {
        // get before any ATTR_VALUE
        while ((item != null) && (item.getTokenID().getNumericID() == JspTagTokenContext.ATTR_VALUE_ID))
            item = item.getPrevious();
        // now collect the symbols
        String symbols = ""; // NOI18N
        while ((item != null) && (item.getTokenID().getNumericID() == JspTagTokenContext.SYMBOL_ID)) {
            symbols = item.getImage() + symbols;
            item = item.getPrevious();
        }
        // two quotes at the end are not allowed
        if (!sup.isValueBeginning(symbols))
            return null;
        String attributeName = ""; // NOI18N
        //there may be a whitespace before the equals sign - trace over the whitespace
        //due to a bug in jsp tag syntax parser the whitespace has tag-directive tokenID
        //so I need to use the token image to recognize whether it is a whitespace
        while ((item != null) && (item.getImage().trim().length() == 0)) {
            item = item.getPrevious();
        }
        //now there should be either tag name or attribute name
        while ((item != null) && (item.getTokenID().getNumericID() == JspTagTokenContext.ATTRIBUTE_ID)) {
            attributeName = item.getImage() + attributeName;
            item = item.getPrevious();
        }
        if (attributeName.trim().length() > 0)
            return attributeName.trim();
        return null;
    }
    
    /** Adds to the list of items <code>compItemList</code> new TagPrefix items with prefix
     * <code>prefix</code> for list of tag names <code>tagStringItems</code>.
     * @param set - <code>SyntaxElement.Tag</code>
     */
    private void addTagPrefixItems(CompletionResultSet result, int anchor, JspSyntaxSupport sup, String prefix, List tagStringItems, SyntaxElement.Tag set) {
        for (int i = 0; i < tagStringItems.size(); i++) {
            Object item = tagStringItems.get(i);
            if (item instanceof TagInfo)
                result.addItem(JspCompletionItem.createPrefixTag(prefix, anchor, (TagInfo)item, set));
            else
                result.addItem(JspCompletionItem.createPrefixTag(prefix + ":" + (String)item, anchor)); // NOI18N
        }
    }
    
    /** Adds to the list of items <code>compItemList</code> new TagPrefix items for prefix list
     * <code>prefixStringItems</code>, followed by all possible tags for the given prefixes.
     */
    private void addTagPrefixItems(CompletionResultSet result, int anchor, JspSyntaxSupport sup, List<Object> prefixStringItems) {
        for (int i = 0; i < prefixStringItems.size(); i++) {
            String prefix = (String)prefixStringItems.get(i);
            // now get tags for this prefix
            List tags = sup.getTags(prefix, ""); // NOI18N
            for (int j = 0; j < tags.size(); j++) {
                Object item = tags.get(j);
                if (item instanceof TagInfo)
                    result.addItem(JspCompletionItem.createPrefixTag(prefix, anchor, (TagInfo)item));
                else
                    result.addItem(JspCompletionItem.createPrefixTag(prefix + ":" + (String)item, anchor)); // NOI18N
            }
        }
    }
    
    /** Adds to the list of items <code>compItemList</code> new TagPrefix items with prefix
     * <code>prefix</code> for list of tag names <code>tagStringItems</code>.
     */
    private void addDirectiveItems(CompletionResultSet result, int anchor, List<TagInfo> directiveStringItems) {
        for (int i = 0; i < directiveStringItems.size(); i++) {
            TagInfo item = directiveStringItems.get(i);
            result.addItem(JspCompletionItem.createDirective( item.getTagName(), anchor, item));
        }
    }
    
    private void addDelimiterItems(CompletionResultSet result, int anchor, String prefix) {
        for(String delimiter : JSP_DELIMITERS) {
            if(delimiter.startsWith(prefix)) {
                result.addItem(JspCompletionItem.createDelimiter(delimiter, anchor));
            }
        }
    }
    
    /** Adds to the list of items <code>compItemList</code> new Attribute items.
     * Only those which are not already in tagDir
     * @param sup the syntax support
     * @param compItemList list to add to
     * @param tagDir tag or directive element
     * @param attributeItems list of strings containing suitable values (String or TagAttributeInfo)
     * @param currentAttr current attribute, may be null
     */
    private void addAttributeItems(CompletionResultSet result, int offset, JspSyntaxSupport sup, 
            SyntaxElement.TagDirective tagDir, List attributeItems, String currentAttr) {
        for (int i = 0; i < attributeItems.size(); i++) {
            Object item = attributeItems.get(i);
            String attr;
            if (item instanceof TagAttributeInfo)
                attr = ((TagAttributeInfo)item).getName();
            else
                attr = (String)item;
            boolean isThere = tagDir.getAttributes().keySet().contains(attr);
            if (!isThere || attr.equalsIgnoreCase(currentAttr) ||
                    (currentAttr != null && attr.startsWith(currentAttr) && attr.length()>currentAttr.length() && !isThere)) {
                if (item instanceof TagAttributeInfo)
                    //XXX This is hack for fixing issue #45302 - CC is to aggressive.
                    //The definition of the tag and declaration doesn't allow
                    //define something like "prefix [uri | tagdir]". In the future
                    //it should be rewritten definition of declaration, which allow
                    //to do it.
                    if ("taglib".equalsIgnoreCase(tagDir.getName())){ //NOI18N
                        if (attr.equalsIgnoreCase("prefix")  //NOI18N
                                || (attr.equalsIgnoreCase("uri") && !tagDir.getAttributes().keySet().contains("tagdir")) //NOI18N
                                || (attr.equalsIgnoreCase("tagdir") && !tagDir.getAttributes().keySet().contains("uri"))) //NOI18N
                            result.addItem(JspCompletionItem.createAttribute(offset, (TagAttributeInfo)item));
                    } else {
                        result.addItem(JspCompletionItem.createAttribute(offset, (TagAttributeInfo)item));
                    } else
                        result.addItem(JspCompletionItem.createAttribute((String)item, offset));
            }
        }
    }
    
}
