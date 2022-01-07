package no.nordicsemi.android.nrfmesh.scenes.adapter;

import android.os.Parcel;
import android.os.Parcelable;

import no.nordicsemi.android.mesh.Scene;

/**
 * A ui state class mapped to display the contents of a {@link Scene}
 */
public class SceneUiState implements Parcelable {
    private final int number;
    private final String name;
    private final boolean isCurrentScene;

    public SceneUiState(final int number, final String name, final boolean isCurrentScene) {
        this.number = number;
        this.name = name;
        this.isCurrentScene = isCurrentScene;
    }

    protected SceneUiState(Parcel in) {
        number = in.readInt();
        name = in.readString();
        isCurrentScene = in.readByte() != 0;
    }

    public static final Creator<SceneUiState> CREATOR = new Creator<SceneUiState>() {
        @Override
        public SceneUiState createFromParcel(Parcel in) {
            return new SceneUiState(in);
        }

        @Override
        public SceneUiState[] newArray(int size) {
            return new SceneUiState[size];
        }
    };

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public boolean isCurrentScene() {
        return isCurrentScene;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SceneUiState sceneUiState = (SceneUiState) o;

        if (number != sceneUiState.number) return false;
        if (isCurrentScene != sceneUiState.isCurrentScene) return false;
        return name.equals(sceneUiState.name);
    }

    @Override
    public int hashCode() {
        int result = number;
        result = 31 * result + name.hashCode();
        result = 31 * result + (isCurrentScene ? 1 : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(number);
        dest.writeString(name);
        dest.writeByte((byte) (isCurrentScene ? 1 : 0));
    }
}
