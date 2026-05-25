/**
 * @author ArcAnc
 * Created at: 25.05.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.data;


import com.arcanc.pulselib.content.model.animation.PAnimation;
import com.arcanc.pulselib.content.model.animation.PAnimationEvent;
import com.arcanc.pulselib.util.PLibDatabase;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

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
			
			List<PAnimationEvent> events = new ArrayList<>(animation.events());
			events.addAll(parseAnimationEvents(entry.getValue()));
			events.sort(Comparator.comparing(PAnimationEvent :: time));
			animations.put(entry.getKey(), new PAnimation(
					animation.name(),
					Math.max(animation.length(), maxEventTime(events)),
					animation.boneAnimations(),
					events));
		}
	}
	
	private static List<PAnimationEvent> parseAnimationEvents(JsonElement animationNode)
	{
		List<PAnimationEvent> events = new ArrayList<>();
		parseEventArray(member(animationNode, "events"), events);
		events.sort(Comparator.comparing(PAnimationEvent :: time));
		return events;
	}
	
	private static float maxEventTime(List<PAnimationEvent> events)
	{
		float maxTime = 0f;
		for (PAnimationEvent event : events)
			maxTime = Math.max(maxTime, event.time());
		return maxTime;
	}
	
	private static void parseEventArray(JsonElement node, List<PAnimationEvent> events)
	{
		if (!isArray(node))
			return;
		
		for (JsonElement eventNode : node.getAsJsonArray())
		{
			String type = stringValue(member(eventNode, "type"), "");
			float time = secondsToTicks(floatValue(member(eventNode, "time"), 0f));
			PAnimationEvent event = switch (type)
			{
				case "sound" -> soundEvent(time, eventNode);
				case "particle" -> particleEvent(time, eventNode);
				default -> null;
			};
			if (event != null)
				events.add(event);
		}
	}
	
	private static PAnimationEvent.@Nullable Sound soundEvent(float time, JsonElement node)
	{
		Identifier sound = identifier(stringValue(member(node, "sound"), stringValue(member(node, "id"), "")));
		if (sound == null)
			return null;
		
		return new PAnimationEvent.Sound(
				time,
				sound,
				stringValue(member(node, "locator"), ""),
				floatValue(member(node, "volume"), 1f),
				floatValue(member(node, "pitch"), 1f));
	}
	
	private static PAnimationEvent.@Nullable Particle particleEvent(float time, JsonElement node)
	{
		Identifier particle = identifier(stringValue(member(node, "particle"), stringValue(member(node, "id"), "")));
		if (particle == null)
			return null;
		
		return new PAnimationEvent.Particle(
				time,
				particle,
				stringValue(member(node, "locator"), ""),
				vector3f(member(node, "offset"), new Vector3f()),
				vector3f(member(node, "motion"), new Vector3f()));
	}
	
	private static @Nullable Identifier identifier(String value)
	{
		if (value.isBlank())
			return null;
		
		Identifier id = Identifier.tryParse(value);
		if (id == null)
			PLibDatabase.LOGGER.warn("Invalid GLTF animation sidecar event identifier: {}", value);
		return id;
	}
	
	private static Vector3f vector3f(JsonElement node, Vector3f fallback)
	{
		if (!isArray(node) || node.getAsJsonArray().size() < 3)
			return new Vector3f(fallback);
		
		return new Vector3f(
				floatAt(node, 0, fallback.x()),
				floatAt(node, 1, fallback.y()),
				floatAt(node, 2, fallback.z()));
	}
	
	private static float secondsToTicks(float seconds)
	{
		return seconds * SECONDS_TO_TICKS;
	}
	
	private static float floatAt(JsonElement node, int index, float fallback)
	{
		if (!isArray(node) || node.getAsJsonArray().size() <= index)
			return fallback;
		return floatValue(node.getAsJsonArray().get(index), fallback);
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
			return com.google.gson.JsonNull.INSTANCE;
		
		JsonObject object = element.getAsJsonObject();
		JsonElement value = object.get(name);
		return value == null ? com.google.gson.JsonNull.INSTANCE : value;
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
