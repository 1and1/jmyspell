/*
 * MemoryLabel.java
 *
 * Created on 11 de enero de 2007, 12:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.dts.spell.examples.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 *
 * @author DreamTangerine
 */
public class MemoryLabel extends JLabel implements Runnable
{
  private boolean wasDestroyed = false ;
  
  /** Creates a new instance of MemoryLabel */
  public MemoryLabel()
  {
    addPropertyChangeListener("ancestor", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        wasDestroyed = !isDisplayable() ;
      }
    }) ;
    
    Thread thread = new Thread(this) ;

    thread.setName("MemoryThread") ;
    thread.setDaemon(true) ;
    thread.setPriority(Thread.MIN_PRIORITY) ;
    thread.start() ;
  }
  
  public void run()
  {
    while (!wasDestroyed)
    {
      SwingUtilities.invokeLater(
        new Runnable()
        {
          public void run()
          { updateText() ; }
        }) ;
      
      try
      { // each second.
        Thread.sleep(1000) ;
      }
      catch (InterruptedException ex)
      {
        ex.printStackTrace();
      }
    }
  }

  private String getFormattedMemory(long memory) {
      String symbol = "bytes" ;

      if (memory > 1024)
      {
          memory = memory / 1024 ;
          symbol = "KBs" ;
      }

      if (memory > 1024)
      {
          memory = memory / 1024 ;
          symbol = "MBs" ;
      }

      if (memory > 1024)
      {
          memory = memory / 1024 ;
          symbol = "GBs" ;
      }

      return String.format("<b>%,d</b> %s", memory, symbol) ;
  }

  private void updateText()
  {
    Runtime runtime = Runtime.getRuntime() ;
    setText(String.format("<html>Free : %s. Total : %s. Max : %s. Used : %s.</html>",
      getFormattedMemory(runtime.freeMemory()),
      getFormattedMemory(runtime.totalMemory()), 
      getFormattedMemory(runtime.maxMemory()),
      getFormattedMemory((runtime.totalMemory() - runtime.freeMemory())))) ;
  }
}
