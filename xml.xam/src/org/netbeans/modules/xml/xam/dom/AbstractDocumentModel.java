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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xam.dom;

import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xam.AbstractModel;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.spi.DocumentModelAccessProvider;
import org.openide.util.Lookup;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Chris Webster
 * @author Rico
 * @author Nam Nguyen
 */
public abstract class AbstractDocumentModel<T extends DocumentComponent<T>> 
        extends AbstractModel<T> implements DocumentModel<T> {

    protected DocumentModelAccess access;
    private boolean needsSync;
    private DocumentListener docListener;
    private javax.swing.text.Document swingDocument;
    
    public AbstractDocumentModel(ModelSource source) {
        super(source);
	docListener = new DocumentChangeListener();
    }

    public javax.swing.text.Document getBaseDocument() {
        return (javax.swing.text.Document) 
            getModelSource().getLookup().lookup(javax.swing.text.Document.class);
    }
    
    public abstract T createRootComponent(Element root);
    
    public boolean areSameNodes(Node n1, Node n2) {
        return getAccess().areSameNodes(n1, n2);
    }
    
    /**
     * Returns QName of elements used in model.  Domain model implementation needs 
     * to override this to be able to embed elements outside of the domain such as
     * child elements of documentation in schema model.
     * @return full set of element QName's or null if there is no needs for distinction 
     * between domain and non-domain elements.
     */
    public Set<QName> getQNames() { 
        return Collections.emptySet(); 
    }
    
    @Override
    protected boolean needsSync() {
	javax.swing.text.Document lastDoc = swingDocument;
	javax.swing.text.Document currentDoc = (javax.swing.text.Document)
		getModelSource().getLookup().lookup(javax.swing.text.Document.class);
    if (currentDoc == null) {
        swingDocument = null;
        return false;
    }
	if (lastDoc == null || currentDoc != lastDoc) {
	    swingDocument = currentDoc;
	    currentDoc.addDocumentListener(new WeakDocumentListener(docListener, currentDoc));
	}
	return needsSync || !currentDoc.equals(lastDoc);
    }
    
    @Override
    protected void syncStarted() {
        needsSync = false;
        getAccess().unsetDirty();
    }

    @Override
    protected synchronized void syncCompleted() {
        super.syncCompleted();
    }

    
    private synchronized void documentChanged() {
	if (!isIntransaction()) {
        getAccess().setDirty();
	    needsSync = true;
	}
    }

    private static class WeakDocumentListener implements DocumentListener {
	
	public WeakDocumentListener(DocumentListener delegate, 
				    javax.swing.text.Document source) {
	    this.source = source;
	    this.delegate = new WeakReference<DocumentListener>(delegate);
	}
	
	private DocumentListener getDelegate() {
	    DocumentListener l = delegate.get();
	    if (l == null) {
		source.removeDocumentListener(this);
	    }
	    
	    return l;
	}
	
	public void removeUpdate(DocumentEvent e) {
	    DocumentListener l = getDelegate();
	    if (l != null) {
		l.removeUpdate(e);
	    }
	}
	
	public void changedUpdate(DocumentEvent e) {
	    DocumentListener l = getDelegate();
	    if (l != null) {
		l.changedUpdate(e);
	    }
	}
	
	public void insertUpdate(DocumentEvent e) {
	    DocumentListener l = getDelegate();
	    if (l != null) {
		l.insertUpdate(e);
	    }
	}
	
	private javax.swing.text.Document source;
	private WeakReference<DocumentListener> delegate;
    }
    
    private class DocumentChangeListener implements DocumentListener {
	public void removeUpdate(DocumentEvent e) {
	    documentChanged();
	}
	
	public void insertUpdate(DocumentEvent e) {
	    documentChanged();
	}
	
	public void changedUpdate(DocumentEvent e) {
	    // ignore these events as these are not changes
	    // to the document text but the document itself
	}
    }

    protected abstract ComponentUpdater<T> getComponentUpdater();
    
    /**
     * Allows match just by tag name, in case full QName is not available.
     */
    private Set<String> elementNames = null;
    public Set<String> getElementNames() {
        if (elementNames == null) {
            elementNames = new HashSet<String>();
            Set<QName> qnames = getQNames();
            for (QName q : qnames) {
                elementNames.add(q.getLocalPart());
            }
        }
        return elementNames;
    }
    
    public ChangeInfo prepareChangeInfo(List<Node> pathToRoot) {
        assert pathToRoot.size() > 0;
        if (pathToRoot.get(pathToRoot.size()-1) instanceof Document) {
            pathToRoot.remove(pathToRoot.size()-1);
        }
        
        assert pathToRoot.size() > 1;
        Node current = null;
        Element parent = null;
        boolean changedIsDomainElement = true;
        Set<QName> qnames = getQNames();
        Set<String> enames = getElementNames();
        if (qnames != null && qnames.size() > 0) {
            for (int i=0; i<pathToRoot.size(); i++) {
                Node n = pathToRoot.get(i);
                if (! (n instanceof Element)) {
                    changedIsDomainElement = false;
                    continue;
                }
                
                QName q = new QName(getAccess().lookupNamespaceURI(n, pathToRoot), n.getLocalName());
                if (qnames.contains(q)) {
                    current = n;
                    if (i+1 < pathToRoot.size()) {
                        parent = (Element) pathToRoot.get(i+1);
                    }
                    break;
                } else if (changedIsDomainElement == true) {
                    changedIsDomainElement =  false;
                }
            }
        } else {
            Node n = pathToRoot.get(0);
            if (n instanceof Element) {
                current = n;
                parent = (Element) pathToRoot.get(1);
            } else {
                current = pathToRoot.get(1);
                if (pathToRoot.size() > 2) {
                    parent = (Element) pathToRoot.get(2);
                }
                changedIsDomainElement =  false;
            }
        }
        
        if (! changedIsDomainElement) {
            int i = pathToRoot.indexOf(current);
            assert i > 0;
            parent = (Element) current;
            current = pathToRoot.get(i-1);
        }
        
        List<Element> rootToParent = new ArrayList<Element>();
        if (parent != null) {
            for (int i = pathToRoot.indexOf(parent); i<pathToRoot.size(); i++) {
                rootToParent.add(0, (Element)pathToRoot.get(i));
            }
        }
        
        List<Node> otherNodes = new ArrayList<Node>();
        if (parent != null) {
            int iCurrent = pathToRoot.indexOf(current);
            for (int i=0; i < iCurrent; i++) {
                otherNodes.add(0, pathToRoot.get(i));
            }
        }
        
        return new ChangeInfo(parent, current, changedIsDomainElement, rootToParent, otherNodes);
    }
    
    public SyncUnit prepareSyncUnit(ChangeInfo change, SyncUnit order) {
        assert (change.getChangedNode() != null);
        AbstractDocumentComponent parentComponent = (AbstractDocumentComponent) change.getParentComponent();
        if (parentComponent == null) {
            parentComponent = (AbstractDocumentComponent) findComponent(change.getRootToParentPath());
        }
        if (parentComponent == null) {
            throw new IllegalArgumentException("Could not find parent component");
        }
        
        DocumentComponent toRemove = null;
        DocumentComponent toAdd = null;
        boolean changed = false;
        
        if (change.isDomainElement()) {
            if (change.isDomainElementAdded()) {
                toAdd = createChildComponent(parentComponent, change.getChangedElement());
            } else {
    			toRemove = parentComponent.findChildComponent(change.getChangedElement());
                if (toRemove == null) {
                    parentComponent.findChildComponentByIdentity(change.getChangedElement());
                }
            }
        } else {
            changed = true;
        }
        
        if (order == null) {
            order = new SyncUnit(parentComponent);
        }
        
        order.addChange(change);
        if (toRemove != null) order.addToRemoveList(toRemove);
        if (toAdd != null) order.addToAddList(toAdd);
        if (changed) order.setComponentChanged(true);
        return order;
    }
    
    protected void firePropertyChangedEvents(SyncUnit unit) {
        firePropertyChangedEvents(unit, null);
    }
    
    protected void firePropertyChangedEvents(SyncUnit unit, Element oldElement) {
        Set<String> propertyNames = new HashSet(unit.getRemovedAttributes().keySet());
        propertyNames.addAll(unit.getAddedAttributes().keySet());
        for (String name : propertyNames) {
            Attr oldAttr = unit.getRemovedAttributes().get(name);
            Attr newAttr = unit.getAddedAttributes().get(name);
            super.firePropertyChangeEvent(
                    new PropertyChangeEvent(
                    unit.getTarget(), name,
                    oldAttr == null ? null : oldAttr.getValue(),
                    newAttr == null ? null : newAttr.getValue()));
        }
        if (unit.hasTextContentChanges()) {
            super.firePropertyChangeEvent(
                    new PropertyChangeEvent(
                    unit.getTarget(), DocumentComponent.TEXT_CONTENT_PROPERTY,
                    oldElement == null ? "" : getAccess().getXmlFragment(oldElement),
                    getAccess().getXmlFragment(unit.getTarget().getPeer())));
        }
    }
    
    public void processSyncUnit(SyncUnit syncOrder) {
        AbstractDocumentComponent targetComponent = (AbstractDocumentComponent) syncOrder.getTarget();
        assert targetComponent != null;
        // skip target component whose some ancestor removed in previous processed syncUnit
        if (! targetComponent.isInDocumentModel()) {
            return;
        }
        
        Element oldElement = syncOrder.getTarget().getPeer();
        syncOrder.updateTargetReference();
        if (syncOrder.isComponentChanged()) {
            ComponentEvent.EventType changeType = ComponentEvent.EventType.VALUE_CHANGED;
            fireComponentChangedEvent(new ComponentEvent(targetComponent, changeType));
            firePropertyChangedEvents(syncOrder, oldElement);
        }
        
        for (DocumentComponent c : syncOrder.getToRemoveList()) {
            removeChildComponent(c);
        }
        
        for (DocumentComponent c : syncOrder.getToAddList()) {
            Element childElement = (Element) ((AbstractDocumentComponent)c).getPeer();
            int index = targetComponent.findDomainIndex(childElement);
            addChildComponent(targetComponent, c, index);
        }
    }
    
    private DocumentComponent createChildComponent(DocumentComponent parent, Element e) {
        DocumentModel m = (DocumentModel) parent.getModel();
        assert m != null : "Cannot create child component from a deleted component.";
        return m.createComponent(parent, e);
    }
    
    public void addChildComponent(Component target, Component child, int index) {
        AbstractDocumentModel m = (AbstractDocumentModel)target.getModel();
        //assert m != null : "Cannot add child to a deleted component.";
        //Work-around xdm overlapping in firing
        if (m == null) return;
        m.getComponentUpdater().update(target, child, index, ComponentUpdater.Operation.ADD);
    }
    
    public void removeChildComponent(Component child) {
        if (child.getParent() == null) return;
        AbstractDocumentModel m = (AbstractDocumentModel) child.getParent().getModel();
        //Work-around xdm overlapping in firing
        //assert m != null : "Cannot remove child from a deleted component.";
        if (m == null) return;
        m.getComponentUpdater().update(child.getParent(), child, ComponentUpdater.Operation.REMOVE);
    }
    
    public DocumentComponent findComponent(Element e) {
        return findComponent((AbstractDocumentComponent) getRootComponent(), e);
    }
    
    private DocumentComponent findComponent(DocumentComponent searchRoot, Element e) {
        if (searchRoot.referencesSameNode(e)) {
            return searchRoot;
        }
        for (Object o : searchRoot.getChildren()) {
            DocumentComponent found = findComponent((DocumentComponent) o, e);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
    
    /**
     * Find the component given a path to its element node from root.  All elements, except for
     * the target element should be in the latest version of the xdm tree.  All components on the
     * path will be updated with latest version elements.
     *
     * Note that returned component could be part of an embedded model, which could be of
     * a different type of model.
     *
     * @param pathFromRoot list of elements from model root to backing element of target component.
     * @return component backed by the last element on pathFromRoot or null if not found.
     */
    public DocumentComponent findComponent(List<Element> pathFromRoot) {
        return findComponent((AbstractDocumentComponent)getRootComponent(), pathFromRoot, 0);
    }
    
    public AbstractDocumentComponent findComponent(AbstractDocumentComponent base, List<Element> pathFromRoot, int current) {
        if (pathFromRoot == null || pathFromRoot.size() <= current) {
            return null;
        }
        Element e = pathFromRoot.get(current);
        if (base.referencesSameNode(e)) {
            if (pathFromRoot.size() == current + 1) {
                base.getChildren(); // make sure children inited
                return base;
            } else {
                for (Object child : base.getChildren()) {
                    AbstractDocumentComponent ac = (AbstractDocumentComponent) child;
                    AbstractDocumentComponent found = findComponent(ac, pathFromRoot, current+1);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        return null;
    }
    
    public DocumentComponent findComponent(int position) {
        if (getState() != State.VALID) {
            return getRootComponent();
        }
            
        Element e = (Element) getAccess().getContainingElement(position);
        if (e == null) {
            return getRootComponent();
        }
        
        List<Element> pathFromRoot = null;
        try {
            pathFromRoot = getAccess().getPathFromRoot(this.getDocument(), e);
        } catch(UnsupportedOperationException ex) {
            // OK
        }
        if (pathFromRoot == null || pathFromRoot.isEmpty()) {
            return findComponent(e);
        } else {
            return findComponent(pathFromRoot);
        }
    }
    
    public String getXPathExpression(DocumentComponent component) {
        Element e = (Element) component.getPeer();
        return getAccess().getXPath(getDocument(), e);
    }

    public org.w3c.dom.Document getDocument() {
        return getAccess().getDocumentRoot();
    }

    public DocumentModelAccess getAccess() { 
        if (access == null) {
            access = getEffectiveAccessProvider().createModelAccess(this);
            if (! (access instanceof ReadOnlyAccess)) {
                access.addUndoableEditListener(this);
                setIdentifyingAttributes();
            }
        }
        return access; 
    }

    private DocumentModelAccessProvider getEffectiveAccessProvider() {
	DocumentModelAccessProvider p = (DocumentModelAccessProvider)
	    getModelSource().getLookup().lookup(DocumentModelAccessProvider.class);
	return p == null ? getAccessProvider() : p;
    }
    
    public static DocumentModelAccessProvider getAccessProvider() {
        DocumentModelAccessProvider provider = (DocumentModelAccessProvider) 
            Lookup.getDefault().lookup(DocumentModelAccessProvider.class);
        if (provider == null) {
            return ReadOnlyAccess.Provider.getInstance();
        }
        return provider;
    }
    
    /**
     * Set the identifying attributes for underlying access to merge.
     */
    protected void setIdentifyingAttributes() {
        ElementIdentity eid = getAccess().getElementIdentity();
        eid.addIdentifier("id");
        eid.addIdentifier("name");
        eid.addIdentifier("ref");
    }

    protected boolean isDomainElement(Node e) {
        if (! (e instanceof Element)) {
            return false;
        }
        
        QName q = new QName(e.getNamespaceURI(), e.getLocalName());
        return getQNames().contains(q) || getElementNames().contains(q.getLocalPart());
    }
    
    @Override
    protected void refresh() {
        Document lastStable = null;
        try {
            lastStable = getDocument();
        } catch(Exception ex) {
            // document is not available when underlying model is broken
        }
        if (lastStable != null && lastStable.getDocumentElement() != null) {
            createRootComponent(lastStable.getDocumentElement());
            setState(State.VALID);
        }
    }
    
    /**
     * Returns QName of all attributes with QName value, sorted by containing 
     * element QName.  
     * Note: if domain model implementation return null, namespace
     * consolidation will not attempt namespace prefix refactoring on each 
     * mutation of the underlying XDM DOM tree.
     */
    public Map<QName,List<QName>> getQNameValuedAttributes() {
        return new HashMap<QName,List<QName>>();
    }
}


