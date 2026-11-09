package com.guilhermegoes.folclore.criatura;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/** A creature at home both in the water and on the shore (the Iara and the Boto). */
public abstract class Anfibia extends Assombracao {
	protected Anfibia(EntityType<? extends Anfibia> type, Level level) {
		super(type, level);
		setPathfindingMalus(PathType.WATER, 0.0F);
		moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.1F, 0.5F, false);
		lookControl = new SmoothSwimmingLookControl(this, 20);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new AmphibiousPathNavigation(this, level);
	}

	@Override
	protected void travelInWater(Vec3 input, double baseGravity, boolean isFalling, double oldY) {
		moveRelative(getSpeed(), input);
		move(MoverType.SELF, getDeltaMovement());
		setDeltaMovement(getDeltaMovement().scale(0.9));
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}
}
