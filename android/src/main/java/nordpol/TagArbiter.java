package nordpol.android;

import android.nfc.Tag;

import nordpol.android.OnDiscoveredTagListener;

/**
 * The TagArbiter is a tool to help handle tags between activities and
 * fragments in your Android app. You need to create a single instance
 * of this class to be shared between the activities and fragments
 * requiring tag instances. This could be done using a singleton or by
 * providig a reference to the instance to every activity/fragment.
 *
 * The basic flow is to create an instance of the TagDispatcher in all
 * activities, but rather than registering the activity directly to
 * the TagDispatcher, one would register the tag arbiter instead. This
 * way an activity that finds a tag can use it whenever it needs it or
 * it can be used by any other activity that needs it. As long as
 * TagArbiter.tagErrored is not called, the tag found will be kept and
 * can be safely reused among several activities.

 * From an end user perspective, this class enables the end user to
 * present their device to the phone once and keep it there while
 * switching between different activities.
 */

public class TagArbiter implements OnDiscoveredTagListener {
    private static TagArbiter instance = new TagArbiter();
    private OnDiscoveredTagListener onDiscoveredTagListener = null;
    private Tag lastTag = null;

    /**
     * Set a listener to receive a tag found by any activity
     *
     * This method either registers the listener for later dispatch
     * when a tag arrives pr calls it immediately on the same thread
     * if a tag has already been found.  Only a single listener is
     * allowed so this method overwrites any existing listener
     * claiming the event for itself.
     * @param onDiscoveredTagListener The listener that requires a tag
     */
    public void setListener(OnDiscoveredTagListener onDiscoveredTagListener) {
        this.onDiscoveredTagListener = onDiscoveredTagListener;
        if(lastTag != null && this.onDiscoveredTagListener != null) {
            this.onDiscoveredTagListener.tagDiscovered(lastTag);
        }
    }

    /**
     * Removes the current listener from the arbiter
     */
    public void unsetListener() {
        onDiscoveredTagListener = null;
    }

    @Override
    public void tagDiscovered(Tag tag) {
        if(onDiscoveredTagListener != null) {
            onDiscoveredTagListener.tagDiscovered(tag);
        }
        lastTag = tag;
    }

    /**
     * Reset a tag previously found e.g. if it erred
     *
     * If the tag provided equals the current lastTag, forget about it
     * by nulling it.
     * @param tag The tag that erred
     */
    public void tagErrored(Tag tag) {
        if(lastTag == tag)
            lastTag = null;
    }

    /**
     * Factory method that returns a singleton instance of this class
     * @returns The singleton instance of this class
     */
    public static TagArbiter getTagArbiter() {
        return instance;
    }
}
