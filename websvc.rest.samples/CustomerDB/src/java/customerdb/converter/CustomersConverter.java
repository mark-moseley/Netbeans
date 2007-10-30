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

import customerdb.Customer;
import java.net.URI;
import java.util.Collection;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.ArrayList;

/**
 *
 * @author Peter Liu
 */

@XmlRootElement(name = "customers")
public class CustomersConverter {
    private Collection<Customer> entities;
    private Collection<CustomerRefConverter> references;
    private URI uri;
    
    /** Creates a new instance of CustomersConverter */
    public CustomersConverter() {
    }

    /**
     * Creates a new instance of CustomersConverter.
     *
     * @param entities associated entities
     * @param uri associated uri
     */
    public CustomersConverter(Collection<Customer> entities, URI uri) {
        this.entities = entities;
        this.uri = uri;
    }

    /**
     * Returns a collection of CustomerRefConverter.
     *
     * @return a collection of CustomerRefConverter
     */
    @XmlElement(name = "customerRef")
    public Collection<CustomerRefConverter> getReferences() {
        references = new ArrayList<CustomerRefConverter>();
        if (entities != null) {
            for (Customer entity : entities) {
                references.add(new CustomerRefConverter(entity, uri, true));
            }
        }
        return references;
    }

    /**
     * Sets a collection of CustomerRefConverter.
     *
     * @param a collection of CustomerRefConverter to set
     */
    public void setReferences(Collection<CustomerRefConverter> references) {
        this.references = references;
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
     * Returns a collection Customer entities.
     *
     * @return a collection of Customer entities
     */
    @XmlTransient
    public Collection<Customer> getEntities() {
        entities = new ArrayList<Customer>();
        if (references != null) {
            for (CustomerRefConverter ref : references) {
                entities.add(ref.getEntity());
            }
        }
        return entities;
    }
}
