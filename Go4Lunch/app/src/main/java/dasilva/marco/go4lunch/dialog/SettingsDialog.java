package dasilva.marco.go4lunch.dialog;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.service.Go4LunchService;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class SettingsDialog implements View.OnClickListener {

    private Context context;
    private EditText editTextRadius;
    private Button deleteAccountBtn, deleteLunchBtn;
    private Go4LunchService service = DI.getService();
    private String deleteString;
    private AlertDialog.Builder settingsDialog;


    public SettingsDialog(Context context){
        this.context = context;
    }

    public void createSettingsDialog(){
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.custom_settings_dialog, null);
        settingsDialog = new AlertDialog.Builder(context);

        editTextRadius = (EditText) view.findViewById(R.id.radius_editText);
        deleteLunchBtn = (Button) view.findViewById(R.id.delete_choosed_lunch);
        deleteAccountBtn = (Button) view.findViewById(R.id.delete_user_button);
        deleteLunchBtn.setOnClickListener(this);
        deleteAccountBtn.setOnClickListener(this);

        settingsDialog.setView(view);
        settingsDialog.setTitle("Settings");
       if (service.getUser().getRadius() != null){
            editTextRadius.setText("Search Zone : " + service.getUser().getRadius());
        } else {
            editTextRadius.setHint("Radius Search");
        }
        editTextRadius.setOnClickListener(this);
        settingsDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                service.getUser().setRadius(editTextRadius.getText().toString());
                service.setUserRadius(service.getUser().getRadius());
            }
        });
        settingsDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
                deleteString = "lunch";
                confirmDialog();
                break;
            case R.id.delete_user_button:
                deleteString = "user";
                confirmDialog();
                break;
            case R.id.radius_editText:
                editTextRadius.setText("");
                editTextRadius.setHint("Radius Search");
                break;
        }
    }

    public void confirmDialog(){
        final AlertDialog.Builder confirmDialog = new AlertDialog.Builder(context);
        confirmDialog.setTitle("Are you sure to delete the selected item ?");
        confirmDialog.setPositiveButton("I'm sure", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (deleteString == "lunch"){
                    service.removeCompleteSelectionDatabase();
                }
                if (deleteString == "user"){

                }
            }
        });
        confirmDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        confirmDialog.show();
    }



}
