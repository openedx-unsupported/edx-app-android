package org.edx.mobile.test.screenshot.test;

import android.test.InstrumentationTestCase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.testing.screenshot.Screenshot;
import com.facebook.testing.screenshot.ViewHelpers;

import org.edx.mobile.R;

public class DummyTest extends InstrumentationTestCase {

    public void testRendering() throws Throwable {
        LayoutInflater inflater = LayoutInflater.from(getInstrumentation().getTargetContext());
        View view = inflater.inflate(R.layout.fragment_course_dashboard, null, false);

        TextView courseDetail = (TextView) view.findViewById(R.id.course_detail_name);
        courseDetail.setText("Course test for screenshot");
        TextView courseDetailExtras = (TextView) view.findViewById(R.id.course_detail_extras);
        courseDetailExtras.setText("XX | xx | xxxxx");
        ImageButton shareButton = (ImageButton) view.findViewById(R.id.course_detail_share);
        shareButton.setVisibility(View.VISIBLE);

        ImageView courseImage = (ImageView) view.findViewById(R.id.header_image_view);
        courseImage.setImageResource(R.drawable.edx_map_login);

        ViewHelpers.setupView(view)
                .layout();

        Screenshot.snap(view)
                .record();
    }
}