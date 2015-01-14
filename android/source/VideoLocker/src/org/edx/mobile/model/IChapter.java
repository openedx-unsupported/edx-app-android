package org.edx.mobile.model;

import java.util.List;

/*
 * TODO: models to be refactored in GA+1
 */
interface IChapter {

    ICourse getCourse();

    List<ISection> getSections();

    String getName();

}
