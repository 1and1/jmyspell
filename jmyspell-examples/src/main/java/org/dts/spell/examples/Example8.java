/*
 * Created on 24/02/2005
 *
 */
package org.dts.spell.examples;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.Action;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import javax.swing.text.TextAction;
import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.DictionaryManager;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.examples.utils.CountWordFilter;
import org.dts.spell.examples.utils.MemoryLabel;
import org.dts.spell.examples.utils.PefomanceSpellCheckErrorListener;
import org.dts.spell.examples.utils.StopWatch;
import org.dts.spell.finder.Word;
import org.dts.spell.swing.panels.JRealTimeSpellPanel;
import org.dts.spell.swing.JTextComponentSpellChecker;
import org.dts.spell.swing.actions.ConfigureSpellCheckerAction;
import org.dts.spell.swing.actions.RealTimeSpellCheckAction;
import org.dts.spell.swing.finder.DocumentWordFinder;
import org.dts.spell.swing.utils.ErrorMsgBox;
import org.dts.spell.tokenizer.FilteredTokenizer;

/**
 * @author DreamTangerine
 *
 */
public class Example8 {

  private JTextComponentSpellChecker textSpellChecker;

  public static Icon getIcon(String name) {
    return new ImageIcon(Example8.class.getResource(name));
  }

  public static void main(final String[] args) {
    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        new Example8().init(args);
      }
    });
  }

  static private Action findAction(JTextComponent textArea, String name, String iconName) {
    Action[] actions = textArea.getActions();

    for (int i = 0; i < actions.length; ++i) {
      if (actions[i].getValue(Action.NAME) == name) {
        KeyStroke[] accelerator = textArea.getKeymap().getKeyStrokesForAction(actions[i]);

        if (null != accelerator && accelerator.length > 0) {
          actions[i].putValue(Action.ACCELERATOR_KEY, accelerator[0]);
        }
        Icon icon = getIcon(iconName);

        if (null != icon) {
          actions[i].putValue(Action.SMALL_ICON, icon);
        }
        return actions[i];
      }
    }
    return null;
  }

  class GetWordAction extends TextAction {

    public GetWordAction() {
      super("Get Word");
    }

    public void actionPerformed(ActionEvent e) {
      JTextComponent textArea = getTextComponent(e);

      try {
        DocumentWordFinder wf = new DocumentWordFinder(textArea.getDocument());

        int caretPos = textArea.getCaretPosition();

        Word wordAt = wf.getWordAt(caretPos);
        Word wordPrevious = wf.getPreviousWord(caretPos);
        Word wordNext = wf.getNextWord(caretPos);

        JOptionPane.showMessageDialog(textArea,
                new String[]{
                  "Caret Pos = " + caretPos,
                  "Previous : #" + wordPrevious + "#",
                  "AT : #" + wordAt + "#",
                  "Next : #" + wordNext + "#"
                });
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(textArea, ex.getLocalizedMessage());
      }

      textArea.requestFocusInWindow();
    }
  }

  class CleanTextAction extends TextAction {

    public CleanTextAction() {
      super("Clean");
      putValue(Action.SMALL_ICON, Example8.getIcon("images/stock_new.png"));
    }

    public void actionPerformed(ActionEvent e) {
      JTextComponent textComponent = getTextComponent(e);

      textComponent.setText(null);
    }
  }

  class OpenTextAction extends TextAction {

    public OpenTextAction() {
      super("Open...");
      putValue(Action.SMALL_ICON, Example8.getIcon("images/stock_open.png"));
    }

    public void actionPerformed(ActionEvent e) {
      JTextComponent textArea = getTextComponent(e);
      Window window = SwingUtilities.getWindowAncestor(textArea);

      window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      
      JFileChooser fileChooser = new JFileChooser();
      
      if (fileChooser.showOpenDialog(textArea) == JFileChooser.APPROVE_OPTION) {
        BufferedReader reader = null;

        try {
          reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()));

          textArea.read(reader, fileChooser.getSelectedFile());
        } catch (IOException ex) {
          JOptionPane.showMessageDialog(textArea, ex.getLocalizedMessage());
          ex.printStackTrace();
        } finally {
          try {
            org.dts.spell.dictionary.myspell.Utils.close(reader);
          } catch (IOException e1) {
            e1.printStackTrace();
          }

          System.gc();
          textArea.requestFocusInWindow();
        }
      }
      
      window.setCursor(Cursor.getDefaultCursor());      
    }
  }

  class CheckPerfomanceAction extends TextAction {

    public CheckPerfomanceAction() {
      super("Perfomance");
    }

    public void actionPerformed(ActionEvent e) {
      JTextComponent textArea = getTextComponent(e);

      Window window = SwingUtilities.getWindowAncestor(textArea);

      window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      PefomanceSpellCheckErrorListener pl = new PefomanceSpellCheckErrorListener();
      CountWordFilter filter = new CountWordFilter() ;

      textSpellChecker.getSpellChecker().check(new DocumentWordFinder(textArea.getDocument(), new FilteredTokenizer(filter)), pl);

      long time = pl.getTime();
      int nErrors = pl.getErrorCount();
      long nChars = textArea.getDocument().getLength();
      int nWords = filter.getWordCount() ;

      JOptionPane.showMessageDialog(textArea,
              String.format("Time %s for %d bad words of %d words and %d characters. (%,d characters by second)",
              StopWatch.getFormatedString(time),
              nErrors,
              nWords,
              nChars,
              (long) (0 == time ? 0 : (nChars / (double) time) * 1000)));

      window.setCursor(Cursor.getDefaultCursor());
      textArea.requestFocusInWindow();
    }
  }

  class AutomaticStopExampleAction extends TextAction {

    public AutomaticStopExampleAction() {
      super("Stop example");
    }

    public void actionPerformed(ActionEvent e) {
      JTextComponent textArea = getTextComponent(e);
      JTextArea area = new JTextArea(10, 10);

      area.setWrapStyleWord(true);
      area.setLineWrap(true);
      area.setText("Example of automatic stop!!!. This is anr error.");

      textSpellChecker.startRealtimeMarkErrors(area);

      JOptionPane.showMessageDialog(null, new JScrollPane(area));
      
      if (null != textArea)
        textArea.requestFocusInWindow() ;
    }
  }
  
  class GCAction extends AbstractAction
  {
    public GCAction()
    {
      super("GC") ;
    }

    public void actionPerformed(ActionEvent e) {
      System.gc();
    }
  }
  
  class RealTimePanelAction extends TextAction
  {
    private JRealTimeSpellPanel panel ;
    
    public RealTimePanelAction(JRealTimeSpellPanel panel)
    {
      super("Spell panel") ;
      this.panel = panel ;
      putValue(Action.SELECTED_KEY, panel.isVisible()) ;      
    }

    public void actionPerformed(ActionEvent e) {
      panel.setVisible(!panel.isVisible()) ;
      putValue(Action.SELECTED_KEY, panel.isVisible()) ;
      
      JTextComponent txtCmp = getTextComponent(e) ;
      
      if (null != txtCmp)
        txtCmp.requestFocusInWindow();
    }
  }

  private JComponent setupTextComponent(JTextComponent textComponent, RealTimeSpellCheckAction realTimeAction) {
    JPopupMenu popUpMenu = new JPopupMenu();

    popUpMenu.add(findAction(textComponent, DefaultEditorKit.copyAction, "images/stock_copy.png"));
    popUpMenu.add(findAction(textComponent, DefaultEditorKit.cutAction, "images/stock_cut.png"));
    popUpMenu.add(findAction(textComponent, DefaultEditorKit.pasteAction, "images/stock_paste.png"));
    popUpMenu.addSeparator();
    popUpMenu.add(new JCheckBoxMenuItem(realTimeAction));

    textComponent.setComponentPopupMenu(popUpMenu);
    textComponent.setToolTipText("This is a tooltip");
    
    if (textComponent instanceof JTextArea)
    {
      JTextArea textArea = (JTextArea) textComponent ;
      
      textArea.setWrapStyleWord(true) ;
      textArea.setLineWrap(true) ;
    }
    
    if (textComponent instanceof JTextField)
      return textComponent ;
    else
      return new JScrollPane(textComponent) ;
  }
  
  private JPanel createTextAreas(RealTimeSpellCheckAction realTimeAction) {
    JPanel result = new JPanel(new GridBagLayout()) ;
    GridBagConstraints c = new GridBagConstraints();

    c.insets = new Insets(2, 2, 2, 2) ;
    c.gridx = 0 ;
    c.gridy = 0 ;
    c.gridwidth = 1 ;
    c.gridheight = 1 ;
    c.weightx = 1 ;
    c.fill = GridBagConstraints.HORIZONTAL ;
    result.add(setupTextComponent(new JTextField("Text field example", 5), realTimeAction), c) ;

    c.gridx = 1 ;
    c.fill = GridBagConstraints.HORIZONTAL ;
    result.add(setupTextComponent(new JTextField("Other text field example", 5), realTimeAction), c) ;

    c.gridx = 0 ;
    c.gridy = 1 ;
    c.gridwidth = 2 ;
    c.weighty = 1 ;    
    c.fill = GridBagConstraints.BOTH ;
    JTextArea textArea = new JTextArea("<html><body><b>Synchronized</b> editors</body></html>", 10, 2) ;
    result.add(setupTextComponent(textArea, realTimeAction), c) ;
    
    textArea = new JTextArea(textArea.getDocument(), null, 10, 2) ;

    c.gridx = 0 ;
    c.gridy = 2 ;
    result.add(setupTextComponent(textArea, realTimeAction), c) ;
    
    textArea.requestFocusInWindow();
    
    return result ;
  }

  private SpellDictionary getInitialDictionary(String[] args) throws IOException, URISyntaxException {
      DictionaryManager dictManager = DictionaryManager.get() ;
      //String dir = args[0] ;

      Locale locale ;
      
      if (args.length > 1)
        locale = new Locale(args[1], args[2]) ;
      else
        locale = Locale.getDefault() ;

      return dictManager.getDictionary(locale) ;
  }

  private void init(final String[] args) {
    try {

      //UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel") ;

      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) ;

      // Allow paint while resize :D
      Toolkit.getDefaultToolkit().setDynamicLayout(true);
 
      // Don't delete background
      System.setProperty("sun.awt.noerasebackground", "true");

      SpellDictionary dict = getInitialDictionary(args) ;
      SpellChecker checker = new SpellChecker(dict);
      textSpellChecker = new JTextComponentSpellChecker(checker);

      JFrame frame = new JFrame("Check Speller II");
      RealTimeSpellCheckAction realTimeAction = textSpellChecker.getRealTimeSpellCheckAction() ;
      JRealTimeSpellPanel realTimePanel = new JRealTimeSpellPanel(textSpellChecker) ;
      JPanel buttonPanel = new JPanel();

      // Load after creation of JRealTimeSpellPanel to allow show load progress
      if (null != dict)
        dict.load() ;

      buttonPanel.add(new JButton(new OpenTextAction()));
      buttonPanel.add(new JButton(new CleanTextAction()));
      buttonPanel.add(new JButton(textSpellChecker.getSpellCheckAction()));
      buttonPanel.add(new JToggleButton(realTimeAction));
      buttonPanel.add(new JButton(new AutomaticStopExampleAction()));
      buttonPanel.add(new JToggleButton(new RealTimePanelAction(realTimePanel))) ;
      buttonPanel.add(new JButton(new ConfigureSpellCheckerAction(textSpellChecker))) ;

      frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      frame.add(realTimePanel, BorderLayout.EAST) ;
      frame.add(buttonPanel, BorderLayout.NORTH);
      frame.add(createTextAreas(realTimeAction), BorderLayout.CENTER);

      JPanel infoPanel = new JPanel();

      infoPanel.add(new JButton(new GCAction()));
      infoPanel.add(new MemoryLabel());

      infoPanel.add(new JButton(new GetWordAction()));
      infoPanel.add(new JButton(new CheckPerfomanceAction()));

      frame.add(infoPanel, BorderLayout.SOUTH);

      frame.pack();
      frame.setVisible(true);

    } catch (Exception ex) {
      ex.printStackTrace();
      ErrorMsgBox.show(ex);
    }
  }
}