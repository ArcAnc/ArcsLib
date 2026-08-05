package com.arcanc.pulselib.content.model.animation;

import java.util.Objects;

@FunctionalInterface
public interface PCondition
{
	PCondition ALWAYS = parameters -> true;
	PCondition NEVER = parameters -> false;

	boolean test(PAnimationParameters parameters);
	
	default void consume(PAnimationParameters parameters)
	{
	}

	static PCondition parameter(String name)
	{
		return parameters -> parameters.getBoolean(name);
	}

	static PCondition greaterThan(String name, float value)
	{
		return parameters -> parameters.get(name) > value;
	}

	static PCondition greaterOrEqual(String name, float value)
	{
		return parameters -> parameters.get(name) >= value;
	}

	static PCondition lessThan(String name, float value)
	{
		return parameters -> parameters.get(name) < value;
	}

	static PCondition triggered(String name)
	{
		Objects.requireNonNull(name);
		return new PCondition()
		{
			@Override
			public boolean test(PAnimationParameters parameters)
			{
				return parameters.isTriggered(name);
			}

			@Override
			public void consume(PAnimationParameters parameters)
			{
				parameters.consumeTrigger(name);
			}
		};
	}
}
