/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dts.spell.myspell.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dreamtangerine
 */
public class CharMap {

    public int maxEntries = 0 ;
    private char map[] = new char[256] ;

    public CharMap() {

    }

    public String convertFromBytes(byte[] bytes) {
        char[] result = new char[bytes.length] ;

        for (int i = 0 ; i < bytes.length ; ++i)
            result[i] = getChar(bytes[i]) ;

        return new String(result) ;
    }

    public byte[] convertFromString(String word) {
        byte[] result = new byte[word.length()] ;

        for (int i = 0 ; i < result.length ; ++i)
            result[i] = getByte(word.charAt(i)) ;

        return result ;
    }

    private int findChar(char c) {
        for (int i = 0 ; i < maxEntries ; ++i)
            if (map[i] == c)
                return i ;

        return -1 ;
    }

    public void put(String word) {
        for (int i = 0 ; i < word.length() ; ++i)
            put(word.charAt(i)) ;
    }

    public void put(char c) {
          try {
        String txt = new String(new char[] { c }) ;

        Charset cs = Charset.forName("ISO8859-15") ;
        float m = cs.newEncoder().maxBytesPerChar();

      byte[] bytes = txt.getBytes("ISO8859-1");


        int index = findChar(c) ;

        if (index < 0) {
            map[maxEntries] = c ;
            ++maxEntries ;
        }
    } catch (UnsupportedEncodingException ex) {
      Logger.getLogger(CharMap.class.getName()).log(Level.SEVERE, null, ex);
    }

    }

    public byte getByte(char c) {
        return (byte) findChar(c) ;
    }

    public char getChar(int index) {
        return map[index] ;
    }

    public void trim() {
        char[] aux = new char [getSize()] ;

        System.arraycopy(map, 0, aux, 0, maxEntries) ;
        map = aux ;
    }

    public int getSize() {
        return maxEntries ;
    }
}
