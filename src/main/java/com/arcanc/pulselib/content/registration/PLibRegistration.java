/**
 * @author ArcAnc
 * Created at: 27.01.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.content.registration;

import com.arcanc.pulselib.content.model.animation.PAnimationChannel;
import com.arcanc.pulselib.content.model.animation.PAnimationChannelType;
import com.arcanc.pulselib.content.model.animation.PAnimationEventType;
import com.arcanc.pulselib.content.model.animation.PAnimationEventTypes;
import com.arcanc.pulselib.content.model.deformer.*;
import com.arcanc.pulselib.content.registration.block.TestBlock;
import com.arcanc.pulselib.content.registration.block.block_entity.TestBlockEntity;
import com.arcanc.pulselib.content.registration.entity.TestEntity;
import com.arcanc.pulselib.content.registration.item.TestBlockItem;
import com.arcanc.pulselib.util.PLibDatabase;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Set;

public class PLibRegistration
{
	public static class AnimationChannelReg
	{
		public static final ResourceKey<Registry<PAnimationChannelType<?>>> REGISTRY_KEY =
				ResourceKey.createRegistryKey(PLibDatabase.rl("animation_channel_type"));
		public static final DeferredRegister<PAnimationChannelType<?>> CHANNEL_TYPES =
				DeferredRegister.create(REGISTRY_KEY, PLibDatabase.MOD_ID);
		public static Registry<PAnimationChannelType<?>> CHANNEL_TYPE_REGISTRY;

		public static final DeferredHolder<PAnimationChannelType<?>, PAnimationChannelType<Vector3f>> POSITION =
				CHANNEL_TYPES.register("position", id -> new PAnimationChannel.Vector3fChannelType(id, new Vector3f(), false));
		public static final DeferredHolder<PAnimationChannelType<?>, PAnimationChannelType<Quaternionf>> ROTATION =
				CHANNEL_TYPES.register("rotation", PAnimationChannel.QuaternionChannelType :: new);
		public static final DeferredHolder<PAnimationChannelType<?>, PAnimationChannelType<Vector3f>> SCALE =
				CHANNEL_TYPES.register("scale", id -> new PAnimationChannel.Vector3fChannelType(id, new Vector3f(1f), true));

		private static void init(@NotNull final IEventBus bus)
		{
			CHANNEL_TYPE_REGISTRY = CHANNEL_TYPES.makeRegistry(builder ->
					builder.maxId(Integer.MAX_VALUE - 1).sync(false));
			CHANNEL_TYPES.register(bus);
		}
	}

	public static class AnimationEventReg
	{
		public static final ResourceKey<Registry<PAnimationEventType<?>>> REGISTRY_KEY =
				ResourceKey.createRegistryKey(PLibDatabase.rl("animation_event_type"));
		public static final DeferredRegister<PAnimationEventType<?>> EVENT_TYPES =
				DeferredRegister.create(REGISTRY_KEY, PLibDatabase.MOD_ID);
		public static Registry<PAnimationEventType<?>> EVENT_TYPE_REGISTRY;

		public static final DeferredHolder<PAnimationEventType<?>, PAnimationEventType<PAnimationEventTypes.SoundData>> SOUND =
				EVENT_TYPES.register("sound", () -> PAnimationEventTypes.SOUND);
		public static final DeferredHolder<PAnimationEventType<?>, PAnimationEventType<PAnimationEventTypes.ParticleData>> PARTICLE =
				EVENT_TYPES.register("particle", () -> PAnimationEventTypes.PARTICLE);
		public static final DeferredHolder<PAnimationEventType<?>, PAnimationEventType<PAnimationEventTypes.CameraShakeData>> CAMERA_SHAKE =
				EVENT_TYPES.register("camera_shake", () -> PAnimationEventTypes.CAMERA_SHAKE);
		public static final DeferredHolder<PAnimationEventType<?>, PAnimationEventType<PAnimationEventTypes.LocatorCallbackData>> LOCATOR_CALLBACK =
				EVENT_TYPES.register("locator_callback", () -> PAnimationEventTypes.LOCATOR_CALLBACK);
		public static final DeferredHolder<PAnimationEventType<?>, PAnimationEventType<PAnimationEventTypes.AnimationParameterData>> ANIMATION_PARAMETER =
				EVENT_TYPES.register("animation_parameter", () -> PAnimationEventTypes.ANIMATION_PARAMETER);

		private static void init(@NotNull final IEventBus bus)
		{
			EVENT_TYPE_REGISTRY = EVENT_TYPES.makeRegistry(builder -> builder.maxId(Integer.MAX_VALUE - 1).sync(false));
			EVENT_TYPES.register(bus);
		}
	}
	
	public static class MeshDeformerReg
	{
		public static final ResourceKey<Registry<PMeshDeformer<?>>> REGISTRY_KEY =
				ResourceKey.createRegistryKey(PLibDatabase.rl("mesh_deformer"));
		public static final DeferredRegister<PMeshDeformer<?>> DEFORMERS =
				DeferredRegister.create(REGISTRY_KEY, PLibDatabase.MOD_ID);
		public static Registry<PMeshDeformer<?>> REGISTRY;

		public static final DeferredHolder<PMeshDeformer<?>, PMeshDeformer<PBendDefinition>> BEND =
				DEFORMERS.register("bend", () -> PBendDeformer.INSTANCE);
		public static final DeferredHolder<PMeshDeformer<?>, PMeshDeformer<PHingeDefinition>> HINGE =
				DEFORMERS.register("hinge", () -> PHingeDeformer.INSTANCE);
		public static final DeferredHolder<PMeshDeformer<?>, PMeshDeformer<PTwistDefinition>> TWIST =
				DEFORMERS.register("twist", () -> PTwistDeformer.INSTANCE);
		public static final DeferredHolder<PMeshDeformer<?>, PMeshDeformer<PStretchDefinition>> STRETCH =
				DEFORMERS.register("stretch", () -> PStretchDeformer.INSTANCE);
		public static final DeferredHolder<PMeshDeformer<?>, PMeshDeformer<PSquashDefinition>> SQUASH =
				DEFORMERS.register("squash", () -> PSquashDeformer.INSTANCE);
		public static final DeferredHolder<PMeshDeformer<?>, PMeshDeformer<PTaperDefinition>> TAPER =
				DEFORMERS.register("taper", () -> PTaperDeformer.INSTANCE);
		public static final DeferredHolder<PMeshDeformer<?>, PMeshDeformer<PWaveDefinition>> WAVE =
				DEFORMERS.register("wave", () -> PWaveDeformer.INSTANCE);

		private static void init(@NotNull final IEventBus bus)
		{
			REGISTRY = DEFORMERS.makeRegistry(builder -> builder.maxId(Integer.MAX_VALUE - 1).sync(false));
			DEFORMERS.register(bus);
		}
	}

	public static class BlockReg
	{
		public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(PLibDatabase.MOD_ID);
		
		public static final DeferredBlock<TestBlock> TEST_BLOCK = BLOCKS.register("test_block", () -> new TestBlock(
				BlockBehaviour.Properties.of().
						noOcclusion()));
		
		private static void init (@NotNull final IEventBus bus)
		{
			BLOCKS.register(bus);
		}
	}
	
	public static class BETypeReg
	{
		public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
				BuiltInRegistries.BLOCK_ENTITY_TYPE, PLibDatabase.MOD_ID);
	
		public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TestBlockEntity>> TEST_BLOCK_ENTITY = BLOCK_ENTITIES.register("test_block_entity", () ->
				new BlockEntityType<>(TestBlockEntity :: new, Set.of(BlockReg.TEST_BLOCK.get()), null));
		
		private static void init (@NotNull final IEventBus bus)
		{
			BLOCK_ENTITIES.register(bus);
		}
	}
	
	public static class ItemReg
	{
		public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PLibDatabase.MOD_ID);
		
		public static final DeferredItem<TestBlockItem> TEST_ITEM = ITEMS.register("test_block", identifier -> new TestBlockItem(
				BlockReg.TEST_BLOCK.get(),
				new Item.Properties()));
		
		/*public static final DeferredItem<TestArmor> TEST_CHESTPLATE = ITEMS.register("test_chest", identifier -> new TestArmor(
				ArmorMaterials.DIAMOND,
				ArmorItem.Type.CHESTPLATE,
				new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(33))));
		
		public static final DeferredItem<TestArmor> TEST_HAT = ITEMS.register("test_hat", identifier -> new TestArmor(
				ArmorMaterials.DIAMOND,
				ArmorItem.Type.HELMET,
				new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(33))));
		
		public static final DeferredItem<TestArmor> TEST_LEGGINGS = ITEMS.register("test_leggings", identifier -> new TestArmor(
				ArmorMaterials.DIAMOND,
				ArmorItem.Type.LEGGINGS,
				new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(33))));
		
		public static final DeferredItem<TestTailItem> TEST_TAIL = ITEMS.register("test_tail", identifier -> new TestTailItem(
				new Item.Properties().stacksTo(1)));*/
		
		private static void init (@NotNull final IEventBus bus)
		{
			ITEMS.register(bus);
		}
	}
	
	public static class EntityTypeReg
	{
		public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, PLibDatabase.MOD_ID);
		
		public static final DeferredHolder<EntityType<?>, EntityType<TestEntity>> TEST_ENTITY = ENTITIES.register("test_entity", key -> EntityType.Builder.of(TestEntity :: new, MobCategory.MISC).
				sized(1, 1).
				clientTrackingRange(5).
				build(key.toString()));
		
		private static void init (@NotNull final IEventBus bus)
		{
			ENTITIES.register(bus);
		}
	}
	
	public static void init(@NotNull final IEventBus bus)
	{
		AnimationChannelReg.init(bus);
		AnimationEventReg.init(bus);
		MeshDeformerReg.init(bus);
		
		//EntityTypeReg.init(bus);
		BlockReg.init(bus);
		BETypeReg.init(bus);
		ItemReg.init(bus);
	}
}
