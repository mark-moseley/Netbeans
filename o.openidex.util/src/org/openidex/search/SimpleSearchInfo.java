/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openidex.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import org.openide.loaders.DataObject;
import org.openidex.search.SearchInfo;

/**
 * Simple implementation of interface <code>SearchInfo</code>.
 * This implementation allows to easily create <code>SearchInfo</code> objects
 * from <code>DataObject</code> containers. It also allows to nest other
 * <code>SearchInfo</code> objects.
 *
 * @author  Marian Petras
 */
public class SimpleSearchInfo implements SearchInfo {

    public static final SearchInfo EMPTY_SEARCH_INFO
        = new SearchInfo() {
            public Iterator objectsToSearch() {
                return Collections.EMPTY_LIST.iterator();
            }
        };

    /**
     */
    private DataObject.Container contentsBase;

    /**
     */
    private List extraContents;

    /**
     */
    private boolean recursive;
    
    /**
     */
    public SimpleSearchInfo() {
    }
    
    /** Creates a new instance of SimpleSearchInfo */
    public SimpleSearchInfo(DataObject.Container container) {
        this(container, true);
    }
    
    /** Creates a new instance of SimpleSearchInfo */
    public SimpleSearchInfo(DataObject.Container container, boolean recursive) {
        contentsBase = container;
        this.recursive = recursive;
    }
    
    /**
     */
    public void add(DataObject dataObject) {
        if (extraContents == null) {
            extraContents = new ArrayList(4);
        }
        extraContents.add(dataObject);
    }
    
    /**
     */
    public void add(SearchInfo nestedInfo) {
        if (extraContents == null) {
            extraContents = new ArrayList(4);
        }
        extraContents.add(nestedInfo);
    }

    /**
     */
    public void remove(DataObject dataObject) {
        if (extraContents != null) {
            extraContents.remove(dataObject);
        }
    }

    /**
     */
    public void remove(SearchInfo nestedInfo) {
        if (extraContents != null) {
            extraContents.remove(nestedInfo);
        }
    }

    /**
     */
    public Iterator objectsToSearch() {

        /* make a copy of contents base: */
        DataObject[] base = null;
        if (contentsBase != null) {
            base = contentsBase.getChildren();
        }

        /* compute the total size of contents: */
        int totalSize = 0;
        if (base != null) {
            totalSize += base.length;
        }
        if (extraContents != null) {
            totalSize += extraContents.size();
        }

        /* return an empty iterator if the search info is empty: */
        if (totalSize == 0) {
            return Collections.EMPTY_LIST.iterator();
        }

        return new SimpleSearchInfoIterator(
                base,
                extraContents != null
                        ? Collections.unmodifiableList(extraContents)
                        : null,
                recursive);
    }
    
    static class SimpleSearchInfoIterator implements Iterator {

        /** */
        DataObject[] contentsBase;
        /** */
        List extraContents;
        /** */
        int contentsBaseIndex = 0;
        /** */
        int contentsBaseSize = 0;
        /** */
        Iterator nestedIterator;
        /** */
        ListIterator extraIterator;

        /**
         * should <code>DataObject.Container</code>s present
         * in the {@link #contentsBase} be handled recursively?
         */
        boolean recursive;

        /**
         */
        SimpleSearchInfoIterator(DataObject[] contentsBase,
                                 List extraContents,
                                 boolean recursive) {
            this.contentsBase = contentsBase;
            this.contentsBaseSize = contentsBase != null ? contentsBase.length
                                                         : 0;
            this.extraContents = extraContents;
            this.recursive = recursive;
        }
        
        /**
         * Checks whether the nested iterator has next element and deletes
         * the iterator if not.
         * This method ensures that:
         * <ul>
         *     <li>either <code>true</code> is returned</li>
         *     <li>or the nested iterator is <code>null</code> upon return
         * </ul>
         *
         * @return  <code>true</code> if the nested iterator contains
         *          at least one more element; <code>false</code> otherwise
         */
        private boolean checkNestedIterator() {
            assert nestedIterator != null;

            if (nestedIterator.hasNext()) {
                return true;
            } else {
                nestedIterator = null;
                return false;
            }
        }
        
        /**
         */
        public boolean hasNext() {

            if (nestedIterator != null && checkNestedIterator()) {
                return true;
            }
            
            /* Look through the contents base for any element: */
            while (contentsBaseIndex < contentsBaseSize) {
                Object dataObject = contentsBase[contentsBaseIndex];
                if (recursive && (dataObject instanceof DataObject.Container)) {
                    contentsBaseIndex++;
                    nestedIterator = new DataObjectContainerIterator(
                            (DataObject.Container) dataObject);
                    if (checkNestedIterator()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }

            /*
             * Contents base is now exhausted. Let's have a look at extra
             * contents...
             */

            /*
             * Check if there is some extra contents and create an iterator for
             * it if it does not exist yet:
             */
            if (extraIterator == null) {
                if (extraContents == null || extraContents.isEmpty()) {
                    return false;
                } else {
                    extraIterator = extraContents.listIterator();
                }
            }

            /* Look through the extra contents for any element: */
            while (extraIterator.hasNext()) {
                Object o = extraIterator.next();
                if (o instanceof SearchInfo) {
                    nestedIterator = ((SearchInfo) o).objectsToSearch();
                    if (checkNestedIterator()) {
                        return true;
                    }
                } else {
                    
                    /*
                     * make sure the next call to Iterator.next() returns
                     * the same element:
                     */
                    extraIterator.previous();
                    return true;
                }
            }
            extraIterator = null;

            /*
             * We found no element in neither contents base nor in extra
             * contents:
             */
            return false;
        }

        /**
         */
        public Object next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            if (nestedIterator != null) {

                /*
                 * We know that the nested iterator has next - see method
                 * checkNestedIterator().
                 */
                return nestedIterator.next();
            }

            if (contentsBaseIndex < contentsBaseSize) {

                /*
                 * We know that the next element is not a container to be
                 * processed recursively - nestedIterator would be non-null
                 * in such a case - see method checkNestedIterator().
                 */
                return contentsBase[contentsBaseIndex++];
            }

            /*
             * Method hasNext() ensures that extraIterator is non-null
             * if and only if extra contents exists and has at least
             * one next element.
             */
            return extraIterator.next();
        }
        
        /**
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    /**
     *
     */
    static class DataObjectContainerIterator implements Iterator {

        /** */
        private DataObject[] contents;

        /** */
        private int index = 0;

        /**
         */
        private Iterator nestedIterator;

        /**
         */
        DataObjectContainerIterator(DataObject.Container container) {
            contents = container.getChildren();
            if ((contents != null) && (contents.length == 0)) {
                contents = null;
            }
        }

        /**
         */
        private boolean checkNestedIterator() {
            assert nestedIterator != null;

            if (nestedIterator.hasNext()) {
                return true;
            } else {
                nestedIterator = null;
                return false;
            }
        }
        
        /**
         */
        public boolean hasNext() {
            if (contents == null) {
                return false;
            }

            if (nestedIterator != null && checkNestedIterator()) {
                return true;
            }

            while (index < contents.length) {
                Object dataObject = contents[index];
                if (dataObject instanceof DataObject.Container) {
                    index++;
                    nestedIterator = new DataObjectContainerIterator(
                            (DataObject.Container) dataObject);
                    if (checkNestedIterator()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }

            return false;
        }

        /**
         */
        public Object next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            if (nestedIterator != null) {
                return nestedIterator.next();
            }
            return contents[index++];
        }
        
        /**
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
}
