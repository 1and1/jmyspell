/*
 * Created on 27/12/2004
 *
 */
package org.dts.spell.dictionary.openoffice;

import java.util.Locale;
import org.dts.spell.dictionary.*;
import org.dts.spell.event.ProgressListenerSupport;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipFile;

import org.dts.spell.dictionary.myspell.MySpell;
import org.dts.spell.dictionary.myspell.Utils;
import org.dts.spell.event.ProgressEvent;
import org.dts.spell.event.ProgressListener;
import org.dts.spell.utils.FileUtils;

/**
 * @author DreamTangerine
 *
 */
public class OpenOfficeSpellDictionary implements SpellDictionary {

  private File personalDict;
  private MySpell mySpell;
  private Future<Object> loader = null;
  private ProgressListenerSupport listeners = new ProgressListenerSupport();
  private Locale locale;
  private URL sourceURL;

  // Constructor that don't load the dictionary, to allow add listeners. Use load to begin the load of dictionary.
  public OpenOfficeSpellDictionary() {
  }

  public OpenOfficeSpellDictionary(Locale locale) {
    this.locale = locale;
  }

  //Emilio Gustavo Ormeño
  public OpenOfficeSpellDictionary(InputStream affIS, InputStream dicIS) throws IOException {
    this(affIS, dicIS, true);
  }

  public OpenOfficeSpellDictionary(InputStream affIS, InputStream dicIS, boolean inBackground) throws IOException {
    this(affIS, dicIS, getPersonalWordsFile(), true);
  }

  public OpenOfficeSpellDictionary(InputStream affIS, InputStream dicIS, File personalDict, boolean inBackground) throws IOException {
    load(affIS, dicIS, personalDict, inBackground);
  }

  public OpenOfficeSpellDictionary(ZipFile zipFile) throws IOException {
    this(zipFile, true);
  }

  public OpenOfficeSpellDictionary(final ZipFile zipFile, boolean inBackground) throws IOException {
    load(zipFile, inBackground);
  }

  public OpenOfficeSpellDictionary(InputStream zipStream, File personalDict) throws IOException {
    this(zipStream, personalDict, true);
  }

  // NOTE : If inBackground is true when the dict is loaded the zipStream is closed.
  public OpenOfficeSpellDictionary(final InputStream zipStream, final File personalDict, boolean inBackground) throws IOException {
    load(zipStream, personalDict, inBackground);
  }

  public OpenOfficeSpellDictionary(File file) throws IOException {
    this(file, true);
  }

  public OpenOfficeSpellDictionary(File file, boolean inBackground) throws IOException {
    load(file, inBackground);
  }

  public OpenOfficeSpellDictionary(File dictFile, File affFile) throws IOException {
    this(dictFile, affFile, true);
  }

  public OpenOfficeSpellDictionary(File dictFile, File affFile, boolean inBackground) throws IOException {
    load(dictFile, affFile, inBackground);
  }

  private void initFromFiles(final File dictFile, final File affFile, boolean inBackground) throws IOException {

    if (inBackground) {
      final ExecutorService executor = Executors.newSingleThreadExecutor();

      loader = executor.submit(
              new Callable<Object>() {

                public Object call() throws Exception {
                  initFromFiles(dictFile, affFile);
                  executor.shutdown(); // we no need more the executor
                  return null;
                }
              });
    } else {
      initFromFiles(dictFile, affFile);

    }
  }

  private synchronized void beginLoad() {
    listeners.beginProgress(new ProgressEvent(this, "Cargando diccionario", 0, 2));
  }

  private synchronized void endLoad(IOException ex) throws IOException {
    listeners.endProgress(new ProgressEvent(this, ex));
    throw ex;
  }

  private synchronized void endLoad(MySpell dict) {
    mySpell = dict;
    listeners.endProgress(new ProgressEvent(this, "Carga realizada con éxito", 2, 2));
  }

  private void initFromFiles(File dictFile, File affFile) throws IOException {
    try {
      beginLoad();

      personalDict = getPersonalWordsFile(getLocale());
      MySpell dict = new MySpell(affFile.getPath(), dictFile.getPath(), listeners);

      readPersonalWords(personalDict, dict);

      endLoad(dict);
    } catch (IOException ex) {
      endLoad(ex);
    }
  }

  private void initFromZipFile(ZipFile zipFile) throws IOException {
    try {
      beginLoad();

      personalDict = getPersonalWordsFile(getLocale());
      MySpell dict = new MySpell(zipFile, listeners);

      readPersonalWords(personalDict, dict);

      endLoad(dict);
    } catch (IOException ex) {
      endLoad(ex);
    }
  }

  private void initFromStream(InputStream zipStream, File personalDict) throws IOException {
    try {
      beginLoad();

      this.personalDict = personalDict;
      MySpell dict = new MySpell(zipStream, listeners);

      readPersonalWords(personalDict, dict);

      endLoad(dict);
    } catch (IOException ex) {
      endLoad(ex);
    }
  }

  private void initFromStreams(InputStream dicIS, InputStream affIS, File personalDict) throws IOException {
    try {
      beginLoad();

      this.personalDict = personalDict;
      MySpell dict = new MySpell(dicIS, affIS, listeners);

      readPersonalWords(personalDict, dict);

      endLoad(dict);
    } catch (IOException ex) {
      endLoad(ex);
    }

  }

  public void addWord(String word) throws SpellDictionaryException {
    waitToLoad();

    PrintWriter pw = null;

    word = word.trim();

    try {
      pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(personalDict, true), mySpell.get_dic_encoding()));

      mySpell.addCustomWord(word);

      pw.println(word);
    } catch (Exception ex) {
      throw new SpellDictionaryException(ex);
    } finally {
      try {
        Utils.close(pw);
      } catch (IOException e) {
        throw new SpellDictionaryException(e);
      }
    }
  }

  public boolean isCorrect(String word) {
    waitToLoad();

    return mySpell.spell(word);
  }

  public List<String> getSuggestions(String word) {
    waitToLoad();

    return mySpell.suggest(word);
  }

  public List<String> getSuggestions(String word, int nMax) {
    waitToLoad();

    return mySpell.suggest(word, nMax);
  }

  public synchronized boolean isLoad() {
    return null != mySpell;
  }

  private void waitToLoad() {

    try {
      if (null != loader && !loader.isDone()) {
        loader.get(); // If were and excpetion this method rethrow it
        loader = null;
      }

      if (!isLoad()) {
        throw new IllegalStateException();
      }

    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static File getPersonalWordsFile(Locale locale) {

    if (null != locale) {
      return new File(FileUtils.getJMySpellDir(), String.format("%s_%s.per", locale.getLanguage(), locale.getCountry()));
    } else {
      return getPersonalWordsFile() ;
  
    }
  }

  private static File getPersonalWordsFile() {
    return new File(FileUtils.getJMySpellDir(), "dictionary.per");
  }

  private void readPersonalWords(File personalFile, MySpell mySpell) throws IOException {
    BufferedReader rd = null;
    FileInputStream fr = null;

    try {
      if (null != personalFile && personalFile.exists() && !personalFile.isDirectory()) {
        fr = new FileInputStream(personalFile);

        rd = new BufferedReader(new InputStreamReader(fr, mySpell.get_dic_encoding()));

        int size = (int) personalFile.length();
        int current = size - fr.available();

        listeners.nextStep(new ProgressEvent(this, "Cargando diccionario personal", current, size));

        String line = rd.readLine();

        while (line != null) {
          current = size - fr.available();
          listeners.nextStep(new ProgressEvent(this, "Cargando diccionario personal", current, size));

          mySpell.addCustomWord(line.trim());
          line = rd.readLine();
        }
      }
    } finally {
      Utils.close(rd);
    }
  }

  public void load(InputStream affIS, InputStream dicIS, boolean inBackground) throws IOException {
    load(affIS, dicIS, getPersonalWordsFile(getLocale()), true);
  }

  public void load(final InputStream affIS, final InputStream dicIS, final File personalDict, boolean inBackground) throws IOException {
    if (inBackground) {
      final ExecutorService executor = Executors.newSingleThreadExecutor();

      loader = executor.submit(
              new Callable<Object>() {

                public Object call() throws Exception {
                  initFromStreams(affIS, dicIS, personalDict);
                  dicIS.close();
                  affIS.close();
                  executor.shutdown(); // we no need more the executor
                  return null;
                }
              });
    } else {
      initFromStreams(affIS, dicIS, personalDict);

    }
  }

  public void load(final ZipFile zipFile, boolean inBackground) throws IOException {
    if (inBackground) {
      final ExecutorService executor = Executors.newSingleThreadExecutor();

      loader = executor.submit(
              new Callable<Object>() {

                public Object call() throws Exception {
                  initFromZipFile(zipFile);
                  executor.shutdown(); // we no need more the executor
                  return null;
                }
              });
    } else {
      initFromZipFile(zipFile);

    }
  }

  public void load(final InputStream zipStream, final File personalDict, boolean inBackground) throws IOException {
    if (inBackground) {
      final ExecutorService executor = Executors.newSingleThreadExecutor();

      loader = executor.submit(
              new Callable<Object>() {

                public Object call() throws Exception {
                  initFromStream(zipStream, personalDict);
                  zipStream.close();
                  executor.shutdown(); // we no need more the executor
                  return null;
                }
              });
    } else {
      initFromStream(zipStream, personalDict);

    }
  }

  public void load(File dictFile, File affFile, boolean inBackground) throws IOException {
    initFromFiles(dictFile, affFile, inBackground);
  }

  public void load(File file, boolean inBackground) throws IOException {
    if (file.getName().endsWith(".zip")) {
      load(new ZipFile(file), inBackground);

    } else {
      load(new File(FileUtils.extractRootFile(file) + ".dic"), new File(FileUtils.extractRootFile(file) + ".aff"), inBackground);
    }
  }

  public void setDictionarySource(URL url) {
    sourceURL = url;
  }

  public void load() throws IOException {
    if (null == sourceURL) {
      throw new IllegalStateException("No dictionary source set");
    }

    load(sourceURL.openStream(), getPersonalWordsFile(getLocale()), true);
  }

  public void addProgressListener(ProgressListener listener) {
    if (null != loader) {
      throw new IllegalStateException("This dictionary load was started");
    }

    listeners.addListener(listener);
  }

  public void removeProgressListener(ProgressListener listener) {
    listeners.removeListener(listener);
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public Locale getLocale() {
    return locale;
  }
}
