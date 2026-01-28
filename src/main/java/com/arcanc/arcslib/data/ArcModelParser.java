/**
 * @author ArcAnc
 * Created at: 26.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.data;


import com.arcanc.arcslib.content.model.*;
import com.arcanc.arcslib.content.model.animation.ArcAnimation;
import com.arcanc.arcslib.content.model.animation.ArcAnimationChannel;
import com.arcanc.arcslib.content.model.animation.ArcBoneAnimation;
import com.arcanc.arcslib.content.model.animation.ArcKeyframeChannel;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ArcModelParser
{
	public static @NotNull ArcModel parse(InputStream stream)
	{
		JsonObject root = JsonParser.parseReader(
				new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
		
		ArcModel model = new ArcModel();
		
		Map<UUID, RawMesh> rawMeshes = parseElements(root);
		
		parseOutliner(root, model, rawMeshes);
		
		buildMeshes(model, rawMeshes);
		
		parseAnimations(root, model);
		
		computeBindPose(model);
		
		return model;
	}
	
	private static @NotNull Map<UUID, RawMesh> parseElements(@NotNull JsonObject root)
	{
		Map<UUID, RawMesh> result = new HashMap<>();
		
		JsonArray elements = root.getAsJsonArray("elements");
		
		if (elements == null)
			return result;
		
		for (JsonElement element : elements)
		{
			JsonObject object = element.getAsJsonObject();
			ElementType type = ElementType.getByName(object.get("type").getAsString());
			UUID uuid = UUID.fromString(object.get("uuid").getAsString());
			JsonArray originElem = object.getAsJsonArray("origin");
			Vector3f origin = new Vector3f(
					originElem.get(0).getAsFloat() / 16f,
					originElem.get(1).getAsFloat() / 16f,
					originElem.get(2).getAsFloat() / 16f);
			
			JsonArray rotationElem = object.getAsJsonArray("rotation");
			Quaternionf rotation = new Quaternionf();
			rotation.rotateXYZ(
					Mth.DEG_TO_RAD * rotationElem.get(0).getAsFloat(),
					Mth.DEG_TO_RAD * rotationElem.get(1).getAsFloat(),
					Mth.DEG_TO_RAD * rotationElem.get(2).getAsFloat());
			
			RawMesh mesh = new RawMesh(uuid, origin, rotation);
			Map<String, Vector3f> vertices = type.verticesGetter.getVertices(object);
			List<RawFace> faces = type.facesGetter.getFaces(object);
			
			mesh.vertices().putAll(vertices);
			mesh.faces().addAll(faces);
		
			result.put(uuid, mesh);
		}
		return result;
	}
	
	private static void parseOutliner(@NotNull JsonObject root, ArcModel model, Map<UUID, RawMesh> rawMeshes)
	{
		JsonArray outliner = root.getAsJsonArray("outliner");
		if (outliner == null)
			return;
		
		JsonArray bones = root.getAsJsonArray("groups");
		if (bones == null)
			return;
		
		Map<UUID, RawBone> bonesData = parseRawBone(bones);
		
		for (JsonElement element : outliner)
			parseOutlinerNode(element, null, model, rawMeshes, bonesData);
	}
	
	private static @NotNull Map<UUID, RawBone> parseRawBone(@NotNull JsonArray bones)
	{
		Map<UUID, RawBone> result = new HashMap<>();
		
		for (JsonElement element : bones)
		{
			JsonObject bone = element.getAsJsonObject();
			
			String name = bone.get("name").getAsString();
			UUID uuid = UUID.fromString(bone.get("uuid").getAsString());
			
			JsonArray origin = bone.getAsJsonArray("origin");
			Vector3f pivot = new Vector3f(
					origin.get(0).getAsFloat() / 16f,
					origin.get(1).getAsFloat() / 16f,
					origin.get(2).getAsFloat() / 16f);
			
			JsonArray rotation = bone.getAsJsonArray("rotation");
			Quaternionf rot = new Quaternionf();
			rot.rotateXYZ(
					Mth.DEG_TO_RAD * rotation.get(0).getAsFloat(),
					Mth.DEG_TO_RAD * rotation.get(1).getAsFloat(),
					Mth.DEG_TO_RAD * rotation.get(2).getAsFloat());
			
			result.put(uuid, new RawBone(name, pivot, rot));
		}
		return result;
	}
	
	private static void parseOutlinerNode(@NotNull JsonElement element, ArcBone parent, ArcModel model, Map<UUID, RawMesh> rawMeshes, Map<UUID, RawBone> rawBones)
	{
		if (element.isJsonPrimitive())
		{
			UUID meshId = UUID.fromString(element.getAsString());
			if (parent != null && rawMeshes.containsKey(meshId))
				model.boneMeshes.computeIfAbsent(parent.uuid(), id -> Pair.of(meshId, new ArrayList<>()));
			return;
		}
		
		JsonObject object = element.getAsJsonObject();
		UUID uuid = UUID.fromString(object.get("uuid").getAsString());
		
		RawBone rawBone = rawBones.get(uuid);
		if (rawBone == null)
			return;
		
		ArcBone bone = new ArcBone(uuid, rawBone.name(), model.bones.size(), parent, rawBone.pivot(), rawBone.rot());
		if (parent != null)
			parent.children().add(bone);
		
		model.bones.put(uuid, bone);
		
		JsonArray children = object.getAsJsonArray("children");
		for (JsonElement child : children)
			parseOutlinerNode(child, bone, model, rawMeshes, rawBones);
	}
	
	
	private static void buildMeshes(@NotNull ArcModel model, Map<UUID, RawMesh> rawMeshes)
	{
		for (Map.Entry<UUID, Pair<UUID, List<UUID>>> entry : model.boneMeshes.entrySet())
		{
			ArcBone bone = model.bones.get(entry.getKey());
			RawMesh raw = rawMeshes.get(entry.getValue().getFirst());
			
			Map<Integer, List<RawFace>> facesByTexture = new HashMap<>();
			for (RawFace face : raw.faces())
				facesByTexture.computeIfAbsent(face.texture(), t -> new ArrayList<>()).
						add(face);
				
			for (Map.Entry<Integer, List<RawFace>> texEntry : facesByTexture.entrySet())
			{
				int textureId = texEntry.getKey();
				List<RawFace> faces = texEntry.getValue();
				List<Vector3f> pos = new ArrayList<>();
				List<Vector3f> nor = new ArrayList<>();
				List<Vector2f> uv = new ArrayList<>();
				
				for (RawFace face : faces)
					triangulate(face, raw.vertices(), pos, nor, uv);
					
				int vCount = pos.size();
				if (vCount == 0)
					continue;
					
				FloatBuffer pBuf = BufferUtils.createFloatBuffer(vCount * 3);
				FloatBuffer nBuf = BufferUtils.createFloatBuffer(vCount * 3);
				FloatBuffer uvBuf = BufferUtils.createFloatBuffer(vCount * 2);
				ByteBuffer bBuf = BufferUtils.createByteBuffer(vCount);
				FloatBuffer wBuf = BufferUtils.createFloatBuffer(vCount);
				
				for (int q = 0; q < vCount; q++)
				{
					Vector3f p = pos.get(q);
					Vector3f n = nor.get(q);
					Vector2f t = uv.get(q);
					
					pBuf.put(p.x()).put(p.y()).put(p.z());
					nBuf.put(n.x()).put(n.y()).put(n.z());
					uvBuf.put(t.x()).put(t.y());
					
					bBuf.put((byte) bone.index());
					wBuf.put(1.0f);
				}
				
				pBuf.flip();
				nBuf.flip();
				uvBuf.flip();
				bBuf.flip();
				wBuf.flip();
				
				//TODO: check if I can use new uuid instead of using raw uuid
				UUID meshUUUID = UUID.randomUUID();
				
				ArcMesh mesh = new ArcMesh(
						meshUUUID,
						new Vector3f(raw.origin()),
						new Quaternionf(raw.rotation()),
						bone.index(),
						vCount,
						pBuf, nBuf, uvBuf,
						bBuf, wBuf,
						textureId
				);
				
				List<UUID> newUUIDList = entry.getValue().getSecond();
				newUUIDList.add(mesh.uuid());
				
				model.meshes.put(mesh.uuid(), mesh);
			}
		}
	}
	
	private static void triangulate(@NotNull RawFace face, Map<String, Vector3f> vertices, List<Vector3f> pos, List<Vector3f> nor, List<Vector2f> uv)
	{
		int[] order = face.vertexIds().length == 4
				? new int[]{0,1,2, 0,2,3}
				: new int[]{0,1,2};
		
		
		for (int q = 0; q < order.length; q += 3)
		{
			Vector3f v0 = vertices.get(face.vertexIds()[order[q]]);
			Vector3f v1 = vertices.get(face.vertexIds()[order[q + 1]]);
			Vector3f v2 = vertices.get(face.vertexIds()[order[q + 2]]);
			
			Vector3f n = v1.sub(v0, new Vector3f()).
							cross(v2.sub(v0, new Vector3f()), new Vector3f()).
					normalize();
			
			for (int w = 0; w < 3; w++)
			{
				int idx = order[q + w];
				pos.add(vertices.get(face.vertexIds()[idx]));
				nor.add(n);
				uv.add(face.uvs()[idx]);
			}
		}
	}
	
	private static void parseAnimations(@NotNull JsonObject root, ArcModel model)
	{
		JsonArray animations = root.getAsJsonArray("animations");
		if (animations == null) return;
		
		for (JsonElement animElem : animations)
		{
			JsonObject animObj = animElem.getAsJsonObject();
			String name = animObj.get("name").getAsString();
			float length = animObj.get("length").getAsFloat();
			
			JsonObject animators = animObj.getAsJsonObject("animators");
			Map<Integer, ArcBoneAnimation> boneAnimations = new HashMap<>();
			
			for (Map.Entry<String, JsonElement> entry : animators.entrySet())
			{
				String boneUuid = entry.getKey();
				ArcBone bone = model.bones.get(UUID.fromString(boneUuid));
				if (bone == null)
					continue;
				
				JsonObject boneAnimator = entry.getValue().getAsJsonObject();
				JsonArray keyframes = boneAnimator.getAsJsonArray("keyframes");
				if (keyframes == null)
					continue;
				
				Map<ArcAnimationChannel, List<ArcKeyframeChannel>> channels = new EnumMap<>(ArcAnimationChannel.class);
				
				for (JsonElement keyElem : keyframes) {
					JsonObject keyObj = keyElem.getAsJsonObject();
					String channelStr = keyObj.get("channel").getAsString().toUpperCase();
					ArcAnimationChannel channel = switch (channelStr)
					{
						case "POSITION" -> ArcAnimationChannel.POSITION;
						case "ROTATION" -> ArcAnimationChannel.ROTATION;
						case "SCALE" -> ArcAnimationChannel.SCALE;
						default -> null;
					};
					if (channel == null)
						continue;
					
					List<ArcKeyframeChannel> keyList = channels.computeIfAbsent(channel, k -> new ArrayList<>());
					
					JsonArray dataPoints = keyObj.getAsJsonArray("data_points");
					for (JsonElement dpElem : dataPoints)
					{
						JsonObject dp = dpElem.getAsJsonObject();
						Vector3f value = new Vector3f(
								Float.parseFloat(dp.get("x").getAsString()),
								Float.parseFloat(dp.get("y").getAsString()),
								Float.parseFloat(dp.get("z").getAsString())
						);
						float time = keyObj.get("time").getAsFloat();
						keyList.add(new ArcKeyframeChannel(time, value));
					}
				}
				
				boneAnimations.put(bone.index(), new ArcBoneAnimation(bone.index(), channels));
			}
			
			model.animations.put(name, new ArcAnimation(name, length, boneAnimations));
		}
	}
	
	private static void computeBindPose(@NotNull ArcModel model)
	{
		for (ArcBone bone : model.bones.values())
		{
			Matrix4f local = new Matrix4f().
							translate(bone.pivot()).
							rotate(bone.baseRotation()).
							translate(bone.pivot().negate(new Vector3f()));
			
			if (bone.parent() != null)
				bone.bindPose().set(bone.parent().bindPose()).mul(local);
			else
				bone.bindPose().set(local);
			
			bone.inverseBindPose().set(bone.bindPose()).invert();
		}
	}
	
	enum ElementType
	{
		CUBE("cube", ArcModelParser :: getVerticesFromCube, ArcModelParser :: getFacesFromCube),
		MESH("mesh", ArcModelParser :: getVerticesFromMesh, ArcModelParser :: getFacesFromMesh);
		
		final String name;
		final VerticesGetter verticesGetter;
		final FacesGetter facesGetter;
		
		ElementType(String name, VerticesGetter verticesGetter, FacesGetter facesGetter)
		{
			this.name = name;
			this.verticesGetter = verticesGetter;
			this.facesGetter = facesGetter;
		}
		
		static ElementType getByName(@NotNull String name)
		{
			if (name.equals(CUBE.name))
				return CUBE;
			else
				return MESH;
		}
	}
	
	private static @NotNull Map<String, Vector3f> getVerticesFromMesh(@NotNull JsonObject object)
	{
		Map<String, Vector3f> result = new HashMap<>();
		
		JsonObject verts = object.getAsJsonObject("vertices");
		for (Map.Entry<String, JsonElement> v : verts.entrySet())
		{
			JsonArray array = v.getValue().getAsJsonArray();
			result.put(v.getKey(), new Vector3f(
					array.get(0).getAsFloat() / 16f,
					array.get(1).getAsFloat() / 16f,
					array.get(2).getAsFloat() / 16f));
		}
		return result;
	}
	
	private static @NotNull Map<String, Vector3f> getVerticesFromCube(@NotNull JsonObject object)
	{
		Map<String, Vector3f> result = new HashMap<>();
		
		JsonArray from = object.getAsJsonArray("from");
		JsonArray to   = object.getAsJsonArray("to");
		
		float x1 = from.get(0).getAsFloat() / 16f;
		float y1 = from.get(1).getAsFloat() / 16f;
		float z1 = from.get(2).getAsFloat() / 16f;
		
		float x2 = to.get(0).getAsFloat() / 16f;
		float y2 = to.get(1).getAsFloat() / 16f;
		float z2 = to.get(2).getAsFloat() / 16f;
		
		result.put("0", new Vector3f(x1, y1, z1));
		result.put("1", new Vector3f(x2, y1, z1));
		result.put("2", new Vector3f(x2, y2, z1));
		result.put("3", new Vector3f(x1, y2, z1));
		
		result.put("4", new Vector3f(x1, y1, z2));
		result.put("5", new Vector3f(x2, y1, z2));
		result.put("6", new Vector3f(x2, y2, z2));
		result.put("7", new Vector3f(x1, y2, z2));
		
		return result;
	}
	
	private static @NotNull List<RawFace> getFacesFromMesh(@NotNull JsonObject object)
	{
		List<RawFace> result = new ArrayList<>();
		
		JsonObject faces = object.getAsJsonObject("faces");
		for (Map.Entry<String, JsonElement> face : faces.entrySet())
		{
			JsonArray vertices = face.getValue().getAsJsonObject().getAsJsonArray("vertices");
			String[] ids = new String[vertices.size()];
			for (int q = 0; q < ids.length; q++)
				ids[q] = vertices.get(q).getAsString();
			
			Map<String, Vector2f> uvs = new HashMap<>();
			JsonObject uv = face.getValue().getAsJsonObject().getAsJsonObject("uv");
			for (Map.Entry<String, JsonElement> uvData : uv.entrySet())
			{
				JsonArray uvArray = uvData.getValue().getAsJsonArray();
				uvs.put(uvData.getKey(), new Vector2f(
						uvArray.get(0).getAsFloat() / 16f,
						uvArray.get(1).getAsFloat() / 16f));
			}
			
			Vector2f[] uvsArray = new Vector2f[ids.length];
			for (int q = 0; q < ids.length; q++)
				uvsArray[q] = uvs.get(ids[q]);
			
			int texture = -1;
			JsonElement element = face.getValue().getAsJsonObject().get("texture");
			if (element != null)
				texture = element.getAsInt();
			
			result.add(new RawFace(ids, uvsArray, texture));
		}
		
		return result;
	}
	
	private static @NotNull List<RawFace> getFacesFromCube(@NotNull JsonObject object)
	{
		List<RawFace> result = new ArrayList<>();
		
		JsonObject faces = object.getAsJsonObject("faces");
		if (faces == null)
			return result;
		
		
		for (Map.Entry<String, JsonElement> entry : faces.entrySet())
		{
			String side = entry.getKey();
			JsonObject face = entry.getValue().getAsJsonObject();
			
			int[] idx = switch (side)
			{
				case "north" -> new int[]{0, 1, 2, 3};
				case "south" -> new int[]{5, 4, 7, 6};
				case "west"  -> new int[]{4, 0, 3, 7};
				case "east"  -> new int[]{1, 5, 6, 2};
				case "up"    -> new int[]{3, 2, 6, 7};
				case "down"  -> new int[]{4, 5, 1, 0};
				default -> null;
			};
			
			if (idx == null)
				continue;
			
			
			String[] ids = new String[]
					{
							String.valueOf(idx[0]),
							String.valueOf(idx[1]),
							String.valueOf(idx[2]),
							String.valueOf(idx[3])
					};
			
			JsonArray uvArr = face.getAsJsonArray("uv");
			float u1 = uvArr.get(0).getAsFloat();
			float v1 = uvArr.get(1).getAsFloat();
			float u2 = uvArr.get(2).getAsFloat();
			float v2 = uvArr.get(3).getAsFloat();
			
			Vector2f[] uvs = new Vector2f[]
					{
							new Vector2f(u1, v1),
							new Vector2f(u2, v1),
							new Vector2f(u2, v2),
							new Vector2f(u1, v2)
					};
			
			int texture = -1;
			JsonElement element = face.get("texture");
			if (element != null)
				texture = element.getAsInt();
			
			result.add(new RawFace(ids, uvs, texture));
		}
		
		return result;
	}
	
	@FunctionalInterface
	private interface VerticesGetter
	{
		Map<String, Vector3f> getVertices (JsonObject object);
	}
	
	@FunctionalInterface
	private interface FacesGetter
	{
		List<RawFace> getFaces(JsonObject object);
	}
}
