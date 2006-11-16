/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.netbeans.lib.cvsclient.command.log.LogInformation;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.SearchHistoryAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.GetCleanAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.UpdateExecutor;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.openide.ErrorManager;
import org.openide.cookies.ViewCookie;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.event.*;
import java.text.DateFormat;
import java.io.File;

/**
 * Shows Search History results in a JList.
 * 
 * @author Maros Sandor
 */
class SummaryView implements MouseListener, ComponentListener, MouseMotionListener {

    private static final double DARKEN_FACTOR = 0.95;    

    private final SearchHistoryPanel master;
    
    private JList       resultsList;
    private JScrollPane scrollPane;

    private final List  dispResults;
    private String      message;
    private AttributeSet searchHiliteAttrs;

    public SummaryView(SearchHistoryPanel master, List results) {
        this.master = master;
        this.dispResults = expandResults(results);
        FontColorSettings fcs = MimeLookup.getLookup(MimePath.get("text/x-java")).lookup(FontColorSettings.class); // NOI18N
        searchHiliteAttrs = fcs.getFontColors("highlight-search"); // NOI18N
        message = master.getCriteria().getCommitMessage();
        resultsList = new JList(new SummaryListModel());
        resultsList.setFixedCellHeight(-1);
        resultsList.addMouseListener(this);
        resultsList.addMouseMotionListener(this);
        resultsList.setCellRenderer(new SummaryCellRenderer());
        resultsList.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SummaryView.class, "ACSN_SummaryView_List"));
        resultsList.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SummaryView.class, "ACSD_SummaryView_List"));
        scrollPane = new JScrollPane(resultsList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        master.addComponentListener(this);
    }

    public void componentResized(ComponentEvent e) {
        int [] selection = resultsList.getSelectedIndices();
        resultsList.setModel(new SummaryListModel());
        resultsList.setSelectedIndices(selection);
    }

    public void componentHidden(ComponentEvent e) {
        // not interested
    }

    public void componentMoved(ComponentEvent e) {
        // not interested
    }

    public void componentShown(ComponentEvent e) {
        // not interested
    }
    
    private static List expandResults(List results) {
        ArrayList newResults = new ArrayList(results.size());
        for (Iterator i = results.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof SearchHistoryPanel.ResultsContainer) {
                newResults.add(o);
                SearchHistoryPanel.ResultsContainer container = (SearchHistoryPanel.ResultsContainer) o;
                for (Iterator j = container.getRevisions().iterator(); j.hasNext();) {
                    SearchHistoryPanel.DispRevision revision = (SearchHistoryPanel.DispRevision) j.next();
                    if (!revision.isBranchRoot()) {
                        newResults.add(revision);
                    }
                }
                for (Iterator j = container.getRevisions().iterator(); j.hasNext();) {
                    SearchHistoryPanel.DispRevision revision = (SearchHistoryPanel.DispRevision) j.next();
                    addResults(newResults, revision, 1);
                }
            } else {
                SearchHistoryPanel.DispRevision revision = (SearchHistoryPanel.DispRevision) o;
                if (!revision.isBranchRoot()) {
                    newResults.add(revision);
                }
                addResults(newResults, revision, 0);
            }
        }
        return newResults;
    }

    private static void addResults(ArrayList newResults, SearchHistoryPanel.DispRevision dispRevision, int indentation) {
        dispRevision.setIndentation(indentation);
        List children = dispRevision.getChildren();
        if (children != null) {
            for (Iterator i = children.iterator(); i.hasNext();) {
                SearchHistoryPanel.DispRevision revision = (SearchHistoryPanel.DispRevision) i.next();
                if (!revision.isBranchRoot()) {
                    newResults.add(revision);
                }
            }
            for (Iterator i = children.iterator(); i.hasNext();) {
                SearchHistoryPanel.DispRevision revision = (SearchHistoryPanel.DispRevision) i.next();
                addResults(newResults, revision, indentation + 1);
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
        int idx = resultsList.locationToIndex(e.getPoint());
        if (idx == -1) return;
        Rectangle rect = resultsList.getCellBounds(idx, idx);
        Point p = new Point(e.getX() - rect.x, e.getY() - rect.y);
        Rectangle diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Diff-" + idx); // NOI18N
        if (diffBounds != null && diffBounds.contains(p)) {
            diffPrevious(idx);
        }
        diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Acp-" + idx); // NOI18N
        if (diffBounds != null && diffBounds.contains(p)) {
            associatedChangesInProject(idx);
        }
        diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Acop-" + idx); // NOI18N
        if (diffBounds != null && diffBounds.contains(p)) {
            associatedChangesInOpenProjects(idx);
        }
        diffBounds = (Rectangle) resultsList.getClientProperty("Summary-tagsLink-" + idx); // NOI18N
        if (diffBounds != null && diffBounds.contains(p)) {
            showAlltags(e.getPoint(), idx);
        }
    }

    public static void showAllTags(Window w, Point p, SearchHistoryPanel.DispRevision drev) {

        final JTextPane tp = new JTextPane();
        tp.setBackground(darker(UIManager.getColor("List.background"))); // NOI18N
        tp.setBorder(BorderFactory.createEmptyBorder(6, 8, 0, 0));
        tp.setEditable(false);

        Style headerStyle = tp.addStyle("headerStyle", null); // NOI18N
        StyleConstants.setBold(headerStyle, true);
        Style unmodifiedBranchStyle = tp.addStyle("unmodifiedBranchStyle", null); // NOI18N
        StyleConstants.setForeground(unmodifiedBranchStyle, Color.GRAY);
            
        String modifiedBranches = drev.getRevision().getBranches();
                    
        Document doc = tp.getDocument();
        try {
            List<String> tags;
                
            doc.insertString(doc.getLength(), NbBundle.getMessage(SummaryView.class, "CTL_TagsWindow_BranchesLabel") + "\n", headerStyle);
            tags = drev.getBranches();
            for (String tag : tags) {
                if (modifiedBranches == null || (modifiedBranches.indexOf(tag.substring(tag.indexOf("(") + 1, tag.indexOf(")")))) == -1) { // NOI18N
                    doc.insertString(doc.getLength(), tag + "\n", unmodifiedBranchStyle); // NOI18N
                } else {
                    doc.insertString(doc.getLength(), tag + "\n", null); // NOI18N
                }
            }
            if (tags.size() == 0) {
                doc.insertString(doc.getLength(), NbBundle.getMessage(SummaryView.class, "CTL_TagsWindow_NoBranchesLabel") + "\n", unmodifiedBranchStyle);
            }

            doc.insertString(doc.getLength(), "\n" + NbBundle.getMessage(SummaryView.class, "CTL_TagsWindow_TagsLabel") + "\n", headerStyle);
            StringBuilder sb = new StringBuilder();
            tags = drev.getTags();
            for (String tag : tags) {
                sb.append(tag);
                sb.append('\n'); // NOI18N
            }
            doc.insertString(doc.getLength(), sb.toString(), null);

            if (tags.size() == 0) {
                doc.insertString(doc.getLength(), NbBundle.getMessage(SummaryView.class, "CTL_TagsWindow_NoTagsLabel"), unmodifiedBranchStyle);
            }
                
        } catch (BadLocationException e) {
            Logger.getLogger(SummaryView.class.getName()).log(Level.WARNING, "Internal error creating tag list", e); // NOI18N
        }
            
        Dimension dim = tp.getPreferredSize();
        tp.setPreferredSize(new Dimension(dim.width * 7 / 6, dim.height));
        final JScrollPane jsp = new JScrollPane(tp);
        jsp.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));

        TooltipWindow ttw = new TooltipWindow(w, jsp);
        ttw.addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent e) {
                tp.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
            }
        });
        ttw.show(p);
    }
    
    private void showAlltags(Point p, int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof SearchHistoryPanel.DispRevision) {
            SwingUtilities.convertPointToScreen(p, resultsList);
            p.x += 10;
            showAllTags(SwingUtilities.windowForComponent(scrollPane), p, (SearchHistoryPanel.DispRevision) o);
        }
    }

    public void mouseEntered(MouseEvent e) {
        // not interested
    }

    public void mouseExited(MouseEvent e) {
        // not interested
    }

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            onPopup(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            onPopup(e);
        }
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
        int idx = resultsList.locationToIndex(e.getPoint());
        if (idx == -1) return;
        Rectangle rect = resultsList.getCellBounds(idx, idx);
        Point p = new Point(e.getX() - rect.x, e.getY() - rect.y);
        Rectangle diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Diff-" + idx); // NOI18N
        if (diffBounds != null && diffBounds.contains(p)) {
            resultsList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }
        diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Acp-" + idx); // NOI18N
        if (diffBounds != null && diffBounds.contains(p)) {
            resultsList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }
        diffBounds = (Rectangle) resultsList.getClientProperty("Summary-Acop-" + idx); // NOI18N
        if (diffBounds != null && diffBounds.contains(p)) {
            resultsList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }
        diffBounds = (Rectangle) resultsList.getClientProperty("Summary-tagsLink-" + idx); // NOI18N
        if (diffBounds != null && diffBounds.contains(p)) {
            resultsList.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return;
        }
        resultsList.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void onPopup(MouseEvent e) {
        int [] sel = resultsList.getSelectedIndices();
        if (sel.length == 0) {
            int idx = resultsList.locationToIndex(e.getPoint());
            if (idx == -1) return;
            resultsList.setSelectedIndex(idx);
            sel = new int [] { idx };
        }
        final int [] selection = sel;

        JPopupMenu menu = new JPopupMenu();
        
        String previousRevision = null;
        SearchHistoryPanel.ResultsContainer container = null; 
        SearchHistoryPanel.DispRevision drev = null;
        Object revCon = dispResults.get(selection[0]);
        if (revCon instanceof SearchHistoryPanel.ResultsContainer) {
            container = (SearchHistoryPanel.ResultsContainer) dispResults.get(selection[0]); 
        } else {
            drev = (SearchHistoryPanel.DispRevision) dispResults.get(selection[0]);
            previousRevision = Utils.previousRevision(drev.getRevision().getNumber().trim());
        }
        if (container != null) {
            String eldest = container.getEldestRevision();
            if (eldest == null) {
                eldest = ((SearchHistoryPanel.DispRevision) container.getRevisions().get(container.getRevisions().size() - 1)).getRevision().getNumber();
            }
            menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_Diff", eldest, container.getNewestRevision())) {
                public void actionPerformed(ActionEvent e) {
                    diffPrevious(selection[0]);
                }
            }));
        } else {
            if (previousRevision != null) {
                menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_DiffToPrevious", previousRevision)) {
                    {
                        setEnabled(selection.length == 1 && dispResults.get(selection[0]) instanceof SearchHistoryPanel.DispRevision);
                    }
                    public void actionPerformed(ActionEvent e) {
                        diffPrevious(selection[0]);
                    }
                }));
            }
        }
        menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_RollbackChange")) {
            {
                setEnabled(someRevisions(selection));
            }
            public void actionPerformed(ActionEvent e) {
                rollbackChange(selection);
            }
        }));
        if (drev != null) {
            if (!"dead".equals(drev.getRevision().getState())) { // NOI18N
                menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_RollbackTo", drev.getRevision().getNumber())) {
                    {
                        setEnabled(selection.length == 1 && dispResults.get(selection[0]) instanceof SearchHistoryPanel.DispRevision);
                    }
                    public void actionPerformed(ActionEvent e) {
                        rollback(selection[0]);
                    }
                }));
                menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_SummaryView_View", drev.getRevision().getNumber())) {
                    {
                        setEnabled(selection.length == 1 && dispResults.get(selection[0]) instanceof SearchHistoryPanel.DispRevision);
                    }
                    public void actionPerformed(ActionEvent e) {
                        RequestProcessor.getDefault().post(new Runnable() {
                            public void run() {
                                view(selection[0]);
                            }
                        });
                    }
                }));
                
            }

            Project prj = master.getProject(drev.getRevision().getLogInfoHeader().getFile());
            if (prj != null) {
                String prjName = ProjectUtils.getInformation(prj).getDisplayName();
                menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_Action_AssociateChangesInProject", prjName)) {
                    {
                        setEnabled(selection.length == 1 && dispResults.get(selection[0]) instanceof SearchHistoryPanel.DispRevision);
                    }
                    public void actionPerformed(ActionEvent e) {
                        associatedChangesInProject(selection[0]);
                    }
                }));
            }
            menu.add(new JMenuItem(new AbstractAction(NbBundle.getMessage(SummaryView.class, "CTL_Action_AssociateChangesInOpenProjects")) {
                {
                    setEnabled(selection.length == 1 && dispResults.get(selection[0]) instanceof SearchHistoryPanel.DispRevision);
                }
                public void actionPerformed(ActionEvent e) {
                    associatedChangesInOpenProjects(selection[0]);
                }
            }));
        }

        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private boolean someRevisions(int[] selection) {
        for (int i = 0; i < selection.length; i++) {
            Object revCon = dispResults.get(selection[i]);
            if (revCon instanceof SearchHistoryPanel. DispRevision) {
                return true;
            }
        }
        return false;
    }

    private void rollbackChange(int [] selection) {
        List changes = new ArrayList();
        for (int i = 0; i < selection.length; i++) {
            int idx = selection[i];
            Object o = dispResults.get(idx);
            if (o instanceof SearchHistoryPanel.DispRevision) {
                SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) o;
                changes.add(drev.getRevision());
            }
        }
        rollbackChanges((LogInformation.Revision[]) changes.toArray(new LogInformation.Revision[changes.size()]));
    }

    private static void rollbackChange(LogInformation.Revision change, ExecutorGroup group) {
        UpdateCommand cmd = new UpdateCommand();
        cmd.setFiles(new File [] { change.getLogInfoHeader().getFile() });
        cmd.setMergeRevision1(change.getNumber());
        cmd.setMergeRevision2(Utils.previousRevision(change.getNumber()));
        group.addExecutors(UpdateExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), null));
    }

    static void rollbackChanges(LogInformation.Revision [] changes) {
        ExecutorGroup group = new ExecutorGroup(NbBundle.getMessage(SummaryView.class, "MSG_SummaryView_RollingBackChange"));
        for (int i = 0; i < changes.length; i++) {
            rollbackChange(changes[i], group);
        }
        group.execute();
    }
    
    private void rollback(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof SearchHistoryPanel.DispRevision) {
            SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) o;
            String revision = drev.getRevision().getNumber().trim();
            File file = drev.getRevision().getLogInfoHeader().getFile();
            GetCleanAction.rollback(file, revision);
        }
    }


    private void view(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof SearchHistoryPanel.DispRevision) {
            SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) o;
            ViewCookie view = (ViewCookie) new RevisionNode(drev).getCookie(ViewCookie.class);
            if (view != null) {
                view.view();
            }
        }
    }    
    private void diffPrevious(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof SearchHistoryPanel.DispRevision) {
            SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) o;
            master.showDiff(drev);
        } else {
            SearchHistoryPanel.ResultsContainer container = (SearchHistoryPanel.ResultsContainer) o;
            master.showDiff(container);
        }
    }

    private void associatedChangesInOpenProjects(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof SearchHistoryPanel.DispRevision) {
            SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) o;
            Project [] projects  = OpenProjects.getDefault().getOpenProjects();
            int n = projects.length;
            SearchHistoryAction.openSearch(
                    (n == 1) ? ProjectUtils.getInformation(projects[0]).getDisplayName() : 
                    NbBundle.getMessage(SummaryView.class, "CTL_FindAssociateChanges_OpenProjects_Title", Integer.toString(n)),
                    drev.getRevision().getMessage().trim(), drev.getRevision().getAuthor(), drev.getRevision().getDate());
        }
    }

    private void associatedChangesInProject(int idx) {
        Object o = dispResults.get(idx);
        if (o instanceof SearchHistoryPanel.DispRevision) {
            SearchHistoryPanel.DispRevision drev = (SearchHistoryPanel.DispRevision) o;
            File file = drev.getRevision().getLogInfoHeader().getFile();
            Project project = master.getProject(file);                
            Context context = Utils.getProjectsContext(new Project[] { master.getProject(file) });
            SearchHistoryAction.openSearch(
                    context, 
                    ProjectUtils.getInformation(project).getDisplayName(),
                    drev.getRevision().getMessage().trim(), drev.getRevision().getAuthor(), drev.getRevision().getDate());
        }
    }
    
    public JComponent getComponent() {
        return scrollPane;
    }

    private class SummaryListModel extends AbstractListModel {

        public int getSize() {
            return dispResults.size();
        }

        public Object getElementAt(int index) {
            return dispResults.get(index);
        }
    }
    
    private static Color darker(Color c) {
        return new Color(Math.max((int)(c.getRed() * DARKEN_FACTOR), 0), 
             Math.max((int)(c.getGreen() * DARKEN_FACTOR), 0),
             Math.max((int)(c.getBlue() * DARKEN_FACTOR), 0));
    }    
    
    private class SummaryCellRenderer extends JPanel implements ListCellRenderer {

        private static final String FIELDS_SEPARATOR = "        "; // NOI18N

        private Style selectedStyle;
        private Style normalStyle;
        private Style branchStyle;
        private Style filenameStyle;
        private Style indentStyle;
        private Style noindentStyle;
        private Style hiliteStyle;
        
        private JTextPane textPane = new JTextPane();
        private JPanel    actionsPane = new JPanel();
        private final JPanel    tagsPanel;
        private final JPanel actionsPanel;
        
        private DateFormat defaultFormat;
        
        private int             index;
        private final HyperlinkLabel  tagsLink;
        private final HyperlinkLabel  diffLink;
        private final HyperlinkLabel  acpLink;
        private final HyperlinkLabel  acopLink;
        
        private final JLabel    tagsLabel;
        private final JLabel    diffToLabel;
        private final JLabel    findCommitInLabel;
        private final JLabel    commaLabel;

        public SummaryCellRenderer() {
            selectedStyle = textPane.addStyle("selected", null); // NOI18N
            StyleConstants.setForeground(selectedStyle, UIManager.getColor("List.selectionForeground")); // NOI18N
            normalStyle = textPane.addStyle("normal", null); // NOI18N
            StyleConstants.setForeground(normalStyle, UIManager.getColor("List.foreground")); // NOI18N
            branchStyle = textPane.addStyle("normal", null); // NOI18N
            StyleConstants.setForeground(branchStyle, Color.GRAY); // NOI18N
            filenameStyle = textPane.addStyle("filename", normalStyle); // NOI18N
            StyleConstants.setBold(filenameStyle, true);
            indentStyle = textPane.addStyle("indent", null); // NOI18N
            StyleConstants.setLeftIndent(indentStyle, 50);
            noindentStyle = textPane.addStyle("noindent", null); // NOI18N
            StyleConstants.setLeftIndent(noindentStyle, 0);
            defaultFormat = DateFormat.getDateTimeInstance();

            hiliteStyle = textPane.addStyle("hilite", normalStyle); // NOI18N
            Color c = (Color) searchHiliteAttrs.getAttribute(StyleConstants.Background);
            if (c != null) StyleConstants.setBackground(hiliteStyle, c);
            c = (Color) searchHiliteAttrs.getAttribute(StyleConstants.Foreground);
            if (c != null) StyleConstants.setForeground(hiliteStyle, c);

            setLayout(new BorderLayout());
            add(textPane);
            add(actionsPane, BorderLayout.PAGE_END);
            
            actionsPane.setLayout(new BorderLayout());
            actionsPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
            
            tagsPanel = new JPanel(); 
            tagsPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 2, 5));
            actionsPanel = new JPanel();
            actionsPanel.setLayout(new FlowLayout(FlowLayout.TRAILING, 2, 5));
            actionsPane.add(tagsPanel, BorderLayout.WEST);
            actionsPane.add(actionsPanel);
            
            tagsLabel = new JLabel();
            tagsLink = new HyperlinkLabel();
            tagsPanel.add(tagsLabel);
            tagsLabel.setBorder(BorderFactory.createEmptyBorder(0, 50 - 2, 0, 0));  // -2 flowlayout hgap
            tagsPanel.add(tagsLink);
            
            diffToLabel = new JLabel(NbBundle.getMessage(SummaryView.class, "CTL_Action_DiffTo")); // NOI18N
            actionsPanel.add(diffToLabel);
            diffLink = new HyperlinkLabel();
            diffLink.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
            actionsPanel.add(diffLink);

            acopLink = new HyperlinkLabel();
            acpLink = new HyperlinkLabel();

            findCommitInLabel = new JLabel(NbBundle.getMessage(SummaryView.class, "CTL_Action_FindCommitIn"));
            actionsPanel.add(findCommitInLabel);
            actionsPanel.add(acpLink);
            
            commaLabel = new JLabel(","); // NOI18N
            actionsPanel.add(commaLabel);
            actionsPanel.add(acopLink);
            
            textPane.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        }
                
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof SearchHistoryPanel.ResultsContainer) {
                renderContainer((SearchHistoryPanel.ResultsContainer) value, index, isSelected);
            } else {
                renderRevision(list, (SearchHistoryPanel.DispRevision) value, index, isSelected);
            }
            return this;
        }

        private void renderContainer(SearchHistoryPanel.ResultsContainer container, int index, boolean isSelected) {

            StyledDocument sd = textPane.getStyledDocument();

            Style style;
            if (isSelected) {
                textPane.setBackground(UIManager.getColor("List.selectionBackground")); // NOI18N
                actionsPane.setBackground(UIManager.getColor("List.selectionBackground")); // NOI18N
                style = selectedStyle;
            } else {
                Color c = UIManager.getColor("List.background"); // NOI18N
                textPane.setBackground((index & 1) == 0 ? c : darker(c));
                actionsPane.setBackground((index & 1) == 0 ? c : darker(c));
                style = normalStyle;
            }
            
            try {
                sd.remove(0, sd.getLength());
                sd.setCharacterAttributes(0, Integer.MAX_VALUE, style, true);
                sd.insertString(0, container.getName(), null);
                sd.setCharacterAttributes(0, sd.getLength(), filenameStyle, false);
                sd.insertString(sd.getLength(), FIELDS_SEPARATOR + container.getPath(), null);
                sd.setCharacterAttributes(0, sd.getLength(), style, false);
                sd.setParagraphAttributes(0, sd.getLength(), noindentStyle, false);
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
            actionsPane.setVisible(false);
        }

        private void renderRevision(JList list, SearchHistoryPanel.DispRevision dispRevision, final int index, boolean isSelected) {
            Style style;
            StyledDocument sd = textPane.getStyledDocument();

            this.index = index;
            
            Color backgroundColor;
            Color foregroundColor;
            
            if (isSelected) {
                foregroundColor = UIManager.getColor("List.selectionForeground"); // NOI18N
                backgroundColor = UIManager.getColor("List.selectionBackground"); // NOI18N
                style = selectedStyle;
            } else {
                foregroundColor = UIManager.getColor("List.foreground"); // NOI18N
                backgroundColor = UIManager.getColor("List.background"); // NOI18N
                backgroundColor = (index & 1) == 0 ? backgroundColor : darker(backgroundColor); 
                style = normalStyle;
            }
            textPane.setBackground(backgroundColor);
            actionsPane.setBackground(backgroundColor);
            tagsPanel.setBackground(backgroundColor);
            actionsPanel.setBackground(backgroundColor);
            
            LogInformation.Revision revision = dispRevision.getRevision();
            String commitMessage = revision.getMessage();
            if (commitMessage.endsWith("\n")) commitMessage = commitMessage.substring(0, commitMessage.length() - 1); // NOI18N
            int indentation = dispRevision.getIndentation();
            try {
                sd.remove(0, sd.getLength());
                sd.setCharacterAttributes(0, Integer.MAX_VALUE, style, true);
                if (indentation == 0) {
                    sd.insertString(0, dispRevision.getRevision().getLogInfoHeader().getFile().getName(), style);
                    sd.setCharacterAttributes(0, sd.getLength(), filenameStyle, false);
                    sd.insertString(sd.getLength(), FIELDS_SEPARATOR + dispRevision.getName().substring(0, dispRevision.getName().lastIndexOf('/')) + "\n", style); // NOI18N
                }
                StringBuilder headerMessageBuilder = new StringBuilder();
                headerMessageBuilder.append(revision.getNumber());
                headerMessageBuilder.append(FIELDS_SEPARATOR);
                headerMessageBuilder.append(defaultFormat.format(revision.getDate()));
                headerMessageBuilder.append(FIELDS_SEPARATOR);
                headerMessageBuilder.append(revision.getAuthor());
                String branch = getBranch(dispRevision);
                
                String headerMessage = headerMessageBuilder.toString();
                sd.insertString(sd.getLength(), headerMessage, style);

                if (branch != null) {
                    sd.insertString(sd.getLength(), FIELDS_SEPARATOR + branch, branchStyle);
                }
                if ("dead".equalsIgnoreCase(dispRevision.getRevision().getState())) { // NOI18N
                    sd.insertString(sd.getLength(), FIELDS_SEPARATOR + NbBundle.getMessage(SummaryView.class, "MSG_SummaryView_DeadState"), style);
                }
                sd.insertString(sd.getLength(), "\n", style); // NOI18N
                
                sd.insertString(sd.getLength(), commitMessage, style);
                if (message != null && !isSelected) {
                    int idx = revision.getMessage().indexOf(message);
                    if (idx != -1) {
                        int len = commitMessage.length();
                        int doclen = sd.getLength();
                        sd.setCharacterAttributes(doclen - len + idx, message.length(), hiliteStyle, false);
                    }
                }
                if (indentation > 0) {
                    sd.setParagraphAttributes(0, sd.getLength(), indentStyle, false);
                } else {
                    sd.setParagraphAttributes(0, sd.getLength(), noindentStyle, false);
                }
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
            
            if (commitMessage != null) {
                int width = master.getWidth();
                if (width > 0) {
                    FontMetrics fm = list.getFontMetrics(list.getFont());
                    Rectangle2D rect = fm.getStringBounds(commitMessage, textPane.getGraphics());
                    int nlc, i;
                    for (nlc = -1, i = 0; i != -1 ; i = commitMessage.indexOf('\n', i + 1), nlc++); // NOI18N
                    if (indentation == 0) nlc++;
                    int lines = (int) (rect.getWidth() / (width - 80) + 1);
                    int ph = fm.getHeight() * (lines + nlc + 1) + 4;        // + 4 text pane border
                    textPane.setPreferredSize(new Dimension(width - 50, ph));
                }
            }
            
            actionsPane.setVisible(true);

            List<String> tags = new ArrayList<String>(dispRevision.getBranches());
            tags.addAll(dispRevision.getTags());
            if (tags.size() > 0) {
                tagsLabel.setVisible(true);
                String tagInfo = tags.get(0);
                tagsLabel.setForeground(isSelected ? foregroundColor : Color.GRAY);
                if (tags.size() > 1) {
                    tagInfo += ","; // NOI18N
                    tagsLink.setVisible(true);                    
                    tagsLink.set("...", foregroundColor, backgroundColor); // NOI18N
                } else {
                    tagsLink.setVisible(false);
                }
                tagsLabel.setText(tagInfo); 
            } else {
                tagsLabel.setVisible(false);
                tagsLink.setVisible(false);
            }

            String prev = Utils.previousRevision(dispRevision.getRevision().getNumber());
            if (prev != null) {
                diffToLabel.setVisible(true);
                diffLink.setVisible(true);
                diffToLabel.setForeground(foregroundColor);
                diffLink.set(prev, foregroundColor, backgroundColor);
            } else {
                diffToLabel.setVisible(false);
                diffLink.setVisible(false);
            }

            Project [] projects  = OpenProjects.getDefault().getOpenProjects();
            if (projects.length > 0) {
                acopLink.setVisible(true);
                acopLink.set(NbBundle.getMessage(SummaryView.class, "CTL_Action_FindCommitInOpenProjects"), foregroundColor, backgroundColor);
            } else {
                acopLink.setVisible(false);
            }
            
            Project prj = master.getProject(dispRevision.getRevision().getLogInfoHeader().getFile());
            if (prj != null) {
                String prjName = ProjectUtils.getInformation(prj).getDisplayName();
                acpLink.setVisible(true);
                acpLink.set("\"" + prjName + "\"", foregroundColor, backgroundColor); // NOI18N
            } else {
                acpLink.setVisible(false);
            }

            if (acpLink.isVisible() || acopLink.isVisible()) {
                findCommitInLabel.setVisible(true);
                findCommitInLabel.setForeground(foregroundColor);
                if (acopLink.isVisible() && acopLink.isVisible()) {
                    commaLabel.setVisible(true);
                    commaLabel.setForeground(foregroundColor);
                } else {
                    commaLabel.setVisible(false);
                }
            } else {
                commaLabel.setVisible(false);
                findCommitInLabel.setVisible(false);
            }
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Rectangle apb = actionsPane.getBounds();
            Rectangle lkb = actionsPanel.getBounds();
            if (diffLink.isVisible()) {
                Rectangle bounds = diffLink.getBounds();
                bounds.setBounds(bounds.x + lkb.x, bounds.y + apb.y + lkb.y, bounds.width, bounds.height);
                resultsList.putClientProperty("Summary-Diff-" + index, bounds); // NOI18N
            }
            if (acpLink.isVisible()) {
                Rectangle bounds = acpLink.getBounds();
                bounds.setBounds(bounds.x + lkb.x, bounds.y + apb.y + lkb.y, bounds.width, bounds.height);
                resultsList.putClientProperty("Summary-Acp-" + index, bounds); // NOI18N
            }
            if (acopLink.isVisible()) {
                Rectangle bounds = acopLink.getBounds();
                bounds.setBounds(bounds.x + lkb.x, bounds.y + apb.y + lkb.y, bounds.width, bounds.height);
                resultsList.putClientProperty("Summary-Acop-" + index, bounds); // NOI18N
            }
            if (tagsLink.isVisible()) {
                Rectangle tpb = tagsPanel.getBounds();
                Rectangle bounds = tagsLink.getBounds();
                bounds.setBounds(bounds.x + tpb.x, bounds.y + apb.y + tpb.y, bounds.width, bounds.height);
                resultsList.putClientProperty("Summary-tagsLink-" + index, bounds); // NOI18N
            }
        }
    }
    
    private String getBranch(SearchHistoryPanel.DispRevision revision) {
        String number = revision.getRevision().getNumber();
        int idx = number.lastIndexOf('.');
        if (idx == number.indexOf('.')) return null;
        int idx2 = number.lastIndexOf('.', idx - 1);
        String branchNumber = number.substring(0, idx2) + ".0" + number.substring(idx2, idx);
        List<LogInformation.SymName> names = revision.getRevision().getLogInfoHeader().getSymNamesForRevision(branchNumber);
        if (names.size() != 1) return null;
        return names.get(0).getName();
    }

    private static class HyperlinkLabel extends JLabel {

        public HyperlinkLabel() {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public void set(String text, Color foreground, Color background) {
            StringBuilder sb = new StringBuilder(100);
            if (foreground.equals(UIManager.getColor("List.foreground"))) { // NOI18N
                sb.append("<html><a href=\"\">"); // NOI18N
                sb.append(text);
                sb.append("</a>"); // NOI18N
            } else {
                sb.append("<html><a href=\"\" style=\"color:"); // NOI18N
                sb.append("rgb("); // NOI18N
                sb.append(foreground.getRed());
                sb.append(","); // NOI18N
                sb.append(foreground.getGreen());
                sb.append(","); // NOI18N
                sb.append(foreground.getBlue());
                sb.append(")"); // NOI18N
                sb.append("\">"); // NOI18N
                sb.append(text);
                sb.append("</a>"); // NOI18N
            }
            setText(sb.toString());
            setBackground(background);
        }
    }
}
