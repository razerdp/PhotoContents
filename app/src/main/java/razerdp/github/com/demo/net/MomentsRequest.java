package razerdp.github.com.demo.net;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import razerdp.github.com.demo.model.entity.MomentsInfo;
import razerdp.github.com.demo.model.entity.MomentsInfo.MomentsFields;
import razerdp.github.com.demo.net.base.BaseRequestClient;
import razerdp.github.com.demo.utils.ToolUtil;

/**
 * Created by 大灯泡 on 2016/10/27.
 * <p>
 * 朋友圈时间线请求
 */

public class MomentsRequest extends BaseRequestClient<List<MomentsInfo>> {

    private int count = 10;
    private int curPage = 0;

    public MomentsRequest() {
    }

    public MomentsRequest setCount(int count) {
        this.count = (count <= 0 ? 10 : count);
        return this;
    }

    public MomentsRequest setCurPage(int page) {
        this.curPage = page;
        return this;
    }


    @Override
    protected void executeInternal(final int requestType, boolean showDialog) {
        BmobQuery<MomentsInfo> query = new BmobQuery<>();
        query.order("-createdAt");
        query.include(MomentsFields.AUTHOR_USER + "," + MomentsFields.HOST);
        query.setLimit(count);
        query.setSkip(curPage * count);
        query.findObjects(new FindListener<MomentsInfo>() {
            @Override
            public void done(List<MomentsInfo> list, BmobException e) {
                if (!ToolUtil.isListEmpty(list)) {
                    onResponseSuccess(list, getRequestType());
                }
            }
        });

    }

}
