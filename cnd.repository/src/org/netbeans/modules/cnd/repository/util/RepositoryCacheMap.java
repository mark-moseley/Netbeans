/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.repository.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author Nickolay Dalmatov
 */

public class RepositoryCacheMap<K,V> extends TreeMap<K,V> {
    private static final long serialVersionUID = 7249069246763182397L;
    private final TreeMap<K, RepositoryCacheValue<V>>   keyToValueStorage;
    private final TreeMap<RepositoryCacheValue<V>, K>   valueToKeyStorage;
    private AtomicInteger                         capacity;
    private final ReentrantReadWriteLock          readWriteLock;
    private static final int                      DEFAULT_CACHE_CAPACITY  = 20;
    private static AtomicInteger currentBornStamp = new AtomicInteger(0);
    
    static private final class CacheEntry<K,V> implements Map.Entry<K,V> {
        private K key;
        private V value;
        
        CacheEntry(final K key, final V value) {
            this.key   = key;
            this.value = value;
            
        }
        
        public K getKey() {
            return this.key;
        }
        
        public V getValue() {
            return this.value;
        }
        
        public V setValue(V value) {
            final V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
        
    }
    
    
    static private final class RepositoryCacheValue<V>  implements Comparable{
        
        public AtomicInteger       frequency;
        public V                   value;
        public AtomicBoolean       newBorn;
        public final int           bornStamp;
        
        RepositoryCacheValue(final V value) {
            frequency = new AtomicInteger(1);
            newBorn   = new AtomicBoolean(true);
            bornStamp = currentBornStamp.incrementAndGet();
            this.value = value;
        }
        
        private int compareAdults(final RepositoryCacheValue<V> elemToCompare) {
            int ownValue = frequency.intValue();
            int objValue = elemToCompare.frequency.intValue();
            
            if (ownValue < objValue) {
                return -1;
            } else if (ownValue == objValue){
                ownValue = bornStamp;
                objValue = elemToCompare.bornStamp;
                
                if (ownValue < objValue)
                    return -1;
                else if (ownValue > objValue)
                    return 1;
                else
                    return 0;
            } else {
                return 1;
            }
        }
        
        private int compareNewBorns(final RepositoryCacheValue<V> elemToCompare) {
            final int ownValue = bornStamp;
            final int objValue = elemToCompare.bornStamp;
            
            if (ownValue < objValue)
                return -1;
            else if (ownValue > objValue)
                return 1;
            else
                return 0;
        }
        
        public int compareTo(final Object o) {
            final RepositoryCacheValue<V> elemToCompare = (RepositoryCacheValue<V>) o;
            final boolean ownChildhood = newBorn.get();
            final boolean objChildhood = elemToCompare.newBorn.get();
            
            if (ownChildhood && objChildhood) {
                return compareNewBorns(elemToCompare);
            } else if (ownChildhood && !objChildhood) {
                return 1;
            } else if (!ownChildhood && objChildhood) {
                return -1;
            } else {
                return compareAdults(elemToCompare);
            }
        }
    }
    
    
    /**
     * Creates a new instance of RepositoryCacheMap
     */
    public RepositoryCacheMap(final int capacity) {
        readWriteLock   = new ReentrantReadWriteLock(true);
        keyToValueStorage         = new TreeMap<K, RepositoryCacheValue<V>>();
        valueToKeyStorage         = new TreeMap<RepositoryCacheValue<V>, K>();
        this.capacity   = new AtomicInteger((capacity >0)?capacity:DEFAULT_CACHE_CAPACITY);
    }
    
    public int size() {
        try {
            readWriteLock.readLock().lock();
            return keyToValueStorage.size();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
    
    public boolean isEmpty() {
        try {
            readWriteLock.readLock().lock();
            return keyToValueStorage.isEmpty();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
    
    public boolean containsKey(final Object key) {
        try {
            readWriteLock.readLock().lock();
            return keyToValueStorage.containsKey(key);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
    
    public boolean containsValue(final Object value) {
        try {
            readWriteLock.readLock().lock();
            return valueToKeyStorage.containsKey(value);
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
    
    public V get(final Object key) {
        V retValue = null;
        
        try {
            readWriteLock.writeLock().lock();
            
            RepositoryCacheValue<V> entry = (RepositoryCacheValue<V>)keyToValueStorage.get(key);
            
            if (entry != null) {
                valueToKeyStorage.remove(entry);
                entry.frequency.incrementAndGet();
                entry.newBorn.set(false);
                valueToKeyStorage.put(entry, (K)key);
                retValue= entry.value;
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
        
        return retValue;
    }
    
    public V put(K key, V value) {
        V retValue = null;
        
        try {
            readWriteLock.writeLock().lock();
            RepositoryCacheValue<V> entry = new RepositoryCacheValue<V> (value);
            
            if (keyToValueStorage.size() < capacity.intValue()) {
                keyToValueStorage.put(key, entry);
                valueToKeyStorage.put(entry, key);
            } else {
                RepositoryCacheValue<V>   minValue = valueToKeyStorage.firstKey();
                K   minKey   = valueToKeyStorage.get(minValue);
                
                keyToValueStorage.remove(minKey);
                valueToKeyStorage.remove(minValue);
                
                keyToValueStorage.put(key, entry);
                valueToKeyStorage.put(entry, key);
                
                retValue =  minValue.value;
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
        
        return retValue;
    }
    
    public V remove(Object key) {
        
        V retValue = null;
        try {
            
            readWriteLock.writeLock().lock();
            RepositoryCacheValue<V> entry = (RepositoryCacheValue<V> )keyToValueStorage.remove(key);
            
            if (entry != null) {
                valueToKeyStorage.remove(entry);
                retValue = entry.value;
            }
            
        } finally {
            readWriteLock.writeLock().unlock();
        }
        
        return retValue;
    }
    
    public void putAll(Map<? extends K, ? extends V> map) {
        // not supported
    }
    
    public void clear() {
        // keyToValueStorage.clear();
    }
    
    public Set<K> keySet() {
        try {
            readWriteLock.readLock().lock();
            return keyToValueStorage.keySet();
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
    
    public Collection<V> values() {
        final Collection<V> newCollection = new ArrayList<V>();
        
        try {
            readWriteLock.readLock().lock();
            
            final Collection<RepositoryCacheValue<V>> origCollection = keyToValueStorage.values();
            final Iterator<RepositoryCacheValue<V>> iter = origCollection.iterator();
            
            while ( iter.hasNext()) {
                newCollection.add(iter.next().value);
            }
            
        } finally {
            readWriteLock.readLock().unlock();
        }
        
        return newCollection;
    }
    
    public Set<Map.Entry<K,V>> entrySet() {
        final TreeSet<Map.Entry<K,V>>   resultSet = new TreeSet<Map.Entry<K,V>>();
        
        try {
            readWriteLock.readLock().lock();
            
            final Set<Map.Entry<K, RepositoryCacheValue<V>>>      aSet = keyToValueStorage.entrySet();
            final Iterator<Map.Entry<K, RepositoryCacheValue<V>>> iter = aSet.iterator();
            
            while (iter.hasNext()) {
                final Map.Entry<K, RepositoryCacheValue<V>> elem = iter.next();
                resultSet.add(new CacheEntry<K,V> (elem.getKey(), elem.getValue().value));
            }

        } finally {
            readWriteLock.readLock().unlock();
        }
        
        return resultSet;
    }
    
    public Set<V> adjustCapacity(final int newCapacity) {

        Set<V>  retSet = new HashSet<V>();
        
        try {
            readWriteLock.writeLock().lock();
            
            if (newCapacity >= capacity.intValue()) {
                capacity.set(newCapacity);
                
            } else if (newCapacity >= keyToValueStorage.size()) {
                capacity.set(newCapacity);
                
            } else {
                final int numToRemove = keyToValueStorage.size() - newCapacity;
                capacity.set(newCapacity);                

                for (int i = 0; i < numToRemove; i++) {
                    final RepositoryCacheValue<V> elem = valueToKeyStorage.firstKey();
                    final K removedKey = valueToKeyStorage.get(elem);
                    
                    retSet.add(elem.value);
                    valueToKeyStorage.remove(elem);
                    keyToValueStorage.remove(removedKey);
                }
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
        return retSet;
    }
}
