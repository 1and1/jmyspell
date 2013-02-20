/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dts.spell.swing.panels;

import java.awt.Dimension;
import java.awt.LayoutManager;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.dts.spell.ErrorInfo;
import org.dts.spell.swing.JTextComponentSpellChecker;

/**
 *
 * @author personal
 */
public abstract class SolutionPanel extends JPanel {

  public SolutionPanel(String name) {
    setName(name);
  }
  
  public SolutionPanel(boolean isDoubleBuffered, String name) {
    super(isDoubleBuffered) ;
    setName(name);
  }

  public SolutionPanel(LayoutManager layout, boolean isDoubleBuffered, String name) {
    super(layout, isDoubleBuffered) ;
    setName(name);
  }

  public SolutionPanel(LayoutManager layout, String name) {
    super(layout) ;
    setName(name);
  }
  
  private ErrorInfo errorInfo ;
  
  public abstract String getTitle() ;
  
  // This method is called from NO EDT thread.
  public abstract void calcSolution(ErrorInfo errorInfo) ;
  
  public abstract void activePanel(ErrorInfo errorInfo) ;

  public abstract boolean isForError(ErrorInfo errorInfo) ;

  public void initFrom(JTextComponentSpellChecker txtCmpSpellChecker)
  {

  }

  protected JLabel createNoWidthLabel(String txt)
  {
    if (txt.isEmpty())
      txt = "X" ;
    
    JLabel label = new JLabel(txt) ;
    label.setPreferredSize(new Dimension(0, label.getPreferredSize().height)) ;
    
    return label ;
  }
  
  protected void recalcSize(JLabel label, String txt)
  {
    label.setPreferredSize(null) ;
    label.setText(txt) ;
    label.setPreferredSize(new Dimension(0, label.getPreferredSize().height)) ;
    
    label.invalidate() ;
    doLayout() ;
  }
  
  boolean tryActivatePanel(ErrorInfo errorInfo)
  {
    boolean result = errorInfo == this.errorInfo ;
    
    if (result)
      activePanel(errorInfo) ;
    
    return result ;
  }
  
  void doCalcSolution(ErrorInfo errorInfo)
  {
    this.errorInfo = errorInfo ;
    calcSolution(errorInfo) ;
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    
    for (int i = 0 ; i < getComponentCount() ; ++i)
      getComponent(i).setEnabled(enabled) ;
  }
}
