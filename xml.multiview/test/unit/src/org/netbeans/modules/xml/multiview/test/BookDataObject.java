/*
 * BookDataObject.java
 *
 * Created on March 9, 2005, 4:06 PM
 */

package org.netbeans.modules.xml.multiview.test;

import org.netbeans.modules.xml.multiview.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import java.io.IOException;

import org.netbeans.api.xml.cookies.ValidateXMLCookie;
import org.netbeans.api.xml.cookies.CheckXMLCookie;
import org.netbeans.spi.xml.cookies.*;

import org.netbeans.modules.xml.multiview.test.bookmodel.*;

/**
 *
 * @author mkuchtiak
 */
public class BookDataObject extends XmlMultiViewDataObject {
    Book book;
    
    /** Creates a new instance of BookDataObject */  
    public BookDataObject (FileObject pf, BookDataLoader loader) throws DataObjectExistsException {
        super (pf, loader);
        //init (pf,loader);
        org.xml.sax.InputSource in = DataObjectAdapters.inputSource(this);
        CheckXMLCookie checkCookie = new CheckXMLSupport(in);
        getCookieSet().add(checkCookie);
        ValidateXMLCookie validateCookie = new ValidateXMLSupport(in);
        getCookieSet().add(validateCookie);
        try {
            parseDocument(false);
        } catch (IOException ex) {
            System.out.println("ex="+ex);
        }
    }
    /**
     *
     * @param updateModel indicator whether model should be updated
     * @return true in case of success, otherwise false
     * @throws IOException
     */
    protected boolean parseDocument(boolean updateModel) throws IOException {
        if (updateModel) {
            if (book==null) {
                book = getBook();
            } else {
                java.io.InputStream is = getEditorSupport().getInputStream();
                Book newBook = null;
                try {
                    newBook = Book.createGraph(is);
                } catch (RuntimeException ex) {
                    System.out.println("runtime error "+ex);
                }
                if (newBook!=null) {
                    book.merge(newBook, org.netbeans.modules.schema2beans.BaseBean.MERGE_UPDATE);
                }
            }
        } else {
            book = getBook();
        }
        if (book==null) return false;
        else return true;
    }
    
    public Book getBook() throws IOException {
        if (book==null) book = Book.createGraph(FileUtil.toFile(getPrimaryFile()));
        return book;
    }

    /** Update text document from data model. Called when something is changed in visual editor.
    */
    protected String generateDocumentFromModel() {
        return "";
    }
    protected DesignMultiViewDesc[] getMultiViewDesc() {
        return new DesignMultiViewDesc[]{new DesignView(this)};
    }
    
    protected boolean isModelCreated() {
        return book!=null;
    }
    
    private static class DesignView extends DesignMultiViewDesc {
        
        DesignView(BookDataObject dObj) {
            super(dObj, "Design");
        }

        public org.netbeans.core.spi.multiview.MultiViewElement createElement() {
            BookDataObject dObj = (BookDataObject)getDataObject();
            return new BookMultiViewElement((BookDataObject)getDataObject());
        }
        
        public java.awt.Image getIcon() {
            return org.openide.util.Utilities.loadImage("org/netbeans/modules/web/dd/resources/DDDataIcon.gif"); //NOI18N
        }
        
        public String preferredID() {
            return "book_multiview_design";
        }
    }
    
}
