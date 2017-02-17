package com.pacoworks.rxerroralgebra;

import com.pacoworks.rxsealedunions.Union2;

import io.reactivex.ObservableTransformer;

/**
 * Provides a factory function for {@link ObservableTransformer} used to transform Observable errors into algebras
 *
 * @param <E> type of the errors
 * @param <V> type of the values
 */
public interface RxErrorAlgebraTransformerFactory<E, V> {
    /**
     *
     * @return
     */
    ObservableTransformer<V, Union2<E, V>> asAlgebra();
}
