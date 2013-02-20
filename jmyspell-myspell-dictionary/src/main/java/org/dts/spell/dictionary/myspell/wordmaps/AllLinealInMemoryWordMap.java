/*
 * AllLinealInMemoryWordMap.java
 *
 * Created on 19 de marzo de 2007, 09:17 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.dts.spell.dictionary.myspell.wordmaps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.dts.spell.dictionary.myspell.HEntry;
import org.dts.spell.myspell.utils.CharMap;

/**
 *
 * @author DreamTangerine
 */
public class AllLinealInMemoryWordMap extends WordMap
{
   private static class SimpleEntry {
      public byte[] word ;
      public byte[] astr ;
   }

  /** Creates a new instance of AllLinealInMemoryWordMap */
  public AllLinealInMemoryWordMap()
  {
  }

  @Override
  public void init(long lastModificationTime, String wordMapName, String encoding) throws IOException
  {
  }

  @Override
  public boolean needEntries() 
  {
    return true ;
  }
  
  public void beginAddEntries(int nEntries) throws IOException
  {
    entries = new ArrayList<HEntry>(nEntries) ;
  }

  public void endAddEntries(int nEntries) throws IOException 
  {
      // TODO : Sort entries.
  }
  
  public HEntry get(String word)
  {
    int index = Collections.binarySearch(entries, word) ;
    
    if (index >= 0)
      return entries.get(index) ;
    else
      return null ;
  }

  public void add(HEntry entry)
  {
    entries.add(entry) ;
  }

  public void addCustomWord(String word)
  {
    int index = Collections.binarySearch(entries, word) ;

    if (index < 0)
    {
        index = -index ;
        entries.add(index, new HEntry(word));
    }
  }

  public Iterator<HEntry> iterator()
  {
    return entries.iterator() ;
  }
  
  private List<HEntry> entries ;
}
