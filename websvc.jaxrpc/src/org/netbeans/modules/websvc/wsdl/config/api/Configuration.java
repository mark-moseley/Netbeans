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

package org.netbeans.modules.websvc.wsdl.config.api;

/**
 *
 * @author Peter Williams
 */
public interface Configuration extends RootInterface {
    public static final String PROPERTY_VERSION="cfg_version"; //NOI18N
    public static final String PROPERTY_STATUS="cfg_status"; //NOI18N
    public static final int STATE_VALID=0;
    public static final int STATE_INVALID_PARSABLE=1;
    public static final int STATE_INVALID_UNPARSABLE=2;

    public void setService(Service value);

    public Service getService();

    public Service newService();

    public void setWsdl(Wsdl value);

    public Wsdl getWsdl();

    public Wsdl newWsdl();

    public void setModelfile(Modelfile value);

    public Modelfile getModelfile();

    public Modelfile newModelfile();

    public void setJ2eeMappingFile(J2eeMappingFile value);

    public J2eeMappingFile getJ2eeMappingFile();

    public J2eeMappingFile newJ2eeMappingFile();

}
