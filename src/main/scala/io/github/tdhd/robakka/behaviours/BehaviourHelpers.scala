
package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._

import scala.reflect.{ ClassTag, classTag }

object BehaviourHelpers {

  def possibleMoves() = {
    List(Agent.MoveUpLeft,
      Agent.MoveUp,
      Agent.MoveUpRight,
      Agent.MoveLeft,
      Agent.MoveRight,
      Agent.MoveDownLeft,
      Agent.MoveDown,
      Agent.MoveDownRight)
  }

  def getRandomMove() = scala.util.Random.shuffle(possibleMoves).head

  // returns all elements in l which have type T and returns a List[T]
  //  def getFromList[T: ClassTag](l: List[World.GameEntity]) = l.filter {
  //    case a: T => true
  //    case _ => false
  //  }.asInstanceOf[List[T]]
  //  def test(agent: World.AgentEntity, worldState: World.State) = {
  //    getFromList[World.AgentEntity](worldState.entities).filter {
  //      case a => true
  //    }
  //  }

  //  def linearizeWorld(self: World.AgentEntity, ws: World.StateContainer) = {
  //  }
  //  def linearizeWorld(self: World.AgentEntity, ws: World.State) = {
  //    val all = entities2MoveCommand[World.GameEntity](self, ws)
  //    val allMoves = possibleMoves
  //    val emptyEntities = List.fill(allMoves.size)("empty")
  //    // map every move to string
  //    emptyEntities.zip(allMoves).map {
  //      x =>
  //        val entity = all.filter { case (entity, moveCommand) => moveCommand == x._2 }.map {
  //          case (agent: World.AgentEntity, moveCommand) => ("agent", moveCommand)
  //          case (plant: World.PlantEntity, moveCommand) => ("plant", moveCommand)
  //        }.headOption
  //        val a = entity.getOrElse(x)
  //    }
  //  }

  /**
   * TODO: refactor! declare direction classes and wrap them in Move(direction: Direction)
   */
  def getDirection(self: World.AgentEntity, entity: World.GameEntity) = {
    (entity.position.row - self.position.row, entity.position.col - self.position.col) match {
      case (-1, -1) => Option((entity, Agent.MoveDownLeft))
      case (-1, 0) => Option((entity, Agent.MoveDown))
      case (-1, 1) => Option((entity, Agent.MoveDownRight))
      case (0, -1) => Option((entity, Agent.MoveLeft))
      case (0, 1) => Option((entity, Agent.MoveRight))
      case (1, -1) => Option((entity, Agent.MoveUpLeft))
      case (1, 0) => Option((entity, Agent.MoveUp))
      case (1, 1) => Option((entity, Agent.MoveUpRight))
      case _ => Option.empty[(World.GameEntity, Agent.MoveCommand)]
    }
  }

  /** can return empty list **/
  def getEntitiesByDirection(self: World.AgentEntity, ws: World.StateContainer, direction: Agent.MoveCommand) = {
    ws.world.flatMap {
      case ((row, col), entities) if ! entities.isEmpty => entities.map{getDirection(self, _)}
    }.toList.filter {
      case Some((entity, move)) => move == direction
      case _ => false
    }.flatten
  }
//  // should return an option of gameentity in the look direction
//  // none if nothing exists there
//  // some(..) if something is there
//  def look(self: World.AgentEntity, ws: World.StateContainer, look: Agent.MoveCommand) = {
//    ws.world.collect {
//      case ((row, col), entities) if ! entities.isEmpty => getDirection(self, entities.head)
//    }.flatten.filter {
//      case (entity, dir) => dir == look
//    }.headOption
//  }

//  def temp2(self: World.AgentEntity, ws: World.StateContainer) = {
//	var vec = List.empty[String]
//    val allMoves = possibleMoves()
//    // map of tuples (move, iterable of entities)
//    val t = allMoves.map {
//	  case move => (move, getEntitiesByDirection(self, ws, move))
//	}
//	t.foreach {
//	  case (Agent.MoveUp, entities) => entities.headOption.map(x => 1)
//	}
//    vec +:= "1"
//  }

//  def temp(self: World.AgentEntity, ws: World.StateContainer) = {
//	var vec = List.empty[String]
//    val allMoves = possibleMoves()
//    val t = allMoves.map {
//	  case move => look(self, ws, move)
//	}
//    vec +:= "1"
//  }

  //  // TODO: what about empty fields?
  //  def linearizeWorldState(self: World.AgentEntity, ws: World.State) = {
  //    entities2MoveCommand[World.GameEntity](self, ws).sortBy {
  //      case (_, Agent.MoveUpLeft) => 1
  //      case (_, Agent.MoveUp) => 2
  //      case (_, Agent.MoveUpRight) => 3
  //      case (_, Agent.MoveLeft) => 4
  //      case (_, Agent.MoveRight) => 5
  //      case (_, Agent.MoveDownLeft) => 6
  //      case (_, Agent.MoveDown) => 7
  //      case (_, Agent.MoveDownRight) => 8
  //    }
  //  }

  /**
   * WRITEME
   */
  def entities2MoveCommand(self: World.AgentEntity, ws: World.StateContainer) = {
    ws.world.flatMap {
      case (_, entities) => entities
    }.toList.map {
      case entity: World.GameEntity => getDirection(self, entity)
      case _ => Option.empty[(World.GameEntity, Agent.MoveCommand)]
    }.asInstanceOf[List[Option[(World.GameEntity, Agent.MoveCommand)]]].flatten
  }

  /**
   * receives a list of entities and returns a list of tuples:
   * (T, Agent.MoveCommand)
   * for every T, the move command is the command required to move to that entity from the current position
   */
  //  def entities2MoveCommand[T: ClassTag](self: World.AgentEntity, ws: World.State) = getFromList[T](ws.entities).map {
  //    case entity: World.GameEntity =>
  //      (entity.position.row - self.position.row, entity.position.col - self.position.col) match {
  //        case (-1, -1) => Option((entity, Agent.MoveDownLeft))
  //        case (-1, 0) => Option((entity, Agent.MoveDown))
  //        case (-1, 1) => Option((entity, Agent.MoveDownRight))
  //        case (0, -1) => Option((entity, Agent.MoveLeft))
  //        case (0, 1) => Option((entity, Agent.MoveRight))
  //        case (1, -1) => Option((entity, Agent.MoveUpLeft))
  //        case (1, 0) => Option((entity, Agent.MoveUp))
  //        case (1, 1) => Option((entity, Agent.MoveUpRight))
  //        case _ => Option.empty[(T, Agent.MoveCommand)]
  //      }
  //    case _ => Option.empty[(T, Agent.MoveCommand)]
  //  }.asInstanceOf[List[Option[(T, Agent.MoveCommand)]]].flatten

  /*
  // http://stackoverflow.com/questions/1094173/how-do-i-get-around-type-erasure-on-scala-or-why-cant-i-get-the-type-paramete
  import scala.reflect.{ ClassTag, classTag }
  // generic implementation
  def matchList2[T: ClassTag](list: List[T]) = list match {
    case strlist: List[String @unchecked] if classTag[T] == classTag[String] => println("A List of strings!")
    case intlist: List[Int @unchecked] if classTag[T] == classTag[Int] => println("A list of ints!")
  }
  def mapEntitiesToPosition[T: ClassTag](list: List[T]) = {
    list.filter {
      case agents: List[World.AgentEntity @unchecked] if classTag[T] == classTag[World.AgentEntity] => true
      case _ => false
    }
  }*/
  // type erasure fix
  //  import scala.reflect.{ ClassTag, classTag }
  //  abstract class A
  //  class B extends A
  //  class C extends A
  //  class D extends A
  //  class E extends A
  //  // returns all elements in l which have type T and returns a List[T]
  //  def getTypesFromList[T: ClassTag](l: List[A]) = l.filter {
  //    case a: T => true
  //    case _ => false
  //  }.asInstanceOf[List[T]]
  //  def mapEntitiesToPosition[T: ClassTag](l: List[T]) = l.map {
  //    case entity: T => entity
  //  }
  //  val a = mapEntitiesToPosition(getTypesFromList[B](List(new B, new C, new B)))
}
