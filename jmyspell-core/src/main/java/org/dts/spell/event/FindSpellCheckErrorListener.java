/*
 * Created on 11/02/2005
 *
 */
package org.dts.spell.event ;

import org.dts.spell.ErrorInfo;
import org.dts.spell.finder.Word ;

/**
 * Esta clase busca el primer error en el texto y para la corrección ortográfica.
 * Nos permite saber cual es la palabra que estaba mal escrita.
 * 
 * @author DreamTangerine
 *  
 */
public class FindSpellCheckErrorListener extends SpellCheckAdapter
{
  private ErrorInfo errorInfo ;

  /**
   *  
   */
  public FindSpellCheckErrorListener()
  {
    super() ;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.dts.spell.event.SpellCheckListener#beginChecking(org.dts.spell.event.SpellCheckEvent)
   */
  public void beginChecking(SpellCheckEvent event)
  {
    errorInfo = null ;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.dts.spell.event.SpellCheckListener#spellingError(org.dts.spell.event.SpellCheckEvent)
   */
  public void spellingError(SpellCheckEvent event)
  {
    createError(ErrorInfo.getSpellingErrorInfo(event), event) ;
  }

  public void badCaseError(SpellCheckEvent event)
  {
    createError(ErrorInfo.getBadCaseErrorInfo(event), event) ;
  }
  
  public void repeatWordError(SpellCheckEvent event)
  {
    createError(ErrorInfo.getRepeatWordErrorInfo(event), event) ;
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
    
    event.cancel() ;
  }
}
