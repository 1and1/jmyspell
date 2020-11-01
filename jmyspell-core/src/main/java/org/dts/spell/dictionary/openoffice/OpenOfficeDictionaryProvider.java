/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dts.spell.dictionary.openoffice;

import java.net.URISyntaxException;
import org.dts.spell.dictionary.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Pattern;
import org.dts.spell.event.ProgressListener;

/**
 *
 * @author dreamtangerine
 */
public class OpenOfficeDictionaryProvider extends AbstractDictionaryProvider {

  public static final String REMOTE_SC_DICTIONARY_LIST_LOCATION = "http://ftp.services.openoffice.org/pub/OpenOffice.org/contrib/dictionaries/";  // NOI18N
  public static final String FILES_PATTERN_STRING = "([a-z]{1,8})_(([A-Z]{1,8})?)(_1-3-2)?(\\.zip)";

  /** Pattern for detecting remote dictionary file archives */
  public static final Pattern DICTIONARY_ZIP = Pattern.compile("\"" + FILES_PATTERN_STRING + "\"");
  public static final Pattern FILES_DICTIONARY_ZIP = Pattern.compile(FILES_PATTERN_STRING);

  public OpenOfficeDictionaryProvider() {
  }

  /** El patrón para obtener los diccionarios remotos.
   * Normalmente se conectará a un servido remoto, vía http u otro mecanismo, que nos dará una lista
   * de archivos ya sea en formato HTML, XML o cualquier otro.
   * Para extraer los nombres de los ficheros de esa lista se usará este patrón.
   * @return The pattern to extract files from the remote system.
   */
  protected Pattern getDictionariesSourceFilesPattern() {
    return DICTIONARY_ZIP;
  }

  /**
   * Nos da el patrón para obetener los ficheros de los diccionarios instalados localmente.
   * @return The pattern to extract installed dictionaries
   */
  protected Pattern getInstalledDictionariesFilesPattern() {
    return FILES_DICTIONARY_ZIP;
  }

  protected URI getDefaultDictionariesSource() throws URISyntaxException {
    return new URI(REMOTE_SC_DICTIONARY_LIST_LOCATION);
  }

  protected File getFileForLocale(Locale locale) {
    String lang = locale.getLanguage();
    String contry = locale.getCountry();
    String fileName = String.format("%s_%s.zip", lang, contry);
    File dictDir = getLocalStorageDir();

    return new File(dictDir, fileName);
  }

  public SpellDictionary getDictionary(Locale locale) throws IOException {
    URL dictURL = getFileForLocale(locale).toURI().toURL();
    OpenOfficeSpellDictionary result = new OpenOfficeSpellDictionary(locale);

    result.setDictionarySource(dictURL);

    return result;
  }

  public void install(Locale locale, ProgressListener listener) throws Exception {
    File installFile = getFileForLocale(locale) ;
    String fileName = installFile.getName() ;
    URL dictURL = getDictionariesSource().resolve(fileName).toURL();
    
    createLocalCopy(dictURL, listener) ;
  }

  public void install(File file, ProgressListener listener) throws Exception {
    Locale locale = getLocaleForFile(file);

    if (null != locale) {
      createLocalCopy(file.toURI().toURL(), listener);
    } else {
      // TODO : Try unzip file and guess locale.
      throw new IOException("El formato de archivo no es correcto");
    }
  }

  public void uninstall(Locale locale, ProgressListener listener) {
    File dirFile = getFileForLocale(locale) ;

    if (dirFile.exists())
      dirFile.delete();
  }

  public String getDisplayName() {
    return "Diccionarios OppenOffice.org java 100%" ;
  }
}
