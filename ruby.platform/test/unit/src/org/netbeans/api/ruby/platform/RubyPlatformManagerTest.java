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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.api.ruby.platform;

import java.io.File;
import java.io.FileOutputStream;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.modules.ruby.spi.project.support.rake.EditableProperties;
import org.openide.filesystems.FileUtil;

public final class RubyPlatformManagerTest extends RubyTestBase {

    public RubyPlatformManagerTest(final String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        RubyPlatformManager.resetPlatforms();
    }

    public void testAddPlatform() throws Exception {
        assertEquals("bundle JRuby", 1, RubyPlatformManager.getPlatforms().size());
        RubyPlatform ruby = RubyPlatformManager.addPlatform(setUpRuby());
        File defaultRubyHome = getTestRubyHome();
        assertEquals("right ruby home", defaultRubyHome, ruby.getHome());
        assertEquals("right ruby lib", new File(defaultRubyHome, "lib/ruby/1.8").getAbsolutePath(), ruby.getLibDir());
        assertEquals("two platforms", 2, RubyPlatformManager.getPlatforms().size());
        RubyPlatformManager.removePlatform(ruby);
        assertEquals("platform removed", 1, RubyPlatformManager.getPlatforms().size());
    }
    
    public void testGetPlatformByPath() throws Exception {
        RubyPlatform ruby = RubyPlatformManager.addPlatform(setUpRuby());
        RubyPlatform alsoRuby = RubyPlatformManager.getPlatformByPath(ruby.getInterpreter());
        assertSame("found by path", ruby, alsoRuby);
        RubyPlatform jruby = RubyPlatformManager.getPlatformByPath(TestUtil.getXTestJRubyPath());
        assertSame("found by path", RubyPlatformManager.getDefaultPlatform(), jruby);
    }
    
    public void test60PlatformInBuildProperties() throws Exception {
        EditableProperties ep = new EditableProperties();
        ep.setProperty("rubyplatform.ruby_(1_8_6).interpreter", "/a/path/to/ruby");
        ep.setProperty("rubyplatform.ruby_(1_8_6).label", "ruby (1.8.6)");
        File bp = new File(getWorkDir(), "build.properties");
        FileOutputStream fos = new FileOutputStream(bp);
        ep.store(fos);
        fos.close();
        RubyPlatformManager.getDefaultPlatform();
    }
    
    public void testPlatformDetection() throws Exception {
        // sanity-check test
        RubyPlatformManager.performPlatformDetection();
    }

    public void testAddInvalidPlatform() throws Exception { // #125296
        RubyPlatformManager.TEST_RUBY_PROPS = null;
        assertEquals("bundle JRuby", 1, RubyPlatformManager.getPlatforms().size());
        FileUtil.toFileObject(getWorkDir()).createData("invalid-ruby");
        RubyPlatform plaf = RubyPlatformManager.addPlatform(new File(getWorkDir(), "invalid-ruby"));
        assertNull("invalid platform", plaf);
    }

    public void testRepositoriesAreStored() throws Exception {
        RubyPlatform platform = RubyPlatformManager.getDefaultPlatform();
        GemManager gemManager = platform.getGemManager();
        String dummyRepo = getWorkDirPath() + "/a";
        assertEquals("one repositories", 1, gemManager.getRepositories().size());
        
        // add and check
        gemManager.addRepository(dummyRepo);
        assertEquals("two repositories", 2, gemManager.getRepositories().size());
        RubyPlatformManager.resetPlatforms();
        platform = RubyPlatformManager.getDefaultPlatform();
        gemManager = platform.getGemManager();
        assertEquals("two repositories", 2, gemManager.getRepositories().size());

        // remove and check
        gemManager.removeRepository(dummyRepo);
        RubyPlatformManager.resetPlatforms();
        platform = RubyPlatformManager.getDefaultPlatform();
        gemManager = platform.getGemManager();
        assertEquals("two repositories", 1, RubyPlatformManager.getDefaultPlatform().getGemManager().getRepositories().size());
        gemManager.removeRepository(dummyRepo);
    }

}
