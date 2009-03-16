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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.PasswordAuthentication;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.collab.chat.PresenceIndicator.PresenceListener;
import org.netbeans.modules.kenai.collab.chat.PresenceIndicator.Status;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Class representing connection to kenai xmpp server
 * @author Jan Becicka
 */
public class KenaiConnection implements PropertyChangeListener {

    //Map <kenai project name, message listener>
    private HashMap<String, PacketListener> listeners = new HashMap<String, PacketListener>();
    private XMPPConnection connection;
    //Map <kenai project name, multi user chat>
    private HashMap<String, MultiUserChat> chats = new HashMap<String, MultiUserChat>();

    //singleton instance
    private static KenaiConnection instance;

    //just logger
    private static Logger XMPPLOG = Logger.getLogger(KenaiConnection.class.getName());

    // Map<name of kenai project, message queue>
    private HashMap<String, LinkedList<Message>> messageQueue = new HashMap<String, LinkedList<Message>>();

    private static ChatNotifications chatNotifications = ChatNotifications.getDefault();

    /**
     * Default singleton instance representing XMPP connection to kenai server
     * @return
     */
    public static synchronized KenaiConnection getDefault() {
        if (instance == null) {
            instance = new KenaiConnection();
            Kenai.getDefault().addPropertyChangeListener(instance);
        }
        return instance;
    }

    /**
     * private constructor to prevent mulitple instances
     */
    private KenaiConnection() {
    }

    void leave(String name) {
        listeners.remove(name);
    }

    private MultiUserChat createChat(KenaiProject prj) {
        MultiUserChat multiUserChat = new MultiUserChat(connection, getChatroomName(prj));
        chats.put(prj.getName(), multiUserChat);
        messageQueue.put(prj.getName(), new LinkedList<Message>());
        multiUserChat.addMessageListener(new MessageL());
        join(multiUserChat);
        return multiUserChat;
    }


    private void join(MultiUserChat chat) {
        try {
            chat.addParticipantListener(PresenceIndicator.getDefault().new PresenceListener());
            chat.join(getUserName());
        } catch (XMPPException ex) {
            XMPPLOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Adds listener to given MultiUserChat
     * only one listener can listen on given MultiUserChat
     * @param muc
     * @param lsn
     */
    public void join(MultiUserChat muc, PacketListener lsn) {
        final String name = StringUtils.parseName(muc.getRoom());
        PacketListener put = listeners.put(name, lsn);
        for (Message m : messageQueue.get(name)) {
            lsn.processPacket(m);
        }
        assert put == null;
    }

    private XMPPException xmppEx;

    /**
     * 
     */
    public synchronized void tryConnect() {
        try {
            connect();
            initChats();
            xmppEx = null;
            PresenceIndicator.getDefault().setStatus(Status.ONLINE);
        } catch (XMPPException ex) {
            xmppEx = ex;
        }
    }

    public XMPPException getXMPPException() {
        return xmppEx;
    }

    /**
     * is connection to kenai xmpp up and living?
     * @return
     */
    public boolean isConnected() {
        return connection != null && connection.isConnected() && connection.isAuthenticated();
    }

    private void connect() throws XMPPException {
        connection = new XMPPConnection(XMPP_SERVER);
        connection.connect();
        login();
        connection.addPacketListener(new PacketL(), new MessageTypeFilter(Type.chat));
    }

    private class PacketL implements PacketListener {
        public void processPacket(Packet packet) {
            final Message msg = (Message) packet;
            try {
                connection.getChatManager().createChat(msg.getFrom(), null).sendMessage(org.openide.util.NbBundle.getMessage(KenaiConnection.class, "CTL_NoPrivateMessages"));
                //chatNotifications.addPrivateMessage(msg);
            } catch (XMPPException ex) {
                Exceptions.printStackTrace(ex);
            }
            //chatNotifications.addPrivateMessage(msg);
        }
    }

    private class MessageL implements PacketListener {
        public void processPacket(Packet packet) {
            final Message msg = (Message) packet;
            final String name = StringUtils.parseName(msg.getFrom());
            final LinkedList<Message> thisQ = messageQueue.get(name);
            thisQ.add(msg);
            final PacketListener listener = listeners.get(name);
            if (listener != null) {
                listener.processPacket(msg);
            } 
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (listener == null || !ChatTopComponent.isInitedAndVisible(name)) {
                        chatNotifications.addGroupMessage(msg);
                    }
                }
            });
        }
    }

    private void login() throws XMPPException {
        connection.login(USER, PASSWORD);
    }

    private void initChats() {
        if (!connection.isConnected()) {
            return;
        }
        for (KenaiProject prj : KenaiConnection.getDefault().getMyProjects()) {
            createChat(prj);
        }
    }

    public Collection<MultiUserChat> getChats() {
        return chats.values();
    }

    /**
     * 
     * @param prj
     * @return
     */
    public MultiUserChat getChat(KenaiProject prj) {
        MultiUserChat multiUserChat = chats.get(prj.getName());
        if (multiUserChat==null) {
            multiUserChat=createChat(prj);
        }
        return multiUserChat;
    }

    /**
     *
     * @param name
     * @return
     */
    public MultiUserChat getChat(String name) {
        return chats.get(name);
    }


    public Roster getRoster() {
        return connection.getRoster();
    }

    private RequestProcessor xmppProcessor = new RequestProcessor("XMPP Processor"); // NOI18N
    public RequestProcessor.Task post(Runnable run) {
        return xmppProcessor.post(run);
    }

    public void propertyChange(final PropertyChangeEvent e) {
        if (Kenai.PROP_LOGIN.equals(e.getPropertyName())) {
            post(new Runnable() {
                public void run() {
                    final PasswordAuthentication pa = (PasswordAuthentication) e.getNewValue();
                    if (pa != null) {
                        USER = pa.getUserName();
                        PASSWORD = new String(pa.getPassword());
                        tryConnect();
                    } else {
                        for (MultiUserChat muc : getChats()) {
                            muc.leave();
                        }
                        chats.clear();
                        connection.disconnect();
                        messageQueue.clear();
                        listeners.clear();
                        PresenceIndicator.getDefault().setStatus(Status.OFFLINE);
                        ChatNotifications.getDefault().clearAll();
                    }
                }
            });
        }
    }
    
//------------------------------------------

    private String USER;
    private String PASSWORD;
    
    //TODO this should be removed when xmpp server starts working on kenai.com
    private static final String XMPP_SERVER = System.getProperty("kenai.xmpp.url","127.0.0.1");

    /**
     * TODO: should return kenai account name
     * @return
     */
    private String getUserName() {
        return USER;
    }


    private String getChatroomName(KenaiProject prj) {
         return prj.getFeatures(KenaiFeature.CHAT)[0].getName();
    }

    //TODO: my projects does not work so far
    public Collection<KenaiProject> getMyProjects() {
        try {
            return Kenai.getDefault().getMyProjects();
        } catch (KenaiException ex) {
            throw new RuntimeException(ex);
        }
    }
}
