package dasilva.marco.go4lunch.ui.map;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.events.DetailsEvent;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.details.DetailsActivity;

public class RviewListAdapter extends RecyclerView.Adapter<RviewListAdapter.ViewHolder> {

    List<PlaceMarker> placeMarkerList;
    Go4LunchService service;

    public RviewListAdapter(List<PlaceMarker> placeList){
        this.placeMarkerList = placeList;
        service = DI.getService();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_of_places_rv, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        final PlaceMarker placeMarker = placeMarkerList.get(i);
            viewHolder.placeName.setText(placeMarker.getName());
            viewHolder.placeAdress.setText(placeMarker.getAdress());
            viewHolder.placePartipants.setText(placeMarker.getLike());
            viewHolder.placeRate.setRating(Float.valueOf(placeMarker.getLike()));
            Location target = new Location(placeMarker.getName());
            target.setLatitude(placeMarker.getLatLng().latitude);
            target.setLongitude(placeMarker.getLatLng().longitude);
            double completeDistance = service.getDistance(service.getCurrentLocation(), target);
            String distance = String.valueOf(completeDistance);
            String[] distanceSeparator = distance.split("\\.");
            viewHolder.placeDistance.setText(distanceSeparator[0] + "m");
            if (placeMarker.getOpeninHours()) {
                viewHolder.placeHoraires.setText("Open");
                viewHolder.placeHoraires.setTextColor(viewHolder.placeHoraires.getContext().getResources().getColor(R.color.green));
            } else {
                viewHolder.placeHoraires.setText("Closed");
                viewHolder.placeHoraires.setTextColor(viewHolder.placeAdress.getContext().getResources().getColor(R.color.red));
            }
            Glide.with(viewHolder.itemView.getContext()).load(placeMarker.getPhotoUrl()).into(viewHolder.placeImage);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new DetailsEvent(placeMarker));
                String url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + service.getPlaceMarker().getId() + "&key="
                        + "AIzaSyDKZnjJaY7UQxDrXsskimpfMb_vY4s6ltc";
                new JsonTask(url).execute();
                Intent intent = new Intent(viewHolder.itemView.getContext(), DetailsActivity.class);
                viewHolder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return placeMarkerList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView placeName;
        public TextView placeAdress;
        public TextView placeHoraires;
        public TextView placeDistance;
        public TextView placePartipants;
        public RatingBar placeRate;
        public ImageView placeImage;

        public ViewHolder(View itemView) {
            super(itemView);

            placeName = (TextView) itemView.findViewById(R.id.place_name);
            placeAdress = (TextView) itemView.findViewById(R.id.place_adress);
            placeHoraires = (TextView) itemView.findViewById(R.id.horaires_place);
            placeDistance = (TextView) itemView.findViewById(R.id.place_distance);
            placePartipants = (TextView) itemView.findViewById(R.id.people_comming);
            placeRate = (RatingBar) itemView.findViewById(R.id.rating_img);
            placeImage = (ImageView) itemView.findViewById(R.id.place_image);
        }
    }
}
