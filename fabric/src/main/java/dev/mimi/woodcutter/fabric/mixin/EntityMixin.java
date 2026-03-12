package dev.mimi.woodcutter.fabric.mixin;

import dev.mimi.woodcutter.fabric.WoodCutterFabricMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "move", at = @At("TAIL"))
    private void woodcutter$afterMove(MoverType moverType, Vec3 movement, CallbackInfo ci) {
        WoodCutterFabricMod mod = WoodCutterFabricMod.getInstance();
        if (mod != null) {
            mod.handleEntityMoved((Entity) (Object) this);
        }
    }
}
