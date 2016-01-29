package nordpol.android;

import android.nfc.Tag;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.Handler;
import android.widget.Toast;

public class TagDispatcher {
    private static final int DELAY_PRESENCE = 5000;

    private OnDiscoveredTagListener tagDiscoveredListener;
    private boolean handleUnavailableNfc;
    private boolean disableSounds;
    private boolean dispatchOnUiThread;
    private boolean broadcomWorkaround;
    private boolean noReaderMode;
    private boolean disableNdefCheck;
    private Activity activity;

    public enum NfcStatus {
       AVAILABLE_ENABLED,
       AVAILABLE_DISABLED,
       NOT_AVAILABLE
    }

    private TagDispatcher(Activity activity,
                          OnDiscoveredTagListener tagDiscoveredListener,
                          boolean handleUnavailableNfc,
                          boolean disableSounds,
                          boolean dispatchOnUiThread,
                          boolean broadcomWorkaround,
                          boolean noReaderMode,
                          boolean disableNdefCheck) {
        this.activity = activity;
        this.tagDiscoveredListener = tagDiscoveredListener;
        this.handleUnavailableNfc = handleUnavailableNfc;
        this.disableSounds = disableSounds;
        this.dispatchOnUiThread = dispatchOnUiThread;
        this.broadcomWorkaround = broadcomWorkaround;
        this.noReaderMode = noReaderMode;
        this.disableNdefCheck = disableNdefCheck;
    }

    public static TagDispatcher get(Activity activity,
                                    OnDiscoveredTagListener tagDiscoveredListener,
                                    boolean handleUnavailableNfc,
                                    boolean disableSounds,
                                    boolean dispatchOnUiThread,
                                    boolean broadcomWorkaround,
                                    boolean noReaderMode,
                                    boolean disableNdefCheck) {
        return new TagDispatcher(activity, tagDiscoveredListener, handleUnavailableNfc, disableSounds,
                                 dispatchOnUiThread, broadcomWorkaround, noReaderMode, disableNdefCheck);
    }

    public static TagDispatcher get(Activity activity,
                                    OnDiscoveredTagListener tagDiscoveredListener,
                                    boolean handleUnavailableNfc,
                                    boolean disableSounds,
                                    boolean dispatchOnUiThread,
                                    boolean broadcomWorkaround,
                                    boolean noReaderMode) {
        return new TagDispatcher(activity, tagDiscoveredListener, handleUnavailableNfc, disableSounds,
                                 dispatchOnUiThread, broadcomWorkaround, noReaderMode, false);
    }

    public static TagDispatcher get(Activity activity,
                                    OnDiscoveredTagListener tagDiscoveredListener,
                                    boolean handleUnavailableNfc,
                                    boolean disableSounds,
                                    boolean dispatchOnUiThread,
                                    boolean broadcomWorkaround) {
        return new TagDispatcher(activity, tagDiscoveredListener, handleUnavailableNfc, disableSounds,
                                 dispatchOnUiThread, broadcomWorkaround, false, false);
    }

    public static TagDispatcher get(Activity activity,
                                    OnDiscoveredTagListener tagDiscoveredListener,
                                    boolean handleUnavailableNfc,
                                    boolean disableSounds,
                                    boolean dispatchOnUiThread) {
        return new TagDispatcher(activity, tagDiscoveredListener, handleUnavailableNfc, disableSounds,
                                 dispatchOnUiThread, true, false, false);
    }

    public static TagDispatcher get(Activity activity,
                                    OnDiscoveredTagListener tagDiscoveredListener,
                                    boolean handleUnavailableNfc,
                                    boolean disableSounds) {
        return new TagDispatcher(activity, tagDiscoveredListener, handleUnavailableNfc, disableSounds,
                                 true, true, false, false);
    }


    public static TagDispatcher get(Activity activity,
                                    OnDiscoveredTagListener tagDiscoveredListener,
                                    boolean handleUnavailableNfc) {
        return new TagDispatcher(activity, tagDiscoveredListener, handleUnavailableNfc, false,
                                 true, true, false, false);
    }

    public static TagDispatcher get(Activity activity,
                                    OnDiscoveredTagListener tagDiscoveredListener) {
        return new TagDispatcher(activity, tagDiscoveredListener, true, false,
                                 true, true, false, false);
    }


    /** Enable exclusive NFC access for the given activity.
     * Using this method makes NFC intent filters in the AndroidManifest.xml redundant.
     * @return NfcStatus.AVAILABLE_ENABLED if NFC was available and enabled,
     * NfcStatus.AVAILABLE_DISABLED if NFC was available and disabled and
     * NfcStatus.NOT_AVAILABLE if no NFC is available on the device.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public NfcStatus enableExclusiveNfc() {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        if (adapter != null) {
            if (!adapter.isEnabled()) {
                if (handleUnavailableNfc) {
                    toastMessage("Please activate NFC and then press back");
                    activity.startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                }
                return NfcStatus.AVAILABLE_DISABLED;
            }
            if (!noReaderMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                enableReaderMode(adapter);
            } else {
                enableForegroundDispatch(adapter);
            }
            return NfcStatus.AVAILABLE_ENABLED;
        }
        if (handleUnavailableNfc) toastMessage("NFC is not available on this device");
        return NfcStatus.NOT_AVAILABLE;
    }

    /**
     * Disable exclusive NFC access for the given activity.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void disableExclusiveNfc() {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        if (adapter != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                disableReaderMode(adapter);
            } else {
                disableForegroundDispatch(adapter);
            }
        }
    }

    /** Call the TagDispatcher's listener.
     * This applies only to older Android versions (pre-KITKAT) and must
     * be called from onNewIntent(...) in the TagDispatcher's activity.
     *
     * @see <a href="http://developer.android.com/reference/android/app/Activity.html#onNewIntent%28android.content.Intent%29">Activity#onNewIntent</a>
     * @param intent The intent received by onNewIntent
     * @return true if a tag was discovered.
     */
    public boolean interceptIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(tag != null) {
            dispatchTag(tag);
            return true;
        } else {
            return false;
        }
    }

    private void dispatchTag(final Tag tag) {
        if(dispatchOnUiThread) {
            if(Looper.myLooper() != Looper.getMainLooper()) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            tagDiscoveredListener.tagDiscovered(tag);
                        }
                    });
            } else {
                tagDiscoveredListener.tagDiscovered(tag);
            }

        } else {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... aParams) {
                    tagDiscoveredListener.tagDiscovered(tag);
                    return null;
                }
            }.execute();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void enableReaderMode(NfcAdapter adapter) {
        Bundle options = new Bundle();
        if(broadcomWorkaround) {
            /* This is a work around for some Broadcom chipsets that does
             * the presence check by sending commands that interrupt the
             * processing of the ongoing command.
             */
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, DELAY_PRESENCE);
        }
        NfcAdapter.ReaderCallback callback = new NfcAdapter.ReaderCallback() {
                public void onTagDiscovered(Tag tag) {
                    dispatchTag(tag);
                }
            };
        int flags = NfcAdapter.FLAG_READER_NFC_A;
        if(disableSounds) {
            flags = flags | NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS;
        }
        if(disableNdefCheck) {
            flags = flags | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
        }
        adapter.enableReaderMode(activity, callback, flags, options);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void disableReaderMode(NfcAdapter adapter) {
        adapter.disableReaderMode(activity);
    }

    private void enableForegroundDispatch(NfcAdapter adapter) {
        /* activity.getIntent() can not be used due to issues with
         * pending intents containing extras of custom classes
         * (https://code.google.com/p/android/issues/detail?id=6822)
         */
        Intent intent = new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        enableForegroundDispatch(adapter, intent);
    }

    private void enableForegroundDispatch(NfcAdapter adapter, Intent intent) {
        if(adapter.isEnabled()) {
            PendingIntent tagIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
            adapter.enableForegroundDispatch(activity, tagIntent, new IntentFilter[]{tag},
                                             new String[][]{new String[]{IsoDep.class.getName()}});
        }
    }

    private void disableForegroundDispatch(NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    private void toastMessage(String message){
      Toast.makeText(activity.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
