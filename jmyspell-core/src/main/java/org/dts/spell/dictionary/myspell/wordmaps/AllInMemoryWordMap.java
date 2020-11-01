/*
 * AllInMemeoryWordMap.java
 *
 * Created on 16 de marzo de 2007, 08:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.dts.spell.dictionary.myspell.wordmaps;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import org.dts.spell.dictionary.myspell.HEntry;

/**
 *
 * @author DreamTangerine
 */
public class AllInMemoryWordMap extends WordMapWithCharMap {

    private HashMap<String, HEntry> map = new HashMap<String, HEntry>();

    /** Creates a new instance of AllInMemoryWordMap */
    public AllInMemoryWordMap() {
    }

    @Override
    public void init(long lastModificationTime, String wordMapName, String encoding) throws IOException {
    }

    @Override
    public boolean needEntries() {
        return true;
    }

    @Override
    public void beginAddEntries(int nEntries) throws IOException {
        map = new HashMap<String, HEntry>(nEntries);
    }

    public void endAddEntries(int nEntries) throws IOException {
    }

    public HEntry get(String word) {
        return map.get(word);
    }

    public void add(HEntry entry) {
        map.put(entry.word, entry);
    }

    public void addCustomWord(String word) {
        add(new HEntry(word));
    }

    public Iterator<HEntry> iterator() {
        return map.values().iterator();
    }
}
