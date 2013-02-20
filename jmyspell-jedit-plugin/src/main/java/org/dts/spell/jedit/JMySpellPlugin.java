package org.dts.spell.jedit ;

import java.io.IOException;

import org.gjt.sp.jedit.* ;
import org.gjt.sp.jedit.EditPlugin ;
import org.gjt.sp.jedit.View ;
import org.gjt.sp.jedit.jEdit ;

import org.dts.spell.SpellChecker ;

public class JMySpellPlugin extends EditPlugin
{
  private static JMySpellChecker jmySpellChecker ;

  /**
   * Displays the spell checker dialog box. This method is called by the
   * spell-check action, defined in actions.xml.
   * 
   * @param view
   *          the current view.
   */
  public static void staticSpellCheck(View view)
  {
    try
    {
      if (null != jmySpellChecker)
        jmySpellChecker.checkText(view) ;
      else
        Macros.error(view, jEdit.getProperty("jmyspell.no-init")) ;
    }
    catch (IllegalStateException ex)
    {
      Macros.error(view, jEdit.getProperty("jmyspell.error.dictionary.not-found"))  ;      
    }
    catch (Exception e) 
    {
      Macros.error(view, e.getLocalizedMessage()) ;
    }
  }

  /**
   * Method called by jEdit to initialize the plugin.
   */
  @Override
  public void start()
  {
    try
    {
      jmySpellChecker = new JMySpellChecker() ;
    }
    catch (Exception e)
    {
      Macros.error(jEdit.getActiveView(), e.getLocalizedMessage()) ;
    }
  }

  @Override
  public void stop()
  {
    unloadDictionary() ;
    jmySpellChecker = null ;
  }
  
  public static void unloadDictionary()
  {
    if (null != jmySpellChecker)
      jmySpellChecker.unloadDictionary() ;
  }

  public static void reloadDictionary()
  {
    try
    {
      if (null != jmySpellChecker)
      {
        jmySpellChecker.unloadDictionary() ;
        jmySpellChecker.loadDictionary() ;
      }
      else
        jmySpellChecker = new JMySpellChecker() ;
    }
    catch (IOException e)
    {
      Macros.error(jEdit.getActiveView(), e.getLocalizedMessage()) ;
    }
  }
  
  public static void resetSpellChecker()
  {
    if (null != jmySpellChecker)    
      jmySpellChecker.resetSpellChecker() ;
  }

  public static SpellChecker getSpellChecker()
  {
    if (jmySpellChecker != null)
      return jmySpellChecker.getSpellChecker() ;
    else
      return null ;
  }
  
  ////
  // TO DEBUG :
  ////
  public static void main(String[] args)
  {
    org.gjt.sp.jedit.jEdit.main(args) ;
  }
}