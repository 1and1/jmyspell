/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dts.spell.swing.panels;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import org.dts.spell.ErrorInfo;

/**
 *
 * @author personal
 */
public abstract class SimpleTextSolutionPanel extends SolutionPanel {

  private JLabel text ;
  private String title ;
  
  public SimpleTextSolutionPanel(String text, String title, String name)
  {
    super(new BorderLayout(), name) ;
    this.text = createNoWidthLabel(text) ;
    this.title = title ;

    this.text.setVerticalAlignment(JLabel.TOP); 
    add(this.text, BorderLayout.CENTER) ;
  }
  
  @Override
  public String getTitle() {
    return title ;
  }

  @Override
  public void calcSolution(ErrorInfo errorInfo) {
    // nothing to do.
  }

  @Override
  public void activePanel(ErrorInfo errorInfo) {
    // nothing to do.
  }
}
