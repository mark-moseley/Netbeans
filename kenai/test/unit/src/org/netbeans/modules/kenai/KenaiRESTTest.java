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

package org.netbeans.modules.kenai;

import org.codeviation.pojson.PojsonLoad;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Maros Sandor
 */
public class KenaiRESTTest {

    public KenaiRESTTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testParseProjectInfo() throws Exception {
        PojsonLoad pl = PojsonLoad.create();
        JsonProjectInfo pinfo = pl.load(project_json, JsonProjectInfo.class);
        assertEquals(pinfo.name, "java-inline");
        // TODO: verify all fields
    }

    @Test
    public void testParseProjectList() throws Exception {
        PojsonLoad pl = PojsonLoad.create();
        JsonListProjects plist = pl.load(project_list_json, JsonListProjects.class);
        assertNull(plist.prev);
        assertNotNull(plist.next);
        assertEquals(plist.projects.length, 10);
        // TODO: verify all fields
    }

    private static final String project_json = "" +
            "{\n" +
            "\"href\": \"http://testkenai.com/api/projects/java-inline\", \n" +
            "\"name\": \"java-inline\", \n" +
            "\"display_name\": \"JavaInline for JRuby\", \n" +
            "\"image\": \"http://testkenai.com/images/defaultProjectImage.jpg\", \n" +
            "\"owner\": \"headius\", \n" +
            "\"description\": \"JavaInline provides a way to embed Java code into Ruby code under JRuby and have it compiled and available at runtime. Depends on Java 6 compiler API.\", \n" +
            "\"created_at\": \"2008-09-09T22:14:35Z\", \n" +
            "\"updated_at\": \"2008-09-12T22:03:10Z\", \n" +
            "\"licenses\": [\n" +
            "\t{\n" +
            "\t\"href\": \"http://testkenai.com/api/licenses/10\", \n" +
            "\t\"name\": \"LGPL-2.1\", \n" +
            "\t\"display_name\": \"GNU Lesser General Public License 2.1\", \n" +
            "\t\"license_uri\": \"http://www.opensource.org/licenses/lgpl-2.1.php\", \n" +
            "\t\"preference_level\": \"recommended\"\n" +
            "\t}\n" +
            "\t], \n" +
            "\"features\": [\n" +
            "\t{\n" +
            "\t\"href\": \"http://testkenai.com/api/projects/java-inline/features/mercurial\", \n" +
            "\t\"name\": \"mercurial\", \n" +
            "\t\"display_name\": \"Mercurial Source Code Repository\", \n" +
            "\t\"type\": \"scm\", \n" +
            "\t\"service\": \"mercurial\", \n" +
            "\t\"url\": null, \n" +
            "\t\"web_url\": \"http://testkenai.com/projects/java-inline/features/mercurial\", \n" +
            "\t\"created_at\": \"2008-09-09T22:14:40Z\", \n" +
            "\t\"updated_at\": \"2008-09-09T22:14:40Z\"\n" +
            "\t}, \n" +
            "\t{\n" +
            "\t\"href\": \"http://testkenai.com/api/projects/java-inline/features/wiki\", \n" +
            "\t\"name\": \"wiki\", \n" +
            "\t\"display_name\": \"Wiki\", \n" +
            "\t\"type\": \"wiki\", \n" +
            "\t\"service\": \"wiki\", \n" +
            "\t\"web_url\": \"http://testkenai.com/projects/java-inline/features/wiki\", \n" +
            "\t\"created_at\": \"2008-09-09T22:14:40Z\", \n" +
            "\t\"updated_at\": \"2008-09-09T22:14:41Z\"\n" +
            "\t}, \n" +
            "\t{\n" +
            "\t\"href\": \"http://testkenai.com/api/projects/java-inline/features/forum\", \n" +
            "\t\"name\": \"forum\", \n" +
            "\t\"display_name\": \"General Discussion\", \n" +
            "\t\"type\": \"forum\", \n" +
            "\t\"service\": \"forum\", \n" +
            "\t\"web_url\": \"http://testkenai.com/projects/java-inline/features/forum\", \n" +
            "\t\"created_at\": \"2008-09-09T22:14:41Z\", \n" +
            "\t\"updated_at\": \"2008-09-09T22:14:41Z\"\n" +
            "\t}, \n" +
            "\t{\n" +
            "\t\"href\": \"http://testkenai.com/api/projects/java-inline/features/commits\", \n" +
            "\t\"name\": \"commits\", \n" +
            "\t\"display_name\": \"Commits Mailing List\", \n" +
            "\t\"type\": \"lists\", \n" +
            "\t\"service\": \"lists\", \n" +
            "\t\"web_url\": \"http://testkenai.com/projects/java-inline/features/commits\", \n" +
            "\t\"created_at\": \"2008-09-09T22:14:41Z\", \n" +
            "\t\"updated_at\": \"2008-09-09T22:14:41Z\"\n" +
            "\t}, \n" +
            "\t{\n" +
            "\t\"href\": \"http://testkenai.com/api/projects/java-inline/features/dev\", \n" +
            "\t\"name\": \"dev\", \n" +
            "\t\"display_name\": \"Dev Mailing List\", \n" +
            "\t\"type\": \"lists\", \n" +
            "\t\"service\": \"lists\", \n" +
            "\t\"web_url\": \"http://testkenai.com/projects/java-inline/features/dev\", \n" +
            "\t\"created_at\": \"2008-09-09T22:14:45Z\", \n" +
            "\t\"updated_at\": \"2008-09-09T22:14:45Z\"\n" +
            "\t}, \n" +
            "\t{\n" +
            "\t\"href\": \"http://testkenai.com/api/projects/java-inline/features/issues\", \n" +
            "\t\"name\": \"issues\", \n" +
            "\t\"display_name\": \"Issues Mailing List\", \n" +
            "\t\"type\": \"lists\", \n" +
            "\t\"service\": \"lists\", \n" +
            "\t\"web_url\": \"http://testkenai.com/projects/java-inline/features/issues\", \n" +
            "\t\"created_at\": \"2008-09-09T22:14:46Z\", \n" +
            "\t\"updated_at\": \"2008-09-09T22:14:46Z\"\n" +
            "\t}, \n" +
            "\t{\n" +
            "\t\"href\": \"http://testkenai.com/api/projects/java-inline/features/users\", \n" +
            "\t\"name\": \"users\", \n" +
            "\t\"display_name\": \"Users Mailing List\", \n" +
            "\t\"type\": \"lists\", \n" +
            "\t\"service\": \"lists\", \n" +
            "\t\"web_url\": \"http://testkenai.com/projects/java-inline/features/users\", \n" +
            "\t\"created_at\": \"2008-09-09T22:14:48Z\", \n" +
            "\t\"updated_at\": \"2008-09-09T22:14:48Z\"\n" +
            "\t}, \n" +
            "\t{\n" +
            "\t\"href\": \"http://testkenai.com/api/projects/java-inline/features/bz\", \n" +
            "\t\"name\": \"bz\", \n" +
            "\t\"display_name\": \n" +
            "\t\"Issue Tracking\", \n" +
            "\t\"type\": \"issues\", \n" +
            "\t\"service\": \"issues\", \n" +
            "\t\"url\": \"http://testkenai.com/bugzilla/buglist.cgi?product=java-inline\", \n" +
            "\t\"web_url\": \"http://testkenai.com/projects/java-inline/features/bz\", \n" +
            "\t\"created_at\": \"2008-09-09T22:14:49Z\", \n" +
            "\t\"updated_at\": \"2008-09-09T22:14:49Z\"\n" +
            "\t}, \n" +
            "\t{\n" +
            "\t\"href\": \"http://testkenai.com/api/projects/java-inline/features/downloads\", \n" +
            "\t\"name\": \"downloads\", \n" +
            "\t\"display_name\": \"Downloads\", \n" +
            "\t\"type\": \"downloads\", \n" +
            "\t\"service\": \"downloads\", \n" +
            "\t\"web_url\": \"http://testkenai.com/projects/java-inline/features/downloads\", \n" +
            "\t\"created_at\": \"2008-10-14T04:31:41Z\", \n" +
            "\t\"updated_at\": \"2008-10-14T04:31:41Z\"}\n" +
            "\t]\n" +
            "}\n" +
            "";

    private static final String project_list_json = "" +
            "{\n" +
            "\"prev\":null,\n" +
            "\"next\":\"http:\\/\\/testkenai.com\\/api\\/projects?page=2\",\n" +
            "\"projects\":[\n" +
            "\t{\n" +
            "\t\"href\":\"http:\\/\\/testkenai.com\\/api\\/projects\\/activerecord-jdbc\",\n" +
            "\t\"name\":\"activerecord-jdbc\",\n" +
            "\t\"display_name\":\"ActiveRecord JDBC\",\n" +
            "\t\"image\":\"http:\\/\\/testkenai.com\\/images\\/defaultProjectImage.jpg\"},\n" +
            "\t{\"href\":\"http:\\/\\/testkenai.com\\/api\\/projects\\/alice\",\"name\":\"alice\",\"display_name\":\"Alice\",\"image\":\"http:\\/\\/testkenai.com\\/images\\/defaultProjectImage.jpg\"},{\"href\":\"http:\\/\\/testkenai.com\\/api\\/projects\\/nbandroid\",\"name\":\"nbandroid\",\"display_name\":\"Android plugin for NetBeans\",\"image\":\"http:\\/\\/testkenai.com\\/attachments\\/images\\/project\\/nbandroid.png\"},{\"href\":\"http:\\/\\/testkenai.com\\/api\\/projects\\/bluebird\",\"name\":\"bluebird\",\"display_name\":\"Bluebird\",\"image\":\"http:\\/\\/testkenai.com\\/attachments\\/images\\/project\\/bluebird.jpg\"},{\"href\":\"http:\\/\\/testkenai.com\\/api\\/projects\\/bz-5052\",\"name\":\"bz-5052\",\"display_name\":\"bz 5052\",\"image\":\"http:\\/\\/testkenai.com\\/images\\/defaultProjectImage.jpg\"},{\"href\":\"http:\\/\\/testkenai.com\\/api\\/projects\\/ccadministrator\",\"name\":\"ccadministrator\",\"display_name\":\"CCAdministrator\",\"image\":\"http:\\/\\/testkenai.com\\/images\\/defaultProjectImage.jpg\"},{\"href\":\"http:\\/\\/testkenai.com\\/api\\/projects\\/chromeupdater\",\"name\":\"chromeupdater\",\"display_name\":\"Chrome Updater\",\"image\":\"http:\\/\\/testkenai.com\\/images\\/defaultProjectImage.jpg\"},{\"href\":\"http:\\/\\/testkenai.com\\/api\\/projects\\/comic-tracker\",\"name\":\"comic-tracker\",\"display_name\":\"Comic Release tracker\",\"image\":\"http:\\/\\/testkenai.com\\/images\\/defaultProjectImage.jpg\"},{\"href\":\"http:\\/\\/testkenai.com\\/api\\/projects\\/clv\",\"name\":\"clv\",\"display_name\":\"compilation log visualizer\",\"image\":\"http:\\/\\/testkenai.com\\/images\\/defaultProjectImage.jpg\"},\n" +
            "\n" +
            "\t{\n" +
            "\t\t\"href\":\"http:\\/\\/testkenai.com\\/api\\/projects\\/csearch\",\n" +
            "\t\t\"name\":\"csearch\",\n" +
            "\t\t\"display_name\":\"Conceptual Search\",\n" +
            "\t\t\"image\":\"http:\\/\\/testkenai.com\\/images\\/defaultProjectImage.jpg\"\n" +
            "\t}\n" +
            "]\n" +
            "}\n" +
            "";
}