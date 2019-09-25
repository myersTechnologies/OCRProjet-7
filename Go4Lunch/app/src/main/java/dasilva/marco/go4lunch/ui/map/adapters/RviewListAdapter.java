package dasilva.marco.go4lunch.ui.map.adapters;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import dasilva.marco.go4lunch.R;
import dasilva.marco.go4lunch.di.DI;
import dasilva.marco.go4lunch.events.DetailsEvent;
import dasilva.marco.go4lunch.model.PlaceMarker;
import dasilva.marco.go4lunch.service.Go4LunchService;
import dasilva.marco.go4lunch.ui.details.DetailsActivity;
import dasilva.marco.go4lunch.ui.map.utils.JsonTask;

public class RviewListAdapter extends RecyclerView.Adapter<RviewListAdapter.ViewHolder> {

    private List<PlaceMarker> placeMarkerList;
    private Go4LunchService service;

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
            viewHolder.placePartipants.setText(String.valueOf(placeMarker.getSelectedTimes()));
            viewHolder.placeRate.setRating(placeMarker.getLikes());
            Location target = new Location(placeMarker.getName());
            target.setLatitude(placeMarker.getLatLng().latitude);
            target.setLongitude(placeMarker.getLatLng().longitude);
            double completeDistance = service.getDistance(service.getCurrentLocation(), target);
            String distance = String.valueOf(completeDistance);
            String[] distanceSeparator = distance.split("\\.");
            String placeDistance = distanceSeparator[0] + viewHolder.itemView.getContext().getString(R.string.m);
            viewHolder.placeDistance.setText(placeDistance);
            if (placeMarker.getOpeninHours()) {
                viewHolder.placeHoraires.setText(R.string.open_true);
                viewHolder.placeHoraires.setTextColor(viewHolder.placeHoraires.getContext().getResources().getColor(R.color.green));
            } else {
                viewHolder.placeHoraires.setText(R.string.open_false);
                viewHolder.placeHoraires.setTextColor(viewHolder.placeAdress.getContext().getResources().getColor(R.color.red));
            }
            Glide.with(viewHolder.itemView.getContext()).load(placeMarker.getPhotoUrl()).into(viewHolder.placeImage);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new DetailsEvent(placeMarker));
                Context context = viewHolder.itemView.getContext();
                String url = context.getString(R.string.url_begin) + service.getPlaceMarker().getId() +
                        context.getString(R.string.and_key) + context.getString(R.string.google_api_key);
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

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView placeName;
        private TextView placeAdress;
        private TextView placeHoraires;
        private TextView placeDistance;
        private TextView placePartipants;
        private RatingBar placeRate;
        private ImageView placeImage;

        private ViewHolder(View itemView) {
            super(itemView);

            placeName =  itemView.findViewById(R.id.place_name);
            placeAdress = itemView.findViewById(R.id.place_adress);
            placeHoraires = itemView.findViewById(R.id.horaires_place);
            placeDistance = itemView.findViewById(R.id.place_distance);
            placePartipants = itemView.findViewById(R.id.people_comming);
            placeRate = itemView.findViewById(R.id.rating_img);
            placeImage = itemView.findViewById(R.id.place_image);
        }
    }
}
