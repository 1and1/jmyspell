/*
 * Created on 27/12/2004
 *
 */
package org.dts.spell.dictionary.myspell;

/**
 * @author DreamTangerine
 *
 */
public class HEntry implements Comparable<String>
{
  public HEntry(String line)
  {
    int ap = line.indexOf('/') ;

    if (ap != -1)
    {
      word = line.substring(0, ap).intern() ;
      astr = line.substring(ap + 1).intern() ;
    }
    else
    {
      word = line ;
      astr = "" ;
    }
  }
  
  public HEntry(String word, String astr)
  {
    this.word = word.intern() ;
    this.astr = astr.intern() ;
  }

  public int compareTo(String other)
  {
    return word.compareTo(other) ;
  }

  public int compareTo(HEntry other)
  {
    return compareTo(other.word) ;
  }
  
  public String word ;
  public String astr ;
}
