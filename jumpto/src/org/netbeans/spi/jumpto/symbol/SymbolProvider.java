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

package org.netbeans.spi.jumpto.symbol;

import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.jumpto.symbol.SymbolProviderAccessor;
import org.netbeans.spi.jumpto.type.SearchType;

/**
 * 
 * A Symbol Provider participates in the Goto Symbol dialog by providing SymbolDescriptors,
 * one for each matched symbol, when asked to do so.
 * 
 * The Symbol Providers are registered in Lookup.
 * @since 1.7
 * 
 * @author Tomas Zezula
 */
public interface SymbolProvider {
    
    /** 
     * Describe this provider with an internal name, in case we want to provide
     * some kind of programmatic filtering
     * 
     * @return An internal String uniquely identifying this symbol provider, such as
     *   "java"
     */
    String name();

    /** 
     * Describe this provider for the user, in case we want to offer filtering
     * capabilities in the Go To Symbol dialog
     * 
     * @return A display name describing the symbols being provided by this SymbolProvider,
     *  such as "Java Symbols", "Ruby Symbols", etc.
     */
    String getDisplayName();
    
    /** 
     * Compute a list of SymbolDescriptors that match the given search text for the given
     * search type. This might be a slow operation, and the infrastructure may end
     * up calling {@link #cancel} on the same symbol provider during the operation, in which
     * case the method can return incomplete results. If there is a "current project",
     * the Go To Symbol infrastructure will perform the search in two passes; first it
     * will call {@link #getSymbolNames} with the current project, which should be a reasonably
     * fast search, and display those symbols first. It will then call the method again
     * with a null project, which should return all symbols.
     * <p>
     * Note that a useful performance optimization is for the SymbolProvider to cache
     * a few of its most recent search results, and if the next search (e.g. more user
     * keystrokes) is a simple narrowing of the search, just filter the previous search
     * result. There is an explicit {@link #cleanup} call that the Go To Symbol dialog
     * will make at the end of the dialog interaction, which can be used to clean up the cache.
     * 
     * @param context search context containg search text and type, optionally project
     * @param result  filled with symbol descriptors and optional message
     */
    void computeSymbolNames(Context context, Result result);
    
    /**
     * Cancel the current operation, if possible. This might be called if the user
     * has typed something (including the backspace key) which makes the current
     * search obsolete and a new one should be initiated.
     */
    void cancel();


    /**
     * The Go To Symbol dialog is dismissed for now - free up resources if applicable.
     * (A new "session" will be indicated by a new call to getSymbolNames.)
     * 
     * This allows the SymbolProvider to cache its most recent search result, and if the next
     * search is simply a narrower search, it can just filter the previous result.
     */
    void cleanup();


    /**
     * Represents search context.
     * Contains search type (such as prefix, regexp), search text and
     * optionally project where to search.
     *
     */
    public static final class Context extends Object {
        private final Project project;
        private final String text;
        private final SearchType type;
        
        static {
            SymbolProviderAccessor.DEFAULT = new SymbolProviderAccessor() {

                @Override
                public Context createContext(Project p, String text, SearchType t) {
                    return new Context(p, text, t);
                }

                @Override
                public Result createResult(List<? super SymbolDescriptor> result, String[] message) {
                    return new Result(result, message);
                }            
            };
        }
        
        Context(Project project, String text, SearchType type) {
            this.project = project;
            this.text = text;
            this.type = type;
        }
        
        /**
         * Return project representing scope of search, if null, the search is not
         * limited.
         *
         * @return project If not null, the type search is limited to the given project.
         */
        public Project getProject() { return project; }

        /**
          * Return the text used for search.
          *
          * @return The text used for the search; e.g. when getSearchType() == SearchType.PREFIX,
          *   text is the prefix that all returned symbols should start with.
          */
        public String getText() { return text; }

        /**
         * Return the type of search.
         *
         * @return Type of search performed, such as prefix, regexp or camel case.
         */
        public SearchType getSearchType() { return type; }
    }
    
    /**
     * Represents a collection of <tt>SymbolDescriptor</tt>s that match 
     * the given search criteria. Moreover, it can contain message 
     * for the user, such as an incomplete search result.
     *
     */
    public static final class Result extends Object {
        
        private List<? super SymbolDescriptor> result;
        private String[] message;

        Result(List<? super SymbolDescriptor> result, String[] message) {
            this.result = result;
            this.message = message;
        }
        
        /**
         * Optional message. It can inform the user about result, e.g.
         * that result can be incomplete etc.
         * 
         * @param  msg  message
         */
        public void setMessage(String msg) {
            message[0] = msg;
        }

        /**
          * Adds result descriptor.
          *
          * @param  symbolDescriptor  symbol descriptor to be added to result
          */
        public void addResult(SymbolDescriptor symbolDescriptor) {
            result.add(symbolDescriptor);
        }

        /**
          * Adds list of result descriptors.
          *
          * @param  symbolDescriptor  symbol descriptor to be added to result
          */
        @SuppressWarnings("unchecked")
        public void addResult(List<? extends SymbolDescriptor> symbolDescriptor) {
            ((List)result).addAll(symbolDescriptor);    //workaround javac issue http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6507334 
        }
    }

}
