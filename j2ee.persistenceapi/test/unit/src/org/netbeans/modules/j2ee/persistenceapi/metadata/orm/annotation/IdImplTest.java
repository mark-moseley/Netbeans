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

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.EntityMappingsMetadata;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Id;

/**
 *
 * @author Andrei Badea
 */
public class IdImplTest extends EntityMappingsTestCase {

    public IdImplTest(String testName) {
        super(testName);
    }

    public void testBasic() throws Exception {
        TestUtilities.copyStringToFileObject(srcFO, "Customer.java",
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class Customer {" +
                "   @Id()" +
                "   @GeneratedValue()" +
                "   private int id;" +
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "Employee.java",
                "import javax.persistence.*;" +
                "@Entity()" +
                "public class Employee {" +
                "   @Id()" +
                "   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = \"seq\")" +
                "   private int id;" +
                "}");
        createModel().runReadAction(new MetadataModelAction<EntityMappingsMetadata, Void>() {
            public Void run(EntityMappingsMetadata metadata) {
                Entity[] entityList = metadata.getRoot().getEntity();
                Entity entity = getEntityByName(entityList, "Customer");
                Id id = entity.getAttributes().getId()[0];
                assertEquals("id", id.getName());
                assertEquals("AUTO", id.getGeneratedValue().getStrategy());
                assertEquals("", id.getGeneratedValue().getGenerator());
                entity = getEntityByName(entityList, "Employee");
                id = entity.getAttributes().getId()[0];
                assertEquals("SEQUENCE", id.getGeneratedValue().getStrategy());
                assertEquals("seq", id.getGeneratedValue().getGenerator());
                return null;
            }
        });
    }
}
