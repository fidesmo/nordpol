package nordpol.android;

import android.app.Activity;

/**
 * TagDispatcherBuilder helps create a TagDispatcher using the builder pattern.
 * <p>
 * TagDispatcherBuilder will return a TagDispatcher after calling the
 * {@link #build()}. Depending on your preferences you can call the methods in
 * the builder to change the default behavior.
 */
public class TagDispatcherBuilder {
    private Activity activity;
    private OnDiscoveredTagListener tagDiscoveredListener;
    private boolean enableUnavailableNfcUserPrompt;
    private boolean enableSounds;
    private boolean enableDispatchingOnUiThread;
    private boolean enableBroadcomWorkaround;
    private boolean enableReaderMode;
    private boolean enableNdefCheck;

    /**
     * Constructor for TagDispatcherBuilder
     * <p>
     * By default the user will be prompted if NFC is unavailable. You can
     * change this behavior with the method
     * {@link #enableUnavailableNfcUserPrompt(boolean)}.
     * <p>
     * By default NFC interactions will produce sounds. You can change this
     * behavior with the method {@link #enableSounds(boolean)}.
     * <p>
     * By default new tags will be dispatched on the UI thread. You can change
     * this behavior with the method
     * {@link #enableDispatchingOnUiThread(boolean)}.
     * <p>
     * By default TagDispatcher will use the Broadcom workaround. You can change
     * this behavior with the method {@link #enableBroadcomWorkaround(boolean)}.
     * <p>
     * By default TagDispatcher will use ReaderMode. You can change this
     * behavior with the method {@link #enableReaderMode(boolean)}.
     * <p>
     * By default TagDispatcher will check NFC NDEF. You can change this
     * behavior with the method {@link #enableReaderMode(boolean)}.
     * <p>
     * Call {@link #build()} to build the TagDispatcher.
     *
     * @param activity               The Activity to attach the TagDispatcher to
     * @param tagDiscoveredListener  A tagDiscoveredListener callback interface
     */
    public TagDispatcherBuilder (final Activity activity,
                                 final OnDiscoveredTagListener tagDiscoveredListener) {
        this.activity = activity;
        this.tagDiscoveredListener = tagDiscoveredListener;
        this.enableUnavailableNfcUserPrompt = true;
        this.enableSounds = true;
        this.enableDispatchingOnUiThread = true;
        this.enableBroadcomWorkaround = true;
        this.enableReaderMode = true;
        this.enableNdefCheck = true;
    }

    /**
     * Sets if TagDispatcher should handle the case of NFC not being available.
     * <p>
     * If NFC is disabled on the device and TagDispatcher is set as responsible
     * then the user will receive a Toast asking them to enable NFC while
     * being sent to the NFC settings. If the device completely lacks NFC and
     * TagDispatcher is set as responsibe then the user will receive a Toast
     * telling them that NFC isn't available on the device.
     *
     * @param enableUnavailableNfcUserPrompt  Should TagDispatcher handle NFC
                                              unavailability?
     * @return                                this TagDispatcherBuilder
     */
    public TagDispatcherBuilder enableUnavailableNfcUserPrompt(boolean enableUnavailableNfcUserPrompt){
        this.enableUnavailableNfcUserPrompt = enableUnavailableNfcUserPrompt;
        return this;
    }

    /**
     * Sets if NFC interactions produce sounds
     *
     * @param enableSounds  Should NFC interactions produce sounds?
     * @return              this TagDispatcherBuilder
     */
    public TagDispatcherBuilder enableSounds(boolean enableSounds){
        this.enableSounds = enableSounds;
        return this;
    }

    /**
     * Sets if new tags should be dispatched on the UI thread
     *
     * @param enableDispatchingOnUiThread  Should new tags be dispatched on UI
     *                                     thread?
     * @return                             this TagDispatcherBuilder
     */
    public TagDispatcherBuilder enableDispatchingOnUiThread(boolean enableDispatchingOnUiThread){
        this.enableDispatchingOnUiThread = enableDispatchingOnUiThread;
        return this;
    }

    /**
     * Delays presence check.
     * <p>
     * Delays presence check, that on Broadcom chips uses an APDU that resets
     * the tag. See
     * <a href="https://code.google.com/p/android/issues/detail?id=58773">issue on Google Code.</a>
     *
     * @param enableBroadcomWorkaround  Should TagDispatcher use the Broadcom
     *                                  workaround?
     * @return                          this TagDispatcherBuilder
     */
    public TagDispatcherBuilder enableBroadcomWorkaround(boolean enableBroadcomWorkaround){
        this.enableBroadcomWorkaround = enableBroadcomWorkaround;
        return this;
    }

    /**
     * Sets if TagDispatcher should use ReaderMode on devices where it is supported
     * <p>
     * For more information on ReaderMode please see
     * <a href="http://developer.android.com/reference/android/nfc/NfcAdapter.html#enableReaderMode%28android.app.Activity,%20android.nfc.NfcAdapter.ReaderCallback,%20int,%20android.os.Bundle%29">the Android Api documentation</a>
     *
     * @param enableReaderMode  Use ReaderMode?
     * @return                  this TagDispatcherBuilder
     */
    public TagDispatcherBuilder enableReaderMode(boolean enableReaderMode){
        this.enableReaderMode = enableReaderMode;
        return this;
    }

    /**
     * Sets if TagDispatcher should disable NFC NDEF checking.
     * <p>
     * If you are not relying on NDEF or want to run heavy operations we
     * suggest to disable checking NDEF as keeping it enabled can make it
     * diffucult to "wake up" secure elements that are stuck in garbage
     * collection.
     *
     * @param enableNdefCheck  Enable NFC NDEF checking?
     * @return                 this TagDispatcherBuilder
     */
    public TagDispatcherBuilder enableNdefCheck(boolean enableNdefCheck){
        this.enableNdefCheck = enableNdefCheck;
        return this;
    }

    /**
     * Build the {@link TagDispatcher}.
     *
     * @return A new TagDispatcher
     */
    public TagDispatcher build() {
      return new TagDispatcher (activity,
                                tagDiscoveredListener,
                                enableUnavailableNfcUserPrompt,
                                enableSounds,
                                enableDispatchingOnUiThread,
                                enableBroadcomWorkaround,
                                enableReaderMode,
                                enableNdefCheck);
    }
  }
