/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dts.spell.dictionary;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.dts.spell.event.ProgressListener;

/**
 *
 * @author dreamtangerine
 */
public interface DictionaryProvider {

  /**
   * A name for the UI.
   * @return Name for the user
   */
  public String getDisplayName() ;

  /**
   *
   * @param locale locale to check for
   * @return return true if this provider have installed a dictionary for that locale.
   */
  public boolean isForLocale(Locale locale) ;

  /**
   *
   * @return the current list of installed dictionaries
   */
  public List<Locale> getInstalledLocales() ;

  /**
   * Get a new dictionary for the <code>locale</code>. Dictionary must be installed.
   *
   * @param locale the locale of the dictionary.
   * @return
   */
  public SpellDictionary getDictionary(Locale locale) throws IOException ;

  /**
   * This function get a list for all locales on internet, ready for installation
   * @return list of lacale for installation
   */
  public List<Locale> getAvailableLocales() ;


  /**
   * Install a local copy of the interntet dictionary for that locale
   * @param locale the local of the dictionary to install
   * @param listener the listener for progress tracking
   */
  public void install(Locale locale, ProgressListener listener) throws Exception ;

  /**
   * Install a local copy form the dictionary
   * @param file the file of the dictionary
   * @param listener the listener for progress tracking
   */
  public void install(File file, ProgressListener listener) throws Exception ;

  /**
   * Uninstall the given locale 
   * @param locale the locale to uninstall
   * @param listener the listener for progress tracking
   */
  public void uninstall(Locale locale, ProgressListener listener) throws Exception ;
}
