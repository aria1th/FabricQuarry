package net.quarrymod.client.gui;

import net.quarrymod.QuarryMod;
import net.quarrymod.block.QuarryBlock.DisplayState;
import net.quarrymod.blockentity.machine.tier3.QuarryBlockEntity;
import net.quarrymod.config.QMConfig;
import net.quarrymod.packets.QMServerboundPackets;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import reborncore.client.gui.builder.GuiBase;
import reborncore.client.gui.builder.widget.GuiButtonExtended;
import reborncore.client.gui.guibuilder.GuiBuilder;
import reborncore.client.screen.builder.BuiltScreenHandler;
import reborncore.common.network.NetworkManager;

public class GuiQuarry extends GuiBase<BuiltScreenHandler> {

	public static final Identifier defaultTextureSheet = new Identifier(QuarryMod.MOD_ID, "textures/gui/guielements.png");

	private final QuarryBlockEntity blockEntity;
	private GuiButtonExtended mineAllButton;
	private GuiButtonExtended mineOresButton;

	public GuiQuarry(int syncID, final PlayerEntity player, final QuarryBlockEntity blockEntity) {
		super(player, blockEntity, blockEntity.createScreenHandler(syncID, player));
		this.blockEntity = blockEntity;
	}

	@Override
	public void init() {
		super.init();
		mineAllButton = addButton(new GuiButtonExtended(x + 29, y + 39, 54, 20, new TranslatableText("gui.quarrymod.quarry.mine_all"), (ButtonWidget buttonWidget) -> changeMineAll(false)));
		mineOresButton = addButton(new GuiButtonExtended(x + 29, y + 39, 54, 20, new TranslatableText("gui.quarrymod.quarry.mine_ores"), (ButtonWidget buttonWidget) -> changeMineAll(true)));
		mineAllButton.visible = false;
		mineOresButton.visible = false;
	}

	@Override
	protected void drawBackground(MatrixStack matrixStack, final float f, final int mouseX, final int mouseY) {
		super.drawBackground(matrixStack, f, mouseX, mouseY);
		final Layer layer = Layer.BACKGROUND;

		drawSlot(matrixStack, 8, 72, layer);

		drawSlot(matrixStack, 30, 20, layer);
		drawSlot(matrixStack, 48, 20, layer);
		drawSlot(matrixStack, 66, 20, layer);
		drawSlot(matrixStack, 84, 20, layer);

		drawSlot(matrixStack, 121, 20, layer);
		drawSlot(matrixStack, 139, 20, layer);

		// upgrades
		getMinecraft().getTextureManager().bindTexture(defaultTextureSheet);
		drawTexture(matrixStack, x - 48, y + 24, 0, 0, 27, 46);

		drawOutputSlotBar(matrixStack, 54, 65, 5, layer);
	}

	@Override
	protected void drawForeground(MatrixStack matrixStack, final int mouseX, final int mouseY) {
		final Layer layer = Layer.FOREGROUND;
		final DisplayState displayState = blockEntity.getDisplayState();

		mineAllButton.visible = blockEntity.getMineAll() && QMConfig.quarryAccessibleExcavationModes >= 3;
		mineOresButton.visible = !blockEntity.getMineAll() && QMConfig.quarryAccessibleExcavationModes >= 3;

		

		if (displayState != DisplayState.Off && displayState != DisplayState.Mining) {
			getMinecraft().getTextureManager().bindTexture(defaultTextureSheet);
			if (displayState == DisplayState.Error)
				drawTexture(matrixStack, 86, 42, 28, 0, 15, 16);
			else
				drawTexture(matrixStack, 86, 42, 44, 0, 15, 15);
		}

		if (blockEntity.getMineAll()) 
			builder.drawDefaultBackground(matrixStack, this, 28, 25, 80, 6);
		
		super.drawForeground(matrixStack, mouseX, mouseY);

		builder.drawProgressBar(matrixStack, this, blockEntity.getProgressScaled(100), 100, 33, 65, mouseX, mouseY, GuiBuilder.ProgressDirection.UP, layer);
		builder.drawMultiEnergyBar(matrixStack, this, 9, 19, (int) blockEntity.getEnergy(), (int) blockEntity.getMaxStoredPower(), mouseX, mouseY, 0, layer);
	}

	public void changeMineAll(boolean mineAll) {
		NetworkManager.sendToServer(QMServerboundPackets.createPacketQuarryMineAll(blockEntity, mineAll));
	}

	@Override
	protected void drawMouseoverTooltip(MatrixStack matrixStack, int mouseX, int mouseY) {
		final DisplayState displayState = blockEntity.getDisplayState();

		if (isPointWithinBounds(28, 18, 80, 19, mouseX, mouseY)
		&& this.focusedSlot != null
		&& !this.focusedSlot.hasStack())
			renderTooltip(matrixStack, new TranslatableText("gui.quarrymod.quarry.filler_blocks"), mouseX, mouseY);

		if (isPointWithinBounds(118, 18, 38, 19, mouseX, mouseY)
		&& this.focusedSlot != null
		&& !this.focusedSlot.hasStack())
			renderTooltip(matrixStack, new TranslatableText("gui.quarrymod.quarry.drill_tubes"), mouseX, mouseY);

		if (isPointWithinBounds(-42, 30, 19, 38, mouseX, mouseY)
		&& this.focusedSlot != null
		&& !this.focusedSlot.hasStack())
			renderTooltip(matrixStack, new TranslatableText("gui.quarrymod.quarry.drill_upgrades"), mouseX, mouseY);

		if (isPointWithinBounds(86, 42, 15, 16, mouseX, mouseY)
		&& displayState != DisplayState.Off && displayState != DisplayState.Mining)
			renderTooltip(matrixStack, 
			new TranslatableText("gui.quarrymod.quarry.state_" + blockEntity.getStateName().toLowerCase()).formatted(displayState.getFormating()), 
			mouseX, mouseY);

		super.drawMouseoverTooltip(matrixStack, mouseX, mouseY);
	}
}
