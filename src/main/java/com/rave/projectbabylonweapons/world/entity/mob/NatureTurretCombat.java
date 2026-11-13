package com.rave.projectbabylonweapons.world.entity.mob;

import com.rave.projectbabylonmaterials.combat.MagicArmorCalculationHelper;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;

public final class NatureTurretCombat {
    private NatureTurretCombat() { }
    public static boolean enemy(Entity direct, LivingEntity owner, boolean owned, LivingEntity target) {
        if (target == direct || !target.isAlive() || !target.isAttackable() || target instanceof ArmorStand
                || target instanceof Player player && (player.isCreative() || player.isSpectator())) return false;
        if (owner == null) return !owned && target instanceof Enemy;
        if (target == owner || !owner.isAlive() || owner.level() != target.level()
                || target.isAlliedTo(owner) || DamageSources.isFriendlyFireBetween(owner, target)) return false;
        if (target instanceof OwnableEntity pet && owner.getUUID().equals(pet.getOwnerUUID())) return false;
        Entity actor = direct instanceof Projectile projectile && projectile.getOwner() != null ? projectile.getOwner() : direct;
        return target instanceof Enemy || target == owner.getLastHurtByMob() || target == owner.getLastHurtMob()
                || actor instanceof NatureTurretEntity turret && (target == turret.getLastHurtByMob() || target instanceof Mob mob && mob.getTarget() == turret)
                || target instanceof Mob mob && mob.getTarget() == owner;
    }
    public static boolean hurt(Entity direct, LivingEntity owner, boolean owned, LivingEntity target, float damage) {
        if (!enemy(direct, owner, owned, target) || !(damage > 0) || !Float.isFinite(damage)) return false;
        var school = SchoolRegistry.NATURE.get();
        float adjusted = MagicArmorCalculationHelper.applyAdjustedMagicDamage(target, damage, (float)school.getResistanceFor(target));
        var type = direct.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(school.getDamageType());
        Entity attacker = owner != null ? owner : direct instanceof Projectile projectile && projectile.getOwner() != null ? projectile.getOwner() : direct;
        return target.hurt(new DamageSource(type, direct, attacker), adjusted);
    }
}
