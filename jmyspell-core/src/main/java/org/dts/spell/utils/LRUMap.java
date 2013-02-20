/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dts.spell.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 *
 * @author DreamTangerine
 */
public class LRUMap<K, V> extends LinkedHashMap<K, V>
{
    private int maxEntries ;
    
    public LRUMap(int maxEntries)
    {
        this(16, maxEntries) ;
    }
    
    public LRUMap(int initialCapacity, int maxEntries)
    {
        this(initialCapacity, 0.75f, maxEntries) ;
    }
    
    public LRUMap(int initialCapacity, float loadFactor, int maxEntries)    
    {
        super(initialCapacity, loadFactor, true) ;
        this.maxEntries = maxEntries ;
    }

    public void setMaxEntries(int maxEntries) {
      this.maxEntries = maxEntries ;

      if (size() > maxEntries) {
        List<K> keys = new ArrayList<K>(keySet()) ;
        List<V> values = new ArrayList<V>(values()) ;

        clear();
        
        ListIterator<K> keysIt = keys.listIterator() ;
        ListIterator<V> valuesIt = values.listIterator() ;

        while (size() <= maxEntries && keysIt.hasNext() && valuesIt.hasNext()) {
          put(keysIt.next(), valuesIt.next()) ;
        }
      }
    }

    public int getMaxEntries() {
      return maxEntries ;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxEntries ;
     }
}
