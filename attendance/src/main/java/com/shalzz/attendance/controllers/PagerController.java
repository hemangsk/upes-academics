/*
 * Copyright (c) 2013-2016 Shaleen Jain <shaleen.jain95@gmail.com>
 *
 * This file is part of UPES Academics.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.shalzz.attendance.controllers;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;

import com.shalzz.attendance.BuildConfig;
import com.shalzz.attendance.DatabaseHandler;
import com.shalzz.attendance.Miscellaneous;
import com.shalzz.attendance.R;
import com.shalzz.attendance.activity.MainActivity;
import com.shalzz.attendance.adapter.TimeTablePagerAdapter;
import com.shalzz.attendance.data.model.remote.ImmutablePeriod;
import com.shalzz.attendance.data.network.DataAPI;
import com.shalzz.attendance.fragment.TimeTablePagerFragment;
import com.shalzz.attendance.wrapper.MyVolley;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class PagerController {

    private TimeTablePagerFragment mView;
    public TimeTablePagerAdapter mAdapter;
    private DatabaseHandler db;
    private Context mContext;
    private Resources mResources;
    private String mTag = "Pager Controller";
    private Date mToday = new Date();

    public PagerController(Context context, TimeTablePagerFragment view, FragmentManager fm) {
        mContext = context;
        mResources = MyVolley.getMyResources();
        mView = view;
        db = new DatabaseHandler(mContext);
        mAdapter = new TimeTablePagerAdapter(fm, mContext);
        mView.mViewPager.setAdapter(mAdapter);
    }

    public void setDate(Date date) {
        mAdapter.setDate(date);
        mView.updateTitle(-1);
        scrollToDate(date);
    }

    public void setToday() {
        setDate(mToday);
    }

    public void scrollToDate(Date date) {
        int pos = mAdapter.getPositionForDate(date);
        mView.mViewPager.setCurrentItem(pos, true);
    }

    public Date getDateForPosition(int position) {
        return mAdapter.getDateForPosition(position);
    }

    public void updatePeriods() {
        DataAPI api = MyVolley.provideApi(MyVolley.provideClient(), MyVolley.provideGson());
        Call<List<ImmutablePeriod>> call = api.getTimetable();
        call.enqueue(new Callback<List<ImmutablePeriod>>() {
            @Override
            public void onResponse(Call<List<ImmutablePeriod>> call,
                                   retrofit2.Response<List<ImmutablePeriod>> response) {
                done();
                if(response.isSuccessful()) {
                    List<ImmutablePeriod> periods = response.body();
                    try {
                        if(periods.size() > 0) {
                            long now = new Date().getTime();
                            for (ImmutablePeriod period : periods) {
                                db.addPeriod(period, now);
                            }

                            if (db.purgeOldPeriods() == 1) {
                                if(BuildConfig.DEBUG)
                                    Log.d(mTag, "Purging Periods...");
                            }

                            // TODO: use an event bus or RxJava to update fragment contents

                            setToday();
                            mView.updateTitle(-1);
                            db.close();
                        } else {
                            String msg = mResources.getString(R.string.unavailable_timetable_error_msg);
                            Miscellaneous.showSnackBar(mView.mSwipeRefreshLayout, msg);
                        }
                        // Update the drawer header
                        ((MainActivity) mView.getActivity()).updateLastSync();
                    }
                    catch (Exception e) {
                        String msg = mResources.getString(R.string.unexpected_error);
                        Miscellaneous.showSnackBar(mView.mSwipeRefreshLayout, msg);
                        if(BuildConfig.DEBUG)
                            e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ImmutablePeriod>> call, Throwable t) {
                done();
                Miscellaneous.showSnackBar(mView.mSwipeRefreshLayout, t.getLocalizedMessage());
                if(BuildConfig.DEBUG)
                    t.printStackTrace();
            }
        });
    }

    public void done() {
        if(mView.mProgress != null) {
            mView.mProgress.setVisibility(View.GONE);
        }
        if(mView.mSwipeRefreshLayout != null) {
            mView.mSwipeRefreshLayout.setRefreshing(false);
        }
        if(mView.mViewPager != null) {
            mView.mViewPager.setVisibility(View.VISIBLE);
        }
    }
}
