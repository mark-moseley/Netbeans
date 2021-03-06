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

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.tasklist.impl.ScannerDescriptor;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;

/**
 *
 * @author S. Aubrecht
 */
public final class TaskFilter {
    
    public static final TaskFilter EMPTY = new TaskFilter( Util.getString( "no-filter" ) ); //NOI18N
    
    private String name;
    private KeywordsFilter keywords;
    private TypesFilter types;
    
    TaskFilter( String name ) {
        this.name = name;
    }
    
    TaskFilter() {
    }
    
    private TaskFilter( TaskFilter src ) {
        this.name = src.name;
        keywords = null == src.keywords ? null : (KeywordsFilter)src.keywords.clone();
        types = null == src.types ? null : (TypesFilter)src.types.clone();
    }
     
    public boolean accept( Task task ) {
        return null == keywords ? true : keywords.accept( task );
    }
    
    public boolean isEnabled( FileTaskScanner scanner ) {
        return null == types ? true : types.isEnabled( ScannerDescriptor.getType( scanner ) );
    }
    
    public boolean isEnabled( PushTaskScanner scanner ) {
        return null == types ? true : types.isEnabled( ScannerDescriptor.getType( scanner ) );
    }
    
    public boolean isTaskCountLimitReached( int currentTaskCount ) {
        return null == types ? false : types.isTaskCountLimitReached( currentTaskCount );
    }
    
    public String getName() {
        return name;
    }
    
    void setName( String newName ) {
        this.name = newName;
    }
    
    KeywordsFilter getKeywordsFilter() {
        return keywords;
    }
    
    void setKeywordsFilter( KeywordsFilter f ) {
        this.keywords = f;
    }
    
    TypesFilter getTypesFilter() {
        return types;
    }
    
    void setTypesFilter( TypesFilter f ) {
        this.types = f;
    }
    
    @Override
    public Object clone() {
        return new TaskFilter( this );
    } 
    
    @Override
    public String toString() {
        return name;
    }
    
    void load( Preferences prefs, String prefix ) throws BackingStoreException {
        name = prefs.get( prefix+"_name", "Filter" ); //NOI18N //NOI18N
        if( prefs.getBoolean( prefix+"_types", false ) ) { //NOI18N
            types = new TypesFilter();
            types.load( prefs, prefix+"_types" ); //NOI18N
        } else {
            types = null;
        }
        
        if( prefs.getBoolean( prefix+"_keywords", false ) ) { //NOI18N
            keywords = new KeywordsFilter();
            keywords.load( prefs, prefix+"_keywords" ); //NOI18N
        } else {
            keywords = null;
        }
    }
    
    void save( Preferences prefs, String prefix ) throws BackingStoreException {
        prefs.put( prefix+"_name", name ); //NOI18N
        
        if( null != types ) {
            prefs.putBoolean( prefix+"_types", true ); //NOI18N
            types.save( prefs, prefix+"_types" ); //NOI18N
        } else {
            prefs.putBoolean( prefix+"_types", false ); //NOI18N
        }
        
        if( null != keywords ) {
            prefs.putBoolean( prefix+"_keywords", true ); //NOI18N
            keywords.save( prefs, prefix+"_keywords" ); //NOI18N
        } else {
            prefs.putBoolean( prefix+"_keywords", false ); //NOI18N
        }
    }
} 
