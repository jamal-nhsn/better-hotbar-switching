package frigidoven.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.PlayerLocal;
import net.minecraft.client.input.InputDevice;
import net.minecraft.client.option.GameSettings;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class HandleSwitchHotbarMixin {
	@Shadow public PlayerLocal thePlayer;
	@Shadow public int hotbarSwapAnimationProgress;

	@Unique private int mLastMouseDWheel = 0;
	@Unique private int mLastSlot = 0;

	@Unique private boolean processedScroll = false;

	@Inject(
		method = "runTick",
		at = @At("HEAD")
	)
	private void updateLocals(CallbackInfo ci) {
		// Modding brings the worst out of us all...
		processedScroll = false;
		mLastMouseDWheel = Mouse.getDWheel();
		if (thePlayer != null ) mLastSlot = thePlayer.inventory.getCurrentSlot();
	}

   @Inject(
		method = "checkBoundInputs",
		at = @At("HEAD"),
		cancellable = true
	)
	private void handleHotbarSwitchAlternative(InputDevice currentInputDevice, CallbackInfoReturnable<Boolean> cir) {
		if (GameSettings.KEY_HOT_BAR_SWITCH.isPressed()) {
			int scrollDelta = Mouse.getDWheel() - mLastMouseDWheel;
			if (scrollDelta != 0) {
				thePlayer.inventory.setCurrentSlot(mLastSlot, false);
				if (!processedScroll) {
					scrollDelta = Integer.signum(scrollDelta);
					boolean deltaIsNegative = scrollDelta < 0;

					int currentHotbarOffset = thePlayer.inventory.getHotbarOffset();
					int newHotbarOffset = (currentHotbarOffset + (deltaIsNegative ? 9 : 27)) % 36;
					thePlayer.setHotbarOffset(newHotbarOffset);

					hotbarSwapAnimationProgress -= 3 * scrollDelta;
					if (Math.abs(hotbarSwapAnimationProgress) > 6) {
						hotbarSwapAnimationProgress = hotbarSwapAnimationProgress < 0 ? -6 : 6;
					}
					processedScroll = true;
				}
				thePlayer.inventory.changeCurrentSlot(0);
			}
			cir.setReturnValue(true);
		}
	}
}
