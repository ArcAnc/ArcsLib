/**
 * @author ArcAnc
 * Created at: 14.08.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer.gl;

import com.arcanc.pulselib.content.model.baked.PBakedMesh;
import com.arcanc.pulselib.content.model.deformer.gpu.PGpuDeformerBuffers;
import com.arcanc.pulselib.content.renderer.PRenderQueue;
import com.arcanc.pulselib.content.renderer.plan.PDrawGroup;
import com.arcanc.pulselib.content.renderer.plan.PRenderPlan;
import com.arcanc.pulselib.util.PRenderTypes;
import com.arcanc.pulselib.util.PTextureCache;
import com.arcanc.pulselib.util.helpers.PLibRenderHelper;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class PGlMultiDrawExecutor
{
	private final PGlGeometryArena geometry = new PGlGeometryArena();
	private final PGlInstanceStream instances = new PGlInstanceStream();
	private final PGlIndirectStream indirect = new PGlIndirectStream();
	private final PGlFrameArena frameArena = new PGlFrameArena(9);
	private final PGlWeightedBlendedOit weightedBlendedOit = new PGlWeightedBlendedOit();

	public void execute(PRenderPlan<RenderType, PBakedMesh, PRenderQueue.InstanceData> plan)
	{
		if (plan.isEmpty())
			return;

		List<PRenderQueue.InstanceData> allInstances = new ArrayList<>();
		List<Draw> draws = new ArrayList<>();
		for (PDrawGroup<RenderType, PBakedMesh, PRenderQueue.InstanceData> group : plan.groups())
		{
			if (group.instances().isEmpty())
				continue;
			int baseInstance = allInstances.size();
			allInstances.addAll(group.instances());
			PBakedMesh mesh = group.mesh();
			draws.add(new Draw(group.pipeline(), mesh, this.geometry.resolve(mesh), group.instances().size(), baseInstance, group.writeDepth()));
		}
		if (draws.isEmpty())
			return;

		boolean persistent = GL.getCapabilities().GL_ARB_buffer_storage || GL.getCapabilities().OpenGL44;
		int frameSlot = persistent ? this.frameArena.begin() : 0;
		if (frameSlot < 0)
			return;
		PGlInstanceStream.Upload instanceStream = this.instances.upload(allInstances, frameSlot, persistent);
		boolean multiDraw = GL.getCapabilities().GL_ARB_multi_draw_indirect || GL.getCapabilities().OpenGL43;
		this.indirect.begin(draws.size(), frameSlot, persistent && multiDraw);
		try
		{
			List<Draw> standardDraws = new ArrayList<>();
			List<Draw> oitDraws = new ArrayList<>();
			for (Draw draw : draws)
			{
				if (!draw.writeDepth() && PRenderTypes.usesOit(draw.type()))
					oitDraws.add(draw);
				else
					standardDraws.add(draw);
			}

			drawGroups(standardDraws, instanceStream, multiDraw, false);
			if (!oitDraws.isEmpty())
			{
				RenderTargetAttachments attachments = attachments(oitDraws.getFirst().type());
				if (this.weightedBlendedOit.begin(attachments.color(), attachments.depth()))
				{
					try
					{
						drawGroups(oitDraws, instanceStream, multiDraw, true);
					}
					finally
					{
						this.weightedBlendedOit.endAccumulation();
					}
				}
				else
					drawGroups(oitDraws, instanceStream, multiDraw, false);
			}
		}
		finally
		{
			if (persistent)
				this.frameArena.finish(frameSlot);
		}
	}

	public void compositeOit()
	{
		this.weightedBlendedOit.composite();
	}

	public void cleanup()
	{
		this.frameArena.awaitAll();
		this.geometry.clear();
		this.instances.close();
		this.indirect.close();
		this.weightedBlendedOit.close();
	}

	private void drawGroups(List<Draw> draws, PGlInstanceStream.Upload instanceStream, boolean multiDraw, boolean oit)
	{
		for (int start = 0; start < draws.size();)
		{
			if (draws.get(start).writeDepth())
			{
				int end = findOpaquePipelineEnd(draws, start);
				drawOpaquePipeline(draws.subList(start, end), instanceStream, multiDraw);
				start = end;
				continue;
			}
			int end = findBatchEnd(draws, start);
			draw(draws.subList(start, end), instanceStream, multiDraw && end - start > 1, oit);
			start = end;
		}
	}

	private static int findBatchEnd(List<Draw> draws, int start)
	{
		Draw first = draws.get(start);
		int end = start + 1;
		while (end < draws.size())
		{
			Draw candidate = draws.get(end);
			if (candidate.type() != first.type() || candidate.slice().page() != first.slice().page() ||
					candidate.slice().indexType() != first.slice().indexType() || candidate.writeDepth() != first.writeDepth())
				break;
			end++;
		}
		return end;
	}

	private static int findOpaquePipelineEnd(List<Draw> draws, int start)
	{
		RenderType type = draws.get(start).type();
		int end = start + 1;
		while (end < draws.size() && draws.get(end).writeDepth() && draws.get(end).type() == type)
			end++;
		return end;
	}

	private void drawOpaquePipeline(List<Draw> draws, PGlInstanceStream.Upload instanceStream, boolean multiDraw)
	{
		Map<ArenaKey, List<Draw>> batches = new LinkedHashMap<>();
		for (Draw draw : draws)
			batches.computeIfAbsent(new ArenaKey(draw.slice().page(), draw.slice().indexType()), ignored -> new ArrayList<>()).add(draw);
		for (List<Draw> batch : batches.values())
			draw(batch, instanceStream, multiDraw && batch.size() > 1, false);
	}

	private void draw(List<Draw> batch, PGlInstanceStream.Upload instanceStream, boolean multiDraw, boolean oit)
	{
		Draw first = batch.getFirst();
		RenderType type = first.type();
		Minecraft mc = PLibRenderHelper.mc();
		TextureAtlas atlas = PTextureCache.getTextureAtlas();
		GpuTextureView lightTexture = mc.gameRenderer.levelLightmap();
		OverlayTexture overlayTexture = mc.gameRenderer.overlayTexture();
		PGpuDeformerBuffers.Bindings deformerBuffers = PGpuDeformerBuffers.upload();
		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform(
				RenderSystem.getModelViewMatrix(), new Vector4f(1.0F), new Vector3f(), new Matrix4f());

		RenderTargetAttachments attachments = attachments(type);

		try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
				first.mesh().uuid() :: toString, attachments.color(), OptionalInt.empty(), attachments.depth(), OptionalDouble.empty()))
		{
			pass.setPipeline(oit ? PRenderTypes.oitPipeline(type) : type.pipeline());
			RenderSystem.bindDefaultUniforms(pass);
			pass.setUniform("DynamicTransforms", dynamicTransforms);
			pass.setUniform("DeformerOperations", deformerBuffers.operations());
			pass.setUniform("DeformerValues", deformerBuffers.values());
			applyActiveScissor(pass);
			pass.bindTexture("Sampler0", atlas.getTextureView(), atlas.getSampler());
			pass.bindTexture("Sampler1", overlayTexture.getTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
			pass.bindTexture("Sampler2", lightTexture, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));
			pass.setVertexBuffer(0, first.mesh().vbo());
			pass.setIndexBuffer(first.mesh().indices(), first.mesh().indexType());
			pass.drawIndexed(0, 0, 0, 1);
			if (oit)
				this.weightedBlendedOit.bindForDraw();

			if (!first.writeDepth())
				GlStateManager._depthMask(false);
			try
			{
				first.slice().page().bind(instanceStream.buffer(), instanceStream.offset());
				if (multiDraw)
				{
					List<PGlIndirectStream.Command> commands = new ArrayList<>(batch.size());
					for (Draw draw : batch)
						commands.add(command(draw));
					PGlIndirectStream.Upload indirectStream = this.indirect.upload(commands);
					ARBMultiDrawIndirect.glMultiDrawElementsIndirect(GL11.GL_TRIANGLES, first.slice().indexType(), indirectStream.offset(), commands.size(), 0);
				}
				else
					for (Draw draw : batch)
					{
						draw.slice().page().setInstanceOffset(instanceStream.offset() + (long)draw.baseInstance() * PGlInstanceStream.STRIDE);
						GL32.glDrawElementsInstancedBaseVertex(GL11.GL_TRIANGLES, draw.slice().indexCount(), draw.slice().indexType(),
								draw.slice().indexOffset(), draw.instanceCount(), draw.slice().baseVertex());
					}
			}
			finally
			{
				if (!first.writeDepth() && !oit)
					GlStateManager._depthMask(true);
				GL30.glBindVertexArray(0);
				GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, 0);
				GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			}
		}
	}

	private static RenderTargetAttachments attachments(RenderType type)
	{
		RenderTarget target = type.outputTarget().getRenderTarget();
		GpuTextureView color = RenderSystem.outputColorTextureOverride != null ?
				RenderSystem.outputColorTextureOverride : target.getColorTextureView();
		GpuTextureView depth = target.useDepth ?
				(RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : target.getDepthTextureView()) : null;
		if (color == null)
			throw new IllegalStateException("Render type " + type + " has no color attachment");
		return new RenderTargetAttachments(color, depth);
	}

	private static PGlIndirectStream.Command command(Draw draw)
	{
		PGlGeometryArena.Slice slice = draw.slice();
		return new PGlIndirectStream.Command(slice.indexCount(), draw.instanceCount(),
				Math.toIntExact(slice.indexOffset() / (slice.indexType() == GL11.GL_UNSIGNED_SHORT ? Short.BYTES : Integer.BYTES)),
				slice.baseVertex(), draw.baseInstance());
	}

	private static void applyActiveScissor(RenderPass pass)
	{
		ScissorState scissor = RenderSystem.getScissorStateForRenderTypeDraws();
		if (scissor.enabled())
			pass.enableScissor(scissor.x(), scissor.y(), scissor.width(), scissor.height());
		else
			pass.disableScissor();
	}

	private record Draw(RenderType type, PBakedMesh mesh, PGlGeometryArena.Slice slice,
	                    int instanceCount, int baseInstance, boolean writeDepth)
	{
	}

	private record ArenaKey(PGlGeometryArena.Page page, int indexType)
	{
	}

	private record RenderTargetAttachments(GpuTextureView color, GpuTextureView depth)
	{
	}
}
