/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dts.spell.dictionary.myspell.wordmaps;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 *
 * @author dreamtangerine
 */
public abstract class WordMapWithCharMap extends WordMap {

    private Charset charSet ;

    protected String getEncoding() {
        return charSet.name() ;
    }

  @Override
  public void init(long lastModificationTime, String wordMapName, String encoding) throws IOException {
    charSet = Charset.forName(encoding) ;
  }

    protected String convertFromBytes(byte[] bytes)  {
        return new String(bytes,  charSet) ;
    }

    protected byte[] convertFromString(String word) {
        return word.getBytes(charSet) ;
    }
}
