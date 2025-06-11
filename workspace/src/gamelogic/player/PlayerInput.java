/*
 * 
 */
package gamelogic.player;

import java.awt.event.KeyEvent;
import java.awt.event.FocusEvent.Cause;

import gameengine.input.KeyboardInputManager;

/**
 * 
 * @author Paul
 *
 */
class PlayerInput {
	/**
	 * 
	 * @return true if the walk-up-key is down
	 */
	public static boolean isJumpKeyDown() {
		return KeyboardInputManager.isKeyDown(KeyEvent.VK_SPACE);
	}
	/**
	 * 
	 * @return true if the walk-left-key is down
	 */
	public static boolean isLeftKeyDown() {
		return KeyboardInputManager.isKeyDown(KeyEvent.VK_A) || KeyboardInputManager.isKeyDown(KeyEvent.VK_LEFT);
	}
	
	//public static KeyEvent()

	public static boolean IsKeyDown(char keyDown) {
		switch (keyDown) {
			case 'A': return KeyboardInputManager.isKeyDown(KeyEvent.VK_A);
			case 'Q': return KeyboardInputManager.isKeyDown(KeyEvent.VK_Q);

			default: return false;
		}
	}
	/**
	 * 
	 * @return true if the walk-right-key is down
	 */
	public static boolean isRightKeyDown() {
		return KeyboardInputManager.isKeyDown(KeyEvent.VK_D) || KeyboardInputManager.isKeyDown(KeyEvent.VK_RIGHT);
	}
}
