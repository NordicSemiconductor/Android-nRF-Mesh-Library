package no.nordicsemi.android.mesh.utils;

/**
 * Inspired by https://github.com/ome/ome-codecs/blob/master/src/main/java/ome/codecs/BitWriter.java
 * <p>
 * Copyright (C) 2005 - 2017 Open Microscopy Environment:
 * - Board of Regents of the University of Wisconsin-Madison
 * - Glencoe Software, Inc.
 * - University of Dundee
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * <p>
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
public class BitWriter {
    /**
     * Buffer storing all bits written thus far.
     */
    private byte[] buf;

    /**
     * Byte index into the buffer.
     */
    private int index;

    /**
     * Bit index into current byte of the buffer.
     */
    private int bit;

    // -- Constructors --

    /**
     * Constructs a new bit writer.
     */
    public BitWriter() {
        this(10);
    }

    /**
     * Constructs a new bit writer with the given initial buffer size.
     */
    public BitWriter(int size) {
        buf = new byte[size];
    }

    /**
     * Writes the given value using the given number of bits.
     */
    public void write(int value, int numBits) {
        if (numBits <= 0) return;
        byte[] bits = new byte[numBits];
        for (int i = 0; i < numBits; i++) {
            bits[i] = (byte) (value & 0x0001);
            value >>= 1;
        }
        for (int i = numBits - 1; i >= 0; i--) {
            int b = bits[i] << (7 - bit);
            buf[index] |= b;
            bit++;
            if (bit > 7) {
                bit = 0;
                index++;
                if (index >= buf.length) {
                    // buffer is full; increase the size
                    byte[] newBuf = new byte[buf.length * 2];
                    System.arraycopy(buf, 0, newBuf, 0, buf.length);
                    buf = newBuf;
                }
            }
        }
    }

    /**
     * Gets an array containing all bits written thus far.
     */
    public byte[] toByteArray() {
        int size = index;
        if (bit > 0) size++;
        byte[] b = new byte[size];
        System.arraycopy(buf, 0, b, 0, size);
        return b;
    }
}
