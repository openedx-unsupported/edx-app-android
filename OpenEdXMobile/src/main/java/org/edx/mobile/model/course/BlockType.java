package org.edx.mobile.model.course;

/**
 * Created by hanning on 5/21/15.
 */
public enum  BlockType {

    COURSE{ @Override public boolean isContainer() {return true;} },
    CHAPTER{ @Override public boolean isContainer() {return true;} },
    SECTION{ @Override public boolean isContainer() {return true;} },
    SEQUENTIAL{ @Override public boolean isContainer() {return true;} },
    VERTICAL{ @Override public boolean isContainer() {return true;} },
    VIDEO{ @Override public boolean isContainer() {return false;} },
    HTML{ @Override public boolean isContainer() {return false;} },
    PROBLEM{ @Override public boolean isContainer() {return false;} },
    DISCUSSION{ @Override public boolean isContainer() {return false;} },
    OTHERS{ @Override public boolean isContainer() {return false;} };

    abstract boolean isContainer();
}
