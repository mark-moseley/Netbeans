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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.rave.propertyeditors.domains;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import com.sun.rave.designtime.DesignProject;
import com.sun.rave.designtime.DesignProperty;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Domain of resource bundles available within the current project. The domain
 * elements are constructed dynamically, by exploring the project source directory
 * for Java properties files.
 */
public class ResourceBundlesDomain extends AttachedDomain {

    // Root URI of the project's source directory
    private static final String BLUE_PRINT_SOURCE_ROOT = "src/java/"; //NOI18N
    private static final String JAKARTA_SOURCE_ROOT = "src/";
    private static final URI SOURCE_URI = URI.create("src/");

    // File name suffix that identifiers a Java property bundle file
    private static final String BUNDLE_SUFFIX = ".properties"; // NOI18N


    public Element[] getElements() {

        DesignProperty designProperty = this.getDesignProperty();

        if (designProperty == null)
            return Element.EMPTY_ARRAY;

        DesignBean bean = getDesignProperty().getDesignBean();
        DesignProject project = bean.getDesignContext().getProject();

        URI resources[] = project.getResources(SOURCE_URI, true);
        if (resources == null)
            return Element.EMPTY_ARRAY;

        Set set = new TreeSet();
        for (int i = 0; i < resources.length; i++) {
            String resource = resources[i].toString();
            String sourceRoot;
            if (resource.startsWith(BLUE_PRINT_SOURCE_ROOT)){
               sourceRoot =  BLUE_PRINT_SOURCE_ROOT;
            }else{
               sourceRoot =  JAKARTA_SOURCE_ROOT;
            }
            if (isBasePropertiesResource(resource)) {
                resource = resource.substring(0, resource.length() - BUNDLE_SUFFIX.length());
                resource = resource.substring(sourceRoot.length());
                String baseName = resource.replace('/', '.');
                set.add(new Element(baseName)); // NOI18N
            }
        }

        return (Element[]) set.toArray(new Element[set.size()]);

    }

    private static boolean isBasePropertiesResource(String resource) {
        if (resource.endsWith(BUNDLE_SUFFIX)) {
            int i = resource.lastIndexOf('/');
            int j = resource.lastIndexOf('_');
            if (j <= i)
                return true;
        }
        return false;
    }

}
