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

package org.netbeans.modules.bugzilla.issue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.modules.bugzilla.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue.Attachment;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue.Comment;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue.IssueField;

/**
 *
 * @author tomas
 */
public class IssueTest extends NbTestCase implements TestConstants {

    private static String REPO_NAME = "Beautiful";

    public IssueTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        BugzillaCorePlugin bcp = new BugzillaCorePlugin();
        try {
            bcp.start(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    public void testStatusOpenIssue() throws MalformedURLException, CoreException, InterruptedException, IOException, Throwable {
//        long ts = System.currentTimeMillis();
//        String summary = "somary" + ts;
//        String id = TestUtil.createIssue(getRepository(), summary);
//        BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
//        assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));
//
//        setSeen(issue);
//
//        String keyword = getKeyword(issue);
//        issue.setFieldValue(IssueField.KEYWORDS, keyword);
//        submit(issue);
//        assertStatus(BugzillaIssue.FIELD_STATUS_NEW, issue, IssueField.KEYWORDS);
//
//        issue.open();
//
//        for (int i = 0; i < 100; i++) {
//            if(BugzillaIssue.FIELD_STATUS_NEW != issue.getFieldStatus(IssueField.KEYWORDS)) {
//                break;
//            }
//            Thread.sleep(500);
//        }
//
//        keyword = getKeyword(issue);
//        issue.setFieldValue(IssueField.KEYWORDS, keyword);
//        submit(issue);
//        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.KEYWORDS);
//    }

    public void testFields() throws Throwable {
        // WARNING: the test assumes that there are more than one value
        // for atributes like platform, versions etc.

        long ts = System.currentTimeMillis();
        String summary = "somary" + ts;
        String id = TestUtil.createIssue(getRepository(), summary);
        BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
        assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));

        for (IssueField f : IssueField.values()) {
            // haven't seen anything yet, everything's new
            assertEquals(BugzillaIssue.FIELD_STATUS_IRELEVANT, issue.getFieldStatus(f));
        }
        setSeen(issue); // reset status


        String keyword = getKeyword(issue);
        String milestone = getMilestone(issue);
        String platform = getPlatform(issue);
        String priority = getPriority(issue);
        String resolution = getResolution(issue);
        String version = getVersion(issue);
        String assignee = "dil@dil.com";
        String reporter = "dil@dil.com";
        String qaContact = "dil@dil.com";
        String assigneeName = "dilino";
        String qaContactName = "dilino";
        String blocks = "1";
        String depends = "2";
        String newcc = "dil@dil.com";
        String cc = "dil@dil.com";
        String url = "http://new.ulr";
        String component = getComponent(issue);
        String severity = getSeverity(issue);

//        issue.setFieldValue(IssueField.ASSIGNED_TO, assignee);
//        issue.setFieldValue(IssueField.ASSIGNED_TO_NAME, assigneeName);
        issue.setFieldValue(IssueField.BLOCKS, blocks);
        issue.setFieldValue(IssueField.COMPONENT, component);
        issue.setFieldValue(IssueField.DEPENDS_ON, depends);
        issue.setFieldValue(IssueField.KEYWORDS, keyword);
        issue.setFieldValue(IssueField.MILESTONE,milestone);
        issue.setFieldValue(IssueField.PLATFORM,platform);
        issue.setFieldValue(IssueField.PRIORITY, priority);
        issue.setFieldValue(IssueField.QA_CONTACT, qaContact);
        issue.setFieldValue(IssueField.QA_CONTACT_NAME, qaContactName);
        issue.setFieldValue(IssueField.SEVERITY, getSeverity(issue));
        issue.setFieldValue(IssueField.SUMMARY, summary + ".new");
        issue.setFieldValue(IssueField.URL, url);
        issue.setFieldValue(IssueField.VERSION, version);
        issue.setFieldValue(IssueField.NEWCC, newcc);

        // can't be changed
//      DESCRIPTION
//      PRODUCT
        // won't test those too
//      CREATION
//      MODIFICATION
        // handled in separate tests
//      STATUS
//      RESOLUTION
//      REMOVECC
//      CC
//      ASSIGNED_TO
//      ASSIGNED_TO_NAME

        submit(issue);

        // assert values
        assertEquals(reporter, issue.getFieldValue(IssueField.REPORTER));
        assertEquals("NEW", issue.getFieldValue(IssueField.STATUS));
        assertEquals(assignee, issue.getFieldValue(IssueField.ASSIGNED_TO));
        assertEquals(assigneeName, issue.getFieldValue(IssueField.ASSIGNED_TO_NAME));
        assertEquals(blocks, issue.getFieldValue(IssueField.BLOCKS));
        assertEquals(component, issue.getFieldValue(IssueField.COMPONENT));
        assertEquals(depends, issue.getFieldValue(IssueField.DEPENDS_ON));
        assertEquals(keyword, issue.getFieldValue(IssueField.KEYWORDS));
        assertEquals(milestone, issue.getFieldValue(IssueField.MILESTONE));
        assertEquals(platform, issue.getFieldValue(IssueField.PLATFORM));
        assertEquals(priority, issue.getFieldValue(IssueField.PRIORITY));
        assertEquals(qaContact, issue.getFieldValue(IssueField.QA_CONTACT));
        assertEquals(qaContactName, issue.getFieldValue(IssueField.QA_CONTACT_NAME));
        assertEquals(severity, issue.getFieldValue(IssueField.SEVERITY));
        assertEquals(summary + ".new", issue.getFieldValue(IssueField.SUMMARY));
        assertEquals(url, issue.getFieldValue(IssueField.URL));
        assertEquals(version, issue.getFieldValue(IssueField.VERSION));
        assertEquals(cc, issue.getFieldValue(IssueField.CC));

        assertEquals(ISSUE_DESCRIPTION, issue.getFieldValue(IssueField.DESCRIPTION));
        assertEquals(TEST_PROJECT, issue.getFieldValue(IssueField.PRODUCT));

        // assert status
        assertStatus(BugzillaIssue.FIELD_STATUS_UPTODATE, issue, IssueField.REPORTER);
        assertStatus(BugzillaIssue.FIELD_STATUS_UPTODATE, issue, IssueField.STATUS);
        assertStatus(BugzillaIssue.FIELD_STATUS_UPTODATE, issue, IssueField.ASSIGNED_TO);
        assertStatus(BugzillaIssue.FIELD_STATUS_UPTODATE, issue, IssueField.ASSIGNED_TO_NAME);
        assertStatus(BugzillaIssue.FIELD_STATUS_NEW, issue, IssueField.BLOCKS);
        assertStatus(BugzillaIssue.FIELD_STATUS_UPTODATE, issue, IssueField.COMPONENT);
        assertStatus(BugzillaIssue.FIELD_STATUS_NEW, issue, IssueField.DEPENDS_ON);
        assertStatus(BugzillaIssue.FIELD_STATUS_NEW, issue, IssueField.KEYWORDS);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.MILESTONE);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.PLATFORM);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.PRIORITY);
        assertStatus(BugzillaIssue.FIELD_STATUS_NEW, issue, IssueField.QA_CONTACT);
        assertStatus(BugzillaIssue.FIELD_STATUS_NEW, issue, IssueField.QA_CONTACT_NAME);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.SEVERITY);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.SUMMARY);
        assertStatus(BugzillaIssue.FIELD_STATUS_NEW, issue, IssueField.URL);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.VERSION);
        assertStatus(BugzillaIssue.FIELD_STATUS_NEW, issue, IssueField.CC);

        assertStatus(BugzillaIssue.FIELD_STATUS_UPTODATE, issue, IssueField.DESCRIPTION);
        assertStatus(BugzillaIssue.FIELD_STATUS_UPTODATE, issue, IssueField.PRODUCT);

        setSeen(issue); // reset status

        keyword = keyword + "," + getKeyword(issue);
        milestone = getMilestone(issue);
        platform = getPlatform(issue);
        priority = getPriority(issue);
        resolution = getResolution(issue);
        version = getVersion(issue);
        qaContact = "dil2@dil.com";
        qaContactName = "dilino2";
        blocks = "1,3";
        depends = "2,4";
        newcc = "dil2@dil.com";
        url = "http://evennewer.ulr";
        component = getComponent(issue);
        severity = getSeverity(issue);

        issue.setFieldValue(IssueField.BLOCKS, blocks);
        issue.setFieldValue(IssueField.COMPONENT, component);
        issue.setFieldValue(IssueField.DEPENDS_ON, depends);
        issue.setFieldValue(IssueField.KEYWORDS, keyword);
        issue.setFieldValue(IssueField.MILESTONE, milestone);
        issue.setFieldValue(IssueField.PLATFORM, platform);
        issue.setFieldValue(IssueField.PRIORITY, priority);
        issue.setFieldValue(IssueField.QA_CONTACT, qaContact);
        issue.setFieldValue(IssueField.QA_CONTACT_NAME, qaContactName);
        issue.setFieldValue(IssueField.SEVERITY, getSeverity(issue));
        issue.setFieldValue(IssueField.SUMMARY, summary + ".new");
        issue.setFieldValue(IssueField.URL, url);
        issue.setFieldValue(IssueField.VERSION, version);
        issue.setFieldValue(IssueField.NEWCC, newcc);

        submit(issue);

        assertStatus(BugzillaIssue.FIELD_STATUS_UPTODATE, issue, IssueField.REPORTER);
        assertStatus(BugzillaIssue.FIELD_STATUS_UPTODATE, issue, IssueField.STATUS);
        assertStatus(BugzillaIssue.FIELD_STATUS_UPTODATE, issue, IssueField.ASSIGNED_TO);
        assertStatus(BugzillaIssue.FIELD_STATUS_UPTODATE, issue, IssueField.ASSIGNED_TO_NAME);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.BLOCKS);
        assertStatus(BugzillaIssue.FIELD_STATUS_UPTODATE, issue, IssueField.COMPONENT);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.DEPENDS_ON);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.KEYWORDS);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.MILESTONE);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.PLATFORM);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.PRIORITY);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.QA_CONTACT);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.QA_CONTACT_NAME);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.SEVERITY);
        assertStatus(BugzillaIssue.FIELD_STATUS_UPTODATE, issue, IssueField.SUMMARY);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.URL);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.VERSION);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.CC);

        assertStatus(BugzillaIssue.FIELD_STATUS_UPTODATE, issue, IssueField.DESCRIPTION);
        assertStatus(BugzillaIssue.FIELD_STATUS_UPTODATE, issue, IssueField.PRODUCT);

        setSeen(issue);

        qaContact = "";
        qaContactName = "";
        blocks = "";
        depends = "";
        url = "";
        keyword = "";

        issue.setFieldValue(IssueField.BLOCKS, blocks);
        issue.setFieldValue(IssueField.DEPENDS_ON, depends);
        issue.setFieldValue(IssueField.KEYWORDS, keyword);
        issue.setFieldValue(IssueField.QA_CONTACT, qaContact);
        issue.setFieldValue(IssueField.QA_CONTACT_NAME, qaContactName);
        issue.setFieldValue(IssueField.URL, url);

        submit(issue);

        assertEquals(blocks, issue.getFieldValue(IssueField.BLOCKS));
        assertEquals(depends, issue.getFieldValue(IssueField.DEPENDS_ON));
        assertEquals(keyword, issue.getFieldValue(IssueField.KEYWORDS));
// XXX WTF        assertEquals(qaContact, issue.getFieldValue(IssueField.QA_CONTACT));
// XXX WTF       assertEquals(qaContactName, issue.getFieldValue(IssueField.QA_CONTACT_NAME));
        assertEquals(url, issue.getFieldValue(IssueField.URL));

        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.BLOCKS);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.DEPENDS_ON);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.KEYWORDS);
// XXX WTF       assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.QA_CONTACT);
// XXX WTF       assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.QA_CONTACT_NAME);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.URL);

//        XXX changing a product might also imply the change of other fields!!!
//
//        String product = getProduct(issue);
//        issue.setFieldValue(IssueField.PRODUCT, product);
//        try {
//            issue.submit();
//        } catch (CoreException ex) {
//            TestUtil.handleException(ex);
//        }
//
//        issue.refresh();
//        assertEquals(product, issue.getFieldValue(IssueField.PRODUCT));

    }

    public void testCC() throws Throwable {
        // WARNING: the test assumes that there are more than one value
        // for atributes like platform, versions etc.

        long ts = System.currentTimeMillis();
        String summary = "somary" + ts;
        String id = TestUtil.createIssue(getRepository(), summary);
        BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
        assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));

        setSeen(issue); // reset status

        // add a cc
        issue.setFieldValue(IssueField.NEWCC, "dil@dil.com");
        submit(issue);
        assertEquals("dil@dil.com", issue.getFieldValue(IssueField.CC));
        assertStatus(BugzillaIssue.FIELD_STATUS_NEW, issue, IssueField.CC);
        setSeen(issue); // reset status

        // add new cc
        issue.setFieldValue(IssueField.NEWCC, "dil2@dil.com");
        submit(issue);
        List<String> ccs = issue.getFieldValues(IssueField.CC);
        assertEquals(2, ccs.size());
        assertTrue(ccs.contains("dil@dil.com"));
        assertTrue(ccs.contains("dil2@dil.com"));
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.CC);

        // add two cc-s at once
        issue.setFieldValue(IssueField.NEWCC, "dil3@dil.com, dil4@dil.com");
        submit(issue);
        ccs = issue.getFieldValues(IssueField.CC);
        assertEquals(4, ccs.size());
        assertTrue(ccs.contains("dil@dil.com"));
        assertTrue(ccs.contains("dil2@dil.com"));
        assertTrue(ccs.contains("dil3@dil.com"));
        assertTrue(ccs.contains("dil4@dil.com"));
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.CC);

        setSeen(issue); // reset status

        // remove a cc
        ccs = new ArrayList<String>();
        ccs.add("dil4@dil.com");
        ccs.add("dil@dil.com");
        issue.setFieldValues(IssueField.REMOVECC, ccs);
        submit(issue);
        ccs = issue.getFieldValues(IssueField.CC);
        assertEquals(2, ccs.size());
        assertTrue(ccs.contains("dil2@dil.com"));
        assertTrue(ccs.contains("dil3@dil.com"));
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.CC);

        setSeen(issue); // reset status

        // remove all
        ccs = new ArrayList<String>();
        ccs.add("dil3@dil.com");
        ccs.add("dil2@dil.com");
        issue.setFieldValues(IssueField.REMOVECC, ccs);
        submit(issue);
        ccs = issue.getFieldValues(IssueField.CC);
        assertEquals(0, ccs.size());
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.CC);

    }

    public void testStartResolveFixedVerifiedClosedReopen() throws Throwable {
        long ts = System.currentTimeMillis();
        String summary = "somary" + ts;
        String id = TestUtil.createIssue(getRepository(), summary);
        BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
        assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));
        assertEquals("NEW", issue.getFieldValue(IssueField.STATUS));
        assertEquals("", issue.getFieldValue(IssueField.RESOLUTION));

        issue.setFieldValue(IssueField.ASSIGNED_TO_NAME, getRepository().getUsername());
        issue.accept();
        submit(issue);
        assertEquals("ASSIGNED", issue.getFieldValue(IssueField.STATUS));
        assertEquals("", issue.getFieldValue(IssueField.RESOLUTION));

        issue.resolve("FIXED");
        submit(issue);
        assertEquals("RESOLVED", issue.getFieldValue(IssueField.STATUS));
        assertEquals("FIXED", issue.getFieldValue(IssueField.RESOLUTION));

        issue.verify();
        submit(issue);
        assertEquals("VERIFIED", issue.getFieldValue(IssueField.STATUS));
        assertEquals("FIXED", issue.getFieldValue(IssueField.RESOLUTION));

        issue.close();
        submit(issue);
        assertEquals("CLOSED", issue.getFieldValue(IssueField.STATUS));
        assertEquals("FIXED", issue.getFieldValue(IssueField.RESOLUTION));

        // we do not support this yet
//        issue.duplicate("1");
//        submit(issue);
//        assertEquals("RESOLVED", issue.getFieldValue(IssueField.STATUS));
//        assertEquals("DUPLICATE", issue.getFieldValue(IssueField.RESOLUTION));

        issue.reopen();
        submit(issue);
        assertEquals("REOPENED", issue.getFieldValue(IssueField.STATUS));
        assertEquals("", issue.getFieldValue(IssueField.RESOLUTION));
    }

    public void testResolveNew() throws Throwable {
        long ts = System.currentTimeMillis();
        String summary = "somary" + ts;

        List<String> rs = Bugzilla.getInstance().getResolutions(getRepository());
        for (String r : rs) {
            if(r.equals("DUPLICATE") ||
               r.equals("MOVED"))
            {
                continue;  // used and tested in a different way
            }
            String id = TestUtil.createIssue(getRepository(), summary);
            BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
            assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));
            assertEquals("NEW", issue.getFieldValue(IssueField.STATUS));
            assertEquals("", issue.getFieldValue(IssueField.RESOLUTION));

            issue.resolve(r);
            submit(issue);
            assertEquals("RESOLVED", issue.getFieldValue(IssueField.STATUS));
            assertEquals(r, issue.getFieldValue(IssueField.RESOLUTION));
        }
    }

    //    we do not support this yet
//    public void testResolveClosed() throws Throwable {
//        long ts = System.currentTimeMillis();
//        String summary = "somary" + ts;
//
//        String id = TestUtil.createIssue(getRepository(), summary);
//        BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
//        assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));
//        assertEquals("NEW", issue.getFieldValue(IssueField.STATUS));
//        assertEquals("", issue.getFieldValue(IssueField.RESOLUTION));
//
//        // fix
//        issue.resolve("FIXED");
//        submit(issue);
//        assertEquals("RESOLVED", issue.getFieldValue(IssueField.STATUS));
//        assertEquals("FIXED", issue.getFieldValue(IssueField.RESOLUTION));
//
//        // try to change to another resolution
//        List<String> rs = Bugzilla.getInstance().getResolutions(getRepository());
//        for (String r : rs) {
//            if(r.equals("DUPLICATE") ||
//               r.equals("MOVED") ||
//               r.equals("FIXED")) // already fixed
//            {
//                continue;  // used and tested in a different way
//            }
//
//            issue.resolve(r);
//            submit(issue);
//            assertEquals("RESOLVED", issue.getFieldValue(IssueField.STATUS));
//            assertEquals(r, issue.getFieldValue(IssueField.RESOLUTION));
//        }
//    }


    public void testResolveDuplicateReopen() throws Throwable {
        long ts = System.currentTimeMillis();
        String summary = "somary" + ts;
        String id = TestUtil.createIssue(getRepository(), summary);
        BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
        assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));
        assertEquals("NEW", issue.getFieldValue(IssueField.STATUS));

        issue.duplicate("1");
        submit(issue);

        assertEquals("RESOLVED", issue.getFieldValue(IssueField.STATUS));
        assertEquals("DUPLICATE", issue.getFieldValue(IssueField.RESOLUTION));

        issue.reopen();
        submit(issue);
        assertEquals("REOPENED", issue.getFieldValue(IssueField.STATUS));
        assertEquals("", issue.getFieldValue(IssueField.RESOLUTION));

        // XXX get dupl ID

    }

    public void testReassign() throws Throwable {
        long ts = System.currentTimeMillis();
        String summary = "somary" + ts;
        String id = TestUtil.createIssue(getRepository(), summary);
        BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
        assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));
        assertEquals("NEW", issue.getFieldValue(IssueField.STATUS));
        assertEquals("dil@dil.com", issue.getFieldValue(IssueField.ASSIGNED_TO));

        issue.reassigne("dil2@dil.com");
        submit(issue);

        assertEquals("dil2@dil.com", issue.getFieldValue(IssueField.ASSIGNED_TO));
    }

    public void testComment() throws Throwable {
        long ts = System.currentTimeMillis();
        String summary = "somary" + ts;
        String id = TestUtil.createIssue(getRepository(), summary);
        BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
        assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));
        assertEquals("NEW", issue.getFieldValue(IssueField.STATUS));
        assertEquals("dil@dil.com", issue.getFieldValue(IssueField.ASSIGNED_TO));

        setSeen(issue);

        // add comment
        String comment = "koment";
        issue.addComment(comment);
        submit(issue);
        assertStatus(BugzillaIssue.FIELD_STATUS_NEW, issue, IssueField.COMMENT_COUNT);

        // get comment
        Comment[] comments = issue.getComments();
        assertEquals(1, comments.length);
        assertEquals(comment, issue.getComments()[0].getText());

        setSeen(issue);

        // one more comment
        comment = "1 more koment";
        issue.addComment(comment);
        submit(issue);
        assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.COMMENT_COUNT);
    }

    public void testAddCommentClose() throws Throwable {
        long ts = System.currentTimeMillis();
        String summary = "somary" + ts;
        String id = TestUtil.createIssue(getRepository(), summary);
        BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
        assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));
        assertEquals("NEW", issue.getFieldValue(IssueField.STATUS));
        assertEquals("dil@dil.com", issue.getFieldValue(IssueField.ASSIGNED_TO));

        String comment = "koment";
        issue.addComment(comment, true);
        issue.refresh();

        Comment[] comments = issue.getComments();
        assertEquals(1, comments.length);
        assertEquals(comment, issue.getComments()[0].getText());

        assertEquals("RESOLVED", issue.getFieldValue(IssueField.STATUS));
        assertEquals("FIXED", issue.getFieldValue(IssueField.RESOLUTION));
    }


    public void testAttachment() throws Throwable {
        try {
            long ts = System.currentTimeMillis();
            String summary = "somary" + ts;
            String id = TestUtil.createIssue(getRepository(), summary);
            BugzillaIssue issue = (BugzillaIssue) getRepository().getIssue(id);
            assertEquals(summary, issue.getFieldValue(IssueField.SUMMARY));
            assertEquals("NEW", issue.getFieldValue(IssueField.STATUS));
            assertEquals("dil@dil.com", issue.getFieldValue(IssueField.ASSIGNED_TO));

            setSeen(issue);

            // add attachment
            String atttext = "my first attachement";
            String attcomment = "my first attachement";
            String attdesc = "file containing text";
            File f = getAttachmentFile(atttext);
            issue.addAttachment(f, attcomment, attdesc, "text/plain");
            issue.refresh();

            // get attachment
            Attachment[] atts = issue.getAttachments();
            assertEquals(1, atts.length);
            assertEquals(attdesc, atts[0].getDesc());
            assertStatus(BugzillaIssue.FIELD_STATUS_NEW, issue, IssueField.ATTACHEMENT_COUNT);

            // get attachment data
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            atts[0].getAttachementData(os);
            String fileConttents = os.toString();
            assertEquals(atttext, fileConttents);

            setSeen(issue);

            // one more  attachment
            atttext = "my second attachement";
            attcomment = "my second attachement";
            attdesc = "file containing text";
            f = getAttachmentFile(atttext);
            issue.addAttachment(f, attcomment, attdesc, "text/plain");
            issue.refresh();

            // get attachment
            atts = issue.getAttachments();
            assertEquals(2, atts.length);
            assertEquals(attdesc, atts[0].getDesc());
            assertStatus(BugzillaIssue.FIELD_STATUS_MODIFIED, issue, IssueField.ATTACHEMENT_COUNT);

        } catch (Exception e) {
            TestUtil.handleException(e);
        }
    }

    public void testRemoveQA() {
        fail("no idea how to do this !!!");
    }

    private void addHandler(LogHandler lh) {
        Logger l = Logger.getLogger("org.netbeans.modules.bugracking.BugtrackingManager");
        l.addHandler(lh);
    }

    private void assertStatus(int expectedStatus, BugzillaIssue issue, IssueField f) {
        int status = issue.getFieldStatus(f);
        if(status != expectedStatus) {
            fail("expected [" + getName(expectedStatus) + "], " +
                 "was [" + getName(status)+ "] " +
                 "because of value [" + issue.getFieldValue(f) + "] " +
                 "vs [" + getSeenValue(issue, f) + "]");
        }
    }

    private String getName(int s) {
        switch(s) {
            case BugzillaIssue.FIELD_STATUS_IRELEVANT:
                return "Irelevant";
            case BugzillaIssue.FIELD_STATUS_NEW :
                return "New";
            case BugzillaIssue.FIELD_STATUS_MODIFIED :
                return "Modified";
            case BugzillaIssue.FIELD_STATUS_UPTODATE :
                return "Uptodate";
            default :
                throw new IllegalStateException("Wrong status " + s);
        }
    }

    private String getSeenValue(BugzillaIssue issue, IssueField f) {
        Map<String, String> m = getRepository().getIssueCache().getSeenAttributes(issue.getID());
        if(m == null) {
            return "";
        }
        String ret = m.get(f.getKey());
        return ret != null ? ret : "";
    }

    private BugzillaRepository getRepository() {
        return TestUtil.getRepository(REPO_NAME, REPO_URL, REPO_USER, REPO_PASSWD);
    }

    private String getKeyword(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = Bugzilla.getInstance().getKeywords(getRepository());
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.KEYWORDS));
    }

    private String getMilestone(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = Bugzilla.getInstance().getTargetMilestones(getRepository(), TEST_PROJECT);
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.MILESTONE));
    }

    private String getPlatform(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = Bugzilla.getInstance().getPlatforms(getRepository());
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.PLATFORM));
    }

    private String getProduct(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = Bugzilla.getInstance().getProducts(getRepository());
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.PRODUCT));
    }

    private String getPriority(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = Bugzilla.getInstance().getPriorities(getRepository());
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.PRIORITY));
    }

    private String getVersion(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = Bugzilla.getInstance().getVersions(getRepository(), TEST_PROJECT);
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.VERSION));
    }

    private String getSeverity(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = Bugzilla.getInstance().getSeverities(getRepository());
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.SEVERITY));
    }

    private String getResolution(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = Bugzilla.getInstance().getResolutions(getRepository());
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.RESOLUTION));
    }

    private String getComponent(BugzillaIssue issue) throws IOException, CoreException {
        List<String> l = Bugzilla.getInstance().getComponents(getRepository(), TEST_PROJECT);
        return getDifferentServerValue(l, issue.getFieldValue(IssueField.RESOLUTION));
    }

    private String getDifferentServerValue(List<String> l, String v) {
        if(v != null) {
            for (String s : l) {
                if(!s.equals(v)) {
                    return s;
                }
            }
            fail("there is no different value then [" + v + "] on the server.");
        }
        return l.get(0);
    }

    private void setSeen(BugzillaIssue issue) throws SecurityException, InterruptedException, IOException {
        LogHandler lh = new LogHandler("finished storing issue");
        addHandler(lh);
        issue.setSeen(true);
        while (!lh.done) {
            Thread.sleep(100);
        }
        for (IssueField f : IssueField.values()) {
            // seen -> everything's uptodate
            assertEquals(BugzillaIssue.FIELD_STATUS_UPTODATE, issue.getFieldStatus(f));
        }
    }

    private void submit(BugzillaIssue issue) throws Throwable {
        try {
            issue.submit();
        } catch (CoreException ex) {
            TestUtil.handleException(ex);
        }
        issue.refresh();
    }

    private File getAttachmentFile(String content) throws Exception {
        FileWriter fw = null;
        File f = null;
        try {
            f = File.createTempFile("bugzillatest", null);
            f.deleteOnExit();
            try {
                f.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                // ignore
            }
            fw = new FileWriter(f);
            fw.write(content);
            fw.flush();
            return f;
        } finally {
            try { if (fw != null) fw.close(); } catch (IOException iOException) { }
        }
    }

    private class LogHandler extends Handler {
        private final String msg;
        private boolean done = false;
        public LogHandler(String msg) {
            this.msg = msg;
        }

        @Override
        public void publish(LogRecord record) {
            if(!done) done = record.getMessage().startsWith(msg);
        }
        @Override
        public void flush() { }
        @Override
        public void close() throws SecurityException { }
    }
}
