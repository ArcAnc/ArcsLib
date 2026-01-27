/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arcslib.content.registration;

import com.arcanc.arcslib.content.block.TestBlock;
import com.arcanc.arcslib.content.block.block_entity.TestBlockEntity;
import com.arcanc.arcslib.content.item.TestBlockItem;
import com.arcanc.arcslib.util.Database;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public class Registration
{
	public static class BlockReg
	{
		public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Database.MOD_ID);
		
		public static final DeferredBlock<TestBlock> TEST_BLOCK = BLOCKS.register("test_block", () -> new TestBlock(
				BlockBehaviour.Properties.of().setId(
						ResourceKey.create(Registries.BLOCK, Database.rl("test_block"))).
						noOcclusion()));
		
		private static void init (@NotNull final IEventBus bus)
		{
			BLOCKS.register(bus);
		}
	}
	
	public static class BETypeReg
	{
		public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
				BuiltInRegistries.BLOCK_ENTITY_TYPE, Database.MOD_ID);
	
		public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TestBlockEntity>> TEST_BLOCK_ENTITY = BLOCK_ENTITIES.register("test_block_entity", () ->
				new BlockEntityType<>(TestBlockEntity :: new, BlockReg.TEST_BLOCK.get()));
		
		private static void init (@NotNull final IEventBus bus)
		{
			BLOCK_ENTITIES.register(bus);
		}
	}
	
	public static class ItemReg
	{
		public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Database.MOD_ID);
		
		public static final DeferredItem<TestBlockItem> TEST_ITEM = ITEMS.register("test_block", identifier -> new TestBlockItem(
				BlockReg.TEST_BLOCK.get(),
				new Item.Properties().setId(
						ResourceKey.create(Registries.ITEM, Database.rl("test_block")))));
		
		private static void init (@NotNull final IEventBus bus)
		{
			ITEMS.register(bus);
		}
	}
	
	public static void init(@NotNull final IEventBus bus)
	{
		BlockReg.init(bus);
		BETypeReg.init(bus);
		ItemReg.init(bus);
	}
}
