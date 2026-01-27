/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.api;


import com.arcanc.arcslib.content.model.ArcModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public abstract class ArcBlockRenderer<T extends BlockEntity & ArcAnimatable> implements ArcRenderer<T>, BlockEntityRenderer<T, BlockEntityRenderState>
{
	private final ArcModelData model;
	private T animatable;
	
	public ArcBlockRenderer(@NotNull ArcModelData model)
	{
		this.model = model;
	}
	
	@Override
	public ArcModel getArcModel()
	{
		return this.model.getModel();
	}
	
	@Override
	public T getAnimatable()
	{
		return this.animatable;
	}
	
	@Override
	public BlockEntityRenderState createRenderState()
	{
		return new BlockEntityRenderState();
	}
	
	@Override
	public void extractRenderState(T blockEntity,
	                               BlockEntityRenderState renderState,
	                               float partialTick,
	                               Vec3 cameraPosition,
	                               ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress)
	{
		BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
		this.animatable = blockEntity;
	}
}
