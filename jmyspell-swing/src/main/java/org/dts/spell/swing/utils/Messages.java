/*
 * Messages.java
 *
 * Created on 29 de julio de 2005, 04:08 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.dts.spell.swing.utils;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 *
 * @author personal
 */
public class Messages
{
  private static ResourceBundle boundle = null ;
  
  static
  {
    try
    {
      boundle = ResourceBundle.getBundle("org.dts.spell.swing.messages") ;
    }
    catch (Exception ex)
    {
      boundle = null ;
    }
  }

  private Messages()
  {
  }

  public static String getString(String str)
  {
    if (null != boundle)
      return boundle.getString(str) ;
    else
      return str ;
  }
  
  public static String getString(String str, Object ... objs)
  {
    if (null != boundle)
      return MessageFormat.format(boundle.getString(str), objs) ;
    else
      return str ;
  }
}

