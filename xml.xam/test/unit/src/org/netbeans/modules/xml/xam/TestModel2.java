package org.netbeans.modules.xml.xam;

import java.io.File;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.dom.DocumentModelAccess;
import org.netbeans.modules.xml.xam.dom.ReadOnlyAccess;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Nam Nguyen
 */
public class TestModel2 extends AbstractDocumentModel<TestComponent2> implements DocumentModel<TestComponent2> {
    TestComponent2 testRoot;
    ReadOnlyAccess access;
    
    /** Creates a new instance of TestModel */
    public TestModel2(Document doc) {
        super(Util.createModelSource(doc));
        try {
            super.sync();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public TestModel2(ModelSource source) {
        super(source);
    }
    
    private static Factory factory = null;
    public static Factory factory() {
        if (factory == null) {
            factory = new Factory();
        }
        return factory;
    }
    
    public static class Factory extends AbstractModelFactory<TestModel2> {
        private Factory() {
            super();
        }
        
        protected TestModel2 createModel(ModelSource source) {
            return new TestModel2(source);
        }
        
        public TestModel2 getModel(ModelSource source) {
            return super.getModel(source);
        }
    }
    
    public TestComponent2 getRootComponent() {
        if (testRoot == null) {
            testRoot = new TestComponent2(this, "test");
        }
        return testRoot;
    }

    public void addChildComponent(Component target, Component child, int index) {
        TestComponent2 parent = (TestComponent2) target;
        TestComponent2 tc = (TestComponent2) child;
        parent.insertAtIndex(tc.getName(), tc, index > -1 ? index : parent.getChildren().size());
    }

    public void removeChildComponent(Component child) {
        TestComponent2 tc = (TestComponent2) child;
        tc.getParent().removeChild(tc.getName(), tc);
    }

    
    public DocumentModelAccess getAccess() {
        if (access == null) {
            access = new ReadOnlyAccess(this);
        }
        return access;
    }

    public TestComponent2 createRootComponent(org.w3c.dom.Element root) {
        if (TestComponent2.NS_URI.equals(root.getNamespaceURI()) &&
            "test".equals(root.getLocalName())) {
                testRoot = new TestComponent2(this, root);
        } else {
            testRoot = null;
        }
        return testRoot;
    }
    
    public TestComponent2 createComponent(TestComponent2 parent, org.w3c.dom.Element element) {
        return TestComponent2.createComponent(this, parent, element);
    }
    
    protected ComponentUpdater<TestComponent2> getComponentUpdater() {
        return null;
    }
    
}
