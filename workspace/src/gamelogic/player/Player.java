package gamelogic.player;

import java.awt.Color;
import java.awt.Graphics;
import java.sql.Time;

import gameengine.PhysicsObject;
import gameengine.graphics.MyGraphics;
import gameengine.hitbox.RectHitbox;
import gamelogic.Main;
import gamelogic.level.Level;
import gamelogic.tiles.Tile;

public class Player extends PhysicsObject{
	public float walkSpeed = 400;
	public float jumpPower = 1350;

	public float waterSpeed = walkSpeed/2;
	public float waterJump = (float)(jumpPower/1.25);

	private boolean isJumping = false;
	private boolean currentlyDashing = false;
	private boolean doubleJumped = false;
	private boolean letGoSpace = false;
	private boolean doubleJumpCooldown = false;
	private boolean canDash = true;
	private boolean swimming = false;

	private boolean longDashCooldown = false;
	private boolean setLongDashCooldown = false;
	private boolean holdingQ = false;
	private boolean pressedQ = false;
	private long holdingQTime;
	
	private double currentAlpha = 0;
	private float currentSpeed;
	private float goalSpeed;

	private long longDashTime;
	private long lastUpdatedAlpha = System.currentTimeMillis();
	//private boolean setAlphaFinite;

	private String lastDirection = "Right";

	private long currentDashCooldownTime;
	private long currentJumpCooldown;

	public Player(float x, float y, Level level) {
	
		super(x, y, level.getLevelData().getTileSize(), level.getLevelData().getTileSize(), level);
		int offset =(int)(level.getLevelData().getTileSize()*0.1); //hitbox is offset by 10% of the player size.
		this.hitbox = new RectHitbox(this, offset,offset, width -offset, height - offset);
	}

	
	static long GetCurrentTimeInSeconds() {
		return System.currentTimeMillis() / 1000;
	}

	public void setSwimmingState(boolean state) {
		swimming = state;
	}

	@Override
	public void update(float tslf) {
		super.update(tslf);
		
		//if (!setAlphaFinite) {
		//	lastUpdatedAlpha = System.currentTimeMillis();
		//}
		movementVector.x = 0;

		if (collisionMatrix[LEF] != null || collisionMatrix[RIG] != null && currentlyDashing) {
			currentlyDashing = false;
			movementVector.x = 0;
			currentAlpha = 0;
		}


		
		float tempSpeed = walkSpeed;
		float tempJump = jumpPower;

		if (swimming) {
			tempSpeed = waterSpeed;
			tempJump = waterJump;
		}

		if(PlayerInput.isLeftKeyDown()) {
			lastDirection = "Left";
			movementVector.x = -tempSpeed;
			//currentSpeed = Math.abs(currentSpeed);
			if (currentSpeed > 0) {
				currentlyDashing = false;
			}
		}

		if(PlayerInput.isRightKeyDown()) {
			lastDirection = "Right";
			movementVector.x = +tempSpeed;
			if (currentSpeed < 0) {
				currentlyDashing = false;
			}
			//currentSpeed = -Math.abs(currentSpeed);
		};

		// press Q, set boolean that you are holding Q.
		// let GO of Q, and boost forward.

		if (PlayerInput.IsKeyDown('Q')) {
			if (!holdingQ) {
				holdingQTime = System.currentTimeMillis();
			}

			holdingQ = true;
			
			/* if (!holdingQ) {
				pressedQ = false;
				holdingQ = true;
				holdingQTime = System.currentTimeMillis();
			} */

			//pressedQ = true;
		} else {

			if (!longDashCooldown && !currentlyDashing && canDash) {
				if (holdingQ) {
				setLongDashCooldown = true;
				canDash = false;
				longDashTime = System.currentTimeMillis();
					currentAlpha = 0;

					int speedCalculation = ((int) 1600) + ((int)(System.currentTimeMillis() - holdingQTime))/5;
					speedCalculation = (int) Math.clamp(speedCalculation, 1600,  1600*2);
					System.out.println(speedCalculation);

					currentlyDashing = true; // will get set when alpha reaches 1 in update

					currentSpeed =  speedCalculation;
					goalSpeed = 0;

					
				/*	if (movementVector.x < 0) {
						movementVector.x = -speedCalculation;
					} else {
						movementVector.x = speedCalculation;
					} */

					movementVector.y = -tempJump;

					//double no = this.linearInterpolation()
					holdingQTime = System.currentTimeMillis() - (long) 1000000;
					currentDashCooldownTime = System.currentTimeMillis();
					longDashCooldown = true;
				}
			}
			holdingQ = false;
		}

		if (!canDash && System.currentTimeMillis() - longDashTime > 5000) {
			canDash = true;
		}
		//if (PlayerInput.is)

		if(PlayerInput.isJumpKeyDown()) {
			if (!isJumping) {
			  movementVector.y = -tempJump;
			  isJumping = true;
			} else if (isJumping && !doubleJumped && letGoSpace && !doubleJumpCooldown) {
				doubleJumpCooldown = true;
				movementVector.y = -tempJump;
				doubleJumped = true;
				currentJumpCooldown = System.currentTimeMillis();
			}
		} else {
			letGoSpace = true;
		}


		// update a bar with this eq ( (5000 - (System.currentTimeMillis() - currentJumpCooldown)) / 5000);
		// return decimal 0-1 

		if (System.currentTimeMillis() - currentJumpCooldown >= 5000 && doubleJumpCooldown) {
			doubleJumpCooldown = false;
			System.out.println("Removed doublejump cooldown");
		}

		if (longDashCooldown) {
			longDashCooldown = false;
		}
		
	    if (currentlyDashing && (System.currentTimeMillis() - lastUpdatedAlpha > 10.67)) {
			lastUpdatedAlpha = System.currentTimeMillis();
			currentAlpha += ((double)  1/40);

			double useAlpha = Math.clamp(currentAlpha, 0, 1);
			double currentMovement = (currentSpeed + (goalSpeed - currentSpeed) * useAlpha);

			if (lastDirection == "Left"){
				movementVector.x = -((int) currentMovement);
			} else {
				movementVector.x = (int) currentMovement;
			};
			
			//System.out.println(movementVector.x);
			if (useAlpha >= 0.5) {
				movementVector.x = 0;
				currentlyDashing = false;
			}
		}

		if(collisionMatrix[BOT] != null) {
			isJumping = false;
			doubleJumped = false;
			letGoSpace = false;
	    }
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(Color.YELLOW);
		MyGraphics.fillRectWithOutline(g, (int)getX(), (int)getY(), width, height);

		if(Main.DEBUGGING) {
			for (int i = 0; i < closestMatrix.length; i++) {
				Tile t = closestMatrix[i];
				if(t != null) {
					g.setColor(Color.RED);
					g.drawRect((int)t.getX(), (int)t.getY(), t.getSize(), t.getSize());
				}
			}
		}
		
		hitbox.draw(g);
	}
}
