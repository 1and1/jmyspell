package org.dts.spell.jedit;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.AbstractOptionPane;
import org.gjt.sp.jedit.gui.*;
import org.gjt.sp.jedit.syntax.*;
import org.gjt.sp.jedit.jEdit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class ModesOptionPane extends AbstractOptionPane 
{
  private JCheckBox tokenMode ;
  private JComboBox modesComboBox ;
  private JCheckBoxList tokensList ;
  private Map<Mode, boolean[]> modesTokens ;  

  /**
   * Creates a new ModesOptionPane object.
   */
  public ModesOptionPane() 
  {
    super("jmyspell-modes") ;
  }

  protected void _init() {
    
    createTokensModesTable() ;
    
    modesComboBox = createModesComboBox() ;
    addComponent(
        jEdit.getProperty("options.jmyspell-modes.modes.label"), modesComboBox) ;
    
    tokensList = createTokensTableScroller() ;
    addComponent(
        jEdit.getProperty("options.jmyspell-modes.tokens.label"), 
        new JScrollPane(tokensList),
        GridBagConstraints.VERTICAL) ;
    
    tokenMode = createEnableModeCheckBox() ;
    addComponent(tokenMode) ;
    
    // Set the current mode 
    View view = jEdit.getActiveView() ;
    
    if (view != null)
      modesComboBox.setSelectedItem(view.getBuffer().getMode()) ;
  }

  private void createTokensModesTable()
  {
    modesTokens = new HashMap<Mode, boolean[]>() ;
    
    loadModes(jEdit.getModes()) ;
  }
  
  private JComboBox createModesComboBox()
  {
    Mode[] mode = jEdit.getModes() ;
    
    JComboBox result = new JComboBox(mode) ;
    
    result.addItemListener(
			new ItemListener()
			{
				public void itemStateChanged(ItemEvent evt)
				{
				  Mode mode = (Mode) evt.getItem() ;
				  
				  if (evt.getStateChange() == ItemEvent.SELECTED)
				    putMode(mode) ;
				  else
				    quitMode(mode) ;
				}
			}
    ) ;
    
    return result ;
  }
  
  private class TokenRenderer extends DefaultTableCellRenderer
  {
    public TokenRenderer()
    {
      setOpaque(true) ;
      Font font = jEdit.getFontProperty("view.font") ;
      
      styles = GUIUtilities.loadStyles(font.getFamily(), font.getSize()) ;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column)
    {
      Color background ;
      Color foreground ;
      Font font ;
      
      if (null != styles[row])
      {
        font = styles[row].getFont() ;
        foreground = styles[row].getForegroundColor() ;
        
        if (styles[row].getBackgroundColor() == null)
          background = jEdit.getColorProperty("view.bgColor") ;
        else
          background = styles[row].getBackgroundColor() ;
      }
      else
      {
        font = jEdit.getFontProperty("view.font") ;
        foreground = jEdit.getColorProperty("view.fgColor") ;
        background = jEdit.getColorProperty("view.bgColor") ;        
      }

      super.getTableCellRendererComponent(
          table, 
          value, 
          isSelected, 
          hasFocus, 
          row, 
          column) ;
      
      if (!isSelected)
      {
        setBackground(background) ;
        setForeground(foreground) ;
      }

      setFont(font) ;
      
      return this ;
    }
    
    private SyntaxStyle[] styles ;
  }
  
  private JCheckBoxList createTokensTableScroller()
  {
    String[] tokensChoices = new String[Token.ID_COUNT] ;
    
    for (int i = 0 ; i < Token.ID_COUNT ; i++)
      tokensChoices[i] = getVisibleTokenName(i) ;
    
    return new JCheckBoxList(tokensChoices) ; 
  }

  private JCheckBox createEnableModeCheckBox()
  {
    JCheckBox result = new JCheckBox(
        jEdit.getProperty("options.jmyspell-modes.disable-mode.label"));
    
    result.addItemListener(
        new ItemListener()
        {
          public void itemStateChanged(ItemEvent evt)
          {
            tokensList.setEnabled(evt.getStateChange() == ItemEvent.SELECTED) ;
            modesComboBox.setEnabled(evt.getStateChange() == ItemEvent.SELECTED) ;
          }
        }) ;
    
    result.setSelected(JMySpellChecker.isTokenMode()) ;    

    tokensList.setEnabled(result.isSelected()) ;
    modesComboBox.setEnabled(result.isSelected()) ;
    
    return result ;
  }
  
  private void quitMode(Mode mode) {
    JCheckBoxList.Entry[] values = tokensList.getValues() ;
    boolean[] tokens = modesTokens.get(mode) ;
    
    for(int i = 0 ; i < Token.ID_COUNT ; i++)
      tokens[i] = values[i].isChecked() ;
  }
  
  private String getVisibleTokenName(int id)
  {
    if (Token.NULL == id)
      return jEdit.getProperty("options.jmyspell-modes.default.label") ;
    else
      return Token.tokenToString((byte)id) ;
  }
  
  private void putMode(Mode mode)
  {
    JCheckBoxList.Entry[] values = tokensList.getValues() ;
    boolean[] tokens = modesTokens.get(mode) ;
    
    for (int i = 0 ; i < Token.ID_COUNT ; i++)
      values[i] = new JCheckBoxList.Entry(tokens[i], getVisibleTokenName(i)) ;
      
    tokensList.setModel(values) ;
    
    // Inside the code, every time you put the model the Render is reset
    tokensList.getColumnModel().getColumn(1).setCellRenderer(new TokenRenderer()) ;
  }
  
  public static String getTokenModeName(Mode mode, int token)
  {
    return "options.jmyspell." + mode.getName() + "." + Token.tokenToString((byte) token)  ;
  }

  public static String getTokenModeName(Mode mode, Token token)
  {
    return getTokenModeName(mode, token.id)  ;
  }
  
  
  public static boolean hasToBeParsed(Mode mode, Token token)
  {
    return jEdit.getBooleanProperty(getTokenModeName(mode, token), false) ;
  }
  
  public static boolean hasToBeParsed(Mode mode, int token)
  {
    return jEdit.getBooleanProperty(getTokenModeName(mode, token), false) ;
  }
  
  protected void saveMode(Mode mode, boolean[] tokens)
  {
    for(int i = 0 ; i < Token.ID_COUNT ; i++)
      jEdit.setBooleanProperty(getTokenModeName(mode, i), tokens[i]) ;
  }
  
  protected void saveModes(Mode[] modes)
  {
    for (int i = 0 ; i < modes.length ; i++)
      saveMode(modes[i], modesTokens.get(modes[i])) ;
  }

  protected boolean[] loadMode(Mode mode)
  {
    boolean[] tokens = new boolean[Token.ID_COUNT] ;

    for (int i = 0 ; i < Token.ID_COUNT ; i++)
      tokens[i] = jEdit.getBooleanProperty(getTokenModeName(mode, i), false) ;
    
    return tokens ; 
  }
  
  protected void loadModes(Mode[] modes)
  {
    for (int i = 0 ; i < modes.length ; i++)
      modesTokens.put(modes[i], loadMode(modes[i])) ;
  }
  
  protected void _save() 
  {
    quitMode((Mode) modesComboBox.getSelectedItem()) ; // To save current mode
    
    saveModes(jEdit.getModes()) ;
    JMySpellChecker.setTokeMode(tokenMode.isSelected()) ;
  }
}