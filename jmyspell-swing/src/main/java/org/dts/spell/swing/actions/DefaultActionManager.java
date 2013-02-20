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
public class DefaultActionManager extends ActionManager {

  @Override
  public AddWordAction createAddWordAction(JTextComponentSpellChecker cmpTxtSpellChecker) {
    return new AddWordAction(cmpTxtSpellChecker) ;
  }

  @Override
  public IgnoreWordAction createIgnoreWordAction(JTextComponentSpellChecker cmpTxtSpellChecker) {
    return new IgnoreWordAction(cmpTxtSpellChecker) ;
  }

  @Override
  public DeleteWordAction createDeleteWordAction(JTextComponentSpellChecker cmpTxtSpellChecker) {
    return new DeleteWordAction(cmpTxtSpellChecker) ;
  }

  @Override
  public SpellCheckAction createSpellCheckAction(JTextComponentSpellChecker cmpTxtSpellChecker) {
    return new SpellCheckAction(cmpTxtSpellChecker) ;
  }

  @Override
  public RealTimeSpellCheckAction createRealTimeSpellCheckAction(JTextComponentSpellChecker cmpTxtSpellChecker) {
    return new RealTimeSpellCheckAction(cmpTxtSpellChecker) ;
  }

  @Override
  protected ReplaceWordAction createReplaceWordAction(JTextComponentSpellChecker cmpTxtSpellChecker) {
    return new ReplaceWordAction(cmpTxtSpellChecker) ;
  }

  @Override
  protected ReplaceAllWordsAction createReplaceAllWordsAction(JTextComponentSpellChecker cmpTxtSpellChecker) {
    return new ReplaceAllWordsAction(cmpTxtSpellChecker) ;
  }
}
