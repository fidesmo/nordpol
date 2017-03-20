package nordpol.android;

public enum NfcDeviceDesign {
    // Needs to be an exact match to attrs.xml nfc_device
    CARD_RUBY(0),
    CARD_BLACK(1),
    USB_BLACK(2),
    USB_BLACK_FIDESMO(3);

    private int ordinal;

    NfcDeviceDesign(int ordinal){
        this.ordinal = ordinal;
    }

    static NfcDeviceDesign getNfcDeviceDesign(int ordinal) {
        return NfcDeviceDesign.values()[ordinal];
    }
}
