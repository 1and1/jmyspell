/*
 * Created on 30/08/2005
 *
 */
package org.dts.spell.filter;

import org.dts.spell.finder.Word;
import org.dts.spell.tokenizer.WordTokenizer;

/**
 * @author DreamTangerine
 *
 */
public interface Filter
{
  public Word filter(Word word, WordTokenizer tokenizer) ;
  
  public void updateCharSequence(
      WordTokenizer tokenizer, 
      int start, 
      int end, 
      int cause) ;
}
