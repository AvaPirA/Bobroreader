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

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import com.avapira.bobroreader.hanabira.Hanabira;
import com.avapira.bobroreader.hanabira.entity.HanabiraBoard;
import com.avapira.bobroreader.util.Consumer;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import org.joda.time.LocalDateTime;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class Bober extends AppCompatActivity implements Castor {
    private static final double DEBUG_INIT_START = DEBUG_time();
    private Toolbar toolbar;
    private boolean loadDiff;
    private Drawer  boardsDrawer;
    private Drawer  featuresDrawer;

    private static final class DrawerIdentifier {

        private static final int SETTINGS = 239;
    }

    private static class ShortSectionDivider extends SectionDrawerItem {

        @Override
        @LayoutRes
        public int getLayoutRes() {
            return R.layout.activity_bober_drawer_boards_section_divider;
        }
    }

    public static final class BoardDrawerItem extends PrimaryDrawerItem {

        public BoardDrawerItem(String s, Drawable icon) {
            super();
            withName(s);
            withIcon(icon);
        }

        public BoardDrawerItem(@StringRes int id, @DrawableRes int idIcon) {
            super();
            withName(id);
            withIcon(idIcon);
        }

        @Override
        public int getLayoutRes() {
            return R.layout.activity_bober_drawer_boards_item;
        }

    }

    public static final class SettingsDrawerItem extends PrimaryDrawerItem {

        public SettingsDrawerItem() {
            super();
            withIdentifier(DrawerIdentifier.SETTINGS);
            withSelectable(false);
            withName("Settings");
        }

    }

    private class BoardSwitcher implements com.mikepenz.materialdrawer.Drawer.OnDrawerItemClickListener {

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            if (drawerItem != null) {
                if (drawerItem instanceof BoardDrawerItem) {
                    BoardDrawerItem clickedItem = (BoardDrawerItem) drawerItem;
                    String boardKey = HanabiraBoard.Info.cutSlashes(clickedItem.getName().getText());
                    Fragment boardFragment = BoardFragment.newInstance(boardKey);
                    getFragmentManager().beginTransaction().replace(R.id.frame_main_container, boardFragment).commit();
                }
            }
            updateDrawerDiff(true);
            return false;
        }

    }

    private class BoardItemGenerator {

        private final Map<String, int[]> pics            = new LinkedHashMap<String, int[]>() {
            {
                put("b",
                    new int[]{R.drawable.banners_b_125700861425839, R.drawable.banners_b_125860969605710, R.drawable
                            .banners_b_b1});
                put("u", new int[]{R.drawable.banners_u_125860969598039});
                put("rf",
                    new int[]{R.drawable.banners_rf_125699252353074, R.drawable.banners_rf_125701163950149, R
                            .drawable.banners_rf_125701647009375, R.drawable.banners_rf_125736226221120, R.drawable
                            .banners_rf_125861026695515});

                put("dt", new int[]{R.drawable.banners_dt_125697739438064});
                put("vg",
                    new int[]{R.drawable.banners_vg_125701543235898, R.drawable.banners_vg_125701543238460, R
                            .drawable.banners_vg_125709977081546, R.drawable.banners_vg_125718465081528, R.drawable
                            .banners_vg_125725276436260, R.drawable.banners_vg_125752214930592, R.drawable
                            .banners_vg_125787756729250, R.drawable.banners_vg_125860991758021});
                put("r", new int[]{R.drawable.banners_r_125699732718180, R.drawable.banners_r_r});
                put("cr",
                    new int[]{R.drawable.banners_cr_cr2, R.drawable.banners_cr_cr4, R.drawable.banners_cr_cr_vampire});
                put("mu",
                    new int[]{R.drawable.banners_mu_125701524833743, R.drawable.banners_mu_125706039606802, R
                            .drawable.banners_mu_125758466249422, R.drawable.banners_mu_125758466251705, R.drawable
                            .banners_mu_125861048005976, R.drawable.banners_mu___116});
                put("oe", new int[]{});
                put("s", new int[]{R.drawable.banners_s_125776130692418, R.drawable.banners_s_125860969610249});
                put("w", new int[]{});
                put("hr", new int[]{});
                put("a",
                    new int[]{R.drawable.banners_a_125700436332204, R.drawable.banners_a_125701704962528, R.drawable
                            .banners_a_125702195165767, R.drawable.banners_a_125761210590870, R.drawable
                            .banners_a_125768405443972});
                put("ma", new int[]{R.drawable.banners_ma_125860969613262, R.drawable.banners_ma_ma});
                put("sw", new int[]{R.drawable.banners_sw_125861045421667, R.drawable.banners_sw_125694314851117});
                put("hau", new int[]{R.drawable.banners_hau_125861045418626});
                put("azu", new int[]{});
                put("tv",
                    new int[]{R.drawable.banners_tv_2bd020d5bb30, R.drawable.banners_tv_2e04001fa57f, R.drawable
                            .banners_tv_snapshot20100724100336, R.drawable.banners_tv_ccd7cbf4e061, R.drawable
                            .banners_tv_55555, R.drawable.banners_tv_46599021_1248208279_clapperboardmanresized2, R
                            .drawable.banners_tv_3672202a3ac0});
                put("cp", new int[]{R.drawable.banners_cp_g125788239756657});
                put("gf", new int[]{R.drawable.banners_gf_125860979571217});
                put("bo", new int[]{});
                put("di",
                    new int[]{R.drawable.banners_di_125702135915554, R.drawable.banners_di_125762259407262, R
                            .drawable.banners_di_125860991769106});
                put("vn", new int[]{R.drawable.banners_vn_125861005475361});
                put("ve",
                    new int[]{R.drawable.banners_ve_125698553182650, R.drawable.banners_ve_125698880498448, R
                            .drawable.banners_ve_125699339172544});
                put("wh",
                    new int[]{R.drawable.banners_wh_125697147834527, R.drawable.banners_wh_wh2, R.drawable
                            .banners_wh_wh1, R.drawable.banners_wh_125861075646865});
                put("fur", new int[]{R.drawable.banners_fur_125861026701646});
                put("to",
                    new int[]{R.drawable.banners_to_to_oppai_edition__lewd_, R.drawable.banners_to_to_lunatic, R
                            .drawable.banners_to_to_cosplay, R.drawable.banners_to_to_cirnotopter, R.drawable
                            .banners_to_125861045424732});
                put("bg",
                    new int[]{R.drawable.banners_bg_bg, R.drawable.banners_bg_125861578033434, R.drawable
                            .banners_bg_125697224028122});
                put("wn", new int[]{R.drawable.banners_wn_125701591350076, R.drawable.banners_wn_125861005478345});
                put("slow",
                    new int[]{R.drawable.banners_slow_slow_2_copy_new, R.drawable.banners_slow_slow_3, R.drawable
                            .banners_slow_slow_4});
                put("mad", new int[]{R.drawable.banners_mad_mad});
                put("d", new int[]{R.drawable.banners_d_125711152029591, R.drawable.banners_d_d_motherland_hears_you});
                put("news", new int[]{R.drawable.banners_news_125710395977840});
            }
        };
        private final int[]              SECTION_LENGTHS = {12, 5, 14, 2};
        private final String[]           SECTION_TITLES  = {"Общее", "Аниме", "На пробу", "Доброчан"};
        private final Random             r               = new Random();

        public ArrayList<IDrawerItem> getItems() {
            int toSection = 0;
            int nextSectionIndex = 0;
            ArrayList<IDrawerItem> items = new ArrayList<>();
            for (Map.Entry<String, int[]> entry : pics.entrySet()) {
                if (toSection-- == 0) {
                    items.add(new ShortSectionDivider().withName(SECTION_TITLES[nextSectionIndex]));
                    toSection = SECTION_LENGTHS[nextSectionIndex++];
                }
                String name = slashify(entry.getKey());
                Drawable drw = getRandomDrawable(entry.getKey());
                items.add(new BoardDrawerItem(name, drw));
            }
            return items;
        }

        private Drawable getRandomDrawable(String key) {
            int[] ids = pics.get(key);
            int id;
            Drawable empty = getDrawable(R.drawable.banners_empty);
            if (ids.length != 0) {
                if (ids.length == 1) {
                    id = ids[0];
                } else {
                    id = ids[r.nextInt(ids.length)];
                }
            } else {
                return empty;
            }
            return getDrawable(id);
        }

        private String slashify(String key) {
            return new StringBuilder("/").append(key).append('/').toString();
        }
    }

    private class PostOpenBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            PostDialogFragment.newInstance(intent.getExtras()).show(getFragmentManager(), null);
        }
    }

    private static double DEBUG_time() {
        return System.nanoTime();
//        return System.currentTimeMillis();
    }

    private static double DEBUG_initRelativeTime() {
        return (DEBUG_time() - DEBUG_INIT_START) / 10e5;
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

    @Override
    public void onFragmentInteraction(Uri uri) {
        //todo
    }

    @Override
    public void retitleOnLoading() {
        toolbar.setTitle("Loading...");
    }

    @Override
    public void retitleOnBoardLoad(String key, int page) {
        toolbar.setTitle(String.format("/%s/ %s [%d]", key, HanabiraBoard.Info.getForKey(key).title, page));
    }

    @Override
    public void retitleOnThreadLoad(String key, String title) {
        toolbar.setTitle(String.format("%s/%s", key, title));
    }

    @Override
    public void onOpenPost(String board, int postDisplayId) {
        DialogFragment postDialog = PostDialogFragment.newInstance(board, postDisplayId);
        postDialog.show(getFragmentManager(), null);
    }

    @Override
    public void onThreadSelected(int threadId) {
        Fragment threadFragment = ThreadFragment.newInstance(threadId);
        getFragmentManager().beginTransaction()
                            .replace(R.id.frame_main_container, threadFragment)
                            .addToBackStack("board_open_thread")
                            .commit();
    }

    private void loadingLog(String newState) {
        Log.d("Bober init",
              new StringBuilder().append(DEBUG_initRelativeTime()).append(' ').append(newState).toString());
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        loadingLog("Freezing time...");
        // Build the local caches inside Joda Time
        new LocalDateTime();
        loadingLog("Manuscripts contemplation...");
        HanabiraBoard.Info.loadBoardsInfo(getResources(), R.raw.boards);

        loadingLog("Superposition...");
        super.onCreate(savedInstanceState);
        loadingLog("Dressing...");
        setContentView(R.layout.activity_bober);
        loadingLog("Pulling out a hat...");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        loadingLog("Dubbing a hat...");
        setSupportActionBar(toolbar);
        loadingLog("Equipping primary weapon...");
        boardsDrawer = generateBoardsDrawerBuilder(savedInstanceState, toolbar).build();
        loadingLog("Equipping secondary weapon...");
        featuresDrawer = generateFeaturesDrawerBuilder(savedInstanceState).append(boardsDrawer);
        loadingLog("Starting a new journey");
        findViewById(R.id.frame_toolbar_container).setVisibility(View.VISIBLE);
        setTheme(R.style.Bobroreader_Theme_Default);
        // TODO create big enough splash logo
        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(new PostOpenBroadcastReceiver(),
                                               new IntentFilter(Hanabira.Action.OPEN_POST.name()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Bober", "Start");
        // todo show pretty /news/ index page
        loadDiff = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                    .getBoolean("pref_load_diff", false);
        updateDrawerDiff(false);
    }

    private void updateDrawerDiff(boolean wait) {
        if (loadDiff) {
            Hanabira.getFlower().getDiff(wait, new Consumer<Map<String, Integer>>() {
                @Override
                public void accept(final Map<String, Integer> diff) {
                    for (IDrawerItem item : boardsDrawer.getAdapter().getDrawerItems()) {
                        if (item instanceof BoardDrawerItem) {
                            BoardDrawerItem i = (BoardDrawerItem) item;
                            String boardKey = shortenizeBoardName(i.getName().getText());
                            i.withBadge(String.format("[%s]", diff.get(boardKey)));
                        }
                    }
                    boardsDrawer.getAdapter().notifyDataSetChanged();
                }
            });
        }
    }

    private DrawerBuilder generateBoardsDrawerBuilder(Bundle savedInstanceState, Toolbar toolbar) {
        ImageView image = (ImageView) LayoutInflater.from(getApplicationContext())
                                                    .inflate(R.layout.activity_bober_drawer_boards_header_horo, null);
        Drawable horo = getDrawable(R.drawable.acc39fc867f_transparent);
        image.setImageDrawable(horo);
        return new DrawerBuilder().withActivity(Bober.this)
                                  .withFullscreen(true)
                                  .withToolbar(toolbar)
                                  .withHeaderDivider(false)
                                  .withHeader(image)
                                  .withDrawerItems(new BoardItemGenerator().getItems())
                                  .withSavedInstance(savedInstanceState)
                                  .withOnDrawerItemClickListener(new BoardSwitcher())
                                  .withOnDrawerListener(new Drawer.OnDrawerListener() {
                                      @Override
                                      public void onDrawerOpened(View drawerView) {
                                          Bober.this.invalidateOptionsMenu();
                                      }

                                      @Override
                                      public void onDrawerClosed(View drawerView) {
                                          Bober.this.invalidateOptionsMenu();
                                      }

                                      @Override
                                      public void onDrawerSlide(View arg0, float slideOffset) {}

                                  })
                                  .withActionBarDrawerToggleAnimated(true);
    }

    private DrawerBuilder generateFeaturesDrawerBuilder(Bundle sIS) {
        return new DrawerBuilder().withDrawerGravity(Gravity.END)
                                  .withActivity(this)
                                  .withHeaderDivider(false)
                                  .withSelectedItem(-1)
                                  .withFullscreen(false)
                                  .withSavedInstance(sIS)
                                  .addDrawerItems(new PrimaryDrawerItem().withName("History")
                                                                         .withIdentifier(0)
                                                                         .withSelectable(false), new PrimaryDrawerItem()
                                                          .withName("Thread AutoHide")
                                                          .withIdentifier(1)
                                                          .withSelectable(false),
                                                  new PrimaryDrawerItem().withName("Bookmarks")
                                                                         .withIdentifier(2)
                                                                         .withSelectable(false),
                                                  new SettingsDrawerItem().withIdentifier(3).withSelectable(false))
                                  .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                                      @Override
                                      public boolean onItemClick(View view, int i, IDrawerItem iDrawerItem) {
                                          switch (iDrawerItem.getIdentifier()) {
                                              case 0:
                                              case 1:
                                              case 2:
                                              case 3:
                                                  startActivity(
                                                          new Intent(getApplicationContext(), SettingsActivity.class));
                                          }
                                          return false;
                                      }
                                  });
    }

    private String shortenizeBoardName(String name) {
        return name.substring(1, name.length() - 1);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.frame_main_container);
        if (currentFragment != null) {
            getFragmentManager().putFragment(outState, "main-fragment", currentFragment);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        getFragmentManager().beginTransaction()
                            .add(getFragmentManager().getFragment(savedInstanceState, "main-fragment"), null)
                            .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean valToSetVisibility = !boardsDrawer.isDrawerOpen();
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(valToSetVisibility);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (boardsDrawer != null && boardsDrawer.isDrawerOpen()) {
            boardsDrawer.closeDrawer();
        } else {
            FragmentManager fm = getFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                Log.i("Bober", "popping backstack");
                fm.popBackStack();
            } else {
                Log.i("Bober", "nothing on backstack, calling super");
                super.onBackPressed();
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("Bober", "destroy");
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        Log.d("Bober", "stop");
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.default_menu, menu);
        return super.onPrepareOptionsMenu(menu);
    }

}
