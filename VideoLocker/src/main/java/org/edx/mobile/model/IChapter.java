package org.edx.mobile.model;

import java.util.List;

/*
 * TODO: models to be refactored in GA+1
 */
public interface IChapter extends IComponent {

    ICourse getCourse();

    List<ISequential> getSequential();

    String getName();


    ISequential getSequentialById(String sid);
}
