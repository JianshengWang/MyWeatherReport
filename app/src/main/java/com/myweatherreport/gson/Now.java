package com.myweatherreport.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by hp-pc on 2018/11/22.
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {

        @SerializedName("txt")
        public String info;

    }

}
