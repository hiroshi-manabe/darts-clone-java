darts-clone-java: A Java port of darts-clone.
=========================================================

SYNOPSIS
--------

A Java port of [darts-clone](https://code.google.com/p/darts-clone/) by Susumu Yata.

USAGE
-----
Pass an array of keys and int values to build a double array.
There are some points you should be aware of:

1. Keys should be given in byte[], not String.
1. Keys must not contain zeros.
1. Keys must be sorted lexically based on their unsigned values. For example, the key 0x80 should be sorted after the key 0x7f. See jp.dartsclone.DoubleArrayTest.java.
1. If you don't need to associate values to keys, pass an array of 0 with the same length as that of keys. This way, you can save some memory. If you pass NULL, consecutive integers starting from 0 will be associated to the keys.
1. Values should not contain negative numbers.

Caveats:

1. "Keys must not contain zeros" means you cannot put UTF-16 string (Java String) represented in byte array.
1. Maybe you want to use UTF-8 string represented in byte arrays. That is basically a good idea. But if you do so, be sure to sort the keys AFTER converting to UTF-8, not before. The sort order may change before converting from String and after converting into UTF-8 byte arrays.

LICENSE
-------
BSD. See LICENSE file.
