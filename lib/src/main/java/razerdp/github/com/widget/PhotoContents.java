package razerdp.github.com.widget;

import android.content.Context;
import android.database.Observable;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import razerdp.github.com.widget.util.SimplePool;


/**
 * Created by 大灯泡 on 2019/7/1
 * <p>
 * Description：
 */
public class PhotoContents extends ViewGroup {
    private static final String TAG = "PhotoContents";

    PhotoContents.LayoutManager mLayout;
    SimplePool<ViewHolder> mPools;
    State mState;
    Adapter mAdapter;
    AdapterDataObserver mObserver;


    public PhotoContents(Context context) {
        this(context, null);
    }

    public PhotoContents(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoContents(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mObserver = new PhotoContentsObserver();
        mPools = new SimplePool<>(9);
        mState = new State();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mLayout == null) {
            defaultOnMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            preventRequestLayout();
            mState.itemCount = mAdapter.getItemCount();

            if (mState.itemCount == 0 || mAdapter == null) {
                doRecycler();
                defaultOnMeasure(widthMeasureSpec, heightMeasureSpec);
                resumeRequestLayout();
                return;
            }
            mLayout.onMeasure(mPools, mState, widthMeasureSpec, heightMeasureSpec);
            resumeRequestLayout();
        }
    }

    void defaultOnMeasure(int widthSpec, int heightSpec) {
        int width = LayoutManager.chooseSize(widthSpec, this.getPaddingLeft() + this.getPaddingRight(), ViewCompat.getMinimumWidth(this));
        int height = LayoutManager.chooseSize(heightSpec, this.getPaddingTop() + this.getPaddingBottom(), ViewCompat.getMinimumHeight(this));
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        preventRequestLayout();
        if (mAdapter == null) {
            doRecycler();
            resumeRequestLayout();
            return;
        }
        if (mLayout == null) {
            resumeRequestLayout();
            return;
        }
        mState.setBounds(getPaddingLeft(), getPaddingTop(), r - getPaddingRight(), b - getPaddingBottom());
        mLayout.onLayoutChildren(mPools, mState);
        mState.dataChanged = false;
        resumeRequestLayout();
    }

    private void doRecycler() {
        final int childCount = getChildCount();
        if (childCount <= 0) return;
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            ViewGroup.LayoutParams p = v.getLayoutParams();
            if (!checkLayoutParams(p)) continue;
            ViewHolder holder = ((LayoutParams) p).mViewHolder;
            if (holder == null) continue;
            mPools.release(holder);
            holder.onRecycled();
        }
        detachAllViewsFromParent();
    }


    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public Adapter getAdapter() {
        return mAdapter;
    }

    public PhotoContents setAdapter(Adapter adapter) {
        if (this.mAdapter == adapter) return this;
        if (this.mAdapter != null) {
            this.mAdapter.unregisterAdapterDataObserver(mObserver);
            this.mAdapter.onDetachedFromPhotoContents(this);
        }
        doRecycler();
        mPools.clearPool(new SimplePool.OnClearListener<ViewHolder>() {
            @Override
            public void onClear(ViewHolder cached) {
                cached.onDead();
            }
        });

        this.mAdapter = adapter;
        if (adapter != null) {
            adapter.registerAdapterDataObserver(mObserver);
            adapter.onAttachedToPhotoContents(this);
        }
        mState.dataChanged = true;
        return this;
    }

    public PhotoContents setLayoutManager(LayoutManager manager) {
        if (this.mLayout != null) {
            doRecycler();
            this.mLayout = null;
        }
        this.mLayout = manager;
        mLayout.bindPhotoContents(this);
        requestLayout();

        return this;
    }

    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public SimplePool<ViewHolder> getRecyclerPools() {
        return mPools;
    }

    private void preventRequestLayout() {
        mState.preventRequestLayout++;
    }

    private void resumeRequestLayout() {
        mState.preventRequestLayout--;
        if (mState.preventRequestLayout < 0) {
            mState.preventRequestLayout = 0;
        }
    }

    @Override
    public void requestLayout() {
//        if (mState == null || mState.preventRequestLayout != 0) {
//            NELog.i(TAG,"preventRequest");
//            return;
//        }
        super.requestLayout();
    }

    public State getState() {
        return mState;
    }

    public abstract static class LayoutManager {

        PhotoContents mPhotoContents;

        public abstract void onMeasure(@NonNull SimplePool<ViewHolder> pool, @NonNull State state, int widthSpec, int heightSpec);

        public abstract void onLayoutChildren(@NonNull SimplePool<ViewHolder> pool, @NonNull State state);

        public static int chooseSize(int spec, int desired, int min) {
            int mode = MeasureSpec.getMode(spec);
            int size = MeasureSpec.getSize(spec);
            switch (mode) {
                case MeasureSpec.AT_MOST:
                    return Math.min(size, Math.max(desired, min));
                case MeasureSpec.EXACTLY:
                    return size;
                case MeasureSpec.UNSPECIFIED:
                default:
                    return Math.max(desired, min);
            }
        }

        protected void setMeasuredDimension(int widthSize, int heightSize) {
            this.mPhotoContents.setMeasuredDimension(widthSize, heightSize);
        }

        private void bindPhotoContents(PhotoContents mPhotoContents) {
            boolean call = this.mPhotoContents != mPhotoContents;
            this.mPhotoContents = mPhotoContents;
            if (call) {
                onAttachedPhotoContents(mPhotoContents);
            }
        }

        public void requestLayout() {
            this.mPhotoContents.requestLayout();
        }

        public PhotoContents getParent() {
            return mPhotoContents;
        }

        public int getChildCount() {
            return mPhotoContents.getChildCount();
        }

        public void addView(View child) {
            this.addView(child, -1);
        }

        public void addView(View child, int index) {
            this.addView(child, index, null);
        }


        public void addView(View child, int index, LayoutParams p) {
            this.addView(child, index, p, false);
        }

        public void addView(View child, int index, LayoutParams p, boolean preventRequestLayout) {
            if (preventRequestLayout) {
                mPhotoContents.addViewInLayout(child, index, p, true);
            } else {
                mPhotoContents.addView(child, index, p);
            }
        }


        public void removeViewAt(int index) {
            View child = this.getChildAt(index);
            if (child != null) {
                mPhotoContents.removeViewAt(index);
            }

        }

        public void removeAllViews() {
            int childCount = mPhotoContents.getChildCount();
            for (int i = childCount - 1; i >= 0; --i) {
                mPhotoContents.removeViewAt(i);
            }
        }

        public void detachView(@NonNull View child) {
            mPhotoContents.detachViewFromParent(child);
        }

        public void detachViewAt(int index) {
            mPhotoContents.detachViewsFromParent(index, 1);
        }

        public LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
            if (lp instanceof LayoutParams) {
                return new LayoutParams((LayoutParams) lp);
            } else {
                return lp instanceof MarginLayoutParams ? new LayoutParams((MarginLayoutParams) lp) : new LayoutParams(lp);
            }
        }

        public View getChildAt(int index) {
            return mPhotoContents.getChildAt(index);
        }

        public void onAttachedPhotoContents(PhotoContents PhotoContents) {

        }

        protected ViewHolder obtainViewHolder(int position) {
            ViewHolder result = mPhotoContents.mPools.acquire();
            if (result == null) {
                result = mPhotoContents.getAdapter().createViewHolder(mPhotoContents, position);
            }
            ViewGroup.LayoutParams lp = result.rootView.getLayoutParams();
            LayoutParams p;

            if (lp == null) {
                p = generateDefaultLayoutParams();
                result.rootView.setLayoutParams(p);
            } else if (!checkLayoutParams(lp)) {
                p = generateLayoutParams(lp);
                result.rootView.setLayoutParams(p);
            } else {
                p = (LayoutParams) lp;
            }

            result.position = position;
            p.mViewHolder = result;

            return result;
        }

        public LayoutParams generateDefaultLayoutParams() {
            return mPhotoContents.generateDefaultLayoutParams();
        }

        protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
            return mPhotoContents.checkLayoutParams(p);
        }

        protected void doRecycler() {
            mPhotoContents.doRecycler();
        }

        protected ViewHolder findViewHolderForView(View v) {
            if (!checkLayoutParams(v.getLayoutParams())) {
                throw new IllegalArgumentException("View的layoutparams不正确");
            }

            return ((LayoutParams) v.getLayoutParams()).mViewHolder;

        }
    }


    public abstract static class AdapterDataObserver {
        public void onChanged() {

        }
    }

    private class PhotoContentsObserver extends AdapterDataObserver {
        @Override
        public void onChanged() {
            mState.reset();
            doRecycler();
            requestLayout();
        }
    }

    static class AdapterDataObservable extends Observable<AdapterDataObserver> {
        AdapterDataObservable() {
        }

        public boolean hasObservers() {
            return !this.mObservers.isEmpty();
        }

        public void notifyChanged() {
            for (int i = this.mObservers.size() - 1; i >= 0; --i) {
                this.mObservers.get(i).onChanged();
            }
        }
    }

    public abstract static class Adapter<VH extends ViewHolder> {
        private final AdapterDataObservable mObservable = new AdapterDataObservable();

        public void registerAdapterDataObserver(@NonNull AdapterDataObserver observer) {
            this.mObservable.registerObserver(observer);
        }

        public void unregisterAdapterDataObserver(@NonNull AdapterDataObserver observer) {
            this.mObservable.unregisterObserver(observer);
        }

        public void onViewRecycled(@NonNull VH holder) {
        }


        public void onAttachedToPhotoContents(@NonNull PhotoContents PhotoContents) {
        }

        public void onDetachedFromPhotoContents(@NonNull PhotoContents PhotoContents) {
        }

        public abstract int getItemCount();

        public abstract VH onCreateViewHolder(ViewGroup parent, int position);

        public abstract void onBindViewHolder(VH viewHolder, int position);

        public final VH createViewHolder(ViewGroup parent, int position) {
            VH holder = onCreateViewHolder(parent, position);
            if (holder.rootView == null) {
                throw new NullPointerException("ViewHolder的rootView不能为空");
            }
            if (holder.rootView.getParent() != null) {
                throw new IllegalArgumentException("ViewHolder的rootView不能有Parent");
            }
            return holder;
        }

        public final void bindViewHolder(@NonNull VH viewHolder, int position) {
            viewHolder.position = position;
            onBindViewHolder(viewHolder, position);
        }


        public final void notifyDataSetChanged() {
            this.mObservable.notifyChanged();
        }
    }

    public abstract static class ViewHolder {
        public final View rootView;
        int position;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
        }

        public int getPosition() {
            return position;
        }

        public void onRecycled() {

        }

        public void onDead() {

        }

        public final <V extends View> V findViewById(int resid) {
            if (resid > 0 && rootView != null) {
                return rootView.findViewById(resid);
            }
            return null;
        }
    }

    public static class LayoutParams extends MarginLayoutParams {
        ViewHolder mViewHolder;

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
    }

    public static class State {

        int preventRequestLayout;
        boolean dataChanged;
        int itemCount;
        Rect rect = new Rect();
        Rect tempRect = new Rect();

        void setBounds(int l, int t, int r, int b) {
            rect.set(l, t, r, b);
        }

        public boolean isDataChanged() {
            return dataChanged;
        }

        public int getItemCount() {
            return itemCount;
        }

        public Rect getBounds() {
            tempRect.set(rect);
            return tempRect;
        }

        void reset() {
            dataChanged = true;
            itemCount = 0;
            rect.setEmpty();
            tempRect.setEmpty();
        }

        @Override
        public String toString() {
            return "State{" +
                    "preventRequestLayout=" + preventRequestLayout +
                    ", dataChanged=" + dataChanged +
                    ", itemCount=" + itemCount +
                    ", rect=" + rect +
                    ", tempRect=" + tempRect +
                    '}';
        }
    }
}
