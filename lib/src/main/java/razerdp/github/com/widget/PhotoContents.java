package razerdp.github.com.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.apmem.tools.layouts.FlowLayout;

import java.util.LinkedList;
import java.util.List;

import razerdp.github.com.widget.adapter.PhotoContentsBaseAdapter;
import razerdp.github.com.widget.adapter.observer.PhotoBaseDataObserver;
import razerdp.github.com.widget.util.SimpleObjectPool;


/**
 * Created by 大灯泡 on 2016/11/9.
 * <p>
 * 适用于朋友圈的九宫格图片显示(用于listview等)
 */

public class PhotoContents extends FlowLayout {

    private final int INVALID_POSITION = -1;

    private PhotoContentsBaseAdapter mAdapter;
    private PhotoImageAdapterObserver mAdapterObserver = new PhotoImageAdapterObserver();
    private InnerRecyclerHelper recycler;
    private int mItemCount;
    private boolean mDataChanged;
    private int itemMargin;
    private int multiChildSize;

    private int maxSingleWidth;
    private int maxSingleHeight;
    //宽高比
    private float singleAspectRatio = 16f / 9f;

    private int mSelectedPosition = INVALID_POSITION;

    private Rect mTouchFrame;

    private Runnable mTouchReset;

    public PhotoContents(Context context) {
        super(context);
        init(context);
    }

    public PhotoContents(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PhotoContents(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        itemMargin = dp2Px(4f);
        recycler = new InnerRecyclerHelper();
        setOrientation(HORIZONTAL);
        setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int childRestWidth = widthSize - getPaddingLeft() - getPaddingRight();
        updateItemCount();
        multiChildSize = childRestWidth / 3 - itemMargin * 2;
        if (maxSingleWidth == 0) {
            maxSingleWidth = childRestWidth * 2 / 3;
            maxSingleHeight = (int) (maxSingleWidth / singleAspectRatio);
        }

        if (mDataChanged) {
            if (mAdapter == null || mItemCount == 0) {
                resetContainer();
                return;
            }
            final int childCount = getChildCount();
            final int oldChildCount = childCount;
            if (oldChildCount > 0) {
                if (oldChildCount == 1) {
                    recycler.addSingleCachedView((ImageView) getChildAt(0));
                } else {
                    for (int i = 0; i < oldChildCount; i++) {
                        View v = getChildAt(i);
                        recycler.addCachedView(i, (ImageView) v);
                    }
                }
            }

            updateItemCount();
            //清除旧的view
            detachAllViewsFromParent();

            int newChildCount = mItemCount;
            if (newChildCount > 0) {
                fillView(newChildCount);
            }
            mDataChanged = false;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void fillView(int childCount) {
        if (childCount == 1) {
            fillSingleView();
        } else if (childCount == 4) {
            fillFourViews();
        } else {
            for (int i = 0; i < childCount; i++) {
                final ImageView child = obtainView(i);
                setupViewAndAddView(i, child, false, false);
            }
        }
    }

    private void fillSingleView() {
        final ImageView singleChild = obtainView(0);
        singleChild.setAdjustViewBounds(true);
        singleChild.setMaxWidth(maxSingleWidth);
        singleChild.setMaxHeight(maxSingleHeight);
        singleChild.setScaleType(ImageView.ScaleType.FIT_START);
        setupViewAndAddView(0, singleChild, false, true);
    }

    private void fillFourViews() {
        for (int i = 0; i < 4; i++) {
            final ImageView child = obtainView(i);
            if (i == 2) {
                setupViewAndAddView(i, child, true, false);
            } else {
                setupViewAndAddView(i, child, false, false);
            }
        }
    }


    private void setupViewAndAddView(int position, @NonNull ImageView v, boolean newLine, boolean isSingle) {
        setItemLayoutParams(v, newLine, isSingle);
        if (onSetUpChildLayoutParamsListener != null) {
            onSetUpChildLayoutParamsListener.onSetUpParams(v, (LayoutParams) v.getLayoutParams(), position, isSingle);
        }
        mAdapter.onBindData(position, v);
        addViewInLayout(v, position, v.getLayoutParams(), true);
    }


    private void setItemLayoutParams(@NonNull ImageView v, boolean needLine, boolean isSingle) {
        ViewGroup.LayoutParams p = v.getLayoutParams();
        if (p == null || !(p instanceof LayoutParams)) {
            LayoutParams childLP = generateDefaultMultiLayoutParams(isSingle);
            childLP.setNewLine(needLine);
            v.setLayoutParams(childLP);
        } else {
            ((LayoutParams) p).setNewLine(needLine);
        }
    }


    public void setAdapter(PhotoContentsBaseAdapter adapter) {
        if (mAdapter != null && mAdapterObserver != null) {
            mAdapter.unregisterDataSetObserver(mAdapterObserver);
        }

        resetContainer();

        mAdapter = adapter;
        mAdapterObserver = new PhotoImageAdapterObserver();
        mAdapter.registerDataSetObserver(mAdapterObserver);
        mDataChanged = true;
        requestLayout();
    }

    public PhotoContentsBaseAdapter getAdapter() {
        return mAdapter;
    }


    private void resetContainer() {
        recycler.clearCache();
        removeAllViewsInLayout();
        invalidate();
    }

    private void updateItemCount() {
        mItemCount = mAdapter == null ? 0 : mAdapter.getCount();
    }

    private ImageView obtainView(int position) {

        ImageView cachedView;
        ImageView child;
        if (mItemCount == 1) {
            cachedView = recycler.getSingleCachedView();
        } else {
            cachedView = recycler.getCachedView(position);
        }
        child = mAdapter.onCreateView(cachedView, this, position);

        if (child != cachedView) {
            if (mItemCount == 1) {
                recycler.addSingleCachedView(child);
            } else {
                recycler.addCachedView(position, child);
            }
        }
        return child;
    }

    protected LayoutParams generateDefaultMultiLayoutParams(boolean isSingle) {
        LayoutParams p;
        if (isSingle) {
//            p = new PhotoContents.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            p = new PhotoContents.LayoutParams(maxSingleWidth, maxSingleHeight);

        } else {
            p = new PhotoContents.LayoutParams(multiChildSize, multiChildSize);
        }
        p.rightMargin = itemMargin;
        p.bottomMargin = itemMargin;
        return p;
    }


    private int dp2Px(float dp) {
        return (int) (dp * getContext().getResources().getDisplayMetrics().density + 0.5f);
    }

    public static class LayoutParams extends FlowLayout.LayoutParams {

        public LayoutParams(int width, int height) {
            this(new ViewGroup.LayoutParams(width, height));
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }


    private class InnerRecyclerHelper {
        private SparseArray<ImageView> mCachedViews;
        private SimpleObjectPool<ImageView> mSingleCachedViews;

        InnerRecyclerHelper() {
            mCachedViews = new SparseArray<>();
            mSingleCachedViews = new SimpleObjectPool<>(9);
        }

        ImageView getCachedView(int position) {
            final ImageView imageView = mCachedViews.get(position);
            if (imageView != null) {
                mCachedViews.remove(position);
                return imageView;
            }
            return null;
        }

        ImageView getSingleCachedView() {
            return mSingleCachedViews.get();
        }

        void addCachedView(int position, ImageView view) {
            mCachedViews.put(position, view);
        }

        void addSingleCachedView(ImageView imageView) {
            mSingleCachedViews.put(imageView);
        }

        void clearCache() {
            mCachedViews.clear();
            mSingleCachedViews.clearPool();
        }

    }

    private class PhotoImageAdapterObserver extends PhotoBaseDataObserver {

        @Override
        public void onChanged() {
            super.onChanged();
            updateItemCount();
            mDataChanged = true;
            requestLayout();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            invalidate();
        }
    }


    public boolean performItemClick(ImageView view, int position) {
        final boolean result;
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(view, position);
            result = true;
        } else {
            result = false;
        }
        return result;
    }
    //------------------------------------------TouchEvent-----------------------------------------------

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return isClickable() || isLongClickable();
        }
        final int actionMasked = event.getActionMasked();
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: {
                onTouchDown(event);
                break;
            }

            case MotionEvent.ACTION_UP: {
                onTouchUp(event);
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                setPressed(false);
                break;
            }
        }
        return true;
    }

    private void onTouchDown(MotionEvent event) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();

        if (!mDataChanged) {
            final int selectionPosition = pointToPosition(x, y);
            if (checkPositionValided(selectionPosition)) {
                View view = getChildAt(selectionPosition);
                if (view != null && view.isEnabled()) {
                    updateChildPressState(selectionPosition, true);
                    this.mSelectedPosition = selectionPosition;
                }
            }
        }
    }

    private void onTouchUp(MotionEvent event) {
        final int selectionPosition = mSelectedPosition;
        if (!mDataChanged) {
            if (checkPositionValided(selectionPosition)) {
                final View child = getChildAt(selectionPosition);
                updateChildPressState(selectionPosition, true);
                performItemClick((ImageView) child, selectionPosition);
                if (mTouchReset != null) {
                    removeCallbacks(mTouchReset);
                }
                mTouchReset = new Runnable() {
                    @Override
                    public void run() {
                        child.setPressed(false);
                    }
                };
                postDelayed(mTouchReset, ViewConfiguration.getPressedStateDuration());
            }
        } else {
            if (checkPositionValided(selectionPosition)) updateChildPressState(selectionPosition, false);
        }
    }


    private boolean checkPositionValided(int position) {
        boolean result = true;
        final int childCount = getChildCount();
        if (mDataChanged) {
            result = false;
        }
        if (position <= INVALID_POSITION || position > childCount - 1) {
            result = false;
        }
        return result;
    }

    public void updateChildPressState(int position, boolean press) {
        if (checkPositionValided(position)) {
            final View child = getChildAt(position);
            if (press) {
                child.requestFocus();
            }
            child.setPressed(press);
        }
    }


    /**
     * 捕获点击的view并返回其position
     *
     * @param x
     * @param y
     * @return
     */
    public int pointToPosition(int x, int y) {
        if (mTouchFrame == null) mTouchFrame = new Rect();
        final int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child != null && child.getVisibility() == VISIBLE) {
                child.getHitRect(mTouchFrame);
                if (mTouchFrame.contains(x, y)) {
                    return i;
                }
            }
        }
        return INVALID_POSITION;
    }


    //------------------------------------------getter/setter-----------------------------------------------

    public float getSingleAspectRatio() {
        return singleAspectRatio;
    }

    public void setSingleAspectRatio(float singleAspectRatio) {
        if (this.singleAspectRatio != singleAspectRatio && maxSingleWidth != 0) {
            this.maxSingleHeight = (int) (maxSingleWidth / singleAspectRatio);
        }
        this.singleAspectRatio = singleAspectRatio;
    }

    public int getMaxSingleWidth() {
        return maxSingleWidth;
    }

    public void setMaxSingleWidth(int maxSingleWidth) {
        this.maxSingleWidth = maxSingleWidth;
    }

    public int getMaxSingleHeight() {
        return maxSingleHeight;
    }

    public void setMaxSingleHeight(int maxSingleHeight) {
        this.maxSingleHeight = maxSingleHeight;
    }

    public OnSetUpChildLayoutParamsListener getOnSetUpChildLayoutParamsListener() {
        return onSetUpChildLayoutParamsListener;
    }

    public void setOnSetUpChildLayoutParamsListener(OnSetUpChildLayoutParamsListener onSetUpChildLayoutParamsListener) {
        this.onSetUpChildLayoutParamsListener = onSetUpChildLayoutParamsListener;
    }

    public OnItemClickListener getmOnItemClickListener() {
        return mOnItemClickListener;
    }

    public void setmOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public List<Rect> getContentViewsGlobalVisibleRects() {
        final int childCount = getChildCount();
        if (childCount <= 0) return null;
        List<Rect> viewRects = new LinkedList<>();
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v != null) {
                Rect rect = new Rect();
                v.getGlobalVisibleRect(rect);
                viewRects.add(rect);
            }
        }
        return viewRects;
    }

    public List<Rect> getContentViewsDrawableRects() {
        final int childCount = getChildCount();
        if (childCount <= 0) return null;
        List<Rect> viewRects = new LinkedList<>();
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v != null) {
                Rect rect = getDrawableBounds((ImageView) v);
                viewRects.add(rect);
            }
        }
        return viewRects;
    }

    public List<Matrix> getContentViewsDrawableMatrixList() {
        final int childCount = getChildCount();
        if (childCount <= 0) return null;
        List<Matrix> viewMatrixs = new LinkedList<>();
        for (int i = 0; i < childCount; i++) {
            View v = getChildAt(i);
            if (v instanceof ImageView && ((ImageView) v).getDrawable() != null) {
                Matrix matrix = ((ImageView) v).getImageMatrix();
                viewMatrixs.add(matrix);
            }
        }
        return viewMatrixs;
    }

    private Rect getDrawableBounds(ImageView iv) {
        if (iv == null || iv.getDrawable() == null) return null;
        Drawable d = iv.getDrawable();
        Rect result = new Rect();
        Rect tDrawableRect = d.getBounds();
        Matrix drawableMatrix = iv.getImageMatrix();

        float[] values = new float[9];
        drawableMatrix.getValues(values);

        result.left = (int) values[Matrix.MTRANS_X];
        result.top = (int) values[Matrix.MTRANS_Y];
        result.right = (int) (result.left + tDrawableRect.width() * values[Matrix.MSCALE_X]);
        result.bottom = (int) (result.top + tDrawableRect.height() * values[Matrix.MSCALE_Y]);

        return result;
    }

    //------------------------------------------Interface-----------------------------------------------
    private OnSetUpChildLayoutParamsListener onSetUpChildLayoutParamsListener;

    public interface OnSetUpChildLayoutParamsListener {
        void onSetUpParams(ImageView child, LayoutParams p, int position, boolean isSingle);
    }

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(ImageView view, int position);
    }
}
