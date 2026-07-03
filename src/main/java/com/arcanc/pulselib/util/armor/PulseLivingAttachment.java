/**
 * @author ArcAnc
 * Created at: 04.07.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.pulselib.util.armor;


import com.arcanc.pulselib.util.PRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.function.Function;

public class PulseLivingAttachment
{
	private final PulseAttachmentAnchor anchor;
	private final String pulseBone;
	private final Function<ResourceLocation, RenderType> renderType;
	private final Vector3f offset;
	private final Quaternionf rotation;
	private final Vector3f scale;
	private final int color;
	
	public PulseLivingAttachment(PulseAttachmentAnchor anchor, String pulseBone)
	{
		this(anchor, pulseBone, PRenderTypes.RenderTypeProvider :: trianglesLit);
	}
	
	public PulseLivingAttachment(PulseAttachmentAnchor anchor, String pulseBone, Function<ResourceLocation, RenderType> renderType)
	{
		this(anchor, pulseBone, renderType, new Vector3f(), defaultQuaternion(), defaultScale(), -1);
	}
	
	public PulseLivingAttachment(PulseAttachmentAnchor anchor, String pulseBone, int color)
	{
		this(anchor, pulseBone, PRenderTypes.RenderTypeProvider :: trianglesLit, color);
	}
	
	public PulseLivingAttachment(PulseAttachmentAnchor anchor, String pulseBone, Function<ResourceLocation, RenderType> renderType, int color)
	{
		this(anchor, pulseBone, renderType, new Vector3f(), defaultQuaternion(), defaultScale(), color);
	}
	
	public PulseLivingAttachment(PulseAttachmentAnchor anchor,
	                             String pulseBone,
	                             Function<ResourceLocation, RenderType> renderType,
	                             Vector3f offset,
	                             int color)
	{
		this(anchor, pulseBone, renderType, offset, defaultQuaternion(), defaultScale(), color);
	}
	
	public PulseLivingAttachment(PulseAttachmentAnchor anchor,
	                             String pulseBone,
	                             Function<ResourceLocation, RenderType> renderType,
	                             Vector3f offset,
	                             Vector3f rotation,
	                             Vector3f scale,
	                             int color)
	{
		this(anchor, pulseBone, renderType, offset, eulerDegreesToQuaternion(rotation), scale, color);
	}
	
	private PulseLivingAttachment(PulseAttachmentAnchor anchor,
	                              String pulseBone,
	                              Function<ResourceLocation, RenderType> renderType,
	                              Vector3f offset,
	                              Quaternionf rotation,
	                              Vector3f scale,
	                              int color)
	{
		this.anchor = Objects.requireNonNull(anchor);
		this.pulseBone = Objects.requireNonNull(pulseBone);
		this.renderType = Objects.requireNonNull(renderType);
		this.offset = new Vector3f(Objects.requireNonNull(offset));
		this.rotation = new Quaternionf(Objects.requireNonNull(rotation));
		this.scale = new Vector3f(Objects.requireNonNull(scale));
		this.color = color;
	}
	
	protected PulseLivingAttachment(Builder<?> builder)
	{
		this(builder.anchor, builder.pulseBone, builder.renderType, builder.offset, builder.rotation, builder.scale, builder.color);
	}
	
	public static Builder<?> builder(PulseAttachmentAnchor anchor, String pulseBone)
	{
		return new Builder<>(anchor, pulseBone);
	}
	
	public PulseAttachmentAnchor anchor()
	{
		return this.anchor;
	}
	
	public String pulseBone()
	{
		return this.pulseBone;
	}
	
	public Function<ResourceLocation, RenderType> renderType()
	{
		return this.renderType;
	}
	
	public Vector3f offset()
	{
		return new Vector3f(this.offset);
	}
	
	public Quaternionf rotation()
	{
		return new Quaternionf(this.rotation);
	}
	
	public Vector3f scale()
	{
		return new Vector3f(this.scale);
	}
	
	public int color()
	{
		return this.color;
	}
	
	private static Quaternionf defaultQuaternion()
	{
		return new Quaternionf();
	}
	
	private static Vector3f defaultScale()
	{
		return new Vector3f(1, 1, 1);
	}
	
	public static class Builder<T extends Builder<T>>
	{
		protected final PulseAttachmentAnchor anchor;
		protected final String pulseBone;
		protected Function<ResourceLocation, RenderType> renderType = PRenderTypes.RenderTypeProvider :: trianglesLit;
		protected Vector3f offset = new Vector3f();
		protected Quaternionf rotation = new Quaternionf();
		protected Vector3f scale = new Vector3f(1, 1, 1);
		protected int color = -1;
		
		protected Builder(PulseAttachmentAnchor anchor, String pulseBone)
		{
			this.anchor = Objects.requireNonNull(anchor);
			this.pulseBone = Objects.requireNonNull(pulseBone);
		}
		
		public T renderType(Function<ResourceLocation, RenderType> renderType)
		{
			this.renderType = Objects.requireNonNull(renderType);
			return self();
		}
		
		public T offset(Vector3f offset)
		{
			this.offset = new Vector3f(Objects.requireNonNull(offset));
			return self();
		}
		
		public T offset(float x, float y, float z)
		{
			return this.offset(new Vector3f(x, y, z));
		}
		
		public T rotation(Vector3f rotation)
		{
			this.rotation = eulerDegreesToQuaternion(rotation);
			return self();
		}
		
		public T rotation(float x, float y, float z)
		{
			return this.rotation(new Vector3f(x, y, z));
		}
		
		public T scale(Vector3f scale)
		{
			this.scale = new Vector3f(Objects.requireNonNull(scale));
			return self();
		}
		
		public T scale(float scale)
		{
			return this.scale(scale, scale, scale);
		}
		
		public T scale(float x, float y, float z)
		{
			return this.scale(new Vector3f(x, y, z));
		}
		
		public T color(int color)
		{
			this.color = color;
			return self();
		}
		
		public PulseLivingAttachment build()
		{
			return new PulseLivingAttachment(this);
		}
		
		@SuppressWarnings("unchecked")
		protected T self()
		{
			return (T)this;
		}
	}
	
	private static Quaternionf eulerDegreesToQuaternion(Vector3f rotation)
	{
		Objects.requireNonNull(rotation);
		return new Quaternionf().rotationXYZ(
				(float)Math.toRadians(rotation.x()),
				(float)Math.toRadians(rotation.y()),
				(float)Math.toRadians(rotation.z()));
	}
}
