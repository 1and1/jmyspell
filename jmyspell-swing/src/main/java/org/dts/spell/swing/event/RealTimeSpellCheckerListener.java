package org.dts.spell.swing.event;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import org.dts.spell.event.SpellCheckAdapter;
import org.dts.spell.event.SpellCheckEvent;
import org.dts.spell.finder.Word;
import org.dts.spell.swing.finder.SynchronizedWordFinder;
import org.dts.spell.ErrorInfo;
import org.dts.spell.swing.ErrorMarker;
import org.dts.spell.swing.utils.TextRange;

public class RealTimeSpellCheckerListener extends SpellCheckAdapter
{
	private ErrorMarker errorMarker ;
  
  public RealTimeSpellCheckerListener()
  {
  }

  public void setErrorMarker(ErrorMarker errorMarker)
  {
    this.errorMarker = errorMarker ;
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
  
  private void markError(SpellCheckEvent event, final ErrorInfo info)
  {
    if (!errorMarker.isActive())
      event.cancel();
    else //try
    {
      final Word badWord = event.getCurrentWord() ;
      final ErrorMarker localErrorMarker = errorMarker ;
      
      SwingUtilities.invokeLater(
         new Runnable()
         {
           public void run()
           { 
             try
             {
               if (localErrorMarker.isActive())
                localErrorMarker.markError(badWord.getStart(), badWord.getEnd(), info) ;
             }
             catch (BadLocationException e)
             {
               // The user delete the word :(.
               e.printStackTrace();
             } 
           }
         }) ;
      }
//      catch (Exception e)
//      {
//        e.printStackTrace();
//      }
  }
  
  @Override
  public void beginChecking(SpellCheckEvent event)
  {
    if (!errorMarker.isActive())
      event.cancel();
    else
    {
      final SynchronizedWordFinder finder = (SynchronizedWordFinder) event.getWordFinder() ;
      final TextRange textRange = finder.getTextRange() ;
      final ErrorMarker localErrorMarker = errorMarker ;
      
      //try
      {
        SwingUtilities.invokeLater(
           new Runnable()
           {
             public void run()
             { 
                if (localErrorMarker.isActive())             
                  localErrorMarker.unMarkRange(textRange.getBegin(), textRange.getEnd()) ;              
             }
           }) ;
      }
//      catch (Exception e)
//      {
//        e.printStackTrace();
//      }
    }
  }
}