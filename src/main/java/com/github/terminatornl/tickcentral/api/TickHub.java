package com.github.terminatornl.tickcentral.api;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class TickHub {

	/**
	 * This field is ment to be changed by other mods that implement the TickExecutor.
	 */
	public static TickInterceptor INTERCEPTOR = new TickInterceptor(){};

	/**
	 * Will be populated with the true update method using ASM
	 * @param block the block
	 * @param worldIn the world
	 * @param pos the block pos
	 * @param state the state of the block
	 * @param random random
	 */
	public static void trueRandomTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random random){
		block.randomTick(worldIn, pos, state, random);
	}

	/**
	 * Will be populated with the true update method using ASM
	 * @param block the block
	 * @param worldIn the world
	 * @param pos the block pos
	 * @param state the state of the block
	 * @param random random
	 */
	public static void trueUpdateTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random random){
		block.updateTick(worldIn,pos,state,random);
	}

	/**
	 * Will be populated with the true update method using ASM
	 * @param tickable the tickable
	 */
	public static void trueUpdate(ITickable tickable){
		tickable.update();
	}
}
