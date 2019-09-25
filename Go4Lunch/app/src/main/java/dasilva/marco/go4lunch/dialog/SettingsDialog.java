package dasilva.marco.go4lunch.dialog;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.main.Main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;



public class SettingsDialog implements View.OnClickListener {

    private Context context;
    private EditText editTextRadius;
    private Go4LunchService service = DI.getService();
    private String deleteString;

    public SettingsDialog(Context context){
        this.context = context;
    }

    public void createSettingsDialog(){
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.custom_settings_dialog, null);
        AlertDialog.Builder settingsDialog = new AlertDialog.Builder(context);

        editTextRadius = view.findViewById(R.id.radius_editText);
        Button deleteLunchBtn = view.findViewById(R.id.delete_choosed_lunch);
        Button deleteAccountBtn = view.findViewById(R.id.delete_user_button);
        deleteLunchBtn.setOnClickListener(this);
        deleteAccountBtn.setOnClickListener(this);

        settingsDialog.setView(view);
        settingsDialog.setTitle(R.string.settings);
       if (service.getUser().getRadius() != null){
           String radiusText = context.getString(R.string.search_zone_hint)+ service.getUser().getRadius();
            editTextRadius.setText(radiusText);
        } else {
            editTextRadius.setHint(R.string.radius_search);
        }
        editTextRadius.setOnClickListener(this);
        settingsDialog.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                service.getUser().setRadius(editTextRadius.getText().toString());
                service.setUserRadius(service.getUser().getRadius());
            }
        });
        settingsDialog.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        settingsDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.delete_choosed_lunch:
                deleteString = context.getString(R.string.lunch_string);
                confirmDialog();
                break;
            case R.id.delete_user_button:
                deleteString = context.getString(R.string.user_string);
                confirmDialog();
                break;
            case R.id.radius_editText:
                editTextRadius.setText("");
                editTextRadius.setHint(R.string.radius_search);
                break;
        }
    }

    private void confirmDialog(){
        final AlertDialog.Builder confirmDialog = new AlertDialog.Builder(context);
        confirmDialog.setTitle(R.string.confirm_delete);
        confirmDialog.setPositiveButton(R.string.sure_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (deleteString.equals(context.getString(R.string.lunch_string))){
                    service.removeCompleteSelectionDatabase();
                }
                if (deleteString.equals(context.getString(R.string.user_string))){
                    service.deleteUserFromFireBase();
                    LoginManager.getInstance().logOut();
                    FirebaseAuth.getInstance().signOut();
                    context.startActivity(new Intent(context, Main.class));
                    Toast.makeText(context, R.string.id, Toast.LENGTH_SHORT).show();
                }
            }
        });
        confirmDialog.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        confirmDialog.show();
    }
}
