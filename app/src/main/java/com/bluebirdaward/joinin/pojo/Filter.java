package com.bluebirdaward.joinin.pojo;

/**
 * Created by duyvu on 4/25/16.
 */
public class Filter {
    public int maxAge;
    public int minAge;
    public int gender;
    public int maxDistance;

    public Filter() {
        //Default
        maxAge = 80;
        minAge = 18;
        gender = 2; //0 men, 1 women, 2 both
        maxDistance = 2000;
    }
}
