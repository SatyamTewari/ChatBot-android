package com.example.slackchatbot.Class;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidadvance.topsnackbar.TSnackbar;
import com.example.slackchatbot.R;

public class CustomSnackBar {

    public static void showSnackBar(String message, Activity activity) {
        try {
            TSnackbar snackbar = TSnackbar.make(activity.findViewById(android.R.id.content), message, TSnackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.TRANSPARENT);
            TextView textView = (TextView) snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
            textView.setVisibility(View.INVISIBLE);
            View view = activity.getLayoutInflater().inflate(R.layout.my_snackbar, null);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView textViewTop = view.findViewById(R.id.text);
            textViewTop.setText(message);
            com.androidadvance.topsnackbar.TSnackbar.SnackbarLayout layout = (com.androidadvance.topsnackbar.TSnackbar.SnackbarLayout) snackbar.getView();
            layout.addView(view);
            snackbar.show();
        } catch (Exception e) {
            Log.e("SnackBar Error", "showSnackBar : "+e.getMessage());
        }
    }
}
