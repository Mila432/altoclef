package adris.altoclef.mixins;

import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
//#if MC >= 12111
import org.spongepowered.asm.mixin.gen.Invoker;
//#else
//$$ import org.spongepowered.asm.mixin.gen.Accessor;
//#endif

@Mixin(PersistentProjectileEntity.class)
public interface PersistentProjectileEntityAccessor {

    //#if MC >= 12111
    @Invoker("isInGround")
    boolean isInGround();
    //#else
    //$$ @Accessor("inGround")
    //$$ boolean isInGround();
    //#endif
}
