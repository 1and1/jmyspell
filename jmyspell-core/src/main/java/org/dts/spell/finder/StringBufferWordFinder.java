/*
 * Created on 11/02/2005
 *
 */
package org.dts.spell.finder;

import org.dts.spell.tokenizer.WordTokenizer;

/**
 * TODO : Cuando se porte todo al 5.0 Utilizar un template para el StringBuffer.
 * 
 * @author DreamTangerine
 *
 */
public class StringBufferWordFinder extends CharSequenceWordFinder
{
  public StringBufferWordFinder(StringBuffer text, WordTokenizer tokenizer)
  {
    super(text, tokenizer) ;
  }

  /**
   * @param text
   */
  public StringBufferWordFinder(StringBuffer text)
  {
    super(text) ;
  }

  @Override
  protected void replace(String newWord, Word currentWord)
  {
    int start = currentWord.getStart() ;
    int oldEnd = currentWord.getEnd() ;
    int newEnd = newWord.length() ;
    
    getStringBuffer().replace(start, oldEnd, newWord) ;
    
    updateCharSequence(start, oldEnd, WordTokenizer.DELETE_CHARS) ;
    
    if (newEnd > 0)
      updateCharSequence(start, newEnd, WordTokenizer.INSERT_CHARS) ;    
  }
  
  public StringBuffer getStringBuffer()
  {
    return (StringBuffer) getCharSequence() ;
  }
}
