package razerdp.github.com.widget.adapter.observer;

/**
 * Created by 大灯泡 on 2019/1/14.
 */
public abstract class TypeAdapterDataObserver {
    public abstract void onChanged();

    public void onItemChanged(int position) {
    }

    public void onItemRangeChanged(int positionStart, int itemCount) {
    }
}
