/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dts.spell.examples.utils;

import org.dts.spell.filter.Filter;
import org.dts.spell.finder.Word;
import org.dts.spell.tokenizer.WordTokenizer;

/**
 *
 * @author dreamtangerine
 */
public class CountWordFilter implements Filter {

  private int nWords  ;

  public Word filter(Word word, WordTokenizer tokenizer) {
    ++nWords ;
    return word ;
  }

  public void updateCharSequence(WordTokenizer tokenizer, int start, int end, int cause) {
    
  }

  /**
   * @return the nWords
   */
  public int getWordCount() {
    return nWords / 2 ;
  }

}
