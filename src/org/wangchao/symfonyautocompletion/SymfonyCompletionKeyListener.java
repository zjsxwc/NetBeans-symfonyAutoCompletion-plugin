/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wangchao.symfonyautocompletion;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
/**
 *
 * @author wangchao
 */
public class SymfonyCompletionKeyListener implements KeyListener {
    public boolean shiftKeyPressing = false;

    @Override
    public void keyTyped(KeyEvent e) {
        return;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SHIFT:
                shiftKeyPressing = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SHIFT:
                shiftKeyPressing = false;
                break;
        }
    }
}
