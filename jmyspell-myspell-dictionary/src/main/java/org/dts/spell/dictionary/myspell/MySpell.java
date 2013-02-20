/*
 * Created on 31/12/2004
 *
 */
package org.dts.spell.dictionary.myspell ;

import org.dts.spell.event.ProgressListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.dts.spell.dictionary.myspell.wordmaps.WordMap;
import org.dts.spell.event.ProgressEvent;
import org.dts.spell.utils.FileUtils;

/**
 * @author DreamTangerine
 *  
 */
public class MySpell
{
  public static final int NOCAP = 0 ;

  public static final int INITCAP = 1 ;

  public static final int ALLCAP = 2 ;

  public static final int HUHCAP = 3 ;

  private static final String UNKNOW_DICTIONARY_NAME = "unknow" ;
  
  private AffixMgr pAMgr ;

  private WordMap pHMgr ;

  private SuggestMgr pSMgr ;

  private String encoding ;

  private int maxSug ;

  private ProgressListener listener ;

  public MySpell(String zipFile, ProgressListener listener) throws IOException
  {
   this(new ZipFile(zipFile), listener) ;
  }
   
  
  public MySpell(ZipFile zipFile, ProgressListener listener) throws IOException
  {
    Enumeration<? extends ZipEntry> entries = zipFile.entries() ;
    InputStream affStream = null ;
    InputStream dStream = null ;
    long lastModificationTime = 0 ;
    String dicName = UNKNOW_DICTIONARY_NAME ;

    this.listener = listener ;

    while (entries.hasMoreElements() && (null == affStream || null == dStream))
    {
      ZipEntry entry = entries.nextElement() ;
      
      if (entry.getName().endsWith(".aff"))
        affStream = zipFile.getInputStream(entry) ;
      else if (entry.getName().endsWith(".dic"))
      {
        dStream = zipFile.getInputStream(entry) ;
        lastModificationTime = entry.getTime() ;
        dicName = FileUtils.extractOnlyName(entry.getName()) ;
      }
    }
      
    initFromStreams(affStream, dStream, lastModificationTime, dicName) ;
    
    affStream.close() ;
    dStream.close() ;
  }

  public MySpell(InputStream zipStream, ProgressListener listener) throws IOException
  {
    this(new ZipInputStream(zipStream), listener) ;
  }

  /**
   * Read the dictionary from a ZipInputStream.
   * Use with care because the format and dependencies make a memory Stream.  
   * @param zipStream
   * @throws IOException
   */
  public MySpell(ZipInputStream zipStream, ProgressListener listener) throws IOException
  {
    ZipEntry entry = zipStream.getNextEntry() ;

    InputStream affStream = null ;
    InputStream dStream = null ;
    long lastModificationTime = 0 ;
    String dicName = UNKNOW_DICTIONARY_NAME ;

    this.listener = listener ;

    // Optimize and only one Stream in memory.
    // In fact, It is better that the first Entry was Affix, because normally is
    // smaller than dictionary.
    while (null != entry)
    {
      if (entry.getName().endsWith(".aff"))
      {
        if (null == dStream)
          affStream = createFromZipEntry(zipStream, entry) ;
        else
        {
          affStream = zipStream ;
          break ;
        }
      }
      else if (entry.getName().endsWith(".dic"))
      {
        dicName = FileUtils.extractOnlyName(entry.getName()) ;
        lastModificationTime = entry.getTime() ;
  
        if (null == affStream)
          dStream = createFromZipEntry(zipStream, entry) ;
        else
        {
          dStream = zipStream ;
          break ;
        }
      }
      
      entry = zipStream.getNextEntry() ;      
    }
      
    initFromStreams(affStream, dStream, lastModificationTime, dicName) ;
    
    // No need close.
  }
  
  public MySpell(InputStream affStream, InputStream dStream, ProgressListener listener) throws IOException
  {
    this.listener = listener ;
    initFromStreams(affStream, dStream, 0, UNKNOW_DICTIONARY_NAME) ;
  }

  public MySpell(String affpath, String dpath, ProgressListener listener) throws IOException
  {
    InputStream affStream = null ;
    InputStream dStream = null ;    

    this.listener = listener ;

    try
    {
      affStream = new FileInputStream(affpath) ; 
      dStream = new FileInputStream(dpath) ;      
      
      File dictionaryFile = new File(dpath) ;
      
      initFromStreams(affStream, dStream, dictionaryFile.lastModified(), FileUtils.extractOnlyName(dictionaryFile)) ;
    }
    finally
    {
      Utils.close(affStream) ;
      Utils.close(dStream) ;      
    }
  }

  private InputStream createFromZipEntry(ZipInputStream zipStream, ZipEntry entry) throws IOException
  {
    int size = (int) entry.getSize() ; // we expect no file longer than int
    byte[] data = new byte[size] ; 
    int cReaded = zipStream.read(data) ;
    int current = cReaded ;
    
    while (cReaded > 0)
    {
      cReaded = zipStream.read(data, current, size - current) ;
      current += cReaded ; 
    }
    
    return new ByteArrayInputStream(data) ;
  }
  
  private void initFromStreams(InputStream affStream, InputStream dStream, long lastModificationTime, String wordMapName)  throws IOException
  {
    encoding = AffixMgr.readEncoding(affStream) ;

    /* first set up the hash manager */
    pHMgr = load_tables(dStream, lastModificationTime, wordMapName) ;

    /* next set up the affix manager */
    /* it needs access to the hash manager lookup methods */
    pAMgr = new AffixMgr(affStream, encoding, pHMgr, listener) ;

    /* get the preferred try string and the dictionary */
    /* encoding from the Affix Manager for that dictionary */
    String try_string = pAMgr.get_try_string() ;
    //encoding = pAMgr.get_encoding();

    /* and finally set up the suggestion manager */
    maxSug = 15 ;
    pSMgr = new SuggestMgr(try_string, maxSug, pAMgr) ;
  }

  public List<String> suggest(String word)
  {
    return suggest(word, maxSug) ;
  }
    
  public List<String> suggest(String word, int nMax)
  {
    String wspace ;

    if (pSMgr == null)
      return Collections.emptyList() ;

    int[] captype = new int[1] ;
    boolean[] abbv = new boolean[1] ;
    String cw = cleanword(word, captype, abbv) ;
    int wl = cw.length() ;

    if (wl == 0)
      return Collections.emptyList() ;

    //int ns = 0 ;
    List<String> wlst = new LinkedList<String>() ;

    switch (captype[0])
    {
      case NOCAP:
      {
        wlst = pSMgr.suggest(wlst, cw, nMax) ;
        break ;
      }

      case INITCAP:
      {
        wspace = cw.toLowerCase() ;

        pSMgr.suggest(wlst, wspace, nMax) ;

        ListIterator<String> it = wlst.listIterator() ;

        while (it.hasNext())
          it.set(Utils.mkInitCap(it.next())) ;

        pSMgr.suggest(wlst, cw, nMax) ;
        break ;
      }

      case HUHCAP:
      {
        pSMgr.suggest(wlst, cw, nMax) ;
        wspace = cw.toLowerCase() ;
        pSMgr.suggest(wlst, wspace, nMax) ;
        break ;
      }

      case ALLCAP:
      {
        wspace = cw.toLowerCase() ;
        pSMgr.suggest(wlst, wspace, nMax) ;

        ListIterator<String> it = wlst.listIterator() ;

        while (it.hasNext())
          it.set((it.next()).toUpperCase()) ;

        pSMgr.suggest(wlst, cw, nMax) ;
        break ;
      }
    }

    if (!wlst.isEmpty())
      return wlst ;

    // try ngram approach since found nothing
    pSMgr.ngsuggest(wlst, cw, pHMgr, nMax) ;

    if (!wlst.isEmpty())
    {
      switch (captype[0])
      {
        case NOCAP:
          break ;

        case HUHCAP:
          break ;

        case INITCAP:
        {
          ListIterator<String> it = wlst.listIterator() ;

          while (it.hasNext())
            it.set(Utils.mkInitCap(it.next())) ;
        }
          break ;

        case ALLCAP:
        {
          ListIterator<String> it = wlst.listIterator() ;

          while (it.hasNext())
            it.set((it.next()).toUpperCase()) ;
        }
          break ;
      }
    }

    return wlst ;
  }

  public boolean spell(String word)
  {
    String rv = null ;

    String cw ;
    String wspace ;

    //int wl = word.length();
    //if (wl > (MAXWORDLEN - 1)) return 0;
    int[] captype = new int[1] ;
    boolean[] abbv = new boolean[1] ;

    cw = cleanword(word, captype, abbv) ;
    int wl = cw.length() ;

    if (wl == 0)
      return true ;

    switch (captype[0])
    {
      case HUHCAP:
      case NOCAP:
      {
        rv = check(cw) ;
        if ((abbv[0]) && (rv == null))
        {
          cw += '.' ;
          rv = check(cw) ;
        }
        break ;
      }

      case ALLCAP:
      {
        wspace = cw.toLowerCase() ;
        rv = check(wspace) ;

        if (rv == null)
        {
          rv = check(Utils.mkInitCap(wspace)) ;
        }
        if (rv == null)
          rv = check(cw) ;

        if (abbv[0] && (rv == null))
        {
          wspace = cw ;
          wspace += '.' ;
          rv = check(wspace) ;
        }
        break ;
      }
      case INITCAP:
      {
        wspace = cw.toLowerCase() ;
        rv = check(wspace) ;
        if (rv == null)
          rv = check(cw) ;

        if (abbv[0] && (rv == null))
        {
          wspace = cw ;
          wspace += '.' ;
          rv = check(wspace) ;
        }
        break ;
      }
    }

    return rv != null ;
  }

  public String get_dic_encoding()
  {
    return encoding ;
  }

  private WordMap load_tables(InputStream tStream, long lastModifiactionTime, String wordMapName) throws IOException
  {
    //StopWatch w = new StopWatch() ;
      
    WordMap result = WordMap.create() ;
    
    result.init(lastModifiactionTime, wordMapName, encoding) ;
    
    if (result.needEntries())
    {
        // raw dictionary - munched file
        BufferedReader rawdict = null ;

        try
        {
          rawdict = new BufferedReader(new InputStreamReader(tStream, encoding)) ;

          // first read the first line of file to get hash table size
          String ts = rawdict.readLine() ;

          if (ts == null)
            throw new IOException(Utils.getString("ERROR_HASH_MANAGER_2")) ;

          int tablesize = Integer.parseInt(ts) ;

          if (tablesize == 0)
            throw new IOException(Utils.getString("ERROR_HASH_MANAGER_4")) ;

          // allocate the hash table
          result.beginAddEntries(tablesize) ;

          int current = 0 ;
          listener.nextStep(new ProgressEvent(this, "Cargando palabras...", current, tablesize)) ;

          // loop through all words on much list and add to hash
          // table and create word and affix strings
          while ((ts = rawdict.readLine()) != null) {
            result.add(new HEntry(ts.trim())) ;
            ++current ;
            listener.nextStep(new ProgressEvent(this, "Cargando palabras...", current, tablesize)) ;
          }

          result.endAddEntries(tablesize) ;
        }
        finally
        {
          Utils.close(rawdict) ;
        }
    }

    return result ;
  }

  private String cleanword(String src, int[] pcaptype, boolean[] pabbrev)
  {
    int p = 0 ;
    int q = 0 ;

    // first skip over any leading special characters
    while ((q < src.length()) && !Character.isLetterOrDigit(src.charAt(q)))
      q++ ;

    // now strip off any trailing special characters
    // if a period comes after a normal char record its presence
    pabbrev[0] = false ;

    int nl = src.substring(q).length() ;

    while ((nl > 0) && !Character.isLetterOrDigit(src.charAt(q + nl - 1)))
      nl-- ;

    if ((q + nl) < src.length() && src.charAt(q + nl) == '.')
      pabbrev[0] = true ;

    // if no characters are left it can't be an abbreviation and can't be
    // capitalized
    if (nl <= 0)
    {
      pcaptype[0] = NOCAP ;
      pabbrev[0] = false ;

      return "" ;
    }

    // now determine the capitalization type of the first nl letters
    int ncap = 0 ;
    int nneutral = 0 ;
    int nc = 0 ;

    p = q ;

    while (nl > 0)
    {
      nc++ ;
      char c = src.charAt(q) ;

      if (Character.isUpperCase(c))
        ncap++ ;

      if (!Character.isUpperCase(c) && !Character.isLowerCase(c))
        nneutral++ ;

      q++ ;
      nl-- ;
    }

    // now finally set the captype
    if (ncap == 0)
      pcaptype[0] = NOCAP ;
    else if ((ncap == 1) && Character.isUpperCase(src.charAt(p)))
      pcaptype[0] = INITCAP ;
    else if ((ncap == nc) || ((ncap + nneutral) == nc))
      pcaptype[0] = ALLCAP ;
    else
      pcaptype[0] = HUHCAP ;

    return src.substring(p, q) ;
  }

  private String check(String word)
  {
    HEntry he = null ;

    if (pHMgr != null)
      he = pHMgr.get(word) ;

    if ((he == null) && (pAMgr != null))
    {
      // try stripping off affixes */
      he = pAMgr.affix_check(word) ;

      // try check compound word
      if ((he == null) && (pAMgr.get_compound() != null))
        he = pAMgr.compound_check(word, pAMgr.get_compound().charAt(0)) ;
    }

    if (he != null)
      return he.word ;

    return null ;
  }
  
  /**
   * This function add a new word to the current WordManager, but this word is not
   * add permanet, that is, is not save in file.
   * 
   * @param word The word to add.
   */
  public void addCustomWord(String word) throws IOException
  {
    pHMgr.addCustomWord(word) ;
  }
}
