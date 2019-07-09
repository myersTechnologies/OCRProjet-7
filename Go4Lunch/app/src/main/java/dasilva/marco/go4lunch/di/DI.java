package dasilva.marco.go4lunch.di;

import dasilva.marco.go4lunch.service.ApiService;
import dasilva.marco.go4lunch.service.Go4LunchService;

public class DI {

    private static Go4LunchService service = new ApiService();

    public static Go4LunchService getService(){
        return service;
    }

    public static Go4LunchService newInstance(){
        return new ApiService();
    }
}
