/*
 * Created on 11/02/2005
 *
 */
package org.dts.spell.jedit;


import org.dts.spell.finder.CharSequenceWordFinder;
import org.dts.spell.finder.Word;
import org.dts.spell.tokenizer.DefaultWordTokenizer;
import org.dts.spell.tokenizer.FilteredTokenizer;
import org.dts.spell.tokenizer.WordTokenizer;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.textarea.Selection;

/**
 * @author DreamTangerine
 *
 */
public class BufferWordFinder extends CharSequenceWordFinder
{
  /**
   */
  public BufferWordFinder(JEditBuffer buffer)
  {
    this(buffer, 0, buffer.getLength(), false) ;
  }

  public BufferWordFinder(JEditBuffer buffer, Selection selection)
  {
    this(buffer, selection.getStart(), selection.getEnd(), false) ;  
  }

  public BufferWordFinder(JEditBuffer buffer, Selection selection, boolean tokenMode)
  {
    this(buffer, selection.getStart(), selection.getEnd(), tokenMode) ;  
  }
  
  
  public BufferWordFinder(JEditBuffer buffer, int offSet, int end, boolean tokenMode)
  {
    super(
     new BufferCharSequenceAdapter(buffer, offSet, end),
     tokenMode ? new FilteredTokenizer(new TokenModeFilter()) : 
         new DefaultWordTokenizer()) ;
  }

  @Override
  protected void replace(String newWord, Word currentWord)
  {
    JEditBuffer buffer = getBuffer() ;
    int start = currentWord.getStart() ;

    buffer.remove(start, currentWord.length()) ;
    updateCharSequence(start, start + currentWord.length(),
        WordTokenizer.DELETE_CHARS) ;

    if (newWord.length() > 0)
    {
      buffer.insert(start, newWord) ;

      updateCharSequence(start, start + newWord.length(),
          WordTokenizer.INSERT_CHARS) ;
    }
  }
  
  public JEditBuffer getBuffer()
  {
    return ((BufferCharSequenceAdapter) getCharSequence()).getBuffer() ;
  }
}