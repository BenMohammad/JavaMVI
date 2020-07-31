package com.benmohammad.javamvi.util;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import javax.annotation.Nonnull;

import static autovalue.shaded.com.google$.common.base.$Preconditions.checkNotNull;

public class ActivityUtils {

    public static void addFragmentToActivity(@Nonnull FragmentManager fm,
                                             @Nonnull Fragment fragment,
                                             int frameId) {
        checkNotNull(fm);
        checkNotNull(fragment);
        FragmentTransaction trans = fm.beginTransaction();
        trans.add(frameId, fragment);
        trans.commit();
    }
}
