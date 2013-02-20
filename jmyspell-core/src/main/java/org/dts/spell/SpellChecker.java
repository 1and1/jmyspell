/*
 * Created on 04/01/2005
 *
 */
package org.dts.spell ;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dts.spell.dictionary.SpellDictionary ;
import org.dts.spell.event.ErrorCountListener ;
import org.dts.spell.event.FindSpellCheckErrorListener ;
import org.dts.spell.event.SpellCheckEvent ;
import org.dts.spell.event.SpellCheckListener ;
import org.dts.spell.finder.Word ;
import org.dts.spell.finder.WordFinder ;
import org.dts.spell.finder.CharSequenceWordFinder ;

/**
 * @author DreamTangerine
 *  
 */
public class SpellChecker
{
  private Set<String> ignore = new HashSet<String>() ;
  private Map<String, String> replace = new HashMap<String, String>() ;  

  private boolean skipNumbers = true ;
  private boolean ignoreUpperCaseWords = false ;
  private boolean caseSensitive = true ; 
  
  public SpellChecker(SpellDictionary dictionary)
  {
    this.dictionary = dictionary ;
  }

  private SpellDictionary dictionary ;

  /**
   * @param dictionary
   *          The dictionary to set.
   */
  public void setDictionary(SpellDictionary dictionary)
  {
    this.dictionary = dictionary ;
  }

  public SpellDictionary getDictionary()
  {
    return dictionary ;
  }

  /**
   * This method add teh word to the ignore table.
   * 
   * @param word
   */
  public void addIgnore(String word)
  {
    ignore.add(word.trim()) ;
  }
  
  public void resetIgnore()
  {
    ignore.clear() ;
  }
  
  public void setSkipNumbers(boolean skip)
  {
    skipNumbers = skip ;
  }
  
  public boolean isSkipNumbers()
  {
    return skipNumbers ;
  }
  
  /**
   * This method add a word to the replace table.
   * 
   * @param oldWord old word to replace.
   * @param newWord new word to replace
   */
  public void addReplace(String oldWord, String newWord)
  {
    replace.put(oldWord.trim(), newWord.trim()) ;
  }
  
  public void resetReplace()
  {
    replace.clear() ;  
  }
  
  public void setCaseSensitive(boolean sensitive)
  {
    caseSensitive = sensitive ;
  }
  
  public boolean isCaseSensitive()
  {
    return caseSensitive ;
  }
  
  public boolean isIgnoreUpperCaseWords()
  {
    return ignoreUpperCaseWords ;
  }
  
  public void setIgnoreUpperCaseWords(boolean ignore)
  {
    ignoreUpperCaseWords = ignore ;
  }
  
  private boolean checkCase(Word word)
  {
    if (isCaseSensitive())
      return word.isCorrectFirstChar() ;
    else
      return true ;
  }

  private final FindSpellCheckErrorListener ERROR_FIND_LISTENER = new FindSpellCheckErrorListener() ;

  
  /**
   * @return true si todo ha ido bien y no tiene errores.
   *  
   */
  public boolean isCorrect(CharSequence txt)
  {
    return isCorrect(new CharSequenceWordFinder(txt)) ;
  }

  public boolean isCorrect(WordFinder finder)
  {
    return check(finder, ERROR_FIND_LISTENER) ;
  }

  public Word checkSpell(CharSequence txt)
  {
    return checkSpell(new CharSequenceWordFinder(txt)) ;
  }
  
  public Word checkSpell(WordFinder finder)
  {
    check(finder, ERROR_FIND_LISTENER) ;

    return ERROR_FIND_LISTENER.getInvalidWord() ;
  }

  public ErrorInfo hasSpellErrors(CharSequence txt)
  {
    return hasSpellErrors(new CharSequenceWordFinder(txt)) ;
  }
  
  public ErrorInfo hasSpellErrors(WordFinder finder)
  {
    check(finder, ERROR_FIND_LISTENER) ;
    
    return ERROR_FIND_LISTENER.getErrorInfo() ;
  }
  
  private final ErrorCountListener ERROR_COUNT_LISTENER = new ErrorCountListener() ;

  public int getErrorCount(CharSequence txt)
  {
    return getErrorCount(new CharSequenceWordFinder(txt)) ;
  }

  public int getErrorCount(WordFinder finder)
  {
    check(finder, ERROR_COUNT_LISTENER) ;

    return ERROR_COUNT_LISTENER.getErrorsCount() ;
  }
  
  private boolean isRepeat(Word word, Word last)
  {
    return null != last && last.hasLetters() && word.equalIgnoreCaseText(last) && !word.isStartOfSentence() ;
  }

  private boolean isNumber(Word word)
  {
    return !word.hasLetters() ;
  }
  
  private boolean canSkipWord(Word word)
  {
    // TODO : Skip internet address
    return (isNumber(word) && isSkipNumbers()) ||
           (word.isUpperCase() && isIgnoreUpperCaseWords()) ;
  }
  
	private SpellCheckEvent checkCurrent(Word word, Word last, WordFinder finder, SpellCheckListener listener)
	{
    String wordText = word.getText() ;
    String newString = replace.get(wordText) ;
    SpellDictionary dict = getDictionary() ;		
		SpellCheckEvent event = null ;

    if (null != newString)
    {
      finder.replace(newString) ;
      word = finder.current() ;      
      wordText = newString ;
    }
    
    if (isRepeat(word, last))
    {
      event = new SpellCheckEvent(this, finder) ;
      listener.repeatWordError(event) ;
    }
    else if (!canSkipWord(word))
    {
      if (!dict.isCorrect(wordText) && !ignore.contains(wordText))
      {
        event = new SpellCheckEvent(this, finder) ;        
        listener.spellingError(event) ;
      } 
      else if (!checkCase(word))
      {
        event = new SpellCheckEvent(this, finder) ;        
        listener.badCaseError(event) ;
      }
    }
		
		return event ;
	}
	
  // the current lastWord
  private Word lastWord = null ;
  
  public void setLastWord(Word lastWord)
  {
    this.lastWord = lastWord ;
  }
  
  public boolean check(WordFinder finder, SpellCheckListener listener)
  {
    boolean result = true ;
    SpellCheckEvent event = new SpellCheckEvent(this, finder) ;
    
    listener.beginChecking(event) ;
    boolean exit = event.isCancel() ;    

    while (!exit && finder.hasNext())
    {
      Word word = finder.next() ;
			event = checkCurrent(word, lastWord, finder, listener) ;
			
			if (null != event)
			{
	      result = false ;
	      exit = event.isCancel() ;
			}
			
			lastWord = word ;
    }

    listener.endChecking(new SpellCheckEvent(this, finder)) ;
    lastWord = null ;
    
    return result ;
  }
}
