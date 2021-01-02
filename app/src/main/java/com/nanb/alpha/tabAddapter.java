package com.nanb.alpha;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class tabAddapter extends FragmentPagerAdapter {
    public tabAddapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                post postfragment = new post();
                return postfragment;
            case 1:
                chat chatfragment = new chat();
                return chatfragment;
            case 2:
                ContactView extrafragment = new ContactView();
                return extrafragment;
                default:
                    return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int i) {
        switch (i){
            case 0:
                return "Post";
            case 1:
               return "Chat";
            case 2:
                return "Contact";
            default:
                return null;
        }
    }
}
