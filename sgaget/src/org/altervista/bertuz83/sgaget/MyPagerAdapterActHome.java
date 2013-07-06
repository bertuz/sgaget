package org.altervista.bertuz83.sgaget;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * User: bertuz
 * Project: prova2.3
 */
public class MyPagerAdapterActHome extends FragmentPagerAdapter {
    static final int NUM_ITEMS = 3;
    private FragmentManager fm;
    private int viewId= -1;

    public MyPagerAdapterActHome(FragmentManager fm, int id) {
        super(fm);

        this.fm= fm;
        this.viewId= id;
    }

    public Fragment getItem(int index) {
        Fragment fragReturn;

        switch(index){
            case 0:
                fragReturn= new FTabPuntiInteresse();
                break;
            case 1:
                fragReturn= new FTabSpostamentoActHome();
                break;
            default:
                fragReturn= new FTabCompletareActHome();
                break;
        }

        return fragReturn;
    }


    @Override
    public int getCount() {
        return NUM_ITEMS;
    }


    public FTabPuntiInteresse getTabHotpoints(){
         return (FTabPuntiInteresse) fm.findFragmentByTag("android:switcher:" + viewId + ":" + 0);
    }


    public FTabSpostamentoActHome getTabSpostamento(){
        return (FTabSpostamentoActHome) fm.findFragmentByTag("android:switcher:" + viewId + ":" + 1);
    }


    public FTabCompletareActHome getTabCompletare(){
        return (FTabCompletareActHome) fm.findFragmentByTag("android:switcher:" + viewId + ":" + 2);
    }
}
