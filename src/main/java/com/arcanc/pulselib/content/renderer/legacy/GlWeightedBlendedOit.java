/**
 * @author ArcAnc
 * Created at: 15.08.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.renderer.legacy;

import com.arcanc.pulselib.util.PRenderTypes;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.neoforge.client.GlStateBackup;
import org.lwjgl.opengl.ARBDrawBuffersBlend;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL40;

public final class GlWeightedBlendedOit
{
	private static final float[] CLEAR_ACCUM = {0f, 0f, 0f, 0f};
	private static final float[] CLEAR_REVEAL = {1f, 1f, 1f, 1f};

	private int framebuffer = -1;
	private int accumulationTexture = -1;
	private int revealageTexture = -1;
	private int width = -1;
	private int height = -1;
	private int depthTexture = -1;
	private boolean frameOpen;
	private boolean accumulationBound;
	private boolean hasContent;
	private int previousDrawFramebuffer;
	private int previousReadFramebuffer;
	private final int[] previousViewport = new int[4];
	private final GlStateBackup previousGlState = new GlStateBackup();

	public boolean isSupported()
	{
		return GL.getCapabilities().OpenGL40 || GL.getCapabilities().GL_ARB_draw_buffers_blend;
	}

	public boolean begin(RenderTarget depthSource)
	{
		if (!isSupported() || PRenderTypes.ShadersProvider.oitComposite() == null)
			return false;
		if (this.accumulationBound)
			throw new IllegalStateException("Weighted blended OIT accumulation is already active");
		if (depthSource.width <= 0 || depthSource.height <= 0 || depthSource.getDepthTextureId() < 0)
			return false;

		captureFramebufferState();
		try
		{
			ensureBuffers(depthSource);
		}
		catch (RuntimeException ignored)
		{
			close();
			restoreFramebufferState();
			return false;
		}

		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, this.framebuffer);
		GL11.glViewport(0, 0, this.width, this.height);
		GL20.glDrawBuffers(new int[]{GL30.GL_COLOR_ATTACHMENT0, GL30.GL_COLOR_ATTACHMENT1});
		if (!this.frameOpen)
		{
			GL30.glClearBufferfv(GL11.GL_COLOR, 0, CLEAR_ACCUM);
			GL30.glClearBufferfv(GL11.GL_COLOR, 1, CLEAR_REVEAL);
			this.frameOpen = true;
		}
		RenderSystem.enableDepthTest();
		RenderSystem.depthMask(false);
		this.accumulationBound = true;
		return true;
	}

	public void endAccumulation()
	{
		if (!this.accumulationBound)
			return;
		this.hasContent = true;
		this.accumulationBound = false;
		restoreFramebufferState();
	}

	public void configureBlend()
	{
		RenderSystem.enableBlend();
		if (GL.getCapabilities().OpenGL40)
		{
			GL40.glBlendEquationi(0, GL14.GL_FUNC_ADD);
			GL40.glBlendFunci(0, GL11.GL_ONE, GL11.GL_ONE);
			GL40.glBlendEquationi(1, GL14.GL_FUNC_ADD);
			GL40.glBlendFunci(1, GL11.GL_ZERO, GL11.GL_ONE_MINUS_SRC_COLOR);
		}
		else
		{
			ARBDrawBuffersBlend.glBlendEquationiARB(0, GL14.GL_FUNC_ADD);
			ARBDrawBuffersBlend.glBlendFunciARB(0, GL11.GL_ONE, GL11.GL_ONE);
			ARBDrawBuffersBlend.glBlendEquationiARB(1, GL14.GL_FUNC_ADD);
			ARBDrawBuffersBlend.glBlendFunciARB(1, GL11.GL_ZERO, GL11.GL_ONE_MINUS_SRC_COLOR);
		}
	}

	public void composite(RenderTarget destination)
	{
		ShaderInstance shader = PRenderTypes.ShadersProvider.oitComposite();
		if (shader == null || this.framebuffer < 0 || !this.frameOpen || !this.hasContent)
		{
			finishFrame();
			return;
		}
		if (this.accumulationBound)
			throw new IllegalStateException("Cannot composite weighted blended OIT while accumulation is active");

		captureFramebufferState();
		destination.bindWrite(true);
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		try
		{
			shader.setSampler("AccumSampler", this.accumulationTexture);
			shader.setSampler("RevealSampler", this.revealageTexture);
			shader.apply();
			BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
			buffer.addVertex(0f, 0f, 0f);
			buffer.addVertex(1f, 0f, 0f);
			buffer.addVertex(1f, 1f, 0f);
			buffer.addVertex(0f, 1f, 0f);
			BufferUploader.draw(buffer.buildOrThrow());
		}
		finally
		{
			shader.clear();
			RenderSystem.disableBlend();
			RenderSystem.depthMask(true);
			RenderSystem.enableDepthTest();
			restoreFramebufferState();
			finishFrame();
		}
	}

	public void finishFrame()
	{
		this.frameOpen = false;
		this.hasContent = false;
	}

	public void close()
	{
		if (this.accumulationTexture >= 0)
			GL11.glDeleteTextures(this.accumulationTexture);
		if (this.revealageTexture >= 0)
			GL11.glDeleteTextures(this.revealageTexture);
		if (this.framebuffer >= 0)
			GL30.glDeleteFramebuffers(this.framebuffer);
		this.framebuffer = -1;
		this.accumulationTexture = -1;
		this.revealageTexture = -1;
		this.width = -1;
		this.height = -1;
		this.depthTexture = -1;
		this.frameOpen = false;
		this.accumulationBound = false;
		this.hasContent = false;
	}

	private void ensureBuffers(RenderTarget target)
	{
		if (this.framebuffer >= 0 && this.width == target.width && this.height == target.height && this.depthTexture == target.getDepthTextureId())
			return;

		close();
		this.width = target.width;
		this.height = target.height;
		this.depthTexture = target.getDepthTextureId();
		this.framebuffer = GL30.glGenFramebuffers();
		this.accumulationTexture = createTexture(GL30.GL_RGBA16F, GL11.GL_RGBA, this.width, this.height);
		this.revealageTexture = createTexture(GL30.GL_R16F, GL11.GL_RED, this.width, this.height);

		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, this.framebuffer);
		GL32.glFramebufferTexture(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, this.accumulationTexture, 0);
		GL32.glFramebufferTexture(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT1, this.revealageTexture, 0);
		GL32.glFramebufferTexture(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, this.depthTexture, 0);
		GL20.glDrawBuffers(new int[]{GL30.GL_COLOR_ATTACHMENT0, GL30.GL_COLOR_ATTACHMENT1});
		if (GL30.glCheckFramebufferStatus(GL30.GL_DRAW_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
			throw new IllegalStateException("Unable to create weighted blended OIT framebuffer");
	}

	private void captureFramebufferState()
	{
		this.previousDrawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
		this.previousReadFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
		GL11.glGetIntegerv(GL11.GL_VIEWPORT, this.previousViewport);
		RenderSystem.backupGlState(this.previousGlState);
	}

	private void restoreFramebufferState()
	{
		GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, this.previousDrawFramebuffer);
		GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, this.previousReadFramebuffer);
		GL11.glViewport(this.previousViewport[0], this.previousViewport[1], this.previousViewport[2], this.previousViewport[3]);
		RenderSystem.restoreGlState(this.previousGlState);
	}

	private static int createTexture(int internalFormat, int format, int width, int height)
	{
		int texture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL11.GL_FLOAT, 0L);
		return texture;
	}
}
