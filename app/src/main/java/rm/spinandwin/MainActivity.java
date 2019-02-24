package rm.spinandwin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import rm.spinandwin.helper.H;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setUpGrid(2);
    }

    int count;

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
                {
                    textView.setBackground(getResources().getDrawable(R.drawable.red_bg));
                    textView.setTag(j);
                }
                else{
                    textView.setBackground(getResources().getDrawable(R.drawable.black_bg));
                    textView.setTag(j);
                }
                textView.setText(j + "");
                j += 3;
            }
        }
        else
        {
            for (int i=0;i<12;i++)
            {
                textView = ((TextView) childLayout.getChildAt(i));
                string = textView.getText().toString().trim();
                //int tag = (int) textView.getTag();
                if (string.equals("6") || string.equals("12") || string.equals("15") || string.equals("24") || string.equals("30") || string.equals("33"))
                    textView.setBackground(getResources().getDrawable(R.drawable.black_bg));
                else
                    textView.setBackground(getResources().getDrawable(R.drawable.red_bg));

                textView.setText(j + "");
                j += 3;
            }
        }
        parentLayout.addView(childLayout);
        count++;
        if (count < 2)
            setUpGrid(1);

    }
}
