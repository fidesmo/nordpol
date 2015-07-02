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

public class TagDispatcher {
    private NfcAdapter.ReaderCallback callback;
    private Activity activity;
    private NfcAdapter adapter;

    private TagDispatcher(Activity activity, NfcAdapter adapter,
                          NfcAdapter.ReaderCallback callback) {
        this.activity = activity;
        this.adapter = adapter;
        this.callback = callback;
    }

    public static TagDispatcher get(Activity activity, NfcAdapter adapter,
                                    NfcAdapter.ReaderCallback callback) {
        return new TagDispatcher(activity, adapter, callback);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void enableExclusiveNfc() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            enableReaderMode();
        } else {
            enableForegroundDispatch();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void disableExclusiveNfc() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            disableReaderMode();
        } else {
            disableForegroundDispatch();
        }
    }

    public boolean interceptIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(tag != null) {
            callback.onTagDiscovered(tag);
            return true;
        } else {
            return false;
        }
    }

    private void enableReaderMode() {
        Bundle options = new Bundle();
        /* This is a work around for some Broadcom chipsets that does
         * the presence check by sending commands that interrupt the
         * processing of the ongoing command.
         */
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 5000);
        adapter.enableReaderMode(activity,
                                 callback,
                                 NfcAdapter.FLAG_READER_NFC_A |
                                 NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK |
                                 NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                                 options);
    }

    private void disableReaderMode() {
        adapter.disableReaderMode(activity);
    }

    private void enableForegroundDispatch() {
        // activity.getIntent() can not be use due to issues with pending intents containg extras of custom classes (https://code.google.com/p/android/issues/detail?id=6822)
        Intent intent = new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        enableForegroundDispatch(intent);
    }

    private void enableForegroundDispatch(Intent intent) {
        if(adapter.isEnabled()) {
            PendingIntent tagIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

            adapter.enableForegroundDispatch(activity, tagIntent, new IntentFilter[]{tag},
                                             new String[][]{new String[]{IsoDep.class.getName()}});
        }
    }

    private void disableForegroundDispatch() {
        adapter.disableForegroundDispatch(activity);
    }

}
