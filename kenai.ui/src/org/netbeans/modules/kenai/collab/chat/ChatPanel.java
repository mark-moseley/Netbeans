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
package org.netbeans.modules.kenai.collab.chat;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Random;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Panel representing single ChatRoom
 * @author Jan Becicka
 */
public class ChatPanel extends javax.swing.JPanel {

    private MultiUserChat muc;
    private boolean disableAutoScroll = false;
    private final HTMLEditorKit editorKit;
    private static final String[][] smileysMap = new String[][] {
        {"8)", "cool"}, // NOI18N
        {"8-)", "cool"}, // NOI18N
        {":]", "grin"}, // NOI18N
        {":-]", "grin"}, // NOI18N
        {":D", "laughing"}, // NOI18N
        {":-D", "laughing"}, // NOI18N
        {":(", "sad"}, // NOI18N
        {":-(", "sad"}, // NOI18N
        {":)", "smiley"}, // NOI18N
        {":-)", "smiley"}, // NOI18N
        {";)", "wink"}, // NOI18N
        {";-)", "wink"} // NOI18N
    };
    private CompoundUndoManager undo;
    private MessageHistoryManager history = new MessageHistoryManager();
    
    public ChatPanel(MultiUserChat chat) {
        this.muc=chat;
        initComponents();
        setName(StringUtils.parseName(chat.getRoom()));
        editorKit= (HTMLEditorKit) inbox.getEditorKit();

        Font font = UIManager.getFont("Label.font"); // NOI18N
        String bodyRule = "body { font-family: " + font.getFamily() + "; " + // NOI18N
                "font-size: " + font.getSize() + "pt; }"; // NOI18N
        final StyleSheet styleSheet = ((HTMLDocument) inbox.getDocument()).getStyleSheet();

        styleSheet.addRule(bodyRule);
        styleSheet.addRule(".buddy {color: black; font-weight: bold; padding: 4px;}"); // NOI18N
        styleSheet.addRule(".time {color: lightgrey; padding: 4px;"); // NOI18N
        styleSheet.addRule(".message {color: lightgrey; padding: 2px 4px;"); // NOI18N
        styleSheet.addRule(".date {color: #cc9922; padding: 7px 0;"); // NOI18N


//        users.setCellRenderer(new BuddyListCellRenderer());
//        users.setModel(new BuddyListModel(chat));
//        users.setModel(new BuddyListModel(ctrl.getRoster()));
//        chat.addParticipantListener(getBuddyListModel());
        MessagingHandleImpl handle = ChatNotifications.getDefault().getMessagingHandle(getName());
        handle.addPropertyChangeListener(new PresenceListener());
        KenaiConnection.getDefault().join(chat,new ChatListener());
        //KenaiConnection.getDefault().join(chat);
        inbox.setBackground(Color.WHITE);
        inbox.addHyperlinkListener(new HyperlinkListener() {

            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    URLDisplayer.getDefault().showURL(e.getURL());
                }
            }
        });
        outbox.setBackground(Color.WHITE);
        splitter.setResizeWeight(0.9);
        refreshOnlineStatus();
        NotificationsEnabledAction bubbleEnabled = new NotificationsEnabledAction();
        inbox.addMouseListener(bubbleEnabled);
        outbox.addMouseListener(bubbleEnabled);
        undo = new CompoundUndoManager(outbox);

        inboxScrollPane.addMouseWheelListener(new MouseWheelListener() {

            public void mouseWheelMoved(MouseWheelEvent e) {
                JScrollBar vbar = inboxScrollPane.getVerticalScrollBar();
                if (vbar==null)
                    return;
                disableAutoScroll = ((vbar.getValue() + vbar.getVisibleAmount()) != vbar.getMaximum());
            }
        });
        inboxScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

            public void adjustmentValueChanged(AdjustmentEvent event) {
                JScrollBar vbar = (JScrollBar) event.getSource();

                if (!event.getValueIsAdjusting()) {
                    return;
                }
                disableAutoScroll = ((vbar.getValue() + vbar.getVisibleAmount()) != vbar.getMaximum());
            }
        });

//        setUpPrivateMessages();
    }

    private class NotificationsEnabledAction extends MouseAdapter implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JCheckBoxMenuItem m = (JCheckBoxMenuItem) e.getSource();
            ChatNotifications.getDefault().setEnabled(getName(),m.getState());
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                try {
                    JPopupMenu menu = new JPopupMenu();
                    String name = Kenai.getDefault().getProject(getName()).getDisplayName();
                    JCheckBoxMenuItem jCheckBoxMenuItem = new JCheckBoxMenuItem(
                            NbBundle.getMessage(ChatPanel.class, "CTL_NotificationsFor", new Object[]{name}),
                            ChatNotifications.getDefault().isEnabled(getName()));
                    jCheckBoxMenuItem.addActionListener(this);
                    menu.add(jCheckBoxMenuItem);
                    menu.show((Component) e.getSource(), e.getX(), e.getY());
                } catch (KenaiException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        outbox.requestFocus();
    }

    private String removeTags(String body) {
        String tmp = body;
        tmp.replaceAll("\r\n", "\n"); // NOI18N
        tmp.replaceAll("\r", "\n"); // NOI18N
        return tmp.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br>"); // NOI18N
    }

    private String replaceLinks(String body) {
        // This regexp works quite nice, should be OK in most cases (does not handle [.,?!] in the end of the URL)
        return body.replaceAll("(http|https|ftp)://([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,4}(/[^ ]*)*", "<a href=\"$0\">$0</a>"); //NOI18N
    }

    private String replaceSmileys(String body) {
        if (body.matches(".*[8:;]-?[]D()].*")) { // NOI18N
            for (int i = 0; i < smileysMap.length; i++) {
                body = body.replace(smileysMap[i][0],
                        "<img align=\"center\" src=\"" + // NOI18N
                        this.getClass().getResource("/org/netbeans/modules/kenai/collab/resources/emo_" + smileysMap[i][1] + "16.png") +
                        "\"></img>"); // NOI18N
            }
        }
        return body;
    }

//    void setUpPrivateMessages() {
//
//        final JPopupMenu popupMenu = new JPopupMenu();
//        popupMenu.add(new SendPrivateMessage());
//
//        users.addMouseListener(new MouseAdapter() {
//
//            @Override
//            public void mousePressed(MouseEvent me) {
//                processMouseEvent(me);
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                processMouseEvent(e);
//            }
//
//            private void processMouseEvent(MouseEvent me) {
//                if (me.isPopupTrigger()) {
//                    users.setSelectedIndex(users.locationToIndex(me.getPoint()));
//                    popupMenu.show(users, me.getX(), me.getY());
//                }
//            }
//        });
//    }
//
//    private class SendPrivateMessage extends AbstractAction {
//
//        public SendPrivateMessage() {
//            super("Send Private Message");
//        }
//
//        public void actionPerformed(ActionEvent e) {
//            Buddy b = (Buddy) users.getModel().getElementAt(users.getSelectedIndex());
//            try {
//                JEditorPane pane = new JEditorPane();
//                JScrollPane scrollPane = new JScrollPane(pane);
//                DialogDescriptor sendMessage = new DialogDescriptor(scrollPane, "Send private message to " + b.getLabel());
//                DialogDisplayer.getDefault().createDialog(sendMessage).setVisible(true);
//                if (sendMessage.getValue()==DialogDescriptor.OK_OPTION) {
//                    muc.createPrivateChat(b.getJid(), null).sendMessage(pane.getText());
//                }
//            } catch (XMPPException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
//
//    }

    private class ChatListener implements PacketListener {

        public void processPacket(Packet packet) {
            final Message message = (Message) packet;
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    setEndSelection();
                    insertMessage(message);
                    if (!ChatPanel.this.isVisible()) {
                        ChatTopComponent.findInstance().setModified(ChatPanel.this);
                    }
                }
            });
        }
    }

    private void refreshOnlineStatus() throws MissingResourceException {
        online.setText(NbBundle.getMessage(ChatPanel.class, "CTL_PresenceOnline", muc.getOccupantsCount()));
        Iterator<String> string = muc.getOccupants();
        StringBuffer buffer = new StringBuffer();
        buffer.append("<html><body>"); // NOI18N
        while (string.hasNext()) {
            buffer.append(StringUtils.parseResource(string.next()) + "<br>"); // NOI18N
        }
        buffer.append("</body></html>"); // NOI18N
        online.setToolTipText(buffer.toString());
    //setEndSelection();
    //insertPresence(presence);
    }

    private class PresenceListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent arg0) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    refreshOnlineStatus();
                }
            });

        }
    }


//    private BuddyListModel getBuddyListModel() {
//        return (BuddyListModel) users.getModel();
//    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitter = new javax.swing.JSplitPane();
        outboxScrollPane = new javax.swing.JScrollPane();
        outbox = new javax.swing.JTextPane();
        inboxPanel = new javax.swing.JPanel();
        inboxScrollPane = new javax.swing.JScrollPane();
        inbox = new JTextPane() {
            public void scrollRectToVisible(Rectangle aRect) {
                if (!disableAutoScroll)
                super.scrollRectToVisible(aRect);
            }
        };
        online = new javax.swing.JLabel();

        splitter.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        outboxScrollPane.setBorder(null);

        outbox.setBorder(null);
        outbox.setMaximumSize(new java.awt.Dimension(0, 16));
        outbox.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                outboxKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                ChatPanel.this.keyTyped(evt);
            }
        });
        outboxScrollPane.setViewportView(outbox);

        splitter.setRightComponent(outboxScrollPane);

        inboxPanel.setBackground(java.awt.Color.white);
        inboxPanel.setLayout(new java.awt.BorderLayout());

        inboxScrollPane.setBorder(null);
        inboxScrollPane.setViewportBorder(null);

        inbox.setBorder(null);
        inbox.setContentType("text/html"); // NOI18N
        inbox.setEditable(false);
        inbox.setText(org.openide.util.NbBundle.getMessage(ChatPanel.class, "ChatPanel.inbox.text", new Object[] {})); // NOI18N
        inboxScrollPane.setViewportView(inbox);

        inboxPanel.add(inboxScrollPane, java.awt.BorderLayout.CENTER);

        online.setBackground(java.awt.Color.white);
        online.setForeground(java.awt.Color.blue);
        online.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        online.setText(org.openide.util.NbBundle.getMessage(ChatPanel.class, "ChatPanel.online.text", new Object[] {})); // NOI18N
        online.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 1, 3, 1));
        inboxPanel.add(online, java.awt.BorderLayout.PAGE_START);

        splitter.setTopComponent(inboxPanel);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(splitter, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(splitter, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    List undoCharsList = Arrays.asList(' ', '.', ',', '!', '\t', '?', ':', ';');

    private void keyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_keyTyped
        if (undoCharsList.contains(evt.getKeyChar())) { // undo state when one of special chars...
            undo.startNewCompoundEdit();
            return;
        }
        if (evt.getKeyChar() == '\n' || evt.getKeyChar() == '\r') {
            if (evt.isAltDown() || evt.isShiftDown() || evt.isControlDown()) {
                try {
                    outbox.getStyledDocument().insertString(outbox.getCaretPosition(), "\r\n", null); //NOI18N
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return;
            }
            try {
                if (!KenaiConnection.getDefault().isConnected() || !muc.isJoined()) {
                    try {
                        KenaiConnection.getDefault().reconnect(muc);
                    } catch (XMPPException xMPPException) {
                        JOptionPane.showMessageDialog(this, xMPPException.getMessage());
                        return;
                    }
                }
                if (!outbox.getText().trim().equals("")) {
                    //remove NL if before the caret...
                    int pos = outbox.getCaretPosition();
                    if (pos > 1 && (outbox.getText().charAt(pos - 1) == '\n' || outbox.getText().charAt(pos - 1) == '\r'))  {
                        try {
                            boolean tryRemoveR = outbox.getText().charAt(pos - 1) == '\n'; // it can be \r\n, \n to be removed
                            outbox.getDocument().remove(pos - 1, 1);
                            pos = outbox.getCaretPosition();
                            if (tryRemoveR && pos > 1 && outbox.getText().charAt(pos - 1) == '\r')  {
                                outbox.getDocument().remove(pos - 1, 1);
                            }
                        } catch (BadLocationException ex) {
                            // harmless
                        }
                    }
                    muc.sendMessage(outbox.getText().trim());
                }
            } catch (XMPPException ex) {
                Exceptions.printStackTrace(ex);
            }
            outbox.setText("");
            undo.discardAllEdits();
            history.resetHistory();
        }
    }//GEN-LAST:event_keyTyped

    private void outboxKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_outboxKeyPressed
        if (evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_Z) {
            try {
                undo.undo();
            } catch (CannotUndoException e) {
                // end of the undo history
            }
            return;
        }
        if (evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_Y) {
            try {
                undo.redo();
            } catch (CannotRedoException e) {
                // end of the redo history
            }
            return;
        }
        if (evt.isControlDown() || evt.isAltDown() || evt.isShiftDown()) {
            if (evt.getKeyCode() == KeyEvent.VK_UP) {
                if (history.isOnStart()) {
                    history.setEditedMessage(outbox.getText());
                }
                String mess = history.getPreviousMessage();
                if (mess != null) {
                    outbox.setText(mess);
                }
                return;
            }
            if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                String message = history.getNextMessage();
                assert message != null;
                outbox.setText(message);
                return;
            }
        }
    }//GEN-LAST:event_outboxKeyPressed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane inbox;
    private javax.swing.JPanel inboxPanel;
    private javax.swing.JScrollPane inboxScrollPane;
    private javax.swing.JLabel online;
    private javax.swing.JTextPane outbox;
    private javax.swing.JScrollPane outboxScrollPane;
    private javax.swing.JSplitPane splitter;
    // End of variables declaration//GEN-END:variables


    private Date lastDatePrinted;
    private Date lastMessageDate;
    private String lastNickPrinted = null;
    private String rgb = null;

    protected void insertMessage(Message message) {
        try {
            HTMLDocument doc = (HTMLDocument) inbox.getStyledDocument();
            final Date timestamp = getTimestamp(message);
            String fromRes = StringUtils.parseResource(message.getFrom());
            history.addMessage(message.getBody()); //Store the message to the history
            Random random = new Random(fromRes.hashCode());
            float randNum = random.nextFloat();
            Color headerColor = Color.getHSBColor(randNum, 0.1F, 0.95F);
            Color messageColor = Color.getHSBColor(randNum, 0.1F, 1.0F);
            boolean printheader = ((lastNickPrinted != null)?(!lastNickPrinted.equals(fromRes)):true); //Nickname is different from the last one, or...
            printheader |= (lastMessageDate != null && timestamp != null)?(timestamp.getTime() > lastMessageDate.getTime() + 120000):true;
            lastNickPrinted = fromRes;
            lastMessageDate = timestamp;
            if (!isSameDate(lastDatePrinted,timestamp)) {
                lastDatePrinted = timestamp;
                printheader = true;
                rgb = null;
                String d = "<table border=\"0\" borderwith=\"0\" width=\"100%\"><tbody><tr><td class=\"date\" align=\"left\">" + // NOI18N
                    (isToday(timestamp)?NbBundle.getMessage(ChatPanel.class, "LBL_Today"):DateFormat.getDateInstance().format(timestamp)) + "</td><td class=\"date\" align=\"right\">" + // NOI18N
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(timestamp) + "</td></tr></tbody></table>"; // NOI18N
                editorKit.insertHTML(doc, doc.getLength(), d, 0, 0, null);
            }
            String text = "";
            if (printheader) {
                if (rgb != null) {
                    text += "<div style=\"height: 3px; background-color: rgb(" + rgb + ")\"></div>"; // NOI18N
                }
                text += "<table border=\"0\" borderwith=\"0\" width=\"100%\"><tbody>" + //NOI18N
                        "<tr style=\"background-color: rgb(" + headerColor.getRed() + "," + headerColor.getGreen() + "," + headerColor.getBlue() + ")\">" + //NOI18N
                        "<td class=\"buddy\" align=\"left\">"+ fromRes + "</td><td class=\"time\" align=\"right\">" + // NOI18N
                        DateFormat.getTimeInstance(DateFormat.SHORT).format(getTimestamp(message)) + "</td></tr></tbody></table>"; // NOI18N
            }
            rgb = messageColor.getRed() + "," + messageColor.getGreen() + "," + messageColor.getBlue(); // NOI18N
            text += "<div class=\"message\" style=\"background-color: rgb(" + rgb + ")\">" + replaceSmileys(replaceLinks(removeTags(message.getBody()))) + "</div>"; // NOI18N

            editorKit.insertHTML(doc, doc.getLength(), text, 0, 0, null);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public static Date getTimestamp(Packet packet) {
         DelayInformation delay = (DelayInformation) packet.getExtension("x", "jabber:x:delay"); // NOI18N
         if (delay != null) {
             return delay.getStamp();
         } else {
             //this is realtime message
             return new Date();
         }
    }

    boolean isToday(Date date) {
        return isSameDate(date, new Date());
    }

    boolean isSameDate(Date date, Date date2) {
        if (date==null) {
            return false;
        }
        return date.getDate() == date2.getDate() && date.getMonth() == date2.getMonth() && date.getYear() == date2.getYear();
    }


    protected void insertPresence(Presence presence) {
        try {
            HTMLDocument doc = (HTMLDocument) inbox.getStyledDocument();
            String text = "<i><b>" + StringUtils.parseResource(presence.getFrom()) + "</b> is now " + presence.getType() + "</i>"; // NOI18N
            editorKit.insertHTML(doc, doc.getLength(), text, 0, 0, null);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }


    // Needed for inserting icons in the right places
    protected void setEndSelection() {
        inbox.setSelectionStart(inbox.getDocument().getLength());
        inbox.setSelectionEnd(inbox.getDocument().getLength());
        }

//    void setUsersListVisible(boolean visible) {
//        usersScrollPane.setVisible(visible);
//    }
}
