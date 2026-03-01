/**
 * @author ArcAnc
 * Created at: 01.03.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.mixin;


import com.arcanc.arcslib.content.renderer.itemHacks.ITrackingItemStackRenderStateCoordsGetter;
import com.arcanc.arcslib.util.Database;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(GuiGraphics.class)
public class GuiGraphicsCoordsHack
{
	@Inject(method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
			at = @At (value = "INVOKE",
					  target = "Lnet/minecraft/client/renderer/item/ItemModelResolver;updateForTopItem(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/ItemOwner;I)V"),
	        locals = LocalCapture.CAPTURE_FAILSOFT)
	private void renderItem(LivingEntity owner,
	                        Level level,
	                        ItemStack itemStack,
	                        int x,
	                        int y,
	                        int seed,
	                        CallbackInfo ci,
	                        TrackingItemStackRenderState itemStackRenderState)
	{
		if (itemStackRenderState instanceof ITrackingItemStackRenderStateCoordsGetter getter)
		{
			getter.arcsLib$setX(x);
			getter.arcsLib$setY(y);
		}
	}
}
