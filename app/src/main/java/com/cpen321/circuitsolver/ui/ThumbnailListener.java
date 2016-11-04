package com.cpen321.circuitsolver.ui;

import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by neil on 30/10/16.
 */

public class ThumbnailListener implements View.OnClickListener {

    @Override
    public void onClick(View view) {
        System.out.println(view.getTag());

        LinearLayout parentView = (LinearLayout) view.getParent();

        for(int i=0; i < parentView.getChildCount(); i++) {
            ImageView imgView = (ImageView) parentView.getChildAt(i);
            imgView.setAlpha(1f);
        }

        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            if (imageView.getTag() == HomeActivity.selectedTag){
                imageView.setAlpha(1f);
                HomeActivity.selectedTag = null;
            } else {
                imageView.setAlpha(0.65f);
                HomeActivity.selectedTag = (String) imageView.getTag();
            }

        }
    }
}
