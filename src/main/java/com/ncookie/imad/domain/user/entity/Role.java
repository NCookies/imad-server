package com.ncookie.imad.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    GUEST("GUEST", "게스트"),
    USER("USER", "일반 사용자");

    private final String key;
    private final String title;
}
