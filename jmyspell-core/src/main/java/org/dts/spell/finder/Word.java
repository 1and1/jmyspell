package org.dts.spell.finder ;

/**
 * A Word, that represent a Word in the WordFinder. Based in Jazzy
 * work. This object can only be mutate by moveTo function.
 * 
 * @see org.dts.spell.finder.WordFinder
 * @author DreamTangerine
 *  
 */
public class Word implements CharSequence
{
  private int start ;
  private String text ;
  private boolean isStart ;

  /**
   * Creates a new Word object.
   * @param text
   *          the String representing the word.
   * @param start
   *          the start index of the word.
   * @param isStart Indicate it is start of the sentence.
   */
  public Word(String text, int start, boolean isStart)
  {
    this.text = text ;
    this.start = start ;
    this.isStart = isStart ;
  }

  /**
   * Creates a new Word object.
   * @param text
   *          the String representing the word.
   * @param start
   *          the start index of the word.
   */
  public Word(String text, int start)
  {
    this(text, start, false) ;
  }
  
  /**
   * return a new word that start at start.
   */
  public void moveTo(int start)
  {
    this.start = start ;
  }
  
  /**
   * @return the end index of the word.
   */
  public int getEnd()
  {
    return getStart() + length();
  }

  /**
   * @return the start index.
   */
  public int getStart()
  {
    return start ;
  }

  /**
   * @return the String representing the word.
   */
  public String getText()
  {
    return text ;
  }
  
  /**
   * @return the length of the word.
   */
  public int length()
  {
    return text.length() ;
  }

  /**
   * @return the text representing the word.
   */
  public String toString()
  {
    return text ;
  }

  /**
   * Nos dice si esta palabra es inicio de frase.
   * 
   * @return true if the word starts a new sentence.
   */
  public boolean isStartOfSentence()
  {
    return isStart ;
  }
  
  public void setStartStartOfSentence(boolean isStart)
  {
    this.isStart = isStart ;
  }
  
  /**
   * Nos dice si está bien el primer carácter. Puesto que al principio de frase debe
   * de ser mayúsculas.
   * 
   * @return Si está bien el primer carácter de la palabra.
   */
  public boolean isCorrectFirstChar()
  {
    char c = text.charAt(0) ;
    
    if (Character.isLetter(c))
    {
      if (isStartOfSentence())
        return Character.isUpperCase(c) ;
      else
        return true ;
    }
    else
      return true ;
  }
  
  public boolean isUpperCase()
  {
    return text.toUpperCase().equals(text) ;
  }
  
  public boolean hasLetters()
  {
    for (int i = 0 ; i < length() ; ++i)
      if (Character.isLetter(text.charAt(i)))
        return true ;
      
    return false ;
  }
  
  public boolean equals(Object o)
  {
    if (null != o && o instanceof Word)
    {
      Word ow = (Word) o ;
      
      return ow.getStart() == getStart() && ow.getText().equals(getText()) ;
    }
    
    return false ;
  }

  public boolean equalText(Word word)
  {
    return equalText(word.getText()) ;
  }

  public boolean equalText(String text)
  {
    return this.getText().equals(text) ;
  }

  public boolean equalIgnoreCaseText(Word word)
  {
    return equalIgnoreCase(word.getText()) ;
  }

  public boolean equalIgnoreCase(String text)
  {
    return getText().equalsIgnoreCase(text) ;
  }
  
  public char charAt(int index)
  {
    return text.charAt(index) ;
  }

  public CharSequence subSequence(int start, int end)
  {
    return text.subSequence(start, end) ;
  }

  public String getStartSentenceWordCase()
  {
    return getStartSentenceWordCase(this) ;
  }
  
  public static String getStartSentenceWordCase(CharSequence word)
  {
    StringBuilder str = new StringBuilder(word) ;
    
    str.setCharAt(0, Character.toUpperCase(word.charAt(0))) ;
    return str.toString() ;
  }
}
