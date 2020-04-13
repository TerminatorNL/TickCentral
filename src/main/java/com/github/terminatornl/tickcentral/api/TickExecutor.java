package com.github.terminatornl.tickcentral.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChorusPlant;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public interface TickExecutor {

	default void redirectRandomTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random random){
		block.randomTick(worldIn, pos, state, random);
	}

	default void redirectUpdateTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random random){
		block.updateTick(worldIn, pos, state, random);
	}

	default void update(ITickable tickable){
		tickable.update();
	}

}
