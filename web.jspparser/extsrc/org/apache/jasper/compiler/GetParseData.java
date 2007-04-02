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

package org.apache.jasper.compiler;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import javax.xml.parsers.*;
import org.apache.jasper.*;
import org.openide.ErrorManager;


/**
 *
 * @author Petr Jiricka
 */
public class GetParseData {

    protected JspCompilationContext ctxt;

    protected Options options;
    private CompilerHacks compHacks;
    private int errorReportingMode;

    private org.netbeans.modules.web.jsps.parserapi.Node.Nodes nbNodes;
    private org.netbeans.modules.web.jsps.parserapi.PageInfo nbPageInfo;
    private Throwable parseException;
    
  
    /** Creates a new instance of ExtractPageData */
    public GetParseData(JspCompilationContext ctxt, int errorReportingMode) {
        this.ctxt = ctxt;
        this.errorReportingMode = errorReportingMode;
        this.options = ctxt.getOptions();
        this.compHacks = new CompilerHacks(ctxt);
    }
    
    public org.netbeans.modules.web.jsps.parserapi.Node.Nodes getNbNodes() {
        return nbNodes;
    }

    public org.netbeans.modules.web.jsps.parserapi.PageInfo getNbPageInfo() {
        return nbPageInfo;
    }
    
    public Throwable getParseException() {
        return parseException;
    }

    /** Code in this method copied over and adapted from Compiler.generateJava() 
     **/
    public void parse() {
        Node.Nodes pageNodes = null;
        PageInfo pageInfo = null;
        String xmlView = null;
        try {
            //String smapStr = null;

            //        long t1=System.currentTimeMillis();

            // Setup page info area
            Compiler comp = compHacks.getCompiler();
            pageInfo = comp.getPageInfo();
            ErrorDispatcher errDispatcher = comp.getErrorDispatcher();


            // pageInfo = new PageInfo(new BeanRepository(ctxt.getClassLoader(),
            //   errDispatcher));

            JspConfig jspConfig = options.getJspConfig();
            JspConfig.JspProperty jspProperty =
                            jspConfig.findJspProperty(ctxt.getJspFile());

            /*
             * If the current uri is matched by a pattern specified in
             * a jsp-property-group in web.xml, initialize pageInfo with
             * those properties.
             */
            pageInfo.setELIgnored(JspUtil.booleanValue(jspProperty.isELIgnored()));
            pageInfo.setScriptingInvalid(JspUtil.booleanValue(jspProperty.isScriptingInvalid()));
            if (jspProperty.getIncludePrelude() != null) {
                pageInfo.setIncludePrelude(jspProperty.getIncludePrelude());
            }
            if (jspProperty.getIncludeCoda() != null) {
                pageInfo.setIncludeCoda(jspProperty.getIncludeCoda());
            }
            //        String javaFileName = ctxt.getServletJavaFileName();

            // Setup the ServletWriter
            //        String javaEncoding = ctxt.getOptions().getJavaEncoding();
            //	OutputStreamWriter osw = null; 
            //	try {
            //	    osw = new OutputStreamWriter(new FileOutputStream(javaFileName),
            //					 javaEncoding);
            //	} catch (UnsupportedEncodingException ex) {
            //            errDispatcher.jspError("jsp.error.needAlternateJavaEncoding", javaEncoding);
            //	}

            //	ServletWriter writer = new ServletWriter(new PrintWriter(osw));
            //        ctxt.setWriter(writer);

            // Reset the temporary variable counter for the generator.
            JspUtil.resetTemporaryVariableName();

            // Parse the file
            ParserController parserCtl = new ParserController(ctxt, comp);
            pageNodes = parserCtl.parse(ctxt.getJspFile());

            //	if (ctxt.isPrototypeMode()) {
            //	    // generate prototype .java file for the tag file
            //	    Generator.generate(writer, this, pageNodes);
            //            writer.close();
            //	    return null;
            //	}

            // Generate FunctionMapper (used for validation of EL expressions and
            // code generation)
            // pageInfo.setFunctionMapper(new FunctionMapperImpl(this));

            // Validate and process attributes
            // Validator.validate(comp, pageNodes);
            xmlView = NbValidator.validate(comp, pageNodes);

            //        long t2=System.currentTimeMillis();
            // Dump out the page (for debugging)
            // Dumper.dump(pageNodes);

            // Collect page info
            Collector.collect(comp, pageNodes);

            // Compile (if necessary) and load the tag files referenced in
            // this compilation unit.

            // PENDING - we may need to process tag files somehow
            //	TagFileProcessor tfp = new TagFileProcessor();
            //	tfp.loadTagFiles(comp, pageNodes);

            //        long t3=System.currentTimeMillis();

            // Determine which custom tag needs to declare which scripting vars
            ScriptingVariabler.set(pageNodes, errDispatcher);

            // Optimizations by Tag Plugins
            TagPluginManager tagPluginManager = options.getTagPluginManager();
            tagPluginManager.apply(pageNodes, errDispatcher, pageInfo);

            // Generate static funciton mapper codes.
            ELFunctionMapper.map(comp, pageNodes);

            // generate servlet .java file
            //	Generator.generate(writer, comp, pageNodes);
            //        writer.close();
            // The writer is only used during the compile, dereference
            // it in the JspCompilationContext when done to allow it
            // to be GC'd and save memory.
            //        ctxt.setWriter(null);

            //        long t4=System.currentTimeMillis();
            //        if( t4-t1 > 500 ) {
            //            log.debug("Generated "+ javaFileName + " total=" +
            //                      (t4-t1) + " generate=" + ( t4-t3 ) + " validate=" + ( t2-t1 ));
            //        }

                    //JSR45 Support - note this needs to be checked by a JSR45 guru
            //        if (! options.isSmapSuppressed()) {
            //            String smapStr = SmapUtil.generateSmap(ctxt, pageNodes);
            //        }

            // If any proto type .java and .class files was generated,
            // the prototype .java may have been replaced by the current
            // compilation (if the tag file is self referencing), but the
            // .class file need to be removed, to make sure that javac would
            // generate .class again from the new .java file just generated.

            // PENDING - we may need to process tag files somehow
            //	tfp.removeProtoTypeFiles(ctxt.getClassFileName());
        }
        catch (ThreadDeath t) {
            throw t;
        }
        catch (Throwable t) {
            parseException = t;
        }
        finally {
            // convert the nodes
            try {
                if (pageNodes != null) {
                    nbNodes = convertNodes(pageNodes);
                }
            }
            catch (JasperException e) {
                if (parseException == null) {
                    parseException = e;
                }
            }
            // convert the pageInfo
            try {
                if (pageInfo != null) {
                    // xmlView may be null
                    nbPageInfo = convertPageInfo(pageInfo, xmlView, ctxt);
                }
            }
            catch (JspException e) {
                if (parseException == null) {
                    parseException = e;
                }
            }
        }
//	return smapStr;
    }
    
    
    private static org.netbeans.modules.web.jsps.parserapi.Node.Nodes convertNodes(Node.Nodes nodes) throws JasperException {
        org.netbeans.modules.web.jsps.parserapi.Node.Nodes nbNodes =
	    NodeConverterVisitor.convertNodes(nodes);
        return nbNodes;
    }    
    
    private static org.netbeans.modules.web.jsps.parserapi.PageInfo convertPageInfo(PageInfo pageInfo, String xmlView, JspCompilationContext ctxt) throws JspException {
        PageInfoImpl nbPageInfo = 
            new PageInfoImpl(
                getTaglibsMapReflect(pageInfo, ctxt),
                getJSPPrefixMapperReflect(pageInfo), 
                getXMLPrefixMapperReflect(pageInfo), 
                ((CompilerHacks.HackPageInfo)pageInfo).getApproxXmlPrefixMapper(), 
                pageInfo.getImports(),
                pageInfo.getDependants(),
                pageInfo.getIncludePrelude(),
                pageInfo.getIncludeCoda(),
                getPluginDclsReflect(pageInfo),
                getPrefixesReflect(pageInfo)
            );
        nbPageInfo.setLanguage(            pageInfo.getLanguage());
        nbPageInfo.setExtends(             pageInfo.getExtends());
        nbPageInfo.setContentType(         pageInfo.getContentType());
        nbPageInfo.setSession(             pageInfo.getSession());
        nbPageInfo.setBufferValue(         pageInfo.getBufferValue());
        nbPageInfo.setAutoFlush(           pageInfo.getAutoFlush());
        nbPageInfo.setIsThreadSafe(        pageInfo.getIsThreadSafe());
        nbPageInfo.setIsErrorPage(         pageInfo.getIsErrorPage());
        nbPageInfo.setErrorPage(           pageInfo.getErrorPage());
        nbPageInfo.setScriptless(          pageInfo.isScriptless());
        nbPageInfo.setScriptingInvalid(    pageInfo.isScriptingInvalid());
        nbPageInfo.setELIgnored(           pageInfo.isELIgnored());
        nbPageInfo.setOmitXmlDecl(         pageInfo.getOmitXmlDecl());
        nbPageInfo.setIsJspPrefixHijacked( pageInfo.isJspPrefixHijacked());
        nbPageInfo.setDoctypeName(         pageInfo.getDoctypeName());
        nbPageInfo.setDoctypeSystem(       pageInfo.getDoctypeSystem());
        nbPageInfo.setDoctypePublic(       pageInfo.getDoctypePublic());
        nbPageInfo.setHasJspRoot(          pageInfo.hasJspRoot());
        nbPageInfo.setBeans(createBeanData(pageInfo.getBeanRepository()));
        // the xml view
        nbPageInfo.setXMLView(xmlView);
        
        return nbPageInfo;
    }
    
    
    private static org.netbeans.modules.web.jsps.parserapi.PageInfo.BeanData[] createBeanData(BeanRepository rep) {
        try {
            initBeanRepositoryFields();
            Vector sessionBeans = (Vector)sessionBeansF.get(rep);
            Vector pageBeans = (Vector)pageBeansF.get(rep);
            Vector appBeans = (Vector)appBeansF.get(rep);
            Vector requestBeans = (Vector)requestBeansF.get(rep);
            Hashtable beanTypes = (Hashtable)beanTypesF.get(rep);
            int size = beanTypes.size();
            org.netbeans.modules.web.jsps.parserapi.PageInfo.BeanData bd[] = 
                new org.netbeans.modules.web.jsps.parserapi.PageInfo.BeanData[size];
            Iterator it = beanTypes.keySet().iterator();
            int index = 0;
            while (it.hasNext()) {
                String id = (String)it.next();
                String type = (String)beanTypes.get(id);
                int scope = PageContext.PAGE_SCOPE;
                if (sessionBeans.contains(id)) {
                    scope = PageContext.SESSION_SCOPE;
                }
                if (appBeans.contains(id)) {
                    scope = PageContext.APPLICATION_SCOPE;
                }
                if (requestBeans.contains(id)) {
                    scope = PageContext.REQUEST_SCOPE;
                }
                
                bd[index] = new BeanDataImpl(id, scope, type);
                ++index;
            }
            return bd;
        }
        catch (IllegalAccessException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            throw new RuntimeException();
        }
    }
    
    static class PageInfoImpl extends org.netbeans.modules.web.jsps.parserapi.PageInfo {
        
        public PageInfoImpl(/*BeanRepository beanRepository*/
                Map taglibsMap,
                Map jspPrefixMapper,
                Map xmlPrefixMapper,
                Map approxXmlPrefixMapper,
                List imports,
                List dependants,
                List includePrelude,
                List includeCoda,
                List pluginDcls,
                Set prefixes
            ) {
            super(taglibsMap, jspPrefixMapper, xmlPrefixMapper, approxXmlPrefixMapper, imports, dependants, includePrelude,
                includeCoda, pluginDcls, prefixes);
        }
        
        private String xmlView;
        
        public void setXMLView(String xmlView) {
            this.xmlView = xmlView;
        }
        
        public String getXMLView() {
            return xmlView;
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append(super.toString());
            sb.append(" ------- XML View (constructed from the original data structure) -----\n");
            if (xmlView == null) {
                sb.append("no XML view\n");
            }
            else {
                sb.append(getXMLView());
            }
            return sb.toString();
        }
        
    }
    
    static class BeanDataImpl implements org.netbeans.modules.web.jsps.parserapi.PageInfo.BeanData {

        private String id;
        private int scope;
        private String className;

        BeanDataImpl(String id, int scope, String className) {
            this.id = id;
            this.scope = scope;
            this.className = className;
        }

        /** Identifier of the bean in the page (variable name). */
        public String getId() {
            return id;
        }

        /** Scope for this bean. Returns constants defined in {@link javax.servlet.jsp.PageContext}. */
        public int getScope() {
            return scope;
        }

        /** Returns the class name for this bean. */
        public String getClassName() {
            return className;
        }

    }
                
    // ------ getting BeanRepository data by reflection
    private static Field sessionBeansF, pageBeansF, appBeansF, requestBeansF, beanTypesF;
    
    private static void initBeanRepositoryFields() {
        if (sessionBeansF == null) {
            try {
                sessionBeansF = BeanRepository.class.getDeclaredField("sessionBeans");
                sessionBeansF.setAccessible(true);
                pageBeansF = BeanRepository.class.getDeclaredField("pageBeans");
                pageBeansF.setAccessible(true);
                appBeansF = BeanRepository.class.getDeclaredField("appBeans");
                appBeansF.setAccessible(true);
                requestBeansF = BeanRepository.class.getDeclaredField("requestBeans");
                requestBeansF.setAccessible(true);
                beanTypesF = BeanRepository.class.getDeclaredField("beanTypes");
                beanTypesF.setAccessible(true);
            }
            catch (NoSuchFieldException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }

    
    // ------ getting BeanRepository data by reflection
    private static Field pluginDclsF, prefixesF, taglibsMapF, jspPrefixMapperF, xmlPrefixMapperF;
    
    private static void initPageInfoFields() {
        if (pluginDclsF == null) {
            try {
                pluginDclsF = PageInfo.class.getDeclaredField("pluginDcls");
                pluginDclsF.setAccessible(true);
                prefixesF = PageInfo.class.getDeclaredField("prefixes");
                prefixesF.setAccessible(true);
                taglibsMapF = PageInfo.class.getDeclaredField("taglibsMap");
                taglibsMapF.setAccessible(true);
                jspPrefixMapperF = PageInfo.class.getDeclaredField("jspPrefixMapper");
                jspPrefixMapperF.setAccessible(true);
                xmlPrefixMapperF = PageInfo.class.getDeclaredField("xmlPrefixMapper");
                xmlPrefixMapperF.setAccessible(true);
            }
            catch (NoSuchFieldException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }
    
    private static Vector getPluginDclsReflect(PageInfo pageInfo) {
        initPageInfoFields();
        try {
            return (Vector)pluginDclsF.get(pageInfo);
        }
        catch (IllegalAccessException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            throw new RuntimeException();
        }
    }
    
    private static HashSet getPrefixesReflect(PageInfo pageInfo) {
        initPageInfoFields();
        try {
            return (HashSet)prefixesF.get(pageInfo);
        }
        catch (IllegalAccessException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            throw new RuntimeException();
        }
    }

    private static class TagInfoCacheRecord{
        long time;
        TagInfo tagInfo;
        
        public TagInfoCacheRecord(long time, TagInfo info){
            this.tagInfo = info;
            this.time = time;
        }
    }
    
    /** The cache for the tag infos from tagfiles. There are stored TagInfoCacheRecord as value and 
     * the path for a tagfile as key. The informations are cached for the whole application. 
     * The cache is changed, when a jsp page is parsed and a tag file was changed. 
     */
    
    private static Hashtable tagInfoCache = new Hashtable();
    
    private static Map getTaglibsMapReflect(PageInfo pageInfo, JspCompilationContext ctxt) {
        initPageInfoFields();
        try {
            Map taglibs = (Map)taglibsMapF.get(pageInfo);
            Iterator iter = taglibs.values().iterator();
            TagLibraryInfo libInfo;    
            TagInfo [] tagInfos;
            // Caching information about tag files
            while (iter.hasNext()){
                libInfo = (TagLibraryInfo)iter.next();                
                tagInfos = null;
                try {
                    ArrayList tags = new ArrayList();
                    if (libInfo instanceof ImplicitTagLibraryInfo){
                        //We need the access for the files
                        Field tagFileMapF = ImplicitTagLibraryInfo.class.getDeclaredField("tagFileMap");
                        tagFileMapF.setAccessible(true);
                        Hashtable tagFileMap = (Hashtable)tagFileMapF.get(libInfo);
                        //Check every file in tag library
                        Enumeration e = tagFileMap.keys();
                        
                        while (e.hasMoreElements()){
                            //Find the path for the file
                            String name = (String) e.nextElement();
                            String filePath = (String)tagFileMap.get(name);
                            
                            URL path =  ctxt.getResource(filePath);
                            File file = new File (new URI( path.toExternalForm() ));
                            // Is there the file in the cache?
                            if (tagInfoCache.containsKey(path)){
                                TagInfoCacheRecord r = (TagInfoCacheRecord)tagInfoCache.get(path);
                                // Is there a change in the tagfile?
                                if (r.time < file.lastModified()){
                                    tagInfoCache.put(path, new TagInfoCacheRecord (file.lastModified(), libInfo.getTagFile(name).getTagInfo()));
                                }
                            }
                            else {
                                tagInfoCache.put(path, new TagInfoCacheRecord (file.lastModified(), libInfo.getTagFile(name).getTagInfo()));
                            }
                            //Obtain information from the cache
                            tags.add(((TagInfoCacheRecord)tagInfoCache.get(path)).tagInfo);
                            tagInfos = new TagInfo[tags.size()];
                            for (int i = 0; i < tags.size(); i++){
                                tagInfos[i] = (TagInfo)tags.get(i);
                            }
                        }
                    }
                    else {
                        TagFileInfo[] tagFiles = libInfo.getTagFiles();  
                        if (tagFiles != null && tagFiles.length > 0){
                            tagInfos = new TagInfo[tagFiles.length + libInfo.getTags().length];
                            for (int i = 0; i < tagFiles.length; i++){
                                // Is it tag file from jar or form WEB-INF/tags
                                // fix for issue #56810                              
                                String[] location = ctxt.getTldLocation(libInfo.getURI());
                                if (location != null){
                                    String path = location[0];
                                    File file = new File (path);
                                    path = path + tagFiles[i].getPath();
                                    // Is there the file in the cache?
                                    if (tagInfoCache.containsKey(path)){
                                        TagInfoCacheRecord r = (TagInfoCacheRecord)tagInfoCache.get(path);
                                        // Is there a change in the tagfile?
                                        if (r.time < file.lastModified()){
                                            tagInfoCache.put(path, new TagInfoCacheRecord (file.lastModified(), 
                                                libInfo.getTagFile(tagFiles[i].getName()).getTagInfo()));
                                        }
                                    }
                                    else {
                                        tagInfoCache.put(path, 
                                            new TagInfoCacheRecord (file.lastModified(), 
                                            libInfo.getTagFile(tagFiles[i].getName()).getTagInfo()));
                                    }
                                    //Obtain information from the cache
                                    tagInfos[i] = ((TagInfoCacheRecord)tagInfoCache.get(path)).tagInfo;
                                }
                                else
                                    tagInfos[i] = libInfo.getTagFile(tagFiles[i].getName()).getTagInfo();
                            }
                            // add tags (created from tag handlers, if there are any one
                            for (int i = 0 ; i < libInfo.getTags().length; i++){
                                tagInfos[tagFiles.length + i] = libInfo.getTags()[i]; 
                            }
                        }
                    }
                    
                    if (tagInfos != null && tagInfos.length > 0){
                        // Setting the taginfos directly to the library. 
                        Field tagInfosF = ImplicitTagLibraryInfo.class.getSuperclass().getDeclaredField("tags");
                        tagInfosF.setAccessible(true);    
                        tagInfosF.set(libInfo, tagInfos);
                    }
                }
                catch (java.lang.NoSuchFieldException e){
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                catch (java.net.MalformedURLException e){
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                catch (org.apache.jasper.JasperException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
                catch (java.net.URISyntaxException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            }
            return taglibs;
        }
        catch (IllegalAccessException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            throw new RuntimeException();
        }
    }
    
    private static Map getJSPPrefixMapperReflect(PageInfo pageInfo) {
        initPageInfoFields();
        try {
            return (Map)jspPrefixMapperF.get(pageInfo);
        }
        catch (IllegalAccessException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            throw new RuntimeException();
        }
    }
    
    private static Map getXMLPrefixMapperReflect(PageInfo pageInfo) {
        initPageInfoFields();
        try {
            return (Map)xmlPrefixMapperF.get(pageInfo);
        }
        catch (IllegalAccessException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            throw new RuntimeException();
        }
    }
    
}
