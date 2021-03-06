/*
 * Project : JAVA: YouRobot
 * ESIPE-MLV
 * 
 * VERNEAU Julien
 * ANTOINE Sébastien
 * 
 * 
 * IR2 - DECEMBER 2011
 * 
 */
package fr.sebgghb22.droidarena.gdx.game.item;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;

import com.badlogic.gdx.graphics.Texture;

/**
 * The Interface Item permit to instantiate an item in the arena.
 */
public interface Item {
	/**
	 * Gets the screen position.
	 *
	 * @return the screen position
	 */
	public Vec2 getScreenPosition();
	 
	/**
	 * Gets the body.
	 *
	 * @return the {@link Body} of the item
	 */
	public Body getBody();

	/**
	 * Gets the body def.
	 *
	 * @return the {@link BodyDef} of the item
	 */
	public BodyDef getBodyDef();

	/**
	 * Sets the body of the item.
	 *
	 * @param createBody the new body
	 */
	public void setBody(Body createBody);

	/**
	 * Gets the angle of the item.
	 *
	 * @return the angle
	 */
	public float getAngle();

	/**
	 * Gets the center of the item.
	 *
	 * @return the center
	 */
	public Vec2 getCenter();

	/**
	 * This function permit to refresh the item.
	 * At each iteration this function calcul some position, attribute 
	 * over all the item on the arena.
	 */
	public void update();
	
	public String getImg();
	
	public void setTexture(Texture t);
	
	public Texture getTexture();
	
	public Properties getProperties();
}
