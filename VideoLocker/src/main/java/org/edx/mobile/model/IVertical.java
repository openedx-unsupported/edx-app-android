package org.edx.mobile.model;


import java.util.List;

public interface IVertical extends IComponent{
    ISequential getSequential();

    List<IUnit> getUnits();

    void setUnitUrl(String url);
    String getUnitUrl();
}
