package nordpol.android;

import android.nfc.Tag;

public interface OnDiscoveredTagListener {
    /** Extract an IsoDep related object from the received tag and use it.
     * @see {@link AndroidCard#get(Tag)}
     * @param t
     */
    public void tagDiscovered(Tag t);
}
