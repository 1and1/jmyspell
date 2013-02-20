/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dts.spell.swing.actions;

import org.dts.spell.swing.JTextComponentSpellChecker;

/**
 *
 * @author dreamtangerine
 */
public abstract class ActionManager {

  private AddWordAction addWordAction ;
  private IgnoreWordAction ignoreWordAction ;
  private DeleteWordAction deleteWordAction ;
  private ReplaceWordAction replaceWordAction ;
  private ReplaceAllWordsAction replaceAllWordsAction ;

  private SpellCheckAction spellCheckAction ;
  private RealTimeSpellCheckAction realTimeSpellCheckAction ;

  protected abstract AddWordAction createAddWordAction(JTextComponentSpellChecker cmpTxtSpellChecker) ;

  protected abstract IgnoreWordAction createIgnoreWordAction(JTextComponentSpellChecker cmpTxtSpellChecker) ;

  protected abstract DeleteWordAction createDeleteWordAction(JTextComponentSpellChecker cmpTxtSpellChecker) ;

  protected abstract ReplaceWordAction createReplaceWordAction(JTextComponentSpellChecker cmpTxtSpellChecker) ;

  protected abstract ReplaceAllWordsAction createReplaceAllWordsAction(JTextComponentSpellChecker cmpTxtSpellChecker) ;

  protected abstract SpellCheckAction createSpellCheckAction(JTextComponentSpellChecker cmpTxtSpellChecker) ;
  
  protected abstract RealTimeSpellCheckAction createRealTimeSpellCheckAction(JTextComponentSpellChecker cmpTxtSpellChecker) ;

  /**
   * @return the addWordAction
   */
  public AddWordAction getAddWordAction(JTextComponentSpellChecker cmpTxtSpellChecker) {
    if (null == addWordAction)
      addWordAction = createAddWordAction(null) ;

    return addWordAction;
  }

  /**
   * @return the ignoreWordAction
   */
  public IgnoreWordAction getIgnoreWordAction(JTextComponentSpellChecker cmpTxtSpellChecker) {
    if (null == ignoreWordAction)
      ignoreWordAction = createIgnoreWordAction(cmpTxtSpellChecker) ;

    return ignoreWordAction;
  }

  /**
   * @return the deleteWordAction
   */
  public DeleteWordAction getDeleteWordAction(JTextComponentSpellChecker cmpTxtSpellChecker) {
    if (null == deleteWordAction)
      deleteWordAction = createDeleteWordAction(cmpTxtSpellChecker) ;

    return deleteWordAction;
  }

  /**
   * @return the spellCheckAction
   */
  public SpellCheckAction getSpellCheckAction(JTextComponentSpellChecker cmpTxtSpellChecker) {
    if (null == spellCheckAction)
      spellCheckAction = createSpellCheckAction(cmpTxtSpellChecker) ;
    
    return spellCheckAction;
  }

  /**
   * @return the realTimeSpellCheckAction
   */
  public RealTimeSpellCheckAction getRealTimeSpellCheckAction(JTextComponentSpellChecker cmpTxtSpellChecker) {
    if (null == realTimeSpellCheckAction)
      realTimeSpellCheckAction = createRealTimeSpellCheckAction(cmpTxtSpellChecker) ;
    
    return realTimeSpellCheckAction;
  }

  // return a shareable action
  public ReplaceWordAction getReplaceWordAction(JTextComponentSpellChecker cmpTxtSpellChecker) {
    if (null == replaceWordAction)
      replaceWordAction = createReplaceWordAction(cmpTxtSpellChecker) ;

    return replaceWordAction ;
  }

  // return a new action every call
  public ReplaceWordAction getReplaceWordAction(JTextComponentSpellChecker cmpTxtSpellChecker, String sugestion) {
    ReplaceWordAction result = createReplaceWordAction(cmpTxtSpellChecker) ;

    result.setSuggestion(sugestion) ;

    return result ;
  }

    // return a shareable action
  public ReplaceAllWordsAction getReplaceAllWordsAction(JTextComponentSpellChecker cmpTxtSpellChecker) {
    if (null == replaceAllWordsAction)
      replaceAllWordsAction = createReplaceAllWordsAction(cmpTxtSpellChecker) ;

    return replaceAllWordsAction ;
  }
}
