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

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.subversion.client.cli.SvnCommand;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class CopyCommand extends SvnCommand {

    private enum CopyType {
        url2url,
        url2file,
        file2url,
        file2file,
    }
    
    private final CopyType type;
    
    private SVNUrl fromUrl;
    private SVNUrl toUrl;
    private File fromFile;    
    private File toFile;
    private String msg;
    private SVNRevision rev;

    public CopyCommand(SVNUrl fromUrl, SVNUrl toUrl, String msg, SVNRevision rev) {        
        this.fromUrl = fromUrl;
        this.toUrl = toUrl;
        this.msg = msg;
        this.rev = rev;        
        type = CopyType.url2url;
    }

    public CopyCommand(SVNUrl fromUrl, File toFile, SVNRevision rev) {        
        this.fromUrl = fromUrl;
        this.toFile = toFile;
        this.rev = rev;        
        type = CopyType.url2file;
    }
    
    public CopyCommand(File fromFile, SVNUrl toUrl, String msg) {        
        this.fromFile = fromFile;
        this.toUrl = toUrl;        
        this.msg = msg;        
        type = CopyType.file2url;
    }
    
    @Override
    protected int getCommand() {
        return ISVNNotifyListener.Command.COPY;
    }
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {        
        arguments.add("copy");        
        switch(type) {
            case url2url: 
                arguments.add(fromUrl);
                arguments.add(toUrl);        
                arguments.add(rev);                
                break;
            case url2file:     
                arguments.add(fromUrl);
                arguments.add(toFile);        
                arguments.add(rev);                
                setCommandWorkingDirectory(toFile);                
                break;
            case file2url:                     
                arguments.add(fromFile);        
                arguments.add(toUrl);
                setCommandWorkingDirectory(fromFile);                
                break;
            default :    
                throw new IllegalStateException("Illegal copytype: " + type);                             
        }                
        arguments.addMessage(msg);
    }    
}
