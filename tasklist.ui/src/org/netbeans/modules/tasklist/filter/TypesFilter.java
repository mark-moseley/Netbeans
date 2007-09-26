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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.tasklist.filter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.tasklist.impl.ScannerDescriptor;

/**
 *
 * @author S. Aubrecht
 */
class TypesFilter {
    
    private Set<String> disabledProviders = new HashSet<String>();
    private int countLimit = 100;
    
    public TypesFilter() {
    }
    
    private TypesFilter( TypesFilter src ) {
        this.countLimit = src.countLimit;
        this.disabledProviders.addAll( src.disabledProviders );
    }
    
    public boolean isEnabled( String type ) {
        return !disabledProviders.contains( type );
    }
    
    public void setEnabled( String type, boolean enabled ) {
        if( !enabled ) {
            disabledProviders.add( type );
        } else {
            disabledProviders.remove( type );
        }
    }
    
    public boolean isTaskCountLimitReached( int taskCount ) {
        return taskCount >= countLimit;
    }
    
    public void setTaskCountLimit( int limit ) {
        this.countLimit = limit;
    }
    
    public int getTaskCountLimit() {
        return this.countLimit;
    }
    
    public TypesFilter clone() {
        return new TypesFilter( this );
    }
    
    void load( Preferences prefs, String prefix ) throws BackingStoreException {
        countLimit = prefs.getInt( prefix+"_countLimit", 100 ); //NOI18N
        disabledProviders.clear();
        String disabled = prefs.get( prefix+"_disabled", "" ); //NOI18N //NOI18N
        StringTokenizer tokenizer = new StringTokenizer( disabled, "\n" ); //NOI18N
        while( tokenizer.hasMoreTokens() ) {
            disabledProviders.add( tokenizer.nextToken() );
        }
    }
    
    void save( Preferences prefs, String prefix ) throws BackingStoreException {
        prefs.putInt( prefix+"_countLimit", countLimit );
        StringBuffer buffer = new StringBuffer();
        for( Iterator<String> type = disabledProviders.iterator(); type.hasNext(); ) {
            buffer.append( type.next() );
            if( type.hasNext() )
                buffer.append( "\n" ); //NOI18N
        }
        prefs.put( prefix+"_disabled", buffer.toString() ); //NOI18N
    }
} 
