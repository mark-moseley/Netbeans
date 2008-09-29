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

package org.netbeans.modules.tasklist.todo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.tasklist.todo.settings.Settings;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author S. Aubrecht
 * @author Tor Norbye
 * @author Trond Norbye
 */
public class TodoTaskScanner extends FileTaskScanner implements PropertyChangeListener {
    
    private static final String GROUP_NAME = "nb-tasklist-todo"; //NOI18N
    
    private Pattern regexp = null;
    private Callback callback;
    
    /** 
     * Creates a new instance of TodoTaskProvider 
     * 
     */
    TodoTaskScanner( String displayName, String description ) {
        super( displayName, description, "Advanced/ToDo" ); //NOI18N
    }
    
    public static TodoTaskScanner create() {
        return new TodoTaskScanner( NbBundle.getBundle( TodoTaskScanner.class ).getString( "LBL_todotask" ), //NOI18N
                NbBundle.getBundle( TodoTaskScanner.class ).getString( "HINT_todotask" ) ); //NOI18N
    }

    public List<? extends Task> scan( FileObject resource ) {
        if( !isSupported( resource ) )
            return null;
        
        if( Settings.getDefault().isScanCommentsOnly() ) {
            return scanComments( resource );
        }
        return scanAll( resource );
    }
    
    private List<? extends Task> scanAll( FileObject resource ) {
        List<Task> tasks = null;
        
        try {
            String text = getContent( resource );
            
            int index = 0;
            int lineno = 1;
            int len = text.length();

            Matcher matcher = getScanRegexp().matcher( text );
            while (index < len && matcher.find(index)) {
                int begin = matcher.start();
                int end   = matcher.end();

                // begin should be the beginning of this line (but avoid 
                // clash if I have two tokens on the same line...
                char c = 'a'; // NOI18N
                int nonwhite = begin;
                while (begin >= index && (c = text.charAt(begin)) != '\n') { // NOI18N
                    if (c != ' ' && c != '\t') { // NOI18N
                        nonwhite = begin;
                    }
                    --begin;
                }
                
                begin = nonwhite;
                
                // end should be the last "nonwhite" character on this line...
                nonwhite = end;
                while (end < len) {
                    c = text.charAt(end);
                    if (c == '\n' || c == '\r') {// NOI18N
                        break;
                    } else if (c != ' ' && c != '\t') {// NOI18N
                            nonwhite = end;
                    }
                    ++end;
                }

                // calculate current line number
                int idx = index;
                while (idx <= begin) {
                    if (text.charAt(idx) == '\n') {// NOI18N
                        ++lineno;
                    }
                    ++idx;
                }
                
                index = end;
                
                String description = new String( text.subSequence(begin, nonwhite+1).toString().toCharArray() );
                
                Task task = Task.create( resource, GROUP_NAME, description, lineno );
                if( null == tasks ) {
                    tasks = new LinkedList<Task>();
                }
                tasks.add( task );
            }
        } catch( IOException e ) {
            Logger.getLogger( getClass().getName() ).log( Level.INFO, null, e );
        } catch( OutOfMemoryError oomE ) {
            System.gc();
            Logger.getLogger( getClass().getName() ).log( Level.INFO, null, oomE );
        }
        return null == tasks ? getEmptyList() : tasks;
    }
     
    private List<? extends Task> scanComments( FileObject resource ) {
        String ext = resource.getExt().toLowerCase();
        String mime = FileUtil.getMIMEType( resource );
            
        String lineComment = Settings.getDefault().getLineComment( ext, mime );
        String blockCommentStart = Settings.getDefault().getBlockCommentStart( ext, mime );
        String blockCommentEnd = Settings.getDefault().getBlockCommentEnd( ext, mime );
        
        SourceCodeCommentParser sccp = new SourceCodeCommentParser( lineComment, blockCommentStart, blockCommentEnd );

        List<Task> tasks = null;

        try {
            String text = getContent( resource );

            sccp.setText( text );

            SourceCodeCommentParser.CommentRegion reg = new SourceCodeCommentParser.CommentRegion();
        
            Matcher matcher = getScanRegexp().matcher( text );
            int len = text.length();
            int lineno = 1;
            int index = 0;
	    int idx = 0;

            // find the first comment region
            if (!sccp.nextRegion(reg)) {
                // Done searching the document... bail out..
                return getEmptyList();
            }
            
            while (index < len && matcher.find(index)) {
                int begin = matcher.start();
                int end   = matcher.end();
                boolean toosoon = false;
                boolean goahead;

                do {
                    goahead = true;

                    // A match within the source comment?                   
                    if (begin < reg.start) {
                        toosoon = true;
                        // too soon.. get next match
                    } else if (begin > reg.stop) {
                        goahead = false;
                        if (!sccp.nextRegion(reg)) {
                            // Done searching the document... bail out..
                            return null == tasks ? getEmptyList() : tasks;
                        }
                    } 
                } while (!goahead);

                if (toosoon) {
                    // find next match!
                    index = end;
                    continue;
                }

                // begin should be the beginning of this line (but avoid 
                // clash if I have two tokens on the same line...
                char c = 'a'; // NOI18N
                int nonwhite = begin;
                while (begin >= index && (c = text.charAt(begin)) != '\n') { // NOI18N
                    if (c != ' ' && c != '\t') { // NOI18N
                        nonwhite = begin;
                    }
                    --begin;
                }
                
                begin = nonwhite;
                
                // end should be the last "nonwhite" character on this line...
                nonwhite = end;
                while (end < len) {
                    c = text.charAt(end);
                    if (c == '\n' || c == '\r') {// NOI18N
                        break;
                    } else if (c != ' ' && c != '\t') {// NOI18N
                            nonwhite = end;
                    }
                    ++end;
                }

                // calculate current line number
                while (idx <= begin) {
                    if (text.charAt(idx) == '\n') {// NOI18N
                        ++lineno;
                    }
                    ++idx;
                }
                
                index = end;
                
                String description = text.subSequence(begin, nonwhite+1).toString();

                Task task = Task.create( resource, GROUP_NAME, description, lineno );
                if( null == tasks ) {
                    tasks = new LinkedList<Task>();
                }
                tasks.add( task );
            }
        } catch( IOException e ) {
            Logger.getLogger( getClass().getName() ).log( Level.INFO, null, e );
        } catch( OutOfMemoryError oomE ) {
            System.gc();
            Logger.getLogger( getClass().getName() ).log( Level.INFO, null, oomE );
        }
        return null == tasks ? getEmptyList() : tasks;
    }
    
    private boolean isSupported( FileObject file ) {
        if( null == file || file.isFolder() )
            return false;
        return Settings.getDefault().isExtensionSupported( file.getExt() )
                || Settings.getDefault().isMimeTypeSupported( FileUtil.getMIMEType(file) );
    }

    Pattern getScanRegexp() {
        // Create regexp from tags
        if (regexp == null) {
            StringBuffer sb = new StringBuffer(200);
            Collection<String> patterns = Settings.getDefault().getPatterns();
            boolean needSeparator = false;
            for( String s : patterns ) {
                if( needSeparator ) {
                    sb.append('|');
                }
                needSeparator = true;
                int n = s.length();
                // Insert token/boundary separator when we're dealing
                // with text tokens, since you probably don't want
                // a todo-match in a comment like
                // "and now process GLYPTODON content".
                // However, for non-token tags, such as "<<<<" don't
                // insert word boundary markers since it won't work - there's
                // no word on the right...
                if (Character.isJavaIdentifierPart(s.charAt(0))) {
                    // isJavaIdentifierPart - roughly matches what regex
                    // considers a word ([a-zA-Z_0-9])

                    // \W instead of \b: Workarond for issue 30250
                    sb.append("\\W"); // NOI18N
                }
                // "escape" the string here such that regexp meta
                // characters are handled literally
                for (int j = 0; j < n; j++) {
                    char c = s.charAt(j);
                    // regexp metachar?
                    if ((c == '(') || (c == ')') ||
                        (c == '{') || (c == '}') ||
                        (c == '[') || (c == ']') ||
                        (c == '?') || (c == '*') || (c == '+') ||
                        (c == '!') || (c == '|') || (c == '\\') ||
                        (c == '^') || (c == '$')) {
                        sb.append('\\');
                    }
                    sb.append(c);
                }
                if (Character.isJavaIdentifierPart(s.charAt(n-1))) {
                    sb.append("\\b"); // NOI18N
                }
            }
            try {
                regexp = Pattern.compile(sb.toString());
            } catch (PatternSyntaxException e) {
                // Internal error: the regexp should have been validated when
                // the user edited it
                Logger.getLogger( getClass().getName() ).log( Level.INFO, null, e );;
                return null;
            }
        }
        return regexp;
    }
    
    private String getContent( FileObject fileObject ) throws IOException {
        char[] buf = new char[1024*64];
        StringBuffer sb = new StringBuffer();
        Charset charset = FileEncodingQuery.getEncoding( fileObject );
        Reader r = new BufferedReader( new InputStreamReader( fileObject.getInputStream(), charset ) );
        int len;
        try {
            while (true) {
                len = r.read(buf);
                if (len == -1) break;
                sb.append(buf, 0, len);
            }
        } finally {
            r.close();
        }
        return sb.toString();
    }    
    
    private List<? extends Task> getEmptyList() {
        List<? extends Task> res = Collections.emptyList();
        return res;
    }

    public void attach( Callback callback ) {
        if( null == callback && null != this.callback ) {
            regexp = null;
            Settings.getDefault().removePropertyChangeListener( this );
        } else if( null != callback && null == this.callback ) {
            Settings.getDefault().addPropertyChangeListener( this );
        }
        this.callback = callback;
    }

    public void propertyChange( PropertyChangeEvent e ) {
        if( Settings.PROP_PATTERN_LIST.equals( e.getPropertyName() )
         || Settings.PROP_SCAN_COMMENTS_ONLY.equals( e.getPropertyName() ) ) {
            regexp = null;
            if( null != callback )
                callback.refreshAll();
        }
    }
    
    @Override
    public void notifyPrepare() {
        getScanRegexp();
    }

    @Override
    public void notifyFinish() {
        regexp = null;
    }
}
