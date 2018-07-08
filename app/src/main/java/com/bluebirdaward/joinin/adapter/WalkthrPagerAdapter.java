package com.bluebirdaward.joinin.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.bluebirdaward.joinin.vc.fragment.WalkthrFragment;

import com.bluebirdaward.joinin.R;

public class WalkthrPagerAdapter extends FragmentStatePagerAdapter {
  private static final int COUNT = 4;
  public SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
  
  // Images resources
  private static final int[] IMAGE_RES_IDS = {
      R.drawable.walkthrimg1, R.drawable.walkthrimg2, R.drawable.walkthrimg3, R.drawable.walkthrimg4
  };
  
  // Text resources
  private static final int[] TITLES_RES_IDS = {
      R.string.walkthrText1, R.string.walkthrText2, R.string.walkthrText3, R.string.walkthrText4
  };
  
  public WalkthrPagerAdapter(FragmentManager fm) {
      super(fm);
  }
   
  @Override
  public Fragment getItem(int position) {
          return WalkthrFragment.newInstance(IMAGE_RES_IDS[position], TITLES_RES_IDS[position], position);
  }
   
  @Override
  public int getCount() {
      return COUNT;
  }

  // Register the fragment when the item is instantiated
  @Override
  public Object instantiateItem(ViewGroup container, int position) {
    Fragment fragment = (Fragment) super.instantiateItem(container, position);
    registeredFragments.put(position, fragment);
    return fragment;
  }

  // Unregister when the item is inactive
  @Override
  public void destroyItem(ViewGroup container, int position, Object object) {
    registeredFragments.remove(position);
    super.destroyItem(container, position, object);
  }

  // Returns the fragment for the position (if instantiated)
  public Fragment getRegisteredFragment(int position) {
    return registeredFragments.get(position);
  }
}