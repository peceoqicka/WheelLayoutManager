package com.peceoqicka.demo.wheelayoutmanager.util

import android.app.Activity
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * <pre>
 *      author  :   peceoqicka
 *      time    :   2019/3/14
 *      version :   1.0
 *      desc    :
 * </pre>
 */
fun timer(timeInMillis: Long, onSuccess: (Long) -> Unit): Disposable {
    return Observable.timer(timeInMillis, TimeUnit.MILLISECONDS)
        .asyncRequest()
        .subscribe(onSuccess)
}

fun <T> Observable<T>.asyncRequest(): Observable<T> {
    return this.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
}

fun <T> Observable<T>.subscribeIgnoreResult(next: (T) -> Unit): Disposable {
    return this.subscribe(next)
}

fun Activity.color(@ColorRes colorResId: Int): Int {
    return ContextCompat.getColor(this, colorResId)
}