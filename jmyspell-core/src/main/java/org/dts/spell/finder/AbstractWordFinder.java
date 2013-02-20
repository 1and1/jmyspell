package org.dts.spell.finder ;

/**
 * <p>
 * Defines common methods and behaviour for various word finder subclasses.
 * </p>
 * You only have to override abstracts methods and call method
 * <code>init</code> when your data is set. For example :
 * <code>
 * 	class StringWordFinder extends AbstractWordFinder
 *  {
 *  	private String text ;
 *  
 *  	public StringWordFinder(String text)
 *    {
 *    	this.text = text ;
 *    
 *    	init() ; // Remember call this method to setup variables.
 *    }
 *  }
 * </code>
 * 
 * @author Anthony Roy (ajr@antroy.co.uk)
 * @author DreamTangerine (DreamTangerine@hotmail.com)
 */
public abstract class AbstractWordFinder implements WordFinder
{
  private Word currentWord = null ;
  private Word nextWord = null ;

  /**
   * Create a AbstractWordFinder
   *  
   */
  public AbstractWordFinder()
  {
  }

  /**
   * Find the next Word of the iteration.
   * 
   * @param currentWord
   *          Current interation word. It can be null if the first iteration.
   * 
   * @return The next word or null if not more words are found.
   */
  protected abstract Word next(Word currentWord) ;

  /**
   * This method scans the text from the end of the last word, and returns a new
   * Word object corresponding to the next word.
   * 
   * @return the next word.
   */
  public Word next()
  {
    if (nextWord == null)
      throw new WordNotFoundException() ;

    currentWord = nextWord ;
    nextWord = next(currentWord) ;

    return currentWord ;
  }

  /**
   * Returns the current word in the iteration.
   * 
   * @return the current word.
   * @throws WordNotFoundException
   *           current word has not yet been set.
   */
  public Word current()
  {
    if (null == currentWord)
      throw new WordNotFoundException() ;

    return currentWord ;
  }

  public boolean hasCurrent()
  {
    return null != currentWord ;
  }
  
  /**
   * @return true if there are further words in the string.
   */
  public boolean hasNext()
  {
    return nextWord != null ;
  }

  /**
   * Replace the current word with the new string newWord.
   * 
   * @param newWord
   *          The new string for the current word.
   * @param currentWord
   *          The current word.
   */
  protected abstract void replace(String newWord, Word currentWord) ;

  /**
   * Replace the current word in the search with a replacement string.
   * 
   * @param newWord
   *          the replacement string.
   * @throws WordNotFoundException
   *           current word has not yet been set.
   */
  public void replace(String newWord)
  {
    if (currentWord == null)
      throw new WordNotFoundException() ;

    boolean isStart = currentWord.isStartOfSentence() ;
    
    if (isStart)
      newWord = Word.getStartSentenceWordCase(newWord) ;
    
    replace(newWord, currentWord) ;
    
    // Change the current word
    currentWord = new Word(newWord, currentWord.getStart(), isStart) ;
    nextWord = next(currentWord) ;
  }

  /**
   * Call this function each time you want begin iterations.
   *  
   */
  public void init()
  {
    init(null) ;
  }
  
  /**
   * Call this function to init from a word. It can be null.
   */
  public void init(Word initWord)
  {
    currentWord = initWord ;
    nextWord = initWord ;
   
    if (null == initWord)
      nextWord = next(initWord) ;
  }
}
