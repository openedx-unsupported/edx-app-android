package org.edx.mobile.whatsnew;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;

import org.edx.mobile.BuildConfig;
import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentWhatsNewBinding;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.util.FileUtil;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.WhatsNewUtil;
import org.edx.mobile.view.custom.IndicatorController;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WhatsNewFragment extends BaseFragment {
    private final Logger logger = new Logger(getClass().getName());

    @Inject
    protected IEdxEnvironment environment;

    private FragmentWhatsNewBinding binding;

    private IndicatorController indicatorController;

    private int noOfPages = 0;
    private int totalPagesViewed = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_whats_new, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final CharSequence title = ResourceUtil.getFormattedString(getResources(),
                R.string.whats_new_title, "version_number", BuildConfig.VERSION_NAME);

        // Setting activity's title for accessibility
        getActivity().setTitle(title);
        binding.screenTitle.setText(title);

        initViewPager();
        initButtons();
        initProgressIndicator();

        final Map<String, String> map = new HashMap<>();
        map.put(Analytics.Keys.APP_VERSION, BuildConfig.VERSION_NAME);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.WHATS_NEW, null,
                null, map);
    }

    private void initViewPager() {
        try {
            final String whatsNewJson = FileUtil.loadTextFileFromResources(getContext(), R.raw.whats_new);
            final Type type = new TypeToken<List<WhatsNewModel>>() {
            }.getType();
            final List<WhatsNewModel> whatsNewModels = new Gson().fromJson(whatsNewJson, type);
            final List<WhatsNewItemModel> items = WhatsNewUtil.getWhatsNewItems(BuildConfig.VERSION_NAME, whatsNewModels);
            if (items == null) {
                getActivity().finish();
                return;
            }
            noOfPages = items.size();
            binding.viewPager.setAdapter(new WhatsNewAdapter(getFragmentManager(), items));
            binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    indicatorController.selectPosition(position);
                    if (position == noOfPages - 1) {
                        binding.nextBtn.setText(R.string.view_my_courses);
                    } else {
                        binding.nextBtn.setText(R.string.label_next);
                    }

                    final int pageNumber = position + 1;
                    if (pageNumber >= totalPagesViewed) {
                        totalPagesViewed = pageNumber;
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        } catch (IOException e) {
            // Submit crash report and end the activity
            logger.error(e, true);
            getActivity().finish();
        }
    }

    private void initButtons() {
        binding.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                environment.getAnalyticsRegistry().trackWhatsNewClosed(BuildConfig.VERSION_NAME,
                        totalPagesViewed, binding.viewPager.getCurrentItem() + 1, noOfPages);
                getActivity().finish();
            }
        });

        binding.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check if last page then end what's new activity
                if (binding.viewPager.getCurrentItem() == noOfPages - 1) {
                    environment.getAnalyticsRegistry().trackWhatsNewSeen(BuildConfig.VERSION_NAME, noOfPages);
                    getActivity().finish();
                } else {
                    binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
                }
            }
        });
    }

    private void initProgressIndicator() {
        indicatorController = new IndicatorController();
        binding.indicatorContainer.addView(indicatorController.newInstance(getContext()));
        indicatorController.setCount(noOfPages);
    }

    private class WhatsNewAdapter extends FragmentStatePagerAdapter {
        @NonNull
        final List<WhatsNewItemModel> list;

        public WhatsNewAdapter(@NonNull FragmentManager fm, @NonNull List<WhatsNewItemModel> list) {
            super(fm);
            this.list = list;
        }

        @Override
        public Fragment getItem(int position) {
            return WhatsNewItemFragment.newInstance(list.get(position));
        }

        @Override
        public int getCount() {
            return list.size();
        }
    }
}
