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

import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;


/**
 * Represents implementation of parser for one language. 
 * Parser is always created by {@link ParserFactory} for some concrete 
 * {@link Snapshot} or collection of {@link Snapshot}s.
 * Parser fires change when some conditions are changed and sources 
 * should be reparsed. One instance of Parser can be reused for more Snapshots
 * created from one Source.
 * 
 * @author Jan Jancura
 */
public abstract class Parser {
    
    /**
     * Called by infastructure when {@link org.netbeans.modules.parsing.api.Source} 
     * is changed, and a new {@link org.netbeans.modules.parsing.api.Snapshot}
     * has been created for it. 
     * 
     * @param snapshot      A snapshot that should be parsed.
     * @param task          A task asking for parsing result.
     * @param event         A scheduler event.
     */
    public abstract void parse (
        Snapshot            snapshot,
        Task                task,
        SchedulerEvent      event
    ) throws ParseException;
    
    /**
     * Called when some task needs some result of parsing.
     * 
     * @param task          A task asking for parsing result.
     * @return              Result of parsing or null.
     */
    public abstract Result getResult (
        Task                task,
        SchedulerEvent      event
    ) throws ParseException;
        
    
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
     * Represents result of parsing created for one specific {@link Task}. 
     * Implementation of this class should provide AST for parsed file, 
     * semantic information, etc. A new instance of Result is created for each
     * Task. When Task execution is finished, task should be invalidated.
     */
    public abstract static class Result {
        
        /**
         * This method is called by Parsing API, when {@link Task} is finished.
         */
        public abstract void invalidate ();
    }
}




