/**
 * @author ArcAnc
 * Created at: 05.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.attachments.humanoid.armor;


import com.arcanc.pulselib.util.attachments.PLivingAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class PArmorClientExtensions implements IClientItemExtensions
{
	private static final Model<Unit> EMPTY_ARMOR_MODEL = new Model.Simple(new ModelPart(List.of(), Map.of()), RenderTypes :: armorCutoutNoCull);
	
	private final IClientItemExtensions base;
	
	public PArmorClientExtensions(IClientItemExtensions base)
	{
		this.base = base;
	}
	
	public static IClientItemExtensions buildFor(Item item, @Nullable IClientItemExtensions base)
	{
		IClientItemExtensions resolvedBase = base == null ? IClientItemExtensions.DEFAULT : base;
		if (!PLivingAttachments.contains(item))
			return resolvedBase;
		return new PArmorClientExtensions(resolvedBase);
	}
	
	@Override
	public @Nullable Font getFont(ItemStack stack, FontContext context)
	{
		return this.base.getFont(stack, context);
	}
	
	@Override
	public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack)
	{
		return this.base.getArmPose(entityLiving, hand, itemStack);
	}
	
	@Override
	public boolean applyForgeHandTransform(PoseStack poseStack,
	                                       LocalPlayer player,
	                                       HumanoidArm arm,
	                                       ItemStack itemInHand,
	                                       float partialTick,
	                                       float equipProcess,
	                                       float swingProcess)
	{
		return this.base.applyForgeHandTransform(poseStack, player, arm, itemInHand, partialTick, equipProcess, swingProcess);
	}
	
	@Override
	public Model<?> getHumanoidArmorModel(ItemStack itemStack, EquipmentClientInfo.LayerType layerType, Model original)
	{
		return this.base.getHumanoidArmorModel(itemStack, layerType, original);
	}
	
	@Override
	public Model<?> getGenericArmorModel(ItemStack itemStack, EquipmentClientInfo.LayerType layerType, Model original)
	{
		if (PLivingAttachments.hidesVanillaArmor(itemStack))
			return EMPTY_ARMOR_MODEL;
		
		return this.base.getGenericArmorModel(itemStack, layerType, original);
	}
	
	@Override
	public void setupModelAnimations(LivingEntity livingEntity,
	                                 ItemStack itemStack,
	                                 EquipmentSlot equipmentSlot,
	                                 Model model,
	                                 float limbSwing,
	                                 float limbSwingAmount,
	                                 float partialTick,
	                                 float ageInTicks,
	                                 float netHeadYaw,
	                                 float headPitch)
	{
		this.base.setupModelAnimations(livingEntity, itemStack, equipmentSlot, model, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
	}
	
	@Override
	public void renderFirstPersonOverlay(ItemStack stack,
	                                     EquipmentSlot equipmentSlot,
	                                     Player player,
	                                     GuiGraphicsExtractor guiGraphics,
	                                     DeltaTracker deltaTracker)
	{
		this.base.renderFirstPersonOverlay(stack, equipmentSlot, player, guiGraphics, deltaTracker);
	}
	
	@Override
	public boolean shouldBobAsEntity(ItemStack stack)
	{
		return this.base.shouldBobAsEntity(stack);
	}
	
	@Override
	public boolean shouldSpreadAsEntity(ItemStack stack)
	{
		return this.base.shouldSpreadAsEntity(stack);
	}
	
	@Override
	public int getArmorLayerTintColor(ItemStack stack, EquipmentClientInfo.Layer layer, int layerIdx, int fallbackColor)
	{
		return PLivingAttachments.hidesVanillaArmor(stack) ? 0 : this.base.getArmorLayerTintColor(stack, layer, layerIdx, fallbackColor);
	}
	
	@Override
	public int getDefaultDyeColor(ItemStack stack)
	{
		return this.base.getDefaultDyeColor(stack);
	}
	
	@Override
	public Identifier getScopeOverlayTexture(ItemStack stack)
	{
		return this.base.getScopeOverlayTexture(stack);
	}
	
	@Override
	public @Nullable Identifier getArmorTexture(ItemStack stack,
	                                            EquipmentClientInfo.LayerType type,
	                                            EquipmentClientInfo.Layer layer,
	                                            Identifier fallback)
	{
		return this.base.getArmorTexture(stack, type, layer, fallback);
	}
}
