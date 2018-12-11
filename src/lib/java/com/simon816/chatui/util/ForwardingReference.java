package com.simon816.chatui.util;

import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectReference;

import java.util.concurrent.CompletableFuture;

public class ForwardingReference implements SubjectReference {

    protected ForwardingSource source;

    public ForwardingReference(ForwardingSource source) {
        this.source = source;
    }

    @Override
    public String getCollectionIdentifier() {
        return source.getContainingCollection().getIdentifier();
    }

    @Override
    public String getSubjectIdentifier() {
        return source.getIdentifier();
    }

    @Override
    public CompletableFuture<Subject> resolve() {
        return CompletableFuture.completedFuture(source);
    }
}
