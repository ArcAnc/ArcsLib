/**
 * @author ArcAnc
 * Created at: 21.08.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer;

import com.arcanc.pulselib.util.PLibDatabase;
import com.arcanc.pulselib.util.PRenderTypes;
import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderPassDescriptor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;

final class RhiWeightedBlendedOit
{
	static final int LAYER_COUNT = 2;
	private static final Vector4fc CLEAR_ACCUMULATION = new Vector4f(0.0F);
	private static final int TEXTURE_USAGE = GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_RENDER_ATTACHMENT;

	private final @Nullable GpuTexture[] accumulationTextures = new GpuTexture[LAYER_COUNT];
	private final @Nullable GpuTextureView[] accumulationViews = new GpuTextureView[LAYER_COUNT];
	private final @Nullable GpuTexture[] revealageTextures = new GpuTexture[LAYER_COUNT];
	private final @Nullable GpuTextureView[] revealageViews = new GpuTextureView[LAYER_COUNT];
	private @Nullable GpuTexture depthPassColorTexture;
	private @Nullable GpuTextureView depthPassColorView;
	private final @Nullable GpuTexture[] layerDepthTextures = new GpuTexture[LAYER_COUNT];
	private final @Nullable GpuTextureView[] layerDepthViews = new GpuTextureView[LAYER_COUNT];
	private int width = -1;
	private int height = -1;
	private boolean disabled;
	private boolean frameOpen;
	private final boolean[] clearNextDepthPass = new boolean[LAYER_COUNT];
	private final boolean[] clearNextAccumulationPass = new boolean[LAYER_COUNT];
	private final boolean[] hasContent = new boolean[LAYER_COUNT];
	private @Nullable GpuTextureView destinationColor;
	private @Nullable GpuTextureView destinationDepth;

	boolean begin(GpuTextureView colorAttachment, @Nullable GpuTextureView depthAttachment)
	{
		if (this.disabled || depthAttachment == null || colorAttachment.getWidth(0) <= 0 || colorAttachment.getHeight(0) <= 0)
			return false;
		if (this.frameOpen)
			return matchesFrame(colorAttachment, depthAttachment);

		try
		{
			ensureBuffers(colorAttachment.getWidth(0), colorAttachment.getHeight(0));
			this.destinationColor = colorAttachment;
			this.destinationDepth = depthAttachment;
			this.frameOpen = true;
			for (int layer = 0; layer < LAYER_COUNT; layer++)
			{
				this.clearNextDepthPass[layer] = true;
				this.clearNextAccumulationPass[layer] = true;
			}
			return true;
		}
		catch (RuntimeException exception)
		{
			PLibDatabase.LOGGER.warn("Disabling weighted blended OIT after RHI target initialization failed", exception);
			this.disabled = true;
			closeBuffers();
			finishFrame();
			return false;
		}
	}

	RenderPass createDepthPass(int layer, Supplier<String> label)
	{
		GpuTextureView color = requireView(this.depthPassColorView, "depth-pass color");
		GpuTextureView layerDepth = layerDepthView(layer);
		OptionalDouble clear = this.clearNextDepthPass[layer] ? OptionalDouble.of(0.0) : OptionalDouble.empty();
		RenderPassDescriptor descriptor = RenderPassDescriptor.create(label).
				withColorAttachment(color, Optional.empty()).
				withDepthAttachment(layerDepth, clear).
				withRenderArea(fullArea(color));
		RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(descriptor);
		this.clearNextDepthPass[layer] = false;
		return pass;
	}

	RenderPass createAccumulationPass(int layer, Supplier<String> label)
	{
		GpuTextureView accumulation = accumulationView(layer);
		GpuTextureView revealage = revealageView(layer);
		GpuTextureView depth = requireView(this.destinationDepth, "destination depth");
		Optional<Vector4fc> accumulationClear = this.clearNextAccumulationPass[layer] ? Optional.of(CLEAR_ACCUMULATION) : Optional.empty();
		Optional<Vector4fc> revealageClear = this.clearNextAccumulationPass[layer] ? Optional.of(CLEAR_ACCUMULATION) : Optional.empty();
		RenderPassDescriptor descriptor = RenderPassDescriptor.create(label).
				withColorAttachment(accumulation, accumulationClear).
				withColorAttachment(revealage, revealageClear).
				withDepthAttachment(depth, OptionalDouble.empty()).
				withRenderArea(fullArea(accumulation));
		RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(descriptor);
		this.clearNextAccumulationPass[layer] = false;
		return pass;
	}

	GpuTextureView layerDepthView(int layer)
	{
		return requireView(this.layerDepthViews[layer], "depth layer " + layer);
	}

	private GpuTextureView accumulationView(int layer)
	{
		return requireView(this.accumulationViews[layer], "accumulation layer " + layer);
	}

	private GpuTextureView revealageView(int layer)
	{
		return requireView(this.revealageViews[layer], "revealage layer " + layer);
	}

	void markContent(int layer)
	{
		this.hasContent[layer] = true;
	}

	void composite()
	{
		if (!this.frameOpen)
		{
			finishFrame();
			return;
		}

		GpuTextureView color = requireView(this.destinationColor, "destination color");
		GpuTextureView depth = requireView(this.destinationDepth, "destination depth");
		try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
				() -> "PulseLib weighted OIT composite", color, Optional.empty(), depth, OptionalDouble.empty()))
		{
			pass.setPipeline(PRenderTypes.RenderPipelinesProvider.OIT_COMPOSITE);
			pass.disableScissor();
			GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
			for (int layer = LAYER_COUNT - 1; layer >= 0; layer--)
			{
				if (!this.hasContent[layer])
					continue;
				pass.bindTexture("AccumSampler", accumulationView(layer), sampler);
				pass.bindTexture("RevealSampler", revealageView(layer), sampler);
				pass.bindTexture("DepthSampler", layerDepthView(layer), sampler);
				pass.draw(3, 1, 0, 0);
			}
		}
		finally
		{
			finishFrame();
		}
	}

	void close()
	{
		finishFrame();
		closeBuffers();
		this.disabled = false;
	}

	private void ensureBuffers(int targetWidth, int targetHeight)
	{
		if (this.accumulationTextures[0] != null && this.width == targetWidth && this.height == targetHeight)
			return;
		closeBuffers();
		this.width = targetWidth;
		this.height = targetHeight;
		for (int layer = 0; layer < LAYER_COUNT; layer++)
		{
			this.accumulationTextures[layer] = createTexture("PulseLib OIT accumulation " + layer, GpuFormat.RGBA16_FLOAT, targetWidth, targetHeight);
			this.accumulationViews[layer] = RenderSystem.getDevice().createTextureView(this.accumulationTextures[layer]);
			this.revealageTextures[layer] = createTexture("PulseLib OIT revealage " + layer, GpuFormat.R16_FLOAT, targetWidth, targetHeight);
			this.revealageViews[layer] = RenderSystem.getDevice().createTextureView(this.revealageTextures[layer]);
			this.layerDepthTextures[layer] = createTexture("PulseLib OIT depth layer " + layer, GpuFormat.D32_FLOAT, targetWidth, targetHeight);
			this.layerDepthViews[layer] = RenderSystem.getDevice().createTextureView(this.layerDepthTextures[layer]);
		}
		this.depthPassColorTexture = createTexture("PulseLib OIT depth-pass color", GpuFormat.R8_UNORM, targetWidth, targetHeight);
		this.depthPassColorView = RenderSystem.getDevice().createTextureView(this.depthPassColorTexture);
	}

	private boolean matchesFrame(GpuTextureView colorAttachment, GpuTextureView depthAttachment)
	{
		return this.destinationColor != null && this.destinationDepth != null &&
				this.destinationColor.texture() == colorAttachment.texture() &&
				this.destinationDepth.texture() == depthAttachment.texture() &&
				this.width == colorAttachment.getWidth(0) && this.height == colorAttachment.getHeight(0);
	}

	private static GpuTexture createTexture(String label, GpuFormat format, int width, int height)
	{
		return RenderSystem.getDevice().createTexture(label, TEXTURE_USAGE, format, width, height, 1, 1);
	}

	private static RenderPass.RenderArea fullArea(GpuTextureView texture)
	{
		return new RenderPass.RenderArea(0, 0, texture.getWidth(0), texture.getHeight(0));
	}

	private void closeBuffers()
	{
		for (int layer = 0; layer < LAYER_COUNT; layer++)
		{
			close(this.accumulationViews[layer]);
			close(this.revealageViews[layer]);
			close(this.layerDepthViews[layer]);
			close(this.accumulationTextures[layer]);
			close(this.revealageTextures[layer]);
			close(this.layerDepthTextures[layer]);
			this.accumulationViews[layer] = null;
			this.revealageViews[layer] = null;
			this.layerDepthViews[layer] = null;
			this.accumulationTextures[layer] = null;
			this.revealageTextures[layer] = null;
			this.layerDepthTextures[layer] = null;
		}
		close(this.depthPassColorView);
		close(this.depthPassColorTexture);
		this.depthPassColorView = null;
		this.depthPassColorTexture = null;
		this.width = -1;
		this.height = -1;
	}

	private void finishFrame()
	{
		this.frameOpen = false;
		for (int layer = 0; layer < LAYER_COUNT; layer++)
		{
			this.clearNextDepthPass[layer] = false;
			this.clearNextAccumulationPass[layer] = false;
			this.hasContent[layer] = false;
		}
		this.destinationColor = null;
		this.destinationDepth = null;
	}

	private static GpuTextureView requireView(@Nullable GpuTextureView view, String name)
	{
		if (view == null)
			throw new IllegalStateException("Weighted blended OIT has no " + name + " attachment");
		return view;
	}

	private static void close(@Nullable AutoCloseable resource)
	{
		if (resource == null)
			return;
		try
		{
			resource.close();
		}
		catch (Exception exception)
		{
			throw new RuntimeException(exception);
		}
	}
}
