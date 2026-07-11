package spigey.asteroide.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import spigey.asteroide.modules.BlockHitboxesModule;
import spigey.asteroide.modules.PassthroughModule;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockOutlineMixin {
    @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true)
    private void enlargeOutline(BlockGetter world, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
        try {
            BlockHitboxesModule bh = Modules.get().get(BlockHitboxesModule.class);
            PassthroughModule pt = Modules.get().get(PassthroughModule.class);
            if(pt.isActive()) if(pt.blocks.get().contains(((BlockState) (Object) this).getBlock())) cir.setReturnValue(Shapes.empty());
            if(bh.isActive()) if (bh.blocks.get().contains(((BlockState) (Object) this).getBlock())) cir.setReturnValue(Shapes.box(bh.miX.get(), bh.miY.get(), bh.miZ.get(), bh.maX.get(), bh.maY.get(), bh.maZ.get()));
        }catch(Exception e){/**/}
    }

    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    private void enlargeOutline2(BlockGetter world, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        try {
            BlockHitboxesModule bh = Modules.get().get(BlockHitboxesModule.class);
            PassthroughModule pt = Modules.get().get(PassthroughModule.class);
            if(pt.isActive()) if(pt.blocks.get().contains(((BlockState) (Object) this).getBlock())) cir.setReturnValue(Shapes.empty());
            if(bh.isActive()) if (bh.blocks.get().contains(((BlockState) (Object) this).getBlock())) cir.setReturnValue(Shapes.box(bh.miX.get(), bh.miY.get(), bh.miZ.get(), bh.maX.get(), bh.maY.get(), bh.maZ.get()));
        }catch(Exception e){/**/}
    }
}
