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
import org.junit.Assert;
import org.junit.Test;

import com.pacoworks.rxsealedunions.Union2;
import com.pacoworks.rxsealedunions.generic.UnionFactories;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class RxErrorAlgebraTransformerFactoryTest {
    @Test
    public void error_ToAlgebra_LeftSide() {
        final int errorCode = 20;
        final RxErrorAlgebraTransformerFactory<Throwable, String> converter = RxErrorAlgebra.create();
        final Union2<Throwable, String> result = Observable.<String> error(new TestException(errorCode))
                .compose(converter.toAlgebra()).blockingFirst();
        result.continued(this.<Throwable> isEqual(new TestException(errorCode)), this.<String> error());
    }

    @Test
    public void success_ToAlgebra_RightSide() {
        final String expected = "Paco";
        final RxErrorAlgebraTransformerFactory<Throwable, String> converter = RxErrorAlgebra.create();
        final Union2<Throwable, String> result = Observable.just(expected).compose(converter.toAlgebra())
                .blockingFirst();
        result.continued(this.<Throwable> error(), this.isEqual(expected));
    }

    @Test
    public void error_ToAlgebraWithTag_LeftSide() {
        final int errorCode = 20;
        final int tag = 1;
        final RxErrorAlgebraTransformerFactory<Pair<Integer, Throwable>, String> converter = RxErrorAlgebra
                .createWithTag(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return tag;
                    }
                });
        final Union2<Pair<Integer, Throwable>, String> result = Observable.<String> error(new TestException(errorCode))
                .compose(converter.toAlgebra()).blockingFirst();
        result.continued(this.isEqual(Pair.<Integer, Throwable> with(tag, new TestException(errorCode))),
                this.<String> error());
    }

    @Test
    public void success_ToAlgebraWithTag_RightSide() {
        final int tag = 1;
        final String expected = "Paco";
        final RxErrorAlgebraTransformerFactory<Pair<Integer, Throwable>, String> converter = RxErrorAlgebra
                .createWithTag(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return tag;
                    }
                });
        final Union2<Pair<Integer, Throwable>, String> result = Observable.just(expected).compose(converter.toAlgebra())
                .blockingFirst();
        result.continued(this.<Pair<Integer, Throwable>> error(), this.isEqual(expected));
    }

    @Test
    public void error_ToAlgebraCustom_LeftSide() {
        final int errorCode = 20;
        final RxErrorAlgebraTransformerFactory<Throwable, String> converter = RxErrorAlgebra
                .createCustom(new Function<Throwable, ObservableSource<? extends Union2<Throwable, String>>>() {
                    @Override
                    public ObservableSource<? extends Union2<Throwable, String>> apply(Throwable throwable)
                            throws Exception {
                        return Observable.just(UnionFactories.<Throwable, String> doubletFactory().first(throwable));
                    }
                }, new Function<String, Union2<Throwable, String>>() {
                    @Override
                    public Union2<Throwable, String> apply(String s) throws Exception {
                        return UnionFactories.<Throwable, String> doubletFactory().second(s);
                    }
                });
        final Union2<Throwable, String> result = Observable.<String> error(new TestException(errorCode))
                .compose(converter.toAlgebra()).blockingFirst();
        result.continued(this.<Throwable> isEqual(new TestException(errorCode)), this.<String> error());
    }

    @Test
    public void success_ToAlgebraCustom_RightSide() {
        final int tag = 1;
        final String expected = "Paco";
        final RxErrorAlgebraTransformerFactory<Pair<Integer, Throwable>, String> converter = RxErrorAlgebra
                .createWithTag(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return tag;
                    }
                });
        final Union2<Pair<Integer, Throwable>, String> result = Observable.just(expected).compose(converter.toAlgebra())
                .blockingFirst();
        result.continued(this.<Pair<Integer, Throwable>> error(), this.isEqual(expected));
    }

    private <T> Consumer<T> isEqual(final T expected) {
        return new Consumer<T>() {
            @Override
            public void accept(T value) throws Exception {
                Assert.assertEquals(expected, value);
            }
        };
    }

    private <T> Consumer<T> error() {
        return new Consumer<T>() {
            @Override
            public void accept(T value) throws Exception {
                Assert.fail();
            }
        };
    }

    private static final class TestException extends Throwable {
        private final int code;

        private TestException(int code) {
            this.code = code;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            TestException that = (TestException)o;
            return code == that.code;
        }

        @Override
        public int hashCode() {
            return code;
        }

        @Override
        public String toString() {
            return "TestException(" + "code=" + code + ')';
        }
    }
}
