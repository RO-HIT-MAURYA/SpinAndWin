package rm.spinandwin.helper;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import rm.spinandwin.R;

public class LoadingDialog extends Dialog {

    public LoadingDialog(@NonNull Context context) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.helper_dialog_loading);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(50, 0, 0, 0)));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }//init

    public LoadingDialog(@NonNull Context context, boolean cancelable) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(cancelable);

        setContentView(R.layout.helper_dialog_loading);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(50, 0, 0, 0)));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }//init

    public LoadingDialog(@NonNull Context context, String msg, boolean cancelable) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(cancelable);

        setContentView(R.layout.helper_dialog_loading);

        ((TextView) findViewById(R.id.msg)).setText(msg);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(50, 0, 0, 0)));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }//init

    @Override
    public void show() {
        super.show();
    }

    public void show(String msg) {
        ((TextView) findViewById(R.id.msg)).setText(msg);
        super.show();
    }

    public void setVisibility(boolean isVisible) {
        if (isVisible)
            show();
        else
            dismiss();
    }

    public void setVisibility(boolean isVisible, String msg) {
        if (isVisible)
            show(msg);
        else
            dismiss();
    }

}//class