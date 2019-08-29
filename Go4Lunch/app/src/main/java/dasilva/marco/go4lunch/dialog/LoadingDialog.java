package dasilva.marco.go4lunch.dialog;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.firebase.DataBaseService;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.main.Main;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;


public class LoadingDialog {

    private Context context;
    private ProgressDialog dialog;


    public LoadingDialog(Context context){
        this.context = context;
        dialog = new ProgressDialog(context);
    }

    public void showLoadingDialog(){
        dialog.setTitle("Loading...");
        dialog.setMessage("Loading, please wait...");
        dialog.show();
    }

    public void dismissLoadingDialog(){
       dialog.dismiss();
    }



}
