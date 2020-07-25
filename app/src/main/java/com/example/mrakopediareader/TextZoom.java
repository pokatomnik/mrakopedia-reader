package com.example.mrakopediareader;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class TextZoom {
    private final int min;
    private final int max;
    private final int step;
    private final int initial;
    private final BehaviorSubject<Integer> zoomSub$;

    public TextZoom(int initial, int min, int max, int step) {
        this.min = min;
        this.max = max;
        this.step = step;

        int initialZoom = (initial < min) || (initial > max) ? min : initial;

        this.initial = initialZoom;
        this.zoomSub$ = BehaviorSubject.createDefault(initialZoom);
    }

    public Observable<Integer> getObservable() {
        return zoomSub$;
    }

    public void zoomIn() {
        final int currentZoom = zoomSub$.getValue();
        if (currentZoom + step > max) {
            return;
        }
        zoomSub$.onNext(currentZoom + step);
    }

    public void zoomOut() {
        final int currentZoom = zoomSub$.getValue();
        if (currentZoom - step < min) {
            return;
        }
        zoomSub$.onNext(currentZoom - step);
    }

    public void reset() {
        this.zoomSub$.onNext(initial);
    }
}
