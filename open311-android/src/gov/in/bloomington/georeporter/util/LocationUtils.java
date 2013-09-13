
package gov.in.bloomington.georeporter.util;

public class LocationUtils {

    // Milliseconds per second
    public static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    public static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    public static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    public static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;    
    

    public static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 100;

    /**
     * We define accuracy as the radius of 68% confidence. In other words, if
     * you draw a circle centered at this location's latitude and longitude, and
     * with a radius equal to the accuracy, then there is a 68% probability that
     * the true location is inside the circle.
     */
    public static final float locationAccuraceThreshold = 30;

}
