package net.ssanj.scout

object Thread {
  final case class Id(value: Long)
  final case class Count(value: Int)

  sealed trait State extends Product with Serializable
  
  object State {
    case object New extends State
    case object Runnable extends State
    case object Blocked extends State
    case object Waiting extends State
    case object TimedWaiting extends State
    case object Terminated extends State
  }

  sealed trait Priority extends Product with Serializable
  
  object Priority {
    case object Min extends Priority
    case object Normal extends Priority
    case object Max extends Priority
    final case class Other(value: Int) extends Priority
  }

  sealed trait IsAlive extends Product with Serializable
  
  object IsAlive {
    case object Alive extends IsAlive
    case object Dead extends IsAlive
  }

  sealed trait IsDaemon extends Product with Serializable
  
  object IsDaemon {
    case object Daemon extends IsDaemon
    case object UI extends IsDaemon
  }

  sealed trait IsInterrupted extends Product with Serializable
  
  object IsInterrupted {
    case object Interrupted extends IsInterrupted
    case object NotInterrupted extends IsInterrupted
  }

  sealed trait IsDestroyed extends Product with Serializable
  
  object IsDestroyed {
    case object Destroyed extends IsDestroyed
    case object NotDestroyed extends IsDestroyed
  }

  final case class Attributes(alive: IsAlive, daemon: IsDaemon, interrupted: IsInterrupted)

  sealed trait Group extends Product with Serializable
  
  object Group {
    case object System extends Group
    final case class SubGroup(name: String, activeCount: Count, maxPriority: Priority, daemon: IsDaemon, destroyed: IsDestroyed, parentName: String) extends Group
  }

  final case class ClassName(value: String)
  final case class FileName(value: String)
  final case class MethodName(value: String)
  final case class LineNumber(value: Int)

  final case class StackElementInfo(className: Option[ClassName], fileName: FileName, methodName: MethodName, lineNumber: Option[LineNumber])

  final case class Info(id: Id, 
                        name: String, 
                        className: ClassName,
                        priority: Priority, 
                        state: State,
                        group: Group,
                        attributes: Attributes,
                        stackTraces: List[StackElementInfo]) 
}