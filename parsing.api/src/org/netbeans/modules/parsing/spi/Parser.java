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

package org.netbeans.modules.parsing.spi;

import javax.swing.event.ChangeListener;

import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;


/**
 * Represents implementation of parser for one or more languages. 
 * Parser is always created by {@link ParserFactory}.
 * Parser fires change when some conditions are changed and all sources 
 * should be reparsed.
 * 
 * @author Jan Jancura
 */
public abstract class Parser {
    
    /**
     * Called when some client needs some result of parsing. Parser must parse
     * source based on {@link Source#getText()} method call. 
     * Original document is there for various registration purposes only.
     * 
     * @param source        A source that should be parsed.
     * @param task          A task asking for parsing result.
     * @return              Result of parsing or null.
     */
    public abstract Result parse (
        Source              source,
        Task                task
    );
        
    
    /**
     * Called by the infrastructure to stop the parser operation.
     */
    public abstract void cancel ();
    
    /**
     * Registers new listener.
     * 
     * @param changeListener
     *                      A change listener to be regiserred.
     */
    public abstract void addChangeListener (
        ChangeListener      changeListener
    );
    
    /**
     * Unregisters listener.
     * 
     * @param changeListener
     *                      A change listener to be unregiserred.
     */
    public abstract void removeChangeListener (
        ChangeListener      changeListener
    );
    
    /**
     * Represents result of parsing. Implementation of this class should 
     * provide AST for parsed file, semantic information, etc.
     */
    public abstract static class Result {
        
        /**
         * This method is called by Parsing API, when tash is finished.
         */
        public abstract void invalidate ();
    }
}




