/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.dartsclone;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.dartsclone.details.DoubleArrayBuilder;
import jp.dartsclone.details.Keyset;

/**
 *
 * @author manabe
 */
public class DoubleArray {
    public void build(byte[][] keys, int[] values) {
        Keyset keyset = new Keyset(keys, values);
        DoubleArrayBuilder builder = new DoubleArrayBuilder();
        builder.build(keyset);
        
        _array = builder.copy();
    }
    
    /**
     * Read from a stream. The stream must implement the available() method.
     * @param stream
     * @throws IOException 
     */
    public void open(InputStream stream) throws IOException {

        int size = (int)(stream.available() / UNIT_SIZE);
        _array = new int[size];
        
        DataInputStream in = null;
        try {
            in = new DataInputStream(new BufferedInputStream(
                    stream));
            for (int i = 0; i < size; ++i) {
                _array[i] = in.readInt();
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
    /**
     * Saves the trie data into a stream.
     * @param stream
     * @throws IOException 
     */
    public void save(OutputStream stream) throws IOException {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new BufferedOutputStream(
                    stream));
            for (int i = 0; i < _array.length; ++i) {
                out.writeInt(_array[i]);
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    
    /**
     * Returns the corresponding value if the key is found. Otherwise returns -1.
     * This method converts the key into UTF-8.
     * @param key search key
     * @return found value
     */
    public int exactMatchSearch(String key) {
        int ret = 0;
        try {
            ret = exactMatchSearch(key.getBytes("UTF-8"));
        } catch(UnsupportedEncodingException ex) {
            Logger.getLogger(DoubleArray.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
    /**
     * Returns the corresponding value if the key is found. Otherwise returns -1.
     * @param key search key
     * @return found value
     */
    public int exactMatchSearch(byte[] key) {
        int unit = _array[0];
        int nodePos = 0;
        
        for (byte b : key) {
            // nodePos ^= unit.offset() ^ b
            nodePos ^= ((unit >>> 10) << ((unit & (1 << 9)) >>> 6)) ^ (b & 0xFF);
            unit = _array[nodePos];
            // if (unit.label() != b)
            if ((unit & ((1 << 31) | 0xFF)) != (b & 0xff)) {
                return -1;
            }
        }
        // if (!unit.has_leaf()) {
        if (((unit >>> 8) & 1) != 1) {
            return -1;
        }
        // unit = _array[nodePos ^ unit.offset()];
        unit = _array[nodePos ^ ((unit >>> 10) << ((unit & (1 << 9)) >>> 6))];
        // return unit.value();
        return unit & ((1 << 31) - 1);
    }
    
    /**
     * Returns the keys that begins with the given key and its corresponding values.
     * The first of the returned pair represents the length of the found key.
     * @param key
     * @param offset
     * @param maxResults
     * @return found keys and values
     */
    public List<Pair<Integer, Integer>> commonPrefixSearch(byte[] key,
            int offset,
            int maxResults) {
        ArrayList<Pair<Integer, Integer>> result = new ArrayList<Pair<Integer, Integer>>();
        int unit = _array[0];
        int nodePos = 0;
        // nodePos ^= unit.offset();
        nodePos ^= ((unit >>> 10) << ((unit & (1 << 9)) >>> 6));
        for (int i = offset; i < key.length; ++i) {
            byte b = key[i];
            nodePos ^= (b & 0xff);
            unit = _array[nodePos];
            // if (unit.label() != b) {
            if ((unit & ((1 << 31) | 0xFF)) != (b & 0xff)) {
                return result;
            }

            // nodePos ^= unit.offset();
            nodePos ^= ((unit >>> 10) << ((unit & (1 << 9)) >>> 6));
            
            // if (unit.has_leaf()) {
            if (((unit >>> 8) & 1) == 1) {
                if (result.size() < maxResults) {
                    // result.add(new Pair<i, _array[nodePos].value());
                    result.add(new Pair<Integer, Integer>(i + 1, _array[nodePos] & ((1 << 31) - 1)));
                }
            }
        }
        return result;
    }
    
    public int size() {
        return _array.length;
    }
    
    private static final int UNIT_SIZE = 4; // sizeof(int)
    private int[] _array;
}
