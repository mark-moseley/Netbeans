/*
 * $Id$
 */

package org.apache.maven.settings.io.jdom;

  //---------------------------------/
 //- Imported classes and packages -/
//---------------------------------/

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import org.apache.maven.model.Model;
import org.apache.maven.settings.Activation;
import org.apache.maven.settings.ActivationFile;
import org.apache.maven.settings.ActivationOS;
import org.apache.maven.settings.ActivationProperty;
import org.apache.maven.settings.IdentifiableBase;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.RepositoryBase;
import org.apache.maven.settings.RepositoryPolicy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.TrackableBase;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.jdom.Content;
import org.jdom.DefaultJDOMFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Class SettingsJDOMWriter.
 * 
 * @version $Revision$ $Date$
 */
public class SettingsJDOMWriter {


      //--------------------------/
     //- Class/Member Variables -/
    //--------------------------/

    /**
     * Field factory.
     */
    private DefaultJDOMFactory factory;

    /**
     * Field lineSeparator.
     */
    private String lineSeparator;


      //----------------/
     //- Constructors -/
    //----------------/

    public SettingsJDOMWriter() {
        factory = new DefaultJDOMFactory();
        lineSeparator = "\n";
    } //-- org.apache.maven.settings.io.jdom.SettingsJDOMWriter()


      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Method findAndReplaceProperties.
     * 
     * @param counter
     * @param props
     * @param name
     * @param parent
     * @return Element
     */
    protected Element findAndReplaceProperties(Counter counter, Element parent, String name, Map props)
    {
        boolean shouldExist = props != null && ! props.isEmpty();
        Element element = updateElement(counter, parent, name, shouldExist);
        if (shouldExist) {
            Iterator it = props.keySet().iterator();
            Counter innerCounter = new Counter(counter.getDepth() + 1);
            while (it.hasNext()) {
                String key = (String) it.next();
                findAndReplaceSimpleElement(innerCounter, element, key, (String)props.get(key), null);
                }
            ArrayList lst = new ArrayList(props.keySet());
            it = element.getChildren().iterator();
            while (it.hasNext()) {
                Element elem = (Element) it.next();
                String key = elem.getName();
                if (!lst.contains(key)) {
                    it.remove();
                }
            }
        }
        return element;
    } //-- Element findAndReplaceProperties(Counter, Element, String, Map) 

    /**
     * Method findAndReplaceSimpleElement.
     * 
     * @param counter
     * @param defaultValue
     * @param text
     * @param name
     * @param parent
     * @return Element
     */
    protected Element findAndReplaceSimpleElement(Counter counter, Element parent, String name, String text, String defaultValue)
    {
        if (defaultValue != null && text != null && defaultValue.equals(text)) {
            Element element =  parent.getChild(name, parent.getNamespace());
            // if exist and is default value or if doesn't exist.. just keep the way it is..
            if ((element != null && defaultValue.equals(element.getText())) || element == null) {
                return element;
            }
        }
        boolean shouldExist = text != null && text.trim().length() > 0;
        Element element = updateElement(counter, parent, name, shouldExist);
        if (shouldExist) {
            element.setText(text);
        }
        return element;
    } //-- Element findAndReplaceSimpleElement(Counter, Element, String, String, String) 

    /**
     * Method findAndReplaceSimpleLists.
     * 
     * @param counter
     * @param childName
     * @param parentName
     * @param list
     * @param parent
     * @return Element
     */
    protected Element findAndReplaceSimpleLists(Counter counter, Element parent, java.util.Collection list, String parentName, String childName)
    {
        boolean shouldExist = list != null && list.size() > 0;
        Element element = updateElement(counter, parent, parentName, shouldExist);
        if (shouldExist) {
            Iterator it = list.iterator();
            Iterator elIt = element.getChildren(childName, element.getNamespace()).iterator();
            if (! elIt.hasNext()) elIt = null;
            Counter innerCount = new Counter(counter.getDepth() + 1);
            while (it.hasNext()) {
                String value = (String) it.next();
                Element el;
                if (elIt != null && elIt.hasNext()) {
                    el = (Element) elIt.next();
                    if (! elIt.hasNext()) elIt = null;
                } else {
                    el = factory.element(childName, element.getNamespace());
                    insertAtPreferredLocation(element, el, innerCount);
                }
                el.setText(value);
                innerCount.increaseCount();
            }
            if (elIt != null) {
                while (elIt.hasNext()) {
                    elIt.next();
                    elIt.remove();
                }
            }
        }
        return element;
    } //-- Element findAndReplaceSimpleLists(Counter, Element, java.util.Collection, String, String) 

    /**
     * Method findAndReplaceXpp3DOM.
     * 
     * @param counter
     * @param dom
     * @param name
     * @param parent
     * @return Element
     */
    protected Element findAndReplaceXpp3DOM(Counter counter, Element parent, String name, Xpp3Dom dom)
    {
        boolean shouldExist = dom != null && (dom.getChildCount() > 0 || dom.getValue() != null);
        Element element = updateElement(counter, parent, name, shouldExist);
        if (shouldExist) {
            replaceXpp3DOM(element, dom, new Counter(counter.getDepth() + 1));
        }
        return element;
    } //-- Element findAndReplaceXpp3DOM(Counter, Element, String, Xpp3Dom) 

    /**
     * Method insertAtPreferredLocation.
     * 
     * @param parent
     * @param counter
     * @param child
     */
    protected void insertAtPreferredLocation(Element parent, Element child, Counter counter)
    {
        int contentIndex = 0;
        int elementCounter = 0;
        Iterator it = parent.getContent().iterator();
        Text lastText = null;
        int offset = 0;
        while (it.hasNext() && elementCounter <= counter.getCurrentIndex()) {
            Object next = it.next();
            offset = offset + 1;
            if (next instanceof Element) {
                elementCounter = elementCounter + 1;
                contentIndex = contentIndex + offset;
                offset = 0;
            }
            if (next instanceof Text && it.hasNext()) {
                lastText = (Text)next;
            }
        }
        if (lastText != null && lastText.getTextTrim().length() == 0) {
            lastText = (Text)lastText.clone();
        } else {
            String starter = lineSeparator;
            for (int i = 0; i < counter.getDepth(); i++) {
                starter = starter + "    "; //TODO make settable?
            }
            lastText = factory.text(starter);
        }
        if (parent.getContentSize() == 0) {
            Text finalText = (Text)lastText.clone();
            finalText.setText(finalText.getText().substring(0, finalText.getText().length() - "    ".length()));
            parent.addContent(contentIndex, finalText);
        }
        parent.addContent(contentIndex, child);
        parent.addContent(contentIndex, lastText);
    } //-- void insertAtPreferredLocation(Element, Element, Counter) 

    /**
     * Method iterateMirror.
     * 
     * @param counter
     * @param childTag
     * @param parentTag
     * @param list
     * @param parent
     */
    protected void iterateMirror(Counter counter, Element parent, java.util.Collection list, java.lang.String parentTag, java.lang.String childTag)
    {
        boolean shouldExist = list != null && list.size() > 0;
        Element element = updateElement(counter, parent, parentTag, shouldExist);
        if (shouldExist) {
            Iterator it = list.iterator();
            Iterator elIt = element.getChildren(childTag, element.getNamespace()).iterator();
            if (!elIt.hasNext()) elIt = null;
            Counter innerCount = new Counter(counter.getDepth() + 1);
            while (it.hasNext()) {
                Mirror value = (Mirror) it.next();
                Element el;
                if (elIt != null && elIt.hasNext()) {
                    el = (Element) elIt.next();
                    if (! elIt.hasNext()) elIt = null;
                } else {
                    el = factory.element(childTag, element.getNamespace());
                    insertAtPreferredLocation(element, el, innerCount);
                }
                updateMirror(value, childTag, innerCount, el);
                innerCount.increaseCount();
            }
            if (elIt != null) {
                while (elIt.hasNext()) {
                    elIt.next();
                    elIt.remove();
                }
            }
        }
    } //-- void iterateMirror(Counter, Element, java.util.Collection, java.lang.String, java.lang.String) 

    /**
     * Method iterateProfile.
     * 
     * @param counter
     * @param childTag
     * @param parentTag
     * @param list
     * @param parent
     */
    protected void iterateProfile(Counter counter, Element parent, java.util.Collection list, java.lang.String parentTag, java.lang.String childTag)
    {
        boolean shouldExist = list != null && list.size() > 0;
        Element element = updateElement(counter, parent, parentTag, shouldExist);
        if (shouldExist) {
            Iterator it = list.iterator();
            Iterator elIt = element.getChildren(childTag, element.getNamespace()).iterator();
            if (!elIt.hasNext()) elIt = null;
            Counter innerCount = new Counter(counter.getDepth() + 1);
            while (it.hasNext()) {
                Profile value = (Profile) it.next();
                Element el;
                if (elIt != null && elIt.hasNext()) {
                    el = (Element) elIt.next();
                    if (! elIt.hasNext()) elIt = null;
                } else {
                    el = factory.element(childTag, element.getNamespace());
                    insertAtPreferredLocation(element, el, innerCount);
                }
                updateProfile(value, childTag, innerCount, el);
                innerCount.increaseCount();
            }
            if (elIt != null) {
                while (elIt.hasNext()) {
                    elIt.next();
                    elIt.remove();
                }
            }
        }
    } //-- void iterateProfile(Counter, Element, java.util.Collection, java.lang.String, java.lang.String) 

    /**
     * Method iterateProxy.
     * 
     * @param counter
     * @param childTag
     * @param parentTag
     * @param list
     * @param parent
     */
    protected void iterateProxy(Counter counter, Element parent, java.util.Collection list, java.lang.String parentTag, java.lang.String childTag)
    {
        boolean shouldExist = list != null && list.size() > 0;
        Element element = updateElement(counter, parent, parentTag, shouldExist);
        if (shouldExist) {
            Iterator it = list.iterator();
            Iterator elIt = element.getChildren(childTag, element.getNamespace()).iterator();
            if (!elIt.hasNext()) elIt = null;
            Counter innerCount = new Counter(counter.getDepth() + 1);
            while (it.hasNext()) {
                Proxy value = (Proxy) it.next();
                Element el;
                if (elIt != null && elIt.hasNext()) {
                    el = (Element) elIt.next();
                    if (! elIt.hasNext()) elIt = null;
                } else {
                    el = factory.element(childTag, element.getNamespace());
                    insertAtPreferredLocation(element, el, innerCount);
                }
                updateProxy(value, childTag, innerCount, el);
                innerCount.increaseCount();
            }
            if (elIt != null) {
                while (elIt.hasNext()) {
                    elIt.next();
                    elIt.remove();
                }
            }
        }
    } //-- void iterateProxy(Counter, Element, java.util.Collection, java.lang.String, java.lang.String) 

    /**
     * Method iterateRepository.
     * 
     * @param counter
     * @param childTag
     * @param parentTag
     * @param list
     * @param parent
     */
    protected void iterateRepository(Counter counter, Element parent, java.util.Collection list, java.lang.String parentTag, java.lang.String childTag)
    {
        boolean shouldExist = list != null && list.size() > 0;
        Element element = updateElement(counter, parent, parentTag, shouldExist);
        if (shouldExist) {
            Iterator it = list.iterator();
            Iterator elIt = element.getChildren(childTag, element.getNamespace()).iterator();
            if (!elIt.hasNext()) elIt = null;
            Counter innerCount = new Counter(counter.getDepth() + 1);
            while (it.hasNext()) {
                Repository value = (Repository) it.next();
                Element el;
                if (elIt != null && elIt.hasNext()) {
                    el = (Element) elIt.next();
                    if (! elIt.hasNext()) elIt = null;
                } else {
                    el = factory.element(childTag, element.getNamespace());
                    insertAtPreferredLocation(element, el, innerCount);
                }
                updateRepository(value, childTag, innerCount, el);
                innerCount.increaseCount();
            }
            if (elIt != null) {
                while (elIt.hasNext()) {
                    elIt.next();
                    elIt.remove();
                }
            }
        }
    } //-- void iterateRepository(Counter, Element, java.util.Collection, java.lang.String, java.lang.String) 

    /**
     * Method iterateServer.
     * 
     * @param counter
     * @param childTag
     * @param parentTag
     * @param list
     * @param parent
     */
    protected void iterateServer(Counter counter, Element parent, java.util.Collection list, java.lang.String parentTag, java.lang.String childTag)
    {
        boolean shouldExist = list != null && list.size() > 0;
        Element element = updateElement(counter, parent, parentTag, shouldExist);
        if (shouldExist) {
            Iterator it = list.iterator();
            Iterator elIt = element.getChildren(childTag, element.getNamespace()).iterator();
            if (!elIt.hasNext()) elIt = null;
            Counter innerCount = new Counter(counter.getDepth() + 1);
            while (it.hasNext()) {
                Server value = (Server) it.next();
                Element el;
                if (elIt != null && elIt.hasNext()) {
                    el = (Element) elIt.next();
                    if (! elIt.hasNext()) elIt = null;
                } else {
                    el = factory.element(childTag, element.getNamespace());
                    insertAtPreferredLocation(element, el, innerCount);
                }
                updateServer(value, childTag, innerCount, el);
                innerCount.increaseCount();
            }
            if (elIt != null) {
                while (elIt.hasNext()) {
                    elIt.next();
                    elIt.remove();
                }
            }
        }
    } //-- void iterateServer(Counter, Element, java.util.Collection, java.lang.String, java.lang.String) 

    /**
     * Method replaceXpp3DOM.
     * 
     * @param parent
     * @param counter
     * @param parentDom
     */
    protected void replaceXpp3DOM(Element parent, Xpp3Dom parentDom, Counter counter)
    {
        if (parentDom.getChildCount() > 0) {
            Xpp3Dom[] childs = parentDom.getChildren();
            Collection domChilds = new ArrayList();
            for (int i = 0; i < childs.length; i++) {
                domChilds.add(childs[i]);
            }
            int domIndex = 0;
            ListIterator it = parent.getChildren().listIterator();
            while (it.hasNext()) {
                Element elem = (Element) it.next();
                Iterator it2 = domChilds.iterator();
                Xpp3Dom corrDom = null;
                while (it2.hasNext()) {
                    Xpp3Dom dm = (Xpp3Dom)it2.next();
                    if (dm.getName().equals(elem.getName())) {
                        corrDom = dm;
                        break;
                    }
                }
                if (corrDom != null) {
                    domChilds.remove(corrDom);
                    replaceXpp3DOM(elem, corrDom, new Counter(counter.getDepth() + 1));
                    counter.increaseCount();
                } else {
                    parent.removeContent(elem);
                }
            }
            Iterator it2 = domChilds.iterator();
            while (it2.hasNext()) {
                Xpp3Dom dm = (Xpp3Dom) it2.next();
                Element elem = factory.element(dm.getName(), parent.getNamespace());
                insertAtPreferredLocation(parent, elem, counter);
                counter.increaseCount();
                replaceXpp3DOM(elem, dm, new Counter(counter.getDepth() + 1));
            }
        } else if (parentDom.getValue() != null) {
            parent.setText(parentDom.getValue());
        }
    } //-- void replaceXpp3DOM(Element, Xpp3Dom, Counter) 

    /**
     * Method updateActivation.
     * 
     * @param value
     * @param element
     * @param counter
     * @param xmlTag
     */
    protected void updateActivation(Activation value, String xmlTag, Counter counter, Element element)
    {
        boolean shouldExist = value != null;
        Element root = updateElement(counter, element, xmlTag, shouldExist);
        if (shouldExist) {
            Counter innerCount = new Counter(counter.getDepth() + 1);
            findAndReplaceSimpleElement(innerCount, root,  "activeByDefault", value.isActiveByDefault() == false ? null : String.valueOf( value.isActiveByDefault() ), "false");
            findAndReplaceSimpleElement(innerCount, root,  "jdk", value.getJdk(), null);
            updateActivationOS( value.getOs(), "os", innerCount, root);
            updateActivationProperty( value.getProperty(), "property", innerCount, root);
            updateActivationFile( value.getFile(), "file", innerCount, root);
        }
    } //-- void updateActivation(Activation, String, Counter, Element) 

    /**
     * Method updateActivationFile.
     * 
     * @param value
     * @param element
     * @param counter
     * @param xmlTag
     */
    protected void updateActivationFile(ActivationFile value, String xmlTag, Counter counter, Element element)
    {
        boolean shouldExist = value != null;
        Element root = updateElement(counter, element, xmlTag, shouldExist);
        if (shouldExist) {
            Counter innerCount = new Counter(counter.getDepth() + 1);
            findAndReplaceSimpleElement(innerCount, root,  "missing", value.getMissing(), null);
            findAndReplaceSimpleElement(innerCount, root,  "exists", value.getExists(), null);
        }
    } //-- void updateActivationFile(ActivationFile, String, Counter, Element) 

    /**
     * Method updateActivationOS.
     * 
     * @param value
     * @param element
     * @param counter
     * @param xmlTag
     */
    protected void updateActivationOS(ActivationOS value, String xmlTag, Counter counter, Element element)
    {
        boolean shouldExist = value != null;
        Element root = updateElement(counter, element, xmlTag, shouldExist);
        if (shouldExist) {
            Counter innerCount = new Counter(counter.getDepth() + 1);
            findAndReplaceSimpleElement(innerCount, root,  "name", value.getName(), null);
            findAndReplaceSimpleElement(innerCount, root,  "family", value.getFamily(), null);
            findAndReplaceSimpleElement(innerCount, root,  "arch", value.getArch(), null);
            findAndReplaceSimpleElement(innerCount, root,  "version", value.getVersion(), null);
        }
    } //-- void updateActivationOS(ActivationOS, String, Counter, Element) 

    /**
     * Method updateActivationProperty.
     * 
     * @param value
     * @param element
     * @param counter
     * @param xmlTag
     */
    protected void updateActivationProperty(ActivationProperty value, String xmlTag, Counter counter, Element element)
    {
        boolean shouldExist = value != null;
        Element root = updateElement(counter, element, xmlTag, shouldExist);
        if (shouldExist) {
            Counter innerCount = new Counter(counter.getDepth() + 1);
            findAndReplaceSimpleElement(innerCount, root,  "name", value.getName(), null);
            findAndReplaceSimpleElement(innerCount, root,  "value", value.getValue(), null);
        }
    } //-- void updateActivationProperty(ActivationProperty, String, Counter, Element) 

    /**
     * Method updateElement.
     * 
     * @param counter
     * @param shouldExist
     * @param name
     * @param parent
     * @return Element
     */
    protected Element updateElement(Counter counter, Element parent, String name, boolean shouldExist)
    {
        Element element =  parent.getChild(name, parent.getNamespace());
        if (element != null && shouldExist) {
            counter.increaseCount();
        }
        if (element == null && shouldExist) {
            element = factory.element(name, parent.getNamespace());
            insertAtPreferredLocation(parent, element, counter);
            counter.increaseCount();
        }
        if (!shouldExist && element != null) {
            int index = parent.indexOf(element);
            if (index > 0) {
                Content previous = parent.getContent(index - 1);
                if (previous instanceof Text) {
                    Text txt = (Text)previous;
                    if (txt.getTextTrim().length() == 0) {
                        parent.removeContent(txt);
                    }
                }
            }
            parent.removeContent(element);
        }
        return element;
    } //-- Element updateElement(Counter, Element, String, boolean) 

    /**
     * Method updateIdentifiableBase.
     * 
     * @param value
     * @param element
     * @param counter
     * @param xmlTag
     */
    protected void updateIdentifiableBase(IdentifiableBase value, String xmlTag, Counter counter, Element element)
    {
        boolean shouldExist = value != null;
        Element root = updateElement(counter, element, xmlTag, shouldExist);
        if (shouldExist) {
            Counter innerCount = new Counter(counter.getDepth() + 1);
            findAndReplaceSimpleElement(innerCount, root,  "id", value.getId(), null);
        }
    } //-- void updateIdentifiableBase(IdentifiableBase, String, Counter, Element) 

    /**
     * Method updateMirror.
     * 
     * @param value
     * @param element
     * @param counter
     * @param xmlTag
     */
    protected void updateMirror(Mirror value, String xmlTag, Counter counter, Element element)
    {
        Element root = element;
        Counter innerCount = new Counter(counter.getDepth() + 1);
        findAndReplaceSimpleElement(innerCount, root,  "mirrorOf", value.getMirrorOf(), null);
        findAndReplaceSimpleElement(innerCount, root,  "name", value.getName(), null);
        findAndReplaceSimpleElement(innerCount, root,  "url", value.getUrl(), null);
        findAndReplaceSimpleElement(innerCount, root,  "id", value.getId(), null);
    } //-- void updateMirror(Mirror, String, Counter, Element) 

    /**
     * Method updateProfile.
     * 
     * @param value
     * @param element
     * @param counter
     * @param xmlTag
     */
    protected void updateProfile(Profile value, String xmlTag, Counter counter, Element element)
    {
        Element root = element;
        Counter innerCount = new Counter(counter.getDepth() + 1);
        updateActivation( value.getActivation(), "activation", innerCount, root);
        findAndReplaceProperties(innerCount, root,  "properties", value.getProperties());
        iterateRepository(innerCount, root, value.getRepositories(),"repositories","repository");
        iterateRepository(innerCount, root, value.getPluginRepositories(),"pluginRepositories","pluginRepository");
        findAndReplaceSimpleElement(innerCount, root,  "id", value.getId(), null);
    } //-- void updateProfile(Profile, String, Counter, Element) 

    /**
     * Method updateProxy.
     * 
     * @param value
     * @param element
     * @param counter
     * @param xmlTag
     */
    protected void updateProxy(Proxy value, String xmlTag, Counter counter, Element element)
    {
        Element root = element;
        Counter innerCount = new Counter(counter.getDepth() + 1);
        findAndReplaceSimpleElement(innerCount, root,  "active", value.isActive() == false ? null : String.valueOf( value.isActive() ), "false");
        findAndReplaceSimpleElement(innerCount, root,  "protocol", value.getProtocol(), "http");
        findAndReplaceSimpleElement(innerCount, root,  "username", value.getUsername(), null);
        findAndReplaceSimpleElement(innerCount, root,  "password", value.getPassword(), null);
        findAndReplaceSimpleElement(innerCount, root,  "port", value.getPort() == 8080 ? null : String.valueOf( value.getPort() ), "8080");
        findAndReplaceSimpleElement(innerCount, root,  "host", value.getHost(), null);
        findAndReplaceSimpleElement(innerCount, root,  "nonProxyHosts", value.getNonProxyHosts(), null);
        findAndReplaceSimpleElement(innerCount, root,  "id", value.getId(), null);
    } //-- void updateProxy(Proxy, String, Counter, Element) 

    /**
     * Method updateRepository.
     * 
     * @param value
     * @param element
     * @param counter
     * @param xmlTag
     */
    protected void updateRepository(Repository value, String xmlTag, Counter counter, Element element)
    {
        Element root = element;
        Counter innerCount = new Counter(counter.getDepth() + 1);
        updateRepositoryPolicy( value.getReleases(), "releases", innerCount, root);
        updateRepositoryPolicy( value.getSnapshots(), "snapshots", innerCount, root);
        findAndReplaceSimpleElement(innerCount, root,  "id", value.getId(), null);
        findAndReplaceSimpleElement(innerCount, root,  "name", value.getName(), null);
        findAndReplaceSimpleElement(innerCount, root,  "url", value.getUrl(), null);
        findAndReplaceSimpleElement(innerCount, root,  "layout", value.getLayout(), "default");
    } //-- void updateRepository(Repository, String, Counter, Element) 

    /**
     * Method updateRepositoryBase.
     * 
     * @param value
     * @param element
     * @param counter
     * @param xmlTag
     */
    protected void updateRepositoryBase(RepositoryBase value, String xmlTag, Counter counter, Element element)
    {
        boolean shouldExist = value != null;
        Element root = updateElement(counter, element, xmlTag, shouldExist);
        if (shouldExist) {
            Counter innerCount = new Counter(counter.getDepth() + 1);
            findAndReplaceSimpleElement(innerCount, root,  "id", value.getId(), null);
            findAndReplaceSimpleElement(innerCount, root,  "name", value.getName(), null);
            findAndReplaceSimpleElement(innerCount, root,  "url", value.getUrl(), null);
            findAndReplaceSimpleElement(innerCount, root,  "layout", value.getLayout(), "default");
        }
    } //-- void updateRepositoryBase(RepositoryBase, String, Counter, Element) 

    /**
     * Method updateRepositoryPolicy.
     * 
     * @param value
     * @param element
     * @param counter
     * @param xmlTag
     */
    protected void updateRepositoryPolicy(RepositoryPolicy value, String xmlTag, Counter counter, Element element)
    {
        boolean shouldExist = value != null;
        Element root = updateElement(counter, element, xmlTag, shouldExist);
        if (shouldExist) {
            Counter innerCount = new Counter(counter.getDepth() + 1);
            findAndReplaceSimpleElement(innerCount, root,  "enabled", value.isEnabled() == true ? null : String.valueOf( value.isEnabled() ), "true");
            findAndReplaceSimpleElement(innerCount, root,  "updatePolicy", value.getUpdatePolicy(), null);
            findAndReplaceSimpleElement(innerCount, root,  "checksumPolicy", value.getChecksumPolicy(), null);
        }
    } //-- void updateRepositoryPolicy(RepositoryPolicy, String, Counter, Element) 

    /**
     * Method updateServer.
     * 
     * @param value
     * @param element
     * @param counter
     * @param xmlTag
     */
    protected void updateServer(Server value, String xmlTag, Counter counter, Element element)
    {
        Element root = element;
        Counter innerCount = new Counter(counter.getDepth() + 1);
        findAndReplaceSimpleElement(innerCount, root,  "username", value.getUsername(), null);
        findAndReplaceSimpleElement(innerCount, root,  "password", value.getPassword(), null);
        findAndReplaceSimpleElement(innerCount, root,  "privateKey", value.getPrivateKey(), null);
        findAndReplaceSimpleElement(innerCount, root,  "passphrase", value.getPassphrase(), null);
        findAndReplaceSimpleElement(innerCount, root,  "filePermissions", value.getFilePermissions(), null);
        findAndReplaceSimpleElement(innerCount, root,  "directoryPermissions", value.getDirectoryPermissions(), null);
        findAndReplaceXpp3DOM(innerCount, root, "configuration", (Xpp3Dom)value.getConfiguration());
        findAndReplaceSimpleElement(innerCount, root,  "id", value.getId(), null);
    } //-- void updateServer(Server, String, Counter, Element) 

    /**
     * Method updateSettings.
     * 
     * @param value
     * @param element
     * @param counter
     * @param xmlTag
     */
    protected void updateSettings(Settings value, String xmlTag, Counter counter, Element element)
    {
        Element root = element;
        Counter innerCount = new Counter(counter.getDepth() + 1);
        findAndReplaceSimpleElement(innerCount, root,  "localRepository", value.getLocalRepository(), null);
        findAndReplaceSimpleElement(innerCount, root,  "interactiveMode", value.isInteractiveMode() == true ? null : String.valueOf( value.isInteractiveMode() ), "true");
        findAndReplaceSimpleElement(innerCount, root,  "usePluginRegistry", value.isUsePluginRegistry() == false ? null : String.valueOf( value.isUsePluginRegistry() ), "false");
        findAndReplaceSimpleElement(innerCount, root,  "offline", value.isOffline() == false ? null : String.valueOf( value.isOffline() ), "false");
        iterateProxy(innerCount, root, value.getProxies(),"proxies","proxy");
        iterateServer(innerCount, root, value.getServers(),"servers","server");
        iterateMirror(innerCount, root, value.getMirrors(),"mirrors","mirror");
        iterateProfile(innerCount, root, value.getProfiles(),"profiles","profile");
        findAndReplaceSimpleLists(innerCount, root, value.getActiveProfiles(), "activeProfiles", "activeProfile");
        findAndReplaceSimpleLists(innerCount, root, value.getPluginGroups(), "pluginGroups", "pluginGroup");
    } //-- void updateSettings(Settings, String, Counter, Element) 

    /**
     * Method updateTrackableBase.
     * 
     * @param value
     * @param element
     * @param counter
     * @param xmlTag
     */
    protected void updateTrackableBase(TrackableBase value, String xmlTag, Counter counter, Element element)
    {
        boolean shouldExist = value != null;
        Element root = updateElement(counter, element, xmlTag, shouldExist);
        if (shouldExist) {
            Counter innerCount = new Counter(counter.getDepth() + 1);
        }
    } //-- void updateTrackableBase(TrackableBase, String, Counter, Element) 

    /**
     * Method write.
     * @deprecated
     * 
     * @param settings
     * @param stream
     * @param document
     * @throws java.io.IOException
     */
    public void write(Settings settings, Document document, OutputStream stream)
        throws java.io.IOException
    {
        updateSettings(settings, "settings", new Counter(0), document.getRootElement());
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat()
        .setIndent("    ")
        .setLineSeparator(System.getProperty("line.separator")));
        outputter.output(document, stream);
    } //-- void write(Settings, Document, OutputStream) 

    /**
     * Method write.
     * 
     * @param settings
     * @param writer
     * @param document
     * @throws java.io.IOException
     */
    public void write(Settings settings, Document document, OutputStreamWriter writer)
        throws java.io.IOException
    {
        Format format = Format.getRawFormat()
        .setEncoding(writer.getEncoding())
        .setLineSeparator(System.getProperty("line.separator"));
        write(settings, document, writer, format);
    } //-- void write(Settings, Document, OutputStreamWriter) 

    /**
     * Method write.
     * 
     * @param settings
     * @param jdomFormat
     * @param writer
     * @param document
     * @throws java.io.IOException
     */
    public void write(Settings settings, Document document, Writer writer, Format jdomFormat)
        throws java.io.IOException
    {
        updateSettings(settings, "settings", new Counter(0), document.getRootElement());
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(jdomFormat);
        outputter.output(document, writer);
    } //-- void write(Settings, Document, Writer, Format) 


      //-----------------/
     //- Inner Classes -/
    //-----------------/

    /**
     * Class Counter.
     * 
     * @version $Revision$ $Date$
     */
    public class Counter {


          //--------------------------/
         //- Class/Member Variables -/
        //--------------------------/

        /**
         * Field currentIndex.
         */
        private int currentIndex = 0;

        /**
         * Field level.
         */
        private int level;


          //----------------/
         //- Constructors -/
        //----------------/

        public Counter(int depthLevel) {
            level = depthLevel;
        } //-- org.apache.maven.settings.io.jdom.Counter(int)


          //-----------/
         //- Methods -/
        //-----------/

        /**
         * Method getCurrentIndex.
         * 
         * @return int
         */
        public int getCurrentIndex()
        {
            return currentIndex;
        } //-- int getCurrentIndex() 

        /**
         * Method getDepth.
         * 
         * @return int
         */
        public int getDepth()
        {
            return level;
        } //-- int getDepth() 

        /**
         * Method increaseCount.
         */
        public void increaseCount()
        {
            currentIndex = currentIndex + 1;
        } //-- void increaseCount() 


}


}
