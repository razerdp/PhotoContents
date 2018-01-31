package razerdp.github.com.widget.adapter;

import android.view.ViewGroup;
import android.widget.ImageView;

import razerdp.github.com.widget.adapter.observer.PhotoAdapterObservable;
import razerdp.github.com.widget.adapter.observer.PhotoBaseDataObserver;


/**
 * Created by 大灯泡 on 2016/11/9.
 */

public abstract class PhotoContentsBaseAdapter {

    private float singleAspectRatio = 16f / 9f;

    private PhotoAdapterObservable mObservable = new PhotoAdapterObservable();


    public void registerDataSetObserver(PhotoBaseDataObserver observer) {
        mObservable.registerObserver(observer);

    }

    public void unregisterDataSetObserver(PhotoBaseDataObserver observer) {
        mObservable.unregisterObserver(observer);
    }

    public void notifyDataChanged() {
        mObservable.notifyChanged();
        mObservable.notifyInvalidated();
    }


    public abstract ImageView onCreateView(ImageView convertView, ViewGroup parent, int position);

    public abstract void onBindData(int position, ImageView convertView);

    public abstract int getCount();

    public int getMaxSingleWidth(int parentWidth) {
        return parentWidth * 2 / 3;
    }

    public int getMaxSingleHeight(int parentWidth) {
        return (int) (getMaxSingleWidth(parentWidth) / singleAspectRatio);
    }

    public int getLineItemCount() {
        return 3;
    }

    public float getItemMarginInDp() {
        return 4f;
    }

    public boolean isNewLine(int position, int itemCount) {
        return (itemCount == 4 && position % 2 == 0) || (itemCount != 1 && itemCount != 4 && position % getLineItemCount() == 0);
    }

}
