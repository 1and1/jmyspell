/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DictionaryConfigPanel.java
 *
 * Created on 05-oct-2009, 9:40:38
 */
package org.dts.spell.swing.panels;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.DictionaryManager;
import org.dts.spell.dictionary.DictionaryManager.LocaleProvider;
import org.dts.spell.dictionary.DictionaryProvider;
import org.dts.spell.event.ProgressEvent;
import org.dts.spell.swing.JTextComponentSpellChecker;
import org.dts.spell.swing.actions.ResetIgnoreWordsAction;
import org.dts.spell.swing.actions.ResetReplaceWordsAction;
import org.dts.spell.swing.utils.ErrorMsgBox;
import org.dts.spell.swing.utils.BlockProgressDialog;
import org.dts.spell.swing.utils.SeparatorLineBorder;

/**
 *
 * @author dreamtangerine
 */
public class DictionaryConfigPanel extends javax.swing.JPanel {

  private ExecutorService loader = Executors.newSingleThreadExecutor();
  private SpellChecker checker;

  /** Creates new form DictionaryConfigPanel */
  public DictionaryConfigPanel(JTextComponentSpellChecker txtCmpSpellChecker, Action closeAction) {
    initComponents();

    checker = txtCmpSpellChecker.getSpellChecker();

    ignoreCase.setSelected(checker.isIgnoreUpperCaseWords());
    ignoreNumbers.setSelected(checker.isSkipNumbers());

    resetIgnoreWordsButton.setAction(getResetIgnoreWordsAction(txtCmpSpellChecker));
    resetReplaceWordsButton.setAction(getResetReplaceWordsAction(txtCmpSpellChecker));
    closeButton.setAction(closeAction);

    unistallDictionaryButtom.setAction(getUninstallAction());
    installDictionaryButton.setAction(getInstallAction());
    //installDictionaryFromFileButton

    initDictionaryCombobox(dictionaryComboBox);
  }

  protected Action getResetIgnoreWordsAction(JTextComponentSpellChecker txtCmpSpellChecker) {
    return new ResetIgnoreWordsAction(txtCmpSpellChecker);
  }

  protected Action getResetReplaceWordsAction(JTextComponentSpellChecker txtCmpSpellChecker) {
    return new ResetReplaceWordsAction(txtCmpSpellChecker);
  }

  public class UninstallAction extends AbstractAction implements ItemListener {

    private DictionaryManager.LocaleProvider localeProvider;

    public UninstallAction() {
      super("Desinstalar");
    }

    public void actionPerformed(ActionEvent e) {
      try {
        if (JOptionPane.showConfirmDialog(
                DictionaryConfigPanel.this,
                String.format("¿ Desea desinstalar ?"),
                "Desintalar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
          DictionaryManager.get().unistall(localeProvider);
        }
      } catch (Exception ex) {
        ErrorMsgBox.show(DictionaryConfigPanel.this, ex);
      }
    }

    public void itemStateChanged(ItemEvent e) {
      if (ItemEvent.SELECTED == e.getStateChange()) {
        Object selObj = e.getItem();

        if (selObj instanceof DictionaryManager.LocaleProvider) {
          localeProvider = (LocaleProvider) selObj;
        } else {
          localeProvider = null;
        }

        setEnabled(null != localeProvider);
      }
    }
  }

  protected DictionaryProvider selectDictionaryProvider() {
    DictionaryProvider[] dps = DictionaryManager.get().getDictionaryProviders();
    String[] names = new String[dps.length];
    int selected = 0;

    for (int i = 0; i < dps.length; ++i) {
      names[i] = dps[i].getDisplayName();
    }

    /*if (dps.length > 1) */ {
      selected = JOptionPane.showOptionDialog(
              DictionaryConfigPanel.this,
              "Seleccione el tipo de diccionario", "Selección de tipo", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, names, names[0]);
    }

    return dps[selected];
  }

  public class InstallDictionariesAction extends AbstractAction {

    public InstallDictionariesAction() {
      super("Instalar más diccionarios...");
    }

    public void actionPerformed(ActionEvent e) {
      final DictionaryProvider dp = selectDictionaryProvider();
      final BlockProgressDialog dlg = new BlockProgressDialog(null);
      try {
        List<Locale> locales = dlg.doTask(new Callable<List<Locale>>() {

          public List<Locale> call() throws Exception {
            dlg.beginProgress(new ProgressEvent(this, "Empezando", 1, 10));
            List<Locale> result = dp.getAvailableLocales();
            dlg.nextStep(new ProgressEvent(this, "Leidos los locales", 2, 10));
            try {
              for (int i = 2; i < 10; ++i) {
                Thread.sleep(500);
                dlg.nextStep(new ProgressEvent(this, "Leidos los locales", i, 10));
              }
            } catch (InterruptedException ex) {
              Logger.getLogger(DictionaryConfigPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            dlg.endProgress(new ProgressEvent(this, "Bien leido", 10, 10));
            return result;
          }
        });

        if (null != dp) {
          JOptionPane.showOptionDialog(
                  DictionaryConfigPanel.this,
                  "Seleccione el diccionario", "Selección de diccionario", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, locales.toArray(), locales.get(0));
        }
      } catch (Exception ex) {
        Logger.getLogger(DictionaryConfigPanel.class.getName()).log(Level.SEVERE, null, ex);
      }

    }
  }

  protected Action getUninstallAction() {
    UninstallAction result = new UninstallAction();
    dictionaryComboBox.addItemListener(result);

    return result;
  }

  protected Action getInstallAction() {
    return new InstallDictionariesAction();
  }

  protected String getDisplayLabel(Locale locale) {
    return String.format("%s de %s", locale.getDisplayLanguage(), locale.getDisplayCountry());
  }

  protected void initDictionaryCombobox(JComboBox dictionaryComboBox) {
    dictionaryComboBox.setRenderer(new DefaultListCellRenderer() {

      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String txt;
        if (value instanceof Locale) {
          Locale locale = (Locale) value;
          txt = getDisplayLabel(locale);
        } else {
          txt = (String) value;
        }

        return super.getListCellRendererComponent(list, txt, index, isSelected, cellHasFocus);
      }
    });
  }

  protected void fillDictionaries() {

    dictionaryComboBox.addItem("Cargando locales, por favor espere");

    loader.submit(new Runnable() {

      public void run() {
        DictionaryManager dm = DictionaryManager.get();

        List<DictionaryManager.LocaleProvider> locales = dm.getInstalledLocales();
        Collections.sort(locales, new Comparator<DictionaryManager.LocaleProvider>() {

          public int compare(DictionaryManager.LocaleProvider o1, DictionaryManager.LocaleProvider o2) {
            return o1.getLocale().getDisplayLanguage().compareTo(o1.getLocale().getDisplayLanguage());
          }
        });

        fillCombobox(locales);
      }
    });
  }

  protected void fillCombobox(final List<DictionaryManager.LocaleProvider> locales) {
    Runnable run = new Runnable() {

      public void run() {
        dictionaryComboBox.removeAllItems();

        if (locales.isEmpty()) {
          dictionaryComboBox.addItem("No tiene diccionarios instalados");
        } else {
          for (DictionaryManager.LocaleProvider localeProvider : locales) {
            dictionaryComboBox.addItem(localeProvider.getLocale());
          }
        }

        dictionaryComboBox.setSelectedItem(checker.getDictionary().getLocale());
      }
    };

    if (SwingUtilities.isEventDispatchThread()) {
      SwingUtilities.invokeLater(run);
    } else {
      run.run();
    }
  }

  @Override
  public void addNotify() {
    super.addNotify();
    loader = Executors.newSingleThreadExecutor();
    fillDictionaries();
  }

  @Override
  public void removeNotify() {
    super.removeNotify();

    if (null != loader) {
      loader.shutdown();
    }

    loader = null;

    checker.setIgnoreUpperCaseWords(ignoreCase.isSelected());
    checker.setSkipNumbers(ignoreNumbers.isSelected());
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    dictionaryPanel = new javax.swing.JPanel();
    dictionaryComboBox = new javax.swing.JComboBox();
    dictionaryLabel = new javax.swing.JLabel();
    unistallDictionaryButtom = new javax.swing.JButton();
    installDictionaryButton = new javax.swing.JButton();
    installDictionaryFromFileButton = new javax.swing.JButton();
    basicPropertiesPanel = new javax.swing.JPanel();
    resetIgnoreWordsLabel1 = new javax.swing.JLabel();
    ignoreNumbers = new javax.swing.JCheckBox();
    ignoreCase = new javax.swing.JCheckBox();
    resetIgnoreWordsLabel = new javax.swing.JLabel();
    resetIgnoreWordsButton = new javax.swing.JButton();
    resetReplaceWordsButton = new javax.swing.JButton();
    closeButton = new javax.swing.JButton();

    dictionaryPanel.setBorder(BorderFactory.createTitledBorder(SeparatorLineBorder.get(), "Diccionario"));

    dictionaryLabel.setText("Diccionario actual:");

    unistallDictionaryButtom.setText("Desintalar");

    installDictionaryButton.setText("Instalar más diccionarios...");

    installDictionaryFromFileButton.setText("Instalar diccionario desde fichero...");

    javax.swing.GroupLayout dictionaryPanelLayout = new javax.swing.GroupLayout(dictionaryPanel);
    dictionaryPanel.setLayout(dictionaryPanelLayout);
    dictionaryPanelLayout.setHorizontalGroup(
      dictionaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(dictionaryPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(dictionaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(dictionaryPanelLayout.createSequentialGroup()
            .addComponent(dictionaryLabel)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(dictionaryComboBox, 0, 364, Short.MAX_VALUE))
          .addGroup(dictionaryPanelLayout.createSequentialGroup()
            .addComponent(unistallDictionaryButtom)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(installDictionaryButton)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(installDictionaryFromFileButton)))
        .addContainerGap())
    );
    dictionaryPanelLayout.setVerticalGroup(
      dictionaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(dictionaryPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(dictionaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(dictionaryLabel)
          .addComponent(dictionaryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(26, 26, 26)
        .addGroup(dictionaryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(unistallDictionaryButtom)
          .addComponent(installDictionaryButton)
          .addComponent(installDictionaryFromFileButton))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    basicPropertiesPanel.setBorder(BorderFactory.createTitledBorder(SeparatorLineBorder.get(), "Opciones del corrector"));

    resetIgnoreWordsLabel1.setText("<html>Si por otro lado desea reiniciar la lista de palabras que el corrector reemplaza automáticamente pulse el botón de <b>reiniciar reemplazar palabras</b>.</html>");

    ignoreNumbers.setText("Ignorar los números.");
    ignoreNumbers.setActionCommand("ignoreNumers");

    ignoreCase.setText("Ignorar mayúsculas y minúsuculas.");

    resetIgnoreWordsLabel.setText("<html>Si desea reiniciar la lista de palabras que el corrector debe de ignorar como erróneas pulse el botón de <b>reiniciar ignorar palabras</b>.<p></html>");

    resetIgnoreWordsButton.setText("Reiniciar ignorar palabras");

    resetReplaceWordsButton.setText("Reiniciar reemplazar palabras");

    javax.swing.GroupLayout basicPropertiesPanelLayout = new javax.swing.GroupLayout(basicPropertiesPanel);
    basicPropertiesPanel.setLayout(basicPropertiesPanelLayout);
    basicPropertiesPanelLayout.setHorizontalGroup(
      basicPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(basicPropertiesPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(basicPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(basicPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(basicPropertiesPanelLayout.createSequentialGroup()
              .addComponent(resetIgnoreWordsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
              .addContainerGap())
            .addGroup(basicPropertiesPanelLayout.createSequentialGroup()
              .addComponent(ignoreCase, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addGap(286, 286, 286))
            .addGroup(basicPropertiesPanelLayout.createSequentialGroup()
              .addComponent(ignoreNumbers, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)
              .addGap(276, 276, 276))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, basicPropertiesPanelLayout.createSequentialGroup()
              .addComponent(resetIgnoreWordsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addContainerGap())
            .addGroup(basicPropertiesPanelLayout.createSequentialGroup()
              .addComponent(resetIgnoreWordsLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 469, Short.MAX_VALUE)
              .addContainerGap()))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, basicPropertiesPanelLayout.createSequentialGroup()
            .addComponent(resetReplaceWordsButton)
            .addContainerGap())))
    );

    basicPropertiesPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {resetIgnoreWordsButton, resetReplaceWordsButton});

    basicPropertiesPanelLayout.setVerticalGroup(
      basicPropertiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(basicPropertiesPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(ignoreNumbers, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(ignoreCase)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(resetIgnoreWordsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(resetIgnoreWordsButton)
        .addGap(18, 18, 18)
        .addComponent(resetIgnoreWordsLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(resetReplaceWordsButton)
        .addContainerGap())
    );

    closeButton.setText("Cerrar");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
          .addComponent(basicPropertiesPanel, javax.swing.GroupLayout.Alignment.LEADING, 0, 481, Short.MAX_VALUE)
          .addComponent(dictionaryPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addContainerGap(365, Short.MAX_VALUE)
        .addComponent(closeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(dictionaryPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(18, 18, 18)
        .addComponent(basicPropertiesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(closeButton)
        .addContainerGap(36, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel basicPropertiesPanel;
  private javax.swing.JButton closeButton;
  private javax.swing.JComboBox dictionaryComboBox;
  private javax.swing.JLabel dictionaryLabel;
  private javax.swing.JPanel dictionaryPanel;
  private javax.swing.JCheckBox ignoreCase;
  private javax.swing.JCheckBox ignoreNumbers;
  private javax.swing.JButton installDictionaryButton;
  private javax.swing.JButton installDictionaryFromFileButton;
  private javax.swing.JButton resetIgnoreWordsButton;
  private javax.swing.JLabel resetIgnoreWordsLabel;
  private javax.swing.JLabel resetIgnoreWordsLabel1;
  private javax.swing.JButton resetReplaceWordsButton;
  private javax.swing.JButton unistallDictionaryButtom;
  // End of variables declaration//GEN-END:variables
}
