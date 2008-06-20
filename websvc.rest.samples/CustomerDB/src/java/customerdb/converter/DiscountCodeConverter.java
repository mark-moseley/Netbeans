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

package customerdb.converter;

import java.math.BigDecimal;
import java.net.URI;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlAttribute;
import javax.ws.rs.core.UriBuilder;
import customerdb.DiscountCode;
import customerdb.service.PersistenceService;


/**
 *
 * @author __USER__
 */

@XmlRootElement(name = "discountCode")
public class DiscountCodeConverter {
    private DiscountCode entity;
    private URI uri;
    private int expandLevel;
    
    /** Creates a new instance of DiscountCodeConverter */
    public DiscountCodeConverter() {
    }

    /**
     * Creates a new instance of DiscountCodeConverter.
     *
     * @param entity associated entity
     * @param uri associated uri
     * @param expandLevel indicates the number of levels the entity graph should be expanded@param isUriExtendable indicates whether the uri can be extended
     */
    public DiscountCodeConverter(DiscountCode entity, URI uri, int expandLevel, boolean isUriExtendable) {
        this.entity = entity;
        this.uri = (isUriExtendable) ? UriBuilder.fromUri(uri).path(entity.getDiscountCode() + "/").build() : uri;
        this.expandLevel = expandLevel;
    }

    /**
     * Creates a new instance of DiscountCodeConverter.
     *
     * @param entity associated entity
     * @param uri associated uri
     * @param expandLevel indicates the number of levels the entity graph should be expanded
     */
    public DiscountCodeConverter(DiscountCode entity, URI uri, int expandLevel) {
        this(entity, uri, expandLevel, false);
    }

    /**
     * Getter for discountCode.
     *
     * @return value for discountCode
     */
    @XmlElement
    public Character getDiscountCode() {
        return (expandLevel > 0) ? getEntity().getDiscountCode() : null;
    }

    /**
     * Setter for discountCode.
     *
     * @param value the value to set
     */
    public void setDiscountCode(Character value) {
        getEntity().setDiscountCode(value);
    }

    /**
     * Getter for rate.
     *
     * @return value for rate
     */
    @XmlElement
    public BigDecimal getRate() {
        return (expandLevel > 0) ? getEntity().getRate() : null;
    }

    /**
     * Setter for rate.
     *
     * @param value the value to set
     */
    public void setRate(BigDecimal value) {
        getEntity().setRate(value);
    }

    /**
     * Getter for customerCollection.
     *
     * @return value for customerCollection
     */
    @XmlElement
    public CustomersConverter getCustomerCollection() {
        if (expandLevel > 0) {
            if (getEntity().getCustomerCollection() != null) {
                return new CustomersConverter(getEntity().getCustomerCollection(), uri.resolve("customerCollection/"), expandLevel - 1);
            }
        }
        return null;
    }

    /**
     * Setter for customerCollection.
     *
     * @param value the value to set
     */
    public void setCustomerCollection(CustomersConverter value) {
        getEntity().setCustomerCollection((value != null) ? value.getEntities() : null);
    }

    /**
     * Returns the URI associated with this converter.
     *
     * @return the uri
     */
    @XmlAttribute
    public URI getUri() {
        return uri;
    }

    /**
     * Sets the URI for this reference converter.
     *
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Returns the DiscountCode entity.
     *
     * @return an entity
     */
    @XmlTransient
    public DiscountCode getEntity() {
        if (entity == null) {
            entity = new DiscountCode();
        }
        return entity;
    }

    /**
     * Returns the resolved DiscountCode entity.
     *
     * @return an resolved entity
     */
    public DiscountCode resolveEntity() {
        if (entity != null) {
            return PersistenceService.getInstance().resolveEntity(DiscountCode.class, entity.getDiscountCode());
        } else {
            return (DiscountCode) UriResolver.getInstance().resolve(DiscountCodeConverter.class, uri);
        }
    }
}
