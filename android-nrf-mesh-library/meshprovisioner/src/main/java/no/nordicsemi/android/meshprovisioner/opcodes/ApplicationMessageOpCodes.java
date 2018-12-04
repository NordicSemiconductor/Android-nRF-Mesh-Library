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

package no.nordicsemi.android.meshprovisioner.opcodes;

public class ApplicationMessageOpCodes {

    /**
     * Opcode for the "Generic OnOff Get" message.
     */
    public static final int GENERIC_ON_OFF_GET = 0x8201;

    /**
     * Opcode for the "Generic OnOff Set" message.
     */
    public static final int GENERIC_ON_OFF_SET = 0x8202;

    /**
     * Opcode for the "Generic OnOff Set Unacknowledged" message.
     */
    public static final int GENERIC_ON_OFF_SET_UNACKNOWLEDGED = 0x8203;

    /**
     * Opcode for the "Generic OnOff Status" message.
     */
    public static final int GENERIC_ON_OFF_STATUS = 0x8204;

    /**
     * Opcode for the "Generic Level Get" message.
     */
    public static final int GENERIC_LEVEL_GET = 0x8205;

    /**
     * Opcode for the "Generic Level Set" message.
     */
    public static final int GENERIC_LEVEL_SET = 0x8206;

    /**
     * Opcode for the "Generic Level Set Unacknowledged" message.
     */
    public static final int GENERIC_LEVEL_SET_UNACKNOWLEDGED = 0x8207;

    /**
     * Opcode for the "Generic Level Status" message.
     */
    public static final int GENERIC_LEVEL_STATUS = 0x8208;

    /**
     * Opcode for the "Light Lightness Get" message
     */
    public static final int LIGHT_LIGHTNESS_GET = 0x824B;

    /**
     * Opcode for the "Light Lightness Set" message
     */
    public static final int LIGHT_LIGHTNESS_SET = 0x824C;

    /**
     * Opcode for the "Light Lightness Set Unacknowledged" message
     */
    public static final int LIGHT_LIGHTNESS_SET_UNACKNOWLEDGED = 0x824D;

    /**
     * Opcode for the "Light Lightness Status" message
     */
    public static final int LIGHT_LIGHTNESS_STATUS = 0x824E;

    /**
     * Opcode for the "Light Ctl Get" message
     */
    public static final int LIGHT_CTL_GET = 0x825D;

    /**
     * Opcode for the "Light Ctl Set" message
     */
    public static final int LIGHT_CTL_SET = 0x825E;

    /**
     * Opcode for the "Light Ctl Set Unacknowledged" message
     */
    public static final int LIGHT_CTL_SET_UNACKNOWLEDGED = 0x825F;

    /**
     * Opcode for the "Light Ctl Status" message
     */
    public static final int LIGHT_CTL_STATUS = 0x8260;

    /**
     * Opcode for the "Light Hsl Get" message
     */
    public static final int LIGHT_HSL_GET = 0x826D;

    /**
     * Opcode for the "Light Hsl Set" message
     */
    public static final int LIGHT_HSL_SET = 0x8276;

    /**
     * Opcode for the "Light Hsl Set Unacknowledged" message
     */
    public static final int LIGHT_HSL_SET_UNACKNOWLEDGED = 0x8277;

    /**
     * Opcode for the "Light Hsl Status" message
     */
    public static final int LIGHT_HSL_STATUS = 0x8278;

    /**
     * Opcode for the "Scene Status" message
     */
    public static final int SCENE_STATUS = 0x5E;

    /**
     * Opcode for the "Scene Register Status" message
     */
    public static final int SCENE_REGISTER_STATUS = 0x8245;

    /**
     * Opcode for the "Scene Get" message
     */
    public static final int SCENE_GET = 0x8241;

    /**
     * Opcode for the "Scene Register Get" message
     */
    public static final int SCENE_REGISTER_GET = 0x8244;

    /**
     * Opcode for the "Scene Recall" message
     */
    public static final int SCENE_RECALL = 0x8242;

    /**
     * Opcode for the "Scene Recall Unacknowledged" message
     */
    public static final int SCENE_RECALL_UNACKNOWLEDGED = 0x8243;

    /**
     * Opcode for the "Scene Store" message
     */
    public static final int SCENE_STORE = 0x8246;

    /**
     * Opcode for the "Scene Store Unacknowledged" message
     */
    public static final int SCENE_STORE_UNACKNOWLEDGED = 0x8247;

    /**
     * Opcode for the "Scene Delete" message
     */
    public static final int SCENE_DELETE = 0x829E;

    /**
     * Opcode for the "Scene Delete Unacknowledged" message
     */
    public static final int SCENE_DELETE_UNACKNOWLEDGED = 0x829F;

}
