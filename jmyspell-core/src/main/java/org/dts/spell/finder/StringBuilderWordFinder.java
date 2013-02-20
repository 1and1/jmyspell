/*
 * Created on 11/02/2005
 *
 */
package org.dts.spell.finder;

import org.dts.spell.tokenizer.WordTokenizer;

/**
 * TODO : Cuando se porte todo al 5.0 Utilizar un template para el StringBuilder.
 * 
 * @author DreamTangerine
 *
 */
public class StringBuilderWordFinder extends CharSequenceWordFinder
{
  public StringBuilderWordFinder(StringBuilder text, WordTokenizer tokenizer)
  {
    super(text, tokenizer) ;
  }

  /**
   * @param text
   */
  public StringBuilderWordFinder(StringBuilder text)
  {
    super(text) ;
  }

  protected void replace(String newWord, Word currentWord)
  {
    int start = currentWord.getStart() ;
    int oldEnd = currentWord.getEnd() ;
    int newEnd = newWord.length() ;
    
    getStringBuilder().replace(start, oldEnd, newWord) ;
    
    updateCharSequence(start, oldEnd, WordTokenizer.DELETE_CHARS) ;
    
    if (newEnd > 0)
      updateCharSequence(start, newEnd, WordTokenizer.INSERT_CHARS) ;    
  }
  
  public StringBuilder getStringBuilder()
  {
    return (StringBuilder) getCharSequence() ;
  }
}
