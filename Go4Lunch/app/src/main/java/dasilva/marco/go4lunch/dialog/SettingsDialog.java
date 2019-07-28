package dasilva.marco.go4lunch.dialog;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.firebase.DataBaseService;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.main.Main;
import dasilva.marco.go4lunch.ui.map.utils.GetNearbyPlacesData;

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


public class SettingsDialog implements View.OnClickListener {

    private Context context;
    private EditText editTextRadius;
    private Go4LunchService service = DI.getService();
    private DataBaseService dataBaseService = DI.getDatabaseService();
    private String deleteString = "";
    private SharedPreferences sharedPreferences;

    public SettingsDialog(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
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
                if (!deleteString.equals(context.getString(R.string.user_string))) {

                    service.getGoogleMap().clear();
                    if (editTextRadius.getText().toString().contains(context.getString(R.string.search_zone_hint))) {
                        String[] radius = editTextRadius.getText().toString().split(":");
                        service.getUser().setRadius(radius[1]);
                        service.setUserRadius(service.getUser().getRadius());
                    } else {
                        service.getUser().setRadius(editTextRadius.getText().toString());
                        service.setUserRadius(service.getUser().getRadius());
                    }
                    service.setListMarkers(null);

                    service.getMapView().getMapAsync(service.getCallback());

                    if (service.getAdapter() != null) {
                        service.getAdapter().notifyDataSetChanged();
                    }
                } else{
                    dataBaseService.deleteUserFromFireBase();
                    Intent intent = new Intent(context, Main.class);
                    context.startActivity(intent);
                    LoginManager.getInstance().logOut();
                    FirebaseAuth.getInstance().signOut();
                }
                    dialog.dismiss();

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
                    dataBaseService.removeCompleteSelectionDatabase();
                    sharedPreferences.edit().remove(context.getString(R.string.choice)).apply();
                    sharedPreferences.edit().remove(context.getString(R.string.choice_adress)).apply();
                    sharedPreferences.edit().remove(context.getString(R.string.joining_users)).apply();
                    dataBaseService.setListOfSelectedPlaces();
                    service.getMapView().getMapAsync(service.getCallback());
                }
                dialog.dismiss();
            }
        });
        confirmDialog.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                deleteString = "";
            }
        });
        confirmDialog.show();
    }

    public void getPlaces(){
        String url = context.getString(R.string.first_part_url) + service.getCurrentLocation().getLatitude()
                + "," + service.getCurrentLocation().getLatitude() +
                context.getString(R.string.radius_search_url) + service.getUser().getRadius() +
                context.getString(R.string.restaurant_type_url) + context.getString(R.string.google_api_key);
        Object dataTransfer[] = new Object[3];
        dataTransfer[0] = service.getGoogleMap();
        dataTransfer[1] = url;
        dataTransfer[2] = context;
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        getNearbyPlacesData.execute(dataTransfer);
    }


}
