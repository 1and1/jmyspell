/*
 * Created on 04/07/2005
 *
 */
package org.dts.spell.swing.utils;

import java.awt.Component;

import javax.swing.JOptionPane ;
import java.awt.Frame ;

/**
 * @author DreamTangerine
 *
 */
public class ErrorMsgBox
{
  private ErrorMsgBox()
  {
    
  }
  
  public static void show(Throwable th)
  {
    show(th.getLocalizedMessage()) ;    
  }
  
  public static void show(Component parent, Throwable th)
  {
    show(parent, th.getLocalizedMessage()) ;
  }

  
  public static void show(Component parent, String txt)
  {
    JOptionPane.showMessageDialog(parent, txt, Messages.getString("ErrorMsgBox.ERROR_TITLE_STRING"), JOptionPane.ERROR_MESSAGE) ;
  }
  
  public static void show(String txt)
  {
    show(getCurrentFrame(), txt) ;
  }

  public static int yesNoCancelMsg(Component parent, String title, String txt)
  {
    return JOptionPane.showConfirmDialog(
        parent, 
        txt, 
        title,
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.ERROR_MESSAGE) ;
  }
  
  public static int yesNoCancelMsg(String title, String txt)
  {
    return yesNoCancelMsg(getCurrentFrame(), title, txt) ;
  }
  
  public static int yesNoCancelMsg(Component parent, String txt)
  {
    return yesNoCancelMsg(parent, Messages.getString("ErrorMsgBox.ERROR_TITLE_STRING"), txt) ;
  }
  
  public static int yesNoCancelMsg(String txt)
  {
    return yesNoCancelMsg(getCurrentFrame(), txt) ;
  }
  
  private static Frame getCurrentFrame()
  {
    Frame[] frames = Frame.getFrames() ;
    
    if (null == frames || frames.length == 0)
      return null ;
    else
      return frames[frames.length - 1] ;
  }
}