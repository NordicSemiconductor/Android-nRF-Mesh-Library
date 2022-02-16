package no.nordicsemi.android.mesh.utils;

public class ArrayUtils {

     public static  byte[] reverseArray(byte[] array){
        for(int i=0; i<array.length/2; i++){
            byte temp = array[i];
            array[i] = array[array.length -i -1];
            array[array.length -i -1] = temp;
        }
        return array;
    }
}
