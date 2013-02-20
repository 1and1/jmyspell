/*
 * Created on 28/12/2004
 *
 */
package org.dts.spell.dictionary.myspell;

import java.io.IOException;

/**
 * @author DreamTangerine
 *
 * Some changes in affix tables
 *
 * @author Alex Buloichik <alex73mail@gmail.com>
 *
 */
public class PfxEntry extends AffEntry
{
  public PfxEntry(AffixMgr pmyMgr, AffixHeader header, String line) throws IOException
  {
    super(pmyMgr, header, line) ;
  }
  
  protected Conditions createConditions(String cs)
  {
    cs = cs.trim() ;
    
    if (!cs.equals("."))
      cs += ".*" ;
    
    return Conditions.createFrom(cs) ;
  }
  
  public HEntry check(String word)
  {
    if (!word.startsWith(appnd))
      return null;
    
    // construct root word
    String firstWord = strip + word.substring(appnd.length());
    
    if (!conds.match(firstWord))
      return null;
    
    HEntry he = pmyMgr.lookup(firstWord);
    if (he != null && he.astr != null)
    {
      if (Utils.TestAff(he.astr, achar, he.astr.length()))
        return he;
    }
    
    // prefix matched but no root word was found
    // if XPRODUCT is allowed, try again but now
    // ross checked combined with a suffix
    if ((xpflg & Utils.XPRODUCT) != 0)
    {
      he = pmyMgr.suffix_check(firstWord, Utils.XPRODUCT, this);
      
      if (he != null)
        return he;
    }
    
    return null;
  }
  
  boolean allowCross()
  {
    return ((xpflg & Utils.XPRODUCT) != 0) ;
  }
  
  /**
   * Add prefix to root word.
   *
   * @param word
   *            root word
   * @return changed word or null if condition fails
   */
  public String add(String word)
  {
    if (!word.startsWith(strip))
      return null;
    
    if (!conds.match(word))
      return null;
    
    // construct word by roow and prefix
    return appnd + word.substring(strip.length());
  }
  
  protected void build_list()
  {
    pmyMgr.build_pfxlist(this) ;
  }
}
