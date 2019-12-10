# 朋友圈图片容器

[ ![Download](https://api.bintray.com/packages/razerdp/maven/PhotoContents/images/download.svg) ](https://bintray.com/razerdp/maven/PhotoContents/_latestVersion)


针对AbsListView或者RecyclerView来进行View的二级缓存，采取的是adapter模式以及观察者。

实现原理实际上很简单，大致如下：

众所周知，RecyclerView或者AbsListview的复用机制都是利用一个数组来缓存view组，在创建View的时候获取出来并传到adapter
当一个View移出屏幕的时候，它会被放到池里面，在下一个view(viewType相同)时，会取出
而取出的View实际上childView等是不变的，因此我们可以利用这个机制，在更新时，将所有view都缓存
然后对比出该view更新前后的不同进行addView

又因为我们知道addView/removeView会造成requestLayout从而重新走了一遍measure和layout，所以为了避免这个情况
我们使用了`attachViewToParent`和`detachAllViewsFromParent`这两个仅针对数组操作的方法来避免这个问题。

当然目前来说这个控件还只是一个初步版本，后续如果有时间我会慢慢优化的。

ps，本控件初衷是为了我的另一个项目[一起撸个朋友圈吧](https://github.com/razerdp/FriendCircle)服务，大部分的更新都会在那里提交，如果您需要获取新的更新信息，可以通过另一个项目获知。


# Preview:
![](https://github.com/razerdp/PhotoContents/blob/master/art/preview.gif)

# Download  [ ![Download](https://api.bintray.com/packages/razerdp/maven/PhotoContents/images/download.svg) ](https://bintray.com/razerdp/maven/PhotoContents/_latestVersion)

**Step 1.**

Add the dependency

```xml
	dependencies {
	        compile 'com.github.razerdp:PhotoContents:{最新版}'  //最新版看上方Jcenter标签
	}
```


### Update log

 - 2019/12/10
    + 增加复用池拦截器，允许自行添加复用池逻辑
 - 2019/07/08
    + 重构
 - 2017/02/20
    + 修复点击后点击空白页面响应了之前的点击事件的问题


