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
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;


/**
 * 实现了ViewPager效果的RecyclerView增强类
 * <p>与{@link CleverLinearLayoutManager}配合使用</p>
 *
 * @author andy
 */
public class CleverRecyclerView extends RecyclerView {
    private static final float DEFAULT_FLING_FRICTION = 0.8f;

    private float mFlingFriction;
    private int mPositionBeforeDragging;
    private int mSmoothScrollTargetPosition;
    private int mCurrentPosition;
    private boolean mNeedAdjustAfterScrollStopped;

    private View mCurrentChildView;
    private CleverRecyclerViewAdapterProxy<?> mCleverRecyclerViewAdapterProxy;
    private CleverRecyclerViewHelper mCleverRecyclerViewHelper;
    private CleverLinearLayoutManager mCleverLinearLayoutManager;
    private boolean mHasUpdatedSnappyRecyclerViewHelper;

    private OnPageChangedListener mOnPageChangedListener;

    public CleverRecyclerView(Context context) {
        super(context);
    }

    public CleverRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CleverRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        return new CleverSavedState(parcelable, mCurrentPosition);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        CleverSavedState cleverSavedState = (CleverSavedState) state;
        mCurrentPosition = mSmoothScrollTargetPosition = cleverSavedState.getLastScrollPostion();
        scrollToPosition(mCurrentPosition);
        super.onRestoreInstanceState(cleverSavedState.getSuperState());
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if (layout instanceof CleverLinearLayoutManager) {
            mCleverRecyclerViewHelper.setOrientation(((CleverLinearLayoutManager) layout).getOrientation());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setAdapter(Adapter adapter) {
        mCleverRecyclerViewAdapterProxy = new CleverRecyclerViewAdapterProxy(this, adapter);
        super.setAdapter(mCleverRecyclerViewAdapterProxy);
    }

    @Override
    public Adapter getAdapter() {
        if (mCleverRecyclerViewAdapterProxy != null) {
            return mCleverRecyclerViewAdapterProxy.getAdapter();
        }
        return null;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!mHasUpdatedSnappyRecyclerViewHelper) {
            mHasUpdatedSnappyRecyclerViewHelper = true;
            mCleverRecyclerViewHelper.updateConfiguration();
        }
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        boolean isFlinging = super.fling((int) (velocityX * mFlingFriction), (int) (velocityY * mFlingFriction));
        if (isFlinging) {
            adjustPositionWithVelocity((int) (velocityX * mFlingFriction), (int) (velocityY * mFlingFriction));
        }
        return isFlinging;
    }

    @Override
    public void smoothScrollToPosition(int position) {
        if (mOnPageChangedListener != null && mCurrentPosition != NO_POSITION && mSmoothScrollTargetPosition != position) {
            mOnPageChangedListener.onPageChanged(position);
        }
        mCurrentPosition = mSmoothScrollTargetPosition = position;
        super.smoothScrollToPosition(position);
    }

    // 1.SCROLL_STATE_DRAGGING -> SCROLL_STATE_IDLE （用户松手后，视图没有在滚动的情况）
    // 2.SCROLL_STATE_DRAGGING -> SCROLL_STATE_SETTLING (会触发onFling方法)-> SCROLL_STATE_IDLE （用户松手后，视图还在在滚动的情况，触发了onFling）
    // 触发了onFling方法后，会在该方法中进行ChildView的位置偏移。并且会触发SCROLL_STATE_SETTLING状态~ 所以一旦进入了SCROLL_STATE_SETTLING状态
    // 则表明了ChildView已经偏移好了位置，那么在SCROLL_STATE_IDLE的时候，就不需要处理位置偏移了
    // smoothScrollToPosition会触发 SCROLL_STATE_SETTLING 状态
    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        switch (state) {
            case SCROLL_STATE_DRAGGING:
                mCurrentChildView = mCleverRecyclerViewHelper.getCurrentFirstVisibleChild();
                mCurrentPosition = getChildAdapterPosition(mCurrentChildView);
                if (mCurrentChildView != null) {
                    mPositionBeforeDragging = mCleverRecyclerViewHelper.getChildCurrentPosition(mCurrentChildView);
                }
                mNeedAdjustAfterScrollStopped = true;
                break;
            case SCROLL_STATE_SETTLING:
                mNeedAdjustAfterScrollStopped = false;
                break;
            case SCROLL_STATE_IDLE:
                //没有触发fling时，需要再次处理子View的位置偏移
                if (mNeedAdjustAfterScrollStopped) {
                    if (mCurrentChildView != null) {
                        float draggingDistance = mCleverRecyclerViewHelper.getChildCurrentPosition(mCurrentChildView) - mPositionBeforeDragging;
                        if (mCleverRecyclerViewHelper.isRightScrollTriggered(draggingDistance) || mCleverRecyclerViewHelper.isDownScrollTriggered(draggingDistance)) {
                            mCurrentPosition--;
                        } else if (mCleverRecyclerViewHelper.isLeftScrollTriggered(draggingDistance) || mCleverRecyclerViewHelper.isUpScrollTriggered(draggingDistance)) {
                            mCurrentPosition++;
                        }

                        int safeTargetPosition = mCleverRecyclerViewHelper.getTargetPositionSafely(mCurrentPosition, getAdapter().getItemCount());
                        smoothScrollToPosition(safeTargetPosition);
                    }
                }
                break;
        }
    }

    private void init() {
        mCleverRecyclerViewHelper = new CleverRecyclerViewHelper(this);
        mCleverLinearLayoutManager = new CleverLinearLayoutManager(getContext());
        mCleverLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        setLayoutManager(mCleverLinearLayoutManager);

        mFlingFriction = (1.0f - DEFAULT_FLING_FRICTION);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
    }

    /**
     * 根据快速滚动的速度对子View进行偏移
     */
    private void adjustPositionWithVelocity(int velocityX, int velocityY) {
        if (getChildCount() > 0) {
            int flingCount = mCleverRecyclerViewHelper.getFlingCountWithVelocity(velocityX, velocityY);
            int safeTargetPosition = mCleverRecyclerViewHelper.getTargetPositionSafely(mCurrentPosition + flingCount, getAdapter().getItemCount());
            smoothScrollToPosition(safeTargetPosition);
        }
    }

    /**
     * 获取当前的位置
     *
     * @return 当前位置
     */
    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    /**
     * 设置布局方向
     *
     * @param orientation
     */
    public void setOrientation(int orientation) {
        mCleverLinearLayoutManager.setOrientation(orientation);
        mCleverRecyclerViewHelper.setOrientation(orientation);
    }

    /**
     * 设置滚动动画的时长
     *
     * @param decelerationDuration 动画时长,默认是280ms
     */
    public void setScrollAnimationDuration(int decelerationDuration) {
        mCleverLinearLayoutManager.setDecelerationDuration(decelerationDuration);
    }

    /**
     * 设置相邻的2个View在当前视图可以显示的区域,目前只支持横向布局,并且在一页只有一个child view的情况下才生效
     *
     * @param displayArea
     */
    public void setAdjacentViewDisplayArea(int displayArea) {
        int mDisplayArea = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, displayArea, getResources().getDisplayMetrics());
        if (getLayoutManager().canScrollHorizontally() && mCleverRecyclerViewHelper.getVisibleChildCount() == 1) {
            setClipToPadding(false);
            setClipChildren(false);
            setPadding(mDisplayArea, 0, mDisplayArea, 0);
        }
    }

    /**
     * 设置快速滑动时的摩擦因子
     *
     * @param friction 取值范围 [0.0 - 1.0]
     *                 <li>默认：0.8f
     *                 <li>注意：值越大，摩擦越大，快速滑动越困难
     */
    public void setFlingFriction(float friction) {
        if (friction > 1.0f || friction < 0f) {
            return;
        }
        mFlingFriction = 1.0f - friction;
    }

    /**
     * 设置触发翻页动作的滚动距离,
     *
     * @param slidingThreshold 取值范围 [0.0 - 1.0]
     *                         <li>默认：0.22f
     *                         <li>注意：值越小，翻页所需的滚动距离越小，即越容易翻页
     */
    public void setSlidingThreshold(float slidingThreshold) {
        if (slidingThreshold > 1.0f || slidingThreshold < 0f) {
            return;
        }
        mCleverRecyclerViewHelper.setSlidingThreshold(slidingThreshold);
    }

    /**
     * 设置一页可以显示的item的数量
     * <p>注意：此方法必须在{@link CleverRecyclerView#setAdapter(Adapter)}之后调用
     *
     * @param visibleChildCount 目标数量
     */
    public void setVisibleChildCount(int visibleChildCount) {
        if (mCleverRecyclerViewAdapterProxy == null) {
            throw new IllegalStateException("you must call this method after #CleverRecyclerView.setAdapter(Adapter adapter)");
        }
        mCleverRecyclerViewHelper.setVisibleChildCount(visibleChildCount);
        mCleverRecyclerViewAdapterProxy.setVisibleChildCount(visibleChildCount);
    }

    /**
     * 监听页面切换
     *
     * @param onPageChangedListener {@link OnPageChangedListener}
     */
    public void setOnPageChangedListener(OnPageChangedListener onPageChangedListener) {
        mOnPageChangedListener = onPageChangedListener;
    }

    /**
     * 页面切换的回调
     */
    public interface OnPageChangedListener {
        void onPageChanged(int currentPosition);
    }

}
