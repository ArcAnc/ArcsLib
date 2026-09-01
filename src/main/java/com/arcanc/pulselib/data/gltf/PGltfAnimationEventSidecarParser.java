/**
 * @author ArcAnc
 * Created at: 27.05.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.data.gltf;


import com.arcanc.pulselib.content.model.animation.PAnimation;
import com.arcanc.pulselib.content.model.animation.PAnimationEvent;
import com.arcanc.pulselib.content.model.animation.PAnimationEventType;
import com.arcanc.pulselib.content.registration.PLibRegistration;
import com.arcanc.pulselib.util.PLibDatabase;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PGltfAnimationEventSidecarParser
{
	private static final float SECONDS_TO_TICKS = 20f;
	
	public static JsonElement parseJson(InputStream stream) throws IOException
	{
		try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8))
		{
			return JsonParser.parseReader(reader);
		}
	}
	
	public static void mergeSidecar(JsonElement root, Map<String, PAnimation> animations)
	{
		JsonElement animationsNode = member(root, "animations");
		if (!isObject(animationsNode))
			animationsNode = root;
		
		if (!isObject(animationsNode))
			return;
		
		for (Map.Entry<String, JsonElement> entry : animationsNode.getAsJsonObject().entrySet())
		{
			PAnimation animation = animations.get(entry.getKey());
			if (animation == null)
				continue;
			
			List<PAnimationEvent<?>> events = new ArrayList<>(animation.events());
			events.addAll(parseAnimationEvents(entry.getValue()));
			events.sort(Comparator.comparingDouble(PAnimationEvent::time));
			animations.put(entry.getKey(), new PAnimation(
					animation.name(),
					Math.max(animation.length(), maxEventTime(events)),
					animation.boneAnimations(),
					events));
		}
	}
	
	private static List<PAnimationEvent<?>> parseAnimationEvents(JsonElement animationNode)
	{
		List<PAnimationEvent<?>> events = new ArrayList<>();
		parseEventArray(member(animationNode, "events"), events);
		events.sort(Comparator.comparingDouble(PAnimationEvent::time));
		return events;
	}
	
	private static float maxEventTime(List<PAnimationEvent<?>> events)
	{
		float maxTime = 0f;
		for (PAnimationEvent<?> event : events)
			maxTime = Math.max(maxTime, event.time());
		return maxTime;
	}
	
	private static void parseEventArray(JsonElement node, List<PAnimationEvent<?>> events)
	{
		if (!isArray(node))
			return;
		
		for (JsonElement eventNode : node.getAsJsonArray())
		{
			String type = stringValue(member(eventNode, "type"), "");
			float time = secondsToTicks(floatValue(member(eventNode, "time"), 0f));
			PAnimationEvent<?> event = typedEvent(time, type, eventNode);
			if (event != null)
				events.add(event);
		}
	}
	
	private static @Nullable PAnimationEvent<?> typedEvent(float time, String rawType, JsonElement node)
	{
		if (rawType.isBlank())
			return null;
		var id = rawType.indexOf(':') >= 0 ? Identifier.tryParse(rawType) : PLibDatabase.rl(rawType);
		if (id == null)
		{
			PLibDatabase.LOGGER.warn("Unknown GLTF animation event type: {}", rawType);
			return null;
		}
		PAnimationEventType<?> type = PLibRegistration.AnimationEventReg.EVENT_TYPES.get(id).orElse(null);
		if (type == null)
		{
			PLibDatabase.LOGGER.warn("Unregistered GLTF animation event type: {}", id);
			return null;
		}
		return decode(time, type, node);
	}

	private static <T> @Nullable PAnimationEvent<T> decode(float time, PAnimationEventType<T> type, JsonElement node)
	{
		return type.codec().codec().parse(JsonOps.INSTANCE, node).resultOrPartial(error ->
				PLibDatabase.LOGGER.warn("Invalid animation event {}: {}", type.id(), error)).map(data -> new PAnimationEvent<>(time, type, data)).orElse(null);
	}
	
	private static float secondsToTicks(float seconds)
	{
		return seconds * SECONDS_TO_TICKS;
	}
	
	private static float floatValue(JsonElement node, float fallback)
	{
		if (isMissing(node) || !node.isJsonPrimitive() || !node.getAsJsonPrimitive().isNumber())
			return fallback;
		return node.getAsFloat();
	}

	private static String stringValue(JsonElement node, String fallback)
	{
		if (isMissing(node) || !node.isJsonPrimitive() || !node.getAsJsonPrimitive().isString())
			return fallback;
		return node.getAsString();
	}
	
	private static JsonElement member(JsonElement element, String name)
	{
		if (!isObject(element))
			return JsonNull.INSTANCE;
		
		JsonObject object = element.getAsJsonObject();
		JsonElement value = object.get(name);
		return value == null ? JsonNull.INSTANCE : value;
	}
	
	private static boolean isMissing(@Nullable JsonElement element)
	{
		return element == null || element.isJsonNull();
	}
	
	private static boolean isObject(@Nullable JsonElement element)
	{
		return element != null && element.isJsonObject();
	}

	private static boolean isArray(@Nullable JsonElement element)
	{
		return element != null && element.isJsonArray();
	}
}
