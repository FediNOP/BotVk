package ru.nop;

import org.joda.time.DateTime;

public class Time {

    private static DateTime now;
    private static DateTime threeHoursLater;

    public static void load() {


        updateTime();

    }


    private static void updateTime() {

        now = DateTime.now().minusHours(1);
        threeHoursLater = now.plusHours(2);

    }


    public static void checkTime() {

        now = DateTime.now().minusHours(1);


        if (now.isAfter(threeHoursLater)) {

            Weather.Update();
            updateTime();



        }
    }


}
