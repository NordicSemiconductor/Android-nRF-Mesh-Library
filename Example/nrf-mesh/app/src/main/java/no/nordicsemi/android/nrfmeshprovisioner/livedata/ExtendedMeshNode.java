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

package no.nordicsemi.android.nrfmeshprovisioner.livedata;

import android.arch.lifecycle.LiveData;
import android.util.Log;

import java.util.Map;

import no.nordicsemi.android.meshprovisioner.BaseMeshNode;
import no.nordicsemi.android.meshprovisioner.configuration.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.utils.Element;

public class ExtendedMeshNode extends LiveData<ExtendedMeshNode> {

    private BaseMeshNode mMeshNode;
    private static final String TAG = ExtendedMeshNode.class.getSimpleName();

    public ExtendedMeshNode(final BaseMeshNode meshNode) {
        this.mMeshNode = meshNode;
        setValue(this);
    }

    public BaseMeshNode getMeshNode() {
        return mMeshNode;
    }

    /**
     * Updates the mesh node and posts the value
     * @param meshNode Provisioned mesh node
     */
    public void updateMeshNode(final BaseMeshNode meshNode) {
        this.mMeshNode = meshNode;
        postValue(this);
    }

    /**
     * Sets the mesh node without posting live data
     * @param meshNode Provisioned mesh node
     */
    public void setMeshNode(final ProvisionedMeshNode meshNode){
        this.mMeshNode = meshNode;
    }

    public boolean hasElements(){
        if(mMeshNode.isProvisioned()) {
            final Map<Integer, Element> elements = ((ProvisionedMeshNode) mMeshNode).getElements();
            return elements != null && !elements.isEmpty();
        }
        return false;
    }

    public boolean hasAddedAppKeys(){
        if(mMeshNode.isProvisioned()) {
            final Map<Integer, String> appKeys = ((ProvisionedMeshNode) mMeshNode).getAddedAppKeys();
            return appKeys != null && !appKeys.isEmpty();
        }
        return false;
    }
}
