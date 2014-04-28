/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.dartsclone;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
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
        Set<String> validKeys = generateValidKeys(NUM_VALID_KEYS);
        Set<String> invalidKeys = generateInvalidKeys(NUM_INVALID_KEYS, validKeys);
        testDarts(validKeys, invalidKeys);
    }
    
    private void testDarts(Set<String> validKeys, Set<String> invalidKeys) {
        List<String> keys = new ArrayList<String>(validKeys);
        List<Integer> values = new ArrayList<Integer>();
        
        int keyId = 0;
        for (String key : keys) {
            values.add(keyId);
            ++keyId;
        }
        
        DoubleArray dict = new DoubleArray();
        dict.build(keys, null);
        testDict(dict, keys, values, invalidKeys);
        
        dict.build(keys, values);
        testDict(dict, keys, values, invalidKeys);

        Random random = new Random();
        random.setSeed(0);
        for (int i = 0; i < values.size(); ++i) {
            values.set(i, random.nextInt(10));
        }
        dict.build(keys, values);
        testDict(dict, keys, values, invalidKeys);
        
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
        testDict(dictCopy, keys, values, invalidKeys);
        
        testCommonPrefixSearch(dict, keys, values, invalidKeys);
    }
    
    private void testDict(DoubleArray dict,
    List<String> keys, List<Integer> values, Set<String> invalidKeys) {
        int value;
        Pair<String, Integer> result;
        
        for (int i = 0; i < keys.size(); ++i) {
            assertEquals(values.get(i).intValue(),
                    dict.exactMatchSearch(keys.get(i)));
        }
        
        for (String invalidKey : invalidKeys) {
            assertEquals(dict.exactMatchSearch(invalidKey), -1);
        }
    }

    private void testCommonPrefixSearch(DoubleArray dict,
    List<String> keys, List<Integer> values, Set<String> invalidKeys) {
        for (int i = 0; i < keys.size(); ++i) {
            List<Pair<String, Integer>> results = dict.commonPrefixSearch(
                    keys.get(i), MAX_NUM_RESULTS);
            
            assertTrue(results.size() >= 1);
            assertTrue(results.size() < 10);
            
            assertEquals(keys.get(i), results.get(results.size() - 1).first);
            assertEquals(values.get(i), results.get(results.size() - 1).second);
        }
    }
    
    private SortedSet<String> generateValidKeys(int numKeys) {
        SortedSet<String> validKeys = new TreeSet<String>();
        Random random = new Random();
        random.setSeed(1);
        StringBuilder keyBuilder = new StringBuilder();
        while (validKeys.size() < numKeys) {
            keyBuilder.setLength(0);
            int length = random.nextInt(8) + 1;
            for (int i = 0; i < length; ++i) {
                keyBuilder.append((char)('A' + random.nextInt(26)));
            }
            validKeys.add(keyBuilder.toString());
        }
        return validKeys;
    }
    
    private SortedSet<String> generateInvalidKeys(int numKeys, Set<String> validKeys) {
        SortedSet<String> invalidKeys = new TreeSet<String>();
        Random random = new Random();
        StringBuilder keyBuilder = new StringBuilder();
        while (invalidKeys.size() < numKeys) {
            keyBuilder.setLength(0);
            int length = random.nextInt(8) + 1;
            for (int i = 0; i < length; ++i) {
                keyBuilder.append((char)('A' + random.nextInt(26)));
            }
            String generatedKey = keyBuilder.toString();
            if (!validKeys.contains(generatedKey)) {
                invalidKeys.add(keyBuilder.toString());
            }
        }
        return invalidKeys;
    }
}
