package snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.adapters.pageradapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments.FoldersFragment;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments.HistoryFragment;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments.PlayListFragment;
import snackvideo.vidstatus.mojstatus.funnyvideo.videoplayer.fragments.VideosFragment;


public class ViewPagerAdapter extends FragmentPagerAdapter {
    private static final int NUM_ITEMS = 4;
    private final String[] tabTitles = new String[]{"Folders", "All Videos", "History", "Playlist"};

    public ViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: // Fragment # 0 - This will show FirstFragment
                return new FoldersFragment();
            case 1: // Fragment # 0 - This will show FirstFragment different title
                return new VideosFragment();
            case 2: // Fragment # 1 - This will show SecondFragment
                return new HistoryFragment();
            case 3:
                return new PlayListFragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }


    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

}
