/*
 * Condition.java
 *
 * Created on 4 de enero de 2007, 05:55 PM
 *
 * Initial version
 *
 * @author DreamTangerine
 *
 * Change array conditions for a pattern-
 *
 * @author Alex Buloichik <alex73mail@gmail.com>
 *
 */

package org.dts.spell.dictionary.myspell;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 *
 * @author DreamTangerine
 */
public final class Conditions
{
  static private HashMap<String, Conditions> readed = null ;
  
  private final static Conditions EMPTY_CONDITIONS = new Conditions() ;
  
  /** Creates a new instance of Condition */
  private Conditions()
  {
  }

  private Conditions(String cs)
  {
    condition = Pattern.compile(cs);    
  }
  
  public boolean match(String word)
  {
    if (null != condition)
      return condition.matcher(word).matches() ;
    else
      return true ;
  }
  
  public static void beginRead()
  {
    if (null != readed)
      throw new IllegalStateException() ;
    
    readed = new HashMap<String, Conditions>() ;  
  }

  public static void endRead()
  {
    if (null == readed)
      throw new IllegalStateException() ;
    
    readed = null ;
  }
  
  public static Conditions createFrom(String cs)
  {
    // if no condition just return
    if (cs.equals("."))
      return EMPTY_CONDITIONS ;
    
    Conditions result = readed.get(cs) ;
    
    if (null == result)
    {
      result = new Conditions(cs) ;
      readed.put(cs, result) ;
    }
    
    return result ;
  }
  
  /** Condition or null if there is no condition declared. */
  private Pattern condition ;
}
