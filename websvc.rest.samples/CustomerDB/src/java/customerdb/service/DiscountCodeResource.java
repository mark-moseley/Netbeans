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

import customerdb.DiscountCode;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.WebApplicationException;
import javax.persistence.NoResultException;
import customerdb.Customer;
import java.util.Collection;
import customerdb.converter.DiscountCodeConverter;
import javax.ws.rs.core.UriInfo;


/**
 *
 * @author __USER__
 */

public class DiscountCodeResource {
    private String id;
    private UriInfo context;
    
    /** Creates a new instance of DiscountCodeResource */
    public DiscountCodeResource() {
    }

    /**
     * Constructor used for instantiating an instance of dynamic resource.
     *
     * @param context HttpContext inherited from the parent resource
     */
    public DiscountCodeResource(String id, UriInfo context) {
        this.id = id;
        this.context = context;
    }

    /**
     * Get method for retrieving an instance of DiscountCode identified by id in XML format.
     *
     * @param id identifier for the entity
     * @return an instance of DiscountCodeConverter
     */
    @GET
    @ProduceMime({"application/xml", "application/json"})
    public DiscountCodeConverter get(@QueryParam("expandLevel")
    @DefaultValue("1")
    int expandLevel) {
        try {
            return new DiscountCodeConverter(getEntity(), context.getAbsolutePath(), expandLevel);
        } finally {
            PersistenceService.getInstance().close();
        }
    }

    /**
     * Put method for updating an instance of DiscountCode identified by id using XML as the input format.
     *
     * @param id identifier for the entity
     * @param data an DiscountCodeConverter entity that is deserialized from a XML stream
     */
    @PUT
    @ConsumeMime({"application/xml", "application/json"})
    public void put(DiscountCodeConverter data) {
        PersistenceService persistenceSvc = PersistenceService.getInstance();
        try {
            persistenceSvc.beginTx();
            updateEntity(getEntity(), data.getEntity());
            persistenceSvc.commitTx();
        } finally {
            persistenceSvc.close();
        }
    }

    /**
     * Delete method for deleting an instance of DiscountCode identified by id.
     *
     * @param id identifier for the entity
     */
    @DELETE
    public void delete() {
        PersistenceService persistenceSvc = PersistenceService.getInstance();
        try {
            persistenceSvc.beginTx();
            DiscountCode entity = getEntity();
            deleteEntity(entity);
            persistenceSvc.commitTx();
        } finally {
            persistenceSvc.close();
        }
    }

    /**
     * Returns a dynamic instance of CustomersResource used for entity navigation.
     *
     * @param id identifier for the parent entity
     * @return an instance of CustomersResource
     */
    @Path("customerCollection/")
    public CustomersResource getCustomerCollectionResource() {
        final DiscountCode parent = getEntity();
        return new CustomersResource(context) {

            @Override
            protected Collection<Customer> getEntities(int start, int max, String query) {
                Collection<Customer> result = new java.util.ArrayList<Customer>();
                int index = 0;
                for (Customer e : parent.getCustomerCollection()) {
                    if (index >= start && (index - start) < max) {
                        result.add(e);
                    }
                    index++;
                }
                return result;
            }

            @Override
            protected void createEntity(Customer entity) {
                super.createEntity(entity);
                entity.setDiscountCode(parent);
            }
        };
    }

    /**
     * Returns an instance of DiscountCode identified by id.
     *
     * @param id identifier for the entity
     * @return an instance of DiscountCode
     */
    protected DiscountCode getEntity() {
        try {
            return (DiscountCode) PersistenceService.getInstance().createQuery("SELECT e FROM DiscountCode e where e.discountCode = :discountCode").setParameter("discountCode", id.charAt(0)).getSingleResult();
        } catch (NoResultException ex) {
            throw new WebApplicationException(new Throwable("Resource for " + context.getAbsolutePath() + " does not exist."), 404);
        }
    }

    /**
     * Updates entity using data from newEntity.
     *
     * @param entity the entity to update
     * @param newEntity the entity containing the new data
     * @return the updated entity
     */
    protected DiscountCode updateEntity(DiscountCode entity, DiscountCode newEntity) {
        Collection<Customer> customerCollection = entity.getCustomerCollection();
        Collection<Customer> customerCollectionNew = newEntity.getCustomerCollection();
        entity = PersistenceService.getInstance().mergeEntity(newEntity);
        for (Customer value : customerCollection) {
            if (!customerCollectionNew.contains(value)) {
                throw new WebApplicationException(new Throwable("Cannot remove items from customerCollection"));
            }
        }
        for (Customer value : customerCollectionNew) {
            if (!customerCollection.contains(value)) {
                DiscountCode oldEntity = value.getDiscountCode();
                value.setDiscountCode(entity);
                if (oldEntity != null && !oldEntity.equals(entity)) {
                    oldEntity.getCustomerCollection().remove(value);
                }
            }
        }
        return entity;
    }

    /**
     * Deletes the entity.
     *
     * @param entity the entity to deletle
     */
    protected void deleteEntity(DiscountCode entity) {
        if (!entity.getCustomerCollection().isEmpty()) {
            throw new WebApplicationException(new Throwable("Cannot delete entity because customerCollection is not empty."));
        }
        PersistenceService.getInstance().removeEntity(entity);
    }
}
