/**
 * @author ArcAnc
 * Created at: 24.02.2026
 * Copyright (c) 2026
 * <p>
 * This code is licensed under "Arc's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package com.arcanc.arclib.content.model.animation;


import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

@FunctionalInterface
public interface ArcInterpolationType
{
	Map<String, ArcInterpolationType> INTERPOLATION_TYPES = new Object2ObjectOpenHashMap<>();
	
	ArcInterpolationType LINEAR = register("linear", ArcInterpolationType :: linear);
	ArcInterpolationType CATMULLROM = register("catmullrom", ArcInterpolationType :: catmullRom);
	ArcInterpolationType BEZIER = register("bezier", ArcInterpolationType :: bezier);
	ArcInterpolationType STEP = register("step", ArcInterpolationType :: step);
	
	double buildTransformer(double strength);
	
	static <T extends ArcInterpolationType> T register(String name, T interpolationType)
	{
		INTERPOLATION_TYPES.putIfAbsent(name, interpolationType);
		return interpolationType;
	}
	
	static double linear(double value)
	{
		return value;
	}
	
	static double step(double value)
	{
		return value < 1.0d ? 0.0d : 1.0d;
	}
	
	static double catmullRom(double value)
	{
		double t2 = value * value;
		double t3 = t2 * value;
		
		return 0.5d * (2 * value + (-1 + value) * t2 + (2 - value) * t3);
	}
	
	static double bezier(double value)
	{
		return bezier(value, 0.5d);
	}
	
	static double bezier(double value, double strength)
	{
		value = Math.max(0.0d, Math.min(1.0d, value));
		strength = Math.max(0.0d, Math.min(1.0d, strength));
		
		double u = 1.0d - value;
		
		double p0 = 0.0d;
		double p1 = strength;
		double p2 = 1.0d - strength;
		double p3 = 1.0d;
		
		return u * u * u * p0 +
				3.0d * u * u * value * p1 +
				3.0d * u * value * value * p2 +
				value * value * value * p3;
	}
}
