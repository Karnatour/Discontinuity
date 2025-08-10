package me.pepperbell.continuity.client.processor;


import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public abstract class ConnectingQuadProcessor extends AbstractQuadProcessor {
	protected ConnectionPredicate connectionPredicate;
	protected boolean innerSeams;

	public ConnectingQuadProcessor(TextureAtlasSprite[] sprites, ProcessingPredicate processingPredicate, ConnectionPredicate connectionPredicate, boolean innerSeams) {
		super(sprites, processingPredicate);
		this.connectionPredicate = connectionPredicate;
		this.innerSeams = innerSeams;
	}
}
