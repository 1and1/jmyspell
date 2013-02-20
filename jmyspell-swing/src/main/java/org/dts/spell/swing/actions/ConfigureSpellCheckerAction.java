/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dts.spell.swing.actions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.dts.spell.swing.JTextComponentSpellChecker;
import org.dts.spell.swing.panels.DictionaryConfigPanel;
import org.dts.spell.swing.utils.CancelableDialog;

/**
 *
 * @author developer
 */
public class ConfigureSpellCheckerAction extends AbstractAction {

  private JTextComponentSpellChecker txtCmpSpellChecker;

  public ConfigureSpellCheckerAction(JTextComponentSpellChecker txtCmpSpellChecker) {
    super("Configurar corrector...");
    this.txtCmpSpellChecker = txtCmpSpellChecker;
  }

  public void actionPerformed(ActionEvent e) {
    Window parent = null;
    Object obj = e.getSource() ;

    if (obj instanceof Component) {
      parent = SwingUtilities.getWindowAncestor((Component) obj) ;
    }
    else if (JFrame.getFrames().length > 0)
      parent = JFrame.getFrames()[0] ;

    CancelableDialog dialog = new CancelableDialog(parent, (String) getValue(NAME));

    dialog.setLayout(new BorderLayout());
    dialog.add(new DictionaryConfigPanel(getTxtCmpSpellChecker(), dialog.getCloseAction()));

    dialog.pack();
    dialog.setLocationRelativeTo(parent) ;
    dialog.setVisible(true);
  }

  /**
   * @return the txtCmpSpellChecker
   */
  public JTextComponentSpellChecker getTxtCmpSpellChecker() {
    return txtCmpSpellChecker;
  }
}
