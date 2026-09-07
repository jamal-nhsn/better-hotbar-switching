package frigidoven.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.client.input.InputDevice;
import net.minecraft.client.option.GameSettings;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.core.player.inventory.container.ContainerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import static net.minecraft.client.option.GameSettings.KEY_HOT_BAR_SWITCH;


@Mixin(Minecraft.class)
public abstract class HandleSwitchHotbarMixin {

	@Shadow public PlayerLocal thePlayer;
	@Shadow public int hotbarSwapAnimationProgress;

	@WrapWithCondition(
		method = "runTick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/core/player/inventory/container/ContainerInventory;changeCurrentSlot(I)V"
		)
	)
	private boolean allowHotbarScroll(ContainerInventory inventory, int offset) {
		if (KEY_HOT_BAR_SWITCH.isPressed()) {
			if (!thePlayer.inventory.currentSlotLocked()) {
				int sign = Integer.signum(offset);
				thePlayer.setHotbarOffset((thePlayer.inventory.getHotbarOffset() + (sign < 0 ? 9 : 27)) % 36);
				hotbarSwapAnimationProgress -= sign * 3;
				if (Math.abs(hotbarSwapAnimationProgress) > 6) {
					hotbarSwapAnimationProgress = hotbarSwapAnimationProgress < 0 ? -6 : 6;
				}
				thePlayer.inventory.changeCurrentSlot(0);
			}
			return false;
		}
		return true;
	}

	@WrapOperation(
		method = "checkBoundInputs",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/option/KeyBinding;isPressEvent(Lnet/minecraft/client/input/InputDevice;)Z"
		)
	)
	private boolean disableHotbarSwitchPressed(KeyBinding keyBinding, InputDevice eventInputDevice, Operation<Boolean> original) {
		return keyBinding != KEY_HOT_BAR_SWITCH && original.call(keyBinding, eventInputDevice);
	}
}
