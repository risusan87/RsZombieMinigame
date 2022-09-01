package jp.risu87.hzp.entity.ai;

import net.minecraft.server.v1_12_R1.EntityInsentient;
import net.minecraft.server.v1_12_R1.EntityLiving;
import net.minecraft.server.v1_12_R1.IEntitySelector;
import net.minecraft.server.v1_12_R1.PathfinderGoal;

import com.google.common.base.Predicates;

public class AILookAtFarPlayer extends PathfinderGoal {
	
	protected EntityInsentient looker;

	protected EntityLiving b;

	protected float c;

	public AILookAtFarPlayer(EntityInsentient aiHolder) {
		this.looker = aiHolder;
		this.c = 256.f;
		a(0);
	}
	
	/**
	 * shouldAIActivated
	 */
	public boolean a() {
		if (this.looker.getGoalTarget() == null) {
			this.b = this.looker.world.a(this.looker.locX, this.looker.locY, this.looker.locZ, this.c, Predicates.and(IEntitySelector.e, IEntitySelector.b(this.looker)));
			this.looker.setGoalTarget(this.b);
		} else {
			this.b = this.looker.getGoalTarget();
		}
		
		return true;
	}
	
	/**
	 * shouldAIProcessContinue
	 */
	public boolean b() {
		if (!this.b.isAlive())
			return false; 
		if (this.looker.h(this.b) > (this.c * this.c))
			return false;
		else 
			return true;
	}
	
	/**
	 * onAIActivate
	 */
	public void c() {
	}
	
	/**
	 * onAIDeactivate
	 */
	public void d() {
		this.b = null;
	}
	
	/**
	 * onAIUpdate
	 */
	public void e() {
		this.looker.getControllerLook().a(this.b.locX, this.b.locY + this.b.getHeadHeight(), this.b.locZ, 10, 40);
	}
}
