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

package org.netbeans.modules.php.project.ui;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.UIResource;
import org.openide.util.ChangeSupport;

// XXX should be replaced (?) by PhpEnvironment.DocumentRoot
/**
 * @author Tomas Mysik
 */
public class LocalServer implements Comparable<LocalServer> {

    private final String virtualHost;
    private final String url;
    private final String documentRoot;
    private final boolean editable;
    private String hint = " "; // NOI18N
    private String srcRoot;

    public LocalServer(final LocalServer localServer) {
        this(localServer.virtualHost, localServer.documentRoot, localServer.srcRoot, localServer.editable);
    }

    public LocalServer(String srcRoot) {
        this(null, null, srcRoot);
    }

    public LocalServer(String documentRoot, String srcRoot) {
        this(null, documentRoot, srcRoot);
    }

    public LocalServer(String virtualHost, String documentRoot, String srcRoot) {
        this(virtualHost, documentRoot, srcRoot, true);
    }

    public LocalServer(String virtualHost, String documentRoot, String srcRoot, boolean editable) {
        this(virtualHost, null, documentRoot, srcRoot, editable);
    }
    public LocalServer(String virtualHost, String url, String documentRoot, String srcRoot, boolean editable) {
        this.virtualHost = virtualHost;
        this.url = url;
        this.documentRoot = documentRoot;
        this.srcRoot = srcRoot;
        this.editable = editable;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public String getUrl() {
        return url;
    }

    public String getDocumentRoot() {
        return documentRoot;
    }

    public String getSrcRoot() {
        return srcRoot;
    }

    public void setSrcRoot(String srcRoot) {
        if (!editable) {
            throw new IllegalStateException("srcRoot cannot be changed because instance is not editable");
        }
        this.srcRoot = srcRoot;
    }

    public boolean isEditable() {
        return editable;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append("[virtualHost: "); // NOI18N
        sb.append(virtualHost);
        sb.append(", url: "); // NOI18N
        sb.append(url);
        sb.append(", documentRoot: "); // NOI18N
        sb.append(documentRoot);
        sb.append(", srcRoot: "); // NOI18N
        sb.append(srcRoot);
        sb.append(", hint: "); // NOI18N
        sb.append(hint);
        sb.append(", editable: "); // NOI18N
        sb.append(editable);
        sb.append("]"); // NOI18N
        return sb.toString();
    }

    public int compareTo(LocalServer ls) {
        if (!editable) {
            return -1;
        }
        return srcRoot.compareTo(ls.getSrcRoot());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LocalServer other = (LocalServer) obj;
        if (virtualHost != other.virtualHost && (virtualHost == null || !virtualHost.equals(other.virtualHost))) {
            return false;
        }
        if (documentRoot != other.documentRoot && (documentRoot == null || !documentRoot.equals(other.documentRoot))) {
            return false;
        }
        if (editable != other.editable) {
            return false;
        }
        if (srcRoot != other.srcRoot && (srcRoot == null || !srcRoot.equals(other.srcRoot))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (virtualHost != null ? virtualHost.hashCode() : 0);
        hash = 97 * hash + (documentRoot != null ? documentRoot.hashCode() : 0);
        hash = 97 * hash + (editable ? 1 : 0);
        hash = 97 * hash + (srcRoot != null ? srcRoot.hashCode() : 0);
        return hash;
    }

    public static class ComboBoxEditor implements javax.swing.ComboBoxEditor, UIResource, DocumentListener {

        private static final long serialVersionUID = -4527321803090719483L;
        private final JTextField component;
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private LocalServer activeItem = null;

        public ComboBoxEditor(JTextField editor) {
            super();

            component = editor;
            component.getDocument().addDocumentListener(this);
        }

        public Component getEditorComponent() {
            return component;
        }

        public void setItem(Object anObject) {
            if (anObject == null) {
                return;
            }
            assert anObject instanceof LocalServer;
            activeItem = (LocalServer) anObject;
            component.setText(activeItem.getSrcRoot());
        }

        public Object getItem() {
            return new LocalServer(activeItem);
        }

        public void selectAll() {
            component.selectAll();
            component.requestFocus();
        }

        public void addActionListener(ActionListener l) {
            component.addActionListener(l);
        }

        public void removeActionListener(ActionListener l) {
            component.removeActionListener(l);
        }

        /**
         * Add listener to the combobox changes.
         * @param l listener to add.
         */
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }

        /**
         * Remove listener from the combobox changes.
         * @param l listener to remove.
         */
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }

        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }

        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }

        private void processUpdate() {
            if (activeItem == null) {
                // no items set yet
                return;
            }
            boolean enabled = false;
            if (activeItem.isEditable()) {
                enabled = true;
                activeItem.setSrcRoot(component.getText().trim());
            }
            component.setEnabled(enabled);
            changeSupport.fireChange();
        }
    }

    public static class ComboBoxRenderer extends JLabel implements ListCellRenderer, UIResource {

        private static final long serialVersionUID = 31965318763243602L;

        public ComboBoxRenderer() {
            super();
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            assert value instanceof LocalServer;
            setName("ComboBox.listRenderer"); // NOI18N
            setText(((LocalServer) value).getSrcRoot());

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
    }

    public static class ComboBoxModel extends AbstractListModel implements MutableComboBoxModel {
        private static final long serialVersionUID = 194511142310432557L;

        private final List<LocalServer> data;
        private LocalServer selected = null;

        public ComboBoxModel(LocalServer... defaultLocalServers) {
            if (defaultLocalServers == null || defaultLocalServers.length == 0) {
                // prevent NPE
                defaultLocalServers = new LocalServer[] {new LocalServer("", "")}; // NOI18N
            }
            data = new ArrayList<LocalServer>(2 * defaultLocalServers.length);
            for (LocalServer localServer : defaultLocalServers) {
                data.add(localServer);
            }
            selected = data.get(0);
        }

        public int getSize() {
            return data.size();
        }

        public LocalServer getElementAt(int index) {
            return data.get(index);
        }

        public void addElement(Object object) {
            assert object instanceof LocalServer;
            LocalServer localServer = (LocalServer) object;
            if (!data.add(localServer)) {
                return;
            }
            Collections.sort(data);
            int idx = indexOf(localServer);
            fireIntervalAdded(this, idx, idx);
        }

        public void insertElementAt(Object object, int index) {
            assert object instanceof LocalServer;
            LocalServer localServer = (LocalServer) object;
            data.add(index, localServer);
            fireIntervalAdded(this, index, index);
        }

        public int indexOf(LocalServer configuration) {
            return data.indexOf(configuration);
        }

        public void removeElement(Object object) {
            assert object instanceof LocalServer;
            LocalServer localServer = (LocalServer) object;
            int idx = indexOf(localServer);
            if (idx == -1) {
                return;
            }
            boolean result = data.remove(localServer);
            assert result;
            fireIntervalRemoved(this, idx, idx);
        }

        public void removeElementAt(int index) {
            if (getElementAt(index) == selected) {
                if (index == 0) {
                    setSelectedItem(getSize() == 1 ? null : getElementAt(index + 1));
                } else {
                    setSelectedItem(getElementAt(index - 1));
                }
            }
            data.remove(index);
            fireIntervalRemoved(this, index, index);
        }

        public void setSelectedItem(Object object) {
            if ((selected != null && !selected.equals(object))
                    || selected == null && object != null) {
                assert object instanceof LocalServer;
                selected = (LocalServer) object;
                fireContentsChanged(this, -1, -1);
            }
        }

        public void fireContentsChanged() {
            fireContentsChanged(this, -1, -1);
        }

        public Object getSelectedItem() {
            return selected;
        }

        public List<LocalServer> getElements() {
            return Collections.unmodifiableList(data);
        }

        public void setElements(List<LocalServer> localServers) {
            int size = data.size();
            data.clear();
            if (size > 0) {
                fireIntervalRemoved(this, 0, size - 1);
            }
            if (localServers.size() > 0) {
                data.addAll(localServers);
                Collections.sort(data);
                fireIntervalAdded(this, 0, data.size() - 1);
            }
        }
    }
}
