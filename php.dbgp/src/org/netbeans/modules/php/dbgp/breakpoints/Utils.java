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
package org.netbeans.modules.php.dbgp.breakpoints;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JEditorPane;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.breakpoints.FunctionBreakpoint.Type;
import org.netbeans.modules.php.dbgp.packets.BrkpntCommandBuilder;
import org.netbeans.modules.php.dbgp.packets.BrkpntRemoveCommand;
import org.netbeans.modules.php.dbgp.packets.BrkpntSetCommand;
import org.netbeans.modules.php.dbgp.packets.BrkpntSetCommand.State;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;


/**
 * @author ads
 *
 */
public class Utils {
    
    final static String  MIME_TYPE = "text/x-php5"; //NOI18N
    public static LineFactory lineFactory = new LineFactory();
    
    private Utils(){
        // avoid inst-ion
    }
    
    public static Line getCurrentLine() {
        FileObject fileObject = EditorContextDispatcher.getDefault().getCurrentFile();
        
        if (!isPhpFile(fileObject)) {
            return null;
        }
        
        return EditorContextDispatcher.getDefault().getCurrentLine();
    }
    
    public static BrkpntSetCommand getCommand( DebugSession session, SessionId id,
            AbstractBreakpoint breakpoint )
    {
        if ( !breakpoint.isSessionRelated(session) ){
            return null;
        }
        
        BrkpntSetCommand command = null;
        if (breakpoint instanceof LineBreakpoint) {
            LineBreakpoint lineBreakpoint = (LineBreakpoint) breakpoint;
            command = BrkpntCommandBuilder.buildLineBreakpoint(id, session
                    .getTransactionId(), lineBreakpoint);

        }
        else if ( breakpoint instanceof FunctionBreakpoint ){
            FunctionBreakpoint functionBreakpoint = (FunctionBreakpoint) breakpoint;
            Type type = functionBreakpoint.getType();
            if ( type == Type.CALL ){
                command = BrkpntCommandBuilder.buildCallBreakpoint(
                        session.getTransactionId(), functionBreakpoint );
            }
            else if( type == Type.RETURN ){
                command = BrkpntCommandBuilder.buildReturnBreakpoint(
                        session.getTransactionId(), functionBreakpoint );
            }
            else {
                assert false;
            }
        }
        
        if (!breakpoint.isEnabled()) {
            command.setState(State.DISABLED);
        }
        
        return command;
    }
    
    public static AbstractBreakpoint getBreakpoint( String id ) {
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager()
                .getBreakpoints();
        for (Breakpoint breakpoint : breakpoints) {
            if (!(breakpoint instanceof AbstractBreakpoint)) {
                continue;
            }
            AbstractBreakpoint bkpnt = (AbstractBreakpoint) breakpoint;
            String bkpntId = bkpnt.getBreakpointId();
            if ( id.equals( bkpntId )){
                return bkpnt;
            }
        }
        return null;
    }

    public static void cleanBreakpoint( DebugSession session , 
            String breakpointId )
    {
        BrkpntRemoveCommand removeCommand = new BrkpntRemoveCommand(
                session.getTransactionId() , breakpointId );
        session.sendCommandLater( removeCommand );
    }
    
    public static void log( Throwable exception ){
        exception.printStackTrace();
    }
    
    public static void log( InvocationTargetException exception ){
        log( exception.getCause() );
    }
    
    public static boolean isPhpFile(FileObject fileObject) {
        if (fileObject == null) {
            return false;
        }
        else {
            String mimeType = fileObject.getMIMEType();
            return MIME_TYPE.equals(mimeType);
        }
    }

    /**
     * NB : <code>line</code> is 1-based debugger DBGP line.
     * It differs from editor line !  
     * @param line 1-based line in file
     * @param remoteFileName  remote file name
     * @param id current debugger session id
     * @return
     */
    public static Line getLine( int line, String remoteFileName , SessionId id) {
        return lineFactory.getLine(line, remoteFileName, id);
    }
    
    public static class LineFactory {
        public Line getLine(int line, String remoteFileName, SessionId id) {
            DataObject dataObject = id.getDataObjectByRemote(remoteFileName);
            if (dataObject == null) {
                return null;
            }

            LineCookie lineCookie = (LineCookie) dataObject.getCookie(LineCookie.class);
            if (lineCookie == null) {
                return null;
            }
            Line.Set set = lineCookie.getLineSet();
            if (set == null) {
                return null;
            }
            if (line < 0 || set.getLines().size() < line) {
                return null;
            }
            return set.getOriginal(line - 1);
        }        
    }
}
