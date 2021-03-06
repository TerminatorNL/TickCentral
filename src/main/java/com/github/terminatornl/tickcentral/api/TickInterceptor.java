package com.github.terminatornl.tickcentral.api;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

@SuppressWarnings("unused")
public interface TickInterceptor {

	default void redirectUpdateTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random random){
		TickHub.trueUpdateTick(block, worldIn, pos, state, random);
	}

	default void redirectRandomTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random random){
		TickHub.trueRandomTick(block, worldIn, pos, state, random);
	}

	default void redirectUpdate(ITickable tickable){
		TickHub.trueUpdate(tickable);
	}

	default void redirectOnUpdate(Entity entity){
		TickHub.trueOnUpdate(entity);
	}

}
