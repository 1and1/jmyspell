/*
 * IndexedFileWordMap.java
 *
 * Created on 19 de marzo de 2007, 06:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.dts.spell.dictionary.myspell.wordmaps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dts.spell.dictionary.myspell.HEntry;
import org.dts.spell.dictionary.myspell.Utils;
import org.dts.spell.dictionary.myspell.Utils.IndexComparator;
import org.dts.spell.dictionary.myspell.Utils.IndexSearcher;
import org.dts.spell.myspell.utils.CharMap;
import org.dts.spell.utils.FileUtils;

/**
 *
 * @author DreamTangerine
 */
public class IndexedFileWordMap extends WordMap {

    private CharMap charMap = new CharMap() ;

     private String wordMapName ;
    
    /** 
     * Creates a new instance of IndexedFileWordMap 
     * 
     * @param onlyFiles No memory index is created. All operations are with files, so so there is less waste of memory, but poor perfomance.
     */
    public IndexedFileWordMap(boolean  onlyFiles) {
        if (onlyFiles)
            indexStorage = new FileIndex() ;
        else
            indexStorage = new MemoryIndex() ;
    }

    private File getWordsFile() {
        return new File(FileUtils.getJMySpellDir(), wordMapName + ".dic");
    }

    private File getIndexFile() {
        return new File(FileUtils.getJMySpellDir(), wordMapName + ".idx");
    }

    @Override
    public void init(long lastModificationTime, String wordMapName, String encoding) throws IOException {
        this.wordMapName = wordMapName ;
        
        File wordFile = getWordsFile();

        if (wordFile.lastModified() > lastModificationTime) {
            try {
                words = new RandomAccessFile(wordFile, "r");
                indexStorage.initFromFile(getIndexFile());
            } catch (Exception ex) {
                if (null != words) {
                    words.close();
                }

                words = null;
            }
        }
    }

    @Override
    public boolean needEntries() {
        return null == words;
    }

    public void beginAddEntries(int nEntries) throws IOException {
        File wordFile = getWordsFile();

        indexStorage.initFromEntries(nEntries, getIndexFile());

        wordFile.delete();

        words = new RandomAccessFile(wordFile, "rw");
    }

    public void endAddEntries(int nEntries) throws IOException {
        // Time to sort
        Utils.heapSort(indexStorage, new IndexComparator() {

            public boolean isLess(int index1, int index2) {
                return getEntryFromPosition((long) index1).compareTo(getEntryFromPosition((long) index2)) < 0;
            }

            public boolean isGreater(int index1, int index2) {
                return getEntryFromPosition((long) index1).compareTo(getEntryFromPosition((long) index2)) > 0;
            }
        });

        indexStorage.endFromEntries(nEntries, getIndexFile());
    }

    public HEntry get(String word) {
        try {
            int index = Utils.binarySearch(word, indexStorage.getSize(), new IndexSearcher<String>() {

                public int compare(int index, String obj) {
                    return getWord(index).compareTo(obj);
                }
            });

            if (index >= 0) {
                return getEntry(index);
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public void add(HEntry entry) throws IOException {
        indexStorage.add((int) words.length());

        charMap.put(entry.word) ;

        words.writeUTF(entry.word);
        words.writeUTF(entry.astr);
    }

    protected long getEntryPosition(int index) {
        return indexStorage.get(index);
    }

    protected int getWordIndex(String word) {
        return Utils.binarySearch(word, indexStorage.getSize(), new IndexSearcher<String>() {

            public int compare(int index, String obj) {
                return getWord(index).compareTo(obj);
            }
        });
    }

    protected String getWord(int index) {
        try {
            words.seek(getEntryPosition(index));

            return words.readUTF();
        } catch (IOException ex) {
            return null;
        }
    }

    protected HEntry getEntry(int index) {
        return getEntryFromPosition(getEntryPosition(index));
    }

    protected HEntry getEntryFromPosition(long position) {
        try {
            words.seek(position);

            return new HEntry(words.readUTF(), words.readUTF());
        } catch (IOException ex) {
            return null;
        }
    }

    public void addCustomWord(String word) throws IOException {
        throw new UnsupportedOperationException();
    }

    private class EntryIterator implements Iterator<HEntry> {

        private HEntry moveNext() {
            HEntry result = null;

            try {
                if (position < words.length()) {
                    words.seek(position);
                    result = new HEntry(words.readUTF(), words.readUTF());
                    position = words.getFilePointer();
                }
            } catch (IOException ex) {
                Logger.getLogger(EntryIterator.class.getName()).log(Level.SEVERE, null, ex);
                result = null;
            }

            return result;
        }

        public EntryIterator() {
            nextEntry = moveNext();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public HEntry next() {
            HEntry result = nextEntry;

            nextEntry = moveNext();

            return result;
        }

        public boolean hasNext() {
            return null != nextEntry;
        }
        private HEntry nextEntry;
        private long position = 0;
    }

    public Iterator<HEntry> iterator() {
        return new EntryIterator();
    }

    private interface IndexStorage extends Utils.HeapSorteable {

        public void initFromFile(File file) throws Exception;

        public void initFromEntries(int nEntries, File file) throws IOException;

        public void add(int index) throws IOException;

        public void endFromEntries(int nEntries, File file) throws IOException;
    }

    private static class MemoryIndex implements IndexStorage {

        public int getSize() {
            return indexes.length;
        }

        public void swap(int index1, int index2) {
            int temp = indexes[index1];

            indexes[index1] = indexes[index2];
            indexes[index2] = temp;
        }

        public int get(int index) {
            return indexes[index];
        }

        public void initFromFile(File file) throws Exception {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));

            try {
                indexes = (int[]) in.readObject();
                nIndex = indexes.length;
            } finally {
                Utils.close(in);
            }
        }

        public void initFromEntries(int nEntries, File file) throws IOException {
            indexes = new int[nEntries];
            nIndex = 0;
        }

        public void add(int index) throws IOException {
            indexes[nIndex] = index;
            ++nIndex;
        }

        public void endFromEntries(int nEntries, File file) throws IOException {
            ObjectOutputStream out = null;

            try {
                out = new ObjectOutputStream(new FileOutputStream(file));
                out.writeObject(indexes);
            } finally {
                Utils.close(out);
            }
        }
        private int[] indexes;
        private int nIndex;
    }

    private static class FileIndex implements IndexStorage {

        private static int BEGIN_DATA = 0x17;
        private static int BEGIN_ARRAY = 0x17 + 0x4;
        private RandomAccessFile indexFile;
        private int nIndex;

        public void initFromFile(File file) throws Exception {
            indexFile = new RandomAccessFile(file, "r");

            indexFile.seek(BEGIN_DATA);
            nIndex = indexFile.readInt();
        }

        public void initFromEntries(int nEntries, File file) throws IOException {
            ObjectOutputStream out = null;

            try {
                out = new ObjectOutputStream(new FileOutputStream(file));
                out.writeObject(new int[0]);
            } finally {
                Utils.close(out);
            }

            indexFile = new RandomAccessFile(file, "rw");

            indexFile.seek(BEGIN_DATA);
            nIndex = indexFile.readInt();
        }

        public void add(int index) throws IOException {
            indexFile.seek(indexFile.length());
            indexFile.writeInt(index);
            indexFile.seek(BEGIN_DATA);
            ++nIndex;
            indexFile.writeInt(nIndex);
        }

        public void endFromEntries(int nEntries, File file) throws IOException {
        }

        public int getSize() {
            return nIndex;
        }

        public void swap(int index1, int index2) {
            int v1 = get(index1);
            int v2 = get(index2);

            try {
                indexFile.seek(BEGIN_ARRAY + 4 * index2);
                indexFile.writeInt(v1);
                
                indexFile.seek(BEGIN_ARRAY + 4 * index1);
                indexFile.writeInt(v2);
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        public int get(int index) {
            try {
                indexFile.seek(BEGIN_ARRAY + 4 * index);
                return indexFile.readInt();
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }
    private RandomAccessFile words;
    private IndexStorage indexStorage = new FileIndex();
}
