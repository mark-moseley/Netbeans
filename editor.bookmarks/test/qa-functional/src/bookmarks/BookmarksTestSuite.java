/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package bookmarks;

import junit.framework.TestSuite;
import org.netbeans.junit.NbTestSuite;


/**
 * Test adding and removing of bookmarks.
 *
 * @author Miloslav Metelka
 */
  public class BookmarksTestSuite extends NbTestSuite {
      
    public BookmarksTestSuite() {
        super("Bookmarks Tests");
        
        addTestSuite(BookmarksPersistenceTest.class);
    }
    

    public static NbTestSuite suite() {
        return new BookmarksTestSuite();
    }
    
}
