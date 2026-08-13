package com.arcanc.pulselib.content.model.animation;

import com.google.gson.JsonElement;

import java.util.Map;

public interface PAnimationFormatDecoder
{
	Map<String, PAnimationTrack<?>> decodeBoneTracks(PAnimationDecodeContext context, JsonElement boneNode);
}
