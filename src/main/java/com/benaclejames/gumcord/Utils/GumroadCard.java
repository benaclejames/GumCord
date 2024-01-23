package com.benaclejames.gumcord.Utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GumroadCard {
    public String visual;
    public String type;
}
