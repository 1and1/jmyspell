/*
 * Created on 29/12/2004
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
public class SfxEntry extends AffEntry
{
  String rappnd ;
  
  public SfxEntry(AffixMgr pmyMgr, AffixHeader header, String line) throws IOException
  {
    super(pmyMgr, header, line) ;
  }
  
  protected Conditions createConditions(String cs)
  {
    cs = cs.trim() ;
    
    if (!cs.equals("."))
      cs = ".*" + cs ;
    
    return Conditions.createFrom(cs) ;
  }
  
  public void readFrom(String line, char type) throws IOException
  {
    super.readFrom(line, type) ;
    
    rappnd = Utils.myRevStrDup(appnd) ;
  }
  
  public HEntry check(String word, int optflags, PfxEntry ppfx)
  {
    // if this suffix is being cross checked with a prefix
    // but it does not support cross products skip it
    if ((optflags & Utils.XPRODUCT) != 0 && (xpflg & Utils.XPRODUCT) == 0)
      return null;
    
    if (!word.endsWith(appnd))
      return null;
    
    String firstWord = word.substring(0, word.length() - appnd.length()) + strip;
    
    if (!conds.match(firstWord))
      return null;
    
    HEntry he = pmyMgr.lookup(firstWord);
    if (he != null && he.astr != null)
    {
      if (Utils.TestAff(he.astr, achar, he.astr.length()) && ((optflags & Utils.XPRODUCT) == 0 || Utils.TestAff(he.astr, ppfx.getName(), he.astr.length())))
        return he;
    }
    
    return null;
  }
  
  public boolean allowCross()
  {
    return ((xpflg & Utils.XPRODUCT) != 0) ;
  }
  
  /**
   * Add suffix to root word.
   *
   * @param word
   *            root word
   * @return changed word or null if condition fails
   */
  public String add(String word)
  {
    if (!conds.match(word))
        return null;
    
    if (!word.endsWith(strip))
      return null;
    
    return word.substring(0, word.length() - strip.length()) + appnd;
  }
  
  protected void build_list()
  {
   pmyMgr.build_sfxlist(this) ;
  }
}
