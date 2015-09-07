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

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import com.avapira.bobroreader.hanabira.Hanabira;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.hanabira.entity.HanabiraUser;
import com.avapira.bobroreader.networking.PersistentCookieStore;
import com.avapira.bobroreader.util.Consumer;
import com.avapira.bobroreader.util.TestCardViewFragment;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import org.joda.time.LocalDateTime;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Map;

public class Bober extends AppCompatActivity {
    static {
        // Build the local caches inside Joda Time immediately instead of lazily
        new LocalDateTime();
    }

    private Drawer drawer = null;

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
                                            .replace(R.id.frame_container, new TestCardViewFragment())
                                            .commit();
                        break;
                    case 261:
                        getFragmentManager().beginTransaction()
                                            .replace(R.id.frame_container, new ThreadFragment())
                                            .commit();
                        break;
                    case 566:
                        String boardKey = HanabiraBoard.Info.cutSlashes(((BoardDrawerItem) drawerItem).getName().getText
                                ());
                        Fragment boardFragment = new BoardFragment();
                        Bundle b = new Bundle();
                        b.putString("board", boardKey);
                        boardFragment.setArguments(b);
                        getFragmentManager().beginTransaction().replace(R.id.frame_container, boardFragment).commit();
                        break;
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
        HanabiraBoard.Info.loadBoardsInfo(rawJsonToString(getResources(), R.raw.boards));
        Hanabira.init(this);
        setContentView(R.layout.activity_boards_navigation_drawer);
        Bober.this.bindCookies();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = generateBuilder(savedInstanceState, toolbar).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // todo show pretty /news/ index page
//        updateDrawerDiff();
//        showUserInfo();
    }

    private void showUserInfo() {
        Hanabira.getFlower().getUser(new Consumer<HanabiraUser>() {
            @Override
            public void accept(HanabiraUser user) {
                TextView tv = (TextView) findViewById(R.id.user_text);
                tv.setText(user.toString());
            }
        });
    }

    public static String rawJsonToString(Resources res, @RawRes int resId) {
        String name = res.getResourceName(resId);
        BufferedInputStream bis = new BufferedInputStream(res.openRawResource(resId));
        try {
            byte[] bytes = new byte[bis.available()];
            int bytesRead = bis.read(bytes);
            Log.i("Bober#rawJsonToString", String.format("Streaming raw file %s: %s bytes read", name, bytesRead));
            return new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void updateDrawerDiff() {
        Hanabira.getFlower().getDiff(new Consumer<Map<String, Integer>>() {
            @Override
            public void accept(final Map<String, Integer> diff) {
                for (IDrawerItem item : drawer.getAdapter().getDrawerItems()) {
                    if (item instanceof BoardDrawerItem) {
                        BoardDrawerItem i = (BoardDrawerItem) item;
                        String boardKey = shortenizeBoardName(i.getName().getText());
                        i.withBadge(String.format("[%s]", diff.get(boardKey)));
                    }
                }
                drawer.getAdapter().notifyDataSetChanged();
            }
        });
    }

    private DrawerBuilder generateBuilder(Bundle savedInstanceState, Toolbar toolbar) {

        ImageView image = (ImageView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.drawer_header_horo,null);
        Drawable horo = getDrawable(R.drawable.acc39fc867f_transparent);
        image.setImageDrawable(horo);
        return new DrawerBuilder().withActivity(Bober.this)
                                  .withFullscreen(true)
                                  .withToolbar(toolbar)
                                  .withHeaderDivider(false)
                                  .withHeader(image)
                                  .addDrawerItems(new ShortSectionDivider().withName("Общее"),
                                                  new BoardDrawerItem("/b/", R.drawable.banners_b_b1).withIdentifier(
                                                          261),
                                                  new BoardDrawerItem("/u/", R.drawable.banners_u_125860969598039)
                                                          .withIdentifier(566),
                                                  new BoardDrawerItem("/rf/",
                                                                      R.drawable.banners_rf_125701163950149)
                                                          .withIdentifier(30),
                                                  new BoardDrawerItem("/dt/", R.drawable.banners_dt_125697739438064),
                                                  new BoardDrawerItem("/vg/", R.drawable.banners_vg_125709977081546),
                                                  new BoardDrawerItem("/r/", R.drawable.banners_r_125699732718180),
                                                  new BoardDrawerItem("/cr/", R.drawable.banners_cr_cr4),
                                                  new BoardDrawerItem("/mu/", R.drawable.banners_mu_125861048005976),
                                                  new BoardDrawerItem("/oe/", R.drawable.banners_empty),
                                                  new BoardDrawerItem("/s/", R.drawable.banners_s_125776130692418),
                                                  new BoardDrawerItem("/w/", R.drawable.banners_empty),
                                                  new BoardDrawerItem("/hr/", R.drawable.banners_empty),
                                                  //
                                                  new ShortSectionDivider().withName("Аниме"),
                                                  new BoardDrawerItem("/a/", R.drawable.banners_a_125768405443972),
                                                  new BoardDrawerItem("/ma/", R.drawable.banners_ma_125860969613262),
                                                  new BoardDrawerItem("/sw/", R.drawable.banners_sw_125861045421667),
                                                  new BoardDrawerItem("/hau/", R.drawable.banners_hau_125861045418626),
                                                  new BoardDrawerItem("/azu/", R.drawable.banners_empty),
                                                  //
                                                  new ShortSectionDivider().withName("На пробу"),
                                                  new BoardDrawerItem("/tv/", R.drawable.banners_tv_55555),
                                                  new BoardDrawerItem("/cp/", R.drawable.banners_cp_g125788239756657),
                                                  new BoardDrawerItem("/gf/", R.drawable.banners_gf_125860979571217),
                                                  new BoardDrawerItem("/bo/", R.drawable.banners_empty),
                                                  new BoardDrawerItem("/di/", R.drawable.banners_di_125762259407262),
                                                  new BoardDrawerItem("/vn/", R.drawable.banners_vn_125861005475361),
                                                  new BoardDrawerItem("/ve/", R.drawable.banners_ve_125698880498448),
                                                  new BoardDrawerItem("/wh/", R.drawable.banners_wh_125861075646865),
                                                  new BoardDrawerItem("/fur/", R.drawable.banners_fur_125861026701646),
                                                  new BoardDrawerItem("/to/", R.drawable.banners_to_125861045424732),
                                                  new BoardDrawerItem("/bg/", R.drawable.banners_bg_125861578033434),
                                                  new BoardDrawerItem("/wn/", R.drawable.banners_wn_125861005478345),
                                                  new BoardDrawerItem("/slow/", R.drawable.banners_slow_slow_4),
                                                  new BoardDrawerItem("/mad/", R.drawable.banners_mad_mad),
                                                  //
                                                  new ShortSectionDivider().withName("Доброчан"),
                                                  new BoardDrawerItem("/d/", R.drawable.banners_d_125711152029591),
                                                  new BoardDrawerItem("/news/",
                                                                      R.drawable.banners_news_125710395977840),
                                                  new SectionDrawerItem().withName("Other stuff"),
                                                  new SettingsDrawerItem().withIdentifier(239).withSelectable(false))
                                  .withSavedInstance(savedInstanceState)
                                  .withOnDrawerItemClickListener(new BoardSwitcher())
                                  .withOnDrawerListener(new Drawer.OnDrawerListener() {

                                      private static final float MENU_CLOSING_THRESHOLD = 1f / 3f;
                                      private static final float MENU_SHOWING_THRESHOLD = 1f - MENU_CLOSING_THRESHOLD;

                                      @Override
                                      public void onDrawerOpened(View drawerView) {
                                          Bober.this.invalidateOptionsMenu();
                                          sliderThinksClosing = null;
                                      }

                                      @Override
                                      public void onDrawerClosed(View drawerView) {
                                          Bober.this.invalidateOptionsMenu();
                                          sliderThinksClosing = null;
                                      }

                                      private float previousOffset;

                                      @Override
                                      public void onDrawerSlide(View arg0, float slideOffset) {
//                                          sliderThinksClosing = drawer.isDrawerOpen();
//                                          if (!sliderThinksClosing) {                    // Opening...
//                                              if (slideOffset > MENU_SHOWING_THRESHOLD) {// ..more half completed...
//                                                  if (slideOffset > previousOffset) {    // ..from this moment
//                                                      sliderHide = true;
//                                                      invalidateOptionsMenu();
//                                                  }                                      // .. else already hidden
//                                              } else {                                   // ..less half completed...
//                                                  if (slideOffset < previousOffset) {    // ..from this moment
//                                                      sliderHide = false;
//                                                      invalidateOptionsMenu();
//                                                  }                                      // else already shown
//                                              }
//                                          } else {                                       // Closing..
//                                              if (slideOffset > MENU_CLOSING_THRESHOLD) {// ..less half completed..
//                                                  if (slideOffset > previousOffset) {    // ..from now
//                                                      sliderHide = true;
//                                                      invalidateOptionsMenu();
//                                                  }                                      // else already hidden
//                                              } else {                                   // ..more half completed..
//                                                  if (slideOffset < previousOffset) {    // ..from now
//                                                      sliderHide = false;
//                                                      invalidateOptionsMenu();
//                                                  }                                      // else already shown
//                                              }
//                                          }
//                                          previousOffset = slideOffset;
                                      }

                                  })
                                  .withActionBarDrawerToggleAnimated(true);
    }

    private String shortenizeBoardName(String name) {
        return name.substring(1, name.length() - 1);
    }

    Boolean sliderThinksClosing;
    boolean sliderHide;
    Menu    menu;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        outState = drawer.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.show_goto:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean valToSetVisibility;
        if (sliderThinksClosing != null) { // sliding now
            valToSetVisibility = !sliderHide;
        } else { // not sliding
            valToSetVisibility = !drawer.isDrawerOpen();
        }
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(valToSetVisibility);
        }
        return true;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.default_menu, menu);
        // todo draw a proper icon for Go-To-Ref-Link
        SearchView searchView = (SearchView) menu.findItem(R.id.show_goto).getActionView();
        int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
        ImageView v = (ImageView) searchView.findViewById(searchImgId);
        SpannableStringBuilder ssb = new SpannableStringBuilder("board/post");
        ssb.setSpan(new StyleSpan(Typeface.MONOSPACE.getStyle()), 0, 10, 0);
//        ImageView icon = (ImageView)searchView.findViewById(android.R.id.search_mag_icon);
        searchView.setQueryHint(ssb);
        Drawable d = getDrawable(android.R.drawable.ic_media_ff);
        d.setTint(getColor(R.color.dobro_primary_text));
        v.setImageDrawable(d);
//        searchView.setOnQueryTextListener(this);
        this.menu = menu;
        return super.onPrepareOptionsMenu(menu);
//        return true;
    }

    public static final class BoardDrawerItem extends PrimaryDrawerItem {

        @Override
        public int getLayoutRes() {
            return R.layout.drawer_item_board;
        }

        public BoardDrawerItem() {
            super();

        }

        public BoardDrawerItem(String s) {
            this();
            withName(s);
        }

        public BoardDrawerItem(@StringRes int id) {
            this();
            withName(id);
        }

        public BoardDrawerItem(StringHolder s) {
            this();
            withName(s);
        }

        public BoardDrawerItem(String s, @DrawableRes int dId) {
            this(s);
            withIcon(dId);
        }

        public BoardDrawerItem(@StringRes int id, @DrawableRes int dId) {
            this(id);
            withIcon(dId);
        }

        public BoardDrawerItem(StringHolder s, @DrawableRes int dId) {
            this(s);
            withIcon(dId);
        }

    }

    public static final class SettingsDrawerItem extends PrimaryDrawerItem {
        public SettingsDrawerItem() {
            super();
            withName("Settings");
        }

    }

}
