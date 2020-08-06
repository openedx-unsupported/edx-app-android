package org.humana.mobile.tta.data.model;

import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class CourseProgram {
    public List<String> getProgramArray() {
        return programArray;
    }

    public void setProgramArray(List<String> programArray) {
        this.programArray = programArray;
    }

    @SerializedName("programs")
    private List<String> programArray;
}
