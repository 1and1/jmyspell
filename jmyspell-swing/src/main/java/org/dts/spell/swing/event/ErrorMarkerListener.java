/*
 * Created on 25/02/2005
 *
 */
package org.dts.spell.swing.event;

import javax.swing.text.JTextComponent;

import org.dts.spell.event.SpellCheckAdapter;
import org.dts.spell.event.SpellCheckEvent;
import org.dts.spell.finder.Word;
import org.dts.spell.ErrorInfo;
import org.dts.spell.swing.ErrorMarker;

/**
 * @author DreamTangerine
 *
 */
public class ErrorMarkerListener extends SpellCheckAdapter
{
  private ErrorMarker errorMarker ; 
  
  /**
   * 
   */
  public ErrorMarkerListener()
  {
    errorMarker = ErrorMarker.get() ;
    errorMarker.setSelectError(false) ;
    errorMarker.setAutoQuit(true) ;
  }

  public void setTextComponent(JTextComponent textComp) 
  {
    errorMarker.setTextComponent(textComp) ;
  }
  
  public JTextComponent getTextComponent()
  {
    return errorMarker.getTextComponent() ;
  }
  
  public ErrorMarker getErrorMarker()
  {
    return errorMarker ;
  }
  
  public boolean isSelectError()
  {
    return errorMarker.isSelectError() ;
  }

  /**
   * @param selectError
   *          The selectError to set.
   */
  public void setSelectError(boolean selectError)
  {
    errorMarker.setSelectError(selectError) ;
  }

  public void unMarkErrors()
  {
    errorMarker.unMarkAllErrors() ;
  }
  
  @Override
  public void beginChecking(SpellCheckEvent event)
  {
    unMarkErrors() ;
  }
  
  @Override
  public void spellingError(SpellCheckEvent event)
  {
    markError(event, ErrorInfo.getSpellingErrorInfo(event)) ;
  }
  
  @Override
  public void badCaseError(SpellCheckEvent event)
  {
    markError(event, ErrorInfo.getBadCaseErrorInfo(event)) ;
  }
  
  @Override
  public void repeatWordError(SpellCheckEvent event)
  {
    markError(event, ErrorInfo.getRepeatWordErrorInfo(event)) ;
  }
  
  private void markError(SpellCheckEvent event, ErrorInfo info)
  {
    try
    {
      // Mark the error
      Word word = event.getCurrentWord() ;
      errorMarker.markError(word.getStart(), word.getEnd(), info) ;
    }
    catch(Exception ex)
    {
      // We only want to trace
      System.out.println(ex) ;
    }
  }
}
