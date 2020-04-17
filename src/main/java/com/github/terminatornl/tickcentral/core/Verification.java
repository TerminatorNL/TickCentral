package com.github.terminatornl.tickcentral.core;

import com.github.terminatornl.tickcentral.TickCentral;
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

	public boolean KILL_MODE = true;
	public boolean ACTIVATED = false;

	public Verification() {
		super(Material.CAKE);
	}

	@Override
	public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
		ACTIVATED = true;
		if(KILL_MODE){
			AbortServer();
		}
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		ACTIVATED = true;
		if(KILL_MODE){
			AbortServer();
		}
	}

	@Override
	public void update() {
		ACTIVATED = true;
		if(KILL_MODE){
			AbortServer();
		}
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
				TickCentral.LOGGER.info("Normal block updates: Redirection-OK");
			}

			@Override
			public void redirectRandomTick(Block block, World worldIn, BlockPos pos, IBlockState state, Random random) {
				TickCentral.LOGGER.info("Random block updates: Redirection-OK");
			}

			@Override
			public void redirectUpdate(ITickable tickable) {
				TickCentral.LOGGER.info("ITickable updates: Redirection-OK");
			}
		};
		Verification verification = new Verification();
		verification.updateTick(null, null, null, null);
		verification.randomTick(null, null, null, null);
		verification.update();
		verification.KILL_MODE = false;
		verification.ACTIVATED = false;
		try{
			TickHub.trueUpdateTick(verification, null, null, null, null);
			TickCentral.LOGGER.info("Normal block updates: TrueTick-OK");
		}catch (NoSuchMethodError e){
			TickCentral.LOGGER.info("Normal block updates: TrueTick-NOT OKAY!", e);
			AbortServer();
		}
		if(verification.ACTIVATED == false){
			TickCentral.LOGGER.info("Normal block updates: TrueTick-NOT FIRED");
		}
		verification.ACTIVATED = false;
		try{
			TickHub.trueRandomTick(verification, null, null, null, null);
			TickCentral.LOGGER.info("Random block updates: TrueTick-OK");
		}catch (NoSuchMethodError e){
			TickCentral.LOGGER.info("Random block updates: TrueTick-NOT OKAY!", e);
			AbortServer();
		}
		if(verification.ACTIVATED == false){
			TickCentral.LOGGER.info("Random block updates: TrueTick-NOT FIRED");
		}
		verification.ACTIVATED = false;
		try{
			TickHub.trueUpdate(verification);
			TickCentral.LOGGER.info("ITickable updates: TrueTick-OK");
		}catch (NoSuchMethodError e){
			TickCentral.LOGGER.info("ITickable updates: TrueTick-NOT OKAY!", e);
			AbortServer();
		}
		if(verification.ACTIVATED == false){
			TickCentral.LOGGER.info("ITickable updates: TrueTick-NOT FIRED");
		}
		verification.ACTIVATED = false;
		TickHub.INTERCEPTOR = origionalExecutor;
		TickCentral.LOGGER.info("Success!");
	}
}
