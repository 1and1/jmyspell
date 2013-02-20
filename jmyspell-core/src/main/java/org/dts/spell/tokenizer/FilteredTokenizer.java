/*
 * Created on 30/08/2005
 *
 */
package org.dts.spell.tokenizer;

import org.dts.spell.filter.Filter;
import org.dts.spell.finder.Word;

/**
 * @author DreamTangerine
 *
 */
public class FilteredTokenizer extends AbstractWordTokenizer
{
  public FilteredTokenizer(Filter filter)
  {
    this(filter, new DefaultWordTokenizer()) ;
  }
  
  /**
   *  Create a Tokenizer that filter the words.
   * 
   * @param filter The filter to use.
   * 
   * @param tokenizer The tokenizer to use to get words.
   * 
   */
  public FilteredTokenizer(Filter filter, WordTokenizer tokenizer)
  {
    this.filter = filter ;
    this.tokenizer = tokenizer ;
  }

  public Word currentWord(int index)
  {
//    Word word = tokenizer.currentWord(index) ;
//    Log.log(Log.ERROR, this, "currentWord " + index + " = #" + word + "#") ;    
//    
    return filter(tokenizer.currentWord(index)) ;
  }

  private Word tokenizerCurrentWord(int index) 
  {
    if (index < 0 || getCharSequence().length() <= index)
      return null ;
    else
      return tokenizer.currentWord(index) ;
  }
  
  private int nextIndex(Word currentWord, int index)
  {
    if (null != currentWord)
      index = currentWord.getEnd() ;

    ++index ;
      
    return index ;
  }
  
  public Word nextWord(int index)
  {
    int length = getCharSequence().length() ;
    Word currentNoFiltered = tokenizer.currentWord(index) ;
    Word current = filter(currentNoFiltered) ;
    Word orgWord = current ;
    Word result = null ;
    
    while (index < length && null == current)
    {
      index = nextIndex(currentNoFiltered, index) ;
      currentNoFiltered = tokenizerCurrentWord(index) ;
      current = filter(currentNoFiltered) ;
    }
    
    if (index < length && null != current)
    {
      if (current.equals(orgWord))
      {
        index = orgWord.getEnd() ;
        
        while (index < length && null == result)
        {
          if (null != current && !current.equals(orgWord))
            result = current ;
          else
          {
            index = nextIndex(currentNoFiltered, index) ;
            currentNoFiltered = tokenizerCurrentWord(index) ;
            current = filter(currentNoFiltered) ;
          }
        }
      }
      else
        result = current ;        
    }
    
    return result ;
  }

  private int previousIndex(Word currentWord, int index)
  {
   if (null != currentWord)
     index = currentWord.getStart() ;
   
   --index ;
      
   return index ;
  }
  
  public Word previousWord(int index)
  {
    Word currentNoFiltered = tokenizer.currentWord(index) ;
    Word current = filter(currentNoFiltered) ;
    Word orgWord = current ;
    Word result = null ;    
    
    while (index > 0 && null == current)
    {
      index = previousIndex(currentNoFiltered, index) ;
      currentNoFiltered = tokenizerCurrentWord(index) ;
      current = filter(currentNoFiltered) ;
    }
    
    if (index > 0 && null != current)
    {
      if (current.equals(orgWord))
      {
        index = orgWord.getStart() ;
        
        while (index > 0 && null == result)
        {
          if (null != current && !current.equals(orgWord))
            result = current ;
          else
          {
            index = previousIndex(currentNoFiltered, index) ;
            currentNoFiltered = tokenizerCurrentWord(index) ;
            current = filter(currentNoFiltered) ;
          }
        }
      }
      else
        result = current ;        
    }
    
    return result ;    
  }
  
  /* (non-Javadoc)
   * @see org.dts.spell.tokenizer.WordTokenizer#updateCharSequence(int, int, int)
   */
  public void updateCharSequence(int start, int end, int cause)
  {
    tokenizer.updateCharSequence(start, end, cause) ;
    filter.updateCharSequence(tokenizer, start, end, cause) ;
  }
  
  public void setCharSequence(CharSequence sequence)
  {
    tokenizer.setCharSequence(sequence) ;
    super.setCharSequence(sequence) ;
  }
  
  protected Word filter(Word word)
  {
    if (null != word)
      return filter.filter(word, tokenizer) ;    
    else
      return null ;
  }
  
  private Filter filter ;
  private WordTokenizer tokenizer ;
}
