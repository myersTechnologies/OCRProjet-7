package dasilva.marco.go4lunch.events;

import dasilva.marco.go4lunch.model.PlaceMarker;

public class DetailsEvent {

    public PlaceMarker placeMarker;

    public DetailsEvent(PlaceMarker placeMarker){
        this.placeMarker = placeMarker;
    }
}
