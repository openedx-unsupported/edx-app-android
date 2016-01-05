package org.edx.mobile.view;

import android.os.Bundle;

import org.edx.mobile.R;

public class ProductTourActivity extends ProductTour {

    @Override
    public void init(Bundle savedInstanceState) {

        // Specific texts and Fragments is more preferred. I've added those just for demonstration,
        addSlide(ProductTourFragment.newInstance("Welcome to edX!", "The leading nonprofit MOOC provider", R.drawable.boy, getResources().getColor(R.color.cyan_3)));
        addSlide(ProductTourFragment.newInstance("Free Courses", "We are providing free courses.\n", R.drawable.books, getResources().getColor(R.color.pink_2)));
        addSlide(ProductTourFragment.newInstance("GET STARTED!", "Don't wait, start learning NOW! \n", R.drawable.smile, getResources().getColor(R.color.grey_3)));

        showSkipButton(true);
        setProgressButtonEnabled(true);
    }

    @Override
    public void onSkipPressed() {
        environment.getRouter().showLaunchScreen(ProductTourActivity.this, false);
    }

    @Override
    public void onDonePressed() {
        environment.getRouter().showLaunchScreen(ProductTourActivity.this, false);
    }

    @Override
    public void onNextPressed() {
        // Do something
    }

}
