/**
 * @author ArcAnc
 * Created at: 21.08.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer.gl;

import com.arcanc.pulselib.util.PLibDatabase;
import com.arcanc.pulselib.util.PRenderTypes;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.opengl.GlTextureView;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.ARBDrawBuffersBlend;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL40;

import java.util.OptionalDouble;
import java.util.OptionalInt;

final class PGlWeightedBlendedOit
{
	private static final float[] CLEAR_ACCUMULATION = {0.0F, 0.0F, 0.0F, 0.0F};
	private static final float[] CLEAR_REVEALAGE = {1.0F, 1.0F, 1.0F, 1.0F};
	private static final float[] CLEAR_DEPTH = {1.0F, 1.0F, 1.0F, 1.0F};

	private int framebuffer = -1;
	private @Nullable ExternalGlTexture accumulationTexture;
	private @Nullable ExternalGlTextureView accumulationView;
	private @Nullable ExternalGlTexture revealageTexture;
	private @Nullable ExternalGlTextureView revealageView;
	private @Nullable ExternalGlTexture fragmentDepthTexture;
	private @Nullable ExternalGlTextureView fragmentDepthView;
	private int width = -1;
	private int height = -1;
	private int depthTexture = -1;
	private boolean depthStencil;
	private boolean disabled;
	private boolean frameOpen;
	private boolean accumulationBound;
	private boolean hasContent;
	private int destinationColorTexture = -1;
	private @Nullable GpuTextureView destinationColor;
	private @Nullable GpuTextureView destinationDepth;
	private @Nullable GlStateSnapshot accumulationState;

	public boolean begin(GpuTextureView colorAttachment, @Nullable GpuTextureView depthAttachment)
	{
		if (this.disabled || !isSupported())
			return false;
		if (this.accumulationBound)
			throw new IllegalStateException("Weighted blended OIT accumulation is already active");
		if (depthAttachment == null || colorAttachment.getWidth(0) <= 0 || colorAttachment.getHeight(0) <= 0)
			return false;
		if (this.frameOpen && !matchesFrame(colorAttachment, depthAttachment))
			return false;

		GlStateSnapshot state = GlStateSnapshot.capture();
		try
		{
			this.ensureBuffers(colorAttachment, depthAttachment);
			this.bindForDraw();
			if (!this.frameOpen)
			{
				this.destinationColor = colorAttachment;
				this.destinationDepth = depthAttachment;
				this.destinationColorTexture = colorTexture(colorAttachment);
				GlStateManager._disableScissorTest();
				GL30.glClearBufferfv(GL11.GL_COLOR, 0, CLEAR_ACCUMULATION);
				GL30.glClearBufferfv(GL11.GL_COLOR, 1, CLEAR_REVEALAGE);
				GL30.glClearBufferfv(GL11.GL_COLOR, 2, CLEAR_DEPTH);
				this.frameOpen = true;
			}
			this.accumulationState = state;
			this.accumulationBound = true;
			return true;
		}
		catch (RuntimeException exception)
		{
			PLibDatabase.LOGGER.warn("Disabling weighted blended OIT after framebuffer initialization failed", exception);
			this.disabled = true;
			this.closeBuffers();
			this.finishFrame();
			state.restore();
			return false;
		}
	}

	public void endAccumulation()
	{
		if (!this.accumulationBound)
			return;
		this.hasContent = true;
		this.accumulationBound = false;
		GlStateSnapshot state = this.accumulationState;
		this.accumulationState = null;
		if (state != null)
			state.restore();
	}

	public void bindForDraw()
	{
		GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.framebuffer);
		GlStateManager._viewport(0, 0, this.width, this.height);
		GL20.glDrawBuffers(new int[]{GL30.GL_COLOR_ATTACHMENT0, GL30.GL_COLOR_ATTACHMENT1, GL30.GL_COLOR_ATTACHMENT2});
		GlStateManager._enableBlend();
		GlStateManager._blendFuncSeparate(GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ONE);
		GL20.glBlendEquationSeparate(GL14.GL_FUNC_ADD, GL14.GL_FUNC_ADD);
		GlStateManager._depthMask(false);
		if (GL.getCapabilities().OpenGL40)
		{
			GL40.glBlendEquationi(1, GL14.GL_FUNC_ADD);
			GL40.glBlendFunci(1, GL11.GL_ZERO, GL11.GL_ONE_MINUS_SRC_COLOR);
			GL40.glBlendEquationi(2, GL14.GL_MIN);
			GL40.glBlendFunci(2, GL11.GL_ONE, GL11.GL_ONE);
		}
		else
		{
			ARBDrawBuffersBlend.glBlendEquationiARB(1, GL14.GL_FUNC_ADD);
			ARBDrawBuffersBlend.glBlendFunciARB(1, GL11.GL_ZERO, GL11.GL_ONE_MINUS_SRC_COLOR);
			ARBDrawBuffersBlend.glBlendEquationiARB(2, GL14.GL_MIN);
			ARBDrawBuffersBlend.glBlendFunciARB(2, GL11.GL_ONE, GL11.GL_ONE);
		}
	}

	public void composite()
	{
		if (!this.frameOpen || !this.hasContent)
		{
			this.finishFrame();
			return;
		}
		if (this.accumulationBound)
			throw new IllegalStateException("Cannot composite weighted blended OIT while accumulation is active");
		GpuTextureView colorAttachment = this.destinationColor;
		if (colorAttachment == null)
			throw new IllegalStateException("Weighted blended OIT frame has no destination color attachment");
		GpuTextureView depthAttachment = this.destinationDepth;
		if (depthAttachment == null)
			throw new IllegalStateException("Weighted blended OIT frame has no destination depth attachment");
		GpuTextureView accumulation = this.accumulationView;
		GpuTextureView revealage = this.revealageView;
		GpuTextureView fragmentDepth = this.fragmentDepthView;
		if (accumulation == null || revealage == null || fragmentDepth == null)
			throw new IllegalStateException("Weighted blended OIT frame has no accumulation textures");

		GlStateSnapshot state = GlStateSnapshot.capture();
		try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
				() -> "PulseLib weighted OIT composite", colorAttachment, OptionalInt.empty(), depthAttachment, OptionalDouble.empty()))
		{
			pass.setPipeline(PRenderTypes.RenderPipelinesProvider.OIT_COMPOSITE);
			pass.disableScissor();
			var sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
			pass.bindTexture("AccumSampler", accumulation, sampler);
			pass.bindTexture("RevealSampler", revealage, sampler);
			pass.bindTexture("DepthSampler", fragmentDepth, sampler);
			pass.draw(0, 3);
		}
		finally
		{
			state.restore();
			this.finishFrame();
		}
	}

	public void close()
	{
		if (this.accumulationBound)
			this.endAccumulation();
		this.closeBuffers();
		this.finishFrame();
		this.disabled = false;
	}

	private void finishFrame()
	{
		this.frameOpen = false;
		this.accumulationBound = false;
		this.hasContent = false;
		this.destinationColorTexture = -1;
		this.destinationColor = null;
		this.destinationDepth = null;
		this.accumulationState = null;
	}

	private static boolean isSupported()
	{
		return GL.getCapabilities().OpenGL40 || GL.getCapabilities().GL_ARB_draw_buffers_blend;
	}

	private void ensureBuffers(GpuTextureView colorAttachment, @Nullable GpuTextureView depthAttachment)
	{
		if (matchesBuffers(colorAttachment, depthAttachment))
			return;

		int targetWidth = colorAttachment.getWidth(0);
		int targetHeight = colorAttachment.getHeight(0);
		int targetDepth = depthTexture(depthAttachment);
		boolean targetDepthStencil = hasDepthStencil(depthAttachment);
		this.closeBuffers();
		this.width = targetWidth;
		this.height = targetHeight;
		this.depthTexture = targetDepth;
		this.depthStencil = targetDepthStencil;
		this.accumulationTexture = createTexture("PulseLib OIT accumulation", TextureFormat.RGBA8,
				GL30.GL_RGBA16F, GL11.GL_RGBA, targetWidth, targetHeight);
		this.accumulationView = new ExternalGlTextureView(this.accumulationTexture);
		this.revealageTexture = createTexture("PulseLib OIT revealage", TextureFormat.RED8,
				GL30.GL_R16F, GL11.GL_RED, targetWidth, targetHeight);
		this.revealageView = new ExternalGlTextureView(this.revealageTexture);
		this.fragmentDepthTexture = createTexture("PulseLib OIT fragment depth", TextureFormat.RED8,
				GL30.GL_R32F, GL11.GL_RED, targetWidth, targetHeight);
		this.fragmentDepthView = new ExternalGlTextureView(this.fragmentDepthTexture);
		this.framebuffer = GlStateManager.glGenFramebuffers();
		GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.framebuffer);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, this.accumulationTexture.glId(), 0);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT1, this.revealageTexture.glId(), 0);
		GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT2, this.fragmentDepthTexture.glId(), 0);
		if (targetDepth != 0)
			GL32.glFramebufferTexture(GL30.GL_FRAMEBUFFER,
					targetDepthStencil ? GL30.GL_DEPTH_STENCIL_ATTACHMENT : GL30.GL_DEPTH_ATTACHMENT, targetDepth, 0);
		GL20.glDrawBuffers(new int[]{GL30.GL_COLOR_ATTACHMENT0, GL30.GL_COLOR_ATTACHMENT1, GL30.GL_COLOR_ATTACHMENT2});
		int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
		if (status != GL30.GL_FRAMEBUFFER_COMPLETE)
			throw new IllegalStateException("Unable to create weighted OIT framebuffer: 0x" + Integer.toHexString(status));
	}

	private boolean matchesBuffers(GpuTextureView colorAttachment, @Nullable GpuTextureView depthAttachment)
	{
		return this.framebuffer >= 0 && this.width == colorAttachment.getWidth(0) &&
				this.height == colorAttachment.getHeight(0) && this.depthTexture == depthTexture(depthAttachment) &&
				this.depthStencil == hasDepthStencil(depthAttachment);
	}

	private boolean matchesFrame(GpuTextureView colorAttachment, @Nullable GpuTextureView depthAttachment)
	{
		return matchesBuffers(colorAttachment, depthAttachment) &&
				this.destinationColorTexture == colorTexture(colorAttachment);
	}

	private static int colorTexture(GpuTextureView colorAttachment)
	{
		return ((GlTexture)colorAttachment.texture()).glId();
	}

	private static int depthTexture(@Nullable GpuTextureView depthAttachment)
	{
		return depthAttachment == null ? 0 : ((GlTexture)depthAttachment.texture()).glId();
	}

	private static boolean hasDepthStencil(@Nullable GpuTextureView depthAttachment)
	{
		return depthAttachment != null && depthAttachment.texture().getFormat().hasStencilAspect();
	}

	private void closeBuffers()
	{
		if (this.accumulationView != null)
			this.accumulationView.close();
		if (this.accumulationTexture != null)
			this.accumulationTexture.close();
		if (this.revealageView != null)
			this.revealageView.close();
		if (this.revealageTexture != null)
			this.revealageTexture.close();
		if (this.fragmentDepthView != null)
			this.fragmentDepthView.close();
		if (this.fragmentDepthTexture != null)
			this.fragmentDepthTexture.close();
		if (this.framebuffer >= 0)
			GlStateManager._glDeleteFramebuffers(this.framebuffer);
		this.framebuffer = -1;
		this.accumulationTexture = null;
		this.accumulationView = null;
		this.revealageTexture = null;
		this.revealageView = null;
		this.fragmentDepthTexture = null;
		this.fragmentDepthView = null;
		this.width = -1;
		this.height = -1;
		this.depthTexture = -1;
		this.depthStencil = false;
	}

	private static ExternalGlTexture createTexture(String label, TextureFormat declaredFormat,
	                                               int internalFormat, int format, int width, int height)
	{
		int activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
		int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		int texture = GL11.glGenTextures();
		try
		{
			GlStateManager._bindTexture(texture);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL11.GL_FLOAT, 0L);
			return new ExternalGlTexture(label, declaredFormat, width, height, texture);
		}
		finally
		{
			GlStateManager._bindTexture(previousTexture);
			GlStateManager._activeTexture(activeTexture);
		}
	}

	private static final class ExternalGlTexture extends GlTexture
	{
		private ExternalGlTexture(String label, TextureFormat format, int width, int height, int id)
		{
			super(GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_RENDER_ATTACHMENT,
					label, format, width, height, 1, 1, id);
		}
	}

	private static final class ExternalGlTextureView extends GlTextureView
	{
		private ExternalGlTextureView(ExternalGlTexture texture)
		{
			super(texture, 0, 1);
		}
	}

	private static final class GlStateSnapshot
	{
		private final int drawFramebuffer;
		private final int readFramebuffer;
		private final int[] viewport = new int[4];
		private final int[] scissorBox = new int[4];
		private final boolean scissor;
		private final boolean depthTest;
		private final boolean depthMask;
		private final int depthFunction;
		private final BlendState blend0;
		private final BlendState blend1;
		private final BlendState blend2;

		private GlStateSnapshot()
		{
			this.drawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
			this.readFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
			GL11.glGetIntegerv(GL11.GL_VIEWPORT, this.viewport);
			GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, this.scissorBox);
			this.scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
			this.depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
			this.depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
			this.depthFunction = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
			this.blend0 = BlendState.capture(0);
			this.blend1 = BlendState.capture(1);
			this.blend2 = BlendState.capture(2);
		}

		private static GlStateSnapshot capture()
		{
			return new GlStateSnapshot();
		}

		private void restore()
		{
			GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, this.drawFramebuffer);
			GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, this.readFramebuffer);
			GlStateManager._viewport(this.viewport[0], this.viewport[1], this.viewport[2], this.viewport[3]);
			GlStateManager._scissorBox(this.scissorBox[0], this.scissorBox[1], this.scissorBox[2], this.scissorBox[3]);
			if (this.scissor)
				GlStateManager._enableScissorTest();
			else
				GlStateManager._disableScissorTest();
			if (this.depthTest)
				GlStateManager._enableDepthTest();
			else
				GlStateManager._disableDepthTest();
			GlStateManager._depthMask(this.depthMask);
			GlStateManager._depthFunc(this.depthFunction);
			this.blend0.restorePrimary();
			this.blend1.restoreIndexed(1);
			this.blend2.restoreIndexed(2);
		}
	}

	private record BlendState(boolean enabled, int equationRgb, int equationAlpha,
	                          int sourceRgb, int destinationRgb, int sourceAlpha, int destinationAlpha)
	{
		private static BlendState capture(int target)
		{
			return new BlendState(
					GL30.glIsEnabledi(GL11.GL_BLEND, target),
					GL30.glGetIntegeri(GL20.GL_BLEND_EQUATION_RGB, target),
					GL30.glGetIntegeri(GL20.GL_BLEND_EQUATION_ALPHA, target),
					GL30.glGetIntegeri(GL14.GL_BLEND_SRC_RGB, target),
					GL30.glGetIntegeri(GL14.GL_BLEND_DST_RGB, target),
					GL30.glGetIntegeri(GL14.GL_BLEND_SRC_ALPHA, target),
					GL30.glGetIntegeri(GL14.GL_BLEND_DST_ALPHA, target));
		}

		private void restorePrimary()
		{
			if (this.enabled)
				GlStateManager._enableBlend();
			else
				GlStateManager._disableBlend();
			GL20.glBlendEquationSeparate(this.equationRgb, this.equationAlpha);
			GlStateManager._blendFuncSeparate(this.sourceRgb, this.destinationRgb, this.sourceAlpha, this.destinationAlpha);
		}

		private void restoreIndexed(int target)
		{
			if (this.enabled)
				GL30.glEnablei(GL11.GL_BLEND, target);
			else
				GL30.glDisablei(GL11.GL_BLEND, target);
			restoreEquation(target, this.equationRgb, this.equationAlpha);
			restoreFunction(target, this.sourceRgb, this.destinationRgb, this.sourceAlpha, this.destinationAlpha);
		}

		private static void restoreEquation(int target, int rgb, int alpha)
		{
			if (GL.getCapabilities().OpenGL40)
				GL40.glBlendEquationSeparatei(target, rgb, alpha);
			else
				ARBDrawBuffersBlend.glBlendEquationSeparateiARB(target, rgb, alpha);
		}

		private static void restoreFunction(int target, int sourceRgb, int destinationRgb,
		                                    int sourceAlpha, int destinationAlpha)
		{
			if (GL.getCapabilities().OpenGL40)
				GL40.glBlendFuncSeparatei(target, sourceRgb, destinationRgb, sourceAlpha, destinationAlpha);
			else
				ARBDrawBuffersBlend.glBlendFuncSeparateiARB(target, sourceRgb, destinationRgb, sourceAlpha, destinationAlpha);
		}
	}
}
