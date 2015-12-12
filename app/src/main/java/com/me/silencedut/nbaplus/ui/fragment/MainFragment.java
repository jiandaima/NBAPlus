package com.me.silencedut.nbaplus.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

import com.me.silencedut.nbaplus.R;
import com.me.silencedut.nbaplus.app.AppService;
import com.me.silencedut.nbaplus.data.Constant;
import com.me.silencedut.nbaplus.event.NewsEvent;
import com.me.silencedut.nbaplus.model.News;
import com.me.silencedut.nbaplus.ui.adapter.RecycleAdapter.MainAdapter;
import com.me.silencedut.nbaplus.utils.AppUtils;
import com.me.silencedut.nbaplus.utils.NumericalUtil;

import butterknife.Bind;

/**
 * Created by SilenceDut on 2015/11/28.
 */
public class MainFragment extends NewsFragment{

    private static final int MIN_ITEM_SIZE=10;
    private static final int ANIM_DURATION_TOOLBAR = 300;
    @Bind(R.id.mian_title)
    View mainTitle;
    public static MainFragment newInstance() {
        MainFragment mainFragment = new MainFragment();
        return mainFragment;
    }


    @Override
    void setAdapter() {
        mainTitle.setVisibility(View.VISIBLE);
        mLoadAdapter=new MainAdapter(getActivity(),mNewsListEntity);
        mNewsListView.setAdapter(mLoadAdapter);
        startIntroAnimation();
    }

    private void initCaChe() {
        AppService.getInstance().initNews(getTaskId(), Constant.NEWSTYPE.NEWS.getNewsType());
    }

    @Override
    public void onRefresh() {
        AppService.getInstance().updateNews(getTaskId(), Constant.NEWSTYPE.NEWS.getNewsType());
    }


    @Override
    public void onLoadMore() {
        if (mLoadAdapter.canLoadMore()) {
            mLoadAdapter.setLoading(true);
            mLoadAdapter.notifyItemChanged(mLoadAdapter.getItemCount() - 1);
            AppService.getInstance().loadMoreNews(getTaskId(),Constant.NEWSTYPE.NEWS.getNewsType(), mNewsId);
        }
    }


    public void onEventMainThread(NewsEvent newsEvent) {
        if(newsEvent!=null&&Constant.NEWSTYPE.NEWS.getNewsType().equals(newsEvent.getNewsType())) {
            mNewsEvent=newsEvent;
            if(Constant.Result.FAIL.equals(newsEvent.getEventResult())) {
                updateView(newsEvent);
            }else {
                News news = newsEvent.getNews();
                mNewsId = news.getNextId();
                switch (newsEvent.getNewsWay()) {
                    case INIT:
                        mNewsListEntity.clear();
                        mNewsListEntity.addAll(news.getNewslist());
                        mLoadAdapter.updateItem(true);
                        break;
                    case UPDATE:
                        mNewsListEntity.clear();
                        mNewsListEntity.addAll(news.getNewslist());
                        //if the data get by refresh is less ,load more instantly
                        if (mNewsListEntity.size() < MIN_ITEM_SIZE) {
                            AppService.getInstance().loadMoreNews(getTaskId(), Constant.NEWSTYPE.NEWS.getNewsType(), mNewsId);
                        } else {
                            stopRefreshing();
                            mLoadAdapter.updateItem(false);
                        }
                        break;
                    case LOADMORE:
                        mNewsListEntity.addAll(news.getNewslist());
                        if (mNewsListEntity.size() < MIN_ITEM_SIZE) {
                            AppService.getInstance().loadMoreNews(getTaskId(), Constant.NEWSTYPE.NEWS.getNewsType(), mNewsId);
                        } else {
                            stopAll();
                            mLoadAdapter.updateItem(false);
                        }
                        break;
                    default:
                        break;
                }
                if(Constant.GETNEWSWAY.UPDATE.equals(newsEvent.getNewsWay())){
                    AppUtils.showSnackBar(newsContainer, R.string.load_success);
                }
            }
        }
    }

    @Override
    protected int getTitle() {
        return R.string.main;
    }

    private void startIntroAnimation() {
        int actionbarSize = NumericalUtil.dp2px(56);
        mToolBar.setTranslationY(-actionbarSize);
        mainTitle.setTranslationY(-actionbarSize);

        mToolBar.animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(300);
        mainTitle.animate()
                .translationY(0)
                .setDuration(ANIM_DURATION_TOOLBAR)
                .setStartDelay(400)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //                        startContentAnimation();
                        initCaChe();
                    }
                }).start();
    }


}
