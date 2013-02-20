/*
 * Created on 29/03/2005
 *
 */
package org.dts.spell.tokenizer ;

import java.text.BreakIterator ;

import org.dts.spell.finder.Word ;

/**
 * @author DreamTangerine
 * 
 */
public class DefaultWordTokenizer extends AbstractWordTokenizer
{
  private BreakIterator wordIterator ;

  private CharIteratorAdapter wordSequence = new CharIteratorAdapter() ;

  public DefaultWordTokenizer()
  {
    this(BreakIterator.getWordInstance()) ;
  }

  public DefaultWordTokenizer(BreakIterator wordIterator)
  {
    this.wordIterator = wordIterator ;
  }

  protected Word scanBefore(CharSequence sequence, int index)
  {
    int start = wordIterator.preceding(index) ;
    int end = wordIterator.next() ;
    
    if (start == BreakIterator.DONE)
      start = 0 ;

    String text = sequence.subSequence(start, end).toString().trim() ;
    
    if (!text.equals(""))
      return new Word(text, start, isStartOfSentence(sequence, start)) ;
    else
      return null ;
  }

  protected Word scanAfter(CharSequence sequence, int index)
  {
    int end = wordIterator.following(index) ;
    int start = wordIterator.previous() ;

    if (end == BreakIterator.DONE)
      end = sequence.length() - 1 ;
    
    String text = sequence.subSequence(start, end).toString().trim() ;
    
    if (!text.equals(""))
      return new Word(text, start, isStartOfSentence(sequence, start)) ;
    else
      return null ;
  }
  
  
  /*
   * (non-Javadoc)
   * 
   * @see org.dts.spell.tokenizer.AbstractWordTokenizer#currentWord(int)
   */
  public Word currentWord(int index)
  {
    assert index >= 0 ;
      
    CharSequence sequence = getCharSequence() ;
    int length = sequence.length() ;
    Word result = null ;
    
    if (length > 0 && !(length < index))
    {
      if (index == length)
        result = scanBefore(sequence, index - 1) ;
      else if (wordIterator.isBoundary(index) && Character.isWhitespace(sequence.charAt(index)))
        result = scanBefore(sequence, index) ;        
      else
        result = scanAfter(sequence, index) ;        
    }

    return result ;
  }

  private void onChangeSequence()
  {
    CharSequence sequence = getCharSequence() ;

    wordSequence.setCharSequence(sequence) ;
    wordIterator.setText(wordSequence) ;
  }

  private void onInsertChars(int start, int end)
  {
    onChangeSequence() ;
  }

  private void onDeleteChars(int start, int end)
  {
    onChangeSequence() ;
  }

  public void updateCharSequence(int start, int end, int cause)
  {
    switch (cause)
    {
      case CHANGE_SEQUENCE:
        onChangeSequence() ;
        break ;

      case INSERT_CHARS:
        onInsertChars(start, end) ;
        break ;

      case DELETE_CHARS:
        onDeleteChars(start, end) ;
        break ;
    }
  }
}
