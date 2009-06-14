/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.card.loader;

import org.netbeans.modules.propdos.PropertiesAdapter;
import org.netbeans.modules.propdos.PropertiesBasedDataObject;
import org.netbeans.modules.propdos.ObservableProperties;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Image;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.javacard.Utils;
import org.netbeans.modules.javacard.api.*;
import org.netbeans.modules.javacard.constants.JCConstants;
import org.netbeans.modules.javacard.constants.JavacardDeviceKeyNames;
import org.netbeans.modules.javacard.platform.DevicePropertiesPanel;
import org.netbeans.modules.javacard.card.BrokenCard;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.actions.DeleteAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.InstanceContent;

import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import javax.swing.BorderFactory;
import org.netbeans.api.validation.adapters.DialogBuilder;
import org.netbeans.modules.javacard.platform.KeysAndValues;
import org.netbeans.spi.actions.Single;
import org.openide.DialogDescriptor;
import org.openide.actions.PropertiesAction;
import org.openide.loaders.DataNode;
import org.openide.nodes.PropertySupport;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

public class CardDataObject extends PropertiesBasedDataObject<Card> implements CardStateObserver {

    private static final String ICON_BASE = "org/netbeans/modules/javacard/resources/card.png"; //NOI18N
    private Reference<Card> cardRef;
    private Reference<ServerDataNode> nodeRef;
    private String platformName;
    private String myName;

    public CardDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader, Card.class);
        content.add(new StringBuilder("platform"), new PlatformConverter()); //NOI18N
        content.add(new CustomizerProvider());
        platformName = pf.getParent().getName();
        myName = pf.getName();
    }

    @Override
    protected Node createNodeDelegate() {
        ServerDataNode result = new ServerDataNode(this);
        nodeRef = new WeakReference<ServerDataNode>(result);
        return result;
    }

    @Override
    public boolean isDeleteAllowed() {
        return true;
    }

    @Override
    protected void onDelete(FileObject parentFolder) throws Exception {
        Card server = getLookup().lookup(Card.class);
        if (server != null && server.isRunning()) {
            server.stopServer();
        }
        File eepromfile = Utils.eepromFileForDevice(platformName, myName, false);
        if (eepromfile != null) {
            //Use FileObject so any views will be notified
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(eepromfile));
            if (fo != null) {
                fo.delete();
            }
        }
    }

    @Override
    protected void onReplaceObject() {
        Card old;
        synchronized (cardLock) {
            old = cardRef == null ? null : cardRef.get();
            cardRef = null;
        }
        if (old != null && old.getState().isRunning()) {
            old.stopServer();
        }
    }
    private final Object cardLock = new Object();

    @Override
    protected Card createFrom(ObservableProperties properties) {
        Card result = null;
        synchronized (cardLock) {
            if (cardRef != null) {
                result = cardRef.get();
                if (result != null) {
                    return result;
                }
            }
        }
        if (result == null) {
            JavacardPlatform platform = findPlatform();
            if (properties.isEmpty() || platform == null) {
                result = new BrokenCard(getName());
            } else {
                result = Card.create(platform, properties);
            }
        }
        assert result != null;
        result.addCardStateObserver(WeakListeners.create(
                CardStateObserver.class, this, result));
        synchronized (cardLock) {
            cardRef = new WeakReference<Card>(result);
        }
        ServerDataNode nd = nodeRef == null ? null : nodeRef.get();
        if (nd != null) {
            nd.checkForRunningStateChange();
        }
        return result;
    }

    public JavacardPlatform findPlatform() {
        DataObject ob = findPlatformDataObject();
        JavacardPlatform platform = ob == null ? null : ob.getLookup().lookup(JavacardPlatform.class);
        return platform;
    }

    private DataObject findPlatformDataObject() {
        FileObject fo = getPrimaryFile().getParent();
        String lookFor = fo.getName();
        //XXX don't iterate, just look for the right name - need to
        //convert in the case of javacard_default
        return Utils.findPlatformDataObjectNamed(fo.getName());
//        for (DataObject ob : Utils.findAllRegisteredJavacardPlatformDataObjects()) {
//            if (lookFor.equals(ob.getName())) {
//                return ob;
//            }
//        }
//        return null;
    }

    public void onStateChange(Card card, CardState old, CardState nue) {
        ServerDataNode nd = nodeRef == null ? null : nodeRef.get();
        if (nd != null) {
            nd.checkForRunningStateChange();
        }
    }

    @Override
    protected void propertyChanged(String propertyName, String newValue) {
        firePropertyChange(propertyName, null, newValue);
    }

    private class CustomizerProvider implements CardCustomizerProvider {

        public CardCustomizer getCardCustomizer() {
            return new CardCustomizerImpl();
        }
    }

    private class CardCustomizerImpl implements CardCustomizer {

        private final DevicePropertiesPanel pnl;

        CardCustomizerImpl() {
            assert EventQueue.isDispatchThread() : "Not on event thread";
            PropertiesAdapter adap = getLookup().lookup(PropertiesAdapter.class);
            pnl = new DevicePropertiesPanel(adap.asProperties());
            pnl.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 12));
        }

        public void save() {
            PropertiesAdapter adap = getLookup().lookup(PropertiesAdapter.class);
            pnl.write(new KeysAndValues.PropertiesAdapter(adap.asProperties()));
        }

        public ValidationGroup getValidationGroup() {
            return pnl.getValidationGroup();
        }

        public boolean isContentValid() {
            return pnl.isAllDataValid();
        }

        public Component getComponent() {
            return pnl;
        }
    }

    private class PlatformConverter implements InstanceContent.Convertor<StringBuilder, JavacardPlatform> {

        public JavacardPlatform convert(StringBuilder arg0) {
            return findPlatform();
        }

        public Class<? extends JavacardPlatform> type(StringBuilder arg0) {
            return JavacardPlatform.class;
        }

        public String id(StringBuilder arg0) {
            return "platform";
        }

        public String displayName(StringBuilder arg0) {
            return getName();
        }
    }

    final class ServerDataNode extends DataNode {
        private final ClearEpromAction clearEpromAction = new ClearEpromAction();

        ServerDataNode(CardDataObject ob) {
            super(ob, Children.LEAF, ob.getLookup());
            setName(ob.getName());
        }

        @Override
        public Image getIcon(int ignored) {
            Image result = ImageUtilities.loadImage(ICON_BASE);
            Card server = cardRef == null ? null : cardRef.get();
            if (server != null && server.isRunning()) {
                Image badge = ImageUtilities.loadImage(
                        "org/netbeans/modules/javacard/resources/running.png"); //NOI18N
                result = ImageUtilities.mergeImages(result, badge, 11, 11);
            }
            return result;
        }

        @Override
        protected Sheet createSheet() {
            Sheet sheet = super.createSheet();
            PropertiesBasedDataObject<?> ob = getLookup().lookup(PropertiesBasedDataObject.class);
            sheet.put(ob.getPropertiesAsPropertySet());
            Sheet.Set set = new Sheet.Set();
            set.setDisplayName(NbBundle.getMessage(ServerDataNode.class, "PROP_SET_OTHER"));
            set.setName(set.getDisplayName());
            set.put(new StateProp());
            sheet.put(set);
            return sheet;
        }

        @Override
        public Action getPreferredAction() {
            return new CustomCustomizeAction();
        }

        @Override
        public String getHtmlDisplayName() {
            Card card = getLookup().lookup(Card.class);

            if (card != null && !card.isValid()) {
                return "<font color='!nb.errorForeground'>" + //NOI18N
                        card.getId();
            }
            return null;
        }

        @Override
        public Image getOpenedIcon(int type) {
            return getIcon(type);
        }

        @Override
        public String getName() {
            DataObject dob = getLookup().lookup(DataObject.class);
            return dob.getName();
        }

        @Override
        public Action[] getActions(boolean context) {
            Action[] result = super.getActions(context);
            Action[] others = new Action[]{
                new StartServerAction(this),
                null,
                new RestartServerAction(this),
                new ResumeServerAction(this),
                null,
                new StopServerAction(this),
                clearEpromAction,
                null,
                SystemAction.get(DeleteAction.class),
                null,
                new CustomCustomizeAction(),
                SystemAction.get(PropertiesAction.class),};
            List<Action> l = new LinkedList<Action>(Arrays.asList(others));
            l.addAll(4, Arrays.asList(result));
            result = l.toArray(result);
            return result;
        }

        @Override
        public String getDisplayName() {
            DataObject dob = getLookup().lookup(DataObject.class);
            String result = null;
            if (dob != null) {
                if (JCConstants.TEMPLATE_DEFAULT_DEVICE_NAME.equals(dob.getName())) { //NOI18N
                    result = NbBundle.getMessage(CardDataObject.class,
                            "DEFAULT_DEVICE_NAME"); //NOI18N
                }
            } else {
                PropertiesAdapter p = getLookup().lookup(PropertiesAdapter.class);
                result = p.asProperties().getProperty(JavacardDeviceKeyNames.DEVICE_DISPLAY_NAME);
            }
            if (result == null) {
                result = dob.getName();
            }
            return result;
        }
        boolean checkingState;

        public void checkForRunningStateChange() {
            if (checkingState) {
                return;
            }
            checkingState = true;
            try {
                fireIconChange();
            } finally {
                checkingState = false;
            }
        }

        private class StateProp extends PropertySupport.ReadOnly<String> {

            StateProp() {
                super("state", String.class, NbBundle.getMessage(StateProp.class, //NOI18N
                        "PROP_STATE"), NbBundle.getMessage(StateProp.class, //NOI18N
                        "DESC_PROP_STATE")); //NOI18N

            }

            @Override
            public String getValue() throws IllegalAccessException, InvocationTargetException {
                Card card = getLookup().lookup(Card.class);
                return card.getState().toString();
            }
        }
    }

    private final class CustomCustomizeAction extends Single<JavacardPlatform> {

        CustomCustomizeAction() {
            super(JavacardPlatform.class, NbBundle.getMessage(CardDataObject.class,
                    "ACTION_CUSTOMIZE"), null); //NOI18N
        }

        @Override
        protected void actionPerformed(JavacardPlatform target) {
            String title = NbBundle.getMessage(CustomCustomizeAction.class,
                    "TTL_DEVICE_DIALOG", getName()); //NOI18N
            PropertiesAdapter adap = getLookup().lookup(PropertiesAdapter.class);
            final DevicePropertiesPanel inner = new DevicePropertiesPanel(adap.asProperties());
            DialogBuilder builder = new DialogBuilder(CardDataObject.class).setModal(true).
                    setTitle(title).
                    setContent(inner).
                    setValidationGroup(inner.getValidationGroup());

            if (builder.showDialog(DialogDescriptor.OK_OPTION)) {
                inner.write(new KeysAndValues.PropertiesAdapter(adap.asProperties()));
            }
        }
    }

    private final class ClearEpromAction extends Single<Card> {

        private volatile boolean enqueued;

        ClearEpromAction() {
            super(Card.class, NbBundle.getMessage(ClearEpromAction.class,
                    "CLEAR_EPROM"), null); //NOI18N
        }

        @Override
        protected void actionPerformed(final Card target) {
            JavacardPlatform p = findPlatform();
            if (p != null) {
                FileObject fld = Utils.sfsFolderForDeviceEepromsForPlatformNamed(p.getSystemName(), false);
                if (fld != null) {
                    final FileObject fo = fld.getFileObject(target.getDisplayName(), JCConstants.EEPROM_FILE_EXTENSION);
                    if (fo != null) {
                        //Do this in the background so we don't block the EQ waiting
                        //for the server process to exit
                        enqueued = true;
                        RequestProcessor.getDefault().post(new Runnable() {
                            public void run() {
                                try {
                                    if (target.isRunning()) {
                                        target.stopServer();
                                    }
                                    fo.delete();
                                } catch (IOException ex) {
                                    Exceptions.printStackTrace(ex);
                                } finally {
                                    enqueued = false;
                                }
                            }
                        });
                    } else {
                        System.err.println("Could not find eprom file");
                    }
                } else {
                    System.err.println("Could not find eprom folder");
                }
            } else {
                System.err.println("could not find platform");
            }
        }

        @Override
        protected boolean isEnabled(Card target) {
            if (enqueued) {
                return false;
            }
            boolean result = super.isEnabled(target);
            if (result) {
                JavacardPlatform p = findPlatform();
                if (p != null) {
                    return Utils.sfsFolderForDeviceEepromsForPlatformNamed(p.getSystemName(), false) != null;
                }
            }
            return false;
        }

        public void run() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
