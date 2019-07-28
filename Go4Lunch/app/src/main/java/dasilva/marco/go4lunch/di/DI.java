package dasilva.marco.go4lunch.di;

import dasilva.marco.go4lunch.firebase.DataBaseService;
import dasilva.marco.go4lunch.firebase.Go4LunchDataBase;
import dasilva.marco.go4lunch.service.ApiService;
import dasilva.marco.go4lunch.service.Go4LunchService;

public class DI {

    private static Go4LunchService service = new ApiService();
    private static DataBaseService databaseService = new Go4LunchDataBase();

    public static Go4LunchService getService(){
        return service;
    }

    public static Go4LunchService newInstance(){
        return new ApiService();
    }

    public static DataBaseService getDatabaseService(){
        return databaseService;
    }

    public static DataBaseService newDatabaseInstance(){
        return new Go4LunchDataBase();
    }
}
