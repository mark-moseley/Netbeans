/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.syntax;

import java.util.*;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.JEditorPane;
import javax.swing.text.JTextComponent;

import javax.servlet.jsp.tagext.*;

import org.openide.filesystems.FileObject;
import org.openide.ErrorManager;

import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.html.HTMLTokenContext;
import org.netbeans.editor.ext.java.JavaSyntaxSupport;
import org.netbeans.editor.ext.java.JavaTokenContext;
import org.netbeans.modules.editor.NbEditorUtilities;
import java.util.*;
/**
 *
 * @author  Petr Jiricka, Petr Nejedly
 */
public class JspSyntaxSupport extends ExtSyntaxSupport {

    /** ErrorManager shared by whole module (package) for logging */
    static final ErrorManager err = 
        ErrorManager.getDefault().getInstance("org.netbeans.modules.web.jspsyntax"); // NOI18N

    /* Constants for various contexts in the text from the point of
    view of JSP completion.*/
    
    /** Completion context for JSP tags (standard or custom) */
    public static final int TAG_COMPLETION_CONTEXT = 1;
    /** Completion context for JSP end tags (standard or custom) */
    public static final int ENDTAG_COMPLETION_CONTEXT = 2;
    /** Completion context for JSP directives */
    public static final int DIRECTIVE_COMPLETION_CONTEXT = 3;
    /** Completion context for JSP comments */
    public static final int COMMENT_COMPLETION_CONTEXT = 4;
    /** Completion context for other JSP text - such as body of custom tags 
     * with TAG_DEPENDENT body content. */
    public static final int TEXT_COMPLETION_CONTEXT = 5;
    /** Completion context for the content language */
    public static final int CONTENTL_COMPLETION_CONTEXT = 6;
    /** Completion context for the scripting language */
    public static final int SCRIPTINGL_COMPLETION_CONTEXT = 7;
    /** Completion context for error */
    public static final int ERROR_COMPLETION_CONTEXT = 8;
    
    private static final String STANDARD_JSP_PREFIX = "jsp";    // NOI18N
    /** Data for completion: TreeMap for standard JSP tags 
    * (tag name, array of attributes). */
    private static TagInfo[] standardTagDatas;
    
    /** Data for completion, when the jsp page is in XML syntax
     **/
    private static TagInfo[] xmlJspTagDatas;
    
    /** Data for completion, when the tag file is in XML syntax
     **/
    private static TagInfo[] xmlTagFileTagDatas;
    
    /** Data for completion: TreeMap for JSP directives
    * (directive name, array of attributes). */
    private static TreeMap directiveJspData;
    private static TreeMap directiveTagFileData;
    
    private static final TokenID[] JSP_BRACKET_SKIP_TOKENS = new TokenID[] {
                JavaTokenContext.LINE_COMMENT,
                JavaTokenContext.BLOCK_COMMENT,
                JavaTokenContext.CHAR_LITERAL,
                JavaTokenContext.STRING_LITERAL,
                JspTagTokenContext.ATTR_VALUE,
                JspTagTokenContext.COMMENT
            };

    protected FileObject fobj;
    
    /** Content language SyntaxSupport cached for getContentLanguageSyntaxSupport */
    private ExtSyntaxSupport contentLanguageSyntaxSupport = null;
    
    /** Special bracket finder is used when caret is in JSP context */
    private boolean useCustomBracketFinder = true;
    
    private boolean isXmlSyntax = false;

    /** Creates new HTMLSyntaxSupport */
    
    public JspSyntaxSupport(BaseDocument doc, boolean isXml) {
        super(doc);
        fobj = (doc == null) ? null : NbEditorUtilities.getDataObject(doc).getPrimaryFile();
        isXmlSyntax = isXml;
    }
    
    public JspSyntaxSupport(BaseDocument doc) {
        this(doc, false);
    }
    
    
    
    public boolean isXmlSyntax(){
        return isXmlSyntax;
    }
    
    private JspParserAPI.ParseResult getParseResult() {
        JspParserAPI.ParseResult result = JspUtils.getCachedParseResult(getDocument(), fobj, true, false);
        if (result == null) {
            result = JspUtils.getCachedParseResult(getDocument(), fobj, false, false);
        }
        return result;
    }
    
    private Map getJspPrefixMapper() {
        // PENDING - must also take xmlPrefixMapper into account
        JspParserAPI.ParseResult result = getParseResult();
        if (result != null) {
            if (result.isParsingSuccess()) {
                // PENDING - can we somehow get incomplete parsed information ?
                return result.getPageInfo().getJspPrefixMapper();
            }
        }
        return null;
    }
    
    private Map getTagLibraries() {
        JspParserAPI.ParseResult result = getParseResult();
        if (result != null) {
            return result.getPageInfo().getTagLibraries();
        }
        return null;
    }
    
    private TagLibraryInfo getTagLibrary(String prefix) {
        Map mapper = getJspPrefixMapper();
        if (mapper != null) {
            Object uri = mapper.get(prefix);
            if (uri != null) {
                Map taglibs = getTagLibraries();
                if (taglibs != null) {
                    return (TagLibraryInfo)taglibs.get(uri);
                }
            }
        }
        return null;
    }
    
    protected SyntaxSupport createSyntaxSupport(Class syntaxSupportClass) {
        if (syntaxSupportClass.isAssignableFrom (JspJavaSyntaxSupport.class)) {
            return new JspJavaSyntaxSupport(getDocument(), this);
        }
        SyntaxSupport support = super.createSyntaxSupport(syntaxSupportClass);
        if (support != null)
            return support;
        //System.out.println("JspSyntaxSupport- createSyntaxSupport - " + NbEditorUtilities.getMimeType(getDocument()) );
        
        EditorKit kit;
        // try the content language support
        kit = JEditorPane.createEditorKitForContentType(JspUtils.getContentLanguage());
        if (kit instanceof BaseKit) {
            support = ((BaseKit)kit).createSyntaxSupport(getDocument());
            if (support != null)
                return support;
        }
        // try the scripting language support
        kit = JEditorPane.createEditorKitForContentType(JspUtils.getScriptingLanguage());
        if (kit instanceof BaseKit) {
            support = ((BaseKit)kit).createSyntaxSupport(getDocument());
            if (support != null)
                return support;
        }
        return null;
    }

    /** Returns SyntaxSupport corresponding to content type of JSP data object. 
     *  HTMLSyntaxSupport is used when we can't find it. */
    protected ExtSyntaxSupport getContentLanguageSyntaxSupport() {
        if (contentLanguageSyntaxSupport != null) {
            return contentLanguageSyntaxSupport;
        }
        
        EditorKit kit =
            JEditorPane.createEditorKitForContentType(JspUtils.getContentLanguage());
        if (kit instanceof BaseKit) {
            SyntaxSupport support = ((BaseKit)kit).createSyntaxSupport(getDocument());
            if (support != null && support instanceof ExtSyntaxSupport) {
                contentLanguageSyntaxSupport = (ExtSyntaxSupport) support;
                return contentLanguageSyntaxSupport;
            }
        }
        return (ExtSyntaxSupport)get( org.netbeans.editor.ext.html.HTMLSyntaxSupport.class );
    }

    public int checkCompletion(JTextComponent target, String typedText, boolean visible ) {
        SyntaxElement elem;
        try{
            elem = getElementChain( target.getCaret().getDot() );
        } catch (BadLocationException ecx ) {
            return COMPLETION_HIDE;
        }
        if (elem == null) return COMPLETION_HIDE;

        //final JspDataObject jspdo;
        char first = typedText.charAt(0);
        switch (elem.getCompletionContext()) {
                // TAG COMPLETION
                case JspSyntaxSupport.TAG_COMPLETION_CONTEXT :
                    if (err.isLoggable (ErrorManager.INFORMATIONAL)) err.log ("TAG_COMPLETION_CONTEXT");   // NOI18N
                    if( !visible && first == ' ' || first == ':' ) return COMPLETION_POPUP;
                    if( visible && first == '>' ) return COMPLETION_HIDE;
                    return visible ? COMPLETION_POST_REFRESH : COMPLETION_CANCEL;

                // ENDTAG COMPLETION
                case JspSyntaxSupport.ENDTAG_COMPLETION_CONTEXT :
                    if (err.isLoggable (ErrorManager.INFORMATIONAL)) err.log ("ENDTAG_COMPLETION_CONTEXT" );   // NOI18N
                    if( visible && first == '>' ) return COMPLETION_HIDE;
                    return visible ? COMPLETION_POST_REFRESH : COMPLETION_CANCEL;

                // DIRECTIVE COMPLETION
                case JspSyntaxSupport.DIRECTIVE_COMPLETION_CONTEXT :
                    if (err.isLoggable (ErrorManager.INFORMATIONAL)) err.log ("DIRECTIVE_COMPLETION_CONTEXT");   // NOI18N
                    if( !visible && first == '@' || first == ' ' ) return COMPLETION_POPUP;
                    if( visible && first == '=' || first == '>' ) return COMPLETION_HIDE;
                    return visible ? COMPLETION_POST_REFRESH : COMPLETION_CANCEL;

                // CONTENT LANGUAGE
                case JspSyntaxSupport.CONTENTL_COMPLETION_CONTEXT :
                    if (err.isLoggable (ErrorManager.INFORMATIONAL)) err.log ("CONTENTL_COMPLETION_CONTEXT");   // NOI18N
                    ExtSyntaxSupport support = getContentLanguageSyntaxSupport();
                    if (support != null) {
                        return support.checkCompletion( target, typedText, visible );
                    }
                    break;
//                    TBD:Combining
//                    return new CompletionQuery.DefaultResult( jspList, "JSP completion4" );
                                        
                case JspSyntaxSupport.SCRIPTINGL_COMPLETION_CONTEXT :
                    if (err.isLoggable (ErrorManager.INFORMATIONAL)) err.log ("SCRIPTINGL_COMPLETION_CONTEXT" );   // NOI18N
                    if (JspUtils.getScriptingLanguage().equals ("text/x-java")) { // NOI18N
                        return ((ExtSyntaxSupport)get( org.netbeans.editor.ext.java.JavaSyntaxSupport.class )).checkCompletion( target, typedText, visible );
                    }
                    break;
        }
        return COMPLETION_HIDE;
    }
    
    /** Returns offset where the next offset after this offset starts. */
    private final int getTokenEnd( TokenItem item ) {
        if (item == null)
            return 0; //getDocument().getLength();
        return item.getOffset() + item.getImage().length();
    }
    
    /** Filters list of strings so only strings starting 
    * with a given prefix are returned in the new List. */
    public final List filterList(List toFilter, String prefix) {
        List newList = new ArrayList();
        Object item;
        for (int i = 0; i < toFilter.size(); i++) {
            item = toFilter.get(i);
            String txt;
            if (item instanceof TagInfo) 
                txt = ((TagInfo)item).getTagName ();
            else if (item instanceof TagAttributeInfo) 
                txt = ((TagAttributeInfo)item).getName ();
            else
                txt = (String)item;
            
            if (txt != null && txt.startsWith(prefix)) {
                newList.add(item);
            }
        }
        return newList;
    }
    
    /** Gets all 'jsp prefixes' whose 'string prefix' matches complPrefix as a list of Strings. */
    protected final List getTagPrefixes(String complPrefix) {
        return filterList(getAllTagPrefixes(), complPrefix);
    }
    
    /** Gets all tags whose 'string prefix' matches complPrefix as a list of Strings. 
    * Assumes that complPrefix also includes the 'jsp prefix'.
    */
    protected final List getTags(String complPrefix) {
        int colonIndex = complPrefix.indexOf(":");  // NOI18N
        if (colonIndex == -1)
            throw new IllegalArgumentException();
        return getTags(complPrefix.substring(0, colonIndex), 
                       complPrefix.substring(colonIndex + 1));
    }
    
    /** Gets all tags whose 'string prefix' matches complPrefix and whose 'jsp prefix'
    * is tagPrefix as a list of Strings. 
    * Assumes that complPrefix does not include the 'jsp prefix'.
    */
    protected final List getTags(String tagPrefix, String complPrefix) {
        return filterList(getAllTags(tagPrefix), complPrefix);
    }
    
    /** Gets attributes for tag whose prefix + name
    * is tagPrefixName as a list of Strings. 
    * The attribute's 'string prefix' must match complPrefix.
    */
    protected final List getTagAttributes(String tagPrefixName, String complPrefix) {
        int colonIndex = tagPrefixName.indexOf(":");    // NOI18N
        if (colonIndex == -1)
            throw new IllegalArgumentException();
        return getTagAttributes(tagPrefixName.substring(0, colonIndex), 
                       tagPrefixName.substring(colonIndex + 1), complPrefix);
    }
    
    /** Gets attributes for tag whose 'jsp prefix'
    * is tagPrefix and whose tag name is tagName as a list of Strings. 
    * The attribute's 'string prefix' must match complPrefix.
    */
    protected final List getTagAttributes(String tagPrefix, String tagName, String complPrefix) {
        return filterList(getAllTagAttributes(tagPrefix, tagName), complPrefix);
    }
    
    /** Gets all directives whose 'string prefix' matches complPrefix as a list of Strings. */
    protected final List getDirectives(String complPrefix) {
        return filterList(getAllDirectives(), complPrefix);
    }
    
    /** Gets attributes for directive <code>directive</code> as a list of Strings.
    * The attribute's 'string prefix' must match complPrefix.  */
    protected final List getDirectiveAttributes(String directive, String complPrefix) {
        return filterList(getAllDirectiveAttributes(directive), complPrefix);
    }
    
    /** 
    *  Returns a list of strings - prefixes available in this support context (JSP file).
    */
    protected List getAllTagPrefixes() {
        List items = new ArrayList();
        
        // jsp: prefix
        items.add(STANDARD_JSP_PREFIX);
        
        Map mapper = getJspPrefixMapper();
        if (mapper != null) {
            // sort it
            TreeSet ts = new TreeSet();
            ts.addAll(mapper.keySet());
            items.addAll(ts);
        }
        // prefixes for tag libraries
/*        TagLibParseSupport support = (dobj == null) ? 
            null : (TagLibParseSupport)dobj.getCookie(TagLibParseSupport.class);
        if (support != null) {
            // add all prefixes from the support
            TagLibParseSupport.TagLibData[] tagLibData = support.getTagLibEditorData().getTagLibData();
            for (int i = 0; i < tagLibData.length; i++) 
                items.add(tagLibData[i].getPrefix());
        }
*/        
        return items;
    }
    
    /**  Returns a list of strings - tag names available for a particular prefix.
     */
    protected List getAllTags(String prefix) {
        List items = new ArrayList();
        
        
        
        // standard JSP tags (jsp:)
        initCompletionData();
        if (STANDARD_JSP_PREFIX.equals(prefix)) {
            TagInfo[] stanTagDatas = getTagInfos();
            for (int i=0; i<stanTagDatas.length; i++) {
                items.add (stanTagDatas[i]);
            }
        }

        TagLibraryInfo info = getTagLibrary(prefix);
        if (info != null) {
            TagInfo[] tags = info.getTags();
            if (tags != null) {
                for (int i = 0; i < tags.length; i++) {
                    items.add(tags[i]);
                }
            }
        }
        // tags from tag libraries
 /*       TagLibParseSupport support = (dobj == null) ? 
            null : (TagLibParseSupport)dobj.getCookie(TagLibParseSupport.class);
        if (support != null) {
            // add all tags for the given prefix
            TagLibParseSupport.TagLibData tagLibData = support.getTagLibEditorData().getTagLibData(prefix);
            if (tagLibData != null) {
                TagLibraryInfo tli = (TagLibraryInfo)tagLibData.getTagLibraryInfo();
                if (tli != null) {
                    TagInfo[] tags = tli.getTags();
                    if (tags != null) {
                        for (int i = 0; i < tags.length; i++) {
                            items.add(tags[i]);
                        }
                    }
                }
            }
        }
*/        
        return items;
    }
    
    /** Should be overriden ny subclasses to support JSP 1.1.
    *  Returns a list of strings - attribute names available for a particular prefix and tag name.
    */
    protected List getAllTagAttributes(String prefix, String tag) {
        List items = new ArrayList();
        
        // attributes for standard JSP tags (jsp:)
        initCompletionData();
        if (STANDARD_JSP_PREFIX.equals(prefix)) {
            TagInfo[] stanTagDatas = getTagInfos();
            for (int i=0; i<stanTagDatas.length; i++) {
                if (stanTagDatas[i].getTagName ().equals (tag)) {
                    TagAttributeInfo[] attrs = stanTagDatas[i].getAttributes ();
                    for (int j=0; j<attrs.length; j++) 
                        items.add (attrs[j]);
                    break;
                }
            }
        }
        
        TagLibraryInfo info = getTagLibrary(prefix);
        if (info != null) {
            TagInfo tagInfo = info.getTag(tag);
            if (tagInfo != null) {
                TagAttributeInfo[] attributes = tagInfo.getAttributes();
                for (int i = 0; i < attributes.length; i++) 
                    items.add(attributes[i]);
            }    
        }
        // attributes for tags from libraries
/*        TagLibParseSupport support = (dobj == null) ? 
            null : (TagLibParseSupport)dobj.getCookie(TagLibParseSupport.class);
        if (support != null) {
            // add all attributes for the given prefix and tag name
            TagLibParseSupport.TagLibData tagLibData = support.getTagLibEditorData().getTagLibData(prefix);
            if (tagLibData != null) {
                TagLibraryInfo tli = (TagLibraryInfo)tagLibData.getTagLibraryInfo();
                if (tli != null) {
                    TagInfo tagInfo = tli.getTag(tag);
                    if (tagInfo != null) {
                        TagAttributeInfo[] attributes = tagInfo.getAttributes();
                        for (int i = 0; i < attributes.length; i++) 
                            items.add(attributes[i]);
                    }    
                }
            }
        }
*/        
        return items;
    }
    
    /** Should be overriden ny subclasses to support JSP 1.1. */
    protected List getAllDirectives() {
        initCompletionData();
        List items = new ArrayList();
        
        //Is xml syntax? => return nothing.
        if (isXmlSyntax) return items;
        
        TreeMap directiveData;
        if(NbEditorUtilities.getMimeType(getDocument()).equals(JspUtils.TAG_MIME_TYPE))
            directiveData = directiveTagFileData;
        else 
            directiveData = directiveJspData;
        for (Iterator it = directiveData.keySet().iterator(); it.hasNext();) {
            items.add(it.next());
        }
        return items;
    }
    
    /** Should be overriden ny subclasses to support JSP 1.1. */
    protected List getAllDirectiveAttributes(String directive) {
        initCompletionData();
        List items = new ArrayList();
        //Is xml syntax? => return nothing.
        if (isXmlSyntax) return items;
        
        TreeMap directiveData;
        if(NbEditorUtilities.getMimeType(getDocument()).equals(JspUtils.TAG_MIME_TYPE))
            directiveData = directiveTagFileData;
        else
            directiveData = directiveJspData;
        String[] attributes = (String[])directiveData.get(directive);
        if (attributes != null) {
            for (int i = 0; i < attributes.length; i++)
                items.add(attributes[i]);
        }
        return items;
    }
    
   public PageInfo.BeanData[] getBeanData() {
       JspParserAPI.ParseResult result = getParseResult();
       if (result != null) {
           return result.getPageInfo().getBeans();
       }
        /*TagLibParseSupport support = (dobj == null) ? 
            null : (TagLibParseSupport)dobj.getCookie(TagLibParseSupport.class);
        return support.getTagLibEditorData().getBeanData();*/
       return null;
    }
    
    public boolean isErrorPage() {
       JspParserAPI.ParseResult result = getParseResult();
       if (result != null) {
           return result.getPageInfo().isErrorPage();
       }
        /*TagLibParseSupport support = (dobj == null) ? 
            null : (TagLibParseSupport)dobj.getCookie(TagLibParseSupport.class);
        return support.getTagLibEditorData().isErrorPage ();*/
        return false;
    }

    
    /**
     * The mapping of the 'global' tag library URI to the location
     * (resource path) of the TLD associated with that tag library.
     * The location is returned as a String array:
     *    [0] The location
     *    [1] If the location is a jar file, this is the location
     *        of the tld.
     */
    public Map getTagLibraryMappings() {
        if (fobj == null) {
            return null;
        }
        return JspUtils.getTaglibMap(getDocument(), fobj);
    }
    
    
    private static void initCompletionData() {
        if (standardTagDatas == null) {
            standardTagDatas = new TagInfo[] {  
              new TagInfo ("fallback", null, TagInfo.BODY_CONTENT_JSP, "alternative text to browsers that do not support OBJECT or EMBED",    // NOI18N
                null, null, new TagAttributeInfo[] {}),
              new TagInfo ("forward", null, TagInfo.BODY_CONTENT_JSP, "forwards request to another URL",    // NOI18N
                null, null, new TagAttributeInfo[] { new TagAttributeInfo ("page", true, "", true)}),       // NOI18N
              new TagInfo ("getProperty", null, TagInfo.BODY_CONTENT_EMPTY, "gets bean property",           // NOI18N
                null, null, new TagAttributeInfo[] { new TagAttributeInfo ("name", false, "", false),       // NOI18N
                                                     new TagAttributeInfo ("property", false, "", false)}), // NOI18N
              new TagInfo ("include", null, TagInfo.BODY_CONTENT_JSP, "includes another page",              // NOI18N
                null, null, new TagAttributeInfo[] { new TagAttributeInfo ("flush", true, "", false),       // NOI18N
                                                     new TagAttributeInfo ("page", true, "", true)}),       // NOI18N
              new TagInfo ("param", null, TagInfo.BODY_CONTENT_EMPTY, "specifies parameter",                // NOI18N
                null, null, new TagAttributeInfo[] { new TagAttributeInfo ("name", true, "", false),        // NOI18N
                                                     new TagAttributeInfo ("value", true, "", true)}),      // NOI18N
              new TagInfo ("params", null, TagInfo.BODY_CONTENT_JSP, "contains parameters",                 // NOI18N
                null, null, new TagAttributeInfo[] {}),
              new TagInfo ("plugin", null, TagInfo.BODY_CONTENT_JSP, "adds plugin to a page",               // NOI18N
                null, null, new TagAttributeInfo[] { new TagAttributeInfo ("align", false, "", false),      // NOI18N
                                                     new TagAttributeInfo ("archive", false, "", false),    // NOI18N
                                                     new TagAttributeInfo ("code", true, "", false),        // NOI18N
                                                     new TagAttributeInfo ("codebase", false, "", false),   // NOI18N
                                                     new TagAttributeInfo ("height", false, "", false),     // NOI18N
                                                     new TagAttributeInfo ("hspace", false, "", false),     // NOI18N
                                                     new TagAttributeInfo ("iepluginurl", false, "", false),// NOI18N
                                                     new TagAttributeInfo ("jreversion", false, "", false), // NOI18N
                                                     new TagAttributeInfo ("name", false, "", false),       // NOI18N
                                                     new TagAttributeInfo ("nspluginurl", false, "", false),// NOI18N
                                                     new TagAttributeInfo ("type", true, "", false),        // NOI18N
                                                     new TagAttributeInfo ("vspace", false, "", false),     // NOI18N
                                                     new TagAttributeInfo ("width", false, "", false)}),    // NOI18N
              new TagInfo ("setProperty", null, TagInfo.BODY_CONTENT_EMPTY, "sets bean property",           // NOI18N
                null, null, new TagAttributeInfo[] { new TagAttributeInfo ("name", false, "", true),        // NOI18N
                                                     new TagAttributeInfo ("param", false, "", false),      // NOI18N
                                                     new TagAttributeInfo ("property", false, "", false),   // NOI18N
                                                     new TagAttributeInfo ("value", false, "", true)}),     // NOI18N
              new TagInfo ("useBean", null, TagInfo.BODY_CONTENT_JSP, "loads a bean to be used in JSP",     // NOI18N
                null, null, new TagAttributeInfo[] { new TagAttributeInfo ("beanName", false, "", false),   // NOI18N
                                                     new TagAttributeInfo ("class", false, "", false),      // NOI18N
                                                     new TagAttributeInfo ("id", false, "", false),         // NOI18N
                                                     new TagAttributeInfo ("scope", false, "", false),      // NOI18N
                                                     new TagAttributeInfo ("type", false, "", false)})     // NOI18N
            };
            
        }
    
        if (directiveJspData == null) {
             directiveJspData = new TreeMap();
            // fill in the data, both directive names and attribute names should be in alphabetical order
            directiveJspData.put("include", new String[]   // NOI18N
                {"file"});      // NOI18N
            directiveJspData.put("page", new String[]      // NOI18N
                {"autoFlush", "buffer", "contentType", "errorPage", "extends",  // NOI18N 
                 "import", "info", "isErrorPage", "isThreadSafe", "language", "pageEncoding", "session"});  // NOI18N
            directiveJspData.put("taglib", new String[]    // NOI18N
            {"prefix", "uri"}); // NOI18N
            
        }
        
        if (directiveTagFileData == null){
            directiveTagFileData = new TreeMap();
            directiveTagFileData.put("attribute", new String[]      // NOI18N
                    {"description", "fragment", "name", "required", "rtexprvalue", "type"});// NOI18N 
            // fill in the data, both directive names and attribute names should be in alphabetical order
            directiveTagFileData.put("include", directiveJspData.get("include"));      // NOI18N
            directiveTagFileData.put("tag", new String[]      // NOI18N
                    {"body-content", "description", "display-name","dynamic-attributes", // NOI18N
                     "example",  "import", "isELEnabled", "isScriptingEnabled", // NOI18N
                     "large-icon", "language", "pageEncoding", "small-icon" //NOI18N
                      }); 
            directiveTagFileData.put("taglib", directiveJspData.get("taglib")); // NOI18N
            directiveTagFileData.put("variable", new String[]      // NOI18N
                    {"declare", "description", "fragment","name-from-attribute", // NOI18N
                     "name-given",  "scope", "variable-class" // NOI18N
                      }); 
        }
        
        
        
        if (xmlJspTagDatas == null) {
            TagInfo[] commonXMLTagDatas;
            commonXMLTagDatas = new TagInfo[]{
                new TagInfo ("declaration", null, TagInfo.BODY_CONTENT_JSP, "",                 // NOI18N
                  null, null, new TagAttributeInfo[] {}),
               new TagInfo ("expression", null, TagInfo.BODY_CONTENT_JSP, "",                 // NOI18N
                  null, null, new TagAttributeInfo[] {}),
               new TagInfo ("scriptlet", null, TagInfo.BODY_CONTENT_JSP, "",                 // NOI18N
                  null, null, new TagAttributeInfo[] {}), 
               new TagInfo ("root", null, TagInfo.BODY_CONTENT_JSP, "",                         // NOI18N
                  null, null, new TagAttributeInfo[] {})
            };
                  
            String [] attr = (String[])directiveJspData.get("page");                // NOI18N
            TagAttributeInfo[] tagAttrInfos = new TagAttributeInfo [attr.length];
            for (int i = 0; i < attr.length; i++)
                tagAttrInfos[i] = new TagAttributeInfo (attr[i], false, "",  false);
            
            xmlJspTagDatas = new TagInfo[] {
                new TagInfo ("directive.page", null, TagInfo.BODY_CONTENT_EMPTY, "",   // NOI18N
                    null, null, tagAttrInfos),               
            };
            
            ArrayList list = new ArrayList();
            for (int i = 0; i < xmlJspTagDatas.length; i++)
                list.add(xmlJspTagDatas[i]);
            for (int i = 0; i < standardTagDatas.length; i++)
                list.add(standardTagDatas[i]);
            for (int i = 0; i < commonXMLTagDatas.length; i++)
                list.add(commonXMLTagDatas[i]);
         
            Collections.sort(list,  new Comparator() {
                public int compare(Object o1, Object o2) {
                    return ((TagInfo)o1).getTagName().compareTo(((TagInfo)o2).getTagName());
                }
            });
            xmlJspTagDatas = new TagInfo[list.size()];
            for (int i = 0; i < list.size(); i++)
                xmlJspTagDatas[i] = (TagInfo)list.get(i);
        
            attr = (String[])directiveTagFileData.get("tag"); // NOI18N
            tagAttrInfos = new TagAttributeInfo [attr.length];
            for (int i = 0; i < attr.length; i++)
                tagAttrInfos[i] = new TagAttributeInfo (attr[i], false, "",  false);
            
            attr = (String[])directiveTagFileData.get("attribute"); // NOI18N
            TagAttributeInfo[] attributeAttrInfos = new TagAttributeInfo [attr.length];
            for (int i = 0; i < attr.length; i++)
                attributeAttrInfos[i] = new TagAttributeInfo (attr[i], false, "",  false);
            
            attr = (String[])directiveTagFileData.get("variable");  // NOI18N
            TagAttributeInfo[] variableAttrInfos = new TagAttributeInfo [attr.length];
            for (int i = 0; i < attr.length; i++)
                variableAttrInfos[i] = new TagAttributeInfo (attr[i], false, "",  false);
            
            xmlTagFileTagDatas = new TagInfo[] {
                new TagInfo ("directive.tag", null, TagInfo.BODY_CONTENT_EMPTY, "", // NOI18N
                    null, null, tagAttrInfos),
                new TagInfo ("directive.attribute", null, TagInfo.BODY_CONTENT_EMPTY, "", // NOI18N
                    null, null, attributeAttrInfos),
                new TagInfo ("directive.variable", null, TagInfo.BODY_CONTENT_EMPTY, "", // NOI18N
                    null, null, variableAttrInfos),
            };
            
            list = new ArrayList();
            for (int i = 0; i < xmlTagFileTagDatas.length; i++)
                list.add(xmlTagFileTagDatas[i]);
            for (int i = 0; i < standardTagDatas.length; i++)
                list.add(standardTagDatas[i]);
            for (int i = 0; i < commonXMLTagDatas.length; i++)
                list.add(commonXMLTagDatas[i]);
         
            Collections.sort(list,  new Comparator() {
                public int compare(Object o1, Object o2) {
                    return ((TagInfo)o1).getTagName().compareTo(((TagInfo)o2).getTagName());
                }
            });
            xmlTagFileTagDatas = new TagInfo[list.size()];
            for (int i = 0; i < list.size(); i++)
                xmlTagFileTagDatas[i] = (TagInfo)list.get(i);
                    
        }
                  
        
    }
    
    private TagInfo[] getTagInfos (){
        TagInfo[] rValue;
        if ( isXmlSyntax()){
            if (NbEditorUtilities.getMimeType(getDocument()).equals(JspUtils.TAG_MIME_TYPE))
                rValue = xmlTagFileTagDatas;
            else
                rValue = xmlJspTagDatas;
        }
        else
            rValue = standardTagDatas;
        return rValue;
    }
    
    public String toString() {
        return printJspCompletionInfo();
    }

    /** Debug output of all tags and directives. */
    private String printJspCompletionInfo() {
        StringBuffer output = new StringBuffer();
        
        output.append("TAGS\n");    // NOI18N
        List tagPrefixes = getTagPrefixes("");  // NOI18N
        for (int i = 0; i < tagPrefixes.size(); i++) {
            String prefix = (String)tagPrefixes.get(i);
            output.append("  " + prefix + "\n");    // NOI18N
            List tags = getTags(prefix, "");    // NOI18N
            for (int j = 0; j < tags.size(); j++) {
                String tagName = (String)tags.get(j);
                output.append("    " + tagName + "\n"); // NOI18N
                List attributes = getTagAttributes(prefix, tagName, "");// NOI18N
                for (int k = 0; k < attributes.size(); k++) {
                    String attribute = (String)attributes.get(k);
                    output.append("      " + attribute + "\n");// NOI18N
                }
            }
        }
        
        output.append("DIRECTIVES\n");// NOI18N
        List directives = getDirectives("");// NOI18N
        for (int i = 0; i < directives.size(); i++) {
            String directive = (String)directives.get(i);
            output.append("  " + directive + "\n");// NOI18N
            List attributes = getDirectiveAttributes(directive, "");// NOI18N
            for (int k = 0; k < attributes.size(); k++) {
                String attribute = (String)attributes.get(k);
                output.append("      " + attribute + "\n");// NOI18N
            }
        }
        
        return output.toString();
    }
    
    /** Returns an item on offset <code>offset</code>
     * This method is largely a workaround for a bug in getTokenChain().
     * If offset falls right between two items, returns one which is just before
     * offset. If <code>offset == 0</code>, retruns null. */
    public TokenItem getItemAtOrBefore(int offset) throws BadLocationException {
        TokenItem backItem = null;
        int chainLength = 100;
        while (backItem == null) {
            if (offset < getDocument().getLength()) {
                backItem = getTokenChain( offset, 
                    Math.min(offset + chainLength, getDocument().getLength())/*, false*/ );
            }
            else {
                // @ end of document
                backItem = getTokenChain (Math.max (offset-50, 0), offset);
            }
                
            if (chainLength++ > 1000)
                break;
        }
        if (backItem == null)
            return null;
        
        // forward to the offset where our token definitely is
//System.out.println("looking for item at offset " + offset);        
//System.out.println("backitem " + backItem);
        TokenItem item;
        while (true) {
            item = backItem.getNext();
//System.out.println("looking at item " + item);
            if (item == null) {
                item = backItem;
//System.out.println("break1");
                break;
            }
            if (item.getOffset() > offset) {
                item = backItem;
//System.out.println("break2");
                break;
            }
            backItem = item;
//System.out.println("backitem2 " + backItem);
        }
        
//System.out.println("REAL Token at offset " + offset + " is " + item );
        TokenItem adjustedItem = (item.getOffset() == offset) ? 
            item.getPrevious() : item;
//System.out.println("ADJUSTED Token at offset " + offset + " is " + adjustedItem );

        TokenID id = (adjustedItem == null) ?
            item.getTokenID() : adjustedItem.getTokenID();
//System.out.println("TokenID (adjusted) at offset " + offset + " is " + id );
        return adjustedItem;
    }
    
    /** Returns SyntaxElement instance for block of tokens, which is either
     * surrounding given offset, or is just after the offset.
     * @param offset offset in document where to search for SyntaxElement
     * @return SyntaxElement surrounding or laying before the offset
     */
    public SyntaxElement getElementChain( int offset ) throws BadLocationException {
        TokenItem item = getItemAtOrBefore(offset);
        if (item == null)
            return null;
        TokenID id = item.getTokenID();
        
        if (id == JspTagTokenContext.COMMENT || 
            id == JspTagTokenContext.ERROR || 
            id == JspTagTokenContext.TEXT ||
            id == JspMultiTokenContext.ERROR) {
//System.out.println("uninteresting JspTag token");
            return null;
        }
        
        if (id == JspTagTokenContext.SYMBOL2) {
//System.out.println("just at symbol");
            if ((getTokenEnd(item) == offset) && isScriptStartToken(item)) {
                return getScriptingChain(item.getNext(), offset);
            }
            if ((getTokenEnd(item) == offset) && isScriptEndToken(item)) {
                TokenItem nextItem = item.getNext();
                if (!isTagDirToken(item))
                    return getContentChain(nextItem, offset);
            }
            return null;
        }
        
        if (id == JspTagTokenContext.TAG ||
            id == JspTagTokenContext.SYMBOL ||
            id == JspTagTokenContext.ATTRIBUTE ||
            id == JspTagTokenContext.ATTR_VALUE ||
            id == JspTagTokenContext.EOL) {
            // may be intetesting: tag, directive, 
            // but may also be a comment. Look back for SYMBOL: <, </, <%@ 
            // or COMMENT
            TokenItem elementStart = item;
            do {
//System.out.println("backtracking, elementStart = " + elementStart);
                if (elementStart == null)
                    return null;
                if (elementStart.getTokenID() == JspTagTokenContext.SYMBOL) {
                    if (elementStart.getImage().equals("<")) {   // NOI18N
                        return getTagOrDirectiveChain(true, elementStart, offset);
                    }
                    if (elementStart.getImage().equals("</")) {   // NOI18N
                        return getEndTagChain(elementStart, offset);
                    }
                    if (elementStart.getImage().equals("<%@")) {   // NOI18N
                        return getTagOrDirectiveChain(false, elementStart, offset);
                    }
                }
                if (elementStart.getTokenID() == JspTagTokenContext.COMMENT) {
                    return null;
                }
                elementStart = elementStart.getPrevious();
            }
            while (true);
        }
        
        // now we are either in the scripting language or in the content language.
        // to determine which one it is, look back for SYMBOL2: <%, <%=, <%!
        // (scripting language) or for any other JspTag token (content language).
        // if nothing found, we are in the content language
        if (isScriptingOrContentToken(item)) {
            TokenItem elementStart = item;
            do {
                if (elementStart.getPrevious() == null) {
                    // we backtracked to the beginning without finding 
                    // a distinguishing symbol - we are in the content language
                    return getContentChain(elementStart, offset);
                }
                elementStart = elementStart.getPrevious(); // now non-null
                if (!isScriptingOrContentToken(elementStart)) {
                    // something from JSP
                    if (isScriptStartToken(elementStart)) {
                        return getScriptingChain(elementStart.getNext(), offset);
                    }
                    else {
                        return getContentChain(elementStart.getNext(), offset);
                    }
                }
            }
            while (true);
        }
        
//System.out.println("muddy waters");
        return null;
    }
    
    /** Returns true if item is a starting symbol for a block in 
     * the scripting language. */
    private boolean isScriptStartToken(TokenItem item) {
        if (item == null)
            return false;
        TokenID id = item.getTokenID();
        if (id == JspTagTokenContext.SYMBOL2) {
            String image = item.getImage();
            if (image.equals("<%") ||   // NOI18N
                image.equals("<%=") ||  // NOI18N
                image.equals("<%!"))    // NOI18N
                return true;
        }
        return false;
    }
    
    /** Returns true if item is an ending symbol for a block in 
     * the scripting language. */
    private boolean isScriptEndToken(TokenItem item) {
        if (item == null)
            return false;
        TokenID id = item.getTokenID();
        if (id == JspTagTokenContext.SYMBOL2) {
            String image = item.getImage();
            if (image.equals("%>")) // NOI18N
                return true;
        }
        return false;
    }
    
    /** Returns true if item is an item which can be INSIDE 
     * a JSP tag or directive (i.e. excuding delimeters). */
    private boolean isInnerTagDirToken(TokenItem item) {
        if (!isTagDirToken(item))
            return false;
        TokenID id = item.getTokenID();
        if (id == JspTagTokenContext.SYMBOL) {
            String image = item.getImage();
            if (image.equals("<") ||    // NOI18N
                image.equals("</") ||   // NOI18N
                image.equals("<%@") ||  // NOI18N
                image.equals("%>") ||   // NOI18N
                image.equals(">") ||    // NOI18N
                image.equals("/>"))     // NOI18N
                return false;
        }
        return true;
    }
        
    /** Returns true if item is an item which can be INSIDE 
     * a JSP tag or directive (i.e. excuding delimeters). */
    private boolean isTagDirToken(TokenItem item) {
        if (item == null)
            return false;
        TokenID id = item.getTokenID();
        if (id == null)
            return false;
        if ((id != JspTagTokenContext.TEXT) &&
            (id != JspTagTokenContext.ERROR) &&
            (id != JspTagTokenContext.TAG) &&
            (id != JspTagTokenContext.SYMBOL) &&
            (id != JspTagTokenContext.ATTRIBUTE) &&
            (id != JspTagTokenContext.ATTR_VALUE) &&
            (id != JspTagTokenContext.EOL)) {
            return false;
        }
        // PENDING - EOL can still be a comment
        return true;
    }

    /** Return true if this item does not belong to JSP syntax
     *  and belongs to one of the syntaxes we delegate to. */
    private boolean isScriptingOrContentToken(TokenItem item) {
        if (item == null)
            return true;
        TokenID id = item.getTokenID();
        if (id == null)
            return true;
        if ((id == JspTagTokenContext.TEXT) ||
            (id == JspTagTokenContext.ERROR) ||
            (id == JspTagTokenContext.TAG) ||
            (id == JspTagTokenContext.SYMBOL) ||
            (id == JspTagTokenContext.COMMENT) ||
            (id == JspTagTokenContext.ATTRIBUTE) ||
            (id == JspTagTokenContext.ATTR_VALUE) ||
            (id == JspTagTokenContext.SYMBOL2) ||
            (id == JspTagTokenContext.EOL) ||
            (id == JspMultiTokenContext.ERROR))
            return false;
        return true;
    }
    
    public boolean isValueBeginning(String text) {
        if (text.trim().endsWith("\"\""))   // NOI18N
            return false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c != ' ') &&
                (c != '=') &&
                (c != '"'))
                return false;
        }
        return true;
    }
        
    // ------- METHODS FOR CONSTRUCTING SEMANTICALLY LIKNKED CHAINS OF TOKENS ------
    
    /** Gets an element representing a tag or directive starting with token item firstToken. */
    private SyntaxElement getTagOrDirectiveChain(boolean tag, TokenItem firstToken, int offset) {
        TokenItem item = firstToken.getNext();
        String name = getWholeWord(item, JspTagTokenContext.TAG);
        while ((item != null) && (item.getTokenID() == JspTagTokenContext.TAG))
            item = item.getNext();
        TreeMap attributes = new TreeMap();
        while (isInnerTagDirToken(item)) {
            // collect the attributes
            if (item.getTokenID() == JspTagTokenContext.ATTRIBUTE) {
                String attributeName = getWholeWord(item, JspTagTokenContext.ATTRIBUTE);
                // forward to the next non-ATTRIBUTE token
                while ((item != null) && (item.getTokenID() == JspTagTokenContext.ATTRIBUTE))
                    item = item.getNext();
                // find the value
                while ((item != null) && 
                       (item.getTokenID() == JspTagTokenContext.SYMBOL) &&
                       (isValueBeginning(item.getImage())))
                    item = item.getNext();
                StringBuffer value = new StringBuffer();
                while ((item != null) && (item.getTokenID() == JspTagTokenContext.ATTR_VALUE)) {
                    value.append(item.getImage());
                    item = item.getNext();
                    // request time values
                    if ((item != null) && (item.getTokenID() == JspTagTokenContext.SYMBOL2)) {
                        // scripting language - something like request time value of a JSP tag
                        while (!isScriptEndToken(item)) {
                            if (item == null)
                                break;
                            else {
                                value.append(item.getImage());
                                item = item.getNext();
                            }
                        }
                        // now it's a script end token
                        if (item != null) {
                            value.append(item.getImage());
                            item = item.getNext();
                        }
                    }
                }
                String vString = value.toString();
                // cut off the beginning and ending quotes
                if (vString.startsWith("\""))   // NOI18N
                    vString = vString.substring(1);
                if (vString.endsWith("\""))     // NOI18N
                    vString = vString.substring(0, vString.length() - 1);
                attributes.put(attributeName, vString);
                continue;
            }
            if (item.getTokenID() == JspTagTokenContext.SYMBOL2) {
                // scripting language - something like request time value of a JSP tag
                while (!isScriptEndToken(item)) {
                    if (item == null)
                        break;
                    else
                        item = item.getNext();
                }
                // now it's a script end token
                if (item != null)
                    item = item.getNext();
                continue;
            }
            // a token I am not interested in
            item = item.getNext();
        }
        if (tag) {
            boolean endslash= false;
            if (item != null)
                endslash = (item.getImage ().equals ("/>"))? true: false;   // NOI18N
                
            return new SyntaxElement.Tag(this, firstToken.getOffset(), 
                (item != null)? getTokenEnd(item): getDocument ().getLength (), 
                name, attributes, endslash);
        }    
        else {
            return new SyntaxElement.Directive(this, firstToken.getOffset(), 
                (item != null)? getTokenEnd(item): getDocument ().getLength (), 
                name, attributes);
        }    
    }
    
    private SyntaxElement getEndTagChain(TokenItem firstToken, int offset) {
        TokenItem item = firstToken.getNext();
        String name = getWholeWord(item, JspTagTokenContext.TAG);
        while ((item != null) && (item.getTokenID() == JspTagTokenContext.TAG))
            item = item.getNext();
        while (isInnerTagDirToken(item)) {
            item = item.getNext();
        }
        return new SyntaxElement.EndTag(this, firstToken.getOffset(), 
            getTokenEnd(item), name);
    }
    
    private String getWholeWord(TokenItem firstToken, TokenID requestedTokenID) {
        StringBuffer sb = new StringBuffer();
        while ((firstToken != null) && (firstToken.getTokenID() == requestedTokenID)) {
            sb.append(firstToken.getImage());
            firstToken = firstToken.getNext();
        }
        return sb.toString().trim();
    }

    /** Returns an element of scripting language starting with firstToken. 
     * If forstToken is null, returns element representing end of the document. 
     */
    private SyntaxElement getScriptingChain(TokenItem firstToken, int offset) {
        if (firstToken == null) {
            return new SyntaxElement.ScriptingL(this, 
                getDocument().getLength(), getDocument().getLength());
        }
        TokenItem item = firstToken;
        do {
            TokenItem nextItem = item.getNext();
            if (nextItem == null) {
                return new SyntaxElement.ScriptingL(this, 
                    firstToken.getOffset(), getDocument().getLength());
            }
            if (!isScriptingOrContentToken(nextItem))
                return new SyntaxElement.ScriptingL(this, 
                    firstToken.getOffset(), getTokenEnd(item));
            item = nextItem;
        }
        while (true);
    }
    
    /** Returns an element of content language starting with firstToken. 
     * If forstToken is null, returns element representing end of the document. 
     */
    private SyntaxElement getContentChain(TokenItem firstToken, int offset) {
        if (firstToken == null) {
            return new SyntaxElement.ContentL(this, 
                getDocument().getLength(), getDocument().getLength());
        }
        TokenItem item = firstToken;
        do {
            TokenItem nextItem = item.getNext();
            if (nextItem == null) {
                return new SyntaxElement.ContentL(this, 
                    firstToken.getOffset(), getDocument().getLength());
            }
            if (!isScriptingOrContentToken(nextItem))
                return new SyntaxElement.ContentL(this, 
                    firstToken.getOffset(), getTokenEnd(item));
            item = nextItem;
        }
        while (true);
    }

    /** The way how to get previous SyntaxElement in document. It is not intended
     * for direct usage, and thus is not public. Usually, it is called from
     * SyntaxElement's method getPrevious()
     */
    SyntaxElement getPreviousElement( int offset ) throws BadLocationException {
        return offset == 0 ? null : getElementChain( offset - 1 );
    }

    public List getPossibleEndTags (int offset, String pattern) throws BadLocationException {
        SyntaxElement elem = getElementChain( offset );
        Stack stack = new Stack();
        List result = new ArrayList();
        Set found = new HashSet();
        
        if( elem != null ) {
            elem = elem.getPrevious();  // we need smtg. before our </
        } else {    // End of Document
            if( offset > 0 ) {
                elem = getElementChain( offset-1 );
            } else { // beginning of document too, not much we can do on empty doc
                return result;
            }
        }

/*        TagLibParseSupport support = (dobj == null) ? 
            null : (TagLibParseSupport)dobj.getCookie(TagLibParseSupport.class);
*/           
        for( ; elem != null; elem = elem.getPrevious() ) {
            
            if( elem instanceof SyntaxElement.EndTag ) {
                stack.push( ((SyntaxElement.EndTag)elem).getName() );
            } else if( elem instanceof SyntaxElement.Tag ) {
                SyntaxElement.Tag tag = (SyntaxElement.Tag)elem;
                
                if (tag.isClosed ())
                    continue;
                
                String image = tag.getName ();
                String prefix = image.substring (0, image.indexOf (':'));
                String name = image.substring (image.indexOf (':')+1);
                TagInfo ti = null;

/*                if (support != null) {
                    // add all tags for the given prefix
                    TagLibParseSupport.TagLibData tagLibData = support.getTagLibEditorData().getTagLibData(prefix);
                    if (tagLibData != null) {
                        TagLibraryInfo tli = (TagLibraryInfo)tagLibData.getTagLibraryInfo();
                        if (tli != null) {
                            ti = tli.getTag(name);
                        }
                    }
                }*/
                if (STANDARD_JSP_PREFIX.equals (prefix)) { 
                    initCompletionData ();
                    TagInfo[] stanTagDatas = getTagInfos();
                    for (int i=0; i<stanTagDatas.length; i++) {
                        if (stanTagDatas[i].getTagName ().equals (name)) {
                            ti = stanTagDatas[i];
                            break;
                        }
                    }
                }
                    
                if (ti == null) continue; // Unknown tag - ignore

                if( stack.empty() ) {           // empty stack - we are on the same tree deepnes - can close this tag
                    if( image.startsWith( pattern ) && !found.contains( image ) ) {    // add only new items
                        found.add( image );

                        if (ti.getBodyContent ().equalsIgnoreCase (TagInfo.BODY_CONTENT_EMPTY))
                            continue;

                        result.add( new CompletionItem.Tag( "/"+image ) );  // NOI18N
                    }
                    // if( ! tag.hasOptionalEnd() ) break;  // If this tag have required EndTag, we can't go higher until completing this tag
                } else {                        // not empty - we match content of stack
                    if( stack.peek().equals( image ) ) { // match - close this branch of document tree
                        stack.pop();
                    } // else if( ! tag.hasOptionalEnd() ) break; // we reached error in document structure, give up
                }

                // this is error - end of empty tag
                if (ti.getBodyContent ().equalsIgnoreCase (TagInfo.BODY_CONTENT_EMPTY))
                    continue;

                // if( tag.isEmpty() ) continue; // ignore empty Tags - they are like start and imediate end

            }
        }
        
        return result;
    }
    
    public FileObject getFileObject() {
        return fobj;
    }
    
    /** Get the bracket finder that will search for the matching bracket
     * or null if the bracket character doesn't belong to bracket
     * characters.
     *
     * Customized finder recognizes also '<' and '>' as bracket chars. It is set to be used 
     * in findMatchingBlock.
     */
    protected ExtSyntaxSupport.BracketFinder getMatchingBracketFinder (char bracketChar) {
        if (useCustomBracketFinder) {
            JspSyntaxSupport.BracketFinder bf = new JspSyntaxSupport.BracketFinder (bracketChar);
            return bf.isValid ()? bf: null;
        }
	else{
            return super.getMatchingBracketFinder (bracketChar); 
	}
    }
    
    /** Find matching bracket or more generally block
     * that matches with the current position.
     * @param offset position of the starting bracket
     * @param simple whether the search should skip comment and possibly other areas.
     *  This can be useful when the speed is critical, because the simple
     *  search is faster.
     * @return array of integers containing starting and ending position
     *  of the block in the document. Null is returned if there's
     *  no matching block.
     */ 
    public int[] findMatchingBlock(int offset, boolean simpleSearch)
	throws BadLocationException {
	    /* TODO - jsp tag matching
	     */
	    int [] r_value = null;
	    
	    TokenItem token = getItemAtOrBefore ((offset<getDocument().getLength())?offset+1:offset);
	    if (token != null){
		if (token.getTokenContextPath().contains(HTMLTokenContext.contextPath)
			&& token.getImage().charAt(0) != '>'){
		    r_value = getContentLanguageSyntaxSupport().findMatchingBlock(offset, simpleSearch);
		}
		else {		    		    
		    if (token.getTokenContextPath().contains(JspTagTokenContext.contextPath)
			    && token.getTokenID().getNumericID() == JspTagTokenContext.TAG_ID){
			return findMatchingTag (token);
		    }
		    else {
			if (token.getTokenID().getNumericID() == JspTagTokenContext.SYMBOL2_ID){
			    return findMatchingScripletDelimiter (token);
			}
			else{
			    if (isScriptingOrContentToken (token)) {
				useCustomBracketFinder = false;
			    }
			    else {
				useCustomBracketFinder = true;
			    }
			    r_value =  super.findMatchingBlock (offset, simpleSearch);
			}
		    }
		}
	    }
	    
	    return r_value;
    }
    
    private int [] findMatchingScripletDelimiter(TokenItem token){
	if (token.getImage().charAt(0) == '<'){
	    do{
		token = token.getNext();
	    } while (token != null && token.getTokenID().getNumericID() != JspTagTokenContext.SYMBOL2_ID);
	}
	else {
	    do{
		token = token.getPrevious();
	    } while (token != null && token.getTokenID().getNumericID() != JspTagTokenContext.SYMBOL2_ID);
	}
	if (token != null){
	    return new int [] {token.getOffset(), token.getOffset() + token.getImage().length()};
	}
	return null;
    }
    
    private int[] findMatchingTag (TokenItem token){
	// TODO - replanning to the other thread. Now it's in awt thread
	if (token.getTokenID().getNumericID() == JspTagTokenContext.TAG_ID){

	    int start; // possition where the matched tag starts
	    int end;   // possition where the matched tag ends
	    int poss = 0; // how many the same tags is inside the mathed tag
	    String tag = token.getImage().trim();

	    while (token != null && token.getTokenID().getNumericID() != JspTagTokenContext.SYMBOL_ID) {				
		token = token.getPrevious();				
	    }
	    if (token == null) 
		return null;
	    if ((token.getImage().length() == 2) && token.getImage().charAt(1) == '/'){
		while ( token != null){
		    if (token.getTokenID().getNumericID() == JspTagTokenContext.TAG_ID) {
			if (token.getImage().trim().equals(tag)){
			    while (token != null && token.getTokenID().getNumericID() != JspTagTokenContext.SYMBOL_ID) {				
				token = token.getPrevious();				
			    }
			    if (token != null) {
				if (token.getImage().length() == 1){
				    if (poss == 0){
					start = token.getOffset();
					end = token.getOffset()+token.getImage().length()+1;
					token = token.getNext();

					while (token != null && !(token.getTokenID().getNumericID() == JspTagTokenContext.SYMBOL_ID
						&& (token.getImage().charAt(0)== '>' || token.getImage().charAt(0)== '<'))){
					    token = token.getNext();
					}
					if (token != null)
					    end = token.getOffset()+1;
					return new int[] {start, end};
				    }
				    else {
					poss++;
				    }
				}
				if (token.getImage().length() == 2){
				    poss--;
				}
			    }

			}
		    }				    
		    token = token.getPrevious();
		}

	    }
	    else{
		if ((token.getImage().length() == 1) && token.getImage().charAt(0) == '<'){
		    poss = 1;
		    TokenItem hToken;
		    while ( token != null){
			if (token.getTokenID().getNumericID() == JspTagTokenContext.TAG_ID) {
			    if (token.getImage().trim().equals(tag)){
				hToken = token;
				while (token != null && token.getTokenID().getNumericID() != JspTagTokenContext.SYMBOL_ID) {				
				    token = token.getPrevious();				
				}
				if (token != null) {
				    if (token.getImage().length() == 2){
					if (poss == 0){
					    start = token.getOffset();
					    end = hToken.getOffset()+hToken.getImage().length()+1;
					    token = token.getNext();

					    while (token != null && (token.getTokenID().getNumericID() != JspTagTokenContext.SYMBOL_ID
						|| token.getImage().charAt(0)!='>')){
						token = token.getNext();
					    }
					    if (token != null)
						end = token.getOffset()+1;
					    return new int[] {start, end};
					}
					else {
					    poss++;
					}
				    }
				    if (token.getImage().length() == 1){
					poss--;
				    }
				}
				token = hToken;
			    }
			}				    
			token = token.getNext();
		    }
		}
	    }
	}
	return null;
    }
    
    /** Get the array of token IDs that should be skipped when
     * searching for matching bracket. It usually includes comments
     * and character and string constants. Returns empty array by default.
     */
    protected TokenID[] getBracketSkipTokens () {
        return JSP_BRACKET_SKIP_TOKENS;
    }
    
    /** Finder for the matching bracket. It gets the original bracket char
    * and searches for the appropriate matching bracket character.
    */
    public class BracketFinder extends ExtSyntaxSupport.BracketFinder {
        
        BracketFinder (char c) { 
            super (c);
        }
        
        /** Check whether the bracketChar really contains
         * the bracket character. If so assign the matchChar
         * and moveCount variables.
         */
        protected boolean updateStatus() {
            if (super.updateStatus ())
                return true;
            boolean valid = true;
            switch (bracketChar) {
                case '<':
                    matchChar = '>';
                    moveCount = +1;
                    break;
                case '>':
                    matchChar = '<';
                    moveCount = -1;
                    break;
                default:
                    valid = false;
            }
            return valid;
        }
        
        boolean isValid () {
            return (moveCount != 0);
        }

    }    
}
