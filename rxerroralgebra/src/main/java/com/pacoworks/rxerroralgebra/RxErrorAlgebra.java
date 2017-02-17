/*
 * Copyright (c) pakoito 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pacoworks.rxerroralgebra;

import java.util.concurrent.Callable;

import org.javatuples.Pair;

import com.pacoworks.rxsealedunions.Union2;
import com.pacoworks.rxsealedunions.generic.UnionFactories;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;

/**
 * Contains helpers to convert Observable error handling into a {@link Union2} of E and V to be used in
 * {@link Observable#compose(ObservableTransformer)}
 * </p>
 * The right side V represents a successful chain operation.
 * </p>
 * The left side E represents a failure in the chain.
 * 
 * @author pakoito
 */
public final class RxErrorAlgebra {
    private RxErrorAlgebra() {
        // No public instances
    }

    /**
     * Creates an {@link RxErrorAlgebraTransformerFactory} using {@link Throwable} for errors and {@link V} for values
     * 
     * @param <V> type of the value
     * @return a factory
     */
    public static <V> RxErrorAlgebraTransformerFactory<Throwable, V> create() {
        return new RxErrorAlgebraTransformerFactory<Throwable, V>() {
            @Override
            public ObservableTransformer<V, Union2<Throwable, V>> toAlgebra() {
                return RxErrorAlgebra.toAlgebra();
            }
        };
    }

    /**
     * Creates an {@link RxErrorAlgebraTransformerFactory} using {@link Pair} of tag {@link T} with {@link Throwable}
     * for errors and {@link V} for values
     * 
     * @param <V> type of the value
     * @param <T> type of the tag
     * @param tagger a function returning the tag associated with an error
     * @return a factory
     */
    public static <V, T> RxErrorAlgebraTransformerFactory<Pair<T, Throwable>, V> createWithTag(
            final Callable<T> tagger) {
        return new RxErrorAlgebraTransformerFactory<Pair<T, Throwable>, V>() {
            @Override
            public ObservableTransformer<V, Union2<Pair<T, Throwable>, V>> toAlgebra() {
                return toAlgebraWithTag(tagger);
            }
        };
    }

    /**
     * Creates an {@link RxErrorAlgebraTransformerFactory} using {@link E} for errors and {@link V} for values
     * 
     * @param <E> type of the error
     * @param <V> type of the value
     * @param leftFunction function that creates the left failure side of the union
     * @param rightFunction function that creates the right success side of the union
     * @return a factory
     */
    public static <E, V> RxErrorAlgebraTransformerFactory<E, V> createCustom(
            final Function<Throwable, ObservableSource<? extends Union2<E, V>>> leftFunction,
            final Function<V, Union2<E, V>> rightFunction) {
        return new RxErrorAlgebraTransformerFactory<E, V>() {
            @Override
            public ObservableTransformer<V, Union2<E, V>> toAlgebra() {
                return toAlgebraCustom(leftFunction, rightFunction);
            }
        };
    }

    /**
     * Creates an {@link ObservableTransformer} using {@link Throwable} for errors and {@link V} for values
     *
     * @param <V> type of the value
     * @return a transformer
     */
    public static <V> ObservableTransformer<V, Union2<Throwable, V>> toAlgebra() {
        return new ObservableTransformer<V, Union2<Throwable, V>>() {
            @Override
            public ObservableSource<Union2<Throwable, V>> apply(Observable<V> upstream) {
                return upstream.map(RxErrorAlgebra.<V> getRightSimpleFunction())
                        .onErrorResumeNext(RxErrorAlgebra.<V> getLeftSimpleFunction());
            }
        };
    }

    /**
     * Creates an {@link ObservableTransformer} using {@link Pair} of tag {@link T} with {@link Throwable} for errors
     * and {@link V} for values
     *
     * @param <V> type of the value
     * @param <T> type of the tag
     * @param tagger a function returning the tag associated with an error
     * @return a transformer
     */
    public static <V, T> ObservableTransformer<V, Union2<Pair<T, Throwable>, V>> toAlgebraWithTag(
            final Callable<T> tagger) {
        return new ObservableTransformer<V, Union2<Pair<T, Throwable>, V>>() {
            @Override
            public ObservableSource<Union2<Pair<T, Throwable>, V>> apply(Observable<V> upstream) {
                return upstream.map(RxErrorAlgebra.<V, T> getRightFunctionWithTag())
                        .onErrorResumeNext(RxErrorAlgebra.<V, T> getLeftFunctionWithTag(tagger));
            }
        };
    }

    /**
     * Creates an {@link ObservableTransformer} using {@link E} for errors and {@link V} for values
     *
     * @param <E> type of the error
     * @param <V> type of the value
     * @param leftFunction function that creates the left failure side of the union
     * @param rightFunction function that creates the right success side of the union
     * @return a transformer
     */
    public static <V, E> ObservableTransformer<V, Union2<E, V>> toAlgebraCustom(
            final Function<Throwable, ObservableSource<? extends Union2<E, V>>> leftFunction,
            final Function<V, Union2<E, V>> rightFunction) {
        return new ObservableTransformer<V, Union2<E, V>>() {
            @Override
            public ObservableSource<Union2<E, V>> apply(Observable<V> upstream) {
                return upstream.map(rightFunction).onErrorResumeNext(leftFunction);
            }
        };
    }

    private static <V> Function<Throwable, ObservableSource<? extends Union2<Throwable, V>>> getLeftSimpleFunction() {
        return new Function<Throwable, ObservableSource<? extends Union2<Throwable, V>>>() {
            @Override
            public ObservableSource<? extends Union2<Throwable, V>> apply(Throwable throwable) throws Exception {
                return Observable.just(UnionFactories.<Throwable, V> doubletFactory().first(throwable));
            }
        };
    }

    private static <V> Function<V, Union2<Throwable, V>> getRightSimpleFunction() {
        return new Function<V, Union2<Throwable, V>>() {
            @Override
            public Union2<Throwable, V> apply(V t) throws Exception {
                return UnionFactories.<Throwable, V> doubletFactory().second(t);
            }
        };
    }

    private static <V, T> Function<Throwable, ObservableSource<? extends Union2<Pair<T, Throwable>, V>>> getLeftFunctionWithTag(
            final Callable<T> tagger) {
        return new Function<Throwable, ObservableSource<? extends Union2<Pair<T, Throwable>, V>>>() {
            @Override
            public ObservableSource<? extends Union2<Pair<T, Throwable>, V>> apply(Throwable throwable)
                    throws Exception {
                return Observable.just(UnionFactories.<Pair<T, Throwable>, V> doubletFactory()
                        .first(Pair.with(tagger.call(), throwable)));
            }
        };
    }

    private static <V, T> Function<V, Union2<Pair<T, Throwable>, V>> getRightFunctionWithTag() {
        return new Function<V, Union2<Pair<T, Throwable>, V>>() {
            @Override
            public Union2<Pair<T, Throwable>, V> apply(V t) throws Exception {
                return UnionFactories.<Pair<T, Throwable>, V> doubletFactory().second(t);
            }
        };
    }
}
