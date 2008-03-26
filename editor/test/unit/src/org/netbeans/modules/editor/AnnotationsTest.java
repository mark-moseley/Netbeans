
package org.netbeans.modules.editor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.text.EditorKit;
import org.netbeans.editor.AnnotationDesc;
import org.netbeans.editor.AnnotationType;
import org.netbeans.editor.AnnotationTypes;
import org.netbeans.editor.Annotations;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.options.AnnotationTypeProcessor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.text.Annotation;
import org.openide.util.Lookup;

/**
 * Test the annotations attached to the editor.
 *
 * @author Miloslav Metelka
 */
public class AnnotationsTest extends BaseDocumentUnitTestCase {
    
    public AnnotationsTest(String testMethodName) {
        super(testMethodName);
        
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    
        EditorTestLookup.setLookup(
            new URL[] {
                EditorTestConstants.EDITOR_LAYER_URL,
                getClass().getClassLoader().getResource(
                        "org/netbeans/modules/editor/resources/annotations-test-layer.xml")
            },
            new Object[] {},
            getClass().getClassLoader()
        );
            
        AnnotationTypes.getTypes().registerLoader(new AnnotationsLoader());
    }

    public void testAnnotations() throws Exception {
        setLoadDocumentText(
            "a\n"
          + "b\n"
          + "c"  
        );
        
        BaseDocument doc = getDocument();
        if (!(doc instanceof NbEditorDocument)) {
            fail("NbEditorDocument instance expecxted. createEditorKit() redefined?");
        }
        
        final NbEditorDocument nbDoc = (NbEditorDocument)doc;
        
        class TestAnnotation extends Annotation {
            
            public String getAnnotationType() {
                return "testAnnotation";
            }

            public String getShortDescription() {
                return "Test Annotation";
            }

        }

        // Must run in AWT thread
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    runE();
                } catch (Exception e) {
                    //e.printStackTrace(getLog());
                    e.printStackTrace();
                    fail(e.getMessage());
                }
            }
            
            private void runE() throws Exception {
                // Add annotation to first line
                Annotation testAnnotation0 = new TestAnnotation();
                nbDoc.addAnnotation(nbDoc.createPosition(0), -1, testAnnotation0);
                
                // Add two annotations to second line
                Annotation testAnnotation1 = new TestAnnotation();
                nbDoc.addAnnotation(nbDoc.createPosition(getLineOffset(1)), -1, testAnnotation1);
                Annotation testAnnotation11 = new TestAnnotation();
                nbDoc.addAnnotation(nbDoc.createPosition(getLineOffset(1)), -1, testAnnotation11);
                
                // Check annotations locations
                List line0Annos = getAnnotations(0);
                assertEquals(line0Annos.size(), 1);
                assertSame(line0Annos.get(0), testAnnotation0);
                
                List line1Annos = getAnnotations(1);
                assertEquals(line1Annos.size(), 2);
                assertSame(line1Annos.get(0), testAnnotation1);
                assertSame(line1Annos.get(1), testAnnotation11);
            }
        });
    }
    
    private int getLineOffset(int lineIndex) {
        return getDocument().getDefaultRootElement().getElement(lineIndex).getStartOffset();
    }
    
    protected EditorKit createEditorKit() {
        return new NbEditorKit();
    }

    private Annotations getAnnotations() {
        return getDocument().getAnnotations();
    }

    private Annotations.LineAnnotations getLineAnnotations(int lineIndex) throws Exception {
        Method getLineAnnotations = Annotations.class.getDeclaredMethod("getLineAnnotations", new Class[] { Integer.TYPE });
        getLineAnnotations.setAccessible(true);
        Annotations.LineAnnotations lineAnnotations = (Annotations.LineAnnotations)
            getLineAnnotations.invoke(getAnnotations(), new Object[] { new Integer(lineIndex) });
        return lineAnnotations;
    }
    
    private List getAnnotations(int lineIndex) throws Exception {
        List annotations;
        Annotations.LineAnnotations lineAnnotations = getLineAnnotations(lineIndex);
        if (lineAnnotations != null) {
            annotations = new ArrayList();
            Class[] inners = NbEditorDocument.class.getDeclaredClasses();
            
            for (Iterator it = lineAnnotations.getAnnotations(); it.hasNext();) {
                AnnotationDesc annoDesc = (AnnotationDesc)it.next();
                Annotation anno = ((Lookup.Provider) annoDesc).getLookup().lookup(Annotation.class);
                annotations.add(anno);
            }
            
        } else {
            annotations = Collections.EMPTY_LIST;
        }
        return annotations;
    } 

    /*package private*/ static final class AnnotationsLoader implements AnnotationTypes.Loader {

        public void loadTypes() {
            try {
                Map typesInstances = new HashMap();
                FileObject typesFolder = Repository.getDefault().getDefaultFileSystem().findResource("Editors/AnnotationTypes");
                FileObject[] types = typesFolder.getChildren();
                
                for (int cntr = 0; cntr < types.length; cntr++) {
                    AnnotationTypeProcessor proc = new AnnotationTypeProcessor();
                    
                    System.err.println("CCC:" + types[cntr].getNameExt());
                    if (types[cntr].getName().startsWith("testAnnotation") && "xml".equals(types[cntr].getExt())) {
                        proc.attachTo(types[cntr]);
                        AnnotationType type = (AnnotationType) proc.instanceCreate();
                        typesInstances.put(type.getName(), type);
                    }
                }
                
                AnnotationTypes.getTypes().setTypes(typesInstances);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void loadSettings() {
        }

        public void saveType(AnnotationType type) {
        }

        public void saveSetting(String settingName, Object value) {
        }

    }

}
