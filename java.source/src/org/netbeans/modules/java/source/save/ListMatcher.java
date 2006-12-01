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
package org.netbeans.modules.java.source.save;

import java.util.*;
import org.netbeans.api.java.lexer.JavaTokenId;
import static org.netbeans.modules.java.source.save.Measure.*;
    
/**
 * Implementation of the Longest Common Subsequence algorithm.
 * That is, given two lists <tt>oldL</tt> and <tt>newL</tt>,
 * this class will find the longest sequence objects
 * that are common and ordered in <tt>oldL</tt> and <tt>newL</tt>.
 * 
 * @author Pavel Flaska
 */
public final class ListMatcher<E> {
    
    // old array of elements
    private final E[] oldL;
    
    // new array of elements
    private final E[] newL;
    
    // contains differences. Filled by compute() method
    private final Stack<ResultItem<E>> result;

    // contains method for distance-measuring
    private final Measure measure;
    
    // create ListMatcher instance
    private ListMatcher(List<? extends E> oldL, List<? extends E> newL, Measure measure) {
        this((E[]) oldL.toArray(), (E[]) newL.toArray(), measure);
    }
    
    // create ListMatcher instance
    private ListMatcher(List<? extends E> oldL, List<? extends E> newL) {
        this((E[]) oldL.toArray(), (E[]) newL.toArray());
    }
    
    // create ListMatcher instance
    private ListMatcher(E[] oldL, E[] newL) {
        this(oldL, newL, null);
    }
    
    private ListMatcher(E[] oldL, E[] newL, Measure measure) {
        this.oldL = oldL;
        this.newL = newL;
        this.measure = measure != null ? measure : Measure.DEFAULT;
        result = new Stack<ResultItem<E>>();
    }

    /**
     * Creates the instance of <tt>ListMatcher</tt> class.
     * 
     * @param  oldL  old array of elements to compare.
     * @param  newL  new array of elements to compare.
     * @return       created instance.
     */
    public static <T> ListMatcher<T> instance(List<? extends T> oldL, List<? extends T> newL) {
        return new ListMatcher<T>(oldL, newL);
    }

    /**
     * Creates the instance of <tt>ListMatcher</tt> class.
     * 
     * @param  oldL  old array of elements to compare.
     * @param  newL  new array of elements to compare.
     * @param  comparator  used for comparing elements.
     * @return       created instance.
     */
    public static <T> ListMatcher<T> instance(List<? extends T> oldL, List<? extends T> newL, Measure measure) {
        return new ListMatcher<T>(oldL, newL, measure);
    }
    /**
     * Creates the instance of <tt>ListMatcher</tt> class.
     * 
     * @param  oldL  old array of elements to compare.
     * @param  newL  new array of elements to compare.
     * @return       created instance.
     */
    public static <T> ListMatcher<T> instance(T[] oldL, T[] newL) {
        return new ListMatcher<T>(oldL, newL);
    }

    /**
     * Represents type of difference.
     */
    public static enum Operation {
        /** Element was inserted. */
        INSERT("insert"),
        
        /** Element was modified. */
        MODIFY("modify"),
        
        /** Element was deleted. */
        DELETE("delete"),
        
        /** Element was not changed, left as it was. */
        NOCHANGE("nochange");
        
        private Operation(String name) {
            this.name = name;
        }
        
        private final String name;
        
        public String toString() {
            return name;
        }
    } // end Operation
    
    /**
     * Represents one difference in old and new list.
     */
    public static final class ResultItem<S> {

        /** element which differs */
        public final S element;
        
        /** kind of operation */
        public final Operation operation;

        /**
         * Creates an instance.
         * 
         * @param  element    element which differs. New element when insert
         *                    or modify, old element when nochange or delete.
         * @param  operation  kind of operation, insert/delete/nochange/modify
         */
        public ResultItem(final S element, final Operation operation) {
            this.element = element;
            this.operation = operation;
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer(128);
            sb.append('{');
            sb.append(operation);
            sb.append("} ");
            sb.append(element);
            return sb.toString();
        }
    };

    /**
     * Computes the lists differences. Just call this method after
     * instance is created.
     * 
     * @return  true, if there were at least one change in new list.
     */
    public boolean match() {
        final int NEITHER     = 0;
        final int UP          = 1;
        final int LEFT        = 2;
        final int UP_AND_LEFT = 3;
        final int UP_AND_LEFT_MOD = 4;
        
        int n = oldL.length;
        int m = newL.length;
        int S[][] = new int[n+1][m+1];
        int R[][] = new int[n+1][m+1];
        int ii, jj;
        
        // initialization
        for (ii = 0; ii <= n; ++ii) {
            S[ii][0] = 0;
            R[ii][0] = UP;
        }
        for (jj = 0; jj <= m; ++jj) {
            S[0][jj] = 0;
            R[0][jj] = LEFT;
        }
        
            // This is the main dynamic programming loop that computes the score and backtracking arrays.
        for (ii = 1; ii <= n; ++ii) {
            for (jj = 1; jj <= m; ++jj) {
                if (oldL[ii-1].equals(newL[jj-1])) {
                    S[ii][jj] = S[ii-1][jj-1] + 1;
                    R[ii][jj] = UP_AND_LEFT;
                } else {
                    int distance = measure.getDistance(oldL[ii-1], newL[jj-1]);
                    // if the distance is betwwen OBJECTS_MATCH and INFINITE_DISTANCE,
                    // old element was modified to new element.
                    if (distance > OBJECTS_MATCH && distance < INFINITE_DISTANCE) {
                        S[ii][jj] = S[ii-1][jj-1] + 1;
                        R[ii][jj] = UP_AND_LEFT_MOD;
                    } else {
                        S[ii][jj] = S[ii-1][jj-1] + 0;
                        R[ii][jj] = distance == OBJECTS_MATCH ? UP_AND_LEFT : NEITHER;
                    }
                }
                
                if (S[ii-1][jj] >= S[ii][jj]) {
                    S[ii][jj] = S[ii-1][jj];
                    R[ii][jj] = UP;
                }
                
                if (S[ii][jj-1] >= S[ii][jj]) {
                    S[ii][jj] = S[ii][jj-1];
                    R[ii][jj] = LEFT;
                }
            }
        }
        
        // The length of the longest substring is S[n][m]
        ii = n;
        jj = m;
        
        // collect result
        // ensure stack is empty
        if (result.empty() == false) result.clear();
        // Trace the backtracking matrix.
        while (ii > 0 || jj > 0) {
            if(R[ii][jj] == UP_AND_LEFT) {
                ii--;
                jj--;
                E element = oldL[ii];
                result.push(new ResultItem(element, Operation.NOCHANGE));
            } else if (R[ii][jj] == UP_AND_LEFT_MOD) {
                ii--;
                jj--;
                E element = newL[ii];
                result.push(new ResultItem(element, Operation.MODIFY));
            } else if (R[ii][jj] == UP) {
                ii--;
                E element = oldL[ii];
                result.push(new ResultItem(element, Operation.DELETE));
            } else if (R[ii][jj] == LEFT) {
                jj--;
                E element = newL[jj];
                result.push(new ResultItem(element, Operation.INSERT));
            }
        }
        return !result.empty();
    }
    
    /**
     * Returns a list of differences computed by <tt>compute()</tt> method.
     * Ensure that method <tt>compute()</tt> was called.
     * 
     * @return  array of differences.
     */
    public ResultItem<E>[] getResult() {
        int size = result.size();
        ResultItem<E>[] temp = new ResultItem[size];
        for (ResultItem<E> item : result) {
            temp[--size] = item;
        }
        return temp;
    }
    
    /**
     * Returns a list of differences computed by <tt>compute()</tt> method.
     * Moreover, it groups <b>remove</b> operation followed by <b>insert</b>
     * to one <b>modify</b> operation.
     * 
     * @return   array of differences.
     */
    public ResultItem<E>[] getTransformedResult() {
        Stack<ResultItem<E>> copy = (Stack<ResultItem<E>>) result.clone();
        ArrayList<ResultItem<E>> temp = new ArrayList<ResultItem<E>>(copy.size());
        while (!copy.empty()) {
            ResultItem<E> item = copy.pop();
            // when operation is remove, ensure that there is not following 
            // insert - in such case, we can merge these two operation to
            // modify operation.
            if (item.operation == Operation.DELETE && 
                !copy.empty() && copy.peek().operation == Operation.INSERT) 
            {
                // yes, it is modify operation.
                ResultItem nextItem = copy.pop();
                temp.add(new ResultItem(nextItem.element, Operation.MODIFY));
            } else {
                temp.add(item);
            }
        }
        return temp.toArray(new ResultItem[0]);
    }
    
    // for testing and debugging reasons.
    public String printResult(boolean transformed) {
        StringBuffer sb = new StringBuffer(128);
        ResultItem<E>[] temp = transformed ? getTransformedResult() : getResult();
        for (int i = 0; i < temp.length; i++) {
            sb.append(temp[i]).append('\n');
        }
        return sb.toString();
    }
    
    /**
     * Creates the <tt>Separator</tt> instance.
     * 
     * @return  separator instance.
     */
    public Separator separatorInstance() {
        return new Separator(getTransformedResult(), JavaTokenId.COMMA);
    }
    
    public static final class Separator<E> {

        static final SItem EMPTY = new SItem(null, 0);
        
        private final ResultItem<E>[] match;
        private E lastInList;
        private E firstInList;
        private final JavaTokenId separator;
        private SItem[] result;
        
        // these two flags are not used currently, if some of fixes
        // will not use them, remove it.
        private boolean allNew;
        private boolean allOld;
        
        public Separator(ResultItem<E>[] match, JavaTokenId separator) {
            this.match = match;
            this.separator = separator;
            this.lastInList = null;
            // this code can be replaced with providing last element
            // as a parameter of this constructor. (new list is mostly
            // available in caller code.)
            for (int i = match.length-1; i > 0; i--) {
                if (match[i].operation != Operation.DELETE) {
                    lastInList = match[i].element;
                    break;
                }
            }
            for (int i = 0; i < match.length; i++) {
                if (match[i].operation != Operation.DELETE) {
                    firstInList = match[i].element;
                    break;
                }
            }
            // currently not used code. Left for a while if they
            // will not be used during bug fixing.
            allNew = allOld = true;
            for (int i = 0; i < match.length; i++) {
                if (match[i].operation == Operation.MODIFY ||
                    match[i].operation == Operation.NOCHANGE)
                    allNew = allOld = false;
                else if (match[i].operation == Operation.INSERT)
                    allOld = false;
                else if (match[i].operation == Operation.DELETE)
                    allNew = false;
            }
        }

        // Just for shorter call
        // create separator item
        private static SItem create(ResultItem item) {
            return create(item, SItem.NONE);
        }
        
        // create separator item
        private static SItem create(ResultItem item, int type) {
            return new SItem(item, type);
        }

        /**
         * Computes separators for every result item. For every result item,
         * it creates separator item. Just call this method after Separator
         * creation.
         * todo (#pf): Should be part of Separator creation?
         */
        public void compute() {
            result = new SItem[match.length];
            for (int i = match.length-1; i >= 0; --i) {
                if (match[i].operation == Operation.DELETE) {
                    if (i == (match.length-1)) {
                        // handle last element deletion
                        if (i > 0 && match[i-1].operation == Operation.DELETE) {
                            result[i] = create(match[i], allOld ? SItem.TAIL : SItem.NONE);
                            while (i > 0 && match[i-1].operation == Operation.DELETE) {
                                if (i > 1 && match[i-2].operation == Operation.DELETE) {
                                    result[--i] = create(match[i], SItem.NEXT);
                                } else {
                                    int mask = (i-1) == 0 ? SItem.HEAD : SItem.PREV;
                                    result[--i] = create(match[i], mask | SItem.NEXT);
                                }
                            }
                        } else {
                            int mask = i > 0 ? SItem.PREV : SItem.HEAD;
                            mask |= allOld ? SItem.TAIL : SItem.NONE;
                            result[i] = create(match[i], mask);
                        }
                    } else {
                        result[i] = create(match[i], SItem.NEXT);
                    }
                } else if (match[i].operation == Operation.INSERT) {
                    if (i == (match.length-1)) {
                        // handle last element creation
                        if (i > 0 && match[i-1].operation == Operation.INSERT) {
                            result[i] = create(match[i], allNew ? SItem.TAIL : SItem.NONE);
                            while (i > 0 && match[i-1].operation == Operation.INSERT) {
                                if (i > 1 && match[i-2].operation == Operation.INSERT) {
                                    result[--i] = create(match[i], SItem.NEXT);
                                } else {
                                    int mask = (i-1) == 0 ? SItem.HEAD : SItem.PREV;
                                    result[--i] = create(match[i], mask | SItem.NEXT);
                                }
                            }
                        } else {
                            int mask = i > 0 ? SItem.PREV : SItem.HEAD;
                            mask |= allNew ? SItem.TAIL : SItem.NONE;
                            result[i] = create(match[i], mask);
                        }
                    } else {
                        result[i] = create(match[i], SItem.NEXT);
                    }
                } else {
                    result[i] = EMPTY;
                }
            }
        }
        
        /**
         * Returns true if the generator has to add/remove head token
         * before the element at <tt>index</tt>.
         * For example, 'throws' keyword when matching 'throws' clause.
         * If all throws are removed, throws keyword has to be removed
         * too.
         * 
         * @param  index  index of element in matcher result.
         * @return  true if separator has to be added/removed.
         */
        public boolean head(int index) { return result[index].head(); }
        
        /**
         * Returns true if the generator has to add/remove separator
         * before the element at <tt>index</tt>.
         * 
         * For example, comma when matching method parameters.
         * 
         * @param  index  index of element in matcher result.
         * @return  true if separator has to be added/removed.
         */
        public boolean prev(int index) { return result[index].prev(); }
        
        /**
         * Returns true if the generator has to add/remove separator
         * after the element at <tt>index</tt>.
         * 
         * For example, comma when matching method parameters.
         * 
         * @param  index  index of element in matcher result.
         * @return  true if separator has to be added/removed.
         */
        public boolean next(int index) { return result[index].next(); }
        
        /**
         * Returns true if the generator has to add/remove tail token
         * after the element at <tt>index</tt>.
         * For example, tail '>' token when matching type parameters.
         * If all type parameters are removed, tail '>' has to be
         * removed too. (also, head '<' has to be removed too. See
         * {@head}.
         * 
         * @param  index  index of element in matcher result.
         * @return  true if separator has to be added/removed.
         */
        public boolean tail(int index) { return result[index].tail(); }
        
        /**
         * Just print all the item. Should be probably removed,
         * used in tests right now. toString() should replace it.
         */
        public String print() {
            if (result != null) {
                 StringBuffer sb = new StringBuffer(128);
                 for (SItem item : result) {
                     if (item != EMPTY)
                         sb.append(item).append('\n');
                 }
                 return sb.toString();
            } else {
                return "Result was not computed!";
            }
        }
        
        // for every result item, instance of SItem class contains
        // information how to handle separator around the item,
        // including head/tail tokens.
        private static final class SItem {
            private static final int NONE = 0x00;
            private static final int PREV = 0x01;
            private static final int NEXT = PREV << 1;
            private static final int HEAD = NEXT << 1;
            private static final int TAIL = HEAD << 1;
            
            private final int type;
            private final ResultItem item;
            
            private SItem(ResultItem item, int type) {
                this.item = item;
                this.type = type;
            }
            
            private boolean prev() { return (type & PREV) != NONE; }
            private boolean next() { return (type & NEXT) != NONE; }
            private boolean head() { return (type & HEAD) != NONE; }
            private boolean tail() { return (type & TAIL) != NONE; }
            
            public String toString() {
                StringBuffer sb = new StringBuffer();
                if (head()) sb.append("head ");
                if (prev()) sb.append("previous ");
                sb.append(item.toString()).append(' ');
                if (next()) sb.append("next ");
                if (tail()) sb.append("tail ");
                return sb.toString();
            }
        }
    }
}