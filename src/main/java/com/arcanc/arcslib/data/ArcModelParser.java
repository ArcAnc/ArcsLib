/**
 * @author ArcAnc
 * Created at: 26.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.data;


import com.arcanc.arcslib.content.model.ArcBone;
import com.arcanc.arcslib.content.model.ArcMesh;
import com.arcanc.arcslib.content.model.ArcModel;
import com.arcanc.arcslib.content.model.animation.ArcAnimation;
import com.arcanc.arcslib.content.model.animation.ArcAnimationChannel;
import com.arcanc.arcslib.content.model.animation.ArcBoneAnimation;
import com.arcanc.arcslib.content.model.animation.ArcKeyFrameChannel;
import com.arcanc.arcslib.util.Database;
import com.arcanc.arcslib.util.helpers.ParserHelper;
import com.mojang.datafixers.util.Pair;
import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class ArcModelParser
{
	public static @NonNull ArcModel parse(InputStream stream) throws IOException
	{
		GltfModel model = new GltfModelReader().readWithoutReferences(stream);
		
		ArcModel arcModel = new ArcModel();
		
		Map<UUID, ArcBone> uuidToBone = new HashMap<>();
		Map<UUID, ArcMesh> uuidToMesh = new HashMap<>();
		Map<NodeModel, ArcBone> nodeToBone = parseBones(model, uuidToBone, uuidToMesh);
		Map<String, ArcAnimation> animations = parseAnimations(model, nodeToBone);
		
		arcModel.bones.putAll(uuidToBone);
		arcModel.meshes.putAll(uuidToMesh);
		arcModel.boneMeshes.putAll(uuidToBone.values().stream().
				collect(Collectors.toMap(ArcBone :: uuid, arcBone -> Pair.of(arcBone.uuid(), arcBone.meshUUIDS().stream().toList()))));
		arcModel.animations.putAll(animations);
		
		return arcModel;

	}
	
	private static @NonNull Map<NodeModel, ArcBone> parseBones(@NonNull GltfModel model, Map<UUID, ArcBone> uuidToBone, Map<UUID, ArcMesh> uuidToMesh)
	{
		List<NodeModel> joints = model.getNodeModels();
		final Map<NodeModel, ArcBone> nodeToBone = new HashMap<>();

		if (joints == null || joints.isEmpty())
			return nodeToBone;
		
		for (NodeModel node : joints)
			parseBone(node, nodeToBone, model, uuidToMesh);
		
		//==============
		// Restore Structure
		for (NodeModel node : joints)
		{
			ArcBone bone = nodeToBone.get(node);
			NodeModel parent = node.getParent();
			if (parent != null)
				bone.setParent(nodeToBone.get(parent));
			
			List<NodeModel> children = node.getChildren();
			if (children != null && !children.isEmpty())
				for (NodeModel child : children)
					bone.children().add(nodeToBone.get(child));
		}
		
		nodeToBone.forEach((nodeModel, arcBone) ->
				uuidToBone.put(arcBone.uuid(), arcBone));
		
		return nodeToBone;
	}

	private static void parseBone(@NonNull NodeModel node, Map<NodeModel, ArcBone> nodeToBone, GltfModel model, Map<UUID, ArcMesh> uuidToMesh)
	{
		float[] rawTranslation = node.getTranslation();
		Vector3f pivot = new Vector3f();
		if (rawTranslation != null && rawTranslation.length == 3)
			pivot.add(rawTranslation[0], rawTranslation[1], rawTranslation[2]);
		float[] rawRotation = node.getRotation();
		Quaternionf baseRotation = new Quaternionf();
		if (rawRotation != null && rawRotation.length == 4)
			baseRotation.add(rawRotation[0], rawRotation[1], rawRotation[2], rawRotation[3]);
		
		UUID boneUUID = UUID.randomUUID();
		String name = node.getName();
		if (name == null)
			name = boneUUID.toString();
		
		ArcBone bone = new ArcBone(
				boneUUID,
				name,
				pivot,
				baseRotation);
		
		List<MeshModel> meshModels = node.getMeshModels();
		if (meshModels != null && !meshModels.isEmpty())
			for (MeshModel meshModel : meshModels)
				bone.meshUUIDS().add(parseMesh(meshModel, model, uuidToMesh));
		
		nodeToBone.put(node, bone);
	}
	
	private static @NonNull UUID parseMesh(@NonNull MeshModel mesh, GltfModel model, Map<UUID, ArcMesh> uuidToMesh)
	{
		//Only one primitive per mesh. At least for BBmodel
		MeshPrimitiveModel primitive = mesh.getMeshPrimitiveModels().getFirst();
		AccessorModel positionsAccessor = primitive.getAttributes().get("POSITION");
		AccessorModel normalsAccessor = primitive.getAttributes().get("NORMAL");
		AccessorModel uvsAccessor = primitive.getAttributes().get("TEXCOORD_0");
		
		FloatBuffer positions = ParserHelper.getFloatBuffer(positionsAccessor);
		FloatBuffer normals = ParserHelper.getFloatBuffer(normalsAccessor);
		FloatBuffer uvs = ParserHelper.getFloatBuffer(uvsAccessor);
		
		int vertexCount = positionsAccessor.getCount();
		
		AccessorModel indicesAccessor = primitive.getIndices();
		ByteBuffer indices = ParserHelper.getByteBuffer(indicesAccessor);
		int indicesCount = indicesAccessor.getCount();
		int indicesType = indicesAccessor.getComponentType();
			
		String textureName = "";
		MaterialModel material = primitive.getMaterialModel();
		if (material instanceof MaterialModelV2 mat)
		{
			TextureModel texture = mat.getBaseColorTexture();
			if (texture != null)
				textureName = texture.getName();
		}
		ArcMesh arcMesh = new ArcMesh(
				UUID.randomUUID(),
				vertexCount,
				positions,
				normals,
				uvs,
				indicesCount,
				indices,
				indicesType,
				textureName
		);
		
		uuidToMesh.put(arcMesh.uuid(), arcMesh);
		return arcMesh.uuid();
	}
	
	private static @NonNull Map<String, ArcAnimation> parseAnimations(@NonNull GltfModel model, Map<NodeModel, ArcBone> nodeToBone)
	{
		Map<String, ArcAnimation> animations = new HashMap<>();
		
		for (int q = 0; q < model.getAnimationModels().size(); q++)
		{
			AnimationModel animationModel = model.getAnimationModels().get(q);
			
			String animationName = animationModel.getName() != null ? animationModel.getName() : "anim_" + q;
			Map<String, ArcBoneAnimation> boneAnimations = new HashMap<>();
			float maxTime = 0f;
			
			for (AnimationModel.Channel channel : animationModel.getChannels())
			{
				NodeModel node = channel.getNodeModel();
				
				if (node == null)
					continue;
				
				ArcBone bone = nodeToBone.get(node);
				if (bone == null)
					continue;
				
				UUID boneUuid = bone.uuid();
				
				ArcAnimationChannel arcChannel;
				switch (channel.getPath())
				{
					case "translation" -> arcChannel = ArcAnimationChannel.POSITION;
					case "rotation" -> arcChannel = ArcAnimationChannel.ROTATION;
					case "scale" -> arcChannel = ArcAnimationChannel.SCALE;
					default -> arcChannel = null;
				}
				if (arcChannel == null)
					continue;
				
				AnimationModel.Sampler sampler = channel.getSampler();
				if (sampler == null)
					continue;
				
				FloatBuffer inputTimes = ParserHelper.getFloatBuffer(sampler.getInput());
				maxTime = Math.max(maxTime, inputTimes.limit() > 0 ? inputTimes.get(inputTimes.limit() - 1) : 0L);
				AccessorModel outputAccessor = sampler.getOutput();
				if (outputAccessor == null)
					continue;
				ByteBuffer bb = outputAccessor.getAccessorData().createByteBuffer();
				List<ArcKeyFrameChannel<?>> keyframes = new ArrayList<>();
				switch (arcChannel)
				{
					case ROTATION ->
					{
						for (int k = 0; k < inputTimes.limit(); k++)
						{
							float time = inputTimes.get(k) * 20;
							Quaternionf value = new Quaternionf(
									bb.getFloat(k * 16),
									bb.getFloat(k * 16 + 4),
									bb.getFloat(k * 16 + 8),
									bb.getFloat(k * 16 + 12));
							keyframes.add(new ArcKeyFrameChannel.RotationKeyFrame(time, value));
						}
					}
					case POSITION ->
					{
						for (int k = 0; k < inputTimes.limit(); k++)
						{
							float time = inputTimes.get(k) * 20;
							float x = bb.getFloat(k * 12);
							float y = bb.getFloat(k * 12 + 4);
							float z = bb.getFloat(k * 12 + 8);
							
							x = x - bone.pivot().x();
							y = y - bone.pivot().y();
							z = z - bone.pivot().z();
							Vector3f value = new Vector3f(x, y, z);
							keyframes.add(new ArcKeyFrameChannel.PositionKeyFrame(time, value));
						}
					}
					case SCALE ->
					{
						for (int k = 0; k < inputTimes.limit(); k++)
						{
							float time = inputTimes.get(k) * 20;
							Vector3f value = new Vector3f(bb.getFloat(k * 12), bb.getFloat(k * 12 + 4), bb.getFloat(k * 12 + 8));
							keyframes.add(new ArcKeyFrameChannel.ScaleKeyFrame(time, value));
						}
					}
				};
				
				ArcBoneAnimation boneAnimation = boneAnimations.computeIfAbsent(bone.name(), k -> new ArcBoneAnimation(boneUuid, new HashMap<>()));
				boneAnimation.channels().put(arcChannel, keyframes);
			}
			animations.put(animationName, new ArcAnimation(animationName, maxTime * 20, boneAnimations));
		}
		
		return animations;
	}
}
