package io.smallrye.reactive.operators.multi;

import io.smallrye.reactive.Multi;
import io.smallrye.reactive.subscription.SwitchableSubscriptionSubscriber;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.Objects;

/**
 * Switches to another Multi if the upstream is empty (completes without having emitted any items).
 */
public final class MultiSwitchOnEmptyOp<T> extends AbstractMultiWithUpstream<T, T> {

    private final Publisher<? extends T> alternative;

    public MultiSwitchOnEmptyOp(Multi<? extends T> upstream, Publisher<? extends T> alternative) {
        super(upstream);
        this.alternative = Objects.requireNonNull(alternative, "alternative");
    }

    @Override
    public void subscribe(Subscriber<? super T> actual) {
        SwitchIfEmptySubscriber<T> parent = new SwitchIfEmptySubscriber<>(actual, alternative);
        actual.onSubscribe(parent);
        upstream.subscribe(parent);
    }

    static final class SwitchIfEmptySubscriber<T> extends SwitchableSubscriptionSubscriber<T> {

        private final Publisher<? extends T> alternative;
        boolean notEmpty;

        SwitchIfEmptySubscriber(Subscriber<? super T> downstream,
                Publisher<? extends T> alternative) {
            super(downstream);
            this.alternative = alternative;
        }

        @Override
        public void onNext(T t) {
            if (!notEmpty) {
                notEmpty = true;
            }
            downstream.onNext(t);
        }

        @Override
        public void onComplete() {
            if (!notEmpty) {
                notEmpty = true;
                alternative.subscribe(this);
            } else {
                downstream.onComplete();
            }
        }
    }
}