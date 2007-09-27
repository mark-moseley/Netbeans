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


package org.netbeans.modules.iep.editor.ps;

import org.netbeans.modules.iep.editor.model.Plan;
import org.netbeans.modules.iep.editor.model.Schema;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.table.DefaultMoveableRowTableModel;
import org.netbeans.modules.iep.editor.tcg.table.MoveableRowTable;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import org.netbeans.modules.iep.editor.tcg.model.TcgProperty;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.dnd.DragGestureEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.openide.util.NbBundle;

/**
 * PartitionPanel.java
 *
 * Created on November 1, 2PartitionPanel_1 *
 * @author Bing Lu
 */
public class TableOutputSchemaPanel extends JPanel implements SharedConstants {
    private static final Logger mLog = Logger.getLogger(TableOutputSchemaPanel.class.getName());
    
    private static final String COL_SEQID = "ems_seqid"; //com.sun.jbi.engine.iep.core.share.SharedConstants    
    
    private Plan mPlan;
    private TcgComponent mComponent;
    private DefaultMoveableRowTableModel mTableModel;
    private MoveableRowTable mTable;

    public TableOutputSchemaPanel(Plan plan, TcgComponent component) {
        mPlan = plan;
        mComponent = component;
        initComponents();
    }
    

    private void initComponents() {
        setLayout(new BorderLayout());
        JPanel topPane = new JPanel();
        topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPane.setLayout(new BorderLayout(5, 5));
        add(topPane, BorderLayout.CENTER);
        JPanel pane = new JPanel();
        pane.setLayout(new BorderLayout(5, 5));
        mTableModel = new DefaultMoveableRowTableModel();
        mTable = new MoveableRowTable(mTableModel) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            public void dragGestureRecognized(DragGestureEvent dge) {
                return;
            }
        };
        Vector data = new Vector();
        try {
            String schemaId = mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY).getStringValue();
            if(!schemaId.trim().equals("")) {
                Schema schema = mPlan.getSchema(schemaId);
                java.util.List attributeMetadataList = new ArrayList(schema.getAttributeMetadataAsList());
                for(int i = 0; i < attributeMetadataList.size(); i+=5) {
                    Vector r = new Vector();
                    String name = (String)attributeMetadataList.get(i);
                    r.add(name);
                    if(i + 1 < attributeMetadataList.size()) {
                        r.add(attributeMetadataList.get(i + 1));
                    } else {
                        r.add("");
                    }
                    if(i + 2 < attributeMetadataList.size()) {
                        r.add(attributeMetadataList.get(i + 2));
                    } else {
                        r.add("");
                    }
                    if(i + 3 < attributeMetadataList.size()) {
                        r.add(attributeMetadataList.get(i + 3));
                    } else {
                        r.add("");
                    }
                    if(i + 4 < attributeMetadataList.size()) {
                        r.add(attributeMetadataList.get(i + 4));
                    } else {
                        r.add("");
                    }
                    data.add(r);
                }
                Vector r = new Vector();
                r.add(COL_SEQID);
                r.add(SQL_TYPE_BIGINT);
                r.add("");
                r.add("");
                r.add("");
                data.add(r);
            } 
        } catch(Exception e) {
            e.printStackTrace();
        }
        Vector colTitle = new Vector();
        colTitle.add(NbBundle.getMessage(TableOutputSchemaPanel.class, "SelectPanel.ATTRIBUTE_NAME"));
        colTitle.add(NbBundle.getMessage(TableOutputSchemaPanel.class, "SelectPanel.DATA_TYPE"));
        colTitle.add(NbBundle.getMessage(TableOutputSchemaPanel.class, "SelectPanel.SIZE"));
        colTitle.add(NbBundle.getMessage(TableOutputSchemaPanel.class, "SelectPanel.SCALE"));
        colTitle.add(NbBundle.getMessage(TableOutputSchemaPanel.class, "SelectPanel.COMMENT"));
        mTableModel.setDataVector(data, colTitle);
        pane.add(new JScrollPane(mTable), BorderLayout.CENTER);
        topPane.add(pane, BorderLayout.CENTER);
    }
    
    public void validateContent(PropertyChangeEvent evt) throws PropertyVetoException {
    }
            
    public void store() {
    }
}