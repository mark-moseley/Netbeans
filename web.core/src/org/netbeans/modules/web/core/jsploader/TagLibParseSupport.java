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

package org.netbeans.modules.web.core.jsploader;

import java.beans.*;
import java.lang.ref.WeakReference;
import java.lang.ref.SoftReference;
import java.util.*;
import java.io.IOException;

import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

import org.openide.filesystems.FileObject;

import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.netbeans.modules.web.core.syntax.spi.JSPColoringData;

/** Support for parsing JSP pages and tag files and cooperation between the parser 
 * and the editor.
 * Parsing is context-aware, which means that a web module and the associated 
 * environment (libraries) is needed to provide proper tag library coloring and completion and other webmodule-dependent
 * features. The support tries to do its best to get good parse results even for
 * pages and tag files for which the web module context is not known, but 
 * in this case nothing can be guaranteed.
 *
 * @author Petr Jiricka
 * @version 
 */
public class TagLibParseSupport implements org.openide.nodes.Node.Cookie {

    private FileObject wmRoot;
    private FileObject jspFile;
    
    // request processing stuff
    private boolean documentDirty;
    private RequestProcessor.Task parsingTask = null;
    private RequestProcessor requestProcessor;

    /** Holds a reference to the JSP coloring data. */
    private WeakReference jspColoringDataRef;
    
    /** Holds a time-based cache of the JspOpenInfo structure. */
    private TimeReference jspOpenInfoRef;
    
    /** Holds the last parse result: JspParserAPI.ParseResult (whether successful or not).
     * The editor should hold a strong reference to this object. That way, if the editor window
     * is closed, memory is reclaimed, but important data is kept when it is needed.
     */
    private SoftReference parseResultRef;

    /** Holds the last successful parse result: JspParserAPI.ParseResult.
     * The editor should hold a strong reference to this object. That way, if the editor window
     * is closed, memory is reclaimed, but important data is kept when it is needed.
     */
    private SoftReference parseResultSuccessfulRef;
    
    private Object parseResultLock = new Object();
    private Object openInfoLock = new Object();

    /** Creates new TagLibParseSupport 
     * @param jspFile the resource to parse
     */
    public TagLibParseSupport(FileObject jspFile) {
        this.jspFile = jspFile;
        //this.proj = proj;
        try{
            this.wmRoot = jspFile.getFileSystem().getRoot();
        }
        catch (org.openide.filesystems.FileStateInvalidException e){
        }
        requestProcessor = new RequestProcessor("background jsp parsing"); // NOI18N
    }

    /** Gets the tag library data relevant for the editor. */
    public JSPColoringData getJSPColoringData() {
        return getJSPColoringData(true);
    }
    
    JSPColoringData getJSPColoringData(boolean prepare) {
        if (jspColoringDataRef != null) {
            Object o = jspColoringDataRef.get();
            if (o != null)
                return (JSPColoringData)o;
        }
        JSPColoringData jcd = new JSPColoringData(this);
        jspColoringDataRef = new WeakReference(jcd);
        if (prepare) {
            prepare();
        }
        return jcd;
    }

    /** Sets the dirty flag - if the document was modified after last parsing. */
    void setDocumentDirty(boolean b) {
        synchronized (parseResultLock) {
            documentDirty = b;
        }
    }

    /** Tests the documentDirty flag. */
    boolean isDocumentDirty() {
        return documentDirty;
    }

    /** Starts the parsing if the this class is 'dirty' and status != STATUS_NOT
    * and parsing is not running yet.
      @return parsing task so caller may listen on its completion.
    */
    Task autoParse() {
        return parseObject(Thread.MIN_PRIORITY);
    }

    /** Method that instructs the implementation of the source element
    * to prepare the element. It is non blocking method that returns
    * task that can be used to control if the operation finished or not.
    *
    * @return task to control the preparation of the elemement
    */
    public Task prepare() {
        return parseObject(Thread.MAX_PRIORITY - 1);
    }

    private Task parseObject(int priority) {
        synchronized (parseResultLock) {
            RequestProcessor.Task t = parsingTask;

            if (t != null) {
                t.setPriority(Math.max(t.getPriority(), priority));
                return t;
            }

//System.out.println("[Parsing] Got parse request");
            setDocumentDirty(false);
            t = requestProcessor.post(new ParsingRunnable(), 0, priority);
            parsingTask = t;
            return parsingTask;
        }
    }
    
    public JspParserAPI.JspOpenInfo getCachedOpenInfo() {
        synchronized (openInfoLock) {
            long timestamp = jspFile.lastModified().getTime();
            if (jspOpenInfoRef == null) {
                jspOpenInfoRef = new TimeReference();
            }
            JspParserAPI.JspOpenInfo info = (JspParserAPI.JspOpenInfo)jspOpenInfoRef.get(timestamp);
            if (info == null) {
                info = JspParserFactory.getJspParser().getJspOpenInfo(jspFile, WebModule.getJspParserWM (wmRoot));
                jspOpenInfoRef.put(info, timestamp);
            }
            return info;
        }
    }
    
    /** Returns a cached parse information about the page.
     * @param successfulOnly if true, and the page has been parsed successfully in the past, returns
     *  the result of this successful parse. Otherwise returns null.
     *  If set to false, never returns null.
     * @param needCurrent if true, attempts to return the result corresponding to the page exactly at this moment<br>
     *   If both parameters are true, and the page is currently successfully parsable, then returns this result, If it is
     *   unparsable, returns null.
     * @return the result of parsing this page
     */
    public JspParserAPI.ParseResult getCachedParseResult(boolean successfulOnly, boolean preferCurrent) {
        boolean needToParse = false;
        if (preferCurrent && isDocumentDirty()) {
            // need to get an up to date copy
            needToParse = true;
        }
        if (parseResultRef == null) {
            // no information available
            needToParse = true;
        }
        
        JspParserAPI.ParseResult ret = null;
        SoftReference myRef = successfulOnly ? parseResultSuccessfulRef : parseResultRef;
        if (myRef != null) {
            ret = (JspParserAPI.ParseResult)myRef.get();
        }
        
        if ((ret == null) && (!successfulOnly)) {
            // to comply with the Javadoc regarding not returning null
            needToParse = true;
        }
        
        if (needToParse) {
            RequestProcessor.Task t = prepare(); // having the reference is important 
                                                 // so the SoftReference does not get garbage collected
            t.waitFinished();
            myRef = successfulOnly ? parseResultSuccessfulRef : parseResultRef;
            if (myRef != null) {
                ret = (JspParserAPI.ParseResult)myRef.get();
            }
        }
        return ret;
    }
    
    private class ParsingRunnable implements Runnable {
        
        /** Holds the result of parsing. Need to hold it here
         * to make sure that we have a strong reference and the SoftReference
         * does not get garbage collected.
         */
        JspParserAPI.ParseResult locResult = null;
        
        public ParsingRunnable () {
        }
        
        public void run() {
            JspParserAPI parser = JspParserFactory.getJspParser();
            // assert parser != null;
            if (parser == null) {
                throw new InternalError();
            }
            locResult = parser.analyzePage(jspFile, WebModule.getJspParserWM (wmRoot), JspParserAPI.ERROR_IGNORE);
            synchronized (TagLibParseSupport.this.parseResultLock) {
                parseResultRef = new SoftReference(locResult);
                if (locResult.isParsingSuccess()) {
                    parseResultSuccessfulRef = new SoftReference(locResult);
                }
                PageInfo pageInfo = locResult.getPageInfo();
                // PENDING - xmlPrefixMapper
                getJSPColoringData(false).applyParsedData(pageInfo.getTagLibraries(), pageInfo.getJspPrefixMapper(), 
                                                          pageInfo.isELIgnored(), locResult.isParsingSuccess());
                // if failure do nothing
                parsingTask = null;
            }
        }
    }

}
