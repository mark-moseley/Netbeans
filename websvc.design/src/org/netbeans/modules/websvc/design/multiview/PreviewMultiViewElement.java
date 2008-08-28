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
package org.netbeans.modules.websvc.design.multiview;

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.text.Document;
import org.openide.awt.UndoRedo;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.websvc.design.javamodel.ServiceModel;
import org.openide.awt.Toolbar;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.DataEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Pospisil
 */
public class PreviewMultiViewElement extends CloneableEditor
        implements MultiViewElement {

    private static final long serialVersionUID = 13L;
    private transient DataObject dataObject;
    private transient DataEditorSupport des;
    private transient Lookup myLookup;
    private transient MultiViewElementCallback multiViewCallback;
    private transient JToolBar toolbar;
    private ServiceModel serviceModel;

    public PreviewMultiViewElement() {
        super(null);
    }
    //private transient DesignView designView;

    public PreviewMultiViewElement(DataEditorSupport des) {
        super(des);
        this.des = des;
        this.dataObject = this.des.getDataObject();
        initialize();
    }

    private void initialize() {
//        removeAll();
//        setLayout(new BorderLayout());
//        // Add listener to status of java source, to detect changes and need to regenerate wsdl
//        final MultiViewSupport mvSupport = dataObject.getCookie(MultiViewSupport.class);
//        this.serviceModel = ServiceModel.getServiceModel(mvSupport.getImplementationBean());
//
//        if (mvSupport != null && mvSupport.getService() != null) {
//            serviceModel.addServiceChangeListener(new ServiceChangeListener() {
//
//                public void propertyChanged(String propertyName, String oldValue, String newValue) {
//                    try {
//                        mvSupport.regenerateWSDL();
//                    } catch (IllegalArgumentException ex) {
//                        Exceptions.printStackTrace(ex);
//                    } catch (IOException ex) {
//                        Exceptions.printStackTrace(ex);
//                    }
//                }
//
//                public void operationAdded(MethodModel method) {
//                }
//
//                public void operationRemoved(MethodModel method) {
//                }
//
//                public void operationChanged(MethodModel oldMethod, MethodModel newMethod) {
//                }
//            });
//        }

        if (des != null) {

            myLookup = Lookups.fixed(dataObject, des);
        }
    }

    public JComponent getVisualRepresentation() {
        if (des == null) {
            JPanel err = new JPanel();
            JLabel emptyLabel = new JLabel("The WSDL Preview can not be rendered,because Java source or WSDL file isn't valid. Please switch to source or design view and correct the source file.");
            err.add(emptyLabel, BorderLayout.CENTER);
            return err;
        } else {
            return this;
        }
    }

    public JComponent getToolbarRepresentation() {
        if (des != null) {
            Document doc = getEditorPane().getDocument();
            if (doc instanceof NbDocument.CustomToolbar) {
                if (toolbar == null) {
                    toolbar = ((NbDocument.CustomToolbar) doc).createToolbar(getEditorPane());
                }
                return toolbar;
            }
        }
        Toolbar tb = new Toolbar();
        return tb;
    }

    public void setMultiViewCallback(MultiViewElementCallback callback) {
        if (des != null) {
            multiViewCallback = callback;
        }
    }

    public CloseOperationState canCloseElement() {

        return CloseOperationState.STATE_OK;

    }

    @Override
    public void componentActivated() {
        if (des != null) {
            super.componentActivated();
            setEditableDisabled();
        }
    }

    @Override
    public void componentDeactivated() {
        if (des != null) {
            super.componentDeactivated();
        }
    }

    @Override
    public void componentOpened() {
        if (des != null) {
            super.componentOpened();
            setEditableDisabled();
        }
    }

    @Override
    public void componentClosed() {
        if (des != null) {
            super.componentClosed();
        }
    }

    @Override
    public void componentShowing() {
        if (des != null) {
            super.componentShowing();
            setActivatedNodes(new Node[]{dataObject.getNodeDelegate()});
            setEditableDisabled();
        }
    }

    @Override
    public void componentHidden() {
        if (des != null) {
            super.componentHidden();
            setActivatedNodes(new Node[]{});
        }
    }

    @Override
    public UndoRedo getUndoRedo() {
        if (des != null) {
            return super.getUndoRedo();
        }
        return null;
    }
    private Lookup lookup;

//    @Override
//    public Lookup getLookup() {
//
//
//        if (lookup == null) {
//            lookup = new ProxyLookup(super.getLookup(), myLookup);
//        }
//        return lookup;
//
//    }
    /**
     *  Sets CloneableEditor instance not editable, according to component specification.
     *  CloneableEditor isn't working properly with MultiViewComponent and part of editor 
     *  functionality couldn't work, otherwise.
     */
    public void setEditableDisabled() {
        JEditorPane prev = this.getEditorPane();
        prev.setEditable(false);
    }
}
