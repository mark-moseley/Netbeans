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

package org.netbeans.modules.parsing.impl;

import java.util.Set;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.impl.event.EventSupport;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public abstract class SourceAccessor {
    
    public static synchronized SourceAccessor getINSTANCE () {
        if (INSTANCE == null) {
            try {
                Class.forName("org.netbeans.modules.parsing.api.Source", true, SourceAccessor.class.getClassLoader());   //NOI18N            
                assert INSTANCE != null;
            } catch (ClassNotFoundException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return INSTANCE;
    }
    
    public static void setINSTANCE (SourceAccessor instance) {
        assert instance != null;
        INSTANCE = instance;
    }
    
    private static volatile SourceAccessor INSTANCE;
    
    /**
     * Sets given {@link SourceFlags} of given {@link Source}
     * @param source
     * @param flags
     */
    public abstract void setFlags (Source source, Set<SourceFlags> flags);

    /**
     * Tests if given {@link Source} has a given {@link SourceFlags}
     * @param source
     * @param flag
     * @return true if the source has given flag
     */
    public abstract boolean testFlag (Source source, SourceFlags flag);

    /**
     * Removes a given {@link SourceFlags} from a given {@link Source}
     * @param source
     * @param flag
     * @return true if the source had given flag and it was removed
     */
    public abstract boolean cleanFlag (Source source, SourceFlags flag);

    /**
     * Tests if a given {@link Source} has a given {@link SourceFlags} and cleans the
     * clean flags.
     * @param source
     * @param flag
     * @return true if the source had given flag
     */
    public abstract boolean testAndCleanFlags (Source source, SourceFlags test, Set<SourceFlags> clean);

    /**
     * Invalidates given {@link Source}
     * @param source to be invalidated
     * @param force if true source is always invalidated
     */
    public abstract void invalidate (Source source, boolean force);

    public abstract void setEvent (Source source, SchedulerEvent event);
    
    public abstract SchedulerEvent getEvent (Source source);
    
    /**
     * Returns cached {@link Parser} when available
     * @param source for which the parser should be obtained
     * @return the {@link Parser} or null
     */
    public abstract Parser getParser (Source source);
    
    /**
     * Sets a cached {@link Parser}.
     * Used only by ParserManagerImpl
     * @param source for which the parser should be set
     * @param the parser
     * @throws IllegalStateException when the given source is already associated
     * with a parser.
     */
    public abstract void setParser (Source source, Parser parser) throws IllegalStateException;
    
    /**
     * SPI method - don't call it directly.
     * Called when Source is passed to TaskProcessor to start listening.
     * @param source to assign listeners to
     */
    public abstract void assignListeners(Source source);
    
    /**
     * SPI method - don't call it directly.
     * Returns event support, used only by Utilities bridge, will be removed
     * @param source
     * @return EventSupport
     */
    public abstract EventSupport getEventSupport (Source source);
    
    public abstract SourceCache getCache (Source source);

    /**
     * SPI method - don't call it directly.
     * Called by the TaskProcessor when a new ParserResultTask is registered
     * @param source for which the task was registered
     * @return number of already registered tasks
     */
    public abstract int taskAdded (Source source);
    
    /**
     * SPI method - don't call it directly.
     * Called by the TaskProcessor when a ParserResultTask is unregistered
     * @param source for which the task was unregistered
     * @return number of still registered tasks
     */
    public abstract int taskRemoved (Source source);
}
