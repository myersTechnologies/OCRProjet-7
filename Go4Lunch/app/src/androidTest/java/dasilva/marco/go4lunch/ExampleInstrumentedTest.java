package dasilva.marco.go4lunch;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.firebase.DataBaseService;
import dasilva.marco.go4lunch.model.User;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.main.Main;
import dasilva.marco.go4lunch.ui.map.activities.MapView;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {


    @Rule
    public ActivityTestRule<Main> mapViewActivityTestRule = new ActivityTestRule<>(Main.class);

    SharedPreferences sharedPreferences;


    @Before
    public void setUp(){
        Context context = InstrumentationRegistry.getTargetContext();
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("choice", "Restaurant Aladin").apply();
    }

    @Test
    public void test(){
        assertTrue(sharedPreferences.getString("choice", null).equals("Restaurant Aladin"));
    }

    @Test
    public void getUsersFromFireBase(){
        /**

        if (service.getListMarkers() == null) {
            if (choice != null) {
                setUserChoiceToList();
                startAlarmToSendANotification(choice);
            } else {
                try {
                    checkChoiceStringToRemoveSelectedPlace();
                }catch (Exception e){}
            }
        }
         **/
    }

    @Test
    public void checkIfPlaceMarkersAreLoadedWithSuccess() {
        //check avec list place markers et markers displayed dans le map
    }

    @Test
    public void checkIfMarkerIsAddedToMaps() {
        //check avec details activity click fab et en return maps devrait l'afficher
    }



}
