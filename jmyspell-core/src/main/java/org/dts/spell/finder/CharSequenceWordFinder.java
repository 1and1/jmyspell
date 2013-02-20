/*
 * Created on 04/02/2005
 *
 */
package org.dts.spell.finder ;

import org.dts.spell.tokenizer.DefaultWordTokenizer;
import org.dts.spell.tokenizer.WordTokenizer;

/**
 * @author DreamTangerine
 *  
 */
public class CharSequenceWordFinder extends AbstractWordFinder
{
  private WordTokenizer tokenizer ;
  
  public CharSequenceWordFinder(CharSequence text, WordTokenizer tokenizer)
  {
    tokenizer.setCharSequence(text) ;
    setTokenizer(tokenizer) ;
  }

  public CharSequenceWordFinder(CharSequence text)
  {
    this(text, new DefaultWordTokenizer()) ;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.dts.spell.finder.AbstractWordFinder#next(org.dts.spell.finder.Word)
   */
  protected Word next(Word currentWord)
  {
    int last ;
    Word word ;    

    if (currentWord == null)
    {
      last = 0 ;
      word = findFirstWord() ;
    }
    else
    {
      last = currentWord.getEnd() ;
      word = getTokenizer().nextWord(last - 1) ;      
    }
    
    return word ;
  }

  /**
   * This method throw a UnsupportedOperationException because a String is
   * inmutable object. If you want replace the word see StringBufferWordFinder
   * or StringBuilderWordFinder.
   *  
   */
  protected void replace(String newWord, Word currentWord)
  {
    throw new UnsupportedOperationException() ;
  }
  
  ////////////////////////////////////////////

  public WordTokenizer getTokenizer()
  {
    return tokenizer ;
  }

  protected void setTokenizer(WordTokenizer tokenizer)
  {
    if (getTokenizer() != tokenizer)
    {
      if (getTokenizer() != null)
        tokenizer.setCharSequence(getTokenizer().getCharSequence()) ;
      
      this.tokenizer = tokenizer ;
      init() ;
    }
  }
  
  protected void updateCharSequence(int start, int end, int cause)
  {
    getTokenizer().updateCharSequence(start, end, cause) ;
  }
  
  public CharSequence getCharSequence()
  {
    if (getTokenizer() != null)
      return getTokenizer().getCharSequence() ;
    else
      return null ;
  }
  
  protected Word findFirstWord()
  {
    Word result = getTokenizer().currentWord(0) ;
    
    if (null == result)
      result = getTokenizer().nextWord(0) ;
    
    return result ;
  }
}