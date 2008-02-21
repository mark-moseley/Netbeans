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

package org.netbeans.modules.web.core.jsploader;

import java.lang.ref.WeakReference;
import java.lang.ref.SoftReference;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.web.core.jsploader.api.TagLibParseCookie;
import org.netbeans.modules.web.core.syntax.spi.ErrorAnnotation;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI.JspOpenInfo;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.netbeans.modules.web.core.syntax.spi.JSPColoringData;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

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
public class TagLibParseSupport implements org.openide.nodes.Node.Cookie, TagLibParseCookie {

    private FileObject jspFile;
    
    // request processing stuff
    private boolean documentDirty;
    private RequestProcessor.Task parsingTask = null;
    private static RequestProcessor requestProcessor;

    private static final int WAIT_FOR_EDITOR_TIMEOUT = 15 * 1000; //15 seconds

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
    
    /** Holds a strong reference to the parsing 'successful' data during an editor 
     * pane is opened for a JSP corresponding to this support. 
     */
    private Object parseResultSuccessfulRefStrongReference = null;

    //this field is used to try to catch the situation when someone calls the parser
    //before editor support is initialized - causing #49300
    private boolean wasAnEditorPaneChangeEvent = false;
    
    private boolean parsingTaskCancelled = false;
    
    /** Whether parser task was started (at least once) or not. */
    private boolean parserStarted = false;
    
    /** Holds reference for annotation errors
     */
    private ErrorAnnotation annotations;
    /** Creates new TagLibParseSupport 
     * @param jspFile the resource to parse
     */
    public TagLibParseSupport(FileObject jspFile) {
        this.jspFile = jspFile;
        //allow max 10 requests to run in parallel & have one RP for all taglib parsings
        if(requestProcessor == null) requestProcessor = new RequestProcessor("background jsp parsing", 10); // NOI18N
        //requestProcessor = RequestProcessor.getDefault();
        annotations = new ErrorAnnotation (jspFile);
    }

    /** Gets the tag library data relevant for the editor. */
    public JSPColoringData getJSPColoringData() {
        // #120530 - do not start parsing
        return getJSPColoringData(false);
    }
    
    private WebModule getWebModule(FileObject fo){
        WebModule wm = WebModule.getWebModule(fo);
        if (wm != null){
            FileObject wmRoot = wm.getDocumentBase();
            if (wmRoot != null && (fo == wmRoot || FileUtil.isParentOf(wmRoot, fo))) {
                return wm;
            }
        }
        return null;
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
        //synchronized (parseResultLock) {
            documentDirty = b;
        //}
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
        //do not parse if it is not necessary
        //this is the BaseJspEditorSupport optimalization since the autoParse causes the webmodule
        //to be reparsed even if it has already been reparsed.
        if(isDocumentDirty() || !isParserStarted()) {
            return parseObject(Thread.MIN_PRIORITY);
        } else {
            return requestProcessor.post(new Runnable() {
                public void run() {
                    //do nothing, just a dummy task
                }
            });
        }
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
        //reset the state so the next parsing will run normally
        parsingTaskCancelled = false;
        
        //debug #49300: print out current stacktrace when the editor support is not initialized yet
        if(!wasAnEditorPaneChangeEvent) 
            Exceptions.attachLocalizedMessage(new IllegalStateException(),
                                              "The TagLibParseSupport.parseObject() is called before editor support is created!"); //NOI18N
        
        synchronized (parseResultLock) {
            RequestProcessor.Task t = parsingTask;

            if (t != null) {
                t.setPriority(Math.max(t.getPriority(), priority));
                return t;
            }
            setParserStarted();

            setDocumentDirty(false);
            t = requestProcessor.post(new ParsingRunnable(), 0, priority);
            parsingTask = t;
            return parsingTask;
        }
    }
    
    
    //used for notifying the parsing thread (to start the parsing)
    void setEditorOpened(boolean editorOpened) {
        //mark that the an editor pane open event was fired
        wasAnEditorPaneChangeEvent = true;
        
        synchronized (parseResultLock) {
            if(!editorOpened) {
                //clean the stronref to the parsing data when the editor is closed
                parseResultSuccessfulRefStrongReference = null;
            }
        }
        
    }
  
    void cancelParsingTask() {
        if(parsingTask !=  null) {
            //there is schedulled or running parsing task -> cancel it!
            boolean removed = parsingTask.cancel();
            parsingTask = null;
            jspColoringDataRef = null;
        }
        
        parsingTaskCancelled = true;
    }
    
    public JspParserAPI.JspOpenInfo getCachedOpenInfo(boolean preferCurrent, boolean useEditor) {
        synchronized (openInfoLock) {
            if (preferCurrent)
                jspOpenInfoRef = null;
            long timestamp = jspFile.lastModified().getTime();
            if (jspOpenInfoRef == null) {
                jspOpenInfoRef = new TimeReference();
            }
            JspParserAPI.JspOpenInfo info = (JspParserAPI.JspOpenInfo)jspOpenInfoRef.get(timestamp);
            if (info == null) {
                info = JspParserFactory.getJspParser().getJspOpenInfo(jspFile, getWebModule(jspFile), useEditor);
                jspOpenInfoRef.put(info, timestamp);
            }
            return info;
        }
    }
    
    public OpenInfo getOpenInfo(boolean preferCurrent, boolean useEditor) {
        JspOpenInfo delegate = getCachedOpenInfo(preferCurrent, useEditor);
        return OpenInfo.create(delegate.isXmlSyntax(), delegate.getEncoding());
    }
    
    public JspParserAPI.ParseResult getCachedParseResult(boolean successfulOnly, boolean preferCurrent) {
        return getCachedParseResult(successfulOnly, preferCurrent, false);
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
    public JspParserAPI.ParseResult getCachedParseResult(boolean successfulOnly, boolean preferCurrent, boolean forceParse) {
        boolean needToParse = forceParse;
        
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
    
    // Flag, whether there is already an error in the jsp page. 
    private boolean hasError = false;

    private synchronized boolean isParserStarted() {
        return parserStarted;
    }

    private synchronized void setParserStarted() {
        this.parserStarted = true;
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
            //test whether the parsing task has been cancelled -
            //someone called EditorCookie.close() during the parsing was waiting
            //on openedLock
            if(!parsingTaskCancelled && getWebModule(jspFile) != null) {
                JspParserAPI parser = JspParserFactory.getJspParser();
                // assert parser != null;
                if (parser == null) {
                    throw new InternalError();
                }
                
                getJSPColoringData(false).parsingStarted();
                
                locResult = parser.analyzePage(jspFile, getWebModule(jspFile), JspParserAPI.ERROR_IGNORE);
                assert locResult != null;
                
                synchronized (TagLibParseSupport.this.parseResultLock) {
                    parseResultRef = new SoftReference(locResult);
                    if (locResult.isParsingSuccess()) {
                        parseResultSuccessfulRef = new SoftReference(locResult);
                        //hold a reference to the parsing data until last editor pane is closed
                        //motivation: the editor doesn't always hold a strogref to this object
                        //so the SoftRef is sometime cleaned even if there is an editor pane opened.
                        parseResultSuccessfulRefStrongReference = locResult;
                        //set icon withouth errors
                        if (hasError){
                            //remove all errors
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    annotations.annotate(new ErrorAnnotation.ErrorInfo[] {});
                                }
                            });
                            hasError = false;
                        }
                    } else {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                for (int i = 0; i < locResult.getErrors().length; i ++){
                                    JspParserAPI.ErrorDescriptor err = locResult.getErrors()[i];
                                    if(checkError(err)) {
                                        annotations.annotate(new ErrorAnnotation.ErrorInfo[] {
                                            new ErrorAnnotation.ErrorInfo(translate(err.getErrorMessage()),
                                                    err.getLine(),
                                                    err.getColumn(),
                                                    ErrorAnnotation.JSP_ERROR)
                                        } );
                                    }
                                }
                                // set icon with error.
                                if (!hasError){
                                    hasError = true;
                                }
                                
                            }
                        });
                    }
                    PageInfo pageInfo = locResult.getPageInfo();
                    
                    // if failure do nothing
                    parsingTask = null;
                    
                    if (pageInfo == null) return;
                    //Map prefixMapper = (pageInfo.getXMLPrefixMapper().size() > 0) ?
                    //    pageInfo.getApproxXmlPrefixMapper() : pageInfo.getJspPrefixMapper();
                    //Map prefixMapper = pageInfo.getJspPrefixMapper();
                    Map prefixMapper = null;
                    if (pageInfo.getXMLPrefixMapper().size() > 0) {
                        prefixMapper = pageInfo.getApproxXmlPrefixMapper();
                        if (prefixMapper.size() == 0){
                            prefixMapper = pageInfo.getXMLPrefixMapper();
                        }
                        prefixMapper.putAll(pageInfo.getJspPrefixMapper());
                    }
                    else {
                        prefixMapper = pageInfo.getJspPrefixMapper();
                    }
                    getJSPColoringData(false).applyParsedData(pageInfo.getTagLibraries(), prefixMapper, 
                                                              pageInfo.isELIgnored(), getCachedOpenInfo(false, false).isXmlSyntax(), 
                                                              locResult.isParsingSuccess());
                }
            }
            
            
        }
        
        private boolean checkError(JspParserAPI.ErrorDescriptor err) {
            if(err.getErrorMessage() == null) {
                Logger.global.log(Level.INFO, null, 
                        new IllegalStateException("Invalid JspParserAPI.ErrorDescription from jsp parser - null error message: " + err.toString()));
                return false;
            }
            return true;
        }
        
        private String translate (String text){
            String value = text.replaceAll("&lt;", "<");
            value = value.replaceAll("&gt;", ">");
            return value;
        }

    }
}
