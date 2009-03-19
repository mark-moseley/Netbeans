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
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaAttribute;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaOperation;
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
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.commands.BugzillaCommand;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class BugzillaIssue extends Issue {

    private TaskData data;
    private BugzillaRepository repository;
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm"; // NOI18N
    private static final SimpleDateFormat CC_DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT);
    private IssueController controller;
    private IssueNode node;

    static final String LABEL_NAME_ID           = "bugzilla.issue.id"; // NOI18N
    static final String LABEL_NAME_SEVERITY     = "bugzilla.issue.severity"; // NOI18N
    static final String LABEL_NAME_PRIORITY     = "bugzilla.issue.priority"; // NOI18N
    static final String LABEL_NAME_STATUS       = "bugzilla.issue.status"; // NOI18N
    static final String LABEL_NAME_RESOLUTION   = "bugzilla.issue.resolution"; // NOI18N
    static final String LABEL_NAME_SUMMARY      = "bugzilla.issue.summary"; // NOI18N

    /**
     * Issue wasn't seen yet
     */
    static final int FIELD_STATUS_IRELEVANT = -1;

    /**
     * Field wasn't changed since the issue was seen the last time
     */
    static final int FIELD_STATUS_UPTODATE = 1;

    /**
     * Field has a value in oposite to the last time when it was seen
     */
    static final int FIELD_STATUS_NEW = 2;

    /**
     * Field was changed since the issue was seen the last time
     */
    static final int FIELD_STATUS_MODIFIED = 4;

    enum IssueField {
        SUMMARY(BugzillaAttribute.SHORT_DESC.getKey()),
        STATUS(TaskAttribute.STATUS),
        PRIORITY(BugzillaAttribute.PRIORITY.getKey()),
        RESOLUTION(TaskAttribute.RESOLUTION),
        PRODUCT(BugzillaAttribute.PRODUCT.getKey()),
        COMPONENT(BugzillaAttribute.COMPONENT.getKey()),
        VERSION(BugzillaAttribute.VERSION.getKey()),
        PLATFORM(BugzillaAttribute.REP_PLATFORM.getKey()),
        OS(BugzillaAttribute.OP_SYS.getKey()),
        MILESTONE(BugzillaAttribute.TARGET_MILESTONE.getKey()),
        REPORTER(BugzillaAttribute.REPORTER.getKey()),
        REPORTER_NAME(BugzillaAttribute.REPORTER_NAME.getKey()),
        ASSIGNED_TO(BugzillaAttribute.ASSIGNED_TO.getKey()),
        ASSIGNED_TO_NAME(BugzillaAttribute.ASSIGNED_TO_NAME.getKey()),
        QA_CONTACT(BugzillaAttribute.QA_CONTACT.getKey()),
        QA_CONTACT_NAME(BugzillaAttribute.QA_CONTACT_NAME.getKey()),
        NEWCC(BugzillaAttribute.NEWCC.getKey()),
        REMOVECC(BugzillaAttribute.REMOVECC.getKey()),
        CC(BugzillaAttribute.CC.getKey()),
        DEPENDS_ON(BugzillaAttribute.DEPENDSON.getKey()),
        BLOCKS(BugzillaAttribute.BLOCKED.getKey()),
        URL(BugzillaAttribute.BUG_FILE_LOC.getKey()),
        KEYWORDS(BugzillaAttribute.KEYWORDS.getKey()),
        SEVERITY(BugzillaAttribute.BUG_SEVERITY.getKey()),
        DESCRIPTION(BugzillaAttribute.LONG_DESC.getKey()),
        CREATION(TaskAttribute.DATE_CREATION),
        MODIFICATION(TaskAttribute.DATE_MODIFICATION),
        COMMENT_COUNT(TaskAttribute.TYPE_COMMENT, false),
        ATTACHEMENT_COUNT(TaskAttribute.TYPE_ATTACHMENT, false);

        private final String key;
        private boolean singleAttribute;

        IssueField(String key) {
            this(key, true);
        }
        IssueField(String key, boolean singleAttribute) {
            this.key = key;
            this.singleAttribute = singleAttribute;
        }
        public String getKey() {
            return key;
        }
        public boolean isSingleAttribute() {
            return singleAttribute;
        }
        public boolean isReadOnly() {
            return !singleAttribute;
        }
    }

    private Map<String, String> attributes;

    /**
     * Defines columns for a view table.
     */
    public static ColumnDescriptor[] DESCRIPTORS;

    public BugzillaIssue(TaskData data, BugzillaRepository repo) {
        super(repo);
        this.data = data;
        this.repository = repo;
    }

    @Override
    public String getDisplayName() {
        return data.isNew() ?
                NbBundle.getMessage(BugzillaIssue.class, "CTL_NewIssue") : // NOI18N
                NbBundle.getMessage(BugzillaIssue.class, "CTL_Issue", new Object[] {getID(), getSummary()}); // NOI18N
    }

    @Override
    public String getTooltip() {
        return getDisplayName();
    }

    public static ColumnDescriptor[] getColumnDescriptors() {
        if(DESCRIPTORS == null) {
            ResourceBundle loc = NbBundle.getBundle(BugzillaIssue.class);
            DESCRIPTORS = new ColumnDescriptor[] {
                new ColumnDescriptor<String>(LABEL_NAME_ID, String.class,
                                                  loc.getString("CTL_Issue_ID_Title"), // NOI18N
                                                  loc.getString("CTL_Issue_ID_Desc")), // NOI18N
                new ColumnDescriptor<String>(LABEL_NAME_SEVERITY, String.class,
                                                  loc.getString("CTL_Issue_Severity_Title"), // NOI18N
                                                  loc.getString("CTL_Issue_Severity_Desc")), // NOI18N
                new ColumnDescriptor<String>(LABEL_NAME_PRIORITY, String.class,
                                                  loc.getString("CTL_Issue_Priority_Title"), // NOI18N
                                                  loc.getString("CTL_Issue_Priority_Desc")), // NOI18N
                new ColumnDescriptor<String>(LABEL_NAME_STATUS, String.class,
                                                  loc.getString("CTL_Issue_Status_Title"), // NOI18N
                                                  loc.getString("CTL_Issue_Status_Desc")), // NOI18N
                new ColumnDescriptor<String>(LABEL_NAME_RESOLUTION, String.class,
                                                  loc.getString("CTL_Issue_Resolution_Title"), // NOI18N
                                                  loc.getString("CTL_Issue_Resolution_Desc")), // NOI18N
                new ColumnDescriptor<String>(LABEL_NAME_SUMMARY, String.class,
                                                  loc.getString("CTL_Issue_Summary_Title"), // NOI18N
                                                  loc.getString("CTL_Issue_Summary_Desc")) // NOI18N
            };
        }
        return DESCRIPTORS;
    }

    @Override
    public BugtrackingController getController() {
        if (controller == null) {
            controller = new IssueController(this);
        }
        return controller;
    }

    @Override
    public String toString() {
        String str = getID() + " : "  + getSummary(); // NOI18N
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
            String value;
            for (IssueField field : IssueField.values()) {
                switch(field) {
                    case REPORTER_NAME:
                    case QA_CONTACT_NAME:
                    case ASSIGNED_TO_NAME:
                        continue;
                    default:
                        value = getFieldValue(field);
                }
                if(value != null && !value.trim().equals("")) {
                    attributes.put(field.key, value);
                }
            }
        }
        return attributes;
    }

    @Override
    public void setSeen(boolean seen) throws IOException {
        super.setSeen(seen);
    }

    @Override
    public String getRecentChanges() {
        if(wasSeen()) {
            return "";
        }
        int status = repository.getIssueCache().getStatus(getID());
        if(status == Issue.ISSUE_STATUS_NEW) {
            return "New";
        } else if(status == Issue.ISSUE_STATUS_MODIFIED) {
            List<IssueField> changedFields = new ArrayList<IssueField>();
            Map<String, String> seenAtributes = getSeenAttributes();
            assert seenAtributes != null;
            for (IssueField f : IssueField.values()) {
                String value = getFieldValue(f);
                String seenValue = seenAtributes.get(f.key);
                if(seenValue == null) {
                    seenValue = "";
                }
                if(!value.trim().equals(seenValue)) {
                    changedFields.add(f);
                }
            }
            int changedCount = changedFields.size();
            assert changedCount > 0 : "status MODIFIED yet zero changes found";
            if(changedCount == 1) {
                String ret = null;
                for (IssueField changedField : changedFields) {
                    switch(changedField) {
                        case SUMMARY :
                            ret = "Summary changed";
                            break;
                        case CC :
                            ret = "CC field changed";
                            break;
                        case KEYWORDS :
                            ret ="Keywords changed";
                            break;
                        case DEPENDS_ON :
                        case BLOCKS :
                            ret ="Dependence changed";
                            break;
                        default :
                            ret = changedField.name() + " changed to " + getFieldValue(changedField);
                    }
                }
                return ret;
            } else {
                String ret = null;
                for (IssueField changedField : changedFields) {
                    switch(changedField) {
                        case SUMMARY :
                            ret = changedCount + " changes, incl. summary";
                            break;
                        case PRIORITY :
                            ret = changedCount + " changes, incl. priority";
                            break;
                        case SEVERITY :
                            ret = changedCount + " changes, incl. severity";
                            break;
                        case PRODUCT :
                            ret = changedCount + " changes, incl. product";
                            break;
                        case COMPONENT :
                            ret = changedCount + " changes, incl. component";
                            break;
                        case PLATFORM :
                            ret = changedCount + " changes, incl. platform";
                            break;
                        case VERSION :
                            ret = changedCount + " changes, incl. version";
                            break;
                        case MILESTONE :
                            ret = changedCount + " changes, incl. milestone";
                            break;
                        case KEYWORDS :
                            ret = changedCount + " changes, incl. keywords";
                            break;
                        case URL :
                            ret = changedCount + " changes, incl. url";
                            break;
                        case ASSIGNED_TO :
                            ret = changedCount + " changes, incl. Assignee";
                            break;
                        case QA_CONTACT :
                            ret = changedCount + " changes, incl. qa contact";
                            break;
                        case DEPENDS_ON :
                        case BLOCKS :
                            ret = changedCount + " changes, inclusive dependence";
                            break;
                        default :
                            ret = changedCount + " changes";
                    }
                    return ret;
                }
            }
        }
        return "";
    }

    /**
     * Returns the id from the given taskData or null if taskData.isNew()
     * @param taskData
     * @return id or null
     */
    public static String getID(TaskData taskData) {
        try {
            if(taskData.isNew()) {
                return null;
                }
            return Integer.toString(BugzillaRepositoryConnector.getBugId(taskData.getTaskId()));
        } catch (CoreException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
        return "";
    }

    TaskRepository getTaskRepository() {
        return repository.getTaskRepository();
    }

    BugzillaRepository getRepository() {
        return repository;
    }

    public String getID() {
        return getID(data);
    }

    public String getSummary() {
        return getFieldValue(IssueField.SUMMARY);
    }

    public void setTaskData(TaskData taskData) {
//        assert !taskData.isPartial(); XXX doesn't work with simple search
        data = taskData;
        attributes = null; // reset
        Bugzilla.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                ((BugzillaIssueNode)getNode()).fireDataChanged();
                fireDataChanged();
            }
        });
    }

    TaskData getTaskData() {
        return data;
    }

    /**
     * Returns the value represented by the given field
     *
     * @param f
     * @return
     */
    String getFieldValue(IssueField f) {
        if(f.isSingleAttribute()) {
            TaskAttribute a = data.getRoot().getMappedAttribute(f.key);
            if(a != null && a.getValues().size() > 1) {
                return listValues(a);
            }
            return a != null ? a.getValue() : ""; // NOI18N
        } else {
            List<TaskAttribute> attrs = data.getAttributeMapper().getAttributesByType(data, f.key);
            // returning 0 would set status MODIFIED instead of NEW
            return "" + ( attrs != null && attrs.size() > 0 ?  attrs.size() : ""); // NOI18N
        }
    }

    /**
     * Returns a comma separated list created
     * from the values returned by TaskAttribute.getValues()
     *
     * @param a
     * @return
     */
    private String listValues(TaskAttribute a) {
        if(a == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        List<String> l = a.getValues();
        for (int i = 0; i < l.size(); i++) {
            String s = l.get(i);
            sb.append(s);
            if(i < l.size() -1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    void setFieldValue(IssueField f, String value) {
        if(f.isReadOnly()) {
            assert false : "can't set value into IssueField " + f.name();
            return;
        }
        TaskAttribute a = data.getRoot().getMappedAttribute(f.key);
        if(a == null) {
            a = new TaskAttribute(data.getRoot(), f.key);
        }
        a.setValue(value);
    }

    List<String> getFieldValues(IssueField f) {
        if(f.isSingleAttribute()) {
            TaskAttribute a = data.getRoot().getMappedAttribute(f.key);
            if(a != null) {
                return a.getValues();
            } else {
                return Collections.emptyList();
            }
        } else {
            List<String> ret = new ArrayList<String>();
            ret.add(getFieldValue(f));
            return ret;
        }
    }

    void setFieldValues(IssueField f, List<String> ccs) {
        TaskAttribute a = data.getRoot().getMappedAttribute(f.key);
        if(a == null) {
            a = new TaskAttribute(data.getRoot(), f.key);
        }
        a.setValues(ccs);
    }

    /**
     * Returns a status value for the given field<br>
     * <ul>
     *  <li>{@link #FIELD_STATUS_IRELEVANT} - issue wasn't seen yet
     *  <li>{@link #FIELD_STATUS_UPTODATE} - field value wasn't changed
     *  <li>{@link #FIELD_STATUS_MODIFIED} - field value was changed
     *  <li>{@link #FIELD_STATUS_NEW} - field has a value for the first time since it was seen
     * </ul>
     * @param f IssueField
     * @return a status value
     */
    int getFieldStatus(IssueField f) {
        if(!wasSeen()) {
            return FIELD_STATUS_IRELEVANT;
        }
        Map<String, String> a = getSeenAttributes();
        String seenValue = a != null ? a.get(f.key) : null;
        if(seenValue == null) {
            seenValue = "";
        }
        if(seenValue.equals("") && !seenValue.equals(getFieldValue(f))) {
            return FIELD_STATUS_NEW;
        } else if (!seenValue.equals(getFieldValue(f))) {
            return FIELD_STATUS_MODIFIED;
        }
        return FIELD_STATUS_UPTODATE;
    }

    private IssueNode createNode() {
        return new BugzillaIssueNode(this);
    }

    void resolve(String resolution) {
        setOperation(BugzillaOperation.resolve);
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(BugzillaOperation.resolve.getInputId());
        ta.setValue(resolution);
    }

    void accept() {
        setOperation(BugzillaOperation.accept);
    }

    void duplicate(String id) {
        setOperation(BugzillaOperation.duplicate);
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(BugzillaOperation.duplicate.getInputId());
        ta.setValue(id);
    }

    void reassign(String user) {
        setOperation(BugzillaOperation.reassign);
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(BugzillaOperation.reassign.getInputId());
        if(ta != null) ta.setValue(user);
        ta = rta.getMappedAttribute(BugzillaAttribute.ASSIGNED_TO.getKey());
        if(ta != null) ta.setValue(user);
    }

    void verify() {
        setOperation(BugzillaOperation.verify);
    }

    void close() {
        setOperation(BugzillaOperation.close);
    }

    void reopen() {
        setOperation(BugzillaOperation.reopen);
    }

    private void setOperation(BugzillaOperation operation) {
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.OPERATION);
        ta.setValue(operation.name());
    }

    Attachment[] getAttachments() {
        List<TaskAttribute> attrs = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_ATTACHMENT);
        if (attrs == null) {
            return new Attachment[0];
        }
        List<Attachment> attachments = new ArrayList<Attachment>(attrs.size());
        for (TaskAttribute taskAttribute : attrs) {
            attachments.add(new Attachment(taskAttribute));
        }
        return attachments.toArray(new Attachment[attachments.size()]);
    }

    void addAttachment(final File file, final String comment, final String desc, String contentType, final boolean patch) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt";
        final FileTaskAttachmentSource attachmentSource = new FileTaskAttachmentSource(file);
        if (contentType == null) {
            String ct = FileUtil.getMIMEType(FileUtil.toFileObject(file));
            if ((ct != null) && (!"content/unknown".equals(ct))) { // NOI18N
                contentType = ct;
            } else {
                contentType = FileTaskAttachmentSource.getContentTypeFromFilename(file.getName());
            }
        }
        attachmentSource.setContentType(contentType);
        final BugzillaTaskAttachmentHandler.AttachmentPartSource source = new BugzillaTaskAttachmentHandler.AttachmentPartSource(attachmentSource);

        BugzillaCommand cmd = new BugzillaCommand() {
            @Override
            public void execute() throws CoreException, IOException, MalformedURLException {
                refresh();
                Bugzilla.getInstance().getClient(repository).postAttachment(
                                getID(),
                                comment,
                                desc,
                                attachmentSource.getContentType(),
                                patch,
                                source,
                                new NullProgressMonitor());
                refresh(); // XXX to much refresh - is there no other way?
            }
        };
        repository.getExecutor().execute(cmd);
    }

    Comment[] getComments() {
        List<TaskAttribute> attrs = data.getAttributeMapper().getAttributesByType(data, TaskAttribute.TYPE_COMMENT);
        if (attrs == null) {
            return new Comment[0];
        }
        List<Comment> comments = new ArrayList<Comment>();
        for (TaskAttribute taskAttribute : attrs) {
            comments.add(new Comment(taskAttribute));
        }
        return comments.toArray(new Comment[comments.size()]);
    }

    // XXX carefull - implicit refresh
    public void addComment(String comment, boolean close) {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt";
        if(comment == null && !close) {
            return;
        }
        refresh();

        // resolved attrs
        if(close) {
            resolve("FIXED"); // XXX constant?
        }
        if(comment != null) {
            addComment(comment);
        }

        BugzillaCommand submitCmd = new BugzillaCommand() {
            @Override
            public void execute() throws CoreException, IOException, MalformedURLException {
                submitAndRefresh();
            }
        };
        repository.getExecutor().execute(submitCmd);
    }

    public void addComment(String comment) {
        if(comment != null) {
            TaskAttribute ta = data.getRoot().createMappedAttribute(TaskAttribute.COMMENT_NEW);
            ta.setValue(comment);
        }
    }

    @Override
    public void attachPatch(File file, String description) {
        addAttachment(file, null, description, null, true);
    }

    void submitAndRefresh() {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt";

        final boolean wasNew = data.isNew();
        final boolean wasSeenAlready = wasNew || repository.getIssueCache().wasSeen(getID());
        BugzillaCommand cmd = new BugzillaCommand() {
            @Override
            public void execute() throws CoreException, IOException, MalformedURLException {
                // submit
                RepositoryResponse rr = Bugzilla.getInstance().getRepositoryConnector().getTaskDataHandler().postTaskData(getTaskRepository(), data, null, new NullProgressMonitor());
                // XXX evaluate rr

                if (!wasNew) {
                    refresh();
                } else {
                    refresh(rr.getTaskId(), true);

                }
                // it was the user who made the changes, so preserve the seen status if seen already
                if (wasSeenAlready) {
                    try {
                        repository.getIssueCache().setSeen(getID(), true);
                        // it was the user who made the changes, so preserve the seen status if seen already
                    } catch (IOException ex) {
                        Bugzilla.LOG.log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        repository.getExecutor().execute(cmd);
    }

    public boolean refresh() {
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt";
        return refresh(getID(), false);
    }

    public boolean refresh(String id, boolean cacheThisIssue) { // XXX cacheThisIssue - we probalby don't need this, just always set the issue into the cache 
        assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt";
        try {
            TaskData td = BugzillaUtil.getTaskData(repository, id);
            if(td == null) {
                return false;
            }
            getRepository().getIssueCache().setIssueData(id, td, this); // XXX
            if (controller != null) {
                controller.refreshViewData();
            }
        } catch (IOException ex) {
            Bugzilla.LOG.log(Level.SEVERE, null, ex);
        }
        return true;
    }


    private Map<String, String> getSeenAttributes() {
        Map<String, String> seenAtributes = repository.getIssueCache().getSeenAttributes(getID());
        if(seenAtributes == null) {
            seenAtributes = new HashMap<String, String>();
        }
        return seenAtributes;
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
                Bugzilla.LOG.log(Level.SEVERE, null, ex);
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
        private final String id;
        private String contentType;
        private String isDeprected;
        private String size;
        private String isPatch;
        private String url;


        public Attachment(TaskAttribute ta) {
            id = ta.getValue();
            Date d = null;
            try {
                d = CC_DATE_FORMAT.parse(ta.getMappedAttribute(TaskAttribute.ATTACHMENT_DATE).getValues().get(0));// XXX value or values?
            } catch (ParseException ex) {
                Bugzilla.LOG.log(Level.SEVERE, null, ex);
            }
            date = d;
            filename = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_FILENAME).getValue();
            desc = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_DESCRIPTION).getValues().get(0);// XXX value or values?
            author = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_AUTHOR).getMappedAttribute(TaskAttribute.PERSON_NAME).getValue();
            contentType = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_CONTENT_TYPE).getValue();
            isDeprected = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_IS_DEPRECATED).getValue();
            isPatch = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_IS_PATCH).getValue();
            size = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_SIZE).getValue();
            url = ta.getMappedAttribute(TaskAttribute.ATTACHMENT_URL).getValue();
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

        public String getContentType() {
            return contentType;
        }

        public String getId() {
            return id;
        }

        public String getIsDeprected() {
            return isDeprected;
        }

        public String getIsPatch() {
            return isPatch;
        }

        public String getSize() {
            return size;
        }

        public String getUrl() {
            return url;
        }

        public void getAttachementData(final OutputStream os) {
            assert !SwingUtilities.isEventDispatchThread() : "Accessing remote host. Do not call in awt";
            BugzillaCommand cmd = new BugzillaCommand() {
                @Override
                public void execute() throws CoreException, IOException, MalformedURLException {
                    Bugzilla.getInstance().getClient(repository).getAttachmentData(id, os, new NullProgressMonitor());
                }
            };
            repository.getExecutor().execute(cmd);
        }
    }

}
