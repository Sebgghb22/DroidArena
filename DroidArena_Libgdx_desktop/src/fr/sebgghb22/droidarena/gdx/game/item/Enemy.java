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

import static fr.sebgghb22.droidarena.gdx.utils.Operations.distance;
import static fr.sebgghb22.droidarena.gdx.utils.Option.IASPEED;
import static fr.sebgghb22.droidarena.gdx.utils.Option.SCALING;
import static fr.sebgghb22.droidarena.gdx.utils.Option.TIMESEC;
import static fr.sebgghb22.droidarena.gdx.utils.Option.VIEWRADIUS;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.Random;

import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.ShapeType;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import com.badlogic.gdx.graphics.Texture;

import fr.sebgghb22.droidarena.gdx.game.arena.Arena;
import fr.sebgghb22.droidarena.gdx.game.arena.MyWorld;
import fr.sebgghb22.droidarena.gdx.utils.Option;

/**
 * The Class Enemy manage the enemy on the arena.
 */
public class Enemy extends Bloc {

	/** The arena. */
	private Arena arena;

	/** The speed of the enemy. */
	private int speed;

	/** The speed rotation of the enemy. */
	private float rotateSpeed = 0.1f;

	/** The angle of the move to turn ont the left */
	private int moveLeft = IASPEED;

	/** The angle of the move to turn on the right. */
	private int moveRight = IASPEED;

	/** The r. */
	private final Random r;

	private boolean active = true;

	private int timeSleeping = TIMESEC * 5;

	protected Texture tex;
	protected String img;

	/**
	 * Instantiates a new enemy.
	 * 
	 * @param p
	 *            the properties of the enemy
	 * @param x
	 *            the abscissa position of the robot
	 * @param y
	 *            the ordinate position of the robot
	 * @param arena
	 *            the arena
	 */
	public Enemy(Properties p, float x, float y, Arena arena) {
		super(p, x, y);
		this.arena = arena;
		r = new Random();
		this.img = p.getImg();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.umlv.urobot.items.Bloc#setBody(org.jbox2d.dynamics.Body)
	 */
	@Override
	public void setBody(Body createBody) {
		this.body = createBody;
		CircleShape shape = new CircleShape();
		shape.m_type = ShapeType.CIRCLE;
		shape.m_radius = Option.UNIT / 4 * SCALING;
		FixtureDef fd = new FixtureDef();
		fd.shape = shape;
		fd.density = p.getResistance();
		fd.friction = p.getResistance();
		this.body.createFixture(fd);
		this.body.setFixedRotation(p.isFixedRotation());
		this.body.setUserData(this);
		this.body.getFixtureList().setSensor(p.isSensor());
		this.body.setTransform(body.getWorldCenter(), r.nextInt(10) + r.nextFloat());
	}

	/**
	 * Use this method to increase the robot.
	 */
	protected void speedUP() {
		body.setLinearVelocity(getDirection());
	}

	/**
	 * Gets the direction of the robot.
	 * 
	 * @return the direction
	 */
	private Vec2 getDirection() {
		float angle = body.getAngle() - (float) PI / 2;
		return new Vec2((float) (cos(angle) * speed), (float) (sin(angle) * speed));
	}

	/**
	 * Use this function to rotate the robot on its right
	 */
	protected void right() {
		body.setTransform(body.getWorldCenter(), body.getAngle() + rotateSpeed);
	}

	/**
	 * Use this function to rotate the robot on its Left.
	 */
	protected void left() {
		body.setTransform(body.getWorldCenter(), body.getAngle() - rotateSpeed);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.umlv.urobot.items.Bloc#update()
	 */
	@Override
	public void update() {

		if (active) {
			World w = MyWorld.getWorld();
			float width = arena.getScreenSizeWidth() * SCALING;
			float height = arena.getScreenSizeHeight() * SCALING;
			final ArrayList<Fixture> fixtureList = new ArrayList<Fixture>();

			float radiusView = (float) (sqrt((width * width) + (height * height)) / VIEWRADIUS);
			AABB aabb = new AABB();
			aabb.lowerBound.set((getCenter().x * SCALING) - radiusView, getCenter().y * SCALING - radiusView);
			aabb.upperBound.set(getCenter().x * SCALING + radiusView, (getCenter().y * SCALING) + radiusView);

			w.queryAABB(new QueryCallback() {
				@Override
				public boolean reportFixture(Fixture arg0) {
					if (arg0 == null)
						return false;
					Bloc b = (Bloc) arg0.getBody().getUserData();
					if (b == null)
						return false;
					if (b.getProperties() != Properties.ENEMY && b.getProperties() != Properties.START && b.getProperties() != Properties.FINISH && b.getProperties() != Properties.BONUS)
						fixtureList.add(arg0);
					return true;
				}
			}, aabb);

			MyCallBack mcb = new MyCallBack();
			Bloc b = null;
			if (!fixtureList.isEmpty()) {
				for (Fixture fixture : fixtureList) {
					b = (Bloc) fixture.getBody().getUserData();
					if (b.getClass() == Robot.class) {
						Robot r = (Robot) b;
						if (!r.isAlive()) {
							randomMove();
						} else {
							w.raycast(mcb, this.body.getPosition(), fixture.getBody().getPosition());
						}
					}
					if (mcb.getEtat() == -1) {
						break;
					}
				}
			}

			if (mcb.getEtat() == 1) {
				followPlayer(mcb.getRobotPosition());
			} else {
				randomMove();
			}

		} else {
			if (timeSleeping > 0) {
				timeSleeping--;
			} else {
				active = true;
				timeSleeping = TIMESEC * 5;
			}
		}
	}

	/**
	 * Set a random direction to apply on the robot
	 */
	private void randomMove() {
		if (speed < 20) {
			speed++;
		}
		speedUP();

		int value = r.nextInt(2);

		switch (value) {
		case 0: {
			if (moveLeft == 0) {
				int timeMove = r.nextInt(15);
				for (int i = 0; i < timeMove; i++) {
					left();
				}
				moveLeft = IASPEED;
			}
			moveLeft--;
			break;
		}
		case 1:
			if (moveRight == 0) {
				int timeMove = r.nextInt(15);
				for (int i = 0; i < timeMove; i++) {
					right();
				}
				moveRight = IASPEED;
			}
			moveRight--;
			break;
		}

	}

	/**
	 * This method can apply a new direction on the enemy to follow the player.
	 * 
	 * @param direction
	 *            position of the player robot
	 */
	private void followPlayer(Vec2 direction) {
		Vec2 enemyPos = this.getBody().getPosition();
		Vec2 robotPos = direction;

		float enemyAngle = 0;
		if ((robotPos.x - enemyPos.x) > 0 && (robotPos.y - enemyPos.y) > 0) {
			enemyAngle = (float) (PI - sin((robotPos.x - enemyPos.x) / distance(enemyPos, robotPos)));
		}
		if ((enemyPos.x - robotPos.x) > 0 && (robotPos.y - enemyPos.y) > 0) {
			enemyAngle = (float) (PI - sin((robotPos.x - enemyPos.x) / distance(enemyPos, robotPos)));
		}
		if ((robotPos.x - enemyPos.x) > 0 && (enemyPos.y - robotPos.y) > 0) {
			enemyAngle = (float) (sin((robotPos.x - enemyPos.x) / distance(enemyPos, robotPos)));
		}
		if ((enemyPos.x - robotPos.x) > 0 && (enemyPos.y - robotPos.y) > 0) {
			enemyAngle = (float) (sin((robotPos.x - enemyPos.x) / distance(enemyPos, robotPos)));
		}

		body.setTransform(body.getWorldCenter(), (enemyAngle));
		if (speed < 30) {
			speed++;
		}
		speedUP();
	}

	public void disable() {
		active = false;
		img = Sprite.ENEMYOFF.getImg();
	}

	@Override
	public Properties getProperties() {
		return p;
	}

	@Override
	public float getAngle() {
		return 0;
	}

	public boolean isActive() {
		return active;
	}
}
