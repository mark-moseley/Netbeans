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
package org.netbeans.modules.php.dbgp.packets;

import org.netbeans.modules.php.dbgp.packets.ContextNamesResponse.Context;


/**
 * @author ads
 *
 */
public class ContextGetCommand extends DbgpCommand {
    
    static final String CONTEXT_GET         = "context_get";    // NOI18N
    
    private static final String CONTEXT_ARG = "-c ";            // NOI18N

    public ContextGetCommand( String transactionId ) {
        super( CONTEXT_GET , transactionId);
        myDepth = -1;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.packets.DbgpCommand#wantAcknowledgment()
     */
    @Override
    public boolean wantAcknowledgment()
    {
        return true;
    }
    
    public void setDepth( int depth ){
        myDepth = depth;
    }
    
    @Override
    protected String getArguments()
    {
        StringBuilder builder = new StringBuilder();
        if ( myDepth > -1 ){
            builder.append( ContextNamesCommand.DEPTH_ARG );
            builder.append( myDepth );
        }
        
        if (myContext != null) {
            if (builder.length() != 0) {
                builder.append(BrkpntSetCommand.SPACE);
            }
            builder.append( CONTEXT_ARG );
            builder.append( myContext.getId() );
        }
        
        return builder.toString();
    }
    
    
    public void setContext( Context context ){
        myContext = context;
    }
    
    public Context getContext() {
        return myContext;
    }
    
    private int myDepth;

    private Context myContext;
}
