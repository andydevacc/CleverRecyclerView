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

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

/**
 * 用于保存CleverRecyclerView的某些状态
 *
 * @author andy
 */
class CleverSavedState extends View.BaseSavedState {
    private int mLastScrollPosition;

    public CleverSavedState(Parcel source) {
        super(source);
        mLastScrollPosition = source.readInt();
    }

    public CleverSavedState(Parcelable superState, int lastScrollPostion) {
        super(superState);
        this.mLastScrollPosition = lastScrollPostion;
    }

    public int getLastScrollPostion() {
        return mLastScrollPosition;
    }

    public void setLastScrollPosition(int lastScrollPosition) {
        mLastScrollPosition = lastScrollPosition;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(mLastScrollPosition);
    }

    public static final Creator<CleverSavedState> CREATOR = new Creator<CleverSavedState>() {
        public CleverSavedState createFromParcel(Parcel in) {
            return new CleverSavedState(in);
        }

        public CleverSavedState[] newArray(int size) {
            return new CleverSavedState[size];
        }
    };
}
