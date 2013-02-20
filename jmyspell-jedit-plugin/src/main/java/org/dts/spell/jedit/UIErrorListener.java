/*
 * Created on 25/02/2005
 *
 */
package org.dts.spell.jedit ;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Graphics2D; 
import java.awt.Rectangle;

import javax.swing.SwingUtilities;

import org.dts.spell.event.SpellCheckEvent ;
import org.dts.spell.finder.Word ;
import org.dts.spell.swing.event.UISpellCheckListener;
import org.dts.spell.swing.utils.ErrorHighlighterPainter;
import org.dts.spell.swing.utils.ErrorMsgBox;

import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.Selection;
import org.gjt.sp.jedit.textarea.TextAreaExtension;

/**
 * @author DreamTangerine
 * 
 */
public class UIErrorListener extends UISpellCheckListener
{
  private class ErrorMarker extends TextAreaExtension
  {
    private int beginError ;
    private int endError ;    
    
    public void setMarks(int begin, int end)
    {
      beginError = begin ;
      endError = end ;
    }
    
    @Override
    public void paintValidLine(Graphics2D gfx,
        int screenLine,
        int physicalLine,
        int start,
        int end,
        int y)
    {
      int errorLineBegin = textArea.getScreenLineOfOffset(beginError) ;
      int errorLineEnd = textArea.getScreenLineOfOffset(endError) ;
     
      if (screenLine == errorLineBegin)
      {
        // All in one line.
        if (screenLine == errorLineEnd)
          end = endError ;
          
        paintErrorLine(gfx, beginError, end, y) ;
      }
      else if (screenLine == errorLineEnd)
      {
        paintErrorLine(gfx, start, endError, y) ;        
      }
    }
    
    // Paint in the same line
    private void paintErrorLine(Graphics2D gfx, int begin, int end, int y)
    {
      FontMetrics fm = textArea.getPainter().getFontMetrics() ;
      Point org = textArea.offsetToXY(begin) ;
      int x1 = org.x ;
      
      org = textArea.offsetToXY(end) ;
      int x2 = org.x ;

      y = y + fm.getAscent() + fm.getDescent() ;
      
      gfx.setColor(Color.RED) ;      
      ErrorHighlighterPainter.paintWaveLine(gfx, x1, x2, y) ;
    }
    
    public Rectangle getErrorRectangle()
    {
      FontMetrics fm = textArea.getPainter().getFontMetrics() ;
      Point org = textArea.offsetToXY(beginError) ;
      
      SwingUtilities.convertPointToScreen(org, textArea) ;
      
      int x1 = org.x ;
      int y1 = org.y ;
      
      org = textArea.offsetToXY(endError) ;
      org.y = org.y + fm.getAscent() + fm.getDescent() ;
      
      SwingUtilities.convertPointToScreen(org, textArea) ;

      int x2 = org.x ;
      int y2 = org.y ;
      
      return new Rectangle(x1, y1, x2 - x1, y2 - y1) ;
    }
  }
  
  private ErrorMarker errorMarker = new ErrorMarker() ;
  private JEditTextArea textArea ;
  
  public UIErrorListener(JEditTextArea textArea)
  {
    this.textArea = textArea ;
  }

  @Override
  public void beginChecking(SpellCheckEvent event)
  {
    super.beginChecking(event) ;

    textArea.selectNone() ;    
    textArea.getPainter().addExtension(errorMarker) ;
  }

  @Override
  public void spellingError(SpellCheckEvent event)
  {
    markError(event) ;
    
    relocateDialog(errorMarker.getErrorRectangle()) ;    
    
    super.spellingError(event) ;
  }

  @Override
  public void badCaseError(SpellCheckEvent event)
  {
    markError(event) ;
    super.badCaseError(event) ;
  }

  @Override
  public void repeatWordError(SpellCheckEvent event)
  {
    markError(event) ;
    super.repeatWordError(event) ;
  }  
  
  private void markError(SpellCheckEvent event)
  {
    try
    {
      int offSet = getOriginOffSet() ;
      Word word = event.getCurrentWord() ;
      int begin = offSet + word.getStart() ;
      int end = offSet + word.getEnd() ;

      textArea.scrollTo(begin, false) ;
      
      int line = textArea.getScreenLineOfOffset(begin) ;
      
      // Show two lines before error line
      if (line > 2)
        line -= 2 ;
      else
        line = 0 ;
      
      errorMarker.setMarks(begin, end) ;
      textArea.setSelection(new Selection.Range(begin, end)) ;

      for (int i = 0 ; i < line ; ++i)
        textArea.scrollDownLine() ;
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
    textArea.getPainter().removeExtension(errorMarker) ;    
    textArea.selectNone() ;    
    super.endChecking(event) ;
  }
  
  // The Offset from the origin. Is util for selection.
  private int originOffSet ;

  /**
   * Set the offset from the origin of the Sequence. It is util for spellcheck
   * over Ranges, to inidcate the range.
   * 
   * @param offSet
   */
  
  public void setOriginOffSet(int offSet)
  {
    originOffSet = offSet ;
  }

  public int getOriginOffSet()
  {
    return originOffSet ;
  }
}
