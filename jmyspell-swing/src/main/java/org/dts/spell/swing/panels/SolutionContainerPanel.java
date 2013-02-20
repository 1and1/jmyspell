/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dts.spell.swing.panels;

import java.awt.CardLayout;
import java.util.ServiceLoader;
import javax.swing.JPanel;
import org.dts.spell.ErrorInfo;
import org.dts.spell.event.ProgressListener;
import org.dts.spell.swing.JTextComponentSpellChecker;

/**
 *
 * @author personal
 */
public class SolutionContainerPanel extends JPanel {

  private SolutionPanel noErrorPanel;
  private SolutionPanel searchSolutionPanel;
  private SolutionPanel loadDictionaryPanel;
  private SolutionPanel noDictionaryPanel;
  private SolutionPanel currentPanel;

  public SolutionContainerPanel(JTextComponentSpellChecker txtCmpSpellChecker) {
    initPanels(txtCmpSpellChecker);
  }

  protected void initPanels(JTextComponentSpellChecker txtCmpSpellChecker) {
    CardLayout cardLayout = new CardLayout(5, 5);
    setLayout(cardLayout);

    addInternalSolutionPanel(getNoErrorPanel(), txtCmpSpellChecker);
    addInternalSolutionPanel(getSearchErrorPanel(), txtCmpSpellChecker);
    addInternalSolutionPanel(getLoadDictionaryPanel(), txtCmpSpellChecker);
    addInternalSolutionPanel(getNoDictionaryPanel(), txtCmpSpellChecker);

    ServiceLoader<SolutionPanel> loader = ServiceLoader.load(SolutionPanel.class);

    for (SolutionPanel panel : loader) {
      addInternalSolutionPanel(panel, txtCmpSpellChecker) ;
    }

    currentPanel = getNoErrorPanel();
  }

  protected void addInternalSolutionPanel(SolutionPanel panel, JTextComponentSpellChecker txtCmpSpellChecker) {
    panel.initFrom(txtCmpSpellChecker);
    addSolutionPanel(panel);
  }

  protected void addSolutionPanel(SolutionPanel panel) {
    add(panel, panel.getName());
  }

  protected CardLayout getCardLayout() {
    return (CardLayout) getLayout();
  }

  public SolutionPanel getSearchErrorPanel() {
    if (null == searchSolutionPanel) {
      searchSolutionPanel = new SearchSolutionPanel();
    }

    return searchSolutionPanel;
  }

  public SolutionPanel getNoErrorPanel() {
    if (null == noErrorPanel) {
      noErrorPanel = new NoErrorSolutionPanel();
    }

    return noErrorPanel;
  }

  public ProgressListener getLoadDictionaryPanelAsListener() {
    return (ProgressListener) getLoadDictionaryPanel() ;
  }

  public SolutionPanel getLoadDictionaryPanel() {
    if (null == loadDictionaryPanel) {
      loadDictionaryPanel = new LoadDictionaryPanel();
    }

    return loadDictionaryPanel;
  }

  public SolutionPanel getNoDictionaryPanel() {
    if (null == noDictionaryPanel) {
      noDictionaryPanel = new NoDictionaryInstalledPanel() ;
    }

    return noDictionaryPanel ;
  }

  public void showSolutionPanel(SolutionPanel panel) {
    getCardLayout().show(this, panel.getName());
    currentPanel = panel;
  }

  protected SolutionPanel getPanelByName(String name) {
    for (int i = 0; i < getComponentCount(); ++i) {
      if (name.equals(getComponent(i).getName())) {
        return (SolutionPanel) getComponent(i);
      }
    }

    return null;
  }

  public SolutionPanel getSolutionPanelFor(ErrorInfo errorInfo) {
    for (int i = 0; i < getComponentCount(); ++i) {
      if (getComponent(i) instanceof SolutionPanel) {
        SolutionPanel panel = (SolutionPanel) getComponent(i);

        if (panel.isForError(errorInfo)) {
          return panel;
        }
      }
    }

    return getNoErrorPanel();
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);

    if (null != currentPanel) {
      currentPanel.setEnabled(enabled);
    }
  }
}
