#CleverRecyclerView
CleverRecyclerView 是一个基于RecyclerView的扩展库，提供了与ViewPager类似的滑动效果并且添加了一些有用的特性。

##效果图

<img src="https://github.com/luckyandyzhang/CleverRecyclerView/blob/master/art/s1.gif" width="300">  

<img src="https://github.com/luckyandyzhang/CleverRecyclerView/blob/master/art/s2.gif" width="300">  

<img src="https://github.com/luckyandyzhang/CleverRecyclerView/blob/master/art/s3.gif" width="300">  

##特性

- 支持横/纵向滚动
- 支持设置一页可显示的item数量
- 支持设置滚动动画的时长
- 支持设置触发滚动到下一页的阀值
- 支持页面切换的监听
- ......

##使用方法

在 `build.gradle` 加入如下依赖：

```groovy
repositories {
    maven {
        url "https://jitpack.io"
    }
}

dependencies {
    compile 'com.android.support:recyclerview-v7:23.1.0'
    compile 'com.github.luckyandyzhang:CleverRecyclerView:1.0.0'
}	
```

用法跟RecyclerView基本一致，但不需要`setLayoutManager()`
>其中的 CleverAdapter 继承于 RecyclerView.Adapter

```java
CleverRecyclerView recyclerView = (CleverRecyclerView) findViewById(R.id.recyclerView);
recyclerView.setAdapter(new CleverAdapter());

//一些常用的方法
//recyclerView.setScrollAnimationDuration(300);
//recyclerView.setOrientation(RecyclerView.VERTICAL);
//recyclerView.setVisibleChildCount(3);
//......
```


##开源协议

    Copyright 2015 Andy

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



