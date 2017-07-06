package com.demai.util;

import java.util.concurrent.ForkJoinPool;

/**
 * Created by dear on 16/5/3.
 */
public class PoolUtil {

    public static ForkJoinPool pool = new ForkJoinPool(100);
}
