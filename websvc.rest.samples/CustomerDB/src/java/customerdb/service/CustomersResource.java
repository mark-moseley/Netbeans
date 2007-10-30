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

package customerdb.service;

import customerdb.Customer;
import java.util.Collection;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Builder;
import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;
import customerdb.converter.CustomersConverter;
import customerdb.converter.CustomerConverter;

/**
 *
 * @author Peter Liu
 */

@UriTemplate("/customers/")
public class CustomersResource {
    @HttpContext
    private UriInfo context;
    
    /** Creates a new instance of CustomersResource */
    public CustomersResource() {
    }

    /**
     * Constructor used for instantiating an instance of dynamic resource.
     *
     * @param context HttpContext inherited from the parent resource
     */
    public CustomersResource(UriInfo context) {
        this.context = context;
    }

    /**
     * Get method for retrieving a collection of Customer instance in XML format.
     *
     * @return an instance of CustomersConverter
     */
    @HttpMethod("GET")
    @ProduceMime({"application/xml", "application/json"})
    public CustomersConverter get() {
        try {
            return new CustomersConverter(getEntities(), context.getAbsolute());
        } finally {
            PersistenceService.getInstance().close();
        }
    }

    /**
     * Post method for creating an instance of Customer using XML as the input format.
     *
     * @param data an CustomerConverter entity that is deserialized from an XML stream
     * @return an instance of CustomerConverter
     */
    @HttpMethod("POST")
    @ConsumeMime({"application/xml", "application/json"})
    public Response post(CustomerConverter data) {
        PersistenceService service = PersistenceService.getInstance();
        try {
            service.beginTx();
            Customer entity = data.getEntity();
            createEntity(entity);
            service.commitTx();
            return Builder.created(context.getAbsolute().resolve(entity.getCustomerId() + "/")).build();
        } finally {
            service.close();
        }
    }

    /**
     * Returns a dynamic instance of CustomerResource used for entity navigation.
     *
     * @return an instance of CustomerResource
     */
    @UriTemplate("{customerId}/")
    public customerdb.service.CustomerResource getCustomerResource() {
        return new CustomerResource(context);
    }

    /**
     * Returns all the entities associated with this resource.
     *
     * @return a collection of Customer instances
     */
    protected Collection<Customer> getEntities() {
        return PersistenceService.getInstance().createQuery("SELECT e FROM Customer e").getResultList();
    }

    /**
     * Persist the given entity.
     *
     * @param entity the entity to persist
     */
    protected void createEntity(Customer entity) {
        PersistenceService.getInstance().persistEntity(entity);
    }
}
