/**
 * @author ArcAnc
 * Created at: 26.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.data;


import com.arcanc.pulselib.content.model.PBone;
import com.arcanc.pulselib.content.model.PMesh;
import com.arcanc.pulselib.content.model.PModel;
import com.arcanc.pulselib.content.model.animation.PAnimation;
import com.arcanc.pulselib.content.model.animation.PAnimationChannel;
import com.arcanc.pulselib.content.model.animation.PBoneAnimation;
import com.arcanc.pulselib.content.model.animation.PKeyFrameChannel;
import com.arcanc.pulselib.util.helpers.PLibParserHelper;
import com.mojang.datafixers.util.Pair;
import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class PGltfModelParser
{
	public static PModel parse(InputStream stream) throws IOException
	{
		GltfModel model = new GltfModelReader().readWithoutReferences(stream);
		
		PModel pModel = new PModel();
		
		Map<UUID, PBone> uuidToBone = new HashMap<>();
		Map<UUID, PMesh> uuidToMesh = new HashMap<>();
		Map<NodeModel, PBone> nodeToBone = parseBones(model, uuidToBone, uuidToMesh);
		Map<String, PAnimation> animations = parseAnimations(model, nodeToBone);
		
		pModel.bones.putAll(uuidToBone);
		pModel.meshes.putAll(uuidToMesh);
		pModel.boneMeshes.putAll(uuidToBone.values().stream().
				collect(Collectors.toMap(PBone :: uuid, pBone -> Pair.of(pBone.uuid(), pBone.meshUUIDS().stream().toList()))));
		pModel.animations.putAll(animations);
		
		return pModel;

	}
	
	private static Map<NodeModel, PBone> parseBones(GltfModel model, Map<UUID, PBone> uuidToBone, Map<UUID, PMesh> uuidToMesh)
	{
		List<NodeModel> joints = model.getNodeModels();
		final Map<NodeModel, PBone> nodeToBone = new HashMap<>();

		if (joints == null || joints.isEmpty())
			return nodeToBone;
		
		for (NodeModel node : joints)
			parseBone(node, nodeToBone, model, uuidToMesh);
		
		//==============
		// Restore Structure
		for (NodeModel node : joints)
		{
			PBone bone = nodeToBone.get(node);
			NodeModel parent = node.getParent();
			if (parent != null)
				bone.setParent(nodeToBone.get(parent));
			
			List<NodeModel> children = node.getChildren();
			if (children != null && !children.isEmpty())
				for (NodeModel child : children)
					bone.children().add(nodeToBone.get(child));
		}
		
		nodeToBone.forEach((nodeModel, pBone) ->
				uuidToBone.put(pBone.uuid(), pBone));
		
		return nodeToBone;
	}

	private static void parseBone(NodeModel node, Map<NodeModel, PBone> nodeToBone, GltfModel model, Map<UUID, PMesh> uuidToMesh)
	{
		float[] rawTranslation = node.getTranslation();
		Vector3f pivot = new Vector3f();
		if (rawTranslation != null && rawTranslation.length == 3)
			pivot.add(rawTranslation[0], rawTranslation[1], rawTranslation[2]);
		float[] rawRotation = node.getRotation();
		Quaternionf baseRotation = new Quaternionf();
		if (rawRotation != null && rawRotation.length == 4)
			baseRotation.set(rawRotation[0], rawRotation[1], rawRotation[2], rawRotation[3]);
		
		UUID boneUUID = UUID.randomUUID();
		String name = node.getName();
		if (name == null)
			name = boneUUID.toString();
		
		PBone bone = new PBone(
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
	
	private static UUID parseMesh(MeshModel mesh, GltfModel model, Map<UUID, PMesh> uuidToMesh)
	{
		//Only one primitive per mesh. At least for BBmodel
		MeshPrimitiveModel primitive = mesh.getMeshPrimitiveModels().getFirst();
		AccessorModel positionsAccessor = primitive.getAttributes().get("POSITION");
		AccessorModel normalsAccessor = primitive.getAttributes().get("NORMAL");
		AccessorModel uvsAccessor = primitive.getAttributes().get("TEXCOORD_0");
		
		FloatBuffer positions = PLibParserHelper.getFloatBuffer(positionsAccessor);
		FloatBuffer normals = PLibParserHelper.getFloatBuffer(normalsAccessor);
		FloatBuffer uvs = PLibParserHelper.getFloatBuffer(uvsAccessor);
		
		int vertexCount = positionsAccessor.getCount();
		
		AccessorModel indicesAccessor = primitive.getIndices();
		ByteBuffer indices = PLibParserHelper.getByteBuffer(indicesAccessor);
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
		PMesh pMesh = new PMesh(
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
		
		uuidToMesh.put(pMesh.uuid(), pMesh);
		return pMesh.uuid();
	}
	
	private static Map<String, PAnimation> parseAnimations(GltfModel model, Map<NodeModel, PBone> nodeToBone)
	{
		Map<String, PAnimation> animations = new HashMap<>();
		
		for (int q = 0; q < model.getAnimationModels().size(); q++)
		{
			AnimationModel animationModel = model.getAnimationModels().get(q);
			
			String animationName = animationModel.getName() != null ? animationModel.getName() : "anim_" + q;
			Map<String, PBoneAnimation> boneAnimations = new HashMap<>();
			float maxTime = 0f;
			
			for (AnimationModel.Channel channel : animationModel.getChannels())
			{
				NodeModel node = channel.getNodeModel();
				
				if (node == null)
					continue;
				
				PBone bone = nodeToBone.get(node);
				if (bone == null)
					continue;
				
				UUID boneUuid = bone.uuid();
				
				PAnimationChannel pChannel;
				switch (channel.getPath())
				{
					case "translation" -> pChannel = PAnimationChannel.POSITION;
					case "rotation" -> pChannel = PAnimationChannel.ROTATION;
					case "scale" -> pChannel = PAnimationChannel.SCALE;
					default -> pChannel = null;
				}
				if (pChannel == null)
					continue;
				
				AnimationModel.Sampler sampler = channel.getSampler();
				if (sampler == null)
					continue;
				
				FloatBuffer inputTimes = PLibParserHelper.getFloatBuffer(sampler.getInput());
				maxTime = Math.max(maxTime, inputTimes.limit() > 0 ? inputTimes.get(inputTimes.limit() - 1) : 0L);
				AccessorModel outputAccessor = sampler.getOutput();
				if (outputAccessor == null)
					continue;
				ByteBuffer bb = outputAccessor.getAccessorData().createByteBuffer();
				List<PKeyFrameChannel<?>> keyframes = new ArrayList<>();
				switch (pChannel)
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
							keyframes.add(new PKeyFrameChannel.RotationKeyFrame(time, value));
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
							keyframes.add(new PKeyFrameChannel.PositionKeyFrame(time, value));
						}
					}
					case SCALE ->
					{
						for (int k = 0; k < inputTimes.limit(); k++)
						{
							float time = inputTimes.get(k) * 20;
							Vector3f value = new Vector3f(bb.getFloat(k * 12), bb.getFloat(k * 12 + 4), bb.getFloat(k * 12 + 8));
							keyframes.add(new PKeyFrameChannel.ScaleKeyFrame(time, value));
						}
					}
				};
				
				PBoneAnimation boneAnimation = boneAnimations.computeIfAbsent(bone.name(), k -> new PBoneAnimation(boneUuid, new HashMap<>()));
				if (pChannel == PAnimationChannel.ROTATION)
					if (trackContainsBaseRotation(keyframes, bone.baseRotation()))
						cleanBaseRot(keyframes, bone.baseRotation());
				boneAnimation.channels().put(pChannel, keyframes);
			}
			animations.put(animationName, new PAnimation(animationName, maxTime * 20, boneAnimations));
		}
		
		return animations;
	}
	
	static void cleanBaseRot(List<PKeyFrameChannel<?>> frames, Quaternionf baseRotation)
	{
		if (frames.isEmpty())
			return;
		
		Quaternionf baseRotInv = new Quaternionf(baseRotation).invert();
		
		for (PKeyFrameChannel<?> frame : frames)
		{
			Quaternionf q = (Quaternionf) frame.value();
			Quaternionf clean = q.mul(baseRotInv, new Quaternionf());
			q.set(clean);
		}
	}
	
	static boolean trackContainsBaseRotation(
			List<PKeyFrameChannel<?>> frames,
			Quaternionf baseRot)
	{
		if (frames.isEmpty())
			return false;
		
		int nearBase = 0;
		int nearIdentity = 0;
		
		Quaternionf identity = new Quaternionf();
		
		for (PKeyFrameChannel<?> frame : frames)
		{
			Quaternionf q = (Quaternionf) frame.value();
			if (nearlyEqual(q, baseRot, 0.001f))
				nearBase++;
			
			if (nearlyEqual(q, identity, 0.001f))
				nearIdentity++;
		}
		
		return nearBase > nearIdentity;
	}
	
	static boolean nearlyEqual(Quaternionf a, Quaternionf b, float eps)
	{
		float dot = Math.abs(a.dot(b));
		return 1.0f - dot < eps;
	}
}
