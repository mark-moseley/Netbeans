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

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.api.SessionId;
import org.netbeans.modules.php.dbgp.breakpoints.AbstractBreakpoint;
import org.netbeans.modules.php.dbgp.breakpoints.Utils;
import org.netbeans.modules.php.dbgp.packets.FeatureGetCommand.Feature;
import org.w3c.dom.Node;


/**
 * @author ads
 *
 */
public class InitMessage extends DbgpMessage {
    
    private static final String IDEKEY  = "idekey";         // NOI18N
    
    private static final String FILE    = "fileuri";        // NOI18N

    InitMessage( Node node ) {
        super(node);
    }
    
    public String getSessionId() {
        // accessor to idekey attribute
        return getAttribute(getNode(), IDEKEY );
    }
    
    public String getFileUri() {
        return getAttribute(getNode(), FILE );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.dbgp.packets.DbgpMessage#process(org.netbeans.modules.php.dbgp.DebugSession)
     */
    @Override
    public void process( DebugSession session, DbgpCommand command )
    {
        setId( session );

        setMaxDataSize( session );
        
        setBreakpoints( session );
        
        RunCommand runCommand = new RunCommand( session.getTransactionId() );
        session.sendCommandLater( runCommand );
    }

    private void setMaxDataSize( DebugSession session ) {
        FeatureGetCommand command = new FeatureGetCommand( 
                session.getTransactionId() );
        command.setFeature( Feature.MAX_DATA );
        DbgpResponse response = session.sendSynchronCommand(command);
        assert response instanceof FeatureGetResponse;
        FeatureGetResponse featureGetResponse = (FeatureGetResponse)response;
        String size = featureGetResponse.getDetails();
        Integer maxSize = 0;
        try {
            maxSize = Integer.parseInt(size);
        }
        catch( NumberFormatException e ) {
            // just skip 
        }
        int current = DbgpMessage.getMaxDataSize();
        if ( current > maxSize  ) {
            FeatureSetCommand setCommand = new FeatureSetCommand( 
                    session.getTransactionId());
            setCommand.setFeature(Feature.MAX_DATA);
            setCommand.setValue( current +"");
            response = session.sendSynchronCommand(setCommand);
            assert response instanceof FeatureSetResponse;
            FeatureSetResponse setResponse = (FeatureSetResponse) response;
            if ( !setResponse.isSuccess() ) {
                DbgpMessage.setMaxDataSize( maxSize );
            }
        }
        else {
            DbgpMessage.setMaxDataSize( maxSize );
        }
    }

    private void setBreakpoints( DebugSession session ) {
        SessionId id = session.getSessionId();
        Breakpoint[] breakpoints = 
            DebuggerManager.getDebuggerManager().getBreakpoints();
        for (Breakpoint breakpoint : breakpoints) {
            if ( !(breakpoint instanceof AbstractBreakpoint)){
                continue;
            }
            AbstractBreakpoint brkpnt = (AbstractBreakpoint) breakpoint;
            BrkpntSetCommand command = Utils.getCommand(session, id, brkpnt);

            if (command == null) {
                continue;
            }

            session.sendCommandLater(command);
        }
        
    }

    private void setId( DebugSession session ) {
        session.setId( this );
    }

}
