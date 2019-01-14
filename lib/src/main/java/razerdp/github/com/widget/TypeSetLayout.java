package razerdp.github.com.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import razerdp.github.com.widget.adapter.TypeLayoutAdapter;

import static razerdp.github.com.widget.adapter.TypeLayoutAdapter.ViewHolder;

/**
 * Created by 大灯泡 on 2019/1/11.
 * <p>
 * 新版PhotoContents
 */
public class TypeSetLayout extends ViewGroup {
    private static final String TAG = "PhotoContents2";


    private TypeLayoutAdapter mAdapter;
    private CacheHelper mCacheHelper = new CacheHelper();
    private State mState = new State();
    private boolean autoMeasure;
    private int mHorizontalSpacing;
    private int mVerticalSpacing;

    public TypeSetLayout(Context context) {
        super(context);
    }

    public TypeSetLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public TypeSetLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        updateItemCount();
        if (mAdapter == null || mState.getItemCount() <= 0) {
            defaultMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        mState.isMeasuring = true;

        //确定尺寸的话，就直接指定咯。。。。
        if (widthMode == MeasureSpec.EXACTLY && height == MeasureSpec.EXACTLY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        //不确定尺寸，则按照指定的宽高进行。。。

        int wideSpec = widthMeasureSpec;
        if (widthMode == MeasureSpec.EXACTLY) {

        }


    }

    private void defaultMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(defaultSize(widthMeasureSpec, getPaddingLeft() + getPaddingRight(), getSuggestedMinimumWidth()),
                defaultSize(heightMeasureSpec, getPaddingTop() + getPaddingBottom(), getSuggestedMinimumHeight()));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    public static int defaultSize(int spec, int desired, int min) {
        final int mode = View.MeasureSpec.getMode(spec);
        final int size = View.MeasureSpec.getSize(spec);
        switch (mode) {
            case View.MeasureSpec.EXACTLY:
                return size;
            case View.MeasureSpec.AT_MOST:
                return Math.min(size, Math.max(desired, min));
            case View.MeasureSpec.UNSPECIFIED:
            default:
                return Math.max(desired, min);
        }
    }


    private void updateItemCount() {
        mState.mItemCount = mAdapter == null ? 0 : mAdapter.getItemCount();
    }

    private View obtainView(int position) {
        if (position < 0 || position >= mState.getItemCount()) {
            throw new IndexOutOfBoundsException("Invalid item position " + position
                    + "(" + position + "). Item count:" + mState.getItemCount());
        }
        ViewHolder holder = null;
        // TODO: 2019/1/14
        return holder.itemView;

    }

    //-----------------------------------------layoutparams-----------------------------------------
    public static class LayoutParams extends MarginLayoutParams {

        private ViewHolder mViewHolder;
        private boolean newLine = false;
        private boolean isSingle;
        private int maxWidth;
        private int maxHeight;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public boolean isNewLine() {
            return newLine;
        }

        public LayoutParams setNewLine(boolean newLine) {
            this.newLine = newLine;
            return this;
        }

        public int getMaxWidth() {
            return maxWidth;
        }

        public LayoutParams setMaxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        public int getMaxHeight() {
            return maxHeight;
        }

        public LayoutParams setMaxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
            return this;
        }
    }


    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }


    private LayoutParams generateDefaultLayoutParamsFromTarget(View v) {
        if (v == null) return generateDefaultLayoutParams();
        ViewGroup.LayoutParams p = v.getLayoutParams();
        if (p == null) return generateDefaultLayoutParams();
        if (!(p instanceof LayoutParams)) {
            return new LayoutParams(p);
        }
        return (LayoutParams) p;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    //-----------------------------------------tools-----------------------------------------
    //缓存类
    private class CacheHelper {
        private SparseArray<ViewHolder> mViewsCache;
        private static final int MAX_SIZE = 16;

        public CacheHelper() {
            mViewsCache = new SparseArray<>(16);
        }

        public ViewHolder get(int viewType) {
            ViewHolder result = mViewsCache.get(viewType);
            if (mAdapter != null && result != null) {
                mAdapter.onViewHolderResume(result);
            }
            trimToMax(MAX_SIZE);
            return result;
        }

        public void put(ViewHolder cache) {
            if (cache == null) return;
            View rootView = cache.itemView;
            mViewsCache.put(cache.getViewType(), cache);
            if (mAdapter != null) {
                mAdapter.onViewHolderCached(cache);
            }
            trimToMax(MAX_SIZE);
        }

        private void trimToMax(int max) {
            if (max < 0) return;
            while (mViewsCache.size() > max) {
                mViewsCache.remove(mViewsCache.size() - 1);
            }
        }
    }

    private static class State {
        private boolean isMeasuring;
        private boolean isLayouting;
        private int flag;
        private int mItemCount;


        public boolean isMeasuring() {
            return isMeasuring;
        }

        public boolean isLayouting() {
            return isLayouting;
        }

        public int getItemCount() {
            return mItemCount;
        }
    }


}
