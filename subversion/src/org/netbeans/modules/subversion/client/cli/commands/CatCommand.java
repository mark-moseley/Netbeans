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

package org.netbeans.modules.subversion.client.cli.commands;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.netbeans.modules.subversion.client.cli.SvnCommand;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class CatCommand extends SvnCommand {

    private enum CatType {
        url,
        file,
    }
    
    private final CatType type;
    
    private SVNUrl url;
    private File file;    
    private SVNRevision rev;
    
    private byte[] bytes;
    
    public CatCommand(SVNUrl url, SVNRevision rev) {        
        this.url = url;                
        this.rev = rev;        
        this.file = null;
        type = CatType.url;

    }
    
    public CatCommand(File file, SVNRevision rev) {        
        this.file = file;
        this.rev = rev;        
        this.url = null;                
        type = CatType.file;
    }

    public InputStream getOutput() {
        return new ByteArrayInputStream(bytes == null ? new byte[] {} : bytes);
    }

    @Override
    public List<String> getCmdError() {
        return null; // XXX don't throw errors to emulate svnCA behavior
    }
    
    @Override
    protected boolean hasBinaryOutput() {
        return true;
    }

    @Override
    protected boolean notifyOutput() {
        return false;
    }    
    
    @Override
    public void output(byte[] bytes) {
        this.bytes = bytes;
    }
    
    @Override
    protected int getCommand() {
        return ISVNNotifyListener.Command.CAT;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {        
        arguments.add("cat");        
        switch(type) {
            case url: 
                arguments.add(url);                
                break;
            case file:     
                arguments.add(file);
                setCommandWorkingDirectory(file);
                break;
            default :    
                throw new IllegalStateException("Illegal cattype: " + type);                             
        }                
        arguments.add(rev);
    }    
}
