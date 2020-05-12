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

import java.util.Locale;

@SuppressWarnings("unused")
public class CompositionDataParser {

    public static String formatCompanyIdentifier(final int companyIdentifier, final boolean add0x) {
        return add0x ? "0x" + String.format(Locale.US, "%04X", companyIdentifier) : String.format(Locale.US, "%04X", companyIdentifier);
    }

    public static String formatProductIdentifier(final int productIdentifier, final boolean add0x) {
        return add0x ? "0x" + String.format(Locale.US, "%04X", productIdentifier) : String.format(Locale.US, "%04X", productIdentifier);
    }

    public static String formatVersionIdentifier(final int versionIdentifier, final boolean add0x) {
        return add0x ? "0x" + String.format(Locale.US, "%04X", versionIdentifier) : String.format(Locale.US, "%04X", versionIdentifier);
    }

    public static String formatReplayProtectionCount(final int replayProtectionCount, final boolean add0x) {
        return add0x ? "0x" + String.format(Locale.US, "%04X", replayProtectionCount) : String.format(Locale.US, "%04X", replayProtectionCount);
    }

    public static String formatFeatures(final int features, final boolean add0x) {
        return add0x ? "0x" + String.format(Locale.US, "%04X", features) : String.format(Locale.US, "%04X", features);
    }

    public static String formatModelIdentifier(final int modelId, final boolean add0x) {
        if (modelId < Short.MIN_VALUE || modelId > Short.MAX_VALUE) {
            return add0x ? "0x" + String.format(Locale.US, "%08X", modelId) : String.format(Locale.US, "%08X", modelId);
        } else {
            return add0x ? "0x" + String.format(Locale.US, "%04X", modelId) : String.format(Locale.US, "%04X", modelId);
        }
    }
}
