package no.lau.domain

import collection.mutable.HashMap
import no.lau.domain.movement.{StackableMovement, Squeezable, Movable}

/**
 * BoardSize X and Y start from 0 to make computation easier to write :)
 */
case class Game(boardSizeX: Int, boardSizeY: Int) {
  val rnd = new scala.util.Random

  var gameBoards:List[HashMap[Tuple2[Int, Int], GamePiece]] = List(new HashMap[Tuple2[Int, Int], GamePiece])

  def currentGameBoard():HashMap[Tuple2[Int, Int], GamePiece] = gameBoards.first

  def newTurn() = {
    gameBoards = cloneCurrent :: gameBoards
    for(gamePiece <- currentGameBoard.values) {
      gamePiece match {
        case stackable: StackableMovement => {
          stackable match {
            case movable: Movable => {
              stackable.movementStack.firstOption match {
                case Some(direction) => {
                  movable.move(direction)
                  stackable.movementStack = stackable.movementStack.tail
                }
                case None =>
              }
            }
          }
        }
        case _ => println("Check that nothing bad happens here!")
      }
    }
    //For all stackable pieces - check if they have a movement scheduled
    //Check if the movement collides with other movements, in which case, roll back both movements
    //Remove a movement from the stack of the stackableMovement

    currentGameBoard
  }

  private def cloneCurrent = currentGameBoard.clone.asInstanceOf[HashMap[Tuple2[Int, Int], GamePiece]]

  /**
   * Simple algorithm for scattering out different objects.
   * Can be increasingly time consuming when nr of free cells -> 0
   * Got any better ways of doing this? :)
   */
  def findRandomFreeCell(): Tuple2[Int, Int] = {
    val randomCell = (rnd.nextInt((0 to boardSizeX) length), rnd.nextInt((0 to boardSizeY) length))
    if (currentGameBoard get(randomCell) isEmpty)
      randomCell
    else
      findRandomFreeCell
  }

  def addRandomly(gamePiece: GamePiece) = currentGameBoard += findRandomFreeCell() -> gamePiece

  //Algorithm can take some time when nr of free cells --> 0
  def whereIs(gamePiece: GamePiece): Tuple2[Int, Int] = {
    val foundItAt: Int = currentGameBoard.values.indexOf(gamePiece)
    currentGameBoard.keySet.toArray(foundItAt)
  }

  def printableBoard() = {
    val table = for (y <- 0 to boardSizeY)
    yield {
        val row = for (x <- 0 to boardSizeX)
        yield currentGameBoard.getOrElse((x, boardSizeY - y), ".")
        row.foldLeft("")(_ + _) + "\n"
      }
    table.foldLeft("")(_ + _)
  }
}

trait GamePiece

//case class Player(name: String, game: Game) extends Movable with Squeezable {override def toString = name.substring(0, 1)} //First letter in name

case class Monster(game: Game, id: Any) extends Movable with Squeezable with StackableMovement {
  override def toString = "H"
}

//Todo blocks should need no identity. When one is moved, it is essentially deleted, and replaced by a new. If possible :) Or has an autogenerated id
case class Block(game: Game, id: Any) extends Movable {override def toString = "B"}

case class StaticWall() extends GamePiece {override def toString = "W"}

case class IllegalMoveException(message: String) extends Throwable

package movement {

trait StackableMovement extends Movable {
  var movementStack:List[Direction] = List()
  def stackMovement(dir:Direction) { movementStack = dir :: movementStack }
}

trait Movable extends GamePiece {
  val game: Game //todo game should preferably be referenced some other way

  /**
   * Used for moving gamepieces around the gameBoard
   * If the route ends up in an illegal move at one stage, the movement will be dropped and an IllegalMovementException will be thrown
   * todo should probably return new location
   **/

  def move(inThatDirection: Direction) {
    val oldLocation = game.whereIs(this)
    val newLocation = tryToMove(inThatDirection)
    move(oldLocation, newLocation)
  }


  def tryToMove(inThatDirection: Direction):Tuple2[Int, Int] = {
    val oldLocation = game.whereIs(this)

    val newLocation = (oldLocation._1 + inThatDirection.dir._1, oldLocation._2 + inThatDirection.dir._2)

    if (isOverBorder(newLocation))
      throw IllegalMoveException("Move caused movable to travel across the border")

    //Is this the correct way to do this?
    game.currentGameBoard.getOrElse(newLocation, None) match {
      case squeezable: Squeezable => {
        val wasSqueezed = try {squeezable.tryToMove(inThatDirection); false}
        catch {
          case ime: IllegalMoveException => squeezable kill; true
        }
        if(!wasSqueezed) throw IllegalMoveException("Nothing to be squeezed against")
      }
      case movable: Movable => movable.move(inThatDirection)
      case gamePiece: GamePiece => throw IllegalMoveException("Trying to move unmovable Gamepiece")
      case None =>
    }
    newLocation
  }

  private def isOverBorder(newLocation: Tuple2[Int, Int]) = newLocation._1 > game.boardSizeX || newLocation._1 < 0 || newLocation._2 > game.boardSizeY || newLocation._2 < 0

  private def move(oldLocation: Tuple2[Int, Int], newLocation: Tuple2[Int, Int]) {
    game.currentGameBoard -= oldLocation
    game.currentGameBoard += newLocation -> this
  }

  def whereAreYou = game.whereIs(this)
}

/**
 * Marks that a gamePiece can be squeezed
 */
trait Squeezable extends Movable {
  var isKilled = false
  def kill() {isKilled = true}
}

// Direction enum should preferably also provide a matrix to indicate that Up is (+1, +0), which could mean that Move didn't have to include the pattern matching.
object Direction extends Enumeration {
  val Up, Down, Right, Left = Value
}

sealed abstract class Direction(val dir: Tuple2[Int, Int])
case object Up extends Direction(0, 1)
case object Down extends Direction(0, -1)
case object Right extends Direction(1, 0)
case object Left extends Direction(-1, 0)
}
