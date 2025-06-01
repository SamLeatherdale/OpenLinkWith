package com.tasomaniac.openwith.resolver;

interface ResolverPresenter {

    void bind(ResolverActivity view, ResolverView.Navigation navigation);

    void unbind(ResolverActivity view);

    void release();
}
