/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dts.spell.dictionary;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;
import org.dts.spell.event.ProgressListener;
import org.dts.spell.event.ProgressListenerSupport;
import org.dts.spell.utils.LRUMap;

/**
 *
 * @author dreamtangerine
 */
public class DictionaryManager {

  private static DictionaryManager instance;
  private ServiceLoader<DictionaryProvider> providers;
  private ProgressListenerSupport progressListeners;
  private LRUMap<Locale, SpellDictionary> cache = new LRUMap<Locale, SpellDictionary>(4);

  private DictionaryManager() {
  }

  private ProgressListenerSupport getProgressListeters() {
    if (null == progressListeners) {
      progressListeners = new ProgressListenerSupport();
    }

    return progressListeners;
  }

  public DictionaryProvider[] getDictionaryProviders() {
    List<DictionaryProvider> dpList = new ArrayList<DictionaryProvider>() ;
    
    for (DictionaryProvider dp : getProviders())
      dpList.add(dp);

    return dpList.toArray(new DictionaryProvider[dpList.size()]) ;
  }

  private ServiceLoader<DictionaryProvider> getProviders() {
    if (null == providers) {
      providers = ServiceLoader.load(DictionaryProvider.class);
    }

    return providers;
  }

  public static DictionaryManager get() {
    if (null == instance) {
      instance = new DictionaryManager();
    }

    return instance;
  }

  public void reloadProviders() {
    getProviders().reload();
  }

  public static class LocaleProvider {

    private Locale locale;
    private DictionaryProvider provider;

    LocaleProvider(Locale locale, DictionaryProvider provider) {
      this.locale = locale;
      this.provider = provider;
    }

    /**
     * @return the locale
     */
    public Locale getLocale() {
      return locale;
    }

    /**
     * @return the provider
     */
    public DictionaryProvider getProvider() {
      return provider;
    }
  }

  public List<LocaleProvider> getInstalledLocales() {
    List<LocaleProvider> result = new ArrayList<LocaleProvider>();

    for (DictionaryProvider dp : getProviders()) {
      for (Locale locale : dp.getInstalledLocales()) {
        result.add(new LocaleProvider(locale, dp));
      }
    }

    return result;
  }

  // Get Availables locales, those not installed
  public List<LocaleProvider> getAvailableLocales() {
    List<LocaleProvider> result = new ArrayList<LocaleProvider>();

    for (DictionaryProvider dp : getProviders()) {
      for (Locale locale : dp.getAvailableLocales()) {
        result.add(new LocaleProvider(locale, dp));
      }
    }

    return result;
  }

  public void install(LocaleProvider localeProvider) throws Exception {
    localeProvider.getProvider().install(localeProvider.getLocale(), getProgressListeters()) ;
  }

  public void install(File file, DictionaryProvider provider) throws Exception {
    provider.install(file, getProgressListeters()) ;
  }

  public void unistall(LocaleProvider localeProvider) throws Exception {
    localeProvider.getProvider().uninstall(localeProvider.getLocale(), getProgressListeters()) ;
  }

  public boolean hasInstalledLocales() {

    for (DictionaryProvider dp : getProviders()) {
      if (!dp.getInstalledLocales().isEmpty()) {
        return true;
      }
    }

    return false;
  }

  /**
   * Get the first dictionary provider that was load with class T.
   * 
   * @param <T>
   * @param providerClass
   * @return
   */
  public <T> T getDictionaryProvider(Class<T> providerClass) {

    for (DictionaryProvider dp : getProviders()) {
      if (providerClass.isInstance(dp)) {
        return providerClass.cast(dp);
      }
    }

    return null;
  }

  public DictionaryProvider getDictionaryProvider(Locale locale) {

    for (DictionaryProvider dp : getProviders()) {
      if (dp.isForLocale(locale)) {
        return dp;
      }
    }

    return null;
  }

  /**
   * Get the first dictionary provider that was load with class T and for <code>locale</code>.
   *
   * @param <T>
   * @param providerClass
   * @return
   */
  public <T> T getDictionaryProvider(Class<T> providerClass, Locale locale) {

    for (DictionaryProvider dp : getProviders()) {
      if (providerClass.isInstance(dp) && dp.isForLocale(locale)) {
        return providerClass.cast(dp);
      }
    }

    return null;
  }

  /**
   * TODO : Set cache parameters.
   * @param locale
   * @return
   * @throws IOException
   */
  public SpellDictionary getDictionary(Locale locale) throws IOException {
    SpellDictionary dictionary = cache.get(locale);

    if (null == dictionary) {
      DictionaryProvider dp = getDictionaryProvider(locale);

      if (null != dp) {
        dictionary = dp.getDictionary(locale);

        if (null != dictionary) {
          dictionary.addProgressListener(getProgressListeters());
          cache.put(locale, dictionary);
        }
      }
    }

    return dictionary;
  }

  public void clearCache() {
    cache.clear();
  }

  public void removeFromCache(Locale locale) {
    cache.remove(locale);
  }

  public void removeFromCache(SpellDictionary dictionary) {
    cache.remove(dictionary.getLocale());
  }

  /**
   * Get dictionary for current locale
   * @return
   */
  public SpellDictionary getDictionary() throws IOException {
    return getDictionary(Locale.getDefault());
  }

  public void addProgressListener(ProgressListener listener) {
    getProgressListeters().addListener(listener);
  }

  public void removeProgressListener(ProgressListener listener) {
    getProgressListeters().removeListener(listener);
  }
}
