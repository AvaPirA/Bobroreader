/*
 * Bobroreader is open source software, created, maintained, and shared under
 * the MIT license by Avadend Piroserpen Arts. The project includes components
 * from other open source projects which remain under their existing licenses,
 * detailed in their respective source files.
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015. Avadend Piroserpen Arts Ltd.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */
package com.avapira.bobroreader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;

public class Bober extends AppCompatActivity {

    private Drawer result = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boards_navigation_drawer);

        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.dobro_dark, null));
//        toolbar.getBackground().setAlpha(90);
        toolbar.setTitle(R.string.title_activity_bobororeader);
        toolbar.setTitleTextColor(getResources().getColor(R.color.dobro_primary_text, null));

        //Create the drawer
        result = new DrawerBuilder().withActivity(this)
                                    .withFullscreen(true).withToolbar(toolbar).withHeader(R.layout.boards_drawer_header)
                                    .addDrawerItems(new SectionDrawerItem().withName("Общее"),
                                            new PrimaryDrawerItem().withName("/b/").withIdentifier(1),
                                            new PrimaryDrawerItem().withName("/u/"),
                                            new PrimaryDrawerItem().withName("/rf/"),
                                            new PrimaryDrawerItem().withName("/dt/"),
                                            new PrimaryDrawerItem().withName("/vg/"),
                                            new PrimaryDrawerItem().withName("/r/"),
                                            new PrimaryDrawerItem().withName("/cr/"),
                                            new PrimaryDrawerItem().withName("/mu/"),
                                            new PrimaryDrawerItem().withName("/oe/"),
                                            new PrimaryDrawerItem().withName("/s/"),
                                            new PrimaryDrawerItem().withName("/w/"),
                                            new PrimaryDrawerItem().withName("/hr/"),
                                            //
                                            new SectionDrawerItem().withName("Аниме"),
                                            new PrimaryDrawerItem().withName("/a/"),
                                            new PrimaryDrawerItem().withName("/ma/"),
                                            new PrimaryDrawerItem().withName("/sw/"),
                                            new PrimaryDrawerItem().withName("/hau/"),
                                            new PrimaryDrawerItem().withName("/azu/"),
                                            //
                                            new SectionDrawerItem().withName("Аниме"),
                                            new PrimaryDrawerItem().withName("/tv/"),
                                            new PrimaryDrawerItem().withName("/cp/"),
                                            new PrimaryDrawerItem().withName("/gf/"),
                                            new PrimaryDrawerItem().withName("/bo/"),
                                            new PrimaryDrawerItem().withName("/di/"),
                                            new PrimaryDrawerItem().withName("/vn/"),
                                            new PrimaryDrawerItem().withName("/ve/"),
                                            new PrimaryDrawerItem().withName("/wh/"),
                                            new PrimaryDrawerItem().withName("/fur/"),
                                            new PrimaryDrawerItem().withName("/to/"),
                                            new PrimaryDrawerItem().withName("/bg/"),
                                            new PrimaryDrawerItem().withName("/wn/"),
                                            new PrimaryDrawerItem().withName("/slow/"),
                                            new PrimaryDrawerItem().withName("/mad/"),
                                            //
                                            new SectionDrawerItem().withName("Доброчан"),
                                            new PrimaryDrawerItem().withName("/d/"),
                                            new PrimaryDrawerItem().withName("/news/"),
                                            //add some more items to get a scrolling list
                                            new SectionDrawerItem().withName("Section 2"))
                                    .withSavedInstance(savedInstanceState)
                                    .build();

//        //USE THIS CODE TO GET A FULL TRANSPARENT STATUS BAR
//        //YOU HAVE TO UNCOMMENT THE setWindowFlag too.
//        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
//            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
//        }
//        if (Build.VERSION.SDK_INT >= 19) {
//            getWindow().getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//        }
//        if (Build.VERSION.SDK_INT >= 21) {
//            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//        }
    }

//
//    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
//        Window win = activity.getWindow();
//        WindowManager.LayoutParams winParams = win.getAttributes();
//        if (on) {
//            winParams.flags |= bits;
//        } else {
//            winParams.flags &= ~bits;
//        }
//        win.setAttributes(winParams);
//    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = result.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //handle the click on the back arrow click
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }
}
