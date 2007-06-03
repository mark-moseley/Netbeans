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

package org.netbeans.modules.websvc.core.webservices.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.source.SourceUtils;
import org.netbeans.modules.websvc.core.webservices.ui.panels.SelectHandlerPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Roderico Cruz, Milan Kuchtiak
 * Displays a Dialog for selecting web service message handler classes
 * that are in a project.
 */
public class WSHandlerDialog {
    private Dialog dialog;
    private SelectHandlerPanel sPanel;
    private AddMessageHandlerDialogDesc dlgDesc;
    private boolean isJaxWS;
    private Map<String, Integer> handlerMap;
    
    //Handler types
    public static final int JAXWS_LOGICAL_HANDLER = 1;
    public static final int JAXWS_MESSAGE_HANDLER = 2;
    public static final int JAXRPC_MESSAGE_HANDLER = 3;
    public static final int INVALID_HANDLER = -1;
    
    /**
     * Creates a new instance of WSHandlerDialog
     */
    public WSHandlerDialog(Project project, boolean isJaxWS) {
        this.isJaxWS = isJaxWS;
        sPanel = new SelectHandlerPanel(project);
        dlgDesc = new AddMessageHandlerDialogDesc(sPanel);
        dialog = DialogDisplayer.getDefault().createDialog(dlgDesc);
        handlerMap = new TreeMap<String, Integer>();
    }
    
    public void show(){
        dialog.setVisible(true);
    }
    
    public boolean okButtonPressed(){
        return dlgDesc.getValue() == DialogDescriptor.OK_OPTION;
    }
    
    public Map<String, Integer> getSelectedClasses(){
        return handlerMap;
    }
    
    private FileObject getFileObjectFromNode(Node n) {
        DataObject dObj = n.getCookie(DataObject.class);
        if (dObj!=null) return dObj.getPrimaryFile();
        return null;
    }
    
    class AddMessageHandlerDialogDesc extends DialogDescriptor{
        Project project;
        final SelectHandlerPanel sPanel;
        
        private Object[] closingOptionsWithoutOK = {DialogDescriptor.CANCEL_OPTION,
        DialogDescriptor.CLOSED_OPTION};
        private Object[] closingOptionsWithOK = {DialogDescriptor.CANCEL_OPTION,
        DialogDescriptor.CLOSED_OPTION, DialogDescriptor.OK_OPTION};
        
        /**
         * Creates a new instance of AddMessageHandlerDialogDesc
         */
        public AddMessageHandlerDialogDesc(SelectHandlerPanel sPanel) {
            super(sPanel, NbBundle.getMessage(WSHandlerDialog.class, "TTL_SelectHandler"));
            this.sPanel = sPanel;
            this.setButtonListener(new AddMessageActionListener(sPanel));
        }
        
        class AddMessageActionListener implements ActionListener{
            SelectHandlerPanel sPanel;
            public AddMessageActionListener(SelectHandlerPanel sPanel){
                this.sPanel = sPanel;
            }
            public void actionPerformed(ActionEvent evt) {
                if(evt.getSource() == NotifyDescriptor.OK_OPTION){
                    boolean accepted = true;
                    String errMsg = null;
                    Node[] selectedNodes = sPanel.getSelectedNodes();
                    for(int i = 0; i < selectedNodes.length; i++){
                        Node node = selectedNodes[i];
                        FileObject javaClassFo = getFileObjectFromNode(node);
                        //FIX-ME: Improve this by filtering the Tree View to only include handlers
                        if(javaClassFo == null){
                            errMsg = NbBundle.getMessage(WSHandlerDialog.class,
                                    "NotJavaClass_msg");
                            accepted = false;
                            break;
                        }
                        final int[] handlerType = new int[]{JAXWS_LOGICAL_HANDLER};
                        
                        JavaSource javaSource = JavaSource.forFileObject(javaClassFo);
                        if(javaSource == null){
                            errMsg = NbBundle.getMessage(WSHandlerDialog.class,
                                    "NotJavaClass_msg");
                            accepted = false;
                            break;
                        }
                        
                        CancellableTask<CompilationController> task = new CancellableTask<CompilationController>() {
                            public void run(CompilationController controller) throws IOException {
                                controller.toPhase(Phase.ELEMENTS_RESOLVED);
                                handlerType[0] = getHandlerType(controller, isJaxWS);
                            }
                            public void cancel() {}
                        };
                        try {
                            javaSource.runUserActionTask(task, true);
                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ex);
                        }
                        
                        if(handlerType[0] == INVALID_HANDLER) {
                            errMsg = NbBundle.getMessage(WSHandlerDialog.class,
                                    "NotHandlerClass_msg");
                            accepted = false;
                            break;
                        } else{
                            FileObject fo = getFileObjectFromNode(node);
                            if (fo!=null) {
                                ClassPath classPath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
                                String handlerClassName = classPath.getResourceName(fo, '.', false);
                                handlerMap.put(handlerClassName, handlerType[0]);
                            }
                        }
                    }
                    if (!accepted) {
                        NotifyDescriptor.Message notifyDescr =
                                new NotifyDescriptor.Message(errMsg,
                                NotifyDescriptor.ERROR_MESSAGE );
                        DialogDisplayer.getDefault().notify(notifyDescr);
                        AddMessageHandlerDialogDesc.this.setClosingOptions(closingOptionsWithoutOK);
                    } else {
                        // Everything was fine so allow OK
                        AddMessageHandlerDialogDesc.this.setClosingOptions(closingOptionsWithOK);
                    }
                }
            }
        }
    }
    public static int getHandlerType(CompilationController cc, boolean isJaxWS) throws IOException {
        SourceUtils srcUtils = SourceUtils.newInstance(cc);
        if (srcUtils!=null) {
            TypeMirror classMirror = srcUtils.getTypeElement().asType();
            
            if(isJaxWS) {
                // test if class extends "javax.xml.ws.handler.LogicalHandler<C extends javax.xml.ws.handler.LogicalMessageContext>"
                TypeElement handlerElement = cc.getElements().getTypeElement("javax.xml.ws.handler.LogicalHandler"); //NOI18N
                DeclaredType handlerType=null;
                if (handlerElement!=null) {
                    TypeElement messageContextElement = cc.getElements().getTypeElement("javax.xml.ws.handler.LogicalMessageContext"); //NOI18N
                    WildcardType wildcardType = cc.getTypes().getWildcardType(messageContextElement.asType(), null);
                    handlerType = cc.getTypes().getDeclaredType(handlerElement, wildcardType);
                }
                if (handlerType!=null && cc.getTypes().isSubtype(classMirror, handlerType)) {
                    return JAXWS_LOGICAL_HANDLER;
                }
                // test if class extends "javax.xml.ws.handler.Handler<C extends javax.xml.ws.handler.MessageContext>"
                handlerElement = cc.getElements().getTypeElement("javax.xml.ws.handler.Handler"); //NOI18N
                handlerType=null;
                if (handlerElement!=null) {
                    TypeElement messageContextElement = cc.getElements().getTypeElement("javax.xml.ws.handler.MessageContext"); //NOI18N
                    WildcardType wildcardType = cc.getTypes().getWildcardType(messageContextElement.asType(), null);
                    handlerType = cc.getTypes().getDeclaredType(handlerElement, wildcardType);
                }
                if (handlerType!=null && cc.getTypes().isSubtype(classMirror, handlerType)) {
                    return JAXWS_MESSAGE_HANDLER;
                }
            } else {
                // test if class extends "javax.xml.rpc.handler.Handler"
                TypeElement handlerElement = cc.getElements().getTypeElement("javax.xml.rpc.handler.Handler"); //NOI18N
                if (handlerElement!=null && cc.getTypes().isSubtype(classMirror, handlerElement.asType())) {
                    return JAXRPC_MESSAGE_HANDLER;
                }
            }
        }
        return INVALID_HANDLER;
    }
    // }
}
