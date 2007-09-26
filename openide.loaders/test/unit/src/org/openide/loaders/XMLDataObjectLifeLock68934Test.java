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
package org.openide.loaders;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.Environment.Provider;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.xml.sax.SAXException;

/** There was a problem for objects queried twice from inside the XML parsing and 
 * lookup preparation.
 *
 * @author Jaroslav Tulach
 */
public class XMLDataObjectLifeLock68934Test extends LoggingTestCaseHid 
implements Node.Cookie {

    private ErrorManager err;
    
    public XMLDataObjectLifeLock68934Test(String s) {
        super(s);
    }
    protected void setUp() throws Exception {
        clearWorkDir();
        
        err = ErrorManager.getDefault().getInstance("TEST-" + getName());
    }
    
    public void testQueryFromInsideTheQuery() throws IOException {
        FileObject res = FileUtil.createData(
            Repository.getDefault().getDefaultFileSystem().getRoot(), 
            getName() + "/R.xml"
        );
        
        err.log("file created: " + res);
        org.openide.filesystems.FileLock l = res.lock();
        OutputStream os = res.getOutputStream(l);
        err.log("stream opened");
        PrintStream ps = new PrintStream(os);
        
        ps.println("<?xml version='1.0' encoding='UTF-8'?>");
        ps.println("<!DOCTYPE MIME-resolver PUBLIC '-//NetBeans//DTD MIME Resolver 1.0//EN' 'http://www.netbeans.org/dtds/mime-resolver-1_0.dtd'>");
        ps.println("<MIME-resolver>");
        ps.println("    <file>");
        ps.println("        <ext name='lenka'/>");
        ps.println("        <resolver mime='hodna/lenka'/>");
        ps.println("    </file>");
        ps.println("</MIME-resolver>");

        err.log("Content written");
        os.close();
        err.log("Stream closed");
        l.releaseLock();
        err.log("releaseLock");
    
        
        final DataObject obj = DataObject.find(res);
        
        class EP implements Environment.Provider, Node.Cookie {
            public EP query = this;
            
            public Lookup getEnvironment(DataObject obj) {
                
                if (query == this) {
                    query = null;
                    query = (EP)obj.getCookie(EP.class);
                
                    assertEquals("Right object: ", XMLDataObject.class, obj.getClass());
                    XMLDataObject xml = (XMLDataObject)obj;
                    String id = null;
                    try {
                        id = xml.getDocument().getDoctype().getPublicId();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        fail("No exception");
                    } catch (SAXException ex) {
                        ex.printStackTrace();
                        fail("No exception");
                    }
                    assertEquals("-//NetBeans//DTD MIME Resolver 1.0//EN", id);
                }
                
                
                return Lookups.singleton(this);
            }
        };
        
        EP environmentProvider = new EP();
        registerIntoLookup(environmentProvider);
        

        EP mine = (EP)obj.getCookie(EP.class);
        
        assertEquals("provider is objects cookie", environmentProvider, mine);
        assertEquals("query inside works as well", environmentProvider, environmentProvider.query);
    }
    
        
}
