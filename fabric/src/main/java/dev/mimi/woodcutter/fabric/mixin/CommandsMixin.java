package dev.mimi.woodcutter.fabric.mixin;

import com.mojang.brigadier.CommandDispatcher;
import dev.mimi.woodcutter.fabric.WoodCutterFabricMod;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public abstract class CommandsMixin {
    @Shadow
    @Final
    private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void woodcutter$registerCommand(Commands.CommandSelection selection, CommandBuildContext context, CallbackInfo ci) {
        WoodCutterFabricMod mod = WoodCutterFabricMod.getInstance();
        if (mod != null) {
            mod.registerCommands(dispatcher);
        }
    }
}
