package com.dynamsoft.demo.dynamsoftbarcodereaderdemo;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.dynamsoft.barcode.Barcode;

public class CustomDialog extends Dialog {

    public CustomDialog(Context context) {
        super(context);
    }

    public CustomDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        TextView tv = (TextView)findViewById(R.id.positiveButton);
        tv.callOnClick();
    }

    public static class Builder {
        private Context context;
        private String title;
        private String message;
        private Spanned spanned;
        private Barcode barcode;
        private String extraInfo;
        private String positiveButtonText;
        private String negativeButtonText;
        private View contentView;
        private DialogInterface.OnClickListener positiveButtonClickListener;
        private DialogInterface.OnClickListener negativeButtonClickListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setMessage(Spanned message) {
            this.spanned = message;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setMessage(Barcode barcode) {
            this.barcode = barcode;
            return this;
        }

        public  Builder setExtraInfo(String info) {
            this.extraInfo = info;
            return this;
        }

        /**
         * Set the Dialog message from resource
         */
        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }

        /**
         * Set the Dialog title from resource
         *
         * @param title
         * @return
         */
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }

        /**
         * Set the Dialog title from String
         *
         * @param title
         * @return
         */

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }

        /**
         * Set the positive button resource and it's listener
         *
         * @param positiveButtonText
         * @return
         */
        public Builder setPositiveButton(int positiveButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.positiveButtonText = (String) context
                    .getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setPositiveButton(String positiveButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(int negativeButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.negativeButtonText = (String) context
                    .getText(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(String negativeButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }

        public CustomDialog create(int layoutId, int themeId) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final CustomDialog dialog = new CustomDialog(context,themeId);
            View layout = inflater.inflate(layoutId, null);
            dialog.addContentView(layout, new LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            // set the dialog title
            ((TextView) layout.findViewById(R.id.title)).setText(title);
            // set the confirm button
            if (positiveButtonText != null) {
                ((TextView) layout.findViewById(R.id.positiveButton))
                        .setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    ((TextView) layout.findViewById(R.id.positiveButton))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.positiveButton).setVisibility(
                        View.GONE);
            }
            // set the cancel button
//            if (negativeButtonText != null) {
//                ((Button) layout.findViewById(R.id.negativeButton))
//                        .setText(negativeButtonText);
//                if (negativeButtonClickListener != null) {
//                    ((Button) layout.findViewById(R.id.negativeButton))
//                            .setOnClickListener(new View.OnClickListener() {
//                                public void onClick(View v) {
//                                    negativeButtonClickListener.onClick(dialog,
//                                            DialogInterface.BUTTON_NEGATIVE);
//                                }
//                            });
//                }
//            } else {
//                // if no confirm button just set the visibility to GONE
//                layout.findViewById(R.id.negativeButton).setVisibility(
//                        View.GONE);
//            }
            // set the content message
            if (message != null) {
                TextView messageView = ((TextView) layout.findViewById(R.id.message));
                messageView.setText(message);
            }
            if (spanned != null) {
                TextView messageView = ((TextView) layout.findViewById(R.id.message));
                NoUnderlineSpan mNoUnderlineSpan = new NoUnderlineSpan();
                Spannable s = new SpannableString(spanned);
                s.setSpan(mNoUnderlineSpan, 0, s.length(), Spanned.SPAN_MARK_MARK);
                messageView.setText(s);
                messageView.setMovementMethod(LinkMovementMethod.getInstance());
            }

            if (barcode != null) {
//                TextView tvBarcodeType = (TextView)layout.findViewById(R.id.tvBarcodeType);
//                TextView tvBarcodeValue = (TextView)layout.findViewById(R.id.tvBarcodeValue);
//                TextView tvBarcodeRegion = (TextView)layout.findViewById(R.id.tvBarcodeRegion);
//                tvBarcodeType.setText(barcode.formatString);
//                tvBarcodeValue.setText(barcode.displayValue);
//                if (barcode.boundingBox != null)
//                    tvBarcodeRegion.setText("{Left: " + barcode.boundingBox.left + ", Top: " + barcode.boundingBox.top + ", Width: " + barcode.boundingBox.width() + ", Height: " + barcode.boundingBox.height());
                TextView messageView = ((TextView) layout.findViewById(R.id.message));
                messageView.setText("Type: " + barcode.formatString + "\r\nValue: " + barcode.displayValue +
                        "\r\nRegion: {Left: " + barcode.boundingBox.left + ", Top: " + barcode.boundingBox.top + ", Width: " + barcode.boundingBox.width() + ", Height: " + barcode.boundingBox.height() + "}");
               if (extraInfo != null) {
                   TextView tvTime = (TextView)layout.findViewById(R.id.tvTime);
                   tvTime.setText(extraInfo + " seconds");
               }
            }
            dialog.setCanceledOnTouchOutside(false);
            dialog.setContentView(layout);
            return dialog;
        }
    }
}

 class NoUnderlineSpan extends UnderlineSpan {

    @Override
    public void updateDrawState(TextPaint ds) {
        //ds.setColor(ds.linkColor);
        ds.setUnderlineText(false);
    }
}
