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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
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
    final private Map<String, MultiUserChat> chats = new HashMap<String, MultiUserChat>();

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
            ProviderManager.getInstance().addExtensionProvider("delay", "urn:xmpp:delay", new DelayExtensionProvider());//NOI18N
            Kenai.getDefault().addPropertyChangeListener(instance);
        }
        return instance;
    }

    /**
     * private constructor to prevent mulitple instances
     */
    private KenaiConnection() {
    }

    synchronized void leave(String name) {
        listeners.remove(name);
    }

    synchronized void tryJoinChat(MultiUserChat chat) throws XMPPException {
        chat.join(getUserName());
    }

    private synchronized  MultiUserChat createChat(KenaiFeature prj) {
        MultiUserChat multiUserChat = new MultiUserChat(connection, getChatroomName(prj));
        chats.put(prj.getName(), multiUserChat);
        messageQueue.put(prj.getName(), new LinkedList<Message>());
        multiUserChat.addMessageListener(new MessageL());
        join(multiUserChat);
        return multiUserChat;
    }


    private void join(MultiUserChat chat) {
        try {
            chat.addParticipantListener(new PresenceListener());
            chat.join(getUserName());
        } catch (XMPPException ex) {
            XMPPLOG.log(Level.INFO, "Cannot join "  + chat.getRoom(), ex);
        }
    }

    /**
     * Adds listener to given MultiUserChat
     * only one listener can listen on given MultiUserChat
     * @param muc
     * @param lsn
     */
    public synchronized void join(MultiUserChat muc, PacketListener lsn) {
        final String name = StringUtils.parseName(muc.getRoom());
        PacketListener put = listeners.put(name, lsn);
        for (Message m : messageQueue.get(name)) {
            lsn.processPacket(m);
        }
        assert put == null:"Chat room " + name + " already joined";
    }

    /**
     * 
     */
    public synchronized void tryConnect()  {
        try {
            connect();
            initChats();
            PresenceIndicator.getDefault().setStatus(Status.ONLINE);
            isConnectionFailed = false;
        } catch (XMPPException ex) {
            isConnectionFailed = true;
        }
    }

    /**
     * is connection to kenai xmpp up and living?
     * @return
     */
    public boolean isConnected() {
        return connection != null && connection.isConnected() && connection.isAuthenticated();
    }

    private void connect() throws XMPPException {
        connection = Kenai.getDefault().getXMPPConnection();
        connection.addPacketListener(new PacketL(), new MessageTypeFilter(Type.chat));
    }

    public synchronized void reconnect(MultiUserChat muc) throws XMPPException {
        if (!connection.isConnected()) {
            connection.connect();
        }
        if (muc==null) {
            for (MultiUserChat m:getChats()) {
                if (!m.isJoined())
                    tryJoinChat(m);
            }
        } else if (!muc.isJoined())
            tryJoinChat(muc);
        isConnectionFailed=false;
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
            synchronized (KenaiConnection.this) {
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
                        if (chatNotifications.isEnabled(name) && (listener == null || !ChatTopComponent.isInitedAndVisible(name))) {
                            chatNotifications.addGroupMessage(msg);
                        } else {
                            chatNotifications.getMessagingHandle(name).notifyMessageReceived(msg);
                            chatNotifications.getMessagingHandle(name).notifyMessagesRead();
                        }
                    }
                });
            }
        }
    }

    private void initChats() {
        if (!connection.isConnected()) {
            return;
        }
        for (KenaiFeature prj : KenaiConnection.getDefault().getMyChats()) {
            try {
                createChat(prj);
            } catch (IllegalStateException ise) {
                Exceptions.printStackTrace(ise);
            }
        }
    }

    public synchronized List<MultiUserChat> getChats() {
        ArrayList<MultiUserChat> copy = new ArrayList<MultiUserChat>(chats.values());
        return copy;
    }

    /**
     * 
     * @param prj
     * @return
     */
    public synchronized MultiUserChat getChat(KenaiFeature prj) {
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
    public synchronized MultiUserChat getChat(String name) {
        return chats.get(name);
    }


    private RequestProcessor xmppProcessor = new RequestProcessor("XMPP Processor"); // NOI18N
    public RequestProcessor.Task post(Runnable run) {
        return xmppProcessor.post(run);
    }

   private boolean isConnectionFailed;
    public boolean isConnectionFailed() {
        return isConnectionFailed;
    }

    public void propertyChange(final PropertyChangeEvent e) {
        if (Kenai.PROP_LOGIN.equals(e.getPropertyName())) {
            if (e.getNewValue() != null) {
                post(new Runnable() {
                    public void run() {
                        synchronized(KenaiConnection.this) {
                            final PasswordAuthentication pa = (PasswordAuthentication) e.getNewValue();
                            USER = pa.getUserName();
                            PASSWORD = System.getProperty("kenai.xmpp.password", new String(pa.getPassword()));
                            tryConnect();
                        }
                    }
                });
            } else {
                try {
                    synchronized(KenaiConnection.this) {
                        for (MultiUserChat muc : getChats()) {
                            try {
                                muc.leave();
                            } catch (IllegalStateException ise) {
                                //we can ignore exceptions on logout
                                XMPPLOG.log(Level.FINE, null, ise);
                            }
                        }
                        chats.clear();
                        messageQueue.clear();
                        listeners.clear();
                    }
                    PresenceIndicator.getDefault().setStatus(Status.OFFLINE);
                    ChatNotifications.getDefault().clearAll();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

//------------------------------------------

    private String USER;
    private String PASSWORD;
    
    private static final String XMPP_SERVER = System.getProperty("kenai.com.url","https://kenai.com").substring(System.getProperty("kenai.com.url","https://kenai.com").lastIndexOf("/")+1);
    private static final String CHAT_ROOM = "@muc." + XMPP_SERVER; // NOI18N

    /**
     * TODO: should return kenai account name
     * @return
     */
    private String getUserName() {
        return USER;
    }


    private String getChatroomName(KenaiFeature prj) {
        return prj.getName() + CHAT_ROOM;
    }

    public Collection<KenaiFeature> getMyChats() {
        ArrayList myChats = new ArrayList();
        try {
            for (KenaiProject prj: Kenai.getDefault().getMyProjects()) {
                myChats.addAll(Arrays.asList(prj.getFeatures(KenaiService.Type.CHAT)));
            }
            return myChats;
        } catch (KenaiException ex) {
            throw new RuntimeException(ex);
        }
    }
}
