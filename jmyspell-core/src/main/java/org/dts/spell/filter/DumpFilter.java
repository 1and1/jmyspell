/*
 * Created on 02/09/2005
 *
 */
package org.dts.spell.filter;

import java.io.OutputStream;
import java.io.PrintWriter;

import org.dts.spell.finder.Word;
import org.dts.spell.tokenizer.WordTokenizer;

/**
 * @author DreamTangerine
 *
 */
public class DumpFilter implements Filter
{
  private PrintWriter writer ;
  
  /**
   * 
   */
  public DumpFilter()
  {
    this(System.out) ;
  }

  public DumpFilter(OutputStream stream)
  {
    writer = new PrintWriter(stream) ;
  }
  
  /* (non-Javadoc)
   * @see org.dts.spell.filter.Filter#filter(org.dts.spell.finder.Word, org.dts.spell.tokenizer.WordTokenizer)
   */
  public Word filter(Word word, WordTokenizer tokenizer)
  {
    writer.println("Word = #" + word + "#") ;
    writer.flush() ;
    
    return word ;
  }

  /* (non-Javadoc)
   * @see org.dts.spell.filter.Filter#updateCharSequence(org.dts.spell.tokenizer.WordTokenizer, int, int, int)
   */
  public void updateCharSequence(
      WordTokenizer tokenizer,
      int start,
      int end,
      int cause)
  {
    // Nothing to do
  }
}
