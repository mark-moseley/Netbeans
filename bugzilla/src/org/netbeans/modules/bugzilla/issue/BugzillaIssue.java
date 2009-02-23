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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.httpclient.HttpException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaAttribute;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaTaskAttachmentHandler;
import org.eclipse.mylyn.internal.tasks.core.data.FileTaskAttachmentSource;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugtracking.spi.IssueNode;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query.ColumnDescriptor;
import org.netbeans.modules.bugzilla.BugzillaRepository;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaIssue extends Issue {
    private TaskData data;
    private BugzillaRepository repository;
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
    private static final SimpleDateFormat CC_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT);
    private IssueController controller;
    private IssueNode node;

    static String LABEL_NAME_ID         = "bugzilla.issue.id";
    static String LABEL_NAME_SEVERITY   = "bugzilla.issue.severity";
    static String LABEL_NAME_PRIORITY   = "bugzilla.issue.priority";
    static String LABEL_NAME_STATUS     = "bugzilla.issue.status";
    static String LABEL_NAME_RESOLUTION = "bugzilla.issue.resolution";
    static String LABEL_NAME_SUMMARY    = "bugzilla.issue.summary";

    private Map<String, String> attributes;

    /**
     * Defines columns for a view table.
     */
    public static ColumnDescriptor[] DESCRIPTORS;

    public BugzillaIssue(TaskData data, BugzillaRepository repo) {
        this.data = data;
        this.repository = repo;
    }

    public static ColumnDescriptor[] getColumnDescriptors() {
        if(DESCRIPTORS == null) {
            ResourceBundle loc = NbBundle.getBundle(BugzillaIssue.class);
            DESCRIPTORS = new ColumnDescriptor[] {
                new ColumnDescriptor(LABEL_NAME_ID, String.class,
                                                  loc.getString("CTL_Issue_ID_Title"),
                                                  loc.getString("CTL_Issue_ID_Desc")),
                new ColumnDescriptor(LABEL_NAME_SEVERITY, String.class,
                                                  loc.getString("CTL_Issue_Severity_Title"),
                                                  loc.getString("CTL_Issue_Severity_Desc")),
                new ColumnDescriptor(LABEL_NAME_PRIORITY, String.class,
                                                  loc.getString("CTL_Issue_Priority_Title"),
                                                  loc.getString("CTL_Issue_Priority_Desc")),
                new ColumnDescriptor(LABEL_NAME_STATUS, String.class,
                                                  loc.getString("CTL_Issue_Status_Title"),
                                                  loc.getString("CTL_Issue_Status_Desc")),
                new ColumnDescriptor(LABEL_NAME_RESOLUTION, String.class,
                                                  loc.getString("CTL_Issue_Resolution_Title"),
                                                  loc.getString("CTL_Issue_Resolution_Desc")),
                new ColumnDescriptor(LABEL_NAME_SUMMARY, String.class,
                                                  loc.getString("CTL_Issue_Summary_Title"),
                                                  loc.getString("CTL_Issue_Summary_Desc"))
            };
        }
        return DESCRIPTORS;
    }

    @Override
    public BugtrackingController getControler() {
        if (controller == null) {
            controller = new IssueController(this);
        }
        return controller;
    }

    @Override
    public void setSeen(boolean seen) {
        setSeen(seen, true);
    }

    @Override
    public String toString() {
        String str = getID() + " : "  + getSeverity() + " : " + getStatus() + " : " + getPriority() + " : " + getSummary();
        return str;
    }

    @Override
    public IssueNode getNode() {
        if(node == null) {
            node = createNode();
        }
        return node;
    }

    @Override
    public Map<String, String> getAttributes() {
        if(attributes == null) {
            attributes = new HashMap<String, String>();
            attributes.put("id", getID());
            attributes.put("summary", getSummary());
            attributes.put("status", getStatus());
            attributes.put("priority", getPriority());
        }
        attributes.put("seen", wasSeen() ? "1" : "0"); // XXX
        return attributes;
    }

    public void setSeen(boolean seen, boolean cacheRefresh) {
        super.setSeen(seen);
        if(cacheRefresh) {
            repository.getCache().setSeen(getID(), seen, getAttributes());
        }
    }

    public void save() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTaskData(TaskData taskData) {
        data = taskData;
        attributes = null; // reset
        ((BugzillaIssueNode)getNode()).fireDataChanged();
    }

    public void update() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void addAttachment(File f, String comment, String desc) {
        FileTaskAttachmentSource attachmentSource = new FileTaskAttachmentSource(f);
        attachmentSource.setContentType("text/plain");
        BugzillaTaskAttachmentHandler.AttachmentPartSource source = new BugzillaTaskAttachmentHandler.AttachmentPartSource(attachmentSource);

        try {
            Bugzilla.getInstance().getRepositoryConnector().getClientManager().getClient(getTaskRepository(), new NullProgressMonitor()).
            postAttachment(getID(), comment, desc, attachmentSource.getContentType(), false, source, new NullProgressMonitor());
        } catch (HttpException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
    }

    public String getID() {
        return getID(data);
    }
    
    public static String getID(TaskData taskData) {
        try {
            return Integer.toString(BugzillaRepositoryConnector.getBugId(taskData.getTaskId()));
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
        return "";
    }

    String getDescription() {
        return getMappedValue(TaskAttribute.DESCRIPTION); // XXX WTF???!!!
    }

    TaskRepository getTaskRepository() {
        return repository.getTaskRepository();
    }

    BugzillaRepository getRepository() {
        return repository;
    }
     
    String getStatus() {
        return getMappedValue(TaskAttribute.STATUS);
    }

    String getPriority() {
        return getMappedValue(TaskAttribute.PRIORITY);
    }

    public String getSummary() {
        return getMappedValue(TaskAttribute.SUMMARY);
    }

    String getResolution() {
        return getMappedValue(TaskAttribute.RESOLUTION);
    }

    String getProduct() {
        return getValue(BugzillaAttribute.PRODUCT.getKey());
    }

    String getComponent() {
        return getValue(BugzillaAttribute.COMPONENT.getKey());
    }

    String getVersion() {
        return getValue(BugzillaAttribute.VERSION.getKey());
    }

    String getPlatform() {
        return getValue(BugzillaAttribute.REP_PLATFORM.getKey());
    }

    String getTargetMilestone() {
        return getValue(BugzillaAttribute.TARGET_MILESTONE.getKey());
    }

    String getReporter() {
        return getValue(BugzillaAttribute.REPORTER.getKey());
    }

    String getAssignedTo() {
        return getValue(BugzillaAttribute.ASSIGNED_TO.getKey());
    }

    String getAssignedToName() {
        return getValue(BugzillaAttribute.ASSIGNED_TO_NAME.getKey());
    }

    String getQAContact() {
        return getValue(BugzillaAttribute.QA_CONTACT.getKey());
    }

    String getQAContactName() {
        return getValue(BugzillaAttribute.QA_CONTACT_NAME.getKey());
    }

    String getCC() {
        return getValue(BugzillaAttribute.CC.getKey());
    }

    String getDependsOn() {
        return getValue(BugzillaAttribute.DEPENDSON.getKey());
    }

    String getBlocks() {
        return getValue(BugzillaAttribute.BLOCKED.getKey());
    }

    String getUrl() {
        return getValue(BugzillaAttribute.BUG_FILE_LOC.getKey());
    }

    String getKeywords() {
        return getValue(BugzillaAttribute.KEYWORDS.getKey());
    }


    String getSeverity() {
        return getValue(BugzillaAttribute.BUG_SEVERITY.getKey());
    }

    Set<TaskAttribute> getResolveAttributes(String resolution) {
        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>();
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.OPERATION);
        ta.setValue("resolve");
        attrs.add(ta);
        ta = rta.getMappedAttribute(TaskAttribute.RESOLUTION);
        ta.setValue(resolution);
        attrs.add(ta);
        return attrs;
    }

    private IssueNode createNode() {
        return new BugzillaIssueNode(this);
    }

    private String getMappedValue(String key) {
        TaskAttribute a = data.getRoot().getMappedAttribute(key);
        return a != null ? a.getValue() : "";
    }

    TaskData getData() {
        return data;
    }

    private String getValue(String key) {
        TaskAttribute a = data.getRoot().getMappedAttribute(key);
        return a != null ? a.getValue() : "";
    }

    Comment[] getComments() {
        List<TaskAttribute> attributes = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_COMMENT);
        if (attributes == null) {
            return new Comment[0];
        }
        List<Comment> comments = new ArrayList<Comment>();
        for (TaskAttribute taskAttribute : attributes) {
            comments.add(new Comment(taskAttribute));
        }
        return comments.toArray(new Comment[comments.size()]);
    }

    Attachment[] getAttachments() {
        List<TaskAttribute> attributes = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_ATTACHMENT);
        if (attributes == null) {
            return new Attachment[0];
        }
        List<Attachment> attachments = new ArrayList<Attachment>(attributes.size());
        for (TaskAttribute taskAttribute : attributes) {
            attachments.add(new Attachment(taskAttribute));
        }
        return attachments.toArray(new Attachment[attachments.size()]);
    }

    // XXX carefull - implicit refresh
    public void addComment(String comment, boolean close) {
        if(comment == null && !close) {
            return;
        }
        refresh();

        // resolved attrs
        Set<TaskAttribute> attrs = null;
        if(close) {
            attrs = getResolveAttributes("FIXED");
        }
        // commet attrs
        if(comment != null) {
            TaskAttribute ta = data.getRoot().createMappedAttribute(TaskAttribute.COMMENT_NEW);
            ta.setValue(comment);
            attrs.add(ta);
        }
        try {
            // done
            RepositoryResponse rr = Bugzilla.getInstance().getRepositoryConnector().getTaskDataHandler().postTaskData(getTaskRepository(), data, attrs, new NullProgressMonitor());
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
    }

    public void refresh() {
        try {
            data = Bugzilla.getInstance().getRepositoryConnector().getTaskData(repository.getTaskRepository(), data.getTaskId(), new NullProgressMonitor());
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
        if(controller != null) {
            controller.refreshViewData();
        }
    }
    
    class Comment {
        private final Date when;
        private final String who;
        private final Long number;
        private final String text;

        public Comment(TaskAttribute a) {            
            Date d = null;
            try {
                d = CC_DATE_FORMAT.parse(a.getMappedAttribute(TaskAttribute.COMMENT_DATE).getValue());
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
            when = d;            
            // XXX check for NULL
            who = a.getMappedAttribute(TaskAttribute.COMMENT_AUTHOR).getMappedAttribute(TaskAttribute.PERSON_NAME).getValue();
            number = Long.parseLong(a.getMappedAttribute(TaskAttribute.COMMENT_NUMBER).getValues().get(0));// XXX value or values?
            text = a.getMappedAttribute(TaskAttribute.COMMENT_TEXT).getValue();
        }

        public Long getNumber() {
            return number;
        }

        public String getText() {
            return text;
        }

        public Date getWhen() {
            return when;
        }

        public String getWho() {
            return who;
        }
    }

    class Attachment {
        private final String desc;
        private final String filename;
        private final String author;
        private final Date date;

        public Attachment(TaskAttribute ta) {
            Date d = null;
            try {
                d = CC_DATE_FORMAT.parse(ta.getMappedAttribute(TaskAttribute.ATTACHMENT_DATE).getValues().get(0));// XXX value or values?
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
            date = d;
            filename = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_FILENAME).getValue();
            desc = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_DESCRIPTION).getValues().get(0);// XXX value or values?
            author = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_AUTHOR).getMappedAttribute(TaskAttribute.PERSON_NAME).getValue();
        }

        public String getAuthor() {
            return author;
        }

        public Date getDate() {
            return date;
        }

        public String getDesc() {
            return desc;
        }

        public String getFilename() {
            return filename;
        }
    }

}
