package rm.spinandwin;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import rm.spinandwin.helper.H;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setUpGrid(2);
    }

    int count;

    public void onRowOneClick(View view)
    {
        if (view.getId() == R.id.transfer)
        {
            dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.transfer_layout);

            dialog.findViewById(R.id.close).setOnClickListener(this);
            dialog.findViewById(R.id.transfer).setOnClickListener(this);
            dialog.show();
        }
    }

    public void on1To36Click(View view)
    {
        H.log("clickIs",((TextView)view).getText().toString());
        H.showMessage(this,((TextView)view).getText().toString());

    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.close)
            dialog.hide();
    }

    private void setUpGrid(int j) {
        int k = j;
        LinearLayout parentLayout = findViewById(R.id.parentLayout);
        View view = View.inflate(this, R.layout.grid_elemet, null);
        LinearLayout childLayout = (LinearLayout) view;
        TextView textView;
        String string;

        if (k==2)
        {
            for (int i = 0; i < 12; i++) {
                textView = ((TextView) childLayout.getChildAt(i));
                string = textView.getText().toString().trim();

                if (string.equals("6") || string.equals("15") || string.equals("24") || string.equals("33"))
                    textView.setBackground(getResources().getDrawable(R.drawable.red_bg));
                else
                    textView.setBackground(getResources().getDrawable(R.drawable.black_bg));

                textView.setText(j + "");
                textView.setTag(j + "");
                j += 3;
            }
        }
        else
        {
            for (int i=0;i<12;i++)
            {
                textView = ((TextView) childLayout.getChildAt(i));
                string = textView.getText().toString().trim();
                if (string.equals("6") || string.equals("12") || string.equals("15") || string.equals("24") || string.equals("30") || string.equals("33"))
                    textView.setBackground(getResources().getDrawable(R.drawable.black_bg));
                else
                    textView.setBackground(getResources().getDrawable(R.drawable.red_bg));

                textView.setText(j + "");
                textView.setTag(j + "");
                j += 3;
            }
        }
        parentLayout.addView(childLayout);
        count++;
        if (count < 2)
            setUpGrid(1);

    }

    public void onBorderClick(View view)
    {
        H.log("borderClickIs",view.getTag().toString());
        H.showMessage(this,view.getTag().toString());
    }
}
