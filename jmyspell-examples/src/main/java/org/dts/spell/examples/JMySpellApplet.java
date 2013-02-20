/*
 * JMySpellApplet.java
 *
 * Created on 16 de agosto de 2005, 04:14 PM
 *
 */

package org.dts.spell.examples;

import java.awt.BorderLayout;
// NOTE : Use if access to clipboard
//import java.awt.Toolkit;
//import java.awt.datatransfer.Clipboard;
//import java.awt.datatransfer.DataFlavor;
//import java.awt.datatransfer.FlavorEvent;
//import java.awt.datatransfer.FlavorListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.undo.UndoManager;

import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.openoffice.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.swing.JTextComponentSpellChecker;
import org.dts.spell.swing.utils.ErrorMsgBox;

/**
 *
 * @author personal
 */
public class JMySpellApplet extends javax.swing.JApplet
{
  private JTextComponentSpellChecker textSpellChecker = null ;
  private JTextArea textArea = null ;
  private UndoManager undoManager = null ;
  private SpellChecker checker = null ;
  
  private Action newAction ;
  private Action checkAction ;
  private Action realTimeCheckAction ;
  private Action copyAction ;
  private Action cutAction ;
  private Action pasteAction ;
  private Action selectAllAction ;
  private Action undoAction ;
  private Action redoAction ;
  
  /** Creates a new instance of JMySpellApplet */
  public JMySpellApplet()
  {
  }
  
  private Icon getIcon(String name)
  {
    return new ImageIcon(getClass().getResource(name)) ;
  }
  
  private SpellDictionary getDictionary() throws IOException
  {
    return getDictionary(getParameter("dictionary.1")) ;
  }
  
  private SpellDictionary getDictionary(String dict) throws IOException
  {
    URL url = new URL(getCodeBase(), dict) ;

    // Load in background the OpenOfficeSpellDictionary, close the stream.
    return new OpenOfficeSpellDictionary(url.openStream(), (File) null) ;
  }
  
  private JTextArea createTextArea()
  {
    JTextArea result = new JTextArea() ;
    
    result.setWrapStyleWord(true) ;
    result.setLineWrap(true) ;
    
    undoManager = new UndoManager() ;
    result.getDocument().addUndoableEditListener(undoManager) ;
    
    return result ;
  }
  
  private static class PropertyChangeListenerForToggleActions implements PropertyChangeListener
  {
    public static final String SELECTED_STATE = "SELECTED_STATE" ;
    
    private PropertyChangeListener delegateListener ;
    private AbstractButton button ;
    
    public PropertyChangeListenerForToggleActions(AbstractButton b, PropertyChangeListener listener)
    {
      delegateListener = listener ;
      button = b ;
    }
    
    public void propertyChange(PropertyChangeEvent evt)
    {
      delegateListener.propertyChange(evt) ;
      
      if (evt.getPropertyName() == SELECTED_STATE)
        button.setSelected(((Boolean) evt.getNewValue()).booleanValue()) ;
    }
    
    public static void configureFromAction(AbstractButton b, Action action)
    {
      b.setSelected(isSelected(action)) ;
    }
    
    public static boolean isSelected(Action action)
    {
      Boolean selected = (Boolean) action.getValue(SELECTED_STATE) ;
      
      if (null != selected)
        return selected.booleanValue() ;
      else
        return false ;
    }
    
    public static void setSelected(Action action, boolean s)
    {
      if (s)
        action.putValue(SELECTED_STATE, Boolean.TRUE) ;
      else
        action.putValue(SELECTED_STATE, Boolean.FALSE) ;
    }
    
    public void removeListener()
    {
      delegateListener = null ;
      button = null ;
    }
  }
  
  
  private Action createNewAction()
  {
    Action result = new AbstractAction(messages.getString("NEW_ACTION_NAME"), getIcon("images/stock_new.png"))
    {
      public void actionPerformed(ActionEvent e)
      {
        stop() ;
        
        textArea.setText("") ;
        System.gc() ;
        
        start() ;
      }
    } ;
    
    result.putValue(Action.MNEMONIC_KEY, getMNemonic("NEW_ACTION_MNEMONIC")) ;
    result.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(messages.getString("NEW_ACTION_ACCELERATOR"))) ;
    result.putValue(Action.SHORT_DESCRIPTION, messages.getString("NEW_ACTION_TOOLTIP")) ;
    
    return result ;
  }
  
  private Action createCheckAction()
  {
    Action result = new AbstractAction(messages.getString("SPELL_CHECK_ACTION_NAME"), getIcon("images/stock_spellcheck.png"))
    {
      public void actionPerformed(ActionEvent e)
      {
        if (textSpellChecker.spellCheck(textArea))
          JOptionPane.showMessageDialog(textArea, messages.getString("TEXT_IS_OK_MSG")) ;
        
        textArea.requestFocusInWindow() ;
      }
    } ;
    
    result.putValue(Action.MNEMONIC_KEY, getMNemonic("SPELL_CHECK_ACTION_MNEMONIC")) ;
    result.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(messages.getString("SPELL_CHECK_ACTION_ACCELERATOR"))) ;
    result.putValue(Action.SHORT_DESCRIPTION, messages.getString("SPELL_CHECK_ACTION_TOOLTIP")) ;
    
    return result ;
  }
  
  private Action createRealTimeCheckAction()
  {
    Action result = new AbstractAction(messages.getString("REALTIME_CHECK_ACTION_NAME"), getIcon("images/stock_autospellcheck.png"))
    {
      public void actionPerformed(ActionEvent e)
      {
        PropertyChangeListenerForToggleActions.setSelected(this,
                !PropertyChangeListenerForToggleActions.isSelected(this)) ;
        
        if (PropertyChangeListenerForToggleActions.isSelected(this))
          textSpellChecker.startRealtimeMarkErrors(textArea) ;
        else
          textSpellChecker.stopRealtimeMarkErrors() ;
        
        textArea.requestFocusInWindow() ;
      }
    } ;
    
    result.putValue(Action.MNEMONIC_KEY, getMNemonic("REALTIME_CHECK_ACTION_MNEMONIC")) ;
    result.putValue(Action.SHORT_DESCRIPTION, messages.getString("REALTIME_CHECK_ACTION_TOOLTIP")) ;
    
    return result ;
  }
  
  private Action findAction(String name)
  {
    Action[] actions = textArea.getActions() ;
    
    for (int i = 0 ; i < actions.length ; ++i)
      if (actions[i].getValue(Action.NAME) == name)
      {
      KeyStroke[] accelerator = textArea.getKeymap().getKeyStrokesForAction(actions[i]) ;
      
      if (null != accelerator && accelerator.length > 0)
        actions[i].putValue(Action.ACCELERATOR_KEY, accelerator[0]) ;
      
      return actions[i] ;
      }
    
    return null ;
  }
  
  private Action createCopyAction()
  {
    Action result = findAction(DefaultEditorKit.copyAction) ;
    
    result.putValue(Action.SMALL_ICON, getIcon("images/stock_copy.png")) ;
    result.putValue(Action.NAME, messages.getString("COPY_ACTION_NAME")) ;
    result.putValue(Action.MNEMONIC_KEY, getMNemonic("COPY_ACTION_MNEMONIC")) ;
    result.putValue(Action.SHORT_DESCRIPTION, messages.getString("COPY_ACTION_TOOLTIP")) ;
    
    if (result.getValue(Action.ACCELERATOR_KEY) == null)
      result.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(messages.getString("COPY_ACTION_ACCELERATOR"))) ;
    
    return result ;
  }
  
  private Action createCutAction()
  {
    Action result = findAction(DefaultEditorKit.cutAction) ;
    
    result.putValue(Action.SMALL_ICON, getIcon("images/stock_cut.png")) ;
    result.putValue(Action.NAME, messages.getString("CUT_ACTION_NAME")) ;
    result.putValue(Action.MNEMONIC_KEY, getMNemonic("CUT_ACTION_MNEMONIC")) ;
    result.putValue(Action.SHORT_DESCRIPTION, messages.getString("CUT_ACTION_TOOLTIP")) ;
    
    if (result.getValue(Action.ACCELERATOR_KEY) == null)
      result.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(messages.getString("CUT_ACTION_ACCELERATOR"))) ;
    
    return result ;
  }
  
  private Action createPasteAction()
  {
    Action result = findAction(DefaultEditorKit.pasteAction) ;
    
    result.putValue(Action.SMALL_ICON, getIcon("images/stock_paste.png")) ;
    result.putValue(Action.NAME, messages.getString("PASTE_ACTION_NAME")) ;
    result.putValue(Action.MNEMONIC_KEY, getMNemonic("PASTE_ACTION_MNEMONIC")) ;
    result.putValue(Action.SHORT_DESCRIPTION, messages.getString("PASTE_ACTION_TOOLTIP")) ;
    
    if (result.getValue(Action.ACCELERATOR_KEY) == null)
      result.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(messages.getString("PASTE_ACTION_ACCELERATOR"))) ;
    
    return result ;
  }
  
  private Action createSelectAllAction()
  {
    Action result = findAction(DefaultEditorKit.selectAllAction) ;
    
    result.putValue(Action.NAME, messages.getString("SELECT_ALL_ACTION_NAME")) ;
    result.putValue(Action.MNEMONIC_KEY, getMNemonic("SELECT_ALL_ACTION_MNEMONIC")) ;
    result.putValue(Action.SHORT_DESCRIPTION, messages.getString("SELECT_ALL_ACTION_TOOLTIP")) ;
    
    if (result.getValue(Action.ACCELERATOR_KEY) == null)
      result.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(messages.getString("SELECT_ALL_ACTION_ACCELERATOR"))) ;
    
    return result ;
  }
  
  private Action createUndoAction()
  {
    Action result = new AbstractAction()
    {
      public void actionPerformed(ActionEvent evt)
      {
        try
        {
          if (undoManager.canUndo())
            undoManager.undo() ;
        }
        catch(Exception ex)
        {
          ErrorMsgBox.show(JMySpellApplet.this, ex) ;
        }
      }
    } ;
    
    result.putValue(Action.SMALL_ICON, getIcon("images/stock_undo.png")) ;
    result.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(messages.getString("UNDO_ACTION_ACCELERATOR"))) ;
    
    return result ;
  }
  
  private Action createRedoAction()
  {
    Action result = new AbstractAction()
    {
      public void actionPerformed(ActionEvent evt)
      {
        try
        {
          if (undoManager.canRedo())
            undoManager.redo() ;
        }
        catch(Exception ex)
        {
          ErrorMsgBox.show(JMySpellApplet.this, ex) ;
        }
      }
    } ;
    
    result.putValue(Action.SMALL_ICON, getIcon("images/stock_redo.png")) ;
    result.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(messages.getString("REDO_ACTION_ACCELERATOR"))) ;
    
    return result ;
  }
  
  private class SelectionActionUpdater implements CaretListener
  {
    public SelectionActionUpdater()
    {
      updateActions(textArea.getSelectionStart(), textArea.getSelectionEnd()) ;
    }
    
    private void updateActions(int starSelc, int endSelc)
    {
      boolean hasSelection = starSelc != endSelc ;
      boolean isAllSelection = Math.abs(endSelc - starSelc) == textArea.getDocument().getLength() ;
      
      copyAction.setEnabled(hasSelection) ;
      cutAction.setEnabled(hasSelection) ;
      selectAllAction.setEnabled(!isAllSelection) ;
    }
    
    public void caretUpdate(CaretEvent e)
    {
      updateActions(e.getDot(), e.getMark()) ;
    }
  }
  
  private class ContentDependActionUptater implements DocumentListener
  {
    public ContentDependActionUptater()
    {
      update(textArea.getDocument().getLength()) ;
    }
    
    private void updateUndo()
    {
      String name = undoManager.getUndoPresentationName() ;
      
      undoAction.setEnabled(undoManager.canUndo()) ;
      undoAction.putValue(Action.NAME, name) ;
      undoAction.putValue(Action.SHORT_DESCRIPTION, name) ;
      undoAction.putValue(Action.MNEMONIC_KEY, new Integer(name.charAt(0))) ;
    }
    
    private void updateRedo()
    {
      String name = undoManager.getRedoPresentationName() ;
      
      redoAction.setEnabled(undoManager.canRedo()) ;
      redoAction.putValue(Action.NAME, name) ;
      redoAction.putValue(Action.SHORT_DESCRIPTION, name) ;
      redoAction.putValue(Action.MNEMONIC_KEY, new Integer(name.charAt(0))) ;
    }
    
    private void update(int nChars)
    {
      updateUndo() ;
      updateRedo() ;
      
      checkAction.setEnabled(nChars > 0) ;
      newAction.setEnabled(nChars > 0) ;
    }
    
    public void insertUpdate(DocumentEvent e)
    {
      update(e.getDocument().getLength()) ;
    }
    
    public void removeUpdate(DocumentEvent e)
    {
      update(e.getDocument().getLength()) ;
    }
    
    public void changedUpdate(DocumentEvent e)
    {
    }
  }

  // NOTE : Only in real application we can access to Clipboard 
//  private class PasteActionUpdater implements FlavorListener
//  {
//    public PasteActionUpdater()
//    {
//      updatePasteAction(Toolkit.getDefaultToolkit().getSystemClipboard()) ;
//    }
//    
//    private void updatePasteAction(Clipboard clipboard)
//    {
//      pasteAction.setEnabled(
//              DataFlavor.selectBestTextFlavor(clipboard.getAvailableDataFlavors()) != null) ;
//    }
//    
//    public void flavorsChanged(FlavorEvent e)
//    {
//      updatePasteAction((Clipboard) e.getSource()) ;
//    }
//  }
  
  private void createActions()
  {
    newAction = createNewAction() ;
    checkAction = createCheckAction() ;
    realTimeCheckAction = createRealTimeCheckAction() ;
    copyAction = createCopyAction() ;
    cutAction = createCutAction() ;
    pasteAction = createPasteAction() ;
    selectAllAction = createSelectAllAction() ;
    undoAction = createUndoAction() ;
    redoAction = createRedoAction() ;
    
    // Update Undo/Redo/SpellCheck.
    textArea.getDocument().addDocumentListener(new ContentDependActionUptater()) ;
    
    // Update Copy/Cut/SelectAll.
    textArea.addCaretListener(new SelectionActionUpdater()) ;
    
    // Update Paste.
    // NOTE : Only signed Applets can access to clipboard :(.
    //Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener(new PasteActionUpdater()) ;
  }
  
  
  private JPanel createDictionaries()
  {
    JPanel result = new JPanel(new BorderLayout()) ;
    
    result.setOpaque(false) ;
    
    JComboBox combo = new JComboBox() ;
    
    String baseName = "dictionary" ;
    int i = 1 ;
    String dict = getParameter(baseName + "." + i) ;
    
    while (null != dict)
    {
      combo.addItem(dict) ;
      ++i ;
      dict = getParameter(baseName + "." + i) ;
    }
    
    combo.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
          try
          {
            String newDictName = (String) e.getItem() ;
            SpellDictionary newDict = getDictionary(newDictName) ;
            
            stop() ;
            checker.setDictionary(newDict) ;
            start() ;
          }
          catch(IOException ex)
          {
            ErrorMsgBox.show(JMySpellApplet.this, ex) ;
          }
        }
      }
    }) ;
    
    combo.setFocusable(true) ;
    
    result.add(combo, BorderLayout.SOUTH) ;
    JLabel label = new JLabel(messages.getString("DICTIONARY_LABEL")) ;
    
    label.setLabelFor(combo) ;
    label.setDisplayedMnemonic(getMNemonic("DICTIONARY_LABEL_MNEMONIC")) ;
    
    result.add(label, BorderLayout.NORTH) ;
    
    return result ;
  }
  
  private static class JActionToggleButton extends JToggleButton
  {
    public JActionToggleButton()
    {
    }
    
    private PropertyChangeListenerForToggleActions listener ;
    
    public void removeActionListener(ActionListener l)
    {
      if (l == getAction())
        listener.removeListener() ;
      
      super.removeActionListener(l) ;
    }
    
    protected void configurePropertiesFromAction(Action a)
    {
      super.configurePropertiesFromAction(a) ;
      
      PropertyChangeListenerForToggleActions.configureFromAction(this, a) ;
    }
    
    protected PropertyChangeListener createActionPropertyChangeListener(Action a)
    {
      listener = new PropertyChangeListenerForToggleActions(
              this, super.createActionPropertyChangeListener(a)) ;
      
      return listener ;
    }
  }
  
  private AbstractButton createToolBarButton(Action action, boolean toggle)
  {
    AbstractButton result ;
    
    if (toggle)
      result = new JActionToggleButton() ;
    else
      result = new JButton() ;
    
    // Quit text in toolbar buttons
    result.putClientProperty("hideActionText", Boolean.TRUE) ;
    result.setAction(action) ;
    result.setFocusable(false) ;
    
    return result ;
  }
  
  private JComponent createToolBar()
  {
    JToolBar result = new JToolBar(messages.getString("TOOLBAR_TITLE")) ;
    
    result.add(createToolBarButton(newAction, false)) ;
    
    result.addSeparator() ;
    result.add(createToolBarButton(checkAction, false)) ;
    result.add(createToolBarButton(realTimeCheckAction, true)) ;
    
    result.addSeparator() ;
    result.add(createToolBarButton(undoAction, false)) ;
    result.add(createToolBarButton(redoAction, false)) ;
    
    result.addSeparator() ;
    result.add(createToolBarButton(cutAction, false)) ;
    result.add(createToolBarButton(copyAction, false)) ;
    result.add(createToolBarButton(pasteAction, false)) ;
    
    result.addSeparator() ;
    result.add(createDictionaries()) ;
    
    result.setFloatable(false) ;
    result.setFocusable(false) ;
    
    return result ;
  }
  
  private JPopupMenu createContextEditMenu()
  {
    JPopupMenu result = new JPopupMenu(messages.getString("EDIT_MENU")) ;
    
    result.add(new JMenuItem(undoAction)) ;
    result.add(new JMenuItem(redoAction)) ;
    
    result.addSeparator() ;
    result.add(new JMenuItem(cutAction)) ;
    result.add(new JMenuItem(copyAction)) ;
    result.add(new JMenuItem(pasteAction)) ;
    
    result.addSeparator() ;
    result.add(new JMenuItem(selectAllAction)) ;
    
    return result ;
  }
  
  private static class JActionCheckBoxButton extends JCheckBoxMenuItem
  {
    public JActionCheckBoxButton(Action action)
    {
      super(action) ;
    }
    
    private PropertyChangeListenerForToggleActions listener ;
    
    public void removeActionListener(ActionListener l)
    {
      if (l == getAction())
        listener.removeListener() ;
      
      super.removeActionListener(l) ;
    }
    
    protected void configurePropertiesFromAction(Action a)
    {
      super.configurePropertiesFromAction(a) ;
      
      PropertyChangeListenerForToggleActions.configureFromAction(this, a) ;
    }
    
    protected PropertyChangeListener createActionPropertyChangeListener(Action a)
    {
      listener = new PropertyChangeListenerForToggleActions(
              this, super.createActionPropertyChangeListener(a)) ;
      
      return listener ;
    }
  }
  
  private JMenuBar createMenuBar()
  {
    JMenuBar result = new JMenuBar() ;
    
    JMenu fileMenu = new JMenu(messages.getString("FILE_MENU")) ;
    fileMenu.setMnemonic(getMNemonic("FILE_MENU_MNEMONIC")) ;
    fileMenu.add(new JMenuItem(newAction)) ;
    result.add(fileMenu) ;
    
    JMenu editMenu = new JMenu(messages.getString("EDIT_MENU")) ;
    editMenu.setMnemonic(getMNemonic("EDIT_MENU_MNEMONIC")) ;
    
    editMenu.add(new JMenuItem(undoAction)) ;
    editMenu.add(new JMenuItem(redoAction)) ;
    editMenu.addSeparator() ;
    editMenu.add(new JMenuItem(cutAction)) ;
    editMenu.add(new JMenuItem(copyAction)) ;
    editMenu.add(new JMenuItem(pasteAction)) ;
    editMenu.addSeparator() ;
    editMenu.add(new JMenuItem(selectAllAction)) ;
    result.add(editMenu) ;
    
    JMenu spellMenu = new JMenu(messages.getString("SPELL_MENU")) ;
    spellMenu.setMnemonic(getMNemonic("SPELL_MENU_MNEMONIC")) ;
    spellMenu.add(new JMenuItem(checkAction)) ;
    spellMenu.add(new JActionCheckBoxButton(realTimeCheckAction)) ;
    result.add(spellMenu) ;
    
    return result ;
  }
  
  public void init()
  {
    try
    {
      SpellDictionary dict = getDictionary() ;
      
      checker = new SpellChecker(dict) ;
      textSpellChecker = new JTextComponentSpellChecker(checker) ;
      textArea = createTextArea() ;
      
      createActions() ;
      
      // Set the menus
      setJMenuBar(createMenuBar()) ;
      textArea.setComponentPopupMenu(createContextEditMenu()) ;
      
      add(createToolBar(), BorderLayout.NORTH) ;
      add(new JScrollPane(textArea), BorderLayout.CENTER) ;
      
      textArea.requestFocusInWindow() ;
    }
    catch(Exception ex)
    {
      ex.printStackTrace() ;
      ErrorMsgBox.show(ex) ;
    }
  }
  
  @Override
  public void start()
  {
    if (PropertyChangeListenerForToggleActions.isSelected(realTimeCheckAction))
      textSpellChecker.startRealtimeMarkErrors(textArea) ;
    
    textArea.requestFocusInWindow() ;
  }
  
  @Override
  public void stop()
  {
    if (PropertyChangeListenerForToggleActions.isSelected(realTimeCheckAction))
      textSpellChecker.stopRealtimeMarkErrors() ;
  }

  private static Integer getMNemonic(String key)
  {
    String mnemonic = messages.getString(key) ;
    
    if(null != mnemonic)
      return new Integer(mnemonic.charAt(0)) ;
    else
      return null ;
  }
  
  private static final ResourceBundle messages = ResourceBundle.getBundle("org/dts/spell/examples/messages");
}
