package org.dts.spell.jedit ;

import org.dts.spell.SpellChecker ;
import org.dts.spell.dictionary.OpenOfficeSpellDictionary ;
import org.dts.spell.dictionary.SpellDictionary ;
import org.gjt.sp.jedit.Macros ;
import org.gjt.sp.jedit.View ;
import org.gjt.sp.jedit.jEdit ;
import org.gjt.sp.jedit.Buffer ;
import org.gjt.sp.jedit.textarea.JEditTextArea ;
import org.gjt.sp.jedit.textarea.Selection ;

import java.io.File ;
import java.io.IOException ;
import java.util.zip.ZipFile ;

import javax.swing.text.Position;
import org.gjt.sp.jedit.buffer.JEditBuffer;

public class JMySpellChecker
{
  private SpellDictionary dictionary = null ;

  private SpellChecker spellChecker = null ;

  /**
   * Creates a new JMySpellChecker object.
   * 
   */
  public JMySpellChecker() throws IOException
  {
    if (isAutomaticLoadDictionary())
      loadDictionary() ;
  }

  private boolean checkSelections(
      Selection[] selection,
      JEditBuffer buffer,
      UIErrorListener listener)
  {
    boolean result = true ;
    boolean isTokenMode = isTokenMode() ;

    for (int i = 0 ; i < selection.length ; ++i)
    {
      listener.setOriginOffSet(selection[i].getStart()) ;
      
      result &= spellChecker.check(
          new BufferWordFinder(buffer, selection[i], isTokenMode),
          listener) ;
    }

    return result ;
  }

  public void checkText(View view) throws IOException
  {
    if (null == spellChecker)
      loadDictionary() ;

    JEditTextArea textArea = view.getTextArea() ;
    JEditBuffer buffer = textArea.getBuffer() ;
    UIErrorListener listener = new UIErrorListener(textArea) ;
    Position caretPos = buffer.createPosition(textArea.getCaretPosition()) ;
    Selection[] selections = textArea.getSelection() ;
    boolean hasSelection = null != selections && selections.length > 0 ;

    spellChecker.setIgnoreUpperCaseWords(JMySpellChecker.isIgnoreUpperCase()) ;
    spellChecker.setCaseSensitive(!JMySpellChecker.isIgnoreCase()) ;
    spellChecker.setSkipNumbers(JMySpellChecker.isIgnoreDigits()) ;
    
    if (!hasSelection)
      selections = new Selection[] { new Selection.Range(0, buffer.getLength()) } ;

    if (checkSelections(selections, buffer, listener))
      Macros.message(view, jEdit.getProperty("jmyspell.no-error.found")) ;

    if (hasSelection)
      textArea.setSelection(selections) ;

    textArea.setCaretPosition(caretPos.getOffset()) ;

    if (isResetSpellChecker())
      resetSpellChecker() ;
  }

  public void loadDictionary() throws IOException
  {
    File dictionaryFile = getDictionaryFile() ;

    if (dictionaryFile.getName().endsWith("zip"))
      dictionary = new OpenOfficeSpellDictionary(new ZipFile(dictionaryFile)) ;
    else
      dictionary = new OpenOfficeSpellDictionary(dictionaryFile) ;

    if (null == spellChecker)
      spellChecker = new SpellChecker(dictionary) ;
    else
      spellChecker.setDictionary(dictionary) ;
  }

  /**
   * Resets list of ignored words.
   */
  public void resetSpellChecker()
  {
    if (null != spellChecker)
      spellChecker.resetIgnore() ;
  }

  /**
   * 
   */
  public void unloadDictionary()
  {
    spellChecker = null ;
    dictionary = null ;
    System.gc() ;
  }

  /**
   * @return Returns the spellChecker.
   */
  public SpellChecker getSpellChecker()
  {
    return spellChecker ;
  }

  private static final String AUTOMATIC_DOWNLOAD = "options.jmyspell.auto-load" ;  
  private static final String RESET_SPELLCHECKER = "options.jmyspell.auto-reset" ;  
  private static final String TOKEN_MODE = "options.jmyspell.token-mode" ;
  private static final String DICTIONARY = "options.jmyspell.dictionary" ;
  private static final String IGNORE_DIGITS = "options.jmyspell.spell.ignore-digits" ;
  private static final String IGNORE_CASE = "options.jmyspell.spell.ignore-case" ;
  private static final String IGNORE_UPPERCASE = "options.jmyspell.spell.ignore-uppercase" ;
  
  public static boolean isResetSpellChecker()
  {
    return jEdit.getBooleanProperty(RESET_SPELLCHECKER, false) ;
  }

  public static void setResetSpellChecker(boolean on)
  {
    jEdit.setBooleanProperty(RESET_SPELLCHECKER, on) ;
  }
  
  public static boolean isAutomaticLoadDictionary()
  {
    return jEdit.getBooleanProperty(AUTOMATIC_DOWNLOAD, false) ;
  }

  public static void setAutomaticLoadDictionary(boolean on)
  {
    jEdit.setBooleanProperty(AUTOMATIC_DOWNLOAD, on) ;
  }
  
  public static boolean isTokenMode()
  {
    return jEdit.getBooleanProperty(TOKEN_MODE, false) ;
  }

  public static void setTokeMode(boolean on)
  {
    jEdit.setBooleanProperty(TOKEN_MODE, on) ;
  }
  
  public static File getDictionaryFile()
  {
    return new File(jEdit.getProperty(DICTIONARY, "")) ;
  }
  
  public static void setDictionaryFile(String file)
  {
    jEdit.setProperty(DICTIONARY, file) ;
  }

  public static boolean isIgnoreUpperCase()
  {
    return jEdit.getBooleanProperty(IGNORE_UPPERCASE, false) ;
  }
  
  public static void setIgnoreUpperCase(boolean on)
  {
    jEdit.setBooleanProperty(IGNORE_UPPERCASE, on) ;
  }

  public static boolean isIgnoreCase()
  {
    return jEdit.getBooleanProperty(IGNORE_CASE, false) ;  
  }
  
  public static void setIgnoreCase(boolean on)
  {
    jEdit.setBooleanProperty(IGNORE_CASE, on) ;  
  }
  
  public static boolean isIgnoreDigits()
  {
    return jEdit.getBooleanProperty(IGNORE_DIGITS, true) ;
  }
  
  public static void setIgnoreDigits(boolean on)
  {
    jEdit.setBooleanProperty(IGNORE_DIGITS, on) ;  
  }
}
