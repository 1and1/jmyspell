/*
 * PefomanceSpellCheckErrorListener.java
 *
 * Created on 8 de febrero de 2007, 08:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.dts.spell.examples.utils;

import org.dts.spell.ErrorInfo;
import org.dts.spell.event.SpellCheckAdapter;
import org.dts.spell.event.SpellCheckEvent;
import org.dts.spell.finder.Word;

public class PefomanceSpellCheckErrorListener extends SpellCheckAdapter
{
  private ErrorInfo errorInfo ;
  private int nErrors ;
  private StopWatch watch = new StopWatch() ;
  private long time ;
  
  /**
   *  
   */
  public PefomanceSpellCheckErrorListener()
  {
    super() ;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.dts.spell.event.SpellCheckListener#beginChecking(org.dts.spell.event.SpellCheckEvent)
   */
  @Override
  public void beginChecking(SpellCheckEvent event)
  {
    errorInfo = null ;
    nErrors = 0 ;
    watch.start() ;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.dts.spell.event.SpellCheckListener#spellingError(org.dts.spell.event.SpellCheckEvent)
   */
  @Override
  public void spellingError(SpellCheckEvent event)
  {
    createError(ErrorInfo.getSpellingErrorInfo(event), event) ;
  }

  @Override
  public void badCaseError(SpellCheckEvent event)
  {
    createError(ErrorInfo.getBadCaseErrorInfo(event), event) ;
  }
  
  @Override
  public void repeatWordError(SpellCheckEvent event)
  {
    createError(ErrorInfo.getRepeatWordErrorInfo(event), event) ;
  }

  @Override
  public void endChecking(SpellCheckEvent event) 
  {
    time = watch.stop() ;
  }
  
  public long getTime()
  {
    return time ;
  }
  
  public Word getInvalidWord()
  {
    if (null != errorInfo)
      return errorInfo.getBadWord() ;
    else
      return null ;
  }
  
  public ErrorInfo getErrorInfo()
  {
    return errorInfo ;
  }
  
  public boolean hasError()
  {
    return errorInfo != null ;
  }
  
  private void createError(ErrorInfo info, SpellCheckEvent event)
  {
    errorInfo = info ;
    nErrors++ ;
    
    //event.cancel() ;
  }
  
  public int getErrorCount()
  {
    return nErrors ;
  }
}
