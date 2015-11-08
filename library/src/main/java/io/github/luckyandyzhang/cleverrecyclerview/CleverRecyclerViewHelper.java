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

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;


/**
 * CleverRecyclerView的辅助类
 *
 * @author andy
 */
class CleverRecyclerViewHelper {
    private static final float DEFAULT_SLIDING_THRESHOLD = 0.22f;
    private static final int DEFAULT_VISIBLE_CHILD_COUNT = 1;

    private RecyclerView mRecyclerView;
    private float mSlidingThreshold;
    private int mVisibleChildCount;
    private int mOrientation;

    private float mHorizontalSlidingThreshold;
    private float mVerticalSlidingThreshold;

    private int mItemCenterPositionX;
    private int mItemCenterPositionY;

    public CleverRecyclerViewHelper(RecyclerView recyclerView) {
        mSlidingThreshold = DEFAULT_SLIDING_THRESHOLD;
        mVisibleChildCount = DEFAULT_VISIBLE_CHILD_COUNT;
        mRecyclerView = recyclerView;
    }

    private void initSlidingThreshold() {
        int itemWidth = (mRecyclerView.getWidth() - mRecyclerView.getPaddingLeft() - mRecyclerView.getPaddingRight()) / mVisibleChildCount;
        int itemHeight = (mRecyclerView.getWidth() - mRecyclerView.getPaddingLeft() - mRecyclerView.getPaddingRight()) / mVisibleChildCount;
        mHorizontalSlidingThreshold = mVisibleChildCount == 1 ? itemWidth * mSlidingThreshold : itemWidth * 0.5f;
        mVerticalSlidingThreshold = mVisibleChildCount == 1 ? itemHeight * mSlidingThreshold : itemHeight * 0.5f;
    }

    private void initCenterParentPosition() {
        mItemCenterPositionX = mRecyclerView.getLeft() + mRecyclerView.getWidth() / (mVisibleChildCount * 2);
        mItemCenterPositionY = mRecyclerView.getTop() + mRecyclerView.getHeight() / (mVisibleChildCount * 2);
    }

    public void updateConfiguration() {
        initSlidingThreshold();
        initCenterParentPosition();
    }

    public void setSlidingThreshold(float slidingThreshold) {
        mSlidingThreshold = slidingThreshold;
        initSlidingThreshold();
    }

    /**
     * 设置一页可以显示多少个View
     *
     * @param visibleChildCount 希望显示的数量
     */
    public void setVisibleChildCount(int visibleChildCount) {
        mVisibleChildCount = visibleChildCount;
        initCenterParentPosition();
    }

    public int getVisibleChildCount() {
        return mVisibleChildCount;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    /**
     * 获取当前第一个可见的View
     *
     * @return 返回当前第一个可见的View
     */
    public View getCurrentFirstVisibleChild() {
        int childCount = mRecyclerView.getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                View child = mRecyclerView.getChildAt(i);
                if (isChildVisible(child)) {
                    return child;
                }
            }
        }
        return null;
    }

    /**
     * 判断指定的View是否在可见范围内
     *
     * @param child 指定view
     * @return true or false
     */
    private boolean isChildVisible(View child) {
        switch (mOrientation) {
            case LinearLayoutManager.HORIZONTAL:
                return child.getLeft() <= mItemCenterPositionX && child.getRight() >= mItemCenterPositionX;
            case LinearLayoutManager.VERTICAL:
                return child.getTop() <= mItemCenterPositionY && child.getBottom() >= mItemCenterPositionY;
            default:
                return false;
        }
    }

    /**
     * 获取当前View的位置
     *
     * @param child 目标View
     * @return LinearLayoutManager.HORIZONTAL: View.getLeft()
     * <p>LinearLayoutManager.VERTICAL: View.getTop()
     */
    public int getChildCurrentPosition(View child) {
        switch (mOrientation) {
            case LinearLayoutManager.HORIZONTAL:
                return child.getLeft();
            case LinearLayoutManager.VERTICAL:
                return child.getTop();
            default:
                return LinearLayoutManager.INVALID_OFFSET;
        }
    }

    /**
     * 根据速度计算出该滑动动作需要偏移多少个view
     *
     * @param velocityX 横向速度
     * @param velocityY 纵向速度
     * @return 需要偏移的个数
     */
    public int getFlingCountWithVelocity(int velocityX, int velocityY) {
        switch (mOrientation) {
            case LinearLayoutManager.HORIZONTAL:
                int childWidth = (mRecyclerView.getWidth() - mRecyclerView.getPaddingLeft() - mRecyclerView.getPaddingRight()) / mVisibleChildCount;
                return velocityX / childWidth;
            case LinearLayoutManager.VERTICAL:
                int childHeight = (mRecyclerView.getHeight() - mRecyclerView.getPaddingTop() - mRecyclerView.getPaddingBottom()) / mVisibleChildCount;
                return velocityY / childHeight;
            default:
                return 0;
        }
    }

    /**
     * 是否触发了 从右到左滑动
     *
     * @param distance 滑动距离
     * @return true or false
     */
    public boolean isLeftScrollTriggered(float distance) {
        return mRecyclerView.canScrollHorizontally(mOrientation)
                && distance <= 0 && Math.abs(distance) >= mHorizontalSlidingThreshold;
    }

    /**
     * 是否触发了 从左到右滑动
     *
     * @param distance 滑动距离
     * @return true or false
     */
    public boolean isRightScrollTriggered(float distance) {
        return mRecyclerView.canScrollHorizontally(mOrientation)
                && distance >= 0 && Math.abs(distance) >= mHorizontalSlidingThreshold;
    }

    /**
     * 是否触发了 从下到上滑动
     *
     * @param distance 滑动距离
     * @return true or false
     */
    public boolean isUpScrollTriggered(float distance) {
        return mRecyclerView.canScrollVertically(mOrientation)
                && distance <= 0 && Math.abs(distance) >= mVerticalSlidingThreshold;
    }

    /**
     * 是否触发了 从上到下滑动
     *
     * @param distance 滑动距离
     * @return true or false
     */
    public boolean isDownScrollTriggered(float distance) {
        return mRecyclerView.canScrollVertically(mOrientation)
                && distance >= 0 && Math.abs(distance) >= mVerticalSlidingThreshold;
    }

    /**
     * 在保证不越界的情况下，获取安全索引
     *
     * @param position 目标索引
     * @param count    容量
     * @return 安全索引
     */
    public int getTargetPositionSafely(int position, int count) {
        if (position < 0) {
            return 0;
        }
        if (position >= count) {
            return count - 1;
        }
        return position;
    }
}
