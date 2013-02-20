/*
 * Created on 30/08/2005
 *
 */
package org.dts.spell.filter;

import java.util.LinkedList;
import java.util.List;

import org.dts.spell.finder.Word;
import org.dts.spell.tokenizer.WordTokenizer;

/**
 * @author DreamTangerine
 *
 */
public class ChainFilter implements Filter
{
  /* (non-Javadoc)
   * @see org.dts.spell.filter.Filter#filter(org.dts.spell.finder.Word, org.dts.spell.tokenizer.WordTokenizer)
   */
  public Word filter(Word word, WordTokenizer tokenizer)
  {
    for (Filter filter : filters)
    {
      word = filter.filter(word, tokenizer) ;
      
      if (null == word)
        return null ;
    }
    
    return word ;
  }

  public void updateCharSequence(
      WordTokenizer tokenizer, 
      int start, 
      int end, 
      int cause) 
  {
    for (Filter filter : filters)
      filter.updateCharSequence(tokenizer, start, end, cause) ;
  }
  
  /**
   * Add the filter. It don't check if it is already added.
   *  
   * @param filter
   */
  public void addFilter(Filter filter)
  {
    filters.add(filter) ;
  }

  /**
   * Remove the filter.
   * @param filter
   */
  public void removeFilter(Filter filter)
  {
    filters.remove(filter) ;
  }
  
  private List<Filter> filters = new LinkedList<Filter>() ; 
}
