package no.lau.domain

import no.lau.domain.movement._

import javax.swing.{JComponent, KeyStroke}
import java.awt.Font

import scala.swing._

/**
 * Created by IntelliJ IDEA.
 * User: beiske
 * Date: Oct 19, 2009
 * Time: 3:27:26 PM
 * To change this template use File | Settings | File Templates.
 */

object UI extends SimpleGUIApplication {
   val game = Game(40, 25)
   val rnd = new scala.util.Random
   1 to 100 foreach {arg => game.addRandomly(Block(game, "a" + rnd.nextInt()))}
   val monsterGunnar = new Monster(game, "MonsterGunnar")
   game.addRandomly(monsterGunnar)
   val board = new TextArea(){
     editable = false
     text = game printableBoard
     
     val inputMap = peer.getInputMap(JComponent.WHEN_FOCUSED)
     inputMap.put(KeyStroke.getKeyStroke("UP"), "up")
     inputMap.put(KeyStroke.getKeyStroke("DOWN"), "down")
     inputMap.put(KeyStroke.getKeyStroke("LEFT"), "left")
     inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "right")
     inputMap.put(KeyStroke.getKeyStroke("typed w"), "up")
     inputMap.put(KeyStroke.getKeyStroke("typed s"), "down")
     inputMap.put(KeyStroke.getKeyStroke("typed a"), "left")
     inputMap.put(KeyStroke.getKeyStroke("typed d"), "right")
     
     val actionMap = peer.getActionMap()
     actionMap.put("up", Action("KeyPressed"){monsterGunnar.move(Up); text = game printableBoard}.peer)
     actionMap.put("down", Action("KeyPressed"){monsterGunnar.move(Down); text = game printableBoard}.peer)
     actionMap.put("left", Action("KeyPressed"){monsterGunnar.move(Left); text = game printableBoard}.peer)
     actionMap.put("right", Action("KeyPressed"){monsterGunnar.move(Right); text = game printableBoard}.peer)
     
     peer.setFont(new Font(Font.MONOSPACED, peer.getFont().getStyle(), 24));
     
   }
   
   def top = new MainFrame {
     contents = new BorderPanel {
       import BorderPanel.Position._
       add(board, Center)
     }
   }
   
   def refresh() = board text = game printableBoard
}  
  
