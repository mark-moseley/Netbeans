/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.util;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.openide.util.NbBundle;

/**
 * An abstraction for a JDBC URL that lets us set and get the various
 * pieces that are defined by the user when establishing a connection.
 * 
 * @author David Van Couvering
 */
public class JdbcUrl extends HashMap<String, String> {
    private JDBCDriver driver;
    private String url;
    
    // The URL specification split into pieces
    private ArrayList<String> urlComponents;
    
    // The static elements of the URL specification
    private ArrayList<String> staticComponents;
    
    private HashSet<String> supportedTokens = new HashSet<String>();
    private HashSet<String> requiredTokens = new HashSet<String>();
    private final boolean parseUrl;
        
    public static final String TOKEN_DB = "<DB>";
    public static final String TOKEN_HOST = "<HOST>";
    public static final String TOKEN_PORT = "<PORT>";
    public static final String TOKEN_SERVERNAME = "<SERVERNAME>";
    public static final String TOKEN_ADDITIONAL = "<ADDITIONAL>";
    public static final String TOKEN_TNSNAME = "<TNSNAME>";
    public static final String TOKEN_SID = "<SID>";
    public static final String TOKEN_SERVICENAME = "<SERVICE>";
    public static final String TOKEN_DSN = "<DSN>";
    
    private static final String OPTIONAL_START = "[";
    private static final String OPTIONAL_END = "]";
    
    // A very common set of supported tokens for a URL, so let's
    // predefine it
    private static Set<String> DB_HOST_PORT_TOKEN_SET = new HashSet<String>();
    
    static {
        DB_HOST_PORT_TOKEN_SET.add(TOKEN_DB);
        DB_HOST_PORT_TOKEN_SET.add(TOKEN_HOST);
        DB_HOST_PORT_TOKEN_SET.add(TOKEN_PORT);
        DB_HOST_PORT_TOKEN_SET.add(TOKEN_ADDITIONAL);        
    }
        
    private final String name;
    private final String className;
    private final String urlTemplate;
    private final String type;
        
    public JdbcUrl(String name, String className, String type, String urlTemplate, boolean parseUrl) {
        this.name = name;
        this.className = className;
        this.type = type;
        this.urlTemplate = urlTemplate;
        this.parseUrl = parseUrl;
        
        if (parseUrl) {
            extractUrlComponents();
        }
    }
    
    public JdbcUrl(String name, String className, String type, String urlTemplate) {
        this(name, className, type, urlTemplate, false);
    }
        
    public JdbcUrl(JDBCDriver driver, String type, String urlTemplate) {
        this(driver.getDisplayName(), driver.getClassName(), type, urlTemplate);
        this.driver = driver;
    }
    
    public JdbcUrl(JDBCDriver driver) {
        this(driver, null, null);
    }
    
    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public String getUrlTemplate() {
        return urlTemplate;
    }
    
    public boolean urlIsParsed() {
        return this.parseUrl;
    }

    public void setDriver(JDBCDriver driver) {
        this.driver = driver;
    }
    
    public JDBCDriver getDriver() {
        return driver;
    }

    public String getDisplayName() {
        if (isEmpty(getType())) {
            return getName();
        } else {
            return getName() + " (" + getType() + ")";
        }
    }
    
    public boolean supportsToken(String token) {
        return supportedTokens.contains(token);
    }
    
    public boolean requiresToken(String token) {
        return requiredTokens.contains(token);
    }
    
    private boolean hasAllRequiredTokens() {
        Set<String> keySet = keySet();
        for ( String token : requiredTokens ) {
            if ( ! keySet.contains(token)) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == null || ! (other instanceof JdbcUrl)) {
            return false;
        }

        JdbcUrl otherUrl = (JdbcUrl)other;
        
        return otherUrl.getDisplayName().equals(this.getDisplayName());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.getDisplayName() != null ? this.getDisplayName().hashCode() : 0);
        return hash;
    }
    
    protected boolean isEmpty(String str) {
        return str == null || str.equals("");
    }

    private MalformedURLException createMalformedURLException() {
        return new MalformedURLException(getMessage("ERR_InvalidURL", getUrlTemplate()));
    }

    private boolean isOptionalStart(String component) {
        return component.equals("["); // NOI18N
    }
    
    private boolean isOptionalStart(char ch) {
        return ch == '[';
    }
    
    private boolean isOptionalEnd(String component) {
        return component.equals("]"); // NOI18N
    }
    
    private boolean isOptionalEnd(char ch) {
        return ch == ']';
    }
    
    private boolean isToken(String component) {
        return component.startsWith("<") && component.endsWith(">"); // NOI18N
    }
    
    private boolean isTokenStart(char ch) {
        return ch == '<';
    }
    
    private boolean isTokenEnd(char ch) {
        return ch == '>';
    }
    
    /**
     * Get the URL string, which may be derived from the components
     * that were set individually.
     * 
     * If this is a parsed URL and not all the required fields have been set, 
     * then this method returns an empty string.
     * 
     * @return the URL string
     */
    public String getUrl() {
        if (! this.urlIsParsed()) {
            return this.url;
        }
        
        if (! this.hasAllRequiredTokens()) {
            return "";
        }
        
        // Compose the URL based on the properties set.
        int length = urlComponents.size();
        UrlSection section = new UrlSection();
        
        // Iterate through the components that define the structure
        // of a URL.  Use this to build up a URL string, substituting
        // in values when parameter tokens are found.
        for (int i=0 ; i < length ; i++) {
            String component = urlComponents.get(i);            
            if (isOptionalStart(component)) {
                section.setOptionalChild(new OptionalSection());
                section = section.getOptionalChild();
            } else if (isOptionalEnd(component)) {
                UrlSection parent = ((OptionalSection)section).getParent();
                parent.addText(section.getText());
                section = parent;
            } else if (supportedTokens.contains(component)) {
                section.setValue(get(component));
            } else {
                section.addText(component);
            }
        }
        
        return section.getText();
    }
    
    
    /**
     * Set the url string.  If this URL is one that we know how to parse,
     * then we extract the values from the URL and set the matching
     * properties.  Otherwise we just save the URL as is.
     * 
     * @param url The URL as entered by the user
     */
    public void setUrl(String url) throws MalformedURLException {
        if (! this.urlIsParsed()) {
            this.url = url;
            return;
        }
        
        // Clear all the old properties, they're invalid now
        clear();
        
        // The URL Buffer encapsulates the URL string and an index
        // pointing into the string.  Putting it in a class likes this
        // makes it easier to pass it around between methods.
        UrlBuffer buf = new UrlBuffer(url);
        
        // Iterate through the components that define the structure of a URL,
        // using this information to parse the URL string and extract
        // values
        int numComponents = urlComponents.size();
        for (int compIndex = 0 ; compIndex < numComponents ; compIndex++) {
            /*
            if (buf.isEOF()) {
                // We've hit the end of the URL string, so no point
                // going through any more components.
                break;
            }
            */
            String component = urlComponents.get(compIndex);
            
            if (isToken(component)) {
                // This means the next part of the URL should contain
                // a value that we care about: extract it
                String value = getTokenValue(buf);
                
                if (isEmpty(value)) {
                    // A required value was not provided.
                    throw createMalformedURLException();
                }
                put(component, value);
                continue;
            }
            
            if (isOptionalStart(component)) {
                // We're in an optional section of the URL; this has to
                // be handled a bit differently...
                compIndex = readOptionalValue(buf, compIndex+1);
                continue;
            }
            
            // If this component of the URL is not a token or a delimiter of 
            // an optional section, then it must be some static text that is
            // required (e.g. "jdbc:mysql://").
            String substring = buf.urlSubString();
            if ( substring == null || ! substring.startsWith(component)) {
                throw createMalformedURLException();
            }
            
            // Move past the static text in the URL string
            skipStaticText(buf, component);
        }
    }
    
    private static String getMessage(String key, String ... values) {
        return NbBundle.getMessage(JdbcUrl.class, key, values);
    }
    
    private void extractUrlComponents() {
        // Go through the URL template and split it up into a series
        // of components or elements that makes it easier to build and
        // parse URLs 
        urlComponents = new ArrayList<String>();
        int length = urlTemplate.length();
        boolean isToken = false;
        int optionalLevel = 0;
        StringBuffer buf = new StringBuffer();
        
        for (int i=0 ; i < length ; i++) {
            char ch = urlTemplate.charAt(i);
            if (isTokenStart(ch)) {
                // Can't have two tokens in a row...
                assert(! isToken);
                
                // Add the text gathered so far as a component of the URL
                buf = addComponent(buf);
                
                // Start with the next component, which is a token
                buf.append(ch);
                isToken = true;
            } else if (isTokenEnd(ch)) {
                assert(isToken);
                
                // Add the end-token character
                buf.append(ch);
                
                String token = buf.toString();

                if (optionalLevel == 0) {
                    requiredTokens.add(token);                    
                } 
                supportedTokens.add(token);
                
                // Add the token as a component of the URL
                buf = addComponent(buf);
                
                isToken = false;      
            } else if (isOptionalStart(ch)) {
                optionalLevel++;
                
                // Add the text gathered so far as a component of the URL
                buf = addComponent(buf);
                
                // Add a new component indicating we're starting an
                // optional section
                urlComponents.add(OPTIONAL_START);                
            } else if (isOptionalEnd(ch)) {
                optionalLevel--;
                
                // Add the text gathered so far as a new component
                buf = addComponent(buf);
                
                // Add a component indicating we're ending the optional section
                urlComponents.add(OPTIONAL_END);                
            } else {
                buf.append(ch);
            }
        }
        
        setStaticComponents();
        
        validateUrlComponents();
    }
    
    /**
     * Create a list of just those components that are static text - not
     * a token for a substitution parameter or an optional section indicator,
     * but just non-variable text.
     * 
     * This list is used when parsing a URL passed in to setUrl() 
     */
    private void setStaticComponents() {
        staticComponents = new ArrayList<String>();
        for (String component : urlComponents) {
            if (isToken(component) || isOptionalStart(component) || isOptionalEnd(component)) {
                continue;
            }
            staticComponents.add(component);
        }
        
    }
    
    /**
     * Helper method to add a component to the URL specification.  Return a newly allocated
     * StringBuffer if the buffer had data in it, otherwise return the
     * existing (empty) buffer
     */
    private StringBuffer addComponent(StringBuffer text) {
        if (text.length() > 0) {
            urlComponents.add(text.toString());
            return new StringBuffer();
        } else {
            return text;
        }
    }
    
    /**
     * Make sure that the components of the URL specification we have 
     * extracted make sense.
     */
    private void validateUrlComponents() {
        int length = urlComponents.size();
        int optionalCount = 0;
        for(int i = 0 ; i < length ; i++) {
            String component = urlComponents.get(i);
            if (component.startsWith("<")) {
                assert(isToken(component));
                
                if (i+1 != length) {
                    // Can't have two tokens in a row without a delimiter
                    assert(!isToken(urlComponents.get(i+1)));
                }
            } else if (isOptionalStart(component)) {
                optionalCount++;
            } else if (isOptionalEnd(component)) {
                optionalCount--;
            } 
        }
        
        assert(optionalCount == 0);
    }
    
    private int readOptionalValue(UrlBuffer buf, int componentIndex) throws MalformedURLException {
        int numComponents = urlComponents.size();
        boolean valueExpected = false;
        for ( ; componentIndex < numComponents ; componentIndex++) {
            String component = urlComponents.get(componentIndex);
            if (isOptionalEnd(component)) {
                return componentIndex;
            } else if (isOptionalStart(component)) {
                componentIndex = readOptionalValue(buf, componentIndex+1);
            } else if (isToken(component)) {
                String value = getTokenValue(buf);
                if (! isEmpty(value)) {
                    put(component, value);
                } else if (valueExpected) {
                    // We got the delimiter for this optional section, but no
                    // value was provided
                    throw createMalformedURLException();
                }
            } else {
                // We are expecting static text as part of this optional section.
                // Let's see if this expected static text is what we really
                // have.  If it isn't, then we know this optional section
                // is not specified.  Skip to the end of this optional section
                int optCount = 0;
                String substring = buf.urlSubString();
                if (substring == null ||
                        (!substring.startsWith(component))) {
                    do {
                        component = urlComponents.get(++componentIndex);
                        if (isOptionalStart(component)) {
                            // A nested optional section, skip past this too
                            optCount++;
                        }
                        
                        if (isOptionalEnd(component) && optCount > 0) {
                            optCount--;
                            component = urlComponents.get(++componentIndex);
                        }
                    } while (! isOptionalEnd(component) || optCount > 0);
                    return componentIndex;
                } else  {
                    // The text for this optional component is here, so we
                    // had better find the value too, or it's a bad URL string
                    valueExpected = true;
                    
                    // Skip past this expected static text
                    skipStaticText(buf, component);
                }
            }
        }

        // Shouldn't get here, we should have hit the optional end component...
        throw createMalformedURLException();
    }
    
    private String getTokenValue(UrlBuffer buf) {
        if (buf.isEOF()) {
            return null;
        }
        
        // Find the next piece of static text
        int index = findStaticText(buf);
        String value = null;
        
        // The value is everything before the next piece of static text.
        // Then move the index in the buffer up to the static text       
        if (index < 0) {
            // No remaining static text, so the full substring *is* the
            // value.
            value = buf.urlSubString();
            buf.incrementIndex(value.length());
            return value;
        } else {
            value = buf.urlSubString().substring(0, index);
            buf.incrementIndex(index);
        }
        
        return value;
    }

    /**
     * Move through the list of the remaining static components of the URL, 
     * and return the index in the URL substring of the first piece we find.
     * 
     * @param urlbuf
     * @return the index into the current url substring where some static
     *      text was found
     */
    private int findStaticText(UrlBuffer urlbuf) {
        if (urlbuf.isEOF()) {
            return -1;
        }
        
        int index = -1;
        String staticStr = urlbuf.currentStatic();
        while (index < 0 && staticStr != null) {            
            index = urlbuf.urlSubString().indexOf(staticStr);
            
            if (index < 0) {
                staticStr = urlbuf.nextStatic();
            }
        }
        
        return index;        
    }
    
    /**
     * Skip past expected static text in the URL string
     * This adjusts the index in the URL buffer and also
     * increments the pointer into the list of static components
     * 
     * @param buf
     * @param staticComponent
     */
    private void skipStaticText(UrlBuffer buf, String staticComponent) {
        assert(buf.currentStatic() != null);
        assert(buf.currentStatic().equals(staticComponent));
        buf.nextStatic();
        buf.incrementIndex(staticComponent.length());        
    }
        
    private class UrlBuffer {
        int index = 0;
        int staticsIndex = 0;
        int length;
        String url;
        private UrlBuffer(String url)  {
            this.url = url;
            this.length = url.length();
        }
        
        private String getFullUrl() {
            return url;
        }

        private String urlSubString() {
            if (index < length) {
                return url.substring(index);
            } else {
                return null;
            }
        }

        private void incrementIndex(int index) {
            this.index = this.index + index;
        }

        private boolean isEOF() {
            return index >= length;
        }
        
        private String currentStatic() {
            if (staticsIndex < staticComponents.size()) {
                return staticComponents.get(staticsIndex);
            } else {
                return null;
            }
        }
        
        private String nextStatic() {
            staticsIndex++;
            return currentStatic();
        }                
    }
    
    private class UrlSection {
        StringBuffer  textBuf = new StringBuffer();
        OptionalSection optionalChild;

        public String getText() {
            return textBuf.toString();
        }

        public void addText(String text) {
            if (text != null) {
                textBuf.append(text);
            }
        }
        
        public void setOptionalChild(OptionalSection child) {
            optionalChild = child;
            child.setParent(this);
        }
        
        public OptionalSection getOptionalChild() {
            return optionalChild;
        }

        public void setValue(String text) {
            addText(text);
        }

    }
    
    private class OptionalSection extends UrlSection {
        private boolean hasValue;        
        UrlSection parent;

        
        public void setParent(UrlSection parent) {
            this.parent = parent;
        }
        
        private UrlSection getParent() {
            return this.parent;
        }
   
        @Override
        public void setValue(String value) {
            if (! isEmpty(value)) {
                hasValue = true;
                super.setValue(value);
            }
        }
        
        @Override
        public String getText() {
            if (hasValue) {
                return super.getText();
            } else {
                return "";
            }
        }
    } 
    
}
