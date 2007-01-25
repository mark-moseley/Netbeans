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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.iep.editor.model;


import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentType;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponentDelegate;
import org.netbeans.modules.iep.editor.tcg.model.TcgModelManager;


/**
 * Interface that extends Component
 *
 * @author Bing Lu
 *
 * @since July 8, 2004
 */
class AttributeMetadataImpl 
    extends TcgComponentDelegate
    implements AttributeMetadata 
{
    /**
     *  Logger.
     */
    private static final java.util.logging.Logger mLog = java.util.logging.Logger.getLogger("org.netbeans.modules.iep.editor.model");
    
    AttributeMetadataImpl() {
        TcgComponentType planType =
            TcgModelManager.getTcgComponentType("/IEP/Metadata/ColumnMetadata");

        initialize(planType.newTcgComponent("ColumnMetadata", planType.getTitle()));
    }
    
    AttributeMetadataImpl(TcgComponent root) {
        try {
            initialize(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getAttributeName() throws Exception {
        return mComponent.getProperty(NAME_KEY).getStringValue();
    }
    
    public String getAttributeType() throws Exception {
        return mComponent.getProperty(TYPE_KEY).getStringValue();
    }
    
    public String getAttributeSize() throws Exception {
        return mComponent.getProperty(SIZE_KEY).getStringValue();
    }
    
    public String getAttributeScale() throws Exception {
        return mComponent.getProperty(SCALE_KEY).getStringValue();
    }
    
    public String getComment() throws Exception {
        return mComponent.getProperty(COMMENT_KEY).getStringValue();
    }
}    
