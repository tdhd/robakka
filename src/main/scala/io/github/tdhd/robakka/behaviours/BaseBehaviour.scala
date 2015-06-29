package io.github.tdhd.robakka.behaviours

import io.github.tdhd.robakka._

import scala.reflect.{ ClassTag, classTag }

object BehaviourHelpers {

//  // returns all elements in l which have type T and returns a List[T]
//  def getFromList[T: ClassTag](l: List[World.GameEntity]) = l.filter {
//    case a: T => true
//    case _ => false
//  }.asInstanceOf[List[T]]
//  def test(agent: World.AgentEntity, worldState: World.State) = {
//    getFromList[World.AgentEntity](worldState.entities).filter {
//      case a => true
//    }
//  }
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

  def getEnemies(agent: World.AgentEntity, worldState: World.State) = {
    worldState.entities.filter {
      case World.AgentEntity(World.Location(row, col), id, team, health, ref, world) =>
        id != agent.agentId && team != agent.team
      case _ => false
    }
  }.asInstanceOf[List[World.AgentEntity]]

  def getPlants(worldState: World.State) = {
    worldState.entities.filter {
      case World.PlantEntity(World.Location(row, col)) => true
      case _ => false
    }
  }.asInstanceOf[List[World.PlantEntity]]
}

// act returns a set of commands: move, attack, defend
trait BaseBehaviour {
  def act(entity: World.AgentEntity, worldState: World.State): Agent.CommandSet
}

