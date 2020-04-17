package com.github.terminatornl.tickcentral;

import com.github.terminatornl.tickcentral.api.TickInterceptor;
import com.github.terminatornl.tickcentral.api.TickHub;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.Random;

public class Verification extends Block implements ITickable {

	public Verification() {
		super(Material.CAKE);
	}

	@Override
	public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
		AbortServer();
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		AbortServer();
	}

	@Override
	public void update() {
		AbortServer();
	}

	private static void AbortServer(){
		TickCentral.LOGGER.info("Unable to transform the code properly. Something is interfering with this mod.");
		FMLCommonHandler.instance().exitJava(1, false);
	}

	public static void EarlyCheck(){
		TickCentral.LOGGER.info("Testing if transformations have been applied...");
		TickInterceptor origionalExecutor = TickHub.INTERCEPTOR;
		TickHub.INTERCEPTOR = new TickInterceptor() {
			@Override
			public void redirectUpdateTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random random) {
				TickCentral.LOGGER.info("Normal block updates: OK");
			}

			@Override
			public void redirectRandomTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random random) {
				TickCentral.LOGGER.info("Random block updates: OK");
			}

			@Override
			public void redirectUpdate(ITickable tickable) {
				TickCentral.LOGGER.info("ITickable updates: OK");
			}
		};
		Block verification = new Verification();
		verification.updateTick(null, null, null, null);
		verification.randomTick(null, null, null, null);
		((ITickable) verification).update();
		TickHub.INTERCEPTOR = origionalExecutor;
		TickCentral.LOGGER.info("Success!");
	}
}
