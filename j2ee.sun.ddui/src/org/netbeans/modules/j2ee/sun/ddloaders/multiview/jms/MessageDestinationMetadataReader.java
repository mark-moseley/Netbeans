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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview.jms;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestination;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.sun.ddloaders.Utils;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.CommonBeanReader;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.common.DDBinding;


/**
 *
 * @author Peter Williams
 */
public class MessageDestinationMetadataReader extends CommonBeanReader {

    public MessageDestinationMetadataReader() {
        super(DDBinding.PROP_MSGDEST);
    }
    
    /** For normalizing data structures within /ejb-jar graph.
     *    /ejb-jar -> /ejb-jar/enterprise-beans
     */
    @Override
    protected CommonDDBean normalizeParent(CommonDDBean parent) {
        if(parent instanceof EjbJar) {
            parent = ((EjbJar) parent).getEnterpriseBeans();
        }
        return parent;
    }
    
    /** Maps interesting fields from security-role descriptor to a multi-level property map.
     * 
     * @return Map<String, Object> where Object is either a String value or nested map
     *  with the same structure (and thus ad infinitum)
     */
    public Map<String, Object> genProperties(CommonDDBean [] beans) {
        Map<String, Object> result = null;
        if(beans instanceof MessageDestination []) {
            MessageDestination [] destinations = (MessageDestination []) beans;
            for(MessageDestination msgDest: destinations) {
                String msgDestName = msgDest.getMessageDestinationName();
                if(Utils.notEmpty(msgDestName)) {
                    if(result == null) {
                        result = new HashMap<String, Object>();
                    }
                    Map<String, String> securityRoleMap = new HashMap<String, String>();
                    result.put(msgDestName, securityRoleMap);
                    securityRoleMap.put(DDBinding.PROP_NAME, msgDestName);
                }
            }
        }
        return result;
    }
}
