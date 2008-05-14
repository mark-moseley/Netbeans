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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.diagrams.nodes.sqd;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.visual.widget.LabelWidget.Alignment;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.dynamics.IMessage;
import org.netbeans.modules.uml.core.support.umlutils.DataFormatter;
import org.netbeans.modules.uml.diagrams.edges.sqd.MessageLabelManager;
import org.netbeans.modules.uml.diagrams.edges.sqd.MessageWidget;
import org.netbeans.modules.uml.diagrams.engines.SequenceDiagramEngine;
import org.netbeans.modules.uml.diagrams.nodes.EditableCompartmentWidget;
import org.netbeans.modules.uml.diagrams.nodes.FeatureWidget;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;

/**
 *
 * @author sp153251
 */
public class NumberedNameLabelWidget extends FeatureWidget implements PropertyChangeListener {
    private IMessage msg;
    private EditableCompartmentWidget label;
    private MessageLabelManager lm;
    public NumberedNameLabelWidget(Scene scene, IMessage msg, MessageLabelManager manager) {
        super(scene);
        this.msg=msg;
        lm=manager;
        initialize(msg);
    }

    public void save(NodeWriter nodeWriter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void propertyChange(PropertyChangeEvent evt) {
        updateUI();
    }

    @Override
    public void refresh()
    {
        updateUI();
    }

    @Override
    protected void updateUI() {
        DesignerScene scene=(DesignerScene) getScene();
        SequenceDiagramEngine engine=(SequenceDiagramEngine) scene.getEngine();
        boolean shownumbers=engine.getSettingValue(SequenceDiagramEngine.SHOW_MESSAGE_NUMBERS)==Boolean.TRUE;
        
        removeCHildrenAndClear();

        DataFormatter formatter = new DataFormatter();

        MessageWidget messageW=lm.getMessage();
        IMessage mesg=(IMessage) ((IPresentationElement) scene.findObject(messageW)).getFirstSubject();
        
        String name=formatter.formatElement(getElement());
        if(shownumbers && msg.getAutoNumber()!=null)name=msg.getAutoNumber()+": "+name;
        if((name==null || name.length()<1))name=lm.retrieveDefaultName();
        label = new EditableCompartmentWidget(getScene());
        label.setMinimumSize(new Dimension(30,4));
        label.setLabel(name);
        label.setAlignment(Alignment.CENTER);
        addChild(label);
    }
    
    public void removeCHildrenAndClear()
    {
        super.removeChildren();
        label=null;
    }
    
    public void setLabel(String expression) {
        setText(expression);
    }

    @Override
    protected void setText(String value) {
        if(label != null)
        {
            label.setLabel(value);
        }
    }
    
    void switchToEditMode() {
        
    }

    public String getWidgetID() {
        return UMLWidgetIDString.NUMBEREDNAMELABELWIDGET.toString();
    }      
}