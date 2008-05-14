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
public class PropertyGetCommand extends SvnCommand {

    private enum GetType {
        url,
        file
    }
    
    private final File file;    
    private final SVNUrl url;
    private final SVNRevision rev;
    private final SVNRevision peg;
    private final String name;
    private final GetType type;
    
    private byte[] bytes;
    
    public PropertyGetCommand(File file, String name) {        
        this.file = file;                
        this.name = name; 
        url = null;
        rev = null;
        peg = null;
        type = GetType.file;
    }
    
    public PropertyGetCommand(SVNUrl url, SVNRevision rev, SVNRevision peg, String name) {        
        this.url = url;                
        this.name = name; 
        this.rev = rev; 
        this.peg = peg; 
        file = null;
        type = GetType.url;
    }

    public byte[] getOutput() {
        return bytes == null ? new byte[] {} : bytes;
    }

    @Override
    protected boolean hasBinaryOutput() {
        return true;
    }    
    
    @Override
    public void output(byte[] bytes) {
        this.bytes = bytes;
    }
    
    @Override
    protected int getCommand() {
        return ISVNNotifyListener.Command.PROPGET;
    }    
    
    @Override
    public void prepareCommand(Arguments arguments) throws IOException {        
        arguments.add("propget");
	arguments.add("--strict");
	arguments.add(name);
        switch (type) {
            case file:
                arguments.add(file);        
                break;
            case url:
                arguments.add(rev);
                arguments.add(url, peg);        
                break;
            default: 
                throw new IllegalStateException("Illegal gettype: " + type);    
        }	
    }    
}
