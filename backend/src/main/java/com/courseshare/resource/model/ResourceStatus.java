package com.courseshare.resource.model;

import java.util.Set;

public final class ResourceStatus {

    public static final int DRAFT = 0;
    public static final int PENDING = 1;
    public static final int PUBLISHED = 2;
    public static final int REJECTED = 3;
    public static final int OFFLINE = 4;
    public static final Set<Integer> EDITABLE = Set.of(DRAFT, REJECTED, OFFLINE);
    public static final Set<Integer> SUBMITTABLE = Set.of(DRAFT, REJECTED, OFFLINE);
    public static final Set<Integer> DELETABLE = Set.of(DRAFT, REJECTED, OFFLINE);

    private ResourceStatus() {
    }
}

