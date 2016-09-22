package org.edx.mobile.test;

import org.edx.mobile.model.Filter;
import org.edx.mobile.model.course.BlockModel;
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.VideoBlockModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static junit.framework.Assert.assertTrue;

/**
 *
  */
public class CourseComponentTest {
    CourseComponent course;
    CourseComponent chapter1;
    CourseComponent chapter2;
    CourseComponent sequential1;
    CourseComponent sequential2;
    CourseComponent vertical1;
    CourseComponent vertical2;
    CourseComponent unit1;
    CourseComponent unit2;
    CourseComponent unit3;
    CourseComponent unit4;


    @Before
    public  void setUp() throws Exception {
        BlockModel bm = Mockito.mock(BlockModel.class);
        bm.type = BlockType.COURSE;
        bm.id = UUID.randomUUID().toString();

        course = new CourseComponent(bm, null);
        course.setCourseId(UUID.randomUUID().toString());

        bm = Mockito.mock(BlockModel.class);
        bm.type = BlockType.CHAPTER;
        bm.id = UUID.randomUUID().toString();
        chapter1 = new CourseComponent(bm, course);
        bm = Mockito.mock(BlockModel.class);
        bm.type = BlockType.SECTION;
        bm.id = UUID.randomUUID().toString();
        sequential1 = new CourseComponent(bm, chapter1);
        bm = Mockito.mock(BlockModel.class);
        bm.type = BlockType.VERTICAL;
        bm.id = UUID.randomUUID().toString();
        vertical1= new CourseComponent(bm, sequential1);
        bm = Mockito.mock(BlockModel.class);
        bm.type = BlockType.VIDEO;
        bm.id = UUID.randomUUID().toString();
        unit1 = new VideoBlockModel(bm, vertical1);

        bm = Mockito.mock(BlockModel.class);
        bm.type = BlockType.HTML;
        bm.id = UUID.randomUUID().toString();
        unit2 = new CourseComponent(bm, vertical1);

        bm = Mockito.mock(BlockModel.class);
        bm.type = BlockType.CHAPTER;
        bm.id = UUID.randomUUID().toString();
        chapter2 = new CourseComponent(bm, course);
        bm = Mockito.mock(BlockModel.class);
        bm.type = BlockType.SECTION;
        bm.id = UUID.randomUUID().toString();
        sequential2 = new CourseComponent(bm, chapter2);
        bm = Mockito.mock(BlockModel.class);
        bm.type = BlockType.VERTICAL;
        bm.id = UUID.randomUUID().toString();
        vertical2= new CourseComponent(bm, sequential2);
        bm = Mockito.mock(BlockModel.class);
        bm.type = BlockType.VIDEO;
        bm.id = UUID.randomUUID().toString();
        unit3 = new VideoBlockModel(bm, vertical2);

        bm = Mockito.mock(BlockModel.class);
        bm.type = BlockType.HTML;
        bm.id = UUID.randomUUID().toString();
        unit4 = new CourseComponent(bm, vertical2);

    }

    @Test
    public void testGetChildContainers() throws Exception {
        assertTrue("getChildContainers failed", course.getChildContainers().size() == 2 );
        assertTrue("getChildContainers failed", chapter1.getChildContainers().size() == 1 );
        assertTrue("getChildContainers failed", sequential1.getChildContainers().size() == 1 );
        assertTrue("getChildContainers failed", vertical1.getChildContainers().size() == 0 );
        assertTrue("getChildContainers failed", unit1.getChildContainers().size() == 0 );

    }

    @Test
    public void testGetChildLeafs() throws Exception {
        assertTrue("getChildContainers failed", course.getChildLeafs().size() == 0 );
        assertTrue("getChildContainers failed", chapter1.getChildLeafs().size() == 0 );
        assertTrue("getChildContainers failed", sequential1.getChildLeafs().size() == 0 );
        assertTrue("getChildContainers failed", vertical1.getChildLeafs().size() == 2 );
        assertTrue("getChildContainers failed", unit1.getChildLeafs().size() == 0 );

    }

    @Test
    public void testFindWithFilter() throws Exception {
        CourseComponent found = course.find(new Filter<CourseComponent>() {
            @Override
            public boolean apply(CourseComponent component) {
                return false;
            }
        });
        assertTrue("find with filter failed", found == null);

        found = course.find(new Filter<CourseComponent>() {
            @Override
            public boolean apply(CourseComponent component) {
                return component == unit1;
            }
        });
        assertTrue("find with filter failed", found == unit1);


        found = course.find(new Filter<CourseComponent>() {
            @Override
            public boolean apply(CourseComponent component) {
                return component.getType() == BlockType.VIDEO;
            }
        });

        assertTrue("find with filter failed", found == unit1);

    }


    @Test
    public void testGetVideos() throws Exception {
        assertTrue("getVideos failed", course.getVideos().size() == 2);
        assertTrue("getVideos failed", chapter1.getVideos().size() == 1 );
        assertTrue("getVideos failed", sequential1.getVideos().size() == 1 );
        assertTrue("getVideos failed", vertical1.getVideos().size() == 1 );
        assertTrue("getVideos failed", unit2.getVideos().size() == 0 );

    }

    @Test
    public void testIisLastChild() throws Exception {
        assertTrue("isLastChild failed", unit1.isLastChild() == false);
        assertTrue("isLastChild failed",  unit2.isLastChild() == true);
        assertTrue("isLastChild failed", unit3.isLastChild() == false);
        assertTrue("isLastChild failed",  unit4.isLastChild() == true);
        assertTrue("isLastChild failed", chapter1.isLastChild() == false);
        assertTrue("isLastChild failed", chapter2.isLastChild() == true);

    }

    @Test
    public void testFetchAllLeafComponents() throws Exception {
        List<CourseComponent> leaves = new ArrayList<>();
        EnumSet<BlockType> types = EnumSet.allOf(BlockType.class);

        course.fetchAllLeafComponents(leaves, types);

        assertTrue("fetchAllLeafComponents failed", leaves.size() == 4 );

        leaves.clear();
        types = EnumSet.of(BlockType.VIDEO);

        course.fetchAllLeafComponents(leaves, types);

        assertTrue("fetchAllLeafComponents failed", leaves.size() == 2 );

        leaves.clear();
        types = EnumSet.noneOf(BlockType.class);

        course.fetchAllLeafComponents(leaves, types);

        assertTrue("fetchAllLeafComponents failed", leaves.size() == 0 );


    }

    @Test
    public void testGetCourseId() throws Exception {
        String courseId = course.getCourseId();

        assertTrue("testGetCourseId failed", unit1.getCourseId().equals( courseId));
        assertTrue("testGetCourseId failed", unit2.getCourseId().equals( courseId));
        assertTrue("testGetCourseId failed", unit3.getCourseId().equals( courseId));
        assertTrue("testGetCourseId failed", unit4.getCourseId().equals( courseId));
        assertTrue("testGetCourseId failed", chapter1.getCourseId().equals( courseId));
        assertTrue("testGetCourseId failed", sequential1.getCourseId().equals( courseId));
        assertTrue("testGetCourseId failed", vertical1.getCourseId().equals( courseId));

    }


    @Test
    public void testGetPath() throws Exception {
        List<CourseComponent> path = unit1.getPath().getPath();

        assertTrue("testGetCourseId failed", path.get(0).equals( course ));
        assertTrue("testGetCourseId failed", path.get( path.size() -1).equals( unit1) );

        path = course.getPath().getPath();
        assertTrue("testGetCourseId failed", path.size() == 1 );

    }

    @Test
    public void testGetCommonAncestor() throws Exception {
        CourseComponent common = CourseComponent.getCommonAncestor(unit1, unit2);

        assertTrue("testGetCommonAncestor failed", common == vertical1 );

        common = CourseComponent.getCommonAncestor(unit2, unit3);

        assertTrue("testGetCommonAncestor failed", common == course );

        common = CourseComponent.getCommonAncestor(unit1, sequential1);

        assertTrue("testGetCommonAncestor failed", common == sequential1 );
    }
}
