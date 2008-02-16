/*
 *  ItemResource
 *
 * Created on February 15, 2008, 8:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package o.n.m.qa.test;

import javax.ws.rs.core.HttpContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.UriParam;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.PUT;
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;

/**
 * REST Web Service
 *
 * @author lukas
 */

public class ItemResource {
    @HttpContext
    private UriInfo context;

    /** Creates a new instance of ItemResource */
    public ItemResource() {
    }

    /**
     * Retrieves representation of an instance of o.n.m.qa.test.ItemResource
     * @param name resource URI parameter
     * @return an instance of java.lang.String
     */
    @GET
    @ProduceMime("application/xml")
    public String getXml(@UriParam("name")
    String name) {
        //TODO return proper representation object
        throw new UnsupportedOperationException();
    }

    /**
     * PUT method for updating or creating an instance of ItemResource
     * @param name resource URI parameter
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @ConsumeMime("application/xml")
    public void putXml(@UriParam("name")
    String name, String content) {
    }

    /**
     * DELETE method for resource ItemResource
     * @param name resource URI parameter
     */
    @DELETE
    public void delete(@UriParam("name")
    String name) {
    }
}
