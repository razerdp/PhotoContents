package razerdp.github.com.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.security.MessageDigest;
import java.util.concurrent.ExecutionException;

import razerdp.github.com.demo.utils.rx.RxCall;
import razerdp.github.com.demo.utils.rx.RxHelper;
import razerdp.github.com.demo.utils.rx.RxTaskCall;

import static com.bumptech.glide.load.resource.bitmap.VideoDecoder.FRAME_OPTION;

/**
 * Created by 大灯泡 on 2019/4/10.
 * <p>
 * 图片加载单例 啊啊啊啊。。。好多重载，这时候有个kotlin多好啊噗
 */

public enum ImageLoadManager {
    INSTANCE;
    private final GlideDispatcher DISPATCHER = new GlideDispatcher();

    private static RequestOptions DEFAULT_REQUEST_OPTIONS = new RequestOptions()
            .placeholder(R.drawable.ic_loading)
            .error(R.drawable.ic_error);

    public void clearMemory(Context context) {
        Glide.get(context).clearMemory();
    }

    public static Glide getGlide(Context context) {
        return Glide.get(context);
    }

    public void loadImage(View target, Object from) {
        loadImage(target, from, R.drawable.ic_loading, R.drawable.ic_error);
    }

    public void loadImage(View target, Object from, @DrawableRes int errorImage) {
        loadImage(target, from, R.drawable.ic_loading, errorImage);
    }

    public void loadImage(View target, Object from, @DrawableRes int loadingImg, @DrawableRes int errorImage) {
        loadImage(target, from, 0, 0, loadingImg, errorImage);
    }

    public void loadImage(View target, Object from, int width, int hegith, @DrawableRes int loadingImg, @DrawableRes int errorImage) {
        RequestOptions options = getOption().placeholder(loadingImg).error(errorImage);
        if (width != 0 && hegith != 0) {
            options = options.override(width, hegith);
        }
        loadImage(target, from, options);
    }

    public void loadImage(View target, String from, @DrawableRes int loadingImg, @DrawableRes int errorImage, boolean cache) {
        RequestOptions options = getOption()
                .placeholder(loadingImg)
                .error(errorImage)
                .skipMemoryCache(!cache)
                .diskCacheStrategy(cache ? DiskCacheStrategy.AUTOMATIC : DiskCacheStrategy.NONE);
        loadImage(target, from, options);
    }

    public RequestOptions getOption() {
        try {
            return DEFAULT_REQUEST_OPTIONS.clone();
        } catch (Exception e) {
            return new RequestOptions()
                    .placeholder(R.drawable.ic_loading)
                    .error(R.drawable.ic_error);
        }
    }

    @SuppressLint("CheckResult")
    public void loadImage(View target, Object from, RequestOptions options) {
        DISPATCHER.getGlide(target, from).apply(options).into((ImageView) target);
    }

    public void loadRoundImage(ImageView target, Object from, @Px int radius) {
        loadRoundImage(target, from, RoundedCornersTransformation.CornerType.ALL, radius);
    }

    public void loadRoundImage(ImageView target, Object from, RoundedCornersTransformation.CornerType type, @Px int radius) {
        loadRoundImage(target, from, R.drawable.ic_error, R.drawable.ic_error, type, radius);
    }


    public void loadRoundImage(ImageView target, Object from, @DrawableRes int loadingImg, @DrawableRes int errorImage, RoundedCornersTransformation.CornerType type, @Px int radius) {
        RequestOptions options = getOption()
                .placeholder(loadingImg)
                .error(errorImage);
        if (target.getScaleType() == ImageView.ScaleType.CENTER_CROP) {
            options.transform(new CenterCrop(), new RoundedCornersTransformation(radius, 0, type));
        } else if (target.getScaleType() == ImageView.ScaleType.CENTER_INSIDE) {
            options.transform(new CenterInside(), new RoundedCornersTransformation(radius, 0, type));
        } else if (target.getScaleType() == ImageView.ScaleType.FIT_CENTER) {
            options.transform(new FitCenter(), new RoundedCornersTransformation(radius, 0, type));
        } else {
            options.transform(new RoundedCornersTransformation(radius, 0, type));
        }
        loadImage(target, from, options);
    }

    public void loadBitmap(Context context, String imageUrl, final OnLoadBitmapListener listener) {
        Log.i("下载图片", "url=" + imageUrl);
        RequestOptions options = DEFAULT_REQUEST_OPTIONS.diskCacheStrategy(DiskCacheStrategy.ALL).skipMemoryCache(false);
        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .apply(options)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        if (listener != null) {
                            listener.onFailed(new IllegalArgumentException("download failed"));
                        }
                    }

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        if (listener != null) {
                            listener.onSuccess(resource);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    public void loadVideoScreenshot(final Context context, String uri, ImageView imageView, long frameTimeMicros) {
        RequestOptions requestOptions = RequestOptions.frameOf(frameTimeMicros)
                .set(FRAME_OPTION, MediaMetadataRetriever.OPTION_CLOSEST)
                .transform(new BitmapTransformation() {
                    @Override
                    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
                        return toTransform;
                    }

                    @Override
                    public void updateDiskCacheKey(MessageDigest messageDigest) {
                        try {
                            messageDigest.update((context.getPackageName() + "RotateTransform").getBytes("utf-8"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        Glide.with(context).load(uri).apply(requestOptions).into(imageView);
    }

    public interface OnLoadBitmapListener {
        void onSuccess(Bitmap bitmap);

        void onFailed(Exception e);
    }

    public void getCache(final Object o, final RxCall<String> cb) {
        if (o == null || cb == null) return;
        if (o instanceof File) {
            cb.onCall(((File) o).getAbsolutePath());
            return;
        }
        RxHelper.runOnBackground(new RxTaskCall<String>() {
            @Override
            public String doInBackground() {
                File file = null;
                try {
                    file = Glide.with(AppContext.getAppContext()).asFile().onlyRetrieveFromCache(true).load(o).submit().get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return (file != null && file.exists()) ? file.getAbsolutePath() : null;
            }

            @Override
            public void onResult(String result) {
                cb.onCall(result);
            }
        });

    }

    private class GlideDispatcher {

        RequestManager getRequestManager(View v, Object o) {
            RequestManager manager;
            if (v.getContext() instanceof Activity) {
                manager = Glide.with((Activity) v.getContext());
            } else {
                manager = Glide.with(v.getContext() == null ? AppContext.getAppContext() : v.getContext());
            }
            return manager;
        }

        RequestBuilder<Drawable> getGlide(View v, Object o) {
            RequestManager manager = getRequestManager(v, o);
            if (o instanceof String) {
                return getGlideString(manager, (String) o);
            } else if (o instanceof Integer) {
                return getGlideInteger(manager, (Integer) o);
            } else if (o instanceof Uri) {
                return getGlideUri(manager, (Uri) o);
            } else if (o instanceof File) {
                return getGlideFile(manager, (File) o);
            }
            return getGlideString(manager, "");
        }

        private RequestBuilder<Drawable> getGlideString(RequestManager manager, String str) {
            return manager.load(str);
        }

        private RequestBuilder<Drawable> getGlideInteger(RequestManager manager, int source) {
            return manager.load(source);
        }

        private RequestBuilder<Drawable> getGlideUri(RequestManager manager, Uri uri) {
            return manager.load(uri);
        }

        private RequestBuilder<Drawable> getGlideFile(RequestManager manager, File file) {
            return manager.load(file);
        }
    }

    public static abstract class OnLoadBitmapListenerAdapter implements OnLoadBitmapListener {

        @Override
        public void onSuccess(Bitmap bitmap) {

        }

        @Override
        public void onFailed(Exception e) {

        }
    }

}
