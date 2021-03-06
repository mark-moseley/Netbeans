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

package org.netbeans.modules.ant.debugger.breakpoints;

import java.lang.IllegalArgumentException;
import java.lang.IndexOutOfBoundsException;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;


/**
 *
 * @author Jan Jancura
 */
public class BreakpointsReader implements Properties.Reader {
    
    
    public String [] getSupportedClassNames () {
        return new String[] {
            AntBreakpoint.class.getName (), 
        };
    }
    
    public Object read (String typeID, Properties properties) {
        if (!(typeID.equals (AntBreakpoint.class.getName ())))
            return null;
        
        Line line = getLine (
            properties.getString ("url", null),
            properties.getInt ("lineNumber", 1));
        if (line == null) return null;
        return new AntBreakpoint (line);
    }
    
    public void write (Object object, Properties properties) {
        AntBreakpoint b = (AntBreakpoint) object;
        FileObject fo = (FileObject) b.getLine ().getLookup ().
            lookup (FileObject.class);
        try {
            properties.setString ("url", fo.getURL ().toString ());
            properties.setInt (
                "lineNumber", 
                b.getLine ().getLineNumber ()
            );
        } catch (FileStateInvalidException ex) {
            ex.printStackTrace ();
        }
    }
    

    private Line getLine (String url, int lineNumber) {
        FileObject file;
        try {
            file = URLMapper.findFileObject (new URL (url));
        } catch (MalformedURLException e) {
            return null;
        }
        if (file == null) return null;
        DataObject dataObject = null;
        try {
            dataObject = DataObject.find (file);
        } catch (DataObjectNotFoundException ex) {
            return null;
        }
        if (dataObject == null) return null;
        LineCookie lineCookie = (LineCookie) dataObject.getCookie
            (LineCookie.class);
        if (lineCookie == null) return null;
        Line.Set ls = lineCookie.getLineSet ();
        if (ls == null) return null;
        try {
            return ls.getCurrent (lineNumber);
        } catch (IndexOutOfBoundsException e) {
        } catch (IllegalArgumentException e) {
        }
        return null;
    }
}
