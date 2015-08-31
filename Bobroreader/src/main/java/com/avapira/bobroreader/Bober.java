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
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import com.avapira.bobroreader.networking.BasicsSupplier;
import com.avapira.bobroreader.networking.PersistentCookieStore;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Map;

public class Bober extends AppCompatActivity {

    private volatile Drawer drawer = null;

    private static class ShortSectionDivider extends SectionDrawerItem {
        @Override
        @LayoutRes
        public int getLayoutRes() {
            return R.layout.section_divider;
        }
    }

    private class BoardSwitcher implements Drawer.OnDrawerItemClickListener {
        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            if (drawerItem != null) {
                Intent intent = null;
                switch (drawerItem.getIdentifier()) {
                    case 239:
                        intent = new Intent(Bober.this, SettingsActivity.class);
                        break;
                    case 30:
                        getFragmentManager().beginTransaction()
                                            .replace(R.id.frame_container, new CardViewFragment())
                                            .commit();
                }
                if (intent != null) {
                    Bober.this.startActivity(intent);
                }
            }

            return false;
        }
    }

    private void bindCookies() {
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(this.getApplicationContext()),
                                                        CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        CookieHandler.setDefault(cookieManager);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindCookies();
        setContentView(R.layout.activity_boards_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = generateBuilder(savedInstanceState, toolbar).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        updateDrawerDiff();
//        BasicsSupplier.getUser(this, new Consumer<HanabiraUser>() {
//            @Override
//            public void accept(HanabiraUser user) {
//                ((TextView) findViewById(R.id.txtLabel)).setText(user.toString());
//            }
//        });
    }

    public void updateDrawerDiff() {
        BasicsSupplier.getDiff(this, new Consumer<Map<String, Integer>>() {
            @Override
            public void accept(final Map<String, Integer> diff) {
                System.out.println("accept");
                for (IDrawerItem item : drawer.getAdapter().getDrawerItems()) {
                    if (item instanceof BoardItem) {
                        BoardItem i = (BoardItem) item;
                        String boardKey = shortenizeBoardName(i.getName().getText());
                        i.withBadge(String.format("[%s]", diff.get(boardKey)));
                    }
                }
                drawer.getAdapter().notifyDataSetChanged();
            }
        });
    }

    private DrawerBuilder generateBuilder(Bundle savedInstanceState, Toolbar toolbar) {
        return new DrawerBuilder().withActivity(Bober.this)
                                  .withFullscreen(true)
                                  .withToolbar(toolbar)
                                  .withHeaderDivider(false)
                                  .withHeader(R.layout.boards_drawer_header)
                                  .addDrawerItems(new ShortSectionDivider().withName("Общее"),
                                                  new BoardItem("/b/", R.drawable.banners_b_b1),
                                                  new BoardItem("/u/", R.drawable.banners_u_125860969598039),
                                                  new BoardItem("/rf/",
                                                                R.drawable.banners_rf_125701163950149).withIdentifier(
                                                          30),
                                                  new BoardItem("/dt/", R.drawable.banners_dt_125697739438064),
                                                  new BoardItem("/vg/", R.drawable.banners_vg_125709977081546),
                                                  new BoardItem("/r/", R.drawable.banners_r_125699732718180),
                                                  new BoardItem("/cr/", R.drawable.banners_cr_cr4),
                                                  new BoardItem("/mu/", R.drawable.banners_mu_125861048005976),
                                                  new BoardItem("/oe/", R.drawable.banners_empty),
                                                  new BoardItem("/s/", R.drawable.banners_s_125776130692418),
                                                  new BoardItem("/w/", R.drawable.banners_empty),
                                                  new BoardItem("/hr/", R.drawable.banners_empty),
                                                  //
                                                  new ShortSectionDivider().withName("Аниме"),
                                                  new BoardItem("/a/", R.drawable.banners_a_125768405443972),
                                                  new BoardItem("/ma/", R.drawable.banners_ma_125860969613262),
                                                  new BoardItem("/sw/", R.drawable.banners_sw_125861045421667),
                                                  new BoardItem("/hau/", R.drawable.banners_hau_125861045418626),
                                                  new BoardItem("/azu/", R.drawable.banners_empty),
                                                  //
                                                  new ShortSectionDivider().withName("На пробу"),
                                                  new BoardItem("/tv/", R.drawable.banners_tv_55555),
                                                  new BoardItem("/cp/", R.drawable.banners_cp_g125788239756657),
                                                  new BoardItem("/gf/", R.drawable.banners_gf_125860979571217),
                                                  new BoardItem("/bo/", R.drawable.banners_empty),
                                                  new BoardItem("/di/", R.drawable.banners_di_125762259407262),
                                                  new BoardItem("/vn/", R.drawable.banners_vn_125861005475361),
                                                  new BoardItem("/ve/", R.drawable.banners_ve_125698880498448),
                                                  new BoardItem("/wh/", R.drawable.banners_wh_125861075646865),
                                                  new BoardItem("/fur/", R.drawable.banners_fur_125861026701646),
                                                  new BoardItem("/to/", R.drawable.banners_to_125861045424732),
                                                  new BoardItem("/bg/", R.drawable.banners_bg_125861578033434),
                                                  new BoardItem("/wn/", R.drawable.banners_wn_125861005478345),
                                                  new BoardItem("/slow/", R.drawable.banners_slow_slow_4),
                                                  new BoardItem("/mad/", R.drawable.banners_mad_mad),
                                                  //
                                                  new ShortSectionDivider().withName("Доброчан"),
                                                  new BoardItem("/d/", R.drawable.banners_d_125711152029591),
                                                  new BoardItem("/news/", R.drawable.banners_news_125710395977840),
                                                  new SectionDrawerItem().withName("Other stuff"),
                                                  new SettingsItem().withIdentifier(239).withSelectable(false))
                                  .withSavedInstance(savedInstanceState)
                                  .withOnDrawerItemClickListener(new BoardSwitcher());
    }

    private String shortenizeBoardName(String name) {
        return name.substring(1, name.length() - 1);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = drawer.saveInstanceState(outState);
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
        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
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

    }

    public static final class SettingsItem extends PrimaryDrawerItem {
        public SettingsItem() {
            super();
            withName("Settings");
        }

    }

}
