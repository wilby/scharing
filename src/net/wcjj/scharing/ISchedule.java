package net.wcjj.scharing;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: androtheos
 * Date: 8/26/12
 * Time: 8:07 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ISchedule extends java.io.Serializable {
    String FILENAME = "schedule.obj";
    String SCHEDULED_TIME = "SCHEDULED_TIME";
    String RINGER_MODE = "RINGER_MODE";
    String SCHEDULE_DOW = "SCHEDULE_DOW";

    String getSchedulesFileNameOnDisk();

    ArrayList<TreeMap<Long, Integer>> getWeek();

    TreeMap<Long, Integer> getDay(int dayOfWeek);

    /**
     * Add a ring change time and mode to the schedule.
     *
     * @param ringerMode An integer that represents the android <b>AudioManager
     * </b> ringer mode constants silent, vibrate or ring.
     * @param dayOfWeek An integer that represents a day of the week 0=Sunday -
     * 6=Saturday.
     * @param startTime The time in millis to change the ringer mode
     * @throws IllegalArgumentException thrown when the startTime is already
     * present in the schedule
     */
    void addRingSchedule(int ringerMode, int dayOfWeek, Long startTime)
            throws IllegalArgumentException;

    /**
     * Delete a scheduled ring mode change.
     *
     * @param dayOfWeek An integer that represents a day of the week 0=Sunday -
     * 6=Saturday.
     * @param startTime The time to delete from the schedule
     * @throws IllegalArgumentException if the schedule does not contain the
     * specified time.
     */
    void delRingSchedule(int dayOfWeek, Long startTime)
            throws IllegalArgumentException;

    /**
     * This method clears the entire ring schedule.
     */
    void delEntireSchedule();

    /**
     * This method changes the ring mode to change to for the time passed in
     * as startTime
     *
     * @param newRingMode An integer that represents the android <b>AudioManager
     * </b> ringer mode constants silent, vibrate or ring.
     * @param dayOfWeek An integer that represents a day of the week 0=Sunday -
     * 6=Saturday.
     * @param startTime The time in millis to change the ringer mode
     * @throws IllegalArgumentException thrown when the startTime is already
     * present in the schedule
     */
    void updateRingSchedule(int dayOfWeek, Long startTime,
                            int newRingMode) throws IllegalArgumentException;

    /**
     * Check to see if the schedule contains the indicated time.
     *
     * @param dayOfWeek An integer that represents a day of the week 0=Sunday -
     * 6=Saturday.
     * @param startTime The time to delete from the schedule
     * @return A boolean that returns true if the schedule has the indicated
     * time and false if it does not.
     */
    boolean hasTime(int dayOfWeek, Long startTime);

    int getRingerMode(int dayOfWeek, Long startTime);

    void saveSchedule(Context context) throws IOException;
}
