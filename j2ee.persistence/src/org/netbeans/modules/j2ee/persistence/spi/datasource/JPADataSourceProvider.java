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

package org.netbeans.modules.j2ee.persistence.spi.datasource;

import java.util.List;

/**
 * This interface represents a data source provider. Should
 * be implemented by projects where it is possible to use data sources.
 * 
 * @author Erno Mononen
 */
public interface JPADataSourceProvider {

    /**
     * Gets all registered data sources. 
     * 
     * @return a list of <code>JPADataSource</code>s representing
     * the available data sources.
     */ 
    List<JPADataSource> getDataSources();
    
    /**
     * Converts the given <code>dataSource</code> to a <code>JPADataSource</code> if possible. 
     * 
     * @return the given <code>dataSource</code> as a <code>JPADataSource</code> or 
     * <code>null</code> if it could not be converted. 
     */ 
    JPADataSource toJPADataSource(Object dataSource);
}
