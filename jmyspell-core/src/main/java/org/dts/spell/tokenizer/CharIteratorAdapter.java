/*
 * Created on 02/04/2005
 *
 */
package org.dts.spell.tokenizer;

import java.text.CharacterIterator;

public class CharIteratorAdapter implements CharacterIterator
{
  private CharSequence sequence ;
  int index = 0 ;
  
  public CharIteratorAdapter()
  {
    this("") ;
  }
  
  public CharIteratorAdapter(CharSequence sequence)
  {
    this.sequence = sequence ;
  }
  
  public char first()
  {
    index = getBeginIndex() ;

    return current() ;
  }

  public char last()
  {
    if (sequence.length() == 0)
      index = getEndIndex() ;
    else
      index = getEndIndex() - 1 ;
    
    return current() ;
  }

  public char current()
  {
    if (sequence.length() > index)
      return sequence.charAt(index) ;
    else
      return DONE ;
  }

  public char next()
  {
    int endIndex = getEndIndex() ;
    char result ;
    
    index++ ;
    
    if (index >= endIndex)
    {
      index = endIndex ;
      result = DONE ;
    }
    else
      result = sequence.charAt(index) ;
    
    return result ;
  }

  public char previous()
  {
    int beginIndex = getBeginIndex() ;
    char result ;
    
    index-- ;
    
    if (index <= beginIndex)
    {
      index = beginIndex ;
      result = DONE ;
    }
    else
      result = sequence.charAt(index) ;
    
    return result ;
  }

  public char setIndex(int position)
  {
    if (position < getBeginIndex() || position > getEndIndex())
      throw new IllegalArgumentException(
          java.text.MessageFormat.format(
            java.util.ResourceBundle.getBundle("org/dts/spell/messages").
              getString("INVALID_POSITION"),
            position, 
            getBeginIndex(),
            getEndIndex())) ;
    
    index = position ;
    
    return current() ;
  }

  public int getBeginIndex()
  {
    return 0 ;
  }

  public int getEndIndex()
  {
    return sequence.length() ;
  }

  public int getIndex()
  {
    return index ;
  }

  public Object clone()
  {
    try
    {
      return super.clone() ;
    }
    catch (CloneNotSupportedException e)
    {
      e.printStackTrace();
      return null ;
    }
  }
  
  public CharSequence getCharSequence()
  {
    return sequence ;
  }
  
  public void setCharSequence(CharSequence sequence)
  {
    setCharSequence(sequence, true) ;
  }
  
  public void setCharSequence(CharSequence sequence, boolean resetIndex)
  {
    this.sequence = sequence ;
    
    if (resetIndex)
      index = 0 ;
  }
}