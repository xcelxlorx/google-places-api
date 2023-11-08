package com.gihae.googleplaces.review;

public record Review (
    String name,
    String profileUrl,
    String rating,
    String text
){}
