/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Batik" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation. For more  information on the
 Apache Software Foundation, please see <http://www.apache.org/>.

*/

package org.apache.batik.css.engine.value;

/**
 * A simple hashtable, not synchronized, with fixed load factor and with
 * equality test made with '=='.
 *
 * @author <a href="mailto:stephane@hillion.org">Stephane Hillion</a>
 * @version $Id$
 */
public class StringMap {

    /**
     * The initial capacity
     */
    protected final static int INITIAL_CAPACITY = 11;

    /**
     * The underlying array
     */
    protected Entry[] table;
	    
    /**
     * The number of entries
     */
    protected int count;
	    
    /**
     * Creates a new table.
     */
    public StringMap() {
	table = new Entry[INITIAL_CAPACITY];
    }

    /**
     * Creates a copy of the given StringMap object.
     * @param t The table to copy.
     */
    public StringMap(StringMap t) {
	count = t.count;
	table = new Entry[t.table.length];
	for (int i = 0; i < table.length; i++) {
	    Entry e = t.table[i];
	    Entry n = null;
	    if (e != null) {
		n = new Entry(e.hash, e.key, e.value, null);
		table[i] = n;
		e = e.next;
		while (e != null) {
		    n.next = new Entry(e.hash, e.key, e.value, null);
		    n = n.next;
		    e = e.next;
		}
	    }
	}
    }

    /**
     * Gets the value corresponding to the given string.
     * @return the value or null
     */
    public Object get(String key) {
	int hash  = key.hashCode() & 0x7FFFFFFF;
	int index = hash % table.length;
	
	for (Entry e = table[index]; e != null; e = e.next) {
	    if ((e.hash == hash) && e.key == key) {
		return e.value;
	    }
	}
	return null;
    }
    
    /**
     * Sets a new value for the given variable
     * @return the old value or null
     */
    public Object put(String key, Object value) {
	int hash  = key.hashCode() & 0x7FFFFFFF;
	int index = hash % table.length;
	
	for (Entry e = table[index]; e != null; e = e.next) {
	    if ((e.hash == hash) && e.key == key) {
		Object old = e.value;
		e.value = value;
		return old;
	    }
	}
	
	// The key is not in the hash table
        int len = table.length;
	if (count++ >= (len * 3) >>> 2) {
	    rehash();
	    index = hash % table.length;
	}
	
	Entry e = new Entry(hash, key, value, table[index]);
	table[index] = e;
	return null;
    }

    /**
     * Rehash the table
     */
    protected void rehash () {
	Entry[] oldTable = table;
	
	table = new Entry[oldTable.length * 2 + 1];
	
	for (int i = oldTable.length-1; i >= 0; i--) {
	    for (Entry old = oldTable[i]; old != null;) {
		Entry e = old;
		old = old.next;
		
		int index = e.hash % table.length;
		e.next = table[index];
		table[index] = e;
	    }
	}
    }

    /**
     * To manage collisions
     */
    protected static class Entry {
	/**
	 * The hash code
	 */
	public int hash;
	
	/**
	 * The key
	 */
	public String key;
	
	/**
	 * The value
	 */
	public Object value;
	
	/**
	 * The next entry
	 */
	public Entry next;
	
	/**
	 * Creates a new entry
	 */
	public Entry(int hash, String key, Object value, Entry next) {
	    this.hash  = hash;
	    this.key   = key;
	    this.value = value;
	    this.next  = next;
	}
    }

    // BEGIN RAVE MODIFICATIONS
    // I need to be able to iterate over these StringMaps
    public java.util.Iterator keys() {
        return new It(true);
    }
    public java.util.Iterator values() {
        return new It(false);
    }
    public int size() {
        return count;
    }
    private class It implements java.util.Iterator {
        /** @param keys If true iterate keyset else valueset */
        private It(boolean keys) {
            this.keys = keys;
            findNext();
        }

        public boolean hasNext() {
            return entry != null;
        }
        public Object next() {
            Entry e = entry;
            findNext();
            return keys ? e.key : e.value;
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private int index = -1;
        private Entry entry = null;
        private boolean keys;
        
        private void findNext() {
            if (entry != null) {
                entry = entry.next;
                if (entry != null) {
                    return;
                }
            }
            if (index == table.length) {
                return;
            }
            index++;
            while (index < table.length) {
                if (table[index] != null) {
                    entry = table[index];
                    return;
                }
                index++;
            }
        }
    }
    // END RAVE MODIFICATIONS

}
