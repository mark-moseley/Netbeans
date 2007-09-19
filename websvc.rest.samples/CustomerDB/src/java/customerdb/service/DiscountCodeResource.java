/*
 * Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package customerdb.service;

import customerdb.DiscountCode;
import javax.ws.rs.UriTemplate;
import javax.ws.rs.UriParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.WebApplicationException;
import javax.persistence.NoResultException;
import customerdb.Customer;
import java.util.Collection;
import customerdb.converter.DiscountCodeConverter;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Peter Liu
 */

public class DiscountCodeResource {
    private UriInfo context;
    
    /** Creates a new instance of DiscountCodeResource */
    public DiscountCodeResource() {
    }

    /**
     * Constructor used for instantiating an instance of dynamic resource.
     *
     * @param context HttpContext inherited from the parent resource
     */
    public DiscountCodeResource(UriInfo context) {
        this.context = context;
    }

    /**
     * Get method for retrieving an instance of DiscountCode identified by id in XML format.
     *
     * @param id identifier for the entity
     * @return an instance of DiscountCodeConverter
     */
    @HttpMethod("GET")
    @ProduceMime({"application/xml", "application/json"})
    public DiscountCodeConverter get(@UriParam("discountCode")
    String id) {
        try {
            return new DiscountCodeConverter(getEntity(id), context.getAbsolute());
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
    @HttpMethod("PUT")
    @ConsumeMime({"application/xml", "application/json"})
    public void put(@UriParam("discountCode")
    String id, DiscountCodeConverter data) {
        PersistenceService service = PersistenceService.getInstance();
        try {
            service.beginTx();
            updateEntity(getEntity(id), data.getEntity());
            service.commitTx();
        } finally {
            service.close();
        }
    }

    /**
     * Delete method for deleting an instance of DiscountCode identified by id.
     *
     * @param id identifier for the entity
     */
    @HttpMethod("DELETE")
    public void delete(@UriParam("discountCode")
    String id) {
        PersistenceService service = PersistenceService.getInstance();
        try {
            service.beginTx();
            DiscountCode entity = getEntity(id);
            service.removeEntity(entity);
            service.commitTx();
        } finally {
            service.close();
        }
    }

    /**
     * Returns a dynamic instance of CustomersResource used for entity navigation.
     *
     * @param id identifier for the parent entity
     * @return an instance of CustomersResource
     */
    @UriTemplate("customers/")
    public CustomersResource getCustomersResource(@UriParam("discountCode")
    String id) {
        final DiscountCode parent = getEntity(id);
        return new CustomersResource(context) {

            @Override
            protected Collection<Customer> getEntities() {
                return parent.getCustomerCollection();
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
    protected DiscountCode getEntity(String id) {
        try {
            return (DiscountCode) PersistenceService.getInstance().createNamedQuery("DiscountCode.findByDiscountCode").setParameter("discountCode", id).getSingleResult();
        } catch (NoResultException ex) {
            throw new WebApplicationException(new Throwable("Resource for " + context.getAbsolute() + " does not exist."), 404);
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
        newEntity.setDiscountCode(entity.getDiscountCode());
        entity.getCustomerCollection().removeAll(newEntity.getCustomerCollection());
        for (Customer value : entity.getCustomerCollection()) {
            value.setDiscountCode(null);
        }
        entity = PersistenceService.getInstance().mergeEntity(newEntity);
        for (Customer value : entity.getCustomerCollection()) {
            value.setDiscountCode(entity);
        }
        return entity;
    }
}
