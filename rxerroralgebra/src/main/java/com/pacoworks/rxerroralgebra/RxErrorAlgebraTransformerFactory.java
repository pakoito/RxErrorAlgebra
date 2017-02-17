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

import com.pacoworks.rxsealedunions.Union2;

import io.reactivex.ObservableTransformer;

/**
 * Provides a factory function for {@link ObservableTransformer} used to transform Observable errors into algebras
 *
 * @param <E> type of the errors
 * @param <V> type of the values
 * @author pakoito
 */
public interface RxErrorAlgebraTransformerFactory<E, V> {
    /**
     * Creates an {@link ObservableTransformer} using {@link E} for errors and {@link V} for values
     *
     * @return a transformer
     */
    ObservableTransformer<V, Union2<E, V>> toAlgebra();
}
