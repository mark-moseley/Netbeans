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




package org.netbeans.test.umllib.vrf.sanity;

import java.awt.Point;
import java.io.PrintStream;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.ElementTypes;

/**
 *
 * @author Alexei Mokeev
 */
public class StateDiagramVerifier extends AbstractDiagramVerifier{
    //String[] elements =
    /** Creates a new instance of ClassDiagramVerifier */
    public StateDiagramVerifier(String diagramName) {
        super(diagramName);
    }
    
    
    public StateDiagramVerifier(String diagramName, PrintStream log) {
        super(diagramName, log);
    }
    
    public StateDiagramVerifier(String diagramName, PrintStream log, String prefix) {
        super(diagramName, log, prefix);
    }
    public boolean doElementCheck(DiagramElementOperator element) {
        log("Checking " + element.getElementType());
        
        return true;
    }
    protected ElementTypes[] getSupportedElements() {
        return new ElementTypes[] {
            ElementTypes.SUBMACHINE_STATE,
            ElementTypes.JUNCTION_POINT_STATE,
            ElementTypes.ENTRY_POINT_STATE,
            ElementTypes.DEEP_HISTORY_STATE,
            ElementTypes.SHALLOW_HISTORY_STATE,
            ElementTypes.CHOICE_PSEUDO_STATE,
            ElementTypes.COMPOSITE_STATE,
            ElementTypes.HORIZONTAL_JOIN_MERGE,
            ElementTypes.VERTICAL_JOIN_MERGE,
            ElementTypes.ABORTED_FINAL_STATE,
            ElementTypes.FINAL_STATE,
            ElementTypes.INITIAL_STATE,
            ElementTypes.SIMPLE_STATE
        };
    }
    
    protected DiagramElementOperator createElementByType(ElementTypes el, int index){        
        if(el == ElementTypes.INITIAL_STATE || el == ElementTypes.FINAL_STATE|| el == ElementTypes.ABORTED_FINAL_STATE 
                || el == ElementTypes.VERTICAL_JOIN_MERGE|| el == ElementTypes.HORIZONTAL_JOIN_MERGE || el == ElementTypes.CHOICE_PSEUDO_STATE
                || el == ElementTypes.SHALLOW_HISTORY_STATE || el == ElementTypes.DEEP_HISTORY_STATE || el == ElementTypes.ENTRY_POINT_STATE
                 || el == ElementTypes.JUNCTION_POINT_STATE){
            try{
                Point p2 = diagram.getDrawingArea().getFreePoint(30);
                diagram.createGenericElementOnDiagram(null,el,p2.x,p2.y);
                DiagramElementOperator e = new DiagramElementOperator(diagram, new DiagramElementOperator.ElementByTypeChooser(el),0);
                return e;
            }catch(Exception e){
                return null;
            }            
        }else{
            return super.createElementByType(el, index);
        }
                                
    }
    
}
