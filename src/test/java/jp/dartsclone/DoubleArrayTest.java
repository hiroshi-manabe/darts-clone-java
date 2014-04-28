/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.dartsclone;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author manabe
 */
public class DoubleArrayTest {
    private static final int NUM_VALID_KEYS = 1 << 16;
    private static final int NUM_INVALID_KEYS = 1 << 17;
    private static final int MAX_NUM_RESULTS = 6;
    
    public DoubleArrayTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testMain() {
        SortedSet<byte[]> validKeys = generateValidKeys(NUM_VALID_KEYS);
        Set<byte[]> invalidKeys = generateInvalidKeys(NUM_INVALID_KEYS, validKeys);
        testDarts(validKeys, invalidKeys);
    }
    
    private void testDarts(SortedSet<byte[]> validKeys, Set<byte[]> invalidKeys) {
        byte[][] byteKeys = validKeys.toArray(new byte[0][0]);
        byte[][] byteInvalidKeys = invalidKeys.toArray(new byte[0][0]);
                
        int[] values = new int[byteKeys.length];
        
        int keyId = 0;
        for (int i = 0; i < values.length; ++i) {
            values[i] = keyId;
            ++keyId;
        }
        
        DoubleArray dict = new DoubleArray();
        dict.build(byteKeys, null);
        testDict(dict, byteKeys, values, byteInvalidKeys);
        
        dict.build(byteKeys, values);
        testDict(dict, byteKeys, values, byteInvalidKeys);

        Random random = new Random();
        random.setSeed(0);
        for (int i = 0; i < values.length; ++i) {
            values[i] = random.nextInt(10);
        }
        dict.build(byteKeys, values);
        testDict(dict, byteKeys, values, byteInvalidKeys);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            dict.save(out);
        } catch (IOException e) {
            fail();
        }
        DoubleArray dictCopy = new DoubleArray();
        try {
            dictCopy.open(new ByteArrayInputStream(out.toByteArray()));
        } catch (IOException e) {
            fail();
        }
        assertEquals(dict.size(), dictCopy.size());
        testDict(dictCopy, byteKeys, values, byteInvalidKeys);
        
        testCommonPrefixSearch(dict, byteKeys, values);
    }
    
    private void testDict(DoubleArray dict,
    byte[][] keys, int[] values, byte[][] invalidKeys) {
        int value;
        Pair<String, Integer> result;
        
        for (int i = 0; i < keys.length; ++i) {
            assertEquals(values[i],
                    dict.exactMatchSearch(keys[i]));
        }
        
        for (byte[] invalidKey : invalidKeys) {
            assertEquals(dict.exactMatchSearch(invalidKey), -1);
        }
    }

    private void testCommonPrefixSearch(DoubleArray dict,
            byte[][] keys, int[] values) {
        for (int i = 0; i < keys.length; ++i) {
            List<Pair<Integer, Integer>> results = dict.commonPrefixSearch(
                    keys[i], MAX_NUM_RESULTS);
            
            assertTrue(results.size() >= 1);
            assertTrue(results.size() < 10);
            
            assertEquals(keys[i].length, results.get(results.size() - 1).first.intValue());
            assertEquals(values[i], results.get(results.size() - 1).second.intValue());
        }
    }
    
    private SortedSet<byte[]> generateValidKeys(int numKeys) {
        SortedSet<byte[]> validKeys = new TreeSet<byte[]>(new Comparator<byte[]>() {
            @Override
            public int compare(byte[] left, byte[] right) {
                for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
                    int a = (left[i] & 0xff);
                    int b = (right[j] & 0xff);
                    if (a != b) {
                        return a - b;
                    }
                }
                return left.length - right.length;
            }            
        });
        
        Random random = new Random();
        random.setSeed(1);
        StringBuilder keyBuilder = new StringBuilder();
        while (validKeys.size() < numKeys) {
            keyBuilder.setLength(0);
            int length = random.nextInt(8) + 1;
            for (int i = 0; i < length; ++i) {
                keyBuilder.append((char)('A' + random.nextInt(26)));
            }
            try {
                validKeys.add(keyBuilder.toString().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(DoubleArrayTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return validKeys;
    }
    
    private Set<byte[]> generateInvalidKeys(int numKeys, Set<byte[]> validKeys) {
        Set<byte[]> invalidKeys = new HashSet<byte[]>();
        Random random = new Random();
        StringBuilder keyBuilder = new StringBuilder();
        while (invalidKeys.size() < numKeys) {
            keyBuilder.setLength(0);
            int length = random.nextInt(8) + 1;
            for (int i = 0; i < length; ++i) {
                keyBuilder.append((char)('A' + random.nextInt(26)));
            }
            byte[] generatedKey;
            try {
                generatedKey = keyBuilder.toString().getBytes("UTF-8");
                if (!validKeys.contains(generatedKey)) {
                    invalidKeys.add(generatedKey);
                }
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(DoubleArrayTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return invalidKeys;
    }
}
