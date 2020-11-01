/*
 * IndexedFileWordMap.java
 *
 * Created on 19 de marzo de 2007, 06:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.dts.spell.dictionary.myspell.wordmaps;

import java.util.Map;
import org.dts.spell.dictionary.myspell.HEntry;
import org.dts.spell.utils.LRUMap;

/**
 *
 * @author DreamTangerine
 */
public class IndexedFileWordMapWithCache extends IndexedFileWordMap {

    /** 
     * Creates a new instance of IndexedFileWordMap with default 2000 words and 3000 positions.
     * 
     */
    public IndexedFileWordMapWithCache() {
        this(false);
    }
    
    /** 
     * Creates a new instance of IndexedFileWordMap with default 2000 words and 3000 positions.
     * 
     */
    public IndexedFileWordMapWithCache(boolean onlyFiles) {
        this(3000, 2000, onlyFiles);
    }

    /**
     * 
     * @param maxPositions max number of positions to Entries in cache. Width 0, there is no cache of positions.
     *
     * @param maxWords max number of words to position in cache. Width 0, there is no cache of words.
     */
    public IndexedFileWordMapWithCache(int maxPositions, int maxWords, boolean onlyFiles) {
        super(onlyFiles) ;
        
        if (maxPositions > 0) {
            positionsLru = new LRUMap<Integer, HEntry>(maxPositions);
        }

        if (maxWords > 0) {
            wordsLru = new LRUMap<String, Integer>(maxWords);
        }
    }
    private Map<Integer, HEntry> positionsLru = null;
    private Map<String, Integer> wordsLru = null;

    @Override
    public HEntry get(String word) {
        if (null != wordsLru) {
            HEntry result = null;

            Integer position = wordsLru.get(word);

            if (null != position) {
                result = getEntryFromPosition(position);
            } else {
                int index = getWordIndex(word);

                if (index > 0) {
                    result = getEntry(index);
                    wordsLru.put(word, (int) getEntryPosition(index));
                }
            }

            return result;

        } else {
            return super.get(word);
        }
    }

    @Override
    protected String getWord(int index) {
        if (null != positionsLru) {
            long position = getEntryPosition(index);
            HEntry entry = positionsLru.get(position);

            if (null == entry) {
                entry = getEntryFromPosition(position);
            }

            return entry.word;
        } else {
            return super.getWord(index);
        }
    }

    @Override
    protected HEntry getEntryFromPosition(long position) {
        if (null != positionsLru) {
            HEntry result = positionsLru.get(position);

            if (null == result) {
                result = super.getEntryFromPosition(position);
                positionsLru.put((int) position, result);
            }

            return result;
        } else {
            return super.getEntryFromPosition(position);
        }
    }
}
