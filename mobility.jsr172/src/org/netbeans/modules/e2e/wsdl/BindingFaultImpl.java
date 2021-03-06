/*
 * BindingFaultImpl.java
 *
 * Created on September 24, 2006, 5:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.e2e.api.wsdl.BindingFault;
import org.netbeans.modules.e2e.api.wsdl.extensions.ExtensibilityElement;

/**
 *
 * @author Michal Skvor
 */
public class BindingFaultImpl implements BindingFault {
    
    private String name;
    
    private List<ExtensibilityElement> extensibilityElements;
    
    /** Creates a new instance of BindingFaultImpl */
    public BindingFaultImpl( String name ) {
        this.name = name;
        extensibilityElements = new ArrayList();
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addExtensibilityElement( ExtensibilityElement extensibilityElement ) {
        extensibilityElements.add( extensibilityElement );
    }

    public List<ExtensibilityElement> getExtensibilityElements() {
        return Collections.unmodifiableList( extensibilityElements );
    }
}
