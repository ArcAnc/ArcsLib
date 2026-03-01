/**
 * @author ArcAnc
 * Created at: 01.03.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.mixin;

import com.arcanc.arcslib.content.renderer.base.ArcItemRenderState;
import com.arcanc.arcslib.content.renderer.itemHacks.ITrackingItemStackRenderStateCoordsGetter;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;


@Mixin (SpecialModelWrapper.class)
public class SpecialModelWrapperCoordsTransmitter
{
	@Inject (method = "update", at = @At (value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState$LayerRenderState;setExtents(Ljava/util/function/Supplier;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
	private void update(ItemStackRenderState output,
	                    ItemStack item,
	                    ItemModelResolver resolver,
	                    ItemDisplayContext displayContext,
	                    ClientLevel level,
	                    ItemOwner owner,
	                    int seed,
	                    CallbackInfo ci,
	                    ItemStackRenderState.LayerRenderState layer,
	                    Object argument)
	{
		if (displayContext == ItemDisplayContext.GUI &&
				argument instanceof ArcItemRenderState<?> arcRenderState &&
				output instanceof ITrackingItemStackRenderStateCoordsGetter getter)
			arcRenderState.extractCoordsForGui(getter.arcsLib$getX(), getter.arcsLib$getY());
	}
}
