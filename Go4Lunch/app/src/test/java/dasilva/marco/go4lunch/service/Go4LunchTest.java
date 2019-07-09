package dasilva.marco.go4lunch.service;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.model.SelectedPlace;
import dasilva.marco.go4lunch.model.User;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class Go4LunchTest {

    private Go4LunchService service;
    private User user;
    private SelectedPlace selectedPlace;
    private PlaceMarker placeMarker;

    @Before
    public void setUp(){
        service = DI.getService();
    }
}