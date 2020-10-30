/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.mesh.utils;

public class DeviceFeatureUtils {

    /**
     * Checks if relay feature is supported by node;
     *
     * @param feature 16-bit feature value
     * @return true if relay bit = 1 and false if relay bit = 0
     */
    public static boolean supportsRelayFeature(final int feature) {
        return ((feature & (1 << 0)) > 0);
    }

    /**
     * Checks if proxy feature is supported by node;
     *
     * @param feature 16-bit feature value
     * @return true if proxy bit = 1 and false if proxy bit = 0
     */
    public static boolean supportsProxyFeature(final int feature) {
        return ((feature & (1 << 1)) > 0);
    }

    /**
     * Checks if friend feature is supported by node;
     *
     * @param feature 16-bit feature value
     * @return true if friend bit = 1 and false if friend bit = 0
     */
    public static boolean supportsFriendFeature(final int feature) {
        return ((feature & (1 << 2)) > 0);
    }

    /**
     * Checks if low power feature is supported by node;
     *
     * @param feature 16-bit feature value
     * @return true if low power bit = 1 and false if low power bit = 0
     */
    public static boolean supportsLowPowerFeature(final int feature) {
        return ((feature & (1 << 3)) > 0);
    }

    /**
     * Returns the relay feature state value
     *
     * @param feature 16-bit feature value
     */
    public static int getRelayFeature(final int feature) {
        return feature & 1;
    }

    /**
     * Returns the proxy feature state value
     *
     * @param feature 16-bit feature value
     */
    public static int getProxyFeature(final int feature) {
        return (feature >> 1) & 1;
    }

    /**
     * Returns the friend feature state value
     *
     * @param feature 16-bit feature value
     */
    public static int getFriendFeature(final int feature) {
        return (feature >> 2) & 1;
    }

    /**
     * Returns the low power feature state value
     *
     * @param feature 16-bit feature value
     */
    public static int getLowPowerFeature(final int feature) {
        return (feature >> 3) & 1;
    }
}
