/*
 * Copyright 2015 Andy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.luckyandyzhang.cleverrecyclerview;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import java.lang.reflect.Field;


/**
 * 自定义了滚动动画时长,子View滚动位置的LinearLayoutManager扩展类
 * <p>与{@link CleverRecyclerView}配合使用，用此类来代替LinearLayoutManager</p>
 *
 * @author andy
 */
class CleverLinearLayoutManager extends LinearLayoutManager {
    private static final int MILLISECONDS_DECELERATION = 280;

    private LinearSmoothScroller mSmoothScroller;
    private int mDecelerationDuration;

    private Field consecutiveUpdates;

    public CleverLinearLayoutManager(Context context) {
        super(context);
        mDecelerationDuration = MILLISECONDS_DECELERATION;
        initSmoothScroller(context);
    }

    public CleverLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public CleverLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void smoothScrollToPosition(final RecyclerView recyclerView, RecyclerView.State state, int position) {
        super.smoothScrollToPosition(recyclerView, state, position);
        mSmoothScroller.setTargetPosition(position);
        startSmoothScroll(mSmoothScroller);
    }

    @Override
    public void startSmoothScroll(RecyclerView.SmoothScroller smoothScroller) {
        super.startSmoothScroll(smoothScroller);
    }

    public void setDecelerationDuration(int decelerationDuration) {
        mDecelerationDuration = decelerationDuration;
    }

    private void initSmoothScroller(Context context) {
        mSmoothScroller = new LinearSmoothScroller(context) {
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return CleverLinearLayoutManager.this
                        .computeScrollVectorForPosition(targetPosition);
            }

            @Override
            protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
                super.onTargetFound(targetView, state, action);

                //FIXME 去掉烦人的提示...
                try {
                    if (consecutiveUpdates == null) {
                        consecutiveUpdates = Action.class.getDeclaredField("consecutiveUpdates");
                        consecutiveUpdates.setAccessible(true);//允许访问私有字段
                    }
                    consecutiveUpdates.setInt(action, 0);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected int calculateTimeForDeceleration(int dx) {
                return mDecelerationDuration;
            }

            @Override
            protected int getHorizontalSnapPreference() {
                return SNAP_TO_START;
            }

            @Override
            protected int getVerticalSnapPreference() {
                return SNAP_TO_START;
            }

//          @Override
//          public int calculateDyToMakeVisible(View view, int snapPreference) {
//              if (!canScrollVertically()) {
//                  return 0;
//              }
//              RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
//              int top = getDecoratedTop(view) - params.topMargin;
//              int bottom = getDecoratedBottom(view) + params.bottomMargin;
//              int viewHeight = bottom - top;
//              int start = getPaddingTop();
//              int end = start + viewHeight;
//              return calculateDtToFit(top, bottom, start, end, snapPreference);
//          }
//
//          @Override
//          public int calculateDxToMakeVisible(View view, int snapPreference) {
//              if (!canScrollHorizontally()) {
//                  return 0;
//              }
//              RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
//              int left = getDecoratedLeft(view) - params.leftMargin;
//              int right = getDecoratedRight(view) + params.rightMargin;
//              int viewWidth = right - left;
//              int start = getPaddingLeft();
//              int end = start + viewWidth;
//              return calculateDtToFit(left, right, start, end, snapPreference);
//          }
        };
    }


}
