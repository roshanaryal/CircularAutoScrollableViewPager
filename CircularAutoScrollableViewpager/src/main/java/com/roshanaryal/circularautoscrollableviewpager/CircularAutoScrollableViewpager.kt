package com.roshanaryal.circularautoscrollableviewpager

import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.PageTransformer
import java.util.Timer
import java.util.TimerTask
import kotlin.math.abs

/**
 * AutoScrollViewPager is a customizable ViewPager2 component for creating auto-scrolling carousels
 * with optional custom item view binding.
 *
 * @param V The type of the RecyclerView.ViewHolder for item views.
 * @param T The type of items displayed in the ViewPager2.
 *
 */
 class CircularAutoScrollableViewpager<V:RecyclerView.ViewHolder,T> {
    companion object {
        const val TAG = "AutoScrollViewPager "
    }

    private var viewPager: ViewPager2
    private var onCreateViewHolder:OnCreateItemViewHolder<V>
    private var onBindItemView: OnBindItemView<V,T>
    private var transformer: PageTransformer? =null
    private var autoScrollDelayInitial: Long = 0
    private var autoScrollTimer: Timer? = null;
    private var currentPosition = 0

    private var isStartedDragging = false
    private var isStartedScrolling = false
    var diffUtilsCallback :DiffUtil.ItemCallback<T>?=null
    private var mAdapter: AutoScrollCarouselItemAdapter

    private var autoScrollDelay: Long = 3000


    private constructor(builder: Builder<V,T>){
        this.viewPager=builder.getViewpager()
        this.onCreateViewHolder=builder.getOnCreateItemViewHolder()
        this.onBindItemView=builder.getOnBindItemView()
        autoScrollDelay=builder.getAutoScrollDelay()
        builder.getTransformer()?.let {
            transformer=it
        }?: kotlin.run {
            transformer=getDefaultPageTransformer()
        }

        builder.getDiffUtilsCallBack()?.let {
            diffUtilsCallback=it
        }?: kotlin.run {
            diffUtilsCallback = DiffUtilsCallBack()
        }

        mAdapter = AutoScrollCarouselItemAdapter()
        viewPager.adapter = mAdapter
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager.offscreenPageLimit = 3
        viewPager.setPageTransformer(transformer)
        initAutoScroll()


    }

    private fun getDefaultPageTransformer(): PageTransformer {
        return PageTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.14f
        }
    }

    /**
     * Binds a list of items to the AutoScrollViewPager for auto-scrolling.
     *
     * @param itemList The list of items to be displayed in the carousel.
     */
    fun bind(itemList: List<T>) {
        val newList = listOf(itemList.last()) + itemList + listOf(itemList.first())
        mAdapter.submitList(newList)
        startAutoScrollTimer()
    }

    private fun initAutoScroll() {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (viewPager.scrollState == ViewPager2.SCROLL_STATE_DRAGGING) {
                    if (isStartedDragging) {
                        stopAutoScrollTimer()
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    when (viewPager.currentItem) {
                        mAdapter.itemCount - 1 -> {
                            viewPager.setCurrentItem(1, false)
                        }
                        0 -> {
                            if (isStartedScrolling) {
                                viewPager.setCurrentItem(mAdapter.itemCount - 2, false)
                            }
                        }
                    }
                    currentPosition = viewPager.currentItem
                    if (isStartedDragging) { ///if user has started swiping viewpager item we will start timer when item will be in idle condition
                        isStartedDragging = false
                        startAutoScrollTimer()
                    }
                }
            }
        })
        isStartedScrolling = false
    }



    /**
     * Sets an onTouch listener for handling touch events.
     *
     * This function will automatically stops and starts auto scroll when user touches/untouches  item .
     *
     * @param p1 The [MotionEvent] to handle.
     */
    fun startStopAutoScrollOnTouch(p1: MotionEvent) {
        isStartedDragging = true
        if (p1.action == MotionEvent.ACTION_DOWN || p1.action == MotionEvent.ACTION_MOVE) {
            stopAutoScrollTimer()
        } else if (p1.action == MotionEvent.ACTION_UP || p1.action == MotionEvent.ACTION_CANCEL) {
            startAutoScrollTimer()
        }
    }


    /**
     * Starts auto scroll
     */
    fun startAutoScroll(){
        if (!isStartedScrolling){
            return
        }
        startAutoScrollTimer()
    }

    private fun startAutoScrollTimer() {
        val delay = if (isStartedScrolling) autoScrollDelay else autoScrollDelayInitial
        Log.d(TAG, "startAutoScroll timer: ")
        stopAutoScrollTimer()
        autoScrollTimer = Timer()
        autoScrollTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                Log.d(TAG, "run: position $currentPosition")
                isStartedScrolling =true
                currentPosition++
                viewPager.post {
                    viewPager.setCurrentItem(currentPosition, true)
                }
            }
        }, delay, autoScrollDelay)
    }


    /**
     * Starts auto-scrolling.
     */
    fun stopAutoScroll(){
        isStartedDragging=false
        stopAutoScrollTimer()
    }

    private fun stopAutoScrollTimer() {
        autoScrollTimer?.cancel()
        autoScrollTimer = null
    }



    /**
     * Builder class for creating instances of AutoScrollViewPager with custom settings.
     *
     * @param yourViewPager The ViewPager2 instance to be used.
     * @param onCreateItemViewHolder Callback for creating a RecyclerView.ViewHolder for item views.
     * @param onBindItemView Callback for binding item views with data.
     */
    class Builder<V : RecyclerView.ViewHolder,T> {
        private var autoScrollDelay: Long = 3000
        private  var viewPager: ViewPager2
        private var onCreateItemViewHolder:OnCreateItemViewHolder<V>
        private var onBindItemView: OnBindItemView<V,T>
        private var transformer: PageTransformer? =null
        private var diffUtilItemCallback:DiffUtil.ItemCallback<T>?=null


        constructor(yourViewPager: ViewPager2,onCreateItemViewHolder: OnCreateItemViewHolder<V>,onBindItemView: OnBindItemView<V,T>) {
            viewPager = yourViewPager
            this.onCreateItemViewHolder =onCreateItemViewHolder
            this.onBindItemView=onBindItemView
        }

        /**
         * Sets the auto-scroll delay in milliseconds.
         *
         * @param delayed The delay between auto-scrolling items.
         * @return This builder for method chaining.
         */
        fun setAutoScrollDelay(delayed: Long): Builder<V,T> {
            this.autoScrollDelay = delayed
            return this
        }

        /**
         * Sets a custom PageTransformer for ViewPager2 item animations.
         *
         * @param transformer The PageTransformer to be used.
         * @return This builder for method chaining.
         */
        fun setPagerTransformer(transformer: PageTransformer):Builder<V,T> {
            this.transformer = transformer
            return this
        }


        fun getViewpager():ViewPager2{
            return viewPager
        }

        fun getTransformer(): PageTransformer?{
            return transformer
        }



        fun getAutoScrollDelay():Long{
            return autoScrollDelay
        }

        fun getOnCreateItemViewHolder():OnCreateItemViewHolder<V>{
            return onCreateItemViewHolder
        }


        fun getOnBindItemView():OnBindItemView<V,T>{
            return onBindItemView
        }

        /**
         * Sets a custom DiffUtil.ItemCallback for efficient item updates when the data changes.
         *
         * @param callBack The custom DiffUtil.ItemCallback.
         * @return This builder for method chaining.
         * default on item same and oncontent same false for every item
         */
        fun setDiffUtilsCallBack(callBack: DiffUtil.ItemCallback<T>):Builder<V,T> {
            this.diffUtilItemCallback=callBack
            return this
        }

        fun getDiffUtilsCallBack():DiffUtil.ItemCallback<T>?{
            return diffUtilItemCallback
        }

        /**
         * Builds and returns an AutoScrollViewPager instance with the configured settings.
         *
         * @return The AutoScrollViewPager instance.
         */
        fun Build():CircularAutoScrollableViewpager<V,T>{
            return CircularAutoScrollableViewpager(this)
        }


    }

    interface OnBindItemView<V,T>{
        fun bindItemView(viewholder: V, item: T)

    }

    interface OnCreateItemViewHolder<V>{
        fun createViewHolder(parent: ViewGroup, viewType: Int):V
    }




    inner class AutoScrollCarouselItemAdapter :
        androidx.recyclerview.widget.ListAdapter<T,V>(diffUtilsCallback!!) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): V {
            return onCreateViewHolder.createViewHolder(parent, viewType)
        }

        override fun onBindViewHolder(holder: V, position: Int) {
            onBindItemView.bindItemView(holder, getItem(position))
        }
    }

    inner class DiffUtilsCallBack : DiffUtil.ItemCallback<T>(){
        override fun areItemsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
            return false
        }

    }



}