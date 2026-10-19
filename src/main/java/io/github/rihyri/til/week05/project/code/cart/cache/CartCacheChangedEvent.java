package io.github.rihyri.til.week05.project.code.cart.cache;

public class CartCacheChangedEvent {

    private final Long userId;

    public CartCacheChangedEvent(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }
}
