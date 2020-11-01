/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dts.spell.dictionary.myspell.wordmaps;

import java.util.Arrays;

/**
 *
 * @author dreamtangerine
 */
public class SimpleHEntry {

    public static class ByteString {

        public ByteString(byte[] str) {
            this.str = str;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ByteString) {
                ByteString byteStr = (ByteString) obj;

                if (str.length == byteStr.str.length) {
                    for (int i = 0; i < str.length; ++i) {
                        if (str[i] != byteStr.str[i]) {
                            return false;

                        }
                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(str);
        }
        private byte[] str;
    }

    public SimpleHEntry(byte[] word, byte[] astr) {
        this.word = word;
        this.astr = astr;
    }
    public byte[] word;
    public byte[] astr;
}
