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

package no.nordicsemi.android.mesh.transport;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import no.nordicsemi.android.mesh.models.SigModel;
import no.nordicsemi.android.mesh.models.VendorModel;
import no.nordicsemi.android.mesh.utils.MeshAddress;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class Element implements Parcelable {

    @Expose
    int locationDescriptor;

    @Expose
    final Map<Integer, MeshModel> meshModels;
    @Expose
    int elementAddress;

    @Expose
    String name;

    public static final Creator<Element> CREATOR = new Creator<Element>() {
        @Override
        public Element createFromParcel(Parcel in) {
            return new Element(in);
        }

        @Override
        public Element[] newArray(int size) {
            return new Element[size];
        }
    };

    /**
     * Constructs an element within a node
     *
     * @param elementAddress     element address
     * @param locationDescriptor location descriptor
     * @param models             models belonging to this element
     */
    Element(final int elementAddress, final int locationDescriptor, @NonNull final Map<Integer, MeshModel> models) {
        this(elementAddress, locationDescriptor, models, "Element: " + MeshAddress.formatAddress(elementAddress, true));
    }

    /**
     * Constructs an element within a node
     *
     * @param elementAddress     element address
     * @param locationDescriptor location descriptor
     * @param models             models belonging to this element
     */
    Element(final int elementAddress, final int locationDescriptor, @NonNull final Map<Integer, MeshModel> models, @NonNull final String name) {
        this.elementAddress = elementAddress;
        this.locationDescriptor = locationDescriptor;
        this.meshModels = models;
        this.name = name;
    }

    Element(final int locationDescriptor, @NonNull final Map<Integer, MeshModel> models) {
        this.locationDescriptor = locationDescriptor;
        this.meshModels = models;
    }

    protected Element(Parcel in) {
        elementAddress = in.readInt();
        locationDescriptor = in.readInt();
        meshModels = new LinkedHashMap<>();
        sortModels(in.readHashMap(MeshModel.class.getClassLoader()));
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(elementAddress);
        dest.writeInt(locationDescriptor);
        dest.writeMap(meshModels);
        dest.writeString(name);
    }

    private void sortModels(final HashMap<Integer, MeshModel> unorderedElements) {
        final Set<Integer> unorderedKeys = unorderedElements.keySet();

        final ArrayList<Integer> orderedKeys = new ArrayList<>(unorderedKeys);
        Collections.sort(orderedKeys);
        for (int key : orderedKeys) {
            meshModels.put(key, unorderedElements.get(key));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Returns the address of the element
     */
    public int getElementAddress() {
        return elementAddress;
    }

    void setElementAddress(final int elementAddress) {
        this.elementAddress = elementAddress;
    }

    /**
     * Returns the location descriptor
     */
    public int getLocationDescriptor() {
        return locationDescriptor;
    }

    void setLocationDescriptor(final int locationDescriptor) {
        this.locationDescriptor = locationDescriptor;
    }

    /**
     * Returns the name of the element
     */
    public String getName() {
        return name;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public void setName(@NonNull final String name) {
        this.name = name;
    }

    public int getSigModelCount() {
        int count = 0;
        for (Map.Entry<Integer, MeshModel> modelEntry : meshModels.entrySet()) {
            if (modelEntry.getValue() instanceof SigModel) {
                count++;
            }
        }
        return count;
    }

    public int getVendorModelCount() {
        int count = 0;
        for (Map.Entry<Integer, MeshModel> modelEntry : meshModels.entrySet()) {
            if (modelEntry.getValue() instanceof VendorModel) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns a list of sig models avaialable in this element
     *
     * @return List containing sig models
     */
    public Map<Integer, MeshModel> getMeshModels() {
        return Collections.unmodifiableMap(meshModels);
    }
}
