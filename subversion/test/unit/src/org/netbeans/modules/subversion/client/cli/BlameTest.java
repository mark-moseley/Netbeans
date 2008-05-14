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

package org.netbeans.modules.subversion.client.cli;

import java.io.File;
import java.util.Date;
import org.tigris.subversion.svnclientadapter.ISVNAnnotations;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNRevision.Number;

/**
 *
 * @author tomas
 */
// XXX add referenceclient
public class BlameTest extends AbstractCLITest {
    
    public BlameTest(String testName) throws Exception {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {        
        if(getName().equals("testBlameFileNullAuthor") || 
           getName().equals("testBlameUrlNullAuthor") ) {
            setAnnonWriteAccess();
            String[] cmd = new String[]{"svnserve", "-d"};
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();   
        }                        
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if(getName().equals("testBlameFileNullAuthor") || 
           getName().equals("testBlameUrlNullAuthor") ) {        
            restoreAuthSettings();
        }
    }

    @Override
    protected String getRepoURLProtocol() {
        if(getName().equals("testBlameFileNullAuthor") || 
           getName().equals("testBlameUrlNullAuthor") ) {        
            return "svn://localhost/";
        }
        return super.getRepoURLProtocol();
    }

    public void testBlameWrong() throws Exception {                                
        // unversioned
        File file = createFile("file");
        
        ISVNClientAdapter c = getNbClient();
        SVNClientException e = null;        
        try {
            c.annotate(file, null, null);
        } catch (SVNClientException ex) {
            e = ex;
        }
        assertNotNull(e);

        // file not exist        
        file = new File(getWC(), "file");                
        e = null;        
        try {
            c.annotate(file, null, null);
        } catch (SVNClientException ex) {
            e = ex;
        }
        assertNotNull(e);
               
        // wrong url
        file = new File(getWC(), "file");                
        e = null;        
        try {
            c.annotate(getFileUrl(file), null, null);
        } catch (SVNClientException ex) {
            e = ex;
        }
        assertNotNull(e);        
        
    }
    
    public void testBlameFile() throws Exception {                                
        blame(fileAnnotator);
    }
    
    public void testBlameUrl() throws Exception {                                
        blame(urlAnnotator);
    }
    
    private void blame(Annotator annotator) throws Exception {                                
        File file = createFile("file");
        add(file);   
        commit(file);
        
        // 1. line
        write(file, "a\n");
        commit(file);
        ISVNInfo info = getInfo(file);
        String author1 = info.getLastCommitAuthor();        
        Date date1 = info.getLastChangedDate();
        Number rev1 = info.getRevision();
        
        // 2. line
        write(file, "a\nb\n");
        commit(file);
        info = getInfo(file);        
        String author2 = info.getLastCommitAuthor();
        Date date2 = info.getLastChangedDate();
        Number rev2 = info.getRevision();
        // 3. line
        write(file, "a\nb\nc\n");
        commit(file);
        info = getInfo(file);        
        String author3 = info.getLastCommitAuthor();
        Date date3 = info.getLastChangedDate();
        Number rev3 = info.getRevision();
        
        ISVNAnnotations a = annotator.annotate(file, null, null);
        
        // test 
        assertEquals(3, a.numberOfLines());
        // XXX a.getInputStream()
        
        assertEquals("a", a.getLine(0));
        assertEquals("b", a.getLine(1));
        assertEquals("c", a.getLine(2));
        
        assertEquals(author1, a.getAuthor(0));
        assertEquals(author2, a.getAuthor(1));
        assertEquals(author3, a.getAuthor(2));
        
        assertEquals(date1, a.getChanged(0));
        assertEquals(date2, a.getChanged(1));
        assertEquals(date3, a.getChanged(2));

        assertEquals(rev1.getNumber(), a.getRevision(0));
        assertEquals(rev2.getNumber(), a.getRevision(1));
        assertEquals(rev3.getNumber(), a.getRevision(2));        
    }
    
    public void testBlameCopied() throws Exception {                                
        File file = createFile("file");
        add(file);   
        commit(file);
        
        // 1. line
        write(file, "a\n");
        commit(file);
        ISVNInfo info = getInfo(file);
        String author1 = info.getLastCommitAuthor();        
        Date date1 = info.getLastChangedDate();
        Number rev1 = info.getRevision();
        
        // 2. line
        write(file, "a\nb\n");
        commit(file);
        info = getInfo(file);        
        String author2 = info.getLastCommitAuthor();
        Date date2 = info.getLastChangedDate();
        Number rev2 = info.getRevision();
        // 3. line
        write(file, "a\nb\nc\n");
        commit(file);
        info = getInfo(file);        
        String author3 = info.getLastCommitAuthor();
        Date date3 = info.getLastChangedDate();
        Number rev3 = info.getRevision();
        
        File copy = new File(getWC(), "copy");
               
        ISVNClientAdapter c = getNbClient();
        copy(file, copy);
        ISVNAnnotations a = c.annotate(copy, null, null);
        
        // test 
        assertEquals(3, a.numberOfLines());
        // XXX a.getInputStream()
        
        assertEquals("a", a.getLine(0));
        assertEquals("b", a.getLine(1));
        assertEquals("c", a.getLine(2));
        
        assertEquals(author1, a.getAuthor(0));
        assertEquals(author2, a.getAuthor(1));
        assertEquals(author3, a.getAuthor(2));
        
        assertEquals(date1, a.getChanged(0));
        assertEquals(date2, a.getChanged(1));
        assertEquals(date3, a.getChanged(2));

        assertEquals(rev1.getNumber(), a.getRevision(0));
        assertEquals(rev2.getNumber(), a.getRevision(1));
        assertEquals(rev3.getNumber(), a.getRevision(2));        
    }
    
    public void testBlameFileStartRevEndRev() throws Exception {                                
        blameStartRevEndRev(fileAnnotator);
    }

    public void testBlameUrlStartRevEndRev() throws Exception {                                
        blameStartRevEndRev(urlAnnotator);
    }
    
    private void blameStartRevEndRev(Annotator annotator) throws Exception {                                
        File file = createFile("file");
        add(file);   
        commit(file);
        
        // 1. line
        write(file, "a\n");
        commit(file);
        ISVNInfo info = getInfo(file);
        String author1 = info.getLastCommitAuthor();        
        Date date1 = info.getLastChangedDate();
        Number rev1 = info.getRevision();
        
        // 2. line
        write(file, "a\nb\n");
        commit(file);
        info = getInfo(file);        
        String author2 = info.getLastCommitAuthor();
        Date date2 = info.getLastChangedDate();
        Number rev2 = info.getRevision();
        // 3. line
        write(file, "a\nb\nc\n");
        commit(file);
        info = getInfo(file);                
        
        ISVNAnnotations a = annotator.annotate(file, rev1, rev2);
        
        // test 
        assertEquals(2, a.numberOfLines());
        
        assertEquals("a", a.getLine(0));
        assertEquals("b", a.getLine(1));
        
        assertEquals(author1, a.getAuthor(0));
        assertEquals(author2, a.getAuthor(1));
        
        assertEquals(date1, a.getChanged(0));
        assertEquals(date2, a.getChanged(1));

        assertEquals(rev1.getNumber(), a.getRevision(0));
        assertEquals(rev2.getNumber(), a.getRevision(1));
    }
    
    public void testBlameFileNullAuthor() throws Exception {                                
        blameNullAuthor(fileAnnotator);
    }
    
    public void testBlameUrlNullAuthor() throws Exception {                                
        blameNullAuthor(urlAnnotator);
    }
    
    private void blameNullAuthor(Annotator annotator) throws Exception {                                
        
        File file = createFile("file");
        add(file);   
        commit(file);
        
        // 1. line
        write(file, "a\n");
        commit(file);
        ISVNInfo info = getInfo(file);
        Number rev1 = info.getRevision();

        ISVNAnnotations a = annotator.annotate(file, null, null);
        
        // test 
        assertEquals(1, a.numberOfLines());        
        assertEquals("a", a.getLine(0));        
        assertNull(a.getAuthor(0));
        // assertNull(a.getChanged(0)); is null only for svnClientAdapter
        assertEquals(rev1.getNumber(), a.getRevision(0));
    }

    private abstract class Annotator {
        protected abstract ISVNAnnotations annotate(File file, SVNRevision revStart, SVNRevision revEnd) throws Exception;
    }
    private Annotator fileAnnotator = new Annotator() {
        public ISVNAnnotations annotate(File file, SVNRevision revStart, SVNRevision revEnd) throws Exception {
            ISVNClientAdapter c = getNbClient();            
            return c.annotate(file, revStart, revEnd);
        }        
    };
    private Annotator urlAnnotator = new Annotator() {
        public ISVNAnnotations annotate(File file, SVNRevision revStart, SVNRevision revEnd) throws Exception {
            ISVNClientAdapter c = getNbClient();            
            return c.annotate(getFileUrl(file), revStart, revEnd);
        }        
    };    
    
}
