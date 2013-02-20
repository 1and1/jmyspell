/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dts.spell.swing.panels;

import org.dts.spell.ErrorInfo;
import org.dts.spell.swing.JTextComponentSpellChecker;
import org.dts.spell.swing.actions.ConfigureSpellCheckerAction;

/**
 *
 * @author developer
 */
public class NoDictionaryInstalledPanel extends SimpleSolutionPanel {

  public NoDictionaryInstalledPanel() {
    super(null, "NO_DICTIONARY_INSTALLED", "<html>No hay diccionarios instalados.</html>", "<html>No hay diccionarios instalados.<p>Use el diálogo de configuración para instalar alguno.</html>");

    //super(Messages.getString("JRealTimeSpellPanel.NO_ERROR"), Messages.getString("JRealTimeSpellPanel.NO_ERROR_TITLE"), "NO_ERROR_PANEL") ;
  }

  @Override
  public boolean isForError(ErrorInfo errorInfo) {
    return false;
  }

  @Override
  public void initFrom(JTextComponentSpellChecker txtCmpSpellChecker) {
    super.initFrom(txtCmpSpellChecker);

    setAction(new ConfigureSpellCheckerAction(txtCmpSpellChecker));
  }

  @Override
  public void calcSolution(ErrorInfo errorInfo) {
  }

  @Override
  public void setEnabled(boolean enabled) {
    // we never disable
  }
}
