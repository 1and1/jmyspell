/*
 * Created on 25/02/2005
 *
 */
package org.dts.spell.swing.event ;

import java.awt.Cursor;
import java.awt.Dialog ;
import java.awt.Frame ;
import java.awt.Window ;

import javax.swing.SwingUtilities ;
import javax.swing.text.JTextComponent ;

import org.dts.spell.event.SpellCheckEvent ;
import org.dts.spell.finder.Word ;
import org.dts.spell.swing.JSpellDialog ;
import org.dts.spell.ErrorInfo;
import org.dts.spell.swing.ErrorMarker ;
import org.dts.spell.swing.utils.ErrorMsgBox;

/**
 * @author DreamTangerine
 * 
 */
public class UIErrorMarkerListener extends UISpellCheckListener
{
  private ErrorMarker errorMarker ;
  private Cursor oldCursor ;

  /**
   * 
   */
  public UIErrorMarkerListener()
  {
    this(null) ;
  }

  /**
   * @param spellDialog
   */
  public UIErrorMarkerListener(JSpellDialog spellDialog)
  {
    super(spellDialog) ;
    errorMarker = ErrorMarker.get() ;
  }

  public void setTextComponent(JTextComponent textComp)
  {
    errorMarker.setTextComponent(textComp) ;
  }

  public void quitTextComponent()
  {
    errorMarker.quitTextComponent() ;
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

  @Override
  public void beginChecking(SpellCheckEvent event)
  {
    if (getSpellDialog() == null)
    {
      Window window = SwingUtilities.getWindowAncestor(errorMarker.getTextComponent()) ;
      JSpellDialog dlg ;

      if (window instanceof Frame)
        dlg = new JSpellDialog((Frame) window) ;
      else if (window instanceof Dialog)
        dlg = new JSpellDialog((Dialog) window) ;
      else
        dlg = null ;

      setSpellDialog(dlg) ;
  
      oldCursor = window.getCursor() ;
      window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)) ;
    }

    super.beginChecking(event) ;
  }

  @Override
  public void spellingError(SpellCheckEvent event)
  {
    markError(event, ErrorInfo.getSpellingErrorInfo(event)) ;
    super.spellingError(event) ;
  }

  @Override
  public void badCaseError(SpellCheckEvent event)
  {
    markError(event, ErrorInfo.getBadCaseErrorInfo(event)) ;
    super.badCaseError(event) ;
  }

  @Override
  public void repeatWordError(SpellCheckEvent event)
  {
    markError(event, ErrorInfo.getRepeatWordErrorInfo(event)) ;
    super.repeatWordError(event) ;
  }  
  
  private void markError(SpellCheckEvent event, ErrorInfo info)
  {
    try
    {
      Word word = event.getCurrentWord() ;

      errorMarker.unMarkAllErrors() ;
      errorMarker.markError(word.getStart(), word.getEnd(), info, true) ;
    }
    catch (Exception ex)
    {
      ErrorMsgBox.show(ex) ;
      System.out.println(ex) ;
    }
  }

  @Override
  public void endChecking(SpellCheckEvent event)
  {
    Window window = SwingUtilities.getWindowAncestor(errorMarker.getTextComponent()) ;    
    
    window.setCursor(oldCursor) ;    
    errorMarker.unMarkAllErrors() ;
    super.endChecking(event) ;
  }
}
