/*
 * Created on 27/12/2004
 *
 */
package org.dts.spell.dictionary.myspell;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 *
 * @author DreamTangerine
 *
 */
public final class Utils
{
  private Utils()
  {
  }
  
  ///////////////////////////////////////////////////
  
  public static int XPRODUCT = (1 << 0) ;
  
  public static boolean TestAff(String a, char b , int c)
  {
    for (int i = 0 ; i < c ; ++i)
      if (a.charAt(i) == b)
        return true ;
    
    return false ;
  }
  
  
  public static String myRevStrDup(String s)
  {
    StringBuilder builder = new StringBuilder(s) ;
    
    return builder.reverse().toString() ;
  }
  
  public static boolean isSubset(String s1, String s2)
  {
    return s2.startsWith(s1) ;
  }
  
  public static void close(Reader rd) throws IOException
  {
    if (null != rd)
      rd.close() ;
  }
  
  public static void close(Writer wt) throws IOException
  {
    if (null != wt)
      wt.close() ;
  }
  
  public static void close(InputStream in) throws IOException
  {
    if (null != in)
      in.close() ;
  }

  public static void close(OutputStream out) throws IOException
  {
    if (null != out)
      out.close() ;
  }
  
  public static String mkInitCap(CharSequence word)
  {
    StringBuilder bd = new StringBuilder(word) ;
    
    bd.setCharAt(0, Character.toUpperCase(bd.charAt(0))) ;
    
    return bd.toString() ;
  }
  
  public interface IndexComparator
  {
    public boolean isLess(int index1, int index2) ;
    public boolean isGreater(int index1, int index2) ;
  }
  
  private static void swap(int[] numbers, int index1, int index2)
  {
    int temp = numbers[index1];
    
    numbers[index1] = numbers[index2];
    numbers[index2] = temp;
  }
  
  private static void siftDown(int[] numbers, int root, int bottom, IndexComparator comparator)
  {
    boolean done = false ;
    int maxChild ;
    
    while ((root * 2 <= bottom) && (!done))
    {
      if (root * 2 == bottom)
        maxChild = root * 2 ;
      else if (comparator.isGreater(numbers[root * 2], numbers[root * 2 + 1]))
        maxChild = root * 2 ;
      else
        maxChild = root * 2 + 1 ;
      
      if (comparator.isLess(numbers[root], numbers[maxChild]))
      {
        swap(numbers, root, maxChild) ;
        root = maxChild ;
      }
      else
        done = true;
    }
  }
  
  public static void heapSort(int[] numbers, IndexComparator comparator)
  {
    int arraySize = numbers.length ;
    int i ;
    
    for (i = (arraySize / 2) - 1; i >= 0 ; i--)
      siftDown(numbers, i, arraySize - 1, comparator);
    
    for (i = arraySize - 1; i >= 1; i--)
    {
      swap(numbers, 0, i) ;
      siftDown(numbers, 0, i - 1, comparator) ;
    }
  }
  
  public static interface HeapSorteable
  {
    public int getSize() ;
    
    public void swap(int index1, int index2) ;
    
    public int get(int index) ;
  }
  
  private static void siftDown(HeapSorteable numbers, int root, int bottom, IndexComparator comparator)
  {
    boolean done = false ;
    int maxChild ;
    
    while ((root * 2 <= bottom) && (!done))
    {
      if (root * 2 == bottom)
        maxChild = root * 2 ;
      else if (comparator.isGreater(numbers.get(root * 2), numbers.get(root * 2 + 1)))
        maxChild = root * 2 ;
      else
        maxChild = root * 2 + 1 ;
      
      if (comparator.isLess(numbers.get(root), numbers.get(maxChild)))
      {
        numbers.swap(root, maxChild) ;
        root = maxChild ;
      }
      else
        done = true;
    }
  }
  
  public static void heapSort(HeapSorteable numbers, IndexComparator comparator)
  {
    int arraySize = numbers.getSize() ;
    int i ;
    
    for (i = (arraySize / 2) - 1; i >= 0 ; i--)
      siftDown(numbers, i, arraySize - 1, comparator);
    
    for (i = arraySize - 1; i >= 1; i--)
    {
      numbers.swap(0, i) ;
      siftDown(numbers, 0, i - 1, comparator) ;
    }
  }
  
  
  public interface IndexSearcher<T>
  {
    public int compare(int index, T obj) ;
  }
  
  public static <T> int binarySearch(T obj, int length, IndexSearcher<T> searcher)
  {
    int low = 0;
    int high = length - 1 ;
    
    while (low <= high)
    {
      int mid = (low + high) / 2 ;
      int cmp = searcher.compare(mid, obj) ;
      
      if (cmp < 0)
        low = mid + 1;
      else if (cmp > 0)
        high = mid - 1;
      else
        return mid;
    }
    
    return -(low + 1);
  }

  ///////////////////////////////////////////////////
  
  private static ResourceBundle boundle = null ;
  
  static
  {
    try
    {
      boundle = ResourceBundle.getBundle("org.dts.spell.dictionary.myspell.messages") ;
    }
    catch (Exception ex)
    {
      boundle = null ;
    }
  }
  
  public static String getString(String str)
  {
    if (null != boundle)
      return boundle.getString(str) ;
    else
      return str ;
  }
  
  public static void throwIOException(String resource, Object... arguments) throws IOException
  {
    MessageFormat form = new MessageFormat(Utils.getString(resource)) ;
    
    throw new IOException(form.format(arguments)) ;
  }
}
