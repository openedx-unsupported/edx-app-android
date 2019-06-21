package org.edx.mobile.base;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;

import com.google.android.gms.cast.framework.CastButtonFactory;

import org.edx.mobile.R;
import org.edx.mobile.event.NewRelicEvent;

import de.greenrobot.event.EventBus;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class BaseAppActivity extends RoboAppCompatActivity {
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().post(new NewRelicEvent(getClass().getSimpleName()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.player_fragment_menu, menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);
        return super.onCreateOptionsMenu(menu);
    }
}
