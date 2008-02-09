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

package org.netbeans.modules.web.jspparser;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI.ParseResult;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI.WebModule;
import org.netbeans.modules.web.jsps.parserapi.JspParserFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Various test cases for jsp parser cache.
 * @author Tomas Mysik
 */
public class CacheTest extends NbTestCase {

    public CacheTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setup(this);
    }

    public void xtestJspParserImpl() throws Exception {
        JspParserAPI jspParser = JspParserFactory.getJspParser();
        assertTrue(jspParser instanceof JspParserImpl);
    }

    public void xtestCachedWebModules() throws Exception {
        JspParserImpl jspParser = getJspParser();

        FileObject jspFo1 = TestUtil.getProjectFile(this, "project2", "/web/basic.jspx");
        WebModule webModule1 = TestUtil.getWebModule(jspFo1);
        jspParser.analyzePage(jspFo1, webModule1, JspParserAPI.ERROR_IGNORE);

        FileObject jspFo2 = TestUtil.getProjectFile(this, "project2", "/web/main.jsp");
        WebModule webModule2 = TestUtil.getWebModule(jspFo2);
        jspParser.analyzePage(jspFo1, webModule2, JspParserAPI.ERROR_IGNORE);

        assertTrue("Only 1 web module should be cached", jspParser.parseSupports.size() == 1);
    }

    public void xtestCachedTagLibMaps() throws Exception {
        JspParserImpl jspParser = getJspParser();

        FileObject jspFo = TestUtil.getProjectFile(this, "emptyWebProject", "/web/index.jsp");
        WebModule webModule = TestUtil.getWebModule(jspFo);

        Map<String, String[]> taglibMap1 = jspParser.getTaglibMap(webModule);
        Map<String, String[]> taglibMap2 = jspParser.getTaglibMap(webModule);

        String url1 = taglibMap1.get("/TestTagLibrary")[0];
        String url2 = taglibMap2.get("/TestTagLibrary")[0];
        assertNotNull(url1);
        assertNotNull(url2);
        assertSame("TagLibMaps should be exactly the same", url1, url2);
    }

    public void testCachedTagLibInfos() throws Exception {
        JspParserImpl jspParser = getJspParser();

        FileObject jspFo = TestUtil.getProjectFile(this, "emptyWebProject", "/web/basic1.jspx");
        WebModule webModule = TestUtil.getWebModule(jspFo);

        ParseResult result = jspParser.analyzePage(jspFo, webModule, JspParserAPI.ERROR_IGNORE);
        Collection<TagLibraryInfo> tagLibs1 = result.getPageInfo().getTaglibs();

        jspFo = TestUtil.getProjectFile(this, "emptyWebProject", "/web/basic2.jspx");
        result = jspParser.analyzePage(jspFo, webModule, JspParserAPI.ERROR_IGNORE);
        Collection<TagLibraryInfo> tagLibs2 = result.getPageInfo().getTaglibs();

        assertTrue(tagLibs1.size() > 0);
        assertTrue(tagLibs2.size() > 0);
        assertTrue(tagLibs1.size() == tagLibs2.size());

        Iterator<TagLibraryInfo> iter1 = tagLibs1.iterator();
        Iterator<TagLibraryInfo> iter2 = tagLibs2.iterator();
        while (iter1.hasNext()) {
            TagLibraryInfo tagLibraryInfo1 = iter1.next();
            TagLibraryInfo tagLibraryInfo2 = iter2.next();
            assertNotNull(tagLibraryInfo1);
            assertNotNull(tagLibraryInfo2);
            assertTrue("TagLibInfos should be exactly the same", tagLibraryInfo1 == tagLibraryInfo2);
        }
    }

    public void xtestChangedTldFile() throws Exception {
        JspParserImpl jspParser = getJspParser();

        FileObject jspFo = TestUtil.getProjectFile(this, "emptyWebProject", "/web/index.jsp");
        WebModule webModule = TestUtil.getWebModule(jspFo);

        Map<String, String[]> taglibMap1 = jspParser.getTaglibMap(webModule);

        // touch file
        touchFile("emptyWebProject", "/web/WEB-INF/c.tld");

        Map<String, String[]> taglibMap2 = jspParser.getTaglibMap(webModule);

        String url1 = taglibMap1.get("http://java.sun.com/jstl/core")[0];
        String url2 = taglibMap2.get("http://java.sun.com/jstl/core")[0];
        assertNotNull(url1);
        assertNotNull(url2);
        assertNotSame("TagLibMaps should not be exactly the same", url1, url2);
        assertEquals("TagLibMaps should be equal", url1, url2);
    }

    public void xtestAddedTldFile() throws Exception {
        JspParserImpl jspParser = getJspParser();

        FileObject jspFo = TestUtil.getProjectFile(this, "emptyWebProject", "/web/index.jsp");
        WebModule webModule = TestUtil.getWebModule(jspFo);

        Map<String, String[]> taglibMap1 = jspParser.getTaglibMap(webModule);

        // add file
        FileObject xml = TestUtil.getProjectFile(this, "project2", "/web/WEB-INF/META-INF/x.tld");
        FileObject destDir = TestUtil.getProjectFile(this, "emptyWebProject", "/web/WEB-INF/");
        xml.copy(destDir, xml.getName(), xml.getExt());
        xml = TestUtil.getProjectFile(this, "emptyWebProject", "/web/WEB-INF/x.tld");

        Map<String, String[]> taglibMap2 = jspParser.getTaglibMap(webModule);

        String url1 = taglibMap1.get("http://java.sun.com/jstl/core")[0];
        String url2 = taglibMap2.get("http://java.sun.com/jstl/core")[0];
        assertNotNull(url1);
        assertNotNull(url2);
        assertNotSame("TagLibMaps should not be exactly the same", url1, url2);
        assertEquals("TagLibMaps should be equal", url1, url2);
    }

    // this test relies on the previous test (adding tld file)
    public void xtestRemovedTldFile() throws Exception {
        JspParserImpl jspParser = getJspParser();

        FileObject jspFo = TestUtil.getProjectFile(this, "emptyWebProject", "/web/index.jsp");
        WebModule webModule = TestUtil.getWebModule(jspFo);

        Map<String, String[]> taglibMap1 = jspParser.getTaglibMap(webModule);

        // touch file
        removeFile("emptyWebProject", "/web/WEB-INF/x.tld");

        Map<String, String[]> taglibMap2 = jspParser.getTaglibMap(webModule);

        String[] url = taglibMap2.get("http://java.sun.com/jstl/xml");
        assertNull("Url should not be found", url);

        String url1 = taglibMap1.get("/TestTagLibrary")[0];
        String url2 = taglibMap2.get("/TestTagLibrary")[0];
        assertNotNull(url1);
        assertNotNull(url2);
        assertNotSame("TagLibMaps should not be exactly the same", url1, url2);
        assertEquals("TagLibMaps should be equal", url1, url2);
    }

    public void xtestChangedWebXml() throws Exception {
        JspParserImpl jspParser = getJspParser();

        FileObject jspFo = TestUtil.getProjectFile(this, "emptyWebProject", "/web/index.jsp");
        WebModule webModule = TestUtil.getWebModule(jspFo);

        Map<String, String[]> taglibMap1 = jspParser.getTaglibMap(webModule);

        // touch file
        touchFile("emptyWebProject", "/web/WEB-INF/web.xml");

        Map<String, String[]> taglibMap2 = jspParser.getTaglibMap(webModule);

        String url1 = taglibMap1.get("http://java.sun.com/jstl/core")[0];
        String url2 = taglibMap2.get("http://java.sun.com/jstl/core")[0];
        assertNotNull(url1);
        assertNotNull(url2);
        assertNotSame("TagLibMaps should not be exactly the same", url1, url2);
        assertEquals("TagLibMaps should be equal", url1, url2);
    }

    private static JspParserImpl getJspParser() {
        return (JspParserImpl) JspParserFactory.getJspParser();
    }

    private void touchFile(String projectName, String projectFile) throws Exception {
        FileObject fmtFo = TestUtil.getProjectFile(this, projectName, projectFile);
        assertNotNull(fmtFo);
        File fmt = FileUtil.toFile(fmtFo);
        assertTrue("Changing timestamp should succeed", fmt.setLastModified(System.currentTimeMillis()));
        FileUtil.refreshFor(fmt);
    }

    private void removeFile(String projectName, String projectFile) throws Exception {
        FileObject fmtFo = TestUtil.getProjectFile(this, projectName, projectFile);
        assertNotNull(fmtFo);
        fmtFo.delete();
    }

    private static boolean compareTagLibMaps(Map<String, String[]> map1, Map<String, String[]> map2) {
        if (map1 == map2) {
            return true;
        }

        if (map1.size() != map2.size()) {
            return false;
        }

        try {
            for (Entry<String, String[]> e : map2.entrySet()) {
                String key = e.getKey();
                List<String> value = Arrays.asList(e.getValue());
                List<String> value2 = Arrays.asList(map1.get(key));
                if (!value.equals(value2)) {
                    return false;
                }
            }
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }

        return true;
    }
}
