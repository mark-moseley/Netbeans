/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.javaee.db;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Peter Williams
 */
public class UrlDataTest {

    public UrlDataTest() {
    }

    private static String [] urls = {
            "jdbc:derby://localhost:1527/travel",
            "jdbc:derby://localhost:1527/travel;create=true",
            "jdbc:sun:sqlserver://localhost:1433;databaseName=sampledb",
            "jdbc:sun:sqlserver://localhost:1433;databaseName=sampledb;create=true;foo=bar;bar=foo",
            "jdbc:sun:sqlserver://localhost:1433;create=true;databaseName=sampledb;foo=bar;bar=foo",
            "jdbc:sqlserver://localhost:1433",
            "jdbc:sqlserver://localhost:1433/sampledb",
            "jdbc:sqlserver://localhost\\instanceName:1433",
            "jdbc:sqlserver://localhost\\instanceName:1433/sampledb",
            "jdbc:sun:oracle://localhost:1521;SID=sampledb",
            "jdbc:oracle:thin:@localhost:1521:sampledb",
            "jdbc:oracle:thin:@localhost:sampledb",
            "jdbc:oracle:thin:@localhost:1521",
            "jdbc:mysql://localhost:3306/baza1250?autoReconnect=true&characterEncoding=cp1250&characterSetResults=cp1250",
            "jdbc:postgresql://localhost:5432/sampledb",
            "jdbc:weblogic:mssqlserver4:sampledb@localhost:1433",
            "jdbc:informix-sqli://localhost:1530/sampledb:INFORMIXSERVER=informixinstancename",
            "jdbc:datadirect:informix://localhost:1530;informixServer=informixinstancename;databaseName=sampledb",
            "jdbc:as400://9.88.24.163",
            "jdbc:as400://myiSeries;database name=IASP1"
    };
    
    /**
     * Test of database URL parsing
     */
    @Test
    public void testUrlParser() {
        for(String url: urls) {
            System.out.println("Parsing: " + url);
            UrlData data = new UrlData(url);
            assertEquals("Parsing " + url + " failed.", url, data.constructUrl());
        }
    }

}