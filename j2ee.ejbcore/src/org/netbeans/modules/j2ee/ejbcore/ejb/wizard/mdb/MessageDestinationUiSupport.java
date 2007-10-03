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

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.mdb;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import org.netbeans.modules.j2ee.common.EventRequestProcessor;
import org.netbeans.modules.j2ee.common.EventRequestProcessor.AsynchronousAction;
import org.netbeans.modules.j2ee.common.EventRequestProcessor.Context;
import org.netbeans.modules.j2ee.deployment.common.api.MessageDestination;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Support for MessageDestinationPanel class.
 * <p>
 * This class contains only static methods.
 * @author Tomas Mysik
 */
public abstract class MessageDestinationUiSupport {
    
    /**
     * Get module and server message destinations.
     * <p>
     * <b>Destinations are fetched asynchronously.</b>
     * @param j2eeModuleProvider 
     * @return holder with both module and server message destinations.
     */
    public static DestinationsHolder getDestinations(final J2eeModuleProvider j2eeModuleProvider) {
        assert j2eeModuleProvider != null;
        final DestinationsHolder holder = new DestinationsHolder();
        
        // fetch references & datasources asynchronously
        EventRequestProcessor.Action action = new AsynchronousAction() {

            public void run(Context actionContext) {
                String msg = NbBundle.getMessage(MessageDestinationUiSupport.class, "MSG_RetrievingDestinations");
                actionContext.getProgress().progress(msg);
                try {
                    holder.setModuleDestinations(j2eeModuleProvider.getConfigSupport().getMessageDestinations());
                    holder.setServerDestinations(j2eeModuleProvider.getConfigSupport().getServerMessageDestinations());
                } catch (ConfigurationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        
        Collection<EventRequestProcessor.Action> asyncActions = Collections.singleton(action);
        EventRequestProcessor erp = new EventRequestProcessor();
        erp.invoke(asyncActions);
        
        return holder;
    }
 
    /**
     * Populate given combo box with given message destinations.
     * @param destinations message destinations for given combo box.
     * @param comboBox combo box to populate.
     * @param selectedItem combo box item to be selected. If it's <code>null</code> no item selection is done.
     */
    public static void populateDestinations(final Set<MessageDestination> destinations, final JComboBox comboBox,
            final MessageDestination selectedItem) {
        assert destinations != null;
        assert comboBox != null;
        
        comboBox.setRenderer(new MessageDestinationListCellRenderer());
        
        List<MessageDestination> sortedDestinations = new ArrayList<MessageDestination>(destinations);
        Collections.sort(sortedDestinations, new MessageDestinationComparator());
        
        comboBox.removeAllItems();
        for (MessageDestination d : sortedDestinations) {
            comboBox.addItem(d);
        }
        
        // select item?
        if (selectedItem != null) {
            comboBox.setSelectedItem(selectedItem);
        }
    }
    
    /**
     * Open the dialog for adding message destination. Create and get created message destination.
     * @param j2eeModuleProvider Java EE module provider.
     * @param moduleDestinations module message destinations.
     * @param serverDestinations server message destinations.
     * @return created message destination or <code>null</code> if no message destination is created.
     */
    public static MessageDestination createMessageDestination(final J2eeModuleProvider j2eeModuleProvider,
            final Set<MessageDestination> moduleDestinations, final Set<MessageDestination> serverDestinations) {
        assert j2eeModuleProvider != null;
        assert moduleDestinations != null;
        assert serverDestinations != null;
        
        // message destination names - create map for faster searching
        Map<String, MessageDestination.Type> destinations = new HashMap<String, MessageDestination.Type>();
        for (MessageDestination md : moduleDestinations) {
            destinations.put(md.getName(), md.getType());
        }
        for (MessageDestination md : serverDestinations) {
            destinations.put(md.getName(), md.getType());
        }
        
        MessageDestinationPanel messageDestination = MessageDestinationPanel.newInstance(destinations);
        final DialogDescriptor dialogDescriptor = new DialogDescriptor(
                messageDestination,
                NbBundle.getMessage(MessageDestinationPanel.class, "LBL_AddMessageDestination"),
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(MessageDestinationPanel.class),
                null);
        // initial invalidation
        dialogDescriptor.setValid(false);
        messageDestination.addPropertyChangeListener(MessageDestinationPanel.IS_VALID,
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        Object newvalue = evt.getNewValue();
                        if ((newvalue != null) && (newvalue instanceof Boolean)) {
                            dialogDescriptor.setValid(((Boolean) newvalue).booleanValue());
                        }
                    }
                }
        
        );
        
        Object option = DialogDisplayer.getDefault().notify(dialogDescriptor);
        MessageDestination md = null;
        if (option == DialogDescriptor.OK_OPTION) {
            md = createMessageDestination(
                    j2eeModuleProvider,
                    messageDestination.getDestinationName(),
                    messageDestination.getDestinationType());
        }
        
        return md;
    }
    
    // this method has to be called asynchronously!
    private static MessageDestination createMessageDestination(final J2eeModuleProvider j2eeModuleProvider,
            final String destinationName, final MessageDestination.Type destinationType) {
        final MessageDestination[] messageDestinations = new MessageDestination[1];
        
        EventRequestProcessor.Action action = new EventRequestProcessor.AsynchronousAction() {
            public void run(Context actionContext) {
                String msg = NbBundle.getMessage(MessageDestinationUiSupport.class, "MSG_CreatingDestination");
                actionContext.getProgress().progress(msg);
                try {
                    messageDestinations[0] = 
                            j2eeModuleProvider.getConfigSupport().createMessageDestination(destinationName, destinationType);
                } catch (ConfigurationException ce) {
                    Exceptions.printStackTrace(ce);
                }
            }
        };
        
        Collection<EventRequestProcessor.Action> asyncActions = Collections.singleton(action);
        EventRequestProcessor erp = new EventRequestProcessor();
        erp.invoke(asyncActions);
        
        return messageDestinations[0];
    }
    
    /**
     * Holder for message destinations (module- and server-).
     */
    public static class DestinationsHolder {
        private Set<MessageDestination> moduleDestinations;
        private Set<MessageDestination> serverDestinations;
        
        public DestinationsHolder() {
        }

        public void setModuleDestinations(final Set<MessageDestination> moduleDestinations) {
            this.moduleDestinations = moduleDestinations;
        }

        public void setServerDestinations(final Set<MessageDestination> serverDestinations) {
            this.serverDestinations = serverDestinations;
        }

        public Set<MessageDestination> getModuleDestinations() {
            if (moduleDestinations == null) {
                moduleDestinations = new HashSet<MessageDestination>();
            }
            return moduleDestinations;
        }

        public Set<MessageDestination> getServerDestinations() {
            if (serverDestinations == null) {
                serverDestinations = new HashSet<MessageDestination>();
            }
            return serverDestinations;
        }
    }
    
    // optional - create factory method for this class
    private static class MessageDestinationComparator implements Comparator<MessageDestination> {
        
        public int compare(MessageDestination md1, MessageDestination md2) {
            
            if (md1 == null) {
                return md2 == null ? 0 : -1;
            }
            
            if (md2 == null) {
                return 1;
            }
            
            String destName1 = md1.getName();
            String destName2 = md2.getName();
            if (destName1 == null) {
                return destName2 == null ? 0 : -1;
            }
            
            return destName2 == null ? 1 : destName1.compareToIgnoreCase(destName2);
        }
    }
    
    // optional - create factory method for this class
    private static class MessageDestinationListCellRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            if (value instanceof MessageDestination) {
                MessageDestination md = (MessageDestination) value;
                setText(md.getName());
                // tooltip
                String type = MessageDestination.Type.QUEUE.equals(md.getType()) ? "LBL_Queue" : "LBL_Topic"; // NOI18N
                StringBuilder sb = new StringBuilder(md.getName());
                sb.append(" ["); // NOI18N
                sb.append(NbBundle.getMessage(MessageDestinationUiSupport.class, type));
                sb.append("]"); // NOI18N
                setToolTipText(sb.toString());
            } else {
                setText(value != null ? value.toString() : ""); // NOI18N
                setToolTipText(""); // NOI18N
            }
            return this;
        }
    }
}
