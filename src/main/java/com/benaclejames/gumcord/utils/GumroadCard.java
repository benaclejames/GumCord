package com.benaclejames.gumcord.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GumroadCard {
    public String visual;
    public String type;
}
