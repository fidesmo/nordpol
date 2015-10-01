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
import android.widget.Toast;

public class TagDispatcher {
    private static final int DELAY_PRESENCE = 5000;
    
    private OnDiscoveredTagListener tagDiscoveredListener;
    private boolean handleUnavailableNfc;
    private Activity activity;

    public enum NfcStatus {
       AVAILABLE_ENABLED,
       AVAILABLE_DISABLED,
       NOT_AVAILABLE
    }

    private TagDispatcher(Activity activity,
                          OnDiscoveredTagListener tagDiscoveredListener,
                          boolean handleUnavailableNfc) {
        this.activity = activity;
        this.tagDiscoveredListener = tagDiscoveredListener;
        this.handleUnavailableNfc = handleUnavailableNfc;
    }

    public static TagDispatcher get(Activity activity,
                                    OnDiscoveredTagListener tagDiscoveredListener,
                                    boolean handleUnavailableNfc) {
        return new TagDispatcher(activity, tagDiscoveredListener, handleUnavailableNfc);
    }

    public static TagDispatcher get(Activity activity,
                                    OnDiscoveredTagListener tagDiscoveredListener) {
        return new TagDispatcher(activity, tagDiscoveredListener, true);
    }


    /** Enable exclusive NFC access for the given activity.
     * Using this method makes NFC intent filters in the AndroidManifest.xml redundant.
     * @returns NfcStatus.AVAILABLE_ENABLED if NFC was available and enabled,
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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
     * @see {@link http://developer.android.com/reference/android/app/Activity.html#onNewIntent%28android.content.Intent%29}
     * @param intent
     * @return true if a tag was discovered.
     */
    public boolean interceptIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(tag != null) {
            tagDiscoveredListener.tagDiscovered(tag);
            return true;
        } else {
            return false;
        }
    }
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void enableReaderMode(NfcAdapter adapter) {
        Bundle options = new Bundle();
        /* This is a work around for some Broadcom chipsets that does
         * the presence check by sending commands that interrupt the
         * processing of the ongoing command.
         */
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, DELAY_PRESENCE);
        NfcAdapter.ReaderCallback callback = new NfcAdapter.ReaderCallback() {
                public void onTagDiscovered(Tag tag) {
                    tagDiscoveredListener.tagDiscovered(tag);
                }
            };
        adapter.enableReaderMode(activity,
                                 callback,
                                 NfcAdapter.FLAG_READER_NFC_A |
                                 NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK |
                                 NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                                 options);
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
