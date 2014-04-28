/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.dartsclone.details;

/**
 *
 * @author
 */
class BitVector {
    boolean get(int id) {
        return (_units.get(id / UNIT_SIZE) >>> (id % UNIT_SIZE) & 1) == 1;
    }
    
    void set(int id, boolean bit) {
        if (bit) {
            _units.set(id / UNIT_SIZE, _units.get(id / UNIT_SIZE)
                    | 1 << (id % UNIT_SIZE));
        }
    }
    
    int rank(int id) {
        int unit_id = id / UNIT_SIZE;
        return _ranks[unit_id] + popCount(_units.get(unit_id)
                & (~0 >>> (UNIT_SIZE - (id % UNIT_SIZE) - 1)));
    }
    
    boolean empty() {
        return _units.empty();
    }
    
    int numOnes() {
        return _numOnes;
    }
    
    int size() {
        return _size;
    }
    
    void append() {
        if ((_size % UNIT_SIZE) == 0) {
            _units.add(0);
        }
        ++_size;
    }
    
    void build() {
        _ranks = new int[_units.size()];
        
        _numOnes = 0;
        for (int i = 0; i < _units.size(); ++i) {
            _ranks[i] = _numOnes;
            _numOnes += popCount(_units.get(i));
        }
    }
    
    void clear() {
        _units.clear();
        _ranks = null;
    }
    
    private static final int UNIT_SIZE = 32; // sizeof(int) * 8
    
    private static int popCount(int unit) {
        unit = ((unit & 0xAAAAAAAA) >>> 1) + (unit & 0x55555555);
        unit = ((unit & 0xCCCCCCCC) >>> 2) + (unit & 0x33333333);
        unit = ((unit >>> 4) + unit) & 0x0F0F0F0F;
        unit += unit >>> 8;
        unit += unit >>> 16;
        return unit & 0xFF;
     }
    
    private AutoIntPool _units = new AutoIntPool();
    private int[] _ranks;
    private int _numOnes;
    private int _size;
}
