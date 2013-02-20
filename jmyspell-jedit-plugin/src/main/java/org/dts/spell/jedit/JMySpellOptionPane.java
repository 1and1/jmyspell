package org.dts.spell.jedit ;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.jEdit;


public class JMySpellOptionPane extends AbstractOptionPane
{
  private PatchChooserPanel dictDir ;
  private JCheckBox automaticLoad ;
  private JCheckBox resetDict ;
  
  private JCheckBox ignoreUpperCase ;
  private JCheckBox ignoreCase ;  
  private JCheckBox ignoreNumbers ;  

  /**
   * Creates a new JazzyOptionPane object.
   */
  public JMySpellOptionPane()
  {
    super("jmyspell-properties") ;
  }

  private void addDictionaryPanel()
  {
    addSeparator("options.jmyspell-properties.dictionary.title.label") ;

    dictDir = new PatchChooserPanel(
        JMySpellChecker.getDictionaryFile(),
        jEdit.getProperty("options.jmyspell-properties.dictionary.browse")) ;
    
    addComponent(
        jEdit.getProperty("options.jmyspell-properties.dictionary.label"), 
        dictDir, 
        GridBagConstraints.HORIZONTAL) ;
  }
  
  private void addOptionPanel()
  {
    addSeparator("options.jmyspell-properties.options.title.label") ;

    automaticLoad = new JCheckBox(
     jEdit.getProperty("options.jmyspell-properties.options.auto-load.label")) ;    

    automaticLoad.setSelected(JMySpellChecker.isAutomaticLoadDictionary()) ;
    addComponent(automaticLoad) ;

    resetDict = new JCheckBox(
        jEdit.getProperty("options.jmyspell-properties.options.auto-reset.label")) ;
    
    resetDict.setSelected(JMySpellChecker.isResetSpellChecker()) ;
    addComponent(resetDict) ;
  }

  private void addSpellOptionPanel()
  {
    addSeparator("options.jmyspell-properties.spell-options.title.label") ;

    ignoreUpperCase = new JCheckBox(
        jEdit.getProperty("options.jmyspell-properties.spell-options.ignore-uppercase.label")) ;
    ignoreUpperCase.setSelected(JMySpellChecker.isIgnoreUpperCase()) ;
    addComponent(ignoreUpperCase) ;

    ignoreCase = new JCheckBox(
        jEdit.getProperty("options.jmyspell-properties.spell-options.ignore-case.label")) ;
    ignoreCase.setSelected(JMySpellChecker.isIgnoreCase()) ;
    addComponent(ignoreCase) ;
    
    ignoreNumbers = new JCheckBox(
        jEdit.getProperty("options.jmyspell-properties.spell-options.ignore-numbers.label")) ;
    ignoreNumbers.setSelected(JMySpellChecker.isIgnoreDigits()) ;
    addComponent(ignoreNumbers) ;
  }
  
  protected void _init()
  {
    addDictionaryPanel() ;
    addOptionPanel() ;
    addSpellOptionPanel() ;
  }

  protected void _save()
  {
    File oldFile = JMySpellChecker.getDictionaryFile() ;
    boolean isNewFile = !oldFile.equals(new File(dictDir.getFile())) ;
    boolean oldLoad = JMySpellChecker.isAutomaticLoadDictionary() ;
    boolean load = automaticLoad.isSelected() ; 
    
    JMySpellChecker.setDictionaryFile(dictDir.getFile()) ;
    JMySpellChecker.setAutomaticLoadDictionary(load) ;
    JMySpellChecker.setResetSpellChecker(resetDict.isSelected()) ;

    JMySpellChecker.setIgnoreUpperCase(ignoreUpperCase.isSelected()) ;    
    JMySpellChecker.setIgnoreCase(ignoreCase.isSelected()) ;
    JMySpellChecker.setIgnoreDigits(ignoreNumbers.isSelected()) ;
    

    if (load)
    {
      if (!oldLoad || isNewFile)
        JMySpellPlugin.reloadDictionary() ;
    }
  }
}

class PatchChooserPanel extends JPanel
{
  public PatchChooserPanel(
      File initFile,
      String buttonText)
  {
    super(new BorderLayout(2, 2)) ;
    
    textField = new JTextField(initFile.getPath()) ;
    add(textField, BorderLayout.CENTER) ;

    add(new JButton(new AbstractAction(buttonText)
    {
      public void actionPerformed(ActionEvent evt)
      {
        openFileDialog() ;
      }
    }), BorderLayout.EAST) ;
  }

  public String getFile()
  {
    return textField.getText() ;
  }

  private JFileChooser getBrowser()
  {
    String fileName = getFile() ;
    String path = null ;
    String position = null ;

    if (!fileName.endsWith("zip"))
      fileName = fileName + ".dic" ;

    File file = new File(fileName) ;
    
    if (file.exists())
    {
      if (file.isDirectory())
        path = file.getPath() ; 
      else
      {
        path = file.getParent() ;
        position = file.getName() ;
      }
    }
    
    JFileChooser result = new JFileChooser(path) ;
    
    result.setFileFilter(
        new FileFilter()
        {
          @Override
          public boolean accept(File f)
          {
            String fileName = f.getName() ;
            
            return f.isDirectory() || 
                    fileName.endsWith(".zip") ||
                    fileName.endsWith(".dic") ||
                    fileName.endsWith(".aff") ;
          }

          @Override
          public String getDescription()
          {
            return jEdit.getProperty("jmyspell.filter.description") ;
          }
        }
    ) ;

    result.setMultiSelectionEnabled(false) ;
    
    if (null != position)
      result.setSelectedFile(new File(position)) ; 
    
    return result ;
  }
  
  private void openFileDialog()
  {
    JFileChooser browser = getBrowser() ;
    
    browser.showOpenDialog(this) ;
    
    File file = browser.getSelectedFile() ;
    
    if (null != file)
    {
      String filePath = file.getPath() ;
      
      if (filePath.endsWith("zip"))
        textField.setText(filePath) ;
      else
      {
        int index = filePath.lastIndexOf('.') ;
        
        textField.setText(filePath.substring(0, index)) ;
      }
    }
  }

  private JTextField textField ;
}
