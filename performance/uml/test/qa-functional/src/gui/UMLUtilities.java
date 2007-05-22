/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package gui;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;

/**
 * Utilities for Performance tests, workarrounds, often used methods, ...
 *
 * @author  mmirilovic@netbeans.org
 */
public class UMLUtilities extends gui.Utilities{

    public static Node getProcessFilesNode(String project){
        String processNode = org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.bpel.project.ui.Bundle", "LBL_Node_Sources");
        return new Node(new ProjectsTabOperator().getProjectRootNode(project),processNode);
    }
    
}
