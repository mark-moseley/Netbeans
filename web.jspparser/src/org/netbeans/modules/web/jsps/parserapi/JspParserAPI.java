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

package org.netbeans.modules.web.jsps.parserapi;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Map;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

import org.netbeans.modules.web.jspparser.ContextUtil;

/**
 *
 * @author  pj97932
 * @version 
 */
public interface JspParserAPI {

    public static abstract class WebModule {
        
        /**
         * Property name that denotes the libraries of the web module. A PropertyChangeEvent with this property is fired if
         * the list of libraries changes, or if the timestamp of any of these libraries changes.
         * @deprecated use classpath API to obtain classpath for document base folder
         */
        public static final String PROP_LIBRARIES = "libraries"; // NOI18N
        
        /**
         * Property name that denotes the package root directories of the web module. A PropertyChangeEvent with this property is fired if
         * the list of package roots changes, or if the timestamp of any of the files contained in these package roots changes.
         * @deprecated use classpath API to obtain classpath for document base folder
         */
        public static final String PROP_PACKAGE_ROOTS = "package_roots"; // NOI18N
        
        /** Returns the document base directory of the web module.
         * May return null if we are parsing a tag file that is outside a web module
         * (that will be packaged into a tag library).
         */
        public abstract FileObject getDocumentBase();
        
        /** Returns InputStream for the file open in editor or null
         * if the file is not open.
         */
        public abstract java.io.InputStream getEditorInputStream (FileObject fo);
        
        /**
         * Returns the list of libraries used by this web module.
         * @deprecated use classpath API to obtain classpath for document base folder
         */
        public abstract FileObject[] getLibraries();
        
        /**
         * Returns the list package roots used by this web module.
         * @deprecated use classpath API to obtain classpath for document base folder
         */
        public abstract FileObject[] getPackageRoots();
        
        public abstract void addPropertyChangeListener(PropertyChangeListener l);
        
        public abstract void removePropertyChangeListener(PropertyChangeListener l);
    }
    
    /** Mode in which some errors (such as error parsing a tag library) are ignored. */
    public static final int ERROR_IGNORE = 1;
    /** Mode in which some errors (such as error parsing a tag library) are reported, 
     * but no accurate error description is needed. */
    public static final int ERROR_REPORT_ANY = 2;
    /** Mode in which an accurate description of all errors is required, so an actual attempt to parse all
     * tag libraries is done, so the parser throws a root cause exception. */
    public static final int ERROR_REPORT_ACCURATE = 3;
    
    public static final String TAG_MIME_TYPE = "text/x-tag"; // NOI18N
    
    /** Returns the information necessary for opening a JSP page in the editor.
     * 
     * @param jspFile the page to analyze
     * @param wm web module in whose context to compile
     * @param useEditor whether to use data from the existing open JSP document, or from the file on the disk
     * @return open information, using either the editor, or the file on the disk
     */    
    public JspOpenInfo getJspOpenInfo(FileObject jspFile, WebModule wm, boolean useEditor);
    
    /** Analyzes JSP and returns the parsed data about the page.
     * 
     * @param wmRoot root of the web module which gives context to this page,
     *   may be null if the page is not within a web module
     * @param jspFile the page to analyze
     * @param proj project in whose context to compile
     * @param errorReportingMode mode for reporting errors, see above
     * @return Parsing results.
     */    
    public JspParserAPI.ParseResult analyzePage(FileObject jspFile, WebModule wm,
        int errorReportingMode);
    
    
    /** Returns the classloader which loads classes from the given web module 
     * (within a project context).
     */
    public URLClassLoader getModuleClassLoader(WebModule wm);
    
    /** Creates a description of a tag library. */
    //public TagLibParseSupport.TagLibData createTagLibData(JspInfo.TagLibraryData info, FileSystem fs);
    
    /**
     * Returns the mapping of the 'global' tag library URI to the location (resource
     * path) of the TLD associated with that tag library. 
     * @param wmRoot the web module for which to return the map
     * @return Map which maps global tag library URI to the location 
     * (resource path) of its tld. The location is
     * returned as a String array:
     *    [0] The location
     *    [1] If the location is a jar file, this is the location of the tld.
     */
    public Map getTaglibMap(WebModule wm) throws IOException;
    
    /** This class represents a result of parsing. It indicates either success
     * or failure. In case of success, provides information about the parsed page,
     * in case of failure, provides information about parsing errors.
     */
    public static class ParseResult {
        
        protected PageInfo   pageInfo;
        protected Node.Nodes nodes;
        protected JspParserAPI.ErrorDescriptor[] errors;
        protected boolean parsedOK;
       
        /** Creates a new ParseResult in case of parse success.
         * @param pageInfo information about the parsed page (from Jasper)
         * @param node exact structure of the  (from Jasper)
         */
        public ParseResult(PageInfo pageInfo, Node.Nodes nodes) {
            this (pageInfo, nodes, null);
        }
        
        /** Creates a new ParseResult in case of parse failure.
         * @param errors information about parse errors
         */
        public ParseResult(JspParserAPI.ErrorDescriptor[] errors) {
            this (null, null, errors);
        }
        
        /** Creates a new ParseResult. If the errors array is null or empty,
         *  the parse is considered successful.
         * @param pageInfo information about the parsed page (from Jasper), may be null
         * @param node exact structure of the  (from Jasper), may be null
         * @param errors information about parse errors, or null, if parsing was successful
         */
        public ParseResult(PageInfo pageInfo, Node.Nodes nodes, JspParserAPI.ErrorDescriptor[] errors) {
            this.pageInfo = pageInfo;
            this.nodes = nodes;
            this.errors = errors;
            this.parsedOK = ((errors == null) || (errors.length == 0));
        }
        
        /** Indicates success or failure of parsing.
         */
        public boolean isParsingSuccess() {
            return parsedOK;
        }
        
        /** Returns all global information about the parsed page.
         *  @exception IllegalStateException if parsing failed
         */
        public PageInfo getPageInfo() {
            return pageInfo;
        }
        
        /** Returns the hierarchical structure of the page.
         *  @exception IllegalStateException if parsing failed
         */
        public Node.Nodes getNodes() {
            return nodes;
        }
        
        /** Returns information about the parse errors if parsing failed.
         *  @exception IllegalStateException if parsing succeeded
         */
        public JspParserAPI.ErrorDescriptor[] getErrors() {
            if (!(parsedOK)) {
                return errors;
            }
            throw new IllegalStateException();
        }
        
        public String toString() {
            StringBuffer result = new StringBuffer();
            result.append("--------- JspParserAPI.parseResult(), success: ");
            result.append(isParsingSuccess());
            result.append("\n");
            if (pageInfo != null) {
                result.append(" ---- PAGEINFO\n");
                result.append(pageInfo.toString());
            }
            if (nodes != null) {
                result.append("\n ---- NODES\n");
                result.append(nodes.toString());
                result.append("\n");
            }
            if (!isParsingSuccess()) {
                result.append("\n ---- ERRORS\n");
                for (int i = 0; i < errors.length; i++) {
                    result.append(errors[i].toString());
                }
            }
            return result.toString();
        }
        
    }
    
    /** Contains data important for opening the page
     * in the editor, e.g. whether the page is in classic
     * or XML syntax, or what is the file encoding.
     */
    public static class JspOpenInfo {
        
        private boolean isXml;
        private String encoding;
        
        public JspOpenInfo(boolean isXml, String encoding) {
            this.isXml = isXml;
            this.encoding = encoding;
        }
        
        public boolean isXmlSyntax() {
            return isXml;
        }
        
        public String getEncoding() {
            return encoding;
        }
        
        public boolean equals(Object o) {
            if (o instanceof JspOpenInfo) {
                JspOpenInfo openInfo2 = (JspOpenInfo)o;
                return (getEncoding().equals(openInfo2.getEncoding()) &&
                        isXmlSyntax() == openInfo2.isXmlSyntax());
            }
            else {
                return false;
            }
        }
        
        public int hashCode() {
            return encoding.hashCode() + (isXml ? 1 : 0);
        }
        
        public String toString() {
            return super.toString() + " [isXml: " + isXml + ", encoding: " + encoding + "]";
        }
        
    }

    /** Represents a description of a parse error.
     */
    public static class ErrorDescriptor {

        protected FileObject wmRoot;
        protected FileObject source;
        protected int line;
        protected int column;
        protected String errorMessage;
        protected String referenceText;

        /** Creates a new ErrorDescriptor. 
         * @param wmRoot the web module in which the error occurs. May be null in some (unusual) cases.
         * @param source the file in which the error occurred. This may be different from the page that was 
         *  originally compiled/parsed, if this is a page segment.
         * @param line line number on which the error occurred
         * @param column column number on which the error occurred
         * @param errorMessage message containing the description of the error
         * @param rererenceText a piece of code (line) that contains the error. May be empty.
         */
        public ErrorDescriptor(FileObject wmRoot, FileObject source, int line, 
        int column, String errorMessage, String referenceText) {
            this.wmRoot = wmRoot;
            this.source = source;
            this.line = line;
            this.column = column;
            this.errorMessage = errorMessage;
            this.referenceText = referenceText;
        }

        /** Returns a file containing the error. */
        public FileObject getSource() {
            return source;
        }

        /** Get the line of the error. */
        public int getLine() {
            return line;
        }

        /** Get the column of the error. */
        public int getColumn() {
            return column;
        }

        /** Get the error message associated with the error. */
        public String getErrorMessage() {
            return errorMessage;
        }

        /** Get the string which contains the error (i.e. contents of the line containing the error. */
        public String getReferenceText() {
            return referenceText;
        }
        
        public String toString() {
            StringBuffer result = new StringBuffer();
            result.append("ERROR in ")
                  .append(getSourcePath())
                  .append(" at [")
                  .append(getLine())
                  .append(", ")
                  .append(getColumn())
                  .append("] ")
                  .append(getErrorMessage())
                  .append("\n")
                  .append(getReferenceText())
                  .append("\n");
            return result.toString();
        }
        
        private String getSourcePath() {
            if (wmRoot == null) {
                return getSource().getNameExt();
            }
            else {
                return ContextUtil.findRelativeContextPath(wmRoot, getSource());
            }
        }
    }
    
    
}
