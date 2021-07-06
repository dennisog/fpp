package fpp.compiler.codegen

import fpp.compiler.analysis._
import fpp.compiler.ast._
import fpp.compiler.util._

/** Writes out C++ for topology private functions */
case class TopPrivateFunctions(
  s: CppWriterState,
  aNode: Ast.Annotated[AstNode[Ast.DefTopology]]
) extends TopologyCppWriterUtils(s, aNode) {

  def getLines: List[Line] = addBannerComment(
    "Private functions",
    List(
      getInitComponentsLines,
      getConfigComponentsLines,
      getSetBaseIdsLines,
      getConnectComponentsLines,
      getRegCommandsLines,
      getLoadParametersLines,
      getStartTasksLines,
      getStopTasksLines,
      getFreeThreadsLines,
      getTearDownComponentsLines,
    ).flatten
  )

  private def getInitComponentsLines: List[Line] = {
    def getCode(ci: ComponentInstance): List[Line] = {
      val name = getNameAsIdent(ci.qualifiedName)
      getCodeLinesForPhase (CppWriter.Phases.initComponents) (ci).getOrElse(
        ci.component.aNode._2.data.kind match {
          case Ast.ComponentKind.Passive => 
            lines(s"$name.init(InstanceIDs::$name);")
          case _ =>
            lines(s"$name.init(QueueSizes::$name, InstanceIDs::$name);")
        }
      )
    }
    addComment(
      "Initialize components",
      wrapInScope(
        "void initComponents(const TopologyState& state) {",
        instances.flatMap(getCode),
        "}"
      )
    )
  }

  private def getConfigComponentsLines: List[Line] = {
    def getCode(ci: ComponentInstance): List[Line] = {
      val name = getNameAsIdent(ci.qualifiedName)
      getCodeLinesForPhase (CppWriter.Phases.configComponents) (ci).getOrElse(Nil)
    }
    addComment(
      "Configure components",
      wrapInScope(
        "void configComponents(const TopologyState& state) {",
        instances.flatMap(getCode),
        "}"
      )
    )
  }

  private def getSetBaseIdsLines: List[Line] =
    addComment(
      "Set component base IDs",
      wrapInScope(
        "void setBaseIds() {",
        instances.map(ci => {
          val name = getNameAsIdent(ci.qualifiedName)
          val id = CppWriter.writeId(ci.baseId)
          line(s"$name.setidBase($id);")
        }),
        "}"
      )
    )

  private def getConnectComponentsLines: List[Line] = {
    def getPortInfo(pii: PortInstanceIdentifier, c: Connection) = {
      val instanceName = getNameAsIdent(pii.componentInstance.qualifiedName)
      val portName = pii.portInstance.getUnqualifiedName
      val portNumber = t.getPortNumber(pii.portInstance, c).get
      (instanceName, portName, portNumber)
    }
    def writeConnection(c: Connection) = {
      val out = getPortInfo(c.from.port, c)
      val in = getPortInfo(c.to.port, c)
      wrapInScope(
        s"${out._1}.set_${out._2}_OutputPort(",
        List(
          s"${out._3},",
          s"${in._1}_get_${in._2}_InputPort(${in._3})"
        ).map(line),
        ");"
      )
    }
    addComment(
      "Connect components",
      wrapInScope(
        "void connectComponents() {",
        addBlankPostfix(
          t.connectionMap.toList.flatMap { 
            case (name, cs) => addComment(name, cs.flatMap(writeConnection))
          }
        ),
        "}"
      )
    )
  }

  private def getRegCommandsLines: List[Line] = {
    def getCode(ci: ComponentInstance): List[Line] = {
      getCodeLinesForPhase (CppWriter.Phases.regCommands) (ci).getOrElse(
        if (hasCommands(ci)) {
          val name = getNameAsIdent(ci.qualifiedName)
          lines(s"$name.regCommands();")
        }
        else Nil
      )
    }
    addComment(
      "Register commands",
      wrapInScope(
        "void regCommands() {",
        instances.flatMap(getCode),
        "}"
      )
    )
  }

  private def getLoadParametersLines: List[Line] = {
    def getCode(ci: ComponentInstance): List[Line] = {
      getCodeLinesForPhase (CppWriter.Phases.loadParameters) (ci).getOrElse(
        if (hasParams(ci)) {
          val name = getNameAsIdent(ci.qualifiedName)
          lines(s"$name.loadParameters();")
        }
        else Nil
      )
    }
    addComment(
      "Load parameters",
      wrapInScope(
        "void loadParameters() {",
        instances.flatMap(getCode),
        "}"
      )
    )
  }

  private def getStartTasksLines: List[Line] = {
    def getCode(ci: ComponentInstance): List[Line] =
      getCodeLinesForPhase (CppWriter.Phases.startTasks) (ci).getOrElse {
        if (isActive(ci)) {
          val name = getNameAsIdent(ci.qualifiedName)
          wrapInScope(
            s"$name.start(",
            List(
              s"TaskIDs::$name,",
              s"Priorities::$name,",
              s"StackSizes::$name"
            ).map(line),
            ");"
          )
        }
        else Nil
      }
    addComment(
      "Start tasks",
      wrapInScope(
        "void startTasks(const TopologyState& state) {",
        instances.flatMap(getCode),
        "}"
      )
    )
  }

  private def getStopTasksLines: List[Line] = {
    def getCode(ci: ComponentInstance): List[Line] =
      getCodeLinesForPhase (CppWriter.Phases.stopTasks) (ci).getOrElse {
        if (isActive(ci)) {
          val name = getNameAsIdent(ci.qualifiedName)
          lines(s"$name.exit();")
        }
        else Nil
      }
    addComment(
      "Stop tasks",
      wrapInScope(
        "void stopTasks(const TopologyState& state) {",
        instances.flatMap(getCode),
        "}"
      )
    )
  }

  private def getFreeThreadsLines: List[Line] = {
    def getCode(ci: ComponentInstance): List[Line] =
      getCodeLinesForPhase (CppWriter.Phases.freeThreads) (ci).getOrElse {
        if (isActive(ci)) {
          val name = getNameAsIdent(ci.qualifiedName)
          lines(s"$name.ActiveComponentBase::join(NULL);")
        }
        else Nil
      }
    addComment(
      "Free threads",
      wrapInScope(
        "void freeThreads(const TopologyState& state) {",
        instances.flatMap(getCode),
        "}"
      )
    )
  }

  private def getTearDownComponentsLines: List[Line] = {
    def getCode(ci: ComponentInstance): List[Line] = {
      val name = getNameAsIdent(ci.qualifiedName)
      getCodeLinesForPhase (CppWriter.Phases.tearDownComponents) (ci).getOrElse(Nil)
    }
    addComment(
      "Tear down components",
      wrapInScope(
        "void tearDownComponents(const TopologyState& state) {",
        instances.flatMap(getCode),
        "}"
      )
    )
  }

}
