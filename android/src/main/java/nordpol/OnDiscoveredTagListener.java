package nordpol.android;

import android.nfc.Tag;

public interface OnDiscoveredTagListener {
    /** Extract an IsoDep related object from the received tag and use it.
     * @see AndroidCard#get(Tag)
     * @param t the tag
     */
    public void tagDiscovered(Tag t);
}
