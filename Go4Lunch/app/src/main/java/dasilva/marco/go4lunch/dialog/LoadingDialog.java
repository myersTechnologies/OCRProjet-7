package dasilva.marco.go4lunch.dialog;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;

import dasilva.marco.go4lunch.R;

public class LoadingDialog {

    private Context context;
    private ProgressDialog dialog;


    public LoadingDialog(Context context){
        this.context = context;
        dialog = new ProgressDialog(context);
    }

    public void noConnectionOrPositionEnabledDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(context.getString(R.string.error_dialog_title));
        alertDialog.setMessage(context.getString(R.string.error_dialog_message));
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    public void showLoadingDialog(){
        dialog.setTitle(context.getString(R.string.loading_title));
        dialog.setMessage(context.getString(R.string.loading_message));
        dialog.show();
    }

    public void dismissLoadingDialog(){
       dialog.dismiss();
    }



}
