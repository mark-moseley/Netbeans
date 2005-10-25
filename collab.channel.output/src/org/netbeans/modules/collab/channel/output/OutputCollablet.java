/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.output;

import java.awt.Image;
import java.beans.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import org.w3c.dom.*;
import org.apache.xmlbeans.*;
import org.openide.util.*;
import org.openide.windows.*;

import com.sun.collablet.CollabMessage;
import com.sun.collablet.Conversation;
import com.sun.collablet.moxc.*;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.ns.shareOutput.x10.*;

public class OutputCollablet extends MOXCCollablet {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static int activeChannelCount;

    //	private static final String OUTPUT_URI=
    //		"urn:com.sun.tools.ide.collab.channel.output/1";
    private static final String OUTPUT_URI = "http://www.netbeans.org/ns/share-output/1_0"; // NOI18N

    //	private static final String PREFIX="share-output"; // NOI18N
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private Conversation conversation;
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private Map remoteOutputIOMap = new HashMap();

    /**
     *
     *
     */
    public OutputCollablet(Conversation conversation) {
        super(conversation);
        activeChannelCount++;
    }

    /**
     *
     *
     */
    public String getDisplayName() {
        return NbBundle.getMessage(OutputCollablet.class, "LBL_OutputChannel_DisplayName");
    }

    /**
     *
     *
     */
    public Icon getIcon() {
        Image image = Utilities.loadImage("org/netbeans/core/resources/frames/output.gif"); //NOI18N;

        if (image != null) {
            return new ImageIcon(image);
        }

        return null;
    }

    /**
     *
     *
     */
    public void close() {
        activeChannelCount--;

        // close remote output tabs
        Set keys = remoteOutputIOMap.keySet();
        Iterator it = keys.iterator();

        while (it.hasNext()) {
            String key = (String) it.next();
            ((InputOutput) remoteOutputIOMap.get(key)).closeInputOutput();

        }
    }


    ////////////////////////////////////////////////////////////////////////////
    // Message handling methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void shareAntOutput(String tabName, String id, String contentString) {
        try {
            OpenDocument openDoc = OpenDocument.Factory.newInstance();
            Open open = openDoc.addNewOpen();
            Content content = open.addNewContent();
            content.setContentType("plain/text");
            content.setStringValue(Base64.encode(contentString.getBytes()));
            open.setInstance(id);
            open.setName(Base64.encode(tabName.getBytes()));
            open.setContent(content);

            HashMap suggestedPrefixes = new HashMap();

            //			suggestedPrefixes.put(SOAP_URI, "soap"); // NOI18N
            //			suggestedPrefixes.put(MOXC_URI, "moxc"); // NOI18N
            suggestedPrefixes.put(OUTPUT_URI, "share-output"); // NOI18N

            XmlOptions opts = new XmlOptions();
            opts.setSavePrettyPrint();
            opts.setSavePrettyPrintIndent(4);
            opts.setCharacterEncoding("UTF-8"); // NOI18N
            opts.setSaveSuggestedPrefixes(suggestedPrefixes);
            opts.setSaveOuter();

            String xmlString = openDoc.xmlText(opts);

            String moxcMsg = constructMOXCMessage(xmlString);
            String sendMsg = new String(moxcMsg.getBytes(), "UTF-8"); // NOI18N
            CollabMessage collabMessage = getConversation().createMessage();
            collabMessage.setContent(moxcMsg);
            Debug.out.println(" send out xml msg: " + moxcMsg);

            // Set a header to ensure the chat channel doesn't pick this up
            //			collabMessage.setHeader("x-channel","not-chat"); // NOI18N
            getConversation().sendMessage(collabMessage);
        } catch (Exception e) {
            Debug.debugNotify(e);
        }
    }

    /**
     *        construct soap message
     *
     */
    private String constructMOXCMessage(String body) {
        // temp, as moxc schema will change
        StringBuffer buffer = new StringBuffer();

        buffer.append("<soap:Envelope xmlns:soap=");
        buffer.append("\"");
        buffer.append(MOXCConstants.SOAP_URI);
        buffer.append("\" ");
        buffer.append("xmlns:moxc=");
        buffer.append("\"");
        buffer.append(MOXCConstants.MOXC_URI);
        buffer.append("\" ");
        buffer.append(">");
        buffer.append("<soap:Header>");
        buffer.append("<moxc:message>");
        buffer.append("<moxc:sender>");
        buffer.append(getPrincipalID());
        buffer.append("</moxc:sender>");
        buffer.append("<moxc:conversation>");
        buffer.append(getConversation().getIdentifier());
        buffer.append("</moxc:conversation>");
        buffer.append("<moxc:channel>");
        buffer.append(OUTPUT_URI);
        buffer.append("</moxc:channel>");
        buffer.append("<moxc:instance>1</moxc:instance>");
        buffer.append("</moxc:message></soap:Header><soap:Body>");
        buffer.append(body);
        buffer.append("</soap:Body></soap:Envelope>");

        return buffer.toString();
    }

    /**
     *
     *
     */
    public String getPrincipalID() {
        String principalId = getConversation().getCollabSession().getUserPrincipal().getIdentifier();

        if (principalId.indexOf("@") != -1) {
            principalId = principalId.substring(0, principalId.indexOf("@"));
        }

        return principalId;
    }

    /**
     *
     *
     */
    public void handleOpenOutput(String outputName, String instance_id, String content) {
        if (remoteOutputIOMap.get(instance_id) != null) {
            try {
                InputOutput io = (InputOutput) remoteOutputIOMap.get(instance_id);
                io.getOut().reset();
                io.getOut().print(content);
                io.getOut().flush();
                io.select();
            } catch (Exception e) {
                Debug.debugNotify(e);
            }
        } else {
            InputOutput inputOutput = IOProvider.getDefault().getIO(outputName, true);
            PrintWriter out = inputOutput.getOut();
            out.print(content);
            out.flush();

            remoteOutputIOMap.put(instance_id, inputOutput);
            inputOutput.select();
        }
    }



    ////////////////////////////////////////////////////////////////////////////
    // Property change support
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public void propertyChange(PropertyChangeEvent event) {
    }

    /**
     *
     *
     */
    protected PropertyChangeSupport getChangeSupport() {
        return changeSupport;
    }

    /**
         *
         *
         */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getChangeSupport().addPropertyChangeListener(listener);
    }

    /**
         *
         *
         */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        getChangeSupport().removePropertyChangeListener(listener);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Utility methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the number of active channels; used to determine if there is
     * state that needs to be stored
     *
     */
    protected static int getActiveChannelCount() {
        return activeChannelCount;
    }

    /**
     * strip out html tag in tab name
     *
     */
    private String getTabName(String name) {
        String tabName = name;

        if (name.indexOf("<html>") != -1) // NOI18N
         {
            tabName = tabName.replaceAll("<html>", ""); // NOI18N
            tabName = tabName.replaceAll("</html>", ""); // NOI18N
        }

        if (name.indexOf("<b>") != -1) // NOI18N
         {
            tabName = tabName.replaceAll("<b>", ""); // NOI18N
            tabName = tabName.replaceAll("</b>", ""); // NOI18N
        }

        if (name.indexOf("&nbsp") != -1) // NOI18N
         {
            tabName = tabName.replaceAll("&nbsp;", ""); // NOI18N
        }

        return tabName;
    }

    public String[] getNamespaces() {
        String[] result = new String[1];
        result[0] = OUTPUT_URI;

        return result;
    }

    public boolean handleMOXCMessage(MOXCMessage message) {
        try {
            String sender = message.getSender();
            Element element = message.getMessageElement();
            OpenDocument openDoc = OpenDocument.Factory.parse(element);
            Open open = openDoc.getOpen();
            String tabName = open.getName();
            Content content = open.getContent();

            String outputName = new String(Base64.decode(open.getName()));
            String outputContent = new String(Base64.decode(open.getContent().getStringValue()));
            String instance_id = open.getInstance();
            outputName = outputName + " [" + sender + "]";

            handleOpenOutput(outputName, instance_id, outputContent);

            return true;
        } catch (XmlException e) {
            Debug.errorManager.notify(e);

            return false;
        }
    }
}
