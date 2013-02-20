/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dts.spell.dictionary;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dts.spell.event.ProgressListener;
import org.dts.spell.utils.FileUtils;

/**
 *
 * @author dreamtangerine
 */
public abstract class AbstractDictionaryProvider implements DictionaryProvider {

  // Where to look for new dictionaries.
  private URI sourceURI;

  public AbstractDictionaryProvider() {
    try {
      sourceURI = getDefaultDictionariesSource();
    } catch (URISyntaxException ex) {
      Logger.getLogger(AbstractDictionaryProvider.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  private FileFilter fileFilter;

  /** El patrón para obtener los diccionarios remotos.
   * Normalmente se conectará a un servido remoto, vía http u otro mecanismo, que nos dará una lista
   * de archivos ya sea en formato HTML, XML o cualquier otro.
   * Para extraer los nombres de los ficheros de esa lista se usará este patrón.
   * @return The pattern to extract files from the remote system.
   */
  protected abstract Pattern getDictionariesSourceFilesPattern();

  /**
   * Nos da el patrón para obetener los ficheros de los diccionarios instalados localmente.
   * @return The pattern to extract installed dictionaries
   */
  protected abstract Pattern getInstalledDictionariesFilesPattern();

  public boolean isForLocale(Locale locale) {
    return getInstalledLocales().contains(locale);
  }

  protected FileFilter createInstalledDictionaryFileFilter() {
    return new FileFilter() {

      public boolean accept(File pathname) {
        Matcher matcher = getInstalledDictionariesFilesPattern().matcher(pathname.getName());

        return matcher.find();
      }
    };
  }

  private FileFilter getInstalledDictionaryFileFilter() {
    if (null == fileFilter) {
      fileFilter = createInstalledDictionaryFileFilter();
    }

    return fileFilter;
  }

  protected Locale getLocaleForFile(File file) {
    String name = file.getName();
    Matcher matcher = getInstalledDictionariesFilesPattern().matcher(name);

    if (matcher.find()) {
      return createLocaleFrom(matcher);
    } else {
      return null;
    }
  }

  protected Locale createLocaleFrom(Matcher matcher) {
    String language = matcher.group(1);
    String country = matcher.group(2);

    return new Locale(language, country);
  }

  protected File getLocalStorageDir() {
    File dictDir = FileUtils.getDictionariesDir();

    return new File(dictDir, getClass().getName());
  }

  protected void addLocaleFromInstalledFile(List<Locale> list, File file) {
    Locale locale = getLocaleForFile(file);

    if (null != locale) {
      list.add(locale);
    }
  }

  protected void addFromInstallDirectory(List<Locale> list, File dictionariesDirectory) {
    if (dictionariesDirectory.isDirectory() && dictionariesDirectory.exists()) {
      File[] files = dictionariesDirectory.listFiles(getInstalledDictionaryFileFilter());

      for (File file : files) {
        addLocaleFromInstalledFile(list, file);
      }
    }
  }

  /**
   * Obtain the default source of dictionaries
   * @return the default source for new dictionaries to install.
   * @throws URISyntaxException
   */
  protected abstract URI getDefaultDictionariesSource() throws URISyntaxException;

  /**
   * Set the remote URI for dictionaries sources. That is the URI from where dictionaries are download for installation.
   * @param uri the URI where are the dictionaries ready for install.
   */
  public void setDictionariesSource(URI uri) {
    this.sourceURI = uri;
  }

  /**
   * Get the URI where dictionaries are ready for install.
   * @return
   */
  public URI getDictionariesSource() {
    return sourceURI;
  }

  protected String readAsString(InputStream in, String encoding) throws UnsupportedEncodingException, IOException {
    Reader reader = new InputStreamReader(in, encoding);
    char[] chars = new char[1024];
    int nCount = reader.read(chars);
    StringWriter writer = new StringWriter();

    while (nCount > 0) {
      writer.write(chars, 0, nCount);
      nCount = reader.read(chars);
    }

    return writer.toString();
  }

  protected String getEncoding(URLConnection connection) {
    String encoding = connection.getContentEncoding();

    if (null == encoding) {
      encoding = connection.getHeaderField("Content-Type");

      if (null != encoding) {
        Matcher matcher = Pattern.compile("(charset=)(.*)").matcher(encoding);

        if (matcher.find()) {
          encoding = matcher.group(2);
        }
      }
    }

    return encoding;
  }

  protected void addLocaleFromSource(List<Locale> list, Matcher matcher) {
    list.add(createLocaleFrom(matcher));
  }

  protected void addFromSources(List<Locale> list) {
    try {
      URL url = getDictionariesSource().toURL();
      URLConnection connection = url.openConnection();

      connection.connect();

      String encoding = getEncoding(connection);
      String html = readAsString(connection.getInputStream(), encoding);

      Matcher matcher = getDictionariesSourceFilesPattern().matcher(html);

      while (matcher.find()) {
        addLocaleFromSource(list, matcher);
      }
    } catch (Exception ex) {
      Logger.getLogger(AbstractDictionaryProvider.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /** 
   * Copy URL to <code>getLocalStorageDir()</code>.
   */
  protected URL createLocalCopy(URL dictURL, ProgressListener listener) throws IOException {
    InputStream in = dictURL.openStream();

    try {
      File dictDir = getLocalStorageDir();
      String file = FileUtils.extractNameAndExtension(dictURL.getFile());
      File result = new File(dictDir, file);

      FileUtils.copyStreamToFile(result, in, listener);
    } finally {
      in.close();
    }

    return dictURL;
  }

  public List<Locale> getInstalledLocales() {
    List<Locale> result = new LinkedList<Locale>();
    addFromInstallDirectory(result, getLocalStorageDir());

    return result;
  }

  /** 
   * Get all Availables locales that are not installed
   */
  public List<Locale> getAvailableLocales() {
    List<Locale> installed = getInstalledLocales();
    List<Locale> result = new LinkedList<Locale>();

    addFromSources(result);
    result.removeAll(installed);

    return result;
  }
}
