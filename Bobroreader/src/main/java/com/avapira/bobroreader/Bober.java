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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.avapira.bobroreader.networking.BasicsSupplier;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.HashMap;
import java.util.Map;

public class Bober extends AppCompatActivity {

    private Drawer result = null;

    private static final Map<String, Integer> news = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boards_navigation_drawer);

        BasicsSupplier.getDiff(this, new Consumer<Map<String, Integer>>() {
            @Override
            public void accept(Map<String, Integer> stringIntegerMap) {
                news.putAll(stringIntegerMap);
            }
        });

        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        toolbar.setBackgroundColor(getResources().getColor(R.color.dobro_dark, null));
        toolbar.setTitle(R.string.title_activity_bobroreader);

        //Create the drawer
        result = new DrawerBuilder().withActivity(this)
                                    .withFullscreen(true)
                                    .withToolbar(toolbar)
                                    .withHeader(R.layout.boards_drawer_header)
                                    .addDrawerItems(new BoardItem("/b/", R.drawable.banners_b_b1),
                                                    new BoardItem("/u/", R.drawable.banners_u_125860969598039),
                                                    new BoardItem("/rf/", R.drawable.banners_rf_125701163950149),
                                                    new BoardItem("/dt/", R.drawable.banners_dt_125697739438064),
                                                    new BoardItem("/vg/", R.drawable.banners_vg_125709977081546),
                                                    new BoardItem("/r/", R.drawable.banners_r_125699732718180),
                                                    new BoardItem("/cr/", R.drawable.banners_cr_cr4),
                                                    new BoardItem("/mu/", R.drawable.banners_mu_125861048005976),
                                                    new BoardItem("/oe/", R.drawable.banners_wh_wh2),
                                                    new BoardItem("/s/", R.drawable.banners_s_125776130692418),
                                                    new BoardItem("/w/", R.drawable.banners_wh_wh1),
                                                    new BoardItem("/hr/", R.drawable.banners_wh_125697147834527),
                                                    //
                                                    new SectionDrawerItem().withName("Аниме"), new BoardItem("/a/"),
                                                    new BoardItem("/ma/"), new BoardItem("/sw/"),
                                                    new BoardItem("/hau/"), new BoardItem("/azu/"),
                                                    //
                                                    new SectionDrawerItem().withName("Аниме"), new BoardItem("/tv/"),
                                                    new BoardItem("/cp/"), new BoardItem("/gf/"), new BoardItem("/bo/"),
                                                    new BoardItem("/di/"), new BoardItem("/vn/"), new BoardItem("/ve/"),
                                                    new BoardItem("/wh/"), new BoardItem("/fur/"),
                                                    new BoardItem("/to/"), new BoardItem("/bg/"), new BoardItem("/wn/"),
                                                    new BoardItem("/slow/"), new BoardItem("/mad/"),
                                                    //
                                                    new SectionDrawerItem().withName("Доброчан"),
                                                    new BoardItem().withName("/d/"), new BoardItem().withName("/news/"),
                                                    new SectionDrawerItem().withName("Other stuff"),
                                                    new SettingsItem().withIdentifier(239))
                                    .withSavedInstance(savedInstanceState)
                                    .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                                        @Override
                                        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                            if (drawerItem != null) {
                                                Intent intent = null;
                                                if (drawerItem.getIdentifier() == 239) {
                                                    intent = new Intent(Bober.this, SettingsActivity.class);
                                                }
                                                if (intent != null) {
                                                    Bober.this.startActivity(intent);
                                                }
                                            }

                                            return false;
                                        }
                                    })
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

    public static final class BoardItem extends PrimaryDrawerItem {

        @Override
        public int getLayoutRes() {
            return R.layout.board_drawer_item;
        }

        public BoardItem() {
            super();

        }

        public BoardItem(String s) {
            this();
            withName(s);
        }

        public BoardItem(@StringRes int id) {
            this();
            withName(id);
        }

        public BoardItem(StringHolder s) {
            this();
            withName(s);
        }

        public BoardItem(String s, @DrawableRes int dId) {
            this(s);
            withIcon(dId);
        }

        public BoardItem(@StringRes int id, @DrawableRes int dId) {
            this(id);
            withIcon(dId);
        }

        public BoardItem(StringHolder s, @DrawableRes int dId) {
            this(s);
            withIcon(dId);
        }


        @Override
        public void onPostBindView(IDrawerItem drawerItem, View view) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(view.getContext());
            view.findViewById(R.id.drawer_subdivider)
                .setVisibility(sp.getBoolean("pref_drawer_show_subdivider", true) ? View.VISIBLE : View.INVISIBLE);

            TextView name = (TextView) view.findViewById(R.id.material_drawer_name);
            String sname = name.getText().toString();
            Integer i = Bober.news.get(sname.substring(1, sname.length() - 1));
            if (i != null) {

                View badgeContainer = view.findViewById(R.id.material_drawer_badge_container);
                TextView badge = (TextView) view.findViewById(R.id.material_drawer_badge);
                badgeContainer.setVisibility(View.VISIBLE);
                badge.setVisibility(View.VISIBLE);
                badge.setText(String.format("[%s]", i));

            }
        }
    }

    public static final class SettingsItem extends PrimaryDrawerItem {
        public SettingsItem() {
            super();
            withName("Settings");
        }

    }

}
