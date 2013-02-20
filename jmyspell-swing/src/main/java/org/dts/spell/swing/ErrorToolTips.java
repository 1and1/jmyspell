/*
 * ErrorToolTips.java
 *
 * Created on 18 de diciembre de 2006, 04:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.dts.spell.swing;

import org.dts.spell.swing.ErrorMarker;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.dts.spell.ErrorInfo;

/**
 *
 * @author DreamTangerine
 */
public class ErrorToolTips
{
  private static class TooltipInfo implements PropertyChangeListener
  {
    public TooltipInfo(ErrorMarker marker, JTextComponent textComponent)
    {
      errorMarker = marker ;
      originalText = textComponent.getToolTipText() ;
      textComponent.addPropertyChangeListener(JComponent.TOOL_TIP_TEXT_KEY, this) ;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
      String newTooltip = (String) evt.getNewValue() ;
      
      // Avoid put original text like an error.
      if (newTooltip != originalText)
        originalText = newTooltip ;
    }
    
    public ErrorMarker errorMarker ;
    public String originalText ;
  }
  
  private class MouseListener extends MouseMotionAdapter
  {
    @Override
    public void mouseMoved(MouseEvent e)
    {
      JTextComponent textCmp = (JTextComponent) e.getSource() ;
      int pos = textCmp.viewToModel(e.getPoint()) ;
      TooltipInfo tooltipInfo = currentEditors.get(textCmp) ;
      ErrorMarker marker = tooltipInfo.errorMarker ;
      String originalText = tooltipInfo.originalText ;
      
      if (marker.hasError(pos))
      {
        String errorText = getErrorMessage(marker, pos) ;
        
        // Avoid change original text like an error.        
        tooltipInfo.originalText = errorText ;
        textCmp.setToolTipText(errorText) ;
      }
      else
        textCmp.setToolTipText(originalText) ;
      
      // Restore original text.
      tooltipInfo.originalText = originalText ;
    }
  } ;
  
  private MouseMotionListener motionMouseListener = new MouseListener() ;
  
  private Hashtable<JTextComponent, TooltipInfo> currentEditors = new Hashtable<JTextComponent, TooltipInfo>() ;
  
  /** Creates a new instance of ErrorToolTips */
  public ErrorToolTips()
  {
  }
  
  public void addErrorMarker(ErrorMarker errorMarker)
  {
    JTextComponent textComponent = errorMarker.getTextComponent() ;
    currentEditors.put(textComponent, new TooltipInfo(errorMarker, textComponent)) ;
    
    textComponent.addMouseMotionListener(motionMouseListener) ;
  }
  
  public void removeErrorMarker(ErrorMarker errorMarker)
  {
    JTextComponent textComponent = errorMarker.getTextComponent() ;
    TooltipInfo info = currentEditors.get(textComponent) ;
    
    currentEditors.remove(textComponent) ;
    textComponent.removeMouseMotionListener(motionMouseListener) ;
    textComponent.removePropertyChangeListener(info) ;
    textComponent.setToolTipText(info.originalText) ;
  }

  /**
   * This is the function to override to personalize the error message text.
   */
  public String getErrorMessage(ErrorMarker marker, int position)
  {
    ErrorInfo info = marker.getErrorInfo(position) ;
    
    if (null != info)
      return info.getDescription() ;
    else
      return null ;
  }
}