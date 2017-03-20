package nordpol.android;

import android.content.res.TypedArray;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import nordpol.android.R;

/**
 * A custom view to help users position NFC device as well as keep it around
 * while transferring. Using the full flow with three states (positioning,
 * transaction and finished-done or finished-fail) is recommended for longer transactions. For
 * shorter transactions (such as UID lookup) only the first state should be
 * used.
 *
 * To use add NfcGuideView to your layout, like so:
 * <pre>
 * {@code
 * <nordpol.android.NfcGuideView
 *     android:id="@+id/nfc_guide_view"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"/>
 * }
 * </pre>
 *
 * To choose between the different NFC device graphics add the following line:
 * For a black card: app:nfc_device="card_black"
 * For a ruby card: app:nfc_device="card_ruby"
 * For a black USB: app:nfc_device="usb_black"
 * For a black USB with a Fidesmo logo: app:nfc_device="usb_black_fidesmo"
 *
 * After setting up the View as usually in Java you can call the
 * setCurrentStatus method on it to change between different NFC transaction
 * states. The available states are the following:
 * NfcGuideViewStatus.STARTING_POSITION
 * NfcGuideViewStatus.TRANSFERRING
 * NfcGuideViewStatus.DONE
 * NfcGuideViewStatus.FAIL

 * Call setCurrentStatus directly on the view, like so:
 * nfcGuideView.setCurrentStatus(NfcGuideView.NfcGuideViewStatus.TRANSFERRING);
 */
public class NfcGuideView extends RelativeLayout {
    private static final float TRANSACTION_ITEM_ICON_X_OFFSET_DP = 25.0f;
    private int guideItemsStartDistance = 50;
    private static final int ANIMATION_DURATION_SHORT = 100;
    private static final int ANIMATION_DURATION_MEDIUM = 200;
    private static final int VALUE_ANIMATOR_DEFAULT_START_DELAY = 0;
    private static final int START_DELAY_LONG = 500;

    private int mNfcGuidePhoneWidth = 0;
    private int nfcGuidePhoneHeight = 0;
    private int mNfcGuideHandWidth = 0;
    private int mRootViewWidth = 0;
    private int mRootViewHeight = 0;

    private View mRootView;
    private ImageView mNfcGuidePhone;
    private ImageView mNfcGuideHand;
    private ProgressBar mProgressBar;
    private ImageView mStatusPositive;
    private ImageView mStatusNegative;
    private ImageView nfcGuideViewTransactionItemIcon;

    public enum NfcGuideViewStatus {
        STARTING_POSITION,
        TRANSFERRING,
        DONE,
        FAIL
    }

    public NfcGuideView(Context context, AttributeSet attrs) {
        super(context, attrs);

        guideItemsStartDistance = getResources().getInteger(R.integer.GUIDE_ITEMS_START_DISTANCE);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(
          attrs,
          R.styleable.NfcGuideView,
          0, 0);

        int nfcDeviceDesign = 0;
        Drawable transactionItemIcon;

        try {
            nfcDeviceDesign = typedArray.getInteger(R.styleable.NfcGuideView_nfc_device, 0);
            transactionItemIcon = typedArray.getDrawable(R.styleable.NfcGuideView_transaction_item_icon);
        } finally {
          typedArray.recycle();
        }

        LayoutInflater mLayoutInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = mLayoutInflator.inflate(R.layout.nfc_guide_view, this, true);

        mProgressBar = (ProgressBar) mRootView.findViewById(R.id.nfc_guide_view_progress_bar);
        mStatusPositive = (ImageView) mRootView.findViewById(R.id.nfc_guide_view_status_positive);
        mStatusNegative = (ImageView) mRootView.findViewById(R.id.nfc_guide_view_status_negative);
        mNfcGuidePhone = (ImageView) mRootView.findViewById(R.id.nfc_guide_view_phone);
        mNfcGuideHand = (ImageView) mRootView.findViewById(R.id.nfc_guide_view_hand);
        nfcGuideViewTransactionItemIcon = (ImageView) mRootView.findViewById(R.id.nfc_guide_view_transaction_item_icon);

        switch(nfcDeviceDesign) {
        case 0:
            mNfcGuideHand.setImageDrawable(getResDrawable(R.drawable.nfc_guide_view_hand_holding_card_ruby));
            break;
        case 1:
            mNfcGuideHand.setImageDrawable(getResDrawable(R.drawable.nfc_guide_view_hand_holding_card_black));
            break;
        case 2:
            mNfcGuideHand.setImageDrawable(getResDrawable(R.drawable.nfc_guide_view_hand_holding_usb_black));
            break;
        case 3:
            mNfcGuideHand.setImageDrawable(getResDrawable(R.drawable.nfc_guide_view_hand_holding_usb_black_fidesmo));
            break;
        }

        if (transactionItemIcon != null) {
            nfcGuideViewTransactionItemIcon.setImageDrawable(transactionItemIcon);
            nfcGuideViewTransactionItemIcon.setVisibility(View.VISIBLE);
        } else {
            nfcGuideViewTransactionItemIcon.setVisibility(View.GONE);
        }

        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    mRootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mRootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                mRootViewWidth = mRootView.getWidth();
                mRootViewHeight = mRootView.getHeight();
                nfcGuideStartPositions();
            }
        });

        mNfcGuidePhone.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    mNfcGuidePhone.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mNfcGuidePhone.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                mNfcGuidePhoneWidth = mNfcGuidePhone.getWidth();
                nfcGuidePhoneHeight = mNfcGuidePhone.getHeight();
                nfcGuideStartPositions();
            }
        });

        mNfcGuideHand.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    mNfcGuideHand.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mNfcGuideHand.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                mNfcGuideHandWidth = mNfcGuideHand.getWidth();
                nfcGuideStartPositions();
            }
        });
    }

    private void nfcGuideStartPositions() {
        mProgressBar
            .animate()
            .alpha(0f);
        mStatusPositive
            .animate()
            .alpha(0f);
        mStatusNegative
            .animate()
            .alpha(0f);

        mNfcGuideHand.animate()
            .x(getHandXStart())
            .alpha(1f)
            .setInterpolator(new AccelerateDecelerateInterpolator());

        mNfcGuidePhone
            .animate()
            .x(getPhoneXStart())
            .alpha(1f)
            .setInterpolator(new AccelerateDecelerateInterpolator());

        nfcGuideViewTransactionItemIcon
                .animate()
                .x(getTransactionItemIconXStart())
                .alpha(1f)
                .setInterpolator(new AccelerateDecelerateInterpolator());

        nfcGuideViewTransactionItemIcon.setY(mRootViewHeight/7);
    }

    private int getHandXStart() {
        return Math.max(getViewCenter()-mNfcGuideHandWidth + guideItemsStartDistance, 0);
    }

    private int getPhoneXStart() {
        return Math.min(getViewCenter() - guideItemsStartDistance, mRootViewWidth - mNfcGuidePhoneWidth);
    }

    private int getTransactionItemIconXStart() {
        return (int) (getPhoneXStart() + getPixelsFromDp(TRANSACTION_ITEM_ICON_X_OFFSET_DP));
    }

    private int getViewCenter(){
        return mRootViewWidth/2;
    }

    private int getHandXTransferring() {
        int centerOffsetXHand = guideItemsStartDistance + guideItemsStartDistance/2;
        return Math.max(getViewCenter() - mNfcGuideHandWidth + centerOffsetXHand, 0);
    }

    private int getPhoneXTransferring() {
        int centerOffsetXPhone = guideItemsStartDistance + guideItemsStartDistance/2;
        return Math.min(getViewCenter() - centerOffsetXPhone, mRootViewWidth - mNfcGuidePhoneWidth);
    }

    private int getTransactionItemIconXTransferring() {
        return (int) (getPhoneXTransferring() + getPixelsFromDp(TRANSACTION_ITEM_ICON_X_OFFSET_DP));
    }

    private void nfcGuideTransferring() {
        mProgressBar
            .animate()
            .alpha(1f)
            .setStartDelay(START_DELAY_LONG);
        mStatusPositive
            .animate()
            .alpha(0f);
        mStatusNegative
            .animate()
            .alpha(0f);

        mNfcGuideHand
            .animate()
            .x(getHandXTransferring())
            .alpha(1f)
            .setInterpolator(new AccelerateDecelerateInterpolator());

        mNfcGuidePhone
            .animate()
            .x(getPhoneXTransferring())
            .alpha(1f)
            .setInterpolator(new AccelerateDecelerateInterpolator());

        nfcGuideViewTransactionItemIcon
                .animate()
                .x(getTransactionItemIconXTransferring())
                .alpha(1f)
                .setInterpolator(new AccelerateDecelerateInterpolator());
    }

    private void nfcGuideDone() {
        mProgressBar
            .animate()
            .alpha(0f)
            .setDuration(ANIMATION_DURATION_SHORT)
            .setStartDelay(VALUE_ANIMATOR_DEFAULT_START_DELAY);
        mStatusPositive
            .animate()
            .alpha(1f)
            .setStartDelay(START_DELAY_LONG);
        mStatusNegative
            .animate()
            .alpha(0f);

        blowUp();
    }

    private void nfcGuideFailed() {
        mProgressBar
            .animate()
            .alpha(0f)
            .setDuration(ANIMATION_DURATION_SHORT)
            .setStartDelay(VALUE_ANIMATOR_DEFAULT_START_DELAY);
        mStatusPositive
            .animate()
            .alpha(0f);
        mStatusNegative
            .animate()
            .alpha(1f)
            .setStartDelay(START_DELAY_LONG);

        blowUp();
    }

    private void blowUp() {
        mNfcGuideHand
            .animate()
            .x(0)
            .alpha(0f)
            .setInterpolator(new AccelerateInterpolator())
            .setDuration(ANIMATION_DURATION_MEDIUM);

        int phonePosition = mRootViewWidth-mNfcGuidePhoneWidth;
        mNfcGuidePhone
            .animate()
            .x(phonePosition)
            .alpha(0f)
            .setInterpolator(new AccelerateInterpolator())
            .setDuration(ANIMATION_DURATION_MEDIUM);

        nfcGuideViewTransactionItemIcon
                .animate()
                .x(phonePosition)
                .alpha(0f)
                .setInterpolator(new AccelerateInterpolator())
                .setDuration(ANIMATION_DURATION_MEDIUM);
    }

    /**
     * Set the NfcGuideViewStatus that the NfcGuideView should have
     *
     * @param nfcGuideViewStatus The NfcGuideViewStatus to be set
     */
    public void setCurrentStatus(NfcGuideViewStatus nfcGuideViewStatus){
        switch (nfcGuideViewStatus){
        case STARTING_POSITION:
            nfcGuideStartPositions();
            break;
        case TRANSFERRING:
            nfcGuideTransferring();
            break;
        case DONE:
            nfcGuideDone();
            break;
        case FAIL:
            nfcGuideFailed();
            break;
        }
    }

    /**
     * Set the Drawable icon that is shown in in the transferring device graphic
     *
     * @param drawable The Drawable to be set
     */
    public void setTransactionItemIcon(Drawable drawable) {
        if (drawable != null) {
            nfcGuideViewTransactionItemIcon.setImageDrawable(drawable);
            nfcGuideViewTransactionItemIcon.setVisibility(View.VISIBLE);
        } else {
            nfcGuideViewTransactionItemIcon.setVisibility(View.GONE);
        }
    }

    @SuppressWarnings("deprecation")
    private Drawable getResDrawable(int resourceId){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return getResources().getDrawable(resourceId, null);
        } else {
            return getResources().getDrawable(resourceId);
        }
    }

    private int getPixelsFromDp(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
