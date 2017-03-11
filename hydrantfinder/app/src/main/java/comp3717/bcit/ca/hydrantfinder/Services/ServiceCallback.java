package comp3717.bcit.ca.hydrantfinder.Services;

/**
 * Created by jaydenliang on 2017-03-09.
 */

public interface ServiceCallback<T> {
    void run(T callbackParameter);
}
