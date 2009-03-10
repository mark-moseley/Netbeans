/*
 * DefinitionImpl.java
 *
 * Created on September 24, 2006, 6:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.netbeans.modules.e2e.api.schema.SchemaHolder;
import org.netbeans.modules.e2e.api.wsdl.Binding;
import org.netbeans.modules.e2e.api.wsdl.Definition;
import org.netbeans.modules.e2e.api.wsdl.Message;
import org.netbeans.modules.e2e.api.wsdl.PortType;
import org.netbeans.modules.e2e.api.wsdl.Service;

/**
 *
 * @author Michal Skvor
 */
public class DefinitionImpl implements Definition {
    
    private SchemaHolder schemaHolder;
    
    private Map<String, Binding> bindings = new HashMap<String, Binding>();
    private Map<QName, Message> messages = new HashMap<QName, Message>();
    private Map<String, Service> services = new HashMap<String, Service>();
    private Map<QName, PortType> portTypes = new HashMap<QName, PortType>();
    
    private String documentation;
    
    private String targetNamespace;
    
    /** Creates a new instance of DefinitionImpl */
    public DefinitionImpl() {
    }

    public void setSchemaHolder( SchemaHolder schemaHolder ) {
        this.schemaHolder = schemaHolder;
    }

    public SchemaHolder getSchemaHolder() {
        return schemaHolder;
    }
    
    public void addBinding( Binding binding ) {
        bindings.put( binding.getName(), binding );
    }

    public Binding getBinding( String name ) {
        return bindings.get( name );
    }

    public Map<String, Binding> getBindings() {
        return Collections.unmodifiableMap( bindings );
    }

    public void addMessage( Message message ) {
        messages.put( message.getQName(), message );
    }

    public Message getMessage( QName name ) {
        return messages.get( name );
    }

    public Map<QName, Message> getMessages() {
        return Collections.unmodifiableMap( messages );
    }

    public void addService( Service service ) {
        services.put( service.getName(), service );
    }

    public Service getService( String name ) {
        return services.get( name );
    }

    public Map<String, Service> getServices() {
        return Collections.unmodifiableMap( services );
    }

    public void addPortType( PortType portType ) {
        portTypes.put( portType.getQName(), portType );
    }

    public PortType getPortType( QName name ) {
        return portTypes.get( name );
    }

    public Map<QName, PortType> getPortTypes() {
        return Collections.unmodifiableMap( portTypes );
    }

    public void setDocumentation( String documentation ) {
        this.documentation = documentation;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setTargetNamespace( String targetNamespace ) {
        this.targetNamespace = targetNamespace;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

}
